/*
 * TimeTool.java
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

import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import dirsyncpro.DirSyncPro;

public class TimeTool{
	
	// Don't let anyone instantiate this class.
	private TimeTool() {
	}
	
	public static void delay(long milliseconds){
		try{
			Thread.sleep(milliseconds); 
		}catch(InterruptedException e){
			DirSyncPro.getLog().printThrowableStack(e);
		}
	}
	
	public static String convertToIndication(long millis){
		String timeStr = "";
		long seconds = millis / 1000;
		if (seconds < 60){
			timeStr = "less than a minute";
		}else if (seconds < 3600){
			timeStr = (int) seconds/60 + " minutes";
		}else if (seconds < 24*3600){
			timeStr = (int) seconds/3600 + " hours " + (int) (seconds % 3600) / 60 + " minutes";
		}else{
			timeStr =  (int) seconds/(24*3600) + " days " + (int) (seconds%(3600*24)) / 3600 + " hours " + (int) (seconds % 3600) / 60 + " minutes";
		}
		return timeStr;
	}
	
	public static boolean isInDayLightTimeNow(){
		return isInDayLightTime(new Date());
	}
	
	private static boolean isInDayLightTime(Date date){
		TimeZone tz = SimpleTimeZone.getDefault();
		return tz.inDaylightTime(date);
	}
}