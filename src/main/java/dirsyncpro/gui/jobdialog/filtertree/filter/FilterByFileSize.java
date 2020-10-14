/*
 * FilterByFileSize.java
 * 
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org)
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

import dirsyncpro.job.Job;

public class FilterByFileSize extends Filter{

	public enum FilterFileSizeType{SmallerThan, Exactly, LargerThan};

	private long value = 0;
	private FilterFileSizeType fileSizeType;
	
	public FilterByFileSize(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByFileSize;
	}

	public FilterByFileSize(Job j, Action a, long val, FilterFileSizeType fst){
		this(j, a);
		value = val;
		fileSizeType = fst;
	}
	
	public boolean matches(Path path){
		File f = path.toFile();
		return (f.isFile() &&
				(
				 (fileSizeType == FilterFileSizeType.SmallerThan && f.length() < value) ||
				 (fileSizeType == FilterFileSizeType.Exactly && f.length() == value) ||
				 (fileSizeType == FilterFileSizeType.LargerThan && f.length() > value)
				)
 			   );
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}
	
	public FilterFileSizeType getFileSizeType() {
		return fileSizeType;
	}

	public void setFileSizeType(FilterFileSizeType type) {
		this.fileSizeType = type;
	}
	
	public String toString(){
		String s = "";
		s += " Files with size";
		if (this.fileSizeType == FilterFileSizeType.SmallerThan){
			s += " smaller than";
		}else if (this.fileSizeType == FilterFileSizeType.Exactly){
			s += " of exactly";
		}else{
			s += " larger than";
		}
		s += " " + this.value + " bytes";
		return s;
	}
	
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByFileSize){
			if (fileSizeType == ((FilterByFileSize) s).getFileSizeType()){
				return (new Long(value)).compareTo(new Long(((FilterByFileSize) s).getValue()));
			}else{
				return fileSizeType.compareTo(((FilterByFileSize) s).getFileSizeType());
			}
		}else{
			return super.compareTo(s);
		}
	}
}
