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
package de.clusteval.program.r;

/**
 * @author Christian Wiwie
 * 
 */
public class UnknownRProgramException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7487877880458179088L;

	/**
	 * @param message
	 */
	public UnknownRProgramException(final String message) {
		super(message);
	}
}
