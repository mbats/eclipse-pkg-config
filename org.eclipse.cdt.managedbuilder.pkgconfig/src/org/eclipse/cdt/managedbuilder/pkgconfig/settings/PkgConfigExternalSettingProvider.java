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
import java.util.Collections;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class PkgConfigExternalSettingProvider extends CExternalSettingProvider {

	public static final String ID = "org.eclipse.cdt.managedbuilder.pkgconfig.extSettings"; //$NON-NLS-1$
	private final static String PACKAGES = "packages";
	
	@Override
	public CExternalSetting[] getSettings(IProject proj,
			ICConfigurationDescription cfg) {
        
		ICLanguageSettingEntry[] includes = getIncludePaths(proj);
		
		ArrayList<ICLanguageSettingEntry> settings = new ArrayList<ICLanguageSettingEntry>();
		Collections.addAll(settings, includes);

		CExternalSetting setting =
				new CExternalSetting(new String[] { "org.eclipse.cdt.core.gcc", "org.eclipse.cdt.core.g++" }, new String[] {
				"org.eclipse.cdt.core.cSource" }, null,
				settings.toArray(new ICLanguageSettingEntry[settings.size()]));
		
		return new CExternalSetting[] { setting };
	}

	private static ICLanguageSettingEntry[] getIncludePaths(IProject proj) {
		String[] pkgIncludes = getIncludesFromCheckedPackages(proj);
		ICLanguageSettingEntry[] newIncludes = formIncludePathEntries(pkgIncludes);
		ArrayList<ICLanguageSettingEntry> newIncludePathEntries = new ArrayList<ICLanguageSettingEntry>();
		ICLanguageSetting lang = getGCCLanguageSetting(proj);
		ICLanguageSettingEntry[] includes = null;
		if (lang!=null) {
			ICLanguageSettingEntry[] currentIncludes = lang.getSettingEntries(ICSettingEntry.INCLUDE_PATH);
			Collections.addAll(newIncludePathEntries, currentIncludes);
			for (ICLanguageSettingEntry entry : newIncludes) {
				if(!newIncludePathEntries.contains(entry) && !entry.getName().equalsIgnoreCase("")) {
					newIncludePathEntries.add(entry);
				}
			}
			includes = (ICLanguageSettingEntry[]) newIncludePathEntries.toArray(new ICLanguageSettingEntry[newIncludePathEntries.size()]);
			lang.setSettingEntries(ICSettingEntry.INCLUDE_PATH, includes);
		}
		return includes;
	}
	
	private static ICLanguageSetting[] getLanguageSettings(IProject proj) {
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
		ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
		ICFolderDescription folderDesc = activeConf.getRootFolderDescription(); 
		ICLanguageSetting[] langSettings = folderDesc.getLanguageSettings();
		return langSettings;
	}
	
	private static ICLanguageSetting getLanguageSetting(IProject proj, String languageId) {
		ICLanguageSetting[] langSettings = getLanguageSettings(proj);
		ICLanguageSetting lang = null;
		for (ICLanguageSetting langSetting : langSettings) {
			if (langSetting.getLanguageId().equalsIgnoreCase(languageId)) {
				lang = langSetting;
				return lang;
			}
		}
		return null;
	}
	
	private static ICLanguageSetting getGCCLanguageSetting(IProject proj) {
		return getLanguageSetting(proj, "org.eclipse.cdt.core.gcc");
	}
	
	private static ICLanguageSetting getGPPLanguageSetting(IProject proj) {
		return getLanguageSetting(proj, "org.eclipse.cdt.core.g++");
	}
	
	private static ICLanguageSettingEntry[] formIncludePathEntries(String[] includes) {
		ArrayList<ICLanguageSettingEntry> incEntries = new ArrayList<ICLanguageSettingEntry>();
		for(String inc : includes) {
			ICIncludePathEntry include = new CIncludePathEntry(new Path(inc),
					ICSettingEntry.INCLUDE_PATH);
			incEntries.add(include);
		}
		return incEntries.toArray(new ICLanguageSettingEntry[incEntries.size()]);
	}
	
	private static String[] getIncludesFromCheckedPackages(IProject proj) {
		ArrayList<String> includes = new ArrayList<String>();
		String[] pkgs = getCheckedPackageNames(proj);
		String cflags = null;
		String[] incPathArray = null;
		for (String pkg : pkgs) {
			cflags = PkgConfigUtil.getCflags(pkg);
			incPathArray = Parser.parseIncPaths(cflags);
			Collections.addAll(includes, incPathArray);
		}
		return includes.toArray(new String[includes.size()]);
	}
	
	private static ICStorageElement getPackageStorage(IProject proj) {
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(proj);
		ICConfigurationDescription activeConf = projectDescription.getActiveConfiguration();
		ICConfigurationDescription desc = activeConf.getConfiguration();
		ICStorageElement strgElem = null;
		try {
			strgElem = desc.getStorage(PACKAGES, true);
		} catch (CoreException e) {
			Activator.getDefault().log(e, "Getting packages from the storage failed.");
		}
		return strgElem;
	}
	
	private static String[] getCheckedPackageNames(IProject proj) {
		ICStorageElement pkgStorage = getPackageStorage(proj);
		String[] pkgNames = pkgStorage.getAttributeNames();
		ArrayList<String> pkgs = new ArrayList<String>();
		String value = null;
		for(String pkgName : pkgNames) {
			value = pkgStorage.getAttribute(pkgName);
			if(value!=null) {
				if(value.equals("true")) {
					pkgs.add(pkgName);
				}
			}
		}
		return (String[]) pkgs.toArray(new String[pkgs.size()]);
	}
	
}
