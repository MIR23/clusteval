/**
 * 
 */
package de.clusteval.quality;

import java.util.HashMap;

/**
 * A clustering quality set is a map with clustering quality measures mapped to
 * clustering quality measure values achieved for each of those.
 * 
 * @author Christian Wiwie
 * 
 */
public class QualitySet extends HashMap<QualityMeasure, QualityMeasureValue> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7026335787094648699L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public QualitySet clone() {
		return (QualitySet) super.clone();
	}
}
