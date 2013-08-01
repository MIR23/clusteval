/**
 * 
 */
package de.clusteval.quality;

import java.io.File;
import java.io.IOException;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.graphmatching.GraphMatching;
import de.clusteval.utils.RNotAvailableException;

/**
 * This type of clustering quality measure uses the R framework to calculate
 * cluster validities.
 * 
 * @author Christian Wiwie
 * 
 */
public abstract class ClusteringQualityMeasureR
		extends
			QualityMeasure {

	/**
	 * Instantiates a new R clustering quality measure.
	 * 
	 * @param repo
	 * @param register
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public ClusteringQualityMeasureR(final Repository repo,
			final boolean register, final long changeDate, final File absPath)
			throws RegisterException {
		super(repo, false, changeDate, absPath);

		if (register)
			this.register();
	}

	/**
	 * The copy constructor of R clustering quality measures.
	 * 
	 * @param other
	 *            The quality measure to clone.
	 * @throws RegisterException
	 */
	public ClusteringQualityMeasureR(final ClusteringQualityMeasureR other)
			throws RegisterException {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.cluster.quality.ClusteringQualityMeasure#getQualityOfClustering
	 * (de.clusteval.cluster.Clustering, de.clusteval.cluster.Clustering,
	 * de.clusteval.data.DataConfig)
	 */
	@Override
	public final QualityMeasureValue getQualityOf(
			GraphMatching clustering, GraphMatching goldStandard,
			DataConfig dataConfig) throws UnknownGoldStandardFormatException,
			UnknownDataSetFormatException, IOException,
			InvalidDataSetFormatVersionException, RNotAvailableException {
		try {
			MyRengine rEngine = new MyRengine("");
			try {
				try {
					return getQualityOfClusteringHelper(clustering,
							goldStandard, dataConfig, rEngine);
				} catch (REXPMismatchException e) {
					// handle this type of exception as an REngineException
					throw new REngineException(rEngine, e.getMessage());
				}
			} catch (REngineException e) {
				this.log.warn("R-framework (" + this.getClass().getSimpleName()
						+ "): " + rEngine.getLastError());
				return QualityMeasureValue.getForDouble(this
						.getMinimum());
			} finally {
				rEngine.close();
			}
		} catch (RserveException e) {
			throw new RNotAvailableException(e.getMessage());
		}
	}

	protected abstract QualityMeasureValue getQualityOfClusteringHelper(
			GraphMatching clustering, GraphMatching goldStandard,
			DataConfig dataConfig, final MyRengine rEngine)
			throws UnknownGoldStandardFormatException,
			UnknownDataSetFormatException, IOException,
			InvalidDataSetFormatVersionException, REngineException,
			IllegalArgumentException, REXPMismatchException;
}
