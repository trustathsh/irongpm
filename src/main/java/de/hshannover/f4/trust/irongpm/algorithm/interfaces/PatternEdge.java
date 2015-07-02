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
package de.hshannover.f4.trust.irongpm.algorithm.interfaces;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;

/**
 * Interface representing an edge between two {@link PatternVertex}
 *
 * @author Leonard Renners
 *
 */
public interface PatternEdge {
	/**
	 * @return The metadata attached to the edge
	 */
	public PatternMetadata getMetadata();

	/**
	 * Equals method using the IF-MAP understanding of edge/metadata equality. This means that two edges of the same
	 * type, between the same vertices (identifiers) are always equal if they are of singleValue cardinality, and never
	 * equal for mutliValue. This method should be used for edge insertion in the graph, but not for graph comparison.
	 *
	 * @param e
	 *            the edge to compare with
	 * @return whether the edge is equal with regard to the if-map understanding of equality
	 */
	public boolean equalsIfmap(Object e);

	/**
	 *
	 * @return whether an equivalent edge has already been found in the real graph
	 */
	public boolean isMatched();

	/**
	 *
	 * @return the equivalent edge found in the real graph (or null)
	 */
	public IfmapEdge getMatchedEdge();

	/**
	 * Sets the matched edge from the real graph. Also sets the boolean!
	 *
	 * @param matchedEdge
	 *            The equivalent matching edge from the real graph.
	 */
	public void assignMatch(IfmapEdge matchedEdge);

	/**
	 * Sets the matching status of an edge. Needed for restriction edges to set them matched without assigning a
	 * concrete edge.
	 *
	 * @param matched
	 *            wether the edge has successfully been matched or not
	 */
	void setMatched(boolean matched);

}
