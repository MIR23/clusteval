/**
 * 
 */
package de.clusteval.paramOptimization;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualitySet;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.Run;
import de.clusteval.run.result.GraphMatchingRunResult;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.runnable.ExecutionRunRunnable;
import de.clusteval.utils.InternalAttributeException;

/**
 * A parameter optimization method determines how a parameter optimization run
 * is executed.
 * 
 * <p>
 * One parameter optimization method is created for every pair of program and
 * data configuration of the run in
 * {@link ParameterOptimizationRun#parseFromFile(File)}, that means for every
 * run runnable one method. The method objects are instantiated as soon as the
 * run is parsed from the filesystem. That means, when the same run is executed
 * several times, the same method object is used.
 * 
 * <p>
 * The method determines the following aspects:
 * 
 * <ul>
 * <li><b>the number of iterations of the optimization process</b></li>
 * <li><b>the parameter sets evaluated</b></li>
 * <li><b>the handling of diverging iterations</b></li>
 * <li><b>the storage of the iteration results</b></li>
 * </ul>
 * 
 * <p>
 * The <b>basic usage</b> of this class is as follows:
 * <ul>
 * <li>Instantiate an object of the parameter optimization method</li>
 * <li>Invoke {@link #reset(File)} and tell the method to initialize itself.
 * Results will be written to the passed file.</li>
 * <li>As long as {@link #hasNext()} returns true, there are more iterations to
 * evaluate</li>
 * <li>Use {@link #next()} to get the next parameter set.</li>
 * <li>Pass the assessed qualities of
 * {@link ExecutionRunRunnable#assessQualities(GraphMatchingRunResult)} to
 * {@link #giveQualityFeedback(QualitySet)}.</li>
 * <li>At the end use {@link #getResult()} to get the results of the iterations</li>
 * </ul>
 * 
 * @author Christian Wiwie
 * 
 * 
 */
public abstract class ParameterOptimizationMethod extends RepositoryObject {

	/**
	 * A helper method for cloning a list of optimization methods.
	 * 
	 * @param optimizationMethods
	 *            The optimization methods to clone.
	 * @return The list of cloned optimization methods.
	 */
	public static List<ParameterOptimizationMethod> cloneOptimizationMethods(
			List<ParameterOptimizationMethod> optimizationMethods) {
		List<ParameterOptimizationMethod> result = new ArrayList<ParameterOptimizationMethod>();

		for (ParameterOptimizationMethod method : optimizationMethods)
			result.add(method.clone());
		return result;
	}

	/**
	 * The run this method belongs to.
	 */
	protected ParameterOptimizationRun run;

	/**
	 * The program configuration this method was created for.
	 */
	protected ProgramConfig programConfig;

	/**
	 * The data configuration this method was created for.
	 */
	protected DataConfig dataConfig;

	/**
	 * The number of iterations that has been performed so far.
	 */
	protected int currentCount;

	/**
	 * This array holds the number of iterations that should be performed for
	 * each optimization parameter.
	 * 
	 * <p>
	 * However, this might not be exactly the number that is performed by the
	 * method and these numbers might be readjusted by the method after the
	 * constructor call. This is due to the fact, that every method handles the
	 * distribution of the iterations itself.
	 * 
	 * <p>
	 * The ordering of this list is assumed to be the same as the ordering of
	 * {@link #params}.
	 */
	protected int[] iterationPerParameter;

	/**
	 * The parameters of the program encapsulated by the program configuration
	 * that are to be optimized.
	 */
	protected List<ProgramParameter<?>> params;

	/**
	 * During a parameter optimization run for each calcuated clustering several
	 * quality measures are assessed. One of these quality measures is used, to
	 * rate the clusterings and to decide, which clusterings performed best.
	 * This quality measure is called the optimization criterion and is stored
	 * in this attribute.
	 */
	protected QualityMeasure optimizationCriterion;

	/**
	 * This object holds the results that are calculated throughout execution of
	 * the parameter optimization run.
	 */
	private ParameterOptimizationResult result;

	/**
	 * This boolean indicates, whether the run is a resumption of a previous run
	 * execution or a completely new execution.
	 */
	protected boolean isResume;

	/**
	 * @param repository
	 *            The repository this object is registered in.
	 * @param register
	 *            Whether this object should be registered implicitely in the
	 *            repository or if the user wants to register manually later.
	 * @param changeDate
	 *            The changedate of this object can be used for identification
	 *            and equality checks of objects.
	 * @param absPath
	 *            The absolute path of this object is used for identification
	 *            and equality checks of objects.
	 * @param run
	 *            The run this method belongs to.
	 * @param programConfig
	 *            The program configuration this method was created for.
	 * @param dataConfig
	 *            The data configuration this method was created for.
	 * @param params
	 *            This list holds the program parameters that are to be
	 *            optimized by the parameter optimization run.
	 * @param optimizationCriterion
	 *            The quality measure used as the optimization criterion (see
	 *            {@link #optimizationCriterion}).
	 * @param iterationPerParameter
	 *            This array holds the number of iterations that are to be
	 *            performed for each optimization parameter.
	 * @param isResume
	 *            This boolean indiciates, whether the run is a resumption of a
	 *            previous run execution or a completely new execution.
	 * @throws RegisterException
	 */
	public ParameterOptimizationMethod(final Repository repository,
			final boolean register, final long changeDate, final File absPath,
			final ParameterOptimizationRun run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final QualityMeasure optimizationCriterion,
			final int[] iterationPerParameter, final boolean isResume)
			throws RegisterException {
		super(repository, false, changeDate, absPath);

		this.log = LoggerFactory.getLogger(this.getClass());
		this.run = run;
		this.programConfig = programConfig;
		this.dataConfig = dataConfig;
		this.params = params;
		this.optimizationCriterion = optimizationCriterion;
		this.iterationPerParameter = iterationPerParameter;
		this.isResume = isResume;

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of parameter optimization methods
	 * 
	 * @param method
	 *            The method to clone.
	 * @throws RegisterException
	 */
	public ParameterOptimizationMethod(final ParameterOptimizationMethod method)
			throws RegisterException {
		super(method);

		// do not clone upstream
		this.run = method.run;
		this.programConfig = method.programConfig.clone();
		this.dataConfig = method.dataConfig.clone();
		this.params = ProgramParameter.cloneParameterList(method.params);
		this.optimizationCriterion = method.optimizationCriterion.clone();
		this.iterationPerParameter = method.iterationPerParameter.clone();
		this.isResume = method.isResume;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.framework.repository.RepositoryObject#equals(java.lang.Object
	 * )
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterOptimizationMethod))
			return false;

		ParameterOptimizationMethod other = (ParameterOptimizationMethod) obj;

		return this.run.equals(other.run)
				&& this.dataConfig.equals(other.dataConfig)
				&& this.programConfig.equals(other.programConfig)
				&& this.optimizationCriterion
						.equals(other.optimizationCriterion)
				&& this.params.equals(other.params)
				&& Arrays.equals(this.iterationPerParameter,
						other.iterationPerParameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.framework.repository.RepositoryObject#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.run.toString() + this.dataConfig.toString()
				+ this.programConfig.toString()
				+ this.optimizationCriterion.toString()
				+ this.params.toString() + Arrays
					.toString(this.iterationPerParameter)).hashCode();
	}

	/**
	 * @param run
	 *            The new run of this method.
	 */
	public void setRun(final ParameterOptimizationRun run) {
		this.run = run;
	}

	/**
	 * @return This list holds the program parameters that are to be optimized
	 *         by the parameter optimization run.
	 */
	public List<ProgramParameter<?>> getOptimizationParameter() {
		return this.params;
	}

	/**
	 * @return The quality measure used as the optimization criterion (see
	 *         {@link #optimizationCriterion}).
	 */
	public final QualityMeasure getOptimizationCriterion() {
		return this.optimizationCriterion;
	}

	/**
	 * This method has to be invoked after every iteration and invocation of
	 * {@link #next()}, to pass the qualities of the last clustering to this
	 * parameter optimization method.
	 * 
	 * <p>
	 * If this method is not invoked after {@link #next()}, before
	 * {@link #next()} is invoked again, an {@link IllegalStateException} will
	 * be thrown.
	 * 
	 * @param qualities
	 *            The clustering qualities for the clustering of the last
	 *            iteration.
	 */
	public void giveQualityFeedback(final QualitySet qualities) {
		ParameterSet last = result.getParameterSets().get(
				result.getParameterSets().size() - 1);
		this.result.put(this.getCurrentCount() + 1, last, qualities);
	}

	protected ParameterSet getNextParameterSet()
			throws InternalAttributeException, RegisterException,
			NoParameterSetFoundException {
		return this.getNextParameterSet(null);
	}

	/**
	 * This method purely determines and calculates the next parameter set that
	 * follows from the current state of the method.
	 * 
	 * <p>
	 * If the force parameter set is given != null, this parameter set is forced
	 * to be evaluated. This scenario is used during resumption of an older run,
	 * where the parameter sets are already fixed and we want to feed them to
	 * this method together with their results exactly as they were performed
	 * last time.
	 * 
	 * <p>
	 * This is a helper-method for {@link #next()} and
	 * {@link #next(ParameterSet)}.
	 * 
	 * @param forcedParameterSet
	 *            If this parameter is set != null, this parameter set is forced
	 *            to be evaluated in the next iteration.
	 * @return The next parameter set.
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 */
	protected abstract ParameterSet getNextParameterSet(
			ParameterSet forcedParameterSet) throws InternalAttributeException,
			RegisterException, NoParameterSetFoundException;

	/**
	 * @return True, if there are more iterations together with parameter sets
	 *         that have to be evaluated.
	 */
	public abstract boolean hasNext();

	/**
	 * This method tells the method that the next parameter set should be
	 * determined and that the invoker wants to start the evaluation of the next
	 * iteration.
	 * 
	 * <p>
	 * This is a convenience method of {@link #next(ParameterSet, long)},
	 * without enforcement of a passed parameter set.
	 * 
	 * @return The parameter set that is being evaluated in the next iteration.
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 */
	public final ParameterSet next() throws InternalAttributeException,
			RegisterException, NoParameterSetFoundException {
		return this.next(null, -1);
	}

	/**
	 * This method tells the method that the next parameter set should be
	 * determined and that the invoker wants to start the evaluation of the next
	 * iteration.
	 * 
	 * <p>
	 * If the force parameter set is given != null, this parameter set is forced
	 * to be evaluated. This scenario is used during resumption of an older run,
	 * where the parameter sets are already fixed and we want to feed them to
	 * this method together with their results exactly as they were performed
	 * last time.
	 * 
	 * <p>
	 * If this method is invoked twice, without
	 * {@link #giveQualityFeedback(QualitySet)} being invoked in between, a
	 * {@link IllegalStateException} is thrown.
	 * 
	 * @param forcedParameterSet
	 *            If this parameter is set != null, this parameter set is forced
	 *            to be evaluated in the next iteration.
	 * @param iterationNumber
	 *            The original number of the iteration when it was previously
	 *            performed.
	 * @return The parameter set that is being evaluated in the next iteration.
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws NoParameterSetFoundException
	 *             This exception is thrown, if no parameter set was found that
	 *             was not already evaluated before.
	 */
	public final ParameterSet next(final ParameterSet forcedParameterSet,
			final long iterationNumber) throws InternalAttributeException,
			RegisterException, NoParameterSetFoundException {
		if (this.result == null)
			throw new IllegalStateException("reset(File) has not been called");
		if (this.result.getParameterSets().size() > 0) {
			ParameterSet last = result.getParameterSets().get(
					result.getParameterSets().size() - 1);
			if (result.get(last) == null)
				throw new IllegalStateException(
						"Quality for last parameter set was not set");
		}

		ParameterSet result = null;

		// If we have a forced parameter set, we do not skip until a new
		// parameter set
		if (forcedParameterSet != null) {

			int numberIterations = (int) (iterationNumber - this.currentCount);

			for (int i = 0; i < numberIterations - 1; i++) {
				// take first parameter set to simulate skipping
				ParameterSet paramSet = this.getResult().getParameterSets()
						.get(0);
				QualitySet qualitySet = this.getResult().get(paramSet);
				this.next(paramSet, this.currentCount + 1);
				this.giveQualityFeedback(qualitySet);
			}

			result = getNextParameterSet(forcedParameterSet);
			this.result.put(iterationNumber, result, null);
			this.result.getParameterSets().add(result);
			// 04.04.2013: changed to adapt method to previously skipped
			// iterations
			this.currentCount = (int) iterationNumber;
		} else {
			do {
				// if result is not equal to null, we found a parameter set,
				// that was already assessed. then we give the same quality
				// feedback as last time and look for the next parameter set
				if (result != null) {
					this.giveQualityFeedback(this.result.get(result));
					this.log.info(run.toString() + " (" + programConfig + ","
							+ dataConfig + ") "
							+ "Skipping calculation of parameter set " + result
							+ " (has already been assessed)");
					result = null;
					if (!hasNext())
						break;
				}
				result = getNextParameterSet();
				this.currentCount++;
				if (!this.result.getParameterSets().contains(result))
					this.result.put(this.currentCount, result, null);
				this.result.getParameterSets().add(result);
			} while (this.result.get(result) != null);
		}
		if (result == null)
			throw new NoParameterSetFoundException(
					"No new parameter set could be found.");
		return result;
	}

	/**
	 * This method takes a string and some additional parameters and parses from
	 * it a parameter optimization method.
	 * 
	 * @param repository
	 *            The repository in which the method should look up the
	 *            parameter optimization method class and where the new object
	 *            should be registered.
	 * @param parameterOptimizationMethod
	 *            The name of the parameter optimization method class.
	 * @param run
	 *            The run the new parameter optimization method belongs to.
	 * @param programConfig
	 *            The program configuration the new parameter optimization
	 *            method belongs to.
	 * @param dataConfig
	 *            The data configuration the new parameter optimization method
	 *            belongs to.
	 * @param params
	 *            The parameters of the program encapsulated by the program
	 *            configuration that are to be optimized.
	 * @param optimizationCriterion
	 *            The quality measure used as the optimization criterion (see
	 *            {@link #optimizationCriterion}).
	 * @param iterationCount
	 *            This array holds the number of iterations that are to be
	 *            performed for each optimization parameter.
	 * @param isResume
	 *            This boolean indiciates, whether the run is a resumption of a
	 *            previous run execution or a completely new execution.
	 * @return The parsed parameter optimization method.
	 * @throws UnknownParameterOptimizationMethodException
	 */
	public static ParameterOptimizationMethod parseFromString(
			final Repository repository,
			final String parameterOptimizationMethod, final Run run,
			final ProgramConfig programConfig, final DataConfig dataConfig,
			final List<ProgramParameter<?>> params,
			final QualityMeasure optimizationCriterion,
			final int[] iterationCount, final boolean isResume)
			throws UnknownParameterOptimizationMethodException {

		Class<? extends ParameterOptimizationMethod> c = repository
				.getParameterOptimizationMethodClass("de.clusteval.paramOptimization."
						+ parameterOptimizationMethod);
		try {
			Constructor<? extends ParameterOptimizationMethod> constr = c
					.getConstructor(Repository.class, boolean.class,
							long.class, File.class,
							ParameterOptimizationRun.class,
							ProgramConfig.class, DataConfig.class, List.class,
							QualityMeasure.class, int[].class, boolean.class);
			// changed 21.03.2013: do not register new parameter optimization
			// methods here, because run is not set yet
			ParameterOptimizationMethod method = constr.newInstance(repository,
					false, System.currentTimeMillis(), new File(
							parameterOptimizationMethod), run, programConfig,
					dataConfig, params, optimizationCriterion, iterationCount,
					isResume);
			return method;
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
		throw new UnknownParameterOptimizationMethodException("\""
				+ parameterOptimizationMethod
				+ "\" is not a known parameter optimization method.");
	}

	/**
	 * @return An array holding the number of iterations that should be
	 *         performed for each optimization parameter. Keep in mind the hints
	 *         in {@link #iterationPerParameter}.
	 * @see #iterationPerParameter
	 */
	public int[] getIterationPerParameter() {
		return this.iterationPerParameter;
	}

	/**
	 * @return The total iteration count this method should perform.
	 */
	public abstract int getTotalIterationCount();

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

	/**
	 * This method returns the results of this parameter optimization run. Keep
	 * in mind, that this method returns an incomplete object, before the run is
	 * finished.
	 * 
	 * @return An wrapper object holding all the results that are calculated
	 *         throughout execution of the parameter optimization run.
	 */
	public ParameterOptimizationResult getResult() {
		// Removed 27.11.2012
		// this.result.register();
		return this.result;
	}

	/**
	 * This method initializes this method by setting the parameter values to be
	 * evaluated during the process ({@link #initParameterValues()}) and by
	 * creating a wrapper object for the {@link #result}.
	 * 
	 * <p>
	 * Furthermore if the run is a resumption of an old run execution, this
	 * method also reperforms all iterations that have been executed during the
	 * last execution of the run.
	 * 
	 * <p>
	 * This method has to be invoked before anything else is done (
	 * {@link #hasNext()} or {@link #next()}).
	 * 
	 * @param absResultPath
	 *            The absolute path pointing to the result file.
	 * @throws ParameterOptimizationException
	 * @throws InternalAttributeException
	 * @throws RegisterException
	 * @throws RunResultParseException
	 */
	public void reset(final File absResultPath)
			throws ParameterOptimizationException, InternalAttributeException,
			RegisterException, RunResultParseException {
		this.result = new ParameterOptimizationResult(
				this.dataConfig.getRepository(), System.currentTimeMillis(),
				// changed 16.09.2012 -> getParentFile
				absResultPath, absResultPath.getParentFile().getParentFile()
						.getName(), run, this);
		initParameterValues();
		if (isResume) {
			/*
			 * We have to simulate the process with the given results, to ensure
			 * that all attributes of the method (also als subclass methods) are
			 * valid and correspond to the result in the file.
			 */
			final ParameterOptimizationResult oldResults = ParameterOptimizationResult
					.parseFromRunResultCompleteFile(
							this.dataConfig.getRepository(), this.run, this,
							absResultPath, false, false, false);
			oldResults.loadIntoMemory();
			List<ParameterSet> parameterSets = oldResults.getParameterSets();
			List<Long> iterationNumbers = oldResults.getIterationNumbers();
			for (int i = 0; i < parameterSets.size(); i++) {
				final ParameterSet paramSet = parameterSets.get(i);
				final long iterationNumber = iterationNumbers.get(i);
				try {
					this.next(paramSet, iterationNumber);
				} catch (NoParameterSetFoundException e) {
					// doesn't occur
				}
				this.giveQualityFeedback(oldResults.get(paramSet));
			}
			oldResults.unloadFromMemory();
			isResume = false;
		}
	}

	/**
	 * This method initializes the parameter values of each optimization
	 * parameter that should be assessed during the process.
	 * 
	 * @throws ParameterOptimizationException
	 * @throws InternalAttributeException
	 */
	@SuppressWarnings("unused")
	protected void initParameterValues() throws ParameterOptimizationException,
			InternalAttributeException {

	}

	// removed 08.01.2013
	// /**
	// * @param dataConfig
	// * The new data configuration.
	// */
	// public void setDataConfig(final DataConfig dataConfig) {
	// this.dataConfig = dataConfig;
	// }

	/**
	 * @return The program configuration this method was created for.
	 */
	public ProgramConfig getProgramConfig() {
		return this.programConfig;
	}

	/**
	 * @return The number of iterations that has been performed so far.
	 */
	public int getCurrentCount() {
		return this.currentCount;
	}

	/**
	 * @return The data configuration this method was created for.
	 */
	public DataConfig getDataConfig() {
		return this.dataConfig;
	}

	@Override
	public ParameterOptimizationMethod clone() {
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
	 * By default, we take the first parameter as density parameter for plots
	 * 
	 * @return The optimization parameter, that should be used as the axis
	 *         variable to plot the results.
	 */
	public ProgramParameter<?> getPlotDensityParameter() {
		return this.params.get(0);
	}

	/**
	 * @param isResume
	 *            A boolean indicating, whether the run is a resumption of a
	 *            previous run execution or a completely new execution.
	 */
	public void setResume(boolean isResume) {
		this.isResume = isResume;
	}

	/**
	 * This method returns a list with the classes of all dataset formats this
	 * parameter optimization method supports. If the list is empty, all dataset
	 * formats are assumed to be compatible.
	 * 
	 * <p>
	 * Some methods only support certain input formats. If this is the case for
	 * your method, override this method and return the compatible formats.
	 * 
	 * <p>
	 * This compatibility is checked as soon as a parameter optimization run is
	 * parsed in
	 * {@link ParameterOptimizationRun#checkCompatibilityParameterOptimizationMethod}.
	 * 
	 * @return A list with all dataset format classes that are compatible to
	 *         this parameter optimization method.
	 */
	public abstract List<Class<? extends DataSetFormat>> getCompatibleDataSetFormatBaseClasses();

	/**
	 * This method returns a list with all programs this parameter optimization
	 * method supports. If the list is empty, all programs are assumed to be
	 * compatible.
	 * 
	 * <p>
	 * Some methods only support certain programs. If this is the case for your
	 * method, override this method and return the compatible programs.
	 * 
	 * <p>
	 * This compatibility is checked as soon as a parameter optimization run is
	 * parsed in
	 * {@link ParameterOptimizationRun#checkCompatibilityParameterOptimizationMethod}.
	 * 
	 * @return A list with all programs that are compatible to this parameter
	 *         optimization method.
	 */
	public abstract List<String> getCompatibleProgramNames();

	/**
	 * This method is used to set the data configuration of this run to another
	 * object. This is needed for example after cloning and conversion of the
	 * old data configuration during the run process.
	 * 
	 * @param dataConfig
	 *            The new data configuration of this run.
	 */
	public void setDataConfig(DataConfig dataConfig) {
		this.dataConfig = dataConfig;
	}

	/**
	 * This method is used to set the program configuration of this run to
	 * another object. This is needed for example after cloning and conversion
	 * of the old program configuration during the run process.
	 * 
	 * @param programConfig
	 *            The new program configuration of this run.
	 */
	public void setProgramConfig(ProgramConfig programConfig) {
		this.programConfig = programConfig;
	}
}
