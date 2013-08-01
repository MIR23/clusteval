/**
 * 
 */
package de.clusteval.utils.regexp;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author Christian Wiwie
 * 
 */
public class AndRegExp extends BinaryRegExp {

	public AndRegExp(final RegExp child1, final RegExp child2) {
		super(child1, child2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return child1.toString() + "&" + child2.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#toDNF()
	 */
	@Override
	public RegExp toDNF() {
		RegExp dnf1 = child1.toDNF();
		RegExp dnf2 = child2.toDNF();
		Set<RegExp> dis1 = getDisjunctiveElements(dnf1);
		Set<RegExp> dis2 = getDisjunctiveElements(dnf2);

		RegExp result = null;
		for (RegExp e1 : dis1) {
			for (RegExp e2 : dis2) {
				if (result == null) {
					result = new AndRegExp(e1, e2);
				} else {
					result = new OrRegExp(result, new AndRegExp(e1, e2));
				}
			}
		}

		return result;
	}

	protected static Set<RegExp> getDisjunctiveElements(final RegExp exp) {
		Set<RegExp> result = new HashSet<RegExp>();
		Stack<RegExp> toCheck = new Stack<RegExp>();
		toCheck.add(exp);
		while (toCheck.size() > 0) {
			RegExp e = toCheck.pop();
			if (e instanceof OrRegExp) {
				for (RegExp child : e.getChilds())
					toCheck.addAll(getDisjunctiveElements(child));
			} else {
				result.add(e);
			}
		}
		return result;
	}
}
