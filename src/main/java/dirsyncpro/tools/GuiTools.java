/*
 * GuiTools.java
 * 
 * Copyright (C) 2011 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.tools;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Contains methods to manipulate arrays
 * 
 * @author O. Givi (info@dirsyncpro.org)
 */

public class GuiTools {
	
	
	public static class MyStyleSheet extends StyleSheet{
		private StyleSheet styles;
		HTMLEditorKit editorKit = new HTMLEditorKit();
		public MyStyleSheet(){
			super();
			styles = new StyleSheet();
	    	Font font = UIManager.getFont("Label.font");
	    	String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
	    	styles.addRule(bodyRule);
	    	editorKit.setStyleSheet(styles);
		}
		
		public EditorKit getEditorKit(){
			return editorKit;
		}
	};
	
	private static EditorKit editorKit = (new MyStyleSheet()).getEditorKit();

	public static void collapseAll(JTree jt){
		// for loop should go backwards because expanding affects getRowCount()
		// row 0 is the root; don't collapse that.
		for (int i = jt.getRowCount()-1; i>0; i--) {
	        jt.collapseRow(i);
		}
	}
	
	public static void expandOneLevel(JTree jt){
		// for loop should go backwards because expanding affects getRowCount()
		for (int i = jt.getRowCount()-1; i>=0; i--) {
	        jt.expandRow(i);
		}
	}
	
	public static void expandAll(JTree jt){
		for (int i = 0; i< jt.getRowCount(); i++) {
	        jt.expandRow(i);
		}
	}
	
	public static void openDialog(JDialog dialog){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// calculate the size of the dialog first
		dialog.pack();
		int width = dialog.getSize().width;
		int height = dialog.getSize().height;
		int x = (dialog.getParent().getWidth() - width)/2 + dialog.getParent().getX();
		int y = (dialog.getParent().getHeight() - height)/2 + dialog.getParent().getY();
		dialog.setBounds(x, y, width, height);
		//based on the screen size
		//dialog.setBounds((screenSize.width-dialog.getSize().width)/2, (screenSize.height-dialog.getSize().height)/2, dialog.getSize().width, dialog.getSize().height);
		dialog.setVisible(true);
	}
	
	public static void browseFolder(JDialog parent, JTextField jtf){
		browseFileAndFolder(parent, jtf, JFileChooser.DIRECTORIES_ONLY);
	}
	
	public static void browseFile(JDialog parent, JTextField jtf){
		browseFileAndFolder(parent, jtf, JFileChooser.FILES_ONLY);
	}
	
	public static void browseFileAndFolder(JDialog parent, JTextField jtf){
		browseFileAndFolder(parent, jtf, JFileChooser.FILES_AND_DIRECTORIES);
	}

	private static void browseFileAndFolder(JDialog parent, JTextField jtf, int mode) {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(mode);

			if (!jtf.getText().equals("")) {
				fileChooser.setCurrentDirectory(new File(jtf.getText()));
			}

			if (mode == JFileChooser.DIRECTORIES_ONLY){
				fileChooser.setApproveButtonText("Select");
				fileChooser.setApproveButtonMnemonic('s');
				fileChooser.setApproveButtonToolTipText("Select directory");
			}

			if (fileChooser.showOpenDialog(parent) == javax.swing.JFileChooser.APPROVE_OPTION) {
				jtf.setText(fileChooser.getSelectedFile().getPath());
			}

		} catch (Exception e) {
			throw new Error(e);
		}
	}
	
	public static EditorKit getDefaultEditorKit(){
		return editorKit;
	}
	
	public static void setSystemLookAndFeel(boolean isSystemLookAndFeel){
		
		try {
			if (isSystemLookAndFeel) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			}
		} catch (Exception e) {
		}
	}

}
