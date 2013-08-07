package de.clusteval.run.result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;

import utils.Pair;
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
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.graphmatching.GraphMatching;
import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.ClusteringRun;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.Run;
import de.clusteval.run.RunException;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.RunResultFormatParser;
import de.clusteval.run.result.format.RunResultNotFoundException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;

/**
 * The Class ClusteringResult.
 * 
 * @author Christian Wiwie
 * 
 */
public class GraphMatchingRunResult extends ExecutionRunResult {

	/** The result format. */
	protected RunResultFormat resultFormat;

	protected Pair<ParameterSet, GraphMatching> graphmatching;

	/**
	 * Instantiates a new clustering result.
	 * 
	 * @param repository
	 *            the repository
	 * @param changeDate
	 * @param dataConfig
	 *            the data config
	 * @param programConfig
	 *            the program config
	 * @param resultFormat
	 *            the result format
	 * @param absPath
	 *            the abs file path
	 * @param runIdentString
	 *            the run ident string
	 * @param run
	 * @throws RegisterException
	 */
	public GraphMatchingRunResult(final Repository repository,
			final long changeDate, final File absPath,
			final DataConfig dataConfig, final ProgramConfig programConfig,
			final RunResultFormat resultFormat, final String runIdentString,
			final Run run) throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run, dataConfig,
				programConfig);

		this.resultFormat = resultFormat;
	}

	/**
	 * The copy constructor of run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public GraphMatchingRunResult(final GraphMatchingRunResult other)
			throws RegisterException {
		super(other);

		this.resultFormat = other.resultFormat.clone();
		this.graphmatching = Pair.getPair(other.graphmatching.getFirst(),
				other.graphmatching.getSecond());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.RunResult#clone()
	 */
	@Override
	public GraphMatchingRunResult clone() {
		try {
			return new GraphMatchingRunResult(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convert to.
	 * 
	 * @param format
	 *            the format
	 * @param internalParams
	 *            Internal parameters used to produced the clustering result
	 *            needed for parsing parameters.
	 * @param params
	 *            Parameters used to produced the clustering result needed for
	 *            parsing parameters.
	 * @return the clustering result
	 * @throws NoRunResultFormatParserException
	 *             the no run result format parser exception
	 * @throws RunResultNotFoundException
	 * @throws RegisterException
	 */
	@SuppressWarnings("unused")
	public GraphMatchingRunResult convertTo(final RunResultFormat format,
			final Map<String, String> internalParams,
			final Map<String, String> params)
			throws NoRunResultFormatParserException,
			RunResultNotFoundException, RegisterException {
		GraphMatchingRunResult result = null;
		RunResultFormatParser p = null;

		this.absPath = resultFormat.getRunResultFile(this.absPath);

		if (!this.absPath.exists())
			throw new RunResultNotFoundException("The result file "
					+ this.absPath.getAbsolutePath() + " does not exist!");

		/*
		 * We already have the same format
		 */
		if (this.getResultFormat().equals(format)) {
			/*
			 * Just copy the result file and return the corresponding
			 * ClusteringRunResult
			 */
			try {
				org.apache.commons.io.FileUtils.copyFile(
						new File(this.getAbsolutePath()),
						new File(this.getAbsolutePath() + ".conv"));
				return new GraphMatchingRunResult(this.repository,
						System.currentTimeMillis(), new File(
								this.absPath.getAbsolutePath() + ".conv"),
						this.dataConfig, this.programConfig, format,
						this.runIdentString, run);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			p = this.repository
					.getRunResultFormatParser(
							this.getResultFormat().getClass().getName())
					.getConstructor(Map.class, Map.class, String.class,
							DataConfig.class)
					.newInstance(internalParams, params,
							this.absPath.getAbsolutePath(), this.dataConfig);
			if (p != null) {
				p.convertToStandardFormat();
				result = new GraphMatchingRunResult(this.repository,
						System.currentTimeMillis(),
						new File(p.getOutputFile()), this.dataConfig,
						this.programConfig, format, this.runIdentString, run);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof FileNotFoundException) {
				/*
				 * Ensure, that all the files of this result are deleted
				 */
				FileUtils.delete(this.absPath);
				throw new RunResultNotFoundException(e.getCause().getMessage());
			}
			e.printStackTrace();
		} catch (IOException e) {
			/*
			 * Ensure, that all the files of this result are deleted
			 */
			FileUtils.delete(this.absPath);
			throw new RunResultNotFoundException(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * @return The clustering corresponding to this clustering run result.
	 */
	public Pair<ParameterSet, GraphMatching> getGraphMatching() {
		return this.graphmatching;
	}

	/**
	 * Gets the result format.
	 * 
	 * @return the result format
	 */
	public RunResultFormat getResultFormat() {
		return resultFormat;
	}

	/**
	 * Sets the result format.
	 * 
	 * @param resultFormat
	 *            the new result format
	 */
	public void setResultFormat(RunResultFormat resultFormat) {
		this.resultFormat = resultFormat;
	}

	/**
	 * @param run
	 *            The run corresponding to the runresult folder.
	 * @param repository
	 *            The repository in which we want to register the runresult.
	 * @param runResultFolder
	 *            A file object referencing the runresult folder.
	 * @param result
	 *            The list of runresults this method fills.
	 * @return The parameter optimization run parsed from the runresult folder.
	 * @throws RegisterException
	 */
	public static Run parseFromRunResultFolder(final ClusteringRun run,
			final Repository repository, final File runResultFolder,
			final List<ExecutionRunResult> result) throws RegisterException {

		File clusterFolder = new File(FileUtils.buildPath(
				runResultFolder.getAbsolutePath(), "clusters"));

		for (final DataConfig dataConfig : run.getDataConfigs()) {
			for (final ProgramConfig programConfig : run.getProgramConfigs()) {
				final File completeFile = new File(
						FileUtils.buildPath(clusterFolder.getAbsolutePath()))
						.listFiles(new FilenameFilter() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see java.io.FilenameFilter#accept(java.io.File,
							 * java.lang.String)
							 */
							@Override
							public boolean accept(File dir, String name) {
								return name.endsWith(".conv");
							}
						})[0];
				final GraphMatchingRunResult tmpResult = parseFromRunResultCompleteFile(
						repository, run, dataConfig, programConfig,
						completeFile);
				if (tmpResult != null)
					result.add(tmpResult);
			}
		}

		return run;
	}

	/**
	 * @param repository
	 * @param run
	 * @param dataConfig
	 * @param programConfig
	 * @param completeFile
	 * @return The parameter optimization run result parsed from the given
	 *         runresult folder.
	 * @throws RegisterException
	 */
	public static GraphMatchingRunResult parseFromRunResultCompleteFile(
			Repository repository, ClusteringRun run,
			final DataConfig dataConfig, final ProgramConfig programConfig,
			final File completeFile) throws RegisterException {
		GraphMatchingRunResult result = null;
		if (completeFile.exists()) {
			result = new GraphMatchingRunResult(repository,
					completeFile.lastModified(), completeFile, dataConfig,
					programConfig, programConfig.getOutputFormat(),
					completeFile.getParentFile().getParentFile().getName(), run);

			/*
			 * Register after parsing
			 */
			result.loadIntoMemory();
			result.register();
			result.unloadFromMemory();

		}
		return result;
	}

	/**
	 * @param parentRepository
	 * @param runResultFolder
	 * @param result
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
	 * @throws InvalidRepositoryException
	 * @throws RepositoryAlreadyExistsException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws DataConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws RunException
	 * @throws UnknownDataStatisticException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
	 * @throws UnknownRunStatisticException
	 * @return The parameter optimization run result parsed from the given
	 *         runresult folder.
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
	 * @throws UnknownParameterType
	 */
	public static Run parseFromRunResultFolder(
			final Repository parentRepository, final File runResultFolder,
			final List<ExecutionRunResult> result) throws IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			UnknownQualityMeasureException, InvalidRunModeException,
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
			UnknownRunDataStatisticException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType {

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

		if (run instanceof ClusteringRun) {
			final ClusteringRun paramRun = (ClusteringRun) run;

			File clusterFolder = new File(FileUtils.buildPath(
					runResultFolder.getAbsolutePath(), "clusters"));
			for (final DataConfig dataConfig : paramRun.getDataConfigs()) {
				for (final ProgramConfig programConfig : paramRun
						.getProgramConfigs()) {
					final File completeFile = new File(FileUtils.buildPath(
							clusterFolder.getAbsolutePath(),
							programConfig.toString() + "_" + dataConfig
									+ ".results.qual.complete"));
					final GraphMatchingRunResult tmpResult = parseFromRunResultCompleteFile(
							parentRepository, paramRun, dataConfig,
							programConfig, completeFile);
					if (tmpResult != null)
						result.add(tmpResult);
				}
			}
		}
		return run;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#loadIntoMemory()
	 */
	@Override
	public void loadIntoMemory() {
		if (absPath.exists()) {
			try {
				final Pair<ParameterSet, GraphMatching> pair = GraphMatching
						.parseFromFile(repository, absPath, true);

				ParameterSet paramSet = new ParameterSet();
				for (String param : pair.getFirst().keySet())
					paramSet.put(param, pair.getFirst().get(param));
				this.graphmatching = Pair.getPair(paramSet, pair.getSecond());
			} catch (Exception e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.result.RunResult#unloadFromMemory()
	 */
	@Override
	public void unloadFromMemory() {
		this.graphmatching = null;
	}
}