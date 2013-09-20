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
package de.clusteval.context;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.run.result.format.RunResultFormat;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class Context extends RepositoryObject {

	/**
	 * @param repository
	 * @param contextName
	 * @return A context object of the class with the given simple name
	 * @throws UnknownContextException
	 */
	public static Context parseFromString(final Repository repository,
			final String contextName) throws UnknownContextException {

		Class<? extends Context> c = repository
				.getContextClass("de.clusteval.context." + contextName);
		Constructor<? extends Context> constr;
		try {
			constr = c.getConstructor(Repository.class, boolean.class,
					long.class, File.class);
			return constr.newInstance(repository, false,
					System.currentTimeMillis(), new File(contextName));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
		}
		throw new UnknownContextException("\"" + contextName
				+ "\" is not a known context.");
	}

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public Context(Repository repository, boolean register, long changeDate,
			File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * @param other
	 * @throws RegisterException
	 */
	public Context(final Context other) throws RegisterException {
		this(other.repository, false, other.changeDate, other.absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.framework.repository.RepositoryObject#equals(java.lang.Object
	 * )
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Context))
			return false;

		Context other = (Context) obj;
		return this.getName().equals(other.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.framework.repository.RepositoryObject#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	@Override
	public final RepositoryObject clone() {
		try {
			return this.getClass().getConstructor(this.getClass())
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

	@Override
	public boolean register() {
		return false;
	}

	@Override
	public boolean unregister() {
		return false;
	}

	/**
	 * Contexts have a unique name.
	 * 
	 * @return The name of this context
	 */
	public abstract String getName();

	/**
	 * @return A set with all simple names of classes this context requires.
	 */
	public abstract Set<String> getRequiredJavaClassFullNames();

	/**
	 * 
	 * @return The standard input format connected to this context. Every
	 *         context has its own standard format, which is used during
	 *         execution of runs.
	 */
	public abstract DataSetFormat getStandardInputFormat();

	/**
	 * 
	 * @return The standard output format connected to this context. Every
	 *         context has its own standard format, which is used during
	 *         execution of runs.
	 */
	public abstract RunResultFormat getStandardOutputFormat();

	@Override
	public String toString() {
		return getName();
	}
}
