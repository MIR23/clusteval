/**
 * 
 */
package de.clusteval.utils.regexp;

/**
 * @author Christian Wiwie
 * 
 */
public class BraketRegExp extends AtomicRegExp {

	public BraketRegExp(final RegExp child) {
		super(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + childs.get(0).toString() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#toDNF()
	 */
	@Override
	public RegExp toDNF() {
		return childs.get(0).toDNF();
	}
}
