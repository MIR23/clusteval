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
package de.clusteval.program.r;

import java.io.File;
import java.util.List;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.result.format.RunResultFormat;

/**
 * Objects of this class encapsulate the configuration of rprograms.
 * 
 * <p>
 * In principle this class does the same as a regular {@link ProgramConfig},
 * except that it takes the invocatin formats from the rprogram.
 * 
 * @author Christian Wiwie
 * 
 */
public class RProgramConfig extends ProgramConfig {

	/**
	 * Instantiates a new rprogram configuration.
	 * 
	 * @param repository
	 *            The repository this program configuration should be registered
	 *            at.
	 * @param register
	 *            A boolean indicating whether to register this rprogram
	 *            configuration.
	 * @param changeDate
	 *            The change date of this program configuration is used for
	 *            equality checks.
	 * @param absPath
	 *            The absolute path of this program configuration.
	 * @param program
	 *            The program this program configuration belongs to.
	 * @param outputFormat
	 *            The output format of the program.
	 * @param compatibleDataSetFormats
	 *            A list of compatible dataset formats of the encapsulated
	 *            program.
	 * @param params
	 *            The parameters of the program.
	 * @param optimizableParameters
	 *            The parameters of the program, that can be optimized.
	 * @throws RegisterException
	 */
	public RProgramConfig(Repository repository, final boolean register,
			long changeDate, File absPath, Program program,
			RunResultFormat outputFormat,
			List<String> compatibleDataSetFormats,
			List<ProgramParameter<?>> params,
			List<ProgramParameter<?>> optimizableParameters)
			throws RegisterException {
		super(repository, register,
				changeDate,
				absPath,
				program,
				outputFormat,
				compatibleDataSetFormats,
				((RProgram) program).getInvocationFormat(),
				// TODO: maybe introduce several invocation format options in R
				// program?
				((RProgram) program).getInvocationFormat(),
				((RProgram) program).getInvocationFormat(),
				((RProgram) program).getInvocationFormat(), params,
				optimizableParameters);
	}

	/**
	 * The copy constructor for RProgram configurations.
	 * 
	 * @param programConfig
	 *            The rprogram configuration to clone.
	 * @throws RegisterException
	 */
	public RProgramConfig(RProgramConfig programConfig)
			throws RegisterException {
		super(programConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramConfig#clone()
	 */
	@Override
	public RProgramConfig clone() {
		try {
			return new RProgramConfig(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}
}
