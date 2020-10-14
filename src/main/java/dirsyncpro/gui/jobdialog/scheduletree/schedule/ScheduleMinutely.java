/*
 * ScheduleMinutely.java
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

import java.util.Calendar;
import java.util.Date;

import dirsyncpro.job.Job;
import dirsyncpro.tools.DateTool;

public class ScheduleMinutely extends Schedule{
	private int interval = -1;
	
	public ScheduleMinutely(Job j){
		type = Schedule.Type.Minutely;
		job = j;
	}
	
	/**
	 * @return the interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * @param value the interval to set
	 */
	public void setInterval(int value) {
		this.interval = value;
		nextEvent = null;
		calculateNextEvent();
	}
	
	/**
	 * Calculates and sets the next upcoming event date.
	 */
	public void calculateNextEvent(){
		if (interval > 0 && calculateNextEventAllowed()){
			Date candidNextEvent = null;
			if (nextEvent == null){
				candidNextEvent = new Date();
				if (hasTimeFrameFrom() && candidNextEvent.compareTo(timeFrameFrom)<0){
					candidNextEvent = timeFrameFrom;
				}
			}else{
				candidNextEvent = DateTool.nextDate(nextEvent, Calendar.MINUTE, interval);
			}
			setNextEvent(candidNextEvent);
		}
	}


	/**
	 * returns a string presenting this schedule
	 */
	public String toString(){
		String str = "Runs every " + interval + " minute" + (interval > 1 ? "s" : "");
		str = str.trim();
		str += super.toString();
		str = str.trim();
		return  str;
	}
}
