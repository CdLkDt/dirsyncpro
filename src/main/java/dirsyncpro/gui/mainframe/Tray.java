/*
 * Tray.java
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

package dirsyncpro.gui.mainframe;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;

public class Tray {
	TrayIcon trayIcon;
	boolean initialized = true;

	public Tray(){
		if (SystemTray.isSupported()) {
	
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/DirSyncPro.png")); 
	
			MouseListener mouseListener = new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent mouseEvent) {
					if (
							(mouseEvent.getClickCount() == 2 && !DirSyncPro.isSingleClickSystemTray()) ||
							(mouseEvent.getClickCount() == 1 && DirSyncPro.isSingleClickSystemTray())
							){
						DirSyncPro.getGui().makeVisible();
					}
				}
	
				@Override
				public void mouseEntered(MouseEvent mouseEvent) {
				}
	
				@Override
				public void mouseExited(MouseEvent mouseEvent) {
				}
	
				@Override
				public void mousePressed(MouseEvent mouseEvent) {
				}
	
				@Override
				public void mouseReleased(MouseEvent mouseEvent) {
				}
			};
	
			ActionListener exitListener = new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					DirSyncPro.getGui().quit();
				}
			};
	
			PopupMenu popup = new PopupMenu();
			MenuItem menuItem = new MenuItem("Open");
			menuItem.addActionListener(new ActionListener() {
	        		public void actionPerformed(ActionEvent e) {
	        		    DirSyncPro.getGui().makeVisible();
	        		}
			});
			popup.add(menuItem);
	        		
			menuItem = new MenuItem("Exit");
			menuItem.addActionListener(exitListener);
			popup.add(menuItem);
	
			trayIcon = new TrayIcon(image, Const.PROGRAM, popup);
	
			trayIcon.setImageAutoSize(true);
			trayIcon.addMouseListener(mouseListener);
	
			try {
				tray.add(trayIcon);
				initialized = true;
			} catch (AWTException e) {
				initialized = false;
				System.err.println("DirSyncPro TrayIcon could not be added.");
			}
	
		} else {
			//  System Tray unsupported
			initialized = false;
		}
	}
	
	public void displayInfo(String caption, String str){
		trayIcon.displayMessage(caption, str, TrayIcon.MessageType.INFO);
	}
	
	public void removeTrayIcon(){
		SystemTray.getSystemTray().remove(trayIcon);
	}

	public boolean isInitialized() {
		return initialized;
	}
}
