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
package de.clusteval.data.goldstandard.format;

import de.clusteval.data.goldstandard.GoldStandardException;

/**
 * @author Christian Wiwie
 */
public class UnknownGoldStandardFormatException extends GoldStandardException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5160151162340633793L;

	/**
	 * Instantiates a new unknown gold standard format exception.
	 * 
	 * @param message
	 *            the message
	 */
	public UnknownGoldStandardFormatException(final String message) {
		super(message);
	}
}
