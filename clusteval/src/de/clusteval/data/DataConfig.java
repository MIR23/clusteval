package de.clusteval.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.RunResultDataSetConfig;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.quality.QualityMeasure;
import file.FileUtils;

/**
 * A data configuration encapsulates options and settings for all kinds of data
 * (dataset and goldstandard). During the execution of a run, when programs are
 * applied to datasets and goldstandards, settings are required that control the
 * behaviour of how this data has to be handled.
 * 
 * <p>
 * A data configuration corresponds to and is parsed from a file on the
 * filesystem in the corresponding folder of the repository (see
 * {@link Repository#dataConfigBasePath} and {@link DataConfigFinder}).
 * 
 * <p>
 * There are several options, that can be specified in the data configuration
 * file (see {@link #parseFromFile(File)}).
 * 
 * 
 * @author Christian Wiwie
 * 
 */
public class DataConfig extends RepositoryObject {

	/**
	 * A helper method for cloning a list of data configurations.
	 * 
	 * @param dataConfigs
	 *            The list of data configurations to clone.
	 * @return The list containing the cloned objects of the input list.
	 */
	public static List<DataConfig> cloneDataConfigurations(
			final List<DataConfig> dataConfigs) {
		List<DataConfig> result = new ArrayList<DataConfig>();

		for (DataConfig dataConfig : dataConfigs) {
			result.add(dataConfig.clone());
		}

		return result;
	}

	/**
	 * A data configuration encapsulates a dataset configuration. This dataset
	 * configuration contains options and settings how to handle the
	 * encapsulated dataset.
	 */
	protected DataSetConfig dataSetConfig;

	/**
	 * The original dataset config. is only != null, if the corresponding
	 * dataset was converted into a different format
	 */
	protected DataSetConfig standardDataSetConfig;

	/**
	 * A data configuration encapsulates a goldstandard configuration. This
	 * goldstandard configuration contains options and settings how to handle
	 * the encapsulated goldstandard.
	 */
	protected GoldStandardConfig goldstandardConfig;

	/**
	 * Instantiates a new data configuration.
	 * 
	 * @param repository
	 *            The repository this data configuration should be registered
	 *            at.
	 * @param changeDate
	 *            The change date of this data configuration is used for
	 *            equality checks.
	 * @param absPath
	 *            The absolute path of this data configuration.
	 * @param datasetConfig
	 *            The dataset configuration encapsulated by this data
	 *            configuration.
	 * @param gsConfig
	 *            The goldstandard configuration encapsulated by this data
	 *            configuration.
	 * @throws RegisterException
	 */
	public DataConfig(final Repository repository, final long changeDate,
			final File absPath, final DataSetConfig datasetConfig,
			final GoldStandardConfig gsConfig) throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.dataSetConfig = datasetConfig;
		this.goldstandardConfig = gsConfig;
		if (this.register()) {
			this.dataSetConfig.addListener(this);
			if (this.hasGoldStandardConfig())
				this.goldstandardConfig.addListener(this);
		}
	}

	/**
	 * Use this method to check, whether this DataConfig has a goldstandard
	 * configuration or not. Some clustering quality measures do not require a
	 * goldstandard to evaluate a clustering (see
	 * {@link QualityMeasure#requiresGoldstandard()}).
	 * 
	 * @return True, if this data configuration has a goldstandard, false
	 *         otherwise.
	 */
	public boolean hasGoldStandardConfig() {
		return this.goldstandardConfig != null;
	}

	/**
	 * The copy constructor for data configurations. This copy constructor
	 * creates a deep copy by not only copying the references of attribute
	 * variables, but also cloning those.
	 * 
	 * @param dataConfig
	 *            the data config
	 * @throws RegisterException
	 */
	public DataConfig(DataConfig dataConfig) throws RegisterException {
		super(dataConfig);

		this.dataSetConfig = dataConfig.dataSetConfig.clone();
		this.goldstandardConfig = dataConfig.hasGoldStandardConfig()
				? dataConfig.goldstandardConfig.clone()
				: null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public DataConfig clone() {
		try {
			return new DataConfig(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method parses a data configuration from a file on the filesystem.
	 * 
	 * <p>
	 * A data configuration contains several options:
	 * <ul>
	 * <li><b>datasetConfig</b>: The name of the dataset configuration to be
	 * encapsulated by this data configuration.</li>
	 * <li><b>goldstandardConfig</b>: The name of the goldstandard configuration
	 * to be encapsulated by this data configuration.</li>
	 * </ul>
	 * 
	 * @param absConfigPath
	 *            The absolute path to the dataset configuration file.
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws DataConfigNotFoundException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NumberFormatException
	 * @throws NoDataSetException
	 * @return The data configuration object.
	 */
	public static DataConfig parseFromFile(final File absConfigPath)
			throws UnknownDataSetFormatException, NoRepositoryFoundException,
			GoldStandardNotFoundException, GoldStandardConfigurationException,
			DataSetConfigurationException, DataSetNotFoundException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, RegisterException,
			UnknownDataSetTypeException, NumberFormatException,
			NoDataSetException {

		if (!absConfigPath.exists())
			throw new DataConfigNotFoundException("Data config \""
					+ absConfigPath + "\" does not exist!");

		Logger log = LoggerFactory.getLogger(DataConfig.class);

		log.debug("Parsing data config \"" + absConfigPath + "\"");

		try {

			HierarchicalINIConfiguration props = new HierarchicalINIConfiguration(
					absConfigPath);
			props.setThrowExceptionOnMissing(true);

			final long changeDate = absConfigPath.lastModified();

			Repository repo = Repository.getRepositoryForPath(absConfigPath
					.getAbsolutePath());

			String datasetConfigName = props.getString("datasetConfig");
			DataSetConfig dataSetConfig;
			if (repo instanceof RunResultRepository)
				dataSetConfig = RunResultDataSetConfig.parseFromFile(new File(
						FileUtils.buildPath(repo.getDataSetConfigBasePath(),
								datasetConfigName + ".dsconfig")));
			else
				dataSetConfig = DataSetConfig.parseFromFile(new File(FileUtils
						.buildPath(repo.getDataSetConfigBasePath(),
								datasetConfigName + ".dsconfig")));

			GoldStandardConfig goldStandardConfig = null;
			try {
				String gsConfigName = props.getString("goldstandardConfig");
				goldStandardConfig = GoldStandardConfig.parseFromFile(new File(
						FileUtils.buildPath(
								repo.getGoldStandardConfigBasePath(),
								gsConfigName + ".gsconfig")));
			} catch (NoSuchElementException e) {
				// No goldstandard config given
			}

			DataConfig result = new DataConfig(repo, changeDate, absConfigPath,
					dataSetConfig, goldStandardConfig);
			result = repo.getRegisteredObject(result);
			return result;
		} catch (ConfigurationException e) {
			throw new DataConfigurationException(e);
		} catch (NoSuchElementException e) {
			throw new DataConfigurationException(e);
		}
	}

	/**
	 * 
	 * @return If during the execution of a run the dataset has been converted
	 *         to a different format, this method returns the converted dataset
	 *         configuration. If no conversion has been performed, this method
	 *         returns the original dataset configuration.
	 */
	public DataSetConfig getDatasetConfig() {
		return dataSetConfig;
	}

	/**
	 * @return The goldstandard configuration of this data configuration. Is
	 *         null, if there is no goldstandard configuration.
	 */
	public GoldStandardConfig getGoldstandardConfig() {
		return goldstandardConfig;
	}

	/**
	 * The name of a gold standard configuration is the filename of the
	 * corresponding file on the filesystem, without the file extension.
	 * 
	 * @return The name of this data configuration.
	 */
	public String getName() {
		return this.absPath.getName().replace(".dataconfig", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
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
	 * @see utils.RepositoryObject#notify(utils.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (event.getOld().equals(this))
				super.notify(event);
			else {
				if (event.getOld().equals(dataSetConfig)) {
					event.getOld().removeListener(this);
					this.log.info("DataConfig "
							+ this
							+ ": DataSetConfig reloaded due to modifications in filesystem");
					event.getReplacement().addListener(this);
					this.dataSetConfig = (DataSetConfig) event.getReplacement();
				} else if (event.getOld().equals(goldstandardConfig)) {
					event.getOld().removeListener(this);
					this.log.info("DataConfig "
							+ this
							+ ": GoldStandardConfig reloaded due to modifications in filesystem");
					event.getReplacement().addListener(this);
					this.goldstandardConfig = (GoldStandardConfig) event
							.getReplacement();
				} else if (event.getOld().equals(standardDataSetConfig)) {
					event.getOld().removeListener(this);
					this.log.info("DataConfig "
							+ this
							+ ": Original DataSet reloaded due to modifications in filesystem");
					event.getReplacement().addListener(this);
					this.standardDataSetConfig = (DataSetConfig) event
							.getReplacement();
				}
			}
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this))
				super.notify(event);
			else {
				if (event.getRemovedObject().equals(dataSetConfig)) {
					event.getRemovedObject().removeListener(this);
					this.log.info("DataConfig " + this
							+ ": Removed, because DataSetConfig "
							+ dataSetConfig + " was removed.");
					RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
							this);
					this.unregister();
					this.notify(newEvent);
				} else if (event.getRemovedObject().equals(goldstandardConfig)) {
					event.getRemovedObject().removeListener(this);
					this.log.info("DataConfig " + this
							+ ": Removed, because GoldStandardConfig "
							+ goldstandardConfig + " was removed.");
					RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
							this);
					this.unregister();
					this.notify(newEvent);
				} else if (event.getRemovedObject().equals(
						standardDataSetConfig)) {
					event.getRemovedObject().removeListener(this);
					this.log.info("DataConfig " + this
							+ ": Removed, because Standard-DataSetConfig "
							+ standardDataSetConfig + " was removed.");
					RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
							this);
					this.unregister();
					this.notify(newEvent);
				}
			}
		}
	}
}
