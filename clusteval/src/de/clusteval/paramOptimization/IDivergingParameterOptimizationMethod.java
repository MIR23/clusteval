/**
 * 
 */
package de.clusteval.paramOptimization;

import de.clusteval.quality.ClusteringQualitySet;


/**
 * @author Christian Wiwie
 *
 */
public interface IDivergingParameterOptimizationMethod {
	/**
	 * @param minimalQualities
	 */
	public void giveFeedbackNotTerminated(ClusteringQualitySet minimalQualities);
}
