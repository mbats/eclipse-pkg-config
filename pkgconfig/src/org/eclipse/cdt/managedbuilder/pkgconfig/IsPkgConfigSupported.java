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
package org.eclipse.cdt.managedbuilder.pkgconfig;

/**
 * See if the pkg-config utility is found from the system.
 *
 */
public class IsPkgConfigSupported {

//	private final boolean supported;
	
	public IsPkgConfigSupported() {
		// Only supported if we can find pkg-config utility.
//		this.supported = PkgConfigUtil.isFound() != null; //TODO: implement
	}
	
}
