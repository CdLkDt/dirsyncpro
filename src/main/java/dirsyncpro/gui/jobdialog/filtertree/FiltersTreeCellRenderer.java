/*
 * FiltersTreeCellRenderer.java
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

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

public class FiltersTreeCellRenderer extends JCheckBox implements TreeCellRenderer{
	
	 public FiltersTreeCellRenderer(){ 
         super(); 
     }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus){ 

    	//super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
    	
    	FiltersTree filtersTree = (FiltersTree) value;
    	
		setText(filtersTree.toString());

		// icon
		setIcon(filtersTree.filter == null ? filtersTree.getRootIcon() : filtersTree.filter.getType().getIcon());
		
		this.setEnabled(filtersTree.hasValues());
		
		if (isSelected || hasFocus) {
            setBackground(UIManager.getColor("Tree.selectionBackground"));
            setForeground(UIManager.getColor("Tree.selectionForeground"));
        }else {
            this.setBackground(UIManager.getColor("Tree.textBackground"));
            setForeground(UIManager.getColor("Tree.textForeground"));
        }

		return this;
	} 
}
