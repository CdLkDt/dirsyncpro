/*
 * DesktopTools.java
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

import java.awt.Desktop;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import dirsyncpro.DirSyncPro;

/**
 * Contains methods to manipulate arrays
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class DesktopTools {
	
	private static URI getFileURI(String filePath){
		URI uri = null;
		filePath = filePath.trim();
		if(filePath.startsWith("http") || filePath.startsWith("\\")){
			if(filePath.startsWith("\\")){
				filePath = "file:" + filePath;
			}
			try{
				filePath = filePath.replaceAll(" ", "%20");
				uri = (new URL(filePath)).toURI();
			}catch (MalformedURLException ex){
				DirSyncPro.getLog().printThrowableStack(ex);
			}catch (URISyntaxException ex){
				DirSyncPro.getLog().printThrowableStack(ex);
			}
		}
		else{
			File file = new File(filePath);
			uri = file.toURI();
		}
		return uri;
	}

	/**
	 * Converts the path to uri and launches the file via the desktop
	 * @param filePath
	 * 			Path to the file
	 */
	public static void launchFile(String filePath){
		if(!Desktop.isDesktopSupported() || filePath == null || filePath.trim().length() == 0){
			return;
		}
		try{      
			Desktop.getDesktop().browse(getFileURI(filePath));
		}catch (Exception ex){
			DirSyncPro.displayError("Unable to open: " + filePath + "\n\n" + ex);
			DirSyncPro.getLog().printThrowableStack(ex);
		}
	}
}
