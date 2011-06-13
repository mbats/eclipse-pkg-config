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

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.ICLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.core.resources.IProject;

public class PkgConfigSettingsProvider extends CExternalSettingProvider {

	public static final String ID = "org.eclipse.cdt.managedbuilder.pkgconfig.extSettings"; //$NON-NLS-1$
	
	@Override
	public CExternalSetting[] getSettings(IProject project,
			ICConfigurationDescription cfg) {
        //supply a simple macro entry
        ArrayList<ICSettingEntry> pathEntries = new ArrayList<ICSettingEntry>();
//        ICIncludePathEntry
//        ICLibraryPathEntry
//        ICLibraryFileEntry
//        pathEntries.add(inc);

        ICSettingEntry[] settings = (ICSettingEntry[]) pathEntries.toArray(new ICSettingEntry[pathEntries.size()]);
        return new CExternalSetting[]{new CExternalSetting(null, null, null, settings)};
	}

}
