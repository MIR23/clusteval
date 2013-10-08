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
import java.io.IOException;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.RNotAvailableException;

/**
 * This class is parent class of all different kind of analyses on a DataConfig.
 * This analyses can be performed unrelated to clustering, since it only
 * requires the dataset (and optionally the goldstandard).
 * 
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class DataStatisticRCalculator<T extends DataStatistic>
		extends
			DataStatisticCalculator<T> {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param dataConfig
	 * @throws RegisterException
	 */
	public DataStatisticRCalculator(Repository repository, long changeDate,
			File absPath, final DataConfig dataConfig) throws RegisterException {
		super(repository, changeDate, absPath, dataConfig);
	}

	/**
	 * The copy constructor of data statistic calculators.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DataStatisticRCalculator(final DataStatisticRCalculator<T> other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.statistics.DataStatisticCalculator#calculateResult()
	 */
	@Override
	protected final T calculateResult()
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			REngineException, RNotAvailableException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				try {
					return calculateResultHelper(rEngine);
				} catch (REXPMismatchException e) {
					// handle this type of exception as an REngineException
					throw new REngineException(rEngine, e.getMessage());
				}
			} catch (REngineException e) {
				this.log.warn("R-framework (" + this.getClass().getSimpleName()
						+ "): " + rEngine.getLastError());
				throw e;
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			throw new RNotAvailableException(e.getMessage());
		}
	}

	protected abstract T calculateResultHelper(final MyRengine rEngine)
			throws IncompatibleDataConfigDataStatisticException,
			UnknownGoldStandardFormatException, UnknownDataSetFormatException,
			IllegalArgumentException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			REngineException, REXPMismatchException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.utils.StatisticCalculator#writeOutputTo(java.io.File)
	 */
	@Override
	public final void writeOutputTo(File absFolderPath)
			throws REngineException, RNotAvailableException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				try {
					writeOutputToHelper(absFolderPath, rEngine);
				} catch (REXPMismatchException e) {
					// handle this type of exception as an REngineException
					throw new REngineException(rEngine, e.getMessage());
				}
			} catch (REngineException e) {
				this.log.warn("R-framework (" + this.getClass().getSimpleName()
						+ "): " + rEngine.getLastError());
				throw e;
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			throw new RNotAvailableException(e.getMessage());
		}
	}

	protected abstract void writeOutputToHelper(File absFolderPath,
			final MyRengine rEngine) throws REngineException,
			REXPMismatchException;
}
