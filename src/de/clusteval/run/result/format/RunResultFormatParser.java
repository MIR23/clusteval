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

import java.io.IOException;
import java.util.Map;

import utils.parse.TextFileParser;
import de.clusteval.data.DataConfig;

/**
 * @author Christian Wiwie
 */
public abstract class RunResultFormatParser extends TextFileParser {

	/** The params. */
	protected Map<String, String> params;

	protected Map<String, String> internalParams;

	protected DataConfig dataConfig;

	/**
	 * Instantiates a new run result format parser.
	 * 
	 * @param internalParams
	 * 
	 * @param params
	 *            the params
	 * @param absFilePath
	 *            the abs file path
	 * @param dataConfig
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath,
			final DataConfig dataConfig) throws IOException {
		super(absFilePath, new int[0], new int[0], true, null, absFilePath
				+ ".conv", OUTPUT_MODE.STREAM);
		this.setLockTargetFile(true);
		this.params = params;
		this.internalParams = internalParams;
		this.dataConfig = dataConfig;
	}

	/**
	 * Instantiates a new run result format parser.
	 * 
	 * @param internalParams
	 * 
	 * @param params
	 *            the params
	 * @param absFilePath
	 *            the abs file path
	 * @param dataConfig
	 * @param absOutputPath
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath,
			final DataConfig dataConfig, final String absOutputPath)
			throws IOException {
		super(absFilePath, new int[0], new int[0], true, null, absOutputPath,
				OUTPUT_MODE.STREAM);
		this.setLockTargetFile(true);
		this.params = params;
		this.internalParams = internalParams;
		this.dataConfig = dataConfig;
	}

	/**
	 * Instantiates a new run result format parser.
	 * 
	 * @param internalParams
	 * 
	 * @param params
	 *            the params
	 * @param absFilePath
	 *            the abs file path
	 * @param dataConfig
	 * @param splitLines
	 *            the split lines
	 * @param outputMode
	 *            the output mode
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath,
			final DataConfig dataConfig, final boolean splitLines,
			final OUTPUT_MODE outputMode) throws IOException {
		super(absFilePath, new int[0], new int[0], splitLines, null,
				absFilePath + ".conv", outputMode);
		this.params = params;
		this.internalParams = internalParams;
		this.dataConfig = dataConfig;
	}

	/**
	 * Convert to standard format.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void convertToStandardFormat() throws IOException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#getLineOutput(java.lang.String[],
	 * java.lang.String[])
	 */
	@SuppressWarnings("unused")
	@Override
	protected String getLineOutput(String[] key, String[] value) {
		StringBuilder sb = new StringBuilder();
		// write the header into the file containing the parameter values
		if (this.parsedLines == 0) {
			String[] paramNames = params.keySet().toArray(new String[0]);

			for (String paramName : paramNames) {
				sb.append(paramName);
				sb.append(",");
			}
			if (paramNames.length > 0)
				sb.deleteCharAt(sb.length() - 1);
			sb.append("\t");
			sb.append("Matching");
			sb.append(System.getProperty("line.separator"));

			for (String paramName : paramNames) {
				sb.append(params.get(paramName));
				sb.append(",");
			}
			if (paramNames.length > 0)
				sb.deleteCharAt(sb.length() - 1);
			sb.append(this.outSplit);
		}

		return sb.toString();
	}

}
