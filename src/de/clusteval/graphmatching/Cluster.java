/*******************************************************************************
 * Copyright (c) 2013 Christian Wiwie.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Christian Wiwie - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.graphmatching;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A cluster is part of a clustering and contains several item.
 * 
 * @author Christian Wiwie
 */
public class Cluster implements Iterable<ClusterItem> {

	/**
	 * The id of the cluster should be unique per clustering
	 */
	protected String id;

	/**
	 * The items contained in this cluster. Since we support fuzzy clusters, for
	 * every item we also have to store the fuzzy coefficient.
	 */
	protected Map<ClusterItem, Float> fuzzyItems;

	/**
	 * The (fuzzy) size of this cluster is the sum of the fuzzy coefficients of
	 * all items contained in this cluster.
	 */
	protected float fuzzySize;

	/**
	 * Instantiates a new cluster with a given id.
	 * 
	 * @param id
	 *            The id of the cluster.
	 */
	public Cluster(final String id) {
		super();
		this.id = id;
		this.fuzzyItems = new HashMap<ClusterItem, Float>();
	}

	/**
	 * The copy constructor of clusters.
	 * 
	 * @param other
	 *            The object to clone.
	 */
	public Cluster(final Cluster other) {
		super();
		this.id = other.id;
		this.fuzzyItems = cloneFuzzyItems(other.fuzzyItems);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Cluster clone() {
		return new Cluster(this);
	}

	protected static Map<ClusterItem, Float> cloneFuzzyItems(
			Map<ClusterItem, Float> fuzzyItems) {
		final Map<ClusterItem, Float> result = new HashMap<ClusterItem, Float>();

		for (Map.Entry<ClusterItem, Float> entry : fuzzyItems.entrySet()) {
			result.put(entry.getKey().clone(), new Float(entry.getValue()));
		}

		return result;
	}

	/**
	 * 
	 * @return The id of the cluster.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Checks whether this cluster contains a certain item.
	 * 
	 * @param item
	 *            The item to check for.
	 * @return true, if this cluster contains the item, false otherwise.
	 */
	public boolean contains(ClusterItem item) {
		return this.fuzzyItems.containsKey(item);
	}

	/**
	 * @return A map with all items contained in this cluster together with
	 *         their fuzzy coefficients.
	 */
	public Map<ClusterItem, Float> getFuzzyItems() {
		return this.fuzzyItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Cluster))
			return false;

		Cluster other = (Cluster) o;
		return fuzzyItems.equals(other.fuzzyItems);
		// return id.equals(other.id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// return (this.fuzzyItems).hashCode();
		return this.id.hashCode();
	}

	/**
	 * The (fuzzy) size of this cluster is the sum of the fuzzy coefficients of
	 * all items contained in this cluster.
	 * 
	 * <p>
	 * In case that this clustering is a crisp clustering (all fuzzy
	 * coefficients = 1.0), this size is the same as {@link #size()}.
	 * 
	 * @return The fuzzy size of this cluster.
	 */
	public float fuzzySize() {
		return this.fuzzySize;
	}

	/**
	 * Add a new item to this cluster with a certain fuzzy coefficient.
	 * 
	 * @param item
	 *            The item to add.
	 * @param fuzzy
	 *            The fuzzy coefficient of the new item.
	 * @return true, if successful
	 */
	// TODO: check that fuzzy coefficients add at most to 1
	public boolean add(ClusterItem item, float fuzzy) {
		if (fuzzy == 0.0)
			return true;
		this.fuzzyItems.put(item, fuzzy);
		this.fuzzySize += fuzzy;
		item.addFuzzyCluster(this, fuzzy);
		return true;
	}

	/**
	 * Remove an item from this cluster.
	 * 
	 * @param item
	 *            The item to remove.
	 * @return True, if successful
	 */
	public boolean remove(ClusterItem item) {
		item.removeFuzzyCluster(this);
		float fuzzy = this.fuzzyItems.remove(item);
		this.fuzzySize -= fuzzy;
		return true;
	}

	/**
	 * @return The number of items contained in this cluster.
	 * 
	 *         <p>
	 *         In case of fuzzy clusterings this does not necessarily return the
	 *         same as {@link #fuzzySize()}!
	 */
	public int size() {
		return this.fuzzyItems.keySet().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id + ": " + fuzzyItems.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ClusterItem> iterator() {
		// TODO: not fuzzy so far
		return this.fuzzyItems.keySet().iterator();
	}
}
