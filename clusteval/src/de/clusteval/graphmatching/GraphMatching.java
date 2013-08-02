/**
 * 
 */
package de.clusteval.graphmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import utils.Pair;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualityMeasureValue;
import de.clusteval.quality.QualitySet;

/**
 * @author Christian Wiwie
 * 
 */
public class GraphMatching implements Iterable<Pair<String, String>> {

	protected static List<Pair<String, String>> cloneMappings(
			List<Pair<String, String>> mappings) {
		final List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();

		for (Pair<String, String> p : mappings)
			result.add(Pair.getPair(p.getFirst(), p.getSecond()));

		return result;
	}

	protected List<Pair<String, String>> mappings;

	protected Map<String, String> graph1ToGraph2;

	/**
	 * 
	 */
	public GraphMatching() {
		super();
		this.mappings = new ArrayList<Pair<String, String>>();

		this.graph1ToGraph2 = new HashMap<String, String>();
	}

	/**
	 * The copy constructor of clusterings.
	 * 
	 * @param other
	 *            The object to clone.
	 */
	public GraphMatching(final GraphMatching other) {
		super();
		this.mappings = cloneMappings(other.mappings);
		updateMap();
	}

	protected void updateMap() {
		for (Pair<String, String> p : mappings)
			this.graph1ToGraph2.put(p.getFirst(), p.getSecond());
	}

	@Override
	public GraphMatching clone() {
		return new GraphMatching(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GraphMatching))
			return false;
		GraphMatching other = (GraphMatching) obj;
		return
		// this.graph1.equals(other.graph1)
		// && this.graph2.equals(other.graph2)
		// &&
		this.mappings.equals(other.mappings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (// this.graph1.toString() + this.graph2.toString() +
		this.mappings.toString()).hashCode();
	}

	public String getMatchingForGraph1Vertex(final String v) {
		return this.graph1ToGraph2.get(v);
	}

	public void addMatching(final Pair<String, String> pair) {
		this.mappings.add(pair);
		this.graph1ToGraph2.put(pair.getFirst(), pair.getSecond());
	}

	public int size() {
		return this.mappings.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Pair<String, String>> iterator() {
		return this.mappings.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Graph Matching: " + mappings.toString() + "]";
	}

	public String toFormattedString() {
		StringBuilder sb = new StringBuilder();
		for (Pair<String, String> mapping : this.mappings) {
			sb.append(mapping.getFirst());
			sb.append(",");
			sb.append(mapping.getSecond());
			sb.append(";");
		}
		return sb.toString();
	}

	/**
	 * This method parses clusterings together with the corresponding parameter
	 * sets from a file.
	 * 
	 * @param repository
	 * 
	 * @param absFilePath
	 *            The absolute path to the input file.
	 * @param parseQualities
	 *            True, if the qualities of the clusterings should also be
	 *            parsed. Those will be taken from .qual-files.
	 * @return A map containing parameter sets and corresponding clusterings.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Pair<Map<String, Double>, GraphMatching> parseFromFile(
			final Repository repository, final File absFilePath,
			final boolean parseQualities) throws IOException {
		GraphMatchingParser parser = new GraphMatchingParser(repository,
				absFilePath.getAbsolutePath(), parseQualities);
		parser.process();

		return parser.getMatchings();
	}

	/**
	 * Assess quality.
	 * 
	 * @param dataConfig
	 * 
	 * @param qualityMeasures
	 *            the quality measures
	 * @return A set of qualities for every quality measure that was passed in
	 *         the list.
	 * @throws UnknownGoldStandardFormatException
	 *             the unknown gold standard format exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 */
	public QualitySet assessQuality(final DataConfig dataConfig,
			final List<QualityMeasure> qualityMeasures)
			throws UnknownGoldStandardFormatException, IOException,
			UnknownDataSetFormatException, InvalidDataSetFormatVersionException {
		final QualitySet resultSet = new QualitySet();
		for (QualityMeasure qualityMeasure : qualityMeasures) {
			// do not calculate, when there is no goldstandard
			if (qualityMeasure.requiresGoldstandard()
					&& !dataConfig.hasGoldStandardConfig())
				continue;
			QualityMeasureValue quality;
			try {
				GraphMatching goldStandard = null;
				if (dataConfig.hasGoldStandardConfig())
					goldStandard = dataConfig.getGoldstandardConfig()
							.getGoldstandard().getClustering();
				quality = qualityMeasure.getQualityOf(this, goldStandard,
						dataConfig);
				if (dataConfig.hasGoldStandardConfig())
					dataConfig.getGoldstandardConfig().getGoldstandard()
							.unloadFromMemory();
				// we rethrow some exceptions, since they mean, that we
				// cannot calculate ANY quality measures for this data
			} catch (UnknownGoldStandardFormatException e) {
				throw e;
			} catch (IOException e) {
				throw e;
			} catch (UnknownDataSetFormatException e) {
				throw e;
			} catch (InvalidDataSetFormatVersionException e) {
				throw e;
			} catch (Exception e) {
				// all the remaining exceptions are catched, because they
				// mean, that the quality measure calculation is flawed
				quality = QualityMeasureValue.getForDouble(Double.NaN);
			}
			resultSet.put(qualityMeasure, quality);
		}
		return resultSet;
	}
}
