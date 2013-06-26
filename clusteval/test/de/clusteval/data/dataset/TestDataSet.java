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
import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import ch.qos.logback.classic.Level;

import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.ConversionInputToStandardConfiguration;
import de.clusteval.data.dataset.format.ConversionStandardToInputConfiguration;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.RelativeDataSetFormat;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.preprocessing.DataPreprocessor;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.StubSQLCommunicator;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.run.ParameterOptimizationRun;
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
		this.repositoryObject = new RelativeDataSet(
				(RelativeDataSet) this.repositoryObject);
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
				new RelativeDataSet(
						repository,
						false,
						new File(
								"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
								.getAbsoluteFile().lastModified(),
						new File(
								"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
								.getAbsoluteFile(), "zachary",
						(RelativeDataSetFormat) (DataSetFormat.parseFromString(
								repository, "RowSimDataSetFormat")),
						DataSetType.parseFromString(repository,
								"PPIDataSetType")), this.repositoryObject);
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
	 * Test method for {@link data.dataset.DataSet#loadIntoMemory()}.
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
	 * @throws UnknownDistanceMeasureException
	 * @throws RNotAvailableException
	 * @throws UnknownContextException
	 */
	@Test
	public void testLoadIntoMemory() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, FormatConversionException,
			IOException, DataSetNotFoundException,
			InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			InstantiationException, IllegalAccessException,
			UnknownDistanceMeasureException, RNotAvailableException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());

		DataSet standard = ((DataSet) this.repositoryObject)
				.preprocessAndConvertTo(
						context,
						DataSetFormat.parseFromString(repository,
								"SimMatrixDataSetFormat"),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration());
		Assert.assertFalse(standard.isInMemory());
		standard.loadIntoMemory();
		Assert.assertTrue(standard.isInMemory());
	}

	/**
	 * Test method for {@link data.dataset.DataSet#unloadFromMemory()}.
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
	 * @throws RNotAvailableException
	 * @throws UnknownContextException
	 */
	@Test
	public void testUnloadFromMemory() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, FormatConversionException,
			IOException, DataSetNotFoundException,
			InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			InstantiationException, IllegalAccessException,
			UnknownDistanceMeasureException, RNotAvailableException {
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/DS1/Zachary_karate_club_similarities.txt")
						.getAbsoluteFile());
		DataSet standard = ((DataSet) this.repositoryObject)
				.preprocessAndConvertTo(
						context,
						DataSetFormat.parseFromString(repository,
								"SimMatrixDataSetFormat"),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration());
		standard.loadIntoMemory();
		Assert.assertTrue(standard.isInMemory());
		standard.unloadFromMemory();
		Assert.assertFalse(standard.isInMemory());
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
				.preprocessAndConvertTo(
						context,
						DataSetFormat.parseFromString(repository,
								"SimMatrixDataSetFormat"),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration());
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
				context,
				DataSetFormat.parseFromString(repository,
						"APRowSimDataSetFormat"),
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()),
				new ConversionStandardToInputConfiguration());

		/*
		 * convertTo(SimMatrixDataSetFormat) is a special case
		 */
		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).preprocessAndConvertTo(
				context,
				DataSetFormat.parseFromString(repository,
						"SimMatrixDataSetFormat"),
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()),
				new ConversionStandardToInputConfiguration());
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
		((DataSet) this.repositoryObject).preprocessAndConvertTo(
				context,
				DataSetFormat.parseFromString(repository,
						"APRowSimDataSetFormat"),
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()),
				new ConversionStandardToInputConfiguration());
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
		((DataSet) this.repositoryObject).preprocessAndConvertTo(
				context,
				DataSetFormat
						.parseFromString(repository, "MatrixDataSetFormat"),
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()),
				new ConversionStandardToInputConfiguration());
	}

	/**
	 * Test method for
	 * {@link data.dataset.DataSet#convertToDirectly(data.dataset.format.DataSetFormat)}
	 * .
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws IOException
	 * @throws DataSetNotFoundException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws RNotAvailableException
	 * @throws InvalidParameterException
	 */
	@Test
	public void testConvertToDirectly() throws NoRepositoryFoundException,
			UnknownDataSetFormatException, IOException,
			DataSetNotFoundException, InvalidDataSetFormatVersionException,
			DataSetConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownDistanceMeasureException, InvalidParameterException,
			RNotAvailableException {

		File targetFile = new File(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.SimMatrix")
				.getAbsoluteFile();
		if (targetFile.exists())
			targetFile.delete();

		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim")
						.getAbsoluteFile());
		DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim");
		filterer.process();
		((DataSet) this.repositoryObject)
				.setAbsolutePath(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).convertToStandardDirectly(
				context,
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()));
		Assert.assertTrue(targetFile.exists());

		targetFile.delete();
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

		this.repositoryObject = DataSet
				.parseFromFile(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim")
						.getAbsoluteFile());
		DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
				"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim");
		filterer.process();
		((DataSet) this.repositoryObject)
				.setAbsolutePath(new File(
						"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip")
						.getAbsoluteFile());
		((DataSet) this.repositoryObject).convertToStandardDirectly(
				context,
				new ConversionInputToStandardConfiguration(
						DistanceMeasure.parseFromString(repository,
								"EuclidianDistanceMeasure"),
						new ArrayList<DataPreprocessor>(),
						new ArrayList<DataPreprocessor>()));
		DataSet standard = ((DataSet) this.repositoryObject)
				.getInStandardFormat();
		Assert.assertEquals(DataSetFormat.parseFromString(repository,
				"SimMatrixDataSetFormat"), standard.getDataSetFormat());
	}

	@Test
	public void testConvertToAbsoluteToAbsolute()
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
						"testCaseRepository/data/datasets/bone_marrow_gene_expr/ALB_ALT_AML.1000genes.res.out2")
						.getAbsoluteFile());
		DataSet newDataSet = ((DataSet) this.repositoryObject)
				.preprocessAndConvertTo(
						context,
						DataSetFormat.parseFromString(repository,
								"MatrixDataSetFormat"),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration());
		Assert.assertEquals("MatrixDataSetFormat", newDataSet
				.getDataSetFormat().getClass().getSimpleName());
		Assert.assertEquals(context.getStandardInputFormat().getClass()
				.getSimpleName(), newDataSet.thisInStandardFormat
				.getDataSetFormat().getClass().getSimpleName());
	}

	@Test
	public void testConvertToStandardToStandard()
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
						"testCaseRepository/data/datasets/bone_marrow_gene_expr/ALB_ALT_AML.1000genes.res.out2.SimMatrix")
						.getAbsoluteFile());
		DataSet newDataSet = ((DataSet) this.repositoryObject)
				.preprocessAndConvertTo(
						context,
						context.getStandardInputFormat(),
						new ConversionInputToStandardConfiguration(
								DistanceMeasure.parseFromString(repository,
										"EuclidianDistanceMeasure"),
								new ArrayList<DataPreprocessor>(),
								new ArrayList<DataPreprocessor>()),
						new ConversionStandardToInputConfiguration());
		Assert.assertEquals(context.getStandardInputFormat().getClass()
				.getSimpleName(), newDataSet.getDataSetFormat().getClass()
				.getSimpleName());
		Assert.assertEquals(context.getStandardInputFormat().getClass()
				.getSimpleName(), newDataSet.thisInStandardFormat
				.getDataSetFormat().getClass().getSimpleName());
	}
}
