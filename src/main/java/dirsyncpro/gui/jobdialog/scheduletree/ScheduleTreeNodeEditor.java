/*
 * ScheduleTreeNodeEditor.java
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
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleDaily;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleHourly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMinutely;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMonthly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleOnce;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleWeekly;

public class ScheduleTreeNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	ScheduleTreeCellRenderer scheduleTreeCellrenderer = new ScheduleTreeCellRenderer();

	ChangeEvent changeEvent = null;

	JTree tree;

	public ScheduleTreeNodeEditor(JTree t) {
		tree = t;
	}

	public Object getCellEditorValue() {
		//dummy
		return null;
	}

	public boolean isCellEditable(EventObject event) {
		boolean editable = false;
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath selectedPath = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (selectedPath != null) {
				Object node = selectedPath.getLastPathComponent();
				if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
					DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) node;
					Schedule schedule = ((ScheduleTree) mutableNode).schedule;
					boolean enabled = (schedule instanceof ScheduleOnce) || (schedule instanceof ScheduleMinutely) || (schedule instanceof ScheduleHourly) || (schedule instanceof ScheduleDaily) || (schedule instanceof ScheduleWeekly) || (schedule instanceof ScheduleMonthly);  
					DirSyncPro.getGui().getJobDialog().enableEditRemoveScheduleButtons(enabled);
					if (mouseEvent.getClickCount() == 2 && !((ScheduleTree) mutableNode).isRoot()){
						DirSyncPro.getGui().getJobDialog().openEditScheduleDialog();
					}
					editable |= (((ScheduleTree) mutableNode).isRoot());
				}
			}
		}
		return editable;
	}

	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {

		Component editorComponent = scheduleTreeCellrenderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

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