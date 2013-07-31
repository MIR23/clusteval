/**
 * 
 */
package de.clusteval.quality;

/**
 * This is a wrapper class for double values calculated by clustering quality
 * measures.
 * 
 * <p>
 * This wrapper class allows for the fact, that iterations of parameter
 * optimizations might not terminate. In this case {@link #toString()} returns
 * <b>"NT"</b>, where "NT" means <b>"Not Terminated"</b>. The factory method
 * {@link #getForNotTerminated()} returns such objects.
 * 
 * @author Christian Wiwie
 * 
 */
public class ClusteringQualityMeasureValue {

	/**
	 * The clustering quality assessed. Set this value to <b>null</b> if the
	 * iteration did not terminate.
	 */
	private Double value;

	/**
	 * A boolean indicating, whether the iteration belonging to this object
	 * terminated.
	 */
	protected boolean isTerminated;

	private ClusteringQualityMeasureValue(final Double value) {
		super();
		this.value = value;
		this.isTerminated = value != null;
	}

	/**
	 * @param value
	 *            The quality of the clustering as a double value.
	 * @return A wrapper object for a clustering quality given as a double
	 *         value.
	 */
	public static ClusteringQualityMeasureValue getForDouble(final double value) {
		return new ClusteringQualityMeasureValue(value);
	}

	/**
	 * @return A wrapper object for an optimization iteration that did not
	 *         terminate.
	 */
	public static ClusteringQualityMeasureValue getForNotTerminated() {
		return new ClusteringQualityMeasureValue(null);
	}

	@Override
	public String toString() {
		if (isTerminated)
			return value.toString();
		return "NT";
	}

	/**
	 * This method returns a clustering quality measure value wrapper object
	 * corresponding to the given string.
	 * 
	 * <p>
	 * If the string equals <b>NT</b>, a wrapper object for a not terminated
	 * iteration is returned by invoking {@link #getForNotTerminated()}.
	 * 
	 * <p>
	 * Otherwise the string is parsed as a double value and the result of
	 * {@link #getForDouble(double)} is returned.
	 * 
	 * @param stringValue
	 *            A string representation of the clustering quality.
	 * @return A clustering quality value wrapper object corresponding to the
	 *         given string.
	 */
	public static ClusteringQualityMeasureValue parseFromString(
			String stringValue) {
		if (stringValue.equals("NT"))
			return ClusteringQualityMeasureValue.getForNotTerminated();
		return ClusteringQualityMeasureValue.getForDouble(Double
				.valueOf(stringValue));
	}

	/**
	 * This method returns the quality of the clustering.
	 * 
	 * <p>
	 * It should only be invoked, if the corresponding iteration terminated and
	 * thus {@link #value} is != null.
	 * 
	 * @return The quality of the corresponding clustering.
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * @return A boolean indicating whether the iteration belonging to this
	 *         value terminated.
	 */
	public boolean isTerminated() {
		return this.isTerminated;
	}
}
