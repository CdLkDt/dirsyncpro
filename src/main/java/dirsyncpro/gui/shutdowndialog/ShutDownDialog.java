/* ShutDownDialog.java
 *
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org)
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

package dirsyncpro.gui.shutdowndialog;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

import dirsyncpro.DirSyncPro;


/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings("unused")
public class ShutDownDialog extends ShutDownDialogObjects {
	int counter = 0;
	Timer timer = new Timer();
	public class CountDownTask extends TimerTask{
		@Override
		public void run() {
			if (counter >0){
				counter--;
				setSeconds();
			}else{
				this.cancel();
				timer.purge();
				setInvisible();
				DirSyncPro.shutDownSystem();
			}
		}		
	};
	
	TimerTask task = new CountDownTask();
	
	public ShutDownDialog(JFrame frame){
		super(frame);
	}
	
	public void startCountDown(){
		counter = 31;
		task = new CountDownTask();
		Date now = new Date();
		long period = 1000; //every seconds
		timer.schedule(task, now, period);
	}
	
	private void setSeconds(){
		secondsJLabel.setText(counter + "");
	}
	
	private void setInvisible(){
		this.setVisible(false);
	}
	
	protected void stopCountDown(){
		task.cancel();
		timer.purge();
	}
}
