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
package de.clusteval.run.result.format;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;

/**
 * Run results (e.g. clusterings) can have different formats. For all kinds of
 * operations the framework needs to know which format a runresult has and how
 * it can be converted to an understandable (standard) format.
 * 
 * <p>
 * Every runresult format comes together with a parser class (see
 * {@link RunResultFormatParser}).
 * 
 * @author Christian Wiwie
 */
public abstract class RunResultFormat extends RepositoryObject {

	/**
	 * This method parses a runresult format from the given string, containing a
	 * runresult format class name.
	 * 
	 * @param repository
	 *            The repository where to look up the runresult format class.
	 * @param runResultFormat
	 *            The runresult format class name as string.
	 * @return The parsed runresult format.
	 * @throws UnknownRunResultFormatException
	 */
	public static RunResultFormat parseFromString(final Repository repository,
			String runResultFormat) throws UnknownRunResultFormatException {

		Class<? extends RunResultFormat> c = repository
				.getRunResultFormatClass("de.clusteval.run.result.format."
						+ runResultFormat);
		try {
			return c.getConstructor(Repository.class, boolean.class,
					long.class, File.class).newInstance(repository, false,
					System.currentTimeMillis(), new File(runResultFormat));
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		throw new UnknownRunResultFormatException("The format \""
				+ runResultFormat + "\" is unknown.");
	}

	/**
	 * Instantiates a new runresult format.
	 * 
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RunResultFormat(final Repository repo, final boolean register,
			final long changeDate, final File absPath) throws RegisterException {
		super(repo, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of runresult formats.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public RunResultFormat(final RunResultFormat other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return this.repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RunResultFormat))
			return false;

		RunResultFormat other = (RunResultFormat) obj;

		return this.getClass().equals(other.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public final RunResultFormat clone() {
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

	/**
	 * @param absPath
	 *            The absolute file path passed to the program to store the
	 *            runresult belonging to this runresult format.
	 * @return The absolute path to the file in which the program has stored the
	 *         runresult (not necessarily the passed path).
	 */
	public abstract File getRunResultFile(final File absPath);
}
