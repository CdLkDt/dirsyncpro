/*
 * ArrayTools.java
 * 
 * Copyright (C) 2010-2011 O. Givi (info@dirsyncpro.org)
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

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains methods to manipulate arrays
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class ArrayTools {
	
	public static String[] addToArray(String str, String[] arr){
		String[] tmp = new String[arr.length + 1];
		System.arraycopy(arr, 0, tmp, 0, arr.length);
		tmp[tmp.length-1] = str;
		return tmp;
	}
	
	public static String[] deleteFromArray(String str, String[] arr){
		ArrayList<String> arrList = new ArrayList<String>(Arrays.asList(arr));
		arrList.remove(str);
		return (String[]) arrList.toArray(new String[arrList.size()]);
	}
	
	/**
	 * replaces String objA with objB in array arr. If objA doesn't exist or it is null, objB is added to the arr.
	 * @param objA object to be replaced 
	 * @param objB object to replace
	 * @param arr array
	 * @return the modified array
	 */
	public static String[] replaceInArray(String objA, String objB, String[] arr){
		ArrayList<String> arrList = new ArrayList<String>(Arrays.asList(arr));
		int i = -1;
		if (objA != null){
			i = arrList.indexOf(objA);
		}
		if (i >= 0){
			arrList.set(i, objB);
		}else{
			arrList.add(objB);
		}
		return (String[]) arrList.toArray(new String[arrList.size()]);
	}
}
