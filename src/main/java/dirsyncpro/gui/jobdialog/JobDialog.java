/* JobDialog.java
 *
 * Copyright (C) 2008-2017 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2005 T. Groetzner 
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

package dirsyncpro.gui.jobdialog;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import dirsyncpro.Const;
import dirsyncpro.Const.IconKey;
import dirsyncpro.Const.SymLinkMode;
import dirsyncpro.Const.SyncConflictResolutionMode;
import dirsyncpro.Const.SyncMode;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.filterdialog.FilterDialog;
import dirsyncpro.gui.jobdialog.filtertree.FiltersTree;
import dirsyncpro.gui.jobdialog.filtertree.FiltersTreeModel;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPattern;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterGroup;
import dirsyncpro.gui.jobdialog.scheduledialog.ScheduleDialog;
import dirsyncpro.gui.jobdialog.scheduletree.ScheduleTree;
import dirsyncpro.gui.jobdialog.scheduletree.ScheduleTreeModel;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.swing.MyJTabbedPane;
import dirsyncpro.job.Job;
import dirsyncpro.tools.FileTools;
import dirsyncpro.tools.GuiTools;

/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
//@SuppressWarnings("unused")
public class JobDialog extends JobDialogObjects {
	
    	private static final int BASIC_TAB_NO = 0;
    	private static final int COMPARE_TAB_NO = 1;
    	private static final int COPY_TAB_NO = 2;
    	private static final int CONFLICT_RESOLUTION_TAB_NO = 3;
    	private static final int CONFLICT_RESOLUTION_MONO_SUBTAB_NO = 0;
    	private static final int CONFLICT_RESOLUTION_BI_SUBTAB_NO = 1;
    	private static final int FILTERS_TAB_NO = 4;
    	private static final int DELETION_TAB_NO = 5;
    	private static final int BACKUP_TAB_NO = 6;
    	private static final int LOG_TAB_NO = 7;
    	private static final int SCHEDULE_TAB_NO = 8;
    	private static final int ADVANCED_TAB_NO = 9;
    	private static final int ACTIONS_TAB_NO = 10;
    
	private FilterDialog filterDialog = new FilterDialog(this);
	private ScheduleDialog scheduleDialog = new ScheduleDialog(this);

	private static Vector<Schedule> schedules = new Vector<Schedule>();
	private Job job;

	// temp vars for within the gui
	private Vector<Filter> filters;

	private String prevPattern;

	public JobDialog(JFrame frame){
        super(frame);
        syncModeComboBox.setModel(new javax.swing.DefaultComboBoxModel<SyncMode>(Const.SyncMode.values()));
        SyncModeComboboxCellRenderer smcbr = new SyncModeComboboxCellRenderer(); 
        syncModeComboBox.setRenderer(smcbr);
	}
	
	protected void syncModeComboBoxClicked(){
		job.setSyncMode((Const.SyncMode) syncModeComboBox.getSelectedItem());
		initJobDialog(job);
	}
	
	protected void setFileDragNDrop(final JTextField jtf) {
		jtf.setDropTarget(new DropTarget() {
			@Override
			public synchronized void drop(DropTargetDropEvent dtde) {
				try {
					Transferable transfer = dtde.getTransferable();
					if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
						List objects = (List) transfer.getTransferData(DataFlavor.javaFileListFlavor);
						for (Object obj : objects) {
							if (obj instanceof File) {
								File f = (File) obj;
								if (f.isDirectory()){
									jtf.setText(f.getAbsolutePath());
								}
							}
						}
					}
				} catch (UnsupportedFlavorException ex) {
					DirSyncPro.getSync().getLog().printMinimal("Drag and Drop failed! " + ex.getMessage(), IconKey.Error);
				} catch (IOException ex) {
					DirSyncPro.getSync().getLog().printMinimal("Drag and Drop failed! " + ex.getMessage(),	IconKey.Error);
				} catch (Exception ex) {
					DirSyncPro.getSync().getLog().printMinimal("Drag and Drop failed! " + ex.getMessage(),	IconKey.Error);
					System.out.println(ex.getMessage());
				} finally {
					dtde.dropComplete(true);
				}
			}
		});
	}
    
    protected void jobEnableLoggingCheckBoxClicked(){
		boolean enabled = dirEnableLoggingCheckBox.isSelected();
		
		dirLogField.setEnabled(enabled);
		dirLogLabel.setEnabled(enabled);
		dirLogBrowseButton.setEnabled(enabled);
		String s = "";
		if (enabled){
			Job job = DirSyncPro.getGui().getSelectedJob();
			//first try if the log is already initialized. If yes get the name there. This is needed because of a probable wildcard.
			s = job.getLog().getFilename();
			if (s.equals("")) {
				s = DirSyncPro.getLogsPath(true) + FileTools.replaceIllegalCharactersInFileName(job.getName()) + "." + Const.LOG_FILE_EXTENSION;				
			}
		}
		dirLogField.setText(s);
	}
	
	public void initJobDialog(Job job) {
		this.job = job;
		dirNameField.setText(job.getName());
		dirSrcField.setText(job.getDirA());
		dirDstField.setText(job.getDirB());

		if (job.getSyncMode() != (Const.SyncMode) syncModeComboBox.getSelectedItem()){
			syncModeComboBox.setSelectedItem(job.getSyncMode());
		}
		
		boolean enabled = job.getLog().isEnabled();
		dirEnableLoggingCheckBox.setSelected(enabled);
		dirLogField.setEnabled(enabled);
		dirLogLabel.setEnabled(enabled);
		dirLogBrowseButton.setEnabled(enabled);
		if (!job.getLog().getFilename().equals("")) {
			//dirLogField.setText(job.getLog().getParentPath() + File.separator + job.getLog().getFilename());
			dirLogField.setText(job.getLog().getFilename());
		}else {
			dirLogField.setText("");
		}
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(dirLogField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));

		dirWithSubfoldersCheckBox.setSelected(job.isRecursive());
		dirVerifyCheckBox.setSelected(job.isVerify());
		
		realtimeSyncCheckBox.setSelected(job.isSyncRealtime());
		realtimeSyncOnStartCheckBox.setSelected(job.isSyncRealtimeOnStart());
		realtimeSyncDelayField.setText(job.getSyncRealtimeDelay() + "");
		adjustRealtimeSyncOptions();

		if(job.getCompareMode() == Const.CompareMode.CompareFileSizesDates){
		    compareFileSizesDatesRadioButton.setSelected(true);
		}
		if(job.getCompareMode() == Const.CompareMode.CompareFileSizesDatesMetaData){
		    compareFileSizesDatesMetaDataRadioButton.setSelected(true);
		}
		if(job.getCompareMode() == Const.CompareMode.CompareFileContents){
		    compareFileContentsRadioButton.setSelected(true);
		}
		
		dirCopyAllCheckBox.setSelected(job.isCopyAll());
		dirCopyNewCheckBox.setSelected(job.isCopyNew());
		dirCopyModifiedCheckBox.setSelected(job.isCopyModified());
		dirCopyLargerCheckBox.setSelected(job.isCopyLarger());
		dirCopyLargerModifiedCheckBox.setSelected(job.isCopyLargerModified());

		dirDeleteFilesCheckBox.setSelected(job.isDelFiles());
		dirDeleteDirsCheckBox.setSelected(job.isDelDirs());
		deleteExcludedFilesACheckBox.setSelected(job.isDeleteExcludedFilesA());
		deleteExcludedDirsACheckBox.setSelected(job.isDeleteExcludedDirsA());
		deleteExcludedFilesBCheckBox.setSelected(job.isDeleteExcludedFilesB());
		deleteExcludedDirsBCheckBox.setSelected(job.isDeleteExcludedDirsB());
		
		if (job.getSyncMode().isBI()){
			if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.CopyModified){
				bidirectionalConflictCopyModifiedRadioButton.setSelected(true);
			} else if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.CopyLarger){
				bidirectionalConflictCopyLargerRadioButton.setSelected(true);
			} else if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.CopyRenamed){
				bidirectionalConflictRenameCopyRadioButton.setSelected(true);
			}else if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.WarnUser){
				bidirectionalConflictWarnUserRadioButton.setSelected(true);
			}else if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.Skip){
				bidirectionalConflictSkipRadioButton.setSelected(true);
			}
		}else{
			if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.CopySource){
				monodirectionalConflictCopySourceRadioButton.setSelected(true);
			}else if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.WarnUser){
				monodirectionalConflictWarnUserRadioButton.setSelected(true);
			}if (job.getSyncConflictResolutionMode() == Const.SyncConflictResolutionMode.Skip){
				monodirectionalConflictSkipRadioButton.setSelected(true);
			}
		}
		
		dirBackupField.setText(job.getHowManyBackups() + "");
		dirBackupDirInlineCheckBox.setSelected(job.isBackupInline());
		dirBackupDirField.setText(job.getBackupDir());
		
		dirTimestampWriteBackCheckBox.setSelected(job.isWriteTimestampBack());
		dirTimestampSyncCheckBox.setSelected(job.isSyncDirTimeStamps());
		ignoreDaylightSavingTimeCheckBox.setSelected(job.isIgnoreDaylightSavingGranularity());
		dirTimestampDiffField.setText(job.getGranularity() + "");
		if (job.getSymLinkMode() == SymLinkMode.SkipSymLinks){
			skipSymLinkRadioButton.setSelected(true);
		}else if (job.getSymLinkMode() == SymLinkMode.CopySymLinks){
			copySymLinkRadioButton.setSelected(true);
		}else{
			followSymLinkRadioButton.setSelected(true);
		}
		
		boolean windows = Const.OS_IS_WINDOWS;
		preserveDOSAttributesCheckBox.setEnabled(windows);
		preserveDOSAttributesLabel.setEnabled(windows);
		preserveFilePermissionsCheckBox.setEnabled(!windows);
		preserveFilePermissionsLabel.setEnabled(!windows);
		preserveFileOwnershipCheckBox.setEnabled(!windows);
		preserveFileOwnershipLabel.setEnabled(!windows);
		
		preserveDOSAttributesCheckBox.setSelected(job.isPreserveDOSAttributes());
		preserveFilePermissionsCheckBox.setSelected(job.isPreservePOSIXFilePermissions());
		preserveFileOwnershipCheckBox.setSelected(job.isPreservePOSIXFileOwnership());
		overrideReadOnlyCheckBox.setSelected(job.isOverrideReadOnly());
		
		arrangeJobDialogTabs();
		
		// if custom, make copies of the filters to edit locally and save on 'apply'
		// init otherwise
		initFilters(((SyncMode)syncModeComboBox.getSelectedItem()).isCustom(), job);
		schedules = (Vector<Schedule>) job.getSchedules().clone();
		
		updateFiltersTree();
		updateScheduleTree();
		
		enableEditRemoveFilterButtons(false);
		
		enableEditRemoveScheduleButtons(false);
		
		//DirSyncPro.getGui().setToJobTreeTab();
		this.setTitle("Edit: " + job.getName());
	}
	
	public void setToBasicOptions(){
		int tabNumber = BASIC_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	public void setToCompareOptions(){
		int tabNumber = COMPARE_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}
	
	public void setToCopyOptions(){
		int tabNumber = COPY_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}
	
	public void setToConflictOptions(){
		int tabNumber = CONFLICT_RESOLUTION_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	public void setToFilterOptions(){
		int tabNumber = FILTERS_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}
	
	
	public void setToDeleteOptions(){
		int tabNumber = DELETION_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}
	
	public void setToBackupOptions(){
		int tabNumber = BACKUP_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	public void setToLogOptions(){
		int tabNumber = LOG_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	public void setToScheduleOptions(){
		int tabNumber = SCHEDULE_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	public void setToAdvancedOptions(){
		int tabNumber = ADVANCED_TAB_NO;
		if (jobTabbedPane.isEnabledAt(tabNumber)){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}else{
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(0);
			
		}
	}

	
	private void enableBasicOptions(boolean enabled, boolean selected){
		int tabNumber = BASIC_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableCompareOptions(boolean enabled, boolean selected){
		int tabNumber = COMPARE_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableCopyOptions(boolean enabled, boolean selected){
		int tabNumber = COPY_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableConflictOptions(boolean enabled, boolean selected){
		int tabNumber = CONFLICT_RESOLUTION_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	public void enableFilterOptions(boolean enabled, boolean selected){
		int tabNumber = FILTERS_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	
	private void enableDeleteOptions(boolean enabled, boolean selected){
		int tabNumber = DELETION_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableBackupOptions(boolean enabled, boolean selected){
		int tabNumber = BACKUP_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableLogOptions(boolean enabled, boolean selected){
		int tabNumber = LOG_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableScheduleOptions(boolean enabled, boolean selected){
		int tabNumber = SCHEDULE_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableAdvancedOptions(boolean enabled, boolean selected){
		int tabNumber = ADVANCED_TAB_NO;
		jobTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) jobTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableConflictOptionsMonoSubtab(boolean enabled, boolean selected){
		int tabNumber = CONFLICT_RESOLUTION_MONO_SUBTAB_NO;
		conflictResolutionJTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) conflictResolutionJTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	private void enableConflictOptionsBISubtab(boolean enabled, boolean selected){
		int tabNumber = CONFLICT_RESOLUTION_BI_SUBTAB_NO;
		conflictResolutionJTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) conflictResolutionJTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	
	/**
	 * enables/disables the Edit and Remove buttons for the edit filters dialog
	 * @param enabled
	 */
	public void enableEditRemoveFilterButtons(boolean enabled){
		editFilterButton.setEnabled(enabled);
		removeFilterButton.setEnabled(enabled);
	}
	
	/**
	 * enables/disables the Edit and Remove buttons for the edit schedules dialog
	 * @param enabled
	 */
	public void enableEditRemoveScheduleButtons(boolean enabled){
		editScheduleButton.setEnabled(enabled);
		removeScheduleButton.setEnabled(enabled);
	}

	protected void applyJobSettings() {
		Job job = DirSyncPro.getGui().getSelectedJob();

		job.setName(dirNameField.getText());
		job.setSrc(dirSrcField.getText());
		job.setDst(dirDstField.getText());
		job.setSyncMode((Const.SyncMode) syncModeComboBox.getSelectedItem());

		if (!dirEnableLoggingCheckBox.isSelected()){
			job.getLog().disable();
		}else if (!job.getLog().getPath().equals(dirLogField.getText())){
			try{
				job.getLog().setFile(dirLogField.getText());
			}catch (Exception e){
				throw new Error(e);
			}
		}

		job.setRecursive(dirWithSubfoldersCheckBox.isSelected());
		job.setVerify(dirVerifyCheckBox.isSelected());
		
		job.setSyncRealtime(realtimeSyncCheckBox.isSelected());
		job.setSyncRealtimeOnStart(realtimeSyncOnStartCheckBox.isSelected());
		job.setSyncRealtimeDelay(Integer.valueOf(realtimeSyncDelayField.getText()));

		if(compareFileSizesDatesRadioButton.isSelected()){
		    job.setCompareMode(Const.CompareMode.CompareFileSizesDates);
		}
		if(compareFileSizesDatesMetaDataRadioButton.isSelected()){
		    job.setCompareMode(Const.CompareMode.CompareFileSizesDatesMetaData);
		}
		if(compareFileContentsRadioButton.isSelected()){
		    job.setCompareMode(Const.CompareMode.CompareFileContents);
		}
		
		job.setCopyAll(dirCopyAllCheckBox.isSelected());
		job.setCopyNew(dirCopyNewCheckBox.isSelected());
		job.setCopyModified(dirCopyModifiedCheckBox.isSelected());
		job.setCopyLarger(dirCopyLargerCheckBox.isSelected());
		job.setCopyLargerModified(dirCopyLargerModifiedCheckBox.isSelected());

		job.setDelFiles(dirDeleteFilesCheckBox.isSelected());
		job.setDelDirs(dirDeleteDirsCheckBox.isSelected());
		job.setDeleteExcludedDirsA(deleteExcludedDirsACheckBox.isSelected());
		job.setDeleteExcludedFilesA(deleteExcludedFilesACheckBox.isSelected());
		job.setDeleteExcludedDirsB(deleteExcludedDirsBCheckBox.isSelected());
		job.setDeleteExcludedFilesB(deleteExcludedFilesBCheckBox.isSelected());

		job.getFilterSet().setFilters(filters);

		job.setSchedules(schedules);
		if (DirSyncPro.getSync().getScheduleEngine().isRunning()){
			DirSyncPro.getSync().getScheduleEngine().initSchedules();
		}
		
		if (job.getSyncMode().isBI()){
			if (bidirectionalConflictCopyModifiedRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.CopyModified);
			}else if (bidirectionalConflictCopyLargerRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.CopyLarger);
			}else if (bidirectionalConflictRenameCopyRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.CopyRenamed);
			}else if (bidirectionalConflictWarnUserRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.WarnUser);
			}else if (bidirectionalConflictSkipRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(Const.SyncConflictResolutionMode.Skip);
			}
		}else{
			if (monodirectionalConflictCopySourceRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(SyncConflictResolutionMode.CopySource);
			}else if (monodirectionalConflictWarnUserRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(SyncConflictResolutionMode.WarnUser);
			}if (monodirectionalConflictSkipRadioButton.isSelected()){
				job.setSyncConflictResolutionMode(SyncConflictResolutionMode.Skip);
			}
		}
		
		try {
			job.setHowManyBackups(Integer.valueOf(dirBackupField.getText()).intValue());
		} catch (NumberFormatException e) {
			job.setHowManyBackups(0);
		}
		dirBackupField.setText(job.getHowManyBackups() + "");
		job.setBackupInline(dirBackupDirInlineCheckBox.isSelected());
		job.setBackupDir(dirBackupDirField.getText());
		job.setGranularity(Integer.valueOf(dirTimestampDiffField.getText()).intValue());
		if (skipSymLinkRadioButton.isSelected()){
			job.setSymLinkMode(SymLinkMode.SkipSymLinks);
		}else if (copySymLinkRadioButton.isSelected()){
			job.setSymLinkMode(SymLinkMode.CopySymLinks);
		}else{
			job.setSymLinkMode(SymLinkMode.FollowSymLinks);
		}
		job.setPreserveDOSAttributes(preserveDOSAttributesCheckBox.isSelected());
		job.setPreservePOSIXFilePermissions(preserveFilePermissionsCheckBox.isSelected());
		job.setPreservePOSIXFileOwnership(preserveFileOwnershipCheckBox.isSelected());
		job.setOverrideReadOnly(overrideReadOnlyCheckBox.isSelected());
		job.setWriteTimestampBack(dirTimestampWriteBackCheckBox.isSelected());
		job.setIgnoreDaylightSavingGranularity(ignoreDaylightSavingTimeCheckBox.isSelected());
		job.setSyncDirTimeStamps(dirTimestampSyncCheckBox.isSelected());
		
		// if file exists display warning dialog
		if (DirSyncPro.getSync().getSyncQ().size() > 0 && (JOptionPane.showConfirmDialog(this,	"You have changed some options of this job. Do you want to reset the sync queue?\nIf Yes, you need to perform a new analysis.\nIf No, the sync queue will be updated according to any changes to the exclude filters.", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)){
			DirSyncPro.getSync().clearSyncQ();
		}else{
			DirSyncPro.getSync().getSyncQ().syncFilterByDSPFilter();
		}
		DirSyncPro.getGui().updateGUIEDT(false);

		
		DirSyncPro.getGui().updateJobsTree();
		DirSyncPro.getSync().setOptionsChanged(true);
		
		// this line must be after clearSyncQ otherwise the schedule thread could bump into a null syncQ 
		job.restartRealtimeListeners();
	}
	
	protected void verifyCopyOptionsInGUI() {
		// dir settings
		if (dirCopyAllCheckBox.isSelected()) {
			dirCopyNewCheckBox.setEnabled(false);
			dirCopyNewLabel.setEnabled(false);
			dirCopyModifiedCheckBox.setEnabled(false);
			dirCopyModifiedLabel.setEnabled(false);
			dirCopyLargerCheckBox.setEnabled(false);
			dirCopyLargerLabel.setEnabled(false);
			dirCopyLargerModifiedCheckBox.setEnabled(false);
			dirCopyLargerModifiedLabel.setEnabled(false);
		} else {
			dirCopyNewCheckBox.setEnabled(true);
			dirCopyNewLabel.setEnabled(true);
			dirCopyModifiedCheckBox.setEnabled(true);
			dirCopyModifiedLabel.setEnabled(true);
			dirCopyLargerCheckBox.setEnabled(true);
			dirCopyLargerLabel.setEnabled(true);
			dirCopyLargerModifiedCheckBox.setEnabled(true);
			dirCopyLargerModifiedLabel.setEnabled(true);

			if (dirCopyLargerCheckBox.isSelected() || dirCopyModifiedCheckBox.isSelected()) {
				dirCopyLargerModifiedCheckBox.setEnabled(false);
				dirCopyLargerModifiedLabel.setEnabled(false);
			} else {
				dirCopyLargerModifiedCheckBox.setEnabled(true);
				dirCopyLargerModifiedLabel.setEnabled(true);
			}
		}
	}
	
	protected void adjustRealtimeSyncOptions(){
		boolean selected = realtimeSyncCheckBox.isSelected();
		realtimeSyncOnStartCheckBox.setEnabled(selected);
		realtimeSyncDelayField.setEnabled(selected);
	}
	
	public void arrangeJobDialogTabs(){
		/**
		 * Dir in/excl	!SAD
		 * File in/excl	!SAD
		 * Log			!SAD
		 * Subs			!SAD
		 * Verify		!SAD
		 * Sync opts	!SAD && !BI
		 * Deletes		!SAD && !BI
		 * Backups		!SAD && !BI
		 * Backup Dir	!SAD && !BI && !Inline
		 * Conflicts	!SAD && BI
		 */
		
		boolean enabled;

		if (syncModeComboBox.getSelectedItem() == Const.SyncMode.ABMirror || syncModeComboBox.getSelectedItem() == Const.SyncMode.BAMirror){
			enableCopyOptions(false, false);
			enableDeleteOptions(false, false);

			//disabling filters re-inits them
			initFilters(false, null);
			enableFilterOptions(false, false);

			enableConflictOptions(true, false);
			enableConflictOptionsBISubtab(false, false);
			enableConflictOptionsMonoSubtab(true, true);
			enableBackupOptions(false, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);
			enableCompareOptions(true, false);
		}else if (syncModeComboBox.getSelectedItem() == Const.SyncMode.ABFull || syncModeComboBox.getSelectedItem() == Const.SyncMode.BAFull){
			enableCopyOptions(false, false);
			enableDeleteOptions(false, false);

			//disabling filters re-inits them
			initFilters(false, null);
			enableFilterOptions(false, false);

			enableConflictOptions(false, false);
			enableBackupOptions(false, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);
			enableCompareOptions(true, false);
		}else if (syncModeComboBox.getSelectedItem() == Const.SyncMode.ABContribute || syncModeComboBox.getSelectedItem() == Const.SyncMode.BAContribute){
			enableCopyOptions(false, false);
			enableDeleteOptions(false, false);

			//disabling filters re-inits them
			initFilters(false, null);
			enableFilterOptions(false, false);

			enableConflictOptions(false, false);
			enableBackupOptions(false, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);
			enableCompareOptions(false, false);
		}else if (syncModeComboBox.getSelectedItem() == Const.SyncMode.BIMirror){
			enableCopyOptions(false, false);
			enableDeleteOptions(false, false);

			//disabling filters re-inits them
			initFilters(false, null);
			enableFilterOptions(false, false);

			enableConflictOptions(true, false);
			enableConflictOptionsBISubtab(true, true);
			enableConflictOptionsMonoSubtab(false, false);
			enableBackupOptions(false, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);
			enableCompareOptions(true, false);
		}else if (syncModeComboBox.getSelectedItem() == Const.SyncMode.BICustom){
			enableCopyOptions(false, false);
			enableDeleteOptions(false, false);

			enableFilterOptions(true, false);

			enableConflictOptions(true, false);
			enableConflictOptionsBISubtab(true, true);
			enableConflictOptionsMonoSubtab(false, false);
			enableBackupOptions(false, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);		
			enableCompareOptions(true, false);
		}else if (syncModeComboBox.getSelectedItem() == Const.SyncMode.ABCustom || syncModeComboBox.getSelectedItem() == Const.SyncMode.BACustom){
			enableCopyOptions(true, false);
			enableConflictOptions(true, false);
			enableConflictOptionsMonoSubtab(true, true);
			enableConflictOptionsBISubtab(false, false);
			enableFilterOptions(true, false);
			enableDeleteOptions(true, false);
			enableBackupOptions(true, false);
			enableLogOptions(true, false);
			enableScheduleOptions(true, false);
			enableAdvancedOptions(true, false);
			enableCompareOptions(true, false);
		}
		verifyCopyOptionsInGUI();
	}
	
	protected void resetJobSettings() {
		Job job = DirSyncPro.getGui().getSelectedJob();
		job.init();
		initJobDialog(job);
		DirSyncPro.getGui().displayInfoDialog("Options are reset. Press OK to apply the changes to the job or Cancel to ignore.");
	}

	protected void copyOptionsToAllJobs() {
		if (JOptionPane.showConfirmDialog(this, "Are you sure you want to to copy the settings of this job\nto 'all' jobs?\n\nThis will first save the changed values in the Edit dialog\nto the job and copy the options afterwards.", "Copy settings to all jobs", JOptionPane.YES_NO_OPTION) == 0){
			Job job = DirSyncPro.getGui().getSelectedJob();
			applyJobSettings();
			DirSyncPro.getSync().copyOptionsToAllJobs(job);
			this.setVisible(false);
		}
	}

	protected void copyOptionsToEnabledJobs() {
		if (JOptionPane.showConfirmDialog(this, "Are you sure you want to to copy the settings of this job\nto 'enabled' jobs?\n\nThis will first save the changed values in the Edit dialog\nto the job and copy the options afterwards.", "Copy settings to all jobs", JOptionPane.YES_NO_OPTION) == 0){
			Job job = DirSyncPro.getGui().getSelectedJob();
			applyJobSettings();
			DirSyncPro.getSync().copyOptionsToEnabledJobs(job);
			this.setVisible(false);
		}
	}

	public JTree getFiltersTreeJTree(){
		return filtersTree;
	}
	
	public JTree getSchedulesTreeJTree(){
		return scheduleTree;
	}

	public void updateFiltersTree(){
		Runnable r = new Runnable(){
			public void run(){
				((FiltersTreeModel) filtersTree.getModel()).reload(getFiltersTree());
				GuiTools.expandAll(filtersTree);
			}
		};
			SwingUtilities.invokeLater(r);
	}

	public void updateScheduleTree(){
		Runnable r = new Runnable(){
			public void run(){
				((ScheduleTreeModel) scheduleTree.getModel()).reload(getScheduleTree());
				GuiTools.expandAll(scheduleTree);
			}
		};
			SwingUtilities.invokeLater(r);
	}
	
	protected void browseLog() {
		try {
			JFileChooser fileChooser = new JFileChooser();

			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			if (!dirLogField.getText().equals("")) {
				File logFile = new File(dirLogField.getText());
				fileChooser.setCurrentDirectory(logFile);
				if (logFile.isFile()) {
					fileChooser.setSelectedFile(logFile);
				}
			}else{
				Job job = DirSyncPro.getGui().getSelectedJob();
				File logFile = new File(DirSyncPro.getLogsPath(true) + FileTools.replaceIllegalCharactersInFileName(job.getName()) + "." + Const.LOG_FILE_EXTENSION);
				fileChooser.setSelectedFile(logFile);
				fileChooser.setCurrentDirectory(new File(DirSyncPro.getLogsPath(false)));
			}

			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				String filename = file.getPath();

				// if only a path was selected suggest a filename
				if (file.isDirectory()) {
					if (!filename.endsWith(File.separator)) {
						filename += File.separator;
					}
					filename += dirNameField.getText() + ".log";
				}

				dirLogField.setText(filename);
			}

		} catch (Exception e) {
			throw new Error(e);
		}
	}


	public void initFilters(boolean edit, Job job){
		if (edit){
			filters = (Vector<Filter>) job.getFilterSet().getFilters().clone();
		}else{
			filters = new Vector<Filter>();
			filters.add(new FilterByPattern(job, Filter.Action.Include, Const.DEFAULT_INCLUDE_FILE, FilterByPattern.FilterPatternType.File, false));
			filters.add(new FilterByPattern(job, Filter.Action.Include, Const.DEFAULT_INCLUDE_DIR, FilterByPattern.FilterPatternType.Directory, false));
		}
		updateFiltersTree();
	}

	protected void openAddFilterDialog() {
		filterDialog.initFilterDialog(null);
		GuiTools.openDialog(filterDialog);
	}

	public void openEditFilterDialog() {
		filterDialog.initFilterDialog(getSelectedFilter());
		GuiTools.openDialog(filterDialog);
	}

	public void addFilter(Filter ft) {
		filters.add(ft);
		Collections.sort(filters);
		updateFiltersTree();
	}

	protected void removeFilter() {
		filters.remove(getSelectedFilter());
		updateFiltersTree();
	}

	protected void openAddScheduleDialog() {
		scheduleDialog.initSchedulesDialog(null);
		GuiTools.openDialog(scheduleDialog);
	}

	public void openEditScheduleDialog() {
		scheduleDialog.initSchedulesDialog(getSelectedSchedule());
		GuiTools.openDialog(scheduleDialog);
	}

	public void addSchedule(Schedule sch) {
		schedules.add(sch);
		for (Schedule sched : schedules){
			sched.calculateNextEvent();
		}
		Collections.sort(schedules);
		updateScheduleTree();
	}

	protected void removeSchedule() {
		schedules.remove(getSelectedSchedule());
		updateScheduleTree();
	}
	
	private Filter getSelectedFilter(){
		Filter filter = null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) filtersTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 2){
			filter = (Filter) node.getUserObject();
		}
		return filter;
	}

	private Schedule getSelectedSchedule(){
		Schedule schedule = null;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) scheduleTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 1){
			schedule = (Schedule) node.getUserObject();
		}
		return schedule;
	}
	
	public ScheduleTree getScheduleTree(){
		ScheduleTree scheduleTree = null;
	
		scheduleTree = new ScheduleTree(null, null); //root
		Schedule schedule;
		
		for (Schedule sch : schedules){
			ScheduleTree st = new ScheduleTree(sch, scheduleTree);
		}
		return scheduleTree;
	}

	public FiltersTree getFiltersTree(){
		FiltersTree filtersTree = new FiltersTree(null, null); //root
		FiltersTree includes = new FiltersTree((Filter) new FilterGroup(null, Filter.Action.Include), filtersTree);
		FiltersTree excludes = new FiltersTree((Filter) new FilterGroup(null, Filter.Action.Exclude), filtersTree);
		if (filters != null){
			for (Filter f : filters){
				if (f.getAction() == Filter.Action.Include){
					FiltersTree ft = new FiltersTree(f, includes);
				}else{
					FiltersTree ft = new FiltersTree(f, excludes);
				}
			}
		}
		return filtersTree;
	}

	public FilterDialog getFilterDialog() {
		return filterDialog;
	}

	public ScheduleDialog getScheduleDialog() {
		return scheduleDialog;
	}

}
