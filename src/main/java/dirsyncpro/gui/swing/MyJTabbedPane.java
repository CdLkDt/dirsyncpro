/*
 * MyJTabbedPane.java
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

package dirsyncpro.gui.swing;

import java.awt.Component;
import java.awt.KeyboardFocusManager;

import javax.swing.JTabbedPane;

public class MyJTabbedPane extends JTabbedPane{
	
	public void setSelectedIndex(int index) {
		Component component = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

		//switch only if (no tab is selected) || (the requester is me) || (other components yields) 
		if(getSelectedIndex()==-1 || component == this || this.requestFocusInWindow(false)) {
			super.setSelectedIndex(index);
		}
	}
	
	public void forceSetSelectedIndex(int index){
		super.setSelectedIndex(index);
	}
}
