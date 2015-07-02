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
package de.hshannover.f4.trust.irongpm.listener;

/**
 * Interface to add a Listener functionality to react on changes within a vertex - instead of addition and removal of
 * vertices and edges.
 *
 * @author Leonard Renners
 *
 * @param <V>
 *            The class of the vertex.
 */
public interface VertexStateListenable<V> {
	/**
	 * Removes the specified graph listener from this graph, if present.
	 *
	 * @param l
	 *            the listener to be removed
	 */
	public void addVertexStateListener(VertexStateListener<V> l);

	/**
	 * Removes the specified vertex set listener from this graph, if present.
	 *
	 * @param l
	 *            the listener to be removed
	 */
	public void removeVertexStateListener(VertexStateListener<V> l);

	/**
	 * Fire a change event and notifies all listener if the state of a vertex has changed.
	 *
	 * @param vertex
	 *            The vertex, which has changed
	 */
	public void fireVertexChanged(V vertex);
}
