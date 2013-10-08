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
package de.clusteval.data.goldstandard;

import de.clusteval.data.DataException;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class GoldStandardException extends DataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7235966832673486396L;

	/**
	 * @param message
	 */
	public GoldStandardException(String message) {
		super(message);
	}

	/**
	 * @param t
	 */
	public GoldStandardException(Throwable t) {
		super(t);
	}
}
