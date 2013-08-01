/**
 * 
 */
package de.clusteval.paramOptimization;

import de.clusteval.quality.QualitySet;


/**
 * @author Christian Wiwie
 *
 */
public interface IDivergingParameterOptimizationMethod {
	/**
	 * @param minimalQualities
	 */
	public void giveFeedbackNotTerminated(QualitySet minimalQualities);
}
