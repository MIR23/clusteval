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
package de.clusteval.run.result;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.ClusteringRun;
import de.clusteval.run.DataAnalysisRun;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.Run;
import de.clusteval.run.RunAnalysisRun;
import de.clusteval.run.RunDataAnalysisRun;
import de.clusteval.run.RunException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;

/**
 * A wrapper class for runresults produced by runs of the framework.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class RunResult extends RepositoryObject {

	/**
	 * @param parentRepository
	 * @param runResultFolder
	 * @param result
	 * @param parseClusterings
	 * @param storeClusterings
	 * @return A runresult object for the given runresult folder.
	 * @throws IOException
	 * @throws UnknownRunResultFormatException
	 * @throws UnknownDataSetFormatException
	 * @throws UnknownQualityMeasureException
	 * @throws InvalidRunModeException
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws NoOptimizableProgramParameterException
	 * @throws UnknownProgramParameterException
	 * @throws UnknownGoldStandardFormatException
	 * @throws InvalidConfigurationFileException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws DataConfigNotFoundException
	 * @throws RunException
	 * @throws UnknownDataStatisticException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
	 * @throws UnknownRunStatisticException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws ConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws NumberFormatException
	 * @throws UnknownRunDataStatisticException
	 * @throws RunResultParseException
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownContextException
	 * @throws IncompatibleContextException
	 * @throws UnknownParameterType
	 */
	public static Run parseFromRunResultFolder(
			final Repository parentRepository, final File runResultFolder,
			final List<ExecutionRunResult> result,
			final boolean parseClusterings, final boolean storeClusterings)
			throws IOException, UnknownRunResultFormatException,
			UnknownDataSetFormatException, UnknownQualityMeasureException,
			InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException,
			UnknownGoldStandardFormatException,
			InvalidConfigurationFileException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			InvalidOptimizationParameterException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType {

		Logger log = LoggerFactory.getLogger(RunResult.class);
		log.debug("Parsing run result from '" + runResultFolder + "'");
		Repository childRepository = Repository
				.getRepositoryForExactPath(runResultFolder.getAbsolutePath());
		if (childRepository == null) {
			childRepository = new RunResultRepository(
					runResultFolder.getAbsolutePath(), parentRepository);
		}
		childRepository.initialize();

		File runFile = null;
		File configFolder = new File(FileUtils.buildPath(
				runResultFolder.getAbsolutePath(), "configs"));
		if (!configFolder.exists())
			return null;
		for (File child : configFolder.listFiles())
			if (child.getName().endsWith(".run")) {
				runFile = child;
				break;
			}
		if (runFile == null)
			return null;
		final Run run = Run.parseFromFile(runFile);

		if (run instanceof ClusteringRun) {
			return GraphMatchingRunResult.parseFromRunResultFolder(
					(ClusteringRun) run, childRepository, runResultFolder,
					result);
		} else if (run instanceof ParameterOptimizationRun) {
			return ParameterOptimizationResult.parseFromRunResultFolder(
					(ParameterOptimizationRun) run, childRepository,
					runResultFolder, result, parseClusterings,
					storeClusterings, true);
		} else if (run instanceof DataAnalysisRun) {
			DataAnalysisRunResult.parseFromRunResultFolder(
					(DataAnalysisRun) run, childRepository, runResultFolder);
			return run;
		} else if (run instanceof RunDataAnalysisRun) {
			RunDataAnalysisRunResult.parseFromRunResultFolder(
					(RunDataAnalysisRun) run, childRepository, runResultFolder);
			return run;
		} else if (run instanceof RunAnalysisRun) {
			RunAnalysisRunResult.parseFromRunResultFolder((RunAnalysisRun) run,
					childRepository, runResultFolder);
			return run;
		}
		return run;
	}

	/** The run ident string. */
	protected String runIdentString;

	protected Run run;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @throws RegisterException
	 */
	public RunResult(Repository repository, long changeDate, File absPath,
			final String runIdentString, final Run run)
			throws RegisterException {
		super(repository, false, changeDate, absPath);
		this.runIdentString = runIdentString;
		this.run = run;
	}

	/**
	 * The copy constructor of run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunResult(final RunResult other) throws RegisterException {
		super(other);
		this.runIdentString = other.runIdentString;
		this.run = other.run.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public abstract RunResult clone();

	/**
	 * @return The unique identifier of this runresult, equal to the name of the
	 *         runresult folder.
	 */
	public String getIdentifier() {
		return this.runIdentString;
	}

	/**
	 * @return The run this runresult belongs to.
	 */
	public Run getRun() {
		return this.run;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return this.repository.getParent().register(this);
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

	/**
	 * This method loads the contents of this run result into the memory by
	 * parsing the files on the filesystem.
	 * 
	 * <p>
	 * The run result might consume a lot of memory afterwards. Only invoke this
	 * method, if you really need access to the run results contents and
	 * afterwards free the contents by invoking {@link #unloadFromMemory()}.
	 * 
	 * @throws RunResultParseException
	 */
	public abstract void loadIntoMemory() throws RunResultParseException;

	/**
	 * This method unloads the contents of this run result from the memory and
	 * releases the reserved memory. This can be helpful especially for large
	 * parameter optimization run results.
	 */
	public abstract void unloadFromMemory();

}
