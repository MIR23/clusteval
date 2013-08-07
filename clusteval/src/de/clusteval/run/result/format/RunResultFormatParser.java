/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import utils.parse.TextFileParser;
import de.clusteval.data.DataConfig;
import de.clusteval.run.result.GraphMatchingRunResult;

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
}