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
package de.clusteval.paramOptimization;

/**
 * @author Christian Wiwie
 * 
 */
public class UnknownParameterOptimizationMethodException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7873220565493877889L;

	/**
	 * @param message
	 */
	public UnknownParameterOptimizationMethodException(String message) {
		super(message);
	}
}
