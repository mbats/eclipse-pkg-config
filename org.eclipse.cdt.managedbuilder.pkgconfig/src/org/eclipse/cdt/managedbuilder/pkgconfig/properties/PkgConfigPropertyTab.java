/*******************************************************************************
 * Copyright (c) 2011 Petri Tuononen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Petri Tuononen - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.pkgconfig.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.cdt.managedbuilder.pkgconfig.settings.PkgConfigExternalSettingProvider;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Property tab to select packages and add pkg-config output
 * of checked packages to compiler and linker (MBS).
 * 
 */
public class PkgConfigPropertyTab extends AbstractCPropertyTab {

	private CheckboxTableViewer pkgCfgViewer;
	private Set<Object> previouslyChecked;
	private ArrayList<Object> newItems = new ArrayList<Object>();
	private ArrayList<Object> removedItems = new ArrayList<Object>();
	private static final int BUTTON_SELECT = 0;
	private static final int BUTTON_DESELECT = 1;
	private final String PACKAGES = "packages";
	private boolean reindexToggle = false;

	private SashForm sashForm;

	private static final String[] BUTTONS = new String[] {
		"Select",
		"Deselect"
	};

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setBackground(sashForm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		Composite c1 = new Composite(sashForm, SWT.NONE);
		GridLayout layout2 = new GridLayout(3, false);
		c1.setLayout(layout2);

		pkgCfgViewer = CheckboxTableViewer.newCheckList(c1, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table tbl = pkgCfgViewer.getTable();
		tbl.setHeaderVisible(true);
		tbl.setLinesVisible(true);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		tbl.setLayoutData(gd);

		createColumns(c1, pkgCfgViewer);
		pkgCfgViewer.setContentProvider(new ArrayContentProvider());
		pkgCfgViewer.setInput(DataModelProvider.INSTANCE.getEntries());

		pkgCfgViewer.addCheckStateListener(new PkgListener());

		pkgCfgViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TableItem itm = tbl.getSelection()[0];
				if (itm.getChecked()) {
					itm.setChecked(false);
				} else {
					itm.setChecked(true);
				}
				handleCheckStateChange();
			}
		});

		//buttons
		Composite compositeButtons = new Composite(c1, SWT.NONE);
		initButtons(compositeButtons, BUTTONS);

		initializePackageStates();
		previouslyChecked = new HashSet<Object>(Arrays.asList(getCheckedItems()));
	}

	/**
	 * Get checked items.
	 * @return
	 */
	private Object[] getCheckedItems() {
		return pkgCfgViewer.getCheckedElements();
	}

	/**
	 * Action for the check state change.
	 */
	private void handleCheckStateChange() {
		Object[] checkedItems = getCheckedItems();

		//check if new items checked
		if(checkedItems.length > previouslyChecked.size()) {
			//add checked items to an array list
			for (Object o : checkedItems) {
				//if new item
				if (!previouslyChecked.contains(o)) {
					newItems.add(o);
				}
			}
			addPackageValues(newItems.toArray(), page.getProject());
			reindexToggle = true;
		} else if (checkedItems.length < previouslyChecked.size()) { //check if new items removed
			Set<Object> checkedItemsSet;
			//add removed items to an array list
			for (Object o : previouslyChecked) {
				//convert array to a set
				checkedItemsSet = new HashSet<Object>(Arrays.asList(checkedItems));
				//if item removed
				if (!checkedItemsSet.contains(o)) {
					removedItems.add(o);
				}
			}
			removePackageValues(removedItems.toArray(), page.getProject());
			reindexToggle = false;
		}

		saveChecked();
		updateData(getResDesc());
		previouslyChecked = new HashSet<Object>(Arrays.asList(checkedItems));
		newItems.clear();
		removedItems.clear();
	}

	/**
	 * Add new flags that the packages need to Tools' Options
	 * 
	 * @param addedItems Object[]
	 * @param proj IProject
	 */
	private void addPackageValues(Object[] addedItems, IProject proj) {
		for (Object item : addedItems) {
			//handle options
			String cflags = PkgConfigUtil.getCflags(item.toString());
			String[] optionsArray = Parser.parseCflagOptions(cflags);
			if (optionsArray!=null) {
				for (String option : optionsArray) {
					PathToToolOption.addOtherFlag(option, proj);
				}
			}
		}
		ManagedBuildManager.saveBuildInfo(proj, true);
	}

	/**
	 * Makes sure that only the flags that are not needed by other packages 
	 * are removed.
	 * 
	 * @param removedItems Object[]
	 */
	private void removePackageValues(Object[] removedItems, IProject proj) {
		String rCflags;
		String cCflags;
		String[] rOptionArray;
		String[] cOptionArray;
		HashMap<String, Boolean> optionMap;

		Object[] checkedItems = getCheckedItems();
		//make sure that the checked items don't contain removed items
		List<Object> checkedList = Arrays.asList(checkedItems);
		for (Object removedItem : removedItems) {
			if (checkedList.contains(removedItem)) {
				checkedList.remove(removedItem);
			}
		}

		for (Object removedPkg : removedItems) {
			//get arrays of removed package flags
			rCflags = PkgConfigUtil.getCflags(removedPkg.toString());
			rOptionArray = Parser.parseCflagOptions(rCflags);

			if (rOptionArray!=null) {
				//load HashMaps
				optionMap = new HashMap<String, Boolean>();
				for (String rO : rOptionArray) {
					optionMap.put(rO, true);
				}

				/*
				 * flag is free to be removed only if none of the remaining
				 * checked packages have the flag.
				 */
				for (Object checked : checkedList) {
					//get arrays of checked package flags
					cCflags = PkgConfigUtil.getCflags(checked.toString());
					cOptionArray = Parser.parseCflagOptions(cCflags);

					//check options 
					List<String> optionsList = Arrays.asList(cOptionArray);
					for (String option : rOptionArray) {
						if (optionsList.contains(option)) {
							optionMap.put(option, false);
						} else {
							optionMap.put(option, true);
						}
					}

				} //end of checked items loop
				//remove unneeded options
				for (Entry<String, Boolean> entry : optionMap.entrySet()) {
					if (entry.getValue() == true) {
						PathToToolOption.removeOtherFlag(entry.getKey(), proj);
					}
				}
			}
		} //end of removed items loop
		ManagedBuildManager.saveBuildInfo(proj, true);
	}

	/**
	 * Initializes the check state of the packages from the storage.
	 */
	private void initializePackageStates() {
		ICConfigurationDescription desc = getResDesc().getConfiguration();
		ICStorageElement strgElem = null;
		try {
			strgElem = desc.getStorage(PACKAGES, true);
			TableItem[] items = pkgCfgViewer.getTable().getItems();
			String value = null;
			for(TableItem item : items) {
				/*
				 * The package names with + symbols were converted so that
				 * + -> plus in order to prevent an error when saving to
				 * ICStorageElement.
				 */
				if (item.getText().contains("+")) {
					String newItemName = item.getText().replace("+", "plus");
					value = strgElem.getAttribute(newItemName);
				} else {
					value = strgElem.getAttribute(item.getText());
				}
				if(value!=null) {
					if(value.equals("true")) {
						item.setChecked(true);
					}
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().log(e, "Initialization of packages failed.");
		}
	}

	/**
	 * Saves checked state of the packages.
	 */
	private void saveChecked() { 
		ICConfigurationDescription desc = getResDesc().getConfiguration();
		ICStorageElement strgElem = null;
		//get storage or create one if it doesn't exist
		try {
			strgElem = desc.getStorage(PACKAGES, true);
		} catch (CoreException e) {
			Activator.getDefault().log(e, "Getting packages from the storage failed.");
		}

		TableItem[] items = pkgCfgViewer.getTable().getItems();
		for(TableItem item : items) {
			if(item != null) {
				String chkd;
				//form literal form of boolean state
				if(item.getChecked()) {
					chkd = "true";
				} else {
					chkd = "false";
				}
				/*
				 * add package name and the checkbox state
				 * to the storage
				 */
				try {  
					String pkgName = item.getText();
					//need to convert + symbols to "plus"
					if (pkgName.contains("+")) {
						String newPkgName = pkgName.replace("+", "plus");
						strgElem.setAttribute(newPkgName, chkd);
					} else {
						strgElem.setAttribute(pkgName, chkd);
					}
				} catch (Exception e) {
					Activator.getDefault().log(e, "Setting attribute to ICStorageElement failed.");
					//Seems like ICStorageElement cannot store Strings with +
					/*
					 * INVALID_CHARACTER_ERR: An invalid or
					 * illegal XML character is specified. 
					 */
				}
			}
		}
	}

	@Override
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		updateData(getResDesc());
	}

	@Override
	protected void performDefaults() {
		//uncheck every checkbox
		pkgCfgViewer.setCheckedElements(new Object[] {});

		//remove values from Tools Options
		handleCheckStateChange();
	}

	@Override
	protected void performOK() {
		//freshen index if new packages have been selected
		if (reindexToggle) {
			rebuiltIndex();
		}
		reindexToggle = false;
	}

	@Override
	protected void updateButtons() {
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
		final ICConfigurationDescription confDesc = cfg.getConfiguration();
		ICProjectDescription projDesc = confDesc.getProjectDescription();

		Job j = new Job("Update Pkg-config exernal settings provider") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				//a set holding external setting providers
				Set<String> externalSettingsProviders = new
						LinkedHashSet<String>(Arrays.asList(confDesc.getExternalSettingsProviderIds()));

				//remove pkg-config external setting provider
				externalSettingsProviders.remove(PkgConfigExternalSettingProvider.ID);
				confDesc.setExternalSettingsProviderIds(externalSettingsProviders.toArray(new
						String[externalSettingsProviders.size()]));

				//add pkg-config external setting provider
				externalSettingsProviders.add(PkgConfigExternalSettingProvider.ID);
				confDesc.setExternalSettingsProviderIds(externalSettingsProviders.toArray(new
						String[externalSettingsProviders.size()]));

				//update external setting providers
				confDesc.updateExternalSettingsProviders(new String[] {PkgConfigExternalSettingProvider.ID});
				return Status.OK_STATUS;
			}
		};
		j.setPriority(Job.INTERACTIVE);
		j.schedule();

		try {
			CoreModel.getDefault().setProjectDescription(page.getProject(), projDesc);
		} catch (CoreException e) {
			Activator.getDefault().log(e, "Setting/updating the project description failed.");
		}
	}

	/**
	 * Check state listener for the table viewer.
	 *
	 */
	public class PkgListener implements ICheckStateListener {

		@Override
		public void checkStateChanged(CheckStateChangedEvent e) {
			handleCheckStateChange();
		}
	}

	/**
	 * Creates table columns, headers and sets the size of the columns.
	 * 
	 * @param parent
	 * @param viewer
	 */
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Packages", "Description" };
		int[] bounds = { 200, 450 };

		//first column is for the package
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DataModel dm = (DataModel) element;
				return dm.getPackage();
			}
		});

		//second column is for the description
		col = createTableViewerColumn(titles[1], bounds[1]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DataModel dm = (DataModel) element;
				return dm.getDescription();
			}
		});		
	}

	/**
	 * Creates a column for the table viewer.
	 * 
	 * @param title
	 * @param bound
	 * @return
	 */
	private TableViewerColumn createTableViewerColumn(String title, int bound) {

		final TableViewerColumn viewerColumn = new TableViewerColumn(pkgCfgViewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();

		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);

		return viewerColumn;
	}

	/**
	 * Get selected item(s).
	 * 
	 * @return
	 */
	private TableItem[] getSelected() {
		TableItem[] selected = pkgCfgViewer.getTable().getSelection();
		return selected;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
	 */
	@Override
	public void buttonPressed (int n) {
		switch (n) {
		case BUTTON_SELECT:
			selectedButtonPressed();
			break;
		case BUTTON_DESELECT:
			deselectedButtonPressed();
			break;
		default:
			break;
		}
		updateButtons();
	}

	/**
	 * Action for the Select button.
	 */
	private void selectedButtonPressed() {
		TableItem[] selected = getSelected();
		for (TableItem itm : selected) {
			itm.setChecked(true);
		}
		handleCheckStateChange();
	}

	/**
	 * Action for the Deselect button.
	 */
	private void deselectedButtonPressed() {
		TableItem[] selected = getSelected();
		for (TableItem itm : selected) {
			itm.setChecked(false);
		}
		handleCheckStateChange();
	}

	/**
	 * Rebuilts the index of the selected project in the workspace.
	 */
	private void rebuiltIndex() {
		ICProject cproject = CoreModel.getDefault().getCModel().getCProject(page.getProject().getName());
		CCorePlugin.getIndexManager().reindex(cproject);
	}

}