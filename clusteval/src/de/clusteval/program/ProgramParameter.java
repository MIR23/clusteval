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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;

import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.utils.InternalAttributeException;

/**
 * An object of this class corresponds to a parameter of a program.
 * 
 * <p>
 * Therefore a program parameter has a reference to a program configuration in
 * which it was defined.
 * 
 * <p>
 * A program parameter has a certain {@link #name} unique for the program
 * configuration, a {@link #description}, a minimal value {@link #minValue}, a
 * maximal value {@link #maxValue} and a default value {@link #def}.
 * 
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class ProgramParameter<T> extends RepositoryObject {

	/**
	 * A helper method for cloning a list of parameters.
	 * 
	 * @param programParameters
	 *            The optimization parameters to clone.
	 * @return The list of cloned optimization parameters.
	 */
	public static List<List<ProgramParameter<?>>> cloneParameterListList(
			List<List<ProgramParameter<?>>> programParameters) {
		List<List<ProgramParameter<?>>> result = new ArrayList<List<ProgramParameter<?>>>();

		for (List<ProgramParameter<?>> list : programParameters) {
			List<ProgramParameter<?>> copyList = new ArrayList<ProgramParameter<?>>();

			for (ProgramParameter<?> param : list)
				copyList.add(param.clone());

			result.add(copyList);
		}

		return result;
	}

	/**
	 * A helper method for cloning a list of parameters.
	 * 
	 * @param programParameters
	 *            The optimization parameters to clone.
	 * @return The list of cloned optimization parameters.
	 */
	public static List<ProgramParameter<?>> cloneParameterList(
			List<ProgramParameter<?>> programParameters) {
		List<ProgramParameter<?>> result = new ArrayList<ProgramParameter<?>>();

		for (ProgramParameter<?> param : programParameters)
			result.add(param.clone());

		return result;
	}

	/**
	 * The program configuration which defined this parameter.
	 */
	protected ProgramConfig programConfig;

	/**
	 * The name of this parameter has to be unique for the program configuration
	 * and program.
	 */
	protected String name;

	/**
	 * A program parameter can have a description.
	 */
	protected String description;

	/**
	 * The minimal value this parameter can be set to. The attribute holds a
	 * string, because the value of this variable can hold a placeholder which
	 * is replaced dynamically by the framework, e.g $(meanSimilarity).
	 */
	protected String minValue;

	/**
	 * The maximal value this parameter can be set to. The attribute holds a
	 * string, because the value of this variable can hold a placeholder which
	 * is replaced dynamically by the framework, e.g $(meanSimilarity).
	 */
	protected String maxValue;

	/**
	 * The possible values this parameter can be set to. The attribute holds a
	 * string[], because the values of this variable can hold placeholders which
	 * are replaced dynamically by the framework, e.g $(meanSimilarity).
	 */
	protected String[] options;

	/**
	 * The default value of this parameter. The attribute holds a string,
	 * because the value of this variable can hold a placeholder which is
	 * replaced dynamically by the framework, e.g $(meanSimilarity).
	 */
	protected String def;

	/**
	 * Instantiates a new program parameter.
	 * 
	 * <p>
	 * The absolute path of a program parameter is defined as the absolute path
	 * of its program configuration concatenated with its name, separated by a
	 * colon.
	 * 
	 * @param repository
	 *            The repository at which to register this program parameter.
	 * @param register
	 *            A boolean indicating, whether to register this object at a
	 *            repository.
	 * 
	 * @param programConfig
	 *            The program configuration which defined this parameter.
	 * @param name
	 *            The name of this parameter has to be unique for the program
	 *            configuration and program.
	 * @param desc
	 *            The name of this parameter has to be unique for the program
	 *            configuration and program.
	 * @param minValue
	 *            The minimal value this parameter can be set to (see
	 *            {@link #minValue}).
	 * @param maxValue
	 *            The maximal value this parameter can be set to (see
	 *            {@link #maxValue}).
	 * @param options
	 *            The possible values of this parameter.
	 * @param def
	 *            The default value of this parameter (see {@link #def}).
	 * @throws RegisterException
	 */
	public ProgramParameter(final Repository repository,
			final boolean register, final ProgramConfig programConfig,
			final String name, final String desc, final String minValue,
			final String maxValue, final String[] options, final String def)
			throws RegisterException {
		super(repository, false, System.currentTimeMillis(), new File(
				programConfig.getAbsolutePath() + ":" + name));

		this.programConfig = programConfig;
		this.name = name;
		this.description = desc;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.options = options;
		this.def = def;

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of program parameters.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ProgramParameter(final ProgramParameter<T> other)
			throws RegisterException {
		super(other);

		// do not clone, because it is upstream
		this.programConfig = other.programConfig;
		this.name = other.name;
		this.description = other.description;
		this.minValue = other.minValue;
		this.maxValue = other.maxValue;
		this.options = other.options;
		this.def = other.def;

	}

	@Override
	public abstract ProgramParameter<T> clone();

	/**
	 * Sets the minimal value.
	 * 
	 * @param minValue
	 *            The new minimal value.
	 */
	public void setMinValue(final String minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return The minimal value of this parameter.
	 */
	public String getMinValue() {
		return this.minValue;
	}

	/**
	 * This method evaluates the string representation of the minimal value
	 * {@link #minValue} to a value corresponding to the dynamic type of this
	 * object, e.g. in case this parameter is a double parameter, it is
	 * evaluated to a double value.
	 * 
	 * <p>
	 * The method requires a data and program configuration, since the string
	 * representation can contain a placeholder of a internal variable which is
	 * replaced by looking it up during runtime. $(meanSimilarity) for example
	 * is evaluated by looking into the data and calculating the mean similarity
	 * of the input.
	 * 
	 * @param dataConfig
	 *            The data configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @param programConfig
	 *            The program configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @return The evaluated value of the {@link #minValue} variable.
	 * @throws InternalAttributeException
	 */
	public abstract T evaluateMinValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException;

	/**
	 * This method checks, whether the {@link #minValue} variable has been set
	 * to a correct not-null value.
	 * 
	 * @return True, if the variable has been set correctly, false otherwise.
	 */
	public abstract boolean isMinValueSet();

	/**
	 * Sets the maximal value.
	 * 
	 * @param maxValue
	 *            The new maximal value
	 */
	public void setMaxValue(final String maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return The maximal value of this parameter.
	 */
	public String getMaxValue() {
		return this.maxValue;
	}

	/**
	 * This method evaluates the string representation of the maximal value
	 * {@link #maxValue} to a value corresponding to the dynamic type of this
	 * object, e.g. in case this parameter is a double parameter, it is
	 * evaluated to a double value.
	 * 
	 * <p>
	 * The method requires a data and program configuration, since the string
	 * representation can contain a placeholder of a internal variable which is
	 * replaced by looking it up during runtime. $(meanSimilarity) for example
	 * is evaluated by looking into the data and calculating the mean similarity
	 * of the input.
	 * 
	 * @param dataConfig
	 *            The data configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @param programConfig
	 *            The program configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @return The evaluated value of the {@link #maxValue} variable.
	 * @throws InternalAttributeException
	 */
	public abstract T evaluateMaxValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException;

	/**
	 * This method checks, whether the {@link #maxValue} variable has been set
	 * to a correct not-null value.
	 * 
	 * @return True, if the variable has been set correctly, false otherwise.
	 */
	public abstract boolean isMaxValueSet();

	/**
	 * Sets the minimal value.
	 * 
	 * @param options
	 *            The possible values of this parameter.
	 */
	public void setOptions(final String[] options) {
		this.options = options;
	}

	/**
	 * @return The possible values of this parameter.
	 */
	public String[] getOptions() {
		return this.options;
	}

	/**
	 * This method evaluates the string representation of the options
	 * {@link #options} to a value corresponding to the dynamic type of this
	 * object, e.g. in case this parameter is a double parameter, it is
	 * evaluated to a double value.
	 * 
	 * <p>
	 * The method requires a data and program configuration, since the string
	 * representation can contain a placeholder of a internal variable which is
	 * replaced by looking it up during runtime. $(meanSimilarity) for example
	 * is evaluated by looking into the data and calculating the mean similarity
	 * of the input.
	 * 
	 * @param dataConfig
	 *            The data configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @param programConfig
	 *            The program configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @return The evaluated value of the {@link #minValue} variable.
	 * @throws InternalAttributeException
	 */
	public abstract T[] evaluateOptions(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException;

	/**
	 * This method checks, whether the {@link #options} variable has been set to
	 * a correct not-null value.
	 * 
	 * @return True, if the variable has been set correctly, false otherwise.
	 */
	public abstract boolean isOptionsSet();

	/**
	 * @return The name of this parameter.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The program configuration which defined this parameter.
	 */
	public ProgramConfig getProgramConfig() {
		return this.programConfig;
	}

	// changed 12.07.2012
	@Override
	public String toString() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(this.getClass().equals(o.getClass())))
			return false;
		ProgramParameter<?> other = (ProgramParameter<?>) o;
		return super.equals(o)
				&& this.programConfig.equals(other.programConfig)
				&& this.name.equals(other.name)
				&& ((this.getDefault() == null && other.getDefault() == null) || (this
						.getDefault() != null && other.getDefault() != null && this
						.getDefault().equals(other.getDefault())))
				&& ((this.getMinValue() == null && other.getMinValue() == null) || (this
						.getMinValue() != null && other.getMinValue() != null && this
						.getMinValue().equals(other.getMinValue())))
				&& ((this.getMaxValue() == null && other.getMaxValue() == null) || (this
						.getMaxValue() != null && other.getMaxValue() != null && this
						.getMaxValue().equals(other.getMaxValue())));
	}

	/**
	 * @return The default value of this parameter.
	 */
	public String getDefault() {
		return this.def;
	}

	/**
	 * @return The description of this parameter.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * This method evaluates the string representation of the default value
	 * {@link #def} to a value corresponding to the dynamic type of this object,
	 * e.g. in case this parameter is a double parameter, it is evaluated to a
	 * double value.
	 * 
	 * <p>
	 * The method requires a data and program configuration, since the string
	 * representation can contain a placeholder of a internal variable which is
	 * replaced by looking it up during runtime. $(meanSimilarity) for example
	 * is evaluated by looking into the data and calculating the mean similarity
	 * of the input.
	 * 
	 * @param dataConfig
	 *            The data configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @param programConfig
	 *            The program configuration which might be needed to evaluate
	 *            certain placeholder variables.
	 * @return The evaluated value of the {@link #def} variable.
	 * @throws InternalAttributeException
	 */
	public abstract T evaluateDefaultValue(final DataConfig dataConfig,
			final ProgramConfig programConfig)
			throws InternalAttributeException;

	/**
	 * Sets the default value of this parameter.
	 * 
	 * @param def
	 *            The new default value of this parameter.
	 */
	public void setDefault(String def) {
		this.def = def;
	}

	/**
	 * Parses a program parameter from a section of a configuration file.
	 * 
	 * <p>
	 * This method only delegates depending on the type of the program parameter
	 * to the methods
	 * {@link DoubleProgramParameter#parseFromStrings(ProgramConfig, String, String, String, String, String)},
	 * {@link IntegerProgramParameter#parseFromStrings(ProgramConfig, String, String, String, String, String)}
	 * and
	 * {@link StringProgramParameter#parseFromStrings(ProgramConfig, String, String, String, String, String)}.
	 * 
	 * @param programConfig
	 *            The program configuration in which the program parameter has
	 *            been defined.
	 * @param name
	 *            The name of the program parameter.
	 * @param config
	 *            The section of the configuration, which contains the
	 *            information about this parameter.
	 * @return The parsed program parameter.
	 * @throws RegisterException
	 * @throws UnknownParameterType
	 */
	public static ProgramParameter<?> parseFromConfiguration(
			final ProgramConfig programConfig, final String name,
			final SubnodeConfiguration config) throws RegisterException,
			UnknownParameterType {
		Map<String, String> paramValues = new HashMap<String, String>();
		paramValues.put("name", name);

		Iterator<String> itSubParams = config.getKeys();
		while (itSubParams.hasNext()) {
			final String subParam = itSubParams.next();
			final String value = config.getString(subParam);

			paramValues.put(subParam, value);
		}
		ParameterType type = ProgramParameter.parseTypeFromString(paramValues
				.get("type"));

		String na = paramValues.get("name");
		String description = paramValues.get("desc");
		String def = paramValues.get("def");

		ProgramParameter<?> param = null;
		if (type.equals(ParameterType.FLOAT)) {
			String minValue = paramValues.get("minValue");
			String maxValue = paramValues.get("maxValue");
			param = DoubleProgramParameter.parseFromStrings(programConfig, na,
					description, minValue, maxValue, def);
		} else if (type.equals(ParameterType.INTEGER)) {
			String minValue = paramValues.get("minValue");
			String maxValue = paramValues.get("maxValue");
			param = IntegerProgramParameter.parseFromStrings(programConfig, na,
					description, minValue, maxValue, def);
		} else if (type.equals(ParameterType.STRING)) {
			String[] options = config.getStringArray("options");
			param = StringProgramParameter.parseFromStrings(programConfig, na,
					description, options, def);
		}
		return param;
	}

	/**
	 * Parses a parameter type from a string. There are different strings
	 * representing the types of parameters:
	 * 
	 * <p>
	 * <ul>
	 * <li><b>0</b>: A String parameter holding only string values.</li>
	 * <li><b>1</b>: An integer parameter holding integer values.</li>
	 * <li><b>2</b>: A double parameter holding double values.</li>
	 * </ul>
	 * 
	 * <p>
	 * A helper method for
	 * {@link #parseFromConfiguration(ProgramConfig, String, SubnodeConfiguration)}.
	 * 
	 * @param value
	 *            A string indicating a number corresponding to a parameter
	 *            type.
	 * @return the parameter type
	 * @throws UnknownParameterType
	 */
	private static ParameterType parseTypeFromString(String value)
			throws UnknownParameterType {

		// String
		if (value.equals("0")) {
			return ParameterType.STRING;
		}
		// Integer
		else if (value.equals("1")) {
			return ParameterType.INTEGER;
		}
		// Float
		else if (value.equals("2")) {
			return ParameterType.FLOAT;
		}

		throw new UnknownParameterType("The parameter type " + value
				+ " is unknown");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.programConfig.toString() + this.name).hashCode();
	}

	/**
	 * An enumeration for parameter types. Program parameters can be of
	 * different type and this enumeration helps distinguishing them.
	 * 
	 * @author Christian Wiwie
	 * 
	 */
	public enum ParameterType {
		/**
		 * A string parameter holding string values.
		 */
		STRING,
		/**
		 * An integer parameter holding integer values.
		 */
		INTEGER,
		/**
		 * A float parameter holding float values.
		 */
		FLOAT
	}
}
