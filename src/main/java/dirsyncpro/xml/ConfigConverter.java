package dirsyncpro.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.tools.FileTools;

public class ConfigConverter {

	enum Deprecated {
		D01("<!DOCTYPE dirsync ", "<!DOCTYPE dirsyncpro "),
		D02("<!ELEMENT dirsync ", "<!ELEMENT dirsyncpro "),
		D03("<!ATTLIST dirsync ", "<!ATTLIST dirsyncpro "),
		D04("<dirsync ", "<dirsyncpro "),
		D05("dirsync>", "dirsyncpro>"),

		// tags directory -> job , as of v. 1.4
		D06("<!ELEMENT directory ", "<!ELEMENT job "),
		D07("<!ATTLIST directory ", "<!ATTLIST job "),
		D08("<directory ", "<job "),
		D09("directory>", "job>"),
		
		//bi dir sync mode , as of v. 1.3
		D10("bidirsyncconflictmode=\"COPY_MODIFIED\"", "bidirsyncconflictmode=\"CopyModified\""),
		D11("bidirsyncconflictmode=\"COPY_LARGER\"", "bidirsyncconflictmode=\"CopyLarger\""),
		D12("bidirsyncconflictmode=\"COPY_LARGER_MODIFIED\"", "bidirsyncconflictmode=\"CopyLargerAndModified\""),
		D13("bidirsyncconflictmode=\"COPY_RENAMED\"", "bidirsyncconflictmode=\"CopyRenamed\""),
		D14("bidirsyncconflictmode=\"WARN_USER\"", "bidirsyncconflictmode=\"WarnUser\""),
		D15("bidirsyncconflictmode=\"RenameCopy\"", "bidirsyncconflictmode=\"CopyRenamed\""),
		D16("SyncMode=\"A to B\"", "SyncMode=\"" + Const.SyncMode.ABCustom.getLiteral() + "\""),
		D17("SyncMode=\"B to A\"", "SyncMode=\"" + Const.SyncMode.BACustom.getLiteral() + "\""),
		D18("SyncMode=\"AB\"", "SyncMode=\"" + Const.SyncMode.ABCustom.getLiteral() + "\""),
		D19("SyncMode=\"BA\"", "SyncMode=\"" + Const.SyncMode.BACustom.getLiteral() + "\""),
		D20("SyncMode=\"Bidirectional\"", "SyncMode=\"" + Const.SyncMode.BIMirror.getLiteral() + "\""),
		D21("SyncMode=\"Bidirectional\"", "SyncMode=\"" + Const.SyncMode.BIMirror.getLiteral() + "\""),
		
		//delexcludedfiles -> delExcludedFilesB & delexcludeddirs -> delExcludeddirsB
		D22(" delexcludedfiles \\(true\\|false\\) ", " delExcludedFilesB \\(true\\|false\\) "),
		D23(" delexcludeddirs \\(true\\|false\\) ", " delExcludedDirsB \\(true\\|false\\) "),
		D24(" delexcludedfiles=\"", " delExcludedFilesB=\""),
		D25(" delexcludeddirs=\"", " delExcludedDirsB=\""),
		
		//sync mode & sync conflict as of 1.41
		D26("SyncMode", "syncMode"),
		D27("syncMode=\"ABIncremental\"", "syncMode=\"" + Const.SyncMode.ABMirror.getLiteral() + "\""),
		D28("syncMode=\"BAIncremental\"", "syncMode=\"" + Const.SyncMode.BAMirror.getLiteral() + "\""),
		D29("bidirsyncconflictmode", "syncConflictResolutionMode"),
		D30("timestampdiff", "granularity"),
		D31("timestampwriteback", "timestampWriteBack"),
		D32("backupinline", "backupInline"),
		D33("backupdir", "backupDir"),
		D34("withsubfolders", "recursive"),
		D35("job include ", "job includeFiles "),
		D36(" include=\"", " includeFiles=\""),
		D37("job exclude ", "job excludeFiles "),
		D38(" exclude=\"", " excludeFiles=\""),
		D39("dirinclude", "includeDirs"),
		D40("direxclude", "excludeDirs"),
		D41("FileSizeSmallerInclude", "includeFileSizeSmallerThan"),
		D42("FileSizeExactlyInclude", "includeFileSizeExactly"),
		D43("FileSizeLargerInclude", "includeFileSizeLargerThan"),
		D44("FileSizeSmallerExclude", "excludeFileSizeSmallerThan"),
		D45("FileSizeExactlyExclude", "excludeFileSizeExactly"),
		D46("FileSizeLargerExclude", "excludeFileSizeLargerThan"),
		D47("copynew", "copyNew"),
		D48("copymodified", "copyModified"),
		D49("copylarger", "copyLarger"),
		D50("copylargermodified", "copyLargerAndModified"),
		D51("copyall", "copyAll"),
		D52("delfiles", "delFiles"),
		D53("deldirs", "delDirs"),
		
		//filters, as of 1.45
		D54(" includeFiles=\"", "__POSTCONVERT_FILTERS__"), //does it all for all old filters
		
		//due to custom bi mode as of 1.46
		D55("syncMode=\"BI\"", "syncMode=\"" + Const.SyncMode.BIMirror.getLiteral() + "\""),

		;

		private Deprecated(String from, String to){
			this.from = from;
			this.to = to;
			this.literal = super.toString();
		}
		
		private String from;
		private String literal;
		private String to;
		public String getFrom(){
			return from;
		}
		public String getTo(){
			return to;
		}
	};

	// no instance allowd
	private ConfigConverter(){
	}
	
	public static boolean convertable(String filename){
		boolean convertable = false;
		String input = FileTools.readFile(filename);
		for (Deprecated dp : Deprecated.values()){
			if (Pattern.compile(".*" + dp.getFrom() + ".*", Pattern.MULTILINE | Pattern.DOTALL).matcher(input).matches()){
				DirSyncPro.getLog().printExcessive("Old pattern in the config file: " + dp.getFrom(), Const.IconKey.Warning);
				convertable = true;
			}
		}
		return convertable;
	}
	
	public static void convert(String filename){
		String input = FileTools.readFile(filename);
		FileTools.writeFile(filename + ".bak", input);
		for (Deprecated dp : Deprecated.values()){
			if (dp.getTo().equals("__POSTCONVERT_FILTERS__")){
				input = convertFilters(input);
			}else{
				input = input.replaceAll(dp.getFrom(), dp.getTo());
			}
		}
		FileTools.writeFile(filename, input);
	}
	
	private static String convertFilters(String in){
		String[] lines = in.split("\n");
		String output = "";
		int i;
		for (i = 0; i < lines.length; i++){
			String line = lines[i];
			output += line + "\n";
			if (line.startsWith("<dirsyncpro ")){
				break;
			}
		}
		i++;
		
		for (int j = i; j < lines.length; j++){
			String line = lines[j];
			if (line.startsWith("  <job ")){
				output += doConvertFilters(line);
			}else{
				output += line + "\n";
			}
		}
		return output;
	}

		
	private static String doConvertFilters(String jobLine){
		Matcher m;
		String output = jobLine;
		
		for (String action : new String[]{"include", "exclude"}){
			for (String fileDir : new String[]{"File", "Dir"}){
				m = Pattern.compile("(.+)( " + action + "" + fileDir + "s=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
				if (m.find()){
					if (m.group(3).length() > 0){
						output = m.group(1) + m.group(4);
						String[] patterns = m.group(3).split(";");
						for (String pattern : patterns){
							output +=  "   <filter type=\"ByPattern\" action=\"" + Character.toUpperCase(action.charAt(0)) + action.substring(1) + "\" patternType=\"" + (fileDir.equals("File") ? "File" : "Directory") + "\" pattern=\"" + pattern + "\"/>\n";
						}
					}else{
						output = m.group(1) + m.group(4);
					}
				}
			}

			for (String fileSizeType : new String[]{"SmallerThan", "Exactly", "LargerThan"}){
				m = Pattern.compile("(.+)( " + action + "FileSize" + fileSizeType + "=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
				if (m.find()){
					if (!m.group(3).equals("-1") ){
						output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"" + Character.toUpperCase(action.charAt(0)) + action.substring(1) + "\" fileSizeType=\"" + fileSizeType + "\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
					}else{
						output = m.group(1) + m.group(4);
					}
				}
			}
		}
		
//		m = Pattern.compile("(.+)( excludeFiles=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (m.group(3).length() > 0){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByPattern\" action=\"Exclude\" patternType=\"File\" pattern=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( includeDirs=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (m.group(3).length() > 0){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByPattern\" action=\"Include\" patternType=\"Directory\" pattern=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( excludeDirs=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (m.group(3).length() > 0){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByPattern\" action=\"Exclude\" patternType=\"Directory\" pattern=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
		
//		m = Pattern.compile("(.+)( includeFileSizeSmallerThan=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Include\" fileSizeType=\"SmallerThan\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( includeFileSizeExactly=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Include\" fileSizeType=\"Exactly\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( includeFileSizeLargerThan=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Include\" fileSizeType=\"LargerThan\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( excludeFileSizeSmallerThan=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Exclude\" fileSizeType=\"SmallerThan\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( excludeFileSizeExactly=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Exclude\" fileSizeType=\"Exactly\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
//		m = Pattern.compile("(.+)( excludeFileSizeLargerThan=\")(.*?)\"(.+)", Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
//		if (m.find()){
//			if (!m.group(3).equals("-1") ){
//				output = m.group(1) + m.group(4) + "   <filter type=\"ByFileSize\" action=\"Exclude\" fileSizeType=\"LargerThan\" fileSizeValue=\"" + m.group(3) + "\"/>\n";;
//			}else{
//				output = m.group(1) + m.group(4) + m.group(5);
//			}
//		}
		return output;
	}
}
