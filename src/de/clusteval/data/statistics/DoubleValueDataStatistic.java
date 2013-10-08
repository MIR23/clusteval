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
package de.clusteval.data.statistics;

import java.io.File;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;


/**
 * @author Christian Wiwie
 * 
 */
public abstract class DoubleValueDataStatistic extends DataStatistic {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @param value
	 * @throws RegisterException
	 */
	public DoubleValueDataStatistic(Repository repository, boolean register,
			long changeDate, File absPath, double value)
			throws RegisterException {
		super(repository, false, changeDate, absPath);
		this.value = value;

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for this statistic.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DoubleValueDataStatistic(final DoubleValueDataStatistic other)
			throws RegisterException {
		super(other);
		this.value = other.value;
	}

	protected double value;

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Statistic#parseFromString(java.lang.String)
	 */
	@Override
	public void parseFromString(String contents) {
		this.value = Double.valueOf(contents);
	}

	/**
	 * @return The double value of this statistic.
	 */
	public double getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.statistics.DataStatistic#toString()
	 */
	@Override
	public String toString() {
		return this.value + "";
	}

}
