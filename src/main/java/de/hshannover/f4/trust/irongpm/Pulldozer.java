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
package de.hshannover.f4.trust.irongpm;

import java.util.EventListener;

import org.apache.log4j.Logger;
import org.jgrapht.Graphs;
import org.jgrapht.event.GraphListener;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.listener.VertexStateListener;

/**
 * Class used to periodically pull for updates from the visitmeta dataservice and update the graph. Should be used
 * within a Thread.
 * 
 * @author Leonard Renners
 * 
 */
public class Pulldozer extends Thread {

	private static final Logger LOGGER = Logger.getLogger(Pulldozer.class);

	private boolean mIsDone = false;
	private long mInterval;
	private IfmapGraphImpl mGraph;

	/**
	 * Constructor.
	 */
	public Pulldozer() {
		mGraph = new IfmapGraphImpl();
		mIsDone = false;
		mInterval = IronGpm.getConfig().getInt("irongpm.updateinterval", 1000);
	}

	/**
	 * Adds a listener to the graph model.
	 * 
	 * @param l
	 *            The listener to add. So far mainly used for GraphListener and VertexStateListener
	 */
	@SuppressWarnings("unchecked")
	public void addListener(EventListener l) {
		if (l instanceof GraphListener) {
			mGraph.addGraphListener((GraphListener<IfmapVertex, IfmapEdge>) l);
		}
		if (l instanceof VertexStateListener) {
			mGraph.addVertexStateListener((VertexStateListener<IfmapVertex>) l);
		}
	}

	@Override
	public void run() {
		try {
			synchronized (this) {
				LOGGER.info("Establishing inital connection and getting initial graph");
				IfmapGraphImpl tmp = DataReciever.getInitialGraph();
				mGraph.setLastUpdated(tmp.getLastUpdated());
				Graphs.addGraph(mGraph, tmp);
				while (!mIsDone) {
					if (DataReciever.isUpdateAvailable(mGraph.getLastUpdated())) {
						while (DataReciever.isUpdateAvailable(mGraph.getLastUpdated())) {
							DataReciever.nextUpdate(mGraph);
						}
					}
					wait(mInterval);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Loop which updates the Graph has ended.");
	}

	public IfmapGraphImpl getGraph() {
		return mGraph;
	}

	/**
	 * Quits the while loop and stops pulling on the dataservice.
	 */
	public void finish() {
		mIsDone = true;
	}

	public void setInterval(long interval) {
		this.mInterval = interval;
	}
}
