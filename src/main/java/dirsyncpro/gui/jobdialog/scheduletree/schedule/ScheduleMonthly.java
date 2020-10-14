/*
 * ScheduleMonthly.java
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
import dirsyncpro.Const.Months;
import dirsyncpro.job.Job;
import dirsyncpro.tools.DateTool;

public class ScheduleMonthly extends Schedule{
	private int day;
	private Date time;

	private HashMap<Const.Months, Boolean> months;
	
	public ScheduleMonthly(Job j){
		type = Schedule.Type.Monthly;
		job = j;
		months = new HashMap<Months, Boolean>();
		months.put(Months.January, false);
		months.put(Months.February, false);
		months.put(Months.March, false);
		months.put(Months.April, false);
		months.put(Months.May, false);
		months.put(Months.June, false);
		months.put(Months.July, false);
		months.put(Months.August, false);
		months.put(Months.September, false);
		months.put(Months.October, false);
		months.put(Months.November, false);
		months.put(Months.December, false);
	}

	private boolean isMonth(Months month){
		return months.get(month);
	}

	
	/**
	 * @return the january
	 */
	public boolean isJanuary() {
		return isMonth(Months.January);
	}
	/**
	 * @param january the january to set
	 */
	public void setJanuary(boolean january) {
		months.put(Months.January, january);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the february
	 */
	public boolean isFebruary() {
		return isMonth(Months.February);
	}
	/**
	 * @param february the february to set
	 */
	public void setFebruary(boolean february) {
		months.put(Months.February, february);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the march
	 */
	public boolean isMarch() {
		return isMonth(Months.March);
	}
	/**
	 * @param march the march to set
	 */
	public void setMarch(boolean march) {
		months.put(Months.March, march);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the april
	 */
	public boolean isApril() {
		return isMonth(Months.April);
	}
	/**
	 * @param april the april to set
	 */
	public void setApril(boolean april) {
		months.put(Months.April, april);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the may
	 */
	public boolean isMay() {
		return isMonth(Months.May);
	}
	/**
	 * @param may the may to set
	 */
	public void setMay(boolean may) {
		months.put(Months.May, may);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the june
	 */
	public boolean isJune() {
		return isMonth(Months.June);
	}
	/**
	 * @param june the june to set
	 */
	public void setJune(boolean june) {
		months.put(Months.June, june);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the july
	 */
	public boolean isJuly() {
		return isMonth(Months.July);
	}
	/**
	 * @param july the july to set
	 */
	public void setJuly(boolean july) {
		months.put(Months.July, july);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the august
	 */
	public boolean isAugust() {
		return isMonth(Months.August);
	}
	/**
	 * @param august the august to set
	 */
	public void setAugust(boolean august) {
		months.put(Months.August, august);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the september
	 */
	public boolean isSeptember() {
		return isMonth(Months.September);
	}
	/**
	 * @param september the september to set
	 */
	public void setSeptember(boolean september) {
		months.put(Months.September, september);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the october
	 */
	public boolean isOctober() {
		return isMonth(Months.October);
	}
	/**
	 * @param october the october to set
	 */
	public void setOctober(boolean october) {
		months.put(Months.October, october);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the november
	 */
	public boolean isNovember() {
		return isMonth(Months.November);
	}
	/**
	 * @param november the november to set
	 */
	public void setNovember(boolean november) {
		months.put(Months.November, november);
		nextEvent = null;
		calculateNextEvent();
	}
	/**
	 * @return the december
	 */
	public boolean isDecember() {
		return isMonth(Months.December);
	}
	/**
	 * @param december the december to set
	 */
	public void setDecember(boolean december) {
		months.put(Months.December, december);
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
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * @param dayNumber the day to set
	 */
	public void setDay(int dayNumber) {
		this.day = dayNumber;
		nextEvent = null;
		calculateNextEvent();
	}

	/**
	 * Calculates and sets the next upcoming event date.
	 */
	public void calculateNextEvent(){
		if (!isJanuary() && !isFebruary() && !isMarch() && !isApril() && !isMay() && !isJune() && !isJuly() && !isAugust() && !isSeptember() && !isOctober() && !isNovember() && !isDecember()){
			// do nothing
		}else{
			if (time != null && day > 0 && calculateNextEventAllowed()){
				Date candidNextEvent = null;
				if (nextEvent == null){
					candidNextEvent = DateTool.getNextDateAtThisTimeAndNumber(time, day, new Date());
					if (hasTimeFrameFrom() && candidNextEvent.compareTo(timeFrameFrom)<0){
						candidNextEvent = DateTool.getNextDateAtThisTimeAndNumber(time, day, timeFrameFrom);
					}
				}else{
					candidNextEvent = nextEvent;
				}
				Calendar cal = Calendar.getInstance();
				cal.setTime(candidNextEvent);
				
				while (!matches(cal.getTime())){
					cal.add(Calendar.MONTH, 1);
					
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
		(cal.get(Calendar.MONTH) == Calendar.JANUARY && isJanuary()) ||
		(cal.get(Calendar.MONTH) == Calendar.FEBRUARY && isFebruary()) ||
		(cal.get(Calendar.MONTH) == Calendar.MARCH && isMarch()) ||
		(cal.get(Calendar.MONTH) == Calendar.APRIL && isApril()) ||
		(cal.get(Calendar.MONTH) == Calendar.MAY && isMay()) ||
		(cal.get(Calendar.MONTH) == Calendar.JUNE && isJune()) ||
		(cal.get(Calendar.MONTH) == Calendar.JULY && isJuly()) ||
		(cal.get(Calendar.MONTH) == Calendar.AUGUST && isAugust()) ||
		(cal.get(Calendar.MONTH) == Calendar.SEPTEMBER && isSeptember()) ||
		(cal.get(Calendar.MONTH) == Calendar.OCTOBER && isOctober()) ||
		(cal.get(Calendar.MONTH) == Calendar.NOVEMBER && isNovember()) ||
		(cal.get(Calendar.MONTH) == Calendar.DECEMBER && isDecember());
	}
	

	
	/**
	 * returns a string presenting this schedule
	 */
	public String toString(){
		String str = "";
		if (isJanuary())
			str += ", January";
		if (isFebruary())
			str += ", February";
		if (isMarch())
			str += ", March";
		if (isApril())
			str += ", April";
		if (isMay())
			str += ", May";
		if (isJune())
			str += ", June";
		if (isJuly())
			str += ", July";
		if (isAugust())
			str += ", August";
		if (isSeptember())
			str += ", September";
		if (isOctober())
			str += ", October";
		if (isNovember())
			str += ", November";
		if (isDecember())
			str += ", December";
		
		if (str.startsWith(",")){
			str = " on " + str.substring(2);
		}
		
		str += " ";
		
		str = "Runs on day " + day + " at " + (new SimpleDateFormat(Const.DefaultTimeFormat)).format(time) + str;
		str = str.trim();
		str += super.toString();
		str = str.trim();
		return str;
	}
}
