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
		return childs.get(0).toString() + "|" + childs.get(1).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#toDNF()
	 */
	@Override
	public RegExp toDNF() {
		return new OrRegExp(childs.get(0).toDNF(), childs.get(1).toDNF());
	}
}
