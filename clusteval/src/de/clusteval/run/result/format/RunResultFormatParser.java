/**
 * 
 */
package de.clusteval.run.result.format;

import java.io.IOException;
import java.util.Map;

import utils.parse.TextFileParser;
import de.clusteval.run.result.ClusteringRunResult;

/**
 * @author Christian Wiwie
 */
public abstract class RunResultFormatParser extends TextFileParser {

	/** The params. */
	protected Map<String, String> params;

	protected Map<String, String> internalParams;

	/** The result. */
	protected ClusteringRunResult result;

	/**
	 * Instantiates a new run result format parser.
	 * 
	 * @param internalParams
	 * 
	 * @param params
	 *            the params
	 * @param absFilePath
	 *            the abs file path
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath)
			throws IOException {
		super(absFilePath, new int[0], new int[0], true, absFilePath + ".conv",
				OUTPUT_MODE.STREAM);
		this.setLockTargetFile(true);
		this.params = params;
		this.internalParams = internalParams;
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
	 * @param splitLines
	 *            the split lines
	 * @param outputMode
	 *            the output mode
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RunResultFormatParser(final Map<String, String> internalParams,
			final Map<String, String> params, final String absFilePath,
			final boolean splitLines, final OUTPUT_MODE outputMode)
			throws IOException {
		super(absFilePath, new int[0], new int[0], splitLines, absFilePath
				+ ".conv", outputMode);
		this.params = params;
		this.internalParams = internalParams;
	}

	/**
	 * Gets the run result.
	 * 
	 * @return the run result
	 */
	public ClusteringRunResult getRunResult() {
		return this.result;
	}

	/**
	 * Convert to standard format.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public abstract void convertToStandardFormat() throws IOException;
}