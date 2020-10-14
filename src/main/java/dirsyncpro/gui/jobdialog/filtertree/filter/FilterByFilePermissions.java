/*
 * FilterByFilePermissions.java
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
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import dirsyncpro.job.Job;

public class FilterByFilePermissions extends Filter{
	int permissionValue = 0;

	public FilterByFilePermissions(Job j, Action a){
		super(j, a);
		type = Filter.Type.ByFilePermissions;
	}

	public FilterByFilePermissions(Job j, Action a, String octalValue){
		this(j, a);
		permissionValue = Integer.parseInt(octalValue, 8);
	}
	
	public boolean matches(Path path){
		
		PosixFileAttributes posixFileAttributes = null;
		Set<PosixFilePermission> filePermissions = null;
		Set<PosixFilePermission> filterPermissions = null;
				
		try{
			posixFileAttributes = Files.readAttributes(path, PosixFileAttributes.class);
		}catch (IOException ioE){
			//TODO
		}
		
		filePermissions = posixFileAttributes.permissions();
		filterPermissions = convertToPosixFilePermission(permissionValue);
		
		return (
				  !(filterPermissions.contains(PosixFilePermission.OWNER_READ) ^ filePermissions.contains(PosixFilePermission.OWNER_READ)) &&
				  !(filterPermissions.contains(PosixFilePermission.OWNER_WRITE) ^ filePermissions.contains(PosixFilePermission.OWNER_WRITE)) &&
				  !(filterPermissions.contains(PosixFilePermission.OWNER_EXECUTE) ^ filePermissions.contains(PosixFilePermission.OWNER_EXECUTE)) &&
				  !(filterPermissions.contains(PosixFilePermission.GROUP_READ) ^ filePermissions.contains(PosixFilePermission.GROUP_READ)) &&
				  !(filterPermissions.contains(PosixFilePermission.GROUP_WRITE) ^ filePermissions.contains(PosixFilePermission.GROUP_WRITE)) &&
				  !(filterPermissions.contains(PosixFilePermission.GROUP_EXECUTE) ^ filePermissions.contains(PosixFilePermission.GROUP_EXECUTE)) &&
				  !(filterPermissions.contains(PosixFilePermission.OTHERS_READ) ^ filePermissions.contains(PosixFilePermission.OTHERS_READ)) &&
				  !(filterPermissions.contains(PosixFilePermission.OTHERS_WRITE) ^ filePermissions.contains(PosixFilePermission.OTHERS_WRITE)) &&
				  !(filterPermissions.contains(PosixFilePermission.OTHERS_EXECUTE) ^ filePermissions.contains(PosixFilePermission.OTHERS_EXECUTE))
				  );
	}

	private Set<PosixFilePermission> convertToPosixFilePermission(int permsValue){
		Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
		if ((permsValue&0400) > 0) perms.add(PosixFilePermission.OWNER_READ);
		if ((permsValue&0200) > 0) perms.add(PosixFilePermission.OWNER_WRITE);
		if ((permsValue&0100) > 0) perms.add(PosixFilePermission.OWNER_EXECUTE);
		if ((permsValue&040) > 0) perms.add(PosixFilePermission.GROUP_READ);
		if ((permsValue&020) > 0) perms.add(PosixFilePermission.GROUP_WRITE);
		if ((permsValue&010) > 0) perms.add(PosixFilePermission.GROUP_EXECUTE);
		if ((permsValue&04) > 0) perms.add(PosixFilePermission.OTHERS_READ);
		if ((permsValue&02) > 0) perms.add(PosixFilePermission.OTHERS_WRITE);
		if ((permsValue&01) > 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);
		return perms;
	}

	private int convertToPermissionValue(Set<PosixFilePermission> perms){
		int i = 0;
		if (perms.contains(PosixFilePermission.OWNER_READ)) i += 0400;
		if (perms.contains(PosixFilePermission.OWNER_WRITE)) i += 0200;
		if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) i += 0100;
		if (perms.contains(PosixFilePermission.GROUP_READ)) i += 040;
		if (perms.contains(PosixFilePermission.GROUP_WRITE)) i += 020;
		if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) i += 010;
		if (perms.contains(PosixFilePermission.OTHERS_READ)) i += 04;
		if (perms.contains(PosixFilePermission.OTHERS_WRITE)) i += 02;
		if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) i += 01;
		return i;
	}
	
	public void setPosixFilePermission(PosixFilePermission p, boolean isSet){
		Set<PosixFilePermission> perms = convertToPosixFilePermission(permissionValue);
		if (isSet){
			perms.add(p);
		}else{
			perms.remove(p);
		}
		permissionValue = convertToPermissionValue(perms);	
	}
	
	public boolean isPermissionSet(PosixFilePermission p){
		Set<PosixFilePermission> perms = convertToPosixFilePermission(permissionValue);
		return perms.contains(p);
	}
	
	protected int getPermissionValue() {
		return permissionValue;
	}
	
	public String getOctalPermissionValue(){
		String octalStr = Integer.toString(permissionValue, 8);
		while (octalStr.length() < 3){
			octalStr = "0" + octalStr;
		}
		return octalStr;
	}

	public void setOctalPermissionValue(String s) {
		this.permissionValue = Integer.parseInt(s, 8);
	}
	
	public String toString(){
		String s = "";
		s += " Permission value= " + getOctalPermissionValue();
		return s;
	}
	
	@Override
	public int compareTo(Filter s) {
		if (s instanceof FilterByFilePermissions){
			return (new Integer(permissionValue)).compareTo(new Integer(((FilterByFilePermissions) s).getPermissionValue()));
		}else{
			return super.compareTo(s);
		}
	}
}
