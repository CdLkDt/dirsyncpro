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

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import dirsyncpro.Const.CopyMode;
import dirsyncpro.Const.SymLinkMode;
import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterSet;

/**
 * Represents a queue of SyncPairs.
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class SyncQ{
	
	private ArrayList<SyncPair> syncQ;
	private ArrayList<SyncPair> syncQViewFiltered;
	private ArrayList<SyncPair> syncQSyncFiltered;
	private HashMap<CopyMode, Boolean> viewFilterMode;
	private HashMap<CopyMode, Boolean> syncFilterMode;
	private boolean syncFiltered;
	private boolean viewFiltered;
	
	private int totalAnalyzedFiles;
	private int totalAnalyzedDirs;
	private int countFiles;
	private int countForcedCopyFiles;
	private int countNewFiles;
	private int countModifiedFiles;
	private int countRedundantFiles;
	private int countLargerFiles;
	private int countLargerAndModifiedFiles;
	private int countConflictFiles;
	private int countDirs;
	private int countForcedCopyDirs;
	private int countNewDirs;
	private int countModifiedDirs;
	private int countRedundantDirs;
	private long bytesForcedCopyFiles;
	private long bytesNewFiles;
	private long bytesModifiedFiles;
	private long bytesRedundantFiles;
	private long bytesLargerFiles;
	private long bytesLargerAndModifiedFiles;

	private boolean synced = false;
	private boolean updated = false;
	
	public SyncQ(){
		syncQ = new ArrayList<SyncPair>();
		countFiles = 0;
		countDirs = 0;
		totalAnalyzedFiles = 0;
		totalAnalyzedDirs = 0;
		countForcedCopyFiles = 0;
		countNewFiles = 0;
		countConflictFiles = 0;
		countModifiedFiles = 0;
		countLargerFiles = 0;
		countLargerAndModifiedFiles = 0;
		countRedundantFiles = 0;
		countForcedCopyDirs = 0;
		countNewDirs = 0;
		countModifiedDirs = 0;
		countRedundantDirs = 0;
		bytesForcedCopyFiles = 0;
		bytesNewFiles = 0;
		bytesModifiedFiles = 0;
		bytesRedundantFiles = 0;
		bytesLargerFiles = 0;
		bytesLargerAndModifiedFiles = 0;
		synced = false;
		updated = true;
		initSyncQViewFilter();
		initSyncQSyncFilter();
	}
	
	public int size(){
		if (syncFiltered){
			return syncQSyncFiltered.size();
		}else{
			return syncQ.size();
		}
	}
	
	public SyncPair get(int i){
		if (syncFiltered){
			return syncQSyncFiltered.get(i);
		}else{
			return syncQ.get(i);
		}
	}
	
	public SyncPair getFilteredView(int i){
		if (viewFiltered){
			return syncQViewFiltered.get(i);
		}else{
			return this.get(i);
		}
	}

	public int viewSize(){
		if (viewFiltered){
			return syncQViewFiltered.size();
		}else{
			return this.size();
		}
	}
	
	public void viewFilter(){
		if (
				getSyncQViewFilterMode(CopyMode.New) && 
				getSyncQViewFilterMode(CopyMode.Modified) && 
				getSyncQViewFilterMode(CopyMode.Larger) && 
				getSyncQViewFilterMode(CopyMode.LargerAndModified) &&
				getSyncQViewFilterMode(CopyMode.DeleteDirs) &&
				getSyncQViewFilterMode(CopyMode.DeleteFiles) &&
				getSyncQViewFilterMode(CopyMode.ConflictFiles)
				){
			viewFiltered = false;
			syncQViewFiltered = null;
		} else{
			viewFiltered = true;
			syncQViewFiltered = new ArrayList<SyncPair>();
			for (int i = 0; i< this.size(); i++){
				SyncPair sp = this.get(i);
				SyncPairStatus sps = sp.getSyncPairStatus();
				if (getSyncQViewFilterMode(sps.matchingCopyMode())){
					syncQViewFiltered.add(sp);
				}
			}
		}
	}
	
	public void cloneSyncSyncQSyncFilter(){
		SyncQ dspSyncQ = DirSyncPro.getSync().getSyncQ();
		syncFilterMode.put (CopyMode.New, dspSyncQ.getSyncQSyncFilterMode(CopyMode.New));
		syncFilterMode.put (CopyMode.Modified, dspSyncQ.getSyncQSyncFilterMode(CopyMode.Modified));
		syncFilterMode.put (CopyMode.Larger, dspSyncQ.getSyncQSyncFilterMode(CopyMode.Larger));
		syncFilterMode.put (CopyMode.LargerAndModified, dspSyncQ.getSyncQSyncFilterMode(CopyMode.LargerAndModified));
		syncFilterMode.put (CopyMode.DeleteFiles, dspSyncQ.getSyncQSyncFilterMode(CopyMode.DeleteFiles));
		syncFilterMode.put (CopyMode.DeleteDirs, dspSyncQ.getSyncQSyncFilterMode(CopyMode.DeleteDirs));
		syncFilterMode.put (CopyMode.ConflictFiles, dspSyncQ.getSyncQSyncFilterMode(CopyMode.ConflictFiles));
	}
	
	public void syncFilter(){
		if (
				getSyncQSyncFilterMode(CopyMode.New) && 
				getSyncQSyncFilterMode(CopyMode.Modified) && 
				getSyncQSyncFilterMode(CopyMode.Larger) && 
				getSyncQSyncFilterMode(CopyMode.LargerAndModified) &&
				getSyncQSyncFilterMode(CopyMode.DeleteDirs) &&
				getSyncQSyncFilterMode(CopyMode.DeleteFiles) &&
				getSyncQSyncFilterMode(CopyMode.ConflictFiles)
				){
			syncFiltered = false;
			syncQSyncFiltered = null;
		} else{
			syncFiltered = true;
			syncQSyncFiltered = new ArrayList<SyncPair>();
			for (int i = 0; i< syncQ.size(); i++){
				SyncPair sp = syncQ.get(i);
				SyncPairStatus sps = sp.getSyncPairStatus();
				if (getSyncQSyncFilterMode(sps.matchingCopyMode())){
					syncQSyncFiltered.add(sp);
				}
			}
		}
		updated = true;
	}

	public void syncFilterByDSPFilter(){
		for (int i  = 0; i < syncQ.size(); i++){
			SyncPair sp = syncQ.get(i);
			FilterSet filters = sp.getJob().getFilterSet();
			if (filters.matchesAnyExcludeFilter(sp.getFileA().toPath())){
				syncQ.remove(i);
				i--;
			}
		}
			
		if (
				getSyncQSyncFilterMode(CopyMode.New) && 
				getSyncQSyncFilterMode(CopyMode.Modified) && 
				getSyncQSyncFilterMode(CopyMode.Larger) && 
				getSyncQSyncFilterMode(CopyMode.LargerAndModified) &&
				getSyncQSyncFilterMode(CopyMode.DeleteDirs) &&
				getSyncQSyncFilterMode(CopyMode.DeleteFiles) &&
				getSyncQSyncFilterMode(CopyMode.ConflictFiles)
				){
			syncFiltered = false;
			syncQSyncFiltered = null;
		} else{
			syncFiltered = true;
			syncQSyncFiltered = new ArrayList<SyncPair>();
			for (int i = 0; i< syncQ.size(); i++){
				SyncPair sp = syncQ.get(i);
				SyncPairStatus sps = sp.getSyncPairStatus();
				if (getSyncQSyncFilterMode(sps.matchingCopyMode())){
					syncQSyncFiltered.add(sp);
				}
			}
		}
		updated = true;
	}
	
	public void filterSymLinks(){
		for (int i  = 0; i < syncQ.size(); i++){
			SyncPair sp = syncQ.get(i);
			SymLinkMode slm = sp.getJob().getSymLinkMode();
			if (slm == SymLinkMode.SkipSymLinks &&
					(
							(sp.getSyncPairStatus().isAB() && Files.isSymbolicLink(sp.getFileA().toPath())) ||
							(!sp.getSyncPairStatus().isAB() && Files.isSymbolicLink(sp.getFileB().toPath()))
					)
				){
				syncQ.remove(i);
				i--;
			}else if (slm == SymLinkMode.CopySymLinks &&
					(
							(sp.getSyncPairStatus().isAB() && Files.isSymbolicLink(sp.getFileA().toPath())) ||
							(!sp.getSyncPairStatus().isAB() && Files.isSymbolicLink(sp.getFileB().toPath()))
					)
				){
				if (sp.getSyncPairStatus().isFileOperation()){
					sp.setSyncPairStatus(sp.getSyncPairStatus().isAB() ? SyncPairStatus.FileAIsSymLink : SyncPairStatus.FileBIsSymLink);
				}else{
					sp.setSyncPairStatus(sp.getSyncPairStatus().isAB() ? SyncPairStatus.DirAIsSymLink : SyncPairStatus.DirBIsSymLink);
				}
			}else{
				// do nothing, the symlink is followed by default (copied as file).
			}
		}
	}
	
	public boolean add(SyncPair sp){
		recalculateStats(sp, +1);
		updated = true;
		return syncQ.add(sp);
	}

	/**
	 * changes the status of all Sync Pairs with the same stat and updates the stats in SyncQ
	 * @param sp the Sync Pair as template to change
	 * @param spsTo the new status
	 */
	public void changeAll(SyncPair sp, SyncPairStatus spsTo){
		SyncPairStatus sps = sp.getSyncPairStatus();
		for (SyncPair spWalker : syncQ){
			if (spWalker.getSyncPairStatus() == sps){
				change(spWalker, spsTo);
			}
		}
		updated = true;
	}
	
	private void recalculateStats(SyncPair sp, int factor){
		SyncPairStatus sps = sp.getSyncPairStatus();
		switch(sps){
			case FileACopyForced:
				countForcedCopyFiles += factor;
				countFiles += factor;
				bytesForcedCopyFiles += factor * sp.getSizeA();
				break;
			case FileBCopyForced:
				countForcedCopyFiles += factor;
				countFiles += factor;
				bytesForcedCopyFiles += factor * sp.getSizeB();
				break;
			case FileAIsNew:
				countNewFiles += factor;
				countFiles += factor;
				bytesNewFiles += factor * sp.getSizeA();
				break;
			case FileBIsNew:
				countNewFiles += factor;
				countFiles += factor;
				bytesNewFiles += factor * sp.getSizeB();
				break;
			case FileAIsModified:
				countModifiedFiles += factor;
				countFiles += factor;
				bytesModifiedFiles += factor * sp.getSizeA();
				break;
			case FileBIsModified:
				countModifiedFiles += factor;
				countFiles += factor;
				bytesModifiedFiles += factor * sp.getSizeB();
				break;
			case FileAIsLarger:
				countLargerFiles += factor;
				countFiles += factor;
				bytesLargerFiles += factor * sp.getSizeA();
				break;
			case FileBIsLarger:
				countLargerFiles += factor;
				countFiles += factor;
				bytesLargerFiles += factor * sp.getSizeB();
				break;
			case FileAIsLargerAndModified:
				countLargerAndModifiedFiles += factor;
				countFiles += factor;
				bytesLargerAndModifiedFiles += factor * sp.getSizeA();
				break;
			case FileBIsLargerAndModified:
				countLargerAndModifiedFiles += factor;
				countFiles += factor;
				bytesLargerAndModifiedFiles += factor * sp.getSizeB();
				break;
			case FileAIsRedundant:
				countRedundantFiles += factor;
				countFiles += factor;
				bytesRedundantFiles += factor * sp.getSizeA();
				break;
			case FileBIsRedundant:
				countRedundantFiles += factor;
				countFiles += factor;
				bytesRedundantFiles += factor * sp.getSizeB();
				break;
			case DirACopyForced:
			case DirBCopyForced:
				countForcedCopyDirs += factor;
				countDirs += factor;
				break;
			case DirAIsNew:
			case DirBIsNew:
				countNewDirs += factor;
				countDirs += factor;
				break;
			case DirAIsModified:
			case DirBIsModified:
				countModifiedDirs += factor;
				countDirs += factor;
				break;
			case DirAIsRedundant:
			case DirBIsRedundant:
				countRedundantDirs += factor;
				countDirs += factor;
				break;
			case ConflictResolutionModified:
			case ConflictResolutionRename:
			case ConflictResolutionWarn:
				countConflictFiles += factor;
				break;
		}
	}
	
	/**
	 * changes the status of a Sync Pair and updates the stats in SyncQ
	 * @param sp the Sync Pair to change
	 * @param spsTo the new status
	 */
	public void change(SyncPair sp, SyncPairStatus spsTo){
		// lower the stats
		recalculateStats(sp, -1);
		//higher the stats
		sp.setSyncPairStatus(spsTo);
		recalculateStats(sp, 1);
		updated = true;
	}
	
	public boolean appendToSyncQ(SyncQ sq){
		this.countFiles += sq.getCountFiles();
		this.countRedundantFiles += sq.getCountRedundantFiles();
		this.countLargerFiles += sq.getCountLargerFiles();
		this.countLargerAndModifiedFiles += sq.getCountLargerAndModifiedFiles();
		this.countModifiedFiles += sq.getCountModifiedFiles();
		this.countNewFiles += sq.getCountNewFiles();
		this.countForcedCopyFiles += sq.getCountForcedCopyFiles();

		this.countDirs += sq.getCountDirs();
		this.countRedundantDirs += sq.getCountRedundantDirs();
		this.countModifiedDirs += sq.getCountModifiedDirs();
		this.countNewDirs += sq.getCountNewDirs();
		this.countConflictFiles += sq.getCountConflictFiles();
		this.countForcedCopyDirs += sq.getCountForcedCopyDirs();

		updated = true;
		return syncQ.addAll(sq.syncQ);
	}
	
	/**
	 * 
	 * @return total number of files
	 */
	public int getCountFiles() {
		return countFiles;
	}

	/**
	 * 
	 * @return total number of dirs
	 */
	public int getCountDirs() {
		return countDirs;
	}

	/**
	 * @param number the totalAnanlyzedFiles to add to
	 */
	public void addTotalAnalyzedFiles(int number) {
		this.totalAnalyzedFiles += number;
		updated = true;
	}

	/**
	 * @param number the totalAnanlyzedDirs to add to
	 */
	public void addTotalAnalyzedDirs(int number) {
		this.totalAnalyzedDirs += number;
		updated = true;
	}

	/**
	 * @return the countForcedCopyFiles
	 */
	public int getCountForcedCopyFiles() {
		return countForcedCopyFiles;
	}

	/**
	 * @return the countNewFiles
	 */
	public int getCountNewFiles() {
		return countNewFiles;
	}

	/**
	 * @return the countModifiedFiles
	 */
	public int getCountModifiedFiles() {
		return countModifiedFiles;
	}

	/**
	 * @return the countLargerFiles
	 */
	public int getCountLargerFiles() {
		return countLargerFiles;
	}

	/**
	 * @return the countLargerAndModifiedFiles
	 */
	public int getCountLargerAndModifiedFiles() {
		return countLargerAndModifiedFiles;
	}

	/**
	 * @return the countRedundantFiles
	 */
	public int getCountRedundantFiles() {
		return countRedundantFiles;
	}

	/**
	 * @return the countForcedCopyDirs
	 */
	public int getCountForcedCopyDirs() {
		return countForcedCopyDirs;
	}

	/**
	 * @return the countNewDirs
	 */
	public int getCountNewDirs() {
		return countNewDirs;
	}

	/**
	 * @return the countModifiedDirs
	 */
	public int getCountModifiedDirs() {
		return countModifiedDirs;
	}

	/**
	 * @return the countRedundantDirs
	 */
	public int getCountRedundantDirs() {
		return countRedundantDirs;
	}

	/**
	 * initializes the viewFilterMode to defaults
	 */
	public void initSyncQViewFilter(){
		viewFilterMode = new HashMap<CopyMode, Boolean>();
		viewFilterMode.put (CopyMode.New, true);
		viewFilterMode.put (CopyMode.Modified, true);
		viewFilterMode.put (CopyMode.Larger, true);
		viewFilterMode.put (CopyMode.LargerAndModified, true);
		viewFilterMode.put (CopyMode.DeleteFiles, true);
		viewFilterMode.put (CopyMode.DeleteDirs, true);
		viewFilterMode.put (CopyMode.ConflictFiles, true);
		viewFilter();
	}

	/**
	 * initializes the syncFilterMode to defaults
	 */
	public void initSyncQSyncFilter(){
		syncFilterMode = new HashMap<CopyMode, Boolean>();
		syncFilterMode.put (CopyMode.New, true);
		syncFilterMode.put (CopyMode.Modified, true);
		syncFilterMode.put (CopyMode.Larger, true);
		syncFilterMode.put (CopyMode.LargerAndModified, true);
		syncFilterMode.put (CopyMode.DeleteFiles, true);
		syncFilterMode.put (CopyMode.DeleteDirs, true);
		syncFilterMode.put (CopyMode.ConflictFiles, true);
		updated = true;
		syncFilter();
	}
	
	/**
	 * 
	 * @param cm the CopyMode
	 * @return boolean if the view filter for cm is set
	 */
	public boolean getSyncQViewFilterMode(CopyMode cm){
		return viewFilterMode.get(cm);
	}
	
	/**
	 * 
	 * @param cm the CopyMode to set the value for
	 * @param b boolean value to set
	 */
	public void setSyncQViewFilterMode(CopyMode cm, boolean b){
		viewFilterMode.put(cm, b);
	}

	/**
	 * 
	 * @param cm the CopyMode
	 * @return boolean if the sync filter for cm is set
	 */
	public boolean getSyncQSyncFilterMode(CopyMode cm){
		return syncFilterMode.get(cm);
	}
	
	/**
	 * 
	 * @param cm the CopyMode to set the value for
	 * @param b boolean value to set
	 */
	public void setSyncQSyncFilterMode(CopyMode cm, boolean b){
		syncFilterMode.put(cm, b);
	}

	/**
	 * @return the synced
	 */
	public boolean isSynced() {
		return synced;
	}

	/**
	 * @param synced the synced to set
	 */
	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public void deleteAllOfKind(SyncPair sp){
		SyncPairStatus sps = sp.getSyncPairStatus(); 
		for (int i = 0; i< this.size(); i++){
			SyncPair spi = this.get(i);
			if (spi.getSyncPairStatus() == sps){
				this.delete(spi);
				i--;
			}
		}
		updated = true;
	}
	
	public void delete(SyncPair sp){
		recalculateStats(sp, -1);
		updated = true;
		syncQ.remove(sp);
	}

	public int getTotalAnalyzedFiles() {
		return totalAnalyzedFiles;
	}

	public int getTotalAnalyzedDirs() {
		return totalAnalyzedDirs;
	}

	public int getCountConflictFiles() {
		return countConflictFiles;
	}

	public boolean isUpdated() {
	    return updated;
	}

	public void setUpdated(boolean updated) {
	    this.updated = updated;
	}

	public long getBytesFiles() {
		return bytesForcedCopyFiles +  bytesNewFiles + bytesModifiedFiles + bytesRedundantFiles + bytesLargerFiles + bytesLargerAndModifiedFiles;
	}

	public long getBytesForcedCopyFiles() {
		return bytesForcedCopyFiles;
	}

	public long getBytesNewFiles() {
		return bytesNewFiles;
	}

	public long getBytesModifiedFiles() {
		return bytesModifiedFiles;
	}

	public long getBytesRedundantFiles() {
		return bytesRedundantFiles;
	}

	public long getBytesLargerFiles() {
		return bytesLargerFiles;
	}

	public long getBytesLargerAndModifiedFiles() {
		return bytesLargerAndModifiedFiles;
	}
}
