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
public class Token extends RegExp {

	protected String token;

	public Token(final String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#getChilds()
	 */
	@Override
	public Set<RegExp> getChilds() {
		return new HashSet<RegExp>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.token;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.regexp.RegExp#toDNF()
	 */
	@Override
	public RegExp toDNF() {
		return this;
	}
}
