/*
 * FiltersTreeModel.java
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

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FiltersTreeModel extends DefaultTreeModel{
	
	private FiltersTree filtersTree;
	private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();


	public FiltersTreeModel(FiltersTree ft){
		super(ft);
		filtersTree = ft;
	}
	
	// the root of the tree
	public Object getRoot(){
		return filtersTree;
	}

	// number of a node's childeren
	public int getChildCount(Object parent){
		return ((FiltersTree) parent).getChildCount();
	}

	// child's position in its parent node
	public int getIndexOfChild(Object parent, Object child){
		for (int i = 0; i < getChildCount(parent); i++) {
			if (getChild(parent, i).equals(child)) { 
				return i; 
			}
		}
		return -1; 
	}

	// get the indexth child of the parent
	public Object getChild(Object nodeObject, int index){
		return ((FiltersTree) nodeObject).getChildAt(index);
	}
	
	// is it leaf?
	public boolean isLeaf(Object node){
		return (((FiltersTree) node).isLeaf());
	}
	
	// value changed
	public void valueForPathChanged(TreePath path, Object newvalue){
	}
	
	public void reload(FiltersTree ft){
		filtersTree = ft;
		int n = getChildCount(filtersTree);
		int[] childIdx = new int[n];
		Object[] children = new Object[n];

		for (int i = 0; i < n; i++){
			childIdx[i] = i;
			children[i] = getChild(filtersTree, i);
		}

		fireTreeStructureChanged(this, new Object[] { filtersTree }, childIdx, children);
	}
}
