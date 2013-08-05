/**
 * 
 */
package de.clusteval.utils.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class AtomicRegExp extends RegExp {

	public AtomicRegExp(final RegExp child) {
		super(new ArrayList<RegExp>(Arrays.asList(child)));
	}
}
