/*
 * J7CopierFactory.java
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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

public class J7CopierFactory {
	//copier objects for copy()
	private static Map<FileSystemProvider, J7Copier> nativeCopiers = new HashMap<>();
	private static FallbackCopier fallbackCopier = new FallbackCopier();
	
	public static J7Copier getCopier(Path srcPath, Path dstPath) {
	    	FileSystemProvider srcFSP = srcPath.getFileSystem().provider(), dstFSP = dstPath.getFileSystem().provider();
			if(srcFSP == dstFSP) {
				J7Copier copier = nativeCopiers.get(srcFSP);
				if(copier == null) {
					copier = new NativeCopier(srcFSP);
					nativeCopiers.put(srcFSP, copier);
				}
				return copier;
			} else {
				return fallbackCopier;
			}
	}
	
	public static void copyFileAttributes(FileSystemProvider sfsp, FileSystemProvider dfsp, Path srcPath, Path dstPath,
			boolean copyDosAttributes, boolean copyPosixPermissions, boolean copyPosixGroupAndOwner) throws IOException {
		if(copyDosAttributes) {
			try {
				DosFileAttributes dosAttr = sfsp.readAttributes(srcPath, DosFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				DosFileAttributeView dosAttrView = dfsp.getFileAttributeView(dstPath, DosFileAttributeView.class);
				dosAttrView.setArchive(dosAttr.isArchive());
				dosAttrView.setHidden(dosAttr.isHidden());
				dosAttrView.setReadOnly(dosAttr.isReadOnly());
				dosAttrView.setSystem(dosAttr.isSystem());
			} catch(UnsupportedOperationException u) {
				//DosFileAttributes not supported
			}
		}
		if(copyPosixPermissions || copyPosixGroupAndOwner) {
			try {
				PosixFileAttributes posixAttr = sfsp.readAttributes(srcPath, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				PosixFileAttributeView posixAttrView = dfsp.getFileAttributeView(dstPath, PosixFileAttributeView.class);
				if(copyPosixPermissions) {
					posixAttrView.setPermissions(posixAttr.permissions());
				}
				if(copyPosixGroupAndOwner) {
					posixAttrView.setGroup(posixAttr.group());
					posixAttrView.setOwner(posixAttr.owner());
				}
			} catch(UnsupportedOperationException u) {
				//PoxisFileAttributes not supported
			}
		}
		BasicFileAttributes srcAttr = sfsp.readAttributes(srcPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		BasicFileAttributeView dstAttrView = dfsp.getFileAttributeView(dstPath, BasicFileAttributeView.class);
		dstAttrView.setTimes(srcAttr.lastModifiedTime(), srcAttr.lastAccessTime(), srcAttr.creationTime());
	}
}
