/*
 * ScheduleOnce.java
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

package dirsyncpro.gui.jobdialog.scheduletree.schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dirsyncpro.Const;
import dirsyncpro.job.Job;
import dirsyncpro.tools.DateTool;

public class ScheduleOnce extends Schedule{
	private Date date;

	public ScheduleOnce(Job j){
		type = Schedule.Type.Once;
		job = j;
	}

	
	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
		nextEvent = null;
		calculateNextEvent();
	}

	/**
	 * Calculates and sets the next upcoming event date.
	 */
	public void calculateNextEvent(){
		if (date != null && calculateNextEventAllowed()){
			Date candidNextEvent = null;
			if (date.compareTo(DateTool.add(new Date(), Calendar.DAY_OF_MONTH, -1)) >= 0){
				candidNextEvent = date;
			}
			setNextEvent(candidNextEvent);
		}
	}
	
	/**
	 * returns a string presenting this schedule
	 */
	public String toString(){
		String str = "Runs once on " + (new SimpleDateFormat(Const.DefaultDateFormat)).format(date);
		str = str.trim();
		str += super.toString();
		str = str.trim();
		return  str;
	}

}
