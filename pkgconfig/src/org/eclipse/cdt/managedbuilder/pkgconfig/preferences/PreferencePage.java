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
package org.eclipse.cdt.managedbuilder.pkgconfig.preferences;


import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog.
 * 
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class.
 * 
 */
public class PreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 * Set preference page to use the pkg-config preference store.
	 */
	public PreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.PreferencePage_0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	/**
	 * Get Description name.
	 * 
	 * @param String Description
	 */
	public String getDescription() {
		return null;
	}

	@Override
	/**
	 * Creates field editors for the preference page.
	 */
	protected void createFieldEditors() {
		//list editor for PKG_CONFIG_PATH values
		PkgConfigPathListEditor includePathListEditor = new PkgConfigPathListEditor(
				PreferenceConstants.PKG_CONFIG_PATH, Messages.PreferencePage_1, 
				getFieldEditorParent());
		addField(includePathListEditor);
		//field PKG_CONFIG_LIBDIR
		addField(new LibDirFieldEditor(PreferenceConstants.PKG_CONFIG_LIBDIR, 
				Messages.PreferencePage_2, getFieldEditorParent())); 
	}

}