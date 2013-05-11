/**
 * 
 */
package de.clusteval.framework;

/**
 * @author Christian Wiwie
 * 
 */
public class BackendServerConfig {

	protected int numberOfThreads;

	protected boolean checkForRunResults;

	/**
	 * 
	 */
	public BackendServerConfig() {
		super();

		// default values
		this.numberOfThreads = Math.max((int) (Runtime.getRuntime()
				.availableProcessors() / 2.0), 1);
		this.checkForRunResults = true;
	}

	/**
	 * @return The maximal number of threads.
	 */
	public int getNumberOfThreads() {
		return this.numberOfThreads;
	}

	/**
	 * 
	 * @return True, if this backend server should check for run results in its
	 *         repository.
	 */
	public boolean getCheckForRunResults() {
		return this.checkForRunResults;
	}

	/**
	 * @param checkForRunResults
	 *            True, if this backend server should check for run results in
	 *            its repository.
	 */
	public void setCheckForRunResults(final boolean checkForRunResults) {
		this.checkForRunResults = checkForRunResults;
	}
}
