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
package de.clusteval.data;

import de.clusteval.utils.ClustEvalException;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class DataException extends ClustEvalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5782785078643769340L;

	/**
	 * @param t
	 */
	public DataException(Throwable t) {
		super(t);
	}

	/**
	 * @param message
	 */
	public DataException(String message) {
		super(message);
	}

}
