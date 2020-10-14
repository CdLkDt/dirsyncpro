/* MainFrame.java
 *
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.gui.mainframe;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;

import dirsyncpro.Const;
import dirsyncpro.Const.CopyMode;
import dirsyncpro.Const.IconKey;
import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.cmddialog.CMDDialog;
import dirsyncpro.gui.jobdialog.JobDialog;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate.FilterDateType;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate.FilterDateDirFile;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileSize;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPattern;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.licensedialog.LicenseDialog;
import dirsyncpro.gui.mainframe.jobtree.JobTree;
import dirsyncpro.gui.mainframe.jobtree.JobsTreeModel;
import dirsyncpro.gui.settingsdialog.SettingsDialog;
import dirsyncpro.gui.shutdowndialog.ShutDownDialog;
import dirsyncpro.gui.updatedialog.UpdateDialog;
import dirsyncpro.job.Job;
import dirsyncpro.message.MessageQ;
import dirsyncpro.sync.Sync;
import dirsyncpro.sync.Sync.SyncError;
import dirsyncpro.sync.SyncPair;
import dirsyncpro.sync.SyncQ;
import dirsyncpro.tools.DesktopTools;
import dirsyncpro.tools.GuiTools;
import dirsyncpro.tools.Log;
import dirsyncpro.tools.TextFormatTool;
import dirsyncpro.updater.Updater;

/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings({ "unused", "serial" })
public class MainFrame extends MainFrameObjects {

	protected Tray dirSyncProTray;
	protected ScreenUpdater screenUpdater;
	
	private JobDialog jobDialog = new JobDialog(this);
	private SettingsDialog settingsDialog = new SettingsDialog(this);
	private LicenseDialog licenseDialog = new LicenseDialog(this);
	private UpdateDialog updateDialog = new UpdateDialog(this);
	private CMDDialog cmdDialog = new CMDDialog(this);
	private ShutDownDialog shutDownDialog = new ShutDownDialog(this);

	protected SwingWorker<Void, Void> syncProcess = null;
	protected SwingWorker<Void, Void> helperProcess = null;
	
	protected boolean isEnabled = true;

	private int progressTotalValue;
	private int progressTotalMax;
	private int progressTotalAdd;
	private String progressTotalString;
	private boolean progressTotalIndeterminate;
	private int progressCurrentValue;
	private int progressCurrentMax;
	private String progressCurrentString;
	private boolean progressCurrentIndeterminate;

	private int prevProgressTotalValue;
	private int prevProgessTotalAdd;
	private int prevProgessTotalMax;
	private String prevProgessTotalString;
	private boolean prevProgessTotalIndeterminate;
	private int prevProgessCurrentValue;
	private int prevProgessCurrentMax;
	private String prevProgessCurrentString;
	private boolean prevProgessCurrentIndeterminate;
	
	private boolean progressbarUpdated = false;
	
	private AtomicBoolean updatingGUI = new AtomicBoolean(false);
	
	protected File currentConfig; // for open/save dialog
	
	private boolean currentConfigAlreadyAccessed = false;
	
	private Updater u;

	// whether the directory is being updated from the gui
	protected boolean isUpdateDirFromGui = true;

	public MainFrame() {
        // always select the last row (the created dummy job)
        jobsTree.setSelectionRow(jobsTree.getRowCount()-1);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-this.getSize().width)/2, (screenSize.height-this.getSize().height)/2, this.getSize().width, this.getSize().height);

        if (DirSyncPro.isBeta()){
        	settingsDialog.enableUpdate();
        }
        
		this.loadOpenRecentSubMenu();
		int lastX = DirSyncPro.getWindowLastGeomteryX();
		int lastY = DirSyncPro.getWindowLastGeomteryY();
		int lastWidth = DirSyncPro.getWindowLastGeomteryWidth();
		int lastHeight = DirSyncPro.getWindowLastGeomteryHeight();
		
		if (lastX != 0 && lastY != 0){
			this.setLocation(lastX, lastY);
		}
		
		if (lastWidth != 0 && lastHeight != 0){
			this.setPreferredSize(new Dimension(lastWidth, lastHeight));
		}
		
		autoResizeSyncQColumns();
		
		this.pack();
		this.setVisible(true);
		
		//start the tray in another thread. don't wait up for the tray which causes problems under Linux 
		SwingWorker<Void, Void> trayProcess = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				dirSyncProTray = new Tray();
				if (DirSyncPro.isOption_iconify()){
					iconifyForm();
				}
				return null;
			}
		};
		trayProcess.execute();
		
		screenUpdater = new ScreenUpdater();
		
		// The license GUI makes the MainGui visible when the license is accepted.
		if (!DirSyncPro.isLicenseAccepted()){
			licenseDialog.openLicenseDialog();
			// for the very first time of each release
			DirSyncPro.setLogLevel(Const.LogLevel.Minimal);
			if (DirSyncPro.isBeta()){
				displayWarningDialog("This is an unstable version and is released for preview\nand/or testing purposes only. Please use with caution!");
			}
		}
	}

	public void makeVisible(){
		this.setState(Frame.NORMAL);
		this.setVisible(true);
		this.toFront();
	}

	/**
	 * Initializes a new configuration.
	 */
	public void initConfig() {
        File lastLoadedConfig = DirSyncPro.getLastLoadedConfig();
        if (DirSyncPro.isLoadLastConfig() && lastLoadedConfig != null){
        	DirSyncPro.getSync().getLog().printMinimal("Loading last opened jobset.", IconKey.Info);
        	openConfig(DirSyncPro.getLastLoadedConfig(), false);
        }else{
        	DirSyncPro.getSync().getLog().printMinimal("Creating a new default jobset.", IconKey.Info);
        	newConfig();
        }
	}

	protected void newConfig(){
		if (!reallyQuit()) return;
		doNewConfig();
	}
	
	private void doNewConfig() {

		isUpdateDirFromGui = false;
		syncQTable.revalidate();
		messagesTable.revalidate();

		if (DirSyncPro.getSync().getScheduleEngine().isRunning()){
			DirSyncPro.getSync().getScheduleEngine().stopScheduler();
		}
		
		updateGuiFromSyncQViewFilter();
		updateGuiFromSyncQSyncFilter();
		updateGuiFromMessagesQViewFilter();
		
		updateJobsTree();

		DirSyncPro.setSync(new Sync());
		isUpdateDirFromGui = false;
		currentConfigAlreadyAccessed = false;
		resetGUI();
		currentConfig = new File(DirSyncPro.getConfigPath(true) + DirSyncPro.getSync().getName() + "." + Const.SYNC_FILE_EXTENSION);
		updateTitle();
	}

	protected void resetGUI(){
		//assuming that the log is already reset
		messagesTable.revalidate();
		messagesTable.repaint();
		registerProgressBars(0, 1, 0, null, false, 0, 1, null, false);
		setToJobTreeTab();
	}
	
	protected void loadOpenRecentSubMenu(){
		openRecentMenu.removeAll();
		File[] files = DirSyncPro.getLastLoadedConfigs();
		for (final File f : files){
			JMenuItem subMenuItem = new JMenuItem(f.getName());
			subMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/file.png")));
			subMenuItem.setToolTipText(f.getAbsolutePath());
			subMenuItem.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                openConfig(f, true);
	            }
	        });
			openRecentMenu.add(subMenuItem);
		}
	}
	
	protected void appendJobs(){
		String filename = "";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new DSPFileFilter(Const.FileType.DSC.getExtension()));
		if (currentConfig != null && currentConfig.isFile() && !(currentConfig.getName().equals(DirSyncPro.getSync().getName() + "." + Const.SYNC_FILE_EXTENSION) && !currentConfigAlreadyAccessed)) {
			fileChooser.setSelectedFile(currentConfig);
			fileChooser.setCurrentDirectory(currentConfig);
		}else{
			File f = new File(DirSyncPro.getConfigPath(false));
			fileChooser.setSelectedFile(f);
			fileChooser.setCurrentDirectory(f);
		}

		if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
			try{
				DirSyncPro.getSync().appendJobs(fileChooser.getSelectedFile().getPath());
			} catch (IllegalArgumentException e) {
				DirSyncPro.displayError("Error while appending jobset '" + filename + "'.\n\n" + e);
				DirSyncPro.getLog().printThrowableStack(e);
			} catch (Exception e) {
				DirSyncPro.displayError("Error while appending jobset '" + filename + "'.\n\n" + e);
				DirSyncPro.getLog().printThrowableStack(e);
			}
		}
	}
	
	protected void saveEnabledJobs(){
		String filename = "";

		javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileFilter(new DSPFileFilter(Const.FileType.DSC.getExtension()));

		if (currentConfig != null && currentConfig.isFile() && !(currentConfig.getName().equals(DirSyncPro.getSync().getName() + "." + Const.SYNC_FILE_EXTENSION) && !currentConfigAlreadyAccessed)) {
			fileChooser.setSelectedFile(currentConfig);
			fileChooser.setCurrentDirectory(currentConfig);
		}else{
			File f = new File(DirSyncPro.getConfigPath(false));
			fileChooser.setSelectedFile(f);
			fileChooser.setCurrentDirectory(f);
		}

		if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getPath();
			// check for FILE_EXTENSION
			if (!filename.toLowerCase().endsWith("." + Const.SYNC_FILE_EXTENSION.toLowerCase())) {
				// file does not end with default extension FILE_EXTENSION; add it
				filename += "." + Const.SYNC_FILE_EXTENSION;
			}

			// if file exists display warning dialog
			if (!(new File(filename).exists())
					|| (JOptionPane.showConfirmDialog(this,	"Configuration '" + filename + "' exists.\nOverwrite?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION))
			{
				try {
					DirSyncPro.getSync().saveEnabled(filename);
				} catch (Exception e) {
					DirSyncPro.displayError("Error while saving enabled jobs to '" + filename + "'.");
				}
			}
		}

	}
	
	protected void openConfig() {
		String filename = "";
		
		if (!reallyQuit()) return;

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new DSPFileFilter(Const.FileType.DSC.getExtension()));
			if (currentConfig != null && currentConfig.isFile() && !(currentConfig.getName().equals(DirSyncPro.getSync().getName() + "." + Const.SYNC_FILE_EXTENSION) && !currentConfigAlreadyAccessed)) {
				fileChooser.setSelectedFile(currentConfig);
				fileChooser.setCurrentDirectory(currentConfig);
			}else{
				File f = new File(DirSyncPro.getConfigPath(false));
				fileChooser.setSelectedFile(f);
				fileChooser.setCurrentDirectory(f);
			}

			if (fileChooser.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
				openConfig(fileChooser.getSelectedFile(), true);
			}
	}
	
	protected void openConfig(File file, boolean saveToRecent) {
		
		doNewConfig(); // delete current loaded config

		if (!file.exists() || !file.isFile())
			return;

		try {

			// load config
			DirSyncPro.getSync().load(file.getPath());
			currentConfigAlreadyAccessed = true;
			currentConfig = file;

			// update GUI
			updateGuiFromSyncQViewFilter();
			updateGuiFromSyncQSyncFilter();
			updateGuiFromMessagesQViewFilter();
			updateJobsTree();

			// remember directory and file
			resetGUI();
			updateTitle();
			if (saveToRecent){
				DirSyncPro.addLastLoadedConfig(currentConfig);
				loadOpenRecentSubMenu();
			}
		} catch (IllegalArgumentException e) {
			DirSyncPro.displayError("Error while loading configuration '" + file.getAbsolutePath() + "'.\n\n");
			DirSyncPro.getLog().printThrowableStack(e);
		} catch (Exception e) {
			DirSyncPro.displayError("Error while loading configuration '" + file.getAbsolutePath() + "'.\n\n");
			DirSyncPro.getLog().printThrowableStack(e);
		}
	}
	
	protected void saveConfig(){
		if (currentConfig != null && currentConfigAlreadyAccessed){
			String filename = currentConfig.getPath();
			try {
				DirSyncPro.getSync().save(filename);
				updateTitle();
			} catch (Exception e) {
				DirSyncPro.displayError("Error while saving configuration '" + filename + "'.");
			}
		}else{
			saveAsConfig();
		}
		DirSyncPro.getSync().setOptionsChanged(false);
	}
	
	protected void saveAsConfig() {
		String filename = "";
		String logfilename = "";

		javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
		fileChooser.setFileFilter(new DSPFileFilter(Const.FileType.DSC.getExtension()));

		if (currentConfig != null && currentConfig.isFile() && !(currentConfig.getName().equals(DirSyncPro.getSync().getName() + "." + Const.SYNC_FILE_EXTENSION) && !currentConfigAlreadyAccessed)) {
			fileChooser.setSelectedFile(currentConfig);
			fileChooser.setCurrentDirectory(currentConfig);
		}else{
			File f = new File(DirSyncPro.getConfigPath(false));
			fileChooser.setSelectedFile(f);
			fileChooser.setCurrentDirectory(f);
		}

		if (fileChooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
			filename = fileChooser.getSelectedFile().getPath();
			logfilename = fileChooser.getSelectedFile().getName() + "." + Const.LOG_FILE_EXTENSION;
			// check for FILE_EXTENSION
			if (!filename.toLowerCase().endsWith("." + Const.SYNC_FILE_EXTENSION.toLowerCase())) {
				// file does not end with default extension FILE_EXTENSION; add it
				filename += "." + Const.SYNC_FILE_EXTENSION;
			}

			//fix the log filename only if it is not changed
			logfilename = logfilename.replaceAll("." + Const.SYNC_FILE_EXTENSION, "");
			if (DirSyncPro.getSync().getLog().isEnabled()){
				DirSyncPro.getSync().getLog().setFile(DirSyncPro.getLogsPath(true) + logfilename);
			}

			// if file exists display warning dialog
			if (!(new File(filename).exists())
					|| (JOptionPane.showConfirmDialog(this,	"Configuration '" + filename + "' exists.\nOverwrite?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION))
			{
				// if the config filename is changed and the log file was not manually changed change the log filename according to the new config name
				if (!DirSyncPro.getSync().getLog().getPath().equals(logfilename) && DirSyncPro.getSync().getLog().getPath().equals(currentConfig.getName().replaceAll(Const.SYNC_FILE_EXTENSION, Const.LOG_FILE_EXTENSION))){
					try{
					DirSyncPro.getSync().getLog().setFile(logfilename);
					}catch (Exception e){
						throw new Error(e);
					}
				}
				
				currentConfig = new File(filename);
				currentConfigAlreadyAccessed = true;
				saveConfig();
				DirSyncPro.addLastLoadedConfig(currentConfig);
				loadOpenRecentSubMenu();

				updateGuiFromSyncQViewFilter();
				updateGuiFromSyncQSyncFilter();
				updateGuiFromMessagesQViewFilter();
			}
		}
	}

	protected void syncQViewFilterChanged(boolean reset){
		if (reset){
			if (DirSyncPro.getSync().getSyncQ() != null){
				DirSyncPro.getSync().getSyncQ().initSyncQViewFilter();
			}
			updateGuiFromSyncQViewFilter();
		}else{
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.New, syncQViewFilterNewFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.Modified, syncQViewFilterModifiedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.Larger, syncQViewFilterLargerFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.LargerAndModified, syncQViewFilterLargerAndModifiedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.DeleteFiles, syncQViewFilterDeletedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.DeleteDirs, syncQViewFilterDeletedDirsCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQViewFilterMode(CopyMode.ConflictFiles, syncQViewFilterConflictFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().viewFilter();
		}
		syncQTable.revalidate();
		syncQTable.repaint();
	}

	protected void downloadURLClicked(){
		u.openDownloadURLinBrowser();
	}
	
	protected void changelogURLClicked(){
		u.openChangelogURLinBrowser();
	}
	
	protected void cleanLog(){
		if (JOptionPane.showConfirmDialog(this, "All current messages will be deleted. Continue?", "Delete all messages?", JOptionPane.OK_CANCEL_OPTION) == 0){
			DirSyncPro.getSync().getLog().cleanMessages();
			messagesTable.revalidate();
			messagesTable.repaint();
		}
	}
	
	protected void messagesQViewFilterChanged(boolean reset){
		if (reset){
			DirSyncPro.getSync().getLog().getMessages().initMessageQViewFilter();
			updateGuiFromMessagesQViewFilter();
		}else{
			MessageQ mq = DirSyncPro.getSync().getLog().getMessages();
			mq.setMessagesQViewFilterMode(IconKey.Info, messagesViewFilterInfosCheckBox.isSelected());
			mq.setMessagesQViewFilterMode(IconKey.Warning, messagesViewFilterWarningsCheckBox.isSelected());
			mq.setMessagesQViewFilterMode(IconKey.Error, messagesViewFilterErrorsCheckBox.isSelected());
			mq.setMessagesQViewFilterMode(IconKey.File, messagesViewFilterFileOperationsCheckBox.isSelected());
			DirSyncPro.getSync().getLog().getMessages().viewFilter();
		}
		messagesTable.revalidate();
		messagesTable.repaint();
	}

	protected void syncQSyncFilterChanged(boolean reset){
		if (reset){
			DirSyncPro.getSync().getSyncQ().initSyncQSyncFilter();
			updateGuiFromSyncQSyncFilter();
		}else{
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.New, syncQSyncFilterNewFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.Modified, syncQSyncFilterModifiedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.Larger, syncQSyncFilterLargerFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.LargerAndModified, syncQSyncFilterLargerAndModifiedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.DeleteFiles, syncQSyncFilterDeletedFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.DeleteDirs, syncQSyncFilterDeletedDirsCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().setSyncQSyncFilterMode(CopyMode.ConflictFiles, syncQSyncFilterConflictFilesCheckBox.isSelected());
			DirSyncPro.getSync().getSyncQ().syncFilter();
		}
		syncQTable.revalidate();
		syncQTable.repaint();
	}



	public void updateGuiFromMessagesQViewFilter(){
		MessageQ mq = DirSyncPro.getSync().getLog().getMessages();
		messagesViewFilterInfosCheckBox.setSelected(mq.getMessagesQViewFilterMode(IconKey.Info));
		messagesViewFilterWarningsCheckBox.setSelected(mq.getMessagesQViewFilterMode(IconKey.Warning));
		messagesViewFilterErrorsCheckBox.setSelected(mq.getMessagesQViewFilterMode(IconKey.Error));
		messagesViewFilterFileOperationsCheckBox.setSelected(mq.getMessagesQViewFilterMode(IconKey.File));
	}

	public void updateGuiFromSyncQViewFilter(){
		if (DirSyncPro.getSync().getSyncQ() != null){
			syncQViewFilterNewFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.New));
			syncQViewFilterModifiedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.Modified));
			syncQViewFilterLargerFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.Larger));
			syncQViewFilterLargerAndModifiedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.LargerAndModified));
			syncQViewFilterDeletedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.DeleteFiles));
			syncQViewFilterDeletedDirsCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.DeleteDirs));
			syncQViewFilterConflictFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQViewFilterMode(CopyMode.ConflictFiles));
		}else{
			syncQViewFilterNewFilesCheckBox.setSelected(true);
			syncQViewFilterModifiedFilesCheckBox.setSelected(true);
			syncQViewFilterLargerFilesCheckBox.setSelected(true);
			syncQViewFilterLargerAndModifiedFilesCheckBox.setSelected(true);
			syncQViewFilterDeletedFilesCheckBox.setSelected(true);
			syncQViewFilterDeletedDirsCheckBox.setSelected(true);
		}
	}
	
	public void updateGuiFromSyncQSyncFilter(){
		if (DirSyncPro.getSync().getSyncQ() != null){
			syncQSyncFilterNewFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.New));
			syncQSyncFilterModifiedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.Modified));
			syncQSyncFilterLargerFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.Larger));
			syncQSyncFilterLargerAndModifiedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.LargerAndModified));
			syncQSyncFilterDeletedFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.DeleteFiles));
			syncQSyncFilterDeletedDirsCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.DeleteDirs));
			syncQSyncFilterConflictFilesCheckBox.setSelected(DirSyncPro.getSync().getSyncQ().getSyncQSyncFilterMode(CopyMode.ConflictFiles));
		}else{
			syncQSyncFilterNewFilesCheckBox.setSelected(true);
			syncQSyncFilterModifiedFilesCheckBox.setSelected(true);
			syncQSyncFilterLargerFilesCheckBox.setSelected(true);
			syncQSyncFilterLargerAndModifiedFilesCheckBox.setSelected(true);
			syncQSyncFilterDeletedFilesCheckBox.setSelected(true);
			syncQSyncFilterDeletedDirsCheckBox.setSelected(true);
			syncQSyncFilterConflictFilesCheckBox.setSelected(true);
		}
	}

	public void setToMessagesTab(){
		tabbedPane.setSelectedIndex(3);
	}
	
	public void setToSchedulesTab(){
		tabbedPane.setSelectedIndex(2);
	}
	
	public void setToSyncQTab(){
		tabbedPane.setSelectedIndex(1);
	}
	
	public void setToJobTreeTab(){
		tabbedPane.setSelectedIndex(0);
	}

	private void enableJPanel(JPanel panel, boolean enabled){
		Component[] components = panel.getComponents();   
		for (int i = 0; i < components.length; i++) {   
			components[i].setEnabled(enabled);
		}
	}
	
	protected void toolsSwapSrcDst() {
		if (JOptionPane.showConfirmDialog(this, "Are you sure you want to swap all the source and destination directories?", "Swap All source and destination directories", JOptionPane.YES_NO_OPTION) == 0){
			DirSyncPro.getSync().swapSrcDst();
			updateJobsTree();
		}
		DirSyncPro.getSync().setOptionsChanged(true);
	}


	protected void openBrowser(){
		u.openDownloadURLinBrowser();
	}
	
	protected void optionsOptionsMenuItemClicked(){
		settingsDialog.initApplicationSettingDialog();
		GuiTools.openDialog(settingsDialog);
	}
	
	protected void disableAllDirs() {
		enableAllDirs(false);
	}

	protected void enableAllDirs() {
		enableAllDirs(true);
	}

	/**
	 * Start Scheduler.
	 */
	public void schedulerStart(){
		DirSyncPro.getSync().getScheduleEngine().startScheduler();
		
		if (DirSyncPro.getSync().getScheduleEngine().isRunning()){
			scheduleStartButton.setEnabled(false);
			startScheduledTasksMenuItem.setEnabled(false);
			setToSchedulesTab();
		}
		updateSchedulesInGUI();
	}
	
	/**
	 * Start Scheduler.
	 */
	public void schedulerStop(){
		if (DirSyncPro.getSync().getScheduleEngine().isRunning()){
			DirSyncPro.getSync().getScheduleEngine().stopScheduler();
			setToSchedulesTab();
			scheduleStartButton.setEnabled(true);
			startScheduledTasksMenuItem.setEnabled(true);
		}else{
			DirSyncPro.displayError("Schedule engine is not running!");
		}
		updateSchedulesInGUI();
	}

	/**
	 * Start Analyze.
	 */
	public void analyzeStart() {
		DirSyncPro.getSync().setMode(Sync.ANALYZE);
		analyze(null);
	}

	/**
	 * Starts a synchronization.
	 */
	public void synchronizationStart() {
		DirSyncPro.getSync().setMode(Sync.SYNCHRONIZATION);
		synchronize(null);
	}

	public void shutDownIfNecessary(){
		if (shutDownButton.isSelected()){
			GuiTools.openDialog(shutDownDialog);
			shutDownDialog.startCountDown();
		}
	}
	
	protected void contents() {
		File manual = new File(DirSyncPro.getProgramPath() + File.separator + Const.manualFilename);
		if (!manual.exists()){
			DirSyncPro.displayError(
					"Unable to open the help file (" + manual.getAbsolutePath() + 
					")\nThe help file resides normally in the program folder.\n\nYou may always download " +
					Const.manualFilename + " from " + Const.HOMEPAGE
					);
		}else{
			DesktopTools.launchFile(manual.getAbsolutePath());
		}
	}

	/***
	 * updates the SyncQ in GUI
	public void accumulateSyncQ(){
		SyncQ syncQ = new SyncQ();
		Object[] dirs = syncQDirList.getSelectedValues();
		for (int i = 0; i< dirs.length; i++){
			Job dir = (Job) dirs[i];
			syncQ.addAll(dir.getSyncQueue());
		}
		DirSyncPro.getSync().setSyncQ(syncQ);
	}
*/
	
	protected void syncQDirSelected() {
		//accumulateSyncQ();
		DirSyncPro.getGui().updateSyncQInGUI();
	}

	protected void jobsTreeMouseHandler(MouseEvent evt){
		// Do nothing if GUI is disabled
		if (!DirSyncPro.getGui().isGuiEnabled()) { return; }

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 1){
			int row = jobsTree.getSelectionRows()[0];
			final Job job = (Job) node.getUserObject();
			int i = DirSyncPro.getSync().getIndexOfJobInJobList(job);
    	
			// popup menu for the jobTree
			if (evt.isPopupTrigger()) { 

				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem menuItem;

				// enable/disable
				if (job.isEnabled()) {
					menuItem = new JMenuItem("Disable");
					menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cancel.png")));
				} else {
					menuItem = new JMenuItem("Enable");
					menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/ok.png")));
				}

				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			job.setEnabled(!job.isEnabled()); // toggle enable
	        			updateJobsTree();
	        		}
	      		});
				popupMenu.add(menuItem);

				// edit
				menuItem = new JMenuItem("Edit");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			openEditJobDialog();
	        		}
	      		});
				popupMenu.add(menuItem);

				// up
				menuItem = new JMenuItem("Up");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dirUp.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			upwardeJob();
	        		}
	      		});
				popupMenu.add(menuItem);

				// up
				menuItem = new JMenuItem("Down");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dirDown.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			downwardJob();
	        		}
	      		});
				popupMenu.add(menuItem);

				// copy
				menuItem = new JMenuItem("Copy");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/copyAll.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			copyJob();
	        		}
	      		});
				popupMenu.add(menuItem);

				// remove
				menuItem = new JMenuItem("Remove");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/deleteFile.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			removeJob();
	        		}
	      		});
				popupMenu.add(menuItem);

				popupMenu.addSeparator();
				// Analyze this job
				menuItem = new JMenuItem("Analyze this job now");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/preview.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			analyze(new Vector<Job>(){{add(DirSyncPro.getGui().getSelectedJob());}});
	        		}
	      		});
				popupMenu.add(menuItem);

				// Synchronize this job
				menuItem = new JMenuItem("Synchronize this job now");
				menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/start.png")));
	
				menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        			synchronize(new Vector<Job>(){{add(DirSyncPro.getGui().getSelectedJob());}});
	        		}
	      		});
				popupMenu.add(menuItem);

				popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
				
			// jobTree Tooltip init
			ToolTipManager.sharedInstance().registerComponent(jobsTree);

		}
	}	
	

	protected void syncQMouseHandler(MouseEvent evt){
		// Do nothing if GUI is disabled
		if (!DirSyncPro.getGui().isGuiEnabled()) { return; }

	    int selectedRow = syncQTable.rowAtPoint(evt.getPoint());
	    int selectedColumn = syncQTable.columnAtPoint(evt.getPoint());
//	    syncQTable.setRowSelectionInterval(selectedRow, selectedRow);
//	    syncQTable.setColumnSelectionInterval(selectedColumn, selectedColumn);

		final SyncPair sp = DirSyncPro.getSync().getSyncQ().get(selectedRow);
		Job job = sp.getJob();
		File file = null;
		if (selectedColumn <3){
			file = sp.getFileA();
		}else if (selectedColumn > 3){
			file = sp.getFileB();
		}
		final File f = file;
		// popup menu for the jobTree
		if (evt.isPopupTrigger()) { 
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem menuItem = null;
			JMenu submenu = null;

			//remove this
			menuItem = new JMenuItem("Remove this from Q");
			menuItem.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
       				DirSyncPro.getSync().getSyncQ().delete(sp);
       				syncQTable.clearSelection();
       				DirSyncPro.getSync().getSyncQ().syncFilter();
       				DirSyncPro.getGui().updateSyncQInGUI();
       				DirSyncPro.getGui().updateStatsinGUI();
        		}
      		});
			menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cancel.png")));
			popupMenu.add(menuItem);
			
			//remove all
			menuItem = new JMenuItem("Remove all of this mode from Q");
			menuItem.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
       				DirSyncPro.getSync().getSyncQ().deleteAllOfKind(sp);
       				syncQTable.clearSelection();
       				DirSyncPro.getSync().getSyncQ().syncFilter();
       				DirSyncPro.getGui().updateSyncQInGUI();
       				DirSyncPro.getGui().updateStatsinGUI();
        		}
      		});
			menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cancel.png")));
			popupMenu.add(menuItem);
			
			//remove selected
			menuItem = new JMenuItem("Remove all selected from Q");
			menuItem.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			int[] rows = syncQTable.getSelectedRows();
        			ArrayList<SyncPair> syncPairs = new ArrayList<SyncPair>();
        			for (int row : rows){
        				syncPairs.add(DirSyncPro.getSync().getSyncQ().get(row));
        			}
        			for (int i = 0 ; i < rows.length ; i++){
        				DirSyncPro.getSync().getSyncQ().delete(syncPairs.get(i));
        			}
       				syncQTable.clearSelection();
       				DirSyncPro.getSync().getSyncQ().syncFilter();
       				DirSyncPro.getGui().updateSyncQInGUI();
       				DirSyncPro.getGui().updateStatsinGUI();
        		}
      		});
			menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cancel.png")));
			popupMenu.add(menuItem);
			
			switch(selectedColumn){
			case 3:
				//Change To
				popupMenu.addSeparator();
				submenu = new JMenu();
				submenu.setText("Change to:");
				submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/syncModeBI16x16.png")));
				popupMenu.add(submenu);
				ArrayList<SyncPairStatus> spsArray = new ArrayList<SyncPairStatus>();
				if (sp.getFileA().isFile() || sp.getFileB().isFile()){
					if (sp.getFileA().exists()){
						spsArray.addAll(Arrays.asList(SyncPairStatus.FileACopyForced, SyncPairStatus.FileAIsRedundant));
					}
					if (sp.getFileB().exists()){
						spsArray.addAll(Arrays.asList(SyncPairStatus.FileBCopyForced, SyncPairStatus.FileBIsRedundant));
					}
				}else{
					if (sp.getFileA().exists()){
						spsArray.addAll(Arrays.asList(SyncPairStatus.DirACopyForced, SyncPairStatus.DirAIsRedundant));
					}
					if (sp.getFileB().exists()){
						spsArray.addAll(Arrays.asList(SyncPairStatus.DirBCopyForced, SyncPairStatus.DirBIsRedundant));
					}
				}
				for (final SyncPairStatus sps : spsArray){ 
					menuItem = new JMenuItem(sps.toString());
					menuItem.addActionListener(new ActionListener() {
		        		public void actionPerformed(ActionEvent e) {
		        			DirSyncPro.getSync().getSyncQ().change(sp, sps);
		       				DirSyncPro.getSync().getSyncQ().syncFilter();
		       				DirSyncPro.getGui().updateSyncQInGUI();
		       				DirSyncPro.getGui().updateStatsinGUI();
		        		}
		      		});
					menuItem.setIcon(sps.getIcon());
					submenu.add(menuItem);
				}

				//Change All To
				submenu = new JMenu();
				submenu.setText("Change all of this mode to:");
				submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/syncModeBI16x16.png")));
				popupMenu.add(submenu);
				job = sp.getJob();
				spsArray = new ArrayList<SyncPairStatus>();
				if (sp.getSyncPairStatus().isFileOperation()){
					spsArray.addAll(Arrays.asList(SyncPairStatus.FileACopyForced, SyncPairStatus.FileBCopyForced, SyncPairStatus.FileBIsRedundant, SyncPairStatus.FileAIsRedundant));
				}else{
					spsArray.addAll(Arrays.asList(SyncPairStatus.DirACopyForced, SyncPairStatus.DirBCopyForced, SyncPairStatus.DirBIsRedundant, SyncPairStatus.DirAIsRedundant));
				}
				for (final SyncPairStatus sps : spsArray){ 
					menuItem = new JMenuItem(sps.toString());
					menuItem.addActionListener(new ActionListener() {
		        		public void actionPerformed(ActionEvent e) {
		        			DirSyncPro.getSync().getSyncQ().changeAll(sp, sps);
		       				DirSyncPro.getSync().getSyncQ().syncFilter();
		       				DirSyncPro.getGui().updateSyncQInGUI();
		       				DirSyncPro.getGui().updateStatsinGUI();
		        		}
		      		});
					menuItem.setIcon(sps.getIcon());
					submenu.add(menuItem);
				}
				
				//Change all selected To
				submenu = new JMenu();
				submenu.setText("Change all selected to:");
				submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/syncModeBI16x16.png")));
				popupMenu.add(submenu);
				job = sp.getJob();
				spsArray = new ArrayList<SyncPairStatus>();
				spsArray.addAll(Arrays.asList(SyncPairStatus.FileDirACopyForced, SyncPairStatus.FileDirBCopyForced, SyncPairStatus.FileDirBIsRedundant, SyncPairStatus.FileDirAIsRedundant));
				for (final SyncPairStatus sps : spsArray){ 
					menuItem = new JMenuItem(sps.toString());
					menuItem.addActionListener(new ActionListener() {
		        		public void actionPerformed(ActionEvent e) {
		        			int[] rows = syncQTable.getSelectedRows();
		        			ArrayList<SyncPair> syncPairs = new ArrayList<SyncPair>();
		        			for (int row : rows){
		        				syncPairs.add(DirSyncPro.getSync().getSyncQ().get(row));
		        			}
		        			for (int i = 0 ; i < rows.length ; i++){
	        					SyncPairStatus spsTmp = sps;
		        				if(syncPairs.get(i).getFileA().isFile()){
		        					switch (sps){
		        					case FileDirACopyForced:
		        						spsTmp = SyncPairStatus.FileACopyForced;
		        						break;
		        					case FileDirAIsRedundant:
		        						spsTmp = SyncPairStatus.FileAIsRedundant;
		        						break;
		        					}
		        				}else{
		        					switch (sps){
		        					case FileDirACopyForced:
		        						spsTmp = SyncPairStatus.DirACopyForced;
		        						break;
		        					case FileDirAIsRedundant:
		        						spsTmp = SyncPairStatus.DirAIsRedundant;
		        						break;
		        					}
		        				}
		        				DirSyncPro.getSync().getSyncQ().change(syncPairs.get(i), spsTmp);
		        			}
		       				syncQTable.clearSelection();
		       				DirSyncPro.getSync().getSyncQ().syncFilter();
		       				DirSyncPro.getGui().updateSyncQInGUI();
		       				DirSyncPro.getGui().updateStatsinGUI();
		        		}
		      		});
					menuItem.setIcon(sps.getIcon());
					submenu.add(menuItem);
				}
				break;
			case 0:
			case 4:
				if (file.exists()){
					popupMenu.addSeparator();
					if (file.isDirectory()){
						menuItem = new JMenuItem("Open folder");
						menuItem.addActionListener(new ActionListener() {
			        		public void actionPerformed(ActionEvent e) {
			        			try{
			        				DesktopTools.launchFile(f.getPath());
			        			}catch(Exception ex){
			        				DirSyncPro.displayError("Could not open the folder!");
			        			}
			        		}
			      		});
						menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dirGreen.png")));
						popupMenu.add(menuItem);
					}else if (file.isFile()){
						menuItem = new JMenuItem("Open");
						menuItem.addActionListener(new ActionListener() {
			        		public void actionPerformed(ActionEvent e) {
			        			try{
			        				DesktopTools.launchFile(f.getPath());
			        			}catch(Exception ex){
			        				DirSyncPro.displayError("Could not open the file!");
			        			}
			        		}
			      		});
						menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icon_file.png")));
						popupMenu.add(menuItem);

						menuItem = new JMenuItem("Open parent folder");
						menuItem.addActionListener(new ActionListener() {
			        		public void actionPerformed(ActionEvent e) {
			        			try{
			        				DesktopTools.launchFile(f.getParentFile().getPath());
			        			}catch(Exception ex){
			        				DirSyncPro.displayError("Could not open the parent folder!");
			        			}
			        		}
			      		});
						menuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/dirGreen.png")));
						popupMenu.add(menuItem);
					}
				}
				
				if (file.exists()){
					popupMenu.addSeparator();
					submenu = new JMenu();
					submenu.setText("Add Exclude Filter:");
					submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter.png")));
					popupMenu.add(submenu);
					
					final String filename = f.getName();
					menuItem = new JMenuItem("Pattern: " + filename);
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByPattern filter = new FilterByPattern(sp.getJob(), Filter.Action.Exclude);
							filter.setPattern(filename);
							filter.setPatternType(f.isFile() ? FilterByPattern.FilterPatternType.File : FilterByPattern.FilterPatternType.Directory);
	
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByPattern.getIcon());
					submenu.add(menuItem);
	
					// Pattern file extension
					if (file.isFile() && filename.lastIndexOf(".") > 0){
						final String extension = "*" + filename.substring(filename.lastIndexOf("."), filename.length());
        					menuItem = new JMenuItem("Extension: " + extension);
        					menuItem.addActionListener(new ActionListener() {
        						public void actionPerformed(ActionEvent e) {
        							FilterByPattern filter = new FilterByPattern(sp.getJob(), Filter.Action.Exclude);
        							filter.setPattern(extension);
        							filter.setPatternType(FilterByPattern.FilterPatternType.File);
        
        							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
        						}
        					});
        					menuItem.setIcon(Filter.Type.ByPattern.getIcon());
        					submenu.add(menuItem);
					}
				}
				break;
			case 1:
			case 5:
				if (file.exists() && file.isFile()){
					submenu = new JMenu();
					submenu.setText("Add Exclude Filter:");
					submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter.png")));
					popupMenu.add(submenu);

					menuItem = new JMenuItem("File is edited earlier than " + TextFormatTool.getLastModifiedLong(f));
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByDate filter = new FilterByDate(sp.getJob(), Filter.Action.Exclude, new Date(f.lastModified()), FilterDateType.EarlierThan, FilterDateDirFile.File);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByDate.getIcon());
					submenu.add(menuItem);

					menuItem = new JMenuItem("File is edited exactly on " + TextFormatTool.getLastModifiedLong(f));
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByDate filter = new FilterByDate(sp.getJob(), Filter.Action.Exclude, new Date(f.lastModified()), FilterDateType.ExactlyOn, FilterDateDirFile.File);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByDate.getIcon());
					submenu.add(menuItem);

					menuItem = new JMenuItem("File is edited later than " + TextFormatTool.getLastModifiedLong(f));
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByDate filter = new FilterByDate(sp.getJob(), Filter.Action.Exclude, new Date(f.lastModified()), FilterDateType.LaterThan, FilterDateDirFile.File);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByDate.getIcon());
					submenu.add(menuItem);
				}
				break;
			case 2:
			case 6:
				if (file.exists() && file.isFile()){
					submenu = new JMenu();
					submenu.setText("Add Exclude Filter:");
					submenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter.png")));
					popupMenu.add(submenu);

					final long fileSize = f.length();
					menuItem = new JMenuItem("File Size < " + fileSize + " bytes");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByFileSize filter = new FilterByFileSize(sp.getJob(), Filter.Action.Exclude);
							filter.setValue(fileSize);
							filter.setFileSizeType(FilterByFileSize.FilterFileSizeType.SmallerThan);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByFileSize.getIcon());
					submenu.add(menuItem);

					menuItem = new JMenuItem("File Size = " + fileSize + " bytes");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByFileSize filter = new FilterByFileSize(sp.getJob(), Filter.Action.Exclude);
							filter.setValue(fileSize);
							filter.setFileSizeType(FilterByFileSize.FilterFileSizeType.Exactly);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByFileSize.getIcon());
					submenu.add(menuItem);

					menuItem = new JMenuItem("File Size > " + fileSize + " bytes");
					menuItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							FilterByFileSize filter = new FilterByFileSize(sp.getJob(), Filter.Action.Exclude);
							filter.setValue(fileSize);
							filter.setFileSizeType(FilterByFileSize.FilterFileSizeType.LargerThan);
							DirSyncPro.getGui().openEditJobDialog(sp.getJob(), filter);
						}
					});
					menuItem.setIcon(Filter.Type.ByFileSize.getIcon());
					submenu.add(menuItem);
				}
				break;
			}
//				if (popupMenu.getComponents().length > 0){
//					popupMenu.addSeparator();
//				}
			
			// wrap up
			if (menuItem != null) {
				// something added
				popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
		// jobTree Tooltip init
		ToolTipManager.sharedInstance().registerComponent(syncQTable);

	}	

	
	public JobTree getJobTree(){
		JobTree jobTree = new JobTree(JobTree.Type.Root, "Jobset", null);
		for (Job job : DirSyncPro.getSync().getJobs()){
			JobTree dirTree = new JobTree(JobTree.Type.Job, job, jobTree);
			JobTree paths = new JobTree(JobTree.Type.Path, job, dirTree);
			JobTree source = new JobTree(JobTree.Type.Source, job, paths);
			JobTree dest = new JobTree(JobTree.Type.Destination, job, paths);

			JobTree filters = new JobTree(JobTree.Type.Filters, job, dirTree);
			
			for (Filter f : job.getFilterSet().getFilters()){
				JobTree jt = new JobTree(JobTree.Type.valueOf("Filter" + (f.getType().toString())), f, filters);
			}

			JobTree mode = new JobTree(JobTree.Type.Mode, job, dirTree);
			JobTree syncMode = new JobTree(JobTree.Type.SyncMode, job, mode);
			JobTree copyMode = new JobTree(JobTree.Type.CopyOptions, job, mode);
			JobTree conflictResolution = new JobTree(JobTree.Type.ConflictResolution, job, mode);
			JobTree subfolders = new JobTree(JobTree.Type.WithSubFolders, job, mode);
			JobTree verify = new JobTree(JobTree.Type.Verify, job, mode);
			JobTree preserve = new JobTree(JobTree.Type.Preserves, job, mode);
			JobTree override = new JobTree(JobTree.Type.OverrideReadOnly, job, mode);
			JobTree realtimeSync = new JobTree(JobTree.Type.RealtimeSync, job, mode);
			JobTree delete = new JobTree(JobTree.Type.Delete, job, mode);
			JobTree granularity = new JobTree(JobTree.Type.Granularity, job, mode);
			JobTree ignoredlsg = new JobTree(JobTree.Type.IgnoreDayLightSavingGranularity, job, mode);
			JobTree writeTSBack = new JobTree(JobTree.Type.WriteTimeStampsBack, job, mode);
			JobTree dirts = new JobTree(JobTree.Type.DirTimeStamps, job, mode);
			JobTree logs = new JobTree(JobTree.Type.Log, job, mode);
			JobTree scheduleTree = new JobTree(JobTree.Type.Schedule, job, dirTree);
			
			for (Schedule sched : job.getSchedules()){
				JobTree scheduleOnce= new JobTree(JobTree.Type.ScheduleItem, sched, scheduleTree);
			}
		}
		return jobTree;
	}
	
	public void updateJobsTree(){
		Runnable r = new Runnable(){
			public void run(){
				int[] rows = {1};
				if (jobsTree.getSelectionRows() != null && jobsTree.getSelectionRows().length > 0){
					rows = jobsTree.getSelectionRows();
				}
				((JobsTreeModel) jobsTree.getModel()).reload(getJobTree());
				jobsTree.setSelectionRows(rows);
			}
		};
			SwingUtilities.invokeLater(r);
	}

	public void updateJobsTree(final int row){
		Runnable r = new Runnable(){
			public void run(){
				((JobsTreeModel) jobsTree.getModel()).reload(getJobTree());
				jobsTree.setSelectionRow(row);
			}
		};
			SwingUtilities.invokeLater(r);
	}

	protected void downwardJob() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 1){
			int row = jobsTree.getSelectionRows()[0];
			Job job = (Job) node.getUserObject();
			int i = DirSyncPro.getSync().getIndexOfJobInJobList(job);
			if (DirSyncPro.getSync().getJobs().size() > 1 && i < DirSyncPro.getSync().getJobs().size() - 1){
				job = (Job) DirSyncPro.getSync().getJobs().remove(i);
				i++;
				DirSyncPro.getSync().getJobs().insertElementAt(job, i);
				updateJobsTree(row + 1);
			}
		}
	}

	protected void upwardeJob() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 1){
			int row = jobsTree.getSelectionRows()[0];
			Job job = (Job) node.getUserObject();
			int i = DirSyncPro.getSync().getIndexOfJobInJobList(job);
			if (i > 0){
				job = (Job) DirSyncPro.getSync().getJobs().remove(i);
				i--;
				DirSyncPro.getSync().getJobs().insertElementAt(job, i);
				updateJobsTree(row - 1);
			}
		}
	}

	protected void removeJob() {
		if (JOptionPane.showConfirmDialog(this, "Are you sure removing the selected job?", "Remove selected job?", JOptionPane.YES_NO_OPTION) == 0){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
			if (node != null && node.getLevel() == 1){
				int row = jobsTree.getSelectionRows()[0];
				Job job = (Job) node.getUserObject();
				int i = DirSyncPro.getSync().getIndexOfJobInJobList(job);
				if (DirSyncPro.getSync().getJobs().size() > 1) {
					DirSyncPro.getSync().getJobs().remove(i);
					updateJobsTree(row - 1);
				}else{
					DirSyncPro.displayError("Job tree must contain at least one job!");
				}
			}
		}
	}

	protected void copyJob() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node != null && node.getLevel() == 1){
			int row = jobsTree.getSelectionRows()[0];
			Job jobOrg = (Job) node.getUserObject();
			int i = DirSyncPro.getSync().getIndexOfJobInJobList(jobOrg);
			Job  job = null;
			try{
				job = (Job) jobOrg.clone();
			} catch(CloneNotSupportedException ce){
				// nothing. Clone is always supported
			}
			
			i++;
	
			String name = job.getName();
			if (name.indexOf(" - Copy ") <= 0){
				name += " - Copy ";
			}else{
				name = name.substring(0, name.indexOf(" - Copy ")) + " - Copy ";
			}
			
			job.setName(name + DirSyncPro.getSync().getNextJobPostfixStringIndex(name));
			if (jobOrg.getLog().isEnabled()){
				job.setLog(new Log(job.getName() + "." + Const.LOG_FILE_EXTENSION, job));
			}
	
			DirSyncPro.getSync().getJobs().insertElementAt(job, i);
			updateJobsTree(row + 1);
		}
	}

	public void openEditJobDialog() {
		Job job;
		JobTree jobTreeNode = getSelectedJobTreeNode();
		if (jobTreeNode == null) return;
		if (jobTreeNode.getUserObject() instanceof Filter){
			job = ((Filter) jobTreeNode.getUserObject()).getJob();
		}else if (jobTreeNode.getUserObject() instanceof Schedule){
			job = ((Schedule) jobTreeNode.getUserObject()).getJob();
		}else{
			job = (Job) jobTreeNode.getUserObject();
		}
		switch (jobTreeNode.type){
			case Job:
				jobDialog.setToBasicOptions();
				break;
			case Path:
			case Source:
			case Destination:
			case SyncMode:
			case WithSubFolders:
				jobDialog.setToBasicOptions();
				break;
			case Filters:
				jobDialog.setToBasicOptions();
				break;
			case FilterByDate:
			case FilterByDOSAttributes:
			case FilterByFileOwnership:
			case FilterByFilePermissions:
			case FilterByFileSize:
			case FilterByPath:
			case FilterByPattern:
				jobDialog.setToFilterOptions();
				break;
			case CopyOptions:
				jobDialog.setToCopyOptions();
				break;
			case Verify:
			case Granularity:
			case IgnoreDayLightSavingGranularity:
			case WriteTimeStampsBack:
			case DirTimeStamps:
				jobDialog.setToAdvancedOptions();
				break;
			case Delete:
				jobDialog.setToDeleteOptions();
				break;
			case Log:
				jobDialog.setToLogOptions();
				break;
			case Schedule:
			case ScheduleItem:
				jobDialog.setToScheduleOptions();
				break;
			default:
			    break;
		}
		
		if (job != null){
			jobDialog.initJobDialog(job);
			GuiTools.openDialog(jobDialog);
		}
	}
	
	public void openEditJobDialog(Job job, Filter filter) {
		jobDialog.initJobDialog(job);
		jobDialog.addFilter(filter);
		jobDialog.enableFilterOptions(true, true);
		GuiTools.openDialog(jobDialog);
	}

	protected void addJob() {
		Job job = new Job(true);
		job.setName(Const.DEFAULT_JOBSET_NAME + " " + DirSyncPro.getSync().getNextJobPostfixStringIndex(Const.DEFAULT_JOBSET_NAME + " "));
		DirSyncPro.getSync().getJobs().add(job);
		int row = jobsTree.getRowCount();
		updateJobsTree(row);
		jobDialog.initJobDialog(job);
		GuiTools.openDialog(jobDialog);
	}

	protected void donate(){
		String url = Const.DONATEURL;
		try{
			Desktop.getDesktop().browse(new URI(url));
		}catch (URISyntaxException e){
			DirSyncPro.displayError("Update URL syntax error!");
		}catch (IOException e){
			DirSyncPro.displayError("Unable to start the default internet browser!");
		}
	}
	
	protected void about() {
		String about = Const.COPYRIGHT + "\n\n" + Const.MESSAGE;
		javax.swing.JOptionPane.showMessageDialog(this, about, Const.PROGRAM,
				javax.swing.JOptionPane.PLAIN_MESSAGE,
				new javax.swing.ImageIcon(getClass().getResource("/icons/DirSyncPro64x64.png")));
	}

	public void quit() {
		if (DirSyncPro.getSync().getState() == Sync.STOP) {
			if (reallyQuit()){
				if (DirSyncPro.isGuiMode()){
					if ( dirSyncProTray != null && dirSyncProTray.isInitialized()){
						dirSyncProTray.removeTrayIcon();
					}
					if (DirSyncPro.getSync().getScheduleEngine().isRunning()){
						DirSyncPro.getSync().getScheduleEngine().stopScheduler();
					}
				}
				
				//save window last geometry
				DirSyncPro.setWindowLastGeometries(this.getX(), getY(), getWidth(), getHeight());
				
				// if no sync is running
				System.exit(0); // quit program
			}
		} else { // if a sync is running
			DirSyncPro.setOption_quit(true); // set option to quit after sync
			stop(); // stop running sync
		}
	}

	protected boolean reallyQuit(){
		boolean quit = true;
		if (DirSyncPro.getSync().isOptionsChanged()){
			int change = JOptionPane.showConfirmDialog(this, "Some job options are changed. Save them before exit?", "Unsaved job options", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (change){
			case 0 : 
				saveConfig();
				quit = true;
				break;
			case 1:
				quit = true;
				break;
			
			case 2:
				quit = false;
				break;
			}
		}
		return (
				quit &&
				(!DirSyncPro.getSync().getScheduleEngine().isRunning() || (DirSyncPro.getSync().getScheduleEngine().isRunning() && JOptionPane.showConfirmDialog(this, "Schedule Engine is running!\nOK to proceed?", "Schedule Engine running", JOptionPane.OK_CANCEL_OPTION) == 0))
				);
	}
	
	public void exitForm() {
		quit();
	}

	public void iconifyForm() {
		if (DirSyncPro.isMinimizeToSystemTray() && dirSyncProTray != null && dirSyncProTray.isInitialized()){
			this.setVisible(false);
			String info = (!DirSyncPro.isSingleClickSystemTray() ? "Double c" : "C") + "lick to restore the window.";
			dirSyncProTray.displayInfo("DirSyncPro minimized to tray.", info);
		}
	}

	protected void analyze(final Vector<Job> jobs) {

		if (DirSyncPro.getSync().getState() == Sync.START
				|| DirSyncPro.getSync().getState() == Sync.PAUSE
				|| DirSyncPro.getSync().getState() == Sync.STOPPING) {
			return;
		}

		syncProcess = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				setToSyncQTab();
				setGuiEnabled(false);
				if (jobs == null){
					DirSyncPro.getSync().analyze();
				}else{
					DirSyncPro.getSync().analyze(jobs);
				}
				setGuiEnabled(true);
				return null;
			}
		};
		syncProcess.execute();
	}

	protected void synchronize(final Vector<Job> jobs) {

		if (DirSyncPro.getSync().getState() == Sync.START
				|| DirSyncPro.getSync().getState() == Sync.PAUSE
				|| DirSyncPro.getSync().getState() == Sync.STOPPING) {
			return;
		}

		syncProcess = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				// switch to output pane
				setToSyncQTab();

				// lock GUI
				setGuiEnabled(false);
				if (jobs == null){
					DirSyncPro.getSync().synchronize();
				}else{
					DirSyncPro.getSync().synchronize(jobs);
				}

				// unlock GUI
				setGuiEnabled(true);

				return null;
			}
		};
		syncProcess.execute();
	}

	/**
	 * Check if the GUI is enabled.
	 * 
	 * @return <code>true</code> if the GUI is enabled, <code>false</code>
	 *         otherwise.
	 */
	public boolean isGuiEnabled() {
		return isEnabled;
	}

	protected void setGuiEnabled(boolean enabled) {
		isEnabled = enabled;

		newButton.setEnabled(enabled);
		openButton.setEnabled(enabled);
		saveButton.setEnabled(enabled);
		saveAsButton.setEnabled(enabled);

		synchronizeButton.setEnabled(enabled);
		synchronizeMenuItem.setEnabled(enabled);
		analyzeButton.setEnabled(enabled);
		analyzeMenuItem.setEnabled(enabled);
		
		newMenuItem.setEnabled(enabled);
		openMenuItem.setEnabled(enabled);
		openRecentMenu.setEnabled(enabled);
		saveMenuItem.setEnabled(enabled);
		saveAsMenuItem.setEnabled(enabled);
		swapSrcDstMenuItem.setEnabled(enabled);
		optionsMenuItem.setEnabled(enabled);

		jobAddButton.setEnabled(enabled);
		jobEditButton.setEnabled(enabled);
		jobCopyButton.setEnabled(enabled);
		jobRemoveButton.setEnabled(enabled);
		jobUpButton.setEnabled(enabled);
		jobDownButton.setEnabled(enabled);
		jobEnableAllButton.setEnabled(enabled);
		jobDisableAllButton.setEnabled(enabled);
		
		syncQViewFilterNewFilesCheckBox.setEnabled(enabled);
		syncQViewFilterNewFilesLabel.setEnabled(enabled);
		syncQViewFilterModifiedFilesCheckBox.setEnabled(enabled);
		syncQViewFilterModifiedFilesLabel.setEnabled(enabled);
		syncQViewFilterLargerFilesCheckBox.setEnabled(enabled);
		syncQViewFilterLargerFilesLabel.setEnabled(enabled);
		syncQViewFilterLargerAndModifiedFilesCheckBox.setEnabled(enabled);
		syncQViewFilterLargerAndModifiedFilesLabel.setEnabled(enabled);
		syncQViewFilterDeletedFilesCheckBox.setEnabled(enabled);
		syncQViewFilterDeletedFilesLabel.setEnabled(enabled);
		syncQViewFilterDeletedDirsCheckBox.setEnabled(enabled);
		syncQViewFilterDeletedDirsLabel.setEnabled(enabled);
		syncQViewFilterResetButton.setEnabled(enabled);
		syncQViewFilterOKButton.setEnabled(enabled);
		syncQViewFilterConflictFilesCheckBox.setEnabled(enabled);
		syncQViewFilterConflictFilesLabel.setEnabled(enabled);

		syncQSyncFilterNewFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterNewFilesLabel.setEnabled(enabled);
		syncQSyncFilterModifiedFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterModifiedFilesLabel.setEnabled(enabled);
		syncQSyncFilterLargerFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterLargerFilesLabel.setEnabled(enabled);
		syncQSyncFilterLargerAndModifiedFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterLargerAndModifiedFilesLabel.setEnabled(enabled);
		syncQSyncFilterDeletedFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterDeletedFilesLabel.setEnabled(enabled);
		syncQSyncFilterDeletedDirsCheckBox.setEnabled(enabled);
		syncQSyncFilterDeletedDirsLabel.setEnabled(enabled);
		syncQSyncFilterConflictFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterConflictFilesLabel.setEnabled(enabled);
		syncQSyncFilterResetButton.setEnabled(enabled);
		syncQSyncFilterOKButton.setEnabled(enabled);
		syncQSyncFilterConflictFilesCheckBox.setEnabled(enabled);
		syncQSyncFilterConflictFilesLabel.setEnabled(enabled);
		
		messagesViewFilterInfosCheckBox.setEnabled(enabled);
		messagesViewFilterInfosLabel.setEnabled(enabled);
		messagesViewFilterWarningsCheckBox.setEnabled(enabled);
		messagesViewFilterWarningsLabel.setEnabled(enabled);
		messagesViewFilterErrorsCheckBox.setEnabled(enabled);
		messagesViewFilterErrorsLabel.setEnabled(enabled);
		messagesViewFilterFileOperationsCheckBox.setEnabled(enabled);
		messagesViewFilterFileOperationsLabel.setEnabled(enabled);
		messagesViewFilterResetButton.setEnabled(enabled);
		messagesViewFilterOKButton.setEnabled(enabled);

		//redundant?
//		if (enabled) {
//			jobDialog.arrangeJobDialogTabs();
//		}
	}

	protected void pause() {
		if (DirSyncPro.getSync().getState() == Sync.STOP
				|| DirSyncPro.getSync().getState() == Sync.STOPPING) {
			pauseButton.setSelected(false);
			return;
		}

		if (DirSyncPro.getSync().getState() == Sync.START) {
			DirSyncPro.getSync().setState(Sync.PAUSE);
			pauseButton.setSelected(true);
			return;
		}

		if (DirSyncPro.getSync().getState() == Sync.PAUSE) {
			DirSyncPro.getSync().setState(Sync.START);
			pauseButton.setSelected(false);
			return;
		}
	}

	protected void stop() {
		pauseButton.setSelected(false);

		if (DirSyncPro.getSync().getState() == Sync.STOP) {
			return;
		}
		DirSyncPro.getSync().setState(Sync.STOPPING);
		DirSyncPro.getSync().setError(SyncError.Aborted);
	}

	protected void enableAllDirs(boolean enabled) {
		DirSyncPro.getSync().enableAllJobs(enabled);
		updateJobsTree();
	}

//	public void updateSyncQTableColumnSizesInGUIEDT(){
//		helperProcess = new SwingWorker<Void, Void>() {
//			protected Void doInBackground() {
//				autoResizeColumn(syncQTable, 1);
//				autoResizeColumn(syncQTable, 2);
//				autoResizeColumn(syncQTable, 5);
//				autoResizeColumn(syncQTable, 6);
//				return null;
//			}
//		};
//		helperProcess.execute();
//	}

	public void autoResizeSyncQColumns(){
		//syncQTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int width = 0;
		width += autoResizeColumn(syncQTable, 1, "00-00-00 00:00");
		width += autoResizeColumn(syncQTable, 2, "999.9 MB");
		width += autoResizeColumn(syncQTable, 5, "00-00-00 00:00");
		width += autoResizeColumn(syncQTable, 6, "999.9 MB");
		
		width = (syncQTable.getParent().getSize().width - width - 64)/2;
		setColumnSize(syncQTable, 0, width, false);
		setColumnSize(syncQTable, 4, width, false);
	}
	
	private int autoResizeColumn(JTable table, int column, String str){
		JLabel jLabel = new JLabel();
		jLabel.setText(str);
		int width = jLabel.getPreferredSize().width + table.getIntercellSpacing().width;
		setColumnSize(table, column, width, true);
		return width;
	}
	
	private void setColumnSize(JTable table, int column, int width, boolean fixed){
		table.getTableHeader().setResizingColumn(table.getColumnModel().getColumn(column));
		if (fixed){
			table.getColumnModel().getColumn(column).setMaxWidth (width);
		}
		table.getColumnModel().getColumn(column).setMinWidth (width);
		table.getColumnModel().getColumn(column).setPreferredWidth (width);
		//table.getTableHeader().setResizingColumn(null);
	}
	
	private void autoResizeColumn(JTable table, int column){
		int width = 0;
		int tableInterSpacing = table.getIntercellSpacing().width;
		int tableRowCount = table.getRowCount();
		//DirSyncPro.getSync().getLog().printMinimal("Starting to resize", IconKey.Error);
		for (int i = 0; i < tableRowCount ; i++){
			int newWidth = table.getCellRenderer(i, column).getTableCellRendererComponent(table, table.getValueAt(i, column), false, false, i, column).getPreferredSize().width + tableInterSpacing;
			if (newWidth > width){
				width = newWidth;
			}
		}
		if (width >= 20){
			table.getTableHeader().setResizingColumn(table.getColumnModel().getColumn(column));
			table.getColumnModel().getColumn(column).setWidth(width);
			table.getTableHeader().setResizingColumn(null);
		}
		//DirSyncPro.getSync().getLog().printMinimal("Resized", IconKey.Error);
	}

	public void scrollMessageTableToButton(){
		JScrollBar bar = messagesTableScrollPane.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
		messagesTable.revalidate();
		messagesTable.repaint();
	}
	
	private void updateSchedulesInGUI(){
		scheduleEngineStatusField.setText((DirSyncPro.getSync().getScheduleEngine().isRunning() ? "Running!" : "Not running!"));
		scheduleTable.revalidate();
		scheduleTable.repaint();
		DirSyncPro.getSync().getScheduleEngine().setScheduleQUpdated(false);
	}

	private void updateMessagesInGUI(){
		messagesTable.revalidate();
		messagesTable.repaint();
		DirSyncPro.getSync().getLog().setMessageQUpdated(false);
	}
	
	private void updateSyncQInGUI(){
		syncQTable.revalidate();
		syncQTable.repaint();
		DirSyncPro.getSync().getSyncQ().setUpdated(false);
	}

	private void updateStatsinGUI(){
	    int analyzedFiles = 0;
		int countFiles = 0;
		int countAlwaysFiles = 0;
		int countNewFiles = 0;
		int countModifiedFiles = 0;
		int countDeleteFiles = 0;
		int countLargerFiles = 0;
		int countLargerAndModifiedFiles = 0;
		int countConflictFiles = 0;
		int analyzedDirs = 0;
		int countDirs = 0;
		int countAlwaysDirs = 0;
		int countNewDirs = 0;
		int countModifiedDirs = 0;
		int countDeleteDirs = 0;

		SyncQ syncQ = DirSyncPro.getSync().getSyncQ();

		if (syncQ != null){
			analyzedFiles = DirSyncPro.getSync().getSyncQ().getTotalAnalyzedFiles();
			countFiles = DirSyncPro.getSync().getSyncQ().getCountFiles();
			countAlwaysFiles = DirSyncPro.getSync().getSyncQ().getCountForcedCopyFiles();
			countNewFiles = DirSyncPro.getSync().getSyncQ().getCountNewFiles();
			countModifiedFiles = DirSyncPro.getSync().getSyncQ().getCountModifiedFiles();
			countDeleteFiles = DirSyncPro.getSync().getSyncQ().getCountRedundantFiles();
			countLargerFiles = DirSyncPro.getSync().getSyncQ().getCountLargerFiles();
			countLargerAndModifiedFiles = DirSyncPro.getSync().getSyncQ().getCountLargerAndModifiedFiles();
			countConflictFiles = DirSyncPro.getSync().getSyncQ().getCountConflictFiles();
			analyzedDirs = DirSyncPro.getSync().getSyncQ().getTotalAnalyzedDirs();
			countDirs = DirSyncPro.getSync().getSyncQ().getCountDirs();
			countAlwaysDirs = DirSyncPro.getSync().getSyncQ().getCountForcedCopyDirs();
			countNewDirs = DirSyncPro.getSync().getSyncQ().getCountNewDirs();
			countModifiedDirs = DirSyncPro.getSync().getSyncQ().getCountModifiedDirs();
			countDeleteDirs = DirSyncPro.getSync().getSyncQ().getCountRedundantDirs();
		}

		syncQTotalNumberOfAnalyzedFiles.setText(analyzedFiles+ "");
		syncQNumberOfFilesToBeSynced.setText(countFiles+ "");
		syncQNumberOfAllwaysFiles.setText(countAlwaysFiles+ "");
		syncQNumberOfNewFiles.setText(countNewFiles + "");
		syncQNumberOfModifieldFiles.setText(countModifiedFiles + "");
		syncQNumberOfLargerFiles.setText(countLargerFiles + "");
		syncQNumberOfLargerAndModifiedFiles.setText(countLargerAndModifiedFiles + "");
		syncQNumberOfDeletedFiles.setText(countDeleteFiles + "");
		syncQNumberOfConflictFiles.setText(countConflictFiles + "");

		syncQTotalNumberOfAnalyzedDirs.setText(analyzedDirs+ "");
		syncQNumberOfDirsToBeSynced.setText(countDirs + "");
		syncQNumberOfAllwaysDirs.setText(countAlwaysDirs + "");
		syncQNumberOfNewDirs.setText(countNewDirs + "");
		syncQNumberOfModifieldDirs.setText(countModifiedDirs + "");
		syncQNumberOfDeletedDirs.setText(countDeleteDirs + "");

		syncQNumberOfFilesDirsToBeSynced.setText("<html><b>" + (countFiles + countDirs) + "</b></html>");
		syncQNumberOfAllwaysFilesDirs.setText("<html><b>" + (countAlwaysFiles + countAlwaysDirs) + "</b></html>");
		syncQNumberOfNewFilesDirs.setText("<html><b>" + (countNewFiles + countNewDirs) + "</b></html>");
		syncQNumberOfModifieldFilesDirs.setText("<html><b>" + (countModifiedFiles + countModifiedDirs) + "</b></html>");
		syncQNumberOfLargerFilesDirs.setText("<html><b>" + countLargerFiles + "</b></html>");
		syncQNumberOfLargerAndModifiedFilesDirs.setText("<html><b>" + countLargerAndModifiedFiles + "</b></html>");
		syncQNumberOfDeletedFilesDirs.setText("<html><b>" + (countDeleteFiles + countDeleteDirs) + "</b></html>");
		syncQNumberOfConflictFilesDirs.setText("<html><b>" + (countConflictFiles) + "</b></html>");
		syncQTotalNumberOfFilesDirs.setText("<html><b>" + (analyzedFiles + analyzedDirs) + "</b></html>");
		
		syncQBytesAllwaysFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesForcedCopyFiles()));
		syncQBytesNewFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesNewFiles()));
		syncQBytesModifieldFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesModifiedFiles()));
		syncQBytesLargerFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesLargerFiles()));
		syncQBytesLargerAndModifiedFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesLargerAndModifiedFiles()));
		syncQBytesDeletedFilesDirs.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesRedundantFiles()));
		syncQBytesFilesDirsToBeSynced.setText(TextFormatTool.getHumanReadable(DirSyncPro.getSync().getSyncQ().getBytesFiles()));
	}
	
	
	/**
	 * Sets the current configuration.
	 * 
	 * @param currentConfig
	 *            The currentConfig to set.
	 */
	public void setCurrentConfig(File currentConfig) {
		this.currentConfig = currentConfig;
		updateTitle();
	}
	
	/**
	 * Returns the current config
	 * @return Current Config
	 */
	public File getCurrentConfig() {
		return currentConfig;
	}

	
	/**
	 * Updates the frame title with configuration name and progress.
	 */
	public void updateTitle() {
		String title = "";

		// progress of sync
		if (DirSyncPro.getSync().getState() != Sync.STOP) {
			title += totalProgress.getString() + " ";
		}

		// filename of current config
		if (currentConfig != null) {
			title += currentConfig.getName() + " - ";
		}

		// title of program
		title += Const.PROGRAM;

		setTitle(title);
	}

	/**
	 * Initializes the progress bars
	 * @param totalValue
	 * @param totalMax
	 * @param totalAdd
	 * @param totalString
	 * @param totalIndeterminate
	 * @param currentValue
	 * @param currentMax
	 * @param currentString
	 * @param currentIndeterminate
	 */
	private String nor(int i){
		String s = i + "";
		while(s.length() < 8){
			s += " ";
		}
		return s;
	}
	public void registerProgressBars(int totalValue, int totalMax, int totalAdd, String totalString, boolean totalIndeterminate, int currentValue, int currentMax, String currentString, boolean currentIndeterminate) {
		//System.out.println("Registered: TVal=" + nor(totalValue) + "\tTMax=" + nor(totalMax) + "\tTadd=" + nor(totalAdd) + "\tCVal=" + nor(currentValue) + "\tCMax=" + nor(currentMax) + "\tTInderterminate=" + totalIndeterminate + "\tCInderterminate=" + currentIndeterminate + "\tTStr='" + totalString + "', CStr='" + currentString + "'");
		//System.out.println("Prev:       TVal=" + nor(prevProgressTotalValue) + "\tTMax=" + nor(prevProgessTotalMax) + "\tTadd=" + nor(prevProgessTotalAdd) + "\tCVal=" + nor(prevProgessCurrentValue) + "\tCMax=" + nor(prevProgessCurrentMax) + "\tTInderterminate=" + prevProgessTotalIndeterminate + "\tCInderterminate=" + prevProgessCurrentIndeterminate + "\tTStr='" + prevProgessTotalString + "', CStr='" + prevProgessCurrentString + "'");
		if (totalValue != -1){
			progressTotalValue = totalValue;
		}
		if (totalMax != -1) progressTotalMax = totalMax;
		if (totalAdd != -1) progressTotalAdd = totalAdd;
		if (totalAdd == 100 && prevProgessTotalAdd != totalAdd){
			progressTotalValue += 100;
		}
		progressTotalString = totalString;
		progressTotalIndeterminate = totalIndeterminate;
		if (currentValue != -1) progressCurrentValue = currentValue;
		if (currentMax != -1) progressCurrentMax = currentMax;
		if (currentString != null && !currentString.equals("") || currentString == null) progressCurrentString = currentString;
		progressCurrentIndeterminate = currentIndeterminate;
	}
	
	public void forceUpdateGUIEDT(){
		while (updatingGUI.get()){
		}
		if (!updatingGUI.get()){
		    if (SwingUtilities.isEventDispatchThread()){
		    	updateGUI();
		    }else{
			Runnable r = new Runnable(){
				public void run(){
					updateGUI();			
				}
			};
			SwingUtilities.invokeLater(r);
		    }
		}
	}
	
	public void updateGUIEDT(boolean forced){
		if (forced){
			//wait till we may update or two seconds
			Timer timer = new Timer(2000, new ActionListener() {
	        	@Override
	            public void actionPerformed(ActionEvent evt) {
        			((Timer) evt.getSource()).stop();
	            }
	        });
	    	timer.start();
			while (updatingGUI.get() || !timer.isRunning()){
				//wait
			}
		}
		if (!updatingGUI.get()){
		    if (SwingUtilities.isEventDispatchThread()){
		    	updateGUI();
		    }else{
			Runnable r = new Runnable(){
				public void run(){
					updateGUI();			
				}
			};
			SwingUtilities.invokeLater(r);
		    }
		}
	}
	
	private void updateGUI(){
		if (updatingGUI.compareAndSet(false, true)) {
			//always update progressbars to show the time
			updateProgressBars();
			if (DirSyncPro.getSync().getSyncQ() != null && DirSyncPro.getSync().getSyncQ().isUpdated()) {
				updateStatsinGUI();
				updateSyncQInGUI();
			}
			if (DirSyncPro.getSync().getScheduleEngine().isScheduleQUpdated()) {
				updateSchedulesInGUI();
			}
			if (DirSyncPro.getSync().getLog().isMessageQUpdated()) {
				updateMessagesInGUI();
			}
			DirSyncPro.getGui().revalidate();
			DirSyncPro.getGui().repaint();
			updatingGUI.set(false);
		}
	}
	
	private void updateProgressBars() {
		//String tmpTotalString = DirSyncPro.getSync().getSyncTime((float) (currentValue+totalAdd)/totalMax);
		//System.out.println("Set       : TVal=" + nor(totalValue) + "\tTMax=" + nor(totalMax) + "\tTadd=" + nor(totalAdd) + "\tCVal=" + nor(currentValue) + "\tCMax=" + nor(currentMax) + "\tTStr='" + totalString + "', CStr='" + currentString + "' Time='" + tmpTotalString + "'");
		//totalMax
		if (progressTotalMax != prevProgessTotalMax){
			totalProgress.setMaximum(progressTotalMax);
			prevProgessTotalMax = progressTotalMax;
		}

		//current
		if (progressCurrentValue != prevProgessCurrentValue){
			currentProgress.setValue(progressCurrentValue);
			prevProgessCurrentValue = progressCurrentValue;
			if (progressTotalAdd > 0){
				// update the total progress bar too, only during analyze
				totalProgress.setValue(progressTotalValue + progressTotalAdd);
			}
		}

		if (progressCurrentMax != prevProgessCurrentMax){
			currentProgress.setMaximum(progressCurrentMax);
			prevProgessCurrentMax = progressCurrentMax;
		}

		if (progressCurrentString == null){
			currentProgress.setString(progressCurrentString);
			prevProgessCurrentString = progressCurrentString;
		}else if (!progressCurrentString.equals(prevProgessCurrentString)){
			currentProgress.setString(progressCurrentString);
			prevProgessCurrentString = progressCurrentString;
		}
		if (progressCurrentIndeterminate != prevProgessCurrentIndeterminate){
			currentProgress.setIndeterminate(progressCurrentIndeterminate);
			prevProgessCurrentIndeterminate = progressCurrentIndeterminate;
		}
		
		//total
		if (progressTotalValue != prevProgressTotalValue){
			totalProgress.setValue(progressTotalValue);
			prevProgressTotalValue = progressTotalValue;
		}

		//total string could only be set to null otherwise will always be used as timer
		String tmpTotalString = DirSyncPro.getSync().getSyncTime((float) (progressTotalValue + progressTotalAdd)/progressTotalMax);
		if (progressTotalString == null){
			totalProgress.setString(progressTotalString);
			prevProgessTotalString = progressTotalString;
		}else{
			// it should always come here to show the time, so no comparison with the prevProgressTotalString
			totalProgress.setString(tmpTotalString);
			prevProgessTotalString = tmpTotalString;
		}
		if (progressTotalIndeterminate != prevProgessTotalIndeterminate){
			totalProgress.setIndeterminate(progressTotalIndeterminate);
			prevProgessTotalIndeterminate = progressTotalIndeterminate;
		}
	}
	
	public ScreenUpdater getScreenUpdater() {
		return screenUpdater;
	}
	
	public File getCurrectConfig(){
		return currentConfig;
	}
	
	public JobDialog getJobDialog(){
		return jobDialog;
	}
	
	public UpdateDialog getUpdateDialog(){
		return updateDialog;
	}
	
	public Job getSelectedJob(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node == null || node.getLevel() == 0){
			return null;
		}else{
			if (node.getUserObject() instanceof Filter){
				return ((Filter) node.getUserObject()).getJob();
			}else{
				return (Job) node.getUserObject();
			}
		}
	}
	
	private JobTree getSelectedJobTreeNode(){
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jobsTree.getLastSelectedPathComponent();
		if (node == null || node.getLevel() == 0){
			return null;
		}else{
			return (JobTree) node;
		}
	}
	
	public void removeTrayIcon(){
		if (dirSyncProTray != null){
			dirSyncProTray.removeTrayIcon();
		}
	}
	
	public void updateAllComponentTreeUI(){
		// repaint all components
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(settingsDialog);
		SwingUtilities.updateComponentTreeUI(licenseDialog);
		SwingUtilities.updateComponentTreeUI(settingsDialog);
		SwingUtilities.updateComponentTreeUI(updateDialog);
		SwingUtilities.updateComponentTreeUI(jobDialog);
		SwingUtilities.updateComponentTreeUI(jobDialog.getFilterDialog());
		SwingUtilities.updateComponentTreeUI(jobDialog.getScheduleDialog());
		SwingUtilities.updateComponentTreeUI(cmdDialog);
		autoResizeSyncQColumns();
	}
	/**
	 * Displays the help dialog.
	 */
	public void displayHelpDialog() {
		JOptionPane.showMessageDialog(this, Const.HELP, "Help", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Displays the usage dialog.
	 */
	public void displayUsageDialog() {
		JOptionPane.showMessageDialog(this, Const.USAGE, "Usage", JOptionPane.WARNING_MESSAGE);
	}
	
	private String formatFixedWidthHTML(String s) {
		String str = s;
		str = str.replaceAll("\n\n", "____MARKER____");
		str = str.replaceAll("\n", " ");
		str = str.replaceAll("(.+?)____MARKER____", "<p style='width: 200px;'>$1</p>");
		str = str.replaceAll("<p style='width: 200px;'> *</p>", "");
		return "<html><body>" + str + "</body></html>";
	}
	
	/**
	 * Displays an error message.
	 * @param s The error message.
	 */	
	public void displayErrorDialog(String s) {
		JOptionPane.showMessageDialog(this, formatFixedWidthHTML(s), "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a warning message.
	 * @param s The warning message.
	 */
	public void displayWarningDialog(String s) {
		JOptionPane.showMessageDialog(this, formatFixedWidthHTML(s), "Warning", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Displays an info  message.
	 * @param s The info message.
	 */
	public void displayInfoDialog(String s) {
		JOptionPane.showMessageDialog(this, formatFixedWidthHTML(s), "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void displayNotImplementedYetInfoDialog() {
		String s = "This feature is not implementented in this beta version. It will probably be implemented in the next upcoming (beta) version.";
		JOptionPane.showMessageDialog(this, formatFixedWidthHTML(s), "Not implemented yet", JOptionPane.INFORMATION_MESSAGE);
	}
	protected void cmdMenuItemClicked(){
		cmdDialog.initCMDDialog();
		GuiTools.openDialog(cmdDialog);
	}
}
