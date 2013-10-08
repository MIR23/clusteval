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
package de.clusteval.run;

import de.clusteval.utils.ClustEvalException;

// TODO: Auto-generated Javadoc
/**
 * The Class MissingParameterValueException.
 * 
 * @author Christian Wiwie
 */
public class MissingParameterValueException extends ClustEvalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5193016408160752172L;

	/**
	 * Instantiates a new missing parameter value exception.
	 * 
	 * @param string
	 *            the string
	 */
	public MissingParameterValueException(String string) {
		super(string);
	}

}
