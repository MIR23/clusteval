package de.clusteval.data.dataset;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import de.clusteval.context.Context;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormatParser;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.utils.FormatConversionException;
import de.clusteval.utils.NamedDoubleAttribute;
import de.clusteval.utils.NamedIntegerAttribute;
import de.clusteval.utils.RNotAvailableException;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * A wrapper class for a dataset on the filesystem.
 * 
 * <p>
 * A dataset corresponds to and is parsed from a file on the filesystem in the
 * corresponding folder of the repository (see
 * {@link Repository#dataSetBasePath} and {@link DataSetFinder}).
 * 
 * <p>
 * When a program should be applied to a certain dataset during a run, the
 * dataset is not directly used by the run but instead a {@link DataSetConfig}
 * is referenced, which encapsulates a dataset together with some settings.
 * 
 * @author Christian Wiwie
 * 
 */
public class DataSet extends RepositoryObject {

	/**
	 * Every data set needs an alias, that is used to represent the data set as
	 * a short string, for example on the website.
	 */
	protected String alias;

	/**
	 * The format of this dataset. The format for a dataset is required by the
	 * framework in order for it to be able to convert it to the internal
	 * standard format.
	 * 
	 * <p>
	 * When a dataset is used during a run, it is first converted from its
	 * original dataset format to the internal standard format. Then it is
	 * converted to the input format required by the clustering method.
	 */
	protected DataSetFormat datasetFormat;

	/**
	 * The type of the dataset is used to categorize the datasets.
	 */
	protected DataSetType datasetType;

	/**
	 * When a dataset is used during a run, it first is converted to the
	 * internal standard format and afterwards into the format required by the
	 * clustering method. This attribute holds the original unconverted dataset.
	 */
	protected DataSet originalDataSet;

	/**
	 * The checksum of a dataset is used to check a dataset for changes and to
	 * check two datasets for equality.
	 */
	protected long checksum;

	/**
	 * Instantiates a new dataset object.
	 * 
	 * @param repository
	 *            The repository this dataset should be registered at.
	 * @param register
	 *            Whether this dataset should be registered in the repository.
	 * @param changeDate
	 *            The change date of this dataset is used for equality checks.
	 * @param absPath
	 *            The absolute path of this dataset.
	 * @param alias
	 *            A short alias name for this data set.
	 * @param dsFormat
	 *            The format of this dataset.
	 * @param dsType
	 *            The type of this dataset
	 * @throws RegisterException
	 */
	public DataSet(final Repository repository, final boolean register,
			final long changeDate, final File absPath, final String alias,
			final DataSetFormat dsFormat, final DataSetType dsType)
			throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.alias = alias;
		this.datasetFormat = dsFormat;
		this.datasetType = dsType;

		this.originalDataSet = this;

		this.checksum = absPath.length();

		createAndRegisterInternalAttributes();

		if (register) {
			if (this.register()) {
				// added 21.03.2013: register dataset format here: only if the
				// dataset has been registered
				this.datasetFormat.register();
				this.datasetFormat.addListener(this);
				// added 21.03.2013: register dataset type here: only if the
				// dataset has been registered
				this.datasetType.register();
				this.datasetType.addListener(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.framework.repository.RepositoryObject#setAbsolutePath(java
	 * .io.File)
	 */
	@Override
	public void setAbsolutePath(File absFilePath) {
		super.setAbsolutePath(absFilePath);

		try {
			createAndRegisterInternalAttributes();
		} catch (RegisterException e) {
		}
	}

	@SuppressWarnings("unused")
	private void createAndRegisterInternalAttributes() throws RegisterException {
		new NamedIntegerAttribute(this.repository, this.getAbsolutePath()
				+ ":numberOfElements", new Integer(Integer.MIN_VALUE));
	}

	/**
	 * Copy constructor for the DataSet class.
	 * 
	 * @param dataset
	 *            the dataset to be cloned
	 * @throws RegisterException
	 */
	public DataSet(DataSet dataset) throws RegisterException {
		super(dataset);

		this.datasetFormat = dataset.datasetFormat.clone();
		this.datasetType = dataset.datasetType.clone();

		this.checksum = absPath.length();

		if (dataset.originalDataSet != null
				&& dataset.originalDataSet != dataset)
			this.originalDataSet = dataset.originalDataSet.clone();
		else
			this.originalDataSet = dataset.originalDataSet;
	}

	@Override
	public DataSet clone() {
		try {
			return new DataSet(this);
		} catch (RegisterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method parses a dataset from a raw data file. First the method
	 * {@link #extractDataSetAttributes(File)} is invoked to parse the header of
	 * the dataset file, that means lines at the beginning of the file prefixed
	 * with '//'. This includes the format of the dataset, the format version
	 * and the type of the dataset.
	 * 
	 * <p>
	 * This method either returns an instance of {@link AbsoluteDataSet} or
	 * {@link RelativeDataSet} depending of the format specified in the file.
	 * 
	 * @param absPath
	 *            The absolute path to the dataset file that should be parsed.
	 * @return The DataSet object
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws UnknownDataSetFormatException
	 * @throws DataSetConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NumberFormatException
	 * @throws NoDataSetException
	 * @see AbsoluteDataSet
	 * @see RelativeDataSet
	 */
	public static DataSet parseFromFile(final File absPath)
			throws NoRepositoryFoundException, DataSetNotFoundException,
			UnknownDataSetFormatException, DataSetConfigurationException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException {

		if (!absPath.exists()) {
			throw new DataSetNotFoundException("The dataset file "
					+ absPath.getParentFile().getName() + "/"
					+ absPath.getName()
					+ " does not exist or is not accessible.");
		}

		try {
			Map<String, String> attributeValues = extractDataSetAttributes(absPath);

			if (attributeValues.size() == 0)
				throw new NoDataSetException("The file " + absPath
						+ " does not contain a dataset header.");

			Repository repo = Repository.getRepositoryForPath(absPath
					.getAbsolutePath());

			String alias;
			if (attributeValues.containsKey("alias"))
				alias = attributeValues.get("alias");
			else
				throw new DataSetConfigurationException(
						"No alias specified for data set "
								+ absPath.getAbsolutePath());
			// check whether the alias is already taken by another dataset ->
			// throw exception
			Collection<DataSet> dataSets;
			if (repo instanceof RunResultRepository)
				dataSets = repo.getParent().getDataSets();
			else
				dataSets = repo.getDataSets();

			for (DataSet ds : dataSets)
				if (!(repo instanceof RunResultRepository)
						&& !(ds.getAbsolutePath().equals(absPath
								.getAbsolutePath()))
						&& ds.getAlias().equals(alias))
					throw new DataSetConfigurationException("The alias ("
							+ alias + ") of the data set "
							+ absPath.getAbsolutePath()
							+ " is already taken by the data set "
							+ ds.getAbsolutePath());

			DataSetFormat dsFormat;
			if (attributeValues.containsKey("dataSetFormat")) {
				if (attributeValues.containsKey("dataSetFormatVersion"))
					dsFormat = DataSetFormat
							.parseFromString(
									repo,
									attributeValues.get("dataSetFormat"),
									Integer.valueOf(
											attributeValues
													.get("dataSetFormatVersion"))
											.intValue());
				else
					dsFormat = DataSetFormat.parseFromString(repo,
							attributeValues.get("datasetFormat"));
			} else {
				throw new DataSetConfigurationException(
						"No format specified for dataset "
								+ absPath.getAbsolutePath());
			}

			DataSetType dsType;
			if (attributeValues.containsKey("dataSetType")) {
				dsType = DataSetType.parseFromString(repo,
						attributeValues.get("dataSetType"));
			} else {
				throw new DataSetConfigurationException(
						"No type specified for dataset "
								+ absPath.getAbsolutePath());
			}

			final long changeDate = absPath.lastModified();

			DataSet result;

			LoggerFactory.getLogger(DataSet.class).debug(
					"Parsing dataset \"" + absPath + "\"");

			result = new DataSet(repo, true, changeDate, absPath, alias,
					(DataSetFormat) dsFormat, dsType);
			result = repo.getRegisteredObject(result);
			LoggerFactory.getLogger(DataSet.class).debug("Dataset parsed");
			return result;
		} catch (IOException e) {
			throw new UnknownDataSetFormatException(e);
		}
	}

	/**
	 * This method parses the header of a dataset file. A header is required for
	 * a dataset file to be recognized by the framework as a valid dataset file.
	 * If the file does not contain any header lines, it is ignored by the
	 * framework. A header line is of the form '// attribute = value'. The
	 * header should contain several lines:
	 * 
	 * <p>
	 * The type of the dataset, e.g. '// dataSetType =
	 * GeneExpressionDataSetType'
	 * <p>
	 * The format of the dataset, e.g. '// dataSetFormat = RowSimDataSetFormat'
	 * <p>
	 * The version of the dataset format, e.g. '// dataSetFormatVersion = 1'
	 * 
	 * @param absPath
	 * @return
	 * @throws IOException
	 */
	protected static Map<String, String> extractDataSetAttributes(
			final File absPath) throws IOException {
		DataSetAttributeParser attributeParser = new DataSetAttributeParser(
				absPath.getAbsolutePath());
		attributeParser.process();
		Map<String, String> attributeValues = attributeParser
				.getAttributeValues();
		return attributeValues;
	}

	/**
	 * Gets the dataset format.
	 * 
	 * @see DataSet#datasetFormat
	 * @return The dataset format
	 */
	public DataSetFormat getDataSetFormat() {
		return datasetFormat;
	}

	/**
	 * @return The type of this dataset.
	 * @see #datasetType
	 */
	public DataSetType getDataSetType() {
		return datasetType;
	}

	/**
	 * Gets the major name of this dataset. The major name corresponds to the
	 * folder the dataset resides in in the filesystem.
	 * 
	 * @return The major name
	 */
	public String getMajorName() {
		return absPath.getParentFile().getName();
	}

	/**
	 * Gets the minor name of this dataset. The minor name corresponds to the
	 * name of the file of this dataset.
	 * 
	 * @return The minor name
	 */
	public String getMinorName() {
		return absPath.getName();
	}

	/**
	 * Gets the full name of this dataset. The full name consists of the minor
	 * and the major name, separated by a slash: MAJOR/MINOR
	 * 
	 * @return The full name
	 */
	public String getFullName() {
		return getMajorName() + "/" + getMinorName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[DataSet:" + getFullName() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() throws RegisterException {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#copyTo(java.io.File, boolean)
	 */
	@Override
	public boolean copyTo(File copyDestination, final boolean overwrite) {
		return copyTo(copyDestination, overwrite, false);
	}

	@Override
	public boolean copyTo(File copyDestination, final boolean overwrite,
			final boolean updateAbsolutePath) {
		boolean result = false;
		if (!copyDestination.exists() || overwrite)
			result = this.datasetFormat.copyDataSetTo(this, copyDestination,
					overwrite);
		if (copyDestination.exists() && updateAbsolutePath)
			this.setAbsolutePath(copyDestination);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#copyToFolder(java.io.File, boolean)
	 */
	@Override
	public boolean copyToFolder(File copyFolderDestination,
			final boolean overwrite) {
		return this.datasetFormat.copyDataSetToFolder(this,
				copyFolderDestination, overwrite);
	}

	/**
	 * This method writes the contents of this dataset to the file denoted by
	 * the {@link #getAbsolutePath()}.
	 * 
	 * @param withHeader
	 *            Whether to write a header into the dataset file.
	 * 
	 * @return True, if the writing was succesfull
	 */
	public boolean writeToFile(final boolean withHeader) {
		// TODO
//		if (!isInMemory())
//			return false;
		return this.getDataSetFormat().writeToFile(this, withHeader);
	}

	/**
	 * This method does not load the content of the dataset into memory, it just
	 * assumes that it has been loaded before and returns the reference.
	 * 
	 * @return The content of this dataset.
	 */
	public Object getDataSetContent() {
		// TODO
		return null;
	}

	/**
	 * This method sets the content of this dataset in memory to a new object.
	 * Contents on file system are not refreshed.
	 * 
	 * @param newContent
	 *            The new content of this dataset.
	 * @return True, if the content of this dataset has been updated to the new
	 *         object.
	 */
	public boolean setDataSetContent(Object newContent) {
		// TODO
		return false;
	}

	/**
	 * This method converts this dataset to a target format:
	 * <p>
	 * First this dataset is converted to a internal standard format (depending
	 * on the type of the Run). Then it is converted to the target format.
	 * 
	 * @param context
	 * @param targetFormat
	 *            This is the format, the dataset is expected to be in after the
	 *            conversion process. After the dataset is converted to the
	 *            internal format, it is converted to the target format.
	 * @return The dataset in the target format.
	 * @throws FormatConversionException
	 * @throws IOException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws RegisterException
	 * @throws RNotAvailableException
	 */
	public DataSet preprocessAndConvertTo(final Context context,
			final DataSetFormat targetFormat) throws FormatConversionException,
			IOException, InvalidDataSetFormatVersionException,
			RegisterException, RNotAvailableException {

		// only one conversion process at a time
		File sourceFile = ClustevalBackendServer.getCommonFile(new File(this
				.getAbsolutePath()));
		synchronized (sourceFile) {
			DataSet result = null;
			DataSetFormat sourceFormat = this.getDataSetFormat();

			// check, whether dataset format parsers are registered.
			// if ((!sourceFormat.equals(context.getStandardInputFormat()) &&
			// !this
			// .getRepository().isRegisteredForDataSetFormat(
			// sourceFormat.getClass()))
			// || (!targetFormat.equals(context.getStandardInputFormat()) &&
			// !this
			// .getRepository().isRegisteredForDataSetFormat(
			// targetFormat.getClass())))
			// throw new FormatConversionException(
			// "No conversion from "
			// + sourceFormat
			// + " to "
			// + targetFormat
			// + " via internal standard format "
			// + context.getStandardInputFormat()
			// + " possible, because of missing dataset format parsers.");

			// 13.04.2013: update the original dataset of the dataset to itself
			this.originalDataSet = this;

			// convert the input format to the standard format
			try {
				// DataSetFormat standardFormat =
				// context.getStandardInputFormat();
				// DataSetFormat.parseFromString(
				// repository, context.getStandardInputFormat().getClass()
				// .getSimpleName());
				// standardFormat.setNormalized(targetFormat.getNormalized());

				// Remove dataset attributes from file and write the result to
				// dataSet.getAbsolutePath() + ".strip"
				final File strippedFilePath = new File(this.getAbsolutePath()
						+ ".strip");
				if (!strippedFilePath.exists()) {
					DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
							this.getAbsolutePath());
					filterer.process();
				}
				this.setAbsolutePath(strippedFilePath);

				try {
					result = this.convertTo(targetFormat);
				} catch (IOException e) {
					e.printStackTrace();
				}

				return result;
			} catch (UnknownDataSetFormatException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}

	protected DataSet convertTo(final DataSetFormat targetFormat)
			throws IOException, InvalidDataSetFormatVersionException,
			RegisterException, UnknownDataSetFormatException,
			InvalidParameterException, RNotAvailableException {

		Map<String, List<String>> conversions = this.repository
				.getAvailableFormatConversionsTo(targetFormat.getClass()
						.getSimpleName());

		String sourceFormat = this.getDataSetFormat().getClass()
				.getSimpleName();

		if (!(conversions.keySet().contains(sourceFormat)))
			throw new IllegalArgumentException(
					"Operation only supported for the standard dataset format");

		List<String> conversionPath = conversions.get(sourceFormat);

		DataSet converted = this;
		for (String conversion : conversionPath) {
			String[] split = conversion.split("_");
			String targetFormatStr = split[1];
			String parserClassSimpleName = split[2];
			String parserMethodStr = split[3];
			Class<? extends DataSetFormatParser> parser = this.repository
					.getDataSetFormatParser(parserClassSimpleName);
			try {
				converted = (DataSet) parser.getMethod(parserMethodStr,
						DataSet.class, DataSetFormat.class).invoke(
						parser.newInstance(),
						converted,
						this.repository.getDataSetFormatClass(targetFormatStr)
								.newInstance());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			// TODO
			converted.originalDataSet = this.originalDataSet;
		}
		return converted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#notify(utils.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (event.getOld().equals(this))
				super.notify(event);
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this))
				super.notify(event);
			else if (event.getRemovedObject().equals(this.datasetFormat)) {
				event.getRemovedObject().removeListener(this);
				this.log.info("DataSet " + this
						+ ": Removed, because DataSetFormat " + datasetFormat
						+ " has changed.");
				RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(this);
				this.unregister();
				this.notify(newEvent);
			} else if (event.getRemovedObject().equals(this.datasetType)) {
				event.getRemovedObject().removeListener(this);
				this.log.info("DataSet " + this
						+ ": Removed, because DataSetType " + datasetType
						+ " has changed.");
				RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(this);
				this.unregister();
				this.notify(newEvent);
			}
		}
	}

	/**
	 * Move this dataset to a new location.
	 * 
	 * <p>
	 * If the overwrite parameter is true and the target file exists already, it
	 * is overwritten.
	 * 
	 * @param targetFile
	 *            A file object wrapping the absolute path of the destination
	 *            this dataset should be moved to.
	 * @param overwrite
	 *            A boolean indicating, whether to overwrite a possibly existing
	 *            target file.
	 * @return True, if this dataset has been moved successfully.
	 */
	public boolean move(final File targetFile, final boolean overwrite) {
		if (!targetFile.exists() || overwrite) {
			boolean result = this.datasetFormat.moveDataSetTo(this, targetFile,
					overwrite);
			if (targetFile.exists())
				this.absPath = targetFile;
			return result;
		}
		if (targetFile.exists())
			this.absPath = targetFile;
		return false;
	}

	/**
	 * @return The alias of this data set.
	 * @see #alias
	 */
	public String getAlias() {
		return this.alias;
	}

	/**
	 * @return This dataset in its original format.
	 * @see #originalDataSet
	 */
	public DataSet getOriginalDataSet() {
		return this.originalDataSet;
	}

	/**
	 * @return Checksum of this dataset
	 * @see #checksum
	 */
	public long getChecksum() {
		return this.checksum;
	}

	/**
	 * @return The object ids contained in the dataset.
	 */
	public List<String> getIds() {
		// TODO
		return null;
	}
}
