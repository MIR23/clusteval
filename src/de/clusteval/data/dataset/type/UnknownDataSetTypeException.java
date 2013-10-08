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
package de.clusteval.data.dataset.type;

/**
 * @author Christian Wiwie
 */
public class UnknownDataSetTypeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -229981758008025814L;

	/**
	 * Instantiates a new unknown data set type exception.
	 * 
	 * @param string
	 *            the string
	 */
	public UnknownDataSetTypeException(String string) {
		super(string);
	}

	/**
	 * @param t
	 */
	public UnknownDataSetTypeException(Throwable t) {
		super(t);
	}
}
