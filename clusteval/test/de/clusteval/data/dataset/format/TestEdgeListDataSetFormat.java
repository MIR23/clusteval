/**
 * 
 */
package de.clusteval.data.dataset.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import de.clusteval.context.Context;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.StubSQLCommunicator;
import de.clusteval.utils.StubRepositoryObject;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * @author Christian Wiwie
 * 
 */
public class TestEdgeListDataSetFormat {

	protected Repository repository;
	protected RepositoryObject repositoryObject;
	protected Context context;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ClustevalBackendServer.logLevel(Level.INFO);
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
		repository = new Repository(
				new File("testCaseRepository").getAbsolutePath(), null);
		repository.setSQLCommunicator(new StubSQLCommunicator(repository));
		repository.initialize();
		repositoryObject = new StubRepositoryObject(this.repository, false,
				System.currentTimeMillis(), new File("test"));
		context = Context.parseFromString(repository, "GraphMatchingContext");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEdgeListDataSetFormatParser()
			throws InvalidDataSetFormatVersionException, IOException,
			UnknownDataSetFormatException, RegisterException,
			UnknownDataSetTypeException, InstantiationException,
			IllegalAccessException {
		Class<? extends DataSetFormatParser> pClass = repository
				.getDataSetFormatParser("de.clusteval.data.dataset.format.EdgeListDataSetFormat");
		DataSetFormatParser p = pClass.newInstance();

		File f = new File(
				"testCaseRepository/results/08_02_2013-14_19_05_rashid/inputs/gedevo_rashid/rashid/N1.edgelist_gedevo.strip")
				.getAbsoluteFile();
		DataSet ds = new DataSet(repository, false, f.lastModified(), f, "Bla",
				DataSetFormat.parseFromString(repository,
						"EdgeListDataSetFormat"), DataSetType.parseFromString(
						repository, "OtherDataSetType"));

		DirectedSparseMultigraph<String, String> g = p
				.parse(new ArrayList<DataSet>(Arrays.asList(new DataSet[]{ds})));
		System.out.println(g);
	}
}
