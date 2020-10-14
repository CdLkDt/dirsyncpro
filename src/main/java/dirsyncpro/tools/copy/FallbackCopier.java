/*
 * FallbackCopier.java
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;

import dirsyncpro.DirSyncPro;

public class FallbackCopier extends J7Copier {
	
	//file size threshold for ETA calculations
	public final static long APPROX_THRESHOLD = 64 * 1024;
	public final static int BUFFER_SIZE = 8 * 1024;
	//option Enum objects for FileChannels
	private final EnumSet<StandardOpenOption> read = EnumSet.of(StandardOpenOption.READ),
			write = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE);

	@Override
	public void copy(Path srcPath, Path dstPath, boolean copyDosAttributes, boolean copyPosixPermissions, boolean copyPosixGroupAndOwner)
			throws IOException, SecurityException {

	    //FileSystemProvider objects for underlying file systems
	    FileSystemProvider srcProv = srcPath.getFileSystem().provider(), dstProv = dstPath.getFileSystem().provider();
		ProgressApproximator approximator = null;
		long srcSize = srcProv.readAttributes(srcPath, BasicFileAttributes.class).size();
		
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
		
    	//fall-back to ByteChannel transfer if providers are not equal (bypassing slow JDK fall-back implementation)
    	FileChannel readCh = null, writeCh = null;
    	try {
    		readCh = srcProv.newFileChannel(srcPath, read);
    		writeCh = dstProv.newFileChannel(dstPath, write);
			readCh.transferTo(0, srcProv.readAttributes(srcPath, BasicFileAttributes.class).size(), writeCh);
		//if ByteChannels are not supported either, use Streams
		} catch(UnsupportedOperationException u) {
			InputStream is = null;
			OutputStream os = null;
			try {
				is = srcProv.newInputStream(srcPath, read.toArray(new OpenOption[0]));
				os = dstProv.newOutputStream(dstPath, write.toArray(new OpenOption[0]));
				byte[] buffer = new byte[BUFFER_SIZE];
				int readBytes;
				while((readBytes = is.read(buffer, 0, BUFFER_SIZE)) > 0) {
					os.write(buffer, 0, readBytes);
				}
			} finally {
				try {if(is != null) is.close();} catch(Exception e) {}
				try {if(os != null) os.close();} catch(Exception e) {}
			}
    	} finally {
	    	try {if(readCh != null) readCh.close();} catch(Exception e) {}
	    	try {if(writeCh != null) writeCh.close();} catch(Exception e) {}
    	}
	    
		J7CopierFactory.copyFileAttributes(srcProv, dstProv, srcPath, dstPath, copyDosAttributes, copyPosixPermissions, copyPosixGroupAndOwner);
		
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
