/**
 * 
 */
package de.clusteval.paramOptimization;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Christian Wiwie
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadableClassParentAnnotation {

	/**
	 * 
	 * @return The name of the parent class required for an annotated class to
	 *         be loaded.
	 */
	String parent();
}
