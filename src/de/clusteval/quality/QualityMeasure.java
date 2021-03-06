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
package de.clusteval.quality;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.graphmatching.GraphMatching;
import de.clusteval.utils.RCalculationException;
import de.clusteval.utils.RNotAvailableException;

/**
 * A clustering quality measure is used to assess the quality of a
 * {@link GraphMatching} by invoking
 * {@link #getQualityOf(GraphMatching, GraphMatching, DataConfig)}.
 * 
 * <p>
 * Every clustering quality measure has a range of possible qualities between
 * {@link #getMinimum()} and {@link #getMaximum()}.
 * 
 * <p>
 * Some clustering quality measures can only be assessed if a goldstandard is
 * available (see {@link #requiresGoldstandard()}).
 * 
 * @author Christian Wiwie
 */
public abstract class QualityMeasure extends RepositoryObject {

	/**
	 * Instantiates a new clustering quality measure.
	 * 
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public QualityMeasure(final Repository repo, final boolean register,
			final long changeDate, final File absPath) throws RegisterException {
		super(repo, false, changeDate, absPath);

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of clustering quality measures.
	 * 
	 * @param other
	 *            The quality measure to clone.
	 * @throws RegisterException
	 */
	public QualityMeasure(final QualityMeasure other) throws RegisterException {
		super(other);
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
	 * Gets the quality of clustering.
	 * 
	 * @param clustering
	 *            the clustering
	 * @param goldStandard
	 *            The expected goldstandard.
	 * @param dataConfig
	 *            the data config
	 * @return the quality of clustering
	 * @throws UnknownGoldStandardFormatException
	 *             the unknown gold standard format exception
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IOException
	 * @throws RNotAvailableException
	 * @throws RCalculationException
	 */
	public abstract QualityMeasureValue getQualityOf(GraphMatching clustering,
			GraphMatching goldStandard, DataConfig dataConfig)
			throws UnknownGoldStandardFormatException,
			UnknownDataSetFormatException, IOException,
			InvalidDataSetFormatVersionException, RNotAvailableException,
			RCalculationException;

	/**
	 * This is a helper method for cloning a list of clustering quality
	 * measures.
	 * 
	 * @param qualityMeasures
	 *            The quality measures to be cloned.
	 * @return A list containining cloned objects of the given quality measures.
	 */
	public static List<QualityMeasure> cloneQualityMeasures(
			final List<QualityMeasure> qualityMeasures) {
		List<QualityMeasure> result = new ArrayList<QualityMeasure>();

		for (QualityMeasure qualityMeasure : qualityMeasures)
			result.add(qualityMeasure.clone());

		return result;
	}

	/**
	 * Parses the from string.
	 * 
	 * @param repository
	 *            the repository
	 * @param qualityMeasure
	 *            the quality measure
	 * @return the clustering quality measure
	 * @throws UnknownQualityMeasureException
	 *             the unknown clustering quality measure exception
	 */
	public static QualityMeasure parseFromString(final Repository repository,
			String qualityMeasure)
			throws UnknownQualityMeasureException {

		Class<? extends QualityMeasure> c = repository
				.getClusteringQualityMeasureClass("de.clusteval.quality."
						+ qualityMeasure);
		try {
			QualityMeasure measure = c.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, false, System.currentTimeMillis(),
					new File(qualityMeasure));

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
		throw new UnknownQualityMeasureException("\""
				+ qualityMeasure + "\" is not a known quality measure.");
	}

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
	 * @see java.lang.Object#clone()
	 */
	@Override
	public QualityMeasure clone() {
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	/**
	 * This method compares two values of this clustering quality measure and
	 * returns true, if the first one is better than the second one.
	 * 
	 * @param quality1
	 *            The first quality value.
	 * @param quality2
	 *            The second quality value.
	 * @return True, if quality1 is better than quality2
	 */
	public final boolean isBetterThan(QualityMeasureValue quality1,
			QualityMeasureValue quality2) {
		if (!quality1.isTerminated)
			return false;
		if (!quality2.isTerminated)
			return true;
		return isBetterThanHelper(quality1, quality2);
	}

	protected abstract boolean isBetterThanHelper(QualityMeasureValue quality1,
			QualityMeasureValue quality2);

	/**
	 * @return The minimal value of the range of possible values of this quality
	 *         measure.
	 */
	public abstract double getMinimum();

	/**
	 * @return The maximal value of the range of possible values of this quality
	 *         measure.
	 */
	public abstract double getMaximum();

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

	/**
	 * Override this method to indicate, whether the quality measure of your
	 * subclass needs a goldstandard to be able to be computed.
	 * 
	 * @return True, if this clustering quality measure requires a goldstandard
	 *         to be able to assess the quality of a clustering.
	 */
	public abstract boolean requiresGoldstandard();

	/**
	 * This alias is used whenever this clustering quality measure is visually
	 * represented and a readable name is needed.
	 * 
	 * @return The alias of this clustering quality measure.
	 */
	public abstract String getAlias();
}
