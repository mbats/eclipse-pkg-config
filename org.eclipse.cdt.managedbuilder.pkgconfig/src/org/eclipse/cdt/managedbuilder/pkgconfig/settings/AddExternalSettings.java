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
package org.eclipse.cdt.managedbuilder.pkgconfig.settings;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Adds or removes the external settings from the selected project
 */
public class AddExternalSettings implements IObjectActionDelegate {

	//selected project
	private IProject proj;

	public void setActivePart(final IAction pr_Action, final IWorkbenchPart pr_TargetPart) {
	}

	public void run(final IAction pr_Action) {

		CoreModel model = CoreModel.getDefault();
		ICProjectDescription projDes = model.getProjectDescription(proj);

		boolean added = false;        

		ICConfigurationDescription[] cfgDes = projDes.getConfigurations();
		for (int i = 0; i < cfgDes.length; i++) {
			String[] settingIds = cfgDes[i].getExternalSettingsProviderIds();

			//see if the external settings are already added for this project.
			int pos = -1;
			for (int j = 0; j < settingIds.length; j++) {
				if (settingIds[j].equals(PkgConfigSettingsProvider.ID)) { 
					pos = j;
					break;
				}
			}

			//If the external settings are not added then add them.
			if (pos == -1) {
				String[] tmp = new String[settingIds.length + 1];
				System.arraycopy(settingIds, 0, tmp, 0, settingIds.length);
				tmp[tmp.length - 1] = PkgConfigSettingsProvider.ID; 
				settingIds = tmp;
				added = true;
			} else {
				//the external settings are added so remove them.
				String[] tmp = new String[settingIds.length - 1];
				for (int j = 0; j < settingIds.length; j++) {
					if (j != pos) {
						if (j < pos) {
							tmp[j] = settingIds[j];
						} else {
							tmp[j - 1] = settingIds[j];
						}
					}
				}
				settingIds = tmp;
				added = false;                
			}            
			/* update the configuration description with
			 * the new External setting providers.
			 */
			cfgDes[i].setExternalSettingsProviderIds(settingIds);
		}


		try {
			model.setProjectDescription(proj, projDes);
		} catch (CoreException e) {
			//ignore
		}

		if (added) {
			MessageDialog.openInformation(null, "External Settings", "added external settings");
		} else {
			MessageDialog.openInformation(null, "External Settings", "removed external settings");
		}
	}

	public void selectionChanged(final IAction pr_Action, final ISelection pr_Selection) {
		if ((pr_Selection instanceof IStructuredSelection) && !(pr_Selection.isEmpty())) {
			Object obj = ((IStructuredSelection) pr_Selection).getFirstElement();
			IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
			if (res != null) {
				proj = res.getProject();
			} else {
				proj = null;
			}
		}
	}
	
}
