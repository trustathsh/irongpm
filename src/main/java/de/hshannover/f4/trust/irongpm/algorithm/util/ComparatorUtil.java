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
package de.hshannover.f4.trust.irongpm.algorithm.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternPropable;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.ifmap.ExtendedIdentifierEncapsulation;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Propable;

/**
 * Pattern-Matching utility class for the comparison of pattern objects and real graph objects.
 * 
 * @author Leonard Renners
 * 
 */
public final class ComparatorUtil {

	private ComparatorUtil() {
		// so secure!
	}

	/**
	 * Compares a real metadata object from an IF-MAP Graph against a pattern metadata. The Pattern-Classes are used to
	 * model MUST or MUST NOT conditions on elements within the graph and the comparator methods take these properties
	 * into account.
	 * 
	 * @param pattern
	 *            The metadata from the pattern graph
	 * @param real
	 *            The metadata from the real graph to compare
	 * @param relationTable
	 *            a table with associated values for "wildcard" attributes
	 * @return Whether the metadata from the real graph "matches" the pattern metadata
	 */
	public static boolean compareProp(PatternPropable pattern, Propable real, Map<String, String> relationTable) {
		if (pattern == null) {
			return false;
		}
		if (real == null) {
			return false;
		}
		if (!pattern.getTypeName().equals(real.getTypeName())) {
			return false;
		}
		List<String> patternProperties = pattern.getProperties();
		for (String patternProperty : patternProperties) {
			boolean isRestriction = pattern.isPropertyRestricted(patternProperty);
			boolean isRelationalProperty = pattern.isPropertyRelated(patternProperty);
			String realValue = real.valueFor(patternProperty);
			String patternValue;
			if (isRelationalProperty) {
				String relationKey = pattern.valueFor(patternProperty);
				if (relationTable.containsKey(relationKey)) {
					patternValue = relationTable.get(relationKey);
				} else {
					patternValue = realValue;
					relationTable.put(relationKey, patternValue);
				}
			} else {
				patternValue = pattern.valueFor(patternProperty);
			}
			if (isRestriction) {
				if (patternValue == null) {
					if (realValue == null) {
						return false;
					}
					return true;
				}
				if (patternValue.equals(realValue)) {
					return false;
				}
			} else {
				if (patternValue == null) {
					if (realValue != null) {
						return false;
					}
					return true;
				}
				if (!patternValue.equals(realValue)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compares a real and a pattern metadata object using the relation table with already stored variables.
	 * 
	 * @param pm
	 *            The pattern metadata object
	 * @param rm
	 *            The real metadata object
	 * @param relationTable
	 *            Stored (assigned) variables/placeholders
	 * @return The comparison results
	 * 
	 */
	public static boolean compare(PatternMetadata pm, Metadata rm, Map<String, String> relationTable) {
		return compareProp(pm, rm, relationTable);
	}

	/**
	 * Compares a real and a pattern vertex object using the relation table with already stored variables.
	 * 
	 * @param pv
	 *            The pattern vertex
	 * @param rv
	 *            The real vertex
	 * @return The comparison results (with a new and emtpy relation Table)
	 */
	public static boolean compare(PatternVertex pv, IfmapVertex rv) {
		return compare(pv, rv, new HashMap<String, String>());
	}

	/**
	 * Compares a real and a pattern edge object using the relation table with already stored variables.
	 * 
	 * @param patternEdge
	 *            The pattern edge
	 * @param realEdge
	 *            The real edge
	 * @return The comparison results (with a new and emtpy relation Table)
	 */
	public static boolean compare(PatternEdge patternEdge, IfmapEdge realEdge) {
		return compare(patternEdge, realEdge, new HashMap<String, String>());
	}

	/**
	 * Compares a pattern vertex with a real vertex (if-map)
	 * 
	 * @param pv
	 *            the pattern vertex
	 * @param rv
	 *            the real vertex
	 * @param relationTable
	 *            Stored (assigned) variables/placeholders
	 * @return whether the two vertices are equal
	 */
	public static boolean compare(PatternVertex pv, IfmapVertex rv, Map<String, String> relationTable) {
		if (rv.isExtendedIdentifier()) {
			rv = new ExtendedIdentifierEncapsulation(rv);
		}
		if (!compareProp(pv, rv, relationTable)) {
			return false;
		}
		for (PatternMetadata pm : pv.getMetadata()) {
			boolean found = false;
			for (Metadata rm : rv.getMetadata()) {
				if (ComparatorUtil.compare(pm, rm, relationTable)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compares a real edge object from an IF-MAP Graph against a pattern edge. The Pattern-Classes are used to model
	 * MUST or MUST NOT conditions on elements within the graph and the comparator methods take these properties into
	 * account.
	 * 
	 * @param patternEdge
	 *            The edge from the pattern graph
	 * @param realEdge
	 *            The edge from the real graph to compare
	 * @param mRelationTable
	 *            a table with associated values for "wildcard" attributes
	 * @return Whether the edge from the real graph "matches" the pattern edge
	 */
	public static boolean compare(PatternEdge patternEdge, IfmapEdge realEdge, Map<String, String> mRelationTable) {
		if (patternEdge == null) {
			return false;
		}
		if (realEdge == null) {
			return false;
		}
		return compare(patternEdge.getMetadata(), realEdge.getMetadata(), mRelationTable);
	}
}
