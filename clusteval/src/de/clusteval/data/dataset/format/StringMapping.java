/**
 * 
 */
package de.clusteval.data.dataset.format;

/**
 * @author Christian Wiwie
 * 
 */
public @interface StringMapping {

	/**
	 * 
	 * @return The key of this mapping.
	 */
	public String key();

	/**
	 * 
	 * @return The value of this mapping.
	 */
	public String value();
}
