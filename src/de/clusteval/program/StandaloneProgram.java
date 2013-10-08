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
package de.clusteval.program;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.clusteval.context.Context;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;

/**
 * A type of program that corresponds to executables on the filesystem.
 * 
 * @author Christian Wiwie
 * 
 */
public class StandaloneProgram extends Program {

	protected String alias;
	protected Context context;

	/**
	 * @param repository
	 *            the repository this program should be registered at.
	 * @param context
	 *            The context of this program
	 * @param register
	 * @param changeDate
	 *            The change date of this program is used for equality checks.
	 * @param absPath
	 *            The absolute path of this program.
	 * @param alias
	 *            The alias of this program.
	 * @throws RegisterException
	 */
	public StandaloneProgram(Repository repository, final Context context,
			final boolean register, long changeDate, File absPath,
			final String alias) throws RegisterException {
		super(repository, false, changeDate, absPath);
		this.alias = alias;
		this.context = context;

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of standalone programs.
	 * 
	 * @param program
	 *            The standalone program to clone.
	 * @throws RegisterException
	 */
	public StandaloneProgram(final StandaloneProgram program)
			throws RegisterException {
		super(program);
		this.alias = program.alias;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#clone()
	 */
	@Override
	public StandaloneProgram clone() {
		try {
			return new StandaloneProgram(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#exec(program.ProgramConfig, java.lang.String,
	 * java.util.Map)
	 */
	@SuppressWarnings("unused")
	@Override
	public Process exec(final DataConfig dataConfig,
			final ProgramConfig programConfig, final String[] invocationLine,
			Map<String, String> effectiveParams,
			Map<String, String> internalParams) throws IOException {
		return Runtime.getRuntime().exec(invocationLine,
				// TODO, check whether this works everywhere
				new String[]{"TERM=xterm"},
				new File(programConfig.getProgram().getAbsolutePath())
						.getParentFile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.Program#getAlias()
	 */
	@Override
	public String getAlias() {
		return this.alias;
	}

	@Override
	public Context getContext() {
		return this.context;
	}

}
