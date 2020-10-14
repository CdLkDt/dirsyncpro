/*
 * ScheduleTreeModel.java
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

package dirsyncpro.gui.jobdialog.scheduletree;

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class ScheduleTreeModel extends DefaultTreeModel{
	
	private ScheduleTree scheduleTree;
	private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();


	public ScheduleTreeModel(ScheduleTree st){
		super(st);
		scheduleTree = st;
	}
	
	// the root of the tree
	public Object getRoot(){
		return scheduleTree;
	}

	// number of a node's childeren
	public int getChildCount(Object parent){
		return ((ScheduleTree) parent).getChildCount();
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
		return ((ScheduleTree) nodeObject).getChildAt(index);
	}
	
	// is it leaf?
	public boolean isLeaf(Object node){
		return (((ScheduleTree) node).isLeaf());
	}
	
	// value changed
	public void valueForPathChanged(TreePath path, Object newvalue){
	}
	
	public void reload(ScheduleTree st){
		scheduleTree = st;
		int n = getChildCount(scheduleTree);
		int[] childIdx = new int[n];
		Object[] children = new Object[n];

		for (int i = 0; i < n; i++){
			childIdx[i] = i;
			children[i] = getChild(scheduleTree, i);
		}

		fireTreeStructureChanged(this, new Object[] { scheduleTree }, childIdx, children);
	}
}
