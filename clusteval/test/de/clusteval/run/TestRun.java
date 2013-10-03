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
package de.clusteval.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.utils.TestRepositoryObject;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class TestRun extends TestRepositoryObject {

	@Test
	public void testRun() throws RegisterException {
		/*
		 * Ensure that a run is registered in the constructor
		 */
		Run run = new ClusteringRun(this.repository, context,
				System.currentTimeMillis(), new File("test"),
				new ArrayList<ProgramConfig>(), new ArrayList<DataConfig>(),
				new ArrayList<QualityMeasure>(),
				new ArrayList<Map<ProgramParameter<?>, String>>());
		Assert.assertTrue(run == this.repository.getRegisteredObject(run));
	}

	@Test
	public void testParseParameterOptimizationRun() throws RegisterException,
			UnknownDataSetFormatException, GoldStandardNotFoundException,
			GoldStandardConfigurationException, DataSetConfigurationException,
			DataSetNotFoundException, DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, NoDataSetException,
			DataConfigurationException, DataConfigNotFoundException,
			NumberFormatException, ConfigurationException,
			UnknownContextException, IOException,
			UnknownRunResultFormatException, UnknownQualityMeasureException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException, NoRepositoryFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownProgramTypeException, UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownDataSetTypeException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			IncompatibleContextException, UnknownParameterType {
		ClustevalBackendServer.logLevel(Level.INFO);
		// 03.10.2013: this run cannot be parsed because it is missing
		// optimizationMethod and optimizationCriterion
		// until now a null pointer exception is thrown, but we want to throw a
		// RunException
		try {
			ParameterOptimizationRun.parseFromFile(new File(FileUtils
					.buildPath("testCaseRepository", "runs", "netal.run")));
		} catch (Exception ex) {
			Assert.assertEquals(RunException.class, ex.getClass());
			Assert.assertEquals(
					"The optimization method has to be specified as attribute 'optimizationMethod'",
					ex.getMessage());
			return;
		}
		// we expect an exception
		Assert.assertTrue(false);
	}
}
