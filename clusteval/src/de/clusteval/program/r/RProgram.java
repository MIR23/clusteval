/**
 * 
 */
package de.clusteval.program.r;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.StringExt;
import de.clusteval.cluster.Clustering;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.RNotAvailableException;

/**
 * A type of progam that encapsulates a program embedded in R.
 * 
 * <p>
 * Note that the name of the method in R is not itself written in plaintext in
 * the
 * {@link #exec(de.clusteval.data.DataConfig, de.clusteval.program.ProgramConfig, String[], java.util.Map, java.util.Map)}
 * method of RPrograms, but instead it is defined in the corresponding program
 * configuration in the invocation format parameter.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class RProgram extends Program {

	/**
	 * Attribute used to store an rengine instance during execution of this
	 * program.
	 */
	protected MyRengine rEngine;

	/**
	 * Attribute used to store the dataset content during execution of this
	 * program.
	 */
	protected Object dataSetContent;

	/**
	 * Attribute to store the data object ids during execution of this program.
	 */
	protected String[] ids;

	/**
	 * Attribute to store the pairwise similarites or absolute coordinates of
	 * data objects during execution of this program.
	 */
	protected double[][] x;

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#register()
	 */
	@Override
	public boolean register() throws RegisterException {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/**
	 * This method parses the major name given as a string, looks up the
	 * corresponding RProgram in the repository and returns a new instance.
	 * 
	 * @param repository
	 *            The repository to lookup the RProgram.
	 * @param rProgram
	 *            The major name (see {@link #getMajorName()}) of the RProgram
	 *            to return.
	 * @return An instance of an RProgram corresponding to the passed string.
	 * @throws UnknownRProgramException
	 */
	public static RProgram parseFromString(final Repository repository,
			String rProgram) throws UnknownRProgramException {
		Class<? extends RProgram> c = repository
				.getRProgramClass("de.clusteval.program.r." + rProgram);

		try {
			Constructor<? extends RProgram> constr = c
					.getConstructor(Repository.class);
			RProgram program = constr.newInstance(repository);
			return program;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		throw new UnknownRProgramException("\"" + rProgram
				+ "\" is not a known RProgram.");
	}

	/**
	 * @param repository
	 *            the repository this program should be registered at.
	 * @param changeDate
	 *            The change date of this program is used for equality checks.
	 * @param absPath
	 *            The absolute path of this program.
	 * @throws RegisterException
	 */
	public RProgram(Repository repository, long changeDate, File absPath)
			throws RegisterException {
		super(repository, false, changeDate, absPath);
	}

	/**
	 * The copy constructor for rprograms.
	 * 
	 * @param rProgram
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RProgram(final RProgram rProgram) throws RegisterException {
		super(rProgram);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#clone()
	 */
	@Override
	public final RProgram clone() {
		try {
			return this.getClass().getConstructor(this.getClass())
					.newInstance(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		this.log.warn("Cloning instance of class "
				+ this.getClass().getSimpleName() + " failed");
		return null;
	}

	/**
	 * The major name of a RProgram corresponds to the simple name of its class.
	 */
	@Override
	public String getMajorName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

	/**
	 * @return The format of the invocation line of this RProgram.
	 */
	public abstract String getInvocationFormat();

	// /**
	// * @return A set containing sets of dataset formats, which this r program
	// * can take as input.
	// * @throws UnknownDataSetFormatException
	// */
	public abstract List<String> getCompatibleDataSetFormats()
			throws UnknownDataSetFormatException;

	/**
	 * @return The runresult formats, the results of this r program will be
	 *         generated in.
	 * @throws UnknownRunResultFormatException
	 */
	public abstract RunResultFormat getRunResultFormat()
			throws UnknownRunResultFormatException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.Program#exec(de.clusteval.data.DataConfig,
	 * de.clusteval.program.ProgramConfig, java.lang.String[], java.util.Map,
	 * java.util.Map)
	 */
	@Override
	public final Process exec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams) throws RNotAvailableException,
			RLibraryNotLoadedException, REngineException,
			REXPMismatchException, IOException {
		try {
			beforeExec(dataConfig, programConfig, invocationLine,
					effectiveParams, internalParams);
			doExec(dataConfig, programConfig, invocationLine, effectiveParams,
					internalParams);
			afterExec(dataConfig, programConfig, invocationLine,
					effectiveParams, internalParams);
			return null;
		} finally {

		}
	}

	@SuppressWarnings("unused")
	protected void beforeExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams)
			throws RLibraryNotLoadedException, REngineException,
			RNotAvailableException {
		// create a connection to R
		try {
			rEngine = new MyRengine("");
		} catch (REngineException e) {
			throw new RNotAvailableException("The RProgram "
					+ this.getMajorName()
					+ " could not establish a connection to R");
		}

		// load the required R libraries
		for (String library : getRequiredRlibraries())
			rEngine.loadLibrary(library, this.getClass().getSimpleName());

		// this will init the ids attribute
		this.dataSetContent = extractDataSetContent(dataConfig);

		rEngine.assign("ids", ids);
	}

	/**
	 * This method is required to initialize the attributes
	 * {@link #dataSetContent}, {@link #ids} and all other attributes of the
	 * data, which are needed in
	 * {@link #doExec(DataConfig, ProgramConfig, String[], Map, Map)}.
	 * 
	 * @param dataConfig
	 * @return
	 */
	protected abstract Object extractDataSetContent(DataConfig dataConfig);

	@SuppressWarnings("unused")
	protected final void doExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams) throws RserveException {
		rEngine.eval("result <- " + StringExt.paste(" ", invocationLine));
	}

	protected void afterExec(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams) throws RserveException,
			REXPMismatchException, IOException {
		try {
			final String resultAsString = execResultToString(dataConfig,
					programConfig, invocationLine, effectiveParams,
					internalParams);

			File output = new File(internalParams.get("o"));

			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			bw.append(resultAsString);
			bw.close();
		} finally {
			rEngine.close();
		}
	}

	@SuppressWarnings("unused")
	protected String execResultToString(DataConfig dataConfig,
			ProgramConfig programConfig, String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams) throws RserveException,
			REXPMismatchException {
		Clustering resultClustering = Clustering.parseFromFuzzyCoeffMatrix(ids,
				getFuzzyCoeffMatrixFromExecResult());

		StringBuilder sb = new StringBuilder();
		sb.append("k\tClustering\n");

		sb.append(getParameterValueForResultFile(effectiveParams) + "\t");
		sb.append(resultClustering.toFormattedString());
		return sb.toString();
	}

	/**
	 * This method extracts the results after executing
	 * {@link #doExec(DataConfig, ProgramConfig, String[], Map, Map)}. By
	 * default the result is stored in the R variable "result".
	 * 
	 * @return A two dimensional float array, containing fuzzy coefficients for
	 *         each object and cluster. Rows correspond to objects and columns
	 *         correspond to clusters. The order of objects is the same as in
	 *         {@link #ids}.
	 * @throws RserveException
	 * @throws REXPMismatchException
	 */
	protected abstract float[][] getFuzzyCoeffMatrixFromExecResult()
			throws RserveException, REXPMismatchException;

	protected abstract String getParameterValueForResultFile(
			final Map<String, String> effectiveParams);
}
