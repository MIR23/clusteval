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
package de.clusteval.utils;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.qos.logback.classic.Level;

import de.clusteval.context.Context;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.framework.repository.StubSQLCommunicator;

/**
 * @author Christian Wiwie
 * 
 */
public class TestRepositoryObject {

	protected Repository repository;
	protected RepositoryObject repositoryObject;
	protected Context context;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ClustevalBackendServer.logLevel(Level.WARN);
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
		context = Context.parseFromString(repository, "ClusteringContext");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.repositoryObject = null;
		repository.finalize();
		Repository.unregister(repository);
	}

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#getAbsolutePath()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAbsolutePath() throws Exception {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);

		Assert.assertEquals(f.getAbsolutePath(),
				this.repositoryObject.getAbsolutePath());
	};

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#setAbsolutePath(java.io.File)}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSetAbsolutePath() throws Exception {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);
		File f2 = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard2.txt")
				.getAbsoluteFile();
		this.repositoryObject.setAbsolutePath(f2);

		Assert.assertEquals(f2.getAbsolutePath(),
				this.repositoryObject.getAbsolutePath());
	};

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#getChangeDate()}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetChangeDate() throws Exception {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);
		Assert.assertEquals(f.lastModified(),
				this.repositoryObject.getChangeDate());
	};

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#equals(java.lang.Object)}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEqualsObject() throws Exception {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);

		/*
		 * Identity
		 */
		Assert.assertEquals(
				new StubRepositoryObject(this.repository, false, f
						.lastModified(), f), this.repositoryObject);
		/*
		 * Mod-date is ignored
		 */
		Assert.assertEquals(
				new StubRepositoryObject(this.repository, false, f
						.lastModified() - 1, f), this.repositoryObject);

		Repository repository2 = new Repository(
				new File("repository2").getAbsolutePath(), null);

		/*
		 * Different repositories
		 */
		Assert.assertFalse(new StubRepositoryObject(repository2, false, f
				.lastModified(), f).equals(this.repositoryObject));

		FileUtils.deleteDirectory(new File("repository2").getAbsoluteFile());

		File f2 = new File(
				"testCaseRepository/data/goldstandards/sfld/sfld_brown_et_al_amidohydrolases_families_gold_standard.txt");
		Assert.assertFalse(this.repositoryObject
				.equals(new StubRepositoryObject(this.repository, false, f2
						.lastModified(), f2)));

		/*
		 * Different classes
		 */
		Assert.assertFalse(this.repositoryObject
				.equals(new StubRepositoryObject(this.repository, false, f2
						.lastModified(), f2)));
	};

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#copyTo(java.io.File)}.
	 * 
	 * @throws IOException
	 * @throws RegisterException
	 * 
	 */
	@Test
	public void testCopyToFile() throws IOException, RegisterException {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);
		File destF = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard_copy.txt")
				.getAbsoluteFile();
		if (destF.exists())
			file.FileUtils.delete(destF);
		this.repositoryObject.copyTo(destF);
		Assert.assertTrue(destF.exists());
		FileUtils.contentEquals(f, destF);
		file.FileUtils.delete(destF);
	}

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#copyTo(java.io.File, boolean)}
	 * .
	 * 
	 * @throws IOException
	 * @throws RegisterException
	 * 
	 */
	@Test
	public void testCopyToFileBoolean() throws IOException, RegisterException {
		File f = new File(
				"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
				.getAbsoluteFile();
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, f.lastModified(), f);
		File destFolder = new File(
				"testCaseRepository/data/goldstandards/DS1/copy");
		if (destFolder.exists())
			FileUtils.deleteDirectory(destFolder);
		destFolder.mkdirs();
		this.repositoryObject.copyToFolder(destFolder);
		File destF = new File(
				"testCaseRepository/data/goldstandards/DS1/copy/Zachary_karate_club_gold_standard.txt");
		Assert.assertTrue(destF.exists());
		FileUtils.contentEquals(f, destF);
		file.FileUtils.delete(destF);
	}

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#addListener(utils.RepositoryListener)}
	 * .
	 * 
	 * @throws RegisterException
	 * 
	 */
	@Test
	public void testAddListener() throws RegisterException {

		/*
		 * Dont add the object itself as a listener
		 */
		Assert.assertFalse(this.repositoryObject
				.addListener(this.repositoryObject));

		RepositoryObject other = new StubRepositoryObject(this.repository,
				false, System.currentTimeMillis(), new File("test2"));

		Assert.assertTrue(this.repositoryObject.addListener(other));

		/*
		 * Already in there
		 */
		Assert.assertFalse(this.repositoryObject.addListener(other));

		Assert.assertTrue(this.repositoryObject.removeListener(other));
		/*
		 * Not in there anymore
		 */
		Assert.assertFalse(this.repositoryObject.removeListener(other));
	}

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#removeListener(utils.RepositoryListener)}
	 * .
	 * 
	 * @throws RegisterException
	 * 
	 */
	@Test
	public void testRemoveListener() throws RegisterException {

		RepositoryObject other = new StubRepositoryObject(this.repository,
				false, System.currentTimeMillis(), new File("test2"));

		/*
		 * Nothing in there
		 */

		Assert.assertFalse(this.repositoryObject.removeListener(other));

		/*
		 * Add it
		 */
		Assert.assertTrue(this.repositoryObject.addListener(other));

		/*
		 * Remove it
		 */
		Assert.assertTrue(this.repositoryObject.removeListener(other));
		/*
		 * Not in there anymore
		 */
		Assert.assertFalse(this.repositoryObject.removeListener(other));

	}

	/**
	 * Test method for
	 * {@link framework.repository.RepositoryObject#notify(utils.RepositoryEvent)}
	 * .
	 * 
	 * @throws IOException
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@SuppressWarnings("unused")
	@Test
	public void testNotifyRepositoryEvent() throws IOException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigurationException, DataSetNotFoundException,
			DataSetConfigurationException, UnknownDataSetFormatException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {

		/*
		 * Create two stub repository objects
		 */
		this.repositoryObject = new StubRepositoryObject(this.repository,
				false, System.currentTimeMillis(), new File("test"));

		StubRepositoryObject other = new StubRepositoryObject(this.repository,
				false, System.currentTimeMillis(), new File("test2"));

		/*
		 * Add the "other" object as listener to the first one
		 */
		this.repositoryObject.addListener(other);

		Assert.assertFalse(other.notified);

		this.repositoryObject.notify(new RepositoryReplaceEvent(
				this.repositoryObject, other));

		Assert.assertTrue(other.notified);
		other.notified = false;

		this.repositoryObject.notify(new RepositoryReplaceEvent(other,
				this.repositoryObject));

		Assert.assertFalse(other.notified);
		other.notified = false;
	}
}
