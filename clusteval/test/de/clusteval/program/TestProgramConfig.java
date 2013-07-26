/**
 * 
 */
package de.clusteval.program;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.Pair;
import ch.qos.logback.classic.Level;
import de.clusteval.context.Context;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.StubSQLCommunicator;

/**
 * @author Christian Wiwie
 * 
 */
public class TestProgramConfig {

	protected Repository repository;
	protected ProgramConfig programConfig;
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
		programConfig = ProgramConfig.parseFromFile(new File(
				"testCaseRepository/programs/configs/gedevo.config")
				.getAbsoluteFile());
		context = Context.parseFromString(repository, "ClusteringContext");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.programConfig = null;
		repository.finalize();
		Repository.unregister(repository);
	}

	@Test
	public void testCheckCompatibilityToDataSetFormats() {
		Assert.assertFalse(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays
						.asList(new String[]{""})));
		Assert.assertFalse(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays
						.asList(new String[]{"EdgeListDataSetFormat"})));

		// mandatory inputs are not replaced by optional
		Assert.assertFalse(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "SeqSimDataSetFormat"})));
		// positive cases
		Assert.assertTrue(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "EdgeListDataSetFormat",
						"SeqSimDataSetFormat"})));
		Assert.assertTrue(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays
						.asList(new String[]{"EdgeListHDataSetFormat"})));
		Assert.assertTrue(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays.asList(new String[]{
						"EdgeListHDataSetFormat", "EdgeListDataSetFormat"})));

		// too many inputs are ignored
		Assert.assertTrue(this.programConfig
				.checkCompatibilityToDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "EdgeListDataSetFormat",
						"EdgeListDataSetFormat", "SeqSimDataSetFormat"})));
	}

	@Test
	public void testCompatibleDataSetFormats() {
		Set<List<Pair<String, String>>> expected = new HashSet<List<Pair<String, String>>>();
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays.asList(new String[]{""})));
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays
						.asList(new String[]{"EdgeListDataSetFormat"})));

		// mandatory inputs are not replaced by optional
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "SeqSimDataSetFormat"})));
		
		// positive cases
		expected.clear();
		List<Pair<String, String>> l = new ArrayList<Pair<String, String>>();
		l.add(Pair.getPair("EdgeListDataSetFormat", "EdgeListDataSetFormat"));
		l.add(Pair.getPair("EdgeListDataSetFormat", "EdgeListDataSetFormat"));
		l.add(Pair.getPair("SeqSimDataSetFormat", "SeqSimDataSetFormat"));
		expected.add(l);
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "EdgeListDataSetFormat",
						"SeqSimDataSetFormat"})));
	
		expected.clear();
		l = new ArrayList<Pair<String, String>>();
		l.add(Pair.getPair("EdgeListHDataSetFormat", "EdgeListHDataSetFormat"));
		expected.add(l);
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays
						.asList(new String[]{"EdgeListHDataSetFormat"})));
		
		expected.clear();
		l = new ArrayList<Pair<String, String>>();
		l.add(Pair.getPair("EdgeListHDataSetFormat", "EdgeListHDataSetFormat"));
		expected.add(l);
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays.asList(new String[]{
						"EdgeListHDataSetFormat", "EdgeListDataSetFormat"})));

		// two possible mappings
		expected.clear();
		l = new ArrayList<Pair<String, String>>();
		l.add(Pair.getPair("EdgeListHDataSetFormat", "EdgeListHDataSetFormat"));
		expected.add(l);
		l = new ArrayList<Pair<String, String>>();
		l.add(Pair.getPair("EdgeListDataSetFormat", "EdgeListDataSetFormat"));
		l.add(Pair.getPair("EdgeListDataSetFormat", "EdgeListDataSetFormat"));
		l.add(Pair.getPair("SeqSimDataSetFormat", "SeqSimDataSetFormat"));
		expected.add(l);
		Assert.assertEquals(expected, this.programConfig
				.getCompatibleDataSetFormats(Arrays.asList(new String[]{
						"EdgeListDataSetFormat", "EdgeListDataSetFormat",
						"EdgeListHDataSetFormat", "SeqSimDataSetFormat"})));
	}

}
