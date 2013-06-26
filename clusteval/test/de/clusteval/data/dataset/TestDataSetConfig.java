/**
 * 
 */
package de.clusteval.data.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
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
		List<DataSet> dataSets = new ArrayList<DataSet>();
		dataSets.add(DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/astral_1_161/blastResults.txt")
						.getAbsoluteFile()));
		Assert.assertEquals(
				new DataSetConfig(
						repository,
						new File(
								"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
								.getAbsoluteFile().lastModified(),
						new File(
								"testCaseRepository/data/datasets/configs/astral_1.dsconfig")
								.getAbsoluteFile(), dataSets), gsConfig);
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
		List<DataSet> ds = dsConfig.getDataSets();
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
		List<DataSet> ds = dsConfig.getDataSets();
		DataSet expected = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/astral_1_161/blastResults.txt")
						.getAbsoluteFile());
		Assert.assertEquals(expected, ds);

		List<DataSet> override = new ArrayList<DataSet>();
		DataSet newDs = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		override.add(newDs);
		dsConfig.setDataSets(override);
		Assert.assertEquals(override, dsConfig.getDataSets());
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
