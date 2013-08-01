/**
 * 
 */
package de.clusteval.utils.regexp;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christian Wiwie
 * 
 */
public class OrRegExp extends BinaryRegExp {

	public OrRegExp(final RegExp child1, final RegExp child2) {
		super(child1, child2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return child1.toString() + "|" + child2.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#toDNF()
	 */
	@Override
	public RegExp toDNF() {
		return new OrRegExp(child1.toDNF(), child2.toDNF());
	}
}
