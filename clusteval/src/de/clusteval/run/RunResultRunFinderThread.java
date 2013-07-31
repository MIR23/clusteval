/**
 * 
 */
package de.clusteval.run;

import de.clusteval.paramOptimization.ParameterOptimizationMethodFinderThread;
import de.clusteval.context.ContextFinderThread;
import de.clusteval.data.dataset.RunResultDataSetConfigFinderThread;
import de.clusteval.data.goldstandard.GoldStandardConfigFinderThread;
import de.clusteval.data.statistics.DataStatisticFinderThread;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.program.ProgramConfigFinderThread;

/**
 * A thread that uses a {@link RunFinder} to check the runresult repository for
 * new runs.
 * 
 * @author Christian Wiwie
 * 
 */
public class RunResultRunFinderThread extends RunFinderThread {

	/**
	 * @param supervisorThread
	 * @param repository
	 *            The repository to check for new runs.
	 * @param checkOnce
	 *            If true, this thread only checks once for new runs.
	 * 
	 */
	public RunResultRunFinderThread(final SupervisorThread supervisorThread,
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
	public RunResultRunFinderThread(final SupervisorThread supervisorThread,
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
			this.supervisorThread.getThread(RunResultDataSetConfigFinderThread.class)
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
}