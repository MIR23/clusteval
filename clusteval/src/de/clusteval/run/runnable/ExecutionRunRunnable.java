/**
 * 
 */
package de.clusteval.run.runnable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import utils.Pair;
import utils.Triple;
import de.clusteval.cluster.Clustering;
import de.clusteval.cluster.paramOptimization.NoParameterSetFoundException;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualitySet;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.IncompatibleDataSetFormatException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.IncompleteGoldStandardException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.r.RProgram;
import de.clusteval.run.ExecutionRun;
import de.clusteval.run.MissingParameterValueException;
import de.clusteval.run.Run;
import de.clusteval.run.result.ClusteringRunResult;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.RunResultNotFoundException;
import de.clusteval.utils.FormatConversionException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;
import de.clusteval.utils.plot.Plotter;
import file.FileUtils;
import format.Formatter;

/**
 * A type of a runnable, that corresponds to {@link ExecutionRun}s and is
 * therefore responsible for performing program configurations and certain data
 * configurations.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class ExecutionRunRunnable extends RunRunnable {

	/**
	 * The program configuration this thread combines with a data configuration.
	 */
	protected ProgramConfig programConfig;

	/**
	 * The data configuration this thread combines with a program configuration.
	 */
	protected DataConfig dataConfig;

	/**
	 * This is the run result format of the program that is being executed by
	 * this runnable.
	 */
	protected RunResultFormat format;

	/**
	 * A map containing all the parameter values set in the run.
	 */
	protected Map<ProgramParameter<?>, String> runParams;

	/**
	 * A map containing the parameters of {@link #runParams} and additionally
	 * internal parameters like file paths that are used throughout execution of
	 * this runnable.
	 */
	protected Map<String, String> effectiveParams;

	/**
	 * The internal parameters are parameters, that cannot be directly
	 * influenced by the user, e.g. the absolute input or output path.
	 */
	protected Map<String, String> internalParams = new HashMap<String, String>();

	/**
	 * A temporary variable holding the absolute path to the current complete
	 * quality output file during execution of the runnable.
	 */
	protected String completeQualityOutput;

	/**
	 * A temporary variable holding a file object pointing to the absolute path
	 * of the current log output file during execution of the runnable
	 */
	protected File logFile;

	/**
	 * A temporary variable holding a file object pointing to the absolute path
	 * of the current clustering output file during execution of the runnable
	 */
	protected File clusteringResultFile;

	/**
	 * A temporary variable holding a file object pointing to the absolute path
	 * of the current clustering quality output file during execution of the
	 * runnable
	 */
	protected File resultQualityFile;

	/**
	 * An object that wraps up all results calculated during the execution of
	 * this runnable. The runnable is responsible for adding new results to this
	 * object during the execution.
	 */
	protected ClusteringRunResult result;

	/**
	 * This number indicates the current iteration performed by the runnable
	 * object.
	 * 
	 * <p>
	 * This is only larger than 1, if we are in PARAMETER_OPTIMIZATION mode.
	 * Then the optimization method will determine, how often we iterate in
	 * total and this attribute will be increased by the runnable after every
	 * iteration.
	 */
	protected int optId;

	/**
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
	public ExecutionRunRunnable(Run run, ProgramConfig programConfig,
			DataConfig dataConfig, String runIdentString, boolean isResume) {
		super(run, runIdentString, isResume);

		this.programConfig = programConfig;
		this.dataConfig = dataConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.getRun()nable.RunRunnable#getRun()
	 */
	@Override
	public ExecutionRun getRun() {
		return (ExecutionRun) super.getRun();
	}

	/**
	 * A helper method to write a header into the complete quality output in the
	 * beginning.
	 * 
	 * <p>
	 * If at all, then this method is invoked by {@link #beginRun()} before
	 * anything has been executed by the runnable.
	 */
	protected void writeHeaderIntoCompleteFile(
			final String completeQualityOutput) {
		StringBuilder sb = new StringBuilder();
		// 04.04.2013: adding iteration numbers into complete file
		sb.append("iteration\t");
		for (int p = 0; p < programConfig.getOptimizableParams().size(); p++) {
			ProgramParameter<?> param = programConfig.getOptimizableParams()
					.get(p);
			if (p > 0)
				sb.append(",");
			sb.append(param);
		}
		sb.append("\t");
		for (ClusteringQualityMeasure measure : this.getRun()
				.getQualityMeasures()) {
			sb.append(measure.getClass().getSimpleName());
			sb.append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		FileUtils.appendStringToFile(completeQualityOutput, sb.toString());
	}

	/**
	 * This method checks, whether the format of the data input is compatible to
	 * the input formats of the program configuration.
	 * 
	 * @return True, if compatible, false otherwise.
	 * @throws IOException
	 * @throws RegisterException
	 * @throws FormatConversionException
	 * @throws RNotAvailableException
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 */
	protected boolean preprocessAndCheckCompatibleDataSetFormat()
			throws IOException, RegisterException,
			InvalidDataSetFormatVersionException,
			UnknownDataSetFormatException, RNotAvailableException {
		/*
		 * only one conversion operation per dataset at a time. otherwise we can
		 * have problems
		 */
		File datasetConfigFile = ClustevalBackendServer.getCommonFile(new File(
				dataConfig.getDatasetConfig().getAbsolutePath()));
		synchronized (datasetConfigFile) {
			/*
			 * Check, whether this program can be applied to this dataset, i.e.
			 * either they are directly compatible or the dataset can be
			 * converted to another compatible DataSetFormat.
			 */
			List<DataSet> dataSets = dataConfig.getDatasetConfig()
					.getDataSets();
			List<String> dataSetFormats = new ArrayList<String>();
			for (DataSet ds : dataSets) {
				dataSetFormats.add(ds.getDataSetFormat().getClass()
						.getSimpleName());
			}
			Set<List<Pair<String, String>>> conversions = programConfig
					.getCompatibleDataSetFormats(dataSetFormats);
			if (conversions.isEmpty())
				return false;

			// find largest mapping
			List<Pair<String, String>> mapping = conversions.iterator().next();
			Iterator<List<Pair<String, String>>> it = conversions.iterator();
			while (it.hasNext()) {
				List<Pair<String, String>> m = it.next();
				if (m.size() > mapping.size())
					mapping = m;
			}

			Set<Pair<String, String>> remainingMappings = new HashSet<Pair<String, String>>(
					mapping);

			// for every dataset, find the target format in the mapping
			for (int i = 0; i < dataSets.size(); i++) {
				DataSet ds = dataSets.get(i);
				Iterator<Pair<String, String>> it2 = remainingMappings
						.iterator();
				while (it2.hasNext()) {
					Pair<String, String> p = it2.next();
					if (p.getFirst().equals(
							ds.getDataSetFormat().getClass().getSimpleName())) {

						// convert the dataset to the target format
						DataSet converted;
						try {
							converted = ds.preprocessAndConvertTo(this.getRun()
									.getContext(), DataSetFormat
									.parseFromString(this.getRun()
											.getRepository(), p.getSecond()));
							// remove this mapping
							remainingMappings.remove(p);

							// added 23.01.2013: rename the new dataset, unique
							// for
							// the program configuration
							int indexOfLastExt = converted.getAbsolutePath()
									.lastIndexOf(".");
							if (indexOfLastExt == -1)
								indexOfLastExt = converted.getAbsolutePath()
										.length();
							String newFileName = converted.getAbsolutePath()
									.substring(0, indexOfLastExt)
									+ "_"
									+ programConfig.getName()
									+ converted.getAbsolutePath().substring(
											indexOfLastExt);
							// if the new dataset file is the same file as the
							// old
							// one, we copy it instead of moving
							if (converted.getAbsolutePath().equals(
									ds.getAbsolutePath())) {
								converted.copyTo(new File(newFileName), false,
										true);
							} else
								converted.move(new File(newFileName), false);

							dataConfig.getDatasetConfig().getDataSets()
									.set(i, converted);

							break;
						} catch (FormatConversionException e) {
							e.printStackTrace();
							// try next format conversion
						}
					}
				}
			}

			return true;
		}
	}

	/**
	 * This method checks, whether the dataset is compatible to the
	 * goldstandard, by verifying, that all objects contained in the dataset
	 * have an entry in the goldstandard and vice versa.
	 * 
	 * @param dataSetConfig
	 *            The dataset configuration encapsulating the dataset to be
	 *            checked.
	 * @param goldStandardConfig
	 *            The goldstandard configuration encapsulating the
	 *            goldstandardto be checked.
	 * @throws IOException
	 * @throws UnknownGoldStandardFormatException
	 * @throws UnknownDataSetFormatException
	 * @throws IncompleteGoldStandardException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IllegalArgumentException
	 */
	protected void checkCompatibilityDataSetGoldStandard(
			DataSetConfig dataSetConfig, GoldStandardConfig goldStandardConfig)
			throws UnknownGoldStandardFormatException,
			IncompleteGoldStandardException, IllegalArgumentException {
		// TODO: will there even be a gold standard
		// DataSet dataSet = dataSetConfig.getDataSet().getInStandardFormat();
		// File dataSetFile = ClustevalBackendServer.getCommonFile(new File(
		// dataSet.getAbsolutePath()));
		// synchronized (dataSetFile) {
		// GoldStandard goldStandard = goldStandardConfig.getGoldstandard();
		// File goldStandardFile = ClustevalBackendServer
		// .getCommonFile(new File(goldStandard.getAbsolutePath()));
		// synchronized (goldStandardFile) {
		//
		// /*
		// * Check whether all ids in the dataset have a corresponding
		// * entry in the gold standard
		// */
		// // dataSet.loadIntoMemory();
		// goldStandard.loadIntoMemory();
		//
		// final Set<String> ids = new HashSet<String>(dataSet.getIds());
		// final Set<ClusterItem> gsItems = goldStandard.getClustering()
		// .getClusterItems();
		// final Set<String> gsIds = new HashSet<String>();
		// for (ClusterItem item : gsItems)
		// gsIds.add(item.getId());
		//
		// if (!gsIds.containsAll(ids)) {
		// ids.removeAll(gsIds);
		// throw new IncompleteGoldStandardException(ids);
		// }
		// }
		// }
	}

	/**
	 * @return The program configuration of this runnable.
	 */
	public ProgramConfig getProgramConfig() {
		return this.programConfig;
	}

	/**
	 * @return The data configuration of this runnable.
	 */
	public DataConfig getDataConfig() {
		return this.dataConfig;
	}

	/**
	 * Helper method for
	 * {@link #parseInvocationLineAndEffectiveParameters(String, String, Map, Map, Map, StringBuilder)}
	 * <p>
	 * Get the original invocation line format from the program configuration
	 * without replacing of any parameters.
	 * 
	 * @return The invocation line.
	 */
	protected String getInvocationFormat() {
		// added 16.01.2013
		if (programConfig.getProgram() instanceof RProgram) {
			return ((RProgram) programConfig.getProgram())
					.getInvocationFormat();
		}
		return programConfig.getInvocationFormat(!dataConfig
				.hasGoldStandardConfig());
	}

	/**
	 * Helper method for
	 * {@link #parseInvocationLineAndEffectiveParameters(String, String, Map, Map, Map, StringBuilder)}
	 * 
	 * <p>
	 * Replace the executable parameter %e% in the invocation line by the
	 * absolute path to the executable.
	 * 
	 * @param invocation
	 *            The invocation line without replaced executable parameter.
	 * @param internalParams
	 *            The map containing all internal parameters, e.g. the
	 *            executable path.
	 * @return The invocation line with replaced executable parameter.
	 */
	protected String[] parseExecutable(final String[] invocation,
			final Map<String, String> internalParams) {
		internalParams.put("e", programConfig.getProgram().getExecutable());
		String[] parsed = invocation.clone();
		for (int i = 0; i < parsed.length; i++)
			parsed[i] = parsed[i].replace("%e%", programConfig.getProgram()
					.getExecutable());
		return parsed;
	}

	/**
	 * Helper method for
	 * {@link #parseInvocationLineAndEffectiveParameters(String, String, Map, Map, Map, StringBuilder)}
	 * 
	 * <p>
	 * Replace the input parameter %i_n% in the invocation line by the absolute
	 * path to the input file.
	 * 
	 * @param invocation
	 *            The invocation line without replaced input parameter.
	 * @param internalParams
	 *            The map containing all internal parameters, e.g. the input
	 *            path.
	 * @return The invocation line with replaced input parameter.
	 */
	protected String[] parseInput(final String[] invocation,
			final Map<String, String> internalParams) {
		List<DataSet> dataSets = dataConfig.getDatasetConfig().getDataSets();
		String[] parsed = invocation.clone();
		for (int i = 0; i < dataSets.size(); i++) {
			internalParams.put("i_" + i, dataSets.get(i).getAbsolutePath());
			for (int j = 0; j < parsed.length; j++)
				parsed[j] = parsed[j].replace("%i_" + i + "%", dataSets.get(i)
						.getAbsolutePath());
		}
		return parsed;
	}

	/**
	 * Helper method for
	 * {@link #parseInvocationLineAndEffectiveParameters(String, String, Map, Map, Map, StringBuilder)}
	 * 
	 * <p>
	 * Replace the goldstandard parameter %gs% in the invocation line by the
	 * absolute path to the goldstandard.
	 * 
	 * @param invocation
	 *            The invocation line without replaced goldstandard parameter.
	 * @param internalParams
	 *            The map containing all internal parameters, e.g. the
	 *            goldstandard path.
	 * @return The invocation line with replaced goldstandard parameter.
	 */
	protected String[] parseGoldStandard(final String[] invocation,
			final Map<String, String> internalParams) {
		if (!dataConfig.hasGoldStandardConfig())
			return invocation;
		internalParams.put("gs", dataConfig.getGoldstandardConfig()
				.getGoldstandard().getAbsolutePath());
		String[] parsed = invocation.clone();
		for (int i = 0; i < parsed.length; i++)
			parsed[i] = parsed[i].replace("%gs%", dataConfig
					.getGoldstandardConfig().getGoldstandard()
					.getAbsolutePath());
		return parsed;
	}

	/**
	 * Helper method for
	 * {@link #parseInvocationLineAndEffectiveParameters(String, String, Map, Map, Map, StringBuilder)}
	 * 
	 * <p>
	 * Replace the output parameter %o% in the invocation line by the absolute
	 * path to the output file.
	 * 
	 * @param invocation
	 *            The invocation line without replaced output parameter.
	 * @param internalParams
	 *            The map containing all internal parameters, e.g. the output
	 *            file path.
	 * @return The invocation line with replaced output parameter.
	 */
	protected String[] parseOutput(final String clusteringOutput,
			final String qualityOutput, final String[] invocation,
			final Map<String, String> internalParams) {
		internalParams.put("o", clusteringOutput);
		internalParams.put("q", qualityOutput);
		String[] parsed = invocation.clone();
		for (int i = 0; i < parsed.length; i++)
			parsed[i] = parsed[i].replace("%o%", clusteringOutput);
		return parsed;
	}

	/**
	 * This method builds up the invocation line by replacing placeholders of
	 * internal parameters by their actual runtime values:
	 * <ul>
	 * <li><b>%e%</b>: The absolute path to the executable</li>
	 * <li><b>%i%</b>: The absolute path to the input file</li>
	 * <li><b>%gs%</b>: The absolute path to the goldstandard file</li>
	 * <li><b>%o%</b>: The absolute path to the output file</li>
	 * </ul>
	 * <p>
	 * Afterwards, non-internal parameters are replaced, that means parameters,
	 * that are defined in the configuration files of the run or program in
	 * {@link #replaceRunParameters(String[])}, e.g.:
	 * <ul>
	 * <li><b>%T%</b> is replaced by 2.0</li>
	 * </ul>
	 * <p>
	 * All placeholders that are not replaced at this point are replaced by the
	 * default values of the corresponding parameters by invoking
	 * {@link #replaceDefaultParameters(String[])}. If the invocation line
	 * contains placeholders that cannot be mapped to a parameter, an exception
	 * is thrown and the process is terminated.
	 * 
	 * @return The parsed invocation line.
	 * 
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 * 
	 */
	public String[] parseInvocationLineAndEffectiveParameters()
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {

		/*
		 * We take the invocation line from the ProgramConfig and replace the
		 * variables %e%, %i%, %gs%, %o% by the absolute path to the executable,
		 * the input, the goldstandard and the output respectively.
		 */
		// split by spaces. this ensures compatibility for spaces in pathes that
		// might be inserted later.
		String[] invocation = getInvocationFormat().split(" ");

		/*
		 * Executable %e%
		 */
		invocation = parseExecutable(invocation, internalParams);

		/*
		 * input %i%
		 */
		invocation = parseInput(invocation, internalParams);

		/*
		 * goldstandard %gs%
		 */
		invocation = parseGoldStandard(invocation, internalParams);
		/*
		 * output %o%
		 */
		invocation = parseOutput(clusteringResultFile.getAbsolutePath(),
				resultQualityFile.getAbsolutePath(), invocation, internalParams);

		invocation = replaceRunParameters(invocation);

		this.log.info(this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ", Iteration " + optId
				+ ") Parameter Set: " + effectiveParams);

		try {
			invocation = replaceDefaultParameters(invocation);
		} catch (MissingParameterValueException e) {
			this.exceptions.add(e);
			return null;
		}

		return invocation;
	}

	/**
	 * Helper method for {@link #parseInvocationLineAndEffectiveParameters()}.
	 * 
	 * <p>
	 * All remaining parameters in the invocation line, that are not set to an
	 * actual value in the run configuration will be set to the default values
	 * of the corresponding parameters defined in the program configuration.
	 * Throw an exception if no value is set for a certain parameter-string.
	 * 
	 * @param invocation
	 * @return The invocation line with all parameters replaced.
	 * @throws MissingParameterValueException
	 * @throws InternalAttributeException
	 */
	protected String[] replaceDefaultParameters(String[] invocation)
			throws MissingParameterValueException, InternalAttributeException {
		String[] parsed = invocation.clone();
		for (int i = 0; i < parsed.length; i++) {
			while (parsed[i].contains("%")) {
				int pos = parsed[i].indexOf("%");
				int endPos = parsed[i].indexOf("%", pos + 1);
				// variable string at very end of invocation line
				if (endPos < 0)
					endPos = parsed[i].length();

				String param = parsed[i].substring(pos + 1, endPos);
				ProgramParameter<?> pa = programConfig
						.getParameterForName(param);
				int arrPos = programConfig.getParams().indexOf(pa);
				if (arrPos < 0) {
					throw new MissingParameterValueException(
							"No value for parameter \"" + param + "\" given");
				}

				String def = programConfig.getParams().get(arrPos)
						.evaluateDefaultValue(dataConfig, programConfig)
						.toString();
				parsed[i] = parsed[i].replace("%" + param + "%", def);

				effectiveParams.put(pa.getName(), def);
			}
		}
		return parsed;
	}

	/**
	 * Helper method for {@link #parseInvocationLineAndEffectiveParameters()}.
	 * 
	 * <p>
	 * Non-internal parameters are replaced, that means parameters, that are
	 * defined in the configuration files of the run or program in
	 * {@link #replaceRunParameters(String)}.
	 * 
	 * @param invocation
	 * @return The invocation line with replaced run parameters.
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 */
	@SuppressWarnings("unused")
	protected String[] replaceRunParameters(String[] invocation)
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {
		/*
		 * Now, replace the remaining parameters given in the run configuration.
		 */

		String[] parsed = invocation.clone();
		for (int i = 0; i < parsed.length; i++)
			for (ProgramParameter<?> param : runParams.keySet()) {
				parsed[i] = parsed[i].replace("%" + param.getName() + "%",
						runParams.get(param));
				effectiveParams.put(param.getName(), runParams.get(param));
			}
		return parsed;
	}

	/**
	 * Method invoked by {@link #doRun()} which performs a single iteration of
	 * the run. If this runnable is of type parameter optimization, this method
	 * is invoked several times. In case of a clustering run, it is invoked only
	 * once.
	 * 
	 * <p>
	 * First this method initializes all files and folder structures in
	 * {@link #initAndEnsureIterationFilesAndFolders()} such that the following
	 * computations can be performed smoothly.
	 * <p>
	 * It initializes all attribute variables needed throughout the process
	 * itself and by invoking
	 * {@link #parseInvocationLineAndEffectiveParameters()}.
	 * <p>
	 * The clustering method is executed with the given parameter values and
	 * settings asynchronously. It waits until the second process finishes.
	 * <p>
	 * The result file of the clustering method is converted to the standard
	 * result format by invoking {@link #convertResult()}.
	 * <p>
	 * Next the qualities of the converted result file are assessed in
	 * {@link #assessQualities(ClusteringRunResult)}.
	 * <p>
	 * Then it invokes {@link #writeQualitiesToFile(List)}, which writes the
	 * assessed cluster qualities into files on the filesystem.
	 * <p>
	 * In {@link #afterClustering(ClusteringRunResult)} all actions are
	 * performed, that require the clustering process to be finished beforehand.
	 * <p>
	 * Last the result is added to the list of run results of the corresponding
	 * run of this runnable.
	 * <p>
	 * In case the run result is missing or cannot be parsed successfully,
	 * {@link #handleMissingRunResult()} is responsible for performing actions
	 * ensuring, that the next iterations can be executed without problems.
	 * 
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws IOException
	 * @throws NoRunResultFormatParserException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 * @throws REXPMismatchException
	 * @throws REngineException
	 * @throws RLibraryNotLoadedException
	 * @throws RNotAvailableException
	 */
	protected void doRunIteration() throws InternalAttributeException,
			RegisterException, IOException, NoRunResultFormatParserException,
			NoParameterSetFoundException, RNotAvailableException,
			RLibraryNotLoadedException {
		if (this.isPaused()) {
			log.info("Pausing...");
			this.runningTime += System.currentTimeMillis() - this.lastStartTime;
			while (this.isPaused()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			log.info("Resuming...");
			this.lastStartTime = System.currentTimeMillis();
		}

		/*
		 * We check from time to time, whether this run got the order to
		 * terminate.
		 */
		if (checkForInterrupted())
			return;

		this.initAndEnsureIterationFilesAndFolders();

		/*
		 * We keep an array, which holds the parameter values that are
		 * effectively set for this run.
		 */
		this.effectiveParams = new HashMap<String, String>();

		this.internalParams = new HashMap<String, String>();

		String[] invocation = this.parseInvocationLineAndEffectiveParameters();

		this.log.debug(this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") Invoking command line: " + invocation);
		this.log.debug(this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") Log-File is located at: \""
				+ logFile.getAbsolutePath() + "\"");

		this.result = new ClusteringRunResult(this.getRun().getRepository(),
				System.currentTimeMillis(), clusteringResultFile, dataConfig,
				programConfig, format, runThreadIdentString, run);
		try {
			/*
			 * We check from time to time, whether this run got the order to
			 * terminate.
			 */
			if (checkForInterrupted())
				return;

			Process proc = programConfig.getProgram().exec(dataConfig,
					programConfig, invocation, effectiveParams, internalParams);

			BufferedWriter bw = new BufferedWriter(new FileWriter(logFile));

			if (proc != null) {
				new StreamGobbler(proc.getInputStream(), bw).start();
				new StreamGobbler(proc.getErrorStream(), bw).start();

				try {
					proc.waitFor();
				} catch (InterruptedException e) {

				}
			}

			/*
			 * We check from time to time, whether this run got the order to
			 * terminate.
			 */
			if (checkForInterrupted())
				return;

			ClusteringRunResult convertedResult = this.convertResult();

			if (convertedResult != null) {
				this.log.debug(this.getRun() + " (" + this.programConfig + ","
						+ this.dataConfig
						+ ") Finished converting result files");

				/*
				 * We check from time to time, whether this run got the order to
				 * terminate.
				 */
				if (checkForInterrupted())
					return;

				List<Pair<ParameterSet, ClusteringQualitySet>> qualities = this
						.assessQualities(convertedResult);
				// 04.04.2013: adding iteration number to qualities
				List<Triple<ParameterSet, ClusteringQualitySet, Long>> qualitiesWithIterations = new ArrayList<Triple<ParameterSet, ClusteringQualitySet, Long>>();
				for (Pair<ParameterSet, ClusteringQualitySet> pair : qualities)
					qualitiesWithIterations
							.add(Triple.getTriple(pair.getFirst(),
									pair.getSecond(), new Long(optId)));
				this.writeQualitiesToFile(qualitiesWithIterations);
				this.afterClustering(convertedResult, qualities);
			}

			/*
			 * Add this RunResult to the list. The RunResult only encapsulates
			 * the path to the result-file and does not hold any actual values,
			 * so we do not need to wait until the thread is finished.
			 */
			synchronized (this.getRun().getResults()) {
				this.getRun().getResults().add(result);
			}
		} catch (RunResultNotFoundException e) {
			this.handleMissingRunResult();
		} catch (RunResultConversionException e) {
			this.handleMissingRunResult();
		} catch (REngineException e1) {
			this.handleMissingRunResult();
		} catch (REXPMismatchException e1) {
			this.handleMissingRunResult();
		}
	}

	/**
	 * This method is responsible for assessing the qualities of a clustering
	 * run result. It takes the clusterings and passes them to
	 * {@link ClusteringRunResult#assessQuality(List)}.
	 * 
	 * @param convertedResult
	 *            The clustering result converted to the default format, such
	 *            that it can be parsed.
	 * @throws InvalidDataSetFormatVersionException
	 * @throws RunResultNotFoundException
	 */
	protected List<Pair<ParameterSet, ClusteringQualitySet>> assessQualities(
			ClusteringRunResult convertedResult)
			throws RunResultNotFoundException {
		this.log.debug(this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") Assessing quality of results...");
		List<Pair<ParameterSet, ClusteringQualitySet>> qualities = new ArrayList<Pair<ParameterSet, ClusteringQualitySet>>();
		try {
			final String qualityFile = this.internalParams.get("q");
			convertedResult.loadIntoMemory();
			final Pair<ParameterSet, Clustering> pair = convertedResult
					.getClustering();
			convertedResult.unloadFromMemory();
			ClusteringQualitySet quals = pair.getSecond().assessQuality(
					dataConfig, this.getRun().getQualityMeasures());
			qualities.add(Pair.getPair(pair.getFirst(), quals));
			for (ClusteringQualityMeasure qualityMeasure : quals.keySet()) {
				FileUtils.appendStringToFile(qualityFile,
						qualityMeasure.getClass().getSimpleName() + "\t"
								+ quals.get(qualityMeasure) + "\n");
			}

			this.log.debug(this.getRun() + " (" + this.programConfig + ","
					+ this.dataConfig + ") Finished quality calculations");
			return qualities;
		} catch (Exception e) {
			throw new RunResultNotFoundException("The result file "
					+ convertedResult.getAbsolutePath()
					+ " does not exist or could not been parsed!");
		}
	}

	/**
	 * Helper method of {@link #assessQualities(ClusteringRunResult)}, invoked
	 * to write the assessed clustering qualities into files.
	 * 
	 * @param qualities
	 *            A list containing pairs of parameter sets and corresponding
	 *            clustering qualities of different measures.
	 */
	protected void writeQualitiesToFile(
			List<Triple<ParameterSet, ClusteringQualitySet, Long>> qualities) {
		// 04.04.2013: adding iteration number into first column
		/*
		 * Write the qualities into the complete file
		 */
		for (Triple<ParameterSet, ClusteringQualitySet, Long> clustSet : qualities) {
			StringBuilder sb = new StringBuilder();
			sb.append(clustSet.getThird());
			sb.append("\t");
			for (int p = 0; p < programConfig.getOptimizableParams().size(); p++) {
				ProgramParameter<?> param = programConfig
						.getOptimizableParams().get(p);
				if (p > 0)
					sb.append(",");
				sb.append(clustSet.getFirst().get(param.getName()));
			}
			sb.append("\t");
			for (ClusteringQualityMeasure measure : this.getRun()
					.getQualityMeasures()) {
				sb.append(clustSet.getSecond().get(measure));
				sb.append("\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");

			FileUtils.appendStringToFile(completeQualityOutput, sb.toString());

			this.log.info(this.getRun() + " (" + this.programConfig + ","
					+ this.dataConfig + ") " + clustSet.getSecond().toString());
		}
	}

	/**
	 * A wrapper method for the conversion of the run result, which handles
	 * logging and adding the converted result to the results of the run.
	 * 
	 * @return The result of the last iteration converted to the standard
	 *         format.
	 * @throws NoRunResultFormatParserException
	 * @throws RunResultNotFoundException
	 * @throws SecurityException
	 * @throws RunResultConversionException
	 */
	protected ClusteringRunResult convertResult()
			throws NoRunResultFormatParserException,
			RunResultNotFoundException, RunResultConversionException {
		/*
		 * Converting and Quality of result
		 */
		this.log.debug(this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") Converting result files...");

		try {
			ClusteringRunResult convertedResult = result.convertTo(this
					.getRun().getContext().getStandardOutputFormat(),
					internalParams, effectiveParams);
			synchronized (this.getRun().getResults()) {
				this.getRun().getResults().add(convertedResult);
			}
			return convertedResult;
		} catch (NoRunResultFormatParserException e) {
			throw e;
		} catch (RunResultNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new RunResultConversionException(
					"The runresult could not be converted");
		}
	}

	/**
	 * This method is invoked by {@link #doRunIteration()} before any
	 * calculations are done, to ensure, that all folders and files are created
	 * such that the remainder process can be performed without problems.
	 * 
	 * <p>
	 * This method also initializes the file object attribute variables that are
	 * used throughout the process: {@link #logFile},
	 * {@link #clusteringResultFile} and {@link #resultQualityFile}.
	 */
	protected void initAndEnsureIterationFilesAndFolders() {
		/*
		 * Insert the unique identifier created earlier into the result paths by
		 * replacing the string "%RUNIDENTSTRING". And replace the %OPTID
		 * placeholder by the id corresponding to this optimization iteration
		 */
		String clusteringOutput;
		if (!isResume)
			clusteringOutput = FileUtils
					.buildPath(
							this.getRun()
									.getRepository()
									.getClusterResultsBasePath()
									.replace("%RUNIDENTSTRING",
											runThreadIdentString),
							programConfig + "_" + dataConfig + "." + optId
									+ ".results");
		else
			clusteringOutput = FileUtils
					.buildPath(
							this.getRun()
									.getRepository()
									.getParent()
									.getClusterResultsBasePath()
									.replace("%RUNIDENTSTRING",
											runThreadIdentString),
							programConfig + "_" + dataConfig + "." + optId
									+ ".results");

		String qualityOutput;
		if (!isResume)
			qualityOutput = FileUtils.buildPath(
					this.getRun().getRepository()
							.getClusterResultsQualityBasePath()
							.replace("%RUNIDENTSTRING", runThreadIdentString),
					programConfig + "_" + dataConfig + "." + optId
							+ ".results.qual");
		else
			qualityOutput = FileUtils.buildPath(
					this.getRun().getRepository().getParent()
							.getClusterResultsQualityBasePath()
							.replace("%RUNIDENTSTRING", runThreadIdentString),
					programConfig + "_" + dataConfig + "." + optId
							+ ".results.qual");

		String logOutput;
		if (!isResume)
			logOutput = FileUtils.buildPath(
					this.getRun().getRepository().getLogBasePath()
							.replace("%RUNIDENTSTRING", runThreadIdentString),
					programConfig + "_" + dataConfig + "." + optId + ".log");
		else
			logOutput = FileUtils.buildPath(
					this.getRun().getRepository().getParent().getLogBasePath()
							.replace("%RUNIDENTSTRING", runThreadIdentString),
					programConfig + "_" + dataConfig + "." + optId + ".log");

		/*
		 * if the output already exists, delete it to avoid complications safety
		 */
		if (new File(clusteringOutput).exists())
			FileUtils.delete(new File(clusteringOutput));
		if (new File(qualityOutput).exists())
			FileUtils.delete(new File(qualityOutput));

		/*
		 * Ensure that the directories to the result and log files exist.
		 */
		logFile = new File(logOutput);
		logFile.getParentFile().mkdirs();
		clusteringResultFile = new File(clusteringOutput);
		clusteringResultFile.getParentFile().mkdirs();
		resultQualityFile = new File(qualityOutput);
		resultQualityFile.getParentFile().mkdirs();
	}

	/**
	 * Overwrite this method in your subclass, if you want to handle missing run
	 * results individually.
	 * 
	 * <p>
	 * This can comprise actions ensuring that further iterations can be
	 * executed smoothly.
	 */
	protected abstract void handleMissingRunResult();

	/**
	 * After a clustering has been calculated by the program, converted to the
	 * standard format by the framework, and quality-assessed, this method
	 * performs all final actions that need to be done at the end of every
	 * iteration after successful clustering, e.g. create plots of the results
	 * of this iteration.
	 * 
	 * @param clusteringRunResult
	 *            The clustering run result of the last iteration in standard
	 *            format.
	 * @param qualities
	 *            The assessed qualities of the clustering of the last
	 *            iteration.
	 */
	@SuppressWarnings("unused")
	protected void afterClustering(
			final ClusteringRunResult clusteringRunResult,
			final List<Pair<ParameterSet, ClusteringQualitySet>> qualities) {
	}

	/**
	 * Set the internal attributes of the framework, e.g. the meanSimilarity
	 * attribute which holds the mean similarity of the input dataset. These
	 * internal attributes are then used later on, to replace parameter
	 * placeholders in the invocation line in
	 * {@link #parseInvocationLineAndEffectiveParameters()}.
	 * 
	 * <p>
	 * This method is invoked in {@link #beforeRun()}, thus is only evaluated
	 * once.
	 * 
	 * <p>
	 * The dataset in standard format is assumed to be loaded before this method
	 * is invoked and to be unloaded after return of this method.
	 * 
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	protected void setInternalAttributes() throws IllegalArgumentException {
		// TODO
		// DataSet ds = this.dataConfig.getDatasetConfig().getDataSet()
		// .getInStandardFormat();
		//
		// if (ds instanceof RelativeDataSet) {
		// RelativeDataSet dataSet = (RelativeDataSet) ds;
		// this.dataConfig
		// .getRepository()
		// .getInternalDoubleAttribute(
		// "$("
		// + this.dataConfig.getDatasetConfig()
		// .getDataSet().getOriginalDataSet()
		// .getAbsolutePath()
		// + ":minSimilarity)")
		// .setValue(dataSet.getDataSetContent().getMinValue());
		// this.dataConfig
		// .getRepository()
		// .getInternalDoubleAttribute(
		// "$("
		// + this.dataConfig.getDatasetConfig()
		// .getDataSet().getOriginalDataSet()
		// .getAbsolutePath()
		// + ":maxSimilarity)")
		// .setValue(dataSet.getDataSetContent().getMaxValue());
		// this.dataConfig
		// .getRepository()
		// .getInternalDoubleAttribute(
		// "$("
		// + this.dataConfig.getDatasetConfig()
		// .getDataSet().getOriginalDataSet()
		// .getAbsolutePath()
		// + ":meanSimilarity)")
		// .setValue(dataSet.getDataSetContent().getMean());
		// }
		// this.dataConfig
		// .getRepository()
		// .getInternalIntegerAttribute(
		// "$("
		// + this.dataConfig.getDatasetConfig()
		// .getDataSet().getOriginalDataSet()
		// .getAbsolutePath()
		// + ":numberOfElements)")
		// .setValue(ds.getIds().size());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.RunRunnable#beforeRun()
	 */
	@SuppressWarnings("unused")
	@Override
	protected void beforeRun() throws UnknownDataSetFormatException,
			InvalidDataSetFormatVersionException, IllegalArgumentException,
			IOException, RegisterException, InternalAttributeException,
			IncompatibleDataSetFormatException,
			UnknownGoldStandardFormatException,
			IncompleteGoldStandardException, RNotAvailableException {
		this.log.info("Run " + this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") " + (!isResume ? "started" : "RESUMED")
				+ " (asynchronously)");

		lastStartTime = System.currentTimeMillis();

		FileUtils.appendStringToFile(
				this.getRun().getLogFilePath(),
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tStarting runThread \""
						+ this.getRun()
						+ " ("
						+ this.programConfig
						+ ","
						+ this.dataConfig
						+ ")\"" + System.getProperty("line.separator"));

		this.format = programConfig.getOutputFormat();

		/*
		 * A Run holds actual values for the parameters of the program. If a
		 * parameter is set to a value here, this will overwrite the default
		 * value of the parameter defined in the program configuration.
		 */
		// 06.04.2013: changed from indexOf to manual search, because
		// programConfig of this runnable and of the run are not identical
		int p = -1;
		for (int i = 0; i < this.getRun().getProgramConfigs().size(); i++) {
			ProgramConfig programConfig = this.getRun().getProgramConfigs()
					.get(i);
			if (programConfig.getName().equals(this.programConfig.getName())) {
				p = i;
				break;
			}
		}
		this.runParams = this.getRun().getParameterValues().get(p);

		boolean found = preprocessAndCheckCompatibleDataSetFormat();
		if (!found) {
			List<DataSetFormat> dsFormats = new ArrayList<DataSetFormat>();
			for (DataSet ds : dataConfig.getDatasetConfig().getDataSets()) {
				dsFormats.add(ds.getDataSetFormat());
			}
			IncompatibleDataSetFormatException ex = new IncompatibleDataSetFormatException(
					"The program \"" + programConfig.getProgram()
							+ "\" cannot be run with the dataset formats \""
							+ dsFormats + "\"");
			// otherwise throw exception
			throw ex;
		}

		try {
			// Load the dataset into memory
			for (DataSet dataSet : this.dataConfig.getDatasetConfig()
					.getDataSets())
				dataSet.getInStandardFormat().loadIntoMemory();

			// if the original dataset is an absolute dataset, load it into
			// memory as well
			// for (DataSet dataSet : this.dataConfig.getDatasetConfig()
			// .getDataSets())
			// if (dataSet.getOriginalDataSet() instanceof AbsoluteDataSet)
			// dataSet.getOriginalDataSet().loadIntoMemory();
			// TODO: needed?

			/*
			 * Check compatibility of dataset with goldstandard
			 */
			if (this.dataConfig.hasGoldStandardConfig())
				checkCompatibilityDataSetGoldStandard(
						this.dataConfig.getDatasetConfig(),
						this.dataConfig.getGoldstandardConfig());
		} catch (IncompleteGoldStandardException e1) {
			// this.exceptions.add(e1);
			// 15.11.2012: missing entries in the goldstandard is no longer
			// a termination criterion. maybe introduce option
			// return;
			// 15.04.2013: since missing entries in the goldstandard distort
			// result qualities, we interrupt again when such an exception is
			// thrown. The user has the possibility of removing samples from the
			// data if this is the case.
			throw e1;
		}
		try {
			Plotter.assessAndWriteIsoMDSCoordinates(this.dataConfig);
		} catch (Exception e) {
		}

		try {
			Plotter.assessAndWritePCACoordinates(this.dataConfig);
		} catch (Exception e) {
		}

		setInternalAttributes();

		/*
		 * Ensure that the target directory exists
		 */
		if (!isResume)
			new File(this.getRun().getRepository()
					.getClusterResultsQualityBasePath()
					.replace("%RUNIDENTSTRING", runThreadIdentString)).mkdirs();
		else
			new File(this.getRun().getRepository().getParent()
					.getClusterResultsQualityBasePath()
					.replace("%RUNIDENTSTRING", runThreadIdentString)).mkdirs();

		/*
		 * Writing all the qualities of the optimization process into one file
		 */
		if (!isResume)
			completeQualityOutput = FileUtils
					.buildPath(
							this.getRun()
									.getRepository()
									.getClusterResultsQualityBasePath()
									.replace("%RUNIDENTSTRING",
											runThreadIdentString),
							programConfig + "_" + dataConfig
									+ ".results.qual.complete");
		else
			completeQualityOutput = FileUtils
					.buildPath(
							this.getRun()
									.getRepository()
									.getParent()
									.getClusterResultsQualityBasePath()
									.replace("%RUNIDENTSTRING",
											runThreadIdentString),
							programConfig + "_" + dataConfig
									+ ".results.qual.complete");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.runnable.RunRunnable#afterRun()
	 */
	@Override
	protected void afterRun() {
		super.afterRun();
		// unload the dataset from memory
		for (DataSet dataSet : this.dataConfig.getDatasetConfig().getDataSets()) {
			dataSet = dataSet.getInStandardFormat();
			if (dataSet != null)
				dataSet.unloadFromMemory();
		}
		// if the original dataset is an absolute dataset, unload it from
		// memory
		// as well
		// for (DataSet dataSet :
		// this.dataConfig.getDatasetConfig().getDataSets()) {
		// dataSet = dataSet.getOriginalDataSet();
		// if (dataSet != null && dataSet instanceof AbsoluteDataSet)
		// dataSet.unloadFromMemory();
		// }
		// TODO: needed?

		FileUtils.appendStringToFile(
				this.getRun().getLogFilePath(),
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tFinished runThread \""
						+ this.getRun()
						+ " ("
						+ this.programConfig
						+ ","
						+ this.dataConfig
						+ ")\" (Duration "
						+ Formatter.formatMsToDuration(runningTime
								+ (System.currentTimeMillis() - lastStartTime))
						+ ")" + System.getProperty("line.separator"));

		this.log.info("Run " + this.getRun() + " (" + this.programConfig + ","
				+ this.dataConfig + ") finished");
	}
}
