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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import de.clusteval.graphmatching.GraphMatching;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Rashid Ibragimov
 * 
 */
public class AligmentGraph extends DirectedSparseMultigraph<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3973085473213625766L;

	private DirectedSparseMultigraph<String, String> graphOne, graphTwo;
	private ArrayList<Set<String>> connectedComponents;
	private Set<String> inducedEdgesOne, inducedEdgesTwo;
	private GraphMatching matching;

	public AligmentGraph(GraphMatching graphMatching,
			DirectedSparseMultigraph<String, String> g1,
			DirectedSparseMultigraph<String, String> g2) {
		super();

		this.graphOne = g1;
		this.graphTwo = g2;
		this.matching = graphMatching;

		this.inducedEdgesOne = new HashSet<String>();
		this.inducedEdgesTwo = new HashSet<String>();

		for (String e : g1.getEdges()) {
			String s = g1.getSource(e);
			String t = g1.getDest(e);

			String sMapped = matching.getMatchingForGraph1Vertex(s);
			String tMapped = matching.getMatchingForGraph1Vertex(t);

			if ((sMapped != null) && (tMapped != null)) {
				inducedEdgesOne.add(e);
			}

			if (g2.findEdge(sMapped, tMapped) != null) {

				String ss = "[" + s + ";" + sMapped + "]";
				String tt = "[" + t + ";" + tMapped + "]";

				this.addVertex(ss);
				this.addVertex(tt);

				if (this.findEdge(ss, tt) == null) {
					String sstt = "(" + ss + ";" + tt + ")";
					String ttss = "(" + tt + ";" + ss + ")";

					this.addEdge(sstt, ss, tt);
					this.addEdge(ttss, tt, ss);
				}

			}
		} // end of for

		for (String e : g2.getEdges()) {
			String sMapped = g1.getSource(e);
			String tMapped = g1.getDest(e);

			String s = matching.getMatchingForGraph2Vertex(sMapped);
			String t = matching.getMatchingForGraph2Vertex(tMapped);

			if ((s != null) && (t != null)) {
				inducedEdgesTwo.add(e);
			}

		} // end of for

		WeakComponentClusterer<String, String> clust = new WeakComponentClusterer<String, String>();
		Set<Set<String>> ccs = clust.transform(this);

		this.connectedComponents = new ArrayList<Set<String>>();

		for (Set<String> vertexSet : ccs) {
			this.connectedComponents.add(vertexSet);
		}

		Collections.sort(this.connectedComponents,
				new Comparator<Set<String>>() {

					public int compare(Set<String> o1, Set<String> o2) {
						Integer i1 = o1.size();
						Integer i2 = o2.size();
						return (i1 > i2 ? -1 : (i1 == i2 ? 0 : 1));
					}
				});
	} // end of constructor

	private int getAmountOfEdgesInConnectedComponent(int index) {
		Set<String> edges = new HashSet<String>();

		for (String v : connectedComponents.get(index)) {
			edges.addAll(this.getNeighbors(v));
		}
		return edges.size();
	}

	public int getAmountOfEdgesInLCC() {
		return getAmountOfEdgesInConnectedComponent(0);
	}

	public int getAmountOfVerticesInLCC() {
		return connectedComponents.get(0).size();
	}

	public int getAmountOfEdgesIn5PercentsofLCCs() {
		int n = 0;

		for (int i = 0; i < (int) (0.05 * connectedComponents.size()); i++) {
			n += getAmountOfEdgesInConnectedComponent(i);
		}

		return n;
	}

	public int getAmountOfVerticesIn5PercentsofLCCs() {
		int n = 0;
		for (int i = 0; i < Math.max((int) (0.05 * connectedComponents.size()),
				1); i++) {
			n += connectedComponents.get(i).size();
		}

		return n;
	}

	public int getAmountOfAlignedEdges() {
		return this.getEdgeCount();
	}

	public double getEdgeCorrectness() {
		return this.getEdgeCount()
				/ (double) Math.min(graphOne.getEdgeCount(),
						graphOne.getEdgeCount());
	}

	public double getInducedConservedStructureMapping() {
		return this.getEdgeCount() / (inducedEdgesTwo.size() / 2.0);
	}

	public double getInducedConservedStructureImage() {
		return this.getEdgeCount() / (inducedEdgesOne.size() / 2.0);
	}

	public int getCompactness(int l) {

		int properDistance;
		int imageDistance;
		int compactnessDist = 0;

		UnweightedShortestPath<String, String> pathsLengthsOne = new UnweightedShortestPath<String, String>(
				graphOne);
		UnweightedShortestPath<String, String> pathsLengthsTwo = new UnweightedShortestPath<String, String>(
				graphTwo);

		for (String v : matching.getMatchingForGraphOneVertices()) {

			KNeighborhoodFilter<String, String> kneighborhoodFilterOne = new KNeighborhoodFilter<String, String>(
					v, l, KNeighborhoodFilter.EdgeType.IN_OUT);
			Graph<String, String> nlv = kneighborhoodFilterOne
					.transform(graphOne);

			for (String u : nlv.getVertices()) {
				if (matching.getMatchingForGraph1Vertex(u) != null) {
					Number distOne = pathsLengthsOne.getDistance(v, u);
					Number distTwo = pathsLengthsTwo.getDistance(
							matching.getMatchingForGraph1Vertex(v),
							matching.getMatchingForGraph1Vertex(u));

					if (distTwo != null) {
						properDistance = distOne.intValue();
						imageDistance = distTwo.intValue();
						compactnessDist += Math.max(imageDistance
								- properDistance, 0);
					}
				}
			} // end of for by l-neighbor u of v

		} // end of for by v

		return compactnessDist;
	}

	public int getCompactnessPreservance(int l, int d) {

		int properDistance;
		int imageDistance;
		int compactnessPreservanceCounter = 0;

		UnweightedShortestPath<String, String> pathsLengthsOne = new UnweightedShortestPath<String, String>(
				graphOne);
		UnweightedShortestPath<String, String> pathsLengthsTwo = new UnweightedShortestPath<String, String>(
				graphTwo);

		for (String v : matching.getMatchingForGraphOneVertices()) {

			KNeighborhoodFilter<String, String> kneighborhoodFilterOne = new KNeighborhoodFilter<String, String>(
					v, l, KNeighborhoodFilter.EdgeType.IN_OUT);
			Graph<String, String> nlv = kneighborhoodFilterOne
					.transform(graphOne);

			for (String u : nlv.getVertices()) {
				if (matching.getMatchingForGraph1Vertex(u) != null) {
					Number distOne = pathsLengthsOne.getDistance(v, u);
					Number distTwo = pathsLengthsTwo.getDistance(
							matching.getMatchingForGraph1Vertex(v),
							matching.getMatchingForGraph1Vertex(u));

					if (distTwo != null) {
						properDistance = distOne.intValue();
						imageDistance = distTwo.intValue();

						if (imageDistance - properDistance <= d) {
							compactnessPreservanceCounter++;
						}
					}
				}
			} // end of for by l-neighbor u of v

		} // end of for by v

		return compactnessPreservanceCounter;
	}

	public int getNeighborhoodPreservance(int l, int d) {

		int neighborsCounter = 0;

		// UnweightedShortestPath<String, String> pathsLengthsOne = new
		// UnweightedShortestPath<String, String>(graphOne);
		UnweightedShortestPath<String, String> pathsLengthsTwo = new UnweightedShortestPath<String, String>(
				graphTwo);

		for (String v : matching.getMatchingForGraphOneVertices()) {

			KNeighborhoodFilter<String, String> kneighborhoodFilterOne = new KNeighborhoodFilter<String, String>(
					v, l, KNeighborhoodFilter.EdgeType.IN_OUT);
			Graph<String, String> nlv = kneighborhoodFilterOne
					.transform(graphOne);

			for (String u : nlv.getVertices()) {

				if (matching.getMatchingForGraph1Vertex(u) != null) {

					Number distTwo = pathsLengthsTwo.getDistance(
							matching.getMatchingForGraph1Vertex(v),
							matching.getMatchingForGraph1Vertex(u));

					if (distTwo != null) {
						if (distTwo.intValue() <= d) {
							neighborsCounter++;
						}
					}
				}
			} // end of for by l-neighbor u of v

		} // end of for by v

		return neighborsCounter;
	}

}
