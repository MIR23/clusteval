/**
 * 
 */
package de.clusteval.data.dataset;

import java.io.File;

import de.clusteval.data.dataset.format.DataSetFormatFinderThread;
import de.clusteval.data.dataset.type.DataSetTypeFinderThread;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.utils.Finder;
import de.clusteval.utils.FinderThread;

/**
 * @author Christian Wiwie
 * 
 */
public class DataSetFinderThread extends FinderThread {

	/**
	 * @param supervisorThread
	 * @param framework
	 * @param checkOnce
	 * 
	 */
	public DataSetFinderThread(final SupervisorThread supervisorThread,
			final Repository framework, final boolean checkOnce) {
		super(supervisorThread, framework, 30000, checkOnce);
	}

	/**
	 * @param supervisorThread
	 * @param framework
	 * @param sleepTime
	 * @param checkOnce
	 * 
	 */
	public DataSetFinderThread(final SupervisorThread supervisorThread,
			final Repository framework, final long sleepTime,
			final boolean checkOnce) {
		super(supervisorThread, framework, sleepTime, checkOnce);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#beforeFind()
	 */
	@Override
	protected void beforeFind() {

		if (!this.repository.getDataSetFormatsInitialized())
			this.supervisorThread.getThread(DataSetFormatFinderThread.class)
					.waitFor();

		if (!this.repository.getDataSetTypesInitialized())
			this.supervisorThread.getThread(DataSetTypeFinderThread.class)
					.waitFor();
		this.log.debug("Checking for Datasets...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#afterFind()
	 */
	@Override
	protected void afterFind() {
		this.repository.setDataSetInitialized();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#getFinder()
	 */
	@Override
	protected Finder getFinder() throws RegisterException {
		return new DataSetFinder(repository, System.currentTimeMillis(),
				new File(repository.getDataSetBasePath()));
	}
}