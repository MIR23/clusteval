/**
 * 
 */
package de.clusteval.paramOptimization;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.utils.Finder;
import de.clusteval.utils.FinderThread;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterOptimizationMethodFinderThread extends FinderThread {

	/**
	 * @param supervisorThread
	 * @param repository
	 *            The repository to check for new parameter optimization
	 *            methods.
	 * @param checkOnce
	 *            If true, this thread only checks once for new parameter
	 *            optimization methods.
	 * 
	 */
	public ParameterOptimizationMethodFinderThread(
			final SupervisorThread supervisorThread,
			final Repository repository, final boolean checkOnce) {
		super(supervisorThread, repository, 30000, checkOnce);
	}

	/**
	 * @param supervisorThread
	 * @param repository
	 *            The repository to check for new parameter optimization
	 *            methods.
	 * @param sleepTime
	 *            The time between two checks.
	 * @param checkOnce
	 *            If true, this thread only checks once for new parameter
	 *            optimization methods.
	 * 
	 */
	public ParameterOptimizationMethodFinderThread(
			final SupervisorThread supervisorThread,
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
		this.log.debug("Checking for parameter optimization methods...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#afterFind()
	 */
	@Override
	protected void afterFind() {
		repository.setParameterOptimizationMethodsInitialized();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#getFinder()
	 */
	@Override
	protected Finder getFinder() throws RegisterException {
		return new ParameterOptimizationMethodFinder(repository);
	}
}