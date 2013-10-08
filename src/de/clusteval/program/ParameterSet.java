/*******************************************************************************
 * Copyright (c) 2013 Christian Wiwie.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Christian Wiwie - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.program;

import java.util.HashMap;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterSet extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 405272229276934252L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public ParameterSet clone() {
		return (ParameterSet) super.clone();
	}
}
