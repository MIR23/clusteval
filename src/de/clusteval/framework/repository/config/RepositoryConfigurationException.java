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
package de.clusteval.framework.repository.config;

import de.clusteval.utils.ClustEvalException;

/**
 * @author Christian Wiwie
 * 
 */
public class RepositoryConfigurationException extends ClustEvalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3499581646376240590L;

	/**
	 * @param message
	 */
	public RepositoryConfigurationException(String message) {
		super(message);
	}
}
