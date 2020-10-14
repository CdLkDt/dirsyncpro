/* ScheduleDialog.java
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

package dirsyncpro.gui.jobdialog.scheduledialog;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.JobDialog;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.Schedule;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleDaily;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleHourly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMinutely;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleMonthly;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleOnce;
import dirsyncpro.gui.jobdialog.scheduletree.schedule.ScheduleWeekly;
import dirsyncpro.gui.swing.MyJTabbedPane;
import dirsyncpro.job.Job;
import dirsyncpro.tools.DateTool;


/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings("unused")
public class ScheduleDialog extends ScheduleDialogObjects {
	private Schedule schedule;

	public ScheduleDialog(JDialog dialog){
		super(dialog);
	}
	
	public void enableBasicsScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 0;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableOnceScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 1;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableMinutelyScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 2;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableHourlyScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 3;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableDailyScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 4;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableWeeklyScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 5;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	private void enableMonthlyScheduleTab(boolean enabled, boolean selected){
		int tabNumber = 6;
		schedulesTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) schedulesTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}

	public void initSchedulesDialog(Schedule sched){
		schedule = sched;
		boolean isEdit = (schedule != null);
		if (isEdit){
			//edit
			boolean once = schedule.getType() == Schedule.Type.Once;
			boolean hourly = schedule.getType() == Schedule.Type.Hourly;
			boolean minutely = schedule.getType() == Schedule.Type.Minutely;
			boolean daily = schedule.getType() == Schedule.Type.Daily;
			boolean weekly = schedule.getType() == Schedule.Type.Weekly;
			boolean monthly = schedule.getType() == Schedule.Type.Monthly;
			
			scheduleOnceRadioButton.setSelected(once);
			scheduleMinutelyRadioButton.setSelected(minutely);
			scheduleHourlyRadioButton.setSelected(hourly);
			scheduleDailyRadioButton.setSelected(daily);
			scheduleWeeklyRadioButton.setSelected(weekly);
			scheduleMonthlyRadioButton.setSelected(monthly);
			
			if (schedule.getTimeFrameFrom().compareTo(Const.NonDate) == 0){
				scheduleTimeFrameFromCheckBox.setSelected(false);
				Date date = DateTool.getNextCompleteHour();
				scheduleTimeFrameFromTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleTimeFrameFromTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));

			}else{
				scheduleTimeFrameFromCheckBox.setSelected(true);
				scheduleTimeFrameFromTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(schedule.getTimeFrameFrom()));
			}
			if (schedule.getTimeFrameTo().compareTo(Const.NonDate) == 0){
				scheduleTimeFrameToCheckBox.setSelected(false);
				Date date = DateTool.getNextCompleteHour();
				scheduleTimeFrameToTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleTimeFrameToTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
			}else{
				scheduleTimeFrameToCheckBox.setSelected(true);
				scheduleTimeFrameToTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(schedule.getTimeFrameTo()));
			}
			
			if (once){
				scheduleOnceTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(((ScheduleOnce) schedule).getDate()));
				enableOnceScheduleTab(true, true);
			}else if (minutely){
				scheduleMinutelyEveryTextField.setText(((ScheduleMinutely) schedule).getInterval() + "");
				enableMinutelyScheduleTab(true, true);
			}else if (hourly){
				scheduleHourlyEveryTextField.setText(((ScheduleHourly) schedule).getInterval() + "");
				enableHourlyScheduleTab(true, true);
			}else if (daily){
				scheduleDailyEveryTextField.setText(((ScheduleDaily) schedule).getInterval() + "");
				scheduleDailyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleDaily) schedule).getTime()));
				enableDailyScheduleTab(true, true);
			}else if (weekly){
				scheduleWeeklyEveryTextField.setText(((ScheduleWeekly) schedule).getInterval() + "");
				scheduleWeeklyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleWeekly) schedule).getTime()));
				scheduleWeeklyMondayCheckBox.setSelected(((ScheduleWeekly) schedule).isMonday());
				scheduleWeeklyTuesdayCheckBox.setSelected(((ScheduleWeekly) schedule).isTuesday());
				scheduleWeeklyWednesdayCheckBox.setSelected(((ScheduleWeekly) schedule).isWednesday());
				scheduleWeeklyThursdayCheckBox.setSelected(((ScheduleWeekly) schedule).isThursday());
				scheduleWeeklyFridayCheckBox.setSelected(((ScheduleWeekly) schedule).isFriday());
				scheduleWeeklySaturdayCheckBox.setSelected(((ScheduleWeekly) schedule).isSaturday());
				scheduleWeeklySundayCheckBox.setSelected(((ScheduleWeekly) schedule).isSunday());
				enableWeeklyScheduleTab(true, true);
			}else if (monthly){
				scheduleMonthlyDayNumberTextField.setText(((ScheduleMonthly) schedule).getDay() + "");
				scheduleMonthlyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(((ScheduleMonthly) schedule).getTime()));
				scheduleMonthlyJanuaryCheckBox.setSelected(((ScheduleMonthly) schedule).isJanuary());
				scheduleMonthlyFebruaryCheckBox.setSelected(((ScheduleMonthly) schedule).isFebruary());
				scheduleMonthlyMarchCheckBox.setSelected(((ScheduleMonthly) schedule).isMarch());
				scheduleMonthlyAprilCheckBox.setSelected(((ScheduleMonthly) schedule).isApril());
				scheduleMonthlyMayCheckBox.setSelected(((ScheduleMonthly) schedule).isMay());
				scheduleMonthlyJuneCheckBox.setSelected(((ScheduleMonthly) schedule).isJune());
				scheduleMonthlyJulyCheckBox.setSelected(((ScheduleMonthly) schedule).isJuly());
				scheduleMonthlyAugustCheckBox.setSelected(((ScheduleMonthly) schedule).isAugust());
				scheduleMonthlySeptemberCheckBox.setSelected(((ScheduleMonthly) schedule).isSeptember());
				scheduleMonthlyOctoberCheckBox.setSelected(((ScheduleMonthly) schedule).isOctober());
				scheduleMonthlyNovemberCheckBox.setSelected(((ScheduleMonthly) schedule).isNovember());
				scheduleMonthlyDecemberCheckBox.setSelected(((ScheduleMonthly) schedule).isDecember());
				enableMonthlyScheduleTab(true, true);
			}
		}else{
			//add
			
			//basics
			scheduleOnceRadioButton.setSelected(true);
			scheduleMinutelyRadioButton.setSelected(false);
			scheduleHourlyRadioButton.setSelected(false);
			scheduleDailyRadioButton.setSelected(false);
			scheduleWeeklyRadioButton.setSelected(false);
			scheduleMonthlyRadioButton.setSelected(false);
			scheduleTimeFrameFromCheckBox.setSelected(false);
			scheduleTimeFrameToCheckBox.setSelected(false);
			
			Date date = DateTool.getNextCompleteHour();
			scheduleTimeFrameFromTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleTimeFrameFromTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
			scheduleTimeFrameToTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleTimeFrameToTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
			scheduleOnceTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(date));
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleOnceTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
			scheduleMinutelyEveryTextField.setText("5");
			scheduleHourlyEveryTextField.setText("1");
			scheduleDailyEveryTextField.setText("1");
			scheduleDailyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(date));
			scheduleWeeklyEveryTextField.setText("1");
			scheduleWeeklyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(date));
			scheduleWeeklyMondayCheckBox.setSelected(true);
			scheduleMonthlyDayNumberTextField.setText("1");
			scheduleMonthlyTimeTextField.setText((new SimpleDateFormat(Const.DefaultTimeFormat)).format(date));
			scheduleMonthlyJanuaryCheckBox.setSelected(true);
			verifyScheduleFields();
		}
		enableBasicsScheduleTab(true, !isEdit);

		scheduleOnceRadioButton.setEnabled(!isEdit);
		scheduleMinutelyRadioButton.setEnabled(!isEdit);
		scheduleHourlyRadioButton.setEnabled(!isEdit);
		scheduleDailyRadioButton.setEnabled(!isEdit);
		scheduleWeeklyRadioButton.setEnabled(!isEdit);
		scheduleMonthlyRadioButton.setEnabled(!isEdit);
		
		schedulesEvent();
	}

	protected void verifyScheduleFields(){
		// generate a mouse event to clean fire 'verify' property
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleTimeFrameToTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleOnceTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleMinutelyEveryTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleHourlyEveryTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleDailyEveryTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleDailyTimeTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleWeeklyEveryTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleWeeklyTimeTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleMonthlyDayNumberTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(scheduleMonthlyTimeTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
	}
	
	protected void schedulesEvent(){
		scheduleTimeFrameFromLabel1.setEnabled(scheduleTimeFrameFromCheckBox.isSelected());
		scheduleTimeFrameFromLabel2.setEnabled(scheduleTimeFrameFromCheckBox.isSelected());
		scheduleTimeFrameFromTextField.setEnabled(scheduleTimeFrameFromCheckBox.isSelected());
		scheduleTimeFrameToLabel1.setEnabled(scheduleTimeFrameToCheckBox.isSelected());
		scheduleTimeFrameToLabel2.setEnabled(scheduleTimeFrameToCheckBox.isSelected());
		scheduleTimeFrameToTextField.setEnabled(scheduleTimeFrameToCheckBox.isSelected());

		enableBasicsScheduleTab(true, false);
		enableOnceScheduleTab(scheduleOnceRadioButton.isSelected(), false);
		enableMinutelyScheduleTab(scheduleMinutelyRadioButton.isSelected(), false);
		enableHourlyScheduleTab(scheduleHourlyRadioButton.isSelected(), false);
		enableDailyScheduleTab(scheduleDailyRadioButton.isSelected(), false);
		enableWeeklyScheduleTab(scheduleWeeklyRadioButton.isSelected(), false);
		enableMonthlyScheduleTab(scheduleMonthlyRadioButton.isSelected(), false);
	}

	protected void applySchedule(){
		JobDialog jobDialog = DirSyncPro.getGui().getJobDialog();
		Job job = DirSyncPro.getGui().getSelectedJob();
		
		boolean isNew = false;
		if (schedule == null){
			isNew = true;
			if (scheduleOnceRadioButton.isSelected()){
				schedule = new ScheduleOnce(job);
			}else if (scheduleMinutelyRadioButton.isSelected()){
				schedule = new ScheduleMinutely(job);
			}else if (scheduleHourlyRadioButton.isSelected()){
				schedule = new ScheduleHourly(job);
			}else if (scheduleDailyRadioButton.isSelected()){
				schedule = new ScheduleDaily(job);
			}else if (scheduleWeeklyRadioButton.isSelected()){
				schedule = new ScheduleWeekly(job);
			}else if (scheduleMonthlyRadioButton.isSelected()){
				schedule = new ScheduleMonthly(job);
			}
		}
		
		try{
			if (scheduleTimeFrameFromCheckBox.isSelected()){
				schedule.setTimeFrameFrom((new SimpleDateFormat(Const.DefaultDateFormat)).parse(scheduleTimeFrameFromTextField.getText()));
			}else{
				schedule.setTimeFrameFrom(Const.NonDate);
			}

			if (scheduleTimeFrameToCheckBox.isSelected()){
				schedule.setTimeFrameTo((new SimpleDateFormat(Const.DefaultDateFormat)).parse(scheduleTimeFrameToTextField.getText()));
			}else{
				schedule.setTimeFrameTo(Const.NonDate);
			}
		}catch (ParseException e){
			//none; will not happen
		}
			
		if (schedule instanceof ScheduleOnce){
			try{
				((ScheduleOnce) schedule).setDate((new SimpleDateFormat(Const.DefaultDateFormat)).parse(scheduleOnceTextField.getText()));
			}catch (ParseException e){
				//none; will not happen
			}
		}else if (schedule instanceof ScheduleMinutely){
			((ScheduleMinutely) schedule).setInterval(Integer.parseInt(scheduleMinutelyEveryTextField.getText()));
		}else if (scheduleHourlyRadioButton.isSelected()){
			((ScheduleHourly) schedule).setInterval(Integer.parseInt(scheduleHourlyEveryTextField.getText()));
		}else if (schedule instanceof ScheduleDaily){
			((ScheduleDaily) schedule).setInterval(Integer.parseInt(scheduleDailyEveryTextField.getText()));
			try{
				((ScheduleDaily) schedule).setTime((new SimpleDateFormat(Const.DefaultTimeFormat)).parse(scheduleDailyTimeTextField.getText()));
			}catch (ParseException e){
				//none; will not happen
			}
			try{
				((ScheduleDaily) schedule).setTime((new SimpleDateFormat(Const.DefaultDateFormat)).parse(scheduleDailyTimeTextField.getText()));
			}catch (ParseException e){
				//none; will not happen
			}
		}else if (schedule instanceof ScheduleWeekly){
			try{
				((ScheduleWeekly) schedule).setTime((new SimpleDateFormat(Const.DefaultTimeFormat)).parse(scheduleWeeklyTimeTextField.getText()));
			}catch (ParseException e){
				//none; will not happen
			}

			((ScheduleWeekly) schedule).setInterval(Integer.parseInt(scheduleWeeklyEveryTextField.getText()));
			((ScheduleWeekly) schedule).setMonday(scheduleWeeklyMondayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setTuesday(scheduleWeeklyTuesdayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setWednesday(scheduleWeeklyWednesdayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setThursday(scheduleWeeklyThursdayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setFriday(scheduleWeeklyFridayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setSaturday(scheduleWeeklySaturdayCheckBox.isSelected());
			((ScheduleWeekly) schedule).setSunday(scheduleWeeklySundayCheckBox.isSelected());
		}else if (schedule instanceof ScheduleMonthly){
			try{
				((ScheduleMonthly) schedule).setTime((new SimpleDateFormat(Const.DefaultTimeFormat)).parse(scheduleMonthlyTimeTextField.getText()));
			}catch (ParseException e){
				//none; will not happen
			}
			((ScheduleMonthly) schedule).setDay(Integer.parseInt(scheduleMonthlyDayNumberTextField.getText()));
			((ScheduleMonthly) schedule).setJanuary(scheduleMonthlyJanuaryCheckBox.isSelected());
			((ScheduleMonthly) schedule).setFebruary(scheduleMonthlyFebruaryCheckBox.isSelected());
			((ScheduleMonthly) schedule).setMarch(scheduleMonthlyMarchCheckBox.isSelected());
			((ScheduleMonthly) schedule).setApril(scheduleMonthlyAprilCheckBox.isSelected());
			((ScheduleMonthly) schedule).setMay(scheduleMonthlyMayCheckBox.isSelected());
			((ScheduleMonthly) schedule).setJune(scheduleMonthlyJuneCheckBox.isSelected());
			((ScheduleMonthly) schedule).setJuly(scheduleMonthlyJulyCheckBox.isSelected());
			((ScheduleMonthly) schedule).setAugust(scheduleMonthlyAugustCheckBox.isSelected());
			((ScheduleMonthly) schedule).setSeptember(scheduleMonthlySeptemberCheckBox.isSelected());
			((ScheduleMonthly) schedule).setOctober(scheduleMonthlyOctoberCheckBox.isSelected());
			((ScheduleMonthly) schedule).setNovember(scheduleMonthlyNovemberCheckBox.isSelected());
			((ScheduleMonthly) schedule).setDecember(scheduleMonthlyDecemberCheckBox.isSelected());
		}

		if (isNew){
			DirSyncPro.getGui().getJobDialog().addSchedule(schedule);
			//add schedule updates the scheduletree already
		}else{
			// if it is not a new filter, we have already the reference to it and it is changed already
			DirSyncPro.getGui().getJobDialog().updateScheduleTree();
		}
	}
}
