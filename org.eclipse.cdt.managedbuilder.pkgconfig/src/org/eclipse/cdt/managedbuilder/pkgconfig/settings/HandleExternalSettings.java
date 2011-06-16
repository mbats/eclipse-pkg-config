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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class HandleExternalSettings {

	private static boolean set = false;
	
	public static void addUpdateExternalSettings(IProject proj) {
		ICConfigurationDescription activeCfg = null;
		ICProjectDescription cProjDescr = null;
        String[] ids = new String[] { PkgConfigExternalSettingProvider.ID };
        if (!set) {
          set = true;
          cProjDescr = CCorePlugin.getDefault().getProjectDescription(proj, true);
          activeCfg = cProjDescr.getActiveConfiguration();
          activeCfg.setExternalSettingsProviderIds(ids);
        } else {
        	cProjDescr = CCorePlugin.getDefault().getProjectDescription(proj, true);
        	activeCfg = cProjDescr.getActiveConfiguration();
        	activeCfg.updateExternalSettingsProviders(ids);
        }	
        try {
            CoreModel.getDefault().setProjectDescription(proj, cProjDescr, true, new NullProgressMonitor());
          }
          catch (CoreException e) {
        	  Activator.getDefault().log(e, "Setting external settings failed.");
          }
	}
	
}
