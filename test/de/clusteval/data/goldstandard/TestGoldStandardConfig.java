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
package de.clusteval.data.goldstandard;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.utils.StubRepositoryObject;
import de.clusteval.utils.TestRepositoryObject;


/**
 * @author Christian Wiwie
 * 
 */
public class TestGoldStandardConfig extends TestRepositoryObject {

	/**
	 * Test method for {@link data.goldstandard.GoldStandardConfig#register()}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	public void testRegister() throws GoldStandardConfigurationException,
			IOException, NoRepositoryFoundException,
			GoldStandardNotFoundException, GoldStandardConfigNotFoundException,
			RegisterException {
		this.repositoryObject = GoldStandardConfig.parseFromFile(new File(
				"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
				.getAbsoluteFile());
		Assert.assertEquals(
				this.repositoryObject,
				this.repository
						.getRegisteredObject((GoldStandardConfig) this.repositoryObject));

		// adding a GoldStandardConfig equal to another one already registered
		// does
		// not register the second object.
		this.repositoryObject = new GoldStandardConfig(
				(GoldStandardConfig) this.repositoryObject);
		Assert.assertEquals(
				this.repository
						.getRegisteredObject((GoldStandardConfig) this.repositoryObject),
				this.repositoryObject);
		Assert.assertFalse(this.repository
				.getRegisteredObject((GoldStandardConfig) this.repositoryObject) == this.repositoryObject);
	}

	/**
	 * Test method for {@link data.goldstandard.GoldStandardConfig#unregister()}
	 * .
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	public void testUnregister() throws GoldStandardConfigurationException,
			IOException, NoRepositoryFoundException,
			GoldStandardNotFoundException, GoldStandardConfigNotFoundException,
			RegisterException {
		this.repositoryObject = GoldStandardConfig.parseFromFile(new File(
				"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
				.getAbsoluteFile());
		Assert.assertEquals(
				this.repositoryObject,
				this.repository
						.getRegisteredObject((GoldStandardConfig) this.repositoryObject));
		this.repositoryObject.unregister();
		// is not registered anymore
		Assert.assertTrue(this.repository
				.getRegisteredObject((GoldStandardConfig) this.repositoryObject) == null);
	}

	/**
	 * Test method for
	 * {@link data.goldstandard.GoldStandardConfig#notify(utils.RepositoryEvent)}
	 * .
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	@Override
	public void testNotifyRepositoryEvent() throws IOException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigurationException,
			GoldStandardConfigNotFoundException, RegisterException {

		/*
		 * REPLACE
		 */

		/*
		 * First check, whether listeners of goldstandardconfigs are notified
		 * correctly when the goldstandardconfig is replaced
		 */
		GoldStandardConfig gsConfig = GoldStandardConfig
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
						.getAbsoluteFile());
		StubRepositoryObject child = new StubRepositoryObject(repository,
				false, System.currentTimeMillis(), new File(
						"testCaseRepository/Bla"));
		gsConfig.addListener(child);

		GoldStandardConfig gsConfig2 = new GoldStandardConfig(gsConfig);

		gsConfig.notify(new RepositoryReplaceEvent(gsConfig, gsConfig2));
		Assert.assertTrue(child.notified);

		/*
		 * Now check, whether goldstandard configs update their references
		 * correctly, when their goldstandard is replaced
		 */
		GoldStandard gs = gsConfig.getGoldstandard();
		GoldStandard gs2 = new GoldStandard(gs);

		gsConfig.notify(new RepositoryReplaceEvent(gs, gs2));

		Assert.assertFalse(gsConfig.getGoldstandard() == gs);
		Assert.assertTrue(gsConfig.getGoldstandard() == gs2);

		/*
		 * REMOVE
		 */

		/*
		 * First check, whether listeners of goldstandardconfigs are notified
		 * correctly when the goldstandardconfig is replaced
		 */
		child.notified = false;
		gsConfig.notify(new RepositoryRemoveEvent(gsConfig));
		Assert.assertTrue(child.notified);

		/*
		 * Now check, whether goldstandard configs remove themselves when their
		 * goldstandard is removed
		 */
		// gsconfig has to be registered
		Assert.assertTrue(repository.getRegisteredObject(gsConfig) == gsConfig);

		gsConfig.notify(new RepositoryRemoveEvent(gs2));

		// not registered anymore
		Assert.assertTrue(repository.getRegisteredObject(gsConfig) == null);
	}

	/**
	 * Test method for
	 * {@link data.goldstandard.GoldStandardConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	@Test(expected = GoldStandardConfigurationException.class)
	public void testParseFromFileGoldStandardNameMissing()
			throws GoldStandardConfigurationException, IOException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigNotFoundException, RegisterException {
		try {
			GoldStandardConfig
					.parseFromFile(new File(
							"testCaseRepository/data/goldstandards/configs/goldStandardConfigTest.gsconfig")
							.getAbsoluteFile());
		} catch (GoldStandardConfigurationException e) {
			// Assert.assertEquals(
			// "'goldstandardName' doesn't map to an existing object",
			// e.getMessage());
			throw e;
		}
	}

	/**
	 * Test method for
	 * {@link data.goldstandard.GoldStandardConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testParseFromFile() throws GoldStandardConfigurationException,
			IOException, NoRepositoryFoundException,
			GoldStandardNotFoundException, GoldStandardConfigNotFoundException,
			RegisterException {
		GoldStandardConfig gsConfig = GoldStandardConfig
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
						.getAbsoluteFile());
		Assert.assertEquals(
				new GoldStandardConfig(
						repository,
						new File(
								"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
								.getAbsoluteFile().lastModified(),
						new File(
								"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
								.getAbsoluteFile(),
						GoldStandard
								.parseFromFile(new File(
										"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
										.getAbsoluteFile())), gsConfig);
	}

	/**
	 * Test method for
	 * {@link data.goldstandard.GoldStandardConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	@Test(expected = GoldStandardConfigurationException.class)
	public void testParseFromFileGoldStandardFileMissing()
			throws GoldStandardConfigurationException, IOException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigNotFoundException, RegisterException {
		try {
			GoldStandardConfig
					.parseFromFile(new File(
							"testCaseRepository/data/goldstandards/configs/goldStandardConfigTest2.gsconfig")
							.getAbsoluteFile());
		} catch (GoldStandardConfigurationException e) {
			// Assert.assertEquals(
			// "'goldstandardFile' doesn't map to an existing object",
			// e.getMessage());
			throw e;
		}
	}

	/**
	 * @throws IOException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws GoldStandardConfigurationException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws RegisterException
	 */
	@Test(expected = GoldStandardConfigNotFoundException.class)
	public void testParseFromNotExistingFile() throws IOException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigurationException,
			GoldStandardConfigNotFoundException, RegisterException {
		GoldStandardConfig
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/configs/DS1_12.gsconfig")
						.getAbsoluteFile());
	}

	/**
	 * Test method for
	 * {@link data.goldstandard.GoldStandardConfig#getGoldstandard()}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testGetGoldstandard()
			throws GoldStandardConfigurationException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			GoldStandardConfigNotFoundException, RegisterException {
		GoldStandardConfig gsConfig = GoldStandardConfig
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
						.getAbsoluteFile());
		GoldStandard gs = gsConfig.getGoldstandard();
		GoldStandard expected = GoldStandard
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/DS1/Zachary_karate_club_gold_standard.txt")
						.getAbsoluteFile());
		Assert.assertEquals(expected, gs);
	}

	/**
	 * Test method for {@link data.goldstandard.GoldStandardConfig#toString()}.
	 * 
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws GoldStandardConfigurationException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws RegisterException
	 */
	@Test
	public void testToString() throws GoldStandardConfigurationException,
			IOException, NoRepositoryFoundException,
			GoldStandardNotFoundException, GoldStandardConfigNotFoundException,
			RegisterException {
		GoldStandardConfig gsConfig = GoldStandardConfig
				.parseFromFile(new File(
						"testCaseRepository/data/goldstandards/configs/DS1_1.gsconfig")
						.getAbsoluteFile());
		Assert.assertEquals("DS1_1", gsConfig.toString());

	}

}
