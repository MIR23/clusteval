/**
 * 
 */
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

import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
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
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.UnknownClusteringQualityMeasureException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.runnable.ClusteringRunRunnable;
import de.clusteval.run.runnable.ExecutionRunRunnable;
import file.FileUtils;

/**
 * A type of execution run that performs exactly one clustering with one
 * parameter set for every pair of program and data configuration.
 * 
 * @author Christian Wiwie
 * 
 */
public class ClusteringRun extends ExecutionRun {

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
	 * @throws RegisterException
	 */
	public ClusteringRun(Repository repository, final Context context,
			long changeDate, File absPath, List<ProgramConfig> programConfigs,
			List<DataConfig> dataConfigs,
			List<QualityMeasure> qualityMeasures,
			List<Map<ProgramParameter<?>, String>> parameterValues)
			throws RegisterException {
		super(repository, context, true, changeDate, absPath, programConfigs,
				dataConfigs, qualityMeasures, parameterValues);

		if (this.register()) {
			// register this Run at all dataconfigs and programconfigs
			for (DataConfig dataConfig : this.dataConfigs) {
				dataConfig.addListener(this);
			}
			for (ProgramConfig programConfig : this.programConfigs) {
				programConfig.addListener(this);
			}

			for (QualityMeasure measure : this.qualityMeasures) {
				// added 21.03.2013: measures are only registered here, if this
				// run has been registered
				measure.register();
				measure.addListener(this);
			}
		}
	}

	/**
	 * Copy constructor of clustering runs.
	 * 
	 * @param clusteringRun
	 *            The clustering run to be cloned.
	 * @throws RegisterException
	 */
	public ClusteringRun(final ClusteringRun clusteringRun)
			throws RegisterException {
		super(clusteringRun);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.ExecutionRun#clone()
	 */
	@Override
	public ClusteringRun clone() {
		try {
			return new ClusteringRun(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
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
		return new ClusteringRunRunnable(runScheduler, run, programConfig,
				dataConfig, runIdentString, isResume);
	}

	/**
	 * Parses a clustering run from a file.
	 * 
	 * <p>
	 * A clustering run file contains several options:
	 * <ul>
	 * <li><b>programConfig</b>: The program configurations of this run (see
	 * {@link ExecutionRun#programConfigs})</li>
	 * <li><b>dataConfig</b>: The data configurations of this run (see
	 * {@link ExecutionRun#dataConfigs})</li>
	 * <li><b>qualityMeasures</b>: The clustering quality measures of this run
	 * (see {@link ExecutionRun#qualityMeasures})</li>
	 * </ul>
	 * 
	 * <p>
	 * For every program configuration a section containing parameter values is
	 * optional:
	 * <ul>
	 * <li><b>[programConfigName]</b></li>
	 * <ul>
	 * <li><b>parameterName</b> = parameterValue</li>
	 * </ul>
	 * </ul>
	 * 
	 * @param absPath
	 *            The absolute file path to the *.run file
	 * @throws ConfigurationException
	 *             the configuration exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnknownRunResultFormatException
	 *             the unknown run result format exception
	 * @throws UnknownDataSetFormatException
	 *             the unknown data set format exception
	 * @throws UnknownClusteringQualityMeasureException
	 *             the unknown clustering quality measure exception
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
	 * @throws UnknownProgramTypeException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws NumberFormatException
	 * @return The parsed clustering run.
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownContextException
	 * @throws IncompatibleContextException
	 */
	public static Run parseFromFile(final File absPath)
			throws ConfigurationException, IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			UnknownClusteringQualityMeasureException,
			UnknownProgramParameterException, NoRepositoryFoundException,
			GoldStandardNotFoundException,
			InvalidOptimizationParameterException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, DataConfigurationException,
			DataConfigNotFoundException, RunException,
			UnknownProgramTypeException, UnknownRProgramException,
			UnknownDistanceMeasureException, RegisterException,
			UnknownDataSetTypeException, NumberFormatException,
			NoDataSetException, UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			UnknownContextException, IncompatibleContextException {
		Logger log = LoggerFactory.getLogger(ClusteringRun.class);
		log.debug("Parsing run \"" + absPath + "\"");

		Run result;
		HierarchicalINIConfiguration props = new HierarchicalINIConfiguration(
				absPath.getAbsolutePath());

		final long changeDate = absPath.lastModified();

		Repository repo = Repository.getRepositoryForPath(absPath
				.getAbsolutePath());

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

			/*
			 * parse the overriding parameter-values for this program config
			 */
			if (props.getSections().contains(programConfig)) {

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

			runParamValues.add(paramMap);
		}

		if (props.getStringArray("qualityMeasures").length == 0)
			throw new RunException(
					"At least one quality measure must be specified");

		/**
		 * We catch the exceptions such that all quality measures are tried to
		 * be loaded once so that they are ALL registered as missing in the
		 * repository.
		 */
		List<UnknownClusteringQualityMeasureException> thrownExceptions = new ArrayList<UnknownClusteringQualityMeasureException>();
		for (String qualityMeasure : props.getStringArray("qualityMeasures")) {
			try {
				qualityMeasures.add(QualityMeasure.parseFromString(
						repo, qualityMeasure));
			} catch (UnknownClusteringQualityMeasureException e) {
				thrownExceptions.add(e);
			}
		}
		if (thrownExceptions.size() > 0) {
			// just throw the first exception
			throw thrownExceptions.get(0);
		}

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

		result = new ClusteringRun(repo, context, changeDate, absPath,
				programConfigs, dataConfigs, qualityMeasures, runParamValues);
		result = repo.getRegisteredObject(result, false);

		log.debug("Run parsed");
		return result;
	}
}
