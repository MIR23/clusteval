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
import de.clusteval.data.DataConfig;
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
import de.clusteval.data.statistics.DataStatistic;
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
import de.clusteval.run.DataAnalysisRun;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.Run;
import de.clusteval.run.RunException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.Statistic;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class DataAnalysisRunResult extends
		AnalysisRunResult<DataConfig, DataStatistic> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @throws RegisterException
	 */
	public DataAnalysisRunResult(Repository repository, long changeDate,
			File absPath, String runIdentString, final Run run)
			throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run);
	}

	/**
	 * The copy constructor for data analysis run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DataAnalysisRunResult(final DataAnalysisRunResult other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#cloneStatistics(java.util.Map)
	 */
	@Override
	protected Map<DataConfig, List<DataStatistic>> cloneStatistics(
			Map<DataConfig, List<DataStatistic>> statistics) {
		final Map<DataConfig, List<DataStatistic>> result = new HashMap<DataConfig, List<DataStatistic>>();

		for (Map.Entry<DataConfig, List<DataStatistic>> entry : statistics
				.entrySet()) {
			List<DataStatistic> newList = new ArrayList<DataStatistic>();

			for (DataStatistic elem : entry.getValue()) {
				newList.add(elem.clone());
			}

			result.put(entry.getKey().clone(), newList);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.AnalysisRunResult#clone()
	 */
	@Override
	public DataAnalysisRunResult clone() {
		try {
			return new DataAnalysisRunResult(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param parentRepository
	 * @param runResultFolder
	 * @return The data analysis run result parsed from the given runresult
	 *         folder.
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
	 * @throws AnalysisRunResultException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws ConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws NumberFormatException
	 * @throws UnknownRunDataStatisticException
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownContextException
	 * @throws IncompatibleContextException
	 */
	public static DataAnalysisRunResult parseFromRunResultFolder(
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
			UnknownGoldStandardFormatException, AnalysisRunResultException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException,
			UnknownRunDataStatisticException, UnknownDataPreprocessorException,
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
		final Run object = Run.parseFromFile(runFile);

		DataAnalysisRunResult analysisResult = null;

		if (object instanceof DataAnalysisRun) {
			final DataAnalysisRun run = (DataAnalysisRun) object;

			File analysesFolder = new File(FileUtils.buildPath(
					runResultFolder.getAbsolutePath(), "analyses"));

			analysisResult = new DataAnalysisRunResult(parentRepository,
					analysesFolder.lastModified(), analysesFolder,
					analysesFolder.getParentFile().getName(), run);

			for (final DataConfig dataConfig : run.getDataConfigs()) {

				List<DataStatistic> statistics = new ArrayList<DataStatistic>();
				for (final Statistic dataStatistic : run.getStatistics()) {
					final File completeFile = new File(FileUtils.buildPath(
							analysesFolder.getAbsolutePath(),
							dataConfig.toString() + "_"
									+ dataStatistic.getIdentifier() + ".txt"));
					if (!completeFile.exists())
						throw new AnalysisRunResultException(
								"The result file of (" + dataConfig + ","
										+ dataStatistic.getIdentifier()
										+ ") could not be found: "
										+ completeFile);
					final String fileContents = FileUtils
							.readStringFromFile(completeFile.getAbsolutePath());

					dataStatistic.parseFromString(fileContents);
					statistics.add((DataStatistic) dataStatistic);

				}
				analysisResult.put(dataConfig, statistics);
			}
			analysisResult.register();
		}
		return analysisResult;
	}

	/**
	 * @return The data configurations encapsulating the datasets that were
	 *         analysed.
	 */
	public Set<DataConfig> getDataConfigs() {
		return this.statistics.keySet();
	}

	/**
	 * @param dataConfig
	 *            The data configuration for which we want to know which data
	 *            statistics were evaluated.
	 * @return The data statistics that were assessed for the given data
	 *         configuration.
	 */
	public List<DataStatistic> getDataStatistics(final DataConfig dataConfig) {
		return this.statistics.get(dataConfig);
	}

	/**
	 * @param run
	 * @param parentRepository
	 * @param runResultFolder
	 * @return The data analysis run result parsed from the given runresult
	 *         folder.
	 * @throws RegisterException
	 * @throws RunResultParseException
	 * 
	 */
	public static DataAnalysisRunResult parseFromRunResultFolder(
			final DataAnalysisRun run, final Repository parentRepository,
			final File runResultFolder) throws RegisterException,
			RunResultParseException {

		DataAnalysisRunResult analysisResult = null;

		File analysesFolder = new File(FileUtils.buildPath(
				runResultFolder.getAbsolutePath(), "analyses"));

		analysisResult = new DataAnalysisRunResult(parentRepository,
				analysesFolder.lastModified(), analysesFolder, analysesFolder
						.getParentFile().getName(), run);

		analysisResult.loadIntoMemory();
		analysisResult.register();
		analysisResult.unloadFromMemory();
		return analysisResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#loadIntoMemory()
	 */
	@Override
	public void loadIntoMemory() throws RunResultParseException {

		for (final DataConfig dataConfig : getRun().getDataConfigs()) {

			List<DataStatistic> statistics = new ArrayList<DataStatistic>();
			for (final Statistic dataStatistic : getRun().getStatistics()) {
				final File completeFile = new File(FileUtils.buildPath(
						absPath.getAbsolutePath(), dataConfig.toString() + "_"
								+ dataStatistic.getIdentifier() + ".txt"));
				if (!completeFile.exists()) {
					throw new RunResultParseException("The result file of ("
							+ dataConfig + "," + dataStatistic.getIdentifier()
							+ ") could not be found: " + completeFile);
				}
				final String fileContents = FileUtils
						.readStringFromFile(completeFile.getAbsolutePath());

				dataStatistic.parseFromString(fileContents);
				statistics.add((DataStatistic) dataStatistic);

			}
			this.put(dataConfig, statistics);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#getRun()
	 */
	@Override
	public DataAnalysisRun getRun() {
		return (DataAnalysisRun) super.getRun();
	}
}
