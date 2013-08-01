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
public abstract class AtomicRegExp extends RegExp {

	protected RegExp child;

	public AtomicRegExp(final RegExp child) {
		super();
		this.child = child;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#getChilds()
	 */
	@Override
	public Set<RegExp> getChilds() {
		Set<RegExp> result = new HashSet<RegExp>();
		result.add(child);
		return result;
	}

}
