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
package de.clusteval.paramOptimization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.run.Run;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.utils.TestRepositoryObject;
import de.clusteval.utils.plot.Plotter;

/**
 * @author Christian Wiwie
 * 
 */
public class TestParameterOptimizationResult extends TestRepositoryObject {

	/**
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		List<ParameterOptimizationResult> result = new ArrayList<ParameterOptimizationResult>();
		final Run run = ParameterOptimizationResult
				.parseFromRunResultFolder(
						repository,
						new File(
								"testCaseRepository/results/11_20_2012-12_45_04_all_vs_DS1")
								.getAbsoluteFile(), result, false, false, false);
		for (ParameterOptimizationResult r : result)
			Plotter.plotParameterOptimizationResult(r);
		System.out.println(result);
	}
}
