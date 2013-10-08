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
package de.clusteval.run.result;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import utils.ArrayIterator;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.run.RUN_STATUS;
import de.clusteval.run.Run;
import de.clusteval.utils.FileFinder;

/**
 * @author Christian Wiwie
 * 
 */
public class RunResultFinder extends FileFinder {

	/**
	 * Instantiates a new run result finder.
	 * 
	 * @param repository
	 *            The repository to register the new run results at.
	 * @throws RegisterException
	 */
	public RunResultFinder(Repository repository) throws RegisterException {
		super(repository, System.currentTimeMillis(), new File(
				repository.getRunResultBasePath()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FileFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<? extends RepositoryObject> getRegisteredObjectSet() {
		return this.getRepository().getRunResults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FileFinder#parseObjectFromFile(java.io.File)
	 */
	@Override
	protected RepositoryObject parseObjectFromFile(File file) throws Exception {
		return RunResult.parseFromRunResultFolder(getRepository(), file,
				new ArrayList<ExecutionRunResult>(), false, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(this.getRepository().getRunResultBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getIterator()
	 */
	@Override
	protected Iterator<File> getIterator() {
		return new ArrayIterator<File>(this.getBaseDir().listFiles());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.isDirectory()
				&& (repository.getRegisteredRunResult(file.getName()) == null)
				&& !isRunning(file.getName());
	}

	protected boolean isRunning(final String uniqueRunIdentifier) {
		RunSchedulerThread runScheduler = repository.getSupervisorThread()
				.getRunScheduler();
		Collection<Run> runs = runScheduler.getRuns();
		for (Run run : runs) {
			if ((run.getStatus().equals(RUN_STATUS.RUNNING) || run.getStatus()
					.equals(RUN_STATUS.SCHEDULED))
					&& run.getRunIdentificationString() != null
					&& run.getRunIdentificationString().equals(
							uniqueRunIdentifier)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return RunResult.class;
	}

}
