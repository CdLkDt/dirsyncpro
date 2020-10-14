/*
 * XmlWriter.java
 * 
 * Copyright (C) 2008-2012 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2002, 2003, 2005, 2006, 2008 F. Gerbig
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

package dirsyncpro.xml;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import dirsyncpro.Const;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileAttribute;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileOwnership;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFilePermissions;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileSize;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPath;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPattern;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleDaily;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleHourly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMinutely;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMonthly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleOnce;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleWeekly;
import dirsyncpro.job.Job;


/**
 * Writes the given data to a XML config file.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
public class XmlWriter {

	private final int INDENTATION = 2;

	private OutputStream out;

	private Stack stack;

	private int indent;

	private String tag;

	/**
	 * Instantiates and initializes a new XMLWriter.
	 * 
	 * @param filename
	 * @param logFile
	 * @param jobs
	 * 
	 * @throws IOException
	 */
	public XmlWriter(String filename, String logFile, Vector jobs) throws IOException {
		out = new FileOutputStream(filename);
		stack = new Stack();
		indent = 0;

		writeProlog();

		tag = Xml.TAG_ROOT;
		writeDTD(tag);
		tag += addAttr(Xml.ATTR_LOGFILE, logFile);
		writeStartTag(tag);

		tag = Xml.TAG_META;
		tag += addAttr(Xml.ATTR_SAVED_WITH_PROGRAM_VERSION, Const.PROGRAM);
		tag += addAttr(Xml.ATTR_SAVED_ON_OS, (System.getProperty("os.name") + "-" + System.getProperty("os.version")) + "-" + System.getProperty("os.arch"));
		tag += addAttr(Xml.ATTR_SAVED_WITH_JAVA_VERSION, (System.getProperty("java.version") + "-" + System.getProperty("java.vendor")));
		writeEmptyTag(tag);
		
		for (Iterator iter = jobs.iterator(); iter.hasNext();) {
			Job job = (Job) iter.next();

			tag = Xml.TAG_JOB;
			tag += addAttr(Xml.ATTR_NAME, job.getName());
			tag += addAttr(Xml.ATTR_ENABLED, job.isEnabled());
			tag += addAttr(Xml.ATTR_SRC, job.getDirA());
			tag += addAttr(Xml.ATTR_DST, job.getDirB());
			tag += addAttr(Xml.ATTR_SYNC_MODE, job.getSyncMode().getLiteral());
			tag += addAttr(Xml.ATTR_WITHSUBFOLDERS, job.isRecursive());
			tag += addAttr(Xml.ATTR_VERIFY, job.isVerify());
			
			tag += addAttr(Xml.ATTR_REALTIME, job.isSyncRealtime());
			tag += addAttr(Xml.ATTR_REALTIME_SYNC_ON_START, job.isSyncRealtimeOnStart());
			tag += addAttr(Xml.ATTR_REALTIME_DELAY, job.getSyncRealtimeDelay());

			tag += addAttr(Xml.ATTR_LOGFILE, job.getLog().getPath());
			tag += addAttr(Xml.ATTR_SYNC_CMP_MODE, job.getCompareMode().toString());
			tag += addAttr(Xml.ATTR_SYNC_ALL, job.isCopyAll());
			tag += addAttr(Xml.ATTR_SYNC_LARGER, job.isCopyLarger());
			tag += addAttr(Xml.ATTR_SYNC_LARGERMODIFIED, job.isCopyLargerModified());
			tag += addAttr(Xml.ATTR_SYNC_MODIFIED, job.isCopyModified());
			tag += addAttr(Xml.ATTR_SYNC_NEW, job.isCopyNew());
			tag += addAttr(Xml.ATTR_DEL_FILES, job.isDelFiles());
			tag += addAttr(Xml.ATTR_DEL_DIRS, job.isDelDirs());
			tag += addAttr(Xml.ATTR_DEL_EXCLUDED_FILES_A, job.isDeleteExcludedFilesA());
			tag += addAttr(Xml.ATTR_DEL_EXCLUDED_DIRS_A, job.isDeleteExcludedDirsA());
			tag += addAttr(Xml.ATTR_DEL_EXCLUDED_FILES_B, job.isDeleteExcludedFilesB());
			tag += addAttr(Xml.ATTR_DEL_EXCLUDED_DIRS_B, job.isDeleteExcludedDirsB());

			tag += addAttr(Xml.ATTR_SYNC_CONFLICT_RESOLUTION_MODE, job.getSyncConflictResolutionMode().toString());
			tag += addAttr(Xml.ATTR_BACKUPS, job.getHowManyBackups() + "");
			tag += addAttr(Xml.ATTR_BACKUPINLINE, job.isBackupInline() + "");
			tag += addAttr(Xml.ATTR_BACKUPDIR, job.getBackupDir());
			tag += addAttr(Xml.ATTR_TIMESTAMPWRITEBACK, job.isWriteTimestampBack());
			tag += addAttr(Xml.ATTR_IGNOREDAYLIGHTSAVINGGRANULARIY, job.isIgnoreDaylightSavingGranularity());
			tag += addAttr(Xml.ATTR_DIRSYNCTIMESTAMP, job.isSyncDirTimeStamps());
			tag += addAttr(Xml.ATTR_TIMESTAMPDIFF, job.getGranularity());
			tag += addAttr(Xml.ATTR_SYM_LINK_MODE, job.getSymLinkMode().getLiteral());
			tag += addAttr(Xml.ATTR_PRESERVE_DOS_ATTRIBUTES, job.isPreserveDOSAttributes());
			tag += addAttr(Xml.ATTR_PRESERVE_POSIX_FILE_PERMISSIONS, job.isPreservePOSIXFilePermissions());
			tag += addAttr(Xml.ATTR_PRESERVE_POSIX_FILE_OWNERSHIP, job.isPreservePOSIXFileOwnership());
			tag += addAttr(Xml.ATTR_OVERRIDE_READ_ONLY, job.isOverrideReadOnly());

			writeStartTag(tag);
			for (Schedule sched : job.getSchedules()){
				tag = Xml.TAG_SCHEDULE;
				switch (sched.getType()){
				case Once:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Once.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_DATE, (new SimpleDateFormat(Const.DefaultDateFormat)).format(((ScheduleOnce) sched).getDate()));
					break;
				case Minutely:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Minutely.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_INTERVAL, ((ScheduleMinutely) sched).getInterval());
					break;
				case Hourly:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Hourly.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_INTERVAL, ((ScheduleHourly) sched).getInterval());
					break;
				case Daily:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Daily.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_INTERVAL, ((ScheduleDaily) sched).getInterval());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIME, (new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleDaily) sched).getTime()));
					break;
				case Weekly:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Weekly.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_INTERVAL, ((ScheduleWeekly) sched).getInterval());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIME, (new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleWeekly) sched).getTime()));
					tag += addAttr(Xml.ATTR_SCHEDULE_MONDAY, ((ScheduleWeekly) sched).isMonday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_TUESDAY, ((ScheduleWeekly) sched).isTuesday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_WEDNESDAY, ((ScheduleWeekly) sched).isWednesday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_THURSDAY, ((ScheduleWeekly) sched).isThursday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_FRIDAY, ((ScheduleWeekly) sched).isFriday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_SATURDAY, ((ScheduleWeekly) sched).isSaturday() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_SUNDAY, ((ScheduleWeekly) sched).isSunday() + "");
					break;
				case Monthly:
					tag += addAttr(Xml.ATTR_SCHEDULE_TYPE, Schedule.Type.Monthly.toString());
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_FROM, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameFrom()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIMEFRAME_TO, (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getTimeFrameTo()));
					tag += addAttr(Xml.ATTR_SCHEDULE_TIME, (new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleMonthly) sched).getTime()));
					tag += addAttr(Xml.ATTR_SCHEDULE_DAY, ((ScheduleMonthly) sched).getDay());
					tag += addAttr(Xml.ATTR_SCHEDULE_JANUARY, ((ScheduleMonthly) sched).isJanuary() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_FEBRUARY, ((ScheduleMonthly) sched).isFebruary() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_MARCH, ((ScheduleMonthly) sched).isMarch() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_APRIL, ((ScheduleMonthly) sched).isApril() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_MAY, ((ScheduleMonthly) sched).isMay() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_JUNE, ((ScheduleMonthly) sched).isJune() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_JULY, ((ScheduleMonthly) sched).isJuly() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_AUGUST, ((ScheduleMonthly) sched).isAugust() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_SEPTEMBER, ((ScheduleMonthly) sched).isSeptember() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_OCTOBER, ((ScheduleMonthly) sched).isOctober() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_NOVEMBER, ((ScheduleMonthly) sched).isNovember() + "");
					tag += addAttr(Xml.ATTR_SCHEDULE_DECEMBER, ((ScheduleMonthly) sched).isDecember() + "");
					break;
				default:
					break;
				}
				writeEmptyTag(tag);
			}
				
			for (Filter filt : job.getFilterSet().getFilters()){
				tag = Xml.TAG_FILTER;
				tag += addAttr(Xml.ATTR_FILTER_TYPE, filt.getType().toString());
				tag += addAttr(Xml.ATTR_FILTER_ACTION, filt.getAction().toString());
				switch (filt.getType()){
				case ByPattern:
					tag += addAttr(Xml.ATTR_FILTER_PATTERN_PATTERN_TYPE, ((FilterByPattern) filt).getPatternType().toString());
					tag += addAttr(Xml.ATTR_FILTER_PATTERN_PATTERN_STR, ((FilterByPattern) filt).getPattern());
					tag += addAttr(Xml.ATTR_FILTER_PATTERN_REGEXP, ((FilterByPattern) filt).isRegExp());
					break;
				case ByPath:
					tag += addAttr(Xml.ATTR_FILTER_PATH, ((FilterByPath) filt).getPathStr());
					break;
				case ByFileSize:
					tag += addAttr(Xml.ATTR_FILTER_FILE_SIZE_TYPE, ((FilterByFileSize) filt).getFileSizeType().toString());
					tag += addAttr(Xml.ATTR_FILTER_FILE_SIZE_VALUE, ((FilterByFileSize) filt).getValue() + "");
					break;
				case ByFilePermissions:
					tag += addAttr(Xml.ATTR_FILTER_FILE_PERMISSION, ((FilterByFilePermissions) filt).getOctalPermissionValue());
					break;
				case ByFileOwnership:
					tag += addAttr(Xml.ATTR_FILTER_FILE_OWNERSHIP_OWNER, ((FilterByFileOwnership) filt).getOwnerStr());
					tag += addAttr(Xml.ATTR_FILTER_FILE_OWNERSHIP_GROUP, ((FilterByFileOwnership) filt).getGroupStr());
					break;
				case ByDOSAttributes:
					tag += addAttr(Xml.ATTR_FILTER_DOS_ATTRIBUTE_READ_ONLY, ((FilterByFileAttribute) filt).isReadOnly() + "");
					tag += addAttr(Xml.ATTR_FILTER_DOS_ATTRIBUTE_HIDDEN, ((FilterByFileAttribute) filt).isHidden() + "");
					tag += addAttr(Xml.ATTR_FILTER_DOS_ATTRIBUTE_SYSTEM, ((FilterByFileAttribute) filt).isSystem() + "");
					tag += addAttr(Xml.ATTR_FILTER_DOS_ATTRIBUTE_ARCHIVE, ((FilterByFileAttribute) filt).isArchive() + "");
					break;
				case ByDate:
					tag += addAttr(Xml.ATTR_FILTER_DATE_MODE, ((FilterByDate) filt).getDateMode().toString());
					tag += addAttr(Xml.ATTR_FILTER_DATE, (new SimpleDateFormat(Const.DefaultDateFormat)).format(((FilterByDate) filt).getDate()));
					tag += addAttr(Xml.ATTR_FILTER_DATE_TYPE, ((FilterByDate) filt).getDateType().toString());
					tag += addAttr(Xml.ATTR_FILTER_DATE_DIRFILE, ((FilterByDate) filt).getDirFile().getLiteral());
					tag += addAttr(Xml.ATTR_FILTER_TIME_UNIT, ((FilterByDate) filt).getUnitType().toString());
					tag += addAttr(Xml.ATTR_FILTER_TIME_UNIT_VALUE, ((FilterByDate) filt).getTimeUnitValue());
					break;
				default:
					break;
				}
				writeEmptyTag(tag);
			}
			writeEndTag();
		}
		writeEndTag();
		out.close();
	}

	/**
	 * Encodes the string using XML entities. The five standard XML entities are
	 * replaced and additionally backslashes and linefeeds are encoded.
	 * 
	 * @param s
	 *            The string to encode.
	 * 
	 * @return The encoded string.
	 */
	private String encode(String s) {
		String t = "";

		if (s == null) {
			return t;
		}

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (c == '<') {
				t += "&lt;";
			} else if (c == '>') {
				t += "&gt;";
			} else if (c == '&') {
				t += "&amp;";
			} else if (c == '"') {
				t += "&quot;";
			} else if (c == '\'') {
				t += "&apos;";
			} else {
				t += c;
			}
		}

		return t;
	}

	private void writeProlog() throws IOException {
		writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}

	private void writeDTD(String tag) throws IOException {
		writeln("<!DOCTYPE " + tag + " [");
		writeln("<!ELEMENT " + Xml.TAG_ROOT + " (job*)>");
		writeln("<!ATTLIST " + Xml.TAG_ROOT + " " + Xml.ATTR_LOGFILE + " CDATA ''>");
		writeln("");
		writeln("<!ELEMENT " + Xml.TAG_JOB + " (schedule*)>");
		writeln("<!ELEMENT " + Xml.TAG_JOB + " (filter*)>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_NAME + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_ENABLED + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SRC + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DST + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_MODE + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_WITHSUBFOLDERS + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_VERIFY + " (true|false) 'false'>");

		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_REALTIME + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_REALTIME_SYNC_ON_START + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_REALTIME_DELAY + " CDATA #REQUIRED>");

		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_LOGFILE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_CMP_MODE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_ALL + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_LARGER + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_MODIFIED + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_LARGERMODIFIED + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_NEW + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_FILES + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_DIRS + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_EXCLUDED_FILES_A + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_EXCLUDED_DIRS_A + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_EXCLUDED_FILES_B + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_DEL_EXCLUDED_DIRS_B + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYNC_CONFLICT_RESOLUTION_MODE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_SYM_LINK_MODE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_TIMESTAMPDIFF + " CDATA '0'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_TIMESTAMPWRITEBACK + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_BACKUPS + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_BACKUPINLINE + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_BACKUPDIR + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_PRESERVE_DOS_ATTRIBUTES + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_PRESERVE_POSIX_FILE_PERMISSIONS + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_PRESERVE_POSIX_FILE_OWNERSHIP + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_JOB + " " + Xml.ATTR_OVERRIDE_READ_ONLY + " (true|false) 'false'>");
		writeln("");
		writeln("<!ELEMENT " + Xml.TAG_SCHEDULE + " EMPTY>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_TYPE + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_TIMEFRAME_FROM + " CDATA '01-01-1970 01:00'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_TIMEFRAME_TO + " CDATA '01-01-1970 01:00'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_INTERVAL + " CDATA '1'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_TIME + " CDATA '01:00'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_DATE + " CDATA '01-01-1970 01:00'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_DAY + " CDATA '1'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_MONDAY + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_TUESDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_WEDNESDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_THURSDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_FRIDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_SATURDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_SUNDAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_JANUARY + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_FEBRUARY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_MARCH + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_APRIL + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_MAY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_JUNE + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_JULY + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_AUGUST + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_SEPTEMBER + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_OCTOBER + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_NOVEMBER + " (true|false) 'false'>");
		writeln("<!ATTLIST " + Xml.TAG_SCHEDULE + " " + Xml.ATTR_SCHEDULE_DECEMBER + " (true|false) 'false'>");

		writeln("<!ELEMENT " + Xml.TAG_FILTER + " EMPTY>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_TYPE + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_ACTION + " CDATA #REQUIRED>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DATE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DATE_TYPE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DATE_MODE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_TIME_UNIT_VALUE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DOS_ATTRIBUTE_READ_ONLY + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DOS_ATTRIBUTE_HIDDEN + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DOS_ATTRIBUTE_SYSTEM + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_DOS_ATTRIBUTE_ARCHIVE + " (true|false) 'true'>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_FILE_OWNERSHIP_OWNER + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_FILE_OWNERSHIP_GROUP + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_FILE_PERMISSION + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_FILE_SIZE_VALUE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_FILE_SIZE_TYPE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_PATH + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_PATTERN_PATTERN_STR + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_PATTERN_PATTERN_TYPE + " CDATA ''>");
		writeln("<!ATTLIST " + Xml.TAG_FILTER + " " + Xml.ATTR_FILTER_PATTERN_REGEXP + " (true|false) 'false'>");

		writeln("]>");
	}

	private void write(String s) throws IOException {
		out.write(s.toString().getBytes("UTF-8"));
	}

	private void writeln() throws IOException {
		write(System.getProperty("line.separator"));
	}

	private void writeln(String s) throws IOException {
		write(s);
		writeln();
	}

	private void writelnText(String s) throws IOException {
		indent();
		writeln(s);
	}

	private void writeStartTag(String s) throws IOException {
		writelnText("<" + s + ">");
		indent += INDENTATION;
		stack.push(firstWord(s)); // Attribute weglassen
	}

	private String firstWord(String s) {
		String[] ss = s.split(" ");
		return ss[0];
	}

	private void writeEmptyTag(String s) throws IOException {
		writelnText("<" + s + "/>");
	}

	private final void writeEndTag() throws IOException {
		indent -= INDENTATION;
		writelnText("</" + stack.pop() + ">");
	}

	private void indent() throws IOException {
		for (int i = 0; i < indent; i++) {
			write(" ");
		}
	}

	private String addAttr(String attr, String value) {
		return " " + attr + "=\"" + encode(value) + "\"";
	}

	private String addAttr(String attr, int value) {
		return " " + attr + "='" + encode(value + "") + "'";
	}

	private String addAttr(String attr, boolean value) {
		return " " + attr + "=\"" + encode(value + "") + "\"";
	}
}