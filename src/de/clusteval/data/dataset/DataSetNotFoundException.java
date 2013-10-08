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
package de.clusteval.data.dataset;

/**
 * @author Christian Wiwie
 * 
 */
public class DataSetNotFoundException extends DataSetException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6957759734342865464L;

	/**
	 * @param message
	 */
	public DataSetNotFoundException(String message) {
		super(message);
	}

}
