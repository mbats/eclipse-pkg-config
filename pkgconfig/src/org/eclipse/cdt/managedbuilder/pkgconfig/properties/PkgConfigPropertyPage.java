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
	private CheckboxTableViewer pkgConfigViewer;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public PkgConfigPropertyPage() {
		super();
	}

	private void addFirstSection(Composite parent) {
//		Composite composite = createDefaultComposite(parent);

		final Table table = new Table(parent,SWT.CHECK);
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

		addFirstSection(composite);
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

	protected void performDefaults() {
		super.performDefaults();
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
			pb = new ProcessBuilder("cmd", "-c", LIST_ALL);		//$NON-NLS-1$ //$NON-NLS-2$
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
		for (String s : packages) {
			int ws = s.indexOf(" ");
			//read as many characters forward that non white space is found
			int start = 0;
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
	
}