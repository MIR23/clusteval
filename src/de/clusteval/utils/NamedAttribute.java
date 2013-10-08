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

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;


/**
 * This class is a wrapper for variables that should be shared throughout the
 * whole framework and should be unambigiously identifiable by their name.
 * 
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class NamedAttribute<T> extends RepositoryObject {

	protected String name;
	protected T value;

	/**
	 * @param repository
	 * @param name
	 * @param value
	 * @throws RegisterException
	 */
	public NamedAttribute(final Repository repository, final String name,
			final T value) throws RegisterException {
		super(repository, false, System.currentTimeMillis(), new File(name));

		this.name = name;
		this.value = value;
	}

	/**
	 * The copy constructor of named attributes.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public NamedAttribute(final NamedAttribute<T> other)
			throws RegisterException {
		super(other);

		this.name = other.name;
		this.value = cloneValue(other.value);
	}

	protected abstract T cloneValue(final T value);

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(this.getClass())))
			return false;

		@SuppressWarnings("unchecked")
		NamedAttribute<T> other = (NamedAttribute<T>) obj;
		return this.getName().equals(other.getName())
				&& this.getValue().equals(other.getValue());
	}

	/**
	 * @return The name of this attribute.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The value of this attribute.
	 */
	public T getValue() {
		return this.value;
	}

	/**
	 * @param value
	 *            The new value of this attribute.
	 */
	public void setValue(final T value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.getValue().toString();
	}
}
