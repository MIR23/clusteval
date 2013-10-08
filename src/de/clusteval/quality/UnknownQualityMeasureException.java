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
package de.clusteval.quality;

/**
 * @author Christian Wiwie
 */
public class UnknownQualityMeasureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 433568096995882002L;

	/**
	 * Instantiates a new unknown clustering quality measure exception.
	 * 
	 * @param string
	 *            the string
	 */
	public UnknownQualityMeasureException(String string) {
		super(string);
	}
}
