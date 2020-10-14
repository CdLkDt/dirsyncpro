/*
 * ScreenUpdater.java
 * 
 * Copyright (C) 2010-2012 O. Givi (info@dirsyncpro.org)
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Timer;

import dirsyncpro.DirSyncPro;

public class ScreenUpdater {
	final Timer timer;
	AtomicBoolean running = new AtomicBoolean(false);
	
    public ScreenUpdater(){
    	final ActionListener taskPerformer = new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent evt) {
        		if (running.compareAndSet(false, true)){
        			if (DirSyncPro.getGui() != null){
        				//we are already within EDT
        				DirSyncPro.getGui().updateGUIEDT(false);
        			}
        			running.set(false);
        		}
            }
        };
		timer = new Timer(10, taskPerformer);
	}
	
    public void startUpdating(){
    	timer.start();
    }
    
    public void stopUpdating(){
    	//wait 1 second before stopping to make sure everything is updated in the GUI.
//    	Timer tmpTimer = new Timer(1000, new ActionListener() {
//    		int counter = 0;
//        	@Override
//            public void actionPerformed(ActionEvent evt) {
//        		if (counter > 0){
//        			counter--;
//        		}else{
//        			if (timer.isRunning()){
//        				timer.stop();
//        			}
//        			((Timer) evt.getSource()).stop();
//        		}
//            }
//        });
//    	tmpTimer.start();
    	// the following line should run in EDT. 
    	DirSyncPro.getGui().updateGUIEDT(false);
    	timer.stop();
    }
}
