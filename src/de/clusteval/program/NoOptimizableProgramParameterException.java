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

/**
 * This exception is thrown, if a program parameter should be used during a
 * parameter optimization process but is not defined as a optimizable parameter.
 * 
 * @author Christian Wiwie
 * 
 */
public class NoOptimizableProgramParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -430736667056265544L;

	/**
	 * @param message
	 */
	public NoOptimizableProgramParameterException(String message) {
		super(message);
	}
}
