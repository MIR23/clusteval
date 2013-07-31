/**
 * 
 */
package de.clusteval.paramOptimization;

/**
 * @author Christian Wiwie
 * 
 */
public class NoParameterSetFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4649681639838411864L;

	/**
	 * @param message
	 * 
	 */
	public NoParameterSetFoundException(final String message) {
		super(message);
	}
}
