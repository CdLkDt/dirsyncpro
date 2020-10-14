/*
 * Scheduler.java
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

package dirsyncpro.gui.jobdialog.scheduletree.schedule;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;

import dirsyncpro.Const.IconKey;
import dirsyncpro.DirSyncPro;
import dirsyncpro.job.Job;
import dirsyncpro.sync.Sync;

public class ScheduleEngine {
	private Vector<Schedule> scheduleQ;
	private AtomicBoolean scheduleQUpdated = new AtomicBoolean(false);
	Timer timer;
	boolean running; 
	Schedule synchronizingSchedule;
	
	public ScheduleEngine(){
		scheduleQ = new Vector<Schedule>();
		scheduleQUpdated.set(true);
		timer = new Timer();
	}
	
	public void addSchedules(Job job){
		for (Schedule s : job.getSchedules()){
			s.calculateNextEvent();
			if (s.getNextEvent() != null){
				scheduleQ.add(s);
			}
		}
		Collections.sort(scheduleQ);
		scheduleQUpdated.set(true);
	}
	
	private void addSchedule(Schedule sched){
		if (sched != null){
			scheduleQ.add(sched);
		}
		Collections.sort(scheduleQ);
		scheduleQUpdated.set(true);
	}
	
	public void initSchedules(){
		scheduleQ.clear();
		for (Job job : DirSyncPro.getSync().getJobs()){
			addSchedules(job);
		}
		scheduleQUpdated.set(true);
	}
	
	public void runSchedulesPeriodically(){
		if (scheduleQ != null && scheduleQ.size() > 0 && DirSyncPro.getSync().getState() == Sync.STOP){
			Schedule sched = scheduleQ.get(0);
			Date now = new Date();
			if (sched.getNextEvent().compareTo(now) <= 0){
				synchronizingSchedule = sched;
				scheduleQUpdated.set(true);
				Vector<Job> v = new Vector<Job>();
				v.add(sched.getJob());
				DirSyncPro.getSync().getLog().printMinimal("Synchronizing job '" + sched.getJob().getName() + "' (scheduled: " + sched.toString().replace("Next event", "Event") + ")", IconKey.Info);
				DirSyncPro.getSync().synchronize(v);
				sched.setSynced();
				if (isRunning()){
					// still running? not stopped?
					scheduleQ.remove(0);
					sched.calculateNextEvent();
					addSchedule(sched);
				}
			}
			synchronizingSchedule = null;
		}
	}
	
	public void startScheduler(){
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				runSchedulesPeriodically();
			}
		};
		
		initSchedules();
		if (scheduleQ.size() > 0){
			Date now = new Date();
			long period = 60 * 1000; //every minute
			timer = new Timer();
			timer.schedule(task, now, period);
			running = true;
			DirSyncPro.getSync().getLog().printMinimal("Schedule engine started!", IconKey.Info);
			scheduleQUpdated.set(true);
		}else{
			if (DirSyncPro.isStartScheduleEngineOnStartup()){
				if (JOptionPane.showConfirmDialog(DirSyncPro.getGui(), "There are no effective scheduled tasks. Schedule Engine will not start!\n\nYou seem to have set the option to start the Schedule Engine upon startup.\nDo you wish to turn it off? ", "Schedule Engine", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					DirSyncPro.setStartScheduleEngineOnStartup(false);
				}
			}else{
				DirSyncPro.displayError("There are no effective scheduled tasks. Schedule Engine will not start!");
			}
		}
	}
	
	public void stopScheduler(){
		timer.cancel();
		timer = null;
		running = false;
		DirSyncPro.getSync().getLog().printMinimal("Schedule engine stopped!", IconKey.Info);
		scheduleQ.clear();
		scheduleQUpdated.set(true);
	}

	/**
	 * @return the scheduleQ
	 */
	public Vector<Schedule> getScheduleQ() {
		return scheduleQ;
	}

	/**
	 * returns the schedule which is synchronizing now
	 * @return Schedule the schedule which is synchronizing now 
	 */
	public Schedule getSynchronizingSchedule(){
		return synchronizingSchedule;
	}
	
	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}
	
	//job - job to be syncronized, Date - date, when synchronization needs to be done
	//Empty record for job means that it need another task to be created
	//if record exists then it need to be updated with a new date and rscheduled after current task completes
	private HashMap<Job, Date> realtimeSchedule = new HashMap<Job, Date>();
	private static ScheduleEngine realtimeInstance = new ScheduleEngine();
	public static ScheduleEngine getRealtimeInstance(){
		return realtimeInstance;
	}
	
	public void scheduleRealtime(final Job j, int delay){
		Date d = null;
		synchronized (realtimeSchedule) {
			d = realtimeSchedule.get(j);
			//something is scheduled and running - just update with a new date
			if (d != null){
				Date now = new Date();
				now.setTime(now.getTime() + delay);
				//d.setTime(d.getTime() + delay);
				if (now.getTime() > d.getTime()){
					realtimeSchedule.put(j, now);
					;//System.out.println("Already scheduled for " + d + ". Extending to " + now);
				}else
					;//System.out.println("Previously scheduled for " + d + " is later than " + now);
				return;
			}
			
			//need to schedule new task for this job
			d = new Date();
			d.setTime(d.getTime() + delay);
			realtimeSchedule.put(j, d);
		}
		
		//no need to synchronize "realtimeSchedule", this is the only thread that will be setting the task
		class TimerTaskRealtime extends TimerTask{
			
			@Override
			public void run() {
				// === 1 ===
				//get the recent date before synchronization
				Date recentSyncDate = null;
				synchronized (realtimeSchedule) {
					recentSyncDate = realtimeSchedule.get(j);
				}
				//if there is more recent request - reschedule synchronization to it's time
				if (recentSyncDate.getTime() > scheduledExecutionTime()){
					timer.schedule(new TimerTaskRealtime(), recentSyncDate);
					;//System.out.println("Rescheduled from " + new Date(scheduledExecutionTime()) + " to " + recentSyncDate);
					return;
				}
				
				// === 2 ===
				//another synchronization is in progress - don't do sync now - reschedule for the next time
				if (DirSyncPro.getSync().getState() != Sync.STOP){
					DirSyncPro.getSync().getLog().printMinimal("Syncronization of " + j.getName() + " is paused for " + 
											j.getSyncRealtimeDelay() + " seconds. Another synchronization is in progress.", IconKey.Warning);
					
					//reschedule next attempt
					Date restartDate = (Date)recentSyncDate.clone();
					restartDate.setTime(restartDate.getTime() + j.getSyncRealtimeDelay() * 1000 + 1000);
					TimerTask tt = new TimerTaskRealtime();
					timer.schedule(tt, restartDate);
					return;
				}
				
				// === 3 ===
				//do synchronization now and schedule another if needed
				;//System.out.println("Running at " + new Date(scheduledExecutionTime()));
				DirSyncPro.getSync().synchronize(new Vector<Job>(){{add(j);}});
				synchronized (realtimeSchedule) {
					
					recentSyncDate = realtimeSchedule.get(j);
					//if date was changed during synchronization - reschedule another task
					if (recentSyncDate.getTime() > scheduledExecutionTime()){
						;//System.out.println("Rescheduling to " + recentSyncDate);
						timer.schedule(new TimerTaskRealtime(), recentSyncDate);
					}
					//otherwise if no sync is required then remove schedule
					else{
						realtimeSchedule.remove(j);
						;//System.out.println("Removed schedule at " + new Date(scheduledExecutionTime()));						
					}
				}
				;//System.out.println("Finished running");				
			}
			
		}
		
		//schedule first time
		TimerTask tt = new TimerTaskRealtime();
		timer.schedule(tt, d);
	}

	public boolean isScheduleQUpdated() {
	    return scheduleQUpdated.get();
	}

	public void setScheduleQUpdated(boolean scheduleQUpdated) {
	    this.scheduleQUpdated.set(scheduleQUpdated);
	}
}
