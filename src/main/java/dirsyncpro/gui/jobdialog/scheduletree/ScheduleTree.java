/*
 * ScheduleTree.java
 *
 * Copyright (C) 2010-2011 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.gui.jobdialog.scheduletree;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import dirsyncpro.Const;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;

public class ScheduleTree extends DefaultMutableTreeNode{
	public Schedule schedule;
	public ScheduleTree parent;
	
	public ScheduleTree(Schedule s, ScheduleTree p){
		schedule = s;
		setUserObject(s);
		parent = p;
		if (p != null){
			p.add(this);
		}
	}
	
	public boolean hasValues(){
		return true;
	}
	
	public String toString(){
		if (parent == null){
			return "Schedules";
		}else{
			return "<html><b>" + schedule.getType().toString() + ":</b> " + schedule.toString() + "</html>";
		}
	}

	public Icon getRootIcon(){
		return new ImageIcon(Const.class.getResource("/icons/calendar.png"));
	}

}