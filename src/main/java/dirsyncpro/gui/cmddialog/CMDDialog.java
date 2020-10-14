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

package dirsyncpro.gui.cmddialog;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import dirsyncpro.Const;
import dirsyncpro.Const.FileType;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.mainframe.DSPFileFilter;
import dirsyncpro.tools.FileTools;


/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
//@SuppressWarnings("unused")
public class CMDDialog extends CMDDialogObjects {

	public CMDDialog(JFrame frame){
		super(frame);
	}
	
	public void initCMDDialog(){
		cmdAddAnalyzeCheckBox.setSelected(false);
		cmdAddSyncCheckBox.setSelected(false);
		cmdAddNoGuiCheckBox.setSelected(false);
		cmdAddQuitCheckBox.setSelected(false);
		if (Const.OS_IS_WINDOWS){
			cmdAddSyncJLabel.setText(cmdAddSyncJLabel.getText().replace("-syns", "/sync"));
			cmdAddAnalyzeJLabel.setText(cmdAddAnalyzeJLabel.getText().replace("-analyze", "/analyze"));
			cmdAddScheduleJLabel.setText(cmdAddScheduleJLabel.getText().replace("-schedule", "/schedule"));
			cmdAddNoGuiJLabel.setText(cmdAddNoGuiJLabel.getText().replace("-nogui", "/nogui"));
			cmdAddQuitJLabel.setText(cmdAddQuitJLabel.getText().replace("-quit", "/quit"));
			cmdAddQuitJLabel.setText(cmdAddQuitJLabel.getText().replace("-iconify", "/iconify"));
		}else{
			cmdAddSyncJLabel.setText(cmdAddSyncJLabel.getText().replace("/sync", "-sync"));
			cmdAddAnalyzeJLabel.setText(cmdAddAnalyzeJLabel.getText().replace("/analyze", "-analyze"));
			cmdAddScheduleJLabel.setText(cmdAddScheduleJLabel.getText().replace("/schedule", "-schedule"));
			cmdAddNoGuiJLabel.setText(cmdAddNoGuiJLabel.getText().replace("/nogui", "-nogui"));
			cmdAddQuitJLabel.setText(cmdAddQuitJLabel.getText().replace("/quit", "-quit"));
			cmdAddQuitJLabel.setText(cmdAddQuitJLabel.getText().replace("/iconify", "-iconify"));
		}
		cmdUpdateCommandField();
	}
	
	protected void cmdSaveBatch(){
		JFileChooser fileChooser = new JFileChooser();
		FileType type;
		if (Const.OS_IS_WINDOWS){
			type = FileType.CMD;
		}else{
			type = FileType.SH;
		}
		fileChooser.setFileFilter(new DSPFileFilter(type.getExtension()));
		
		File currentConfig = DirSyncPro.getGui().getCurrectConfig();
		
		if (currentConfig.exists() || JOptionPane.showConfirmDialog(this, "You have chosen to save a batch file for a config file which is not saved yet.\nRunning this batch file will cause an error.\n\nAre you sure you want to save this file?", "Config not saved yet!", JOptionPane.YES_NO_OPTION) == 0){
			File f = new File(currentConfig.getAbsolutePath().replace(FileType.DSC.getExtension(), type.getExtension()));
			fileChooser.setSelectedFile(f);
			fileChooser.setCurrentDirectory(f.getParentFile());
	
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				String filename = fileChooser.getSelectedFile().getPath();
				if (!fileChooser.getSelectedFile().exists() || (JOptionPane.showConfirmDialog(this,	"Batchfile '" + filename + "' exists.\nOverwrite?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION)){
					FileTools.writeFile(filename, cmdCommandField.getText());
					DirSyncPro.displayInfo("Batch file saved successfully!");
				}
			}
		}
	}
	
	protected void cmdCopyToClipboard(){
		StringSelection selection = new StringSelection(cmdCommandField.getText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, null);
		DirSyncPro.displayInfo("Command is copied to the system clipboard. You may paste it elswhere.");
	}
	
	protected void cmdUpdateCommandField(){
		String command = "";
		// running program path
		String path = DirSyncPro.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = new File(path).getParentFile().getPath();
		
		try{
			path = URLDecoder.decode(path, "UTF-8");
		}catch (UnsupportedEncodingException e){
			String s = "Error getting the path of the jar file!\n\n" + e;
			DirSyncPro.displayError(s);
			DirSyncPro.getLog().printThrowableStack(e);
		}

		if (!path.endsWith(File.separator)){
			path += File.separator;
		}

		if (Const.OS_IS_WINDOWS){
			String s = path + "DirSyncPro.exe"; 
			if (s.contains(" ")){
				s = "\"" + s + "\"";
			}
			command += s;
		}else{
			String s = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			if (s.contains(" ")){
				s = "\"" + s + "\"";
			}
			command += s + " -Xmx512M -jar ";
			s = path + "dirsyncpro.jar";
			if (s.contains(" ")){
				s = "\"" + s + "\"";
			}
			command += s;
		}
		
		if (cmdAddSyncCheckBox.isSelected()){
			command += " " + (Const.OS_IS_WINDOWS ? "/sync" : "-sync");
			cmdAddAnalyzeCheckBox.setEnabled(false);
		}else{
			cmdAddAnalyzeCheckBox.setEnabled(true);
			if (cmdAddAnalyzeCheckBox.isSelected()){
				command += " " + (Const.OS_IS_WINDOWS ? "/analyze" : "-analyze");
			}
		}

		if (cmdAddScheduleCheckBox.isSelected()){
			command += " " + (Const.OS_IS_WINDOWS ? "/schedule" : "-schedule");
			cmdAddNoGuiCheckBox.setEnabled(false);
			cmdAddNoGuiCheckBox.setSelected(false);
			cmdAddQuitCheckBox.setEnabled(false);
			cmdAddQuitCheckBox.setSelected(false);
		}else{
			cmdAddNoGuiCheckBox.setEnabled(true);
			cmdAddQuitCheckBox.setEnabled(true);

			if (cmdAddNoGuiCheckBox.isSelected()){
				command += " " + (Const.OS_IS_WINDOWS ? "/nogui" : "-nogui");
			}

			if (cmdAddQuitCheckBox.isSelected()){
				command += " " + (Const.OS_IS_WINDOWS ? "/quit" : "-quit");
			}
		}
		if (cmdAddIconifyCheckBox.isSelected()){
			command += " " + (Const.OS_IS_WINDOWS ? "/iconify" : "-iconify");
			cmdAddNoGuiCheckBox.setEnabled(false);
			cmdAddNoGuiCheckBox.setSelected(false);
		}
		
		
		File currentConfig = DirSyncPro.getGui().getCurrectConfig();
		String s = currentConfig.getAbsolutePath();
		if (s.contains(" ")){
			s = "\"" + s + "\"";
		}

		command += " " + s; 
		
		cmdCommandField.setText(command);
		cmdCommandField.setCaretPosition(0);
	}
}
