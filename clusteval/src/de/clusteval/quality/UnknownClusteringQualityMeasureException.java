/**
 * 
 */
package de.clusteval.quality;

/**
 * @author Christian Wiwie
 */
public class UnknownClusteringQualityMeasureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 433568096995882002L;

	/**
	 * Instantiates a new unknown clustering quality measure exception.
	 * 
	 * @param string
	 *            the string
	 */
	public UnknownClusteringQualityMeasureException(String string) {
		super(string);
	}
}
