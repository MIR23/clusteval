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

import java.io.File;
import java.util.List;

import de.clusteval.context.Context;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.utils.Statistic;

/**
 * An abstract class for all run types, that involve conduction of any analyses.
 * This can involve analyses of data and/or clustering results.
 * 
 * <p>
 * An analysis run has a list of statistics in {@link #statistics}, that should
 * be assessed for the objects of analysis.
 * 
 * @author Christian Wiwie
 * @param <S>
 * 
 */
public abstract class AnalysisRun<S extends Statistic> extends Run {

	/**
	 * The statistics that should be assessed for the objects of analysis.
	 */
	protected List<S> statistics;

	/**
	 * @param repository
	 *            the repository
	 * @param context
	 * @param changeDate
	 *            The date this run was performed.
	 * @param absPath
	 *            The absolute path to the file on the filesystem that
	 *            corresponds to this run.
	 * @param statistics
	 *            The statistics that should be assessed for the objects of
	 *            analysis.
	 * @throws RegisterException
	 */
	public AnalysisRun(Repository repository, final Context context,
			long changeDate, File absPath, List<S> statistics)
			throws RegisterException {
		super(repository, context, changeDate, absPath);
		this.statistics = statistics;
	}

	/**
	 * Copy constructor for analysis runs.
	 * 
	 * @param other
	 *            The analysis run to be cloned.
	 * @throws RegisterException
	 */
	public AnalysisRun(AnalysisRun<S> other) throws RegisterException {
		super(other);

		this.statistics = cloneStatistics(other.statistics);
	}

	/**
	 * @param statistics
	 * @return
	 */
	protected abstract List<S> cloneStatistics(List<S> statistics);

	/**
	 * @return A list with all statistics that belong to this run.
	 * @see #statistics
	 */
	public List<S> getStatistics() {
		return this.statistics;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.config.Run#register()
	 */
	@Override
	public boolean register() throws RegisterException {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.Run#notify(framework.repository.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		super.notify(e);
		if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;

			if (this.statistics.contains(event.getRemovedObject())) {
				event.getRemovedObject().removeListener(this);
				this.log.info("Run " + this + ": Removed, because Statistic "
						+ event.getRemovedObject().getClass().getSimpleName()
						+ " has changed.");
				RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(this);
				this.unregister();
				this.notify(newEvent);
			}
		}
	}
}
