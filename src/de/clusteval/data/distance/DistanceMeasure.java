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
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.utils.RNotAvailableException;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class DistanceMeasure extends RepositoryObject {

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public DistanceMeasure(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of this distance measures.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DistanceMeasure(final DistanceMeasure other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final DistanceMeasure clone() {
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
	 * Parses the from string.
	 * 
	 * @param repository
	 *            the repository
	 * @param distanceMeasure
	 *            the distance measure
	 * @return the distance measure
	 * @throws UnknownDistanceMeasureException
	 */
	public static DistanceMeasure parseFromString(final Repository repository,
			String distanceMeasure) throws UnknownDistanceMeasureException {
		Class<? extends DistanceMeasure> c = repository
				.getDistanceMeasureClass("de.clusteval.data.distance."
						+ distanceMeasure);
		try {
			DistanceMeasure measure = c.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, false, System.currentTimeMillis(),
					new File(distanceMeasure));

			return measure;
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
		throw new UnknownDistanceMeasureException("\"" + distanceMeasure
				+ "\" is not a known distance measure.");
	}

	/**
	 * @param point1
	 *            A point with double valued coordinates.
	 * @param point2
	 *            A point with double valued coordinates.
	 * @return Distance between point1 and point2.
	 * @throws RNotAvailableException
	 */
	public abstract double getDistance(double[] point1, double[] point2)
			throws RNotAvailableException;

	/**
	 * This method indicates, whether a distance measure supports the bulk
	 * calculation of all pairwise distances of rows of a matrix with rows of a
	 * second matrix. Overwrite it in your subclass and return the appropriate
	 * boolean value. If your subclass supports matrices you also have to
	 * overwrite {@link #getDistances(double[][])} with a correct
	 * implementation.
	 * 
	 * @return True, if this distance measure supports bulk distance calculation
	 *         of matrices.
	 */
	public abstract boolean supportsMatrix();

	/**
	 * This method calculates all pairwise distances between the rows of a
	 * matrix.
	 * 
	 * @param matrix
	 *            A matrix containing samples in each row and features in the
	 *            columns.
	 * @return Matrix containing all pairwise distances of rows of the matrix
	 * @throws RNotAvailableException
	 */
	public abstract double[][] getDistances(double[][] matrix)
			throws RNotAvailableException;

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this.getClass().equals(obj.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

}
