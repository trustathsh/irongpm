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
 * This file is part of irongpm, version 0.1.0,
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

import java.util.List;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * Interface for a vertex representing an IF-MAP Identifier
 * 
 * @author Leonard Renners
 * 
 */
public interface PatternVertex extends PatternPropable {
	/**
	 * @return The metadata attached to the identifier
	 */
	public List<PatternMetadata> getMetadata();

	/**
	 * 
	 * @param m
	 *            the metadata to add
	 * @return whether the operation was successful
	 */
	public boolean addMetadata(PatternMetadata m);

	/**
	 * 
	 * @return whether an equivalent edge has already been found in the real graph
	 */
	public boolean isMatched();

	/**
	 * Sets the matched vertex from the real graph. Also sets the boolean!
	 * 
	 * @param matchedVertex
	 *            The equivalent matching vertex from the real graph.
	 */
	public void assignMatch(IfmapVertex matchedVertex);

	/**
	 * 
	 * @return the equivalent edge found in the real graph (or null)
	 */
	public IfmapVertex getMatchedVertex();

	/**
	 * @return a deep copy of the object
	 */
	public PatternVertex copy();

	/**
	 * Compares not only the vertex itself but also the assigned matching real vertex
	 * 
	 * @param other
	 * @return Whether the vertex AND the matched vertex are equal!
	 */
	public boolean equalsWithMatch(Object other);
}
