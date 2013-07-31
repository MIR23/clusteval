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

import utils.Pair;
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
import de.clusteval.run.RunDataAnalysisRun;
import de.clusteval.run.RunException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.RunDataStatistic;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.Statistic;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class RunDataAnalysisRunResult extends
		AnalysisRunResult<Pair<List<String>, List<String>>, RunDataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @throws RegisterException
	 */
	public RunDataAnalysisRunResult(Repository repository, long changeDate,
			File absPath, String runIdentString, final Run run)
			throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run);
	}

	/**
	 * The copy constructor for run data analysis run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunDataAnalysisRunResult(final RunDataAnalysisRunResult other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#cloneStatistics(java.util.Map)
	 */
	@Override
	protected Map<Pair<List<String>, List<String>>, List<RunDataStatistic>> cloneStatistics(
			Map<Pair<List<String>, List<String>>, List<RunDataStatistic>> statistics) {
		final Map<Pair<List<String>, List<String>>, List<RunDataStatistic>> result = new HashMap<Pair<List<String>, List<String>>, List<RunDataStatistic>>();

		for (Map.Entry<Pair<List<String>, List<String>>, List<RunDataStatistic>> entry : statistics
				.entrySet()) {
			List<RunDataStatistic> newList = new ArrayList<RunDataStatistic>();

			for (RunDataStatistic elem : entry.getValue()) {
				newList.add(elem.clone());
			}
			Pair<List<String>, List<String>> oldPair = entry.getKey();
			result.put(new Pair<List<String>, List<String>>(oldPair.getFirst(),
					oldPair.getSecond()), newList);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#clone()
	 */
	@Override
	public RunDataAnalysisRunResult clone() {
		try {
			return new RunDataAnalysisRunResult(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#getRun()
	 */
	@Override
	public RunDataAnalysisRun getRun() {
		return (RunDataAnalysisRun) super.getRun();
	}

	/**
	 * @param parentRepository
	 * @param runResultFolder
	 * @return The run-data analysis runresult parsed from the given runresult
	 *         folder.
	 * 
	 * @throws RepositoryAlreadyExistsException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
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
	 * @throws UnknownRunStatisticException
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
	public static RunDataAnalysisRunResult parseFromRunResultFolder(
			final Repository parentRepository, final File runResultFolder)
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
				runResultFolder.getAbsolutePath(), parentRepository);
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

		RunDataAnalysisRunResult analysisResult = null;

		if (run instanceof RunDataAnalysisRun) {
			final RunDataAnalysisRun runDataRun = (RunDataAnalysisRun) run;

			File analysesFolder = new File(FileUtils.buildPath(
					runResultFolder.getAbsolutePath(), "analyses"));

			analysisResult = new RunDataAnalysisRunResult(parentRepository,
					analysesFolder.lastModified(), analysesFolder,
					analysesFolder.getParentFile().getName(), runDataRun);

			analysisResult.loadIntoMemory();
			analysisResult.register();
			analysisResult.unloadFromMemory();
		}
		return analysisResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#loadIntoMemory()
	 */
	@Override
	public void loadIntoMemory() throws RunResultParseException {

		List<RunDataStatistic> statistics = new ArrayList<RunDataStatistic>();
		for (final Statistic runDataStatistic : this.getRun().getStatistics()) {
			final File completeFile = new File(FileUtils.buildPath(
					absPath.getAbsolutePath(), runDataStatistic.getIdentifier()
							+ ".txt"));
			if (!completeFile.exists())
				throw new RunResultParseException("The result file of ("
						+ runDataStatistic.getIdentifier()
						+ ") could not be found: " + completeFile);
			final String fileContents = FileUtils
					.readStringFromFile(completeFile.getAbsolutePath());

			runDataStatistic.parseFromString(fileContents);
			statistics.add((RunDataStatistic) runDataStatistic);

		}
		this.put(Pair.getPair(this.getRun()
				.getUniqueRunAnalysisRunIdentifiers(), this.getRun()
				.getUniqueDataAnalysisRunIdentifiers()), statistics);
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
	 * @return A set with all pairs of identifiers of run analysis runresults
	 *         and data analysis runresults.
	 */
	public Set<Pair<List<String>, List<String>>> getUniqueIdentifierPairs() {
		return this.statistics.keySet();
	}

	/**
	 * @param uniqueRunIdentifierPair
	 *            A pair with identifier of run analysis runresult and data
	 *            analysis runresult for which we want to know which run-data
	 *            statistics were evaluated.
	 * @return A list with all run-data statistics that were evaluated for the
	 *         given pair.
	 */
	public List<RunDataStatistic> getRunDataStatistics(
			final Pair<List<String>, List<String>> uniqueRunIdentifierPair) {
		return this.statistics.get(uniqueRunIdentifierPair);
	}

	/**
	 * @param run
	 *            The run corresponding to the given runresult folder.
	 * @param repository
	 *            The repository in which we want to register the parsed
	 *            runresult.
	 * @param runResultFolder
	 *            The folder containing the runresult.
	 * @return The run-data analysis runresult parsed from the given runresult
	 *         folder.
	 * @throws RunResultParseException
	 * @throws RegisterException
	 * 
	 */
	public static RunDataAnalysisRunResult parseFromRunResultFolder(
			final RunDataAnalysisRun run, final Repository repository,
			final File runResultFolder) throws RunResultParseException,
			RegisterException {

		RunDataAnalysisRunResult analysisResult = null;

		File analysesFolder = new File(FileUtils.buildPath(
				runResultFolder.getAbsolutePath(), "analyses"));

		analysisResult = new RunDataAnalysisRunResult(repository,
				analysesFolder.lastModified(), analysesFolder, analysesFolder
						.getParentFile().getName(), run);

		List<RunDataStatistic> statistics = new ArrayList<RunDataStatistic>();
		for (final Statistic runDataStatistic : run.getStatistics()) {
			final File completeFile = new File(FileUtils.buildPath(
					analysesFolder.getAbsolutePath(),
					runDataStatistic.getIdentifier() + ".txt"));
			if (!completeFile.exists())
				throw new RunResultParseException("The result file of ("
						+ runDataStatistic.getIdentifier()
						+ ") could not be found: " + completeFile);
			final String fileContents = FileUtils
					.readStringFromFile(completeFile.getAbsolutePath());

			runDataStatistic.parseFromString(fileContents);
			statistics.add((RunDataStatistic) runDataStatistic);

		}
		analysisResult.put(
				Pair.getPair(run.getUniqueRunAnalysisRunIdentifiers(),
						run.getUniqueDataAnalysisRunIdentifiers()), statistics);

		analysisResult.register();
		return analysisResult;
	}
}
