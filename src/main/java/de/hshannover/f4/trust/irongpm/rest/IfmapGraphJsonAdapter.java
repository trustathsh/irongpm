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
package de.hshannover.f4.trust.irongpm.rest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapEdgeImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapVertexImpl;
import de.hshannover.f4.trust.irongpm.ifmap.MetadataImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;

/**
 * Adapter class to (de-)serialize and transform between JSON and the JGraphT datamodel using Google gson.
 *
 * @author Leonard Renners
 *
 */
public class IfmapGraphJsonAdapter implements JsonDeserializer<IfmapGraphImpl>, JsonSerializer<IfmapGraphImpl> {

	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_TYPENAME = "typename";
	public static final String KEY_PROPERTIES = "properties";
	public static final String KEY_IDENTIFIERS = "identifiers";
	public static final String KEY_METADATA = "metadata";
	public static final String KEY_LINKS = "links";
	public static final String KEY_UPDATES = "updates";
	public static final String KEY_DELETES = "deletes";
	public static final String KEY_RAWDATA = "rawData";

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	@Override
	public JsonElement serialize(IfmapGraphImpl src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject jsonGraph = new JsonObject();
		jsonGraph.add(KEY_TIMESTAMP, GSON.toJsonTree(src.getLastUpdated()));

		Set<IfmapVertex> visitedNodes = new HashSet<>();
		JsonArray jsonLinks = new JsonArray();
		for (IfmapVertex i : src.vertexSet()) {
			if (!visitedNodes.contains(i)) {
				Set<IfmapVertex> seenNodes = new HashSet<>();
				for (IfmapEdge l : src.edgesOf(i)) {
					IfmapVertex other = i.equals(src.getEdgeSource(l)) ? src.getEdgeTarget(l) : src.getEdgeSource(l);
					if (!seenNodes.contains(other) && !visitedNodes.contains(other)) {
						Set<IfmapEdge> links = src.getAllEdges(i, other);
						JsonObject jsonLink = toJson(links, i, other);
						jsonLinks.add(jsonLink);
						seenNodes.add(other);
					}
				}
			}
			if (i.getMetadata().size() > 0) {
				JsonObject identifierWithMetadata = toJson(i, i.getMetadata());
				jsonLinks.add(identifierWithMetadata);
			}
			visitedNodes.add(i);
		}
		jsonGraph.add(KEY_LINKS, jsonLinks);
		return jsonGraph;
	}

	/**
	 * @param identifier
	 * @param metadata
	 * @return A JSON Object representng the metadata connected to an identifier
	 */
	public JsonObject toJson(IfmapVertex identifier, List<Metadata> metadata) {
		JsonObject identifierWithMetadata = new JsonObject();

		identifierWithMetadata.add(KEY_IDENTIFIERS, toJson(identifier));
		JsonElement jsonMetadata = null;
		if (metadata.size() > 1) {
			jsonMetadata = new JsonArray();
			for (Metadata m : metadata) {
				((JsonArray) jsonMetadata).add(toJson(m));
			}
		} else {
			jsonMetadata = toJson(metadata.get(0));
		}
		identifierWithMetadata.add(KEY_METADATA, jsonMetadata);
		return identifierWithMetadata;
	}

	/**
	 * @param links
	 * @param source
	 * @param target
	 * @return A JSON Object representing all edges between the source and the edge node
	 */
	public JsonObject toJson(Set<IfmapEdge> links, IfmapVertex source, IfmapVertex target) {
		List<Metadata> allMetadata = new ArrayList<>();
		for (IfmapEdge l : links) {
			allMetadata.add(l.getMetadata());
		}

		JsonObject jsonLink = new JsonObject();

		JsonArray jsonIdentifiers = new JsonArray();
		jsonIdentifiers.add(toJson(source));
		jsonIdentifiers.add(toJson(target));
		jsonLink.add(KEY_IDENTIFIERS, jsonIdentifiers);

		JsonElement jsonMetadata = null;
		if (allMetadata.size() > 1) {
			jsonMetadata = new JsonArray();
			for (Metadata m : allMetadata) {
				((JsonArray) jsonMetadata).add(toJson(m));
			}
		} else {
			jsonMetadata = toJson(allMetadata.get(0));
		}
		jsonLink.add(KEY_METADATA, jsonMetadata);

		return jsonLink;

	}

	/**
	 * @param identifier
	 * @return A JSON object representing the identifier
	 */
	public JsonObject toJson(IfmapVertex identifier) {
		JsonObject jsonIdentifier = new JsonObject();
		jsonIdentifier.add(KEY_TYPENAME, GSON.toJsonTree(identifier.getTypeName()));

		JsonObject jsonProperties = new JsonObject();
		for (String property : identifier.getProperties()) {
			jsonProperties.add(property, GSON.toJsonTree(identifier.valueFor(property)));
		}
		jsonIdentifier.add(KEY_PROPERTIES, jsonProperties);
		jsonIdentifier.add(KEY_RAWDATA, GSON.toJsonTree(identifier.getRawData()));

		return jsonIdentifier;
	}

	/**
	 * @param metadata
	 * @return A JSON object representing the metadata
	 */
	public JsonObject toJson(Metadata metadata) {
		JsonObject jsonMetadata = new JsonObject();
		jsonMetadata.add(KEY_TYPENAME, GSON.toJsonTree(metadata.getTypeName()));

		JsonObject jsonProperties = new JsonObject();
		for (String property : metadata.getProperties()) {
			jsonProperties.add(property, GSON.toJsonTree(metadata.valueFor(property)));
		}
		jsonMetadata.add(KEY_PROPERTIES, jsonProperties);
		jsonMetadata.add(KEY_RAWDATA, GSON.toJsonTree(metadata.getRawData()));
		return jsonMetadata;
	}

	@Override
	public IfmapGraphImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		IfmapGraphImpl graph = new IfmapGraphImpl();
		TypeToken<HashMap<String, JsonElement>> hashMapType = new TypeToken<HashMap<String, JsonElement>>() {
		};
		HashMap<String, JsonElement> jsonGraph = GSON.fromJson(json, hashMapType.getType());
		graph.setLastUpdated(jsonGraph.get("timestamp").getAsLong());
		JsonArray jsonLinkList = jsonGraph.get("links").getAsJsonArray();

		// Iterate over all link items, i.e. entries of identifier(s) and their
		// metadata
		for (JsonElement jsonLink : jsonLinkList) {
			HashMap<String, JsonElement> jsonItem = GSON.fromJson(jsonLink, hashMapType.getType());

			// Either we have two identifiers with a link that contains metadata
			if (jsonItem.get("identifiers").isJsonArray()) {
				JsonArray jsonIdentifierList = jsonItem.get("identifiers").getAsJsonArray();
				IfmapVertex identifierOne = ifmapVertexFromJson(jsonIdentifierList.get(0));
				IfmapVertex identifierTwo = ifmapVertexFromJson(jsonIdentifierList.get(1));
				IfmapVertex existingIdentifierOne = findVertex(identifierOne, graph);
				IfmapVertex existingIdentifierTwo = findVertex(identifierTwo, graph);
				if (existingIdentifierOne == null) {
					graph.addVertex(identifierOne);
				} else {
					identifierOne = existingIdentifierOne;
				}
				if (existingIdentifierTwo == null) {
					graph.addVertex(identifierTwo);
				} else {
					identifierTwo = existingIdentifierTwo;
				}

				if (jsonItem.get("metadata") != null) {
					// More than 1 metadatum
					if (jsonItem.get("metadata").isJsonArray()) {
						JsonArray jsonMetadataList = jsonItem.get("metadata").getAsJsonArray();
						for (JsonElement jsonMetadata : jsonMetadataList) {
							Metadata meta = metadataFromJson(jsonMetadata);
							IfmapEdgeImpl edge = new IfmapEdgeImpl(identifierOne, identifierTwo, meta);
							graph.addEdgeSensitive(identifierOne, identifierTwo, edge);
						}
						// Exactly one connected metadatum
					} else {
						Metadata meta = metadataFromJson(jsonItem.get("metadata"));
						IfmapEdgeImpl edge = new IfmapEdgeImpl(identifierOne, identifierTwo, meta);
						edge.setMetadata(meta);
						graph.addEdgeSensitive(identifierOne, identifierTwo, edge);
					}
				}

				// ... or we have a single identifier with metadata attached to
			} else {
				IfmapVertex identifier = ifmapVertexFromJson(jsonItem.get("identifiers"));
				IfmapVertex existingIdentifier = findVertex(identifier, graph);
				if (existingIdentifier == null) {
					graph.addVertex(identifier);
				} else {
					identifier = existingIdentifier;
				}

				if (jsonItem.get("metadata") != null) {
					// More than 1 metadatum
					if (jsonItem.get("metadata").isJsonArray()) {
						JsonArray jsonMetadataList = jsonItem.get("metadata").getAsJsonArray();
						for (JsonElement jsonMetadata : jsonMetadataList) {
							Metadata meta = metadataFromJson(jsonMetadata);
							graph.addMetadataToVertex(identifier, meta);
							// identifier.addMetadata(meta);
						}
						// Exactly one connected metadatum
					} else {
						Metadata meta = metadataFromJson(jsonItem.get("metadata"));
						graph.addMetadataToVertex(identifier, meta);
						// identifier.addMetadata(meta);
					}
				}
				// ... or we have broken data

			}
		}
		return graph;
	}

	private Metadata metadataFromJson(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JsonBindingMetadata metaJson = gson.fromJson(jsonElement, JsonBindingMetadata.class);
		return new MetadataImpl(metaJson.typename, metaJson.properties, metaJson.rawData);
	}

	private IfmapVertex ifmapVertexFromJson(JsonElement jsonElement) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		JsonBindingIdentifier identifierJson = gson.fromJson(jsonElement, JsonBindingIdentifier.class);
		return new IfmapVertexImpl(identifierJson.typename, identifierJson.properties, identifierJson.rawData);
	}

	private IfmapVertex findVertex(IfmapVertex other, IfmapGraphImpl graph) {
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
