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
package org.eclipse.cdt.managedbuilder.pkgconfig.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;

/**
 * Add other flags to compiler's miscellaneous option.

 */
public class PathToToolOption {

	//tool input extensions
	private static final String[] inputTypes = {"cpp", "c"};  //$NON-NLS-1$ //$NON-NLS-2$
	
	//tool option values
	public static final int OTHER_FLAG = 1;
	
	private final static String OtherFlagsOptionName = "Other flags"; //$NON-NLS-1$

	/**
	 * Adds new other flag to Compiler's Other flags option.
	 * 
	 * @param otherFlag Include path to be added to Compiler's Include Option 
	 */
	public static void addOtherFlag(String otherFlag, IProject proj) {
		if (proj != null) {
			addPathToToolOption(otherFlag, OTHER_FLAG, proj);
		}
	}

	/**
	 * Removes an other flag from Compiler's Other flags option. 
	 * 
	 * @param otherFlag Other flag to be removed from Compiler's Other flags Option 
	 */
	public static void removeOtherFlag(String otherFlag, IProject proj) {
		if (proj != null) {
			removePathFromToolOption(otherFlag, OTHER_FLAG, proj);
		}
	}
	
	/**
	 * Adds a path to Tool's option.
	 * 
	 * @param path Path to be added to Tool's option
	 * @param var Tool option's value
	 */
	private static void addPathToToolOption(String path, int var, IProject proj) {
		//check if the given path exists
		if (path.length()>0 && (pathExists(path) || var==OTHER_FLAG)) {
			IConfiguration cf = getActiveBuildConf(proj);
			if (cf != null) {
				//Add path for the Tool's option
				addPathToSelectedToolOptionBuildConf(cf, path, var);
			}
		}
	}

	/**
	 * Removes a path from Tool's option.
	 * 
	 * @param path Path to be removed from Tool's option
	 * @param var Tool option's value
	 */
	private static void removePathFromToolOption(String path, int var, IProject proj) {
		//check if the given path exists
		if (path.length()>0 && pathExists(path) || var==OTHER_FLAG) {
			IConfiguration cf = getActiveBuildConf(proj);
			//remove a path from the Tool's option
			removePathFromSelectedToolOptionBuildConf(cf, path, var);
		}
	}

	/**
	 * Add a value to specific build configuration's Tool option. 
	 * 
	 * @param cf Build configuration
	 * @param value Value to be added (path, file name, flag etc.)
	 * @param var Value of the option type
	 * @return boolean True if path was added successfully
	 */
	private static boolean addPathToSelectedToolOptionBuildConf(IConfiguration cf, String value, int var) {
		switch (var) {
		case OTHER_FLAG:
			return addOtherFlagToToolOption(cf, value);
		default:
			return false;
		}
	}

	/**
	 * Removes a path from specific build configuration's Tool option. 
	 * 
	 * @param cf Build configuration
	 * @param value Value to be removed (path, file name, flag etc.)
	 * @param var Value of the option type
	 * @return boolean True if path was removed successfully
	 */
	private static boolean removePathFromSelectedToolOptionBuildConf(IConfiguration cf, String value, int var) {
		switch (var) {
		case OTHER_FLAG:
			return removeOtherFlagFromToolOption(cf, value);
		default:
			return false;
		}
	}

	/**
	 * Adds an other flag to compiler's Other flags Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param otherFlag Other flag
	 * @return boolean Returns true if Other flags Option was added successfully to the compiler.
	 */
	private static boolean addOtherFlagToToolOption(IConfiguration cf, String otherFlag) {
		ITool compiler = getCompiler(cf);
		//If the compiler is found from the given build configuration
		if (compiler != null) {
			//get compiler's Other flags option
			IOption otherFlagOption = getCompilerOtherFlagsOption(cf);
			if (otherFlagOption != null) {
				//add other flag to compiler's Other flags Option type
				boolean val = addOtherFlagToToolOption(cf, compiler, otherFlagOption, otherFlag);
				return val;
			}
			return false;
		} 
		//adding the other flag failed
		return false;
	}
	
	/**
	 * Removes an other flag from Compiler's Other flags Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param removeOtherFlag Include path to be removed from Compiler's Include path Option
	 */
	private static boolean removeOtherFlagFromToolOption(IConfiguration cf, String removeOtherFlag) {
		ITool compiler = getCompiler(cf);
		//If the compiler is found from the given build configuration
		if (compiler != null) {
			//get compiler's Other flags option
			IOption otherFlagOption = getCompilerOtherFlagsOption(cf);
			if (otherFlagOption != null) {
				//remove an other flag from compiler's Other flags option.
				removeOtherFlagFromToolOption(cf, compiler, otherFlagOption, removeOtherFlag);
				return true;
			}
			return false;
		} 
		return false;
	}

	/**
	 * Adds new other flag to the Compiler's Other flags Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param newOtherFlag
	 * @since 8.0
	 */
	private static boolean addOtherFlagToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newOtherFlag) {
		String flags = option.getValue().toString();
		if (flags == null) {
			flags = ""; //$NON-NLS-1$
		}

		if (!flags.contains(newOtherFlag)) {
			//append the new flag to existing flags
			flags = flags+" "+newOtherFlag; //$NON-NLS-1$

			//add a new other flag to compiler's other flags option.
				ManagedBuildManager.setOption(cf, cfTool, option, flags);
		} else {
			return false;
		}
		return false;
	}
	
	/**
	 * Removes an other flag from given Build configuration's Compiler's Other flags Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param removeOtherFlag Other flag to be removed from compiler's Other flags Option
	 */
	private static void removeOtherFlagFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeOtherFlag) {
		//remove an other flag from compiler's Other flags option.
		if (option != null) {
			String flags = option.getValue().toString();
			if (flags == null) {
				return;
			}

			if (flags.contains(removeOtherFlag)) {
				//remove from existing flags
				flags = flags.replace(" "+removeOtherFlag, ""); //$NON-NLS-1$ //$NON-NLS-2$

				//set other flags to compiler's other flags option.
				ManagedBuildManager.setOption(cf, cfTool, option, flags);
			}
		}
	}

	/**
	 * Return compiler according to the input type.
	 * @param cf IConfiguration Build configuration
	 * @return ITool Compiler
	 */
	private static ITool getCompiler(IConfiguration cf) {
		//get compiler according to the input type
		for(int i=0; i<inputTypes.length; i++) {
			ITool tool = getIToolByInputType(cf, inputTypes[i]);
			if (tool != null) {
				return tool;
			}
		}
		return null;
	}

	/**
	 * Returns ITool associated based on the input extension.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param ext input extension associated with ITool
	 * @return ITool Tool that matches input extension
	 */
	private static ITool getIToolByInputType(IConfiguration cf, String ext) {
		//get ITool associated with the input extension
		return cf.getToolFromInputExtension(ext);
	}

	/**
	 * Returns compiler's Other flags Option type.
	 * 
	 * @param cf IConfiguration Project build configuration
	 * @return IOption Tool option type
	 */
	private static IOption getCompilerOtherFlagsOption(IConfiguration cf) {
		//get ITool associated with the input extension
		ITool cfTool = getCompiler(cf);
		//get option id for other flags
		String otherFlagsOptionId = getOptionIdByName(cfTool, OtherFlagsOptionName);
		return getToolOptionType(cfTool, otherFlagsOptionId);
	}

	/**
	 * Returns Tool's option id.
	 * 
	 * @param cfTool ITool Tool
	 * @param name Option's name
	 * @return optionId Tool's option id.
	 */
	private static String getOptionIdByName(ITool cfTool, String name) {
		String optionId = null;
		//get all Tool options.
		IOption[] options = cfTool.getOptions();
		for (IOption opt : options) {
			//try to match option name
			if(opt.getName()==name) {
				//get option id
				optionId = opt.getId();
				break;
			}
		}	
		return optionId;
	}
	
	/**
	 * Returns Tool's Option type by Id.
	 * 
	 * @param cfTool ITool Tool
	 * @param optionId String Tool option type id
	 * @return IOption Tool option type
	 */
	private static IOption getToolOptionType(ITool cfTool, String optionId) {
		//get option type with specific id for the ITool
		return cfTool.getOptionById(optionId);
	}

	/**
	 * Adds one or more paths to the list of paths.
	 * 
	 * @param existingPaths Existing list of paths to add to
	 * @param newPath New path to add. May include multiple directories with a path delimiter.
	 * @return String[] List that includes existing paths as well as new paths.
	 */
	public static String[] addNewPathToExistingPathList(String[] existingPaths, String newPath) {
		List<String> newPathList = new ArrayList<String>();
		String path;
		//adds existing paths to new paths list
		for (int i = 0; i < existingPaths.length; i++) {
			path = existingPaths[i];
			newPathList.add(path);
		}
		//separates new path if it has multiple paths separated by a path separator
		String[] newPathArray = newPath.split(Separators.getPathSeparator());
		for (int i = 0; i < newPathArray.length; i++) {
			path = newPathArray[i];
			newPathList.add(path);
		}
		//creates a new list that includes all existing paths as well as new paths
		String[] newArray = newPathList.toArray(new String[0]);
		return newArray;
	}

	/**
	 * Checks if a file path exists.
	 * 
	 * @return boolean True if the file exists.
	 */
	private static boolean pathExists(String path) {
		//return true if path exists.
		return new File(path).exists();
	}

	/**
	 * Get the active build configuration.
	 * 
	 * @param proj IProject
	 * @return IConfiguration
	 */
	public static IConfiguration getActiveBuildConf(IProject proj) {
		IConfiguration conf = null;
		IManagedBuildInfo info = null;
		//try to get Managed build info
		try {
			info = ManagedBuildManager.getBuildInfo(proj); //null if doesn't exists
		} catch (Exception e) { //if not a managed build project
			Activator.getDefault().log(IStatus.INFO, e, "Not a managed build project."); //$NON-NLS-1$
			return conf;
		}
		//info can be null for projects without build info. For example, when creating a project
		//from Import -> C/C++ Executable
		if(info == null) {
			return conf;
		}
		conf = info.getDefaultConfiguration();
		return conf;
	}
	
}
