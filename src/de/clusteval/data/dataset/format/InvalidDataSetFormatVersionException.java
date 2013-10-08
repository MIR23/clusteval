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
package de.clusteval.data.dataset.format;

import de.clusteval.data.dataset.DataSetException;

/**
 * @author Christian Wiwie
 * 
 */
public class InvalidDataSetFormatVersionException extends DataSetException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6502244551092326961L;

	/**
	 * @param message
	 */
	public InvalidDataSetFormatVersionException(final String message) {
		super(message);
	}
}
