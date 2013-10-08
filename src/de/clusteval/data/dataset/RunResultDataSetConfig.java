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
package de.clusteval.data.dataset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.LoggerFactory;

import utils.Triple;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * @author Christian Wiwie
 * 
 */
public class RunResultDataSetConfig extends DataSetConfig {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param groups
	 * @param ds
	 * @throws RegisterException
	 */
	public RunResultDataSetConfig(Repository repository, long changeDate,
			File absPath, final List<String> groups,
			List<Triple<String, DataSet, String>> ds) throws RegisterException {
		super(repository, changeDate, absPath, groups, ds);
	}

	/**
	 * @param datasetConfig
	 * @throws RegisterException
	 */
	public RunResultDataSetConfig(DataSetConfig datasetConfig)
			throws RegisterException {
		super(datasetConfig);
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
	 * </ul>
	 * 
	 * @param absConfigPath
	 *            The absolute path to the dataset configuration file.
	 * @throws DataSetConfigurationException
	 * @throws NoRepositoryFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws RegisterException
	 * @throws NumberFormatException
	 * @return The dataset configuration object.
	 */
	public static DataSetConfig parseFromFile(final File absConfigPath)
			throws DataSetConfigurationException, NoRepositoryFoundException,
			DataSetConfigNotFoundException, RegisterException,
			NumberFormatException {

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

				// we take the dataset from the runresult repository
				DataSet dataSet = repo.getDataSetWithName(datasetName + "/"
						+ datasetFile);

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

}
