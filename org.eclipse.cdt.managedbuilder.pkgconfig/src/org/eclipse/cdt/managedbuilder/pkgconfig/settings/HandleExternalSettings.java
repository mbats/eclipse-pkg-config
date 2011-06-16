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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class HandleExternalSettings {

	public static void addUpdateExternalSettings(IProject proj) {
		ICConfigurationDescription activeCfg = null;
		ICProjectDescription cProjDesc = null;
		String[] ids = new String[] { PkgConfigExternalSettingProvider.ID };
		cProjDesc = CCorePlugin.getDefault().getProjectDescription(proj, true);
		activeCfg = cProjDesc.getActiveConfiguration();
		String[] settingIds = activeCfg.getExternalSettingsProviderIds();
		ArrayList<String> settingIdList = new ArrayList<String>(Arrays.asList(settingIds)); 
		for(String s : settingIdList){
			System.out.println(s);
		}
		if (!settingIdList.contains(PkgConfigExternalSettingProvider.ID)) { 
			activeCfg.setExternalSettingsProviderIds(ids);
		} else {
			activeCfg.updateExternalSettingsProviders(ids);
		}	
		try {
			CoreModel.getDefault().setProjectDescription(proj, cProjDesc);
		}
		catch (CoreException e) {
			Activator.getDefault().log(e, "Setting external settings failed.");
		}
	}
	
}
