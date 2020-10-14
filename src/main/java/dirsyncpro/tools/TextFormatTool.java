/*
 * TextFromatTool.java
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

import java.io.File;
import java.text.SimpleDateFormat;

import dirsyncpro.Const;

public class TextFormatTool{
	
	// Don't let anyone instantiate this class.
	private TextFormatTool() {
	}
	
	/**
	 * Returns a date string for the last modification of the file.
	 * 
	 * @return Time of last modification of the file as a date string.
	 */
	public static String getLastModifiedShort(File f) {
		return (new SimpleDateFormat(Const.DefaultDateShortFormat)).format(f.lastModified());
	}
	
	/**
	 * Returns a date string for the last modification of the time.
	 * 
	 * @return Time of last modification of the file as a date string.
	 */
	public static String getLastModifiedShort(long time) {
		return (new SimpleDateFormat(Const.DefaultDateShortFormat)).format(time);
	}

	/**
	 * Returns a date string for the last modification of the file.
	 * 
	 * @return Time of last modification of the file as a date string.
	 */
	public static String getLastModifiedLong(File f) {
		return (new SimpleDateFormat(Const.DefaultDateLongFormat)).format(f.lastModified());
	}

	/**
	 * Returns a date string for the last modification of the time.
	 * 
	 * @return Time of last modification of the file as a date string.
	 */
	public static String getLastModifiedLong(long time) {
		return (new SimpleDateFormat(Const.DefaultDateLongFormat)).format(time);
	}

	/**
	 * Returns the formatted length.
	 * 
	 * @param f
	 *            The File of which the size is needed
	 * @return Formatted length.
	 */
	public static String getLength(File f) {

		long length = f.length();
		return getHumanReadable(length);
	}
	
	
	/**
	 * Returns the human readable formatted number.
	 * 
	 * @param number
	 *            The number to format
	 * @return Formatted number.
	 */
	public static String getHumanReadable(long number){
	    int base = 1024;
	    if (number < base){
		return number + " B";
	    }else{
		int exponent = (int) (Math.log(number) / Math.log(base));
		char unit = "KMGTPE".charAt(exponent - 1);
		return String.format("%.1f %sB", number / Math.pow(base, exponent), unit);
	    }
	    
	}
	
	/**
	 * Returns the string representing this date
	 * @param date
	 * @return the date string
	 */
	public static String getDateText(long millis){
		String s = "";
		long hours = millis / 3600000;
		long minutes = (millis % 3600000)/1000;
		System.out.println(millis + " " + hours + " " + minutes);
		
		if (minutes > 1){
			if (hours > 1){
				s = hours + " hours and " + minutes + " minutes";
			}else if (hours == 1){
				s = hours + " hour and " + minutes + " minutes";
			}
		}else if (minutes == 1){
			if (hours > 1){
				s = hours + " hours and " + minutes + " minute";
			}else if (hours == 1){
				s = hours + " hour and " + minutes + " minute";
			}
		}else{
			if (hours > 1){
				s = hours + " hours";
			}else if (hours == 1){
				s = hours + " hour";
			}else{
				s = "less than a minute";
			}
		}
		return s;
	}
}