/**
 * 
 */
package de.clusteval.run.runnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.rosuda.REngine.REngineException;

import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;

import utils.ProgressPrinter;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.data.statistics.IncompatibleDataConfigDataStatisticException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownClusteringQualityMeasureException;
import de.clusteval.run.AnalysisRun;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.Run;
import de.clusteval.run.RunException;
import de.clusteval.run.result.AnalysisRunResultException;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.FormatConversionException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.Statistic;
import de.clusteval.utils.StatisticCalculator;
import file.FileUtils;

/**
 * A type of a runnable, that corresponds to {@link AnalysisRun} and is
 * therefore responsible for analysing a object of interest. This can be for
 * example analysis of a dataset (in case of {@link DataAnalysisRunRunnable}) or
 * of run results ({@link RunAnalysisRunRunnable}).
 * 
 * @author Christian Wiwie
 * @param <T>
 *            A type of statistic, that should be assessed and stored during
 *            execution of this runnable, e.g. {@link DataStatistic}.
 * @param <R>
 *            A type of run result, that the results of this runnable will be
 *            of.
 * 
 */
public abstract class AnalysisRunRunnable<T extends Statistic, R extends RunResult>
		extends
			RunRunnable {

	/**
	 * A list of all statistic-classes that should be assessed during execution
	 * of this runnable.
	 */
	protected List<T> statistics;

	/**
	 * The results of this runnables are stored in a list. Every result is a
	 * single statistic object. A statistic object encapsulates its assessed
	 * value itself.
	 */
	protected List<T> results;

	/**
	 * The runresult object is a wrapper object that tells the framework, that
	 * the result folder of this runnable in the repository results directory
	 * (see {@link Repository#runResultBasePath}) holds a run result.
	 */
	protected R result;

	/**
	 * A temporary variable needed during execution of this runnable.
	 */
	protected T currentStatistic;

	/**
	 * A temporary variable needed during execution of this runnable.
	 */
	protected String analysesFolder;

	/**
	 * A temporary variable needed during execution of this runnable.
	 */
	protected File calcFile;

	/**
	 * A temporary variable needed during execution of this runnable.
	 */
	protected Repository repo;

	/**
	 * @param run
	 *            The run this runnable belongs to.
	 * @param runIdentString
	 *            The unique identification string of the run which is used to
	 *            store the results in a unique folder to avoid overwriting.
	 * @param statistics
	 *            The statistics that should be assessed during execution of
	 *            this runnable.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public AnalysisRunRunnable(final Run run, final String runIdentString,
			List<T> statistics, final boolean isResume) {
		super(run, runIdentString, isResume);
		this.statistics = statistics;
		this.progress = new ProgressPrinter(100, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.RunRunnable#beforeRun()
	 */
	@Override
	public void beforeRun() {
		if (!isResume)
			this.repo = this.getRun().getRepository();
		else
			this.repo = this.getRun().getRepository().getParent();

		new File(this.repo.getAnalysisResultsBasePath().replace(
				"%RUNIDENTSTRING", runThreadIdentString)).mkdirs();
		this.results = new ArrayList<T>();
		this.analysesFolder = FileUtils.buildPath(this.repo
				.getAnalysisResultsBasePath().replace("%RUNIDENTSTRING",
						runThreadIdentString));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.RunRunnable#afterRun()
	 */
	@Override
	public void afterRun() {
		try {
			this.result = createRunResult();
		} catch (RegisterException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.RunRunnable#doRun()
	 */
	@Override
	public void doRun() {
		int currentPos = 0;
		for (T statistic : statistics) {
			try {
				this.currentStatistic = statistic;
				String output = getOutputPath();

				final File outputFile = new File(output);
				if (isResume && outputFile.exists())
					continue;

				BufferedWriter bw;
				bw = new BufferedWriter(new FileWriter(outputFile));

				this.calcFile = new File(FileUtils.buildPath(
						repo.getRunStatisticBasePath(),
						statistic.getIdentifier() + ".jar")).getAbsoluteFile();

				StatisticCalculator<T> calc = getStatisticCalculator();

				this.beforeStatisticCalculate();
				results.add(calc.calculate());

				bw.write(results.get(results.size() - 1).toString());

				bw.close();

				calc.writeOutputTo(new File(output));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (FormatConversionException e) {
				e.printStackTrace();
			} catch (InvalidDataSetFormatVersionException e) {
				e.printStackTrace();
			} catch (RegisterException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			} catch (GoldStandardConfigurationException e) {
				e.printStackTrace();
			} catch (DataSetConfigurationException e) {
				e.printStackTrace();
			} catch (DataSetNotFoundException e) {
				e.printStackTrace();
			} catch (DataSetConfigNotFoundException e) {
				e.printStackTrace();
			} catch (GoldStandardConfigNotFoundException e) {
				e.printStackTrace();
			} catch (DataConfigurationException e) {
				e.printStackTrace();
			} catch (DataConfigNotFoundException e) {
				e.printStackTrace();
			} catch (IncompatibleDataConfigDataStatisticException e) {
				e.printStackTrace();
			} catch (UnknownGoldStandardFormatException e) {
				e.printStackTrace();
			} catch (UnknownDataSetFormatException e) {
				e.printStackTrace();
			} catch (UnknownRunResultFormatException e) {
				e.printStackTrace();
			} catch (UnknownClusteringQualityMeasureException e) {
				e.printStackTrace();
			} catch (InvalidRunModeException e) {
				e.printStackTrace();
			} catch (UnknownParameterOptimizationMethodException e) {
				e.printStackTrace();
			} catch (NoOptimizableProgramParameterException e) {
				e.printStackTrace();
			} catch (UnknownProgramParameterException e) {
				e.printStackTrace();
			} catch (InternalAttributeException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationFileException e) {
				e.printStackTrace();
			} catch (RepositoryAlreadyExistsException e) {
				e.printStackTrace();
			} catch (InvalidRepositoryException e) {
				e.printStackTrace();
			} catch (NoRepositoryFoundException e) {
				e.printStackTrace();
			} catch (GoldStandardNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidOptimizationParameterException e) {
				e.printStackTrace();
			} catch (RunException e) {
				e.printStackTrace();
			} catch (UnknownDataStatisticException e) {
				e.printStackTrace();
			} catch (UnknownProgramTypeException e) {
				e.printStackTrace();
			} catch (UnknownRProgramException e) {
				e.printStackTrace();
			} catch (IncompatibleParameterOptimizationMethodException e) {
				e.printStackTrace();
			} catch (UnknownDistanceMeasureException e) {
				e.printStackTrace();
			} catch (UnknownRunStatisticException e) {
				e.printStackTrace();
			} catch (AnalysisRunResultException e) {
				e.printStackTrace();
			} catch (RepositoryConfigNotFoundException e) {
				e.printStackTrace();
			} catch (RepositoryConfigurationException e) {
				e.printStackTrace();
			} catch (UnknownDataSetTypeException e) {
				e.printStackTrace();
			} catch (NoDataSetException e) {
				e.printStackTrace();
			} catch (UnknownRunDataStatisticException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (RunResultParseException e) {
				e.printStackTrace();
			} catch (UnknownDataPreprocessorException e) {
				e.printStackTrace();
			} catch (IncompatibleDataSetConfigPreprocessorException e) {
				e.printStackTrace();
			} catch (UnknownContextException e) {
				e.printStackTrace();
			} catch (IncompatibleContextException e) {
				e.printStackTrace();
			} catch (RNotAvailableException e) {
			} catch (REngineException e) {
			} finally {
				currentPos++;
				int iterationPercent = (int) (currentPos
						/ (double) this.statistics.size() * 100);
				this.progress.update(iterationPercent);
			}
		}
	}

	/**
	 * A helper method for {@link #doRun()}. It returns the absolute path to the
	 * result file for the current statistic to be calculated.
	 * 
	 * <p>
	 * This method is abstract since it has to provide different behaviour for
	 * different subtypes of this class.
	 * 
	 * @return Abstract path to the output file.
	 */
	protected abstract String getOutputPath();

	/**
	 * A helper method for {@link #doRun()} which returns the statistic
	 * calculator for the current statistic to be calculated.
	 * 
	 * <p>
	 * This method is abstract since it has to provide different behaviour for
	 * different subtypes of this class.
	 * 
	 * @return
	 * @throws FormatConversionException
	 * @throws IOException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws RegisterException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws UnknownDataSetFormatException
	 */
	protected abstract StatisticCalculator<T> getStatisticCalculator()
			throws FormatConversionException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, UnknownDataSetFormatException;

	/**
	 * A helper method of {@link #doRun()}, which can be overridden to do any
	 * kind of precalculations and operations needed before a statistic is
	 * assessed.
	 */
	protected abstract void beforeStatisticCalculate();

	/**
	 * This method creates a run result object encapsulating the results of this
	 * runnable which has the right subtype depending on the dynamic type of
	 * this class.
	 * 
	 * @return A runresult object encapsulating the results of this runnable.
	 * @throws RegisterException
	 */
	protected abstract R createRunResult() throws RegisterException;
}
