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
 * A {@link RepositoryReplaceEvent} is created, when some repository object is
 * replaced by another repository object, where replacing means, that the old
 * object is unregistered and the new object is registered instead.
 * 
 * <p>
 * The replace-constraints are enforced by the repository in the register
 * methods. For example an object <b>old</b> is only replaced by another object
 * <b>new</b>, if <b>old.equals(new)</b>.
 * 
 * <p>
 * When this event is created, the replacement already happend and it is the job
 * of the receiver of this event, to handle the replacement gracefully, e.g.
 * updating references from the old to the new object.
 * 
 * @author Christian Wiwie
 * 
 */
public class RepositoryReplaceEvent implements RepositoryEvent {

	/**
	 * The object, that has been replaced and unregistered from the repository.
	 */
	protected RepositoryObject old;

	/**
	 * The object, that has replaced the old object and was registered at the
	 * repository.
	 */
	protected RepositoryObject replacement;

	/**
	 * @param old
	 *            The object, that has been replaced and unregistered from the
	 *            repository.
	 * @param replacement
	 *            The object, that has replaced the old object and was
	 *            registered at the repository.
	 */
	public RepositoryReplaceEvent(final RepositoryObject old,
			final RepositoryObject replacement) {
		super();

		this.old = old;
		this.replacement = replacement;
	}

	/**
	 * 
	 * @return The object, that has been replaced and unregistered from the
	 *         repository.
	 */
	public RepositoryObject getOld() {
		return this.old;
	}

	/**
	 * 
	 * @return The object, that has replaced the old object and was registered
	 *         at the repository.
	 */
	public RepositoryObject getReplacement() {
		return this.replacement;
	}
}
