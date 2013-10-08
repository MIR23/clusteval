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
package de.clusteval.data.distance;

import java.io.File;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.RNotAvailableException;

/**
 * This type of distance measure uses the R framework.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class DistanceMeasureR extends DistanceMeasure {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public DistanceMeasureR(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of this R distance measures.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DistanceMeasureR(final DistanceMeasureR other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.data.distance.DistanceMeasure#getDistance(double[],
	 * double[])
	 */
	@Override
	public final double getDistance(double[] point1, double[] point2)
			throws RNotAvailableException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				try {
					return getDistanceHelper(point1, point2, rEngine);
				} catch (REXPMismatchException e) {
					// handle this type of exception as an REngineException
					throw new REngineException(rEngine, e.getMessage());
				}
			} catch (REngineException e) {
				this.log.warn("R-framework (" + this.getClass().getSimpleName()
						+ "): " + rEngine.getLastError());
				// TODO
				return -1.0;
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			throw new RNotAvailableException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.data.distance.DistanceMeasure#getDistances(double[][])
	 */
	@Override
	public double[][] getDistances(double[][] matrix)
			throws RNotAvailableException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				try {
					return getDistancesHelper(matrix, rEngine);
				} catch (REXPMismatchException e) {
					// handle this type of exception as an REngineException
					throw new REngineException(rEngine, e.getMessage());
				}
			} catch (REngineException e) {
				this.log.warn("R-framework (" + this.getClass().getSimpleName()
						+ "): " + rEngine.getLastError());
				// TODO
				return null;
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			throw new RNotAvailableException(e.getMessage());
		}
	}

	protected abstract double getDistanceHelper(double[] point1,
			double[] point2, final MyRengine rEngine) throws REngineException,
			REXPMismatchException;

	protected abstract double[][] getDistancesHelper(double[][] matrix,
			final MyRengine rEngine) throws REngineException,
			REXPMismatchException;
}
