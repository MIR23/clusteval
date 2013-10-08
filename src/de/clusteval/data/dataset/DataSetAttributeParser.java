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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.parse.TextFileParser;

/**
 * @author Christian Wiwie
 * 
 */
public class DataSetAttributeParser extends TextFileParser {

	/**
	 * Is used to determine whether a line contains an attribute
	 */
	public static String attributeLinePrefix = "//";

	/**
	 * The pattern build from the attributeLinePrefix
	 */
	public static Pattern attributeLinePrefixPattern = Pattern.compile("\\s*"
			+ attributeLinePrefix + ".*");

	protected Map<String, String> attributeValues;

	/**
	 * @param absFilePath
	 * @throws IOException
	 */
	public DataSetAttributeParser(String absFilePath) throws IOException {
		super(absFilePath, new int[0], new int[0], false);
		this.setLockTargetFile(true);
		this.attributeValues = new HashMap<String, String>();
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
		String line = value[0];

		Matcher m = attributeLinePrefixPattern.matcher(line);
		if (m.matches()) {
			/*
			 * Remove prefix
			 */
			String stripped = line.replace(attributeLinePrefix, "");
			/*
			 * Remove tabs and spaces
			 */
			stripped = stripped.replaceAll("\\s*", "");

			String[] split = stripped.split("=");
			this.attributeValues.put(split[0], split[1]);
		} else
			this.terminate();
	}

	/**
	 * @return A map containing all attributes together with their values.
	 */
	public Map<String, String> getAttributeValues() {
		return this.attributeValues;
	}
}
