/**
 * 
 */
package de.clusteval.cluster;

import junit.framework.Assert;
import junitx.framework.ArrayAssert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.clusteval.graphmatching.Cluster;
import de.clusteval.graphmatching.ClusterItem;
import de.clusteval.graphmatching.Clustering;

/**
 * @author Christian Wiwie
 * 
 */
public class TestClustering {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParseFromIntArray() {
		String[] ids = new String[]{"1", "2", "3", "4", "5"};
		int[] clusterIds = new int[]{1, 1, 1, 2, 2};

		Clustering expected = new Clustering();
		Cluster cluster1 = new Cluster("0");
		cluster1.add(new ClusterItem("1"), 1.0f);
		cluster1.add(new ClusterItem("2"), 1.0f);
		cluster1.add(new ClusterItem("3"), 1.0f);
		expected.addCluster(cluster1);
		Cluster cluster2 = new Cluster("1");
		cluster2.add(new ClusterItem("4"), 1.0f);
		cluster2.add(new ClusterItem("5"), 1.0f);
		expected.addCluster(cluster2);

		Clustering clustering = Clustering.parseFromIntArray(ids, clusterIds);
		Assert.assertEquals(expected, clustering);
	}

	@Test
	public void testToFormattedString() {
		String[] ids = new String[]{"1", "2", "3", "4", "5"};
		int[] clusterIds = new int[]{1, 1, 1, 2, 2};

		Clustering clustering = Clustering.parseFromIntArray(ids, clusterIds);
		Assert.assertEquals("5:1.0,4:1.0;3:1.0,2:1.0,1:1.0",
				clustering.toFormattedString());
	}

	@Test
	public void testToFormattedStringEmpty() {
		String[] ids = new String[]{};
		int[] clusterIds = new int[]{};

		Clustering clustering = Clustering.parseFromIntArray(ids, clusterIds);
		Assert.assertEquals("", clustering.toFormattedString());
	}

	@Test
	public void testParseFromIntArrayEmpty() {
		String[] ids = new String[]{};
		int[] clusterIds = new int[]{};

		Clustering clustering = Clustering.parseFromIntArray(ids, clusterIds);
		Assert.assertEquals(new Clustering(), clustering);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParseFromIntArrayDifferentLength() {
		String[] ids = new String[]{};
		int[] clusterIds = new int[]{1};

		Clustering.parseFromIntArray(ids, clusterIds);
	}

	@Test
	public void testParseFromIntArraySameObjects() {
		String[] ids = new String[]{"1", "2", "3", "4", "5"};
		int[] clusterIds = new int[]{1, 1, 1, 2, 2};

		Clustering clustering = Clustering.parseFromIntArray(ids, clusterIds);
		Assert.assertTrue(clustering
				.getClusterForItem(clustering.getClusterItemWithId("1"))
				.keySet().iterator().next() == clustering
				.getClusterForItem(clustering.getClusterItemWithId("2"))
				.keySet().iterator().next());
		Assert.assertTrue(clustering
				.getClusterForItem(clustering.getClusterItemWithId("2"))
				.keySet().iterator().next() == clustering
				.getClusterForItem(clustering.getClusterItemWithId("3"))
				.keySet().iterator().next());
		Assert.assertTrue(clustering
				.getClusterForItem(clustering.getClusterItemWithId("4"))
				.keySet().iterator().next() == clustering
				.getClusterForItem(clustering.getClusterItemWithId("5"))
				.keySet().iterator().next());
	}

	@Test
	public void testClusterIdsToFuzzyCoeff() {
		int[] clusterIds = new int[]{1, 2, 2, 2, 5, 3, 4, 1, 1};
		float[][] result = Clustering.clusterIdsToFuzzyCoeff(clusterIds);
		float[][] expected = new float[][]{{1f, 0f, 0f, 0f, 0f},
				{0f, 1f, 0f, 0f, 0f}, {0f, 1f, 0f, 0f, 0f},
				{0f, 1f, 0f, 0f, 0f}, {0f, 0f, 1f, 0f, 0f},
				{0f, 0f, 0f, 1f, 0f}, {0f, 0f, 0f, 0f, 1f},
				{1f, 0f, 0f, 0f, 0f}, {1f, 0f, 0f, 0f, 0f}};
		for (int i = 0; i < expected.length; i++)
			ArrayAssert.assertEquals(expected[i], result[i], 0f);
	}

}
