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
package de.clusteval.data.dataset.generator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class DataSetGenerator extends RepositoryObject {

	/**
	 * This attribute corresponds to the name of the folder located in
	 * {@link Repository#getDataSetBasePath()}, in which the dataset (and
	 * goldstandard) will be stored.
	 */
	private String folderName;

	/**
	 * This attribute corresponds to the name of the dataset file, in which the
	 * generated dataset (and optionally goldstandard) will be stored within the
	 * {@link #folderName}.
	 */
	private String fileName;

	/**
	 * The alias of the data set that is to be generated.
	 */
	private String alias;

	/**
	 * @param repository
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public DataSetGenerator(Repository repository, boolean register,
			long changeDate, File absPath) throws RegisterException {
		super(repository, register, changeDate, absPath);
	}

	/**
	 * The copy constructor of dataset generators.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public DataSetGenerator(DataSetGenerator other) throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.RepositoryObject#clone()
	 */
	@Override
	public RepositoryObject clone() {
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

	/**
	 * @return A wrapper object keeping all options of your dataset generator
	 *         together with the default options of all dataset generators. The
	 *         options returned by this method are going to be used and
	 *         interpreted in your subclass implementation in
	 *         {@link #generateDataSet()} .
	 */
	public Options getAllOptions() {
		// options of actual generator implementation
		Options options = this.getOptions();

		// default options of all generators
		this.addDefaultOptions(options);
		return options;
	}

	/**
	 * @return A wrapper object keeping the options of your dataset generator.
	 *         The options returned by this method are going to be used and
	 *         interpreted in your subclass implementation in
	 *         {@link #generateDataSet()} .
	 */
	protected abstract Options getOptions();

	/**
	 * If your dataset generator also creates a goldstandard for the generated
	 * dataset, this method has to return true.
	 * 
	 * <p>
	 * If a goldstandard is to be created, it is going to be stored under the
	 * same {@link #folderName} and with the same {@link #fileName} as the
	 * dataset, but within the goldstandard directory of the repository.
	 * 
	 * @return A boolean indicating, whether your dataset generator also
	 *         generates a corresponding goldstandard for the created dataset.
	 */
	public abstract boolean generatesGoldStandard();

	/**
	 * This method has to be invoked with command line arguments for this
	 * generator. Valid arguments are defined by the options returned by
	 * {@link #getOptions()}.
	 * 
	 * @param cliArguments
	 * @return The generated {@link DataSet}.
	 * @throws ParseException
	 *             This exception is thrown, if the passed arguments are not
	 *             valid.
	 * @throws DataSetGenerationException
	 * @throws GoldStandardGenerationException
	 */
	public DataSet generate(final String[] cliArguments) throws ParseException,
			DataSetGenerationException, GoldStandardGenerationException {
		CommandLineParser parser = new PosixParser();

		Options options = this.getAllOptions();

		CommandLine cmd = parser.parse(options, cliArguments);

		this.folderName = cmd.getOptionValue("folderName");
		this.fileName = cmd.getOptionValue("fileName");
		this.alias = cmd.getOptionValue("alias");

		this.handleOptions(cmd);

		// Ensure, that the dataset target file does not exist yet
		File targetFile = new File(FileUtils.buildPath(
				this.repository.getDataSetBasePath(), this.folderName,
				this.fileName));

		if (targetFile.exists())
			throw new ParseException(
					"A dataset with the given name does already exist!");
		targetFile.getParentFile().mkdirs();

		DataSet dataSet = generateDataSet();

		if (this.generatesGoldStandard()) {
			// Ensure, that the goldstandard target file does not exist yet
			targetFile = new File(FileUtils.buildPath(
					this.repository.getGoldStandardBasePath(), this.folderName,
					this.fileName));

			if (targetFile.exists())
				throw new ParseException(
						"A goldstandard with the given name does already exist!");
			targetFile.getParentFile().mkdirs();

			generateGoldStandard();
		}
		return dataSet;
	}

	/**
	 * Adds the default options of dataset generators to the given Options
	 * attribute
	 * 
	 * @param options
	 *            The existing Options attribute, holding already the options of
	 *            the actual generator implementation.
	 */
	private void addDefaultOptions(final Options options) {
		OptionBuilder.withArgName("folderName");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder
				.withDescription("The name of the folder to store this dataset in.");
		Option option = OptionBuilder.create("folderName");
		options.addOption(option);

		OptionBuilder.withArgName("fileName");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder
				.withDescription("The name of the dataset file to generate.");
		option = OptionBuilder.create("fileName");
		options.addOption(option);

		OptionBuilder.withArgName("alias");
		OptionBuilder.isRequired();
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("The alias of the data set.");
		option = OptionBuilder.create("alias");
		options.addOption(option);
	}

	/**
	 * This method is reponsible for interpreting the arguments passed to this
	 * generator call and to initialize possibly needed member variables.
	 * 
	 * <p>
	 * If you want to react to certain options in your implementation of
	 * {@link #generateDataSet()}, initialize member variables in this method.
	 * 
	 * @param cmd
	 *            A wrapper object for the arguments passed to this generator.
	 * @throws ParseException
	 */
	protected abstract void handleOptions(final CommandLine cmd)
			throws ParseException;

	protected String getFileName() {
		return this.fileName;
	}

	protected String getFolderName() {
		return this.folderName;
	}

	protected String getAlias() {
		return this.alias;
	}

	/**
	 * This method needs to be implemented in subclasses and is a helper method
	 * for {@link #generate(String[])}. It provides the core of a dataset
	 * generator by generating the dataset file and creating a {@link DataSet}
	 * wrapper object for it.
	 * 
	 * @return A {@link DataSet} wrapper object for the generated dataset file.
	 * @throws DataSetGenerationException
	 *             If something goes wrong during the generation process, this
	 *             exception is thrown.
	 */
	protected abstract DataSet generateDataSet()
			throws DataSetGenerationException;

	/**
	 * This method needs to be implemented in subclasses and is a helper method
	 * for {@link #generate(String[])}. It provides the functionality to
	 * generate the goldstandard file and creating a {@link GoldStandard}
	 * wrapper object for it.
	 * 
	 * @return A {@link GoldStandard} wrapper object for the generated
	 *         goldstandard file.
	 * @throws GoldStandardGenerationException
	 *             If something goes wrong during the generation process, this
	 *             exception is thrown.
	 */
	protected abstract GoldStandard generateGoldStandard()
			throws GoldStandardGenerationException;

	/**
	 * Parses a dataset generator from string.
	 * 
	 * @param repository
	 *            the repository
	 * @param dataSetGenerator
	 *            The simple name of the dataset generator class.
	 * @return the clustering quality measure
	 * @throws UnknownDataSetGeneratorException
	 */
	public static DataSetGenerator parseFromString(final Repository repository,
			String dataSetGenerator) throws UnknownDataSetGeneratorException {

		Class<? extends DataSetGenerator> c = repository
				.getDataSetGeneratorClass("de.clusteval.data.dataset.generator."
						+ dataSetGenerator);
		try {
			DataSetGenerator generator = c.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, false, System.currentTimeMillis(),
					new File(dataSetGenerator));
			return generator;

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {

		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		}
		throw new UnknownDataSetGeneratorException("\"" + dataSetGenerator
				+ "\" is not a known dataset generator.");
	}

	/**
	 * @return A set with names of all R libraries this class requires.
	 */
	public abstract Set<String> getRequiredRlibraries();

}
