package de.clusteval.program;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.cluster.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.program.r.RProgram;
import de.clusteval.program.r.RProgramConfig;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import file.FileUtils;

/**
 * A program configuration encapsulates a program together with options and
 * settings.
 * 
 * <p>
 * A program configuration corresponds to and is parsed from a file on the
 * filesystem in the corresponding folder of the repository (see
 * {@link Repository#programConfigBasePath} and {@link ProgramConfigFinder}).
 * 
 * <p>
 * There are several options, that can be specified in the program configuration
 * file (see {@link #parseFromFile(File)}).
 * 
 * 
 * @author Christian Wiwie
 * 
 */
public class ProgramConfig extends RepositoryObject {

	/**
	 * A helper method for cloning a list of program configurations.
	 * 
	 * @param programConfigs
	 *            The list of program configurations to clone.
	 * @return The list containing the cloned program configurations of the
	 *         input list.
	 */
	public static List<ProgramConfig> cloneProgramConfigurations(
			final List<ProgramConfig> programConfigs) {
		List<ProgramConfig> result = new ArrayList<ProgramConfig>();

		for (ProgramConfig programConfig : programConfigs) {
			result.add(programConfig.clone());
		}

		return result;
	}

	/**
	 * The program this configuration belongs to.
	 */
	protected Program program;

	/**
	 * This is the default invocation line used to invoke the program, when this
	 * program configuration is used together with some data configuration.
	 * 
	 * <p>
	 * This invocation line is used, if
	 * <ul>
	 * <li>there is a goldstandard in the data configuration</li>
	 * <li>the run is not of type parameter optimization</li>
	 * </ul>
	 */
	protected String invocationFormat;

	/**
	 * This invocation line is used, if
	 * <ul>
	 * <li>there is no goldstandard in the data configuration</li>
	 * <li>and the run is not of type parameter optimization</li>
	 * </ul>
	 */
	protected String invocationFormatWithoutGoldStandard;

	/**
	 * This invocation line is used, if
	 * <ul>
	 * <li>there is a goldstandard in the data configuration</li>
	 * <li>and the run is of type parameter optimization</li>
	 * </ul>
	 */
	protected String invocationFormatParameterOptimization;

	/**
	 * This invocation line is used, if
	 * <ul>
	 * <li>there is no goldstandard in the data configuration</li>
	 * <li>and the run is of type parameter optimization</li>
	 * </ul>
	 */
	protected String invocationFormatParameterOptimizationWithoutGoldStandard;

	protected List<String> compatibleDataSetFormats;

	/**
	 * The output format of the program
	 */
	protected RunResultFormat outputFormat;

	/**
	 * A list holding all parameters of the program.
	 */
	protected List<ProgramParameter<?>> params;

	/**
	 * A list holding all optimizable parameter of the program. Optimizable
	 * parameters are those parameters, that can in principle be optimized in
	 * parameter optimization runs (see {@link ParameterOptimizationRun}).
	 */
	protected List<ProgramParameter<?>> optimizableParameters;

	/**
	 * Instantiates a new program config.
	 * 
	 * @param repository
	 *            The repository this program configuration should be registered
	 *            at.
	 * @param register
	 *            A boolean indicating whether to register this program
	 *            configuration at the repository.
	 * @param changeDate
	 *            The change date of this program configuration is used for
	 *            equality checks.
	 * @param absPath
	 *            The absolute path of this program configuration.
	 * @param program
	 *            The program this program configuration belongs to.
	 * @param outputFormat
	 *            The output format of the program.
	 * @param compatibleDataSetFormats
	 *            A list of compatible dataset formats of the encapsulated
	 *            program.
	 * @param invocationFormat
	 *            The invocation line for runs with goldstandard and without
	 *            parameter optimization
	 * @param invocationFormatWithoutGoldStandard
	 *            The invocation line for runs without goldstandard and without
	 *            parameter optimization
	 * @param invocationFormatParameterOptimization
	 *            The invocation line for runs with goldstandard and with
	 *            parameter optimization
	 * @param invocationFormatParameterOptimizationWithoutGoldStandard
	 *            The invocation line for runs without goldstandard and with
	 *            parameter optimization
	 * @param params
	 *            The parameters of the program.
	 * @param optimizableParameters
	 *            The parameters of the program, that can be optimized.
	 * @throws RegisterException
	 */
	public ProgramConfig(
			final Repository repository,
			final boolean register,
			final long changeDate,
			final File absPath,
			final Program program,
			final RunResultFormat outputFormat,
			final List<String> compatibleDataSetFormats,
			final String invocationFormat,
			final String invocationFormatWithoutGoldStandard,
			final String invocationFormatParameterOptimization,
			final String invocationFormatParameterOptimizationWithoutGoldStandard,
			final List<ProgramParameter<?>> params,
			final List<ProgramParameter<?>> optimizableParameters)
			throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.program = program;
		this.outputFormat = outputFormat;
		this.compatibleDataSetFormats = compatibleDataSetFormats;

		this.invocationFormat = invocationFormat;
		this.invocationFormatWithoutGoldStandard = invocationFormatWithoutGoldStandard;
		this.invocationFormatParameterOptimization = invocationFormatParameterOptimization;
		this.invocationFormatParameterOptimizationWithoutGoldStandard = invocationFormatParameterOptimizationWithoutGoldStandard;

		this.params = params;
		this.optimizableParameters = optimizableParameters;

		if (register && this.register()) {
			this.program.register();
			this.program.addListener(this);

			// TODO
			// for (Set<DataSetFormat> set : this.compatibleDataSetFormats) {
			// for (DataSetFormat dsFormat : set) {
			// dsFormat.register();
			// dsFormat.addListener(this);
			// }
			// }

			outputFormat.register();
			outputFormat.addListener(this);
		}
	}

	/**
	 * The copy constructor of program configurations.
	 * 
	 * @param programConfig
	 *            The program configuration to be cloned.
	 * @throws RegisterException
	 */
	public ProgramConfig(ProgramConfig programConfig) throws RegisterException {
		super(programConfig);

		this.program = programConfig.program.clone();
		this.outputFormat = programConfig.outputFormat.clone();
		// this.compatibleDataSetFormats = DataSetFormat
		// .cloneDataSetFormats(programConfig.compatibleDataSetFormats);
		this.compatibleDataSetFormats = programConfig.compatibleDataSetFormats;

		this.invocationFormat = programConfig.invocationFormat;
		this.invocationFormatWithoutGoldStandard = programConfig.invocationFormatWithoutGoldStandard;
		this.invocationFormatParameterOptimization = programConfig.invocationFormatParameterOptimization;
		this.invocationFormatParameterOptimizationWithoutGoldStandard = programConfig.invocationFormatParameterOptimizationWithoutGoldStandard;

		this.params = ProgramParameter.cloneParameterList(programConfig.params);
		this.optimizableParameters = ProgramParameter
				.cloneParameterList(programConfig.optimizableParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public ProgramConfig clone() {
		try {
			return new ProgramConfig(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * This method parses a program configuration from a file on the filesystem.
	 * 
	 * <p>
	 * A program configuration contains several options:
	 * <ul>
	 * <li><b>type</b>: The type of the encapsulated program, either standalone
	 * or the java class name of the R program (default: standalone)</li>
	 * <li><b>program</b> (optional): This is the full name of the program, this
	 * configuration encapsulates (see {@link Program#getFullName()}).</li>
	 * <li><b>outputFormat</b>: (see {@link #outputFormat})</li>
	 * <li><b>compatibleDataSetFormats</b>: (see
	 * {@link #compatibleDataSetFormats})</li>
	 * <li><b>alias</b>: (see {@link StandaloneProgram#alias})</li>
	 * <li><b>parameters</b>: (see {@link #params})</li>
	 * <li><b>optimizationParameters</b>: (see {@link #optimizableParameters})</li>
	 * <li><b>[invocationFormat]</b></li>
	 * <ul>
	 * <li><b>invocationFormat</b>: see {@link #invocationFormat}</li>
	 * <li><b>invocationFormatWithoutGoldStandard</b> (optional): see
	 * {@link #invocationFormatWithoutGoldStandard}</li>
	 * <li><b>invocationFormatParameterOptimization</b> (optional): see
	 * {@link #invocationFormatParameterOptimization}</li>
	 * <li><b>invocationFormatParameterOptimizationWithoutGoldStandard</b>
	 * (optional): see
	 * {@link #invocationFormatParameterOptimizationWithoutGoldStandard}</li>
	 * </ul>
	 * </ul>
	 * 
	 * <p>
	 * The invocationFormat section is not required for RPrograms. They provide
	 * their own invocation format by implementing
	 * {@link RProgram#getInvocationFormat()}.
	 * 
	 * <p>
	 * For every program parameter the program configuration can contain another
	 * section, which is parsed by the
	 * {@link ProgramParameter#parseFromConfiguration(ProgramConfig, String, org.apache.commons.configuration.SubnodeConfiguration)}
	 * method.
	 * 
	 * @param absConfigPath
	 *            The absolute file path to the program configuration.
	 * @throws ConfigurationException
	 * @throws FileNotFoundException
	 * @throws UnknownRunResultFormatException
	 * @throws UnknownDataSetFormatException
	 * @throws NoRepositoryFoundException
	 * @throws InvalidOptimizationParameterException
	 * @throws UnknownProgramParameterException
	 * @throws UnknownProgramTypeException
	 * @throws UnknownRProgramException
	 * @throws RegisterException
	 * @return The program configuration.
	 * @throws UnknownContextException
	 */
	public static ProgramConfig parseFromFile(final File absConfigPath)
			throws ConfigurationException, FileNotFoundException,
			UnknownRunResultFormatException, UnknownDataSetFormatException,
			NoRepositoryFoundException, InvalidOptimizationParameterException,
			UnknownProgramParameterException, UnknownProgramTypeException,
			UnknownRProgramException, RegisterException,
			UnknownContextException {

		if (!absConfigPath.exists())
			throw new FileNotFoundException("Program config \"" + absConfigPath
					+ "\" does not exist!");

		Logger log = LoggerFactory.getLogger(ProgramConfig.class);
		log.debug("Parsing program config \"" + absConfigPath + "\"");
		HierarchicalINIConfiguration props = new HierarchicalINIConfiguration(
				absConfigPath);
		props.setThrowExceptionOnMissing(true);

		Repository repo = Repository.getRepositoryForPath(absConfigPath
				.getAbsolutePath());

		Context context;
		// by default we are in a clustering context
		if (props.containsKey("context"))
			context = Context.parseFromString(repo, props.getString("context"));
		else
			context = Context.parseFromString(repo, "ClusteringContext");

		/*
		 * Added 07.08.2012 Type of programconfig is either standalone or R
		 */
		String type;
		if (props.containsKey("type")) {
			type = props.getString("type");
		} else
			// Default
			type = "standalone";

		long changeDate;
		Program programP = null;
		// initialize compatible dataset formats

		RunResultFormat runresultFormat;
		List<String> compatibleDataSetFormats;
		if (type.equals("standalone")) {
			String program = FileUtils.buildPath(repo.getProgramBasePath(),
					props.getString("program"));

			File programFile = new File(program);
			if (!(programFile).exists())
				throw new FileNotFoundException(
						"The given program executable does not exist: "
								+ programFile.getAbsolutePath());

			changeDate = programFile.lastModified();

			String outputFormat = props.getString("outputFormat");

			compatibleDataSetFormats = Arrays.asList(props.getString(
					"compatibleDataSetFormats").split("\\|"));

			runresultFormat = RunResultFormat.parseFromString(repo,
					outputFormat);

			String alias = props.getString("alias");

			programP = new StandaloneProgram(repo, context, true, changeDate,
					programFile, alias);
		} else if (repo.isRProgramRegistered("de.clusteval.program.r." + type)) {
			programP = RProgram.parseFromString(repo, type);

			RProgram rProgram = (RProgram) programP;

			compatibleDataSetFormats = rProgram.getCompatibleDataSetFormats();

			runresultFormat = rProgram.getRunResultFormat();
		} else {
			throw new UnknownProgramTypeException("The type " + type
					+ " is unknown.");
		}

		List<String> paras = Arrays.asList(props.getStringArray("parameters"));
		List<ProgramParameter<?>> params = new ArrayList<ProgramParameter<?>>();
		List<ProgramParameter<?>> optimizableParameters = new ArrayList<ProgramParameter<?>>();

		changeDate = absConfigPath.lastModified();

		// check whether there are parameter-sections for parameters, that are
		// not listed in the parameters-list
		Set<String> sections = props.getSections();
		sections.removeAll(paras);
		sections.remove(null);
		sections.remove("invocationFormat");

		if (sections.size() > 0) {
			throw new UnknownProgramParameterException(
					"There are parameter-sections "
							+ sections
							+ " in ProgramConfig "
							+ absConfigPath.getName()
							+ " for undefined parameters. Please add them to the parameter-list.");
		}

		ProgramConfig result;

		if (type.equals("standalone")) {
			String invocationFormat = props.getSection("invocationFormat")
					.getString("invocationFormat");
			String invocationFormatWithoutGoldStandard = null;
			String invocationFormatParameterOptimization = null;
			String invocationFormatParameterOptimizationWithoutGoldStandard = null;

			if (props.getSection("invocationFormat").containsKey(
					"invocationFormatWithoutGoldStandard"))
				invocationFormatWithoutGoldStandard = props.getSection(
						"invocationFormat").getString(
						"invocationFormatWithoutGoldStandard");
			else
				invocationFormatWithoutGoldStandard = invocationFormat;

			if (props.getSection("invocationFormat").containsKey(
					"invocationFormatParameterOptimization"))
				invocationFormatParameterOptimization = props.getSection(
						"invocationFormat").getString(
						"invocationFormatParameterOptimization");

			if (props.getSection("invocationFormat").containsKey(
					"invocationFormatParameterOptimizationWithoutGoldStandard"))
				invocationFormatParameterOptimizationWithoutGoldStandard = props
						.getSection("invocationFormat")
						.getString(
								"invocationFormatParameterOptimizationWithoutGoldStandard");
			else
				invocationFormatParameterOptimizationWithoutGoldStandard = invocationFormatParameterOptimization;

			result = new ProgramConfig(repo, true, changeDate, absConfigPath,
					programP, runresultFormat, compatibleDataSetFormats,
					invocationFormat, invocationFormatWithoutGoldStandard,
					invocationFormatParameterOptimization,
					invocationFormatParameterOptimizationWithoutGoldStandard,
					params, optimizableParameters);
		}
		// RProgram
		else {
			result = new RProgramConfig(repo, true, changeDate, absConfigPath,
					programP, runresultFormat, compatibleDataSetFormats,
					params, optimizableParameters);
		}

		// add parameter objects for input (i), executable (e), output (o)
		// and goldstandard (gs)
		params.add(new StringProgramParameter(repo, false, result, "i",
				"Input", null, null, null));
		params.add(new StringProgramParameter(repo, false, result, "e",
				"Executable", null, null, null));
		params.add(new StringProgramParameter(repo, false, result, "o",
				"Output", null, null, null));
		params.add(new StringProgramParameter(repo, false, result, "q",
				"Quality", null, null, null));
		params.add(new StringProgramParameter(repo, false, result, "gs",
				"Goldstandard", null, null, null));

		/*
		 * Get the optimization parameters (parameters, that can be optimized
		 * for this program in parameter_optimization runmode
		 */
		String[] optimizableParams = props
				.getStringArray("optimizationParameters");

		// iterate over all parameters
		for (String pa : paras) {

			// skip the empty string
			if (pa.equals(""))
				continue;

			final Map<String, String> paramValues = new HashMap<String, String>();
			paramValues.put("name", pa);

			ProgramParameter<?> param = ProgramParameter
					.parseFromConfiguration(result, pa, props.getSection(pa));
			params.add(param);

			/*
			 * Check if this parameter is declared as an optimizable parameter
			 */
			boolean optimizable = false;
			for (String optPa : optimizableParams)
				if (optPa.equals(pa)) {
					optimizable = true;
					break;
				}

			if (optimizable) {
				/*
				 * Check if min and max values are given for this parameter,
				 * which is necessary for optimizing it
				 */
				if (!(param.isMinValueSet()) || !(param.isMaxValueSet()))
					throw new InvalidOptimizationParameterException(
							"The parameter "
									+ param
									+ " cannot be used as an optimization parameter, because its min and max values are not set.");
				optimizableParameters.add(param);
			}
		}

		result = repo.getRegisteredObject(result);
		return result;
	}

	/**
	 * This method returns the invocation line format for non
	 * parameter-optimization runs.
	 * 
	 * @param withoutGoldStandard
	 *            This boolean indicates, whether this method returns the
	 *            invocation format for the case with- or without goldstandard.
	 * 
	 * @return The invocation line format
	 */
	public String getInvocationFormat(boolean withoutGoldStandard) {
		if (withoutGoldStandard)
			return invocationFormatWithoutGoldStandard;
		return invocationFormat;
	}

	/**
	 * Internal Parameter Optimization is an alternative for parameter
	 * optimization, in that the program handles the parameter optimization
	 * itself. In this case, the framework invokes the program only once.
	 * 
	 * @return True, if the encapsulated program supports internal parameter
	 *         optimization, false otherwise.
	 */
	public boolean supportsInternalParameterOptimization() {
		return invocationFormatParameterOptimization != null;
	}

	/**
	 * This method returns the invocation line format for parameter-optimization
	 * runs.
	 * 
	 * @param withoutGoldStandard
	 *            This boolean indicates, whether this method returns the
	 *            invocation format for the case with- or without goldstandard.
	 * 
	 * @return The invocation line format
	 */
	public String getInvocationFormatParameterOptimization(
			boolean withoutGoldStandard) {
		if (withoutGoldStandard)
			return invocationFormatParameterOptimizationWithoutGoldStandard;
		return invocationFormatParameterOptimization;
	}

	/**
	 * 
	 * @return The list of parameters of the encapsulated program.
	 * @see #params
	 */
	public List<ProgramParameter<?>> getParams() {
		return params;
	}

	/**
	 * 
	 * @param repo
	 *            The repository to check for available format conversions
	 * @param programConfig
	 * @param inputFormats
	 * @return True, if the provided input formats are compatibly with this
	 *         program configuration.
	 * @see #compatibleDataSetFormats
	 */
	public static boolean checkCompatibilityToDataSetFormats(
			final Repository repo, final ProgramConfig programConfig,
			final List<String> inputFormats) {

		Set<Map<String, String>> possibleMappings = new HashSet<Map<String, String>>();

		// we iterate over all possible sets of input formats this program
		// configuration supports
		for (String requiredFormatsStr : programConfig.compatibleDataSetFormats) {
			// optional input formats will end with '?'
			List<String> requiredFormats = new ArrayList<String>(
					Arrays.asList(requiredFormatsStr.split("\\&")));
			List<String> optionalFormats = new ArrayList<String>();
			for (String f : requiredFormats)
				if (f.endsWith("?")) {
					optionalFormats.add(f.replace("?", ""));
				}
			for (String f : optionalFormats) {
				requiredFormats.remove(f + "?");
				requiredFormats.add(f);
			}

			// build up a mapping from provided input formats to required input
			// formats
			Map<String, String> mapping = new HashMap<String, String>();

			List<String> remainingInputFormats = new ArrayList<String>(
					inputFormats);

			// for every required format we check, which formats can be
			// converted to it
			boolean stillValid = true;
			for (String requiredFormat : requiredFormats) {
				Map<String, List<String>> pathsToRequiredFormat = repo
						.getAvailableFormatConversionsTo(requiredFormat);

				// check whether we have got any of these inputs
				Set<String> compatibleSourceFormats = new HashSet<String>(
						pathsToRequiredFormat.keySet());
				compatibleSourceFormats.retainAll(remainingInputFormats);

				// if there is no compatible source formats for this format we
				// check whether it is optional
				if (compatibleSourceFormats.isEmpty()) {
					if (!optionalFormats.contains(requiredFormat)) {
						stillValid = false;
						break;
					}
					continue;
				}
				// TODO: greedy, just take the first one
				String sourceFormat = compatibleSourceFormats
						.toArray(new String[0])[0];
				mapping.put(sourceFormat, requiredFormat);

				remainingInputFormats.remove(sourceFormat);
			}
			if (stillValid)
				possibleMappings.add(mapping);
		}

		return !possibleMappings.isEmpty();
	}

	/**
	 * 
	 * @return The list of optimizable parameters of the encapsulated program.
	 * @see #optimizableParameters
	 */
	public List<ProgramParameter<?>> getOptimizableParams() {
		return optimizableParameters;
	}

	/**
	 * This method returns the program parameter with the given id and throws an
	 * exception, of none such parameter exists.
	 * 
	 * @param id
	 *            The name the parameter should have.
	 * @throws UnknownProgramParameterException
	 * @return The program parameter with the appropriate name
	 */
	public ProgramParameter<?> getParamWithId(final String id)
			throws UnknownProgramParameterException {
		for (ProgramParameter<?> param : this.params)
			if (param.name.equals(id))
				return param;
		throw new UnknownProgramParameterException(
				"The program parameter with id \"" + id + "\" is unknown.");
	}

	/**
	 * 
	 * @return The encapsulated program.
	 * @see #program
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * 
	 * @return The output format of the encapsulated program.
	 * @see #outputFormat
	 */
	public RunResultFormat getOutputFormat() {
		return this.outputFormat;
	}

	/**
	 * @return The name of the program configuration is the name of the file
	 *         without extension.
	 */
	public String getName() {
		return this.absPath.getName().replace(".config", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
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
			if (event.getOld().equals(this)) {
				super.notify(event);

				for (ProgramParameter<?> param : params)
					param.unregister();
			} else {
				if (event.getOld().equals(program)) {
					event.getOld().removeListener(this);
					this.log.info("ProgramConfig "
							+ this
							+ ": Program reloaded due to modifications in filesystem");
					event.getReplacement().addListener(this);
					// added 06.07.2012
					this.program = (Program) event.getReplacement();
				}
			}
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this)) {
				super.notify(event);

				for (ProgramParameter<?> param : params)
					param.unregister();
			} else {
				if (event.getRemovedObject().equals(program)) {
					event.getRemovedObject().removeListener(this);
					this.log.info("ProgramConfig " + this
							+ ": Removed, because Program " + program
							+ " was removed.");
					RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
							this);
					this.unregister();
					this.notify(newEvent);
					// }
					// a dataset format class changed
					// TODO
					// else if (this.compatibleDataSetFormats.contains(event
					// .getRemovedObject())) {
					// event.getRemovedObject().removeListener(this);
					// this.log.info("ProgramConfig " + this
					// + ": Removed, because DataSetFormat "
					// + event.getRemovedObject() + " has changed.");
					// RepositoryRemoveEvent newEvent = new
					// RepositoryRemoveEvent(
					// this);
					// this.unregister();
					// this.notify(newEvent);
				} // the runresult format class changed
				else if (this.outputFormat.equals(event.getRemovedObject())) {
					event.getRemovedObject().removeListener(this);
					this.log.info("ProgramConfig " + this
							+ ": Removed, because RunResultFormat "
							+ event.getRemovedObject() + " has changed.");
					RepositoryRemoveEvent newEvent = new RepositoryRemoveEvent(
							this);
					this.unregister();
					this.notify(newEvent);
				}
			}
		}
	}

	/**
	 * TODO: merge this and {@link #getParamWithId(String)}
	 * 
	 * @param name
	 *            the name
	 * @return the parameter for name
	 */
	public ProgramParameter<?> getParameterForName(final String name) {
		ProgramParameter<?> pa = null;
		for (int i = 0; i < this.getParams().size(); i++) {
			pa = this.getParams().get(i);
			if (pa.toString().equals(name) || pa.name.equals(name)) {
				return pa;
			}
		}
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}
}
