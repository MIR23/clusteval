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
package de.clusteval.data.dataset;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.clusteval.data.dataset.format.AbsoluteDataSetFormat;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * 
 * An absolute dataset contains data in terms of absolute coordinates, that
 * means similarities between object pairs can be calculated by looking at the
 * absolute coordinates of two objects.
 * 
 * @author Christian Wiwie
 * 
 */
public class AbsoluteDataSet extends DataSet {

	/**
	 * This variable holds the contents of the dataset, after
	 * {@link #loadIntoMemory()} and before {@link #unloadFromMemory()} was
	 * invoked.
	 * <p>
	 * For absolute datasets the stored data are absolute coordinates. These are
	 * stored in a matrix form (see {@link DataMatrix}).
	 */
	private DataMatrix dataMatrix;

	/**
	 * 
	 * @param repository
	 *            the repository this dataset should be registered at.
	 * @param register
	 *            Whether this dataset should be registered in the repository.
	 * @param changeDate
	 *            The change date of this dataset is used for equality checks.
	 * @param alias
	 *            A short alias name for this data set.
	 * @param absPath
	 *            The absolute path of this dataset.
	 * @param dsFormat
	 *            The format of this dataset.
	 * @param dsType
	 *            The type of this dataset
	 * @throws RegisterException
	 */
	public AbsoluteDataSet(Repository repository, final boolean register,
			long changeDate, File absPath, final String alias,
			AbsoluteDataSetFormat dsFormat, DataSetType dsType)
			throws RegisterException {
		super(repository, register, changeDate, absPath, alias, dsFormat,
				dsType);
	}

	/**
	 * @param dataset
	 * @throws RegisterException
	 */
	public AbsoluteDataSet(AbsoluteDataSet dataset) throws RegisterException {
		super(dataset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#clone()
	 */
	@Override
	public AbsoluteDataSet clone() {
		try {
			return new AbsoluteDataSet(this);
		} catch (RegisterException e) {
			e.printStackTrace();
			// should not occur
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#loadIntoMemory()
	 */
	@Override
	public boolean loadIntoMemory() throws IllegalArgumentException,
			IOException, InvalidDataSetFormatVersionException {
		if (!isInMemory())
			this.dataMatrix = this.getDataSetFormat().parse(this);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#getDataSetContent()
	 */
	@Override
	public DataMatrix getDataSetContent() {
		return this.dataMatrix;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.data.dataset.DataSet#setDataSetContent(java.lang.Object)
	 */
	@Override
	public boolean setDataSetContent(Object newContent) {
		if (!(newContent instanceof DataMatrix))
			return false;

		this.dataMatrix = (DataMatrix) newContent;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#isInMemory()
	 */
	@Override
	public boolean isInMemory() {
		return this.dataMatrix != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#unloadFromMemory()
	 */
	@Override
	public boolean unloadFromMemory() {
		this.dataMatrix = null;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#getDataSetFormat()
	 */
	@Override
	public AbsoluteDataSetFormat getDataSetFormat() {
		return (AbsoluteDataSetFormat) super.getDataSetFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see data.dataset.DataSet#getIds()
	 */
	@Override
	public List<String> getIds() {
		return Arrays.asList(this.dataMatrix.getIds());
	}
}
