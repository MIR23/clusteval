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
public class DataSetConfigurationException extends DataSetException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3281232015350571965L;

	/**
	 * @param message
	 */
	public DataSetConfigurationException(String message) {
		super(message);
	}

	/**
	 * @param t
	 */
	public DataSetConfigurationException(Throwable t) {
		super(t);
	}

}
