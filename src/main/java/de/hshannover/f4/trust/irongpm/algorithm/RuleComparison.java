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
 * This file is part of irongpm, version 0.3.1,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 - 2016 Trust@HsH
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.util.ComparatorUtil;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraph;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapVertexImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;

/**
 * Object encapsulating the comparison of one rule on one specific pattern from one starting point.
 * 
 * @author Leonard Renners
 * 
 */
public class RuleComparison {

	private PatternRule mRule;
	private PatternGraph mPatternGraph;
	private IfmapGraph mRealGraph;
	private Set<RuleMatch> mResult;

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            The rule.
	 * @param realGraph
	 *            The real graph for the comparison.
	 */
	public RuleComparison(PatternRule rule, IfmapGraph realGraph) {
		mRule = rule;
		mPatternGraph = mRule.getPattern();
		mRealGraph = realGraph;
		mResult = new HashSet<>();
		compare();
	}

	/**
	 * Runs the matching of the patter.
	 * 
	 */
	public void compare() {

		// Empty Pattern - nothing to do
		if (mPatternGraph.vertexSet().isEmpty()) {
			return;
		}

		PatternVertex patternStart = mPatternGraph.vertexSet().iterator().next();

		// Randomly choose a starting point and start recursive algorithm for
		// each match in the real graph.
		for (IfmapVertex realStart : mRealGraph.vertexSet()) {

			HashMap<String, String> initalRelation = new HashMap<>();
			if (ComparatorUtil.compare(patternStart, realStart, initalRelation)) {
				PatternGraph fillingPattern = mPatternGraph.copy();
				fillingPattern.setRelationTable(initalRelation);
				PatternVertex fillingPatternStart = findVertexInGraph(fillingPattern, patternStart);
				fillingPatternStart.assignMatch(realStart);
				traversePattern(fillingPatternStart, realStart, fillingPattern);
			}
		}
	}

	private void traversePattern(PatternVertex currentPatternVertex, IfmapVertex currentRealVertex, PatternGraph pattern) {
		boolean incomplete = false;

		for (PatternEdge patternEdge : pattern.edgesOf(currentPatternVertex)) {
			if (!patternEdge.isMatched()) {
				incomplete = true;
				for (IfmapEdge realEdge : mRealGraph.edgesOf(currentRealVertex)) {
					if (ComparatorUtil.compare(patternEdge, realEdge, pattern.getRelationTable())) {
						// potential edge found
						PatternVertex patternTarget;
						IfmapVertex realTarget;
						if (mPatternGraph.getEdgeSource(patternEdge).equals(currentPatternVertex)) {
							patternTarget = mPatternGraph.getEdgeTarget(patternEdge);
						} else {
							patternTarget = mPatternGraph.getEdgeSource(patternEdge);
						}
						if (mRealGraph.getEdgeSource(realEdge).equals(currentRealVertex)) {
							realTarget = mRealGraph.getEdgeTarget(realEdge);
						} else {
							realTarget = mRealGraph.getEdgeSource(realEdge);
						}
						if (patternTarget.isMatched()) {
							// if target has already been visited - check if
							// target is equal
							if (patternTarget.getMatchedVertex().equals(realTarget)) {
								// Copy pattern and vertices and assign
								// matches
								// and traverse algorithm
								PatternGraph patternCopy = pattern.copy();
								PatternVertex currentVertexInCopy = findVertexInGraph(patternCopy, currentPatternVertex);
								PatternVertex targetVertexInCopy = findVertexInGraph(patternCopy, patternTarget);
								PatternEdge edgeCopy = null;
								for (PatternEdge e : patternCopy.getAllEdges(currentVertexInCopy, targetVertexInCopy)) {
									if (e.equals(patternEdge)) {
										edgeCopy = e;
										break;
									}
								}
								edgeCopy.assignMatch(realEdge);

								traversePattern(targetVertexInCopy, realTarget, patternCopy);
							}
						} else {
							// else check the equality of the edge target
							if (ComparatorUtil.compare(patternTarget, realTarget, pattern.getRelationTable())) {
								// Copy pattern and vertices and assign
								// matches
								// and traverse algorithm
								PatternGraph patternCopy = pattern.copy();
								PatternVertex currentVertexInCopy = findVertexInGraph(patternCopy, currentPatternVertex);
								PatternVertex targetVertexInCopy = findVertexInGraph(patternCopy, patternTarget);

								PatternEdge edgeCopy = null;
								for (PatternEdge e : patternCopy.getAllEdges(currentVertexInCopy, targetVertexInCopy)) {
									if (e.equals(patternEdge)) {
										edgeCopy = e;
										break;
									}
								}
								edgeCopy.assignMatch(realEdge);
								targetVertexInCopy.assignMatch(realTarget);
								traversePattern(targetVertexInCopy, realTarget, patternCopy);
							}
						}
					}
				}
				break;
			}
		}
		if (incomplete) {
			return;
		}
		if (pattern.isCompletelyMatched()) {
			mResult.add(convertAndPrepareResult(pattern));
			return;
		}
		for (PatternVertex patternVertex : pattern.vertexSet()) {
			if (patternVertex.isMatched() && !pattern.allEdgesMatched(patternVertex)
					&& !patternVertex.equals(currentPatternVertex)) {
				traversePattern(patternVertex, patternVertex.getMatchedVertex(), pattern);
			}
		}
		return;
	}

	private PatternVertex findVertexInGraph(PatternGraph graph, PatternVertex vertex) {
		for (PatternVertex v : graph.vertexSet()) {
			if (vertex.equalsWithMatch(v)) {
				return v;
			}
		}
		return null;
	}

	public Set<RuleMatch> getResult() {
		return mResult;
	}

	private RuleMatch convertAndPrepareResult(PatternGraph completedPattern) {
		IfmapGraphImpl result = new IfmapGraphImpl();
		result.setLastUpdated(mRealGraph.getLastUpdated());

		for (PatternVertex v : completedPattern.vertexSet()) {
			result.addVertex(stripMetadataFromVertex(v, v.getMatchedVertex(), completedPattern.getRelationTable()));
		}
		for (PatternEdge e : completedPattern.edgeSet()) {
			result.addEdgeSensitive(mRealGraph.getEdgeSource(e.getMatchedEdge()),
					mRealGraph.getEdgeTarget(e.getMatchedEdge()), e.getMatchedEdge());
		}
		return new RuleMatch(mRule.getId(), result, completedPattern.getPublishVertex().getMatchedVertex(),
				completedPattern.getRelationTable());
	}

	private IfmapVertex stripMetadataFromVertex(PatternVertex patternVertex, IfmapVertex vertex,
			Map<String, String> relationTable) {
		IfmapVertexImpl result = new IfmapVertexImpl(vertex);
		for (Metadata rm : vertex.getMetadata()) {
			for (PatternMetadata pm : patternVertex.getMetadata()) {
				if (ComparatorUtil.compare(pm, rm, relationTable)) {
					result.addMetadata(rm);
					break;
				}
			}
		}
		return result;
	}
}
