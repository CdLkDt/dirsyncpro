/*
 * SyncQTableModel.java
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

package dirsyncpro.gui.mainframe.syncqtable;

import java.io.File;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import dirsyncpro.Const.SyncPairStatus;
import dirsyncpro.DirSyncPro;
import dirsyncpro.sync.SyncPair;
import dirsyncpro.tools.TextFormatTool;

@SuppressWarnings("serial")
public class SyncQTableModel extends AbstractTableModel {
	//private static final long serialVersionUID = 45L;
	private static final String[] columnNames = {"Dir A", "Date", "Size", "Mode", "Dir B","Date", "Size"}; 

	public SyncQTableModel(){
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
		return 7;
	}
	
	/**
	 * @see TableModel#getRowCount()
	 */
	public final int getRowCount() {
		if (DirSyncPro.getSync() != null && DirSyncPro.getSync().getSyncQ() != null){
			return DirSyncPro.getSync().getSyncQ().viewSize();
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
		
		SyncPair sp = DirSyncPro.getSync().getSyncQ().getFilteredView(row);
		File f = null;

		if (column >= 0 && column <= 2)
			f = sp.getFileA();
		else if (column >= 4 && column <= 6)
			f = sp.getFileB();

		item = "";
		if (f != null)
			switch (column) {
			case 0:
				if (sp.isFileAExists()){
					item = f.getName();
				}else if (sp.getSyncPairStatus().equals(SyncPairStatus.FileAIsRedundant) || sp.getSyncPairStatus().equals(SyncPairStatus.DirAIsRedundant)){
					item = "<html><strike>" + f.getName() + "</strike></html>";
				}else if (sp.isSynced() && sp.getSyncPairStatus().equals(SyncPairStatus.ConflictResolutionRename)){
					item = "<html><i>" + f.getName() + "</i></html>";
				}
				break;
			case 4:
				if (sp.isFileBExists()){
					item = f.getName();
				}else if (sp.getSyncPairStatus().equals(SyncPairStatus.FileBIsRedundant) || sp.getSyncPairStatus().equals(SyncPairStatus.DirBIsRedundant)){
					item = "<html><strike>" + f.getName() + "</strike></html>";
				}else if (sp.isSynced() && sp.getSyncPairStatus().equals(SyncPairStatus.ConflictResolutionRename)){
					item = "<html><i>" + f.getName() + "</i></html>";
				}
				break;
			case 1:
				if (sp.isFileAExists()){
					item = TextFormatTool.getLastModifiedShort(sp.getDateA());
				}else if (sp.isSynced() && sp.getSyncPairStatus().equals(SyncPairStatus.ConflictResolutionRename)){
					item = "<html><i>" + TextFormatTool.getLastModifiedShort(sp.getDateA()) + "</i></html>";
				}
				break;
			case 5:
				if (sp.isFileBExists()){
					item = TextFormatTool.getLastModifiedShort(sp.getDateB());
				}else if (sp.isSynced() && sp.getSyncPairStatus().equals(SyncPairStatus.ConflictResolutionRename)){
					item = "<html><i>" + TextFormatTool.getLastModifiedShort(sp.getDateB()) + "</i></html>";
				}
				break;
			case 2:
				if (sp.isFileAExists()){
					item = TextFormatTool.getHumanReadable(sp.getSizeA());
				}
				break;
			case 6:
				if (sp.isFileBExists()){
					item = TextFormatTool.getHumanReadable(sp.getSizeB());
				}
				break;
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
