/*
 * FilterByFileAttribyte.java
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

import dirsyncpro.job.Job;

public class FilterByFileAttribute extends Filter{

	private Boolean readOnly = false;
	private Boolean hidden = false;
	private Boolean system = false;
	private Boolean archive = false;
	
	public FilterByFileAttribute(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByDOSAttributes;
	}
	
	public FilterByFileAttribute(Job j, Action a, boolean ro, boolean hi, boolean sy, boolean ar){
		this(j, a);
		readOnly = ro;
		hidden = hi;
		system = sy;
		archive = ar;
	}
	
	public boolean matches(Path path){
		DosFileAttributes dosFileAttributes = null;
		try{
			dosFileAttributes = Files.readAttributes(path, DosFileAttributes.class);
		}catch (IOException ioE){
			//TODO
		}
		
		return (
				  (readOnly && dosFileAttributes.isReadOnly()) ||
				  (hidden && dosFileAttributes.isHidden()) ||
				  (system && dosFileAttributes.isSystem()) ||
				  (archive && dosFileAttributes.isArchive())
				);
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public String toString(){
		String s = "";
		s += " Files with the DOS attributes:";
		if (this.readOnly != false){
			s += " read-only";
		}
		if (this.hidden != false){
			s += " hidden";
		}
		if (this.system != false){
			s += " system";
		}
		if (this.archive != false){
			s += " archive";
		}
		return s;
	}
	
	protected int toValue(){
		int i = 0;
		if (readOnly) i += 1000;
		if (hidden) i += 100;
		if (archive) i += 10;
		if (system) i += 1;
		return i;
	}
	
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByFileAttribute){
			return (new Integer(this.toValue())).compareTo(new Integer(((FilterByFileAttribute) s).toValue()));
		}else{
			return super.compareTo(s);
		}
	}
}
