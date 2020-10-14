/*
 * SyncModeComboboxCellRenderer.java
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

package dirsyncpro.gui.jobdialog;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import dirsyncpro.Const;

class SyncModeComboboxCellRenderer extends JLabel implements ListCellRenderer {

      protected javax.swing.border.Border noFocusBorder;

      /**
       * A renderer to render the cells of the directory definitions table.
       */
      public SyncModeComboboxCellRenderer() {
          super();
          if (noFocusBorder == null) {
              noFocusBorder = new javax.swing.border.EmptyBorder(1, 1, 1, 1);
          }
          setOpaque(true);
          setBorder(noFocusBorder);
      }

      /**
       * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
       */
      public java.awt.Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus)
      {
          setComponentOrientation(list.getComponentOrientation());
          if (isSelected || cellHasFocus) {
              setBackground(list.getSelectionBackground());
              setForeground(list.getSelectionForeground());
          }
          else {
              setBackground(list.getBackground());
              setForeground(list.getForeground());
          }

          Const.SyncMode sm = (Const.SyncMode) value;
          setText(sm.toHTMLString());
          setIcon(sm.getIcon());
          setFont(list.getFont());
          javax.swing.border.Border border = null;
          if (cellHasFocus) {
              if (isSelected) {
                  border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
              }
              if (border == null) {
                  border = UIManager.getBorder("List.focusCellHighlightBorder");
              }
          } else {
              border = noFocusBorder;
          }
          setBorder(border);
          return this;
      }
  }
