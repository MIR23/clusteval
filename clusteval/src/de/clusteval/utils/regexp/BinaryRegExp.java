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
