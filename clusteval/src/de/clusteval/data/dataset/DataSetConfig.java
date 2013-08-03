package de.clusteval.data.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.LoggerFactory;

import utils.Triple;
import de.clusteval.data.dataset.format.DataSetFormatParser;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.Parsable;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import file.FileUtils;

/**
 * 
 * A dataset configuration encapsulates options and settings for a dataset.
 * During the execution of a run, when programs are applied to datasets,
 * settings are required that control the behaviour of how the dataset has to be
 * handled.
 * 
 * <p>
 * A dataset configuration corresponds to and is parsed from a file on the
 * filesystem in the corresponding folder of the repository (see
 * {@link Repository#dataSetConfigBasePath} and {@link DataSetConfigFinder}).
 * 
 * <p>
 * There are several options, that can be specified in the dataset configuration
 * file (see {@link #parseFromFile(File)}).
 * 
 * @author Christian Wiwie
 * 
 */
public class DataSetConfig extends RepositoryObject {

	/**
	 * A helper method for cloning a list of data sets.
	 * 
	 * @param dataSets
	 *            The list of data sets to clone.
	 * @return The list containing the cloned objects of the input list.
	 */
	public static List<Triple<String, DataSet, String>> cloneDataSets(
			final List<Triple<String, DataSet, String>> dataSets) {
		List<Triple<String, DataSet, String>> result = new ArrayList<Triple<String, DataSet, String>>();

		for (Triple<String, DataSet, String> dataSet : dataSets) {
			result.add(Triple.getTriple(dataSet.getFirst() + "", dataSet
					.getSecond().clone(), dataSet.getThird()));
		}

		return result;
	}

	/**
	 * A dataset configuration encapsulates a dataset. This attribute stores a
	 * reference to the dataset wrapper object.
	 */
	protected List<Triple<String, DataSet, String>> datasets;

	protected List<String> groups;

	protected Map<String, List<Triple<String, DataSet, String>>> groupToDataSet;

	protected List<DirectedSparseMultigraph<String, String>> graphs;

	/**
	 * Instantiates a new dataset configuration.
	 * 
	 * @param repository
	 *            The repository this dataset configuration should be registered
	 *            at.
	 * @param changeDate
	 *            The change date of this dataset configuration is used for
	 *            equality checks.
	 * @param absPath
	 *            The absolute path of this dataset configuration.
	 * @param groups
	 * @param ds
	 *            The encapsulated dataset.
	 * @throws RegisterException
	 */
	public DataSetConfig(final Repository repository, final long changeDate,
			final File absPath, final List<String> groups,
			final List<Triple<String, DataSet, String>> ds)
			throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.groups = groups;

		this.datasets = ds;

		initGroupToDataSets();

		if (this.register()) {
			for (Triple<String, DataSet, String> dataset : this.datasets)
				dataset.getSecond().addListener(this);
		}
	}

	protected void initGroupToDataSets() {
		this.groupToDataSet = new HashMap<String, List<Triple<String, DataSet, String>>>();
		for (Triple<String, DataSet, String> triple : this.datasets) {
			if (!this.groupToDataSet.containsKey(triple.getThird()))
				this.groupToDataSet.put(triple.getThird(),
						new ArrayList<Triple<String, DataSet, String>>());
			this.groupToDataSet.get(triple.getThird()).add(triple);
		}
	}

	/**
	 * The copy constructor for dataset configurations.
	 * 
	 * @param datasetConfig
	 *            The dataset configuration to be cloned.
	 * @throws RegisterException
	 */
	public DataSetConfig(DataSetConfig datasetConfig) throws RegisterException {
		super(datasetConfig);

		this.datasets = cloneDataSets(datasetConfig.datasets);
		this.groups = new ArrayList<String>(datasetConfig.groups);

		initGroupToDataSets();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public DataSetConfig clone() {
		try {
			return new DataSetConfig(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method parses a dataset configuration from a file on the filesystem.
	 * 
	 * <p>
	 * A dataset configuration contains several options:
	 * <ul>
	 * <li><b>datasetName</b>: The folder the dataset file lies within.</li>
	 * <li><b>datasetFile</b>: The filename of the dataset file.</li>
	 * <li><b>distanceMeasureAbsoluteToRelative</b>: If the dataset contains
	 * absolute coordinates, this measure is used to calculate the pairwise
	 * distances/similarities between the object pairs.</li>
	 * <li><b>preprocessorBeforeDistance</b>: A comma seperated list of data
	 * preprocessors to apply, before the data is converted to pairwise
	 * similarities (the standard input format)</li>
	 * <li><b>preprocessorAfterDistance</b>: A comma seperated list of data
	 * preprocessors to apply, after the data is converted to pairwise
	 * similarities (the standard input format)</li>
	 * </ul>
	 * 
	 * @param absConfigPath
	 *            The absolute path to the dataset configuration file.
	 * @throws DataSetConfigurationException
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NumberFormatException
	 * @throws NoDataSetException
	 * @return The dataset configuration object.
	 */
	public static DataSetConfig parseFromFile(final File absConfigPath)
			throws DataSetConfigurationException,
			UnknownDataSetFormatException, NoRepositoryFoundException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException {

		if (!absConfigPath.exists())
			throw new DataSetConfigNotFoundException("Dataset config \""
					+ absConfigPath + "\" does not exist!");

		LoggerFactory.getLogger(DataSetConfig.class).debug(
				"Parsing dataset config \"" + absConfigPath + "\"");

		try {
			HierarchicalINIConfiguration conf = new HierarchicalINIConfiguration(
					absConfigPath);

			Repository repo = Repository.getRepositoryForPath(absConfigPath
					.getAbsolutePath());

			List<Triple<String, DataSet, String>> dataSets = new ArrayList<Triple<String, DataSet, String>>();

			List<String> groups = new ArrayList<String>(Arrays.asList(conf
					.getStringArray("groups")));

			Set<String> sections = conf.getSections();
			for (String section : sections) {
				if (section == null)
					continue;
				SubnodeConfiguration props = conf.getSection(section);
				props.setThrowExceptionOnMissing(true);

				String datasetName = props.getString("datasetName");
				String datasetFile = props.getString("datasetFile");
				String groupName = props.getString("groupName");

				DataSet dataSet = DataSet.parseFromFile(new File(FileUtils
						.buildPath(repo.getDataSetBasePath(), datasetName,
								datasetFile)));

				dataSets.add(Triple.getTriple(section, dataSet, groupName));
			}
			DataSetConfig result = new DataSetConfig(repo,
					absConfigPath.lastModified(), absConfigPath, groups,
					dataSets);
			result = repo.getRegisteredObject(result);
			return result;
		} catch (ConfigurationException e) {
			throw new DataSetConfigurationException(e);
		} catch (NoSuchElementException e) {
			throw new DataSetConfigurationException(e);
		}
	}

	/**
	 * @return The dataset, this configuration belongs to.
	 */
	public List<Triple<String, DataSet, String>> getDataSets() {
		return datasets;
	}

	public List<Triple<String, DataSet, String>> getDataSetsForGroup(
			final String groupName) {
		return this.groupToDataSet.get(groupName);
	}

	public List<DirectedSparseMultigraph<String, String>> getGraphs() {
		return this.graphs;
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
			if (event.getOld().equals(this)) {
				super.notify(event);
			} else {
				// check whether the object was a dataset contained in this
				// dataset config
				for (int i = 0; i < datasets.size(); i++) {
					DataSet dataset = datasets.get(i).getSecond();
					if (event.getOld().equals(dataset)) {
						event.getOld().removeListener(this);
						this.log.info("DataSetConfig "
								+ this.absPath.getName()
								+ ": Dataset reloaded due to modifications in filesystem");
						event.getReplacement().addListener(this);
						// added 06.07.2012
						this.datasets.get(i).setSecond(
								(DataSet) event.getReplacement());
						break;
					}
				}
			}
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this))
				super.notify(event);
			else {
				// check whether the object was a dataset contained in this
				// dataset config
				for (int i = 0; i < datasets.size(); i++) {
					DataSet dataset = datasets.get(i).getSecond();
					if (event.getRemovedObject().equals(dataset)) {
						event.getRemovedObject().removeListener(this);
						this.log.info("DataSetConfig " + this
								+ ": Removed, because DataSet " + dataset
								+ " was removed.");
						RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
								this);
						this.unregister();
						this.notify(newEvent);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.absPath.getName().replace(".dsconfig", "");
	}

	/**
	 * @param datasets
	 *            The new datasets
	 */
	public void setDataSets(List<Triple<String, DataSet, String>> datasets) {
		this.datasets = datasets;
	}

	/**
	 * Load this dataset into memory. When this method is invoked, it parses the
	 * dataset file on the filesystem using the
	 * {@link DataSetFormatParser#parse(DataSet)} method corresponding to the
	 * dataset format of this dataset. Then the contents of the dataset is
	 * stored in a member variable.
	 * 
	 * @return true, if successful
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public boolean loadIntoMemory() throws UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException {
		this.graphs = this.parse();
		return true;
	}

	/**
	 * Checks whether this dataset is loaded into the memory.
	 * 
	 * @return true, if is in memory
	 */
	public boolean isInMemory() {
		return this.graphs != null;
	}

	/**
	 * Unload the contents of this dataset from memory.
	 * 
	 * @return true, if successful
	 */
	public boolean unloadFromMemory() {
		this.graphs = null;
		return true;
	}

	/**
	 * @param dataSet
	 *            The dataset to be parsed.
	 * @return A wrapper object containing the contents of the dataset
	 * @throws IllegalArgumentException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IOException
	 */
	protected List<DirectedSparseMultigraph<String, String>> parse()
			throws IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException {
		List<DirectedSparseMultigraph<String, String>> result = new ArrayList<DirectedSparseMultigraph<String, String>>();
		for (String group : groups) {
			List<Triple<String, DataSet, String>> dataSets = groupToDataSet
					.get(group);
			DataSetFormatParser parser = null;
			Set<String> optionalInputs = null;
			List<DataSet> inputs = new ArrayList<DataSet>();
			// find the parsable dataset
			for (Triple<String, DataSet, String> triple : dataSets) {
				if (triple.getSecond().getDataSetFormat() instanceof Parsable) {
					parser = triple.getSecond().getDataSetFormat()
							.getDataSetFormatParser();
					optionalInputs = new HashSet<String>(
							Arrays.asList(((Parsable) triple.getSecond()
									.getDataSetFormat()).optionalInputs()));
					inputs.add(triple.getSecond());
					break;
				}
			}
			if (parser == null || optionalInputs == null)
				throw new IllegalArgumentException(
						"No parser found to parse the datasetconfig");
			// find optional inputs
			for (Triple<String, DataSet, String> triple : dataSets) {
				if (optionalInputs.contains(triple.getSecond()
						.getDataSetFormat().getClass().getSimpleName()))
					inputs.add(triple.getSecond());
			}

			result.add(parser.parse(inputs));
		}
		return result;
	}
}
