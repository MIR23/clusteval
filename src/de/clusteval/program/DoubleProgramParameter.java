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

import javax.script.ScriptException;

import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.InternalAttributeException;

/**
 * @author Christian Wiwie
 * 
 */
public class DoubleProgramParameter extends ProgramParameter<Double> {

	/**
	 * Parse a double program parameter from strings.
	 * 
	 * @param programConfig
	 *            The program configuration defining this parameter.
	 * @param name
	 *            The name of the parameter.
	 * @param desc
	 *            The description of the parameter.
	 * @param minValue
	 *            The minimal value of the parameter.
	 * @param maxValue
	 *            The maximal value of the parameter.
	 * @param def
	 *            The default value of the parameter.
	 * @return The parsed double program parameter.
	 * @throws RegisterException
	 */
	public static DoubleProgramParameter parseFromStrings(
			final ProgramConfig programConfig, final String name,
			final String desc, final String minValue, final String maxValue,
			final String def) throws RegisterException {
		final Repository repo = programConfig.getRepository();

		DoubleProgramParameter result = new DoubleProgramParameter(repo, true,
				programConfig, name, desc, minValue, maxValue, def);

		result = programConfig.getRepository().getRegisteredObject(result);

		return result;
	}

	/**
	 * The constructor for double program parameters.
	 * 
	 * @param repository
	 *            The repository to register the new parameter.
	 * @param register
	 *            Whether to register the new parameter.
	 * 
	 * @param programConfig
	 *            The program configuration defining this parameter.
	 * @param name
	 *            The name of the parameter.
	 * @param desc
	 *            The description of the parameter.
	 * @param minValue
	 *            The minimal value of the parameter.
	 * @param maxValue
	 *            The maximal value of the parameter.
	 * @param def
	 *            The default value of the parameter.
	 * @throws RegisterException
	 */
	protected DoubleProgramParameter(final Repository repository,
			final boolean register, final ProgramConfig programConfig,
			final String name, final String desc, String minValue,
			String maxValue, String def) throws RegisterException {
		super(repository, register, programConfig, name, desc, minValue,
				maxValue, new String[0], def);
	}

	/**
	 * The copy constructor of double program parameters.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DoubleProgramParameter(final DoubleProgramParameter other)
			throws RegisterException {
		super(other);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramParameter#clone(program.ProgramParameter)
	 */
	@Override
	public DoubleProgramParameter clone() {
		try {
			return new DoubleProgramParameter(this);
		} catch (RegisterException e) {
			// should not occur
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
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
	 * @see program.ProgramParameter#isMinValueSet()
	 */
	@Override
	public boolean isMinValueSet() {
		return !this.minValue.equals("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramParameter#isMaxValueSet()
	 */
	@Override
	public boolean isMaxValueSet() {
		return !this.maxValue.equals("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramParameter#evaluateMinValue()
	 */
	@Override
	public Double evaluateMinValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException {

		/*
		 * Parse minValue
		 */
		String newMinValue = this.repository.evaluateInternalAttributes(
				minValue, dataConfig, programConfig);

		try {
			newMinValue = this.repository.evaluateJavaScript(newMinValue);
		} catch (ScriptException e) {
			throw new InternalAttributeException("The expression '" + minValue
					+ "' for parameter attribute " + this.programConfig + "/"
					+ this.name + "/minValue is invalid");
		}

		return Double.parseDouble(newMinValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramParameter#evaluateMaxValue()
	 */
	@Override
	public Double evaluateMaxValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException {

		/*
		 * Parse maxValue
		 */
		String newMaxValue = this.repository.evaluateInternalAttributes(
				maxValue, dataConfig, programConfig);

		try {
			newMaxValue = this.repository.evaluateJavaScript(newMaxValue);
		} catch (ScriptException e) {
			throw new InternalAttributeException("The expression '" + maxValue
					+ "' for parameter attribute " + this.programConfig + "/"
					+ this.name + "/maxValue is invalid");
		}

		return Double.parseDouble(newMaxValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see program.ProgramParameter#evaluateDefaultValue()
	 */
	@Override
	public Double evaluateDefaultValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException {

		/*
		 * Parse default
		 */

		String newDefaultValue = this.repository.evaluateInternalAttributes(
				def, dataConfig, programConfig);

		try {
			newDefaultValue = this.repository
					.evaluateJavaScript(newDefaultValue);
		} catch (ScriptException e) {
			throw new InternalAttributeException("The expression '" + def
					+ "' for parameter attribute " + this.programConfig + "/"
					+ this.name + "/def is invalid");
		}

		return Double.parseDouble(newDefaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.program.ProgramParameter#evaluateOptions(de.clusteval.data
	 * .DataConfig, de.clusteval.program.ProgramConfig)
	 */
	@Override
	public Double[] evaluateOptions(DataConfig dataConfig,
			ProgramConfig programConfig) throws InternalAttributeException {
		return new Double[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.program.ProgramParameter#isOptionsSet()
	 */
	@Override
	public boolean isOptionsSet() {
		return false;
	}
}
