/*
 * ProgressApproximator.java
 * 
 * Copyright (C) 2012 O. Givi (info@dirsyncpro.org), Michael Lux
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

package dirsyncpro.tools.copy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Path;

import dirsyncpro.DirSyncPro;

public class ProgressApproximator {

	//singleton pattern, initialization by JVM
	private final static ProgressApproximator instance = new ProgressApproximator();
	
	private final static double LOG_FACTOR = Math.log(10);
	public static double log10(double val) {
		return Math.log(val)/LOG_FACTOR;
	}
	
	public static ProgressApproximator getApproximator() {
		return instance;
	}
	
	private class Approximation {
		
		private double approx = 0.;
		
		//update Approximation with last copy time and last file size
		public void updateApprox(long duration, double fileSize) {
			approx = fileSize / duration;
		}
		
		//tells whether we have at least one approximation in this object
		public boolean hasApprox() {
			return approx > 0;
		}
		
		//get approximation value
		public double getApprox() {
			return approx;
		}
		
	}
	
	//private constructor for singleton pattern
	private ProgressApproximator() {
		for(int i = 0; i < 20; i++) {
			approx[i] = new Approximation();
		}
	}
	
	//array with approximation objects for file sizes 1, 10, 100, 1000, ..., 10^19 bytes
	private Approximation approx[] = new Approximation[20];
	//size of current file
	private double curFileSize = 1;
	//best approximation value for current file
	private double curApprox = 0;
	//index of curApprox in approx[] array
	private int curApproxIndex = 0;
	//system time when startApprox() was called
	private long startTime = 0;
	//performs GUI progress update
	private final ActionListener taskPerformer = new ActionListener() {
    	@Override
        public void actionPerformed(ActionEvent evt) {
            updateProgress();
        }
    };
    //Timer for GUI progress update
    private final javax.swing.Timer t = new javax.swing.Timer(10, taskPerformer);
	
	//start an approximation with a file with given size
	public void startApprox(Path path, boolean updateProgress) {
		curFileSize = path.toFile().length();
		//calculate index in approx array, using binary logarithm
		curApproxIndex = (int)log10(curFileSize);
		//if file is too large, assign to highest allowed size bucket
		if(curApproxIndex >= approx.length) {
			curApproxIndex = approx.length-1;
		}
		//fix: search for most accurate value according to current file size
		for(int dist = 0; dist < approx.length; dist++) {
			int upper = curApproxIndex + dist;
			if(upper < approx.length) {
				if(approx[upper].hasApprox()) {
					curApproxIndex = upper;
					break;
				}
			}
			int lower = curApproxIndex - dist;
			if(lower >= 0) {
				if(approx[lower].hasApprox()) {
					curApproxIndex = lower;
					break;
				}
			}
		}
		//calculate approximation
		double dividend = 0, divisor = 0;
		if(approx[curApproxIndex].hasApprox()) {
			divisor = 64.;
			dividend = approx[curApproxIndex].getApprox() * divisor;
		}
		double dividendInc = 32.;
		for(int dist = 1; dist < 5; dist++, dividendInc /= 2.) {
			int upper = curApproxIndex + dist;
			if(upper < approx.length) {
				if(approx[upper].hasApprox()) {
					dividend += approx[upper].getApprox() * dividendInc;
					divisor += dividendInc;
					continue;
				}
			}
			int lower = curApproxIndex - dist;
			if(lower >= 0) {
				if(approx[lower].hasApprox()) {
					dividend += approx[lower].getApprox() * dividendInc;
					divisor += dividendInc;
					continue;
				}
			}
		}
		if(divisor > 0.) {
			curApprox = dividend/divisor;
		} else {
			curApprox = 0;
		}
		if(updateProgress) {
			if (curApprox == 0){
				int value = (int) (getNormalizedApprox() * 99.);
				if (DirSyncPro.isGuiMode()){
					DirSyncPro.getGui().registerProgressBars(-1, -1, value, "", false, value, 100, path.getFileName() + " (copying & estimating copy speed...)", true);
				}
			}
			t.start();
		}
		startTime = System.currentTimeMillis();
//		System.out.println("Actual estimation: " + curApprox);
	}
	
	public void endApprox() {
		approx[curApproxIndex].updateApprox(System.currentTimeMillis() - startTime, curFileSize);
		if(t.isRunning()) {
			t.stop();
		}
		//System.out.println("Real value: " + ( (double) curFileSize / (double) (System.currentTimeMillis() - startTime) ) + "\n");
	}
	
	public double getNormalizedApprox() {
		double nApprox = ( curApprox * (System.currentTimeMillis() - startTime) ) / curFileSize;
		if(nApprox > 1.) {
			return 1.;
		} else {
			return nApprox;
		}
	}
	
	public void updateProgress() {
		if (DirSyncPro.isGuiMode() && curApprox > 0){
			// normally: DirSyncPro.getGui().registerProgressBarsEDT(-1, -1, -1, "", false, (int) (getNormalizedApprox() * 100.), 100, "", false);
			//                                                                                                             ---
			// but because the approximation is not accurate, we just go up to 99% and will stop till the copy process finishes.
			int value = (int) (getNormalizedApprox() * 99.);
			DirSyncPro.getGui().registerProgressBars(-1, -1, value, "", false, value, 100, "", false);
		}
	}
	
}
