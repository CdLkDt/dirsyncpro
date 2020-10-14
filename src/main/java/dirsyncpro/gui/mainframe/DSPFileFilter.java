/*
 * DSPFileFilter.java
 * 
 * Copyright (C) 2010-2011 O. Givi
 * Copyright (C) 2003, 2006 F. Gerbig
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

package dirsyncpro.gui.mainframe;
import java.io.File;

import javax.swing.filechooser.FileFilter;

import dirsyncpro.Const;

/**
 * Shows only files with the configuration extension of DirSyncPro.
 *  
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
public class DSPFileFilter extends FileFilter {
	
	String extension;
	
	public DSPFileFilter(String ext){
		super();
		this.extension = ext;
	}
	
	/**
	 * Returns if the given File shall be shown in a file chooser.
	 * 
	 * @param f The File in question.
	 * @return <code>true</code> if the file shall be shown, <code>false</code> else.
	 */
	public boolean accept(File f) {
		// show all directories
		if (f.isDirectory()) {
			return true;
		}

		// show all files with the right extension
		if (f.getAbsolutePath().toLowerCase().endsWith("." + extension)) {
			return true;
		}

		// hide everything else (files with a wrong extension)
		return false;
	}

	/**
	 * Returns a description for this file filter.
	 * 
	 * @return The description for this file filter.
	 */
	public String getDescription() {
		return Const.FileType.valueOf(extension.toUpperCase()).getDescription();
	}
}
