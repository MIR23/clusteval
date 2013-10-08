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
 * @author Christian Wiwie
 * 
 */
public class ClustEvalException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2371618638043608163L;

	/**
	 * @param message
	 */
	public ClustEvalException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ClustEvalException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ClustEvalException(String message, Throwable cause) {
		super(message, cause);
	}

}
