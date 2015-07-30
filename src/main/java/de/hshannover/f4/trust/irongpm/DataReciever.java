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
package de.hshannover.f4.trust.irongpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgrapht.Graphs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;
import de.hshannover.f4.trust.irongpm.rest.IfmapGraphJsonAdapter;
import de.hshannover.f4.trust.irongpm.rest.VisitmetaResource;

/**
 * 
 * Class to manage the rest connection to the visitmeta service and request graph information.
 * 
 * @author Leonard Renners
 * 
 */
public final class DataReciever {

	private static VisitmetaResource visitmeta;
	private static GsonBuilder gsob = new GsonBuilder().disableHtmlEscaping();
	private static Gson gson;
	private static JsonParser parser = new JsonParser();
	private static boolean isInitialized = false;
	private static final Logger LOGGER = Logger.getLogger(DataReciever.class);

	private static final String RESTPOSTFIX = "graph";

	/**
	 * Cause security.
	 */
	private DataReciever() {
	}

	/**
	 * Initializes the shared components.
	 */
	public static void init() {
		String restUri = IronGpm.getConfig().getString("dataservice.url", "http://localhost:8000") + "/"
				+ IronGpm.getConfig().getString("dataservice.connection", "localMAPServer") + "/" + RESTPOSTFIX + "/";
		visitmeta = new VisitmetaResource(restUri, IronGpm.getConfig().getBoolean("dataservice.rawxml", true));
		gsob.registerTypeAdapter(IfmapGraphImpl.class, new IfmapGraphJsonAdapter());
		gson = gsob.create();
		isInitialized = true;
	}

	/**
	 * Gets the current graph from the visitmeta dataservice.
	 * 
	 * @return The graph of the newest timestamp.
	 */
	public static IfmapGraphImpl getCurrentGraph() {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		String json = visitmeta.get("current");
		JsonArray array = parser.parse(json).getAsJsonArray();
		IfmapGraphImpl graph = new IfmapGraphImpl();
		for (JsonElement elem : array) {
			IfmapGraphImpl graphPart = gson.fromJson(elem, IfmapGraphImpl.class);
			graph.setLastUpdated(graphPart.getLastUpdated());
			Graphs.addGraph(graph, graphPart);
		}
		LOGGER.debug("Recieved current graph: " + graph);
		return graph;
	}

	/**
	 * @param timestamp
	 * @return Whether a newer version of the graph (newer than the provided timestamp) is available
	 */
	public static boolean isUpdateAvailable(Long timestamp) {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		String json = visitmeta.get("changes");
		JsonObject object = parser.parse(json).getAsJsonObject();

		@SuppressWarnings("unchecked")
		Map<String, String> changesMap = gson.fromJson(object, Map.class);
		if (changesMap.isEmpty()) {
			return false;
		}
		List<String> changeTimestamps = new ArrayList<String>(changesMap.keySet());
		Collections.sort(changeTimestamps);
		int index = changeTimestamps.indexOf(timestamp.toString());
		return index < changeTimestamps.size() - 1;
	}

	/**
	 * @param timestamp
	 * @return The timestamp of the next newer version of the graph
	 */
	public static Long getNextTimestamp(Long timestamp) {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		String json = visitmeta.get("changes");
		JsonObject object = parser.parse(json).getAsJsonObject();

		@SuppressWarnings("unchecked")
		Map<String, String> changesMap = gson.fromJson(object, Map.class);
		List<String> changeTimestamps = new ArrayList<String>(changesMap.keySet());
		Collections.sort(changeTimestamps);

		int index = changeTimestamps.indexOf(timestamp.toString());
		if (index == -1) {
			return Long.valueOf(changeTimestamps.get(0));
		}
		if (isUpdateAvailable(timestamp)) {
			return Long.valueOf(changeTimestamps.get(index + 1));
		} else {
			return timestamp;
		}
	}

	/**
	 * @return The initial graph from the visitmeta dataservice
	 */
	public static IfmapGraphImpl getInitialGraph() {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		String json = visitmeta.get("initial");
		JsonArray array = parser.parse(json).getAsJsonArray();
		IfmapGraphImpl graph = new IfmapGraphImpl();
		graph.setLastUpdated((long) 1);
		for (JsonElement elem : array) {
			IfmapGraphImpl graphPart = gson.fromJson(elem, IfmapGraphImpl.class);
			graph.setLastUpdated(graphPart.getLastUpdated());
			for (IfmapVertex v : graphPart.vertexSet()) {
				if (!graph.containsVertex(v)) {
					graph.addVertex(v);
				}
			}
			for (IfmapEdge edge : graphPart.edgeSet()) {
				if (!graph.containsEdge(edge)) {
					IfmapVertex source = graphPart.getEdgeSource(edge);
					IfmapVertex target = graphPart.getEdgeTarget(edge);
					graph.addEdgeSensitive(source, target, edge);
				}
			}
		}
		LOGGER.debug("Recieved initial graph: " + graph);
		return graph;
	}

	/**
	 * Updates the graph to the next newer version.
	 * 
	 * @param oldGraph
	 *            The graph to update
	 * @return The updated graph (it is the oldGraph object!!)
	 */
	public static synchronized boolean nextUpdate(IfmapGraphImpl oldGraph) {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		Long nextTimestamp = getNextTimestamp(oldGraph.getLastUpdated());
		return updateGraph(oldGraph, nextTimestamp);
	}

	/**
	 * Updates the graph (stepwise) to the version at the given timestamp.
	 * 
	 * @param oldGraph
	 *            The graph to update
	 * @param timestamp
	 *            The timestamp of the desired version
	 * @return The updated graph (it is the oldGraph object!!)
	 */
	public static synchronized boolean updateGraph(IfmapGraphImpl oldGraph, Long timestamp) {
		if (!isInitialized) {
			LOGGER.warn("DataReciever was not initialized properly. Call init() first! Trying to initilaize now.");
			init();
		}
		if (oldGraph.getLastUpdated().equals(timestamp)) {
			return false;
		}

		String json = visitmeta.get(oldGraph.getLastUpdated() + "/" + timestamp);

		JsonObject obj = parser.parse(json).getAsJsonObject();
		JsonArray updates = (JsonArray) obj.get("updates");
		JsonArray deletes = (JsonArray) obj.get("deletes");

		oldGraph.setLastUpdated(timestamp);
		for (JsonElement elem : deletes) {
			IfmapGraphImpl graphPart = gson.fromJson(elem, IfmapGraphImpl.class);
			for (IfmapEdge e : graphPart.edgeSet()) {
				IfmapVertex source = graphPart.getEdgeSource(e);
				IfmapVertex target = graphPart.getEdgeTarget(e);
				for (IfmapVertex v : oldGraph.vertexSet()) {
					if (source.equals(v)) {
						oldGraph.removeMetadataFromVertex(v, source.getMetadata());
					}
					if (target.equals(v)) {
						oldGraph.removeMetadataFromVertex(v, source.getMetadata());
					}
				}
				oldGraph.removeEdgeSensitive(e);
				if (oldGraph.containsVertex(source)) {
					if (oldGraph.edgesOf(source).isEmpty() && source.getMetadata().isEmpty()) {
						oldGraph.removeVertex(source);
					}
				}
				if (oldGraph.containsVertex(target)) {
					if (oldGraph.edgesOf(target).isEmpty() && target.getMetadata().isEmpty()) {
						oldGraph.removeVertex(target);
					}
				}
			}
			for (IfmapVertex v : graphPart.vertexSet()) {
				for (IfmapVertex orig : oldGraph.vertexSet()) {
					if (orig.equals(v)) {
						oldGraph.removeMetadataFromVertex(orig, v.getMetadata());
						if (oldGraph.edgesOf(orig).isEmpty() && orig.getMetadata().isEmpty()) {
							oldGraph.removeVertex(orig);
						}
					}
				}
			}
		}
		for (JsonElement elem : updates) {
			IfmapGraphImpl graphPart = gson.fromJson(elem, IfmapGraphImpl.class);
			for (IfmapVertex v : graphPart.vertexSet()) {
				if (!oldGraph.containsVertex(v)) {
					oldGraph.addVertex(v);
				} else {
					IfmapVertex existingVertex = findVertex(v, oldGraph);
					for (Metadata m : v.getMetadata()) {
						if (!existingVertex.getMetadata().contains(m)) {
							oldGraph.addMetadataToVertex(v, m);
						}
					}
				}
			}
			for (IfmapEdge edge : graphPart.edgeSet()) {
				if (!oldGraph.containsEdge(edge)) {
					IfmapVertex source = graphPart.getEdgeSource(edge);
					IfmapVertex target = graphPart.getEdgeTarget(edge);
					oldGraph.addEdgeSensitive(source, target, edge);
				}
			}
		}
		return true;
	}

	/**
	 * Finds the equivalent vertex in the graph
	 * 
	 * @param other
	 *            The vertex to compare with
	 * @param graph
	 *            The graph in which to search
	 * @return The corresponding (equal) vertex within the graph - or null
	 */
	private static IfmapVertex findVertex(IfmapVertex other, IfmapGraphImpl graph) {
		if (!graph.containsVertex(other)) {
			return null;
		}
		for (IfmapVertex v : graph.vertexSet()) {
			if (v.equals(other)) {
				return v;
			}
		}
		return null;
	}

}
