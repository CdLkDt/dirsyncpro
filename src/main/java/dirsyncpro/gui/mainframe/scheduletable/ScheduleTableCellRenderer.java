/*
 * ScheduleTableCellRenderer.java
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

package dirsyncpro.gui.mainframe.scheduletable;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import dirsyncpro.Const.IconKey;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;

public class ScheduleTableCellRenderer implements TableCellRenderer {

	protected JLabel cell;
	
	public ScheduleTableCellRenderer(){
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (row >= DirSyncPro.getSync().getScheduleEngine().getScheduleQ().size() || row < 0)
			return null;

		Schedule sched = DirSyncPro.getSync().getScheduleEngine().getScheduleQ().elementAt(row);

		cell = new JLabel();
		cell.setText(String.valueOf(value));

		//icon
		if (column == 0){
			cell.setIcon(sched.getType().getIcon());
		}
		
		//background color
		cell.setOpaque(true);
		if (DirSyncPro.getSync().getScheduleEngine().getSynchronizingSchedule() == sched){
			// use the same color as SyncPairStatus.Synced.color
			cell.setBackground(IconKey.Synced.getColor());
		}else{
			cell.setBackground(IconKey.Time.getColor());
		}
		return cell;
	}
}