/**
 * 
 */
package de.clusteval.data.dataset.format;


/**
 * A DataSetFormat annotated as Parsable can be parsed to a Graph.
 * 
 * @author Christian Wiwie
 * 
 */
//@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.METHOD)
public interface Parsable {

	/**
	 * 
	 * @return Optional inputs that can be introduced in the graph parsing
	 *         process.
	 */
	public String[] optionalInputs();
}
