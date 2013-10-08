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
package de.clusteval.run.runnable;

import java.io.File;
import java.io.IOException;

import de.clusteval.paramOptimization.NoParameterSetFoundException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.IncompatibleDataSetFormatException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.IncompleteGoldStandardException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.program.ProgramConfig;
import de.clusteval.run.ClusteringRun;
import de.clusteval.run.Run;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;

/**
 * A type of an execution runnable, that corresponds to {@link ClusteringRun}
 * and is therefore responsible for performing only a single clustering.
 * 
 * <p>
 * In {@link #doRun()} a ClusteringRunRunnable executes only a single iteration.
 * 
 * @author Christian Wiwie
 * 
 */
public class ClusteringRunRunnable extends ExecutionRunRunnable {

	/**
	 * @param runScheduler
	 *            The run scheduler that the newly created runnable should be
	 *            passed to and executed by.
	 * @param run
	 *            The run this runnable belongs to.
	 * @param runIdentString
	 *            The unique identification string of the run which is used to
	 *            store the results in a unique folder to avoid overwriting.
	 * @param programConfig
	 *            The program configuration encapsulating the program executed
	 *            by this runnable.
	 * @param dataConfig
	 *            The data configuration used by this runnable.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public ClusteringRunRunnable(RunSchedulerThread runScheduler, Run run,
			ProgramConfig programConfig, DataConfig dataConfig,
			String runIdentString, boolean isResume) {
		super(run, programConfig, dataConfig, runIdentString, isResume);
		this.future = runScheduler.registerRunRunnable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#beforeClustering()
	 */
	@Override
	protected void beforeRun() throws IllegalArgumentException,
			UnknownDataSetFormatException, IOException,
			InvalidDataSetFormatVersionException, RegisterException,
			InternalAttributeException, IncompatibleDataSetFormatException,
			UnknownGoldStandardFormatException,
			IncompleteGoldStandardException, RNotAvailableException {
		super.beforeRun();

		if (!new File(completeQualityOutput).exists() || !isResume)
			writeHeaderIntoCompleteFile(completeQualityOutput);

		optId = 1;
	}

	@Override
	protected void doRun() throws InternalAttributeException,
			RegisterException, IOException, NoRunResultFormatParserException,
			NoParameterSetFoundException, RNotAvailableException,
			RLibraryNotLoadedException {
		this.doRunIteration();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#handleMissingRunResult()
	 */
	@Override
	protected void handleMissingRunResult() {
		this.log.info(this.getRun()
				+ " ("
				+ this.programConfig
				+ ","
				+ this.dataConfig
				+ ") The result of this run could not be found. Please consult the log files of the program");
	}
}
