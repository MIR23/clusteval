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
package de.clusteval.data.preprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetAttributeFilterer;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;

/**
 * @author Christian Wiwie
 * 
 */
public class TestRangeNormalizationDataPreprocesser {

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
	public void test() throws RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigurationException,
			NoDataSetException, NumberFormatException, RegisterException,
			NoRepositoryFoundException, UnknownDataSetTypeException,
			UnknownDataPreprocessorException, IOException {
		ClustevalBackendServer.logLevel(Level.INFO);
		Repository repo = new Repository(
				new File("testCaseRepository").getAbsolutePath(), null);
		repo.initialize();

		File f = new File(
				"testCaseRepository/data/datasets/synthetic/cassini250");

		DataSet ds = DataSet.parseFromFile(f);

		DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
				f.getAbsolutePath());
		filterer.process();

		ds.setAbsolutePath(new File(
				"testCaseRepository/data/datasets/synthetic/cassini250.strip"));

		DataPreprocessor proc = DataPreprocessor.parseFromString(repo,
				"RangeNormalizationDataPreprocessor");

		DataSet newDs = proc.preprocess(ds);
		Assert.assertTrue(new File(newDs.getAbsolutePath()).exists());
	}
}
