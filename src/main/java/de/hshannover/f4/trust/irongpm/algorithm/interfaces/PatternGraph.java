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
package de.hshannover.f4.trust.irongpm.algorithm.interfaces;

import java.util.Map;

import org.jgrapht.Graph;

/**
 * Interface for a PatternGraph used in the Pattern Matching Engine / Algorithms
 * 
 * @author Leonard Renners
 * 
 */
public interface PatternGraph extends Graph<PatternVertex, PatternEdge> {

	/**
	 * Adds metadata to an existing vertex in the graph.
	 * 
	 * @param vertex
	 *            The vertex to add metadata to
	 * @param m
	 *            The metadata to add
	 * @return Whether the operation was successful
	 */
	public boolean addMetadataToVertex(PatternVertex vertex, PatternMetadata m);

	/**
	 * Adds a PatternEdge to the graph, with regard to IF-MAP understanding of equality.
	 * 
	 * @param toAdd
	 *            The edge to be added
	 * @return Whether the graph has changed or not (ergo the edge has been added/changed)
	 */
	public boolean addEdgeSensitive(PatternVertex v1, PatternVertex v2, PatternEdge toAdd);

	/**
	 * @return Whether the pattern is completely matched within the real graph - in particular whether an equivalent
	 *         node was found for each vertex and edge
	 */
	public boolean isCompletelyMatched();

	/**
	 * @return a deep copy of the object
	 */
	public PatternGraph copy();

	/**
	 * Returns whether all connected edges have an assigned matching component from the real graph
	 * 
	 * @param v
	 *            The vertex to check
	 * @return Whether all connected edges have already assigned matches
	 */
	public boolean allEdgesMatched(PatternVertex v);

	/**
	 * WARNING: returns a the actual relation table reference so one can alter the relations for this match
	 * 
	 * @return the relation table associated with the match so far
	 */
	public Map<String, String> getRelationTable();

	/**
	 * Sets a new relation table for the match
	 * 
	 * @param relationTable
	 *            the new relation table
	 */
	public void setRelationTable(Map<String, String> relationTable);

	/**
	 * Gets the vertex which is used for publishing metadata as the result of a rule. Returns a random vertex of the
	 * pattern if no vertex was set manually!
	 * 
	 * @return the vertex of which the matching vertex will be used in the publish process.
	 */
	public PatternVertex getPublishVertex();
}
