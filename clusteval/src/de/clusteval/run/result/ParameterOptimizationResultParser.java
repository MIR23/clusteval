/**
 * 
 */
package de.clusteval.run.result;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.clusteval.paramOptimization.ParameterOptimizationMethod;
import de.clusteval.graphmatching.Clustering;

import utils.StringExt;
import utils.parse.TextFileParser;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramParameter;
import de.clusteval.quality.ClusteringQualityMeasure;
import de.clusteval.quality.ClusteringQualityMeasureValue;
import de.clusteval.quality.ClusteringQualitySet;
import de.clusteval.run.ParameterOptimizationRun;

/**
 * @author Christian Wiwie
 * 
 */
public class ParameterOptimizationResultParser extends TextFileParser {

	protected List<ProgramParameter<?>> parameters = new ArrayList<ProgramParameter<?>>();
	protected List<ClusteringQualityMeasure> qualityMeasures = new ArrayList<ClusteringQualityMeasure>();
	protected ParameterOptimizationMethod method;
	protected ParameterOptimizationRun run;
	protected ParameterOptimizationResult tmpResult;
	protected boolean parseClusterings, storeClusterings;

	/**
	 * @param method
	 * @param run
	 * @param tmpResult
	 * @param absFilePath
	 * @param keyColumnIds
	 * @param valueColumnIds
	 * @param parseClusterings
	 * @param storeClusterings
	 * @throws IOException
	 */
	public ParameterOptimizationResultParser(
			final ParameterOptimizationMethod method,
			final ParameterOptimizationRun run,
			final ParameterOptimizationResult tmpResult,
			final String absFilePath, int[] keyColumnIds, int[] valueColumnIds,
			final boolean parseClusterings, final boolean storeClusterings)
			throws IOException {
		super(absFilePath, keyColumnIds, valueColumnIds);
		this.setLockTargetFile(true);
		this.method = method;
		this.run = run;
		this.tmpResult = tmpResult;
		this.parseClusterings = parseClusterings;
		this.storeClusterings = storeClusterings;
	}

	@SuppressWarnings("unused")
	@Override
	protected void processLine(String[] key, String[] value) {
		if (this.currentLine == 0) {
			/*
			 * Parse header line
			 */
			// 04.04.2013: added iteration number into first column
			String[] paramSplit = StringExt.split(value[1], ",");
			for (String p : paramSplit)
				parameters
						.add(method.getProgramConfig().getParameterForName(p));
			for (int i = 2; i < value.length; i++) {
				String q = value[i];
				for (ClusteringQualityMeasure other : run.getQualityMeasures())
					if (other.getClass().getSimpleName().equals(q)) {
						qualityMeasures.add(other);
						break;
					}
			}
		} else {
			try {
				long iterationNumber = Long.valueOf(value[0]);
				ParameterSet paramSet = new ParameterSet();
				String[] paramSplit = StringExt.split(value[1], ",");
				for (int pos = 0; pos < paramSplit.length; pos++) {
					ProgramParameter<?> p = this.parameters.get(pos);
					paramSet.put(p.getName(), Double.valueOf(paramSplit[pos]));
				}

				ClusteringQualitySet qualitySet = new ClusteringQualitySet();

				// changed 03.04.2013 this does not necessarily work,
				// because line number not always corresponds to iteration
				// number.
				// added 14.03.2013
				// ensure, that the iteration result file containing the
				// clustering exists
				String iterationId = iterationNumber + "";
				String clusteringFilePath = this.getAbsoluteFilePath().replace(
						"results.qual.complete", iterationId + ".results.conv");
				File absFile = new File(clusteringFilePath).getAbsoluteFile();
				// if the corresponding file exists take the qualities for
				// granted
				// if (absFile.exists()) {
				for (int pos = 2; pos < value.length; pos++) {
					ClusteringQualityMeasure other = this.qualityMeasures
							.get(pos - 2);
					qualitySet.put(other, ClusteringQualityMeasureValue
							.parseFromString(value[pos]));
				}
				// }
				// if the file does not exist, put NT quality values
				// else {
				// for (int pos = 1; pos < value.length; pos++) {
				// ClusteringQualityMeasure other = this.qualityMeasures
				// .get(pos - 1);
				// qualitySet.put(other, ClusteringQualityMeasureValue
				// .getForNotTerminated());
				// }
				// }
				tmpResult.parameterSets.add(paramSet);
				tmpResult.iterationNumbers.add(iterationNumber);

				// added 20.08.2012
				if (parseClusterings) {
					// if (absFile.exists()) {
					try {
						Clustering clustering = Clustering.parseFromFile(
								method.getRepository(), absFile, false)
								.getSecond();
						if (storeClusterings)
							tmpResult.put(iterationNumber, paramSet,
									qualitySet, clustering);
						else {
							// in this case, the clustering is only
							// registerd in the repository and therefore
							// added to the database
							tmpResult.put(iterationNumber, paramSet,
									qualitySet, null);
						}
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// }
				tmpResult.put(iterationNumber, paramSet, qualitySet);
			} catch (Exception e) {
			}
		}
	}
}
