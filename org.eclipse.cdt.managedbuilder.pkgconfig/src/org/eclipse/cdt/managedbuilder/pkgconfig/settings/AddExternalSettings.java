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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
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
 * Adds or update the external settings from the selected project
 */
public class AddExternalSettings implements IObjectActionDelegate {

    private IProject slctProj;
    
    @Override
    public void setActivePart(final IAction pr_Action, final IWorkbenchPart pr_TargetPart) {
    }
    
    @Override
    public void run(final IAction pr_Action) {
        boolean added = false;        
		ICConfigurationDescription activeCfg = null;
		ICProjectDescription cProjDesc = null;
		String[] ids = new String[] { PkgConfigExternalSettingProvider.ID };
        CoreModel model = CoreModel.getDefault();
		cProjDesc = model.getProjectDescription(slctProj, true);
		activeCfg = cProjDesc.getActiveConfiguration();
		String[] settingIds = activeCfg.getExternalSettingsProviderIds();
		ArrayList<String> settingIdList = new ArrayList<String>(Arrays.asList(settingIds)); 
		if (!settingIdList.contains(PkgConfigExternalSettingProvider.ID)) { 
			added = true;
			activeCfg.setExternalSettingsProviderIds(ids);
		} else {
			added  = false;
			activeCfg.updateExternalSettingsProviders(ids);
		}	
		try {
			CoreModel.getDefault().setProjectDescription(slctProj, cProjDesc);
		}
		catch (CoreException e) {
			Activator.getDefault().log(e, "Setting external settings failed.");
		}
        
        try {
            model.setProjectDescription(slctProj, cProjDesc);
        } catch (CoreException e) {
        	//TODO: Log error
        }
        
        if (added) {
            MessageDialog.openInformation(null, "External Settings", "Added external settings");
        } else {
            MessageDialog.openInformation(null, "External Settings", "Updated external settings");
        }
    }

    @Override
    public void selectionChanged(final IAction pr_Action, final ISelection pr_Selection) {
        if ((pr_Selection instanceof IStructuredSelection) && !(pr_Selection.isEmpty())) {
            Object obj = ((IStructuredSelection) pr_Selection).getFirstElement();
            IResource res = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
            if (res != null) {
                slctProj = res.getProject();
            } else {
                slctProj = null;
            }
        }
    }
    
}
