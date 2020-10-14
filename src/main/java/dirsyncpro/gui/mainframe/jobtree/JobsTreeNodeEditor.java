/*
 * JobsTreeNodeEditor.java
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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

import dirsyncpro.DirSyncPro;
import dirsyncpro.job.Job;

public class JobsTreeNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	JobsTreeCellRenderer jobsTreeCellrenderer = new JobsTreeCellRenderer();

	ChangeEvent changeEvent = null;

	JTree tree;

	public JobsTreeNodeEditor(JTree t) {
		tree = t;
	}

	public Object getCellEditorValue() {
		//dummy
		return null;
	}

	public boolean isCellEditable(EventObject event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath selectedPath = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (selectedPath != null) {
				Object node = selectedPath.getLastPathComponent();
				if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
					DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) node;
					if (mutableNode.getLevel() != 0){
						JobTree.Type noteType = ((JobTree) mutableNode).type;
						int checkBoxWidth = new JCheckBox().getPreferredSize().width;
						if (noteType == JobTree.Type.Job){
								if (mouseEvent.getX() <= tree.getPathBounds(selectedPath).x + checkBoxWidth && DirSyncPro.getGui().isGuiEnabled()){
									Job job = (Job) mutableNode.getUserObject();
									job.setEnabled(!job.isEnabled());
								}else if (mouseEvent.getClickCount() == 2 && DirSyncPro.getGui().isGuiEnabled()){
									DirSyncPro.getGui().openEditJobDialog();
								}
						}else if (mouseEvent.getClickCount() == 2 && DirSyncPro.getGui().isGuiEnabled()){
							DirSyncPro.getGui().openEditJobDialog();
						}
					}
				}
			}
		}
		return true;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {

		Component editorComponent = jobsTreeCellrenderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (stopCellEditing()) {
					fireEditingStopped();
				}
			}
		};
		
		if (editorComponent instanceof JCheckBox) {
			((JCheckBox) editorComponent).addItemListener(itemListener);
		}

		return editorComponent;
	}
}