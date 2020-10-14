/*
 * DateTool.java
 * 
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dirsyncpro.Const;

public class DateTool{
	
	// Don't let anyone instantiate this class.
	private DateTool() {
	}

	/**
	 * Compares the given dates. Dates are only accurate to
	 * the second; therefore file dates are divided by 1000 and truncated
	 * (converting milliseconds to seconds).
	 * 
	 * @param fAD
	 *            date 1
	 * @param fBD
	 *            date 2
	 * @param gran
	 * 			  Granularity
	 * @param idlsgran
	 * 			  whether to ignore the daylight saving granularity
	 * @return int <code>-1</code> if the first file is newer than the second one;
	 *         int <code>0</code> if the dates are the same;
	 *         int <code>1</code> if the second date is newer than the first one.
	 */
	public static int cmpDates(long fAD, long fBD, int gran, boolean idlsgran) {
		long srcLastModified, dstLastModified, diff, offset;
		
		// convert to seconds
		srcLastModified = fAD / 1000;
		dstLastModified = fBD / 1000;
		diff = srcLastModified - dstLastModified;
		
		offset = Const.DEFAULT_GRANULARITY_TOLERANCE + gran;
		
		if (idlsgran){
			offset += 3600;
		}
		
		if (Math.abs(diff) <= offset){
			return 0;
		}else if (diff < offset){
			return 1;
		}else{
			return -1;
		}
	}

	
	public static Date getNextCompleteHour(){
		return getNextCompleteHour(new Date());
	}
	
	public static Date getNextDayAtThisTime(Date time){
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
		cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		
		Calendar now = Calendar.getInstance();
		if (cal.compareTo(now) < 0){
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		return cal.getTime();
	}

	public static Date getNextDateAtThisTimeAndNumber(Date time, int day, Date date){
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
		cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.DAY_OF_MONTH, day);
		
		Calendar now = Calendar.getInstance();
		if (cal.compareTo(now) < 0){
			cal.add(Calendar.MONTH, 1);
		}
		return cal.getTime();
	}

	public static Date getNextCompleteHour(Date time){
		Calendar timeCal = Calendar.getInstance();
		timeCal.setTime(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		if (cal.compareTo(timeCal) < 0){
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return cal.getTime();
	}
	
	public static Date add(Date time, int field, int amount){
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.add(field, amount);
		return cal.getTime();
	}
	
	public static Date nextDate(Date date, int field, int value){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(field, value);
		return cal.getTime();
	}
	
	public static long getSeconds(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.SECOND);
	}
	
	public static void printDate(Date date){
		System.out.println((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
	}
}