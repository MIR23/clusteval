/**
 * 
 */
package de.clusteval.data.dataset;

import java.io.File;
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.preprocessing.DataPreprocessor;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
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
public class TestDataSetConfig extends TestRepositoryObject {

	/**
	 * Test method for {@link data.dataset.DataSetConfig#register()}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	public void testRegister() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		this.repositoryObject = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		Assert.assertEquals(this.repositoryObject, this.repository
				.getRegisteredObject((DataSetConfig) this.repositoryObject));

		// adding a DataSetConfig equal to another one already registered
		// does
		// not register the second object.
		this.repositoryObject = new DataSetConfig(
				(DataSetConfig) this.repositoryObject);
		Assert.assertEquals(this.repository
				.getRegisteredObject((DataSetConfig) this.repositoryObject),
				this.repositoryObject);
		Assert.assertFalse(this.repository
				.getRegisteredObject((DataSetConfig) this.repositoryObject) == this.repositoryObject);
	}

	/**
	 * Test method for {@link data.dataset.DataSetConfig#unregister()} .
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	public void testUnregister() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		this.repositoryObject = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		Assert.assertEquals(this.repositoryObject, this.repository
				.getRegisteredObject((DataSetConfig) this.repositoryObject));
		this.repositoryObject.unregister();
		// is not registered anymore
		Assert.assertTrue(this.repository
				.getRegisteredObject((DataSetConfig) this.repositoryObject) == null);
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSetConfig#notify(utils.RepositoryEvent)} .
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Override
	public void testNotifyRepositoryEvent() throws NoRepositoryFoundException,
			DataSetNotFoundException, DataSetConfigurationException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {

		/*
		 * REPLACE
		 */

		/*
		 * First check, whether listeners of DataSetconfigs are notified
		 * correctly when the DataSetconfig is replaced
		 */
		DataSetConfig gsConfig = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		StubRepositoryObject child = new StubRepositoryObject(repository,
				false, System.currentTimeMillis(), new File(
						"testCaseRepository/Bla"));
		gsConfig.addListener(child);

		DataSetConfig gsConfig2 = new DataSetConfig(gsConfig);

		gsConfig.notify(new RepositoryReplaceEvent(gsConfig, gsConfig2));
		Assert.assertTrue(child.notified);

		/*
		 * Now check, whether DataSet configs update their references correctly,
		 * when their DataSet is replaced
		 */
		RelativeDataSet gs = (RelativeDataSet) (gsConfig.getDataSet());
		RelativeDataSet gs2 = new RelativeDataSet(gs);

		gsConfig.notify(new RepositoryReplaceEvent(gs, gs2));

		Assert.assertFalse(gsConfig.getDataSet() == gs);
		Assert.assertTrue(gsConfig.getDataSet() == gs2);

		/*
		 * REMOVE
		 */

		/*
		 * First check, whether listeners of DataSetconfigs are notified
		 * correctly when the DataSetconfig is replaced
		 */
		child.notified = false;
		gsConfig.notify(new RepositoryRemoveEvent(gsConfig));
		Assert.assertTrue(child.notified);

		/*
		 * Now check, whether DataSet configs remove themselves when their
		 * DataSet is removed
		 */
		// gsconfig has to be registered
		Assert.assertTrue(repository.getRegisteredObject(gsConfig) == gsConfig);

		gsConfig.notify(new RepositoryRemoveEvent(gs2));

		// not registered anymore
		Assert.assertTrue(repository.getRegisteredObject(gsConfig) == null);
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSetConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test(expected = DataSetConfigurationException.class)
	public void testParseFromFileDataSetNameMissing()
			throws DataSetConfigurationException, NoRepositoryFoundException,
			DataSetNotFoundException, UnknownDataSetFormatException,
			DataSetConfigNotFoundException, UnknownDistanceMeasureException,
			RegisterException, UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		try {
			DataSetConfig
					.parseFromFile(new File(
							"testCaseRepository/data/datasets/configs/testDataSetConfig.dsconfig")
							.getAbsoluteFile());
		} catch (DataSetConfigurationException e) {
			// Assert.assertEquals(
			// "'datasetName' doesn't map to an existing object",
			// e.getMessage());
			throw e;
		}
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSetConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test
	public void testParseFromFile() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		DataSetConfig gsConfig = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		Assert.assertEquals(
				new DataSetConfig(
						repository,
						new File(
								"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
								.getAbsoluteFile().lastModified(),
						new File(
								"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
								.getAbsoluteFile(),
						DataSet.parseFromFile(new File(
								"testCaseRepository/data/datasets/astral_1_161/blastResults.txt")
								.getAbsoluteFile()),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration()), gsConfig);
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSetConfig#parseFromFile(java.io.File)}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test(expected = DataSetConfigurationException.class)
	public void testParseFromFileDataSetFileMissing()
			throws DataSetConfigurationException, NoRepositoryFoundException,
			DataSetNotFoundException, UnknownDataSetFormatException,
			DataSetConfigNotFoundException, UnknownDistanceMeasureException,
			RegisterException, UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		try {
			DataSetConfig
					.parseFromFile(new File(
							"testCaseRepository/data/datasets/configs/testDataSetConfig2.dsconfig")
							.getAbsoluteFile());
		} catch (DataSetConfigurationException e) {
			// Assert.assertEquals(
			// "'datasetFile' doesn't map to an existing object",
			// e.getMessage());
			throw e;
		}
	}

	/**
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test(expected = DataSetConfigNotFoundException.class)
	public void testParseFromNotExistingFile()
			throws NoRepositoryFoundException, DataSetNotFoundException,
			DataSetConfigurationException, UnknownDataSetFormatException,
			DataSetConfigNotFoundException, UnknownDistanceMeasureException,
			RegisterException, UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/DS1_12.gsconfig")
				.getAbsoluteFile());
	}

	/**
	 * Test method for {@link data.dataset.DataSetConfig#getDataSet()}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test
	public void testGetDataSet() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		DataSetConfig dsConfig = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		DataSet ds = dsConfig.getDataSet();
		DataSet expected = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/astral_1_161/blastResults.txt")
						.getAbsoluteFile());
		Assert.assertEquals(expected, ds);
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSetConfig#setDataSet(data.dataset.DataSet)} .
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test
	public void testSetDataSet() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		DataSetConfig dsConfig = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		DataSet ds = dsConfig.getDataSet();
		DataSet expected = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/astral_1_161/blastResults.txt")
						.getAbsoluteFile());
		Assert.assertEquals(expected, ds);

		DataSet override = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		dsConfig.setDataSet(override);
		Assert.assertEquals(override, dsConfig.getDataSet());
	}

	/**
	 * Test method for {@link data.dataset.DataSetConfig#toString()}.
	 * 
	 * @throws DataSetNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigNotFoundException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataPreprocessorException
	 * @throws NumberFormatException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 */
	@Test
	public void testToString() throws DataSetConfigurationException,
			NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigNotFoundException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NumberFormatException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException {
		DataSetConfig gsConfig = DataSetConfig.parseFromFile(new File(
				"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
				.getAbsoluteFile());
		Assert.assertEquals("astral_1", gsConfig.toString());

	}

}
