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

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.run.Run;
import de.clusteval.run.RunAnalysisRun;
import de.clusteval.run.result.RunAnalysisRunResult;
import de.clusteval.run.statistics.RunStatistic;
import de.clusteval.run.statistics.RunStatisticCalculator;
import de.clusteval.utils.StatisticCalculator;

import file.FileUtils;

/**
 * A type of analysis runnable, that corresponds to {@link RunAnalysisRun} and
 * is responsible for analysing a run result.
 * 
 * @author Christian Wiwie
 * 
 */
public class RunAnalysisRunRunnable
		extends
			AnalysisRunRunnable<RunStatistic, RunAnalysisRunResult> {

	/**
	 * The unique identifier of a run result run identifier.
	 */
	protected String uniqueRunAnalysisRunIdentifier;

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
	 * @param uniqueRunIdentifier
	 *            The unique identifier of a run result run identifier.
	 * @param statistics
	 *            The statistics that should be assessed during execution of
	 *            this runnable.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public RunAnalysisRunRunnable(RunSchedulerThread runScheduler, Run run,
			String runIdentString, final boolean isResume,
			String uniqueRunIdentifier, List<RunStatistic> statistics) {
		super(run, runIdentString, statistics, isResume);
		this.uniqueRunAnalysisRunIdentifier = uniqueRunIdentifier;
		this.future = runScheduler.registerRunRunnable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#beforeStatisticCalculate()
	 */
	@Override
	protected void beforeStatisticCalculate() {
		this.log.info("Run " + this.getRun() + " - Analysing "
				+ currentStatistic.getIdentifier());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#createRunResult()
	 */
	@Override
	protected RunAnalysisRunResult createRunResult() throws RegisterException {
		return new RunAnalysisRunResult(this.getRun().getRepository(), true,
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
		return FileUtils.buildPath(
				analysesFolder,
				uniqueRunAnalysisRunIdentifier + "_"
						+ currentStatistic.getIdentifier() + ".txt");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#getStatisticCalculator()
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected StatisticCalculator<RunStatistic> getStatisticCalculator()
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Class<? extends RunStatisticCalculator> calcClass = run.getRepository()
				.getRunStatisticCalculator(
						currentStatistic.getClass().getName());
		Constructor<? extends RunStatisticCalculator> constr = calcClass
				.getConstructor(Repository.class, long.class, File.class,
						String.class);
		RunStatisticCalculator calc = constr.newInstance(repo,
				calcFile.lastModified(), calcFile,
				this.uniqueRunAnalysisRunIdentifier);
		return calc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.AnalysisRunRunnable#afterRun()
	 */
	@Override
	public void afterRun() {
		super.afterRun();
		result.put(this.runThreadIdentString, results);
		this.getRun().getResults().add(result);
	}
}
