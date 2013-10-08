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
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.runnable.ExecutionRunRunnable;
import de.clusteval.run.runnable.InternalParameterOptimizationRunRunnable;
import file.FileUtils;

/**
 * A type of execution run that does the same as
 * {@link ParameterOptimizationRun}, by using a programs internal parameter
 * optimization mode instead of doing the parameter optimization itself within
 * the framework.
 * 
 * @author Christian Wiwie
 * 
 */
public class InternalParameterOptimizationRun extends ExecutionRun {

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
	public InternalParameterOptimizationRun(Repository repository,
			final Context context, long changeDate, File absPath,
			List<ProgramConfig> programConfigs, List<DataConfig> dataConfigs,
			List<QualityMeasure> qualityMeasures,
			List<Map<ProgramParameter<?>, String>> parameterValues)
			throws RegisterException {
		super(repository, context, true, changeDate, absPath, programConfigs,
				dataConfigs, qualityMeasures, parameterValues);
	}

	/**
	 * Copy constructor of internal parameter optimization runs.
	 * 
	 * @param otherRun
	 *            The internal parameter optimization run to be cloned.
	 * @throws RegisterException
	 */
	protected InternalParameterOptimizationRun(
			final InternalParameterOptimizationRun other)
			throws RegisterException {
		super(other);
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
		return new InternalParameterOptimizationRunRunnable(runScheduler, run,
				programConfig, dataConfig, runIdentString, isResume);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.ExecutionRun#clone()
	 */
	@Override
	public InternalParameterOptimizationRun clone() {
		try {
			return new InternalParameterOptimizationRun(this);
		} catch (RegisterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parses an internal parameter optimization run from a file.
	 * 
	 * <p>
	 * An internal parameter optimization run file contains several options:
	 * <ul>
	 * <li><b>programConfig</b>: The program configurations of this run (see
	 * {@link ExecutionRun#programConfigs})</li>
	 * <li><b>dataConfig</b>: The data configurations of this run (see
	 * {@link ExecutionRun#dataConfigs})</li>
	 * <li><b>qualityMeasures</b>: The clustering quality measures of this run
	 * (see {@link ExecutionRun#qualityMeasures})</li>
	 * <li><b>optimizationMethod</b>: The default parameter optimization method
	 * for all program configurations without an explicit optimization method.</li>
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
	 * @throws UnknownQualityMeasureException
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
	 * @throws UnknownRProgramException
	 * @throws UnknownDistanceMeasureException
	 * @throws RegisterException
	 * @throws UnknownDataSetTypeException
	 * @throws NoDataSetException
	 * @throws NumberFormatException
	 * @return The parserd internal parameter optimization run.
	 * @throws UnknownDataPreprocessorException
	 * @throws IncompatibleDataSetConfigPreprocessorException
	 * @throws UnknownContextException
	 * @throws UnknownParameterType
	 */
	public static Run parseFromFile(final File absPath)
			throws ConfigurationException, IOException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			UnknownQualityMeasureException, UnknownProgramParameterException,
			NoRepositoryFoundException, GoldStandardNotFoundException,
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
			UnknownContextException, UnknownParameterType {
		Logger log = LoggerFactory.getLogger(Run.class);
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

		for (String programConfig : props.getStringArray("programConfig")) {
			ProgramConfig newProgramConfig = ProgramConfig
					.parseFromFile(new File(FileUtils.buildPath(
							repo.getProgramConfigBasePath(), programConfig
									+ ".config")));
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

		for (String dataConfig : props.getStringArray("dataConfig")) {
			dataConfigs.add(repo.getRegisteredObject(DataConfig
					.parseFromFile(new File(FileUtils.buildPath(
							repo.getDataConfigBasePath(), dataConfig
									+ ".dataconfig")))));
		}

		checkCompatibilityQualityMeasuresDataConfigs(dataConfigs,
				qualityMeasures);

		result = new InternalParameterOptimizationRun(repo, context,
				changeDate, absPath, programConfigs, dataConfigs,
				qualityMeasures, runParamValues);
		result = repo.getRegisteredObject(result, false);

		log.debug("Run parsed");
		return result;
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
