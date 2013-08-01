/**
 * 
 */
package de.clusteval.data.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;

import org.junit.Test;

import de.clusteval.context.UnknownContextException;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.StubSQLCommunicator;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.utils.FormatConversionException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.TestRepositoryObject;

/**
 * @author Christian Wiwie
 * 
 */
public class TestDataSet extends TestRepositoryObject {

	/**
	 * Test method for {@link data.dataset.DataSet#register()}.
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 */
	public void testRegister() throws UnknownDataSetFormatException,
			NoRepositoryFoundException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());

		Assert.assertEquals(this.repositoryObject, this.repository
				.getRegisteredObject((DataSet) this.repositoryObject));

		// adding a data set equal to another one already registered does
		// not register the second object.
		this.repositoryObject = new DataSet((DataSet) this.repositoryObject);
		Assert.assertEquals(this.repository
				.getRegisteredObject((DataSet) this.repositoryObject),
				this.repositoryObject);
		Assert.assertFalse(this.repository
				.getRegisteredObject((DataSet) this.repositoryObject) == this.repositoryObject);
	}

	/**
	 * Registering a dataset of a runresult repository that is not present in
	 * the parent repository should not be possible.
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws InvalidRepositoryException
	 * @throws RepositoryAlreadyExistsException
	 * @throws FileNotFoundException
	 * @throws RegisterException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws UnknownDataSetTypeException
	 * @throws NoSuchAlgorithmException
	 */
	@Test(expected = DataSetNotFoundException.class)
	public void testRegisterRunResultRepositoryNotPresentInParent()
			throws FileNotFoundException, RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, NoRepositoryFoundException,
			DataSetNotFoundException, DataSetConfigurationException,
			UnknownDataSetFormatException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			NoSuchAlgorithmException {
		repository.initialize();
		Repository runResultRepository = new RunResultRepository(
				new File(
						"testCaseRepository/results/12_04_2012-14_05_42_tc_vs_DS1")
						.getAbsolutePath(), repository);
		runResultRepository.setSQLCommunicator(new StubSQLCommunicator(
				runResultRepository));
		runResultRepository.initialize();
		DataSet.parseFromFile(new File(
				"testCaseRepository/results/12_04_2012-14_05_42_tc_vs_DS1/inputs/DS1/testCaseDataSetNotPresentInParent.txt")
				.getAbsoluteFile());
	}

	/**
	 * Test method for {@link data.dataset.DataSet#unregister()}.
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	public void testUnregister() throws UnknownDataSetFormatException,
			NoRepositoryFoundException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {

		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());

		Assert.assertEquals(this.repositoryObject, this.repository
				.getRegisteredObject((DataSet) this.repositoryObject));
		this.repositoryObject.unregister();
		// is not registered anymore
		Assert.assertTrue(this.repository
				.getRegisteredObject((DataSet) this.repositoryObject) == null);
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSet#parseFromFile(java.io.File, data.dataset.format.DataSetFormat)}
	 * .
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 */
	@Test
	public void testParseFromFile() throws UnknownDataSetFormatException,
			NoRepositoryFoundException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		Assert.assertEquals(
				new DataSet(
						repository,
						false,
						new File(
								"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
								.getAbsoluteFile().lastModified(),
						new File(
								"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
								.getAbsoluteFile(), "zachary", DataSetFormat
								.parseFromString(repository,
										"RowSimDataSetFormat"), DataSetType
								.parseFromString(repository, "PPIDataSetType")),
				this.repositoryObject);
	}

	/**
	 * @throws NoRepositoryFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws FileNotFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test(expected = DataSetNotFoundException.class)
	public void testParseFromNotExistingFile()
			throws UnknownDataSetFormatException, NoRepositoryFoundException,
			DataSetNotFoundException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities2.txt")
						.getAbsoluteFile());
	}

	/**
	 * Test method for {@link data.dataset.DataSet#getDataSetFormat()}.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testGetDataSetFormat() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		DataSetFormat dsFormat = ((DataSet) this.repositoryObject)
				.getDataSetFormat();
		Assert.assertEquals(DataSetFormat.parseFromString(repository,
				"RowSimDataSetFormat"), dsFormat);
	}

	/**
	 * Test method for {@link data.dataset.DataSet#getMajorName()}.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testGetMajorName() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		DataSet casted = (DataSet) this.repositoryObject;
		Assert.assertEquals("DS1", casted.getMajorName());
	}

	/**
	 * Test method for {@link data.dataset.DataSet#getMinorName()}.
	 * 
	 * @throws NoRepositoryFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testGetMinorName() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		DataSet casted = (DataSet) this.repositoryObject;

		Assert.assertEquals(casted.getMinorName(), casted.getAbsolutePath()
				.substring(casted.getAbsolutePath().lastIndexOf("/") + 1));
	}

	/**
	 * Test method for {@link data.dataset.DataSet#getFullName()}.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws FileNotFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testGetFullName() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		Assert.assertEquals("DS1/Zachary_karate_club_similarities.txt",
				((DataSet) this.repositoryObject).getFullName());
	}

	/**
	 * Test method for {@link data.dataset.DataSet#toString()}.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 */
	@Test
	public void testToString() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, DataSetNotFoundException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		Assert.assertEquals(
				"[DataSet:DS1/Zachary_karate_club_similarities.txt]",
				((DataSet) this.repositoryObject).toString());
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSet#convertTo(data.dataset.format.DataSetFormat)}
	 * . Only verify, that the conversion process is started correctly and the
	 * result file is created in the end. verification of the conversion result
	 * itself is not done here.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws FormatConversionException
	 * @throws DataSetNotFoundException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws RNotAvailableException
	 * @throws UnknownContextException
	 */
	@Test
	public void testConvertTo() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, FormatConversionException,
			IOException, DataSetNotFoundException,
			InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			InstantiationException, IllegalAccessException,
			UnknownDistanceMeasureException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, RNotAvailableException {
		/*
		 * SimMatrixDataSetFormat.convertTo() is a special case
		 */
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/nora_cancer/all_expression_spearman.txt")
						.getAbsoluteFile());
		DataSet newDataSet = ((DataSet) this.repositoryObject)
				.preprocessAndConvertTo(context, DataSetFormat.parseFromString(
						repository, "SimMatrixDataSetFormat"));
		Assert.assertEquals(this.repositoryObject.getAbsolutePath(),
				newDataSet.getAbsolutePath());
		/*
		 * SimMatrixDataSetFormat.convertTo(APRowSimDataSetFormat)
		 */
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/nora_cancer/all_expression_spearman.txt")
						.getAbsoluteFile());
		newDataSet = ((DataSet) this.repositoryObject).preprocessAndConvertTo(
				context, DataSetFormat.parseFromString(repository,
						"APRowSimDataSetFormat"));

		/*
		 * convertTo(SimMatrixDataSetFormat) is a special case
		 */
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).preprocessAndConvertTo(context,
				DataSetFormat.parseFromString(repository,
						"SimMatrixDataSetFormat"));
		Assert.assertTrue(new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.SimMatrix")
				.getAbsoluteFile().exists());

		/*
		 * Convert to a non standard format
		 */
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).preprocessAndConvertTo(context,
				DataSetFormat.parseFromString(repository,
						"APRowSimDataSetFormat"));
		Assert.assertTrue(new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.APRowSim")
				.getAbsoluteFile().exists());
		Assert.assertTrue(new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.APRowSim.map")
				.getAbsoluteFile().exists());

		new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.SimMatrix")
				.getAbsoluteFile().deleteOnExit();
		new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.APRowSim")
				.getAbsoluteFile().deleteOnExit();
		new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.APRowSim.map")
				.getAbsoluteFile().deleteOnExit();
	}

	@Test(expected = FormatConversionException.class)
	public void testConvertToRelativeToAbsolute()
			throws NoRepositoryFoundException, UnknownDataSetFormatException,
			FormatConversionException, IOException, DataSetNotFoundException,
			InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			InstantiationException, IllegalAccessException,
			UnknownDistanceMeasureException, IllegalArgumentException,
			SecurityException, InvocationTargetException,
			NoSuchMethodException, RNotAvailableException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/sfld/sfld_brown_et_al_amidohydrolases_protein_similarities_for_beh.txt")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).preprocessAndConvertTo(context,
				DataSetFormat
						.parseFromString(repository, "MatrixDataSetFormat"));
	}

	/**
	 * Test method for {@link data.dataset.DataSet#getInStandardFormat()}.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws DataSetNotFoundException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws DataSetConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws RNotAvailableException
	 * @throws InvalidParameterException
	 */
	@Test
	public void testGetInStandardFormat() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, IOException,
			DataSetNotFoundException, InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownDistanceMeasureException, InvalidParameterException,
			RNotAvailableException {
		// TODO
	}
}
