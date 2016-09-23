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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jgrapht.event.GraphChangeEvent;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import com.google.gson.GsonBuilder;

import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.util.ComparatorUtil;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.listener.GraphVertexModifyEvent;
import de.hshannover.f4.trust.irongpm.listener.VertexStateListener;
import de.hshannover.f4.trust.irongpm.rest.IfmapGraphJsonAdapter;

/**
 * Simple Matching algorithm - will be triggered whenever a change in the graph occurs, but does not work on the change,
 * but look for the whole graph structure.
 * 
 * @author Leonard Renners
 * 
 */
public class BasicMatchingAlgorithm implements GraphListener<IfmapVertex, IfmapEdge>, VertexStateListener<IfmapVertex> {

	private static final Logger LOGGER = Logger.getLogger(BasicMatchingAlgorithm.class);

	private static HashMap<Long, ArrayList<Integer>> mFiredPatterns = new HashMap<>();
	// TODO: Check if Hash calculation on a graph is correct!

	private static ArrayList<PatternRule> mRulePatterns = new ArrayList<>();

	private static final Executor EXEC = Executors.newCachedThreadPool();

	private static GsonBuilder gsob = new GsonBuilder();

	/**
	 * Constructor.
	 */
	public BasicMatchingAlgorithm() {
		super();
		gsob.registerTypeAdapter(IfmapGraphImpl.class, new IfmapGraphJsonAdapter());
	}

	/**
	 * Adds a rule to the patterns to compare on a change in the graph structure.
	 * 
	 * @param rule
	 *            the new rule
	 */
	public void addRule(PatternRule rule) {
		mRulePatterns.add(rule);
		mFiredPatterns.put(rule.getId(), new ArrayList<Integer>());
	}

	/**
	 * Checks if an RuleId is already being taken
	 * 
	 * @param id
	 *            the ID of the rule to check
	 * @return Wether the ruleId is already in use.
	 */
	public boolean hasRuleId(long id) {
		return mFiredPatterns.containsKey(id);
	}

	/**
	 * Checks wehter rules the rules are affected by the incoming event and fires all rules whcih might be affected,
	 * i.e. which might be fulfilled since the event is part of the pattern.
	 * 
	 * @param event
	 */
	public void checkAndFireRules(final GraphChangeEvent event) {
		final IfmapGraphImpl graphCopy = new IfmapGraphImpl((IfmapGraphImpl) event.getSource());
		for (final PatternRule r : mRulePatterns) {
			Runnable doIt = new Runnable() {
				@Override
				public void run() {
					if (isRuleAffected(r, event)) {
						LOGGER.debug("Rule " + r.getId() + " affected - checking.");
						matchRule(graphCopy, r);
					}
				}
			};
			EXEC.execute(doIt);
		}
	}

	private void matchRule(IfmapGraphImpl graph, PatternRule rule) {
		Set<RuleMatch> resultSet = new RuleComparison(rule, graph).getResult();
		for (RuleMatch r : resultSet) {
			int matchedHash = r.getResultGraph().hashCode();
			synchronized (this) {
				List<Integer> firedHashes = mFiredPatterns.get(r.getRuleId());
				if (!firedHashes.contains(matchedHash)) {
					mFiredPatterns.get(r.getRuleId()).add(matchedHash);
					LOGGER.debug("Rule " + rule.getId() + " fired - performing actions...");
					for (RuleAction action : rule.getActions()) {
						action.performAction(rule, r);
					}
				}
			}
		}
	}

	@Override
	public void vertexAdded(GraphVertexChangeEvent<IfmapVertex> e) {
		// Can be ignored - is covered by edgeAdded, since a vertex cannot be
		// added in IF-MAP without an edge!
		// checkAndFireRules(e);
	}

	@Override
	public void vertexRemoved(GraphVertexChangeEvent<IfmapVertex> e) {
		// Can be ignored - a pattern cannot be fulfilled by a vertex removal
		// (no NOT relationships supported)
		// checkAndFireRules(e);
	}

	@Override
	public void edgeAdded(GraphEdgeChangeEvent<IfmapVertex, IfmapEdge> e) {
		checkAndFireRules(e);
	}

	@Override
	public void edgeRemoved(GraphEdgeChangeEvent<IfmapVertex, IfmapEdge> e) {
		// Can be ignored - a pattern cannot be fulfilled by a vertex removal
		// (no NOT relationships supported)
		// checkAndFireRules(e);
	}

	@Override
	public void vertexChanged(GraphVertexModifyEvent<IfmapVertex> e) {
		checkAndFireRules(e);
	}

	@SuppressWarnings("unchecked")
	private boolean isRuleAffected(PatternRule rule, GraphChangeEvent e) {
		if (e instanceof GraphVertexChangeEvent<?>) {
			GraphVertexChangeEvent<IfmapVertex> event = (GraphVertexChangeEvent<IfmapVertex>) e;
			IfmapVertex vertex = event.getVertex();
			for (PatternVertex patternVertex : rule.getPattern().vertexSet()) {
				if (ComparatorUtil.compare(patternVertex, vertex)) {
					return true;
				}
			}
			return false;
		}
		if (e instanceof GraphEdgeChangeEvent<?, ?>) {
			GraphEdgeChangeEvent<IfmapVertex, IfmapEdge> event = (GraphEdgeChangeEvent<IfmapVertex, IfmapEdge>) e;
			IfmapEdge edge = event.getEdge();
			for (PatternEdge patternEdge : rule.getPattern().edgeSet()) {
				if (ComparatorUtil.compare(patternEdge, edge)) {
					return true;
				}
			}
			return false;
		}
		// Unknown Event type
		LOGGER.warn("Unknown Graph Change Event. No rules triggered!");
		return false;
	}
}
