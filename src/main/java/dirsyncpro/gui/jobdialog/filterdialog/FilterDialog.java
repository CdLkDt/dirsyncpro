/* FilterDialog.java
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

package dirsyncpro.gui.jobdialog.filterdialog;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.nio.file.attribute.PosixFilePermission;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;

import dirsyncpro.Const;
import dirsyncpro.DirSyncPro;
import dirsyncpro.gui.jobdialog.JobDialog;
import dirsyncpro.gui.jobdialog.filtertree.filter.Filter;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate.FilterDateDirFile;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByDate.FilterTimeUnitType;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileAttribute;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileOwnership;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFilePermissions;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByFileSize;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPath;
import dirsyncpro.gui.jobdialog.filtertree.filter.FilterByPattern;
import dirsyncpro.gui.swing.MyJTabbedPane;
import dirsyncpro.job.Job;


/**
 * Contains the GUI methods.
 * 
 * @author F. Gerbig, O. Givi (info@dirsyncpro.org)
 */
@SuppressWarnings("unused")
public class FilterDialog extends FilterDialogObjects {

       	
       	{
       		filtersDateTimeUnitComboBox.setModel(new javax.swing.DefaultComboBoxModel<FilterTimeUnitType>(FilterTimeUnitType.values()));
       	}
       	//end FIXME
    
	// temp vars for within the gui
	private String prevPattern;
	private enum FilterTabs{ByPattern, BySize, ByDate, ByPath, ByFileAttribute, ByGroup, ByFilePermission};
	private Filter filter;
	
	public FilterDialog(JDialog dialog){
		super(dialog);
	}
	
	private void enableFilterTab(FilterTabs ft, boolean enabled, boolean selected){
		int tabNumber = ft.ordinal() + 1;
		filtersTabbedPane.setEnabledAt(tabNumber, enabled);
		if (selected){
			((MyJTabbedPane) filtersTabbedPane).forceSetSelectedIndex(tabNumber);
		}
	}
	

	private void disableAllFilterTabs(){
		for (FilterTabs ft : FilterTabs.values()){
			int tabNumber = ft.ordinal() + 1;
			filtersTabbedPane.setEnabledAt(tabNumber, false);
		}
	}
	
	public void initFilterDialog(Filter ft){
		filter = ft;
		boolean isEdit = (ft != null);
		if (isEdit){
			//edit
				boolean byPattern = ft instanceof FilterByPattern;
				boolean byFileSize = ft instanceof FilterByFileSize;
				boolean byDate = ft instanceof FilterByDate;
				boolean byPath = ft instanceof FilterByPath;
				boolean byFileAttribute = ft instanceof FilterByFileAttribute;
				boolean byFileOwnership = ft instanceof FilterByFileOwnership;
				boolean byFilePermissions = ft instanceof FilterByFilePermissions; 
				
				filtersByPatternRadioButton.setSelected(byPattern);
				filtersByFileSizeRadioButton.setSelected(byFileSize);
				filtersByDateRadioButton.setSelected(byDate);
				filtersByPathRadioButton.setSelected(byPath);
				filtersByFileAttributeRadioButton.setSelected(byFileAttribute);
				filtersByFileOwnershipRadioButton.setSelected(byFileOwnership);
				filtersByFilePermissionsRadioButton.setSelected(byFilePermissions);
				filtersByPatternRadioButton.setEnabled(false);
				filtersByFileSizeRadioButton.setEnabled(false);
				filtersByDateRadioButton.setEnabled(false);
				filtersByPathRadioButton.setEnabled(false);
				filtersByFileAttributeRadioButton.setEnabled(false);
				filtersByFileOwnershipRadioButton.setEnabled(false);
				filtersByFilePermissionsRadioButton.setEnabled(false);
				filtersByPatternLabel.setEnabled(false);
				filtersByFileSizeLabel.setEnabled(false);
				filtersByDateLabel.setEnabled(false);
				filtersByPathLabel.setEnabled(false);
				filtersByFileAttributeLabel.setEnabled(false);
				filtersByFileOwnershipLabel.setEnabled(false);
				filtersByFilePermissionsLabel.setEnabled(false);

				filtersIncludeRadioButton.setSelected(ft.getAction() == Filter.Action.Include);
				filtersExcludeRadioButton.setSelected(ft.getAction() == Filter.Action.Exclude);
				
				disableAllFilterTabs();
				
				if (byPattern){
					enableFilterTab(FilterTabs.ByPattern, true, true);
					filtersFilePatternRadioButton.setSelected(((FilterByPattern) ft).getPatternType() == FilterByPattern.FilterPatternType.File);
					filtersDirPatternRadioButton.setSelected(((FilterByPattern) ft).getPatternType() == FilterByPattern.FilterPatternType.Directory);
					prevPattern = ((FilterByPattern) ft).getPattern();
					filtersRegularExpressionCheckBox.setSelected(((FilterByPattern) ft).isRegExp());
					filtersPatternField.setText(prevPattern);
				}else if (byFileSize){
					enableFilterTab(FilterTabs.BySize, true, true);
					filtersFileSizeField.setText(((FilterByFileSize) ft).getValue() + "");
					filtersSmallerFileSizeRadioButton.setSelected(((FilterByFileSize) ft).getFileSizeType() == FilterByFileSize.FilterFileSizeType.SmallerThan);
					filtersExactFileSizeRadioButton.setSelected(((FilterByFileSize) ft).getFileSizeType() == FilterByFileSize.FilterFileSizeType.Exactly);
					filtersLargerFileSizeRadioButton.setSelected(((FilterByFileSize) ft).getFileSizeType() == FilterByFileSize.FilterFileSizeType.LargerThan);
				}else if (byDate){
					enableFilterTab(FilterTabs.ByDate, true, true);
					filtersDateModeSpecificTimeRadioButton.setSelected(((FilterByDate) ft).isFilterByModificationTime());
					filtersDateTimeUnitField.setText(((FilterByDate) ft).isFilterByTimeUnit() ? ((FilterByDate) ft).getTimeUnitValue() + "" : "0");
					filtersDateTimeUnitComboBox.setSelectedItem(((FilterByDate) ft).getUnitType());
					filtersDateTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(((FilterByDate) ft).getDate()));
					filtersEarlierDateRadioButton.setSelected(((FilterByDate) ft).getDateType() == FilterByDate.FilterDateType.EarlierThan);
					filtersSameDateRadioButton.setSelected(((FilterByDate) ft).getDateType() == FilterByDate.FilterDateType.ExactlyOn);
					filtersLaterDateRadioButton.setSelected(((FilterByDate) ft).getDateType() == FilterByDate.FilterDateType.LaterThan);
					filtersDateDirFileRadioButton.setSelected(((FilterByDate) ft).getDirFile() == FilterByDate.FilterDateDirFile.DirFile);
					filtersDateDirRadioButton.setSelected(((FilterByDate) ft).getDirFile() == FilterByDate.FilterDateDirFile.Dir);
					filtersDateFileRadioButton.setSelected(((FilterByDate) ft).getDirFile() == FilterByDate.FilterDateDirFile.File);
				}else if (byPath){
					enableFilterTab(FilterTabs.ByPath, true, true);
					filtersPathFilterField.setText(((FilterByPath) ft).getPathStr());
				}else if (byFileAttribute){
					enableFilterTab(FilterTabs.ByFileAttribute, true, true);
					filtersReadOnlyAttributeCheckBox.setSelected(((FilterByFileAttribute) ft).isReadOnly());
					filtersHiddenAttributeCheckBox.setSelected(((FilterByFileAttribute) ft).isHidden());
					filtersSystemAttributeCheckBox.setSelected(((FilterByFileAttribute) ft).isSystem());
					filtersArchiveAttributeCheckBox.setSelected(((FilterByFileAttribute) ft).isArchive());
				}else if (byFileOwnership){
					enableFilterTab(FilterTabs.ByGroup, true, true);
					filtersGroupFilterField.setText(((FilterByFileOwnership) ft).getGroupStr());
					filtersOwnerFilterField.setText(((FilterByFileOwnership) ft).getOwnerStr());
				}else if (byFilePermissions){
					enableFilterTab(FilterTabs.ByFilePermission, true, true);
					filtersPermissionFilterField.setText(((FilterByFilePermissions) ft).getOctalPermissionValue());
					filtersPermissionFilterURCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_READ));
					filtersPermissionFilterUWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_WRITE));
					filtersPermissionFilterUXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_EXECUTE));
					filtersPermissionFilterGRCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_READ));
					filtersPermissionFilterGWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_WRITE));
					filtersPermissionFilterGXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_EXECUTE));
					filtersPermissionFilterORCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_READ));
					filtersPermissionFilterOWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_WRITE));
					filtersPermissionFilterOXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_EXECUTE));
				}
		}else{
			//add
			
			filtersByPatternLabel.setEnabled(true);
			filtersByPatternRadioButton.setEnabled(true);
			filtersByFileSizeLabel.setEnabled(true);
			filtersByFileSizeRadioButton.setEnabled(true);
			filtersByDateLabel.setEnabled(true);
			filtersByDateRadioButton.setEnabled(true);
			filtersByPathLabel.setEnabled(true);
			filtersByPathRadioButton.setEnabled(true);
			
			boolean enabled = Const.OS_IS_WINDOWS;
			filtersByFileAttributeLabel.setEnabled(enabled);
			filtersByFileAttributeRadioButton.setEnabled(enabled);

			filtersByFileOwnershipLabel.setEnabled(!enabled);
			filtersByFileOwnershipRadioButton.setEnabled(!enabled);
			filtersByFilePermissionsLabel.setEnabled(!enabled);
			filtersByFilePermissionsRadioButton.setEnabled(!enabled);


			//basics
			filtersByPatternRadioButton.setSelected(true);
			filtersIncludeRadioButton.setSelected(true);
			
			
			//by pattern
			filtersFilePatternRadioButton.setSelected(true);
			filtersPatternField.setText("*");
			filtersRegularExpressionCheckBox.setSelected(false);
			
			//by file size
			filtersSmallerFileSizeRadioButton.setSelected(true);
			filtersFileSizeField.setText("0");
			
			//by date
			filtersDateModeSpecificTimeRadioButton.setSelected(true);
			filtersDateModeTimeUnitRadioButton.setSelected(false);		
			filtersPathFilterField.setText("");
			filtersDateTextField.setText((new SimpleDateFormat(Const.DefaultDateFormat)).format(new Date()));
			filtersDateModeSpecificTimeRadioButton.setSelected(true);
			adjustFilterDateTypeRadioButtons();
			filtersEarlierDateRadioButton.setSelected(true);
			filtersDateDirFileRadioButton.setSelected(true);
			
			//by path
			filtersPathFilterField.setText("");
			
			//by Attribute
			filtersReadOnlyAttributeCheckBox.setSelected(false);
			filtersHiddenAttributeCheckBox.setSelected(false);
			filtersSystemAttributeCheckBox.setSelected(false);
			filtersArchiveAttributeCheckBox.setSelected(false);
		
			
			//by Ownership
			filtersOwnerFilterField.setText("");
			filtersGroupFilterField.setText("");
			
			//by Permission
			filtersPermissionFilterField.setText("000");
			permissionFilterValueChanged();
			
			((MyJTabbedPane) filtersTabbedPane).forceSetSelectedIndex(0);
			
			verifyFilterFields();
			
			filterEvent();
		}
	}
	
	protected void adjustFilterDateTypeRadioButtons(){
		if (filtersDateModeSpecificTimeRadioButton.isSelected()){
	        filtersDateSpecificTimeJLabelAfter.setEnabled(true);
	        filtersDateTextField.setEnabled(true);
	        filtersDateTimeUnitJLabelAfter.setEnabled(false);
	        filtersDateTimeUnitComboBox.setEnabled(false);
	        filtersDateTimeUnitField.setEnabled(false);
		}else{
			filtersDateSpecificTimeJLabelAfter.setEnabled(false);
	        filtersDateTextField.setEnabled(false);
	        filtersDateTimeUnitJLabelAfter.setEnabled(true);
	        filtersDateTimeUnitComboBox.setEnabled(true);
	        filtersDateTimeUnitField.setEnabled(true);
		}
	}

	protected void verifyFilterFields(){
		//generate a mouse event to clean fire 'verify' property
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(filtersFileSizeField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(filtersDateTextField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new MouseEvent(filtersPermissionFilterField, MouseEvent.MOUSE_CLICKED, 0, 0, -1, -1, 1, false));
	}
	
	protected void filterEvent(){
		filtersTabbedPane.setEnabledAt(1, false);
		enableFilterTab(FilterTabs.ByPattern, filtersByPatternRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.BySize, filtersByFileSizeRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.ByDate, filtersByDateRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.ByPath, filtersByPathRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.ByFileAttribute, filtersByFileAttributeRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.ByGroup, filtersByFileOwnershipRadioButton.isSelected(), false);
		enableFilterTab(FilterTabs.ByFilePermission, filtersByFilePermissionsRadioButton.isSelected(), false);
	}
	
	protected void applyFilter(){
		JobDialog jobDialog = DirSyncPro.getGui().getJobDialog();
		Job job = DirSyncPro.getGui().getSelectedJob();
		Filter.Action ac = (filtersIncludeRadioButton.isSelected() ? Filter.Action.Include : Filter.Action.Exclude);
		
		boolean isNew = false;
		if (filter == null){
			isNew = true;
			if (filtersByPatternRadioButton.isSelected()){
				filter = new FilterByPattern(job, ac);
			}else if(filtersByPathRadioButton.isSelected()){
				filter = new FilterByPath(job, ac);
			}else if(filtersByFileSizeRadioButton.isSelected()){
				filter = new FilterByFileSize(job, ac);
			}else if(filtersByFilePermissionsRadioButton.isSelected()){
				filter = new FilterByFilePermissions(job, ac);
			}else if(filtersByFileOwnershipRadioButton.isSelected()){
				filter = new FilterByFileOwnership(job, ac);
			}else if(filtersByFileAttributeRadioButton.isSelected()){
				filter = new FilterByFileAttribute(job, ac);
			}else if(filtersByDateRadioButton.isSelected()){
				filter = new FilterByDate(job, ac);
			}
		}else{
			filter.setAction(ac);
		}
		
		if (filter instanceof FilterByPattern){
			String pattern = filtersPatternField.getText();
			if (pattern.contains(";")){
				String[] patterns = pattern.split(";");
				for (String p : patterns){
					// for the first time we may have already initialized, but it's not that big deal.
					filter = new FilterByPattern(job, ac);
					((FilterByPattern) filter).setPattern(p);
					((FilterByPattern) filter).setPatternType(filtersFilePatternRadioButton.isSelected() ? FilterByPattern.FilterPatternType.File : FilterByPattern.FilterPatternType.Directory);
					DirSyncPro.getGui().getJobDialog().addFilter(filter);
				}
				filter = null; //causes not to be added once more at the end of this method
			}else{
				((FilterByPattern) filter).setPattern(filtersPatternField.getText());
				((FilterByPattern) filter).setPatternType(filtersFilePatternRadioButton.isSelected() ? FilterByPattern.FilterPatternType.File : FilterByPattern.FilterPatternType.Directory);
				((FilterByPattern) filter).setRegExp(filtersRegularExpressionCheckBox.isSelected());
			}
		}else if(filter instanceof FilterByPath){
			((FilterByPath) filter).setPathStr(filtersPathFilterField.getText());
		}else if(filter instanceof FilterByFileSize){
			((FilterByFileSize) filter).setValue(Long.valueOf(filtersFileSizeField.getText()));
			if (filtersSmallerFileSizeRadioButton.isSelected()){
				((FilterByFileSize) filter).setFileSizeType(FilterByFileSize.FilterFileSizeType.SmallerThan);
			}else if (filtersExactFileSizeRadioButton.isSelected()){
				((FilterByFileSize) filter).setFileSizeType(FilterByFileSize.FilterFileSizeType.Exactly);
			}else if (filtersLargerFileSizeRadioButton.isSelected()){
				((FilterByFileSize) filter).setFileSizeType(FilterByFileSize.FilterFileSizeType.LargerThan);
			}
		}else if(filter instanceof FilterByFilePermissions){
			((FilterByFilePermissions) filter).setOctalPermissionValue(filtersPermissionFilterField.getText());
		}else if(filter instanceof FilterByFileOwnership){
			((FilterByFileOwnership) filter).setOwnerStr(filtersOwnerFilterField.getText());
			((FilterByFileOwnership) filter).setGroupStr(filtersGroupFilterField.getText());
		}else if(filter instanceof FilterByFileAttribute){
			((FilterByFileAttribute) filter).setReadOnly(filtersReadOnlyAttributeCheckBox.isSelected());
			((FilterByFileAttribute) filter).setHidden(filtersHiddenAttributeCheckBox.isSelected());
			((FilterByFileAttribute) filter).setSystem(filtersSystemAttributeCheckBox.isSelected());
			((FilterByFileAttribute) filter).setArchive(filtersArchiveAttributeCheckBox.isSelected());
		}else if(filter instanceof FilterByDate){
		    	if(filtersDateModeSpecificTimeRadioButton.isSelected()){
		    	    ((FilterByDate) filter).setDateMode(FilterByDate.FilterDateMode.SpecificTime);
		    	}
			try{
				((FilterByDate) filter).setDate((new SimpleDateFormat(Const.DefaultDateFormat)).parse(filtersDateTextField.getText()));
			}catch (ParseException e){
				((FilterByDate) filter).setDate(null);
			}
			if (filtersEarlierDateRadioButton.isSelected()){
				((FilterByDate) filter).setDateType(FilterByDate.FilterDateType.EarlierThan);
			}else if (filtersSameDateRadioButton.isSelected()){
				((FilterByDate) filter).setDateType(FilterByDate.FilterDateType.ExactlyOn);
			}else if (filtersLaterDateRadioButton.isSelected()){
				((FilterByDate) filter).setDateType(FilterByDate.FilterDateType.LaterThan);
			}
			if(filtersDateModeTimeUnitRadioButton.isSelected()){
			    ((FilterByDate) filter).setDateMode(FilterByDate.FilterDateMode.TimeUnit);
			}
			String unitsNumberText = filtersDateTimeUnitField.getText();
			try{
			    ((FilterByDate) filter).setTimeUnitValue((unitsNumberText != null && !unitsNumberText.equals("")) ? Integer.parseInt(unitsNumberText) : 0);
			}catch(NumberFormatException e){				
			    ((FilterByDate) filter).setTimeUnitValue(0);
			}
			FilterByDate.FilterTimeUnitType filterUnitType = (FilterByDate.FilterTimeUnitType) filtersDateTimeUnitComboBox.getSelectedItem();
			((FilterByDate) filter).setUnitType(filterUnitType);
			
			if (filtersDateDirFileRadioButton.isSelected()) {
				((FilterByDate) filter).setDirFile(FilterDateDirFile.DirFile);
			}else if (filtersDateDirRadioButton.isSelected()) {
				((FilterByDate) filter).setDirFile(FilterDateDirFile.Dir);
			}else {
				((FilterByDate) filter).setDirFile(FilterDateDirFile.File);
			}
		}

		if (isNew){
			if (filter != null){
				DirSyncPro.getGui().getJobDialog().addFilter(filter);
			}
			//add filter updates the filterstree already
		}else{
			// if it is not a new filter, we have already the reference to it and it is changed already
			DirSyncPro.getGui().getJobDialog().updateFiltersTree();
		}

	}
	
	protected void permissionFilterCheckBoxClicked(){
		FilterByFilePermissions permFilter;
		if (filter != null){
			permFilter = (FilterByFilePermissions) filter.clone();
		}else{
			//dummy
			permFilter = new FilterByFilePermissions(null, null);
		}
		
		permFilter.setPosixFilePermission(PosixFilePermission.OWNER_READ, filtersPermissionFilterURCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.OWNER_WRITE, filtersPermissionFilterUWCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.OWNER_EXECUTE, filtersPermissionFilterUXCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.GROUP_READ, filtersPermissionFilterGRCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.GROUP_WRITE, filtersPermissionFilterGWCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.GROUP_EXECUTE, filtersPermissionFilterGXCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.OTHERS_READ, filtersPermissionFilterORCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.OTHERS_WRITE, filtersPermissionFilterOWCheckBox.isSelected());
		permFilter.setPosixFilePermission(PosixFilePermission.OTHERS_EXECUTE, filtersPermissionFilterOXCheckBox.isSelected());
		
		filtersPermissionFilterField.setText(permFilter.getOctalPermissionValue());
	}

	protected void permissionFilterValueChanged(){
		//fire validation
		FilterByFilePermissions  ft;
		if (filter != null){
			ft = (FilterByFilePermissions) filter.clone();
		}else{
			//dummy
			ft = new FilterByFilePermissions(null, null);
		}
		if (filtersPermissionFilterField.getText().length() == 3 && filtersPermissionFilterField.getText().matches("[0-7][0-7][0-7]")){
			ft.setOctalPermissionValue(filtersPermissionFilterField.getText());
		}
		filtersPermissionFilterURCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_READ));
		filtersPermissionFilterUWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_WRITE));
		filtersPermissionFilterUXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OWNER_EXECUTE));
		filtersPermissionFilterGRCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_READ));
		filtersPermissionFilterGWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_WRITE));
		filtersPermissionFilterGXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.GROUP_EXECUTE));
		filtersPermissionFilterORCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_READ));
		filtersPermissionFilterOWCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_WRITE));
		filtersPermissionFilterOXCheckBox.setSelected(((FilterByFilePermissions) ft).isPermissionSet(PosixFilePermission.OTHERS_EXECUTE));
	}
	
	public boolean isPatternRegularExpression(){
	    return filtersRegularExpressionCheckBox.isSelected();
	}
}
