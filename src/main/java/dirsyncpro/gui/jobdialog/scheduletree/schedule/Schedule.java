/*
 * Schedule.java
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
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import dirsyncpro.Const;
import dirsyncpro.job.Job;

public class Schedule implements Comparable<Schedule>{
	public enum Type{
		Once("/icons/once.png"), 
		Minutely("/icons/minutely.png"),
		Hourly("/icons/hourly.png"), 
		Daily("/icons/daily.png"), 
		Weekly("/icons/weekly.png"), 
		Monthly("/icons/monthly.png");
		
		private Icon icon;
		public Icon getIcon(){
			return icon;
		}
		private Type(String iconFile){
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
		}
	};

	protected Type type;
	protected Date timeFrameFrom = Const.NonDate;
	protected Date timeFrameTo = Const.NonDate;
	protected Date nextEvent;
	protected Date lastSynced;
	private Date modificationTime;
	protected Job job;

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @return the timeFrameFrom
	 */
	public Date getTimeFrameFrom() {
		return timeFrameFrom;
	}

	/**
	 * @param timeFrameFrom the timeFrameFrom to set
	 */
	public void setTimeFrameFrom(Date timeFrameFrom) {
		this.timeFrameFrom = timeFrameFrom;
	}
	
	/**
	 * @return the timeFrameTo
	 */
	public Date getTimeFrameTo() {
		return timeFrameTo;
	}

	/**
	 * @param timeFrameTo the timeFrameTo to set
	 */
	public void setTimeFrameTo(Date timeFrameTo) {
		this.timeFrameTo = timeFrameTo;
	}
	
	/**
	 * @return the upcoming event in the extended classes.
	 */
	public Date getNextEvent(){
		return nextEvent;
	}

	/**
	 * Calculates and sets the next upcoming event date.
	 * method to be overriden by extended classes.
	 */
	public void calculateNextEvent(){
	}

	protected boolean withinTimeFrame(Date date){
		return 
			(!hasTimeFrameFrom() || timeFrameFrom.compareTo(date) <= 0) && 
			(!hasTimeFrameTo() || timeFrameTo.compareTo(date)>=0);
	}
	
	protected void setNextEvent(Date date){
		if (date != null && withinTimeFrame(date)){
			nextEvent = date;
			modificationTime = new Date();
		}else{
			nextEvent = null;
		}
			
	}
	
	public boolean hasTimeFrameFrom(){
		return (timeFrameFrom.compareTo(Const.NonDate) != 0);
	}
	
	public boolean hasTimeFrameTo(){
		return (timeFrameTo.compareTo(Const.NonDate) != 0);
	}
	
	public String toString(){
		String str = "";
		if (timeFrameFrom.compareTo(Const.NonDate) != 0){
			str += ", from " + (new SimpleDateFormat(Const.DefaultDateFormat)).format(timeFrameFrom) + " ";
		}
		if (timeFrameTo.compareTo(Const.NonDate) != 0){
			str += ", to " + (new SimpleDateFormat(Const.DefaultDateFormat)).format(timeFrameTo);
		}
		str = str.trim();
		if (nextEvent != null){
			str += ", Next event: " + (new SimpleDateFormat(Const.DefaultDateFormat)).format(nextEvent);
		}else{
			str += " (EXPIRED)";
		}
		return str;
	}

	/**
	 * @return the modificationTime
	 */
	public Date getModificationTime() {
		return modificationTime;
	}
	
	@Override
	public int compareTo(Schedule s) {
		if (nextEvent.compareTo(s.getNextEvent()) == 0){
			if (modificationTime.compareTo(s.getModificationTime()) == 0){
				return type.compareTo(s.getType());
			}else{
				return modificationTime.compareTo(s.getModificationTime());
			}
		}else{
			return nextEvent.compareTo(s.getNextEvent());
		}
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}
	
	public void setSynced(){
		lastSynced = nextEvent;
	}
	
	protected boolean calculateNextEventAllowed(){
		return (nextEvent == null || (nextEvent == lastSynced));
	}
}
