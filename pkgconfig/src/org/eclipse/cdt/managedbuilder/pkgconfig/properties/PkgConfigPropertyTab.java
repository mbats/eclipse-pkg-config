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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Property tab to select packages and add pkg-config output
 * to compiler & linker from checked packages
 * 
 * TODO: Save selected packages to .cproject
 * TODO: Initialize selected packages from .cproject
 */
public class PkgConfigPropertyTab extends AbstractCPropertyTab {

	private Table tbl;
	private CheckboxTableViewer pkgCfgViewer;
	private Object newChecked;
	private Object removedItem;
	private boolean newItemToggle;
	private Set<Object> set = new HashSet<Object>();
	
	protected SashForm sashForm;
	protected Composite comp;
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setBackground(sashForm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		Composite c1 = new Composite(sashForm, SWT.NONE);
		c1.setLayout(new GridLayout(2, false));
		setupLabel(c1, PropertyConstants.PkgConfigTab, 2, GridData.FILL_HORIZONTAL); 
		tbl = new Table(c1, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		tbl.setLayoutData(new GridData(GridData.FILL_BOTH));
//		tbl.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				handleSelectionChanged();
////				updateButtons();
//			}});
		pkgCfgViewer = new CheckboxTableViewer(tbl);
		pkgCfgViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});

		pkgCfgViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
//				storeValues(); //TODO: uncomment when implemented
				newItemToggle = false;
				//get checked items
				Object[] checkedItems = getCheckedItems();
				
				//check for added item
				if (checkedItems.length > set.size()) {
					//compare to old set
					for (Object o : checkedItems) {
						if (!set.contains(o)) {
							newChecked = o;
							set.add(o); //populate set with new item
							newItemToggle = true;
						}
					}
				}
				
				//check for removed item
				if (checkedItems.length < set.size()) {
					List<Object> list = Arrays.asList(checkedItems);
				    Set<Object> newSet = new HashSet<Object>(list);
				    Object[] oldCheckedItems = set.toArray();
				    for (Object o : oldCheckedItems) {
				    	if(!newSet.contains(o)) {
				    		removedItem = o;
				    		newItemToggle = false;
				    	}
				    }
				}
				handleCheckStateChanged();
			}});

		pkgCfgViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TableItem itm = tbl.getSelection()[0];
				if (itm.getChecked()) {
					itm.setChecked(false);
				} else {
					itm.setChecked(true);
				}
			}
		});
		
		Composite c = new Composite(c1, SWT.NONE);
		c.setLayoutData(new GridData(GridData.END));

		comp = new Composite(sashForm, SWT.NULL);
		GridData gd = new GridData();
		comp.setLayout(new TabFolderLayout());

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		comp.setLayoutData(gd);

		sashForm.setWeights(new int[] {100, 100});
		initializeValues();
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
	private void handleCheckStateChanged() {
		IProject proj = page.getProject();
		//handle include paths
		if (newItemToggle) {
			String incPaths = PkgConfigUtil.pkgOutputCflags(newChecked.toString());
			String[] incPathArray = Parser.parseIncPaths(incPaths);
			for (String inc : incPathArray) {
				PathToToolOption.addIncludePath(inc, proj);
			}
		} else {
			String incPaths = PkgConfigUtil.pkgOutputCflags(removedItem.toString());
			String[] incPathArray = Parser.parseIncPaths(incPaths);
			for (String inc : incPathArray) {
				PathToToolOption.removeIncludePath(inc, proj);
			}
		}
		//handle library paths
		if (newItemToggle) {
			String libPaths = PkgConfigUtil.pkgOutputLibs(newChecked.toString());
			String[] libPathArray = Parser.parseLibPaths(libPaths);
			for (String libPath : libPathArray) {
				PathToToolOption.addLibraryPath(libPath, proj);
			}
		} else {
			String libPaths = PkgConfigUtil.pkgOutputLibs(removedItem.toString());
			String[] libPathArray = Parser.parseLibPaths(libPaths);
			for (String libPath : libPathArray) {
				PathToToolOption.removeLibraryPath(libPath, proj);
			}
		}
		//handle libraries
		if (newItemToggle) {
			String libs = PkgConfigUtil.pkgOutputLibs(newChecked.toString());
			String[] libArray = Parser.parseLibs(libs);
			for (String lib : libArray) {
				PathToToolOption.addLib(lib, proj);
			}
		} else {
			String libs = PkgConfigUtil.pkgOutputLibs(removedItem.toString());
			String[] libArray = Parser.parseLibs(libs);
			for (String lib : libArray) {
				PathToToolOption.removeLib(lib, proj);
			}
		}
	}
	
	/**
	 * Get selected table item.
	 * @return
	 */
	private TableItem getSelectedItem() {
		TableItem itm = tbl.getSelection()[0];
		return itm;
	}
	
	private void handleSelectionChanged() {
	}
	
//	/**
//	 * Get selected item(s).
//	 * Only needed if multiselection with select/deselect button is implemented.
//	 * 
//	 * @return
//	 */
//	private Object[] getSelected() {
//		Object[] obs = ((IStructuredSelection)pkgCfgViewer.getSelection()).toArray();
//		return obs;
//	}
	
	private void storeValues() { 
		//TODO: Find out how to save the state of checked checkboxes
		//TODO: Save to .cproject
	}
	
	/**
	 * Initialize package list.
	 */
	private void initializeValues() {
		ArrayList<String> pkgs = Parser.parsePackageList(PkgConfigUtil.getAllPackages());
		Collections.sort(pkgs, String.CASE_INSENSITIVE_ORDER);
		for (String pkg : pkgs) {
			pkgCfgViewer.add(pkg);
		}
	}

	@Override
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
	}

	@Override
	protected void performDefaults() {
		//uncheck every checkbox
		Object[] elements = {};
		pkgCfgViewer.setCheckedElements(elements);
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
	}

	@Override
	protected void updateButtons() {
	}
	
	public IProject getProject() {
		IProject proj = page.getProject();
		return proj;
	}
	
	public IProject getSelectedProject() {
		IEditorPart ed = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if(ed  != null) {
		    IFileEditorInput input = (IFileEditorInput)ed.getEditorInput() ;
		    IFile file = input.getFile();
		    IProject activeProject = file.getProject();
		    return activeProject;
		}
		return null;
	}
	
}