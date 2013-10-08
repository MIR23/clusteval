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
package de.clusteval.framework.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import file.FileUtils;

/**
 * A {@link RepositoryObject} provides integrated functionalities in terms of
 * automatic handling by the {@link Repository} it is registered in.
 * 
 * <p>
 * Functionality of this repository registration includes
 * <ul>
 * <li>automatic detection of changes of repository objects</li>
 * <li>automatic notification of changes about other repository objects this
 * object listens to</li>
 * <li>notifications of other objects about changes of this object</li>
 * <li>central access to all objects of the framework in the repository</li>
 * <li>copy handling</li>
 * </ul>
 * 
 * @author Christian Wiwie
 */
public abstract class RepositoryObject implements RepositoryListener {

	/**
	 * The repository this object is registered in.
	 */
	protected Repository repository;

	/**
	 * The changedate of this object can be used for identification and equality
	 * checks of objects.
	 */
	protected long changeDate;

	/**
	 * A set with all the listeners, that want to be informed about changes of
	 * this object.
	 */
	protected Set<RepositoryListener> listener;

	/**
	 * The absolute path of this object is used for identification and equality
	 * checks of objects.
	 */
	protected File absPath;

	protected Logger log;

	/**
	 * Instantiates a new repository object.
	 * 
	 * <p>
	 * This is a convenience constructor which implicitely registers the new
	 * object in its repository.
	 * 
	 * @param repository
	 *            The repository this object is registered in.
	 * @param changeDate
	 *            The changedate of this object can be used for identification
	 *            and equality checks of objects.
	 * @param absPath
	 *            The absolute path of this object is used for identification
	 *            and equality checks of objects.
	 * @throws RegisterException
	 */
	public RepositoryObject(final Repository repository, final long changeDate,
			final File absPath) throws RegisterException {
		this(repository, true, changeDate, absPath);
	}

	/**
	 * Instantiates a new repository object.
	 * 
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
	 * @throws RegisterException
	 */
	public RepositoryObject(final Repository repository,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		super();
		this.repository = repository;
		this.changeDate = changeDate;
		this.listener = Collections
				.synchronizedSet(new HashSet<RepositoryListener>());
		this.absPath = absPath;
		this.log = LoggerFactory.getLogger(this.getClass());

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for repository objects.
	 * 
	 * <p>
	 * Cloned repository objects are never registered at the repository.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RepositoryObject(final RepositoryObject other)
			throws RegisterException {
		this(other.repository, false, other.changeDate, new File(
				other.absPath.getAbsolutePath()));
	}

	@Override
	public abstract RepositoryObject clone();

	/**
	 * @return The repository this object is registered in.
	 * @see #repository
	 */
	public Repository getRepository() {
		return this.repository;
	}

	/**
	 * @return The absolute path of this repository object.
	 * @see #absPath
	 */
	public String getAbsolutePath() {
		return absPath.getAbsolutePath();
	}

	/**
	 * @param absFilePath
	 *            The new absolute file path.
	 * @see #absPath
	 */
	public void setAbsolutePath(final File absFilePath) {
		this.absPath = absFilePath;
	}

	/**
	 * Any subclass needs to implement this method. It will be responsible to
	 * register a new object of the subclass at the repository.
	 * 
	 * @return true, if successful
	 * @throws RegisterException
	 *             An exception is thrown if something goes wrong during the
	 *             registering process, that might be interesting to handle
	 *             individually.
	 */
	public abstract boolean register() throws RegisterException;

	/**
	 * Any subclass needs to implement this method. It will be responsible to
	 * unregister an object of the subclass from the repository.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean unregister();

	/**
	 * Do on register.
	 */
	protected void doOnRegister() {
		// by default nothing
	}

	/**
	 * @return The changedate of this repository object.
	 * @see #changeDate
	 */
	public long getChangeDate() {
		return this.changeDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		RepositoryObject other = (RepositoryObject) obj;

		return this.repository.equals(other.repository)
				&& ((this.absPath == null && other.absPath == null) || this.absPath
						.equals(other.absPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.repository.toString() + this.absPath.toString())
				.hashCode();
	}

	/**
	 * A convenience method for {@link #copyTo(File, boolean)}, with overwriting
	 * enabled.
	 * 
	 * @param copyDestination
	 *            The absolute path to the destination file.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyTo(final File copyDestination) {
		return copyTo(copyDestination, true);
	}

	/**
	 * A convenience method for {@link #copyTo(File, boolean, boolean)}, with
	 * waiting for the operation to finish.
	 * 
	 * @param copyDestination
	 *            The absolute path to the destination file.
	 * @param overwrite
	 *            Whether the possibly already existing target file should be
	 *            overwritten.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyTo(final File copyDestination, final boolean overwrite) {
		// by default we wait until the copied file is equal
		return copyTo(copyDestination, overwrite, true);
	}

	/**
	 * This method copies the file corresponding to this repository object to
	 * the destination.
	 * 
	 * <p>
	 * <b>Hint:</b> Use the wait parameter with caution: It might increase the
	 * ressource load of this method considerably. Also the wait operation might
	 * not terminate, if source and target filesystem use different encodings
	 * and the equality checks return false.
	 * 
	 * @param copyDestination
	 *            The absolute path to the destination file.
	 * @param overwrite
	 *            Whether the possibly already existing target file should be
	 *            overwritten.
	 * @param wait
	 *            Whether to wait for this operation to finish, were the
	 *            completion of the operation is determined by steadily
	 *            comparing source and target file for equality.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyTo(final File copyDestination, final boolean overwrite,
			final boolean wait) {
		try {
			if (!copyDestination.exists() || overwrite)
				org.apache.commons.io.FileUtils.copyFile(this.absPath,
						copyDestination);
			while (wait
					&& !org.apache.commons.io.FileUtils.contentEquals(
							this.absPath, copyDestination)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * A convenience method for {@link #copyToFolder(File, boolean)}, with
	 * overwriting enabled.
	 * 
	 * @param copyFolderDestination
	 *            The folder in which this file should be copied
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyToFolder(final File copyFolderDestination) {
		return copyToFolder(copyFolderDestination, true);
	}

	/**
	 * This method copies the file corresponding to this repository object into
	 * the destination folder.
	 * 
	 * @param copyFolderDestination
	 *            The folder in which this file should be copied
	 * @param overwrite
	 *            Whether a possibly already existing target file within the
	 *            destination folder should be overwritten.
	 * @return True, if the copy operation was successful.
	 */
	public boolean copyToFolder(final File copyFolderDestination,
			final boolean overwrite) {
		File targetFile = new File(
				FileUtils.buildPath(copyFolderDestination.getAbsolutePath(),
						this.absPath.getName()));
		return copyTo(targetFile, overwrite);
	}

	/**
	 * Add a listener to this objects listeners. Those are for example informed
	 * when this object is removed from the repository or replaced by another
	 * object.
	 * 
	 * @param listener
	 *            The new listener.
	 * @return True, if the listener was added successfully
	 */
	public boolean addListener(final RepositoryListener listener) {
		if (listener.equals(this))
			return false;
		return this.listener.add(listener);
	}

	/**
	 * Remove a listener from this objects listener.
	 * 
	 * @param listener
	 *            The listener to remove.
	 * @return True, if the listener was removed successfully
	 */
	public boolean removeListener(final RepositoryListener listener) {
		return this.listener.remove(listener);
	}

	/**
	 * A helper method of {@link #notify(RepositoryEvent)}, in case this object
	 * needs to inform its listeners about the passed event.
	 * 
	 * @param event
	 *            The event related to this object which is going to be
	 *            propagated to the listeners of this object.
	 * @throws RegisterException
	 */
	private void notifyListener(final RepositoryEvent event)
			throws RegisterException {
		List<RepositoryListener> toNotify = new ArrayList<RepositoryListener>(
				this.listener);
		for (RepositoryListener listener : toNotify) {
			listener.notify(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryListener#notify(utils.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (event.old.equals(this))
				// this object is going to be removed and replaced from the
				// repository
				this.notifyListener(event);
			else {
				// do something in subclasses
			}
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.old.equals(this)) {
				// this object is going to be removed and replaced from the
				// repository
				this.notifyListener(event);
			} else {
				// do something in subclasses
			}
		}
	}

	/**
	 * @return The logger of this object.
	 */
	public Logger getLog() {
		return log;
	}
}
