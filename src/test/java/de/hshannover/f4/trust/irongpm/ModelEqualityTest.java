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
package de.hshannover.f4.trust.irongpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapEdgeImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapVertexImpl;
import de.hshannover.f4.trust.irongpm.ifmap.MetadataImpl;

/**
 * Test class for the equality between IF-MAP Edges, Vertices and Metadata
 *
 * @author Leonard Renners
 *
 */
public class ModelEqualityTest {

	/**
	 * Sets up the test environment.
	 */
	@Before
	public void setUp() {
	}

	/**
	 * Tests the equality of vertices
	 */
	@Test
	public void testVertices() {
		IfmapVertexImpl id1 = new IfmapVertexImpl("ip-address");
		IfmapVertexImpl id2 = new IfmapVertexImpl("ip-address");
		IfmapVertexImpl id3 = new IfmapVertexImpl("device");
		id1.addProperty("alpha", "eins");
		id1.addProperty("beta", "zwei");
		id2.addProperty("alpha", "eins");
		id2.addProperty("beta", "zwei");
		id3.addProperty("alpha", "eins");
		id3.addProperty("beta", "zwei");
		assertEquals(id1, id2);
		assertFalse(id1.equals(id3));
		assertFalse(id3.equals(id2));
	}

	/**
	 * Tests the equality of 2 singleValue metadata
	 */
	@Test
	public void testSingleValueMetadata() {
		MetadataImpl m1 = new MetadataImpl("event");
		MetadataImpl m2 = new MetadataImpl("event");
		assertEquals(m1, m2);
		m1.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");
		m2.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");
		assertEquals(m1, m2);
		m1.addProperty("alpha", "eins");
		m2.addProperty("alpha", "eins");
		assertEquals(m1, m2);
		m1.addProperty("beta", "zwei");
		assertFalse(m1.equals(m2));
		m2.addProperty("beta", "drei");
		assertFalse(m1.equals(m2));
		m2.addProperty("beta", "zwei");
		assertEquals(m1, m2);
	}

	/**
	 * Tests the equality of 2 mutliValue metadata
	 */
	@Test
	public void testMetadata() {
		MetadataImpl m1 = new MetadataImpl("event");
		MetadataImpl m2 = new MetadataImpl("event");
		assertEquals(m1, m2);
		m1.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		m2.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		assertEquals(m1, m2);
		m1.addProperty("alpha", "eins");
		m2.addProperty("alpha", "eins");
		assertEquals(m1, m2);
		m1.addProperty("beta", "zwei");
		assertFalse(m1.equals(m2));
		m2.addProperty("beta", "drei");
		assertFalse(m1.equals(m2));
		m2.addProperty("beta", "zwei");
		assertEquals(m1, m2);
	}

	/**
	 * Tests the equality of a singleValue and a multiValue metadata
	 */
	@Test
	public void testDifferentMetadata() {
		MetadataImpl m1 = new MetadataImpl("event");
		MetadataImpl m2 = new MetadataImpl("event");
		assertEquals(m1, m2);
		m1.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");
		m2.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		assertFalse(m1.equals(m2));
		m1.addProperty("alpha", "eins");
		m2.addProperty("alpha", "eins");
		assertFalse(m1.equals(m2));
		m1.addProperty("beta", "zwei");
		m2.addProperty("beta", "drei");
		assertFalse(m1.equals(m2));
	}

	/**
	 * Tests the equality of edges with attached singleValueMetadata.
	 */
	@Test
	public void testSingleValueEdge() {
		IfmapGraphImpl graph = new IfmapGraphImpl();
		IfmapVertexImpl ip = new IfmapVertexImpl("ip-address");
		IfmapVertexImpl device = new IfmapVertexImpl("device");
		graph.addVertex(ip);
		graph.addVertex(device);

		MetadataImpl metaDevIp = new MetadataImpl("device-ip");
		MetadataImpl metaDevIp2 = new MetadataImpl("device-ip");
		metaDevIp.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");
		metaDevIp.addProperty("alpha", "eins");
		metaDevIp2.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");
		metaDevIp2.addProperty("alpha", "eins");

		assertEquals(metaDevIp, metaDevIp2);
		IfmapEdgeImpl devIp = new IfmapEdgeImpl(device, ip, metaDevIp);
		assertTrue(graph.addEdgeSensitive(device, ip, devIp));
		IfmapEdgeImpl devIp2 = new IfmapEdgeImpl(device, ip, metaDevIp2);
		assertFalse(graph.addEdgeSensitive(device, ip, devIp2));
		assertEquals(1, graph.edgeSet().size());
		assertFalse(graph.addEdgeSensitive(device, ip, devIp2));
		assertEquals(1, graph.edgeSet().size());

		assertTrue(devIp.equals(devIp2));
		assertTrue(devIp.equalsNonIfmap(devIp2));
		metaDevIp.addProperty("beta", "zwei");
		metaDevIp2.addProperty("beta", "drei");
		assertTrue(devIp.equals(devIp2));
		assertFalse(devIp.equalsNonIfmap(devIp2));
		assertTrue(graph.addEdgeSensitive(device, ip, devIp2));
		assertEquals(1, graph.edgeSet().size());
	}

	/**
	 * Tests the equality of edges with attached singleValueMetadata.
	 */
	@Test
	public void testMultiValueEdge() {
		IfmapGraphImpl graph = new IfmapGraphImpl();
		IfmapVertexImpl ip = new IfmapVertexImpl("ip-address");
		IfmapVertexImpl mac = new IfmapVertexImpl("mac-address");
		graph.addVertex(ip);
		graph.addVertex(mac);

		MetadataImpl metaIpMac = new MetadataImpl("ip-mac");
		metaIpMac.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		metaIpMac.addProperty("alpha", "eins");
		metaIpMac.addProperty("beta", "zwei");
		MetadataImpl metaIpMac2 = new MetadataImpl("ip-mac");
		metaIpMac2.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		metaIpMac2.addProperty("alpha", "eins");
		metaIpMac2.addProperty("beta", "zwei");

		IfmapEdgeImpl macIp = new IfmapEdgeImpl(ip, mac, metaIpMac);
		assertTrue(graph.addEdgeSensitive(ip, mac, macIp));
		assertEquals(1, graph.edgeSet().size());

		IfmapEdgeImpl macIp2 = new IfmapEdgeImpl(ip, mac, metaIpMac2);
		assertFalse(graph.addEdgeSensitive(ip, mac, macIp2));
		assertEquals(1, graph.edgeSet().size());

		metaIpMac2.addProperty("beta", "drei");
		assertTrue(graph.addEdgeSensitive(ip, mac, macIp2));
		assertEquals(2, graph.edgeSet().size());

		assertFalse(macIp.equalsNonIfmap(macIp2));
	}

	/**
	 * Tests the equality of edges with attached singleValueMetadata.
	 */
	@Test
	public void testFullGraphs() {
		IfmapGraphImpl graph = new IfmapGraphImpl();
		IfmapGraphImpl graph2 = new IfmapGraphImpl();
		// TODO Here!

	}

	/**
	 * Tears down the test environment.
	 */
	@After
	public void tearDown() {
		IfmapVertexImpl id1 = new IfmapVertexImpl("ip-address", new HashMap<String, String>(), null);
		IfmapVertexImpl id2 = new IfmapVertexImpl("ip-address", new HashMap<String, String>(), null);
		id1.addProperty("alpha", "eins");
		id1.addProperty("beta", "zwei");
		id2.addProperty("alpha", "eins");
		id2.addProperty("beta", "zwei");
		assertEquals(id1, id2);
	}
}
