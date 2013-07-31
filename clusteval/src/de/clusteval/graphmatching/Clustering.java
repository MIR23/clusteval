/**
 * 
 */
package de.clusteval.graphmatching;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Pair;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.quality.ClusteringQualityMeasure;
import de.clusteval.quality.ClusteringQualityMeasureValue;
import de.clusteval.quality.ClusteringQualitySet;

/**
 * A clustering contains several clusters. Every cluster contains cluster items.
 */
public class Clustering implements Iterable<Cluster> {

	/**
	 * The fuzzy size of this clustering is the sum of all fuzzy coefficients of
	 * any item contained in any cluster.
	 */
	protected float fuzzySize;

	/**
	 * The clusters contained in this clustering.
	 */
	protected Set<Cluster> clusters;

	/**
	 * Used to get clusters in O(1) with their id.
	 */
	protected Map<String, Cluster> clusterIdToCluster;

	/**
	 * A map from cluster item to cluster and fuzzy coefficient. This serves for
	 * fast access and performance purposes only.
	 */
	protected Map<ClusterItem, Map<Cluster, Float>> itemToCluster;

	/**
	 * Used to get cluster items in O(1) with their id.
	 */
	protected Map<String, ClusterItem> itemIdToItem;

	/**
	 * If the qualities of this clustering were set using the method
	 * {@link #setQualities(ClusteringQualitySet)}, they are stored in this
	 * attribute.
	 */
	protected ClusteringQualitySet qualities;

	/**
	 * Instantiates a new clustering.
	 */
	public Clustering() {
		super();
		this.clusters = new HashSet<Cluster>();
		this.clusterIdToCluster = new HashMap<String, Cluster>();
		this.itemToCluster = new HashMap<ClusterItem, Map<Cluster, Float>>();
		this.itemIdToItem = new HashMap<String, ClusterItem>();
	}

	/**
	 * The copy constructor of clusterings.
	 * 
	 * @param other
	 *            The object to clone.
	 */
	public Clustering(final Clustering other) {
		super();
		this.clusters = cloneClusters(other.clusters);
		this.clusterIdToCluster = cloneClusterIdToCluster(other.clusterIdToCluster);
		this.itemToCluster = cloneItemToClusters(other.itemToCluster);
		this.itemIdToItem = cloneItemIdToItem(other.itemIdToItem);
	}

	@Override
	public Clustering clone() {
		return new Clustering(this);
	}

	protected static Map<String, Cluster> cloneClusterIdToCluster(
			Map<String, Cluster> clusterIdToCluster) {
		final Map<String, Cluster> result = new HashMap<String, Cluster>();

		for (Map.Entry<String, Cluster> entry : clusterIdToCluster.entrySet()) {
			result.put(entry.getKey(), entry.getValue().clone());
		}

		return result;
	}

	protected static Map<String, ClusterItem> cloneItemIdToItem(
			Map<String, ClusterItem> itemIdToItem) {
		final Map<String, ClusterItem> result = new HashMap<String, ClusterItem>();

		for (Map.Entry<String, ClusterItem> entry : itemIdToItem.entrySet()) {
			result.put(entry.getKey(), entry.getValue().clone());
		}

		return result;
	}

	protected static Map<ClusterItem, Map<Cluster, Float>> cloneItemToClusters(
			Map<ClusterItem, Map<Cluster, Float>> itemToCluster2) {
		final Map<ClusterItem, Map<Cluster, Float>> result = new HashMap<ClusterItem, Map<Cluster, Float>>();

		for (Map.Entry<ClusterItem, Map<Cluster, Float>> entry : itemToCluster2
				.entrySet()) {
			final Map<Cluster, Float> newMap = new HashMap<Cluster, Float>();

			for (Map.Entry<Cluster, Float> entry2 : entry.getValue().entrySet()) {
				newMap.put(entry2.getKey().clone(),
						new Float(entry2.getValue()));
			}

			result.put(entry.getKey().clone(), newMap);
		}

		return result;
	}

	protected static Set<Cluster> cloneClusters(Set<Cluster> clusters) {
		final Set<Cluster> result = new HashSet<Cluster>();

		for (Cluster cl : clusters)
			result.add(cl.clone());

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Clustering))
			return false;
		Clustering other = (Clustering) obj;
		return this.clusters.equals(other.clusters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.clusters.hashCode();
	}

	/**
	 * @param item
	 *            The item to look for.
	 * @return A map containing all clusters together with fuzzy coefficients,
	 *         in which the given item is contained.
	 */
	public Map<Cluster, Float> getClusterForItem(ClusterItem item) {
		return this.itemToCluster.get(item);
	}

	/**
	 * @param id
	 *            The id of the cluster.
	 * @return The cluster with the given id.
	 */
	public Cluster getClusterWithId(final String id) {
		return this.clusterIdToCluster.get(id);
	}

	/**
	 * @return A set with all clusters of this clustering.
	 */
	public Set<Cluster> getClusters() {
		return this.clusters;
	}

	/**
	 * @param qualitySet
	 *            Set the qualities of this clustering.
	 */
	public void setQualities(final ClusteringQualitySet qualitySet) {
		this.qualities = qualitySet;
	}

	/**
	 * @return Returns the qualities of this clustering.
	 * @see Clustering#qualities
	 */
	public ClusteringQualitySet getQualities() {
		return this.qualities;
	}

	/**
	 * @return A set with all cluster items contained in this clustering.
	 */
	public Set<ClusterItem> getClusterItems() {
		return this.itemToCluster.keySet();
	}

	/**
	 * 
	 * @param id
	 *            The id of the cluster item.
	 * @return The cluster item with the given id.
	 */
	public ClusterItem getClusterItemWithId(final String id) {
		return this.itemIdToItem.get(id);
	}

	/**
	 * Add a cluster to this clustering.
	 * 
	 * @param cluster
	 *            The cluster to add.
	 * @return true, if the cluster is added and hasn't been in the clustering
	 *         before.
	 */
	public boolean addCluster(final Cluster cluster) {
		boolean b = this.clusters.add(cluster);
		if (b) {
			this.clusterIdToCluster.put(cluster.getId(), cluster);
			this.fuzzySize += cluster.fuzzySize();
			Map<ClusterItem, Float> items = cluster.getFuzzyItems();
			for (ClusterItem item : items.keySet()) {
				if (!this.itemToCluster.containsKey(item))
					this.itemToCluster.put(item, new HashMap<Cluster, Float>());
				this.itemToCluster.get(item).putAll(item.getFuzzyClusters());

				if (!itemIdToItem.containsKey(item.getId()))
					itemIdToItem.put(item.getId(), item);
			}
		}
		return b;
	}

	/**
	 * Remove a cluster item from this clustering by removing the item from
	 * every cluster contained.
	 * 
	 * @param item
	 *            The item to remove
	 * @return True if this item was contained in this clustering.
	 */
	public boolean removeClusterItem(final ClusterItem item) {
		Map<Cluster, Float> fuzzyClusters = this.itemToCluster.remove(item);
		boolean result = false;
		for (Cluster cl : fuzzyClusters.keySet())
			result = cl.remove(item);
		return result;
	}

	/**
	 * @return The fuzzy size of this clustering.
	 * @see #fuzzySize
	 */
	public float fuzzySize() {
		return this.fuzzySize;
	}

	/**
	 * @return The number of items in this clustering. In case of fuzzy
	 *         clusterings this may differ from the fuzzy size.
	 */
	public int size() {
		return this.itemToCluster.keySet().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Cluster> iterator() {
		return this.clusters.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Clustering: " + clusters.toString() + "]";
	}

	/**
	 * 
	 * @return A string representing this clustering, where clusters are
	 *         separated by semi-colons and elements of clusters are separated
	 *         by commas.
	 */
	public String toFormattedString() {
		StringBuilder sb = new StringBuilder();
		for (Cluster cluster : this.clusters) {
			for (Map.Entry<ClusterItem, Float> entry : cluster.getFuzzyItems()
					.entrySet()) {
				if (entry.getValue() > 0f) {
					sb.append(entry.getKey());
					sb.append(":");
					sb.append(entry.getValue());
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(";");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * This method parses clusterings together with the corresponding parameter
	 * sets from a file.
	 * 
	 * @param repository
	 * 
	 * @param absFilePath
	 *            The absolute path to the input file.
	 * @param parseQualities
	 *            True, if the qualities of the clusterings should also be
	 *            parsed. Those will be taken from .qual-files.
	 * @return A map containing parameter sets and corresponding clusterings.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Pair<Map<String, Double>, Clustering> parseFromFile(
			final Repository repository, final File absFilePath,
			final boolean parseQualities) throws IOException {
		ClusteringParser parser = new ClusteringParser(repository,
				absFilePath.getAbsolutePath(), parseQualities);
		parser.process();

		return parser.getClusterings();
	}

	/**
	 * Convert an integer array holding cluster ids for every object to a fuzzy
	 * coefficient matrix.
	 * 
	 * @param clusterIds
	 *            The cluster ids of the objects.
	 * @return Fuzzy coefficient matrix. [i][j] holds the fuzzy coefficient for
	 *         object i and cluster j.
	 */
	public static float[][] clusterIdsToFuzzyCoeff(final int[] clusterIds) {
		Map<Integer, Integer> clusterPos = new HashMap<Integer, Integer>();
		for (int id : clusterIds)
			if (!(clusterPos.containsKey(id)))
				clusterPos.put(id, clusterPos.size());

		int numberClusters = clusterPos.keySet().size();

		float[][] fuzzy = new float[clusterIds.length][numberClusters];
		for (int i = 0; i < clusterIds.length; i++)
			fuzzy[i][clusterPos.get(clusterIds[i])] = 1.0f;
		return fuzzy;
	}

	/**
	 * The passed clustering is assumed to be a hard (non-fuzzy) clustering.
	 * 
	 * @param objectIds
	 *            The ids of the cluster items.
	 * @param clusterIds
	 *            Position i holds the cluster id of cluster item i.
	 * @return A clustering wrapper object.
	 */
	public static Clustering parseFromIntArray(final String[] objectIds,
			final int[] clusterIds) {
		return parseFromFuzzyCoeffMatrix(objectIds,
				clusterIdsToFuzzyCoeff(clusterIds));
	}

	/**
	 * @param objectIds
	 *            The ids of the cluster items.
	 * @param fuzzyCoeffs
	 *            Position [i,j] is the fuzzy coefficient of object i and
	 *            cluster j.
	 * @return A clustering wrapper object.
	 */
	public static Clustering parseFromFuzzyCoeffMatrix(
			final String[] objectIds, final float[][] fuzzyCoeffs) {
		if (objectIds.length != fuzzyCoeffs.length)
			throw new IllegalArgumentException(
					"The number of object ids and cluster ids needs to be the same.");
		Map<String, Cluster> clusters = new HashMap<String, Cluster>();

		for (int i = 0; i < fuzzyCoeffs.length; i++) {
			ClusterItem item = new ClusterItem(objectIds[i]);
			for (int j = 0; j < fuzzyCoeffs[i].length; j++) {
				final String clusterId = j + "";
				Cluster cluster = clusters.get(clusterId);
				if (cluster == null) {
					cluster = new Cluster(clusterId);
					clusters.put(clusterId, cluster);
				}

				cluster.add(item, fuzzyCoeffs[i][j]);
			}
		}
		Clustering clustering = new Clustering();
		for (Cluster cl : clusters.values())
			clustering.addCluster(cl);
		return clustering;
	}

	/**
	 * Assess quality.
	 * 
	 * @param dataConfig
	 * 
	 * @param qualityMeasures
	 *            the quality measures
	 * @return A set of qualities for every quality measure that was passed in
	 *         the list.
	 * @throws UnknownGoldStandardFormatException
	 *             the unknown gold standard format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 */
	public ClusteringQualitySet assessQuality(final DataConfig dataConfig,
			final List<ClusteringQualityMeasure> qualityMeasures)
			throws UnknownGoldStandardFormatException, IOException,
			UnknownDataSetFormatException, InvalidDataSetFormatVersionException {
		// TODO: 20.08.2012 ensure, that this runresult is in standard format
		final ClusteringQualitySet resultSet = new ClusteringQualitySet();
		for (ClusteringQualityMeasure qualityMeasure : qualityMeasures) {
			// do not calculate, when there is no goldstandard
			if (qualityMeasure.requiresGoldstandard()
					&& !dataConfig.hasGoldStandardConfig())
				continue;
			ClusteringQualityMeasureValue quality;
			try {
				Clustering goldStandard = null;
				if (dataConfig.hasGoldStandardConfig())
					goldStandard = dataConfig.getGoldstandardConfig()
							.getGoldstandard().getClustering();
				quality = qualityMeasure.getQualityOfClustering(this,
						goldStandard, dataConfig);
				if (dataConfig.hasGoldStandardConfig())
					dataConfig.getGoldstandardConfig().getGoldstandard()
							.unloadFromMemory();
				// we rethrow some exceptions, since they mean, that we
				// cannot calculate ANY quality measures for this data
			} catch (UnknownGoldStandardFormatException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch (UnknownDataSetFormatException e) {
				throw e;
			} catch (InvalidDataSetFormatVersionException e) {
				throw e;
			} catch (Exception e) {
				// all the remaining exceptions are catched, because they
				// mean, that the quality measure calculation is flawed
				quality = ClusteringQualityMeasureValue
						.getForDouble(Double.NaN);
			}
			resultSet.put(qualityMeasure, quality);
		}
		return resultSet;
	}
}
