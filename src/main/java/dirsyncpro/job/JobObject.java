/*
 * DirectoryVarGetSet.java
 *
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2006, 2008 F. Gerbig
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

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import dirsyncpro.Const;
import dirsyncpro.Const.CompareMode;
import dirsyncpro.Const.SymLinkMode;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterSet;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.sync.RealtimeListener;
import dirsyncpro.tools.Log;

/**
 * Contains variables and getter and setter methods for the Directory class.
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org) 
 */
public abstract class JobObject {
	
	String name = "";

	protected boolean enabled;

	// log object for this directory
	private Log log;

	// sync mode
	protected Const.SyncMode syncMode;
		/*
		 * Mirror implicates: copyAll=false, copyNew=true, copyLarger=false, copyModified=true, copyLargerModified=false, deleteFiles=true, deleteDirs=true, filterSet={}, compareMode=CompareFileSizesDates;  
		 * Full implicates: copyAll=true, copyNew=false, copyLarger=false, copyModified=false, copyLargerModified=false, deleteFiles=true, deleteDirs=true, filterSet={}, compareMode=CompareFileSizesDates;  
		 * Contribute implicates: copyAll=false, copyNew=true, copyLarger=false, copyModified=false, copyLargerModified=false, deleteFiles=false, deleteDirs=false, filterSet={}, compareMode=CompareFileSizesDates;  
		 * Custom initializes like Mirror but could be change any option.  
		 */

	// Conflict mode for the bidirectional sync
	protected Const.SyncConflictResolutionMode syncConflictResolutionMode;
	
	// Compare mode
	protected Const.CompareMode compareMode;
		
	// source and destination directories
	protected String dirA = "";
	protected Path pathA;
	protected String dirB = "";
	protected Path pathB;

	protected Vector<Schedule> schedules;
	protected FilterSet filterSet;
	
	//realtime sync
	protected boolean syncRealtime;
	protected boolean syncRealtimeOnStart;
	protected int syncRealtimeDelay = Const.DefaultRealtimeSyncDelayValue;
	protected RealtimeListener dirAListener;
	protected RealtimeListener dirBListener;
	
	// copy options
	private boolean recursive;
	private boolean verify;
	private boolean copyAll;
	private boolean copyNew;
	private boolean copyLarger;
	private boolean copyModified;
	private boolean copyLargerModified;
	private boolean deleteDirs;
	private boolean deleteFiles;
	private boolean deleteExcludedDirsA;
	private boolean deleteExcludedFilesA;
	private boolean deleteExcludedDirsB;
	private boolean deleteExcludedFilesB;
	private int howManyBackups;
	private boolean backupInline;
	private String backupDir;
	
	private SymLinkMode symLinkMode;
	private boolean preserveDOSAttributes;
	private boolean preservePOSIXFilePermissions;
	private boolean preservePOSIXFileOwnership;
	private boolean overrideReadOnly;
	private boolean writeTimestampBack;
	private boolean syncDirTimeStamps;
	private boolean ignoreDaylightSavingGranularity;
	private int granulaity;


	/**
	 * Returns the destination path of the directory to dirsyncpro.
	 * 
	 * @return The destination path.
	 */
	public String getDirB() {
		return dirB;
	}

	/**
	 * Returns the name of the dirsyncpro action.
	 * 
	 * @return String The name of the dirsyncpro action.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the source path of the directory to dirsyncpro.
	 * 
	 * @return The source path.
	 */
	public String getDirA() {
		return dirA;
	}

	/**
	 * Returns if all files are copied.
	 * 
	 * @return <code>true</code> if all files are copied, else
	 *         <code>false</code>.
	 */
	public boolean isCopyAll() {
		return copyAll;
	}

	/**
	 * Returns if only larger files are copied.
	 * 
	 * @return <code>true</code> if only larger files are copied, else
	 *         <code>false</code>.
	 */
	public boolean isCopyLarger() {
		return copyLarger;
	}

	/**
	 * Returns if only files are copied that are larger <b>and </b> modified.
	 * 
	 * @return <code>true</code> if only are copied that are larger <b>and
	 *         </b> modified, else <code>false</code>.
	 */
	public boolean isCopyLargerModified() {
		return copyLargerModified;
	}

	/**
	 * Returns if only modified files are copied.
	 * 
	 * @return <code>true</code> if only modified files are copied, else
	 *         <code>false</code>.
	 */
	public boolean isCopyModified() {
		return copyModified;
	}

	/**
	 * Returns if only new files (files not existing in the destination
	 * directory) are copied.
	 * 
	 * @return <code>true</code> if only new files are copied, else
	 *         <code>false</code>.
	 */
	public boolean isCopyNew() {
		return copyNew;
	}

	/**
	 * Returns if directories deleted in the source directory are deleted in the
	 * destination directory.
	 * 
	 * @return <code>true</code> if directories deleted in the source
	 *         directory are deleted in the destination directory, else
	 *         <code>false</code>.
	 */
	public boolean isDelDirs() {
		return deleteDirs;
	}

	/**
	 * Returns if files deleted in the source directory are deleted in the
	 * destination directory.
	 * 
	 * @return <code>true</code> if files deleted in the source directory are
	 *         deleted in the destination directory, else <code>false</code>.
	 */
	public boolean isDelFiles() {
		return deleteFiles;
	}

	/**
	 * Get how man files of a changed or deleted file should be kept.
	 * 
	 * @return number of backups to keep (<code>0</code> means none).
	 */
	public int getHowManyBackups() {
		return howManyBackups;
	}

	/**
	 * Determines whether this directory is enabled.
	 * 
	 * @return <code>true</code> if the component is enabled,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns if the dirsyncpro is done with verify.
	 * 
	 * @return <code>true</code> if verify is enabled.
	 */
	public boolean isVerify() {
		return verify;
	}

	/**
	 * Returns if the dirsyncpro is done with subfolders.
	 * 
	 * @return <code>true</code> if the job is recursive, else
	 *         <code>false</code>.
	 */
	public boolean isRecursive() {
		return recursive;
	}

	/**
	 * Sets whether all files are copied.
	 * 
	 * @param copyAll
	 *            <code>true</code> if all files are copied.
	 */
	public void setCopyAll(boolean copyAll) {
		this.copyAll = copyAll;
	}

	/**
	 * Sets whether only larger files are copied.
	 * 
	 * @param copyLarger
	 *            <code>true</code> if only larger files are copied.
	 */
	public void setCopyLarger(boolean copyLarger) {
		this.copyLarger = copyLarger;
	}

	/**
	 * Sets whether only files are copied that are larger <b>and </b> modified.
	 * 
	 * @param copyLargerModified
	 *            <code>true</code> if only files are copied that are larger
	 *            <b>and </b> modified.
	 */
	public void setCopyLargerModified(boolean copyLargerModified) {
		this.copyLargerModified = copyLargerModified;
	}

	/**
	 * Sets whether only modified files are copied.
	 * 
	 * @param copyModified
	 *            <code>true</code> if only modified files are copied.
	 */
	public void setCopyModified(boolean copyModified) {
		this.copyModified = copyModified;
	}

	/**
	 * Sets whether only new files (files not existing in the destination
	 * directory) are copied.
	 * 
	 * @param copyNew
	 *            <code>true</code> if only new files are copied.
	 */
	public void setCopyNew(boolean copyNew) {
		this.copyNew = copyNew;
	}

	/**
	 * Sets whether directories deleted in the source directory are deleted in
	 * the destination directory.
	 * 
	 * @param delDirs
	 *            <code>true</code> if directories deleted in the source
	 *            directory are deleted in the destination directory.
	 */
	public void setDelDirs(boolean delDirs) {
		this.deleteDirs = delDirs;
	}

	/**
	 * Sets whether files deleted in the source directory are deleted in the
	 * destination directory.
	 * 
	 * @param delFiles
	 *            <code>true</code> if files deleted in the source directory
	 *            are deleted in the destination directory.
	 */
	public void setDelFiles(boolean delFiles) {
		this.deleteFiles = delFiles;
	}

	/**
	 * Set how many backups of a changed or deleted file to keep. 
	 * @param howManyBackups
	 */
	public void setHowManyBackups(int howManyBackups) {
		this.howManyBackups = Math.min(howManyBackups, Const.BACKUP_MAX_NUMBER);
	}


	/**
	 * Sets the destination path of the directory to dirsyncpro.
	 * 
	 * @param dst
	 *            The destination path.
	 */
	public void setDst(String dst) {
		this.dirB = dst;
	}

	/**
	 * Enables or disables this directory, depending on the value of the
	 * parameter enabled.
	 * 
	 * @param enabled
	 *            If <code>true</code>, this component is enabled; otherwise
	 *            this component is disabled.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Sets the name of the dirsyncpro action.
	 * 
	 * @param name
	 *            The name of this dirsyncpro action.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the source path of the directory to dirsyncpro.
	 * 
	 * @param src
	 *            The source path.
	 */
	public void setSrc(String src) {
		this.dirA = src;
	}

	/**
	 * Sets whether copied files should be verified.
	 * @param b Whether copied files should be verified.
	 *            Set to <code>true</code> to enable verify.
	 */
	public void setVerify(boolean b) {
		verify = b;
	}

	/**
	 * Sets whether the dirsyncpro is done with subfolders.
	 * @param recursive Whether the dirsyncpro is done with subfolders.
	 *            <code>true</code> if the synchroize is done with subfolders.
	 */
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * Returns the directory log object
	 * @return Log log
	 * 			The log
	 * 
	 */
	public Log getLog() {
		return log;
	}

	/**
	 * Sets the directory log object
	 * @param log
	 * The log.
	 */
	public void setLog(Log log) {
		if (this.log != null){
			// close file so it is released
			log.disable();
		}
		this.log = log;
	}

	/**
	 * @return the syncMode
	 */
	public Const.SyncMode getSyncMode() {
		return syncMode;
	}

	
	/**
	 * @return the backupDir
	 */
	public String getBackupDir() {
		if (this.isBackupInline() || backupDir.equals("")){
			return "";
		}else{
			return backupDir;
		}
	}

	/**
	 * @param backupDir the backupDir to set
	 */
	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}

	/**
	 * @return the backupInline
	 */
	public boolean isBackupInline() {
		return backupInline;
	}

	/**
	 * @param backupInline the backupInline to set
	 */
	public void setBackupInline(boolean backupInline) {
		this.backupInline = backupInline;
	}
	
	/**
	 * @return the syncCompareMode
	 */
	public Const.CompareMode getCompareMode() {
	    return compareMode;
	}

	/**
	 * @param compareMode the syncCompareMode to set
	 */
	public void setCompareMode(Const.CompareMode compareMode) {
		this.compareMode = compareMode;
		boolean windows = Const.OS_IS_WINDOWS;
		if (compareMode.equals(CompareMode.CompareFileSizesDatesMetaData)){
			if (windows){
				this.setPreserveDOSAttributes(true);
			}else{
				this.setPreservePOSIXFileOwnership(true);
				this.setPreservePOSIXFilePermissions(true);
			}
		}else{
			if (windows){
				this.setPreserveDOSAttributes(false);
			}else{
				this.setPreservePOSIXFileOwnership(false);
				this.setPreservePOSIXFilePermissions(false);
			}
		}
	}

	/**
	 * @return the biDirSyncConflictMode
	 */
	public Const.SyncConflictResolutionMode getSyncConflictResolutionMode() {
		return syncConflictResolutionMode;
	}

	/**
	 * @param scrm the Sync Conflict Resolution Mode to set
	 */
	public void setSyncConflictResolutionMode(Const.SyncConflictResolutionMode scrm) {
		this.syncConflictResolutionMode = scrm;
	}

	/**
	 * @return the deleteExcludedDirsA
	 */
	public boolean isDeleteExcludedDirsA() {
		return deleteExcludedDirsA;
	}

	/**
	 * @param deleteExcludedDirsA the deleteExcludedDirsA to set
	 */
	public void setDeleteExcludedDirsA(boolean deleteExcludedDirs) {
		this.deleteExcludedDirsA = deleteExcludedDirs;
	}

	/**
	 * @return the deleteExcludedFilesA
	 */
	public boolean isDeleteExcludedFilesA() {
		return deleteExcludedFilesA;
	}

	/**
	 * @param deleteExcludedFilesA the deleteExcludedFilesA to set
	 */
	public void setDeleteExcludedFilesA(boolean deleteExcludedFiles) {
		this.deleteExcludedFilesA = deleteExcludedFiles;
	}

	/**
	 * @return the deleteExcludedDirsB
	 */
	public boolean isDeleteExcludedDirsB() {
		return deleteExcludedDirsB;
	}

	/**
	 * @param deleteExcludedDirsB the deleteExcludedDirsB to set
	 */
	public void setDeleteExcludedDirsB(boolean deleteExcludedDirsB) {
		this.deleteExcludedDirsB = deleteExcludedDirsB;
	}

	/**
	 * @return the deleteExcludedFilesB
	 */
	public boolean isDeleteExcludedFilesB() {
		return deleteExcludedFilesB;
	}

	/**
	 * @param deleteExcludedFilesB the deleteExcludedFilesB to set
	 */
	public void setDeleteExcludedFilesB(boolean deleteExcludedFilesB) {
		this.deleteExcludedFilesB = deleteExcludedFilesB;
	}

	public SymLinkMode getSymLinkMode() {
		return symLinkMode;
	}

	public void setSymLinkMode(SymLinkMode slm) {
		this.symLinkMode = slm;
	}

	public boolean isWriteTimestampBack() {
		return writeTimestampBack;
	}

	public void setWriteTimestampBack(boolean writeTimestampBack) {
		this.writeTimestampBack = writeTimestampBack;
	}

	public int getGranularity() {
		return granulaity;
	}

	public void setGranularity(int gran) {
		this.granulaity = gran;
	}

	/**
	 * Returns whether this job has schedules
	 * @return boolean, whether this job has schedules
	 */
	public boolean HasSchedules(){
		return (schedules.size() > 0);
	}

	/**
	 * @return the schedules
	 */
	public Vector<Schedule> getSchedules() {
		return schedules;
	}

	/**
	 * @param sched
	 */
	public void addSchedule(Schedule sched){
		schedules.add(sched);
		for (Schedule sch : schedules){
			sch.calculateNextEvent();
		}
		Collections.sort(schedules);
	}
	
	/**
	 * @param schedules the schedules to set
	 */
	public void setSchedules(Vector<Schedule> schedules) {
		this.schedules = schedules;
	}
	
	/**
	 * @return the filters array list
	 */
	public FilterSet getFilterSet() {
		return filterSet;
	}

	public boolean isSyncDirTimeStamps() {
		return syncDirTimeStamps;
	}

	public void setSyncDirTimeStamps(boolean syncDirTimeStamps) {
		this.syncDirTimeStamps = syncDirTimeStamps;
	}

	public boolean isIgnoreDaylightSavingGranularity() {
		return ignoreDaylightSavingGranularity;
	}

	public void setIgnoreDaylightSavingGranularity(boolean ignoreDaylightSavingGranularity) {
		this.ignoreDaylightSavingGranularity = ignoreDaylightSavingGranularity;
	}

	public boolean isPreserveDOSAttributes() {
		return preserveDOSAttributes;
	}

	public void setPreserveDOSAttributes(boolean preserveDOSAttributes) {
		this.preserveDOSAttributes = preserveDOSAttributes;
	}

	public boolean isPreservePOSIXFilePermissions() {
		return preservePOSIXFilePermissions;
	}

	public void setPreservePOSIXFilePermissions(boolean preservePOSIXFilePermissions) {
		this.preservePOSIXFilePermissions = preservePOSIXFilePermissions;
	}

	public boolean isPreservePOSIXFileOwnership() {
		return preservePOSIXFileOwnership;
	}

	public void setPreservePOSIXFileOwnership(boolean preservePOSIXFileOwnership) {
		this.preservePOSIXFileOwnership = preservePOSIXFileOwnership;
	}

	public boolean isSyncRealtime() {
		return syncRealtime;
	}

	public void setSyncRealtime(boolean syncRealtime) {
		this.syncRealtime = syncRealtime;
	}

	public boolean isSyncRealtimeOnStart() {
		return syncRealtimeOnStart;
	}

	public void setSyncRealtimeOnStart(boolean syncRealtimeOnStart) {
		this.syncRealtimeOnStart = syncRealtimeOnStart;
	}

	public int getSyncRealtimeDelay() {
		return syncRealtimeDelay;
	}

	public void setSyncRealtimeDelay(int syncRealtimeDelay) {
		this.syncRealtimeDelay = syncRealtimeDelay;
	}

	public boolean isOverrideReadOnly() {
		return overrideReadOnly;
	}

	public void setOverrideReadOnly(boolean overwriteReadOnly) {
		this.overrideReadOnly = overwriteReadOnly;
	}
}
