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
package de.hshannover.f4.trust.irongpm.listener;

import org.apache.log4j.Logger;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * Dummy listener implementation. Simply logging all events.
 *
 * @author Leonard Renners
 *
 */
public class LoggingListener implements GraphListener<IfmapVertex, IfmapEdge>, VertexStateListener<IfmapVertex> {

	private static final Logger LOGGER = Logger.getLogger(LoggingListener.class);

	/**
	 * Constructor.
	 */
	public LoggingListener() {
		super();
	}

	@Override
	public void vertexAdded(GraphVertexChangeEvent<IfmapVertex> e) {
		LOGGER.info("va: " + e.getVertex());

	}

	@Override
	public void vertexRemoved(GraphVertexChangeEvent<IfmapVertex> e) {
		LOGGER.info("vr: " + e.getVertex());
	}

	@Override
	public void edgeAdded(GraphEdgeChangeEvent<IfmapVertex, IfmapEdge> e) {
		LOGGER.info("ea: " + e.getEdge());
	}

	@Override
	public void edgeRemoved(GraphEdgeChangeEvent<IfmapVertex, IfmapEdge> e) {
		LOGGER.info("er: " + e.getEdge());
	}

	@Override
	public void vertexChanged(GraphVertexModifyEvent<IfmapVertex> e) {
		LOGGER.info("vm: " + e.getVertex());
	}

}
