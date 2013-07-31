/**
 * 
 */
package de.clusteval.run.result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;

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
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownClusteringQualityMeasureException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.Run;
import de.clusteval.run.RunAnalysisRun;
import de.clusteval.run.RunException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.RunStatistic;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.Statistic;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class RunAnalysisRunResult extends
		AnalysisRunResult<String, RunStatistic> {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @throws RegisterException
	 */
	public RunAnalysisRunResult(Repository repository, boolean register,
			long changeDate, File absPath, String runIdentString, final Run run)
			throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run);

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for run analysis run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunAnalysisRunResult(final RunAnalysisRunResult other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#cloneStatistics(java.util.Map)
	 */
	@Override
	protected Map<String, List<RunStatistic>> cloneStatistics(
			Map<String, List<RunStatistic>> statistics) {
		final Map<String, List<RunStatistic>> result = new HashMap<String, List<RunStatistic>>();

		for (Map.Entry<String, List<RunStatistic>> entry : statistics
				.entrySet()) {
			List<RunStatistic> newList = new ArrayList<RunStatistic>();

			for (RunStatistic elem : entry.getValue()) {
				newList.add(elem.clone());
			}

			result.put(new String(entry.getKey()), newList);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#clone()
	 */
	@Override
	public RunAnalysisRunResult clone() {
		try {
			return new RunAnalysisRunResult(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param repository
	 *            The repository in which we want to register the parsed
	 *            runresult.
	 * @param runResultFolder
	 *            The runresult folder from which we want to parse the
	 *            runresult.
	 * @return The run analysis runresult parsed from the runresult folder.
	 * 
	 * @throws RepositoryAlreadyExistsException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws UnknownRunStatisticException
	 * @throws UnknownDistanceMeasureException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownRProgramException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownDataStatisticException
	 * @throws RunException
	 * @throws InvalidOptimizationParameterException
	 * @throws GoldStandardNotFoundException
	 * @throws NoRepositoryFoundException
	 * @throws UnknownProgramParameterException
	 * @throws NoOptimizableProgramParameterException
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws InvalidRunModeException
	 * @throws UnknownClusteringQualityMeasureException
	 * @throws InvalidConfigurationFileException
	 * @throws UnknownDataSetFormatException
	 * @throws UnknownRunResultFormatException
	 * @throws IOException
	 * @throws DataConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws GoldStandardConfigurationException
	 * @throws UnknownGoldStandardFormatException
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
	 */
	public static RunAnalysisRunResult parseFromRunResultFolder(
			final Repository repository, final File runResultFolder)
			throws RepositoryAlreadyExistsException,
			InvalidRepositoryException, GoldStandardConfigurationException,
			DataSetConfigurationException, DataSetNotFoundException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			InvalidConfigurationFileException,
			UnknownClusteringQualityMeasureException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException, NoRepositoryFoundException,
			GoldStandardNotFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			UnknownGoldStandardFormatException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException {
		Repository childRepository = new RunResultRepository(
				runResultFolder.getAbsolutePath(), repository);
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
		final Run object = Run.parseFromFile(runFile);

		RunAnalysisRunResult analysisResult = null;

		if (object instanceof RunAnalysisRun) {
			final RunAnalysisRun run = (RunAnalysisRun) object;

			File analysesFolder = new File(FileUtils.buildPath(
					runResultFolder.getAbsolutePath(), "analyses"));

			analysisResult = new RunAnalysisRunResult(repository, false,
					analysesFolder.lastModified(), analysesFolder,
					analysesFolder.getParentFile().getName(), run);

			analysisResult.loadIntoMemory();
			analysisResult.register();
			analysisResult.unloadFromMemory();
		}
		return analysisResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#getRun()
	 */
	@Override
	public RunAnalysisRun getRun() {
		return (RunAnalysisRun) super.getRun();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#loadIntoMemory()
	 */
	@Override
	public void loadIntoMemory() throws RunResultParseException {

		for (final String uniqueRunIdentifier : this.getRun()
				.getUniqueRunAnalysisRunIdentifiers()) {

			List<RunStatistic> statistics = new ArrayList<RunStatistic>();
			for (final Statistic runStatistic : this.getRun().getStatistics()) {
				final File completeFile = new File(FileUtils.buildPath(
						absPath.getAbsolutePath(), uniqueRunIdentifier + "_"
								+ runStatistic.getIdentifier() + ".txt"));
				if (!completeFile.exists())
					throw new RunResultParseException("The result file of ("
							+ uniqueRunIdentifier + ","
							+ runStatistic.getIdentifier()
							+ ") could not be found: " + completeFile);
				final String fileContents = FileUtils
						.readStringFromFile(completeFile.getAbsolutePath());

				runStatistic.parseFromString(fileContents);
				statistics.add((RunStatistic) runStatistic);

			}
			this.put(uniqueRunIdentifier, statistics);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#unloadFromMemory()
	 */
	@Override
	public void unloadFromMemory() {
		this.statistics.clear();
	}

	/**
	 * @return A set with all runresult identifiers, which the run analysed
	 *         which produced this runresult.
	 */
	public Set<String> getUniqueRunIdentifier() {
		return this.statistics.keySet();
	}

	/**
	 * @param uniqueRunIdentifier
	 *            The runresult identifier for which we want to know the
	 *            assessed run statistics.
	 * @return A list with all run statistics assessed for the given runresult
	 *         identifier.
	 */
	public List<RunStatistic> getRunStatistics(final String uniqueRunIdentifier) {
		return this.statistics.get(uniqueRunIdentifier);
	}

	/**
	 * @param run
	 *            The run analysis run corresponding to the given runresult
	 *            folder.
	 * @param repository
	 *            The repository in which we want to register the parsed
	 *            runresult.
	 * @param runResultFolder
	 *            A file object referencing the runresult folder.
	 * @return The run analysis runresult parsed from the given runresult
	 *         folder.
	 * @throws RunResultParseException
	 * @throws RegisterException
	 * 
	 */
	public static RunAnalysisRunResult parseFromRunResultFolder(
			final RunAnalysisRun run, final Repository repository,
			final File runResultFolder) throws RunResultParseException,
			RegisterException {

		RunAnalysisRunResult analysisResult = null;

		File analysesFolder = new File(FileUtils.buildPath(
				runResultFolder.getAbsolutePath(), "analyses"));

		analysisResult = new RunAnalysisRunResult(repository, false,
				analysesFolder.lastModified(), analysesFolder, analysesFolder
						.getParentFile().getName(), run);

		for (final String uniqueRunIdentifier : run
				.getUniqueRunAnalysisRunIdentifiers()) {

			List<RunStatistic> statistics = new ArrayList<RunStatistic>();
			for (final Statistic runStatistic : run.getStatistics()) {
				final File completeFile = new File(FileUtils.buildPath(
						analysesFolder.getAbsolutePath(), uniqueRunIdentifier
								+ "_" + runStatistic.getIdentifier() + ".txt"));
				if (!completeFile.exists())
					throw new RunResultParseException("The result file of ("
							+ uniqueRunIdentifier + ","
							+ runStatistic.getIdentifier()
							+ ") could not be found: " + completeFile);
				final String fileContents = FileUtils
						.readStringFromFile(completeFile.getAbsolutePath());

				runStatistic.parseFromString(fileContents);
				statistics.add((RunStatistic) runStatistic);

			}
			analysisResult.put(uniqueRunIdentifier, statistics);
		}

		analysisResult.register();
		return analysisResult;
	}
}
