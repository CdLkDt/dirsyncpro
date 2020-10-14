/*
 * FilterByPattern.java
 * 
 * Copyright (C) 2017 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.gui.jobdialog.filtertree.filter;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

import dirsyncpro.Const;
import dirsyncpro.job.Job;

public class FilterByPattern extends Filter{

	public enum FilterPatternType{File, Directory};

	private String pattern = "";
	private FilterPatternType patternType;
	private boolean regExp;
	
	public FilterByPattern(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByPattern;
	}
	
	public FilterByPattern (Job j, Action a, String pat, FilterPatternType fpt, boolean regExp){
		this(j, a);
		this.regExp = regExp;
		patternType = fpt;
		pattern = pat;
		if (!regExp){
			pattern = pattern.replace("\\", "");
		}
	}
	
	public boolean matches(Path path){
		File file = path.toFile();
		String fileName = file.getName();
		return (
				  (
					(file.isFile() && patternType == FilterPatternType.File) || 
					(file.isDirectory() && patternType == FilterPatternType.Directory)
				  ) && matchDependingOnOS(fileName) 
				);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	/**
	 * Compares a String to the 'pattern' depending on the operating system
	 * (If the OS is MS Windows the comparison i done case insensitive).
	 * This comparison is used to match file and directory patterns to files and directories.
	 * 
	 * @param a The string to compare.
	 * 
	 * @return <code>true</code> if the strings match, <code>false</code> otherwise.
	 */
	private boolean matchDependingOnOS(String a) {
		String patternToMatch = pattern;
		String stringToMatch = a;

		if (!regExp){
			patternToMatch = this.getRegEx();
		}
		if (Const.OS_IS_WINDOWS) {
		    return Pattern.compile(patternToMatch, Pattern.CASE_INSENSITIVE).matcher(stringToMatch).matches();
		}else{
		    return Pattern.compile(patternToMatch).matcher(stringToMatch).matches();
		}
	}

	public FilterPatternType getPatternType() {
		return patternType;
	}

	public void setPatternType(FilterPatternType type) {
		this.patternType = type;
	}
	
	public String toString(){
		String s = "";
		if (this.patternType == FilterPatternType.File){
			s += " Files";
		}else{
			s += " Dirs";
		}
		s += " matching pattern: '" + this.pattern + "'" + (regExp ? " (reg exp)" : "");
		return s;
	}
	
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByPattern){
			if (patternType == ((FilterByPattern) s).getPatternType()){
				return pattern.compareTo(((FilterByPattern) s).getPattern());
			}else{
				return patternType.compareTo(((FilterByPattern) s).getPatternType());
			}
		}else{
			return super.compareTo(s);
		}
	}
	
	private String getRegEx() {
		String regExpPattern = pattern;

		// all regular expression meta chars
		String[] metaChars = {"\\", "(", ")", "{", "}", "^", "$", "|", ".", "+"};

		try {
			// escape meta chars
			for (String metachar : metaChars){
				regExpPattern = regExpPattern.replace(metachar, "\\" + metachar);
			}

			// convert repetition
			regExpPattern = regExpPattern.replace("?", ".?");
			regExpPattern = regExpPattern.replace("*", ".*");

		} catch (StringIndexOutOfBoundsException e) {
		}

		return regExpPattern;
	}

	public boolean isRegExp() {
		return regExp;
	}

	public void setRegExp(boolean regExp) {
		this.regExp = regExp;
	}
}
