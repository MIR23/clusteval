/**
 * 
 */
package de.clusteval.data.statistics;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.configuration.ConfigurationException;
import org.rosuda.REngine.REngineException;

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
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
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
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.RunException;
import de.clusteval.run.result.AnalysisRunResultException;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.StatisticCalculator;

/**
 * This class is parent class of all different kind of analyses on a DataConfig.
 * This analyses can be performed unrelated to clustering, since it only
 * requires the dataset (and optionally the goldstandard).
 * 
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class DataStatisticCalculator<T extends DataStatistic>
		extends
			StatisticCalculator<T> {

	protected DataConfig dataConfig;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public DataStatisticCalculator(Repository repository, long changeDate,
			File absPath, final DataConfig dataConfig) throws RegisterException {
		super(repository, changeDate, absPath);
		this.dataConfig = dataConfig;
	}

	/**
	 * The copy constructor of data statistic calculators.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DataStatisticCalculator(final DataStatisticCalculator<T> other)
			throws RegisterException {
		super(other);
		this.dataConfig = other.dataConfig.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DataStatisticCalculator<T> clone() {
		try {
			return this.getClass()
					.getConstructor(DataStatisticCalculator.class)
					.newInstance(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		this.log.warn("Cloning instance of class "
				+ this.getClass().getSimpleName() + " failed");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#calculate()
	 */
	@Override
	public T calculate() throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, ConfigurationException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, UnknownRunResultFormatException,
			UnknownClusteringQualityMeasureException, InvalidRunModeException,
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
			RepositoryConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			IncompatibleContextException, UnknownContextException,
			REngineException, RNotAvailableException {
		return super.calculate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#calculate()
	 */
	@Override
	protected abstract T calculateResult()
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			REngineException, RNotAvailableException;

	@Override
	public T getStatistic() {
		return super.getStatistic();
	}
}
