/**
 * 
 */
package de.clusteval.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.clusteval.cluster.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;

/**
 * @author Christian Wiwie
 * 
 */
public class TestProgramConfig {

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
	public void testParseFromFileCompatibleDataSetFormats()
			throws FileNotFoundException, RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, UnknownDataSetFormatException,
			ConfigurationException, RegisterException, UnknownContextException,
			UnknownRunResultFormatException, NoRepositoryFoundException,
			InvalidOptimizationParameterException,
			UnknownProgramParameterException, UnknownProgramTypeException,
			UnknownRProgramException {
		Repository repo = new Repository(
				new File("testCaseRepository").getAbsolutePath(), null);
		repo.initialize();
		ProgramConfig pc = ProgramConfig
				.parseFromFile(new File(
						"testCaseRepository/programs/configs/compatibleDataSetFormatsTest.config")
						.getAbsoluteFile());
		System.out.println(pc.compatibleDataSetFormats);
		Assert.assertEquals(
				Pattern.compile(
						"RowSimDataSetFormat|RowSimDataSetFormat?SimMatrixDataSetFormat")
						.toString(), pc.compatibleDataSetFormats.toString());
	}
}
