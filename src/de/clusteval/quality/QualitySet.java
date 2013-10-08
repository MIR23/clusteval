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
package de.clusteval.quality;

import java.util.HashMap;

/**
 * A clustering quality set is a map with clustering quality measures mapped to
 * clustering quality measure values achieved for each of those.
 * 
 * @author Christian Wiwie
 * 
 */
public class QualitySet extends HashMap<QualityMeasure, QualityMeasureValue> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7026335787094648699L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public QualitySet clone() {
		return (QualitySet) super.clone();
	}
}
