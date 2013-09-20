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
package de.clusteval.data.preprocessing;

import java.io.File;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.SupervisorThread;
import de.clusteval.utils.Finder;
import de.clusteval.utils.FinderThread;

/**
 * @author Christian Wiwie
 * 
 */
public class DataPreprocessorFinderThread extends FinderThread {

	/**
	 * @param supervisorThread
	 * @param repository
	 * @param checkOnce
	 * 
	 */
	public DataPreprocessorFinderThread(
			final SupervisorThread supervisorThread,
			final Repository repository, final boolean checkOnce) {
		super(supervisorThread, repository, 30000, checkOnce);
	}

	/**
	 * @param supervisorThread
	 * @param framework
	 * @param sleepTime
	 * @param checkOnce
	 * 
	 */
	public DataPreprocessorFinderThread(
			final SupervisorThread supervisorThread,
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
		this.log.debug("Checking for new DataPreprocessor...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#afterFind()
	 */
	@Override
	protected void afterFind() {
		repository.setDataPreprocessorsInitialized();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FinderThread#getFinder()
	 */
	@Override
	protected Finder getFinder() throws RegisterException {
		return new DataPreprocessorFinder(repository,
				System.currentTimeMillis(), new File(
						repository.getDataPreprocessorBasePath()));
	}
}
