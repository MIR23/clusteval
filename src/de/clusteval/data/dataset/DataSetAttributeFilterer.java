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
