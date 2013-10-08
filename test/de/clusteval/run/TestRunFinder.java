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
package de.clusteval.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.StubSQLCommunicator;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.framework.threading.RepositorySupervisorThread;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.Run;

import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class TestRunFinder {

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

	/**
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unused")
	@Test
	public void testRunChangeNumberOfIterations()
			throws RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			IOException, InterruptedException, NoSuchAlgorithmException {
		String base = new File("testCaseRepository").getAbsolutePath();
		TestRepository repo = new TestRepository(base, null);
		repo.setSQLCommunicator(new StubSQLCommunicator(repo));
		// create a new run
		File f = new File(FileUtils.buildPath(base, "runs", "testCase.run"));
		PrintWriter bw = new PrintWriter(new FileWriter(f));
		bw.println("programConfig = TransClust_2");
		bw.println("dataConfig = DS1");
		bw.println("qualityMeasures = SilhouetteValueRClusteringQualityMeasure,TransClustF2ClusteringQualityMeasure");
		bw.println("mode = parameter_optimization");
		bw.println("optimizationMethod = LayeredDivisiveParameterOptimizationMethod");
		bw.println("optimizationCriterion = TransClustF2ClusteringQualityMeasure");
		bw.println("optimizationIterations = 100");
		bw.println("");
		bw.println("[TransClust_2]");
		bw.println("optimizationParameters = T");
		bw.flush();
		bw.close();

		new ClustevalBackendServer(repo, false);

		Run run = repo.getRunWithName("testCase");
		Assert.assertEquals(100, ((ParameterOptimizationRun) run)
				.getOptimizationMethods().get(0).getTotalIterationCount());

		f.delete();
		f.createNewFile();
		bw = new PrintWriter(new FileWriter(f));
		bw.println("programConfig = TransClust_2");
		bw.println("dataConfig = DS1");
		bw.println("qualityMeasures = SilhouetteValueRClusteringQualityMeasure,TransClustF2ClusteringQualityMeasure");
		bw.println("mode = parameter_optimization");
		bw.println("optimizationMethod = LayeredDivisiveParameterOptimizationMethod");
		bw.println("optimizationCriterion = TransClustF2ClusteringQualityMeasure");
		bw.println("optimizationIterations = 1000");
		bw.println("");
		bw.println("[TransClust_2]");
		bw.println("optimizationParameters = T");
		bw.flush();
		bw.close();

		while (repo.registeredTestCaseRun < 2)
			Thread.sleep(100);

		run = repo.getRunWithName("testCase");
		Assert.assertEquals(1000, ((ParameterOptimizationRun) run)
				.getOptimizationMethods().get(0).getTotalIterationCount());

		f.deleteOnExit();
	}
}

class TestRepository extends Repository {

	protected int registeredTestCaseRun = 0;

	/**
	 * @param basePath
	 * @param parent
	 * @throws FileNotFoundException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws NoRepositoryFoundException
	 * @throws NoSuchAlgorithmException
	 */
	public TestRepository(String basePath, Repository parent)
			throws FileNotFoundException, RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			NoSuchAlgorithmException {
		super(basePath, parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.Repository#register(run.ParameterOptimizationRun)
	 */
	@Override
	public boolean register(ParameterOptimizationRun object)
			throws RegisterException {
		boolean result = super.register(object);
		if (object.getAbsolutePath().contains("testCase.run") && result)
			registeredTestCaseRun++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.Repository#createSupervisorThread()
	 */
	@Override
	protected SupervisorThread createSupervisorThread() {
		// no scanning for runresults
		return new RepositorySupervisorThread(this,
				this.repositoryConfig.getThreadSleepTimes(), false, false);
	}

}
