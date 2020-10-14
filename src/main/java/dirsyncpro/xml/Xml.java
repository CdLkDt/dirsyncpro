/*
 * Xml.java
 * 
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
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

/**
 * Constants for XML tags and attributes.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
abstract class Xml {

	//Don't let anyone instantiate this class.
	private Xml() {
	}

	static final String TAG_ROOT = "dirsyncpro";
	static final String TAG_JOB = "job";
	static final String TAG_SCHEDULE = "schedule";
	static final String TAG_FILTER = "filter";
	static final String TAG_META = "meta";
	static final String ATTR_LOGFILE = "logfile";
	static final String ATTR_SAVED_WITH_PROGRAM_VERSION = "program";
	static final String ATTR_SAVED_ON_OS = "os";
	static final String ATTR_SAVED_WITH_JAVA_VERSION = "java";
	static final String ATTR_NAME = "name";
	static final String ATTR_ENABLED = "enabled";
	static final String ATTR_SRC = "src";
	static final String ATTR_DST = "dst";
	static final String ATTR_SYNC_MODE = "syncMode";
	static final String ATTR_WITHSUBFOLDERS = "recursive";
	static final String ATTR_VERIFY = "verify";
	static final String ATTR_REALTIME = "realtimeSync";
	static final String ATTR_REALTIME_SYNC_ON_START = "realtimeSyncOnStart";
	static final String ATTR_REALTIME_DELAY = "realtimeDelayValue";	
	static final String ATTR_TIMESTAMPWRITEBACK = "timestampWriteBack";
	static final String ATTR_DIRSYNCTIMESTAMP = "syncDirTimeStamp";
	static final String ATTR_IGNOREDAYLIGHTSAVINGGRANULARIY = "ignoreDaylightSavingGarnularity";
	static final String ATTR_TIMESTAMPDIFF = "granularity";
	static final String ATTR_SYM_LINK_MODE = "symLinkMode";
	static final String ATTR_SYNC_CMP_MODE = "compareMode";
	static final String ATTR_SYNC_NEW = "copyNew";
	static final String ATTR_SYNC_MODIFIED = "copyModified";
	static final String ATTR_SYNC_LARGER = "copyLarger";
	static final String ATTR_SYNC_LARGERMODIFIED = "copyLargerAndModified";
	static final String ATTR_SYNC_ALL = "copyAll";
	static final String ATTR_DEL_FILES = "delFiles";
	static final String ATTR_DEL_DIRS = "delDirs";
	static final String ATTR_DEL_EXCLUDED_DIRS_A = "delExcludedDirsA";
	static final String ATTR_DEL_EXCLUDED_FILES_A = "delExcludedFilesA";
	static final String ATTR_DEL_EXCLUDED_DIRS_B = "delExcludedDirsB";
	static final String ATTR_DEL_EXCLUDED_FILES_B = "delExcludedFilesB";
	static final String ATTR_SYNC_CONFLICT_RESOLUTION_MODE = "syncConflictResolutionMode";
	static final String ATTR_BACKUPS = "backups";
	static final String ATTR_BACKUPINLINE = "backupInline";
	static final String ATTR_BACKUPDIR = "backupDir";
	static final String ATTR_PRESERVE_DOS_ATTRIBUTES = "preserveDOSAttributes";
	static final String ATTR_PRESERVE_POSIX_FILE_PERMISSIONS = "preservePOSIXFilePermissions";
	static final String ATTR_PRESERVE_POSIX_FILE_OWNERSHIP = "preservePOSIXFileOwnership";
	static final String ATTR_OVERRIDE_READ_ONLY = "overrideReadOnly";
	
	static final String ATTR_SCHEDULE_TYPE = "type";
	static final String ATTR_SCHEDULE_TIMEFRAME_FROM = "timeFrameFrom";
	static final String ATTR_SCHEDULE_TIMEFRAME_TO = "timeFrameTo";
	static final String ATTR_SCHEDULE_INTERVAL = "interval";
	static final String ATTR_SCHEDULE_TIME = "time";
	static final String ATTR_SCHEDULE_DATE = "date";
	static final String ATTR_SCHEDULE_DAY = "day";
	static final String ATTR_SCHEDULE_MONDAY = "monday";
	static final String ATTR_SCHEDULE_TUESDAY = "tuesday";
	static final String ATTR_SCHEDULE_WEDNESDAY = "wednesday";
	static final String ATTR_SCHEDULE_THURSDAY = "thursday";
	static final String ATTR_SCHEDULE_FRIDAY = "friday";
	static final String ATTR_SCHEDULE_SATURDAY = "saturday";
	static final String ATTR_SCHEDULE_SUNDAY = "sunday";
	static final String ATTR_SCHEDULE_JANUARY = "january";
	static final String ATTR_SCHEDULE_FEBRUARY = "february";
	static final String ATTR_SCHEDULE_MARCH = "march";
	static final String ATTR_SCHEDULE_APRIL = "april";
	static final String ATTR_SCHEDULE_MAY = "may";
	static final String ATTR_SCHEDULE_JUNE = "june";
	static final String ATTR_SCHEDULE_JULY = "july";
	static final String ATTR_SCHEDULE_AUGUST = "august";
	static final String ATTR_SCHEDULE_SEPTEMBER = "september";
	static final String ATTR_SCHEDULE_OCTOBER = "october";
	static final String ATTR_SCHEDULE_NOVEMBER = "november";
	static final String ATTR_SCHEDULE_DECEMBER = "december";
	
	static final String ATTR_FILTER_TYPE = "type";
	static final String ATTR_FILTER_ACTION = "action";
	static final String ATTR_FILTER_DATE_MODE = "dateMode";
	static final String ATTR_FILTER_DATE = "date";
	static final String ATTR_FILTER_DATE_TYPE = "dateType";
	static final String ATTR_FILTER_DATE_DIRFILE = "dirFile";
	static final String ATTR_FILTER_TIME_UNIT = "timeUnit";
	static final String ATTR_FILTER_TIME_UNIT_VALUE = "timeUnitValue";
	static final String ATTR_FILTER_DOS_ATTRIBUTE_READ_ONLY = "readonly";
	static final String ATTR_FILTER_DOS_ATTRIBUTE_HIDDEN = "hidden";
	static final String ATTR_FILTER_DOS_ATTRIBUTE_SYSTEM = "system";
	static final String ATTR_FILTER_DOS_ATTRIBUTE_ARCHIVE = "archive";
	static final String ATTR_FILTER_FILE_OWNERSHIP_OWNER = "owner";
	static final String ATTR_FILTER_FILE_OWNERSHIP_GROUP = "group";
	static final String ATTR_FILTER_FILE_PERMISSION = "permissionValue";
	static final String ATTR_FILTER_FILE_SIZE_VALUE = "fileSizeValue";
	static final String ATTR_FILTER_FILE_SIZE_TYPE = "fileSizeType";
	static final String ATTR_FILTER_PATH = "path";
	static final String ATTR_FILTER_PATTERN_PATTERN_STR = "pattern";
	static final String ATTR_FILTER_PATTERN_PATTERN_TYPE = "patternType";
	static final String ATTR_FILTER_PATTERN_REGEXP = "regularExpression";
}