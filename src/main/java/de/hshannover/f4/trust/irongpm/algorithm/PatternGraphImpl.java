/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irongpm, version 0.0.1,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 - 2015 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.irongpm.algorithm;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Multigraph;

import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;

/**
 * Main datastructure class encapsulating a full graph using the JgraphT Listenable graph structure in combination with
 * the IF-MAP datatypes.
 * 
 * @author Leonard Renners
 * 
 */
public class PatternGraphImpl extends DefaultListenableGraph<PatternVertex, PatternEdge> implements PatternGraph {

	/**
	 *
	 */
	private static final long serialVersionUID = -3104953215717098366L;

	private HashMap<String, String> mRelationTable;

	private PatternVertex mPublishVertex = null;

	/**
	 * Constructor.
	 */
	public PatternGraphImpl() {
		super(new Multigraph<PatternVertex, PatternEdge>(PatternEdge.class));
		mRelationTable = new HashMap<>();
	}

	@Override
	public boolean addMetadataToVertex(PatternVertex vertex, PatternMetadata m) {
		if (!containsVertex(vertex)) {
			return false;
		}
		for (PatternVertex v : vertexSet()) {
			if (v.equals(vertex)) {
				if (v.addMetadata(m)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes an IfmapEdgeImpl from the graph, with regard to IF-MAP understanding of equality.
	 * 
	 * @param toRemove
	 *            The edge to be removed
	 * @return Whether the graph has changed or not (ergo the edge has been removed)
	 */
	public boolean removeEdgeSensitive(PatternEdge toRemove) {
		if (toRemove.getMetadata().isSingleValue()) {
			for (PatternEdge e : edgeSet()) {
				if (toRemove.equalsIfmap(e)) {
					return removeEdge(e);
				}
			}
		}
		return removeEdge(toRemove);
	}

	@Override
	public boolean addEdgeSensitive(PatternVertex v1, PatternVertex v2, PatternEdge toAdd) {
		if (!toAdd.getMetadata().isSingleValue()) {
			return addEdge(v1, v2, toAdd);
		} else {
			for (PatternEdge e : edgeSet()) {
				if (toAdd.equalsIfmap(e)) {
					if (!e.equals(toAdd)) {
						removeEdgeSensitive(e);
						return addEdge(v1, v2, toAdd);
					}
				}
			}
		}
		return addEdge(v1, v2, toAdd);
	}

	@Override
	public boolean isCompletelyMatched() {
		for (PatternVertex v : vertexSet()) {
			if (!v.isMatched()) {
				return false;
			}
		}
		for (PatternEdge e : edgeSet()) {
			if (!e.isMatched()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public PatternGraph copy() {
		PatternGraphImpl copy = new PatternGraphImpl();
		for (String rel : getRelationTable().keySet()) {
			copy.getRelationTable().put(rel, getRelationTable().get(rel));
		}
		for (PatternVertex v : this.vertexSet()) {
			PatternVertex copiedVertex = v.copy();
			copy.addVertex(copiedVertex);
			if (v.equals(this.getPublishVertex())) {
				copy.setPublishVertex(copy.findVertexInGraph(v));
			}
		}
		for (PatternEdge e : this.edgeSet()) {
			PatternVertex v1 = copy.findVertexInGraph(getEdgeSource(e));
			PatternVertex v2 = copy.findVertexInGraph(getEdgeTarget(e));
			PatternEdge newEdge = new BasicPatternEdge(v1, v2, e.getMetadata());
			newEdge.assignMatch(e.getMatchedEdge());
			newEdge.setMatched(e.isMatched());
			copy.addEdgeSensitive(v1, v2, newEdge);
		}
		return copy;
	}

	@Override
	public boolean allEdgesMatched(PatternVertex v) {
		for (PatternEdge e : this.edgesOf(v)) {
			if (!e.isMatched()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ss
	 * 
	 * @param vertex
	 * @return s
	 */
	public PatternVertex findVertexInGraph(PatternVertex vertex) {
		for (PatternVertex v : this.vertexSet()) {
			if (vertex.equalsWithMatch(v)) {
				return v;
			}
		}
		return null;
	}

	@Override
	public Map<String, String> getRelationTable() {
		return mRelationTable;
	}

	@Override
	public void setRelationTable(Map<String, String> relationTable) {
		mRelationTable = new HashMap<>(relationTable);
	}

	@Override
	public PatternVertex getPublishVertex() {
		if (mPublishVertex == null) {
			if (!vertexSet().isEmpty()) {
				return vertexSet().iterator().next();
			}
		}
		return mPublishVertex;
	}

	/**
	 * Sets the vertex which is used for publishing metadata as the result of a rule.
	 * 
	 * @param vertex
	 *            the vertex of which the matching vertex will be used in the publish process
	 */
	public void setPublishVertex(PatternVertex vertex) {
		mPublishVertex = vertex;
	}
}
