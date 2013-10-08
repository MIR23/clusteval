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
package de.clusteval.framework.threading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Pair;
import utils.Triple;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.Repository;
import de.clusteval.run.MissingParameterValueException;
import de.clusteval.run.RUN_STATUS;
import de.clusteval.run.Run;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.runnable.RunRunnable;
import de.clusteval.run.runnable.RunRunnableInitializationException;
import file.FileUtils;

/**
 * Threads of this class are responsible for scheduling, creating, starting and
 * terminating runs.
 * 
 * <p>
 * The methods {@link #schedule(String, String)},
 * {@link #scheduleResume(String, String)} and
 * {@link #terminate(String, String)} of this thread are invoked by the backend
 * server {@link ClustevalBackendServer} which in turn gets its commands from a
 * client.
 * 
 * @author Christian Wiwie
 * 
 */
public class RunSchedulerThread extends ClustevalThread {

	/**
	 * A queue containing all the runs that were scheduled, but not yet
	 * executed. Every entry of the queue contains (clientId,runId,isResume).
	 */
	protected Queue<Triple<String, String, Boolean>> runQueue;

	/**
	 * A map containing all the runs that are executed right now. The map maps
	 * from client id to collections of runs.
	 */
	protected Map<String, Collection<Run>> clientToRuns;
	/**
	 * A map containing all the run resumes that are executed right now. The map
	 * maps from client id to collections of runs.
	 */
	protected Map<String, Collection<Run>> clientToRunResumes;

	/**
	 * The repository this run scheduler belongs to. This scheduler can only
	 * control runs that are contained in this repository.
	 */
	protected Repository repository;

	protected Logger log;

	/**
	 * A thread pool containing all threads that were started to execute runs.
	 * This data structure allows convenient control over the number of threads
	 * to be started in parallel.
	 */
	protected ScheduledThreadPoolExecutor threadPool;

	/**
	 * Constructor of run scheduler threads.
	 * 
	 * @param supervisorThread
	 * 
	 * @param repository
	 *            The repository this run scheduler belongs to. This scheduler
	 *            can only control runs that are contained in this repository.
	 * @param numberThreads
	 *            The maximal number of threads to run in parallel.
	 */
	public RunSchedulerThread(final SupervisorThread supervisorThread,
			final Repository repository, final int numberThreads) {
		super(supervisorThread);
		this.setName(this.getName().replace("Thread", "RunScheduler"));
		this.runQueue = new ConcurrentLinkedQueue<Triple<String, String, Boolean>>();
		this.clientToRuns = new HashMap<String, Collection<Run>>();
		this.clientToRunResumes = new HashMap<String, Collection<Run>>();
		this.repository = repository;
		this.log = LoggerFactory.getLogger(this.getClass());
		this.threadPool = new ScheduledThreadPoolExecutor(numberThreads);
		this.threadPool.setMaximumPoolSize(this.threadPool.getCorePoolSize());
		this.start();
	}

	/**
	 * @param clientId
	 * @return
	 */
	// TODO
	// public Map<String, Triple<Triple<RUN_STATUS, Float>, Map<Triple<String,
	// String>, Map<String, Triple<Map<String, Double>, Double>>>>>
	// getOptimizationRunStatusForClientId(
	// String clientId) {
	//
	// synchronized (this.runQueue) {
	// Map<String, Triple<Triple<RUN_STATUS, Float>, Map<Triple<String, String>,
	// Map<String, Triple<Map<String, Double>, Double>>>>> result = new
	// HashMap<String, Triple<Triple<RUN_STATUS, Float>, Map<Triple<String,
	// String>,
	// Map<String, Triple<Map<String, Double>, Double>>>>>();
	//
	// if (this.runQueue.containsKey(clientId)) {
	// for (String runId : this.runQueue.get(clientId)) {
	// result.put(
	// runId,
	// Triple.getTriple(
	// Triple.getTriple(RUN_STATUS.SCHEDULED, 100f),
	// (Map<Triple<String, String>, Map<String, Triple<Map<String, Double>,
	// Double>>>) new HashMap<Triple<String, String>, Map<String,
	// Triple<Map<String,
	// Double>, Double>>>()));
	// }
	// }
	//
	// Collection<Run> toRemove = new HashSet<Run>();
	//
	// if (this.clientToRuns.containsKey(clientId)) {
	// for (Run run : this.clientToRuns.get(clientId)) {
	// result.put(
	// run.getName(),
	// Triple.getTriple(
	// Triple.getTriple(run.getStatus(),
	// run.getPercentFinished()),
	// run.getOptimizationStatus()));
	// if (result.get(run.getName()).getFirst()
	// .equals(RUN_STATUS.FINISHED))
	// toRemove.add(run);
	// }
	// }
	//
	// for (Run run : toRemove)
	// this.clientToRuns.get(clientId).remove(run);
	//
	// return result;
	// }
	// }

	/**
	 * This method is invoked by
	 * {@link ClustevalBackendServer#performRun(String, String)} and schedules a
	 * run. As soon as ressources are available this run is then performed in an
	 * asynchronous way in its own thread.
	 * 
	 * @param clientId
	 *            The id of the client that wants to schedule this run.
	 * @param runId
	 *            The id of the run that should be scheduled
	 * @return True, if successful. That means, a run with the given id must
	 *         exist and the run has not been scheduled already.
	 */
	public boolean schedule(final String clientId, final String runId) {
		final Triple<String, String, Boolean> newTriple = Triple.getTriple(
				clientId, runId, false);
		/*
		 * Check if this run exists
		 */
		if (this.repository.getRun(runId) == null) {
			this.log.warn("A job with id " + runId + " does not exist");
			return false;
		}

		/*
		 * Job has already been scheduled
		 */
		if (this.runQueue.contains(newTriple)) {
			this.log.warn("The job " + runId + " has already been scheduled");
			return false;
		}
		for (String client : this.clientToRuns.keySet()) {
			boolean found = false;
			for (Run run : this.clientToRuns.get(client))
				if (run.getName().equals(runId)) {
					if (run.getStatus().equals(RUN_STATUS.RUNNING)
							|| run.getStatus().equals(RUN_STATUS.SCHEDULED)) {
						found = true;
					}
					break;
				}
			if (found) {
				this.log.warn("The job " + runId + " is already running");
				return false;
			}
		}

		this.repository.getRun(runId).setStatus(RUN_STATUS.SCHEDULED);

		this.log.info("Run scheduled..." + runId);
		return this.runQueue.add(newTriple);
	}

	/**
	 * This method is invoked by
	 * {@link ClustevalBackendServer#resumeRun(String, String)} and schedules a
	 * resume of a run. As soon as ressources are available this resume is then
	 * performed in an asynchronous way in its own thread.
	 * 
	 * @param clientId
	 *            The id of the client that wants to schedule this run.
	 * @param uniqueRunResultIdentifier
	 *            The unique identifier of the run result that should be
	 *            resumed.
	 * @return True, if successful. That means, a run result with the given id
	 *         must exist and the run has not been scheduled already.
	 */
	public boolean scheduleResume(String clientId,
			String uniqueRunResultIdentifier) {
		final Triple<String, String, Boolean> newTriple = Triple.getTriple(
				clientId, uniqueRunResultIdentifier, true);
		if (!new File(FileUtils.buildPath(
				this.repository.getRunResultBasePath(),
				uniqueRunResultIdentifier)).exists()) {
			this.log.warn("No run results were found under "
					+ FileUtils.buildPath(
							this.repository.getRunResultBasePath(),
							uniqueRunResultIdentifier));
			return false;
		}

		/*
		 * Job has already been scheduled
		 */
		if (this.runQueue.contains(newTriple)) {
			this.log.warn("The job " + uniqueRunResultIdentifier
					+ " has already been scheduled");
			return false;
		}
		for (String client : this.clientToRunResumes.keySet()) {
			boolean found = false;
			for (Run run : this.clientToRunResumes.get(client))
				if (run.getRunIdentificationString().equals(
						uniqueRunResultIdentifier)) {
					if (run.getStatus().equals(RUN_STATUS.RUNNING)
							|| run.getStatus().equals(RUN_STATUS.SCHEDULED)) {
						found = true;
					}
					break;
				}
			if (found) {
				this.log.warn("The job " + uniqueRunResultIdentifier
						+ " is already running");
				return false;
			}
		}

		this.log.info("Run resume scheduled..." + uniqueRunResultIdentifier);
		return this.runQueue.add(newTriple);
	}

	/**
	 * This method is invoked by
	 * {@link ClustevalBackendServer#terminateRun(String, String)} and
	 * terminates a run that is currently being executed.
	 * 
	 * @param clientId
	 *            The id of the client that wants to terminate this run.
	 * @param runId
	 *            The id of the run that should be terminated
	 * @return True, if successful. That means, a run with the given id must
	 *         exist and it is currently being executed.
	 */
	public boolean terminate(final String clientId, final String runId) {
		// assume the run is a run resume
		boolean result = terminate(clientId, runId, true);
		// otherwise it is assumed to be a run
		if (!result)
			result = terminate(clientId, runId, false);
		return result;
	}

	protected boolean terminate(final String clientId, final String runId,
			final boolean isResume) {
		final Triple<String, String, Boolean> triple = Triple.getTriple(
				clientId, runId, isResume);
		/*
		 * Job is in the queue, has not been executed
		 */
		if (this.runQueue.contains(triple)) {
			this.log.info("Job " + runId
					+ " has been removed from the runs queue.");
			return this.runQueue.remove(triple);
		}

		/*
		 * Run is being executed
		 */
		if (this.clientToRuns.containsKey(clientId)) {
			for (Run run : this.clientToRuns.get(clientId))
				if (run.getName().equals(runId)) {
					if (run.terminate()) {
						return this.clientToRuns.get(clientId).remove(run);
					}
				}
		}
		/*
		 * Run resume is being executed
		 */
		if (this.clientToRunResumes.containsKey(clientId)) {
			for (Run run : this.clientToRunResumes.get(clientId))
				if (run.getName().equals(runId)) {
					if (run.terminate()) {
						return this.clientToRunResumes.get(clientId)
								.remove(run);
					}
				}
		}
		return false;
	}

	/**
	 * @return A collection containing names of all enqueued runs and run
	 *         resumes.
	 */
	public Queue<String> getQueue() {
		final Queue<String> result = new ConcurrentLinkedQueue<String>();

		for (Map.Entry<String, Collection<Run>> entry : this.clientToRuns
				.entrySet())
			for (Run run : entry.getValue())
				if (run.getStatus().equals(RUN_STATUS.SCHEDULED))
					result.add(run.getName());
		for (Map.Entry<String, Collection<Run>> entry : this.clientToRunResumes
				.entrySet())
			for (Run run : entry.getValue())
				if (run.getStatus().equals(RUN_STATUS.SCHEDULED))
					result.add(run.getRunIdentificationString());

		for (Triple<String, String, Boolean> triple : runQueue)
			result.add(triple.getSecond());

		return result;
	}

	/**
	 * 
	 * @return A collection of runs, that have been executed or resumed.
	 */
	public Set<Run> getRuns() {
		final Set<Run> result = new HashSet<Run>();
		for (Collection<Run> coll : this.clientToRunResumes.values())
			result.addAll(coll);
		for (Collection<Run> coll : this.clientToRuns.values())
			result.addAll(coll);
		return result;
	}

	/**
	 * 
	 * This method is invoked by
	 * {@link ClustevalBackendServer#getRunStatusForClientId(String)} and gets
	 * the status of all runs and run resumes scheduled and executed by the user
	 * with the given id.
	 * 
	 * @param clientId
	 *            The id of the client for which we want to know the status of
	 *            its scheduled and executed runs and run resumes.
	 * @return A map containing the id of the runs and run resumes together with
	 *         their current status and percentage (if currently executing).
	 */
	public Map<String, Pair<RUN_STATUS, Float>> getRunStatusForClientId(
			String clientId) {
		Map<String, Pair<RUN_STATUS, Float>> result = new HashMap<String, Pair<RUN_STATUS, Float>>();

		// iterate over all scheduled runs and run resumes
		for (Triple<String, String, Boolean> triple : this.runQueue) {
			if (triple.getFirst().equals(clientId))
				result.put(triple.getSecond(),
						Pair.getPair(RUN_STATUS.SCHEDULED, 100f));
		}

		// iterate over all executing runs
		if (this.clientToRuns.containsKey(clientId)) {
			for (Run run : this.clientToRuns.get(clientId)) {
				result.put(run.getName(),
						Pair.getPair(run.getStatus(), run.getPercentFinished()));
			}
		}

		/*
		 * iterate over all executing run resumes
		 */
		if (this.clientToRunResumes.containsKey(clientId)) {
			for (Run run : this.clientToRunResumes.get(clientId)) {
				result.put(run.getName(),
						Pair.getPair(run.getStatus(), run.getPercentFinished()));
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// wait for new runs and run resumes
		while (!this.isInterrupted()) {

			// check the run queue for a new run or run resume
			final Triple<String, String, Boolean> pair = this.runQueue.poll();
			if (pair != null) {
				String clientId = pair.getFirst();
				final String runId = pair.getSecond();
				final Run run;
				final RunSchedulerThread finalScheduler = this;
				boolean isResume = pair.getThird();

				if (!isResume) {
					// take a cloned copy of the run
					run = this.repository.getRun(runId).clone();

					if (!this.clientToRuns.containsKey(clientId))
						this.clientToRuns.put(clientId, new HashSet<Run>());
					if (this.clientToRuns.get(clientId).contains(run))
						this.clientToRuns.get(clientId).remove(run);
					this.clientToRuns.get(clientId).add(run);

					Thread t = new Thread() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.lang.Runnable#run()
						 */
						@Override
						public void run() {
							try {
								run.perform(finalScheduler);
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (RunRunnableInitializationException e) {
								e.printStackTrace();
							}
						}
					};
					t.start();
				} else {
					List<ParameterOptimizationResult> results = new ArrayList<ParameterOptimizationResult>();
					try {
						run = ParameterOptimizationResult
								.parseFromRunResultFolder(
										repository,
										new File(FileUtils.buildPath(repository
												.getRunResultBasePath(), runId)),
										results, false, false, false).clone();
						run.setStatus(RUN_STATUS.SCHEDULED);

						if (!this.clientToRunResumes.containsKey(clientId))
							this.clientToRunResumes.put(clientId,
									new HashSet<Run>());
						if (this.clientToRunResumes.get(clientId).contains(run))
							this.clientToRunResumes.get(clientId).remove(run);
						this.clientToRunResumes.get(clientId).add(run);

						Thread t = new Thread() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see java.lang.Runnable#run()
							 */
							@Override
							public void run() {
								try {
									run.resume(finalScheduler, runId);
								} catch (MissingParameterValueException e1) {
									e1.printStackTrace();
								} catch (IOException e1) {
									e1.printStackTrace();
								} catch (NoRunResultFormatParserException e1) {
									e1.printStackTrace();
								} catch (RunRunnableInitializationException e) {
									e.printStackTrace();
								}
							}
						};
						t.start();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				this.interrupt();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#interrupt()
	 */
	@Override
	public void interrupt() {
		for (Collection<Run> runs : this.clientToRuns.values())
			for (Run run : runs)
				run.terminate();
		this.threadPool.shutdown();
		super.interrupt();
	}

	/**
	 * This method takes a {@link RunRunnable} and adds it to the thread pool of
	 * this run scheduler thread. The thread pool then determines, when the
	 * runnable can and will be performed depending on the available ressources.
	 * 
	 * @param runRunnable
	 *            The new runnable to perform.
	 * @return A future object, that allows to retrieve the current status of
	 *         the execution of the runnable.
	 */
	public Future<?> registerRunRunnable(RunRunnable runRunnable) {
		return this.threadPool.submit(runRunnable);
	}
}
