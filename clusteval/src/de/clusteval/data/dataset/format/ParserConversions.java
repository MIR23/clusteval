/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Christian Wiwie
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParserConversions {

	/**
	 * 
	 * @return All available conversions in an array.
	 */
	public StringMapping[] conversions();
}
