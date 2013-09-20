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
package de.clusteval.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.threading.ClustevalThread;
import de.clusteval.framework.threading.SupervisorThread;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class FinderThread extends ClustevalThread {

	protected Repository repository;

	protected long sleepTime;

	protected boolean checkOnce;

	protected Logger log;

	protected Finder currentFinder;

	/**
	 * @param supervisorThread
	 * @param framework
	 * @param checkOnce
	 * 
	 */
	public FinderThread(final SupervisorThread supervisorThread,
			final Repository framework, boolean checkOnce) {
		this(supervisorThread, framework, 30000, checkOnce);
	}

	/**
	 * @param supervisorThread
	 * @param framework
	 * @param sleepTime
	 * @param checkOnce
	 * 
	 */
	public FinderThread(final SupervisorThread supervisorThread,
			final Repository framework, final long sleepTime, boolean checkOnce) {
		super(supervisorThread);
		this.setName(this.getName().replace("Thread",
				this.getClass().getSimpleName()));
		this.log = LoggerFactory.getLogger(this.getClass());
		this.repository = framework;
		this.sleepTime = sleepTime;
		this.checkOnce = checkOnce;
		// aquire the lock in the beginning, such that no other threads
		// depending on termination of this thread start processing before this
		// thread finished finding at least once
		this.start();
	}

	protected abstract void beforeFind();

	protected abstract Finder getFinder() throws RegisterException;

	protected abstract void afterFind();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// this.lock.lock();
		while (!this.isInterrupted()) {
			try {
				this.beforeFind();
				currentFinder = getFinder();
				currentFinder.findAndRegisterObjects();
				this.afterFind();
				try {
					// try release the lock, if we still keep it (only
					// before first finishing of finding)
					this.setInitialized();
				} catch (IllegalMonitorStateException e) {

				}
				this.repository.commitDB();
				if (checkOnce)
					return;
				sleep(sleepTime);
			} catch (InterruptedException e) {
				this.interrupt();
			} catch (RegisterException e) {
				e.printStackTrace();
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
		super.interrupt();
		if (currentFinder != null)
			currentFinder.interrupt();
	}
}
