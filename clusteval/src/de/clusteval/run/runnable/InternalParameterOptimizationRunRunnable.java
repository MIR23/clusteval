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
import de.clusteval.run.Run;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;

/**
 * @author Christian Wiwie
 * 
 */
public class InternalParameterOptimizationRunRunnable
		extends
			ExecutionRunRunnable {

	/**
	 * @param runScheduler
	 * @param run
	 * @param programConfig
	 * @param dataConfig
	 * @param runIdentString
	 * @param isResume
	 */
	public InternalParameterOptimizationRunRunnable(
			RunSchedulerThread runScheduler, Run run,
			ProgramConfig programConfig, DataConfig dataConfig,
			String runIdentString, boolean isResume) {
		super(run, programConfig, dataConfig, runIdentString, isResume);
		this.future = runScheduler.registerRunRunnable(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#getInvocationFormat()
	 */
	@Override
	protected String getInvocationFormat() {
		return programConfig
				.getInvocationFormatParameterOptimization(!dataConfig
						.hasGoldStandardConfig());
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

		this.optId = 1;
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
