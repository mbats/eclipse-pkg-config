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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
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
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Property tab to select packages and add pkg-config output
 * of checked packages to compiler and linker.
 * 
 */
public class PkgConfigPropertyTab extends AbstractCPropertyTab {

	private CheckboxTableViewer pkgCfgViewer;
	private ArrayList<Object> newItems = new ArrayList<Object>();
	private ArrayList<Object> removedItems = new ArrayList<Object>();
	private Set<Object> previouslyChecked;
	private boolean newItemToggle;
	private static final int BUTTON_SELECT = 0;
	private static final int BUTTON_DESELECT = 1;
	private final String PACKAGES = "packages";
	
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
	 * Add or remove include paths, library paths and libraries of the checked package.
	 */
	private void handleAddRemove() {
		IProject proj = page.getProject();
		if (newItemToggle) { //add
			for (Object item : newItems) {
				//handle options
				String cflags = PkgConfigUtil.pkgOutputCflags(item.toString());
				String[] optionsArray = Parser.parseCflagOptions(cflags);
				for (String option : optionsArray) {
					PathToToolOption.addOtherFlag(option, proj);
				}
				//handle include paths
				String[] incPathArray = Parser.parseIncPaths(cflags);
				for (String inc : incPathArray) {
					PathToToolOption.addIncludePath(inc, proj);
				}
				//handle library paths
				String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(item.toString());
				String[] libPathArray = Parser.parseLibPaths2(libPaths);
				for (String libPath : libPathArray) {
					PathToToolOption.addLibraryPath(libPath, proj);
				}
				//handle libraries
				String libs = PkgConfigUtil.pkgOutputLibFilesOnly(item.toString());
				String[] libArray = Parser.parseLibs2(libs);
				for (String lib : libArray) {
					PathToToolOption.addLib(lib, proj);
				}
			}
		} else { //remove
			//TODO: Make sure that values that are still needed by other packages are not removed
			for (Object item : removedItems) {
				//handle options
				String cflags = PkgConfigUtil.pkgOutputCflags(item.toString());
				String[] optionsArray = Parser.parseCflagOptions(cflags);
				for (String option : optionsArray) {
					PathToToolOption.removeOtherFlag(option, proj);
				}
				//handle include paths
				String[] incPathArray = Parser.parseIncPaths(cflags);
				for (String inc : incPathArray) {
					PathToToolOption.removeIncludePath(inc, proj);
				}
				//handle library paths
				String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(item.toString());
				String[] libPathArray = Parser.parseLibPaths2(libPaths);
				for (String libPath : libPathArray) {
					PathToToolOption.removeLibraryPath(libPath, proj);
				}
				//handle libraries
				String libs = PkgConfigUtil.pkgOutputLibFilesOnly(item.toString());
				String[] libArray = Parser.parseLibs2(libs);
				for (String lib : libArray) {
					PathToToolOption.removeLib(lib, proj);
				}
			}
		}
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
		} catch (CoreException e) {
			e.printStackTrace();
		}
		TableItem[] items = pkgCfgViewer.getTable().getItems();
		for(TableItem item : items) {
			String value = strgElem.getAttribute(item.getText());
			if(value!=null) {
				if(value.equals("true")) {
					item.setChecked(true);
				}
			}
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
			e.printStackTrace();
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
					strgElem.setAttribute(pkgName, chkd);
				} catch (Exception e) {
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
		Object[] elements = {};
		pkgCfgViewer.setCheckedElements(elements);
		
		//remove values from Tools Options
		handleCheckStateChange();
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
		ICConfigurationDescription confDesc = cfg.getConfiguration();
		ICProjectDescription projDesc = confDesc.getProjectDescription();
		try {
			CoreModel.getDefault().setProjectDescription(page.getProject(), projDesc);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		saveChecked();
	}

	@Override
	protected void updateButtons() {
	}
	
	/**
	 * Check state listener for the table viwer.
	 *
	 */
	public class PkgListener implements ICheckStateListener {

		@Override
		public void checkStateChanged(CheckStateChangedEvent e) {
			handleCheckStateChange();
		}
	}
	
	/**
	 * Action for the check state change.
	 */
	private void handleCheckStateChange() {
		newItemToggle = false;
		//get checked items
		Object[] checkedItems = getCheckedItems();
		
		//check if new items checked
		if(checkedItems.length > previouslyChecked.size()) {
			//add checked items to an array list
			for (Object o : checkedItems) {
				//if new item
				if (!previouslyChecked.contains(o)) {
					newItems.add(o);
					newItemToggle = true;
				}
			}
		} else if (checkedItems.length < previouslyChecked.size()) { //check if new items removed
			Set<Object> checkedItemsSet;
			//add removed items to an array list
			for (Object o : previouslyChecked) {
				//convert array to a set
				checkedItemsSet = new HashSet<Object>(Arrays.asList(checkedItems));
				//if item removed
				if (!checkedItemsSet.contains(o)) {
					removedItems.add(o);
					newItemToggle = false;
				}
			}
		}
		
		handleAddRemove();
		updateData(getResDesc());
		previouslyChecked = new HashSet<Object>(Arrays.asList(checkedItems));
		newItems.clear();
		removedItems.clear();
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
		if (selected.length>0) {
			newItemToggle = true;
		}
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
		if (selected.length>0) {
			newItemToggle = false;
		}
		for (TableItem itm : selected) {
			itm.setChecked(false);
		}
		handleCheckStateChange();
	}
	
}