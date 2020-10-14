/*
 * WildcardTools.java
 *
 * Copyright (C) 2005, 2007, 2008 F. Gerbig (fgerbig@users.sourceforge.net)
 * Copyright (C) 2005 T. Groetzner 
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;

import dirsyncpro.Const;
import dirsyncpro.job.Job;

/**
 * Tools for handling the time and date wildcards. 
 * @author T. Groetzner, F. Gerbig (fgerbig@users.sourceforge.net) 
 */
public class WildcardTools {
	public enum WCType{Date, Time, User, Job};
	
	public enum WildCard{
		DATE("<date>", WCType.Date),
		DATE_DAY("<DD>", WCType.Date),
		DATE_MONTH("<MM>", WCType.Date),
		DATE_YEAR("<YYYY>", WCType.Date),

		TIME("<time>", WCType.Time),
		TIME_HOUR("<hh>", WCType.Time),
		TIME_MINUTE("<mm>", WCType.Time),
		TIME_SECOND("<ss>", WCType.Time),
		
		NAME("<jobname>", WCType.Job),
		
		USERNAME("<username>", WCType.User),
		USERHOME("<userhome>", WCType.User);
		
		private String string;
		private WCType type;
		
		private WildCard(String str, WCType wct){
			this.string = str;
			type = wct;
		}
		public String getString() {
			return string;
		}
		public WCType getType() {
			return type;
		}
	};
	
	// Don't let anyone instantiate this class.
	private WildcardTools() {
	}

	/**
	 * Replaces the date wildcards in the given String with the given Date.
	 * @param s The String containing wildcards.
	 * @param d The date to replace the wildcards with.
	 * @return The string with the wildcards replaced.
	 */
	public static String replaceDateWildcards(String s, Date d) {
		String date;
		String year;
		String month;
		String day;
	
		// format date
		SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
		df.applyPattern("yyyy-MM-dd");
		date = df.format(d);
	
		// format single date parts
		df.applyPattern("yyyy"); // year
		year = df.format(d);
		df.applyPattern("MM"); // month
		month = df.format(d);
		df.applyPattern("dd"); // day
		day = df.format(d);

		s = replaceAll(s, WildCard.DATE.getString(), date);
		s = replaceAll(s, WildCard.DATE_YEAR.getString(), year);
		s = replaceAll(s, WildCard.DATE_MONTH.getString(), month);
		s = replaceAll(s, WildCard.DATE_DAY.getString(), day);
	
		return s;
	}

	/**
	 * Replaces the time wildcards in the given String with the given Date.
	 * @param s The String containing wildcards.
	 * @param d The date to replace the wildcards with.
	 * @return The string with the wildcards replaced.
	 */
	public static String replaceTimeWildcards(String s, Date d) {
		String time;
		String hour;
		String minute;
		String second;

		// format time
		SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
		df.applyPattern("HH_mm_ss");
		time = df.format(d);
		
		// format single time parts
		df.applyPattern("HH"); // hour
		hour = df.format(d);
		df.applyPattern("mm"); // minute
		minute = df.format(d);
		df.applyPattern("ss"); // second
		second = df.format(d);
		
		s = replaceAll(s, WildCard.TIME.getString(), time);
		s = replaceAll(s, WildCard.TIME_HOUR.getString(), hour);
		s = replaceAll(s, WildCard.TIME_MINUTE.getString(), minute);
		s = replaceAll(s, WildCard.TIME_SECOND.getString(), second);

		return s;
	}

	/**
	 * Replaces the jobname wildcards in the given String with the corresponding system properties.
	 * @param s The String containing wildcards.
	 * @param job The directory to replace the wildcards with.
	 * @return The string with the wildcards replaced.
	 */
	public static String replaceDirectoryWildcards(String s, Job job) {
		return replaceAll(s, WildCard.NAME.getString(), job.getName());
	}

	/**
	 * Replaces the user wildcards in the given String with the corresponding system properties.
	 * @param s The String containing wildcards.
	 * @return The string with the wildcards replaced.
	 */
	public static String replaceUserWildcards(String s) {
		s = replaceAll(s, WildCard.USERNAME.getString(), System.getProperty("user.name",""));
		s = replaceAll(s, WildCard.USERHOME.getString(), System.getProperty("user.home",""));
		return s;
	}
	
	/**
	 * Generic "replace all" method replacing all occurences of &lt;what&gt; in &lt;where&gt; with &lt;withWhat&gt;.
	 * @param where The <code>String</code> to change.
	 * @param what The <code>String</code> to replace.
	 * @param withWhat The <code>String</code> to replace with.
	 * @return The string with all occurences of &lt;what&gt; replaced by &lt;withWhat&gt;.
	 */
	public static String replaceAll(String where, String what, String withWhat) {
		while (where.indexOf(what) != -1) {
			where = where.replace(what, withWhat);
		}
		return where;
	}
}
