/*
 * Filter.java
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

import javax.swing.Icon;
import javax.swing.ImageIcon;

import dirsyncpro.Const;
import dirsyncpro.job.Job;

public class Filter implements Comparable<Filter>, Cloneable{
	public enum Type{
		ExcludeGroup("/icons/excludeFile.png"),
		IncludeGroup("/icons/includeFile.png"),
		ByPattern("/icons/pattern.png"),
		ByFileSize("/icons/smaller.png"),
		ByDate("/icons/calendar.png"),
		ByPath("/icons/dirBlue.png"),
		ByDOSAttributes("/icons/file.png"),
		ByFileOwnership("/icons/group.png"),
		ByFilePermissions("/icons/permissions.png");
		
		private Icon icon;
		public Icon getIcon(){
			return icon;
		}
		private Type(String iconFile){
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
		}
	};
	
	public enum Action{
		Include("/icons/includeFile.png"),
		Exclude("/icons/excludeFile.png");

		private Icon icon;
		public Icon getIcon(){
			return icon;
		}
		private Action(String iconFile){
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
		}
	}

	protected Type type;
	protected Action action;
	protected Job job;

	public Filter(Job j, Action a){
		job = j;
		action = a;
	}
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	
	public String toString(){
		String str = "Filter: ";
		str += this.action.toString();
		return str;
	}

	@Override
	public int compareTo(Filter s) {
		return this.type.compareTo(s.type);
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}


	public Action getAction() {
		return action;
	}


	public void setAction(Action action) {
		this.action = action;
	}
	
	public Object clone(){
		try{
			return super.clone();
		}catch (CloneNotSupportedException e){
			// will never happen
			return null; //dummy
		}
	}
	
	//dummy to override
	public boolean matches(Path path){
		return true;
	}
}
