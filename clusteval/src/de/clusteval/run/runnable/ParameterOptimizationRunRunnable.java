/**
 * 
 */
package de.clusteval.run.runnable;

import java.io.File;
import java.io.IOException;
import java.util.List;

import utils.ProgressPrinter;
import utils.Triple;
import de.clusteval.cluster.paramOptimization.IDivergingParameterOptimizationMethod;
import de.clusteval.cluster.paramOptimization.NoParameterSetFoundException;
import de.clusteval.cluster.paramOptimization.ParameterOptimizationException;
import de.clusteval.cluster.paramOptimization.ParameterOptimizationMethod;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualityMeasureValue;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.IncompatibleDataSetFormatException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.IncompleteGoldStandardException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.Run;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.plot.Plotter;
import file.FileUtils;

/**
 * A type of an execution runnable, that corresponds to
 * {@link ParameterOptimizationRun}s and is therefore responsible for performing
 * several clusterings iteratively.
 * 
 * <p>
 * In {@link #doRun()} the optimization method {@link #optimizationMethod}
 * determines, how many iterations are to be performed.
 * 
 * @author Christian Wiwie
 * 
 */
public class ParameterOptimizationRunRunnable extends ExecutionRunRunnable {

	/**
	 * This attribute is set to some instance of an parameter optimization
	 * method, that will determine the sequence of parameter sets during the
	 * optimization process.
	 */
	protected ParameterOptimizationMethod optimizationMethod;

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
	 * @param optimizationMethod
	 *            The optimization method which determines the parameter sets
	 *            during the optimization process and stores the results.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public ParameterOptimizationRunRunnable(RunSchedulerThread runScheduler,
			Run run, ProgramConfig programConfig, DataConfig dataConfig,
			ParameterOptimizationMethod optimizationMethod,
			String runIdentString, boolean isResume) {
		super(run, programConfig, dataConfig, runIdentString, isResume);

		this.optimizationMethod = optimizationMethod;
		if (optimizationMethod != null) {
			this.optimizationMethod.setResume(isResume);
		}
		this.progress = new ProgressPrinter(100, false);
		this.future = runScheduler.registerRunRunnable(this);
	}

	/**
	 * This method replaces the optimization parameters with the values given in
	 * the run configuration.
	 */
	protected String[] parseOptimizationParameters(String[] invocation) {
		final String[] parsed = invocation.clone();
		try {
			// 15.04.2013: changed invocation of next() to beginning of
			// doRunIteration() in order to get the right iteration numbner
			// now here: get the parameter set created there
			List<ParameterSet> paramSets = optimizationMethod.getResult()
					.getParameterSets();
			ParameterSet optimizationParamValues = paramSets.get(paramSets
					.size() - 1);
			for (int i = 0; i < parsed.length; i++) {
				for (String param : optimizationParamValues.keySet()) {
					parsed[i] = parsed[i].replace("%" + param + "%",
							optimizationParamValues.get(param) + "");
					effectiveParams.put(param,
							optimizationParamValues.get(param) + "");
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return parsed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * run.runnable.ExecutionRunRunnable#replaceRemainingParameters(java.util
	 * .Map, java.lang.String, java.util.Map)
	 */
	@Override
	protected String[] replaceRunParameters(String[] invocation)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {
		invocation = this.parseOptimizationParameters(invocation);
		return super.replaceRunParameters(invocation);
	}

	/**
	 * @return Get the optimization method of this parameter optimization run
	 *         runnable.
	 * @see #optimizationMethod
	 */
	public ParameterOptimizationMethod getOptimizationMethod() {
		return this.optimizationMethod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#beforeRun()
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

		/*
		 * Pass converted data configuration to optimization method
		 */
		this.optimizationMethod.setDataConfig(this.dataConfig);
		this.optimizationMethod.setProgramConfig(this.programConfig);
		try {
			this.optimizationMethod.reset(new File(completeQualityOutput));
			if (isResume) {
				// in case of resume, we have to update the current percentage
				int iterationPercent = (int) (this.optimizationMethod
						.getCurrentCount()
						/ (double) this.optimizationMethod
								.getTotalIterationCount() * 100);
				this.progress.update(iterationPercent);
			}
		} catch (ParameterOptimizationException e) {
			e.printStackTrace();
		} catch (RunResultParseException e) {
			e.printStackTrace();
		}

		// this.optId = this.optimizationMethod.getCurrentCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#endRun()
	 */
	@Override
	protected void afterRun() {
		super.afterRun();

		if (this.optimizationMethod != null
				&& this.optimizationMethod.getResult() != null) {
			if (this.optimizationMethod.getResult().getOptimalParameterSet() != null)
				this.log.info("Optimal Parameter set for "
						+ programConfig
						+ " & "
						+ dataConfig
						+ ":\t"
						+ this.optimizationMethod.getResult()
								.getOptimalParameterSet()
						+ ""
						+ this.optimizationMethod.getResult()
								.getOptimalCriterionValue());
			/*
			 * TODO: option, whether to plot
			 */
			Plotter.plotParameterOptimizationResult(this.optimizationMethod
					.getResult());

			// clear memory-hungry internal attributes of clustering results
			ParameterOptimizationResult result = this.optimizationMethod
					.getResult();
			result.unloadFromMemory();
		}
	}

	@Override
	protected void doRun() throws InternalAttributeException,
			RegisterException, IOException, NoRunResultFormatParserException,
			RNotAvailableException, RLibraryNotLoadedException {
		try {
			while (this.optimizationMethod.hasNext()) {
				if (checkForInterrupted())
					return;
				this.doRunIteration();
			}
		} catch (NoParameterSetFoundException e) {
			// this exception just indicates, that no parameter set has been
			// found and the parameter optimization terminated earlier than
			// expected.
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#doRunIteration()
	 */
	@Override
	protected void doRunIteration() throws InternalAttributeException,
			RegisterException, IOException, NoRunResultFormatParserException,
			NoParameterSetFoundException, RNotAvailableException,
			RLibraryNotLoadedException {
		try {
			optimizationMethod.next();
			this.optId = this.optimizationMethod.getCurrentCount();
			super.doRunIteration();
		} finally {
			// changed 25.01.2013
			int iterationPercent = (int) (this.optimizationMethod
					.getCurrentCount()
					/ (double) this.optimizationMethod.getTotalIterationCount() * 100);
			// this.progress.update(this.progress.getCurrentPos() + 1);
			this.progress.update(iterationPercent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.ExecutionRunRunnable#handleMissingRunResult()
	 */
	@Override
	protected void handleMissingRunResult() {
		if (this.optimizationMethod instanceof IDivergingParameterOptimizationMethod) {
			this.log.info(this.getRun()
					+ " ("
					+ this.programConfig
					+ ","
					+ this.dataConfig
					+ ") The result of this run could not be found. Probably the program did not converge with this parameter set.");
		} else {
			this.log.info(this.getRun()
					+ " ("
					+ this.programConfig
					+ ","
					+ this.dataConfig
					+ ") The result of this run could not be found. Please consult the log files of the program");
		}

		/*
		 * There is no results file
		 */
		/*
		 * Write the minimal quality values into the complete results file
		 */
		StringBuilder sb = new StringBuilder();
		sb.append(this.optId);
		sb.append("\t");
		for (int p = 0; p < programConfig.getOptimizableParams().size(); p++) {
			ProgramParameter<?> param = programConfig.getOptimizableParams()
					.get(p);
			if (p > 0)
				sb.append(",");
			sb.append(effectiveParams.get(param.getName()));
		}
		sb.append("\t");
		for (int i = 0; i < this.getRun().getQualityMeasures().size(); i++) {
			sb.append(ClusteringQualityMeasureValue.getForNotTerminated());
			sb.append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		FileUtils.appendStringToFile(completeQualityOutput, sb.toString());

		ClusteringQualitySet minimalQualities = new ClusteringQualitySet();
		for (ClusteringQualityMeasure measure : this.getRun()
				.getQualityMeasures())
			minimalQualities.put(measure,
					ClusteringQualityMeasureValue.getForNotTerminated());
		if (this.optimizationMethod instanceof IDivergingParameterOptimizationMethod) {
			((IDivergingParameterOptimizationMethod) this.optimizationMethod)
					.giveFeedbackNotTerminated(minimalQualities);
		} else {
			this.optimizationMethod.giveQualityFeedback(minimalQualities);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * run.runnable.ExecutionRunRunnable#afterQualityAssessment(java.util.List)
	 */
	@Override
	// 04.04.2013: adding iteration number into complete file
	protected void writeQualitiesToFile(
			List<Triple<ParameterSet, ClusteringQualitySet, Long>> qualities) {
		// in this case, the list contains only one element
		this.optimizationMethod.giveQualityFeedback(qualities.get(0)
				.getSecond());
		super.writeQualitiesToFile(qualities);
	}
}
