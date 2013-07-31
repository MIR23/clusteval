/**
 * 
 */
package de.clusteval.framework.threading;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.paramOptimization.ParameterOptimizationMethodFinderThread;
import de.clusteval.data.dataset.DataSetConfigFinderThread;
import de.clusteval.data.dataset.format.DataSetFormatFinderThread;
import de.clusteval.data.statistics.DataStatisticFinderThread;
import de.clusteval.framework.repository.Repository;
import de.clusteval.quality.ClusteringQualityMeasureFinderThread;
import de.clusteval.run.RunFinderThread;
import de.clusteval.run.result.format.RunResultFormatFinderThread;
import de.clusteval.run.statistics.RunStatisticFinderThread;

/**
 * This supervisor thread is responsible for starting and keeping alive all
 * other threads of the framework. He continuously checks whether the other
 * threads are still alive as they should. In case any of those threads
 * terminates in an uncontrolled way, the supervisor thread restarts a new
 * thread of the corresponding class of threads that was terminated
 * unexpectedly.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class SupervisorThread extends Thread {

	/**
	 * @param classes
	 *            The clusteval thread classes to add to a list.
	 * @return A list of clusteval thread classes.
	 */
	public static List<Class<? extends ClustevalThread>> createList(
			Class<? extends ClustevalThread>... classes) {
		List<Class<? extends ClustevalThread>> result = new ArrayList<Class<? extends ClustevalThread>>();
		for (Class<? extends ClustevalThread> clazz : classes)
			result.add(clazz);
		return result;
	}

	/**
	 * The repository this supervisor belongs to and for which all threads
	 * should be supervised.
	 */
	protected Repository repository;

	/**
	 * A map containing all threads that were started by this supervisor
	 * together with their thread class.
	 * 
	 * <p>
	 * This map is used by the supervisor to iterate through the existing
	 * threads and checking whether they are still alive. If the supervisor
	 * thread finds a thread in this map that terminated unexpectedly, he
	 * creates a new thread of this class.
	 */
	protected Map<Class<? extends ClustevalThread>, ClustevalThread> threads;

	/**
	 * The time between two checks of this thread.
	 */
	protected long supervisorSleepTime;

	/**
	 * A boolean indicating, whether this thread only should check once and then
	 * terminate. This can be useful in some subclasses of this class (e.g.
	 * {@link RunResultRepositorySupervisorThread}).
	 */
	protected boolean checkOnce;

	protected Logger log;

	/**
	 * Constructor of abstract supervisor threads.
	 * 
	 * @param repository
	 *            The repository this supervisor belongs to and for which all
	 *            threads should be supervised.
	 * @param threads
	 *            A list containing all threads this supervisor thread should
	 *            start and keep alive.
	 * @param threadSleepTimes
	 *            A map containing sleep times for threads (must not be
	 *            complete).
	 * @param checkOnce
	 *            A boolean indicating, whether this thread only should check
	 *            once and then terminate. This can be useful in some subclasses
	 *            of this class (e.g.
	 *            {@link RunResultRepositorySupervisorThread}).
	 */
	public SupervisorThread(final Repository repository,
			final List<Class<? extends ClustevalThread>> threads,
			final Map<String, Long> threadSleepTimes, final boolean checkOnce) {
		super();
		this.setName(this.getName().replace("Thread", "Supervisor"));
		this.log = LoggerFactory.getLogger(this.getClass());

		this.repository = repository;
		this.supervisorSleepTime = threadSleepTimes
				.containsKey("SupervisorThread") ? threadSleepTimes
				.get("SupervisorThread") :
		// if not specified, check every 5 seconds
				5000;
		this.checkOnce = checkOnce;

		this.threads = new LinkedHashMap<Class<? extends ClustevalThread>, ClustevalThread>();

		for (Class<? extends ClustevalThread> thread : threads) {
			try {
				this.threads.put(
						thread,
						(threadSleepTimes.containsKey(thread.getSimpleName())) ?
						// if we have a specific sleep time for this thread use
						// it
								thread.getConstructor(SupervisorThread.class,
										Repository.class, long.class,
										boolean.class).newInstance(
										this,
										repository,
										threadSleepTimes.get(thread
												.getSimpleName()), checkOnce) :
								// otherwise we use the default sleep time of
								// the thread class
								thread.getConstructor(SupervisorThread.class,
										Repository.class, boolean.class)
										.newInstance(this, repository,
												checkOnce));
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
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// check, that all threads are running
		while (true) {
			try {
				if (this.isInterrupted()) {
					for (Thread t : this.threads.values()) {
						t.interrupt();
						t.join();
					}
					return;
				}

				// changed at 08.05.2012
				for (Class<? extends ClustevalThread> threadClass : this.threads
						.keySet()) {
					if (this.threads.get(threadClass) == null
							|| (this.threads.get(threadClass).getState()
									.equals(State.TERMINATED) && !checkOnce)) {
						this.log.warn("Restarting "
								+ threadClass.getSimpleName());
						Constructor<? extends ClustevalThread> constr;
						try {
							constr = threadClass.getConstructor(
									SupervisorThread.class, Repository.class,
									boolean.class);
							this.threads
									.put(threadClass, constr.newInstance(this,
											repository, false));
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						} catch (SecurityException e) {
							e.printStackTrace();
						} catch (InstantiationException e) {
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
				if (checkOnce && this.repository.isInitialized())
					return;
				sleep(this.supervisorSleepTime);
			} catch (InterruptedException e) {
				this.interrupt();
			}
			// added 12.12.2012
			catch (ConcurrentModificationException e) {
				e.printStackTrace();
				// do nothing
			}
		}
	}

	/**
	 * @return The thread which finds dataset formats.
	 */
	public DataSetFormatFinderThread getDataSetFormatThread() {
		return (DataSetFormatFinderThread) this.threads
				.get(DataSetFormatFinderThread.class);
	}

	/**
	 * @return The thread which finds dataset configurations.
	 */
	public DataSetConfigFinderThread getDataSetConfigThread() {
		return (DataSetConfigFinderThread) this.threads
				.get(DataSetConfigFinderThread.class);
	}

	/**
	 * @return The thread which finds runresult formats.
	 */
	public RunResultFormatFinderThread getRunResultFormatThread() {
		return (RunResultFormatFinderThread) this.threads
				.get(RunResultFormatFinderThread.class);
	}

	/**
	 * @return The thread which finds runs.
	 */
	public RunFinderThread getRunFinderThread() {
		return (RunFinderThread) this.threads.get(RunFinderThread.class);
	}

	/**
	 * @return The thread which finds data statistics.
	 */
	public DataStatisticFinderThread getDataStatisticFinderThread() {
		return (DataStatisticFinderThread) this.threads
				.get(DataStatisticFinderThread.class);
	}

	/**
	 * @return The thread which finds run statistics.
	 */
	public RunStatisticFinderThread getRunStatisticFinderThread() {
		return (RunStatisticFinderThread) this.threads
				.get(RunStatisticFinderThread.class);
	}

	/**
	 * @return The thread which finds clustering quality measures.
	 */
	public ClusteringQualityMeasureFinderThread getClusteringQualityMeasureFinderThread() {
		return (ClusteringQualityMeasureFinderThread) this.threads
				.get(ClusteringQualityMeasureFinderThread.class);
	}

	/**
	 * @return The run scheduler thread.
	 */
	public RunSchedulerThread getRunScheduler() {
		return (RunSchedulerThread) this.threads.get(RunSchedulerThread.class);
	}

	/**
	 * @return The thread which finds parameter optimization methods.
	 */
	public ParameterOptimizationMethodFinderThread getParameterOptimizationMethodFinderThread() {
		return (ParameterOptimizationMethodFinderThread) this.threads
				.get(ParameterOptimizationMethodFinderThread.class);
	}

	/**
	 * @param clazz
	 *            The class for which we want the thread instance
	 * @return The thread instance of the passed class.
	 */
	public ClustevalThread getThread(Class<? extends ClustevalThread> clazz) {
		return this.threads.get(clazz);
	}
}