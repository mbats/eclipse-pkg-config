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

/**
 * Parses pkg-config utility output.
 *
 */
public class Parser {
	
	//for testing only
	public static void main(String[] args) {
		System.out.println("Options");
		System.out.println("######################");
		String options = PkgConfigUtil.pkgOutputCflags("gtk+-2.0");
		String[] optionsArray = parseOptions(options);
		for (String l : optionsArray) {
			System.out.println(l);
		}
		
		System.out.print("\n");
		System.out.println("Include paths");
		System.out.println("######################");
		
		String incPaths = PkgConfigUtil.pkgOutputCflags("gtk+-2.0");
		String[] incPathArray = parseIncPaths(incPaths);
		for (String l : incPathArray) {
			System.out.println(l);
		}
		
		System.out.print("\n");
		System.out.println("Library search paths");
		System.out.println("######################");
		
		String libsPaths = PkgConfigUtil.pkgOutputLibs("gtk+-2.0");
		String[] libPathArray = parseLibPaths(libsPaths);
		for (String l : libPathArray) {
			System.out.println(l);
		}
		
		System.out.print("\n");
		System.out.println("Libraries");
		System.out.println("######################");
		
		String libs = PkgConfigUtil.pkgOutputLibs("gtk+-2.0");
		String[] libArray = parseLibs(libs);
		for (String l : libArray) {
			System.out.println(l);
		}
	}
	
	/**
	 * Parses options from pkg-config --cflags output.
	 * 
	 * @param s Output from pkg-config.
	 * @return Parsed String array.
	 */
	public static String[] parseOptions(String s) {
		//find the index where include list starts
		int end = s.indexOf("-I");
		//truncate include paths
		s = s.substring(0, end-1);
		//insert options to an array
		String[] options = s.split(" ");
		return options;
	}
	
	/**
	 * Parses include paths from pkg-config --cflags output.
	 * 
	 * @param s Output from pkg-config.
	 * @return Parsed String array.
	 */
	public static String[] parseIncPaths(String s) {
		//find the index where include list starts
		int start = s.indexOf("-I");
		//truncate other than include paths
		s = s.substring(start, s.length());
		//remove library search path flags
		String s2 = s.replace("-I", "");
		//insert include paths to an array
		String[] incPaths = s2.split(" ");
		return incPaths;
	}
	
	/**
	 * Parses library search paths from pkg-config --libs output.
	 * 
	 * @param s Output from pkg-config.
	 * @return Parsed String array.
	 */
	public static String[] parseLibPaths(String s) {
		//find the index where library list starts
		int end = s.indexOf("-l");
		//truncate libraries
		s = s.substring(0, end-1);
		//remove library search path flags
		String s2 = s.replace("-L", "");
		//insert lib search paths to an array
		String[] libPaths = s2.split(" ");
		return libPaths;
	}
	
	/**
	 * Parses libraries from pkg-config --libs output.
	 * 
	 * @param s Output from pkg-config.
	 * @return Parsed String array.
	 */
	public static String[] parseLibs(String s) {
		//find the index where library list starts
		int start = s.indexOf("-l");
		//truncate library search paths
		s = s.substring(start, s.length());
		//remove lib flags
		String s2 = s.replace("-l", "");
		//insert libs to an array
		String[] libs = s2.split(" ");
		return libs;
	}
	
}
