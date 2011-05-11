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

import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class PkgConfigPropertyPage extends PropertyPage implements IWorkbenchPropertyPage{

	private Table tbl;
	private CheckboxTableViewer pkgCfgViewer;

	/**
	 * Constructor.
	 */
	public PkgConfigPropertyPage() {
		super();
	}

	private void addPackageTable(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		tbl = new Table(composite, SWT.CHECK);
		tbl.setLinesVisible(true);
		tbl.setHeaderVisible(true);

		pkgCfgViewer = new CheckboxTableViewer(tbl);
//		pkgConfigViewer.setContentProvider(new ViewContentProvider());
//		pkgConfigViewer.setLabelProvider(new ViewLabelProvider());
		
		for (String pkg : parsePackageList(PkgConfigUtil.getAllPackages())) {
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

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addPackageTable(composite);
		return composite;
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
	 * Parse package list so that only package names are added to ArrayList.
	 * 
	 * @param packages
	 * @return
	 */
	private ArrayList<String> parsePackageList(ArrayList<String> packages) {
		ArrayList<String> operated = new ArrayList<String>();
		for (String s : packages) {
			//cut the string after the first white space
			int end = s.indexOf(" ");
			operated.add(s.substring(0, end));
		}
		return operated;
	}
	
	/**
	 * Parse package list that only package descriptions are added to ArrayList.
	 * 
	 * @param packages
	 * @return
	 */
	private ArrayList<String> parseDescription(ArrayList<String> packages) {
		ArrayList<String> operated = new ArrayList<String>();
		int ws, start = 0;
		for (String s : packages) {
			ws = s.indexOf(" ");
			//read as many characters forward that non white space is found
			find: for (int i=1; i+ws<s.length(); i++) {
				if (s.charAt(ws+i) != ' ') {
					start = ws+i;
					break find;
				}
			}
			operated.add(s.substring(start, s.length()));
		}
		return operated;
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
	
//	public boolean performOk() {
//		chkdItms = getCheckedItems();
//		return super.performOk();
//	}
	
}