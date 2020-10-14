/*
 * FiltersTree.java
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

package dirsyncpro.gui.jobdialog.filtertree;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import dirsyncpro.Const;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;

public class FiltersTree extends DefaultMutableTreeNode{
	public Filter filter;
	public FiltersTree parent;
	
	public FiltersTree(Filter f, FiltersTree p){
		filter = f;
		setUserObject(f);
		parent = p;
		if (p != null){
			p.add(this);
		}
	}
	
	public boolean hasValues(){
		return true;
		//TODO: next = redundant???
//		boolean hasV = false;
//		if (this.type.equals(Type.IncludeFile) || this.type.equals(Type.IncludeDir) || this.type.equals(Type.ExcludeFile) || this.type.equals(Type.ExcludeDir)|| this.type.equals(Type.FileSizeSmaller) || this.type.equals(Type.FileSizeExactly) || this.type.equals(Type.FileSizeLarger)){
//			hasV = true;
//		}else{
//			for (Enumeration e = this.children(); e.hasMoreElements();) {
//				hasV |= ((FiltersTree) e.nextElement()).hasValues();
//		    }
//		}
//		return hasV;
	}
	
	public String toString(){
		if (parent == null){
			return "Filters";
		}else{
			return "<html>" + filter.toString() + "</html>";
		}
	}
	
	public Icon getRootIcon(){
		return new ImageIcon(Const.class.getResource("/icons/filter.png"));
	}
}