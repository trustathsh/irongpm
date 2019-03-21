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

import java.util.Map;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraph;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * Class encapsulating the result of a successful match of a rule and the graph.
 * 
 * @author Leonard Renners
 * 
 */
public class RuleMatch {

	private long mRuleId;
	private IfmapGraph mResultGraph;
	private IfmapVertex mPublishVertex;
	private Map<String, String> mNamedProperties;

	/**
	 * Constructor.
	 * 
	 * @param ruleId
	 *            id of the fired rule
	 * @param graph
	 *            ifmapGraph containing the matched elements
	 * @param publishVertex
	 *            vertex to publish information onto (if required by the rule)
	 * @param properties
	 *            hashmap of named properties, e.g. used for replacement in the recommendation
	 */
	public RuleMatch(long ruleId, IfmapGraph graph, IfmapVertex publishVertex, Map<String, String> properties) {
		this.mRuleId = ruleId;
		this.mResultGraph = graph;
		this.mPublishVertex = publishVertex;
		this.mNamedProperties = properties;
	}

	public long getRuleId() {
		return mRuleId;
	}

	public IfmapGraph getResultGraph() {
		return mResultGraph;
	}

	public IfmapVertex getPublishVertex() {
		return mPublishVertex;
	}

	public Map<String, String> getNamedProperties() {
		return mNamedProperties;
	}
	
	@Override
	public int hashCode() {
		return mResultGraph.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RuleMatch)) {
			return false;
		}
		RuleMatch other = (RuleMatch) obj;
		return mResultGraph.equals(other.getResultGraph());
	}
}
