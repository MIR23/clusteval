/**
 * 
 */
package de.clusteval.program;

import java.util.HashMap;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterSet extends HashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 405272229276934252L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public ParameterSet clone() {
		return (ParameterSet) super.clone();
	}
}
