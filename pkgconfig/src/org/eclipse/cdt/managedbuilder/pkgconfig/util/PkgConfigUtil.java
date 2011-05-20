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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Runs pkg-config utility in the command line and outputs necessary
 * options to build the given package.
 *
 */
public class PkgConfigUtil {

	//Constant variables
	private static final String LIST_PACKAGES = "pkg-config --list-all";
	private static final String OUTPUT_LIBS = "pkg-config --libs ";
	private static final String OUTPUT_CFLAGS = "pkg-config --cflags ";
	private static final String OUTPUT_ALL = "pkg-config --cflags --libs ";
	private static final String OUTPUT_ONLY_LIB_PATHS = "pkg-config --libs-only-L ";
	private static final String OUTPUT_ONLY_LIB_FILES = "pkg-config --libs-only-l ";

	/**
	 * Get options needed to build the given package.
	 * 
	 * @param command
	 * @param pkg
	 * @return
	 */
	private static String pkgOutput(String command, String pkg) {
		ProcessBuilder pb = null;
		if (OSDetector.isUnix()) {
			pb = new ProcessBuilder("bash", "-c", command + pkg);	//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isWindows()) {
			pb = new ProcessBuilder("cmd", "/c", command + pkg);	//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isMac()) {
			pb = new ProcessBuilder("bash", "-c", command + pkg);	//$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader
			(new InputStreamReader(p.getInputStream()));
			line = input.readLine();
			if (line != null) {
				return line;
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get cflags and libraries needed to build the given package.
	 * 
	 * @param pkg
	 * @return
	 */
	public static String pkgOutputAll(String pkg) {
		return pkgOutput(OUTPUT_ALL, pkg);
	}
	
	/**
	 * Get libraries (files and paths) needed to build the given package.
	 * 
	 * @param pkg
	 * @return
	 */
	public static String pkgOutputLibs(String pkg) {
		return pkgOutput(OUTPUT_LIBS, pkg);
	}
	
	/**
	 * Get library paths needed to build the given package.
	 * 
	 * @param pkg
	 * @return
	 */
	public static String pkgOutputLibPathsOnly(String pkg) {
		return pkgOutput(OUTPUT_ONLY_LIB_PATHS, pkg);
	}

	/**
	 * Get library files needed to build the given package.
	 * 
	 * @param pkg
	 * @return
	 */
	public static String pkgOutputLibFilesOnly(String pkg) {
		return pkgOutput(OUTPUT_ONLY_LIB_FILES, pkg);
	}
	
	/**
	 * Get cflags needed to build the given package.
	 * 
	 * @param pkg
	 * @return
	 */
	public static String pkgOutputCflags(String pkg) {
		return pkgOutput(OUTPUT_CFLAGS, pkg);
	}
	
	/**
	 * Get all packages that pkg-config utility finds (package name with description).
	 * 
	 * @return
	 */
	public static ArrayList<String> getAllPackages() {
		ProcessBuilder pb = null;
		if (OSDetector.isUnix()) {
			pb = new ProcessBuilder("bash", "-c", LIST_PACKAGES);	//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isWindows()) {
			pb = new ProcessBuilder("cmd", "/c", LIST_PACKAGES);		//$NON-NLS-1$ //$NON-NLS-2$
		} else if (OSDetector.isMac()) {
			pb = new ProcessBuilder("bash", "-c", LIST_PACKAGES);	//$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader
					(new InputStreamReader(p.getInputStream()));
			ArrayList<String> packageList = new ArrayList<String>();
			do {
				line = input.readLine();
				if (line != null) {
					packageList.add(line);
				}
			} while(line != null);
			input.close();
			return packageList;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
