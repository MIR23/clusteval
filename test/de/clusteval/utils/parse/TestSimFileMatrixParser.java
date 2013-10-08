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
package de.clusteval.utils.parse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import junitx.framework.FileAssert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.clusteval.data.dataset.DataSetAttributeFilterer;

import utils.SimilarityMatrix;
import utils.parse.SimFileMatrixParser;
import utils.parse.SimFileParser.SIM_FILE_FORMAT;
import utils.parse.TextFileParser.OUTPUT_MODE;

/**
 * @author Christian Wiwie
 * 
 */
public class TestSimFileMatrixParser {

	protected SimFileMatrixParser parser;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMatrixParsing() {
		try {
			/*
			 * "test/utils/parse/rowSimTestFile.sim"
			 */
			DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim");
			filterer.process();

			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM);
			parser.process();
			SimilarityMatrix matrix = parser.getSimilarities();

			double[][] expected = new double[][]{new double[]{1.0, 0.6, 0.5},
					new double[]{0.6, 0.5, 0.1}, new double[]{0.5, 0.1, 0.8}};
			SimilarityMatrix expectedMatrix = new SimilarityMatrix(expected);
			expectedMatrix.setIds(new String[]{"1", "2", "3"});
			assertEquals(matrix, expectedMatrix);

			filterer = new DataSetAttributeFilterer(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile2.sim");
			filterer.process();

			/*
			 * "test/utils/parse/rowSimTestFile2.sim"
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile2.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM);
			parser.process();
			matrix = parser.getSimilarities();

			expected = new double[][]{new double[]{2354.12, 394.2, 2214.0},
					new double[]{394.2, 2354.12, -123.0},
					new double[]{2214.0, -123.0, 2354.12}};
			expectedMatrix = new SimilarityMatrix(expected);
			expectedMatrix.setIds(new String[]{"1", "2", "3"});
			assertEquals(matrix, expectedMatrix);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Parsing did not succeed");
		}
	}

	@Test
	/**
	 * 
	 */
	public void testOutput() {
		try {
			DataSetAttributeFilterer filterer = new DataSetAttributeFilterer(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim");
			filterer.process();
			/*
			 * "test/utils/parse/rowSimTestFile.sim" BURST output
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM,
					null,
					null,
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out",
					OUTPUT_MODE.BURST, SIM_FILE_FORMAT.ID_ID_SIM);
			parser.process();

			/*
			 * Testing the similarity matrix
			 */
			SimilarityMatrix matrix = parser.getSimilarities();

			double[][] expected = new double[][]{new double[]{1.0, 0.6, 0.5},
					new double[]{0.6, 0.5, 0.1}, new double[]{0.5, 0.1, 0.8}};
			SimilarityMatrix expectedMatrix = new SimilarityMatrix(expected);
			expectedMatrix.setIds(new String[]{"1", "2", "3"});
			assertEquals(matrix, expectedMatrix);

			/*
			 * Testing the output file
			 */
			FileAssert
					.assertEquals(
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip"),
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out"));

			/*
			 * STREAM output
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM,
					null,
					null,
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out2",
					OUTPUT_MODE.STREAM, SIM_FILE_FORMAT.ID_ID_SIM);
			parser.process();

			FileAssert
					.assertEquals(
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out"),
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out2"));

			/*
			 * Convert ID_ID_SIM -> MATRIX_HEADER, STREAM
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM,
					null,
					null,
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.matriHeader",
					OUTPUT_MODE.STREAM, SIM_FILE_FORMAT.MATRIX_HEADER);
			parser.process();

			/*
			 * Convert ID_ID_SIM -> MATRIX_HEADER, BURST
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip",
					SIM_FILE_FORMAT.ID_ID_SIM,
					null,
					null,
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.matriHeader2",
					OUTPUT_MODE.BURST, SIM_FILE_FORMAT.MATRIX_HEADER);
			parser.process();

			FileAssert
					.assertEquals(
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.matriHeader"),
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.matriHeader2"));

			/*
			 * Convert MATRIX_HEADER -> ID_ID_SIM, BURST
			 */
			parser = new SimFileMatrixParser(
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.matriHeader",
					SIM_FILE_FORMAT.MATRIX_HEADER,
					null,
					null,
					"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.row",
					OUTPUT_MODE.BURST, SIM_FILE_FORMAT.ID_ID_SIM);
			parser.process();
			assertEquals(expectedMatrix, parser.getSimilarities());

			FileAssert
					.assertEquals(
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip"),
							new File(
									"testCaseRepository/data/datasets/rowSimTest/rowSimTestFile.sim.strip.out.row"));

		} catch (IOException e) {
			fail("Parsing did not succeed");
		}
	}
}
