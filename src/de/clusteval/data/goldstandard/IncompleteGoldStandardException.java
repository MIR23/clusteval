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

import java.util.Set;

/**
 * @author Christian Wiwie
 * 
 */
public class IncompleteGoldStandardException extends GoldStandardException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4986352597738030172L;

	/**
	 * @param missingIds
	 */
	public IncompleteGoldStandardException(final Set<String> missingIds) {
		super("The goldstandard is missing entries: " + missingIds);
	}

}
