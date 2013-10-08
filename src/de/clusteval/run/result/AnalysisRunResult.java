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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.run.Run;
import de.clusteval.utils.Statistic;


/**
 * @author Christian Wiwie
 * @param <S>
 *            Type of the objects, on which the statistics are applied.
 * @param <T>
 *            Type of the Statistics
 * 
 */
public abstract class AnalysisRunResult<S extends Object, T extends Statistic>
		extends
			RunResult {

	protected Map<S, List<T>> statistics;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @throws RegisterException
	 */
	public AnalysisRunResult(Repository repository, long changeDate,
			File absPath, String runIdentString, final Run run)
			throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run);
		this.statistics = new HashMap<S, List<T>>();
	}

	/**
	 * The copy constructor for analysis run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public AnalysisRunResult(final AnalysisRunResult<S, T> other)
			throws RegisterException {
		super(other);
		this.statistics = cloneStatistics(other.statistics);
	}

	/**
	 * A helper method for cloning a map of statistics.
	 * 
	 * @param statistics
	 *            The map to clone.
	 * @return The cloned map.
	 */
	protected abstract Map<S, List<T>> cloneStatistics(
			Map<S, List<T>> statistics);

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.RunResult#clone()
	 */
	@Override
	public abstract AnalysisRunResult<S, T> clone();

	/**
	 * @param object
	 * @param statistics
	 */
	public void put(final S object, final List<T> statistics) {
		this.statistics.put(object, statistics);
	}
}
