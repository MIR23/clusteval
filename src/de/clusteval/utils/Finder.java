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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class Finder extends RepositoryObject {

	protected Map<String, List<Throwable>> knownExceptions;

	protected boolean interrupted;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public Finder(Repository repository, long changeDate, File absPath)
			throws RegisterException {
		super(repository, changeDate, absPath);

		this.knownExceptions = this.repository.getKnownFinderExceptions();
	}

	/**
	 * The copy constructor of finder.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public Finder(final Finder other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Finder clone() {
		try {
			return this.getClass().getConstructor(Finder.class)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
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

	/**
	 * Find files and try to parse them. If the parsing process is successful it
	 * will implicitely register the new object at the repository. if the object
	 * was known before it will updated, if and only if the changeDate of the
	 * found object is newer.
	 * 
	 * @throws RegisterException
	 */
	@SuppressWarnings("unused")
	public void findAndRegisterObjects() throws RegisterException {
		Iterator<File> fileIt = getIterator();

		while (fileIt.hasNext()) {
			if (interrupted)
				return;

			File programDir = fileIt.next();

			if (checkFile(programDir)) {
				try {
					doOnFileFound(programDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected abstract File getBaseDir();

	protected abstract Iterator<File> getIterator();

	protected abstract boolean checkFile(final File file);

	protected abstract void doOnFileFound(final File file) throws Exception;

	protected abstract Class<?> getClassToFind();

	@Override
	public void notify(RepositoryEvent e) {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (this.repository.getKnownFinderExceptions().containsKey(
					event.getOld().getAbsolutePath()))
				this.repository.getKnownFinderExceptions().remove(
						event.getOld().getAbsolutePath());
			event.getOld().removeListener(this);
			event.getReplacement().addListener(this);
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			// 31.01.2013: removed clearing of exceptions of this object. this
			// has to happen after a repeated and successful parsing of the same
			// file (e.g. FileFinder.doOnFileFound())
			event.getRemovedObject().removeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(this.getClass())))
			return false;

		Finder rf = (Finder) obj;
		return this.repository.equals(rf.getRepository());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.repository.hashCode();
	}

	/**
	 * Invoke this method to interrupt this finder.
	 */
	public void interrupt() {
		this.interrupted = true;
	}
}
