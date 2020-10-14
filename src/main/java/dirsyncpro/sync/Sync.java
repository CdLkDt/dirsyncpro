/*
 *
 * Copyright (C) 2008-2016 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2006-2008 F. Gerbig
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

import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;

import dirsyncpro.Const;
import dirsyncpro.Const.IconKey;
import dirsyncpro.Const.LogLevel;
import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.DirSyncPro;
import dirsyncpro.exceptions.IncompleteConfigurationException;
import dirsyncpro.exceptions.WarningException;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleEngine;
import dirsyncpro.gui.mainframe.MainFrame;
import dirsyncpro.job.Job;
import dirsyncpro.tools.FileTools;
import dirsyncpro.tools.Log;
import dirsyncpro.tools.WildcardTools;
import dirsyncpro.xml.ConfigConverter;
import dirsyncpro.xml.XmlReader;
import dirsyncpro.xml.XmlWriter;

/**
 * Represents the synchronization.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
public class Sync{

	/** The mode of the synchronization: synchronization, or preview */
	public static final int SYNCHRONIZATION = 1;

	/** The mode of the synchronization: synchronization, or preview */
	public static final int ANALYZE = 2;

	/** The state the synchronization is in: start, pause, stop, or stopping */
	public static final int STOP = 0;

	/** The state the synchronization is in: start, pause, stop, or stopping */
	public static final int STOPPING = -1;

	/** The state the synchronization is in: start, pause, stop, or stopping */
	public static final int START = 1;

	/** The state the synchronization is in: start, pause, stop, or stopping */
	public static final int PAUSE = 2;


	public enum SyncError{
		NoError(0),
		Warning(1),
		ErrorThisJob(1),
		ErrorOtherJob(1),
		ErrorFatal(2),
		Aborted(3);

		private int exitCode;

		private SyncError(int ec){
			exitCode = ec;
		}

		public int getExitCode() {
			return exitCode;
		}
	}

	private String syncSessionName;
	
	/** The directory definitions of the synchronization */
	private Vector<Job> jobs = new Vector<Job>();

	private int dirCounter = 0;

	private int mode = SYNCHRONIZATION;

	private int state = STOP;

	private SyncError error = SyncError.NoError;

	private Date syncDate;

	private Log log;
	private LogLevel logLevel;
	
	private boolean optionsChanged = false;
	private boolean alreadyAnalyzed = false;
	
	private SyncQ syncQ;
	private ScheduleEngine scheduleEngine;
	
	/**
	 * Initializes a new synchronization.
	 */
	public Sync() {
		
		jobs = new Vector<Job>();
		syncQ = new SyncQ();
		scheduleEngine = new ScheduleEngine();

		Job job = new Job(true);
		job.setName(Const.DEFAULT_JOBSET_NAME + " 1");
		jobs.add(job);

		MainFrame gui = DirSyncPro.getGui();
		if (DirSyncPro.isGuiMode()){
			DirSyncPro.getGui().updateJobsTree();
		}
		
		this.syncSessionName = Const.DEFAULT_SYNC_SESSION_NAME;
		setUpLogFile(DirSyncPro.isJobsetLogEnabled());
		this.logLevel = DirSyncPro.getLogLevel();
		this.optionsChanged = false;
		this.alreadyAnalyzed = false;

		mode = SYNCHRONIZATION;
		state = STOP;

	}

	private void checkConfigConvertable(String filename){
		if (ConfigConverter.convertable(filename)){
	    	if (DirSyncPro.isGuiMode()){
				if (JOptionPane.showConfirmDialog(DirSyncPro.getGui(), "Your config file is created with an earlier version of\nDirSync Pro. Do you wish to make a backup, convert\n it to the latest version and save it? ", "Config file coversion", JOptionPane.OK_CANCEL_OPTION) == 0){
					DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ConfigConverter.convert(filename);
					DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					DirSyncPro.getGui().displayInfoDialog("Conversion succeeded! A copy of you original config file is saved to:\n" + filename + ".bak");
				}else{
					return;
				}
			}else {
				log.printMinimal("Your config file is created with an earlier version of DirSync Pro. Please use the GUI to convert your config file to the new format.", Const.IconKey.Error);
			}
		}
	}
	
	public void appendJobs(String filename) throws Exception{
		checkConfigConvertable(filename);
		XmlReader xmlReader = new XmlReader(filename);
		this.jobs.addAll(xmlReader.getJobs());
		for (Object j : xmlReader.getJobs()){
			if (j instanceof Job){
				((Job)j).restartRealtimeListeners();
			}
		}
	}
	
	public void saveEnabled(String xmlfilename) throws Exception {
		new XmlWriter(xmlfilename, log.getFilename(), this.getEnabledJobs());
	}
	
	/**
	 * Loads a configuration.
	 * 
	 * @param filename
	 *            The name of the file.
	 * @throws Exception
	 */
	public void load(String filename) throws Exception {
		checkConfigConvertable(filename);
		XmlReader xmlReader = new XmlReader(filename);

		this.syncSessionName = filename.replace("." + Const.SYNC_FILE_EXTENSION, "");
		this.log = new Log(xmlReader.getLogFileName(), null);
		this.jobs = xmlReader.getJobs();	
		for (Object j : this.jobs){
			if (j instanceof Job){
				((Job)j).restartRealtimeListeners();
			}
		}
	}

	/**
	 * Saves a configuration.
	 * 
	 * @param filename
	 *            The name of the file.
	 * @throws Exception
	 */
	public void save(String xmlfilename) throws Exception {
		new XmlWriter(xmlfilename, log.getFilename(), jobs);
	}

	/**
	 * Swaps the source and destination directories in all directory
	 * definitions.
	 */
	public void swapSrcDst() {
		String temp;

		// on all jobs
		for (Job job : jobs){
			temp = job.getDirA(); // swap src and dst
			job.setSrc(job.getDirB());
			job.setDst(temp);
		}
	}


	public String getSyncTime(float f){
		long elapsed = 0;
		if (syncDate != null){
			elapsed = ((new Date()).getTime() - syncDate.getTime());
		}
		
		String timeStr = "";
		if (elapsed < 3600000){
			timeStr = (new SimpleDateFormat("mm:ss")).format(new Date(elapsed));
		} else{
			timeStr = (new SimpleDateFormat("HH:mm:ss")).format(new Date(elapsed));
		}
		
		long totalTime = 0;
		if (f > 0){
			totalTime = (long) (elapsed / f);
		}

		String totalTimeStr = "";	
		if (totalTime < 3600000){
			totalTimeStr = (new SimpleDateFormat("mm:ss")).format(new Date(totalTime));
		} else{
			totalTimeStr = (new SimpleDateFormat("HH:mm:ss")).format(new Date(totalTime));
		}
		
		if (totalTime > 0 && elapsed > 60000){
			timeStr += " / " + totalTimeStr;
		}
		
		return timeStr;
	}
	
	private void printConfiguration(){
		log.printExcessive("Synchronization configuration:", Const.IconKey.Info);
		String syncLogFilename = "";
		//TODO: Fixme! jobset log has no wild cards?
		syncLogFilename = WildcardTools.replaceDateWildcards(log.getFilename(), syncDate);
		syncLogFilename = WildcardTools.replaceUserWildcards(syncLogFilename);
		log.printExcessive("  Logfile: \"" + syncLogFilename + "\"", Const.IconKey.Info);
		
		log.printExcessive("Number of jobs to synchronize: " + getNumberOfEnabledJobs(), Const.IconKey.Info);
	}
	
	
	private Job replaceWildCards(Job job){
		String globalLogFilename = "";
		globalLogFilename = WildcardTools.replaceDateWildcards(log.getPath(), syncDate);
		globalLogFilename = WildcardTools.replaceUserWildcards(globalLogFilename);

		// replace wildcards in source directories
		job.setSrc(WildcardTools.replaceUserWildcards(job.getDirA()));
		
		// replace wildcards in destination directories
		job.setDst(WildcardTools.replaceDateWildcards(job.getDirB(), syncDate)); 
		job.setDst(WildcardTools.replaceTimeWildcards(job.getDirB(), syncDate)); 
		job.setDst(WildcardTools.replaceUserWildcards(job.getDirB())); 

		// replace date, time, and user wildcards in log file
		String logFilename = WildcardTools.replaceDateWildcards(job.getLog().getPath(), syncDate);
		logFilename = WildcardTools.replaceTimeWildcards(logFilename, syncDate);
		logFilename = WildcardTools.replaceUserWildcards(logFilename);
		// replace directory definition name wildcard in log file
		logFilename = WildcardTools.replaceDirectoryWildcards(logFilename, job);
		job.getLog().setFile(logFilename);
		return job;
	}
	
	/**
	 * Analyzes all jobs one by one.
	 */
	public void analyze() {
		analyze(jobs);
	}
	
	/**
	 * Analyzes the given jobs one by one.
	 */
	public void analyze(Vector<Job> js) {
		setState(Sync.START);

		syncQ = new SyncQ();
		try {
			if (DirSyncPro.isGuiMode()) {
				DirSyncPro.getGui().updateGuiFromSyncQViewFilter();
				DirSyncPro.getGui().updateGuiFromSyncQSyncFilter();
				// set cursor to wait cursor
				DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DirSyncPro.getGui().getScreenUpdater().startUpdating();
			}

			setError(SyncError.NoError);
			syncDate = new Date();

			log.printMinimal("Started analyzing.", Const.IconKey.Info);

			printConfiguration();
			
			if (DirSyncPro.isGuiMode()) {
				DirSyncPro.getGui().registerProgressBars(0, js.size() * 100, 0, "", false, -1, -1, "", false);
			}

			dirCounter = 0;
			for (Job jobOrg : js){

				// if an error has happened it has happened in an other
				// directory
				if (getError() == SyncError.ErrorThisJob) {
					setError(SyncError.ErrorOtherJob);
				}

				try {
					// create a copy of the job, so that replacing the wildcards
					// will not affect the actual job definition
					Job job = (Job) jobOrg.clone();

					if (!job.isEnabled()) {
						continue;
					}

					dirCounter++;

					replaceWildCards(job);
					
					// set the syncQ only for the original dir profile. This costs less memory.
					//jobOrg.analyze();
					job.analyze();
					if (DirSyncPro.isGuiMode()) {
						DirSyncPro.getGui().updateGuiFromSyncQViewFilter();
						DirSyncPro.getGui().updateGuiFromSyncQSyncFilter();
					}

					if (isStopping()) {
						log.printMinimal("Stopped!", IconKey.Warning);
						break;
					}

				} catch (IncompleteConfigurationException ice) {
					setError(SyncError.ErrorThisJob);
					log.printMinimal("Skipping because of incomplete configuration.", Const.IconKey.Error);
				}
			}

			// done analyzing the jobs, manage found symbolic links
			if (syncQ.size() <= 0){
				syncQ.filterSymLinks();
			}
			
			switch (getError()) {
				case Warning: // finished with warnings
					log.printMinimal("Finished analyzing with warnings!", IconKey.Warning);
					break;

				case ErrorThisJob: // finished with errors
				case ErrorOtherJob:
					log.printMinimal("Finished analyzing with errors!", IconKey.Error);

					// beep
					Toolkit.getDefaultToolkit().beep();

					// show error message
					if (DirSyncPro.isGuiMode()) {
						DirSyncPro.displayError("Finished analyzing with errors!");
						DirSyncPro.getGui().setToMessagesTab();
						DirSyncPro.getGui().scrollMessageTableToButton();
					}
					break;

				case Aborted:
					log.printMinimal("Analyzing aborted by the user!", IconKey.Info);
					break;

				default: // finished without warnings or errors
					if (syncQ.size() <= 0){
						log.printMinimal("No changes found!", IconKey.Info);
					}
					log.printMinimal("Finished analyzing.", IconKey.Info);
					this.alreadyAnalyzed = true;
			}

			// quit if "/quit" specified as command line option
			if (DirSyncPro.isOption_analyze() && DirSyncPro.isOption_quit()) {
				System.exit(error.getExitCode());
			}

		} catch (OutOfMemoryError e){
			outOfMemoryError(e);
		}catch (Throwable e) {
			fatalErrorOccured(e);
		} finally {
			setState(Sync.STOP);
			//getSyncQ().filterSymLinks();
			if (DirSyncPro.isGuiMode()) {
				if (syncQ.size() <= 0 && scheduleEngine.getScheduleQ().size() <= 0){
					DirSyncPro.getGui().setToMessagesTab();
				}
				// change cursor back to default cursor
				DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				DirSyncPro.getGui().getScreenUpdater().stopUpdating();
				//catch up the last values of the progess bars
				//DirSyncPro.getGui().autoResizeSyncQColumns(); //runs once now in MainFrame construct. //TODO delete after a while? 
				DirSyncPro.getGui().registerProgressBars(1, 1, 0, "", false, 1, 1, null, false);
				DirSyncPro.getGui().updateGUIEDT(true);
				DirSyncPro.getGui().updateTitle();
				DirSyncPro.getGui().scrollMessageTableToButton();
			}
		}
	}

	/**
	 * Synchronizes the Sync and eventually analyzes all jobs
	 */
	public void synchronize(){
		synchronize(jobs);
	}
	
	/**
	 * Synchronizes the Sync and eventually analyzes the given jobs
	 */
	public void synchronize(Vector<Job> js) {
		
		if (!alreadyAnalyzed || syncQ.isSynced()){
			analyze(js);
		}
	
		if (isStopping()) {
			// syncQ is done.
			syncQ.setSynced(true);
			return;
		} 
		
		setState(Sync.START);

		try {
			if (DirSyncPro.isGuiMode()) {
				// set cursor to wait cursor
				DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				DirSyncPro.getGui().registerProgressBars(0, syncQ.size(), 0, "", false, -1, -1, "", false);
				DirSyncPro.getGui().getScreenUpdater().startUpdating();
			}

			setError(SyncError.NoError);
			syncDate = new Date();
			log.printMinimal("Started synchronization.", Const.IconKey.Info);

			printConfiguration();
			
			for (int i=0; i<syncQ.size(); i++){
				SyncPair sp = syncQ.get(i);
				SyncPairStatus sps = sp.getSyncPairStatus();
				
				if (DirSyncPro.isGuiMode()) {
					sleepOnPause();
					if (isStopping()) {
						// syncQ is done.
						syncQ.setSynced(true);
						return;
					}
				}

				if (sp.isSynced()){ 
					break;
				}
				if (DirSyncPro.isGuiMode()){
//					DirSyncPro.getGui().registerProgressBarsEDT(i+1, syncQ.size(), 0, "Synchonizing items in the sync Q...", false, -1, -1, "", false);
					DirSyncPro.getGui().registerProgressBars(i*100, syncQ.size() * 100, 0, "", false, -1, -1, "", false);
				}
				
				//DirAIsRedundant, FileAIsRedundant, DirBIsRedundant, FileBIsRedundant
				if (getError() == SyncError.ErrorThisJob) {
					getLog().printMinimal("Skipping 'Delete Files' and 'Delete Dirs' because of errors while synchronizing this directory.", IconKey.Error);
				}else{
					try{
						if (sps.equals(SyncPairStatus.DirAIsRedundant) || sps.equals(SyncPairStatus.FileAIsRedundant)){
							if (!sp.getFileA().exists() || FileTools.deleteRecursive(sp.getFileA(), sp.getJob().getDirA(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isOverrideReadOnly())) {
								getLog().printModerate("Deleted:" + sp.getFileA().getAbsolutePath(), IconKey.Deleted);
								sp.setSynced();
								if (sp.getJob().isSyncDirTimeStamps()){
									try{
										FileTools.copyParentTimestamp(sp.getFileB(), sp.getFileA());
									}catch (WarningException e){
										getLog().printModerate(e.getMessage(), IconKey.Warning);
									}
								}
							}else{
								getLog().printModerate("Failed to delete:" + sp.getFileA().getAbsolutePath(), IconKey.Warning);
								setError(SyncError.Warning);
							}
						}
					
						if (sps.equals(SyncPairStatus.DirBIsRedundant) || sps.equals(SyncPairStatus.FileBIsRedundant)){
							if (!sp.getFileB().exists() || FileTools.deleteRecursive(sp.getFileB(), sp.getJob().getDirB(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isOverrideReadOnly())) {
								getLog().printModerate("Deleted:" + sp.getFileB().getAbsolutePath(), IconKey.Deleted);
								sp.setSynced();
								if (sp.getJob().isSyncDirTimeStamps()){
									try{
										FileTools.copyParentTimestamp(sp.getFileA(), sp.getFileB());
									}catch (WarningException e){
										getLog().printModerate(e.getMessage(), IconKey.Warning);
									}
								}
							}else{
								getLog().printModerate("Failed to delete:" + sp.getFileB().getAbsolutePath(), IconKey.Warning);
								setError(SyncError.Warning);
							}
						}
					} catch (WarningException we){
						getLog().printMinimal("Warning: " + we.getMessage(), IconKey.Warning);
						setError(SyncError.Warning);
					}
				}
				//BiDirConflictRename
				if (sps.equals(SyncPairStatus.ConflictResolutionRename)){
					if (sp.getFileA().renameTo(new File(sp.getFileA().getAbsoluteFile() + "." + Const.DIR_A_RENAME_FILE_EXTENSION ))){
						getLog().printModerate("Renamed: " + sp.getFileA().getAbsolutePath(), IconKey.Info);
					}else{
						getLog().printModerate("Failed to rename: " + sp.getFileA().getAbsolutePath(), IconKey.Warning);
						setError(SyncError.Warning);
					}
					if (sp.getFileB().renameTo(new File(sp.getFileB().getAbsoluteFile() + "." + Const.DIR_B_RENAME_FILE_EXTENSION ))){
						getLog().printModerate("Renamed: " + sp.getFileB().getAbsolutePath(), IconKey.Info);
					}else{
						getLog().printModerate("Failed to rename:" + sp.getFileB().getAbsolutePath(), IconKey.Warning);
						setError(SyncError.Warning);
					}
					try{
						FileTools.copy(
								new File(sp.getFileA().getAbsoluteFile() + "." + Const.DIR_A_RENAME_FILE_EXTENSION), 
								new File(sp.getFileB().getAbsoluteFile() + "." + Const.DIR_A_RENAME_FILE_EXTENSION), 
								sp.getJob().getDirB(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isVerify(), sp.getJob().isWriteTimestampBack(), sp.getJob().isSyncDirTimeStamps(), sp.getJob().isPreserveDOSAttributes(), sp.getJob().isPreservePOSIXFilePermissions(), sp.getJob().isPreservePOSIXFileOwnership(), sp.getJob().isOverrideReadOnly());
						getLog().printModerate("Copied: " + sp.getFileA().getAbsoluteFile() + "." + Const.DIR_A_RENAME_FILE_EXTENSION, IconKey.File);
						FileTools.copy(
								new File(sp.getFileB().getAbsoluteFile() + "." + Const.DIR_B_RENAME_FILE_EXTENSION), 
								new File(sp.getFileA().getAbsoluteFile() + "." + Const.DIR_B_RENAME_FILE_EXTENSION), 
								sp.getJob().getDirA(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isVerify(), sp.getJob().isWriteTimestampBack(), sp.getJob().isSyncDirTimeStamps(), sp.getJob().isPreserveDOSAttributes(), sp.getJob().isPreservePOSIXFilePermissions(), sp.getJob().isPreservePOSIXFileOwnership(), sp.getJob().isOverrideReadOnly());
						getLog().printModerate("Copied: " + sp.getFileB().getAbsoluteFile() + "." + Const.DIR_A_RENAME_FILE_EXTENSION, IconKey.File);
						sp.setSynced();
					} catch (WarningException we){
						getLog().printMinimal("Warning: " + we.getMessage(), IconKey.Warning);
						setError(SyncError.Warning);
					}
				}
				
				//MonodirConflict copy source: happens in copy
				
				//Conflict Warn/Skip
				// Do nothing
				
				//DirAIsModified, DirBIsModified
				if (sps.equals(SyncPairStatus.DirAIsModified)){
					try{
						FileTools.copyTimestamp(sp.getFileA().toPath(), sp.getFileB().toPath());
					}catch (WarningException e){
						getLog().printModerate(e.getMessage(), IconKey.Warning);
					}
					sp.setSynced();
				}
				if (sps.equals(SyncPairStatus.DirBIsModified)){
					try{
						FileTools.copyTimestamp(sp.getFileB().toPath(), sp.getFileA().toPath());
					}catch (WarningException e){
						getLog().printModerate(e.getMessage(), IconKey.Warning);
					}
					sp.setSynced();
				}
				
				//DirAIsNew, DirBIsNew, DirAIsCopyAll, DirBIsCopyAll
				if (sps.equals(SyncPairStatus.DirAIsNew) || sps.equals(SyncPairStatus.DirACopyForced)){  
					File f = sp.getFileB();
					if (f.exists()){
						sp.setSynced();
						getLog().printModerate("Dir already exists: " + f.getAbsolutePath(), IconKey.DirA);
					}else{
						if (f.mkdirs()){
							getLog().printModerate("Created dir: " + f.getAbsolutePath(), IconKey.DirA);
							sp.setSynced();
							if (sp.getJob().isSyncDirTimeStamps()){
								try{
									FileTools.copyTimestamp(sp.getFileA().toPath(), sp.getFileB().toPath());
									FileTools.copyParentTimestamp(sp.getFileA(), sp.getFileB());
								}catch (WarningException e){
									getLog().printModerate(e.getMessage(), IconKey.Warning);
								}
							}
						}else{
							getLog().printModerate("Failed to create dir:" + f.getAbsolutePath(), IconKey.Warning);
							setError(SyncError.Warning);
						}
					}
				}
				
				if (sps.equals(SyncPairStatus.DirBIsNew) || sps.equals(SyncPairStatus.DirBCopyForced)){
					File f = sp.getFileA();
					if (f.exists()){
						sp.setSynced();
						getLog().printModerate("Dir already exists: " + f.getAbsolutePath(), IconKey.DirA);
					}else{
						if (f.mkdirs()){
							getLog().printModerate("Created dir: " + f.getAbsolutePath(), IconKey.DirB);
							sp.setSynced();
							if (sp.getJob().isSyncDirTimeStamps()){
								try{
									FileTools.copyTimestamp(sp.getFileB().toPath(), sp.getFileA().toPath());
									FileTools.copyParentTimestamp(sp.getFileB(), sp.getFileA());
								}catch (WarningException e){
									getLog().printModerate(e.getMessage(), IconKey.Warning);
								}
							}
						}else{
							getLog().printModerate("Failed to create dir:" + f.getAbsolutePath(), IconKey.Warning);
							setError(SyncError.Warning);
						}
					}
				}

				//FileAIsNew, FileAIsModified, FileAIsLarger, FileAIsLargerAndModified, FileAIsCopyAll
				//FileBIsNew, FileBIsModified, FileBIsLarger, FileBIsLargerAndModified, FileBIsCopyAll
				try{
					if (sps.equals(SyncPairStatus.FileAIsNew) || sps.equals(SyncPairStatus.FileAIsModified) || sps.equals(SyncPairStatus.FileAIsLarger) || sps.equals(SyncPairStatus.FileAIsLargerAndModified) || sps.equals(SyncPairStatus.FileACopyForced) || sps.equals(SyncPairStatus.ConflictResolutionCopySourceA)){  
						//File f = replacePath(sp.getFileA(), new File(dirA), new File(dirB));
						File f = sp.getFileB();
						FileTools.copy(sp.getFileA(), f, sp.getJob().getDirB(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isVerify(), sp.getJob().isWriteTimestampBack(), sp.getJob().isSyncDirTimeStamps(), sp.getJob().isPreserveDOSAttributes(), sp.getJob().isPreservePOSIXFilePermissions(), sp.getJob().isPreservePOSIXFileOwnership(), sp.getJob().isOverrideReadOnly());
						getLog().printModerate("Copied: " + sp.getFileA().getAbsoluteFile(), IconKey.File);
						sp.setSynced();
					}
					if (sps.equals(SyncPairStatus.FileBIsNew) || sps.equals(SyncPairStatus.FileBIsModified) || sps.equals(SyncPairStatus.FileBIsLarger) || sps.equals(SyncPairStatus.FileBIsLargerAndModified) || sps.equals(SyncPairStatus.FileBCopyForced) || sps.equals(SyncPairStatus.ConflictResolutionCopySourceB)){  
						//File f = replacePath(sp.getFileB(), new File(dirB), new File(dirA));
						File f = sp.getFileA();
						FileTools.copy(sp.getFileB(), f, sp.getJob().getDirA(), sp.getJob().getHowManyBackups(), sp.getJob().getBackupDir(), sp.getJob().isVerify(), sp.getJob().isWriteTimestampBack(), sp.getJob().isSyncDirTimeStamps(), sp.getJob().isPreserveDOSAttributes(), sp.getJob().isPreservePOSIXFilePermissions(), sp.getJob().isPreservePOSIXFileOwnership(), sp.getJob().isOverrideReadOnly());
						getLog().printModerate("Copied: " + sp.getFileB().getAbsoluteFile(), IconKey.File);
						sp.setSynced();
					}
				} catch (WarningException we){
					getLog().printMinimal("Warning: " + we.getMessage(), IconKey.Warning);
					setError(SyncError.Warning);
				}

				//FileAIsSymbolicLink, DirAIsSymLink
				try{
					if (sps.equals(SyncPairStatus.FileAIsSymLink) || sps.equals(SyncPairStatus.DirAIsSymLink)){
						FileTools.copySymLink(sp.getFileA().toPath(), sp.getFileB().toPath());
						sp.setSynced();
					} 
					if (sps.equals(SyncPairStatus.FileBIsSymLink) || sps.equals(SyncPairStatus.DirBIsSymLink)){
						FileTools.copySymLink(sp.getFileB().toPath(), sp.getFileA().toPath());
						sp.setSynced();
					}
				}catch (WarningException we){
					getLog().printMinimal("Warning: " + we.getMessage(), IconKey.Warning);
					setError(SyncError.Warning);
				}
				
				sleepOnPause();
				if (isStopping()) {
					// syncQ is done.
					syncQ.setSynced(true);
					break;
				}
			}

			switch (getError()) {
				case Warning: // finished with warnings
					log.printMinimal("Finished synchronization with warnings!", IconKey.Warning);
					break;

				case ErrorThisJob: // finished with errors
				case ErrorOtherJob:
					log.printMinimal("Finished synchronization with errors!", IconKey.Error);

					// beep
					Toolkit.getDefaultToolkit().beep();

					// show error message
					if (DirSyncPro.isGuiMode()) {
						DirSyncPro.displayError("Finished synchronization with errors!");
						DirSyncPro.getGui().setToMessagesTab();
					}
					break;

				case Aborted:
					log.printMinimal("Synchronization aborted by the user!", IconKey.Info);
					break;

				default: // finished without warnings or errors
					log.printMinimal("Finished synchronization", IconKey.Info);
			}

			
			// quit if "/quit" specified as command line option
			if (DirSyncPro.isOption_quit()) {
				System.exit(error.getExitCode());
			}

		} catch (OutOfMemoryError e){
			outOfMemoryError(e);
		} catch (Throwable e) {
			fatalErrorOccured(e);
		} finally {
			setState(Sync.STOP);
			if (DirSyncPro.isGuiMode()) {
				if (syncQ.size() <= 0 && scheduleEngine.getScheduleQ().size() <= 0){
					DirSyncPro.getGui().setToMessagesTab();
				}
				// change cursor back to default cursor
				DirSyncPro.getGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				DirSyncPro.getGui().getScreenUpdater().stopUpdating();
				DirSyncPro.getGui().registerProgressBars(1, 1, 0, "", false, 1, 1, null, false);
				//catch up the last values of the progess bars
				DirSyncPro.getGui().updateGUIEDT(true);
				DirSyncPro.getGui().updateTitle();
				DirSyncPro.getGui().scrollMessageTableToButton();

				if (!DirSyncPro.isKeepSyncQAfterSync()){
					//Sync is done
					syncQ.setSynced(true);
				}
			}
			DirSyncPro.getGui().shutDownIfNecessary();
		}
	}

	private void outOfMemoryError(OutOfMemoryError e){
		if (log != null) {
			log.printMinimal("Out of memory error occured; the program will write debug info to the global log file (if enabled) and exit.", IconKey.Error);
			log.printMinimal("Java (JRE) may have been started with low memroy.\n", IconKey.Error);
			log.printMinimal("Please restart DirSync Pro using the DirSyncPro.exe executable (MS \n", IconKey.Error);
			log.printMinimal("Windows) or by using -Xmx512M flag (java -Xmx512M -jar dirsyncpro.jar)\n", IconKey.Error);
			log.printMinimal("to allocate more memroy for DirSync Pro.\n\n", IconKey.Error);
			log.printMinimal("*** DEBUG INFORMATION START ***", IconKey.Error);
			log.printMinimal("OS:      " + System.getProperty("os.name") + " on " + System.getProperty("os.arch"), IconKey.Error);
			log.printMinimal("JAVA:    " + System.getProperty("java.vendor") + " Version " + System.getProperty("java.version") + " Class-Version "	+ System.getProperty("java.class.version"), IconKey.Error);
			log.printMinimal("PROGRAM: " + Const.PROGRAM, IconKey.Error);
			log.printThrowableStack(e);
			log.printMinimal("*** DEBUG INFORMATION END ***", IconKey.Error);
			log.printMinimal("", IconKey.Error);
			log.printMinimal("If this problem keeps happening, please try to reproduce the error", IconKey.Error);
			log.printMinimal("and to find out which specific action triggers it; then send a description of", IconKey.Error);
			log.printMinimal("this error and the debug information to:" + Const.EMAIL, IconKey.Error);
			log.printMinimal("Thanks in advance.", IconKey.Error);
			log.printMinimal("", IconKey.Error);
			log.printMinimal("Exiting...", IconKey.Error);

			if (DirSyncPro.isGuiMode()) {
				DirSyncPro.displayError(
						"Out of memory Error!\n\n" + 
						"Java (JRE) may have been started with low memroy.\n" +
						"Please restart DirSync Pro using the DirSyncPro.exe executable (MS \n" +
						"Windows) or by using -Xmx512M flag (java -Xmx512M -jar dirsyncpro.jar)\n" +
						"to allocate more memroy for DirSync Pro.\n\n" +
						"If this problem keeps happening, please try to reproduce the error and to\n" +
						"find out which specific action triggers it; then send a description of\n" +
						"this error and the debug information to: " + Const.EMAIL + 
						"\n\nThanks in advance!\n\n" +
						"DirSync Pro will quit now!");
			}
		}
		getLog().printThrowableStack(e);
		e.printStackTrace(System.err);
		System.exit(SyncError.ErrorFatal.getExitCode());
	}
	
	private void fatalErrorOccured(Throwable e){
		if (log != null) {
			log.printMinimal("A fatal error occured; the program will write debug info to the global log file (if enabled) and exit.", IconKey.Error);
			log.printMinimal("*** DEBUG INFORMATION START ***", IconKey.Error);
			log.printMinimal("OS:      " + System.getProperty("os.name") + " on " + System.getProperty("os.arch"), IconKey.Error);
			log.printMinimal("JAVA:    " + System.getProperty("java.vendor") + " Version " + System.getProperty("java.version") + " Class-Version "	+ System.getProperty("java.class.version"), IconKey.Error);
			log.printMinimal("PROGRAM: " + Const.PROGRAM, IconKey.Error);
			log.printMinimal("FILE ENCODING: " + System.getProperty("file.encoding"), IconKey.Error);
			log.printThrowableStack(e);
			log.printMinimal("*** DEBUG INFORMATION END ***", IconKey.Error);
			log.printMinimal("", IconKey.Error);
			log.printMinimal("Please try to reproduce the error and to find out which specific action triggers it;", IconKey.Error);
			log.printMinimal("then send a description of this error and the debug information to: " + Const.EMAIL, IconKey.Error);
			log.printMinimal("Thanks in advance.", IconKey.Error);
			log.printMinimal("", IconKey.Error);
			log.printMinimal("Exiting...", IconKey.Error);

			if (DirSyncPro.isGuiMode()) {
				DirSyncPro.displayError(
						"A fatal error occured:\n" + e + ".\n\n" + 
						"The program will write debug information to the global log file (if enabled) and exit.\n\n"	+ 
						"Please try to reproduce the error and to find out which specific action triggers it;\n" + 
						"then send a description of this error and the debug information to:\n" + Const.EMAIL + 
						"\n\nThanks in advance!");
			}
		}
		getLog().printThrowableStack(e);
		e.printStackTrace(System.err);
		System.exit(SyncError.ErrorFatal.getExitCode());
	}
	
	private void checkAndSyncParentDirs(SyncPair sp){
		if (sp.getJob().isSyncDirTimeStamps()){
			
		}
	}
	
	/**
	 * Checks if the mode if the synchronization is analyze.
	 * 
	 * @return <code>true</code> if the synchronization is in analyze mode,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAnalyze() {
		return (mode == ANALYZE);
	}

	/**
	 * @return Returns the syncMode.
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * @param syncMode
	 *            The syncMode to set.
	 */
	public void setMode(int syncMode) {
		this.mode = syncMode;
	}

	/**
	 * @return Returns the syncState.
	 */
	public int getState() {
		return state;
	}

	/**
	 * Checks if DirSyncPro is stopping, i.e. if <code>syncState</code> is set to
	 * <code>STOPPING</code>.
	 * 
	 * @return <code>true</code> if DirSyncPro is stopping.
	 */
	public boolean isStopping() {
		return state == STOPPING;
	}

	/**
	 * @param syncState
	 *            The syncState to set.
	 */
	public void setState(int syncState) {
		this.state = syncState;
	}

	/**
	 * Sleeps if <code>syncState</code> is pause.
	 */
	public void sleepOnPause() {
		while (getState() == PAUSE) {
			try {
				Thread.sleep(Const.PAUSE);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * @param error
	 *            The error to set.
	 */
	public void setError(SyncError error) {
		this.error = error;
	}

	/**
	 * @return Returns the error.
	 */
	public SyncError getError() {
		return error;
	}

	/**
	 * Counts the number of enables directory definitions.
	 * 
	 * @return The number of enables directory definitions.
	 */
	public int getNumberOfEnabledJobs() {
		int count = 0;
		for (Job job : jobs){
			if (job.isEnabled()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Set the enables status of all directory definitions to the given value.
	 * 
	 * @param enabled
	 *            The value to set in all directory definitions.
	 */
	public void enableAllJobs(boolean enabled) {
	    	for (Job job : jobs){
			job.setEnabled(enabled);
		}
	}

	/**
	 * Set the options of all directory definitions to the default settings.
	 */
	public void setAllJobsToDefaults() {
	    	for (Job job : jobs){
			if (job.isEnabled()){
				job.init();
			}
		}
	}


	/**
	 * Returns the next postfix numbering index for a new/copied directory
	 * @param s
	 * 			The string after which the postfix string should be searched
	 * @return the next postfix integer index 
	 */
	public int getNextJobPostfixStringIndex(String s){
		int max = 1;
	    	for (Job job : jobs){
			if (job.getName().indexOf(s, 0) >= 0){
				String n = job.getName().substring(s.length());
				try{
					int i = Integer.valueOf(n).intValue();
					i++;
					if (i > max) max = i;
				}catch (NumberFormatException e){
				}
			}
		}
		return max;
	}
	
	/**
	 * Copy the options of the given job definitions to ALL job
	 * definitions (including itself).
	 * 
	 * @param jobWithOptions
	 *            The directory definition with the options to copy.
	 */
	public void copyOptionsToAllJobs(Job jobWithOptions) {
	    	for (Job job : jobs){
			job.copyOptions(jobWithOptions);
		}
	}

	/**
	 * Copy the options of the given job definitions to all ENABLED
	 * job definitions (including itself).
	 * 
	 * @param jobWithOptions
	 *            The directory definition with the options to copy.
	 */
	public void copyOptionsToEnabledJobs(Job jobWithOptions) {
	    	for (Job job : jobs){
			if (job.isEnabled()) {
				job.copyOptions(jobWithOptions);
			}
		}
	}

	/**
	 * @return Returns the jobs.
	 */
	public Vector<Job> getJobs() {
		return jobs;
	}

	public int getIndexOfJobInJobList(Job j){
		int i = 0;
	    	for (Job job : jobs){
			if (job.equals(j)){
				return i;
			}else{
				i++;
			}
		}
		if (i < jobs.size()){
			return i;
		}else{
			return -1;
		}
	}
	
	/**
	 * @return Returns the enabled jobs.
	 */
	public Vector<Job> getEnabledJobs() {
		Vector<Job> enabledDirs = new Vector<Job>();
	    	for (Job job : jobs){
			if (job.isEnabled()){
				enabledDirs.add(job);
			}
		}
		return enabledDirs;
	}

	public Log getLog(){
		return log;
	}
	
	/**
	 * @param withFile: wheher to setup a log file or no log file
	 */
	public void setUpLogFile(boolean withFile) {
		if (log == null){
			this.log = new Log(withFile ? syncSessionName + "." + Const.LOG_FILE_EXTENSION : "", null);
		}else{
			this.log.setFile(withFile ? syncSessionName + "." + Const.LOG_FILE_EXTENSION : "");
		}
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	// FIXME: overbodig?
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * @return the syncQ
	 */
	public SyncQ getSyncQ() {
		return syncQ;
	}
	
	/**
	 * clear the syncQ
	 */
	public void clearSyncQ() {
		syncQ = new SyncQ();
		this.alreadyAnalyzed = false;
	}

	/**
	 * @return the syncSessionName
	 */
	public String getName() {
		return syncSessionName;
	}

	/**
	 * @return the optionsChanged
	 */
	public boolean isOptionsChanged() {
		return optionsChanged;
	}

	/**
	 * @param optionsChanged the optionsChanged to set
	 */
	public void setOptionsChanged(boolean optionsChanged) {
		this.optionsChanged = optionsChanged;
	}

	/**
	 * @return the scheduleEngine
	 */
	public ScheduleEngine getScheduleEngine() {
		return scheduleEngine;
	}

	public Date getSyncDate() {
		return syncDate;
	}
}
