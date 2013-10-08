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
package de.clusteval.program.r;

import java.io.File;
import java.util.Map;

import org.rosuda.REngine.REngineException;

import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.RelativeDataSet;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ProgramConfig;
import de.clusteval.utils.RNotAvailableException;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class RelativeDataRProgram extends RProgram {

	/**
	 * @param repository
	 *            the repository this program should be registered at.
	 * @param changeDate
	 *            The change date of this program is used for equality checks.
	 * @param absPath
	 *            The absolute path of this program.
	 * @throws RegisterException
	 */
	public RelativeDataRProgram(Repository repository, long changeDate,
			File absPath) throws RegisterException {
		super(repository, changeDate, absPath);
	}

	/**
	 * The copy constructor for rprograms.
	 * 
	 * @param rProgram
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RelativeDataRProgram(final RelativeDataRProgram rProgram)
			throws RegisterException {
		super(rProgram);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.RProgram#beforeExec(de.clusteval.data.DataConfig,
	 * de.clusteval.program.ProgramConfig, java.lang.String[], java.util.Map,
	 * java.util.Map)
	 */
	@Override
	protected void beforeExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams)
			throws RLibraryNotLoadedException, REngineException,
			RNotAvailableException {
		super.beforeExec(dataConfig, programConfig, invocationLine,
				effectiveParams, internalParams);

		rEngine.assign("x", x);
		rEngine.eval("x <- max(x)-x");
		rEngine.eval("x <- as.dist(x)");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.r.RProgram#extractDataSetContent(de.clusteval.data
	 * .DataConfig)
	 */
	@Override
	protected SimilarityMatrix extractDataSetContent(DataConfig dataConfig) {
		RelativeDataSet dataSet = (RelativeDataSet) (dataConfig
				.getDatasetConfig().getDataSet().getInStandardFormat());
		SimilarityMatrix simMatrix = dataSet.getDataSetContent();
		this.ids = dataSet.getIds().toArray(new String[0]);
		this.x = simMatrix.toArray();
		return simMatrix;
	}
}
