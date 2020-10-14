/*
 * Const.java
 * 
 * Copyright (C) 2008-2018 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro;

import java.awt.Color;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import dirsyncpro.tools.TimeTool;

/**
 * Contains the constants used in DirSyncPro. 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
public class Const {
	//Don't let anyone instantiate this class.
	private Const(){
	}

	/** The version number. */ // TODO program version
	public static final String VERSION = "1.53";

	/** The program name */
	public static final String PROGRAM = "DirSync Pro " + VERSION;

	public static final String dspHomeDir = ".DirSyncPro";
	public static final String dspConfigDir = "configs";
	public static final String dspLogsDir = "logs";
	public static final boolean dspHomeInline = true;
	
	public static final int DEFAULT_GRANULARITY_TOLERANCE = 2;

	/** The name of the properties file. */
	public static final String PROPERTIES_FILENAME = "dirsyncpro.properties";
	public enum Properties{
		SystemLookAndFeel("dirsyncpro.gui.systemlookandfeel", "true"),
		LicenseAccepted("dirsyncpro.licenseaccepted", ""),
		CheckForUpdates("dirsyncpro.checkforupdates", "true"),
		ConfigPath("dirsyncpro.configpath", ""),
		LogsPath("dirsyncpro.logspath", ""),
		LogLevel("dirsyncpro.loglevel", Const.LogLevel.Moderate.toString()),
		GlobalLogEnabled("dirsyncpro.log.globallog.enabled", "false"),
		JobsetLogEnabled("dirsyncpro.log.jobsetlog.enabled", "false"),
		MinimizeToSystemTray("dirsyncpro.systemtray.minimize", "true"),
		SystemTraySingleClick("dirsyncpro.systemtray.singleclick", "false"),
		KeepAfterSync("dirsyncpro.syncq.keepaftersync", "false"),
		LoadLastConfig("dirsyncpro.loadlastconfig", "true"),
		StartScheduleEngine("dirsyncpro.startscheduleengine", "false"),
		WindowLastGeometryX("dirsyncpro.window.last.geometry.x", "0"),
		WindowLastGeometryY("dirsyncpro.window.last.geometry.y", "0"),
		WindowLastGeometryWidth("dirsyncpro.window.last.geometry.width", "0"),
		WindowLastGeometryHeight("dirsyncpro.window.last.geometry.height", "0"),
		ShutDownCommand("dirsyncpro.shutdown.command", Const.shutDownCommand),
		LastLoadedConfigs("dirsyncpro.lastloadedconfigs", "");
		
		private String prString;
		private String prDefault;
		private Properties(String prst, String dflt){
			prString = prst;
			prDefault = dflt;
		}
		public String getPrString() {
			return prString;
		}
		public String getDefault() {
			return prDefault;
		}
	}
	
	public static final int DefaultRealtimeSyncDelayValue = 60;
	public static final String DefaultDateFormat = "dd-MM-yyyy HH:mm";
	public static final String DefaultDateShortFormat = "dd-MM-yy HH:mm";
	public static final String DefaultDateLongFormat = "dd-MM-yyyy HH:mm:ss";
	public static final String DefaultTimeFormat = "HH:mm";
	public static final String DefaultLogDateFormat = "yyyy-MM-dd HH:mm";
	public static final boolean isInDayLightTimeNow = TimeTool.isInDayLightTimeNow(); // for performance reason set once at startup.
	
	public enum WeekDays{Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday};
	public enum Months{January, February, March, April, May, June, July, August, September, October, November, December};
	
	public static final Date NonDate = new Date(0); 
	
	/** Dir A */
	public static final String DIR_A = "DirA";
	/** Dir B */
	public static final String DIR_B = "DirB";
	
	/** Sync Mode */
	public enum SyncMode {
		ABMirror("Mirror A -> B (incremental)", "/icons/syncModeAB16x16.png"), 
		BAMirror("Mirror B -> A (incremental)", "/icons/syncModeBA16x16.png"), 
		BIMirror("Synchronize A <-> B (incremental)", "/icons/syncModeBI16x16.png"),
		BICustom("Synchronize A <-> B (custom)", "/icons/syncModeBI16x16.png"),
		ABFull("Backup A -> B (full)", "/icons/syncModeAB16x16.png"),
		BAFull("Restore B -> A (full)", "/icons/syncModeBA16x16.png"),
		ABContribute("Contribute A -> B (incremental)", "/icons/syncModeAB16x16.png"),
		BAContribute("Contribute B -> A (incremental)", "/icons/syncModeBA16x16.png"),
		ABCustom("Synchronize A -> B (custom)", "/icons/syncModeAB16x16.png"), 
		BACustom("Synchronize B -> A (custom)", "/icons/syncModeBA16x16.png") 
		;

		private SyncMode(String name, String iconFile){
			this.name = name;
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
			this.literal = super.toString();
		}
		
		private String name;
		private String literal;
		private Icon icon;
		public String toString(){
			if (literal == null){
				literal = this + "";
			}
			return name;
		}
		
		public String toHTMLString(){
			if (literal == null){
				literal = this + "";
			}
			String name = this.name;
			name = name.replaceAll("<->", "<span style=\"font-family:Courier; font-weight:bold;\">&harr;</span>");
			name = name.replaceAll("->", "<span style=\"font-family:Courier; font-weight:bold;\">&rarr;</span>");
//			name = name.replaceAll("\\(incremental\\)", "<span style=\"color:258101\">(incremental)</span>");
//			name = name.replaceAll("\\(full\\)", "<span style=\"color:258101\">(full)</span>");
			name = name.replaceAll("\\(custom\\)", "<span style=\"color:blue\">(custom)</span>");
			name = "<html>" + name + "</html>";
			return name;
		}
		
		public String getLiteral(){
			return literal;
		}
		public Icon getIcon(){
			return icon;
		}
		public boolean isCustom(){
			return (this.equals(ABCustom) || this.equals(BACustom) || this.equals(BICustom));
		}
		public boolean isBI(){
			return (this.equals(BIMirror) || this.equals(BICustom));
		}
	};

	/** Compare Mode */
	public enum CompareMode {CompareFileSizesDates, CompareFileSizesDatesMetaData, CompareFileContents};
	public static final CompareMode COMPARE_MODE_DEFAULT = CompareMode.CompareFileSizesDates;

	
	/** Copy Mode */
	public enum CopyMode {
		All("Copy All"), 
		New("Copy New"), 
		Modified("Copy Modified"), 
		Larger("Larger"), 
		LargerAndModified("Larger and Modified"), 
		DeleteFiles("Delete unnecessary files after synchronization"), 
		DeleteDirs("Delete unnecessary dirs after synchronization"),
		ConflictFiles("Conflict files"); //
		
		private CopyMode(String name){
				this.name = name;
		}
		private String name;
		public String toString(){
			return name;
		}
	};
	
	public enum SymLinkMode {
		SkipSymLinks("Skip symbolic links"),
		CopySymLinks("Copy Symbolic links"),
		FollowSymLinks("Follow Symbolic links");
		
		private String name;
		private String literal;
		private SymLinkMode(String name){
			this.name = name;
			this.literal = super.toString();
		}
		public String toString(){
			return name;
		}
		public String getLiteral(){
			return literal;
		}
	}
	
	/** Sync pair status */
	public enum SyncPairStatus {
		FileACopyForced(true, false, true, "Forced Copy File A", "/icons/syncpairstatus_faa.png", new Color(255, 255, 255)),
		FileAIsNew(true, false, true, "File A is New", "/icons/syncpairstatus_fan.png", new Color(255, 255, 255)),
		FileBCopyForced(true, false, false, "Forced Copy File B", "/icons/syncpairstatus_fba.png", new Color(255, 255, 255)),
		FileBIsNew(true, false, false, "File B is New", "/icons/syncpairstatus_fbn.png", new Color(255, 255, 255)),
		FileAIsModified(true, false, true, "File A is Modified", "/icons/syncpairstatus_fam.png", new Color(255, 255, 255)), 
		FileBIsModified(true, false, false, "File B is Modified", "/icons/syncpairstatus_fbm.png", new Color(255, 255, 255)),
		FileAIsLarger(true, false, true, "File A is Larger", "/icons/syncpairstatus_fal.png", new Color(255, 255, 255)),
		FileBIsLarger(true, false, false, "File B is Larger", "/icons/syncpairstatus_fbl.png", new Color(255, 255, 255)),
		FileAIsLargerAndModified(true, false, true, "File A is Larger and Modified", "/icons/syncpairstatus_falm.png", new Color(255, 255, 255)),
		FileBIsLargerAndModified(true, false, false, "File B is Larger and Modified", "/icons/syncpairstatus_fblm.png", new Color(255, 255, 255)),
		FileAIsRedundant(true, false, false, "File A is Redundant", "/icons/syncpairstatus_far.png", new Color(255, 255, 255)),
		FileBIsRedundant(true, false, true, "File B is Redundant", "/icons/syncpairstatus_fbr.png", new Color(255, 255, 255)),
		FileAIsSymLink(true, false, true, "File A is Copied as SymLink", "/icons/syncpairstatus_fas.png", new Color(255, 255, 255)),
		FileBIsSymLink(true, false, false, "File B is Copied as SymLink", "/icons/syncpairstatus_fbs.png", new Color(255, 255, 255)),
		DirAIsSymLink(false, true, true, "Dir A is Copied as SymLink", "/icons/syncpairstatus_fas.png", new Color(255, 255, 255)),
		DirBIsSymLink(false, true, false, "Dir B is Copied as SymLink", "/icons/syncpairstatus_fbs.png", new Color(255, 255, 255)),
		DirACopyForced(false, true, true, "Forced Copy Dir A", "/icons/syncpairstatus_daa.png", new Color(255, 255, 255)),
		DirAIsNew(false, true, true, "Dir A is New", "/icons/syncpairstatus_dan.png", new Color(255, 255, 255)),
		DirAIsModified(false, true, true, "Dir A is Modified", "/icons/syncpairstatus_dam.png", new Color(255, 255, 255)), 
		DirBIsModified(false, true, false, "Dir B is Modified", "/icons/syncpairstatus_dbm.png", new Color(255, 255, 255)),
		DirBCopyForced(false, true, false, "Forced Copy Dir B", "/icons/syncpairstatus_dba.png", new Color(255, 255, 255)),
		DirBIsNew(false, true, false, "Dir B is New", "/icons/syncpairstatus_dbn.png", new Color(255, 255, 255)),
		DirAIsRedundant(false, true, false, "Directory A is Redundant", "/icons/syncpairstatus_dar.png", new Color(255, 255, 255)),
		DirBIsRedundant(false, true, true, "Directory B is Redundant", "/icons/syncpairstatus_dbr.png", new Color(255, 255, 255)),
		FileDirACopyForced(true, false, true, "Forced Copy File/Dir A", "/icons/syncpairstatus_faa.png", new Color(255, 255, 255)),
		FileDirBCopyForced(true, false, false, "Forced Copy File/Dir B", "/icons/syncpairstatus_fba.png", new Color(255, 255, 255)),
		FileDirAIsRedundant(true, false, true, "File/Dir A is Redundant", "/icons/syncpairstatus_far.png", new Color(255, 255, 255)),
		FileDirBIsRedundant(true, false, false, "File/Dir B is Redundant", "/icons/syncpairstatus_fbr.png", new Color(255, 255, 255)),
		ConflictResolutionRename(false, true, true, "Conflict resolution by renaming both files", "/icons/syncpairstatus_bir.png", new Color(255, 255, 255)),
		ConflictResolutionModified(false, true, true, "Conflict resolution by copying the modified file", "/icons/syncpairstatus_bir.png", new Color(255, 255, 255)),
		ConflictResolutionWarn(false, true, true, "Conflict resolution by warning the user", "/icons/syncpairstatus_biw.png", new Color(255, 255, 185)),
		ConflictResolutionCopySourceA(false, true, true, "Conflictt resolution by copying the source", "/icons/syncpairstatus_faa.png", new Color(255, 255, 185)),
		ConflictResolutionCopySourceB(false, true, true, "Conflict resolution by copying the source", "/icons/syncpairstatus_fba.png", new Color(255, 255, 185)),
		;
		private String name;
		private Icon icon;
		private Color color;
		private boolean isFileOperation;
		private boolean isDirOperation;
		private boolean ab;
		private SyncPairStatus(boolean isFO, boolean isDO, boolean isAB, String name, String iconFile, Color c){
			this.isFileOperation = isFO;
			this.isDirOperation = isDO;
			this.ab = isAB;
			this.name = name;
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
			this.color = c;
		}
		public String toString(){
			return name;
		}

		public Icon getIcon(){
			return icon;
		}

		public Color getColor(){
			return color;
		}
		
		public boolean isAB(){
			return ab;
		}

		public CopyMode matchingCopyMode(){
			if (this.equals(FileAIsNew) || this.equals(FileBIsNew) || this.equals(DirAIsNew) || this.equals(DirBIsNew)){
				return CopyMode.New;
			}
			if (this.equals(FileAIsModified) || this.equals(FileBIsModified)){
				return CopyMode.Modified;
			}
			if (this.equals(FileAIsLarger) || this.equals(FileBIsLarger)){
				return CopyMode.Larger;
			}
			if (this.equals(FileAIsLargerAndModified) || this.equals(FileBIsLargerAndModified)){
				return CopyMode.LargerAndModified;
			}
			if (this.equals(FileAIsRedundant) || this.equals(FileBIsRedundant)){
				return CopyMode.DeleteFiles;
			}
			if (this.equals(DirAIsRedundant) || this.equals(DirBIsRedundant)){
				return CopyMode.DeleteDirs;
			}
			if (this.equals(ConflictResolutionModified) || this.equals(ConflictResolutionRename) || this.equals(ConflictResolutionWarn)){
				return CopyMode.ConflictFiles;
			}
			//dummy
			return null;
		}
		
		public boolean isFileOperation(){
			return isFileOperation;
		}

		public boolean isDirOperation(){
			return isDirOperation;
		}
	};
	
	/** Default include/excludes */
	public static final String ALL_INCLUSIVE_PATTERN = "*";
	public static final String DEFAULT_INCLUDE_DIR = ALL_INCLUSIVE_PATTERN;
	public static final String DEFAULT_EXCLUDE_DIR = "";
	public static final String DEFAULT_INCLUDE_FILE = ALL_INCLUSIVE_PATTERN;
	public static final String DEFAULT_EXCLUDE_FILE = "";
	
	/** Conflict mode "Copy Modified" for the Bidirectional Synchronization*/
	public enum SyncConflictResolutionMode {CopyModified, CopyLarger, CopyLargerAndModified, CopyRenamed, WarnUser, Skip, CopySource};
	public static final SyncConflictResolutionMode BIDIR_SYNC_CONFLICT_RESOLUTION_MODE_DEFAULT = SyncConflictResolutionMode.CopyModified;
	public static final SyncConflictResolutionMode MONODIR_SYNC_CONFLICT_RESOLUTION_MODE_DEFAULT = SyncConflictResolutionMode.WarnUser;

	/** log level */
	public enum LogLevel {Minimal, Moderate, Excessive};
	
	public enum FileType{
		DSC("dsc", "DirSync Pro config file"),
		SH("sh", "bash script"),
		CMD("cmd", "MS Windows command line batch");
		
		private String extension;
		private String description;
		
		private FileType(String ext, String descr){
			this.extension = ext;
			this.description = descr;
		}

		public String getExtension() {
			return extension;
		}

		public String getDescription() {
			return description;
		}
	}
	
    /** The file extension for config files. */
	public static final String SYNC_FILE_EXTENSION = "dsc";
	public static final String LOG_FILE_EXTENSION = "log";

	/** The DirA & DirB file extension for conflicting modified/larger files in bidirectional sync. */
	public static final String DIR_A_RENAME_FILE_EXTENSION = "DirA";
	public static final String DIR_B_RENAME_FILE_EXTENSION = "DirB";

	public static final String DEFAULT_SYNC_SESSION_NAME= "DirSyncProJobset";
	public static final String DEFAULT_GLOBALLOG_FILENAME= "DirSyncPro" + "." + LOG_FILE_EXTENSION;
	public static final String DEFAULT_JOBSET_NAME= "Job";

    /** The number of milliseconds to sleep while pause. */
    public static final long PAUSE = 500;

    /** true if the operating system is MS Windows, false otherwise. */
	public static final boolean OS_IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

	public static final String shutDownCommand = (OS_IS_WINDOWS ? "shutdown.exe -s -t 0" : "shutdown -h now");
	
	/** The copyright message. */
	public static final String COPYRIGHT = "DirSync Pro is designed and developed by O. Givi.\n" +
			"DirSync Pro is based on DirSync (Directory Synchronize) by E. Gerber and F. Gerbig.\n\n" +
			"(C) 2008-2018 O. Givi\n" +
			"(C) 2002-2008 by E. Gerber, F. Gerbig and T. Groetzner.\n" +
			"Thanks to P. Kolacz, M.V. Beloshapkin, M. Lux, G. Noorlander, T. Brixel, D. Caravana, R. Williams\n" +
			"and D. Petre for the contributions";
	/** The License message */
	public static final String LICENSE = "This program comes with ABSOLUTELY NO WARRANTY; It is licensed according to GPL v3.\n" +
			"By using this program you ACCEPT the license terms.\nPlease read the 'License.txt' file accompanying this program.";

	/** Contacts */
	public static final String HOMEPAGE = "https://www.dirsyncpro.org";
	public static final String DONATEURL = "https://www.dirsyncpro.org/donate.html";
	public static final String EMAIL = "info@dirsyncpro.org";
	public static final String TWITTER = "@DirSyncPro";

	/** The update script location (URL). */
	public static final String UPDATEURL = "http://update.dirsyncpro.org/";

	/** The "about" message. */
	public static final String MESSAGE = 
	"Homepage: " +	HOMEPAGE + "\n" +
	"Contact:  " + EMAIL + "\n" +
	"Twitter:  " + TWITTER + "\n\n" +
	
	"This program is governed by the GNU License (GPL) either version 3 of the License, or any later version.\n" +
	"Most icons are from the Kommon Desktop Environment (KDE) released under the GPL. Please consult the\n" +
	"License.txt file or the Manual for information on the license. For more help on DirSync Pro please consult\n" +
	"the manual or take a look at the homepage.\n\n" +
	"If this program is good for your purpose please:\n" +
	" 1) talk about DirSync Pro on the internet (facebook, blogs, forums, ...), dig it, twit it, etc.\n" +
	" 2) consider a donation, any amount you would like, as a gesture to let us keep developing this program\n" +
    "    (please visit: " + HOMEPAGE + ").";

	/** The help message. */
	public static String HELP = MESSAGE + "\n\n" + "For help on using DirectorySynchronize Pro try '" + DirSyncPro.getOptionsMarker() + "usage'.\n";
	public static String manualFilename = "Manual.pdf";
	
	
	/** The usage message. */
	public static String USAGE = "Usage:\n'dirsyncpro [" 
	+ DirSyncPro.getOptionsMarker() 
	+ "help] [" + DirSyncPro.getOptionsMarker() 
	+ "usage] [" + DirSyncPro.getOptionsMarker()
	+ "sync] [" + DirSyncPro.getOptionsMarker()
	+ "analyze] [" + DirSyncPro.getOptionsMarker()
	+ "schedule] [" + DirSyncPro.getOptionsMarker()
	+ "iconify] [" + DirSyncPro.getOptionsMarker()
	+ "quit] ["	+ DirSyncPro.getOptionsMarker()
	+ "nogui] <configfile>'\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "help      Display help on homepage, contact, and license.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "usage     Displays this screen.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "sync      Starts a synchronization if a configuration file is specified.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "analyze   Starts a analyze rather than a synchronization if a configuration file is specified.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "schedule  Starts the schedule engine as soon as the GUI is started.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "iconify   Minimizes the Gui to the tray (if available) on start up.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "quit      Quits the program after the analyze or the synchronization.\n"
	+ "  "
	+ DirSyncPro.getOptionsMarker()
	+ "nogui     Start DirectorySynchronize Pro in console mode and start synchronization.\n\n"
	+ "Usage examples:\n" + "  * Start in GUI mode:\n"
	+ "      dirsyncpro\n" + "  * Start synchronization in GUI mode and quit afterwards:\n"
	+ "      dirsyncpro " + DirSyncPro.getOptionsMarker() + "sync " + DirSyncPro.getOptionsMarker() + "quit <configfile>\n"
	+ "  * Start synchronization in command line mode:\n"
	+ "      dirsyncpro " + DirSyncPro.getOptionsMarker() + "sync " + DirSyncPro.getOptionsMarker() + "nogui <configfile>\n";

	/** Display selection constants */
	public enum Display {ALL, COPY_NEW, COPY_MODIFIED, DELETED, ERROR};
	
	/** The default name for the backup folder. */
	public static final String BACKUP_FOLDER_NAME = ".DirSyncProBackup";
	/** The maximum number of backup versions of a single file to keep. 
	 *  Be careful to increase this above 98, because the backup filename only has a two digit version. */
	public static final int BACKUP_MAX_NUMBER = 50;
	
	public static final int OPEN_RECENT_NUMBER_OF_DOCS = 8;
	
	public enum IconKey {
		Sync("/icons/icon_sync.png", new Color(255, 255, 255)), 
		Dir("/icons/dirBlue.png", new Color(255, 255, 255)), 
		Subdir("/icons/dirViolet.png", new Color(255, 255, 255)), 
		Info("/icons/icon_info.png", new Color(255, 255, 255)), 
		Config("/icons/icon_config.png", new Color(255, 255, 255)), 
		CopyForced("/icons/copyAll.png", new Color(255, 255, 255)), 
		CopyNew("/icons/copyNew.png", new Color(255, 255, 255)), 
		CopyModified("/icons/copyModified.png", new Color(255, 255, 255)), 
		CopyLarger("/icons/copyLarger.png", new Color(255, 255, 255)), 
		CopyLargerModified("/icons/copyLargerModified.png", new Color(255, 255, 255)), 
		Deleted("/icons/deleteFile.png", new Color(255, 255, 255)),
		Warning("/icons/icon_warning.png", new Color(255, 255, 185)), 
		Error("/icons/icon_error.png", new Color(255, 185, 185)), 
		File("/icons/icon_file.png", new Color(255, 255, 255)), 
		DirA("/icons/dirGreen.png", new Color(255, 255, 255)), 
		DirB("/icons/dirOrange.png", new Color(255, 255, 255)), 
		SyncConflict("/icons/dirRed.png", new Color(255, 255, 255)),
		Time("/icons/time.png", new Color(255, 255, 255)),
		Synced("/icons/syncpairstatus_synced.png", new Color(185, 255, 185)) 
		;
		
		private Icon icon;
		private Color color;
		private IconKey(String iconFile, Color c){
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
			this.color = c;
		}
		
		public Icon getIcon(){
			return icon;
		}
		public Color getColor(){
			return color;
		}
		
		public IconKey mapForMessageQ(){
			if (this.equals(Info) || this.equals(Warning) || this.equals(Error)){
				return this;
			}else{
				return File;
			}
		}
	};
}
