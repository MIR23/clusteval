package de.clusteval.data.dataset.format;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import de.clusteval.data.dataset.DataSet;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.utils.RNotAvailableException;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import file.FileUtils;

/**
 * Datasets can have different formats. For all kinds of operations the
 * framework needs to know which format a dataset has and how it can be
 * converted to an understandable (standard) format.
 * 
 * <p>
 * Every dataset format comes together with a parser class (see
 * {@link DataSetFormatParser}).
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class DataSetFormat extends RepositoryObject {

	/**
	 * This method returns a deep copy of the given list of dataset formats,
	 * i.e. the objects of the list are also cloned.
	 * 
	 * @param dataSetFormats
	 *            The list of dataset formats to clone.
	 * @return The cloned list of dataset formats.
	 */
	public static List<Set<DataSetFormat>> cloneDataSetFormats(
			final List<Set<DataSetFormat>> dataSetFormats) {
		List<Set<DataSetFormat>> result = new ArrayList<Set<DataSetFormat>>();

		for (Set<DataSetFormat> set : dataSetFormats) {
			Set<DataSetFormat> newSet = new HashSet<DataSetFormat>();
			for (DataSetFormat dataSetFormat : set) {
				newSet.add(dataSetFormat.clone());
			}
			result.add(newSet);
		}

		return result;
	}

	/**
	 * A boolean indicating, whether the dataset format is normalized.
	 */
	private boolean normalized;

	/**
	 * The version number of the dataset format.
	 * 
	 * <p>
	 * This is used for compatibility reasons to ensure, that if at some point a
	 * format specification changes, the framework can recognize this.
	 */
	private int version;

	/**
	 * This method parses a dataset format from the given string, containing a
	 * dataset format class name and a given dataset format version.
	 * 
	 * @param repository
	 *            The repository where to look up the dataset format class.
	 * @param datasetFormat
	 *            The dataset format class name as string.
	 * @param formatVersion
	 *            The version of the dataset format.
	 * @return The parsed dataset format.
	 * @throws UnknownDataSetFormatException
	 */
	public static DataSetFormat parseFromString(final Repository repository,
			String datasetFormat, final int formatVersion)
			throws UnknownDataSetFormatException {
		Class<? extends DataSetFormat> c = repository
				.getDataSetFormatClass("de.clusteval.data.dataset.format."
						+ datasetFormat);
		try {
			Constructor<? extends DataSetFormat> constr = c.getConstructor(
					Repository.class, boolean.class, long.class, File.class,
					int.class);
			// changed 21.03.2013: do not register dataset formats here
			return constr.newInstance(repository, false,
					System.currentTimeMillis(), new File(datasetFormat),
					formatVersion);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new UnknownDataSetFormatException("\"" + datasetFormat
				+ "\" is not a known dataset format.");
	}

	/**
	 * This method parses a dataset format from the given string, containing a
	 * dataset format class name.
	 * 
	 * @param repository
	 *            The repository where to look up the dataset format class.
	 * @param datasetFormat
	 *            The dataset format class name as string.
	 * @return The parsed dataset format.
	 * @throws UnknownDataSetFormatException
	 */
	public static DataSetFormat parseFromString(final Repository repository,
			String datasetFormat) throws UnknownDataSetFormatException {
		return parseFromString(repository, datasetFormat,
				repository.getCurrentDataSetFormatVersion(datasetFormat));
	}

	/**
	 * This method parses several dataset formats from a string array.
	 * 
	 * <p>
	 * This is a convenience method for
	 * {@link #parseFromString(Repository, String)}.
	 * 
	 * @param repo
	 *            the repo
	 * @param datasetFormats
	 *            the dataset formats
	 * @return the list
	 * @throws UnknownDataSetFormatException
	 *             the unknown data set format exception
	 */
	public static List<DataSetFormat> parseFromString(final Repository repo,
			String[] datasetFormats) throws UnknownDataSetFormatException {
		List<DataSetFormat> result = new LinkedList<DataSetFormat>();
		for (String dsFormat : datasetFormats) {
			result.add(parseFromString(repo, dsFormat));
		}
		return result;
	}

	/**
	 * @param dataSet
	 *            The dataset to be written to the filesystem.
	 * @param withHeader
	 *            Whether to write the header into the dataset file.
	 * @return True, if the dataset has been written to filesystem successfully.
	 */
	public boolean writeToFile(final DataSet dataSet, final boolean withHeader) {
		final DataSetFormatParser parser = getDataSetFormatParser();
		if (parser == null)
			throw new IllegalArgumentException(
					"Operation only supported for the standard dataset format");
		return parser.writeToFile(dataSet, withHeader);
	}

	/**
	 * Convert the given dataset with this dataset format and the given version
	 * using the passed configuration.
	 * 
	 * <p>
	 * This method validates, that the passed dataset has the correct format and
	 * that the version of the format is supported.
	 * 
	 * @param dataSet
	 *            The dataset to convert to the standard format.
	 * @param targetFormat
	 * @return The converted dataset.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InvalidDataSetFormatVersionException
	 * @throws RegisterException
	 * @throws UnknownDataSetFormatException
	 * @throws RNotAvailableException
	 */
	public final DataSet convertTo(DataSet dataSet, DataSetFormat targetFormat)
			throws IOException, InvalidDataSetFormatVersionException,
			RegisterException, UnknownDataSetFormatException,
			RNotAvailableException {
		Map<String, List<String>> conversions = this.repository
				.getAvailableFormatConversionsTo(targetFormat.getClass()
						.getSimpleName());

		String sourceFormat = dataSet.getDataSetFormat().getClass()
				.getSimpleName();

		if (!(conversions.keySet().contains(sourceFormat)))
			throw new IllegalArgumentException(
					"Operation only supported for the standard dataset format");

		List<String> conversionPath = conversions.get(sourceFormat);

		DataSet converted = dataSet;
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
		}
		return converted;
	}

	/**
	 * 
	 * @return An instance of the dataset format parser corresponding to this
	 *         dataset format class.
	 */
	public abstract DataSetFormatParser getDataSetFormatParser();

	/**
	 * Instantiates a new dataset format with the given version.
	 * 
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * 
	 * @param version
	 *            The version of the dataset format.
	 * @throws RegisterException
	 */
	public DataSetFormat(final Repository repo, final boolean register,
			final long changeDate, final File absPath, final int version)
			throws RegisterException {
		super(repo, false, changeDate, absPath);
		this.version = version;
		this.log = LoggerFactory.getLogger(this.getClass());

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of dataset formats.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DataSetFormat(final DataSetFormat other) throws RegisterException {
		super(other);

		this.normalized = other.normalized;
		this.version = other.version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/**
	 * @param normalized
	 *            Whether this dataset is normalized.
	 */
	public void setNormalized(final boolean normalized) {
		this.normalized = normalized;
	}

	/**
	 * @return Whether this dataset is normalized.
	 */
	public boolean getNormalized() {
		return this.normalized;
	}

	/**
	 * @return The version number of the dataset format.
	 */
	public int getVersion() {
		return this.version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DataSetFormat))
			return false;

		DataSetFormat other = (DataSetFormat) o;
		return (other.getClass().getSimpleName()
				.equals(this.getClass().getSimpleName())
				&& this.normalized == other.normalized && this.getVersion() == other
				.getVersion());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final DataSetFormat clone() {
		try {
			return this.getClass().getConstructor(this.getClass())
					.newInstance(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		this.log.warn("Cloning instance of class "
				+ this.getClass().getSimpleName() + " failed");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.getClass().toString() + this.normalized + this
				.getVersion()).hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":v" + this.getVersion();
	}

	/**
	 * This method copies the given dataset to the given target file, assuming
	 * that the format of the dataset is this dataset format.
	 * 
	 * @param dataSet
	 *            The dataset to copy to the target file destination.
	 * @param copyDestination
	 *            The target file to which to copy the given dataset.
	 * @param overwrite
	 *            Whether to overwrite the possibly already existing target
	 *            file.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyDataSetTo(final DataSet dataSet,
			final File copyDestination, final boolean overwrite) {
		try {
			if (!copyDestination.exists() || overwrite)
				org.apache.commons.io.FileUtils.copyFile(
						new File(dataSet.getAbsolutePath()), copyDestination);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * This method copies the given dataset to the given target file, assuming
	 * that the format of the dataset is this dataset format.
	 * 
	 * @param dataSet
	 *            The dataset to copy to the target file destination.
	 * @param moveDestination
	 *            The target file to which to copy the given dataset.
	 * @param overwrite
	 *            Whether to overwrite the possibly already existing target
	 *            file.
	 * @return True, if the copy operation was successful.
	 */
	public boolean moveDataSetTo(final DataSet dataSet,
			final File moveDestination, final boolean overwrite) {
		try {
			if (!moveDestination.exists() || overwrite)
				org.apache.commons.io.FileUtils.moveFile(
						new File(dataSet.getAbsolutePath()), moveDestination);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * This method copies the given dataset into the given target folder,
	 * assuming that the format of the dataset is this dataset format.
	 * 
	 * @param dataSet
	 *            The dataset to copy to the target file destination.
	 * @param copyFolderDestination
	 *            The target folder to which into copy the given dataset.
	 * @param overwrite
	 *            Whether to overwrite the possibly already existing target
	 *            file.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyDataSetToFolder(final DataSet dataSet,
			final File copyFolderDestination, final boolean overwrite) {
		try {
			File targetFile = new File(FileUtils.buildPath(
					copyFolderDestination.getAbsolutePath(),
					new File(dataSet.getAbsolutePath()).getName()));
			if (!targetFile.exists() || overwrite)
				org.apache.commons.io.FileUtils.copyFile(
						new File(dataSet.getAbsolutePath()), targetFile);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * This alias is used whenever this dataset format is visually represented
	 * and a readable name is needed.
	 * 
	 * @return The alias of this dataset format.
	 */
	public abstract String getAlias();
}
