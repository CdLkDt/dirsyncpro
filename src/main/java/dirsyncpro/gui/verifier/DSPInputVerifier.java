/*
 * DSPInputVerifier.java
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

package dirsyncpro.gui.verifier;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import dirsyncpro.Const;

public class DSPInputVerifier extends InputVerifier implements KeyListener, MouseListener {
	JDialog popup;
	protected String errorStr;
	JLabel jText;
	
	public DSPInputVerifier(JDialog parentDialog, JComponent component, String eStr){
		super();
		errorStr = eStr;
		component.addKeyListener(this);
		component.addMouseListener(this);
		popup = new JDialog(parentDialog);
		popup.getContentPane().setLayout(new FlowLayout());
        popup.setUndecorated(true);
        popup.getContentPane().setBackground(Color.PINK);
        popup.getContentPane().add(new JLabel(Const.IconKey.Error.getIcon()));
        jText = new JLabel(errorStr);
        popup.getContentPane().add(jText);
        popup.setFocusableWindowState(false);
	}
	
	public boolean verify(JComponent component) {
		return true;
    }

	protected void setErrorStr(String eStr){
	    jText.setText(eStr);
	}
	
	protected void setOriginalColor(JComponent component){
		if (component.isEnabled()){
			component.setBackground(UIManager.getColor("TextField.background"));
		}else{
			component.setBackground(UIManager.getColor("TextField.inactiveBackground"));
		}
	}
	
	protected String getErrorString(){
	    return errorStr;
	}
	
	public void keyPressed(KeyEvent e) {
        popup.setVisible(false);
        setOriginalColor(((JTextField) e.getComponent()));
    }

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
	
	public void mouseClicked(MouseEvent e){
		popup.setVisible(false);
        setOriginalColor(((JTextField) e.getComponent()));
	}
	
	public void mouseEntered(MouseEvent e){
	}

	public void mouseExited(MouseEvent e){
	}

	public void mousePressed(MouseEvent e){
		popup.setVisible(false);
        setOriginalColor(((JTextField) e.getComponent()));
	}

	public void mouseReleased(MouseEvent e){
	}
}
