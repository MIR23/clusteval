package de.clusteval.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ArraysExt;
import utils.Pair;
import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.ParameterOptimizationMethod;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.context.Context;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
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
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.runnable.ExecutionRunRunnable;
import de.clusteval.run.runnable.ParameterOptimizationRunRunnable;
import file.FileUtils;

/**
 * A type of execution run that performs several clusterings with different
 * parameter sets determined in an automatized way for every pair of program and
 * data configuration.
 * 
 * <p>
 * The evaluated parameter sets during a parameter optimization for one pair of
 * program and data configuration are determined by the corresponding
 * {@link ParameterOptimizationMethod} stored in {@link #optimizationMethods}.
 * 
 * <p>
 * Every evaluated parameter set is stored in the
 * {@link #optimizationParameters} attribute, such that evaluation of the
 * results is possible after termination of the run.
 * 
 * <p>
 * The results of the clusterings evaluated for every parameter set are also
 * stored in the {@link ParameterOptimizationMethod} object.
 * 
 * @author Christian Wiwie
 * 
 */
public class ParameterOptimizationRun extends ExecutionRun {

	/**
	 * This method verifies compatibility between a parameter optimization
	 * method, the data input format and the program configuration.
	 * 
	 * <p>
	 * Some parameter optimization do only work for certain programs, e.g.
	 * {@link GapStatisticParameterOptimizationMethod } works only for
	 * {@link KMeansClusteringRProgram} and {@link AbsoluteDataSetFormat}.
	 * 
	 * @param dataConfigs
	 * @param programConfigs
	 * @param optimizationMethods
	 * @throws IncompatibleParameterOptimizationMethodException
	 * 
	 */
	protected static void checkCompatibilityParameterOptimizationMethod(
			final List<ParameterOptimizationMethod> optimizationMethods,
			final List<ProgramConfig> programConfigs,
			final List<DataConfig> dataConfigs)
			throws IncompatibleParameterOptimizationMethodException {
		// TODO
		// for (ParameterOptimizationMethod method : optimizationMethods) {
		// if (!method.getCompatibleDataSetFormatBaseClasses().isEmpty()) {
		// // for every datasetformat we check, whether it class is
		// // compatible
		// for (DataConfig dataConfig : dataConfigs) {
		// Class<? extends DataSetFormat> dataSetFormatClass = dataConfig
		// .getDatasetConfig().getDataSet().getDataSetFormat()
		// .getClass();
		// boolean compatible = false;
		// for (Class<? extends DataSetFormat> parentClass : method
		// .getCompatibleDataSetFormatBaseClasses()) {
		// if (parentClass.isAssignableFrom(dataSetFormatClass)) {
		// compatible = true;
		// break;
		// }
		// }
		// if (!compatible) {
		// throw new IncompatibleParameterOptimizationMethodException(
		// "The ParameterOptimizationMethod "
		// + method.getClass().getSimpleName()
		// + " cannot be applied to the dataset "
		// + dataConfig.getDatasetConfig()
		// .getDataSet()
		// + " with the format "
		// + dataSetFormatClass.getSimpleName());
		// }
		// }
		// }
		//
		// if (!method.getCompatibleProgramNames().isEmpty()) {
		// // for every program we check, whether it class is
		// // compatible
		// for (ProgramConfig programConfig : programConfigs) {
		// String programName = programConfig.getProgram()
		// .getMajorName();
		// boolean compatible = method.getCompatibleProgramNames()
		// .contains(programName);
		// if (!compatible) {
		// throw new IncompatibleParameterOptimizationMethodException(
		// "The ParameterOptimizationMethod "
		// + method.getClass().getSimpleName()
		// + " cannot be applied to the program "
		// + programName);
		// }
		// }
		// }
		// }
	}

	/**
	 * This list holds another list of optimization parameters for every program
	 * configuration. These optimization parameters are to be optimized by this
	 * run.
	 */
	protected List<List<ProgramParameter<?>>> optimizationParameters;

	/**
	 * This list holds the parameter optimization methods for every pair of
	 * program and data configuration. These method objects control and keep
	 * track of the parameter sets and the results.
	 */
	protected List<ParameterOptimizationMethod> optimizationMethods;

	/**
	 * New objects of this type are automatically registered at the repository.
	 * 
	 * @param repository
	 *            the repository
	 * @param context
	 * @param changeDate
	 *            The date this run was performed.
	 * @param absPath
	 *            The absolute path to the file on the filesystem that
	 *            corresponds to this run.
	 * @param programConfigs
	 *            The program configurations of the new run.
	 * @param dataConfigs
	 *            The data configurations of the new run.
	 * @param qualityMeasures
	 *            The clustering quality measures of the new run.
	 * @param parameterValues
	 *            The parameter values of this run.
	 * @param optimizationParameters
	 *            The parameters that are to be optimized during this run.
	 * @param optimizationMethods
	 *            The parameter optimization methods determines which parameter
	 *            sets are to be evaluated and stores the results.
	 * @throws RegisterException
	 */
	public ParameterOptimizationRun(final Repository repository,
			final Context context, final long changeDate, final File absPath,
			final List<ProgramConfig> programConfigs,
			final List<DataConfig> dataConfigs,
			final List<QualityMeasure> qualityMeasures,
			final List<Map<ProgramParameter<?>, String>> parameterValues,
			final List<List<ProgramParameter<?>>> optimizationParameters,
			final List<ParameterOptimizationMethod> optimizationMethods)
			throws RegisterException {
		super(repository, context, false, changeDate, absPath, programConfigs,
				dataConfigs, qualityMeasures, parameterValues);

		this.optimizationParameters = optimizationParameters;
		this.optimizationMethods = optimizationMethods;

		if (this.register()) {
			// register this Run at all dataconfigs and programconfigs
			for (DataConfig dataConfig : this.dataConfigs) {
				dataConfig.addListener(this);
			}
			for (ProgramConfig programConfig : this.programConfigs) {
				programConfig.addListener(this);
			}
			for (ParameterOptimizationMethod method : this.optimizationMethods)
				method.addListener(this);

			for (QualityMeasure measure : this.qualityMeasures) {
				// added 21.03.2013: measures are only registered here, if this
				// run has been registered
				measure.register();
				measure.addListener(this);
			}
		}
	}

	/**
	 * Copy constructor of parameter optimization runs.
	 * 
	 * @param otherRun
	 *            The parameter optimization run to be cloned.
	 * @throws RegisterException
	 */
	protected ParameterOptimizationRun(final ParameterOptimizationRun otherRun)
			throws RegisterException {
		super(otherRun);
		this.optimizationMethods = ParameterOptimizationMethod
				.cloneOptimizationMethods(otherRun.optimizationMethods);
		this.optimizationParameters = ProgramParameter
				.cloneParameterListList(otherRun.optimizationParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.ExecutionRun#createRunRunnableFor(framework.RunScheduler,
	 * run.Run, program.ProgramConfig, data.DataConfig, java.lang.String,
	 * boolean)
	 */
	@Override
	protected ExecutionRunRunnable createRunRunnableFor(
			RunSchedulerThread runScheduler, Run run,
			ProgramConfig programConfig, DataConfig dataConfig,
			String runIdentString, boolean isResume) {

		// 06.04.2013: changed from indexOf to this manual search, because at
		// this point the passed programConfig and dataConfig are moved clones
		// of the originals in #runPairs
		int p = -1;
		for (int i = 0; i < ((ParameterOptimizationRun) run).getRunPairs()
				.size(); i++) {
			Pair<ProgramConfig, DataConfig> pair = ((ParameterOptimizationRun) run)
					.getRunPairs().get(i);
			if (pair.getFirst().getName().equals(programConfig.getName())
					&& pair.getSecond().getName().equals(dataConfig.getName())) {
				p = i;
				break;
			}
		}

		ParameterOptimizationMethod optimizationMethod = ((ParameterOptimizationRun) run)
				.getOptimizationMethods().get(p);
		ParameterOptimizationRunRunnable t = new ParameterOptimizationRunRunnable(
				runScheduler, run, programConfig, dataConfig,
				optimizationMethod, runIdentString, isResume);
		run.progress.addSubProgress(t.getProgressPrinter(), 100);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.Run#clone()
	 */
	@Override
	public ParameterOptimizationRun clone() {
		try {
			return new ParameterOptimizationRun(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.ExecutionRun#notify(framework.repository.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		super.notify(e);
		if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (optimizationMethods.contains(event.getRemovedObject())) {
				event.getRemovedObject().removeListener(this);
				this.log.info("Run " + this
						+ ": Removed, because ParameterOptimizationMethod "
						+ event.getRemovedObject() + " has changed.");
				RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(this);
				this.unregister();
				this.notify(newEvent);
			}
		}
	}

	/**
	 * Parses a parameter optimization run from a file.
	 * 
	 * <p>
	 * A parameter optimization run file contains several options:
	 * <ul>
	 * <li><b>programConfig</b>: The program configurations of this run (see
	 * {@link ExecutionRun#programConfigs})</li>
	 * <li><b>dataConfig</b>: The data configurations of this run (see
	 * {@link ExecutionRun#dataConfigs})</li>
	 * <li><b>qualityMeasures</b>: The clustering quality measures of this run
	 * (see {@link ExecutionRun#qualityMeasures})</li>
	 * <li><b>optimizationMethod</b>: The default parameter optimization method
	 * for all program configurations without an explicit optimization method.</li>
	 * <li><b>optimizationCriterion</b> = {@link QualityMeasure}</li>
	 * <li><b>optimizationIterations</b> = Number of iterations for a pair of
	 * program and data configuration</li>
	 * </ul>
	 * 
	 * <p>
	 * For every program configuration a section containing parameter values is
	 * optional:
	 * <ul>
	 * <li><b>[programConfigName]</b></li>
	 * <ul>
	 * <li><b>parameterName</b> = parameterValue (for every parameter a value
	 * can be specified)</li>
	 * <li><b>optimizationParameters</b> = param_1,param_2,..,param_k (a comma
	 * separated list of parameters, see {@link #optimizationParameters})</li>
	 * <li><b>optimizationMethod</b> = {@link ParameterOptimizationMethod} (see
	 * {@link #optimizationMethods})</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param absPath
	 *            The absolute file path to the *.run file
	 * @return The parsed parameter optimization run
	 * @throws ConfigurationException
	 *             the configuration exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnknownRunResultFormatException
	 *             the unknown run result format exception
	 * @throws UnknownDataSetFormatException
	 *             the unknown data set format exception
	 * @throws UnknownQualityMeasureException
	 *             the unknown clustering quality measure exception
	 * @throws UnknownParameterOptimizationMethodException
	 * @throws NoOptimizableProgramParameterException
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
	 * @throws IncompatibleParameterOptimizationMethodException
	 * @throws UnknownDistanceMeasureException
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
	public static Run parseFromFile(final File absPath)
			throws ConfigurationException, IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			UnknownQualityMeasureException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException, NoRepositoryFoundException,
			GoldStandardNotFoundException,
			InvalidOptimizationParameterException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, RunException,
			UnknownProgramTypeException, UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NumberFormatException,
			NoDataSetException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException,
			UnknownParameterType {
		Logger log = LoggerFactory.getLogger(ParameterOptimizationRun.class);
		log.debug("Parsing run \"" + absPath + "\"");

		Run result;
		HierarchicalINIConfiguration props = new HierarchicalINIConfiguration(
				absPath.getAbsolutePath());

		Repository repo = Repository.getRepositoryForPath(absPath
				.getAbsolutePath());

		final long changeDate = absPath.lastModified();

		Context context;

		// by default we are in a clustering context
		if (props.containsKey("context"))
			context = Context.parseFromString(repo, props.getString("context"));
		else
			context = Context.parseFromString(repo, "ClusteringContext");

		/*
		 * A run consists of a set of programconfigs and a set of dataconfigs,
		 * that are pairwise combined.
		 */
		List<ProgramConfig> programConfigs = new LinkedList<ProgramConfig>();
		List<DataConfig> dataConfigs = new LinkedList<DataConfig>();
		/*
		 * The quality measures that should be calculated for every pair of
		 * programconfig+dataconfig.
		 */
		List<QualityMeasure> qualityMeasures = new LinkedList<QualityMeasure>();
		/*
		 * A list with parameter values that are set in the run config. They
		 * will overwrite the default values of the program config.
		 */
		List<Map<ProgramParameter<?>, String>> runParamValues = new ArrayList<Map<ProgramParameter<?>, String>>();
		/*
		 * These parameters are optimized when this run is in parameter
		 * optimization mode. For every program one list.
		 */
		List<List<ProgramParameter<?>>> optimizationParameters = new ArrayList<List<ProgramParameter<?>>>();
		/*
		 * The optimization methods, for every program one method.F
		 */
		List<ParameterOptimizationMethod> optimizationMethods = new ArrayList<ParameterOptimizationMethod>();

		/*
		 * Default optimization method for all programs, where no specific
		 * method is defined
		 */
		String paramOptMethod = props.getString("optimizationMethod");
		List<String> paramOptMethods = new ArrayList<String>();

		if (props.getStringArray("programConfig").length == 0)
			throw new RunException(
					"At least one program config must be specified");
		for (String programConfig : props.getStringArray("programConfig")) {
			ProgramConfig newProgramConfig = ProgramConfig
					.parseFromFile(new File(FileUtils.buildPath(
							repo.getProgramConfigBasePath(), programConfig
									+ ".config")));

			if (!newProgramConfig.getProgram().getContext().equals(context))
				throw new IncompatibleContextException(
						"Incompatible run context (" + context
								+ ") and program context ("
								+ newProgramConfig.getProgram().getContext()
								+ ")");

			newProgramConfig = repo.getRegisteredObject(newProgramConfig);
			programConfigs.add(newProgramConfig);

			Map<ProgramParameter<?>, String> paramMap = new HashMap<ProgramParameter<?>, String>();
			List<ProgramParameter<?>> optParaList = new ArrayList<ProgramParameter<?>>();

			/*
			 * parse the overriding parameter-values for this program config
			 */
			if (props.getSections().contains(programConfig)) {

				/*
				 * These parameters are used for parameter optimization. If we
				 * are in parameter optimization mode and there are concrete
				 * values for this parameters in this section, they will be
				 * ignored.
				 */
				final String[] optimizationParas = props.getSection(
						programConfig).getStringArray("optimizationParameters");

				/*
				 * Check whether the given optimization parameter are indeed
				 * defined as optimizable parameters in the program config.
				 */
				for (String optPa : optimizationParas) {
					try {
						ProgramParameter<?> p = newProgramConfig
								.getParamWithId(optPa);
						if (!newProgramConfig.getOptimizableParams()
								.contains(p))
							throw new NoOptimizableProgramParameterException(
									"The run config "
											+ absPath.getName()
											+ " contained invalid optimization parameters: "
											+ optPa
											+ " is not an optimizable program parameter of program "
											+ newProgramConfig.getProgram());
						optParaList.add(p);
					} catch (UnknownProgramParameterException e) {
						/*
						 * Modify the message
						 */
						throw new UnknownProgramParameterException("The run "
								+ absPath.getName()
								+ " contained invalid parameter values: "
								+ newProgramConfig.getProgram()
								+ " does not have a parameter " + optPa);
					}
				}

				if (props.getSection(programConfig).containsKey(
						"optimizationMethod")) {
					paramOptMethods.add(props.getSection(programConfig)
							.getString("optimizationMethod"));
				}
				/*
				 * Default optimization method of this run config
				 */
				else
					paramOptMethods.add(paramOptMethod);

				/*
				 * General parameters, not only for optimization.
				 */
				Iterator<String> itParams = props.getSection(programConfig)
						.getKeys();
				while (itParams.hasNext()) {
					String param = itParams.next();
					if (param != null
							&& !param.equals("optimizationParameters")
							&& !param.equals("optimizationMethod"))
						try {
							ProgramParameter<?> p = newProgramConfig
									.getParamWithId(param);

							boolean optParam = false;
							for (String optPa : optimizationParas)
								if (optPa.equals(param)) {
									optParaList.add(p);
									optParam = true;
									break;
								}
							if (!optParam)
								paramMap.put(p, props.getSection(programConfig)
										.getString(param));
						} catch (UnknownProgramParameterException e) {
							log.error("The run " + absPath.getName()
									+ " contained invalid parameter values: "
									+ newProgramConfig.getProgram()
									+ " does not have a parameter " + param);
						}
				}
			}
			/*
			 * If there are no explicit optimization parameters set in the run
			 * config, use all optimizable parameters of program config.
			 */
			else {
				optParaList.addAll(newProgramConfig.getOptimizableParams());
				paramOptMethods.add(paramOptMethod);
			}

			if (optParaList.isEmpty())
				throw new RunException(
						"At least one optimization parameter must be specified for program configuration "
								+ programConfig);

			runParamValues.add(paramMap);
			optimizationParameters.add(optParaList);
		}

		if (props.getStringArray("qualityMeasures").length == 0)
			throw new RunException(
					"At least one quality measure must be specified");

		/**
		 * We catch the exceptions such that all quality measures are tried to
		 * be loaded once so that they are ALL registered as missing in the
		 * repository.
		 */
		List<UnknownQualityMeasureException> thrownExceptions = new ArrayList<UnknownQualityMeasureException>();
		for (String qualityMeasure : props.getStringArray("qualityMeasures")) {
			try {
				qualityMeasures.add(QualityMeasure.parseFromString(repo,
						qualityMeasure));
			} catch (UnknownQualityMeasureException e) {
				thrownExceptions.add(e);
			}
		}
		if (thrownExceptions.size() > 0) {
			// just throw the first exception
			throw thrownExceptions.get(0);
		}

		QualityMeasure optimizationCriterion = null;

		String paramOptCriterion = props.getString("optimizationCriterion");
		optimizationCriterion = QualityMeasure.parseFromString(repo,
				paramOptCriterion);
		if (!qualityMeasures.contains(optimizationCriterion))
			throw new UnknownQualityMeasureException(
					"The optimization criterion is not contained in the list of quality measures.");

		if (props.getStringArray("dataConfig").length == 0)
			throw new RunException("At least one data config must be specified");
		for (String dataConfig : props.getStringArray("dataConfig")) {
			dataConfigs.add(repo.getRegisteredObject(DataConfig
					.parseFromFile(new File(FileUtils.buildPath(
							repo.getDataConfigBasePath(), dataConfig
									+ ".dataconfig")))));
		}

		checkCompatibilityQualityMeasuresDataConfigs(dataConfigs,
				qualityMeasures);

		String paramOptIterations = props.getString("optimizationIterations");

		for (int i = 0; i < programConfigs.size(); i++) {
			for (int j = 0; j < dataConfigs.size(); j++) {
				optimizationMethods
						.add(ParameterOptimizationMethod.parseFromString(
								repo,
								paramOptMethods.get(i),
								// first we initialize the object with a null
								// reference instead of the run
								null,
								programConfigs.get(i),
								dataConfigs.get(j),
								optimizationParameters.get(i),
								optimizationCriterion,
								paramOptIterations != null
										? ArraysExt.rep(Integer
												.valueOf(paramOptIterations),
												optimizationParameters.get(i)
														.size()) : ArraysExt
												.rep(10, optimizationParameters
														.get(i).size()), false));
			}
		}

		result = new ParameterOptimizationRun(repo, context, changeDate,
				absPath, programConfigs, dataConfigs, qualityMeasures,
				runParamValues, optimizationParameters, optimizationMethods);
		result = repo.getRegisteredObject(result, false);

		// now we set the run reference of the methods
		// added 21.03.2013: handle registering of the methods
		for (int i = 0; i < optimizationMethods.size(); i++) {
			ParameterOptimizationMethod method = optimizationMethods.get(i);
			method.setRun((ParameterOptimizationRun) result);

			method.register();
			optimizationMethods.set(i, repo.getRegisteredObject(method));
		}

		ParameterOptimizationRun.checkCompatibilityParameterOptimizationMethod(
				optimizationMethods, programConfigs, dataConfigs);

		log.debug("Run parsed");
		return result;
	}

	/**
	 * @return A list of parameter lists for every program configuration, that
	 *         are to be optimized.
	 * @see #optimizationParameters
	 */
	public List<List<ProgramParameter<?>>> getOptimizationParameters() {
		return this.optimizationParameters;
	}

	/**
	 * @return A list with optimization methods. One method for every program.
	 * @see #optimizationMethods
	 */
	public List<ParameterOptimizationMethod> getOptimizationMethods() {
		return this.optimizationMethods;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.config.Run#register()
	 */
	@Override
	public boolean register() throws RegisterException {
		return this.repository.register(this);
	}
}
