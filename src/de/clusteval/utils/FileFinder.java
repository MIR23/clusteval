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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class FileFinder extends Finder {

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public FileFinder(Repository repository, long changeDate, File absPath)
			throws RegisterException {
		super(repository, changeDate, absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#doOnFileFound(java.io.File)
	 */
	@Override
	protected void doOnFileFound(File file) throws Exception {
		try {
			RepositoryObject newObject = parseObjectFromFile(file);
			/*
			 * We want to be informed when this object changed, such that we can
			 * clear our known exceptions
			 */
			newObject.addListener(this);

			// Clear the exceptions associated to this object
			knownExceptions.remove(file.getAbsolutePath());
		} catch (Exception e) {
			// check if we already logged a warning for this
			boolean known = false;
			if (knownExceptions.containsKey(file.getAbsolutePath())) {
				for (Throwable other : knownExceptions.get(file
						.getAbsolutePath()))
					if ((e.getMessage() == null && other.getMessage() == null)
							|| (e.getMessage() != null
									&& other.getMessage() != null && other
									.getMessage().equals(e.getMessage()))) {
						known = true;
						break;
					}
			}
			if (known)
				this.getLog().debug(
						"Could not parse " + getClassToFind().getSimpleName()
								+ " " + file + ": " + e.getMessage());
			else {
				if (!knownExceptions.containsKey(file.getAbsolutePath()))
					knownExceptions.put(file.getAbsolutePath(),
							new ArrayList<Throwable>());
				knownExceptions.get(file.getAbsolutePath()).add(e);
				String message;
				if (e.getMessage() != null)
					message = e.getMessage();
				else if (e.getCause() != null) {
					PrintWriter writer = new PrintWriter(new StringWriter());
					e.getCause().printStackTrace(writer);
					message = writer.toString();
				} else
					message = "";
				this.getLog().warn(
						"Could not parse " + getClassToFind().getSimpleName()
								+ " " + file + ": " + message);
			}

			/*
			 * Remove the corresponding object from the repository
			 */
			RepositoryObject object = this.repository.getRegisteredObject(file);
			if (object != null) {
				object.unregister();
				object.notify(new RepositoryRemoveEvent(object));
			}
		}
	}

	protected abstract Collection<? extends RepositoryObject> getRegisteredObjectSet();

	protected void validateRegisteredObjects() throws RegisterException {

		/*
		 * First check whether all registered objects currently in the
		 * repository do still exist
		 */
		Collection<RepositoryObject> toRemove = new HashSet<RepositoryObject>();
		for (RepositoryObject object : getRegisteredObjectSet())
			if (!new File(object.getAbsolutePath()).exists()) {
				toRemove.add(object);
			}

		for (RepositoryObject object : toRemove) {
			object.unregister();
			object.notify(new RepositoryRemoveEvent(object));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#findAndRegisterObjects()
	 */
	@Override
	public void findAndRegisterObjects() throws RegisterException {
		validateRegisteredObjects();
		super.findAndRegisterObjects();
	}

	protected abstract RepositoryObject parseObjectFromFile(final File file)
			throws Exception;
}
