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

package dirsyncpro.gui.licensedialog;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;

import dirsyncpro.DirSyncPro;
import dirsyncpro.tools.GuiTools;

/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings("unused")
public class LicenseDialog extends LicenseDialogObjects {
	private static final long serialVersionUID = 1L;
	protected boolean licenseAccepted;
	
	public LicenseDialog (JFrame frame){
		super(frame);
	}


	protected void licenseAccepted(){
		licenseAccepted = true;
		DirSyncPro.setLicenseAccepted();
		setVisible(false);
	}
	
	protected void licenseNotAccepted(){
		// we are in gui
		if (DirSyncPro.getGui() != null){
			DirSyncPro.getGui().removeTrayIcon();
		}
		System.exit(0);
	}

	public void openLicenseDialog(){
		String licenseText = "";
		try {
			InputStream is = getClass().getResourceAsStream("/License.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
	
			String line;
			while ((line = br.readLine()) != null) {
				licenseText += line + "\n";
			}
	}catch (Exception e){
			System.out.println("Unable to show the License. Please read License.txt.\n\n" + e);
			DirSyncPro.getLog().printThrowableStack(e);
			System.exit(1);
		}

		licenceTextArea.setText(licenseText);
		
		GuiTools.openDialog(this);
	}
}
