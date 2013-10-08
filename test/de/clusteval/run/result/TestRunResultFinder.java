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
package de.clusteval.run.result;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.SQLCommunicator;
import de.clusteval.framework.repository.StubSQLCommunicator;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.framework.threading.RepositorySupervisorThread;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.run.RUN_STATUS;
import de.clusteval.run.Run;
import de.clusteval.run.result.RunResult;


/**
 * @author Christian Wiwie
 * 
 */
public class TestRunResultFinder {

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

//	@Test
	public void testRunInProgressNotFound() throws FileNotFoundException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			InterruptedException, NoSuchAlgorithmException {

		TestRepository repository = new TestRepository(new File(
				"testCaseRepository").getAbsolutePath(), null);
		ClustevalBackendServer framework = new ClustevalBackendServer(
				repository, false);
		framework.performRun("1", "tc_vs_DS1");
		Run run = repository.getRun("tc_vs_DS1");
		while (!run.getStatus().equals(RUN_STATUS.FINISHED)) {
			Thread.sleep(100);
		}
		Assert.assertFalse(repository.assertionFailed);
	}

//	@Test
	public void testRunNotInProgressNotFound() throws FileNotFoundException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			InterruptedException, NoSuchAlgorithmException {

		TestRepository repository = new TestRepository(new File(
				"testCaseRepository").getAbsolutePath(), null);
		repository.setSQLCommunicator(new StubSQLCommunicator(repository));
		ClustevalBackendServer framework = new ClustevalBackendServer(
				repository, false);
		while (!repository.isInitialized()) {
			Thread.sleep(100);
		}
		// We do not perform a new run, so we should not have the failed
		// assertion
		Assert.assertFalse(repository.assertionFailed);
	}

}

class TestRepository extends Repository {

	protected boolean assertionFailed;

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
	 * @see framework.repository.Repository#createSQLCommunicator()
	 */
	@Override
	protected SQLCommunicator createSQLCommunicator() {
		return new StubSQLCommunicator(this);
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

	public boolean register(RunResult object) {
		String runIdent = object.runIdentString;
		Run run = this.getRunWithName(object.run.getName());
		if (!assertionFailed)
			assertionFailed = !(run.getStatus().equals(RUN_STATUS.FINISHED) || run
					.getStatus().equals(RUN_STATUS.INACTIVE))
					&& (run.getRunIdentificationString() != null && run
							.getRunIdentificationString().equals(runIdent));
		return super.register(object);
	};

}
