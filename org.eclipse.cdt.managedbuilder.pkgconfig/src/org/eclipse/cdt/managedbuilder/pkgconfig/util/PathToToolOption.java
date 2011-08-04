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

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.pkgconfig.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;

/**
 * Add include and library search paths and libraries to Tool's (compiler, linker) options.
 * 
 * TODO: Modify so that the Tool Options are added to the selected build configuration.
 * 		 IF all configurations are selected then add Tool Options to all configurations.
 */
public class PathToToolOption {

	//tool input extensions
	private static final String[] inputTypes = {"cpp", "c"};  //$NON-NLS-1$ //$NON-NLS-2$
	
	//tool option values
	public static final int INCLUDE = 1;
	public static final int LIB = 2;
	public static final int LIB_PATH = 3;
	public static final int OTHER_FLAG = 4;
	
	private final static String OtherFlagsOptionName = "Other flags"; //$NON-NLS-1$
	
	/**
	 * Adds new include path to Compiler's Include path option.
	 * 
	 * @param includePath Include path to be added to Compiler's Include Option 
	 */
	public static void addIncludePath(String includePath, IProject proj) {
		if (proj != null) {
			addPathToToolOption(includePath, INCLUDE, proj);
		}
	}

	/**
	 * Removes an include path from Compiler's Include path option. 
	 * 
	 * @param includePath Include path to be removed from Compiler's Include Option 
	 */
	public static void removeIncludePath(String includePath, IProject proj) {
		if (proj != null) {
			removePathFromToolOption(includePath, INCLUDE, proj);
		}
	}

	/**
	 * Adds a new Library to Linker's Libraries Option.
	 * 
	 * @param lib Library name to be added to Linker's Libraries Option
	 */
	public static void addLib(String lib, IProject proj) {
		if (proj != null) {
			addPathToToolOption(lib, LIB, proj);	
		}
	}

	/**
	 * Removes a Library from Linker's Libraries Option.
	 * 
	 * @param lib Library name to be removed from Linker's Libraries Option
	 */
	public static void removeLib(String lib, IProject proj) {
		if (proj != null) {
			removePathFromToolOption(lib, LIB, proj);
		}
	}

	/**
	 * Adds a new Library search path to Linker's Library search path Option.
	 * 
	 * @param libDir Library search path to be added to Linker's Library search path Option
	 */
	public static void addLibraryPath(String libDir, IProject proj) {
		if (proj != null) {
			addPathToToolOption(libDir, LIB_PATH, proj);
		}
	}

	/**
	 * Removes a Library search path from Linker's Library search path Option.
	 * 
	 * @param libDir Library search path to be removed from Linker's Library search path Option
	 */	
	public static void removeLibraryPath(String libDir, IProject proj) {
		if (proj != null) {
			removePathFromToolOption(libDir, LIB_PATH, proj);
		}
	}

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
		if (path.length()>0 && (pathExists(path) || var==LIB || var==OTHER_FLAG)) {
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
		if (path.length()>0 && pathExists(path) || var==LIB || var==OTHER_FLAG) {
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
		case INCLUDE:
			return addIncludePathToToolOption(cf, value);
		case LIB:
			return addLibToToolOption(cf, value);
		case LIB_PATH:
			return addLibSearchPathToToolOption(cf, value);
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
		case INCLUDE:
			return removeIncludePathFromToolOption(cf, value);
		case LIB:
			return removeLibFromToolOption(cf, value);
		case LIB_PATH:
			return removeLibSearchPathFromToolOption(cf, value);
		case OTHER_FLAG:
			return removeOtherFlagFromToolOption(cf, value);
		default:
			return false;
		}
	}

	/**
	 * Adds an include path to Compiler's Include path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param newIncludePath Include path to be added to Compiler's Include path Option
	 */
	private static boolean addIncludePathToToolOption(IConfiguration cf, String newIncludePath) {
		ITool compiler = getCompiler(cf);
		//If the compiler is found from the given build configuration
		if (compiler != null) {
			//get compiler's Include paths option.
			IOption compilerIncPathOption = getCompilerIncludePathOption(cf);
			if (compilerIncPathOption != null) {
				//add a new include path to compiler's Include paths option.
				boolean val = addIncludePathToToolOption(cf, compiler, compilerIncPathOption, newIncludePath);
				return val;
			} else {
				return false;
			}
		} 
		return false;
	}

	/**
	 * Removes an include path from Compiler's Include path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param removeIncludePath Include path to be removed from Compiler's Include path Option
	 */
	private static boolean removeIncludePathFromToolOption(IConfiguration cf, String removeIncludePath) {
		ITool compiler = getCompiler(cf);
		//If the compiler is found from the given build configuration
		if (compiler != null) {
			//get compiler's Include paths option.
			IOption compilerIncPathOption = getCompilerIncludePathOption(cf);
			if (compilerIncPathOption != null) {
				//remove an include path from compiler's Include paths option.
				removeIncludePathFromToolOption(cf, compiler, compilerIncPathOption, removeIncludePath);
				return true;
			} else {
				return false;
			}
		} 
		return false;
	}

	/**
	 * Adds a Library to Linker's Libraries Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param lib Library name
	 * @return boolean Returns true if Library Option was added successfully to the Linker.
	 */
	private static boolean addLibToToolOption(IConfiguration cf, String lib) {
		ITool linker = getLinker(cf);
		//If the linker is found from the given build configuration
		if (linker != null) {
			//get Linker's Libraries option
			IOption librariesOption = getLinkerLibrariesOption(cf);
			if (librariesOption != null) {
				//add library to Linker's Libraries Option type
				boolean val = addLibraryToToolOption(cf, linker, librariesOption, lib);
				return val;
			} else {
				return false;
			}
		} 
		//adding the library failed
		return false;
	}

	/**
	 * Removes a Library from Linker's Libraries Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param removeLib Library name
	 * @return boolean Returns true if Library Option was removed successfully from the Linker.
	 */
	private static boolean removeLibFromToolOption(IConfiguration cf, String removeLib) {
		ITool linker = getLinker(cf);
		//If the Linker is found from the given build configuration
		if (linker != null) {
			//get Linker's Libraries option
			IOption librariesOption = getLinkerLibrariesOption(cf);
			if (librariesOption != null) {
				//remove a library from linker's Libraries Option type
				removeLibraryFromToolOption(cf, linker, librariesOption, removeLib);
				return true;
			} else {
				return false;
			}
		} 
		//removing the library failed
		return false;
	}

	/**
	 * Adds a Library search path to Linker's Library search path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param libDir Library search path
	 * @return boolean Returns true if Library search path Option was added successfully to the Linker.
	 */
	private static boolean addLibSearchPathToToolOption(IConfiguration cf, String libDir) {
		ITool linker = getLinker(cf);
		//If the linker is found from the given build configuration
		if (linker != null) {
			//get Linker's Library search path option
			IOption libDirOption = getLinkerLibrarySearchPathOption(cf);
			if (libDirOption != null) {
				//add library search path to linker's Library Search Path Option type
				boolean val = addLibrarySearchPathToToolOption(cf, linker, libDirOption, libDir);
				return val;
			}
		} 
		//adding library failed
		return false;
	}

	/**
	 * Removes a Library search path from Linker's Library search path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param removeLibDir Library search path
	 * @return boolean Returns true if Library search path Option was removed successfully from the Linker.
	 */
	private static boolean removeLibSearchPathFromToolOption(IConfiguration cf, String removeLibDir) {
		ITool linker = getLinker(cf);
		//If the linker is found from the given build configuration
		if (linker != null) {
			//get Linker's Library search path option
			IOption libDirOption = getLinkerLibrarySearchPathOption(cf);
			if (libDirOption != null) {
				//remove a library search path from linker's Library Search Path Option type
				removeLibrarySearchPathFromToolOption(cf, linker, libDirOption, removeLibDir);
				return true;
			}
		} 
		//removing the library search path failed
		return false;
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
			} else {
				return false;
			}
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
			} else {
				return false;
			}
		} 
		return false;
	}
	
	/**
	 * Adds include path to given Build configuration's Tool's Include path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param newIncludePath Include path to be added to Tool's Include path option
	 */
	private static boolean addIncludePathToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newIncludePath) {
		try {
			//add path only if it does not exists
			String[] incPaths = option.getIncludePaths();
			for (String inc : incPaths) {
				if (inc.equalsIgnoreCase(newIncludePath)) {
					return false;
				}
			}
			//add a new include path to linker's Include paths option.
			addInputToToolOption(cf, cfTool, option, newIncludePath, incPaths);
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Adding an include path to Tool Option failed.");
		}
		return true;
	}

	/**
	 * Removes an include path from given Build configuration's Tool's Include path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param removeIncludePath Include path to be removed from Tool's Include path option
	 */
	private static void removeIncludePathFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeIncludePath) {
		try {
			//remove an include path from linker's Include paths option.
			removeInputFromToolOption(cf, cfTool, option, removeIncludePath, option.getIncludePaths());
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Removing an include path from Tool Option failed.");
		}
	}

	/**
	 * Adds new Library to the Linker's Libraries Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param newLibrary Library
	 */
	private static boolean addLibraryToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newLibrary) {
		try {
			//add library only if it does not exists
			String[] libraries = option.getLibraries();
			for (String lib : libraries) {
				if (lib.equalsIgnoreCase(newLibrary)) {
					return false;
				}
			}
			//add a new library to linker's Libraries option.
			addInputToToolOption(cf, cfTool, option, newLibrary, libraries);
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Adding a library to Tool Option failed.");
		}
		return true;
	}

	/**
	 * Removes a new Library from the Linker's Libraries Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param removeLibrary Library
	 */
	private static void removeLibraryFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeLibrary) {
		try {
			//remove a library from linker's Libraries option.
			removeInputFromToolOption(cf, cfTool, option, removeLibrary, option.getLibraries());
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Removing a library from Tool Option failed.");
		}
	}

	/**
	 * Adds new Library search path to the Linker's Library search path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param newLibraryPath Library search path
	 * @since 8,0
	 */
	private static boolean addLibrarySearchPathToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newLibraryPath) {
		try {
			//add path only if it does not exists
			String[] libPaths = option.getLibraryPaths();
			for (String libPath : libPaths) {
				if (libPath.equalsIgnoreCase(newLibraryPath)) {
					return false;
				}
			}
			//add a new library path to linker's Library search path option.
			addInputToToolOption(cf, cfTool, option, newLibraryPath, libPaths);
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Adding a library search path to Tool Option failed.");
		}
		return true;
	}

	/**
	 * Removes a Library search path from the Linker's Library search path Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param removeSearchPath Library search path
	 * @since 8.0
	 */
	private static void removeLibrarySearchPathFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeSearchPath) {
		try {
			//remove a library path from linker's Library search path option.
			removeInputFromToolOption(cf, cfTool, option, removeSearchPath, option.getLibraryPaths());
		} catch (BuildException e) {
			Activator.getDefault().log(e, "Removing a library search path from Tool Option failed.");
		}
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
			flags = "";
		}

		if (!flags.contains(newOtherFlag)) {
			//append the new flag to existing flags
			flags = flags+" "+newOtherFlag;

			//add a new other flag to compiler's other flags option.
			if (option != null) {
				ManagedBuildManager.setOption(cf, cfTool, option, flags);
			}
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
				flags = flags.replace(" "+removeOtherFlag, "");

				//set other flags to compiler's other flags option.
				ManagedBuildManager.setOption(cf, cfTool, option, flags);
			}
		}
	}
	
	/**
	 * Adds a new value to specific Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param newValue New value to be added to the Option type
	 * @param existingValues Existing Option type values
	 */
	private static void addInputToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newValue, String[] existingValues) {
		if (option != null) {
			//append new value with existing values
			String[] newValues = addNewPathToExistingPathList(existingValues, newValue);
			//set new values array for the option for the given build configuration
			ManagedBuildManager.setOption(cf, cfTool, option, newValues);
		} else{
			//log error
		}
	}

	/**
	 * Removes a value from a specific Option.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @param cfTool ITool Tool
	 * @param option Tool Option type
	 * @param removeValue Value to be removed from the Option type
	 * @param existingValues Existing Option type values
	 */
	private static void removeInputFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeValue, String[] existingValues) {
		if (option != null) {
			//check that list has values
			if(existingValues.length>0) {
				//remove value from existing values
				String[] newValues = ArrayUtil.removePathFromExistingPathList(existingValues, removeValue);
				//set new values array for the option for the given build configuration
				ManagedBuildManager.setOption(cf, cfTool, option, newValues);
			}
		} else{
			//log error
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
	 * Returns linker.
	 * 
	 * @param cf IConfiguration Build configuration
	 * @return ITool linker
	 * TODO: Rewrite
	 */
	private static ITool getLinker(IConfiguration cf) {
		ITool[] tools = cf.getTools();
		/*
		 * Search for the right tool.
		 * Trying to achieve better support for custom toolchains.
		 */
		//see if c++ or c project
		ITool compiler = getIToolByInputType(cf, "cpp");
		boolean cppProject = false;
		if (compiler != null) {
			cppProject = true;
		}
		for (ITool tool : tools) {
			String name = tool.getBaseId();
			if (cppProject) {
				if (name.contains("cpp.linker")) {
					return tool;
				}
			} else {
				if (name.contains("c.linker")) {
					return tool;
				}
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
	 * Returns compiler Include path Option type.
	 * 
	 * @param cf IConfiguration Project build configuration
	 * @return IOption Tool option type
	 */
	private static IOption getCompilerIncludePathOption(IConfiguration cf) {
		ITool cfTool = getCompiler(cf);
		//get option id for include paths
		String includeOptionId = getOptionIdByValueType(cfTool, IOption.INCLUDE_PATH);
		return getToolOptionType(cfTool, includeOptionId);
	}

	/**
	 * Returns Linker Libraries Option type.
	 * 
	 * @param cf IConfiguration Project build configuration
	 * @return IOption Tool option type
	 */
	private static IOption getLinkerLibrariesOption(IConfiguration cf) {
		ITool cfTool = getLinker(cf);
		//get option id for libraries
		String libOptionId = getOptionIdByValueType(cfTool, IOption.LIBRARIES);
		return getToolOptionType(cfTool, libOptionId);
	}

	/**
	 * Returns Linker Library search path Option type.
	 * 
	 * @param cf IConfiguration Project build configuration
	 * @return IOption Tool option type
	 */
	private static IOption getLinkerLibrarySearchPathOption(IConfiguration cf) {
		//get ITool associated with the input extension
		ITool cfTool = getLinker(cf);
		//get option id for library paths
		String libDirOptionId = getOptionIdByValueType(cfTool, IOption.LIBRARY_PATHS);
		return getToolOptionType(cfTool, libDirOptionId);
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
	 * @param optionValueType Option's value type.
	 * @return optionId Tool's option id.
	 */
	private static String getOptionIdByValueType(ITool cfTool, int optionValueType) {
		String optionId = null;
		//get all Tool options.
		IOption[] options = cfTool.getOptions();
		for (IOption opt : options) {
			try {
				//try to match option value type
				if(opt.getValueType()==optionValueType) {
					//get option id
					optionId = opt.getId();
					break;
				}
			} catch (BuildException e) {
				Activator.getDefault().log(e, "Getting Option id by value type failed.");
			}
		}	
		return optionId;
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
			Activator.getDefault().log(IStatus.INFO, e, "Not a managed build project.");
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
