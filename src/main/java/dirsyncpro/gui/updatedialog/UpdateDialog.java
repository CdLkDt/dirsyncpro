/* GuiHandler.java
 *
 * Copyright (C) 2008-2011 O. Givi (info@dirsyncpro.org)
 * Copyright (C) 2005 T. Groetzner 
 * Copyright (C) 2003-2006, 2008 F. Gerbig
 * Copyright (C) 2002 E. Gerber
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

package dirsyncpro.gui.updatedialog;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dirsyncpro.Const;
import dirsyncpro.Const.IconKey;
import dirsyncpro.DirSyncPro;
import dirsyncpro.tools.GuiTools;
import dirsyncpro.updater.Updater;


/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings("serial")
public class UpdateDialog extends UpdateDialogObjects {
	
	private Updater u;

	public UpdateDialog(JFrame frame){
		super(frame);
	}
	
	public void checkForUpdate(boolean inBackground, Component parent){
		if (!inBackground || DirSyncPro.isCheckforUpdate()){ 
			u = new Updater(inBackground);
			if (u.contacted()){
				if (u.isUpdateable()){
					openUpdateDialog();
				}else if (! inBackground){
					JOptionPane.showMessageDialog(parent, "No program update available. You already have the most recent version!", "Update", JOptionPane.QUESTION_MESSAGE, new javax.swing.ImageIcon(getClass().getResource("/icons/update48x48.png")));
				}
			}
		}
	}

	private void openUpdateDialog(){
		localVersion.setText(Const.VERSION);
		remoteVersion.setText(u.getNewVersion());
		String c = u.getChangelogURL();
		String d = u.getUpdateURL();
		c = "<html><u><a href=\"" + c + "\">" + c + "</a></u></html>";
		d = "<html><u><a href=\"" + d + "\">" + d + "</a></u></html>";
		changelogURL.setText(c);
		downloadURL.setText(d);
		GuiTools.openDialog(this);
		DirSyncPro.getSync().getLog().printMinimal("New program update available!", IconKey.Warning);
		DirSyncPro.getGui().updateGUIEDT(false);
	}

	protected void downloadURLClicked(){
		u.openDownloadURLinBrowser();
	}
	
	protected void changelogURLClicked(){
		u.openChangelogURLinBrowser();
	}
	
	protected void openBrowser(){
		u.openDownloadURLinBrowser();
	}
	
}
