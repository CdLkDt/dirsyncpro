/*
 * SyncQTableCellRenderer.java
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

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import dirsyncpro.Const.IconKey;
import dirsyncpro.DirSyncPro;
import dirsyncpro.sync.SyncPair;
import dirsyncpro.tools.TextFormatTool;

public class SyncQTableCellRenderer implements TableCellRenderer {

	protected JLabel cell;
	
	public SyncQTableCellRenderer(){
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		int size = DirSyncPro.getSync().getSyncQ().viewSize();
		if (row >= size || row < 0)
			return null;

		if (row >= size || row < 0)
			return null;

		SyncPair sp = DirSyncPro.getSync().getSyncQ().getFilteredView(row);

		cell = new JLabel();
		cell.setText(String.valueOf(value));
		//background color
		cell.setOpaque(true);
		if (isSelected) {
			cell.setBackground(UIManager.getColor("Tree.selectionBackground"));
            cell.setForeground(UIManager.getColor("Tree.selectionForeground"));
        }else {
    		cell.setBackground(sp.getSyncPairStatus().getColor());
            cell.setForeground(UIManager.getColor("Tree.textForeground"));
        }

		// Begin: for performance not in every case bellow
		File f = null;
		if (column <3){
			f = sp.getFileA();
		}else{
			f = sp.getFileB();
		}
		// End: for performance

		switch(column){
		case 0: //Name A
			cell.setHorizontalAlignment(JLabel.LEFT);
			cell.setIcon(sp.getIconA());
			cell.setToolTipText(f.getAbsolutePath());
			break;
		case 1: //Date A
			cell.setHorizontalAlignment(JLabel.RIGHT);
			if (f != null && sp.isFileAExists()){
				cell.setToolTipText(TextFormatTool.getLastModifiedLong(sp.getDateA()));
			}
			break;
		case 2: //Size A
			cell.setHorizontalAlignment(JLabel.RIGHT);
			if (f != null && sp.isFileAExists()){
				cell.setToolTipText(sp.getSizeA() + " Bytes");
			}
			break;
		case 3: //Mode
			cell.setHorizontalAlignment(JLabel.CENTER);
			if (sp.isSynced()){
				cell.setIcon(IconKey.Synced.getIcon());
				cell.setToolTipText(IconKey.Synced.toString());
			}else{
				cell.setIcon(sp.getSyncPairStatus().getIcon());
				cell.setToolTipText(sp.getSyncPairStatus().toString());
			}
			break;
		case 4: //Name B
			cell.setHorizontalAlignment(JLabel.LEFT);
			cell.setIcon(sp.getIconB());
			cell.setToolTipText(f.getAbsolutePath());
			break;
		case 5: //Date B
			cell.setHorizontalAlignment(JLabel.RIGHT);
			if (f != null && sp.isFileBExists()){
				cell.setToolTipText(TextFormatTool.getLastModifiedLong(sp.getDateB()));
			}
			break;
		case 6: // Size B
			cell.setHorizontalAlignment(JLabel.RIGHT);
			if (f != null && sp.isFileBExists()){
				cell.setToolTipText(sp.getSizeB() + " Bytes");
			}
			break;
		}
		
		return cell;
	}
}