package de.clusteval.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ProgressPrinter;
import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.context.Context;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.result.GraphMatchingRunResult;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.runnable.RunRunnable;
import de.clusteval.run.runnable.RunRunnableInitializationException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;
import format.Formatter;

/**
 * A representation of an abstract run including configurations and results. A
 * run is an entity, that can be performed by the framework. Depending on the
 * concrete subclass of the run, actions during execution differ. After
 * execution of the run results are stored in {@link #results}.
 * 
 * <p>
 * A run corresponds to a *.run-file on the file system in the run-directory of
 * the repository. The name of the run is deduced from the filesystem in
 * {@link #getName()}, thus it is unique for every repository.
 * 
 * <p>
 * Every time a run is performed, its results are also stored in a new
 * subdirectory in the results-directory of the repository. The subdirectory is
 * named after the {@link #runIdentString}, which consists of a time-stamp and
 * the name of this run.
 * 
 * <p>
 * When a run is performed, it's divided into a number of atomic operations that
 * can be performed in parallel, objects of subclasses of {@link RunRunnable}.
 * Those are then passed to the run scheduler and are performed in any order in
 * parallel.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class Run extends RepositoryObject {

	/**
	 * This method is invoked by different copy-constructors of subclasses of
	 * this Run, e.g. by ExecutionRun.ExecutionRun(ExecutionRun) to clone a
	 * given run object.
	 * 
	 * <p>
	 * It is a convenience method to clone a map containing parameter values.
	 * 
	 * @param parameterValues
	 * @return
	 */
	protected static List<Map<ProgramParameter<?>, String>> cloneParameterValues(
			List<Map<ProgramParameter<?>, String>> parameterValues) {
		List<Map<ProgramParameter<?>, String>> result = new ArrayList<Map<ProgramParameter<?>, String>>();

		for (Map<ProgramParameter<?>, String> map : parameterValues) {
			Map<ProgramParameter<?>, String> copyMap = new HashMap<ProgramParameter<?>, String>();
			for (Map.Entry<ProgramParameter<?>, String> entry : map.entrySet()) {
				copyMap.put(entry.getKey().clone(), entry.getValue() + "");
			}
			result.add(copyMap);
		}

		return result;
	}

	/**
	 * This method parses a {@link Run} object from a *.run file. It parses all
	 * values from this file and ensures, that the values of the parameters are
	 * valid.
	 * 
	 * <p>
	 * Depending on the type of the run defined by the *.run file, this method
	 * dedicates the parsing of the file to parseFromFile methods of the
	 * corresponding subclass.
	 * 
	 * @param absPath
	 *            the abs path
	 * @return A {@link Run} object.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnknownRunResultFormatException
	 *             If the program configuration of one of the programs belonging
	 *             to this run contains an unknown RunResultFormat, this
	 *             exception is thrown.
	 * @throws UnknownDataSetFormatException
	 *             If the program configuration of one of the programs belonging
	 *             to this run contains an unknown DataSetFormat, this exception
	 *             is thrown.
	 * @throws InvalidConfigurationFileException
	 *             If any of the configuration files is missing a required
	 *             value, this exception is thrown.
	 * @throws UnknownQualityMeasureException
	 *             the unknown clustering quality measure exception
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws InvalidRunModeException
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws NoOptimizableProgramParameterException
	 * @throws UnknownGoldStandardFormatException
	 * @throws UnknownProgramParameterException
	 * @throws NoRepositoryFoundException
	 * @throws GoldStandardNotFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws GoldStandardConfigurationException
	 * @throws DataSetNotFoundException
	 * @throws DataSetConfigurationException
	 * @throws DataSetConfigNotFoundException
	 * @throws GoldStandardConfigNotFoundException
	 * @throws DataConfigNotFoundException
	 * @throws DataConfigurationException
	 * @throws RunException
	 * @throws UnknownDataStatisticException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
	 * @throws UnknownRunStatisticException
	 * @throws UnknownRunDataStatisticException
	 * @throws ConfigurationException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws NumberFormatException
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownContextException
	 * @throws IncompatibleContextException
	 * @throws UnknownParameterType
	 */
	@SuppressWarnings("unused")
	public static Run parseFromFile(final File absPath) throws IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			InvalidConfigurationFileException, UnknownQualityMeasureException,
			FileNotFoundException, InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownGoldStandardFormatException,
			UnknownProgramParameterException, NoRepositoryFoundException,
			GoldStandardNotFoundException,
			InvalidOptimizationParameterException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			UnknownRunDataStatisticException, ConfigurationException,
			RegisterException, UnknownDataSetTypeException,
			NumberFormatException, NoDataSetException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType {

		HierarchicalINIConfiguration props;
		try {
			props = new HierarchicalINIConfiguration(absPath.getAbsolutePath());
		} catch (ConfigurationException e) {
			throw new FileNotFoundException(
					"The given run file does not exist or is not accessible.");
		}
		props.setThrowExceptionOnMissing(true);

		Logger log = LoggerFactory.getLogger(Run.class);

		log.debug("Parsing run \"" + absPath + "\"");
		Run r = null;
		try {
			String runMode = props.getString("mode");

			/*
			 * If none is defined, default mode is clustering.
			 */
			if (runMode == null || runMode.equals("clustering"))
				r = ClusteringRun.parseFromFile(absPath);
			else if (runMode.equals("parameter_optimization"))
				r = ParameterOptimizationRun.parseFromFile(absPath);
			/*
			 * Added at 3rd of july 2012
			 */
			else if (runMode.equals("internal_parameter_optimization"))
				r = InternalParameterOptimizationRun.parseFromFile(absPath);
			/*
			 * Added 29 of july 2012
			 */
			else if (runMode.equals("dataAnalysis"))
				r = DataAnalysisRun.parseFromFile(absPath);
			/*
			 * Added 20 of august 2012
			 */
			else if (runMode.equals("runAnalysis"))
				r = RunAnalysisRun.parseFromFile(absPath);
			/*
			 * Added 22 of august 2012
			 */
			else if (runMode.equals("runDataAnalysis"))
				r = RunDataAnalysisRun.parseFromFile(absPath);
			else
				throw new InvalidRunModeException("The given mode is invalid: "
						+ runMode);

		} catch (NoSuchElementException e) {
			throw new InvalidConfigurationFileException("The run file "
					+ absPath + " is missing an entry: "
					+ e.getLocalizedMessage());
		} catch (ConfigurationException e) {
			throw new FileNotFoundException(
					"The given run configuration file does not exist or is not accessible.");
		}
		log.debug("Run parsed");
		return r;
	}

	/**
	 * The starting time of the execution of this run. Is used to calculate the
	 * duration of the execution afterwards.
	 */
	protected long startTime;

	/**
	 * Unique identifier of this run - consists of a time & date stamp, as well
	 * as the name of the run. This string is inserted into the results path of
	 * every run, to avoid overwriting of any files (input, output, whatsoever)
	 * between several runs.
	 */
	protected String runIdentString;

	/**
	 * After this run was performed using the {@link #perform()} method, all the
	 * results are stored in this list. If this run is a ExecutionRun this list
	 * contains one {@link GraphMatchingRunResult} object for every executed
	 * combination of program and dataset.
	 */
	protected List<RunResult> results;

	/**
	 * The path to the log file in the results-directory of the execution of
	 * this run.
	 */
	protected String logFilePath;

	/**
	 * Keeps track of the progress of this run when it is executed. Can be used
	 * to get a percental status.
	 */
	protected volatile ProgressPrinter progress;

	/**
	 * The status of this run.
	 * <p>
	 * Initially when a Run object is created its status is
	 * {@link RUN_STATUS.INACTIVE}.
	 * 
	 * <p>
	 * When a Run should be performed it is passed to the run scheduler. Then
	 * the runs status is {@link RUN_STATUS.SCHEDULED}.
	 * 
	 * <p>
	 * As soon as the run is started by the scheduler, the runs status is
	 * {@link RUN_STATUS.RUNNING}.
	 * 
	 * <p>
	 * After the runs completion its status is {@link RUN_STATUS.FINISHED}.
	 * 
	 * <p>
	 * When the run is terminated by the user during its execution and before
	 * it's finished its status is {@link RUN_STATUS.TERMINATED}.
	 */
	protected RUN_STATUS status;

	/**
	 * Contains the runnable objects created during the execution of this run.
	 */
	protected List<RunRunnable> runnables;

	/**
	 * Every run belongs to a context.
	 */
	protected Context context;

	/**
	 * The constructor of this class takes a date and configuration. It is
	 * protected, to force usage of the static method
	 * 
	 * @param repository
	 *            the repository
	 * @param context
	 *            The context of this run
	 * @param changeDate
	 *            The date this run was performed.
	 * @param absPath
	 *            The absolute path to the file on the filesystem that
	 *            corresponds to this run.
	 * @throws RegisterException
	 */
	protected Run(final Repository repository, final Context context,
			final long changeDate, final File absPath) throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.runnables = new ArrayList<RunRunnable>();
		this.status = RUN_STATUS.INACTIVE;
		this.context = context;
	}

	/**
	 * A copy constructor for the Run class.
	 * 
	 * <p>
	 * Runnables, run results and status of the given run are not copied into
	 * the new run.
	 * 
	 * @param otherRun
	 *            The run to clone.
	 * @throws RegisterException
	 */
	protected Run(final Run otherRun) throws RegisterException {
		super(otherRun);

		this.runnables = new ArrayList<RunRunnable>();
		this.status = RUN_STATUS.INACTIVE;
		this.context = otherRun.context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public abstract Run clone();

	/**
	 * This method is invoked by {@link #perform(RunSchedulerThread)} after
	 * completion of {@link #doPerform(RunSchedulerThread)}.
	 * 
	 * <p>
	 * Override this method to do any postcalculations on the run results after
	 * everything is finished.
	 */
	protected void afterPerform() {
		FileUtils.appendStringToFile(
				this.logFilePath,
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tFinished run \""
						+ this.getName()
						+ "\" (Duration "
						+ Formatter.formatMsToDuration(System
								.currentTimeMillis() - startTime)
						+ ")"
						+ System.getProperty("line.separator"));
		this.log.info("Run " + this + " - All processes finished");
		this.setStatus(RUN_STATUS.FINISHED);
	}

	/**
	 * This method is invoked by {@link #resume(RunSchedulerThread, String)}
	 * after completion of {@link #doResume(RunSchedulerThread, String)}.
	 * 
	 * <p>
	 * Override this method to do any postcalculations on the run results after
	 * everything is finished.
	 * 
	 * @param runIdentString
	 *            The unique run identifier of the results directory,
	 *            corresponding to an execution of a run, that should by
	 *            resumed.
	 */
	@SuppressWarnings("unused")
	protected void afterResume(final String runIdentString) {
		FileUtils.appendStringToFile(
				this.logFilePath,
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tFinished run \""
						+ this.getName()
						+ "\" (Duration "
						+ Formatter.formatMsToDuration(System
								.currentTimeMillis() - startTime)
						+ ")"
						+ System.getProperty("line.separator"));
		this.log.info("Run " + this + " - All processes finished");
		this.setStatus(RUN_STATUS.FINISHED);
	}

	/**
	 * This method is invoked by {@link #perform(RunSchedulerThread)} before
	 * {@link #doPerform(RunSchedulerThread)} is invoked.
	 * 
	 * <p>
	 * Override this method in subclasses to do any operation, that should only
	 * be done once per run execution, and before any runnable is started. This
	 * can be useful for logging or for filesystem operations like copying any
	 * input files to avoid overriding files unintentionally when they would be
	 * performed in the runnables asynchronously instead.
	 * 
	 * @throws IOException
	 */
	protected void beforePerform() throws IOException {
		log.info("Starting run \"" + this.getName() + "\"");
		log.info("Run mode is \"" + this.getClass().getSimpleName() + "\"");
		/*
		 * Change the status of this run
		 */
		// this.setStatus(RUN_STATUS.RUNNING);

		/*
		 * Initialize a ProgressPrinter, which keeps the progression of the run
		 */
		this.progress = new ProgressPrinter(getUpperLimitProgress(), true);

		initRunIdentificationString();

		this.startTime = System.currentTimeMillis();

		this.copyConfigurationFiles(false);

		this.logFilePath = FileUtils.buildPath(new File(this.getRepository()
				.getClusterResultsBasePath()).getParentFile().getAbsolutePath()
				.replace("%RUNIDENTSTRING", runIdentString), "logs",
				runIdentString + ".log");
		if (!new File(this.logFilePath).exists()) {
			new File(this.logFilePath).getParentFile().mkdirs();
			new File(this.logFilePath).createNewFile();
		}

		FileUtils.appendStringToFile(
				this.logFilePath,
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tStarting run \""
						+ this.getName()
						+ "\"" + System.getProperty("line.separator"));
		/*
		 * All threads are stored in this list, to be able to wait for them
		 * asynchronously later on.
		 */
		this.runnables.clear();

		/*
		 * Reset the results list
		 */
		this.results = new ArrayList<RunResult>();
	}

	/**
	 * This method is invoked by {@link #resume(RunSchedulerThread, String)}
	 * before {@link #doResume(RunSchedulerThread, String)} is invoked.
	 * 
	 * <p>
	 * Override this method in subclasses to do any operation, that should only
	 * be done once per run execution, and before any runnable is started. This
	 * can be useful for logging or for filesystem operations like copying any
	 * input files to avoid overriding files unintentionally when they would be
	 * performed in the runnables asynchronously instead.
	 * 
	 * @param runIdentString
	 *            The unique run identifier of the results directory,
	 *            corresponding to an execution of a run, that should by
	 *            resumed.
	 * 
	 * @throws IOException
	 */
	protected void beforeResume(final String runIdentString) throws IOException {
		log.info("RESUMING run \"" + this.getName() + "\"");
		log.info("Run mode is \"" + this.getClass().getSimpleName() + "\"");
		/*
		 * Change the status of this run
		 */
		// this.status = RUN_STATUS.RUNNING;

		/*
		 * Initialize a ProgressPrinter, which keeps the progression of the run
		 */
		this.progress = new ProgressPrinter(getUpperLimitProgress(), true);

		this.runIdentString = runIdentString;

		this.startTime = System.currentTimeMillis();

		this.copyConfigurationFiles(true);

		this.logFilePath = FileUtils.buildPath(new File(this.getRepository()
				.getParent().getClusterResultsBasePath()).getParentFile()
				.getAbsolutePath().replace("%RUNIDENTSTRING", runIdentString),
				"logs", runIdentString + ".log");
		if (!new File(this.logFilePath).exists()) {
			new File(this.logFilePath).getParentFile().mkdirs();
			new File(this.logFilePath).createNewFile();
		}

		FileUtils.appendStringToFile(
				this.logFilePath,
				Formatter.currentTimeAsString(true, "MM_dd_yyyy-HH_mm_ss",
						Locale.UK)
						+ "\tRESUMING run \""
						+ this.getName()
						+ "\"" + System.getProperty("line.separator"));

		/*
		 * All threads are stored in this list, to be able to wait for them
		 * asynchronously later on.
		 */
		this.runnables.clear();

		/*
		 * Reset the results list
		 */
		this.results = new ArrayList<RunResult>();
	}

	/**
	 * When this run is performed, this method copies all configuration files to
	 * the results directory.
	 * <p>
	 * This method is invoked by {@link #beforePerform()} or
	 * {@link #beforeResume(String)}. Thus it is not executed asynchronously to
	 * avoid overwriting of several threads in the result directory.
	 * 
	 * @param isResume
	 *            Indicates, whether the execution of this run is a resumption
	 *            or not.
	 */
	protected void copyConfigurationFiles(final boolean isResume) {
		/*
		 * Copy all the configuration files for later reproducability
		 */
		if (!isResume) {
			File movedConfigsDir = getMovedConfigsDir();
			movedConfigsDir.mkdirs();
			this.copyToFolder(movedConfigsDir);
		} else {
			File movedConfigsDir = new File(FileUtils.buildPath(
					new File(this.getRepository().getParent()
							.getClusterResultsBasePath()).getParentFile()
							.getAbsolutePath()
							.replace("%RUNIDENTSTRING", runIdentString),
					"configs"));
			movedConfigsDir.mkdirs();
			this.copyToFolder(movedConfigsDir, false);
		}
	}

	/**
	 * This method will be invoked by {@link #doResume(RunSchedulerThread)} to
	 * create the p'th runnable for the resumption of an execution of this run
	 * and to submit it to the run scheduler.
	 * 
	 * @param runScheduler
	 *            The run scheduler to which the newly created runnable should
	 *            be passed.
	 * @param p
	 *            The index of the runnable to be created.
	 * @throws RunRunnableInitializationException
	 */
	protected abstract RunRunnable createAndScheduleRunnableForResumePair(
			RunSchedulerThread runScheduler, int p)
			throws RunRunnableInitializationException;

	/**
	 * This method will be invoked by {@link #doPerform(RunSchedulerThread)} to
	 * create the p'th runnable for the execution of this run and to submit it
	 * to the run scheduler.
	 * 
	 * @param runScheduler
	 *            The run scheduler to which the newly created runnable should
	 *            be passed.
	 * @param p
	 *            The index of the runnable to be created.
	 * @throws RunRunnableInitializationException
	 */
	protected abstract RunRunnable createAndScheduleRunnableForRunPair(
			RunSchedulerThread runScheduler, int p)
			throws RunRunnableInitializationException;

	/**
	 * This method creates instances of subclasses of RunRunnable and passes
	 * them to the run scheduler. The run scheduler then executes the run
	 * runnables at some point in the future depending on the available physical
	 * ressources.
	 * 
	 * <p>
	 * The RunRunnable objects represent atomic peaces of the overall run, which
	 * can be executed in parallel.
	 * 
	 * @param runScheduler
	 *            The run scheduler, this run should be executed by.
	 * @throws RunRunnableInitializationException
	 */
	public void doPerform(final RunSchedulerThread runScheduler)
			throws RunRunnableInitializationException {

		for (int p = 0; p < getNumberOfRunRunnables(); p++) {
			RunRunnable r = createAndScheduleRunnableForRunPair(runScheduler, p);
			this.runnables.add(r);
		}
	}

	/**
	 * This method will perform this run, i.e. it combines every program with
	 * every dataset contained in this run's configuration. For every such
	 * combination the result of this will be added to the list of run results
	 * 
	 * @param runScheduler
	 *            The run scheduler, this run should be executed by.
	 * @param runIdentString
	 *            The unique run identifier of the results directory,
	 *            corresponding to an execution of a run, that should by
	 *            resumed.
	 * @throws RunRunnableInitializationException
	 */
	@SuppressWarnings("unused")
	public void doResume(final RunSchedulerThread runScheduler,
			final String runIdentString)
			throws RunRunnableInitializationException {

		/*
		 * Execute every "runpair", consisting of a program (i.e. its
		 * configuration) and a dataset (i.e. its configuration).
		 */
		for (int p = 0; p < getNumberOfRunRunnables(); p++) {
			RunRunnable r = createAndScheduleRunnableForResumePair(
					runScheduler, p);
			this.runnables.add(r);
		}
	}

	/**
	 * @see #logFilePath
	 * @return The path to the log file when this run is executed.
	 */
	public String getLogFilePath() {
		return this.logFilePath;
	}

	/**
	 * This method constructs and returns the path to the configuration
	 * subdirectory in the results directory of this run execution.
	 * 
	 * <p>
	 * This method should only be invoked while a run is executed, because only
	 * then the unique run identification string is set.
	 * 
	 * @return The path to the configuration subdirectory in the results
	 *         directory of this run execution.
	 */
	protected File getMovedConfigsDir() {
		return new File(FileUtils.buildPath(new File(this.getRepository()
				.getClusterResultsBasePath()).getParentFile().getAbsolutePath()
				.replace("%RUNIDENTSTRING", runIdentString), "configs"));
	}

	/**
	 * This method constructs and returns the path to the goldstandard
	 * subdirectory in the results directory of this run execution.
	 * 
	 * <p>
	 * This method should only be invoked while a run is executed, because only
	 * then the unique run identification string is set.
	 * 
	 * @return The path to the goldstandard subdirectory in the results
	 *         directory of this run execution.
	 */
	protected File getMovedGoldStandardsDir() {
		return new File(FileUtils.buildPath(new File(this.getRepository()
				.getClusterResultsBasePath()).getParentFile().getAbsolutePath()
				.replace("%RUNIDENTSTRING", runIdentString), "goldstandards"));
	}

	/**
	 * This method constructs and returns the path to the dataset subdirectory
	 * in the results directory of this run execution.
	 * 
	 * <p>
	 * This method should only be invoked while a run is executed, because only
	 * then the unique run identification string is set.
	 * 
	 * @return The path to the dataset subdirectory in the results directory of
	 *         this run execution.
	 */
	protected File getMovedInputsDir() {
		return new File(FileUtils.buildPath(new File(this.getRepository()
				.getClusterResultsBasePath()).getParentFile().getAbsolutePath()
				.replace("%RUNIDENTSTRING", runIdentString), "inputs"));
	}

	/**
	 * The name of a run is used to uniquely identify runs.
	 * 
	 * <p>
	 * The name of a run is deduced from the filename of the run file. It is the
	 * filename without file extension.
	 * 
	 * @return the name
	 */
	public String getName() {
		return absPath.getName().replace(".run", "");
	}

	/**
	 * @return The number of run runnables this run will create. This number
	 *         will be used in the {@link #doPerform(RunSchedulerThread)} method
	 *         to create the correct number of runnables.
	 */
	protected abstract int getNumberOfRunRunnables();

	/**
	 * Returns the current status of this run execution in terms of finished
	 * percentage. Depending on the current status of the run, this method
	 * returns different values:
	 * <p>
	 * If the run is scheduled, finished or terminated this method always
	 * returns 100%.
	 * <p>
	 * If the run is currently running, the percentage of the execution is
	 * returned.
	 * <p>
	 * Otherwise this method returns 0%.
	 * 
	 * @return The percent finished
	 */
	public float getPercentFinished() {
		if (this.status.equals(RUN_STATUS.SCHEDULED))
			return 100;
		else if (this.status.equals(RUN_STATUS.RUNNING))
			synchronized (this.progress) {
				return this.progress.getPercent();
			}
		else if (this.status.equals(RUN_STATUS.FINISHED))
			return 100;
		else if (this.status.equals(RUN_STATUS.TERMINATED))
			return 100;
		return 0;
	}

	/**
	 * Gets the results.
	 * 
	 * @return Get the list of run results that are produced by the execution of
	 *         this run.
	 */
	public List<RunResult> getResults() {
		return this.results;
	}

	/**
	 * @see #runIdentString
	 * @return The unique run identification string created when this run is
	 *         executed.
	 */
	public String getRunIdentificationString() {
		return this.runIdentString;
	}

	/**
	 * @return A list containing all run runnables this run created during its
	 *         execution.
	 */
	public List<RunRunnable> getRunRunnables() {
		return this.runnables;
	}

	/**
	 * @see #status
	 * @return the status
	 */
	public RUN_STATUS getStatus() {
		return this.status;
	}

	/**
	 * Implement this method in subclasses to provide the number of steps this
	 * run performs before it is finished. This method is then later on used by
	 * the method {@link #getPercentFinished()} to calculate the finished
	 * percentage.
	 * 
	 * @return
	 */
	protected abstract long getUpperLimitProgress();

	/**
	 * This method sets the unique run identification string of this run when it
	 * is executed. This unique identifier is also used, amongst other things,
	 * as the name of the results directory of this run.
	 */
	private void initRunIdentificationString() {
		this.runIdentString = Formatter.currentTimeAsString(true,
				"MM_dd_yyyy-HH_mm_ss", Locale.UK) + "_" + this.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#notify(utils.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (event.getOld().equals(this))
				super.notify(event);
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this))
				super.notify(event);
		}
	}

	/**
	 * This method will perform this run.
	 * <p>
	 * First this method invokes {@link #beforePerform()} and does all necessary
	 * preoperations before the actual run is started. Afterwards in
	 * {@link #doPerform(RunSchedulerThread)} the runnables are created and
	 * executed asynchronously by the run scheduler. Then invoking
	 * {@link #waitForRunnablesToFinish()} guarantees that the method waits
	 * until all calculations are finished. Last {@link #afterPerform()}
	 * performs additional operations with the results after all calculations
	 * are finished.
	 * 
	 * @param runScheduler
	 *            The run scheduler, this run should be executed by.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws RunRunnableInitializationException
	 */
	public void perform(final RunSchedulerThread runScheduler)
			throws IOException, RunRunnableInitializationException {
		beforePerform();
		doPerform(runScheduler);
		waitForRunnablesToFinish();
		afterPerform();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() throws RegisterException {
		return this.repository.register(this);
	}

	/**
	 * This method will resume a previously started run. This method should only
	 * be invoked on a run, that was parsed from a runresult folder. Otherwise
	 * it will show unexpected behaviour.
	 * 
	 * @param runScheduler
	 *            The run scheduler, this run should be executed by.
	 * @param runIdentString
	 *            The unique run identifier of the results directory,
	 *            corresponding to an execution of a run, that should by
	 *            resumed.
	 * 
	 * @throws MissingParameterValueException
	 *             If a parameter required in the invocation line of some
	 *             program, is neither set in the program nor in the run
	 *             configuration, an exception will be thrown.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws NoRunResultFormatParserException
	 *             For every {@link RunResultFormat} there needs to be a parser,
	 *             that converts this format into the default format of the
	 *             framework for later analysis. If no such parser exists for
	 *             some format, this exception will be thrown.
	 * @throws RunRunnableInitializationException
	 */
	@SuppressWarnings("unused")
	public void resume(final RunSchedulerThread runScheduler,
			final String runIdentString) throws MissingParameterValueException,
			IOException, NoRunResultFormatParserException,
			RunRunnableInitializationException {
		beforeResume(runIdentString);
		doResume(runScheduler, runIdentString);
		waitForRunnablesToFinish();
		afterResume(runIdentString);
	}

	/**
	 * Sets the status of this run.
	 * 
	 * @param status
	 */
	public void setStatus(RUN_STATUS status) {
		this.status = status;
		// 31.08.2012
		// update status in DB
		this.getRepository().updateStatusOfRun(this, status.toString());
	}

	/**
	 * This method terminates the execution of this run. It waits for the
	 * termination of all corresponding threads.
	 * 
	 * @return True if everything, including all corresponding threads, could be
	 *         terminated, false otherwise.
	 */
	public abstract boolean terminate();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Run:" + this.getName() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/**
	 * @return A map with the optimization status of this run.
	 */
	// TODO
	// public Map<Pair<String, String>, Map<String, Pair<Map<String, Double>,
	// Double>>> getOptimizationStatus() {
	// Map<Pair<String, String>, Map<String, Pair<Map<String, Double>, Double>>>
	// result = new HashMap<Pair<String, String>, Map<String, Pair<Map<String,
	// Double>, Double>>>();
	// try {
	// for (RunRunnable thread : this.runnables) {
	// Pair<String, String> configs = Pair.getPair(
	// thread.programConfig.toString(),
	// thread.dataConfig.toString());
	// ClusteringQualitySet qualTmp = thread.optimizationMethod
	// .getResult().getOptimalCriterionValue();
	// Map<ClusteringQualityMeasure, ParameterSet> params =
	// thread.optimizationMethod
	// .getResult().getOptimalParameterSets();
	// Map<ClusteringQualityMeasure, Map<String, Double>> paramTmp = new
	// HashMap<ClusteringQualityMeasure, Map<String, Double>>();
	// for (ClusteringQualityMeasure measure : params.keySet()) {
	// ParameterSet pSet = params.get(measure);
	// Map<String, Double> tmp = new HashMap<String, Double>();
	// for (ProgramParameter p : pSet.keySet())
	// tmp.put(p.toString(), pSet.get(p));
	//
	// paramTmp.put(measure, tmp);
	// }
	//
	// Map<String, Pair<Map<String, Double>, Double>> qualities = new
	// HashMap<String, Pair<Map<String, Double>, Double>>();
	// for (ClusteringQualityMeasure measure : qualTmp.keySet())
	// qualities.put(
	// measure.getClass().getSimpleName(),
	// Pair.getPair(paramTmp.get(measure),
	// qualTmp.get(measure)));
	//
	// result.put(configs, qualities);
	// }
	// } catch (Exception e) {
	// }
	// return result;
	// }

	/**
	 * This method is invoked by {@link #perform(RunSchedulerThread)}, after
	 * completion of {@link #doPerform(RunSchedulerThread)}.
	 * 
	 * <p>
	 * It waits, until all threads (corresponding to created runnables) are
	 * finished.
	 * 
	 * <p>
	 * During this time, it updates the progress after completion of single
	 * threads, such that {@link #getPercentFinished()} returns the correct
	 * value.
	 * 
	 * <p>
	 * Additionally it checks, whether any of the threads threw exceptions and
	 * prints those exceptions.
	 * 
	 */
	protected void waitForRunnablesToFinish() {
		this.log.info("Run " + this + " - Waiting for runs to finish...");
		for (RunRunnable r : runnables) {
			RunRunnable t = r;
			try {
				try {
					t.waitFor();
				} catch (CancellationException e) {
				} catch (ExecutionException e) {
				}
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * 
	 * @return The context of this run.
	 */
	public Context getContext() {
		return this.context;
	}
}
