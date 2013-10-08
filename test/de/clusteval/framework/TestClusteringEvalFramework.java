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
package de.clusteval.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.ConnectException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.serverclient.BackendClient;

/**
 * @author Christian Wiwie
 * 
 */
public class TestClusteringEvalFramework {

	protected static ClustevalBackendServer framework;

	protected static BackendClient client;

	protected static int port = 2321;

	protected static int clientId;

	/**
	 * @throws FileNotFoundException
	 * @throws ConnectException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryAlreadyExistsException
	 * @throws NoRepositoryFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 */
	@BeforeClass
	public static void setUp() throws FileNotFoundException, ConnectException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			ParseException, NoSuchAlgorithmException {
		ClustevalBackendServer.logLevel(Level.INFO);
		framework = new ClustevalBackendServer(new Repository(new File(
				"testCaseRepository").getAbsolutePath(), null), false);
		ClustevalBackendServer.port = port;
		ClustevalBackendServer.registerServer(framework);
		client = new BackendClient(new String[]{"-port", "" + port});
		clientId = Integer.valueOf(client.getClientId());
	}

	@AfterClass
	public static void tearDown() {
		framework.shutdown(clientId + "", 5000);
		Assert.assertFalse(framework.isRunning());
	}

//	@Test
	public void testPerformNotExistingRun() throws ConnectException,
			ParseException {
		client = new BackendClient(new String[]{"-port", port + "",
				"-clientId", clientId + "", "-performRun", "notExistingRun"});

	}

//	@Test
	public void testPerformParamOptimizationRun() throws ConnectException,
			InterruptedException, ParseException {
		String uniqueRunId = "11_20_2012-12_45_04_all_vs_DS1";
		String resultsDirectory = new File("testCaseRepository/results/"
				+ uniqueRunId + "/clusters").getAbsolutePath();
		/*
		 * Remove old output folder
		 */
		try {
			FileUtils.deleteDirectory(new File(resultsDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}

		client = new BackendClient(new String[]{"-port", port + "",
				"-clientId", clientId + "", "-resumeRun", uniqueRunId,
				"-waitForRuns"});
		client.join();

		String[] programDataPairs = new String[]{"APcluster_1_DS1",
				"MCL_1_DS1", "TransClust_2_DS1"};

		/*
		 * Verify the output
		 */
		for (String programDataPair : programDataPairs) {
			for (int i = 1; i <= 10; i++) {
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.conv")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.qual")).exists());
			}

			Assert.assertTrue(new File(file.FileUtils.buildPath(
					resultsDirectory, programDataPair
							+ ".results.qual.complete")).exists());
		}

	}

//	@Test
	public void testPerformLayeredParamOptimizationRun()
			throws ConnectException, InterruptedException, ParseException {
		String uniqueRunId = "11_20_2012-12_46_12_tc_vs_DS1_layered";
		String resultsDirectory = new File("testCaseRepository/results/"
				+ uniqueRunId + "/clusters").getAbsolutePath();
		/*
		 * Remove old output folder
		 */
		try {
			FileUtils.deleteDirectory(new File(resultsDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}

		client = new BackendClient(new String[]{"-port", port + "",
				"-clientId", clientId + "", "-resumeRun", uniqueRunId,
				"-waitForRuns"});
		client.join();

		String[] programDataPairs = new String[]{"TransClust_2_DS1"};

		/*
		 * Verify the output
		 */
		for (String programDataPair : programDataPairs) {
			for (int i = 1; i <= 10; i++) {
				Assert.assertTrue(
						file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results"),
						new File(file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results"))
								.exists());
				Assert.assertTrue(
						file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results.conv"),
						new File(file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results.conv"))
								.exists());
				Assert.assertTrue(
						file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results.qual"),
						new File(file.FileUtils.buildPath(resultsDirectory,
								programDataPair + "." + i + ".results.qual"))
								.exists());
			}

			Assert.assertTrue(new File(file.FileUtils.buildPath(
					resultsDirectory, programDataPair
							+ ".results.qual.complete")).exists());
		}

	}

//	@Test
	public void testPerformInternalParamOptimizationRun()
			throws ConnectException, InterruptedException, ParseException {
		String uniqueRunId = "11_20_2012-12_46_37_tc_vs_DS1_internal";
		String resultsDirectory = new File("testCaseRepository/results/"
				+ uniqueRunId + "/clusters").getAbsolutePath();
		/*
		 * Remove old output folder
		 */
		try {
			FileUtils.deleteDirectory(new File(resultsDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}

		client = new BackendClient(new String[]{"-port", port + "",
				"-clientId", clientId + "", "-resumeRun", uniqueRunId,
				"-waitForRuns"});
		client.join();

		String[] programDataPairs = new String[]{"TransClust_2_DS1"};

		/*
		 * Verify the output
		 */
		for (String programDataPair : programDataPairs) {
			for (int i = 1; i <= 1; i++) {
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.conv")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.qual")).exists());
			}

			Assert.assertTrue(new File(file.FileUtils.buildPath(
					resultsDirectory, programDataPair
							+ ".results.qual.complete")).exists());
		}

	}

//	@Test
	public void testPerformClusteringRun() throws ConnectException,
			InterruptedException, ParseException {
		String uniqueRunId = "11_20_2012-12_46_58_all_vs_DS1_clustering";
		String resultsDirectory = new File("testCaseRepository/results/"
				+ uniqueRunId + "/clusters").getAbsolutePath();
		/*
		 * Remove old output folder
		 */
		try {
			FileUtils.deleteDirectory(new File(resultsDirectory));
		} catch (IOException e) {
			e.printStackTrace();
		}

		client = new BackendClient(new String[]{"-port", port + "",
				"-clientId", clientId + "", "-resumeRun", uniqueRunId,
				"-waitForRuns"});
		client.join();

		String[] programDataPairs = new String[]{"APcluster_1_DS1",
				"MCL_1_DS1", "TransClust_2_DS1"};

		/*
		 * Verify the output
		 */
		for (String programDataPair : programDataPairs) {
			for (int i = 1; i <= 1; i++) {
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.conv")).exists());
				Assert.assertTrue(new File(file.FileUtils.buildPath(
						resultsDirectory, programDataPair + "." + i
								+ ".results.qual")).exists());
			}

			Assert.assertTrue(new File(file.FileUtils.buildPath(
					resultsDirectory, programDataPair
							+ ".results.qual.complete")).exists());
		}

	}

	// @Test
	// public void testShutdown() throws FileNotFoundException {
	// }
}
