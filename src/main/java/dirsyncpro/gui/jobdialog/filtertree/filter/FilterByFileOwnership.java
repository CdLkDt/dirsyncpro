/*
 * FilterByFileOwnership.java
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;

import dirsyncpro.job.Job;

public class FilterByFileOwnership extends Filter{

	private String ownerStr = "";
	private String groupStr = "";
	
	public FilterByFileOwnership(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByFileOwnership;
	}
	
	public FilterByFileOwnership(Job j, Action a, String owner, String group){
		this(j, a);
		ownerStr = owner;
		groupStr = group;
	}
	
	public boolean matches(Path path){
		PosixFileAttributes posixFileAttributes = null;
		
		try{
			posixFileAttributes = Files.readAttributes(path, PosixFileAttributes.class);
		}catch (IOException ioE){
			//TODO
		}
		
		return (
				  (!ownerStr.equals("")  && posixFileAttributes.owner().getName().equals(ownerStr)) &&
				  (!groupStr.equals("")  && posixFileAttributes.group().getName().equals(groupStr))
			  );
	}

	public String getGroupStr() {
		return groupStr;
	}

	public void setGroupStr(String groupStr) {
		this.groupStr = groupStr;
	}

	public String getOwnerStr() {
		return ownerStr;
	}

	public void setOwnerStr(String ownerStr) {
		this.ownerStr = ownerStr;
	}

	public String toString(){
		String s = super.toString();
		s += " Files/Dirs with the ownership: ";
		if (!this.ownerStr.equals("")){
			s += " owner='" + this.ownerStr + "'";
		}
		if (!this.groupStr.equals("")){
			s += " group='" + this.groupStr + "'";
		}
		return s;
	}
	@Override
	public int compareTo(Filter s) {
		// moet worden aangepast nadat radio owner/group in the gui is aangebracht.
		return super.compareTo(s);
	}
}
