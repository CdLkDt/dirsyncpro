/*
 * DirSyncPro.java
 *
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2003-2006, 2008 F. Gerbig
 * Copyright (C) 2005 T. Groetzner 
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

package dirsyncpro;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import dirsyncpro.Const.LogLevel;
import dirsyncpro.gui.mainframe.MainFrame;
import dirsyncpro.sync.Sync;
import dirsyncpro.tools.FileTools;
import dirsyncpro.tools.Log;
import dirsyncpro.tools.TextFormatTool;
import dirsyncpro.updater.Updater;
//SYMLINKMODE ADDED, check if it works fine and then imlement rules to handle the job.
/**
 * The main class of DirSyncPro.
 * 
 * @author E. Gerber , F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
public class DirSyncPro {

	private static Sync sync = null;
	private static MainFrame gui = null;
	private static Properties properties;
	private static Log log;

	private static boolean option_help;
	private static boolean option_usage;
	private static boolean option_noGui;
	private static boolean option_analyze;
	private static boolean option_sync;
	private static boolean option_schedule;
	private static boolean option_iconify;
	private static boolean option_quit;

	private static String homePath = "";
	private static boolean homePathSet = false; 

	private static String[] parseOptions(String[] args) {

		option_help = false;
		option_usage = false;
		option_noGui = false;
		option_analyze = false;
		option_sync = false;
		option_iconify = false;
		option_quit = false;

		List<String> options = new ArrayList<String>(Arrays.asList(args));

		for (Iterator<String> iter = options.iterator(); iter.hasNext();) {
			// all arguments
			String option = iter.next().toLowerCase();

			if (option.startsWith(DirSyncPro.getOptionsMarker())) {
				// is option ?
				option = option.substring(1);
				iter.remove(); // remove option from arguments

				switch (option){
				case "?":
				case "h":
				case "help":
					option_help = true;
					break;
				case "usage":
					option_usage = true;
					break;
				case "nogui":
					option_noGui = true;
					break;
				case "analyze":
					option_analyze = true;
					break;
				case "sync":
					option_sync = true;
					break;
				case "schedule":
					option_schedule = true;
					break;
				case "iconify":
					option_iconify = true;
					break;
				case "quit":
					option_quit = true;
					break;
				default:
					DirSyncPro.displayError("Unknown option '" + option + "' at the command line!");
					option_usage = true;
				}
			}
		}
		return (String[]) options.toArray(new String[options.size()]);
	}

	/**
	 * The main class of DirSyncPro.
	 * 
	 * @param args
	 *            The command line argguments.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(Const.PROGRAM);
		System.out.println(Const.COPYRIGHT);
		System.out.println(Const.LICENSE);
		System.out.println();
		
		String[] argsOrg = args.clone();

		args = parseOptions(args);

		DirSyncPro.loadProperties();
		setUpLogFile(isGlobalLogEnabled());
		sync = new Sync();
				
		// help or usage?
		if (option_help) {
			DirSyncPro.displayHelp();
		} else if (option_usage) {
			DirSyncPro.displayUsage();
		}

		// both of 'analyze' or 'sync' specified
		if (option_analyze && option_sync) {
			DirSyncPro.displayError("Please specify exactly one of the arguments 'analyze' or 'sync'.");
			DirSyncPro.displayUsage();
		}

		if (isBeta()){
			
			DirSyncPro.getProperties().setProperty(Const.Properties.CheckForUpdates.getPrString(), "true");
			System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			System.out.print("This is an unstable version and is released for preview\nand/or testing purposes only. Please use with care!\n");
			System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			if (option_noGui){
				System.out.println("Checking for updates is enabled by default in this beta version!");
			}
		}
		
		if (getSync() != null){
			getSync().getLog().printExcessive("CMD arguments: " + Arrays.toString(argsOrg), Const.IconKey.Info);
			getSync().getLog().printExcessive("Runtime Max Memory: " + TextFormatTool.getHumanReadable(Runtime.getRuntime().maxMemory()), Const.IconKey.Info);
			getSync().getLog().printExcessive("JVM parameters: " + ManagementFactory.getRuntimeMXBean().getInputArguments().toString(), Const.IconKey.Info);
		}

		switch (args.length) {

		case 0: // no configuration specified => gui
			if (option_noGui) {
				System.out.println("Error: No configuration file specified.");
				DirSyncPro.displayUsage();
			} else {
				if (((ColorUIResource) UIManager.get("Table.gridColor")).getRGB() == -1){
					// Table.gridColor = white (in OS X), make it gray
					UIManager.put("Table.gridColor", new ColorUIResource(Color.gray));
				}
				
				gui = new MainFrame();
				gui.initConfig();
				
				SwingWorker<Void, Void> updateProcess = new SwingWorker<Void, Void>() {
					protected Void doInBackground() {
						getGui().getUpdateDialog().checkForUpdate(true, null);
						return null;
					}
				};
				updateProcess.execute();
				//check for updates only if no argument is specified 

				if (option_schedule || isStartScheduleEngineOnStartup()){
					gui.schedulerStart();
				}
			}
			break;

		case 1: // configuration specified
			if (option_noGui) {
				Updater u = new Updater(false);
				if (u.contacted()){
					if (u.isUpdateable()){
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
						System.out.println("New version available! Your version: " + Const.VERSION + ", Newer version: " + u.getNewVersion());
						System.out.println("Changes: " +  u.getChangelogURL());
						System.out.println("Please visit: " + u.getUpdateURL());
						System.out.print("!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
					}
				}
				System.out.println();
				if (getSync() != null){
					getSync().getLog().printExcessive("Initiated from the command line.", Const.IconKey.Info);
				}
				try {
					sync.load(args[0]);

					if (option_analyze) {
						getSync().analyze();
					} else {
						getSync().synchronize(); 
					}

				} catch (Exception e) {
					System.err.println("Error while loading configuration '" + args[0] + "'.");
				}

			} else { // gui
				gui = new MainFrame();

				try {
					File file = (new File(args[0])).getCanonicalFile();

					sync.load(args[0]);
					gui.setCurrentConfig(file);
					gui.updateJobsTree();

					if (option_iconify){
						gui.iconifyForm();
					}
					
					if (option_analyze) {
						gui.analyzeStart(); // start analyze
					}
					if (option_sync) {
						gui.synchronizationStart(); // start synchronization
					}
					if (option_schedule){
						gui.schedulerStart();
					}

				} catch (Exception e) {
					gui.setCurrentConfig(null);
					displayError("Error while loading configuration '" + args[0] + "'.");
				}
			}
			break;

		default:
			displayError("Too many arguments.");
			displayUsage();
		}
	}

	/**
	 * Returns whether the progam is in GUI mode.
	 * 
	 * @return boolean <code>true</code> if the program is in GUI mode,
	 *         <code>false</code> else.
	 */
	public static boolean isGuiMode() {
		return !((option_noGui) || (gui == null));
	}

	/** Displays the help message. */
	public static void displayHelp() {
		System.out.println(Const.HELP);
		if (isGuiMode()) {
			DirSyncPro.getGui().displayHelpDialog();
		}
		System.exit(0);
	}

	/** Displays the usage message. */
	public static void displayUsage() {
		System.out.println(Const.USAGE);
		if (isGuiMode()) {
			DirSyncPro.getGui().displayUsageDialog();
		}
		System.exit(1);
	}

	/** 
	 * Displays an error message.
	 * @param s The error message. 
	 */
	public static void displayError(String s) {
		System.out.println("ERROR: " + s + "\n");
		if (isGuiMode()) {
			DirSyncPro.getGui().displayErrorDialog(s);
		}
	}

	/** 
	 * Displays an info message.
	 * @param s The info message. 
	 */
	public static void displayInfo(String s) {
		if (getSync() != null){
			getSync().getLog().printMinimal(s, Const.IconKey.Info);
		}else{
			System.out.println("Info: " + s + "\n");
		}
		if (isGuiMode()) {
			DirSyncPro.getGui().displayInfoDialog(s);
		}
	}

	/** 
	 * Displays a warning message.
	 * @param s The warning message. 
	 */
	public static void displayWarning(String s) {
		if (getSync() != null){
			getSync().getLog().printMinimal(s, Const.IconKey.Warning);
		}else{
			System.out.println("Warning: " + s + "\n");
		}
		if (isGuiMode()) {
			DirSyncPro.getGui().displayWarningDialog(s);
		}
	}

	/**
	 * Get the GUI.
	 * @return The GUI.
	 */
	public static MainFrame getGui() {
		return gui;
	}

	/**
	 * Get the properties.
	 * @return The properties.
	 */
	private static Properties getProperties() {
		return properties;
	}

	/**
	 * Set the quit option.
	 * @param option_quit If set to <code>true</code> the program will quit after the current operation.
	 */
	public static void setOption_quit(boolean option_quit) {
		DirSyncPro.option_quit = option_quit;
	}

	/**
	 * Get the setting of the quit option.
	 * @return <code>true</code> if the option is enabled, <code>false</code> otherwise.
	 */
	public static boolean isOption_quit() {
		return option_quit;
	}

	/**
	 * Get the setting of the iconify option.
	 * @return <code>true</code> if the option is enabled, <code>false</code> otherwise.
	 */
	public static boolean isOption_iconify() {
		return option_iconify;
	}

	/**
	 * Get the setting of the analyze option.
	 * @return <code>true</code> if the option is enabled, <code>false</code> otherwise.
	 */
	public static boolean isOption_analyze() {
		return option_analyze;
	}

	/**
	 * Get the character preceding a command line option on this platform.
	 * @return The character preceding a command line option.
	 */
	public static String getOptionsMarker() {
		return(Const.OS_IS_WINDOWS ? "/" : "-");
	}

	/**
	 * Loads the properties.
	 * @throws Exception
	 */
	public static void loadProperties() throws Exception {

		// search properties at specified location or in current directory
		String hp = getHomePath();
		if (hp.equals("")){
			hp = null;
		}
		String filename = new File(hp, Const.PROPERTIES_FILENAME).getCanonicalPath();

		// init properties
		properties = new Properties();

		// if program properties exist, load them
		if (new File(filename).exists()) {
			properties.load(new FileInputStream(filename));
		}

		//set the defaults
		for (Const.Properties pr : Const.Properties.values()){
			properties.put(pr.getPrString(), properties.getProperty(pr.getPrString(), pr.getDefault()));
		}
		
		saveProperties();
	}

	/**
	 * Saves the properties. 
	 */
	private static void saveProperties() {
		try { // write properties to disk
			String path = getHomePath();
			if (path.equals("")){
				path = null;
			}
			String filename = new File(path, Const.PROPERTIES_FILENAME).getCanonicalPath();
			FileOutputStream out = new FileOutputStream(filename);
			properties.store(out, "Properties of DirSync Pro " + Const.VERSION);
			out.close();
		} catch (Exception e) {
			getLog().printThrowableStack(e);
		}
	}

	/**
	 * Set the synchronization.
	 * @param sync The synchronization.
	 */
	public static void setSync(Sync sync) {
		DirSyncPro.sync = sync;
	}

	/**
	 * Gets the synchronization.
	 * @return The synchronization.
	 */
	public static Sync getSync() {
		return sync;
	}

	/**
	 * Check if the option "no GUI" is enabled.
	 * @return Returns the option_noGui.
	 */
	public static boolean isOption_noGui() {
		return option_noGui;
	}
	
	/**
	 * Check if the option "no GUI" is enabled.
	 * @return Returns the option_noGui.
	 */
	public static boolean isBeta() {
		return Const.VERSION.matches(".*[a-zA-Z].*");
	}
	
	public static String getProgramPath(){
		
		String programPath = System.getProperty("java.class.path");
		if (programPath != null){
			File f = new File(System.getProperty("java.class.path"));
			return f.getAbsoluteFile().getParentFile().getAbsolutePath();
		}else{
			return new File("").getAbsolutePath();
		}
	}
	
	private static void initHomePath(){
		String path = System.getProperty("dirsyncpro.home", "");
		path = path.replace("\"", "");
		// TODO: log is not initialized so null pointer exception occurs. Perhaps file-less log + flushing messages when the log file is set.
		if (path.length() > 0 && FileTools.directoryIsWritable(path)){
			//log.printExcessive("'dirsyncpro.home' property is set and the corresponding directory is wriable.", Const.IconKey.Info);
			homePath = path;
			homePathSet = true;
		}else{
			if (path.length() > 0 && !FileTools.directoryIsWritable(path)){
				//log.printMinimal("'dirsyncpro.home' property is set but the corresponding directory is not wriable!", Const.IconKey.Warning);
				//log.printMinimal("Trying the program folder...", Const.IconKey.Warning);
			}

			path = getProgramPath();
			if (FileTools.directoryIsWritable(path)){
				//log.printExcessive("Path to DirSync Pro program is wriable to store the property file.", Const.IconKey.Info);
				homePath = path;
				homePathSet = true;
			}else {
				//log.printMinimal("Path to DirSync Pro program is wriable to store the property file!", Const.IconKey.Warning);
				//log.printMinimal("Trying '" + Const.dspHomeDir + "' in the user home directory ...", Const.IconKey.Warning);
				
				path = System.getProperty("user.home") + File.separator + Const.dspHomeDir;
				if (FileTools.directoryIsWritable(path)){
					//log.printExcessive("'" + Const.dspHomeDir + "' in the user home directory is wriable to store the property file.", Const.IconKey.Info);
					File f = new File(path + File.separator + Const.PROPERTIES_FILENAME);
					f = new File(path);
					if (!f.exists()){
						f.mkdirs();
					}
					homePath = path;
					homePathSet = true;
				}else {
					//log.printMinimal("'" + Const.dspHomeDir + "' in the user home directory is not wriable to store the property file!", Const.IconKey.Warning);
					//log.printMinimal("Trying the current directory ...", Const.IconKey.Warning);
					
					path = "";
					if (FileTools.directoryIsWritable(path)){
						//log.printExcessive("Current directory is wriable to store the property file.", Const.IconKey.Info);
						homePath = path;
						homePathSet = true;
					}else {
						//log.printMinimal("Current directory is not wriable to store the property file!", Const.IconKey.Warning);
						//log.printMinimal("You may set another paths in the options window.", Const.IconKey.Warning);
						displayWarning(
								"Folder regarding 'dirsyncpro.home' parameter (if defined), program folder, user home\n" +
								"directory and current directory are not writable to store the DirSync Pro property file.\n" +
								"Program options could not be saved. You may set another paths in the options window.");
					}
				}
			}
		}
	}
	
	/**
	 * get the path where dirsyncpro.properties is saved
	 * @return the path
	 */
	public static String getHomePath(){
		if (!homePathSet){
			initHomePath();
		}
		return homePath;
	}

	/**
	 * get the path to the folder where the configs are saved
	 * @param withFileSeprator boolean whether to return the path with a file seprator postfix
	 * @return the path
	 */
	public static String getConfigPath(boolean withFileSeprator){
		String path = properties.getProperty(Const.Properties.ConfigPath.getPrString(), getHomePath());
		File f = new File(path);
		f = new File(f.getAbsolutePath());
		path = f.getAbsolutePath();
		if (withFileSeprator && !path.equals("")){
			path += File.separator;
		}
		return path;
	}
	
	/**
	 * set the path to the folder where the configs are saved
	 * @param cp the path
	 */
	public static void setConfigPath(String cp){
		if (FileTools.directoryIsWritable(cp)){	
			properties.setProperty(Const.Properties.ConfigPath.getPrString(), cp);
		}else{
			properties.setProperty(Const.Properties.ConfigPath.getPrString(), getHomePath());
			displayInfo("The given config path is not writable! Using:\n" + (new File(getHomePath())).getAbsolutePath() + "\n instead to save the config files.");
		}
		saveProperties();
	}

	/**
	 * get the path to the folder where the logs are saved
	 * @param withFileSeprator boolean whether to return the path with a file seprator postfix
	 * @return the path.
	 */
	public static String getLogsPath(boolean withFileSeprator){
		String path = properties.getProperty(Const.Properties.LogsPath.getPrString(), getHomePath());
		File f = new File(path);
		f = new File(f.getAbsolutePath());
		path = f.getAbsolutePath();
		if (withFileSeprator && !path.equals("")){
			path += File.separator;
		}
		return path;
	}
	
	/**
	 * Sets the path to the folder where the logs are saved
	 * @param lp the path
	 */
	public static void setLogsPath(String lp){
		String path = lp;
		if (FileTools.directoryIsWritable(path)){
			properties.setProperty(Const.Properties.LogsPath.getPrString(), path);
		}else{
			properties.setProperty(Const.Properties.LogsPath.getPrString(), getHomePath());
			displayInfo("The given log path is not writable! Using:\n" + (new File(getHomePath())).getAbsolutePath() + "\n instead to save logs.");
			path = getHomePath();
		}
		DirSyncPro.getSync().getLog().setPath(path);
		saveProperties();
	}

	/**
	 * @return the logsInline
	 */
	public static boolean isLogsInline() {
		return properties.getProperty(Const.Properties.LogsPath.getPrString(), getHomePath()).equals("");
	}

	/**
	 * @param lil the logsInline to set
	 */
	public static void setLogsInline(boolean lil) {
		if (lil){
			setLogsPath("");
		}
	}

	/**
	 * @return the configInline
	 */
	public static boolean isConfigInline() {
		return properties.getProperty(Const.Properties.ConfigPath.getPrString(), getHomePath()).equals("");
	}

	/**
	 * @param lil the configInline to set
	 */
	public static void setConfigInline(boolean cil) {
		if (cil){
			setConfigPath("");
		}
	}

	/**
	 * 
	 * @return boolean if the license for this version is already accepted.
	 */
	public static boolean isLicenseAccepted(){
		return properties.getProperty(Const.Properties.LicenseAccepted.getPrString(), "").equalsIgnoreCase(Const.PROGRAM);
	}
	
	public static void setLicenseAccepted(){
		properties.setProperty(Const.Properties.LicenseAccepted.getPrString(), Const.PROGRAM);
		saveProperties();
	}
	
	/**
	 * 
	 * @return boolean if should check for updates automatically
	 */
	public static boolean isCheckforUpdate(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.CheckForUpdates.getPrString(), Const.Properties.CheckForUpdates.getDefault()));
	}
	
	public static void setCheckForUpdates(boolean cu){
		properties.setProperty(Const.Properties.CheckForUpdates.getPrString(), Boolean.toString(cu));
		saveProperties();
	}

	public static boolean isMinimizeToSystemTray(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.MinimizeToSystemTray.getPrString(), Const.Properties.MinimizeToSystemTray.getDefault()));
	}
	
	public static void setMinimizeToSystemTray(boolean singleclick){
		properties.setProperty(Const.Properties.MinimizeToSystemTray.getPrString(), Boolean.toString(singleclick));
		saveProperties();
	}

	public static boolean isSingleClickSystemTray(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.SystemTraySingleClick.getPrString(), Const.Properties.SystemTraySingleClick.getDefault()));
	}
	
	public static void setSingleClickSystemTray(boolean singleClick){
		properties.setProperty(Const.Properties.SystemTraySingleClick.getPrString(), Boolean.toString(singleClick));
		saveProperties();
	}

	public static boolean isKeepSyncQAfterSync(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.KeepAfterSync.getPrString(), Const.Properties.KeepAfterSync.getDefault()));
	}
	
	public static void setKeepSyncQAfterSync(boolean keepSyncQAfterSync){
		properties.setProperty(Const.Properties.KeepAfterSync.getPrString(), Boolean.toString(keepSyncQAfterSync));
		saveProperties();
	}

	public static boolean isLoadLastConfig(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.LoadLastConfig.getPrString(), Const.Properties.LoadLastConfig.getDefault()));
	}
	
	public static void setLoadLastConfig(boolean loadLastConfig){
		properties.setProperty(Const.Properties.LoadLastConfig.getPrString(), Boolean.toString(loadLastConfig));
		saveProperties();
	}

	public static File getLastLoadedConfig(){
		File[] lastloadedConfigs = getLastLoadedConfigs();
		if (lastloadedConfigs.length > 0){
			return lastloadedConfigs[0];
		}else{
			return null;
		}
	}
	
	public static void addLastLoadedConfig(File file){
		File[] files = getLastLoadedConfigs();
		ArrayList<String> filePaths = new ArrayList<String>();
		ArrayList<File> toSave = new ArrayList<File>();
		for (File f : files){
			if (!filePaths.contains(f.getPath()) && !f.getPath().equals(file.getPath())){
				filePaths.add(f.getPath());
				toSave.add(f);
			}
		}
		if (toSave.size() == Const.OPEN_RECENT_NUMBER_OF_DOCS){
			toSave.remove(toSave.size()-1);
		}
		toSave.add(0, file);
		setLastLoadedConfigs((File[]) toSave.toArray(new File[toSave.size()]));
	}

	public static File[] getLastLoadedConfigs(){
		String[] configs = properties.getProperty(Const.Properties.LastLoadedConfigs.getPrString(), "").split(", ", Const.OPEN_RECENT_NUMBER_OF_DOCS);
		ArrayList<File> configFiles = new ArrayList<File>();
		for (String s : configs){
			s = s.replaceAll("\"", "");
			File f = new File(s);
			if (f.exists() && f.isFile()){
				configFiles.add(f);
			}
		}
		return (File[]) configFiles.toArray(new File[configFiles.size()]);
	}
	
	private static void setLastLoadedConfigs(File[] configFiles){
		String s = "";
		for (File f : configFiles){
			if (f.exists() && f.isFile()){
				s += ", \"" + f.getPath() + "\"";
			}
		}
		s = s.replaceAll("^, ", "");
		properties.setProperty(Const.Properties.LastLoadedConfigs.getPrString(), s);
		saveProperties();
	}

	public static boolean isSystemLookAndFeel(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.SystemLookAndFeel.getPrString(), Const.Properties.SystemLookAndFeel.getDefault()));
	}
	
	public static void setSystemLookAndFeel(boolean slf){
		properties.setProperty(Const.Properties.SystemLookAndFeel.getPrString(), Boolean.toString(slf));
		saveProperties();
	}

	public static LogLevel getLogLevel(){
		return Enum.valueOf(LogLevel.class, (properties.getProperty(Const.Properties.LogLevel.getPrString(), Const.Properties.LogLevel.getDefault())));
	}
	
	public static void setLogLevel(LogLevel ll){
		properties.setProperty(Const.Properties.LogLevel.getPrString(), ll.toString());
		getSync().setLogLevel(ll);
		saveProperties();
	}

	public static boolean isGlobalLogEnabled(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.GlobalLogEnabled.getPrString(), Const.Properties.GlobalLogEnabled.getDefault()));
	}
	
	public static void setGlobalLogEnabled(boolean enabled){
		properties.setProperty(Const.Properties.GlobalLogEnabled.getPrString(), Boolean.toString(enabled));
		saveProperties();
	}

	public static boolean isJobsetLogEnabled(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.JobsetLogEnabled.getPrString(), Const.Properties.JobsetLogEnabled.getDefault()));
	}
	
	public static void setJobsetLogEnabled(boolean enabled){
		properties.setProperty(Const.Properties.JobsetLogEnabled.getPrString(), Boolean.toString(enabled));
		saveProperties();
	}

	public static boolean isStartScheduleEngineOnStartup(){
		return Boolean.valueOf(properties.getProperty(Const.Properties.StartScheduleEngine.getPrString(), Const.Properties.StartScheduleEngine.getDefault()));
	}
	
	public static void setStartScheduleEngineOnStartup(boolean enabled){
		properties.setProperty(Const.Properties.StartScheduleEngine.getPrString(), Boolean.toString(enabled));
		saveProperties();
	}
	
	public static void setWindowLastGeometries(int x, int y, int width, int height){
		properties.setProperty(Const.Properties.WindowLastGeometryX.getPrString(), x + "");
		properties.setProperty(Const.Properties.WindowLastGeometryY.getPrString(), y + "");
		properties.setProperty(Const.Properties.WindowLastGeometryWidth.getPrString(), width + "");
		properties.setProperty(Const.Properties.WindowLastGeometryHeight.getPrString(), height + "");
		saveProperties();
	}
	
	public static int getWindowLastGeomteryX(){
		return Integer.valueOf((properties.getProperty(Const.Properties.WindowLastGeometryX.getPrString(), Const.Properties.WindowLastGeometryX.getDefault())));
	}
	
	public static int getWindowLastGeomteryY(){
		return Integer.valueOf((properties.getProperty(Const.Properties.WindowLastGeometryY.getPrString(), Const.Properties.WindowLastGeometryY.getDefault())));
	}

	public static int getWindowLastGeomteryWidth(){
		return Integer.valueOf((properties.getProperty(Const.Properties.WindowLastGeometryWidth.getPrString(), Const.Properties.WindowLastGeometryWidth.getDefault())));
	}

	public static int getWindowLastGeomteryHeight(){
		return Integer.valueOf((properties.getProperty(Const.Properties.WindowLastGeometryHeight.getPrString(), Const.Properties.WindowLastGeometryHeight.getDefault())));
	}

	public static String getShutDownCommand(){
		return properties.getProperty(Const.Properties.ShutDownCommand.getPrString(), Const.Properties.ShutDownCommand.getDefault());
	}
	
	public static void setShutDownCommand(String sdc){
		properties.setProperty(Const.Properties.ShutDownCommand.getPrString(), sdc);
		saveProperties();
	}

	public static Log getLog() {
		return log;
	}
	
	public static void setUpLogFile(boolean withFile){
		if (log == null){
			log = new Log(withFile ? Const.DEFAULT_GLOBALLOG_FILENAME : "", null);
		}else{
			log.setFile(withFile ? Const.DEFAULT_GLOBALLOG_FILENAME : "");
		}
	}
	
	public static void shutDownSystem(){
		try{
			Runtime.getRuntime().exec(getShutDownCommand()); 
			System.exit(0);
		}catch(IOException e){
			System.err.println("Could not shut down the system");
			getLog().printThrowableStack(e);
		}
	}
}
