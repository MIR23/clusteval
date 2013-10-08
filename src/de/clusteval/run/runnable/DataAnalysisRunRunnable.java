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
package de.clusteval.run.runnable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import de.clusteval.data.DataConfig;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.data.statistics.DataStatisticCalculator;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.run.DataAnalysisRun;
import de.clusteval.run.Run;
import de.clusteval.run.result.DataAnalysisRunResult;
import de.clusteval.utils.StatisticCalculator;
import file.FileUtils;

/**
 * A type of analysis runnable, that corresponds to {@link DataAnalysisRun} and
 * is responsible for analysing a data configuration (dataset and goldstandard).
 * 
 * @author Christian Wiwie
 * 
 */
public class DataAnalysisRunRunnable
		extends
			AnalysisRunRunnable<DataStatistic, DataAnalysisRunResult> {

	/**
	 * The data configuration to be analysed by this runnable.
	 */
	protected DataConfig dataConfig;

	/**
	 * @param runScheduler
	 *            The run scheduler that the newly created runnable should be
	 *            passed to and executed by.
	 * 
	 * @param run
	 *            The run this runnable belongs to.
	 * @param runIdentString
	 *            The unique identification string of the run which is used to
	 *            store the results in a unique folder to avoid overwriting.
	 * @param dataConfig
	 *            The data configuration to be analysed by this runnable.
	 * @param statistics
	 *            The statistics that should be assessed during execution of
	 *            this runnable.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public DataAnalysisRunRunnable(RunSchedulerThread runScheduler, Run run,
			String runIdentString, final boolean isResume,
			DataConfig dataConfig, List<DataStatistic> statistics) {
		super(run, runIdentString, statistics, isResume);
		this.dataConfig = dataConfig;
		this.future = runScheduler.registerRunRunnable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#createRunResult()
	 */
	@Override
	protected DataAnalysisRunResult createRunResult() throws RegisterException {
		return new DataAnalysisRunResult(this.getRun().getRepository(),
				System.currentTimeMillis(), new File(analysesFolder),
				this.runThreadIdentString, run);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#getOutputPath()
	 */
	@Override
	protected String getOutputPath() {
		return FileUtils.buildPath(analysesFolder, dataConfig + "_"
				+ currentStatistic.getClass().getSimpleName() + ".txt");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * run.runnable.AnalysisRunRunnable#getStatisticCalculator(java.lang.Class)
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected StatisticCalculator<DataStatistic> getStatisticCalculator()
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {

		Class<? extends DataStatisticCalculator> calcClass = run
				.getRepository().getDataStatisticCalculator(
						currentStatistic.getClass().getName());
		Constructor<? extends DataStatisticCalculator> constr = calcClass
				.getConstructor(Repository.class, long.class, File.class,
						DataConfig.class);
		DataStatisticCalculator calc = constr.newInstance(repo,
				calcFile.lastModified(), calcFile, this.dataConfig);
		return calc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#beforeStatisticCalculate()
	 */
	@Override
	protected void beforeStatisticCalculate() {
		this.log.info("Run " + this.getRun() + " - (" + dataConfig
				+ ") Analysing " + currentStatistic.getIdentifier());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#afterRun()
	 */
	@Override
	public void afterRun() {
		super.afterRun();
		result.put(this.dataConfig, results);
		this.getRun().getResults().add(result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.run.runnable.AnalysisRunRunnable#beforeRun()
	 */
	@Override
	public void beforeRun() {
		super.beforeRun();

		// TODO: no conversion implemented yet
		// DataSet current = this.dataConfig.getDatasetConfig().getDataSet();
		//
		// DataSet ds;
		// try {
		// ds = current.preprocessAndConvertTo(this.run.getContext(), this.run
		// .getContext().getStandardInputFormat());
		//
		// this.dataConfig = new DataConfig(this.dataConfig);
		// this.dataConfig.getDatasetConfig().setDataSet(ds);
		// } catch (Exception e) {
		// throw new IllegalArgumentException(
		// "The given data configuration could not be converted.");
		// }
	}
}
