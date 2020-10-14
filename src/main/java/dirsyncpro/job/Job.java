/*
 * Directory.java
 * 
 * Copyright (C) 2008-2016 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2003-2006, 2008 F. Gerbig
 * Copyright (C) 2002 E. Gerber  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */

package dirsyncpro.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Vector;

import javax.annotation.PostConstruct;

import dirsyncpro.Const;
import dirsyncpro.Const.CompareMode;
import dirsyncpro.Const.IconKey;
import dirsyncpro.Const.SymLinkMode;
import dirsyncpro.Const.SyncConflictResolutionMode;
import dirsyncpro.Const.SyncMode;
import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.DirSyncPro;
import dirsyncpro.exceptions.IncompleteConfigurationException;
import dirsyncpro.exceptions.WarningException;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPattern;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterSet;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleEngine;
import dirsyncpro.sync.RealtimeListener;
import dirsyncpro.sync.Sync;
import dirsyncpro.sync.Sync.SyncError;
import dirsyncpro.sync.SyncPair;
import dirsyncpro.tools.DateTool;
import dirsyncpro.tools.FileTools;
import dirsyncpro.tools.Log;
import dirsyncpro.tools.TextFormatTool;
import dirsyncpro.tools.WildcardTools;

/**
 * Represents a directory (source and destination) to be synchronized. Contains
 * the source and destination path to sync, whether the sync will be done
 * recursive, which file extensions to fileInclude and fileExclude, if and where
 * to log what's being done and how the synchronization should be done.
 * 
 * @author E. Gerber, F. Gerbig, O. Givi (info@dirsyncpro.org)
 */

public class Job extends JobObject implements Cloneable {
	/**
	 * Initializes a new Directory.
	 */
	private Job() {
		init();
		this.setEnabled(true);
	}

	/**
	 * Initializes a new Directory.
	 * @param enabled The enabled state of the directory.
	 */
	public Job(boolean enabled) {
		this();
		this.setEnabled(enabled);
	}

	public String toString(){
		return getName();
	}
	
	/**
	 * Initializes the Directory
	 */
	public void init(){
		this.setLog(new Log("", this)); //dummy log. default log is used instead initially.
		if (this.getSyncMode() == null){
			this.setSyncMode(Const.SyncMode.ABMirror);
		}else{
			//make sure all settings are ok; 
			this.setSyncMode(this.getSyncMode());
		}
		this.setCompareMode(Const.COMPARE_MODE_DEFAULT);
		this.setRecursive(true);
		this.setVerify(false);
		this.setSyncRealtime(false);
		this.setSyncRealtimeOnStart(true);
		this.setHowManyBackups(0);
		this.setBackupInline(true);
		this.setSymLinkMode(SymLinkMode.SkipSymLinks);
		this.setWriteTimestampBack(false);
		this.setSyncDirTimeStamps(false);
		this.setIgnoreDaylightSavingGranularity(false);
		this.setGranularity(0);
		this.setPreserveDOSAttributes(false);
		this.setPreservePOSIXFileOwnership(false);
		this.setPreservePOSIXFilePermissions(false);
		this.schedules = new Vector<Schedule>();
	}
	
	/**
	 * @param syncMode the syncMode to set
	 */
	public void setSyncMode(Const.SyncMode syncMode) {
		this.syncMode = syncMode;
		switch (syncMode){
			case ABMirror:
			case BAMirror:
				this.setCopyAll(false);
				this.setCopyNew(true);
				this.setCopyModified(true);
				this.setCopyLargerModified(false);
				this.setDelDirs(true);
				this.setDelFiles(true);
				this.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.CopySource);
				this.initFilters();
				break;
			case ABCustom:
			case BACustom:
			case BICustom:
				break;
			case BIMirror:
				this.setCopyAll(false);
				this.setCopyNew(true);
				this.setCopyModified(true);
				this.setCopyLargerModified(false);
				this.setDelDirs(true);
				this.setDelFiles(true);
				this.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.WarnUser);
				this.initFilters();
				break;
			case ABFull:
			case BAFull:
				this.setCopyAll(true);
				this.setCopyNew(false);
				this.setCopyModified(false);
				this.setCopyLargerModified(false);
				this.setDelDirs(true);
				this.setDelFiles(true);
				this.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.CopySource);
				this.initFilters();
				break;
			case ABContribute:
			case BAContribute:
				this.setCopyAll(false);
				this.setCopyNew(true);
				this.setCopyModified(false);
				this.setCopyLargerModified(false);
				this.setDelDirs(false);
				this.setDelFiles(false);
				this.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.Skip);
				this.initFilters();
				break;
		}
		this.setDeleteExcludedDirsA(false);
		this.setDeleteExcludedFilesA(false);
		this.setDeleteExcludedDirsB(false);
		this.setDeleteExcludedFilesB(false);
		this.setCompareMode(Const.COMPARE_MODE_DEFAULT);
		this.setPreserveDOSAttributes(false);
		this.setPreservePOSIXFilePermissions(false);
		this.setPreservePOSIXFileOwnership(false);
		this.setOverrideReadOnly(false);
	}
	
	private void initFilters(){
		this.filterSet = new FilterSet();
		this.filterSet.addFilter(new FilterByPattern(this, Filter.Action.Include, Const.DEFAULT_INCLUDE_FILE, FilterByPattern.FilterPatternType.File, false));
		this.filterSet.addFilter(new FilterByPattern(this, Filter.Action.Include, Const.DEFAULT_INCLUDE_DIR, FilterByPattern.FilterPatternType.Directory, false));
	}
	
	public void resetFilters(){
		this.filterSet = new FilterSet();
	}
	
	/**
	 * Changes the path of the given file from the sorce path to the destination
	 * path.
	 * 
	 * @param srcFile
	 *            The file in the source directory.
	 * @param srcPath
	 *            The source path.
	 * @param dstPath
	 *            The destination path.
	 * @return File The file with the destination path.
	 */
	private static Path replacePath(Path srcFile, Path srcPath, Path dstPath) {

		// get actual path
		String path = srcFile.toAbsolutePath().toString();
		// delete source path from actual path
		String cut = path.substring(srcPath.toAbsolutePath().toString().length(), path.length());

		if (!cut.startsWith(File.separator)) {
			cut = File.separator + cut;
		}
		// add destination path with remaining actual path
		String newPath = dstPath.toAbsolutePath().toString() + cut;

		return new File(newPath).toPath();
	}

	/**
	 * Changes the path of the given files from the sorce path to the
	 * destination path.
	 * 
	 * @param srcFiles
	 *            The files in the source directory.
	 * @param srcPath
	 *            The source path.
	 * @param dstPath
	 *            The destination path.
	 * @return File[] The files with the destination path.
	 */
	private static ArrayList<Path> replacePath(ArrayList<Path> srcFiles, Path srcPath, Path dstPath) {

		ArrayList<Path> dstFiles = new ArrayList<Path>();

		// for all files
		for (int i = 0; i < srcFiles.size(); i++) {
			dstFiles.add(replacePath(srcFiles.get(i), srcPath, dstPath));
		}

		return dstFiles;
	}



	private class DSPFileVisitor extends SimpleFileVisitor<Path> {
		FilterSet filters;
		boolean ab;
		int depth;
		boolean deletionAnalysis;

		public DSPFileVisitor(FilterSet filters, boolean ab) {
			super();
			this.filters = filters;
			this.ab = ab;
			this.depth = -1;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (DirSyncPro.getSync().isStopping()) {
				return FileVisitResult.TERMINATE;
			}
			if (filters.matchesAnyIncludeFilter(file)) {
				if (filters.matchesAnyExcludeFilter(file)) {
					analyzeFilePair(file, attr, ab, deletionAnalysis, true);
				} else {
					analyzeFilePair(file, attr, ab, deletionAnalysis, false);
					DirSyncPro.getSync().getSyncQ().addTotalAnalyzedFiles(1);
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
			depth++;
			if (DirSyncPro.getSync().isStopping()) {
				return FileVisitResult.TERMINATE;
			}
			if (depth == 0){
				//skip the root folders (src/dst)
				 return FileVisitResult.CONTINUE;
			}else{
				if (filters.matchesAnyIncludeFilter(dir)) {
					if (filters.matchesAnyExcludeFilter(dir)) {
						analyzeFilePair(dir, attr, ab, deletionAnalysis, true);
						return FileVisitResult.SKIP_SUBTREE;
					} else {
						analyzeFilePair(dir, attr, ab, deletionAnalysis, false);
						DirSyncPro.getSync().getSyncQ().addTotalAnalyzedDirs(1);
						return FileVisitResult.CONTINUE;
					}
				}else{
					return FileVisitResult.SKIP_SUBTREE;
				}
			}
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException{
			if (exc != null){
				getLog().printMinimal("Unable to analyze: " + dir.toAbsolutePath() + " (" + exc.getMessage() + ")", IconKey.Error);
			}
			return FileVisitResult.CONTINUE;
		}
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException{
			if (exc != null){
				getLog().printMinimal("Unable to analyze: " + file.toAbsolutePath() + " (" + exc.getMessage() + ")", IconKey.Error);
			}
			return FileVisitResult.CONTINUE;
		}

		public void setDeletionAnalysis(boolean deletionAnalysis) {
			this.deletionAnalysis = deletionAnalysis;
		}
		
		public void resetDepth(){
			this.depth = -1;
		}
	}
	
	
	private SyncPair analyzeSyncPairStatus(File fA, File fB, BasicFileAttributes attr, boolean ab, boolean deletionAnalysis, boolean excluded){
		//attr is the atributes of the file/dir @source
		//attr already contains the file attriutes, so not reading them again will give us some performance boost
		long fADate = 0;
		long fBDate = 0;
		boolean fAEx = true; //will change otherwise if exception happens
		boolean fBEx = true; //will change otherwise if exception happens
		boolean fAIsDir = false;
		boolean fBIsDir = false;
		boolean fAIsFile = false;
		boolean fBIsFile = false;
		boolean fAIsSymLink = false;
		boolean fBIsSymLink = false;
		long fASize = 0;
		long fBSize = 0;
		
		boolean attrBelongsToA = (ab && !deletionAnalysis) || (!ab && deletionAnalysis);
		boolean attrBelongstoB = (!ab && !deletionAnalysis) || (ab && deletionAnalysis);

		// reading BasicFileAttribute should be run first because they can imply if there could be an I/O Error thus file not existing
		BasicFileAttributes attrA = null;
		if (attrBelongsToA){
			attrA = attr;
		}else{
			try{
				attrA = Files.readAttributes(fA.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			}catch (FileNotFoundException | NoSuchFileException e){
				fAEx= false;
			}catch (IOException e){
				getLog().printMinimal("I/O Error: " + e.getMessage(), IconKey.Error);
				return null;
			}
		}
		fADate = (fAEx ? attrA.lastModifiedTime().toMillis() : 0);
		fAIsDir = (fAEx ? attrA.isDirectory() : false);
		fAIsFile = fAEx && !fAIsDir;
		fASize = (fAEx ? attrA.size(): 0);
		fAIsSymLink = (fAEx ? attrA.isSymbolicLink() : false);

		BasicFileAttributes attrB = null;
		if (attrBelongstoB){
			attrB = attr;
		}else{
			try{
				attrB = Files.readAttributes(fB.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			}catch (FileNotFoundException | NoSuchFileException e){
				fBEx= false;
			}catch (IOException e){
				getLog().printMinimal("I/O Error: " + e.getMessage(), IconKey.Error);
				return null;
			}
		}
		fBDate = (fBEx ? attrB.lastModifiedTime().toMillis() : 0);
		fBIsDir = (fBEx ? attrB.isDirectory() : false);
		fBIsFile = fBEx && !fBIsDir;
		fBSize = (fBEx ? attrB.size(): 0);
		fBIsSymLink = (fBEx ? attrB.isSymbolicLink() : false);

		if (deletionAnalysis){
			getLog().printExcessive("Deletion analysis info A: " + fA.getAbsolutePath() + " [" + (fAEx ? "exists" : "does not exist") + (fAIsDir ? ", dir" : ", file") + (fAIsSymLink ? ", symlink" : "") + "]", IconKey.Info);
			getLog().printExcessive("Deletion analysis info B: " + fB.getAbsolutePath() + " [" + (fBEx ? "exists" : "does not exist") + (fBIsDir ? ", dir" : ", file") + (fBIsSymLink ? ", symlink" : "") + "]", IconKey.Info);

			if (!excluded){
				assert fAEx || fBEx;
	    	    if ((getHowManyBackups() > 0) && (fA.getAbsolutePath().contains(Const.BACKUP_FOLDER_NAME) || fB.getAbsolutePath().contains(Const.BACKUP_FOLDER_NAME))) {
	    		// don't delete things in the backup folder
	    	    } else if (ab && fBEx && fBIsDir && !fAEx && isDelDirs()) {
	    	    	getLog().printModerate("Redundant: " + fB.getAbsolutePath(), IconKey.Deleted);
	    	    	return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBIsRedundant, ab, this);
	    	    } else if (ab && fBEx && !fBIsDir && !fAEx && isDelFiles()) {
	    	    	getLog().printModerate("Redundant: " + fB.getAbsolutePath(), IconKey.Deleted);
	    	    	return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsRedundant, ab, this);
	    	    } else if (!ab && fAEx && fAIsDir && !fBEx && isDelDirs()) {
	    	    	getLog().printModerate("Redundant: " + fA.getAbsolutePath(), IconKey.Deleted);
	    	    	return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirAIsRedundant, ab, this);
	    	    } else if (!ab && fAEx && !fAIsDir && !fBEx && isDelFiles()) {
	    	    	getLog().printModerate("Redundant: " + fA.getAbsolutePath(), IconKey.Deleted);
	    	    	return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsRedundant, ab, this);
	    	    }  
	    	    // default for deletion analysis
	    	    return null;
			}
		}else{
			// !deletionAnalysis
			if (excluded){
				// !deletionAnalysis && excluded
				//This file is being excluded (during copy analysis). Delete it if excluded files/dirs are intended to be deleted.
				boolean isDeleteExcluded = getSyncMode() != SyncMode.BIMirror && getFilterSet().hasExcludeFilters();
				if (isDeleteExcluded && !fAIsDir && fAEx && isDeleteExcludedFilesA()){
					getLog().printModerate("Delete excluded file from Dir A: " + fA.getAbsolutePath(), IconKey.Deleted);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsRedundant, ab, this);
				}
				if (isDeleteExcluded && fAIsDir && fAEx && isDeleteExcludedDirsA()){
					getLog().printModerate("Delete excluded dir from Dir A: " + fA.getAbsolutePath(), IconKey.Deleted);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirAIsRedundant, ab, this);
				}
				if (isDeleteExcluded && !fBIsDir && fBEx && isDeleteExcludedFilesB()){
					getLog().printModerate("Delete excluded file from Dir B: " + fB.getAbsolutePath(), IconKey.Deleted);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsRedundant, ab, this);
				}
				if (isDeleteExcluded && fBIsDir && fBEx && isDeleteExcludedDirsB()){
					getLog().printModerate("Delete excluded dir from Dir B: " + fB.getAbsolutePath(), IconKey.Deleted);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBIsRedundant, ab, this);
				}
			}else{
				// !deletionAnalysis && !!excluded
				//copy analysis
				getLog().printExcessive("Copy analysis info A: " + fA.getAbsolutePath() + " [" + (fAEx ? "exists" : "does not exist") + (fAIsDir ? ", dir" : ", file") + (fAIsSymLink ? ", symlink" : "") + ", " + TextFormatTool.getLastModifiedLong(fADate) + " ("+ fADate+ "), " + fASize + " bytes]", IconKey.Info);
				getLog().printExcessive("Copy analysis info B: " + fB.getAbsolutePath() + " [" + (fBEx ? "exists" : "does not exist") + (fBIsDir ? ", dir" : ", file") + (fBIsSymLink ? ", symlink" : "") + ", " + TextFormatTool.getLastModifiedLong(fBDate) + " ("+ fBDate+ "), " + fBSize + " bytes]", IconKey.Info);

				if (getSymLinkMode() == SymLinkMode.SkipSymLinks && (fAIsSymLink || fBIsSymLink)){
					getLog().printModerate("Skipping SymLink: " + fA.getAbsolutePath(), IconKey.DirA);
					return null;
				}
				if ((ab && getSymLinkMode() == SymLinkMode.CopySymLinks && fAEx && !fAIsDir && fAIsSymLink) &&
						// Maybe add a check to verify if the targets of existing sym-links are the same?
						(!fBEx))
				{
					getLog().printModerate("Copy SymLink: " + fA.getAbsolutePath(), IconKey.CopyModified);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsSymLink, ab, this);
				}
				if ((!ab && getSymLinkMode() == SymLinkMode.CopySymLinks && fBEx && !fBIsDir && fBIsSymLink) &&
						// Maybe add a check to verify if the targets of existing sym-links are the same?
						(!fAEx))
				{
					getLog().printModerate("Copy SymLink: " + fB.getAbsolutePath(), IconKey.CopyModified);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsSymLink, ab, this);
				}
				if ((ab && getSymLinkMode() == SymLinkMode.CopySymLinks && fAEx && fAIsDir && fAIsSymLink) &&
						// Maybe add a check to verify if the targets of existing sym-links are the same?
						(!fBEx))
				{
					getLog().printModerate("Copy SymLink: " + fA.getAbsolutePath(), IconKey.DirA);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirAIsSymLink, ab, this);
				}
				if ((!ab && getSymLinkMode() == SymLinkMode.CopySymLinks && fBEx && fBIsDir && fBIsSymLink) &&
						// Maybe add a check to verify if the targets of existing sym-links are the same?
						(!fAEx))
				{
					getLog().printModerate("Copy SymLink: " + fA.getAbsolutePath(), IconKey.DirB);
					return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBIsSymLink, ab, this);
				}

				
	    		if (ab && fAEx && fAIsDir && isCopyAll()){
	    			getLog().printModerate("Copy forced: " + fA.getAbsolutePath(), IconKey.DirA);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirACopyForced, ab, this);
	    		}
	    		
	    		if (!ab && fBEx && fBIsDir && isCopyAll()){
	    			getLog().printModerate("Copy forced: " + fB.getAbsolutePath(), IconKey.DirB);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBCopyForced, ab, this);
	    		}
	    		if (ab && fAEx && !fAIsDir && isCopyAll()){
	    			getLog().printModerate("Copy forced: " + fA.getAbsolutePath(), IconKey.CopyForced);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileACopyForced, ab, this);
	    		}
	    		
	    		if (!ab && fBEx && !fBIsDir && isCopyAll()){
	    			getLog().printModerate("Copy forced: " + fB.getAbsolutePath(), IconKey.CopyForced);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBCopyForced, ab, this);
	    		}
	    
	    		if (ab && fAEx && !fBEx && fAIsDir && isCopyNew()){
	    			getLog().printModerate("New: " + fA.getAbsolutePath(), IconKey.DirA);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirAIsNew, ab, this);
	    		}
	    		
	    		if (!ab && !fAEx && fBEx && fBIsDir && isCopyNew()){
	    			getLog().printModerate("New: " + fB.getAbsolutePath(), IconKey.DirB);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBIsNew, ab, this);
	    		}
	    
	    		if (ab && fAEx && !fBEx && isCopyNew()){
	    			getLog().printModerate("New: " + fA.getAbsolutePath(), IconKey.CopyNew);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsNew, ab, this);
	    		}
	    		
	    		if (!ab && !fAEx && fBEx && isCopyNew()){
	    			getLog().printModerate("New: " + fB.getAbsolutePath(), IconKey.CopyNew);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsNew, ab, this);
	    		}
	    
	    		assert fAEx && fBEx;

	    		boolean fileASameSizefileB = (fASize == fBSize); 
	    		boolean fileAIsLarger = (fASize > fBSize);
	    		boolean fileBIsLarger = (fASize < fBSize);
	    		
	    		int modCompare = DateTool.cmpDates(fADate, fBDate, getGranularity(), isIgnoreDaylightSavingGranularity());
	    		
	    		boolean fileASameDatefileB = (modCompare == 0);
	    		boolean fileAIsModified = (modCompare == -1);
	    		boolean fileBIsModified = (modCompare == 1);
	    
	    		if (fileASameSizefileB && getCompareMode() == Const.CompareMode.CompareFileContents && fAIsFile && fBIsFile &&
	    				( //do this only for modes in which modified files should be copied
	    						(isCopyModified()) 
	    						|| 
	    						(getSyncMode().equals(SyncMode.BICustom) && getSyncConflictResolutionMode() == SyncConflictResolutionMode.CopyModified)) 
	    				){
	    		    try {
	    			modCompare = FileTools.checksumIdentical(fA, fB) ? 0 : 1;
	    		    } catch (WarningException we) {
	    		    	getLog().printMinimal("Warning: " + we.getMessage(), IconKey.Warning);
	    		    	DirSyncPro.getSync().setError(SyncError.Warning);
	    		    }
	    		    fileAIsModified = fileBIsModified = (modCompare == 1);
	    		}
	    
	    		if (isCopyModified() && fileASameDatefileB && getCompareMode() == Const.CompareMode.CompareFileSizesDatesMetaData &&
	    				(  //do this only for modes in which modified files/dirs should be copied
	    						(isCopyModified()) 
	    						|| 
	    						(getSyncMode().equals(SyncMode.BICustom) && getSyncConflictResolutionMode() == SyncConflictResolutionMode.CopyModified)) 
	    				){
	    		    modCompare = FileTools.cmpFileAttributes(fA, fB);
	    		    fileAIsModified = fileBIsModified = (modCompare == 1);
	    		}
	    		
	    		if (isSyncDirTimeStamps() && ab && !fileASameDatefileB && fAIsDir && fBIsDir &&
	    				(
	    						(isCopyModified()) 
	    						|| 
	    						(getSyncMode().isBI() && getSyncConflictResolutionMode() == SyncConflictResolutionMode.CopyModified)) 
	    				)
	    			{
	    				getLog().printModerate("Modified: " + fA.getAbsolutePath(), IconKey.CopyModified);
	    				return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirAIsModified, ab, this);
	    			}
	    		
	    		if (isSyncDirTimeStamps() && !ab && !fileASameDatefileB &&
	    				(
	    						(isCopyModified())
	    						|| 
	    						(getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyModified))) 
	    				)
	    			{
	    				getLog().printModerate("Modified: " + fB.getAbsolutePath(), IconKey.CopyModified);
	    				return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.DirBIsModified, ab, this);
	    			}
	    
	    		// do nothing if both dirs exist and not returned above
	    		if (!fAEx || !fBEx || fAIsDir || fBIsDir){
	    			return null;
	    		}
	    		
	    		assert !fAIsDir && !fBIsDir && fAEx && fBEx;
	    
	    		if (ab && 
	    			(
	    					(fileAIsModified && (isCopyModified()) && (getSyncMode() == SyncMode.ABMirror || getSyncMode() == SyncMode.ABCustom)) 
	    					|| 
	    					(fileAIsModified && getSyncMode().isBI() && getSyncConflictResolutionMode() == SyncConflictResolutionMode.CopyModified))
	    			)
	    		{
	    			getLog().printModerate("Modified: " + fA.getAbsolutePath(), IconKey.CopyModified);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsModified, ab, this);
	    		}
	    		
	    		if (!ab && 
	    			(
	    					(fileBIsModified && (isCopyModified()) && (getSyncMode() == SyncMode.BAMirror || getSyncMode() == SyncMode.BACustom))
	    					||
	    					(fileBIsModified && getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyModified))) 
	    			)
	    		{
	    			getLog().printModerate("Modified: " + fB.getAbsolutePath(), IconKey.CopyModified);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsModified, ab, this);
	    		}
	    
	    		if (ab && fileAIsLarger &&
	    			(
	    					(isCopyLarger() && (getSyncMode() == SyncMode.ABMirror || getSyncMode() == SyncMode.ABCustom))
	    					||
	    					(getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyLarger))) 
	    			)
	    		{
	    			getLog().printModerate("Larger: " + fA.getAbsolutePath(), IconKey.CopyLarger);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsLarger, ab, this);
	    		}
	    		
	    		if (!ab && fileBIsLarger &&
	    			(
	    					(isCopyLarger() && (getSyncMode() == SyncMode.BAMirror || getSyncMode() == SyncMode.BACustom))
	    					|| 
	    					(getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyLarger))) 
	    			)
	    		{
	    			getLog().printModerate("Larger: " + fB.getAbsolutePath(), IconKey.CopyLarger);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsLarger, ab, this);
	    		}
	    
	    		if (ab && fileAIsModified && fileAIsLarger &&
	    				(
	    						(isCopyLargerModified() && (getSyncMode() == SyncMode.ABMirror || getSyncMode() == SyncMode.ABCustom))
	    						|| 
	    						(getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyLargerAndModified))) 
	    				)
	    		{
	    			getLog().printModerate("Larger & Modified: " + fA.getAbsolutePath(), IconKey.CopyLargerModified);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileAIsLargerAndModified, ab, this);
	    		}
	    		
	    		if (!ab && fileBIsModified && fileBIsLarger &&
	    				(
	    						(isCopyLargerModified() && (getSyncMode() == SyncMode.BAMirror || getSyncMode() == SyncMode.BACustom))
	    						|| 
	    						(getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyLargerAndModified))) 
	    				)
	    		{
	    			getLog().printModerate("Larger & Modified: " + fB.getAbsolutePath(), IconKey.CopyLargerModified);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.FileBIsLargerAndModified, ab, this);
	    		}
	    		
	    		//Monodirectional conflict resolution by copying the source
	    		if (ab && ((!fileASameDatefileB || !fileASameSizefileB) && !getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopySource))){
	    			getLog().printModerate("Monodirectional conflict resolution by copying the source: " + fA.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionCopySourceA, ab, this);
	    		}
	    		if (!ab && ((!fileASameDatefileB || !fileASameSizefileB) && !getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopySource))){
	    			getLog().printModerate("Monodirectional conflict resolution by copying the source: " + fB.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionCopySourceB, ab, this);
	    		}
	    		
	    		//Bidirectional conflict resolution by renaming
	    		if ( (ab && getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyRenamed)) && 
	    			 (fileAIsModified || !fileASameSizefileB) )
	    		{
	    			getLog().printModerate("Bidirectional conflict resolution by renaming: " + fA.getAbsolutePath() + " & " + fB.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionRename, ab, this);
	    		}
	    		if ( (!ab && getSyncMode().isBI() && getSyncConflictResolutionMode().equals(SyncConflictResolutionMode.CopyRenamed)) && 
	    				 (fileBIsModified || !fileASameSizefileB) )
	    		{
	    			getLog().printModerate("Bidirectional conflict resolution by renaming: " + fA.getAbsolutePath() + " & " + fB.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionRename, ab, this);
	    		}
	    
	    		//Bidirectional & Monodirectional conflict resolutin by warning
	    		if (ab && (
	    					((fileAIsModified || !fileASameSizefileB) && getSyncMode().isBI()) || 
	    					 ((!fileASameDatefileB || !fileASameSizefileB) && !getSyncMode().isBI())
	    					 )
	    					 && getSyncConflictResolutionMode() == SyncConflictResolutionMode.WarnUser)
	    		{
	    			getLog().printModerate("Sync conflict warning: " + fA.getAbsolutePath() + " & " + fB.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionWarn, ab, this);
	    		}
	    		if (!ab && (
	    				((fileBIsModified || !fileASameSizefileB) && getSyncMode().isBI()) || 
	    				 ((!fileASameDatefileB || !fileASameSizefileB) && !getSyncMode().isBI())
	    				 )
	    				 && getSyncConflictResolutionMode() == SyncConflictResolutionMode.WarnUser)
	    		{
	    			getLog().printModerate("Sync conflict warning: " + fA.getAbsolutePath() + " & " + fB.getAbsolutePath(), IconKey.SyncConflict);
	    			return new SyncPair(fA, fB, fAEx, fBEx, fAIsDir, fBIsDir, fADate, fBDate, fASize, fBSize, SyncPairStatus.ConflictResolutionWarn, ab, this);
	    		} 
	    		//default
	    		return null;
			}
		}	
		//dummy
		return null;
	}
	
	private void analyzeFilePair(Path p, BasicFileAttributes attr, boolean ab, boolean deletionAnalysis, boolean excluded){
		DirSyncPro.getSync().sleepOnPause();
		SyncPair sp = null;
		File fA = null, fB = null;

		if ((ab && !deletionAnalysis) || (!ab && deletionAnalysis)){
			fA = p.toFile();
			fB = replacePath(p, pathA, pathB).toFile();
		}else if ((!ab && !deletionAnalysis) || (ab && deletionAnalysis)){
			fB = p.toFile();
			fA = replacePath(p, pathB, pathA).toFile();
		}
		
		getLog().printModerate((deletionAnalysis ? "Deletion analysis, checking: " : "Comparison analysis, comparing: ") + fA.getAbsolutePath() + " AND " + fB.getAbsolutePath(), IconKey.Info);

		sp = analyzeSyncPairStatus(fA, fB, attr, ab, deletionAnalysis, excluded);

		if (sp != null && sp.getSyncPairStatus() != null){
			DirSyncPro.getSync().getSyncQ().add(sp);
		}
	}
	
	private void generateSyncQ(Path pA, Path pB, boolean ab){
		// calculate number of steps

		pathA = new File(dirA).toPath();
		pathB = new File(dirB).toPath();
		DSPFileVisitor dspFileVistor = new DSPFileVisitor(filterSet, ab);

		int maxDepth = (isRecursive() ? Integer.MAX_VALUE : 1);
		
		if (DirSyncPro.isGuiMode()) {
			DirSyncPro.getGui().registerProgressBars(-1, -1, 0, "", false, 0, 100, "Analyzing: " + this.name, true);
		}
		try{
			//deletion Analysis
			//do not delete stuff in Bidirectional Sync Mode
			boolean deletionAnalysis = (isDelFiles() || isDelDirs()) && !syncMode.isBI();
			if (deletionAnalysis){
				dspFileVistor.setDeletionAnalysis(true);
				dspFileVistor.resetDepth();
				if (ab){
					Files.walkFileTree(pB, (!getSymLinkMode().equals(SymLinkMode.SkipSymLinks) ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class)), maxDepth, dspFileVistor);
				}else{
					Files.walkFileTree(pA, (!getSymLinkMode().equals(SymLinkMode.SkipSymLinks) ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class)), maxDepth, dspFileVistor);
				}
			}

			//copy analysis
			dspFileVistor.setDeletionAnalysis(false);
			dspFileVistor.resetDepth();
			if (ab){
				Files.walkFileTree(pA, (getSymLinkMode() == SymLinkMode.FollowSymLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class)), maxDepth, dspFileVistor);
			}else{
				Files.walkFileTree(pB, (getSymLinkMode() == SymLinkMode.FollowSymLinks ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class)), maxDepth, dspFileVistor);
			}
		}catch (IOException exc){
			getLog().printMinimal("Unable to analyze: " + this.name + " (" + exc.getMessage() + ")", IconKey.Error);
		}
	}
 	
	public void analyze() throws IncompleteConfigurationException{
		DirSyncPro.getSync().sleepOnPause();
		if (DirSyncPro.getSync().isStopping()) {
			return;
		}

		getLog().printMinimal("*** Analyzing job: " + name, IconKey.Info);
		printJobOptions();
		
		// check if src directory exists
		if (dirA.equals("")) {
			getLog().printMinimal(" Directory A is not specified!", IconKey.Error);
			throw new IncompleteConfigurationException("Directory A is not specified!");
		}
		File fA = new File(dirA);
		if (!fA.exists() || !fA.isDirectory()) {
			getLog().printMinimal(" Directory A: '"	+ fA.getAbsolutePath()	+ "' doesn't exist or isn't a directory!", IconKey.Error);
			throw new IncompleteConfigurationException();
		}
		if (fA.exists() && !fA.isDirectory()) {
			getLog().printMinimal(" Directory A: '" + fA.getAbsolutePath() + "' isn't a directory!", IconKey.Error);
			throw new IncompleteConfigurationException();
		}
		// check if dst directory exists
		if (dirB.equals("")) {
			getLog().printMinimal(" Directory B is not specified!", IconKey.Error);
			throw new IncompleteConfigurationException("Directory B is not specified!");
		}
		File fB = new File(dirB);
		if (!fB.exists()) {
			getLog().printMinimal(" Directory B: '"	+ fB.getAbsolutePath()	+ "' doesn't exist, it will be created when synchronizing.", IconKey.Warning);
			//DirSyncPro.getSync().getSyncQ().add(new SyncPair(fA, fB, analyzeSyncPairStatus(fA, fB, true, true, false, true, true, false), this));
			DirSyncPro.getSync().setError(Sync.SyncError.Warning);
		}
		if (fB.exists() && !fB.isDirectory()) {
			getLog().printMinimal(" Directory B: '" + fB.getAbsolutePath() + "' isn't a directory!", IconKey.Error);
			throw new IncompleteConfigurationException();
		}
		if (syncMode == SyncMode.ABMirror || syncMode == SyncMode.ABFull || syncMode == SyncMode.ABContribute || syncMode == SyncMode.ABCustom){
			generateSyncQ(fA.toPath(), fB.toPath(), true);
		}else if (syncMode == SyncMode.BAMirror || syncMode == SyncMode.BAFull || syncMode == SyncMode.BAContribute || syncMode == SyncMode.BACustom){
			generateSyncQ(fA.toPath(), fB.toPath(), false);
		}else if (syncMode.isBI()){
			generateSyncQ(fA.toPath(), fB.toPath(), true);
			generateSyncQ(fA.toPath(), fB.toPath(), false);
		}
	}

	/**
	 * Copies the options from the given job to this job.
	 * @param job The job from which to copy the options.
	 */
	public void copyOptions(Job job) {
		setSyncMode(job.getSyncMode());
		setSyncConflictResolutionMode(job.getSyncConflictResolutionMode());
		
		setCompareMode(job.getCompareMode());

		setCopyAll(job.isCopyAll());
		setCopyNew(job.isCopyNew());
		setCopyLarger(job.isCopyLarger());
		setCopyModified(job.isCopyModified());
		setCopyLargerModified(job.isCopyLargerModified());
		
		setDelFiles(job.isDelFiles());
		setDelDirs(job.isDelDirs());
		setRecursive(job.isRecursive());
		setVerify(job.isVerify());
		
		setSyncRealtime(job.isSyncRealtime());
		setSyncRealtimeOnStart(job.isSyncRealtimeOnStart());
		setSyncRealtimeDelay(job.getSyncRealtimeDelay());
		
		setGranularity(job.getGranularity());
		setSyncDirTimeStamps(job.isSyncDirTimeStamps());
		setIgnoreDaylightSavingGranularity(job.isIgnoreDaylightSavingGranularity());
		
		filterSet = (FilterSet) job.getFilterSet().clone();
	
		setHowManyBackups(job.getHowManyBackups());
		setBackupInline(job.isBackupInline());
		setBackupDir(job.getBackupDir());
		
		setSymLinkMode(job.getSymLinkMode());
		setWriteTimestampBack(job.isWriteTimestampBack());
		
		setSchedules(job.getSchedules());
	}
	
	private void printJobOptions(){
		getLog().printExcessive("Options for job '" + getName() + "': ", Const.IconKey.Info);
		getLog().printExcessive("  Source Path: \"" + getDirA() + "\"", Const.IconKey.Info);
		getLog().printExcessive("  Destination Path: \"" + getDirB() + "\"", Const.IconKey.Info);
		getLog().printExcessive("  Sync Mode: \"" + getSyncMode() + "\"", Const.IconKey.Info);
		getLog().printExcessive("  Traverse subfolders: " + isRecursive(), Const.IconKey.Info);
		getLog().printExcessive("  Verify: " + isVerify(), Const.IconKey.Info);
		getLog().printExcessive("  Conflict mode for BiDir Sync: " + getSyncConflictResolutionMode(), Const.IconKey.Info);
		getLog().printExcessive("  Number of Backups: " + getHowManyBackups(), Const.IconKey.Info);
		getLog().printExcessive("  Backup in dest dir: " + isBackupInline(), Const.IconKey.Info);
		if (!isBackupInline()) getLog().printExcessive("  Backup dir: " + getBackupDir(), Const.IconKey.Info);
		getLog().printExcessive("  Write Timestamp back: " + isWriteTimestampBack(), Const.IconKey.Info);
		getLog().printExcessive("  Granularity: " + getGranularity() + " s", Const.IconKey.Info);
		getLog().printExcessive("  Ignore daylight saving granularity: " + isIgnoreDaylightSavingGranularity(), Const.IconKey.Info);
		getLog().printExcessive("  Sync timestamp of the dirs: " + isSyncDirTimeStamps(), Const.IconKey.Info);
		getLog().printExcessive("  Symbolic Link handling: " + getSymLinkMode(), Const.IconKey.Info);
		getLog().printExcessive("  Preserve DOS attributes: " + isPreserveDOSAttributes(), Const.IconKey.Info);
		getLog().printExcessive("  Preserve POSIX permissions: " + isPreservePOSIXFilePermissions(), Const.IconKey.Info);
		getLog().printExcessive("  Preserve POSIX ownership: " + isPreservePOSIXFileOwnership(), Const.IconKey.Info);

		String logFilename = getLog().getFilename();
		logFilename = WildcardTools.replaceDateWildcards(logFilename, DirSyncPro.getSync().getSyncDate());
		logFilename = WildcardTools.replaceTimeWildcards(logFilename, DirSyncPro.getSync().getSyncDate());
		//HIERRR
		
		getLog().printExcessive("  Logfile: \"" + logFilename + "\"", Const.IconKey.Info);

		if (filterSet.hasFilters()) {
			for (Filter filt : filterSet.getFilters()){
				getLog().printExcessive("  Filter: " + filt.getAction().toString() + " " + filt.toString(), Const.IconKey.Info);
			}
		}

		getLog().printExcessive("  Sync options:", Const.IconKey.Info);
		getLog().printExcessive("    All: " + isCopyAll(), Const.IconKey.Info);
		getLog().printExcessive("    New: " + isCopyNew(), Const.IconKey.Info);
		getLog().printExcessive("    Modified: " + isCopyModified(), Const.IconKey.Info);
		getLog().printExcessive("    Larger: " + isCopyLarger(), Const.IconKey.Info);
		getLog().printExcessive("    LargerModified: " + isCopyLargerModified(), Const.IconKey.Info);
		getLog().printExcessive("    Delete Files: " + isDelFiles(), Const.IconKey.Info);
		getLog().printExcessive("    Delete Dirs: " + isDelDirs(), Const.IconKey.Info);
	}

	/**
	 * Creates and returns a copy of this object.
     * @return     a clone of this instance.
     * @exception  CloneNotSupportedException  if the object's class does not
     *               support the <code>Cloneable</code> interface.
     * @see java.lang.Cloneable
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		restartRealtimeListeners();
	}
	
	/**
	 * Stops running listeners and start new if necessary
	 */
	public void restartRealtimeListeners()
	{
		//final Job j1 = this;
		//DirSyncPro.getSync().synchronize(new Vector<Job>(){{add(j1);}});
		
		stopRealtimeListeners();
		if (!enabled)
			return;
		
		if (!syncRealtime)
			return;
		if (dirA.equals("") || dirB.equals(""))
			return;
		
		// initial syncronization
		if (syncRealtimeOnStart)
			ScheduleEngine.getRealtimeInstance().scheduleRealtime((Job) this, 0);
		
		// Start DirA listener
		try {
			dirAListener = new RealtimeListener(dirA);
		} catch (IOException e1) {
			getLog().printMinimal("Unable to setup realtime monitor for " + dirA + ": " + e1.getMessage(), IconKey.Error);
			return;
		}
		
		// Start DirB listener
		try {
			dirBListener = new RealtimeListener(dirB);
		} catch (IOException e1) {
			getLog().printMinimal("Unable to setup realtime monitor for " + dirB + ": " + e1.getMessage(), IconKey.Error);
			return;
		}
		
		final Job j = this;
		//schedule next syncronization once any change appears
		RealtimeListener.RealtimeChangeListener changeListener = new RealtimeListener.RealtimeChangeListener()
		{
			public void change(WatchEvent<Path> we){
				ScheduleEngine.getRealtimeInstance().scheduleRealtime(j, syncRealtimeDelay * 1000);
			}
		};
			
			dirAListener.setChangeListener(changeListener);
			dirBListener.setChangeListener(changeListener);
			dirAListener.start();
			dirBListener.start();
			
		}
		
		protected void stopRealtimeListeners(){
			if (dirAListener != null)
				dirAListener.stop();
			if (dirBListener != null)
				dirBListener.stop();
		}
}