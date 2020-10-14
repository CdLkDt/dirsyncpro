/*
 * ScheduleWeekly.java
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
import java.util.HashMap;

import dirsyncpro.Const;
import dirsyncpro.Const.WeekDays;
import dirsyncpro.job.Job;
import dirsyncpro.tools.DateTool;

public class ScheduleWeekly extends Schedule{
	private int interval = -1;
	private Date time;

	private HashMap<WeekDays, Boolean> days;
	
	public ScheduleWeekly(Job j){
		type = Schedule.Type.Weekly;
		job = j;
		days = new HashMap<WeekDays, Boolean>();
		days.put(WeekDays.Sunday, false);
		days.put(WeekDays.Monday, false);
		days.put(WeekDays.Tuesday, false);
		days.put(WeekDays.Wednesday, false);
		days.put(WeekDays.Thursday, false);
		days.put(WeekDays.Friday, false);
		days.put(WeekDays.Saturday, false);
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
	
	private boolean isDay(WeekDays day){
		return days.get(day);
	}
	
	/**
	 * @return the sunday
	 */
	public boolean isSunday() {
		return isDay(WeekDays.Sunday);
	}
	/**
	 * @param sunday the sunday to set
	 */
	public void setSunday(boolean sunday) {
		days.put(WeekDays.Sunday, sunday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the monday
	 */
	public boolean isMonday() {
		return isDay(WeekDays.Monday);
	}
	/**
	 * @param monday the monday to set
	 */
	public void setMonday(boolean monday) {
		days.put(WeekDays.Monday, monday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the tuesday
	 */
	public boolean isTuesday() {
		return isDay(WeekDays.Tuesday);
	}
	/**
	 * @param tuesday the tuesday to set
	 */
	public void setTuesday(boolean tuesday) {
		days.put(WeekDays.Tuesday, tuesday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the wednesday
	 */
	public boolean isWednesday() {
		return isDay(WeekDays.Wednesday);
	}
	/**
	 * @param wednesday the wednesday to set
	 */
	public void setWednesday(boolean wednesday) {
		days.put(WeekDays.Wednesday, wednesday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the thursday
	 */
	public boolean isThursday() {
		return isDay(WeekDays.Thursday);
	}
	/**
	 * @param thursday the thursday to set
	 */
	public void setThursday(boolean thursday) {
		days.put(WeekDays.Thursday, thursday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the friday
	 */
	public boolean isFriday() {
		return isDay(WeekDays.Friday);
	}
	/**
	 * @param friday the friday to set
	 */
	public void setFriday(boolean friday) {
		days.put(WeekDays.Friday, friday);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the saturday
	 */
	public boolean isSaturday() {
		return isDay(WeekDays.Saturday);
	}
	/**
	 * @param saturday the saturday to set
	 */
	public void setSaturday(boolean saturday) {
		days.put(WeekDays.Saturday, saturday);
		nextEvent = null;
		calculateNextEvent();
	}

	/**
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(Date time) {
		this.time = time;
		nextEvent = null;
		calculateNextEvent();
	}
	

	/**
	 * Calculates and sets the next upcoming event date.
	 */
	public void calculateNextEvent(){
		if (!isSaturday() && !isMonday() && !isTuesday() && !isWednesday() && !isThursday() && !isFriday() && !isSunday()){
			// do nothing
		}else{
			if (time != null && interval > 0 && calculateNextEventAllowed()){
				Date candidNextEvent = null;
				if (nextEvent == null){
					candidNextEvent = DateTool.getNextDayAtThisTime(time);
					if (hasTimeFrameFrom() && candidNextEvent.compareTo(timeFrameFrom)<0){
						candidNextEvent = timeFrameFrom;
					}
				}else{
					// we already had a nextEvent, move to the next day
					candidNextEvent = DateTool.nextDate(nextEvent, Calendar.DAY_OF_MONTH, 1);
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(candidNextEvent);
				
				if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
					if (withinTimeFrame(cal.getTime())){
						cal.set(Calendar.DAY_OF_WEEK, 1);
						cal.add(Calendar.WEEK_OF_YEAR, interval);
					}else{
						setNextEvent(null);
						return;
					}
				}
				while (!matches(cal.getTime())){
					cal.add(Calendar.DAY_OF_WEEK, 1);
					
				}
				candidNextEvent = cal.getTime();
				setNextEvent(candidNextEvent);
			}
		}
	}

	private boolean matches(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && isSunday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && isMonday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY && isTuesday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY && isWednesday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY && isThursday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && isFriday()) ||
		(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && isSaturday());
	}
	
	public String toString(){
		String str = "";
		if (isMonday())
			str += ", Monday";
		if (isTuesday())
			str += ", Tuesday";
		if (isWednesday())
			str += ", Wednesday";
		if (isThursday())
			str += ", Thursday";
		if (isFriday())
			str += ", Friday";
		if (isSaturday())
			str += ", Saturday";
		if (isSunday())
			str += ", Sunday";

		if (str.startsWith(",")){
			str = " on" + str.substring(1);
		}

		str += " ";
		
		str = "Runs every " + interval + " week" + (interval > 1 ? "s" : "") + " at " + (new SimpleDateFormat(Const.DefaultTimeFormat)).format(time) + str;
		str = str.trim();
		str += super.toString();
		str = str.trim();
		return str; 
	}
}
