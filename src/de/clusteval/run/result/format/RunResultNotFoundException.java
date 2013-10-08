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
package de.clusteval.run.result.format;

/**
 * @author Christian Wiwie
 * 
 */
public class RunResultNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6428149018225112065L;

	/**
	 * @param message
	 */
	public RunResultNotFoundException(String message) {
		super(message);
	}
}
