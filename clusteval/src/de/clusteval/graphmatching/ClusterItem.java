/**
 * 
 */
package de.clusteval.graphmatching;

import java.util.HashMap;
import java.util.Map;

/**
 * A cluster item can be part of several clusters.
 * 
 * @author Christian Wiwie
 */
public class ClusterItem {

	/**
	 * The id of the item, should be unique.
	 */
	protected String id;

	/**
	 * The clusters this item is contained in together with the corresponding
	 * fuzzy coefficients.
	 */
	protected Map<Cluster, Float> fuzzyClusters;

	/**
	 * Instantiates a new cluster item with a certain id.
	 * 
	 * @param id
	 *            The id of the new cluster item.
	 */
	public ClusterItem(final String id) {
		super();

		this.id = id;
		this.fuzzyClusters = new HashMap<Cluster, Float>();
	}

	/**
	 * The copy constructor of cluster items.
	 * 
	 * @param other
	 *            The object to clone.
	 */
	public ClusterItem(final ClusterItem other) {
		super();

		this.id = other.id;
		this.fuzzyClusters = cloneFuzzyClusters(other.fuzzyClusters);
	}

	@Override
	public ClusterItem clone() {
		return new ClusterItem(this);
	}

	protected static Map<Cluster, Float> cloneFuzzyClusters(
			Map<Cluster, Float> fuzzyClusters) {
		final Map<Cluster, Float> result = new HashMap<Cluster, Float>();

		for (Map.Entry<Cluster, Float> entry : fuzzyClusters.entrySet()) {
			result.put(entry.getKey().clone(), new Float(entry.getValue()));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClusterItem))
			return false;

		ClusterItem other = (ClusterItem) obj;

		return this.id.equals(other.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.id).hashCode();
	}

	/**
	 * 
	 * @return The id of this cluster item.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * 
	 * @return A map with all clusters this item is contained in together with
	 *         the corresponding fuzzy coefficients.
	 */
	public Map<Cluster, Float> getFuzzyClusters() {
		return this.fuzzyClusters;
	}

	/**
	 * Adds a new fuzzy cluster to this items' list of clusters.
	 * 
	 * @param cluster
	 *            The new cluster to add.
	 * @param fuzzy
	 *            The fuzzy coefficient
	 * @return true, if an old fuzzy coefficient for this cluster was replaced,
	 *         false otherwise.
	 */
	public boolean addFuzzyCluster(final Cluster cluster, float fuzzy) {
		return this.fuzzyClusters.put(cluster, fuzzy) != null;
	}

	/**
	 * Removes one of this items' clusters.
	 * 
	 * @param cluster
	 *            The cluster to remove
	 * @return True, if the cluster was contained in the list and was removed,
	 *         false otherwise.
	 */
	public boolean removeFuzzyCluster(final Cluster cluster) {
		return this.fuzzyClusters.remove(cluster) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.id;
	}
}
