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
package de.clusteval.utils.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utils.parse.AbstractNode;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class BinaryRegExp extends RegExp {

	public BinaryRegExp(final RegExp child1, final RegExp child2) {
		super(new ArrayList<RegExp>(Arrays.asList(child1, child2)));
	}
}
