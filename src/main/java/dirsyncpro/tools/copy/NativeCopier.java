/*
 * NativeCopier.java
 * 
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org), Michael Lux
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

package dirsyncpro.tools.copy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;

import dirsyncpro.DirSyncPro;

public class NativeCopier extends J7Copier {
	
	//file size threshold for ETA calculations
	public final static long APPROX_THRESHOLD = 64 * 1024;
	//file system provider
	private final FileSystemProvider fsProv;
	
	public NativeCopier(FileSystemProvider prov) {
		fsProv = prov;
	}

	@Override
	public void copy(Path srcPath, Path dstPath, boolean copyDosAttributes, boolean copyPosixPermissions, boolean copyPosixGroupAndOwner)
			throws IOException, SecurityException {
		
		ProgressApproximator approximator = null;
		long srcSize = fsProv.readAttributes(srcPath, BasicFileAttributes.class).size();
		
		if(srcSize > APPROX_THRESHOLD) {
			//use approximation if file > 64 kB ...
			approximator = ProgressApproximator.getApproximator();
		    approximator.startApprox(srcPath, true);
		} else {
			//... else set progress to 0 percent
			if(DirSyncPro.isGuiMode()){
				DirSyncPro.getGui().registerProgressBars(-1, -1, -1, "", false, 0, 100, "", false);
			}
		}
		
		fsProv.copy(srcPath, dstPath, StandardCopyOption.REPLACE_EXISTING);

		J7CopierFactory.copyFileAttributes(fsProv, fsProv, srcPath, dstPath, copyDosAttributes, copyPosixPermissions, copyPosixGroupAndOwner);
	    
	    if(approximator != null) {
	    	//file transfer complete, update approximation ...
			approximator.endApprox();
	    } else {
	    	//... or set progress to 100 percent
			if(DirSyncPro.isGuiMode()){
				DirSyncPro.getGui().registerProgressBars(-1, -1, -1, "", false, 100, 100, "", false);
			}
		}
		
	}

}
