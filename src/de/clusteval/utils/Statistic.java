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
package de.clusteval.utils;

import java.io.File;
import java.util.Set;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;

/**
 * An abstract class representing a property of some object, that can be
 * assessed in analysis runs.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class Statistic extends RepositoryObject {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public Statistic(Repository repository, boolean register, long changeDate,
			File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of statistics.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public Statistic(final Statistic other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public abstract Statistic clone();

	/**
	 * The string returned by this method is used to represent this type of
	 * statistic throughout the framework (e.g. in the configuration files)
	 * 
	 * @return A string representing this statistic class.
	 */
	public final String getIdentifier() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

	/**
	 * Parses the values of a statistic from a string and stores them in the
	 * local attributes of this object.
	 * 
	 * @param contents
	 *            The string to parse the values from.
	 * 
	 */
	public abstract void parseFromString(final String contents);

	@Override
	public abstract String toString();

	/**
	 * This alias is used whenever this statistic is visually represented and a
	 * readable name is needed.
	 * 
	 * @return The alias of this statistic.
	 */
	public abstract String getAlias();

	/**
	 * @return The context of this statistic. A statistic can only be assessed
	 *         for runs of the right context.
	 */
	// TODO
//	public abstract Context getContext();

}
