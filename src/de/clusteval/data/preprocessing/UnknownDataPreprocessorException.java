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
package de.clusteval.data.preprocessing;

import de.clusteval.utils.ClustEvalException;

/**
 * @author Christian Wiwie
 * 
 */
public class UnknownDataPreprocessorException extends ClustEvalException {

	/**
	 * @param string
	 */
	public UnknownDataPreprocessorException(String string) {
		super(string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2579604548304742773L;

}
