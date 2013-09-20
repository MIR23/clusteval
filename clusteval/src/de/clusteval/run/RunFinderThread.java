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
package de.clusteval.run;

import de.clusteval.paramOptimization.ParameterOptimizationMethodFinderThread;
import de.clusteval.context.ContextFinderThread;
import de.clusteval.data.dataset.DataSetConfigFinderThread;
import de.clusteval.data.goldstandard.GoldStandardConfigFinderThread;
import de.clusteval.data.statistics.DataStatisticFinderThread;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.program.ProgramConfigFinderThread;
import de.clusteval.utils.Finder;
import de.clusteval.utils.FinderThread;

/**
 * A thread that uses a {@link RunFinder} to check the repository for new runs.
 * 
 * @author Christian Wiwie
 * 
 */
public class RunFinderThread extends FinderThread {

	/**
	 * @param supervisorThread
	 * @param repository
	 *            The repository to check for new runs.
	 * @param checkOnce
	 *            If true, this thread only checks once for new runs.
	 * 
	 */
	public RunFinderThread(final SupervisorThread supervisorThread,
			final Repository repository, final boolean checkOnce) {
		super(supervisorThread, repository, 30000, checkOnce);
	}

	/**
	 * @param supervisorThread
	 * @param repository
	 *            The repository to check for new runs.
	 * @param sleepTime
	 *            The time between two checks.
	 * @param checkOnce
	 *            If true, this thread only checks once for new runs.
	 * 
	 */
	public RunFinderThread(final SupervisorThread supervisorThread,
			final Repository repository, final long sleepTime,
			final boolean checkOnce) {
		super(supervisorThread, repository, sleepTime, checkOnce);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#beforeFind()
	 */
	@Override
	protected void beforeFind() {

		if (!this.repository.getDataSetConfigsInitialized())
			this.supervisorThread.getThread(DataSetConfigFinderThread.class)
					.waitFor();

		if (!this.repository.getGoldStandardConfigsInitialized())
			this.supervisorThread.getThread(
					GoldStandardConfigFinderThread.class).waitFor();

		if (!this.repository.getProgramConfigsInitialized())
			this.supervisorThread.getThread(ProgramConfigFinderThread.class)
					.waitFor();

		if (!this.repository.getDataStatisticsInitialized())
			this.supervisorThread.getThread(DataStatisticFinderThread.class)
					.waitFor();

		if (!this.repository.getParameterOptimizationMethodsInitialized())
			this.supervisorThread.getThread(
					ParameterOptimizationMethodFinderThread.class).waitFor();

		if (!this.repository.getContextsInitialized())
			this.supervisorThread.getThread(ContextFinderThread.class)
					.waitFor();

		this.log.debug("Checking for Runs...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#afterFind()
	 */
	@Override
	protected void afterFind() {
		this.repository.setRunsInitialized();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#getFinder()
	 */
	@Override
	protected Finder getFinder() throws RegisterException {
		return new RunFinder(repository);
	}
}
