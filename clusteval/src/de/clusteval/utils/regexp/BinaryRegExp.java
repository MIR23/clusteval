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
public abstract class BinaryRegExp extends RegExp {

	protected RegExp child1;
	protected RegExp child2;

	public BinaryRegExp(final RegExp child1, final RegExp child2) {
		super();
		this.child1 = child1;
		this.child2 = child2;
	}
	
	/* (non-Javadoc)
	 * @see de.clusteval.utils.regexp.RegExp#getChilds()
	 */
	@Override
	public Set<RegExp> getChilds() {
		Set<RegExp> result = new HashSet<RegExp>();
		result.add(child1);
		result.add(child2);
		return result;
	}
}
