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

/**
 * During execution of a run, the corresponding object has a certain status.
 */
public enum RUN_STATUS {
	/**
	 * Immediately after giving the command to execute the job, it is added to
	 * the queue of the
	 * {@link de.clusteval.framework.threading.RunSchedulerThread} with status
	 * SCHEDULED.
	 */
	SCHEDULED,
	/**
	 * As soon as ressourcess are available, the scheduler starts the job and
	 * its status is changed to RUNNING.
	 */
	RUNNING,
	/**
	 * When the job containing all its subprocesses is completely finished, its
	 * status is changed to FINISHED.
	 */
	FINISHED,
	/**
	 * When a job is terminated before it was finished, its status is changed to
	 * TERMINATED.
	 */
	TERMINATED,
	/**
	 * When a job is just inactive
	 */
	INACTIVE
}
