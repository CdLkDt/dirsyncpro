/*
 * FilterByPath.java
 * 
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org)
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

import java.nio.file.Path;
import java.nio.file.Paths;

import dirsyncpro.Const;
import dirsyncpro.job.Job;

public class FilterByPath extends Filter{

	private String pathStr = "";
	
	public FilterByPath(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByPath;
	}

	public FilterByPath(Job j, Action a, String path){
		this(j, a);
		pathStr = path;
	}
	
	public boolean matches(Path path){
		return (path.compareTo(Paths.get(pathStr)) == 0);
	}

	/**
	 * Compares two Strings depending on the operating system
	 * (If the OS is MS Windows the comparison i done case insensitive).
	 * This comparison is used to match file and directory patterns to files and directories.
	 * 
	 * @param a The first string.
	 * @param b The second string.
	 * 
	 * @return <code>true</code> if the strings match, <code>false</code> otherwise.
	 */
	private boolean matchDependingOnOS(String a, String b) { 
		if (Const.OS_IS_WINDOWS) { 
			return a.toLowerCase().matches(b.toLowerCase());
		} else {
			return a.matches(b);
		}
	}

	public String getPathStr() {
		return pathStr;
	}

	public void setPathStr(String pathStr) {
		this.pathStr = pathStr;
	}
	
	public String toString(){
		String s = "";
		s += " Path: '" + this.pathStr + "'";
		return s;
	}
	
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByPath){
			return pathStr.compareTo(((FilterByPath) s).getPathStr());
		}else{
			return super.compareTo(s);
		}
	}
}
