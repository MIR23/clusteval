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
package de.clusteval.program;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;

import de.clusteval.context.Context;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryEvent;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.framework.repository.RepositoryRemoveEvent;
import de.clusteval.framework.repository.RepositoryReplaceEvent;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.utils.RNotAvailableException;

/**
 * A wrapper class for programs used by this framework.
 * 
 * <p>
 * A program object encapsulates a executable, that can be executed using the
 * {@link #exec(DataConfig, ProgramConfig, String[], Map, Map)} method. This
 * method takes the data and its configuration, the program and its
 * configuration, the complete invocation line and all parameters used for this
 * invocation.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class Program extends RepositoryObject {

	/**
	 * Instantiates a new program.
	 * 
	 * @param repository
	 *            the repository this program should be registered at.
	 * @param register
	 *            Whether this program should be registered in the repository.
	 * @param changeDate
	 *            The change date of this program is used for equality checks.
	 * @param absPath
	 *            The absolute path of this program.
	 * @throws RegisterException
	 */
	public Program(final Repository repository, final boolean register,
			final long changeDate, final File absPath) throws RegisterException {
		// we register ourselves after initializing
		super(repository instanceof RunResultRepository ? repository
				.getParent() : repository, false, changeDate, absPath);

		if (register)
			this.register();
	}

	/**
	 * The copy constructor for programs.
	 * 
	 * @param program
	 *            The program to clone.
	 * @throws RegisterException
	 */
	protected Program(final Program program) throws RegisterException {
		super(program);
	}

	@Override
	public abstract Program clone();

	/**
	 * Gets the absolute path of the executable.
	 * 
	 * @return the executable
	 */
	public String getExecutable() {
		return absPath.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getMajorName();
	}

	/**
	 * This method returns the major name of this program. The major name of the
	 * program is defined as the foldername its executable lies in.
	 * 
	 * @return The major name of this program.
	 */
	public String getMajorName() {
		return this.absPath.getParentFile().getName();
	}

	/**
	 * This method returns the minor name of this program. The minor name
	 * corresponds to the name of the executable of this program.
	 * 
	 * @return The minor name.
	 */
	public String getMinorName() {
		return this.absPath.getName();
	}

	/**
	 * This method returns the full name of this program. The full name
	 * corresponds to the concatenated major and minor name separated by a
	 * slash: MAJOR/MINOR
	 * 
	 * @return The full name.
	 */
	public String getFullName() {
		return getMajorName() + "/" + getMinorName();
	}

	/**
	 * This alias is used whenever this program is visually represented and a
	 * readable name is needed.
	 * 
	 * @return The alias of this program.
	 */
	public abstract String getAlias();

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() throws RegisterException {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#notify(utils.RepositoryEvent)
	 */
	@Override
	public void notify(RepositoryEvent e) throws RegisterException {
		if (e instanceof RepositoryReplaceEvent) {
			RepositoryReplaceEvent event = (RepositoryReplaceEvent) e;
			if (event.getOld().equals(this))
				super.notify(event);
		} else if (e instanceof RepositoryRemoveEvent) {
			RepositoryRemoveEvent event = (RepositoryRemoveEvent) e;
			if (event.getRemovedObject().equals(this))
				super.notify(event);
		}
	}

	/**
	 * This method executes this program on the data defined in the data
	 * configuration.
	 * 
	 * <p>
	 * The complete invocation line is also passed. It is taken from the program
	 * configuration used by the run. All parameter placeholders contained in
	 * this invocation line are already replaced by their actual values.
	 * 
	 * <p>
	 * Additionally all parameter values are passed in the two map parameters.
	 * 
	 * @param dataConfig
	 *            This configuration encapsulates the data, this program should
	 *            be applied to.
	 * @param programConfig
	 *            This parameter contains some additional configuration for this
	 *            program.
	 * @param invocationLine
	 *            This is the complete invocation line, were all parameter
	 *            placeholders are already replaced by their actual values.
	 * @param effectiveParams
	 *            This map contains only the program parameters defined in the
	 *            program configuration together with their actual values.
	 * @param internalParams
	 *            This map contains parameters, that are not program specific,
	 *            but related and necessary for the execution of the program,
	 *            e.g. the path to the output or log files created by the
	 *            program.
	 * @return A Process object which can be used to get the status of or to
	 *         control the execution of this program.
	 * @throws IOException
	 * @throws RNotAvailableException
	 * @throws RLibraryNotLoadedException
	 * @throws REngineException
	 * @throws REXPMismatchException
	 */
	public abstract Process exec(final DataConfig dataConfig,
			final ProgramConfig programConfig, final String[] invocationLine,
			final Map<String, String> effectiveParams,
			final Map<String, String> internalParams) throws IOException,
			RNotAvailableException, RLibraryNotLoadedException, REngineException, REXPMismatchException;

	/**
	 * @return The context of this program. A run can only perform this program,
	 *         if it has the same context.
	 * @throws UnknownContextException
	 */
	public abstract Context getContext() throws UnknownContextException;
}
