/*
 * FileTools.java
 * 
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org), Michael Lux
 * Copyright (C) 2010-2011 O. Givi (info@dirsyncpro.org), Toj
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2003-2006, 2008 F. Gerbig
 * Copyright (C) 2002 E. Gerber  
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.exceptions.ErrorException;
import dirsyncpro.exceptions.WarningException;
import dirsyncpro.tools.copy.J7CopierFactory;

/**
 * Contains methods to compare file date and size and to copy a file.
 * 
 * @author E. Gerber , F. Gerbig (fgerbig@users.sourceforge.net)
 */
public class FileTools {

	private static final int BUFFER_SIZE = 1 * 1024 * 1024;

	// a buffer for the copy method
	private static byte[] buffers = new byte[BUFFER_SIZE];

	// Don't let anyone instantiate this class.
	private FileTools() {
	}

	/**
	 * Compares the date of the given file against the given date. File date is only accurate to
	 * the second; therefore file dates are divided by 60000 and truncated
	 * (converting milliseconds to minutes).
	 * 
	 * @param file
	 *            The source file.
	 * @param date
	 *            The given date.
	 * @param gran
	 * 			  Granularity
	 * @param idlsgran
	 * 			  whether to ignore the daylight saving granularity
	 * @return int <code>1</code> if the  file is newer than the
	 *         date, <code>0</code> if the file is modified on date,
	 *         <code>-1</code> if the date is newer.
	 */
	public static int cmpFileDatesInMinutes(File file, Date date) {
		long fileLastModified;
		
		// convert to minutes
		fileLastModified = file.lastModified() / 60000;
		long minutes = date.getTime()/60000;
		if (fileLastModified > minutes){
			return 1;
		}else if (fileLastModified == minutes){
			return 0;
		}else{
			return -1;
		}
	}

	

	/**
	 * Compares the dates of the given files. File dates are only accurate to
	 * the second; therefore file dates are divided by 1000 and truncated
	 * (converting milliseconds to seconds).
	 * 
	 * @param src
	 *            The source file.
	 * @param dst
	 *            The destination file.
	 * @param gran
	 * 			  Granularity
	 * @param idlsgran
	 * 			  whether to ignore the daylight saving granularity
	 * @return int <code>-1</code> if the first file is newer than the second one;
	 *         int <code>0</code> if the modification times are the same;
	 *         int <code>1</code> if the second file is newer than the first one.
	 *         If the second file doesn't exist <code>-1</code> is returned.
	 */
	public static int cmpFileDates(File src, File dst, int gran, boolean idlsgran) {
		long srcLastModified, dstLastModified, diff, offset;
		
		if (!dst.exists()) {
			return -1;
		}

		// convert to seconds
		srcLastModified = src.lastModified() / 1000;
		dstLastModified = dst.lastModified() / 1000;
		diff = srcLastModified - dstLastModified;
		
		offset = Const.DEFAULT_GRANULARITY_TOLERANCE + gran;
		
		if (idlsgran){
			offset += 3600;
		}
		
		if (Math.abs(diff) <= offset){
			return 0;
		}else if (diff < offset){
			return 1;
		}else{
			return -1;
		}
	}

	/**
	 * Compares the sizes of the given files.
	 * 
	 * @param src
	 *            The source file.
	 * @param dst
	 *            The destination file.
	 * @return int <code>-1</code> if the first file is smaller than the second one or the second file doesn't exist,
	 *             <code>0</code> if the sizes are the same, 
	 *             <code>1</code>  if the first file is larger than the second one.
	 */
	public static int cmpFileSizes(File src, File dst) {
		if (!dst.exists()) {
			return -1;
		}
		long srcSize = src.length();
		long dstSize = dst.length();
		if (srcSize < dstSize){
			return -1;
		}else if (srcSize == dstSize){
			return 0;
		}else{
			return 1;
		}
	}

	
	/**
	  * Compares the attributes of the given files.
	  * 
	  * @param src
	  *            The source file.
	  * @param dst
	  *            The destination file.
	  * @return int <code>1</code> there are differences in file attributes between the files
	  *             <code>0</code> both files have the same attributes
	  *             <code>-1</code> second file does not exist.
	  */
	public static int cmpFileAttributes(File src, File dst) {
	    if (!dst.exists()) {
		return -1;
	    }
   
	    Path srcPath = Paths.get(src.getAbsolutePath());
	    Path dstPath = Paths.get(dst.getAbsolutePath());
   	    
	    if (Const.OS_IS_WINDOWS){
		    try{
				//DOS ATTRIBUTES
				DosFileAttributeView srcDosFileAttributeView = Files.getFileAttributeView(srcPath, DosFileAttributeView.class);
				DosFileAttributeView dstDosFileAttributeView = Files.getFileAttributeView(dstPath, DosFileAttributeView.class);
				if(srcDosFileAttributeView != null && dstDosFileAttributeView != null){
				    DosFileAttributes srcDosFileAttributes = srcDosFileAttributeView.readAttributes();
				    DosFileAttributes dstDosFileAttributes = dstDosFileAttributeView.readAttributes();
		          
				    if(srcDosFileAttributes.isArchive() != dstDosFileAttributes.isArchive() || srcDosFileAttributes.isHidden() != dstDosFileAttributes.isHidden() 
		        		|| srcDosFileAttributes.isReadOnly() != dstDosFileAttributes.isReadOnly() || srcDosFileAttributes.isSystem() != dstDosFileAttributes.isSystem()){
					return 1;
				    }
				}
		    }catch(IOException ex){
			//DosFileAttributes not supported
		    }      
	    }else{
		    try {
				//POSIX ATTRIBUTES AND PERMISSIONS
				PosixFileAttributeView srcPosixFileAttributeView = Files.getFileAttributeView(srcPath, PosixFileAttributeView.class);
				PosixFileAttributeView dstPosixFileAttributeView = Files.getFileAttributeView(dstPath, PosixFileAttributeView.class);
		
				if(srcPosixFileAttributeView != null && dstPosixFileAttributeView != null){
				    PosixFileAttributes srcPosixFileAttributes = srcPosixFileAttributeView.readAttributes();
				    PosixFileAttributes dstPosixFileAttributes = dstPosixFileAttributeView.readAttributes();
		
				    if(!srcPosixFileAttributes.permissions().containsAll(dstPosixFileAttributes.permissions()) 
					    || !dstPosixFileAttributes.permissions().containsAll(srcPosixFileAttributes.permissions())){
					return 1;
				    }          
				    
				    if(!srcPosixFileAttributes.owner().equals(dstPosixFileAttributes.owner())
					    && !srcPosixFileAttributes.group().equals(dstPosixFileAttributes.group())){
					return 1;
				    }
				}
		    }catch (IOException e) {
				//PoxisFileAttributes not supported
		    }
	    }
	    return 0;
  }
	
	
	/**
	 * Copies the source file to the given destination with the same filename.
	 * 
	 * @param srcFile The file to copy.
	 * @param dstFile The destination (where to copy the source file).
	 * @param dst The path to Dir B.
	 * @param howManyBackups The number of backups to keep.
	 * @param backupDir the directory in which the backups are made
	 * @param verify whether to verify the copied file
	 * @param writeTimeStampBack whether to write the timestamp of the dest file back to the source file 
	 * @throws IOException
	 * @throws WarningException
	 * @throws ErrorException
	 */
	public static void copy(File srcFile, File dstFile, String dst, int howManyBackups, String backupDir, boolean verify, boolean writeTimeStampBack, boolean syncParentDirTimeStamps, boolean copyDosAttributes, boolean copyPosixPermissions, boolean copyPosixGroupAndOwner, boolean overrideReadOnly) throws WarningException, ErrorException{

		// first test for common errors:

		// does source file exist ?
		if (!srcFile.exists()) {
			throw new WarningException("Source file not found: " + srcFile.getAbsolutePath());
		}

		// is source file really a file ?
		if (!srcFile.isFile()) {
			throw new WarningException("Source isn't a file: " + srcFile.getAbsolutePath());
		}

		// can the source file be read ?
		if (!srcFile.canRead()) {
			throw new WarningException("Source file cannot be read: " + srcFile.getAbsolutePath());
		}

		// create the destination directory if necessary
		if ((dstFile.getParentFile() != null) && (!dstFile.getParentFile().isDirectory())) {
			dstFile.getParentFile().mkdirs();
		}

		if (dstFile.exists()) { // does the destination file already exist?

			// can the existing destination file be overwritten ?
			if (!dstFile.canWrite()) {
				if (overrideReadOnly){
					dstFile.setWritable(true);
				}else{
					throw new WarningException("Destination file cannot be overwritten: " + dstFile.getAbsolutePath());
				}
			} else {
				if (howManyBackups > 0) {
					createBackup(dstFile, dst, howManyBackups, backupDir);
				}
			}
			
		} else {

			// can the nonexistent destination file be created?
			try {
				dstFile.createNewFile();
			} catch (Exception e) {
				throw new WarningException("Destination file cannot be created: " + dstFile.getAbsolutePath());
			}
		}

		// everything ok: copy
		
		try {

	    	if (DirSyncPro.isGuiMode()){
				DirSyncPro.getGui().registerProgressBars(-1, -1, 0, "", false, -1, -1, srcFile.getAbsolutePath(), false);
			}
	    	
	    	//get Path objects
	    	Path srcPath = Paths.get(srcFile.toURI()), dstPath = Paths.get(dstFile.toURI());
	    	//get appropriate J7Copier object and copy
	    	J7CopierFactory.getCopier(srcPath, dstPath).copy(srcPath, dstPath, copyDosAttributes, copyPosixPermissions, copyPosixGroupAndOwner);

			if (syncParentDirTimeStamps){
				Path srcParent = srcPath.getParent(); 
				Path dstParent = dstPath.getParent();
				if (!isRoot(srcParent) && !isRoot(dstParent)){
					copyTimestamp(srcParent, dstParent);
				}
			}
			
		} catch (IOException ioe) {
			System.out.println(ioe.getStackTrace());
			throw new WarningException("I/O error: could not copy: " + srcFile.getAbsoluteFile());
			
		} catch(SecurityException e) {
			throw new WarningException("A SecurityException occured! Could not copy the file: " + srcFile.getAbsolutePath());
			
		} finally {
			if (verify){
				if (!checksumIdentical(srcFile, dstFile)){
					throw new WarningException("Verify failed: " + srcFile.getAbsoluteFile() + ", " + dstFile.getAbsoluteFile());
				}
			}
			
			if (writeTimeStampBack){
				// copy destination file modification date back to the
				// source file; some platforms round or truncate the file
				// modification date and the important point is, that the
				// timestamps are equal so the file isn't copied again by
				// the "copy modified" option.
					copyTimestamp(dstFile.toPath(), srcFile.toPath());
			}else{
				try{
					dstFile.setLastModified(srcFile.lastModified());
				} catch (Exception e){
					throw new WarningException("Unable to set the modification time for the file: " + dstFile.getAbsoluteFile());
				}
			}
		}
	}

	/**
	 * Create Symbolic Link
	 * @param source source link
	 * @param dest destination link
	 * @throws WarningException if failed to copy the sym link
	 */
	public static void copySymLink(Path source, Path dest) throws WarningException{
		try{
			Path target = Files.readSymbolicLink(source);
			Files.createSymbolicLink(dest, target);
		}catch(IOException | UnsupportedOperationException x){
			throw new WarningException("Unable to copy symbolic link: " + x.getMessage() + ": " + source.toAbsolutePath());
		}
	}
		
	/**
	 * Sets the modification time of dstFile according to srcFile
	 * @param srcFile of which the modification time is copied.
	 * @param dstFile to which the modification time is copied.
	 */
	public static void copyTimestamp(Path srcPath, Path dstPath) throws WarningException{
		try {
			J7CopierFactory.copyFileAttributes(srcPath.getFileSystem().provider(), dstPath.getFileSystem().provider(),
					srcPath, dstPath, false, false, false);
		} catch(IOException e) {
			throw new WarningException("Unable to set copy file times: " + srcPath + " to " + dstPath);
		}
	}

	
	/**
	 * Sets the modification time of the parent of dstFile according to the parent of srcFile
	 * @param srcFile of which the parent modification time is copied.
	 * @param dstFile to which the parent modification time is copied.
	 */
	public static void copyParentTimestamp(File srcFile, File dstFile) throws WarningException{
		Path fAParent = srcFile.getParentFile().toPath();
		Path fBParent = dstFile.getParentFile().toPath();
		if (!FileTools.isRoot(fAParent) && !FileTools.isRoot(fBParent)){
			copyTimestamp(fAParent, fBParent);
		}

	}

	/**
	 * Compares two files.
	 * 
	 * @param srcFile
	 *            The source file to compare.
	 * @param dstFile
	 *            The destination file to compare.
	 * @return <code>true</code> if the checksums match.
	 */
	public static boolean checksumIdentical(File srcFile, File dstFile) throws WarningException{
		return checksum(srcFile) == checksum(dstFile);
	}

	/**
	 * Calculates the checksum of a file.
	 * 
	 * @param file
	 *            The file which checksum shall be calculated.
	 * @return The checksum.
	 */
	public static long checksum(File file) throws WarningException{
		// The input stream
		FileInputStream in = null;
		// The checksum
		Checksum checksum = null;

		try {

			in = new FileInputStream(file);
			checksum = new CRC32();

			int bytes_read; 

			while ((bytes_read = in.read(buffers)) != -1) {
				checksum.update(buffers, 0, bytes_read);
			}

		} catch (IOException e) {
			//throw new Error(e.getMessage());
			throw new WarningException("Could not open input stream to verify the file '" + file.getAbsolutePath() + "' (probably because access to the file is denied)");
		} finally {
			if (in != null) {
				try {
					in.close(); // always close streams.
				} catch (IOException e) {
				}
			}
		}

		return checksum.getValue();
	}

	/**
	 * Removes all symbolic links from the given array.
	 * 
	 * @param filesAndLinks
	 *            A list of files and links.
	 * @return File[] A list of files only (links have been removed).
	 */
	public static File[] removeLinks(File[] filesAndLinks) {
		List files = new ArrayList(Arrays.asList(filesAndLinks));

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			if (Files.isSymbolicLink(file.toPath())) {
				iter.remove();
			}
		}
		return (File[]) files.toArray(new File[files.size()]);
	}

	/**
	 * Deletes a file and creates a backup if necessary.
	 * 
	 * @param file The file to delete.
	 * @param dst The path to the destination directory.
	 * @param howManyBackups The number of backups to keep. 
	 * @param backupDir the directory in which the backups are made
	 * @return <code>true</code> if the deletion could be completed,
	 *         <code>false</code> if an error occoured.
	 * @throws WarningException 
	 * @throws ErrorException 
	 *         
	 */
	public static boolean deleteFile(File file, String dst, int howManyBackups, String backupDir, boolean overrideReadOnly) throws WarningException, ErrorException {
		if (!file.canWrite() && overrideReadOnly){
			file.setWritable(true);
		}
		if (howManyBackups > 0 && !file.isDirectory()) {
			createBackup(file, dst, howManyBackups, backupDir);
		}	
		return file.delete();
	}

	/**
	 * Deletes a directory with contained files and subdirectories.
	 * 
	 * @param dir The directory to delete.
	 * @param dst The path to the destination directory.
	 * @param howManyBackups The number of backups to keep.
	 * @param backupDir the directory in which the backups are made 
	 * @return <code>true</code> if the deletion could be completed,
	 *         <code>false</code> if an error occoured.
	 * @throws WarningException 
	 * @throws ErrorException 
	 */
	public static boolean deleteRecursive(File f, String dst, int howManyBackups, String backupDir, boolean overrideReadOnly) throws WarningException, ErrorException {
		boolean result = true;
		if (f.isDirectory()) {
			File[] filesAndDirs = f.listFiles();
			if (filesAndDirs != null){
				for (int i = 0; i < filesAndDirs.length; i++) {
					result &= deleteRecursive(filesAndDirs[i], dst, howManyBackups, backupDir, overrideReadOnly);
				}
			}
		}else if (howManyBackups > 0) {
			createBackup(f, dst, howManyBackups, backupDir);
		}
		if (!f.canWrite() && overrideReadOnly){
			f.setWritable(true);
		}
		return result && f.delete();
	}
	
	/**
	 * Returns only the path portion of a full filename (consisting of path, filename, and extension).
	 * @param filename The full filename.
	 * @return The path.
	 */
	public static String getOnlyPath(String filename) {
		if (filename == null || filename.equals("")) {
			return "";
		}
		return filename.substring(0, filename.lastIndexOf(File.separator));
	}

	/**
	 * Returns only the filename portion of a full filename (consisting of path, filename, and extension).
	 * @param filename The full filename.
	 * @return The filename.
	 */
	public static String getOnlyFilename(String filename) {
		if (filename == null || filename.equals("")) {
			return "";
		}
		filename = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
		if (filename.lastIndexOf('.') == -1) {
			return filename;
		}
		return filename.substring(0, filename.lastIndexOf('.'));
	}

	/**
	 * Returns only the extension portion of a full filename (consisting of path, filename, and extension).
	 * @param filename The full filename.
	 * @return The extension.
	 */
	public static String getOnlyExtension(String filename) {
		if (filename == null || filename.equals("")) {
			return "";
		}
		if (filename.lastIndexOf('.') == -1) {
			return "";
		}
		return filename.substring(filename.lastIndexOf('.') + 1, filename.length()).toLowerCase();
	}

	/**
	 * Ensures that the given path ends with a file separator.
	 * @param path The path ending or not ending with a file separator.
	 * @return The path ending with a file separator.
	 */
	public static String ensurePathEndsWithSeparator(String path) {
		if (path == null) {
			return "";
		}
	
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		
		return path;
	}
	
	/**
	 * Check whether a path is writable; path must be a directory.
	 * @param path
	 * @return true if writable, false otherwise.
	 */
	public static boolean directoryIsWritable(String path){
		if (!path.equals("")){
			path = ensurePathEndsWithSeparator(path);
		}
		path += UUID.randomUUID().toString();
		File f = new File(path);
		if (f.mkdir()){
			f.delete();
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Specifies the backup file for a given file to backup.
	 * @param file The file to backup.
	 * @param dst The path to Dir B.
	 * @param backupCount The number of this backup.
	 * @param backupDir the directory in which the backups are made
	 * @return The backup file.
	 * @throws IOException
	 */
	public static File getBackupFile(File file, String dst, int backupCount, String backupDir) throws IOException {
		String pathFilenameExtension = file.getCanonicalPath();

		String path = file.getParentFile().getCanonicalPath();
		path = path.substring(dst.length(), path.length());
		/** path is the relative path from dst to the file
		 * example: 
		 * absolutepath: d:\documents\file.txt
		 * file: file.txt
		 * path: \documents\
		 * bd: d:\SyncFolder
		 * backupfile: d:\SyncFolder\.DirSyncProBackup\documents\file00.txt 
		 */
		String filename = getOnlyFilename(pathFilenameExtension);
		String extension = getOnlyExtension(pathFilenameExtension);
		String count = new DecimalFormat("00").format(backupCount);

		String bd;
		if (backupDir.equals("")){
			bd = dst;
		}else{
			bd = backupDir;
		}
		
		return new File(ensurePathEndsWithSeparator(bd) 
				+ Const.BACKUP_FOLDER_NAME 
				+ ensurePathEndsWithSeparator(path) 
				+ filename + "_" + count + (extension.equals("")?"":"." + extension));
	}
	
	/**
	 * @param file
	 * @param dst The path to Dir B.
	 * @param howManyBackups The number of backups to keep.
	 * @param backupDir the directory in which the backups are made
	 * @throws WarningException
	 * @throws ErrorException
	 */
	public static void createBackup(File file, String dst, int howManyBackups, String backupDir) throws WarningException, ErrorException {
		try {
			// move old backups
			rotateBackups(file, dst, backupDir);
			// copy file to backup (without further backup)
			copy(file, getBackupFile(file, dst, 0, backupDir), dst, 0, backupDir, false, false, true, true, true, true, true);
			// delete not necessary backups
			deleteOldBackups(file, dst, howManyBackups, backupDir);
		} catch (IOException ioe) {
			DirSyncPro.getLog().printThrowableStack(ioe);
			throw new ErrorException("Could not create backup(s)!\n\n" + ioe);
		}
	}
	
	/**
	 * Rotates existing backups (increases their number until it reaches the maximum number of backups).
	 * 
	 * @param file The file to backup.
	 * @param dst The path to Dir B.
	 * @param backupDir the directory in which the backups are made
	 * @throws IOException
	 */
	public static void rotateBackups(File file, String dst, String backupDir) throws IOException {
		// if it exists, delete backup with maximal number
		if (getBackupFile(file, dst, Const.BACKUP_MAX_NUMBER, backupDir).exists()) {
			getBackupFile(file, dst, Const.BACKUP_MAX_NUMBER, backupDir).delete();
		}
			
		// rename other backups	
		for (int i = Const.BACKUP_MAX_NUMBER - 1; i >= 0; i--) {
			if (getBackupFile(file, dst, i, backupDir).exists()) {
				getBackupFile(file, dst, i, backupDir).renameTo(getBackupFile(file, dst, i+1, backupDir));
			}
		}
	}
	
	/**
	 * Deletes old backups (backups with a number greater than the maximum number of backups).
	 * 
	 * @param file The file to backup.
	 * @param dst The path to Dir B.
	 * @param howManyBackups The number of backups to keep.
	 * @param backupDir the directory in which the backups are made 
	 * @throws IOException
	 */
	public static void deleteOldBackups(File file, String dst, int howManyBackups, String backupDir) throws IOException {
		for (int i = howManyBackups; i <= Const.BACKUP_MAX_NUMBER; i++) {
			if (getBackupFile(file, dst, i, backupDir).exists()) {
				getBackupFile(file, dst, i, backupDir).delete();
			}
		}
	}

	/**
	 * replaces all illegal characters in the given filename with '_' char
	 * @param filename the input filename
	 * @return the filename inclusive replacements
	 */
	public static String replaceIllegalCharactersInFileName(String filename){
		// illegal chars
		// MS Windows ? [ ] / \ = + < > : ; " , *
		// Mac :
		// Linux /
		String s = filename.replaceAll("[\\?\\/\\\\=\\+\\*<>:;\\\",\\[\\]]", "_");
		return s;
	}

	/**
	 * Reads a file and returns its content.
	 * @param filename
	 * @return
	 */
	public static String readFile(String filename){
		String lines = "";
		BufferedReader br;
		try{
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null){
				lines += line + System.getProperty("line.separator");
			}
			br.close();
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
		return lines;
	}
	
	/**
	 * Writes a string to a file
	 * @param filename
	 * @param content
	 */
	public static void writeFile(String filename, String content){
		BufferedWriter bw;
		try{
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			bw.write(content);
			bw.close();
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static boolean isRoot(Path p){
		return (p.toString().equals(p.getRoot().toString()));
	}
}
