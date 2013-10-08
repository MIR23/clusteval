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
package de.clusteval.utils;

/**
 * The Class InvalidConfigurationFileException.
 * 
 * @author Christian Wiwie
 */
public class InvalidConfigurationFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8767826036439521944L;

	/**
	 * Instantiates a new invalid configuration file exception.
	 * 
	 * @param message
	 *            the message
	 */
	public InvalidConfigurationFileException(String message) {
		super(message);
	}
}
