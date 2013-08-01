/**
 * 
 */
package de.clusteval.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;


import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.run.ClusteringRun;
import de.clusteval.run.Run;
import de.clusteval.utils.TestRepositoryObject;

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
}
