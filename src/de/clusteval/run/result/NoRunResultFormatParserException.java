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
package de.clusteval.run.result;

/**
 * The Class NoRunResultFormatParserException.
 * 
 * @author Christian Wiwie
 */
public class NoRunResultFormatParserException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8352448872899833995L;

	/**
	 * Instantiates a new no run result format parser exception.
	 * 
	 * @param string
	 *            the string
	 */
	public NoRunResultFormatParserException(String string) {
		super(string);
	}

}
