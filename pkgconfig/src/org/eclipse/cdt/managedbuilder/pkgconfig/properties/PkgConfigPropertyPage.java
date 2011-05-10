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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.pkgconfig.util.OSDetector;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PropertyPage;

public class PkgConfigPropertyPage extends PropertyPage {

	private static final String LIST_ALL = "pkg-config --list-all";
	private Table table;
	private CheckboxTableViewer pkgConfigViewer;

	/**
	 * Constructor.
	 */
	public PkgConfigPropertyPage() {
		super();
	}

	private void addPackageTable(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		table = new Table(composite, SWT.CHECK);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		pkgConfigViewer = new CheckboxTableViewer(table);
//		pkgConfigViewer.setContentProvider(new ViewContentProvider());
//		pkgConfigViewer.setLabelProvider(new ViewLabelProvider());
		
		for (String pkg : parsePackageList(getAllPackages())) {
			pkgConfigViewer.add(pkg);
		}
		
		pkgConfigViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				TableItem itm = table.getSelection()[0];
				if (itm.getChecked()) {
					itm.setChecked(false);
				} else {
					itm.setChecked(true);
				}
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

//	public boolean performOk() {
//
//	}

	/**
	 * Get all packages that pkg-config utility finds (package name with description).
	 * 
	 * @return
	 */
	private ArrayList<String> getAllPackages() {
		ProcessBuilder pb = null;
		if (OSDetector.isUnix()) {
			pb = new ProcessBuilder("bash", "-c", LIST_ALL);	//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isWindows()) {
			pb = new ProcessBuilder("cmd", "/c", LIST_ALL);		//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isMac()) {
			pb = new ProcessBuilder("bash", "-c", LIST_ALL);	//$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader
					(new InputStreamReader(p.getInputStream()));
			ArrayList<String> packageList = new ArrayList<String>();
			do {
				line = input.readLine();
				if (line != null) {
					packageList.add(line);
				}
			} while(line != null);
			input.close();
			return packageList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
	 * Get selected TableItems.
	 * 
	 * @return
	 */
	protected TableItem[] getSelected() {
		return table.getSelection();
	}
	
}