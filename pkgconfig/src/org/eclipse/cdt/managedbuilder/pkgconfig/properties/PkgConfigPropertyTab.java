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

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

/**
 * Property tab to select packages.
 * 
 */
public class PkgConfigPropertyTab extends AbstractCPropertyTab {

	private Table tbl;
	private CheckboxTableViewer pkgCfgViewer;
	private static final int DEFAULT_HEIGHT = 250;
	
	protected SashForm sashForm;
	protected Composite parserGroup;
	
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
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				handleSelectionChanged();
				updateButtons();
			}});
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
//				saveChecked();
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

		parserGroup = new Composite(sashForm, SWT.NULL);
		GridData gd = new GridData();
		parserGroup.setLayout(new TabFolderLayout());

		PixelConverter converter = new PixelConverter(parent);
		gd.heightHint = converter.convertHorizontalDLUsToPixels(DEFAULT_HEIGHT);

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		parserGroup.setLayoutData(gd);

		sashForm.setWeights(new int[] {100, 100});
		initializeValues();
	}
	
	/**
	 * Get selected item(s).
	 * 
	 * @return
	 */
	private Object[] getSelected() {
		Object[] obs = ((IStructuredSelection)pkgCfgViewer.getSelection()).toArray();
		return obs;
	}
	
	/**
	 * Get checked items.
	 * @return
	 */
	private Object[] getCheckedItems() {
		return pkgCfgViewer.getCheckedElements();
	}
	
	private void handleSelectionChanged(){
		//test
		Object[] pkgs = getCheckedItems();
		for (Object pkg : pkgs) {
			//handle include paths
			String incPaths = PkgConfigUtil.pkgOutputCflags(pkg.toString());
			String[] incPathArray = Parser.parseIncPaths(incPaths);
			for (String inc : incPathArray) {
				PathToToolOption.addIncludePath(inc);
			}
			//handle library paths
			String libPaths = PkgConfigUtil.pkgOutputLibs(pkg.toString());
			String[] libPathArray = Parser.parseLibPaths(libPaths);
			for (String libPath : libPathArray) {
				PathToToolOption.addLibraryPath(libPath);
			}
			//handle libraries
			String libs = PkgConfigUtil.pkgOutputLibs(pkg.toString());
			String[] libArray = Parser.parseLibs(libs);
			for (String lib : libArray) {
				PathToToolOption.addLib(lib);
			}
		}
	}
	
	private void storeValues() { //TODO: Find out how to save the state of checked checkboxes
		
	}
	
	/**
	 * Initialize package list.
	 */
	private void initializeValues() {
		for (String pkg : Parser.parsePackageList(PkgConfigUtil.getAllPackages())) {
			pkgCfgViewer.add(pkg);
		}
	}

	@Override
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {
		
	}

	@Override
	protected void performDefaults() {
		
	}

	@Override
	protected void updateData(ICResourceDescription cfg) {
		
	}

	@Override
	protected void updateButtons() {
		
	}

}