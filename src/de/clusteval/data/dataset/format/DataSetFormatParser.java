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
package de.clusteval.data.dataset.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.data.dataset.DataSet;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

/**
 * @author Christian Wiwie
 */
public abstract class DataSetFormatParser {

	/** The log. */
	protected Logger log;

	protected boolean normalize;

	/*
	 * Determines to which version of the DataSetFormat this parser converts to
	 * and from.
	 */
	protected int version;

	/**
	 * Instantiates a new data set format parser.
	 */
	public DataSetFormatParser() {
		super();
		this.version = 1;
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * The Enum DATASETFORMAT_SUFFIX.
	 */
	protected static enum DATASETFORMAT_SUFFIX {

		/** The Row sim. */
		RowSim,
		/** The AP row sim. */
		APRowSim,
		/** The Sim matrix. */
		SimMatrix
	}

	/**
	 * @param dataSets
	 *            The datasets to be parsed.
	 * @return A wrapper object containing the contents of the dataset
	 * @throws IllegalArgumentException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IOException
	 */
	public abstract DirectedSparseMultigraph<String, String> parse(
			List<DataSet> dataSets) throws IOException,
			InvalidDataSetFormatVersionException;

	/**
	 * This method writes the contents of the dataset hold in memory to the
	 * filesystem.
	 * 
	 * <p>
	 * This method assumes, that the data set has the correct format and that
	 * the dataset is loaded into memory. If any of these conditions does not
	 * hold, nothing is written to the filesystem.
	 * 
	 * @param dataSet
	 * @return
	 */
	protected final boolean writeToFile(DataSet dataSet,
			final boolean withHeader) {
		if (!dataSet.getDataSetFormat().getClass().getSimpleName()
				.equals(this.getClass().getSimpleName().replace("Parser", "")))
			return false;

		// TODO
//		if (!dataSet.isInMemory())
//			return false;

		// create the target file
		final File dataSetFile = new File(dataSet.getAbsolutePath());

		try {
			// dataset file
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					dataSetFile));
			if (withHeader)
				writeHeaderIntoFile(dataSet, writer);
			writeToFileHelper(dataSet, writer);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected abstract void writeToFileHelper(DataSet dataSet,
			BufferedWriter writer) throws IOException;

	protected final void writeHeaderIntoFile(DataSet dataSet,
			BufferedWriter writer) throws IOException {
		// writer header
		writer.append("// dataSetFormat = ");
		writer.append(dataSet.getDataSetFormat().getClass().getSimpleName());
		writer.newLine();
		writer.append("// dataSetType = ");
		writer.append(dataSet.getDataSetType().getClass().getSimpleName());
		writer.newLine();
		writer.append("// dataSetFormatVersion = ");
		writer.append(dataSet.getDataSetFormat().getVersion() + "");
		writer.newLine();
	}

	/**
	 * Removes the result file name suffix.
	 * 
	 * @param resultFileName
	 *            the result file name
	 * @return the string
	 */
	protected static String removeResultFileNameSuffix(
			final String resultFileName) {
		StringBuilder sb = new StringBuilder(resultFileName);
		for (DATASETFORMAT_SUFFIX suffix : DATASETFORMAT_SUFFIX.values()) {
			if (resultFileName.endsWith("." + suffix.name())) {
				int pos = sb.lastIndexOf("." + suffix.name());
				sb.delete(pos, pos + ("." + suffix.name()).length());
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * @param normalize
	 *            Whether this dataset should be normalized.
	 */
	public void setNormalize(final boolean normalize) {
		this.normalize = normalize;
	}
}
