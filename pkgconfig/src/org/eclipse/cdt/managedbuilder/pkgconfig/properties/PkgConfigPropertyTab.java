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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

public class PkgConfigPropertyTab extends AbstractCPropertyTab {

	private Table tbl;
	private CheckboxTableViewer pkgCfgViewer;

	/**
	 * Constructor.
	 */
	public PkgConfigPropertyTab() {
	}

	private void addPackageTable(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		tbl = new Table(composite, SWT.CHECK);
		tbl.setLinesVisible(true);
		tbl.setHeaderVisible(true);

		pkgCfgViewer = new CheckboxTableViewer(tbl);
//		pkgConfigViewer.setContentProvider(new ViewContentProvider());
//		pkgConfigViewer.setLabelProvider(new ViewLabelProvider());
		
		for (String pkg : Parser.parsePackageList(PkgConfigUtil.getAllPackages())) {
			pkgCfgViewer.add(pkg);
		}
		
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
		
		pkgCfgViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
//		data.grabExcessHorizontalSpace = true;
		parent.setLayoutData(data);
		addPackageTable(parent);
	}
	
	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
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
	
	/**
	 * 
	 * @param event
	 */
	private void handleSelectionChanged(SelectionChangedEvent event){
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
	
	private void initializeValues() {
		
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