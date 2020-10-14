/*
 * JobsTree.java
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

package dirsyncpro.gui.mainframe.jobtree;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import dirsyncpro.Const;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.job.Job;

public class JobTree extends DefaultMutableTreeNode{
	public enum Type {
		Root("/icons/DirSyncPro.png"),
		Job("/icons/name.png"),
		Path("/icons/dirBlue.png"),
		Source("/icons/dirGreen.png"),
		Destination("/icons/dirOrange.png"),
		Mode("/icons/syncModeBI16x16.png"),
		
		Filters("/icons/filter.png"),
		FilterByPattern("/icons/pattern.png"),
		FilterByFileSize("/icons/smaller.png"),
		FilterByDate("/icons/calendar.png"),
		FilterByPath("/icons/dirBlue.png"),
		FilterByDOSAttributes("/icons/file.png"),
		FilterByFileOwnership("/icons/group.png"),
		FilterByFilePermissions("/icons/permissions.png"),
		
		FileSizeSmallerInclude("/icons/smaller.png"),
		FileSizeExactlyInclude("/icons/sameSize.png"),
		FileSizeLargerInclude("/icons/larger.png"),
		FileSizeSmallerExclude("/icons/smaller.png"),
		FileSizeExactlyExclude("/icons/sameSize.png"),
		FileSizeLargerExclude("/icons/larger.png"),

		SyncMode("/icons/syncModeBI16x16.png"),
		CopyOptions("/icons/copyAll.png"),
		ConflictResolution("/icons/conflict_icon.png"),
		WithSubFolders("/icons/withSubdirs.png"),
		Verify("/icons/verify.png"),
		Preserves("/icons/permissions.png"),
		OverrideReadOnly("/icons/link.png"),
		RealtimeSync("/icons/icon_sync.png"),
		Delete("/icons/deleteDir.png"),
		SymLink("/icons/deleteDir.png"),
		Granularity("/icons/timestampDiff.png"),
		WriteTimeStampsBack("/icons/writeTimestampBack.png"),
		DirTimeStamps("/icons/timestampDiffDir.png"),
		IgnoreDayLightSavingGranularity("/icons/timestamp.png"),
		Log("/icons/log.png"),
		
		Schedule("/icons/calendar.png"),
		ScheduleItem("/icons/calendar.png");
		
		private Icon icon;
		public Icon getIcon(){
			return icon;
		}
		private Type(String iconFile){
			this.icon = new ImageIcon(Const.class.getResource(iconFile));
		}
	};
	public Type type;
	public Object value;
	public JobTree parent;
//	public ArrayList<JobTree> children = new ArrayList<JobTree>();
	
	public JobTree(Type t, Object v, JobTree p){
		type = t;
		value = v;
		setUserObject(v);
		parent = p;
		if (type != Type.Root){
			p.add(this);
		}
	}
	
	public String toString(){
		if (type == Type.Job){
			return value.toString();
		}else if (type == Type.Path){
			return "<html><b>Paths:</b> Source:'" + ((Job) value).getDirA() + "', Destination:'" + ((Job) value).getDirB() + "'</html>";
		}else if (type == Type.Source){
			return "<html><b>Source:</b> '" + ((Job) value).getDirA() + "'</html>";
		}else if (type == Type.Destination){
			return "<html><b>Destination:</b> '" + ((Job) value).getDirB() + "'</html>";
		}else if (type == Type.Mode){
			Job job = (Job) value;
			String txt = "<html><b>Mode:</b> " + job.getSyncMode().toString().replace("<html>", "").replace("</html>", "") + ", Copy Options: ";
			if (job.isCopyAll()){
				txt += Const.CopyMode.All + "+";
			}
			if (job.isCopyNew()){
				txt += Const.CopyMode.New + "+";
			}
			if (job.isCopyModified()){
				txt += Const.CopyMode.Modified + "+";
			}
			if (job.isCopyLarger()){
				txt += Const.CopyMode.Larger + "+";
			}
			if (job.isCopyLargerModified()){
				txt += Const.CopyMode.LargerAndModified + "+";
			}
			txt = txt.replaceAll("\\+$", "");
			txt = txt.replaceAll(": $", ": None");
			txt += ", Traverse Subfolders: ";
			if (job.isRecursive()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += ", Verify: ";
			if (job.isVerify()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += ", Deletion: ";
			if (job.isDelFiles()){
				txt += "Files+";
			}
			if (job.isDelDirs()){
				txt += "Dirs+";
			}
			
			if (job.isDeleteExcludedFilesA() || job.isDeleteExcludedFilesB()){
				txt += "Excluded Files in Dir ";
				if (job.isDeleteExcludedFilesA()){
					txt += "A&";
				}
				if (job.isDeleteExcludedFilesB()){
					txt += "B+";
				}
			}
			
			if (job.isDeleteExcludedDirsA() || job.isDeleteExcludedDirsB()){
				txt += "Excluded Dirs in Dir ";
				if (job.isDeleteExcludedDirsA()){
					txt += "A&";
				}
				if (job.isDeleteExcludedDirsB()){
					txt += "B+";
				}
			}

			txt = txt.replaceAll("\\+$", "");
			txt = txt.replaceAll(": $", ": None");
			txt += "</html>";
			return txt;
		}else if (type == Type.SyncMode){
			return "<html><b>Sync Mode:</b> " + ((Job) value).getSyncMode() + "</html>";
		}else if (type == Type.CopyOptions){
			Job job = (Job) value;
			String txt = "<html><b>Copy Options:</b> ";
			if (job.isCopyAll()){
				txt += Const.CopyMode.All + "+";
			}
			if (job.isCopyNew()){
				txt += Const.CopyMode.New + "+";
			}
			if (job.isCopyModified()){
				txt += Const.CopyMode.Modified + "+";
			}
			if (job.isCopyLarger()){
				txt += Const.CopyMode.Larger + "+";
			}
			if (job.isCopyLargerModified()){
				txt += Const.CopyMode.LargerAndModified + "+";
			}
			txt = txt.replaceAll("\\+$", "");
			txt += "</html>";
			return txt;
		}else if (type == Type.ConflictResolution){
			return "<html><b>Conflict Resolution:</b> " + ((Job) value).getSyncConflictResolutionMode().toString() + "</html>";
		}else if (type == Type.WithSubFolders){
			Job job = (Job) value;
			String txt = "<html><b>Traverse subfolders:</b> ";
			if (job.isRecursive()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.Verify){
			Job job = (Job) value;
			String txt = "<html><b>Verify after copy:</b> ";
			if (job.isVerify()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.Preserves){
			Job job = (Job) value;
			String txt = "<html><b>Preserve:</b> ";
			boolean flag = false;
			if (job.isPreserveDOSAttributes()){
				txt += "DOS attributes";
				flag = true;
			}
			if (job.isPreservePOSIXFilePermissions()){
				txt += ", POSIX file permissions";
				flag = true;
			}
			if (job.isPreservePOSIXFileOwnership()){
				txt += ", POSIX file ownerships";
				flag = true;
			}

			if (!flag){
				txt += "No file metadata";
			}else{
				txt = txt.replace(":</b> , ", ":</b> ");
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.OverrideReadOnly){
			Job job = (Job) value;
			String txt = "<html><b>Override Read-Only Attribute:</b> ";
			if (job.isOverrideReadOnly()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.RealtimeSync){
			Job job = (Job) value;
			String txt = "<html><b>Realtime Sync:</b> ";
			if (job.isSyncRealtime()){
				txt += "Yes, Delay: " + job.getSyncRealtimeDelay() + ", Sync on start: " + (job.isSyncRealtimeOnStart() ? "Yes" : "");
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.Granularity){
			Job job = (Job) value;
			String txt = "<html><b>Timestamp granularity:</b> ";
			txt += job.getGranularity();
			txt += " second(s)</html>";
			return txt;
		}else if (type == Type.WriteTimeStampsBack){
			Job job = (Job) value;
			String txt = "<html><b>Write timestamp back to source:</b> ";
			if (job.isWriteTimestampBack()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.IgnoreDayLightSavingGranularity){
			Job job = (Job) value;
			String txt = "<html><b>Ignore daylight saving granularity:</b> ";
			if (job.isIgnoreDaylightSavingGranularity()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.DirTimeStamps){
			Job job = (Job) value;
			String txt = "<html><b>Synchronize directory timestamps:</b> ";
			if (job.isSyncDirTimeStamps()){
				txt += "Yes";
			}else{
				txt += "No";
			}
			txt += "</html>";
			return txt;
		}else if (type == Type.Delete){
			Job job = (Job) value;
			String txt = "<html><b>Delete after sync redundant files/dirs:</b> ";
			if (job.isDelFiles()){
				txt += "Files+";
			}
			if (job.isDelDirs()){
				txt += "Dirs+";
			}
			if (job.isDeleteExcludedFilesA() || job.isDeleteExcludedFilesB()){
				txt += "Excluded Files in Dir ";
				if (job.isDeleteExcludedFilesA()){
					txt += "A&";
				}
				if (job.isDeleteExcludedFilesB()){
					txt += "B+";
				}
			}
			
			if (job.isDeleteExcludedDirsA() || job.isDeleteExcludedDirsB()){
				txt += "Excluded Dirs in Dir ";
				if (job.isDeleteExcludedDirsA()){
					txt += "A&";
				}
				if (job.isDeleteExcludedDirsB()){
					txt += "B+";
				}
			}
			txt = txt.replaceAll("\\+$", "");
			txt = txt.replaceAll(":<\\/b> $", ":<\\/b> None");
			txt += "</html>";
			return txt;
		}else if (type == Type.Filters){
			return	"<html><b>Filters:</b>" + (((Job) value).getFilterSet().hasFilters() ? " has filters" : "has no filters") + "</html>";
		}else if (type.toString().matches("FilterBy.+")){
			return	"<html><b>Filter: </b>" + ((Filter) value).getAction() + " " + ((Filter) value).toString() + "</html>";
		}else if (type == Type.Log){
			String filename = ((Job) value).getLog().getFilename();
			return	"<html><b>Job log: </b>" + (filename.equals("") ? "None" : "'" + filename + "'") + "</html>";
		}else if (type == Type.Schedule){
			return "<html><b>Schedules:</b>" + (((Job) parent.value).HasSchedules() ? " has some schedules" : " None") + "</html>"; 
		}else{
			return value.toString();
		}
	}
	
	/**
	 * Prints this JobTree.
	 */
	public void print(){
		print(this, "");
	}
	
	/**
	 * Prints a JobTree
	 * @param jt JobTree to print
	 * @param ident Beginning ident (mostly "").
	 */
	public void print(JobTree jt, String ident){
		if (jt.isLeaf()){
			System.out.println(ident + jt.type);
		}else{
			System.out.print(ident + jt.type);
			if (jt.type.equals(JobTree.Type.Job)){
				System.out.println("(" + jt.value + ")");
			}else{
				System.out.println();
			}
			Enumeration children = jt.children();
			while (children.hasMoreElements()){
				JobTree ch = (JobTree) children.nextElement();
				print(ch, ident + " ");
			}
		}
	}
}