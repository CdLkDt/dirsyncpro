/*
 * ScheduleTableModel.java
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

import java.text.SimpleDateFormat;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;

@SuppressWarnings("serial")
public class ScheduleTableModel extends AbstractTableModel {
	//private static final long serialVersionUID = 45L;
	private static final String[] columnNames = {"Type", "Job", "Next Event", "Details"};

	public ScheduleTableModel(){
	}
	
	/**
	 * @see TableModel#getColumnName()
	 */
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }
	
	/**
	 * @see TableModel#getColumnCount()
	 */
	public final int getColumnCount() {
		return 4;
	}
	
	/**
	 * @see TableModel#getRowCount()
	 */
	public final int getRowCount() {
		if (DirSyncPro.getSync() != null && DirSyncPro.getSync().getScheduleEngine().getScheduleQ() != null){
			return DirSyncPro.getSync().getScheduleEngine().getScheduleQ().size();
		}else{
			return 0;
		}
	}

	/**
	 * @see TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * @see TableModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int column) {
		if (row < 0 || row >= getRowCount() || column < 0 || column >= getColumnCount()) 
			return null;

		String item = "";
		Schedule sched = DirSyncPro.getSync().getScheduleEngine().getScheduleQ().elementAt(row);

		switch (column){
			case 0: 
				item = sched.getType().name(); 
				break;
			case 1:
				item = sched.getJob().getName();
				break;
			case 2:
				if (sched.getNextEvent() != null){
					item = (new SimpleDateFormat(Const.DefaultDateFormat)).format(sched.getNextEvent());
				}else{
					item = "";
				}
				break;
			case 3:
				item = sched.toString().replaceFirst(", Next event.*", "");
		}
		return item;
	}

	/**
	 * @see TableModel#getColumnClass(int)
	 */
	public final Class<?> getColumnClass(int column) {
		return String.class;
	}
}
