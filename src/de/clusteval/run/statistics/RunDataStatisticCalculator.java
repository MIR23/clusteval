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
package de.clusteval.run.statistics;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.InvalidConfigurationFileException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.StatisticCalculator;

/**
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class RunDataStatisticCalculator<T extends RunDataStatistic>
		extends
			StatisticCalculator<T> {

	/**
	 * This method parses a string and maps it to a subclass of
	 * {@link RunDataStatistic} looking it up in the given repository.
	 * 
	 * @param repository
	 *            The repository to look for the classes.
	 * @param runDataStatistic
	 *            The string representation of a run data statistic subclass.
	 * @return A subclass of {@link RunDataStatistic}.
	 * @throws UnknownRunDataStatisticException
	 */
	public static RunDataStatistic parseFromString(final Repository repository,
			String runDataStatistic) throws UnknownRunDataStatisticException {
		Class<? extends RunDataStatistic> c = repository
				.getRunDataStatisticClass("de.clusteval.run.statistics."
						+ runDataStatistic);

		try {
			RunDataStatistic statistic = c.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, true, System.currentTimeMillis(),
					new File(runDataStatistic));
			return statistic;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		throw new UnknownRunDataStatisticException("\"" + runDataStatistic
				+ "\" is not a known run data statistic.");
	}

	/**
	 * This method parses several strings and maps them to subclasses of
	 * {@link RunDataStatistic} looking them up in the given repository.
	 * 
	 * @param repo
	 *            The repository to look for the classes.
	 * @param runStatistics
	 *            The string representation of a run data statistic subclass.
	 * @return A subclass of {@link RunDataStatistic}.
	 * @throws UnknownRunDataStatisticException
	 */
	public static List<RunDataStatistic> parseFromString(final Repository repo,
			String[] runStatistics) throws UnknownRunDataStatisticException {
		List<RunDataStatistic> result = new LinkedList<RunDataStatistic>();
		for (String runStatistic : runStatistics) {
			result.add(parseFromString(repo, runStatistic));
		}
		return result;
	}

	protected List<String> uniqueRunIdentifiers;
	protected List<String> uniqueDataIdentifiers;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param uniqueRunIdentifiers
	 * @param uniqueDataIdentifiers
	 * @throws RegisterException
	 */
	public RunDataStatisticCalculator(Repository repository, long changeDate,
			File absPath, final List<String> uniqueRunIdentifiers,
			final List<String> uniqueDataIdentifiers) throws RegisterException {
		super(repository, changeDate, absPath);
		this.uniqueRunIdentifiers = uniqueRunIdentifiers;
		this.uniqueDataIdentifiers = uniqueDataIdentifiers;
	}

	/**
	 * The copy constructor of run data statistic calculators.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunDataStatisticCalculator(final RunDataStatisticCalculator<T> other)
			throws RegisterException {
		super(other);

		this.uniqueRunIdentifiers = new ArrayList<String>(
				other.uniqueRunIdentifiers);
		this.uniqueDataIdentifiers = new ArrayList<String>(
				other.uniqueDataIdentifiers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public RunDataStatisticCalculator<T> clone() {
		try {
			return this.getClass()
					.getConstructor(RunDataStatisticCalculator.class)
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
			RNotAvailableException, REngineException {
		return super.calculate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#calculateResult()
	 */
	@Override
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
			RepositoryConfigurationException, RegisterException,
			UnknownDataSetTypeException, NoDataSetException,
			UnknownRunDataStatisticException, RunResultParseException,
			REngineException, RNotAvailableException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.StatisticCalculator#getStatistic()
	 */
	@Override
	public abstract T getStatistic();
}
