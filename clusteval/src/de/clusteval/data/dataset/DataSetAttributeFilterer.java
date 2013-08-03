/**
 * 
 */
package de.clusteval.data.dataset;

import java.io.IOException;

import utils.parse.TextFileParser;

/**
 * @author Christian Wiwie
 * 
 */
public class DataSetAttributeFilterer extends TextFileParser {

	/**
	 * @param absFilePath
	 * @throws IOException
	 */
	public DataSetAttributeFilterer(String absFilePath) throws IOException {
		super(absFilePath, new int[0], new int[0], false, null, absFilePath
				+ ".strip", OUTPUT_MODE.STREAM);
		this.setLockTargetFile(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#isLockingTargetFile()
	 */
	@Override
	public boolean isLockingTargetFile() {
		return super.isLockingTargetFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#processLine(java.lang.String[],
	 * java.lang.String[])
	 */
	@SuppressWarnings("unused")
	@Override
	protected void processLine(String[] key, String[] value) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#checkLine(java.lang.String)
	 */
	@Override
	protected boolean checkLine(String line) {
		return !DataSetAttributeParser.attributeLinePrefixPattern.matcher(line)
				.matches();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#getLineOutput(java.lang.String[],
	 * java.lang.String[])
	 */
	@SuppressWarnings("unused")
	@Override
	protected String getLineOutput(String[] key, String[] value) {
		return value[0] + System.getProperty("line.separator");
	}

}
