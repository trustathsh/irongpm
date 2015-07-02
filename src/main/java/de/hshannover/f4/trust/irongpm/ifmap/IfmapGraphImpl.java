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
package de.hshannover.f4.trust.irongpm.ifmap;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Multigraph;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;
import de.hshannover.f4.trust.irongpm.listener.GraphVertexModifyEvent;
import de.hshannover.f4.trust.irongpm.listener.VertexStateListenable;
import de.hshannover.f4.trust.irongpm.listener.VertexStateListener;

/**
 * Main datastructure class encapsulating a full graph using the JgraphT Listenable graph structure in combination with
 * the IF-MAP datatypes.
 * 
 * @author Leonard Renners
 * 
 */
public class IfmapGraphImpl extends DefaultListenableGraph<IfmapVertex, IfmapEdge> implements IfmapGraph,
		VertexStateListenable<IfmapVertex> {

	/**
	 *
	 */
	private static final long serialVersionUID = -3104953215717098366L;
	private Long mLastUpdated;
	private List<VertexStateListener<IfmapVertex>> mVertexStateListeners = new ArrayList<>();

	/**
	 * Constructor.
	 */
	public IfmapGraphImpl() {
		super(new Multigraph<IfmapVertex, IfmapEdge>(IfmapEdge.class));
	}

	/**
	 * Copy constructor. Creates a new instance on the basis of an existing.
	 * 
	 * @param oldGraph
	 *            The old graph
	 */
	public IfmapGraphImpl(IfmapGraphImpl oldGraph) {
		super(new Multigraph<IfmapVertex, IfmapEdge>(oldGraph.getEdgeFactory()));
		setLastUpdated(oldGraph.getLastUpdated());
		for (IfmapVertex v : oldGraph.vertexSet()) {
			addVertex(v);
		}
		for (IfmapEdge e : oldGraph.edgeSet()) {
			addEdge(oldGraph.getEdgeSource(e), oldGraph.getEdgeTarget(e), e);
		}
	}

	public void setLastUpdated(Long timestamp) {
		mLastUpdated = timestamp;
	}

	@Override
	public Long getLastUpdated() {
		return mLastUpdated;
	}

	@Override
	public boolean addMetadataToVertex(IfmapVertex vertex, Metadata m) {
		if (!containsVertex(vertex)) {
			return false;
		}
		for (IfmapVertex v : vertexSet()) {
			if (v.equals(vertex)) {
				if (((IfmapVertexImpl) v).addMetadata(m)) {
					fireVertexChanged(v);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean removeMetadataFromVertex(IfmapVertex vertex, Metadata m) {
		if (!containsVertex(vertex)) {
			return false;
		}
		for (IfmapVertex v : vertexSet()) {
			if (v.equals(vertex)) {
				if (((IfmapVertexImpl) v).removeMetadata(m)) {
					fireVertexChanged(v);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes metadata from an existing vertex in the graph.
	 * 
	 * @param vertex
	 *            The vertex to remove metadata from
	 * @param meta
	 *            The metadata to remove
	 * @return Whether the operation was successful
	 */
	public boolean removeMetadataFromVertex(IfmapVertex vertex, List<Metadata> meta) {
		boolean changed = false;
		if (!containsVertex(vertex)) {
			return false;
		}
		for (Metadata m : meta) {
			if (removeMetadataFromVertex(vertex, m)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void addVertexStateListener(VertexStateListener<IfmapVertex> l) {
		mVertexStateListeners.add(l);
	}

	@Override
	public void removeVertexStateListener(VertexStateListener<IfmapVertex> l) {
		mVertexStateListeners.remove(l);
	}

	@Override
	public void fireVertexChanged(IfmapVertex vertex) {
		for (VertexStateListener<IfmapVertex> l : mVertexStateListeners) {
			l.vertexChanged(new GraphVertexModifyEvent<IfmapVertex>(this, GraphVertexModifyEvent.VERTEX_MODIFIED,
					vertex));
		}
	}

	@Override
	public boolean removeEdgeSensitive(IfmapEdge toRemove) {
		if (!(toRemove instanceof IfmapEdgeImpl)) {
			return false;
		}
		IfmapEdgeImpl rem = (IfmapEdgeImpl) toRemove;
		if (rem.getMetadata().isSingleValue()) {
			for (IfmapEdge e : edgeSet()) {
				if (rem.equals(e)) {
					return removeEdge(e);
				}
			}
		}
		return removeEdge(toRemove);
	}

	@Override
	public boolean addEdgeSensitive(IfmapVertex v1, IfmapVertex v2, IfmapEdge toAdd) {
		if (!(toAdd instanceof IfmapEdgeImpl)) {
			return false;
		}
		IfmapEdgeImpl add = (IfmapEdgeImpl) toAdd;
		if (!add.getMetadata().isSingleValue()) {
			return addEdge(v1, v2, toAdd);
		} else {
			for (IfmapEdge e : edgeSet()) {
				if (add.equals(e)) {
					if (!e.equalsNonIfmap(add)) {
						removeEdgeSensitive(e);
						return addEdge(v1, v2, toAdd);
					}
				}
			}
		}
		return addEdge(v1, v2, toAdd);
	}

	@Override
	public int hashCode() {
		if (edgeSet().isEmpty()) {
			return vertexSet().hashCode();
		} else {
			int result = 1;
			for (IfmapEdge e : edgeSet()) {
				result += e.hashCodeNonIfmap();
			}
			return result;
		}
	}

}
