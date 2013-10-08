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

/**
 * A {@link RepositoryRemoveEvent} is created, when some
 * {@link RepositoryObject} is unregistered from the repository, but not
 * replaced.
 * 
 * <p>
 * When this event is created, the removal already happend and it is the job of
 * the receiver of this event, to handle the removal gracefully, e.g. remove
 * themselves from the repository, in case they depended on the removed object.
 * 
 * @author Christian Wiwie
 * 
 */
public class RepositoryRemoveEvent implements RepositoryEvent {

	/**
	 * The object, that has been unregistered from the repository.
	 */
	protected RepositoryObject old;

	/**
	 * @param old
	 *            The object, that has been unregistered from the repository.
	 */
	public RepositoryRemoveEvent(final RepositoryObject old) {
		super();
		this.old = old;
	}

	/**
	 * @return The object, that has been unregistered from the repository.
	 */
	public RepositoryObject getRemovedObject() {
		return this.old;
	}
}
