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
package de.clusteval.graphmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import utils.Pair;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
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

	protected QualitySet qualities;

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
		this.qualities = other.qualities;
	}

	protected void updateMap() {
		if (this.graph1ToGraph2 == null)
			this.graph1ToGraph2 = new HashMap<String, String>();
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

	public Set<String> getMatchingForGraphOneVertices() {
		return this.graph1ToGraph2.keySet();
	}

	public Collection<String> getMatchingForGraphTwoVertices() {
		return this.graph1ToGraph2.values();
	}

	public String getMatchingForGraph2Vertex(final String mappedV) {

		Set<Entry<String, String>> set = this.graph1ToGraph2.entrySet();

		for (Entry<String, String> entry : set) {
			if (entry.getValue() == mappedV) {
				return entry.getKey();
			}
		}
		return null;
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
	public static Pair<ParameterSet, GraphMatching> parseFromFile(
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

	/**
	 * @param qualitySet
	 *            Set the qualities of this clustering.
	 */
	public void setQualities(final QualitySet qualitySet) {
		this.qualities = qualitySet;
	}

	/**
	 * @return Returns the qualities of this clustering.
	 * @see GraphMatching#qualities
	 */
	public QualitySet getQualities() {
		return this.qualities;
	}
}
