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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Christian Wiwie
 * 
 */
public class ClustevalThread extends Thread {

	/**
	 * This boolean is used to synchronize different threads.
	 */
	private AtomicBoolean initialized;

	protected SupervisorThread supervisorThread;

	/**
	 * @param supervisorThread
	 * 
	 */
	public ClustevalThread(final SupervisorThread supervisorThread) {
		super();
		this.initialized = new AtomicBoolean(false);
		this.supervisorThread = supervisorThread;
	}

	protected void setInitialized() {
		synchronized (this.initialized) {
			this.initialized.set(true);
			this.initialized.notifyAll();
		}
	}

	/**
	 * 
	 */
	public void waitFor() {
		synchronized (this.initialized) {
			if (!this.initialized.get()) {
				try {
					this.initialized.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return The supervisor thread that created this thread.
	 */
	public SupervisorThread getSupervisorThread() {
		return this.supervisorThread;
	}

}
