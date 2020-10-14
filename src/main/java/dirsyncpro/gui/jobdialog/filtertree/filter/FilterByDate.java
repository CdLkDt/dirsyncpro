/*
 * FilterByDate.java
 * 
 * Copyright (C) 2012-2018 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.gui.jobdialog.filtertree.filter;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import dirsyncpro.Const;
import dirsyncpro.job.Job;
import dirsyncpro.tools.FileTools;

public class FilterByDate extends Filter{

	public enum FilterDateType{EarlierThan, ExactlyOn, LaterThan};

	public enum FilterDateMode{SpecificTime, TimeUnit};
	
	public enum FilterDateDirFile {
		File("Files"),
		Dir("Directories"),
		DirFile("Directories and files");
		
		String name;
		String literal;
		private FilterDateDirFile(String s) {
			name = s;
			literal = super.toString();
		}
		public String toString(){
			return name;
		}
		public String getLiteral() {
			return literal;
		}
		
		public static FilterDateDirFile valueOfOrDefault(String s) {
			FilterDateDirFile fdf;
			try {
				fdf = FilterDateDirFile.valueOf(s);
			} catch (Exception e) {
				fdf = DirFile;
			}
			return fdf;
		}
	};

	
	
	public enum FilterTimeUnitType {
	    Hours(Calendar.HOUR), 
	    Days(Calendar.DAY_OF_YEAR), 
	    Weeks(Calendar.WEEK_OF_YEAR), 
	    Months(Calendar.MONTH);
	
	    private FilterTimeUnitType(int value){
        		this.value = value;
	    }

	    private int value;
	    
	    public int getValue() {
	    		return value;
	    }
	};
	
	private FilterDateMode filterDateMode;
	private Date date;
	private FilterDateDirFile dirFile;
	private FilterDateType filterDateType;
	private int timeUnitValue;
	private FilterTimeUnitType unitType; 
	
	public FilterByDate(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByDate;
		dirFile = FilterDateDirFile.DirFile; // default: both Dirs and Files
	}
	
	public FilterByDate(Job j, Action a, Date d, FilterDateType fdt, FilterDateDirFile fdf){
		this(j, a);
		date = d;
		filterDateType = fdt;
		this.filterDateMode = FilterDateMode.SpecificTime;
		this.timeUnitValue = 0;
		this.unitType = FilterTimeUnitType.Hours; //dummy
		this.dirFile = fdf;
	}
	
	public FilterByDate(Job j, Action a, Integer unitValue, FilterTimeUnitType unitType, FilterDateType dateType, FilterDateDirFile fdf){
		this(j, a);
		this.timeUnitValue = unitValue;
		this.unitType = unitType;
		this.filterDateMode = FilterDateMode.TimeUnit;
		this.filterDateType = dateType;
		this.date = new Date();
		this.dirFile = fdf;
	}
	
	
	public boolean matches(Path path){
	    	if(filterDateMode == FilterDateMode.TimeUnit){
	    	    Calendar c = new GregorianCalendar();
	    	    c.add(unitType.getValue(), -1*timeUnitValue);
	    	    date = c.getTime();
	    	}	    

	    	File f = path.toFile();
	    	boolean exists = f.exists();
	    boolean isDir = exists && f.isDirectory();
	    boolean isFile = exists && !isDir;
	    	boolean matchable = (
	    				dirFile == FilterDateDirFile.DirFile ||
	    				(isDir && dirFile == FilterDateDirFile.Dir) ||
	    				(isFile && dirFile == FilterDateDirFile.File)
	    			);
	    	
    		int fileComp = FileTools.cmpFileDatesInMinutes(f, date);
		return (
				matchable && (
						(filterDateType == FilterDateType.EarlierThan && fileComp == -1) ||
						(filterDateType == FilterDateType.ExactlyOn && fileComp == 0) ||
						(filterDateType == FilterDateType.LaterThan && fileComp == 1)
					)
				);
	}

	public FilterDateMode getDateMode() {
	    return filterDateMode;
	}
	
	public boolean isFilterByTimeUnit(){
		return filterDateMode == FilterDateMode.TimeUnit;
	}
	
	public boolean isFilterByModificationTime(){
		return filterDateMode == FilterDateMode.SpecificTime;
	}
	
	public void setDateMode(FilterDateMode mode) {
	    this.filterDateMode = mode;
	}
	
	public Integer getTimeUnitValue() {
	    return timeUnitValue;
	}
	
	public void setTimeUnitValue(int unit) {
	    this.timeUnitValue = unit;
	}
	
	public FilterTimeUnitType getUnitType() {
	    return unitType;
	}
	
	public void setUnitType(FilterTimeUnitType unitType) {
	    this.unitType = unitType;
	}
	
	public FilterDateType getDateType() {
		return filterDateType;
	}

	public void setDateType(FilterDateType type) {
		this.filterDateType = type;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String toString(){
		String s = "";
		s += " " + this.dirFile.toString() + " modified";
		if (this.filterDateType == FilterDateType.EarlierThan){
			s += " earlier than";
		}else if (this.filterDateType == FilterDateType.ExactlyOn){
			s += " exactly on";
		}else{
			s += " later than";
		}
		if(filterDateMode == FilterDateMode.SpecificTime){
			s += " " + (new SimpleDateFormat(Const.DefaultDateFormat)).format(this.date);
		}else{
		    s += " " + timeUnitValue;
		    switch(unitType){
			case Hours:
			    s+= " hours"; 
			    break;
			case Days:
			    s+= " days"; 
			    break;
			case Weeks:
			    s+= " weeks"; 
			    break;
			case Months:
			    s+= " months"; 
			    break;
		    }
		    s+= " ago";
		}
		return s;
	}
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByDate){
			return date.compareTo(((FilterByDate) s).getDate());
		}else{
			return super.compareTo(s);
		}
	}

	public FilterDateDirFile getDirFile() {
		return dirFile;
	}

	public void setDirFile(FilterDateDirFile dirFile) {
		this.dirFile = dirFile;
	}
}
