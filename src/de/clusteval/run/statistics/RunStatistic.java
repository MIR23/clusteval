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
package de.clusteval.run.statistics;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.run.RunAnalysisRun;
import de.clusteval.utils.Statistic;

/**
 * A run statistic is a type of {@link Statistic}, which corresponds to
 * properties of run results.
 * 
 * <p>
 * Run statistics correspond to {@link RunAnalysisRun} in the class hierarchy.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class RunStatistic extends Statistic {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RunStatistic(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of run statistics.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunStatistic(final RunStatistic other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final RunStatistic clone() {
		try {
			return this.getClass().getConstructor(this.getClass())
					.newInstance(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		this.log.warn("Cloning instance of class "
				+ this.getClass().getSimpleName() + " failed");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/**
	 * This method parses a string and maps it to a subclass of
	 * {@link RunStatistic} looking it up in the given repository.
	 * 
	 * @param repository
	 *            The repository to look for the classes.
	 * @param runStatistic
	 *            The string representation of a run statistic subclass.
	 * @return A subclass of {@link RunStatistic}.
	 * @throws UnknownRunStatisticException
	 */
	public static RunStatistic parseFromString(final Repository repository,
			String runStatistic) throws UnknownRunStatisticException {
		Class<? extends RunStatistic> c = repository
				.getRunStatisticClass("de.clusteval.run.statistics."
						+ runStatistic);

		try {
			RunStatistic statistic = c.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, false, System.currentTimeMillis(),
					new File(runStatistic));
			return statistic;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		throw new UnknownRunStatisticException("\"" + runStatistic
				+ "\" is not a known run statistic.");
	}

	/**
	 * This method parses several strings and maps them to subclasses of
	 * {@link RunStatistic} looking them up in the given repository.
	 * 
	 * @param repo
	 *            The repository to look for the classes.
	 * @param runStatistics
	 *            The string representation of a run statistic subclass.
	 * @return A subclass of {@link RunStatistic}.
	 * @throws UnknownRunStatisticException
	 */
	public static List<RunStatistic> parseFromString(final Repository repo,
			String[] runStatistics) throws UnknownRunStatisticException {
		List<RunStatistic> result = new LinkedList<RunStatistic>();
		for (String runStatistic : runStatistics) {
			result.add(parseFromString(repo, runStatistic));
		}
		return result;
	}
}
