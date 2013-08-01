/**
 * 
 */
package de.clusteval.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.rosuda.REngine.REngineException;

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
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.IncompatibleDataConfigDataStatisticException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.RunException;
import de.clusteval.run.result.AnalysisRunResultException;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;

/**
 * Together with every statistic class comes a calculator class, which is a
 * factory class for the corresponding statistic. The calculator has a
 * {@link #calculate()} method, which calculates, stores and returns a statistic
 * object.
 * 
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class StatisticCalculator<T extends Statistic>
		extends
			RepositoryObject {

	/**
	 * This attribute holds the statistic, after {@link #calculate()} has been
	 * invoked.
	 */
	protected T lastResult;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public StatisticCalculator(Repository repository, long changeDate,
			File absPath) throws RegisterException {
		super(repository, true, changeDate, absPath);
	}

	/**
	 * The copy constructor of statistic calculators
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public StatisticCalculator(final StatisticCalculator<T> other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public abstract StatisticCalculator<T> clone();

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
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

	/**
	 * Calculate the result. This method stores the calculated result in the
	 * {@link #lastResult} attribute for later usage, e.g. in
	 * {@link #writeOutputTo(File)}.
	 * 
	 * @return The calculated statistic.
	 * @throws IncompatibleDataConfigDataStatisticException
	 * @throws UnknownGoldStandardFormatException
	 * @throws UnknownDataSetFormatException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws ConfigurationException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws DataConfigNotFoundException
	 * @throws UnknownRunResultFormatException
	 * @throws UnknownQualityMeasureException
	 * @throws InvalidRunModeException
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws NoOptimizableProgramParameterException
	 * @throws UnknownProgramParameterException
	 * @throws InternalAttributeException
	 * @throws InvalidConfigurationFileException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws RunException
	 * @throws UnknownDataStatisticException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
	 * @throws UnknownRunStatisticException
	 * @throws AnalysisRunResultException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws UnknownRunDataStatisticException
	 * @throws RunResultParseException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleContextException
	 * @throws UnknownContextException
	 * @throws REngineException
	 * @throws RNotAvailableException
	 */
	public T calculate() throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, ConfigurationException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, UnknownRunResultFormatException,
			UnknownQualityMeasureException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException, InternalAttributeException,
			InvalidConfigurationFileException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			AnalysisRunResultException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			IncompatibleContextException, UnknownContextException,
			REngineException, RNotAvailableException {
		this.lastResult = calculateResult();
		return this.lastResult;
	}

	/**
	 * Overwrite this method in subclasses to provide your own statistic
	 * calculator type.
	 * 
	 * @return The calculated statistic.
	 * @throws IncompatibleDataConfigDataStatisticException
	 * @throws UnknownGoldStandardFormatException
	 * @throws UnknownDataSetFormatException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws ConfigurationException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws DataConfigNotFoundException
	 * @throws UnknownRunResultFormatException
	 * @throws UnknownQualityMeasureException
	 * @throws InvalidRunModeException
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws NoOptimizableProgramParameterException
	 * @throws UnknownProgramParameterException
	 * @throws InternalAttributeException
	 * @throws InvalidConfigurationFileException
	 * @throws RepositoryAlreadyExistsException
	 * @throws InvalidRepositoryException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws RunException
	 * @throws UnknownDataStatisticException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
	 * @throws UnknownRunStatisticException
	 * @throws AnalysisRunResultException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws UnknownRunDataStatisticException
	 * @throws RunResultParseException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleContextException
	 * @throws UnknownContextException
	 * @throws REngineException
	 * @throws RNotAvailableException
	 */
	protected abstract T calculateResult()
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, ConfigurationException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, UnknownRunResultFormatException,
			UnknownQualityMeasureException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException, InternalAttributeException,
			InvalidConfigurationFileException,
			RepositoryAlreadyExistsException, InvalidRepositoryException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			AnalysisRunResultException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException,
			RepositoryConfigNotFoundException,
			RepositoryConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			REngineException, RNotAvailableException;

	/**
	 * @param absFolderPath
	 *            The absolute path to the folder where the statistic should be
	 *            written to.
	 * @throws REngineException
	 * @throws RNotAvailableException
	 */
	public abstract void writeOutputTo(final File absFolderPath)
			throws REngineException, RNotAvailableException;

	/**
	 * @return The statistic calculated during the last {@link #calculate()}
	 *         invocation.
	 */
	public T getStatistic() {
		return this.lastResult;
	}
}
