/*
 * FilterGroup.java
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

import dirsyncpro.job.Job;

public class FilterGroup extends Filter{

	
	public FilterGroup(Job j, Action a){
		super(j, a);
		if (a == Action.Include){
			type = Filter.Type.IncludeGroup;
		}else{
			type = Filter.Type.ExcludeGroup;
		}
			
	}

	
	public String toString(){
		String s = super.toString();
		return s;
	}
}
