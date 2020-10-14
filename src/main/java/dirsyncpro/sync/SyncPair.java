/*
 *
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.sync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.attribute.BasicFileAttributes;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.Const.IconKey;
import dirsyncpro.Const.SyncConflictResolutionMode;
import dirsyncpro.Const.SyncMode;
import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.exceptions.WarningException;
import dirsyncpro.job.Job;
import dirsyncpro.sync.Sync.SyncError;
import dirsyncpro.tools.FileTools;
import dirsyncpro.tools.TextFormatTool;

/**
 * Represents a file pair to synchronize.
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class SyncPair{
	
	private File fileA;
	private long dateA;
	private long sizeA;
	private File fileB;
	private long dateB;
	private long sizeB;
	private boolean fileAExists;
	private boolean fileBExists;
	private boolean fileAIsDir;
	private boolean fileBIsDir;
	private Const.SyncPairStatus syncPairStatus;
	private boolean synced;
	private Job job;
	private Icon icon = null; 
	
	public SyncPair(File a, File b, boolean fAEx, boolean fBEx, boolean fAD, boolean fBD, long fADate, long fBDate, long fAS, long fBS, SyncPairStatus sps, boolean ab, Job j){
		fileA = a;
		fileB = b;
		sizeA = fAS;
		sizeB = fBS;
		dateA = fADate;
		dateB = fBDate;
		fileAExists = fAEx;
		fileBExists = fBEx;
		fileAIsDir = fAD;
		fileBIsDir = fBD;
		synced = false;
		icon = null;
		syncPairStatus = sps;
		job = j;
		if (icon == null){
			//if the icon is already saved, don't delete it.
			if (fileAExists){
				try{
					icon = FileSystemView.getFileSystemView().getSystemIcon(fileA);
				}catch (Exception e){
					icon = null;
				}
			}else{
				try{
					icon = FileSystemView.getFileSystemView().getSystemIcon(fileB);
				}catch (Exception e){
					icon = null;
				}
			}
		}
	}

	public File getFileA() {
		return fileA;
	}

	public File getFileB() {
		return fileB;
	}

	public SyncPairStatus getSyncPairStatus() {
		return syncPairStatus;
	}

	protected void setSyncPairStatus(Const.SyncPairStatus syncPairStatus) {
		this.syncPairStatus = syncPairStatus;
	}

	public void setFileA(File fileA) {
		this.fileA = fileA;
	}

	public void setFileB(File fileB) {
		this.fileB = fileB;
	}
	
	public void setSynced(){
		this.synced = true;

		switch(syncPairStatus){
			case FileACopyForced:
			case FileAIsNew:
			case FileAIsModified:
			case FileAIsLarger:
			case FileAIsLargerAndModified:
			case DirACopyForced:
			case DirAIsNew:
			case DirAIsModified:
			case FileAIsSymLink:
			case DirAIsSymLink:
			case FileBIsRedundant:
			case DirBIsRedundant:
				readAttributesB();
				break;
			case FileBCopyForced:
			case FileBIsNew:
			case FileBIsModified:
			case FileBIsLarger:
			case FileBIsLargerAndModified:
			case DirBCopyForced:
			case DirBIsNew:
			case DirBIsModified:
			case FileBIsSymLink:
			case DirBIsSymLink:
			case FileAIsRedundant:
			case DirAIsRedundant:
				readAttributesA();
				break;
			case ConflictResolutionModified:
			case ConflictResolutionRename:
			case ConflictResolutionWarn:
				readAttributesA();
				readAttributesB();
				break;
		}
			
	}

	private void readAttributesA(){
		BasicFileAttributes attr = null;
		try{
			attr = Files.readAttributes(fileA.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			fileBExists = true;
		}catch (FileNotFoundException | NoSuchFileException e){
			fileAExists = false;
		}catch (IOException e){
			DirSyncPro.getSync().getLog().printMinimal("I/O Error: " + e.getMessage(), IconKey.Error);
		}
		fileAIsDir = (fileAExists ? attr.isDirectory() : false);
		dateA = (fileAExists ? attr.lastModifiedTime().toMillis() : 0);
		sizeA = (fileAExists ? attr.size() : 0);
	}
	
	private void readAttributesB(){
		BasicFileAttributes attr = null;
		try{
			attr = Files.readAttributes(fileB.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			fileBExists = true;
		}catch (FileNotFoundException | NoSuchFileException e){
			fileBExists = false;
		}catch (IOException e){
			DirSyncPro.getSync().getLog().printMinimal("I/O Error: " + e.getMessage(), IconKey.Error);
		}
		fileBIsDir = (fileBExists ? attr.isDirectory() : false);
		dateB = (fileBExists ? attr.lastModifiedTime().toMillis() : 0);
		sizeB = (fileBExists ? attr.size() : 0);
	}
	
	public boolean isSynced(){
		return synced;
	}

	/**
	 * @return the deletedFileIcon
	 */
	public Icon getIconA() {
		if (syncPairStatus == SyncPairStatus.FileAIsRedundant || syncPairStatus == SyncPairStatus.DirAIsRedundant){
			return icon;
		}else if (fileAExists){
			return icon;
		}else{
			return null;
		}
	}

	public Icon getIconB() {
		if (syncPairStatus == SyncPairStatus.FileBIsRedundant || syncPairStatus == SyncPairStatus.DirBIsRedundant){
			return icon;
		}else if (fileBExists){
			return icon;
		}else{
			return null;
		}
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}
	
	public long getDateA() {
		return dateA;
	}

	public long getSizeA() {
		return sizeA;
	}

	public long getDateB() {
		return dateB;
	}

	public long getSizeB() {
		return sizeB;
	}

	public boolean isFileAExists() {
		return fileAExists;
	}

	public void setFileAExists(boolean fileAExists) {
		this.fileAExists = fileAExists;
	}

	public boolean isFileBExists() {
		return fileBExists;
	}

	public void setFileBExists(boolean fileBExists) {
		this.fileBExists = fileBExists;
	}
	
}
