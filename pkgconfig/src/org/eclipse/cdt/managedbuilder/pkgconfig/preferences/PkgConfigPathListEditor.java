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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
import org.eclipse.cdt.ui.newui.MultiCfgContributedEnvironment;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * New implementation of PkgConfigListEditor.
 * Used to select PKG_CONFIG_PATH values from the dialog.
 * 
 * TODO: Fix issue: Environment variables disappear when the workspace is restarted
 * 
 */
public class PkgConfigPathListEditor extends PkgConfigListEditor {

	private static final String SEPARATOR = System.getProperty("path.separator", ";"); //$NON-NLS-1$ //$NON-NLS-2$
	private ICConfigurationDescription cfgd = null;
	private final MultiCfgContributedEnvironment ce = new MultiCfgContributedEnvironment();
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	PkgConfigPathListEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}
	
	@Override
	/**
	 * Functionality for New button.
	 * Shows a browser dialog to select a directory and returns that directory path.
	 */
	protected String getNewInputObject() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());
		final Text text = new Text(getShell(), SWT.BORDER);
		dlg.setFilterPath(text.getText());
		dlg.setText(Messages.PkgConfigPathListEditor_0);
		dlg.setMessage(Messages.PkgConfigPathListEditor_1);
		String dir = dlg.open();
		if(dir == null) {
			return null;
		}
		//remove white spaces
		dir = dir.trim();
		if (dir.length()!=0) {
			//get all existing items in the list
			String[] existingItems = getList().getItems();
			//check that the list doesn't already contain the added item
			if (existingItems.length>0) {
				//return null if duplicate item found
				for (String item : existingItems) {
					if (item.equalsIgnoreCase(dir)) {
						return null;
					}
				}					
			}
			//add a new PKG_CONFIG_PATH to the preference store
			PreferenceStore.appendPkgConfigPath(dir);
			
			/*
			 * add a new PKG_CONFIG_PATH environment variable
			 * to every project's every build configuration
			 */
//			ICConfigurationDescription[] cfgs;
//			cfgs = new ICConfigurationDescription[] {cfgd};
//			for (ICConfigurationDescription cfg : cfgs) { 
//				ce.addVariable("PKG_CONFIG_PATH", PreferenceStore.getPkgConfigPath(), 
//						IEnvironmentVariable.ENVVAR_APPEND, 
//						SEPARATOR, cfg);
//			}
			
			//get all project in the workspace
			IProject[] projects = PathToToolOption.getProjectsInWorkspace();
			
			IContributedEnvironment ice = CCorePlugin.getDefault()
					.getBuildEnvironmentManager().getContributedEnvironment();

			for (IProject proj : projects) {
				ICProjectDescription projDesc = CoreModel.getDefault()
						.getProjectDescription(proj, true);

				ICConfigurationDescription[] cfgs;
				cfgs = new ICConfigurationDescription[] {cfgd};
				for (ICConfigurationDescription cfg : cfgs) { 
					ice.addVariable("PKG_CONFIG_PATH", PreferenceStore.getPkgConfigPath(),
							IEnvironmentVariable.ENVVAR_APPEND, ";", cfg);
					try {	
						CoreModel.getDefault().setProjectDescription(proj, projDesc);
//						CCorePlugin.getDefault().setProjectDescription(proj, projDesc);
					} catch (CoreException e) {
					}
				}
			}
	
//			if (cfgd != null) {
//				ce.appendEnvironment(cfgd);
//			} 
			return dir;
		}
		return null;
	}

	@Override
	/**
	 * Removes the path from the list as well as from the Tool's Option.
	 */
	protected void removePressed() {
		List incList = getList();
        setPresentsDefaultValue(false);
        String[] selected = incList.getSelection();
        for (String s : selected) {
            //remove PKG_CONFIG_PATH from the preference store
            PreferenceStore.removePkgConfigPath(s);
    		/*
    		 * remove one entry of PKG_CONFIG_PATH environment variable
    		 * from every project's every build configuration
    		 */
    		ICConfigurationDescription[] cfgs;
    		cfgs = new ICConfigurationDescription[] {cfgd};
    		for (ICConfigurationDescription cfg : cfgs) { 
    			ce.addVariable("PKG_CONFIG_PATH", PreferenceStore.getPkgConfigPath(), 
    					IEnvironmentVariable.ENVVAR_APPEND, 
    					SEPARATOR, cfg);
    		}
    		incList.remove(s);
    		selectionChanged();
        }
	}
	
}