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
package de.hshannover.f4.trust.irongpm.listener;

import org.jgrapht.event.GraphVertexChangeEvent;

/**
 * An event signaling, that an attribute of a vertex has changed.
 *
 * @author Leonard Renners
 *
 * @param <V>
 *            The class of the vertex.
 */
public class GraphVertexModifyEvent<V> extends GraphVertexChangeEvent<V> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8049281893294491856L;

	/**
	 * Vertex state changed event. This event is fired after a vertex modified.
	 */
	public static final int VERTEX_MODIFIED = 21;

	/**
	 * Creates a new GraphVertexModifyEvent object.
	 *
	 * @param eventSource
	 *            the source of the event.
	 * @param type
	 *            the type of the event.
	 * @param vertex
	 *            the vertex that the event is related to.
	 */
	public GraphVertexModifyEvent(Object eventSource, int type, V vertex) {
		super(eventSource, type, vertex);
	}

}
