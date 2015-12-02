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
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.PatternGraphImpl;
import de.hshannover.f4.trust.irongpm.algorithm.RuleComparison;
import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.util.ComparatorUtil;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapEdgeImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraph;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapVertexImpl;
import de.hshannover.f4.trust.irongpm.ifmap.MetadataImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * TESTs
 * 
 * @author Leonard Renners
 * 
 */
public class PatternComparisonTest {

	private IfmapGraph realGraph;
	private IfmapVertexImpl ip1;
	private IfmapVertexImpl dev1;
	private IfmapVertexImpl dev2;
	private IfmapVertexImpl dev3;
	private IfmapVertexImpl mac1;
	private IfmapVertexImpl mac2;
	private MetadataImpl event1;
	private MetadataImpl event2;
	private MetadataImpl event3;
	private MetadataImpl ipMacMeta1;
	private MetadataImpl ipMacMeta2;
	private MetadataImpl devIpMeta1;
	private MetadataImpl devIpMeta2;
	private MetadataImpl loopMeta1;
	private MetadataImpl loopMeta2;
	private MetadataImpl restrictedMeta1;
	private IfmapEdgeImpl ipMacEdge1;
	private IfmapEdgeImpl ipMacEdge2;
	private IfmapEdgeImpl devIpEdge1;
	private IfmapEdgeImpl devIpEdge2;
	private IfmapEdgeImpl loopEdge1;
	private IfmapEdgeImpl loopEdge2;
	private IfmapEdgeImpl restrictedEdge1;

	/**
	 * Sets up the test environment.
	 */
	@Before
	public void setUp() {
		realGraph = new IfmapGraphImpl();

		ip1 = new IfmapVertexImpl("ip-address");
		ip1.addProperty("/ip-address[@type]", "IPv4");
		ip1.addProperty("/ip-address[@value]", "127.0.0.1");

		dev1 = new IfmapVertexImpl("device");
		dev1.addProperty("/device/name", "device1");

		dev2 = new IfmapVertexImpl("device");
		dev2.addProperty("/device/name", "device2");

		dev3 = new IfmapVertexImpl("device");
		dev3.addProperty("/device/name", "device3");

		mac1 = new IfmapVertexImpl("mac-address");
		mac1.addProperty("/mac-address[@value]", "aa:bb:cc:dd:ee:ff");

		mac2 = new IfmapVertexImpl("mac-address");
		mac2.addProperty("/mac-address[@value]", "11:22:33:44:55:66");

		event1 = new MetadataImpl("event");
		event1.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		event1.addProperty("related", "12345");

		event2 = new MetadataImpl("event");
		event2.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");
		event2.addProperty("related", "67890");

		event3 = new MetadataImpl("event");
		event3.addProperty("/meta:event[@ifmap-cardinality]", "multiValue");

		ipMacMeta1 = new MetadataImpl("ip-mac");
		ipMacMeta1.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");

		ipMacMeta2 = new MetadataImpl("ip-mac");
		ipMacMeta2.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");

		devIpMeta1 = new MetadataImpl("device-ip");
		devIpMeta1.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");

		devIpMeta2 = new MetadataImpl("device-ip");
		devIpMeta2.addProperty("/meta:event[@ifmap-cardinality]", "singleValue");

		ipMacEdge1 = new IfmapEdgeImpl(ip1, mac1, ipMacMeta1);
		ipMacEdge2 = new IfmapEdgeImpl(ip1, mac2, ipMacMeta2);
		devIpEdge1 = new IfmapEdgeImpl(dev1, ip1, devIpMeta1);
		devIpEdge2 = new IfmapEdgeImpl(dev2, ip1, devIpMeta2);

		dev1.addMetadata(event1);
		dev2.addMetadata(event2);
		dev2.addMetadata(event3);

		realGraph.addVertex(dev1);
		realGraph.addVertex(dev2);
		realGraph.addVertex(dev3);
		realGraph.addVertex(ip1);
		realGraph.addVertex(mac1);
		realGraph.addVertex(mac2);

		realGraph.addEdgeSensitive(dev1, ip1, devIpEdge1);
		realGraph.addEdgeSensitive(dev2, ip1, devIpEdge2);
		realGraph.addEdgeSensitive(ip1, mac1, ipMacEdge1);
		realGraph.addEdgeSensitive(ip1, mac2, ipMacEdge2);

		loopMeta1 = new MetadataImpl("loop");
		loopEdge1 = new IfmapEdgeImpl(dev1, mac1, loopMeta1);
		realGraph.addEdgeSensitive(dev1, mac1, loopEdge1);

		loopMeta2 = new MetadataImpl("loop");
		loopEdge2 = new IfmapEdgeImpl(dev3, mac2, loopMeta2);
		realGraph.addEdgeSensitive(dev3, mac2, loopEdge2);
	}

	/**
	 *
	 */
	@Test
	public void testComparison() {
		HashMap<String, String> relationTable = new HashMap<>();
		relationTable.put("binky", "value");

		BasicPatternMetadata patternMeta = new BasicPatternMetadata("device-ip");
		patternMeta.addProperty("binky", "binky", true, false);

		MetadataImpl realMeta = new MetadataImpl("device-ip");
		realMeta.addProperty("binky", "binky");

		BasicPatternVertex patternDev = new BasicPatternVertex("device");
		IfmapVertexImpl realDev = new IfmapVertexImpl("device");
		assertTrue(ComparatorUtil.compare(patternDev, realDev, relationTable));

		patternDev.addProperty("key1", "binky", false, true);
		realDev.addProperty("key1", "value");
		assertTrue(ComparatorUtil.compare(patternDev, realDev, relationTable));

		patternDev.addProperty("binky", "binky", true, false);
		assertTrue(ComparatorUtil.compare(patternDev, realDev, relationTable));

		realDev.addProperty("binky", "binky");
		assertFalse(ComparatorUtil.compare(patternDev, realDev, relationTable));
	}

	/**
	 *
	 */
	@Test
	public void testRegularMetadataComparison() {
		HashMap<String, String> relationTable = new HashMap<>();

		MetadataImpl realMeta = new MetadataImpl("metadata");
		realMeta.addProperty("test", "test");

		BasicPatternMetadata otherMetadata = new BasicPatternMetadata("other");
		assertFalse(ComparatorUtil.compare(otherMetadata, realMeta, relationTable));

		BasicPatternMetadata regularMetadata = new BasicPatternMetadata("metadata");

		assertTrue(ComparatorUtil.compare(regularMetadata, realMeta, relationTable));

		BasicPatternMetadata regularMetadataWithMatchingProperty = new BasicPatternMetadata("metadata");
		regularMetadataWithMatchingProperty.addProperty("test", "test", false, false);
		assertTrue(ComparatorUtil.compare(regularMetadataWithMatchingProperty, realMeta, relationTable));

		BasicPatternMetadata regularMetadataWithMissingProperty = new BasicPatternMetadata("metadata");
		regularMetadataWithMissingProperty.addProperty("missing", "missing", false, false);
		assertFalse(ComparatorUtil.compare(regularMetadataWithMissingProperty, realMeta, relationTable));

		BasicPatternMetadata regularMetadataWithMatchingRestrictedProperty = new BasicPatternMetadata("metadata");
		regularMetadataWithMatchingRestrictedProperty.addProperty("test", "test", true, false);
		assertFalse(ComparatorUtil.compare(regularMetadataWithMatchingRestrictedProperty, realMeta, relationTable));

		BasicPatternMetadata regularMetadataWithMissingRestrictedProperty = new BasicPatternMetadata("metadata");
		regularMetadataWithMissingRestrictedProperty.addProperty("missing", "missing", true, false);
		assertTrue(ComparatorUtil.compare(regularMetadataWithMissingRestrictedProperty, realMeta, relationTable));
	}

	/**
	 *
	 */
	@Test
	public void testVertexWithoutMetadataComparison() {
		HashMap<String, String> relationTable = new HashMap<>();

		IfmapVertexImpl realVertex = new IfmapVertexImpl("vertex");
		realVertex.addProperty("test", "test");

		BasicPatternVertex otherVertex = new BasicPatternVertex("other");
		assertFalse(ComparatorUtil.compare(otherVertex, realVertex, relationTable));

		BasicPatternVertex vertexWithoutProperty = new BasicPatternVertex("vertex");

		assertTrue(ComparatorUtil.compare(vertexWithoutProperty, realVertex, relationTable));

		BasicPatternVertex vertexWithMatchingProperty = new BasicPatternVertex("vertex");
		vertexWithMatchingProperty.addProperty("test", "test", false, false);
		assertTrue(ComparatorUtil.compare(vertexWithMatchingProperty, realVertex, relationTable));

		BasicPatternVertex vertexWithMissingProperty = new BasicPatternVertex("vertex");
		vertexWithMissingProperty.addProperty("missing", "missing", false, false);
		assertFalse(ComparatorUtil.compare(vertexWithMissingProperty, realVertex, relationTable));

		BasicPatternVertex vertexWithMatchingRestrictedProperty = new BasicPatternVertex("vertex");
		vertexWithMatchingRestrictedProperty.addProperty("test", "test", true, false);
		assertFalse(ComparatorUtil.compare(vertexWithMatchingRestrictedProperty, realVertex, relationTable));

		BasicPatternVertex vertexWithMissingRestrictedProperty = new BasicPatternVertex("vertex");
		vertexWithMissingRestrictedProperty.addProperty("missing", "missing", true, false);
		assertTrue(ComparatorUtil.compare(vertexWithMissingRestrictedProperty, realVertex, relationTable));
	}

	/**
	 *
	 */
	@Test
	public void testVertexWithMetadataComparison() {
		HashMap<String, String> relationTable = new HashMap<>();

		IfmapVertexImpl realVertex = new IfmapVertexImpl("vertex");

		MetadataImpl realMetadata = new MetadataImpl("metadata");
		realVertex.addMetadata(realMetadata);

		BasicPatternMetadata matchingMeta = new BasicPatternMetadata("metadata");
		BasicPatternMetadata nonMatchingMeta = new BasicPatternMetadata("other");

		BasicPatternVertex otherVertex = new BasicPatternVertex("other");
		otherVertex.addMetadata(matchingMeta);
		assertFalse(ComparatorUtil.compare(otherVertex, realVertex, relationTable));

		BasicPatternVertex vertexWithMatchingMetadata = new BasicPatternVertex("vertex");
		vertexWithMatchingMetadata.addMetadata(matchingMeta);
		assertTrue(ComparatorUtil.compare(vertexWithMatchingMetadata, realVertex, relationTable));

		BasicPatternVertex vertexWithNonMatchingMetadata = new BasicPatternVertex("vertex");
		vertexWithNonMatchingMetadata.addMetadata(nonMatchingMeta);
		assertFalse(ComparatorUtil.compare(vertexWithNonMatchingMetadata, realVertex, relationTable));
	}

	/**
	 * Tests the comparison with a simple pattern to be present (no restrictions or related properties)
	 */
	@Test
	public void testSimplePattern() {
		PatternGraphImpl pattern = new PatternGraphImpl();
		BasicPatternVertex dev = new BasicPatternVertex("device");
		BasicPatternVertex ip = new BasicPatternVertex("ip-address");
		BasicPatternVertex mac = new BasicPatternVertex("mac-address");

		BasicPatternMetadata devIpMeta = new BasicPatternMetadata("device-ip");
		BasicPatternMetadata ipMacMeta = new BasicPatternMetadata("ip-mac");

		BasicPatternEdge devIpEdge = new BasicPatternEdge(dev, ip, devIpMeta);
		BasicPatternEdge ipMacEdge = new BasicPatternEdge(ip, mac, ipMacMeta);

		pattern.addVertex(dev);
		pattern.addVertex(ip);
		pattern.addVertex(mac);

		pattern.addEdgeSensitive(dev, ip, devIpEdge);
		pattern.addEdgeSensitive(ip, mac, ipMacEdge);

		BasicPatternRule rule = new BasicPatternRule(pattern);

		Set<RuleMatch> resultSet = new RuleComparison(rule, realGraph).getResult();

		assertEquals(4, resultSet.size());
		for (RuleMatch res : resultSet) {
			IfmapGraph r = res.getResultGraph();
			if (r.containsVertex(dev1) && r.containsVertex(mac1)) {
				assertTrue(r.containsVertex(ip1));

				assertTrue(r.containsEdge(devIpEdge1));
				assertTrue(r.containsEdge(ipMacEdge1));

				assertFalse(r.containsVertex(dev2));
				assertFalse(r.containsVertex(mac2));
				assertFalse(r.containsVertex(dev3));
				assertFalse(r.containsEdge(devIpEdge2));
				assertFalse(r.containsEdge(ipMacEdge2));
				assertFalse(r.containsEdge(loopEdge1));
				assertFalse(r.containsEdge(loopEdge2));

				IfmapVertex resultDev = findVertexInGraph(r, dev1);
				IfmapVertex resultMac = findVertexInGraph(r, mac1);
				IfmapVertex resultIp = findVertexInGraph(r, ip1);

				assertTrue(resultDev.getMetadata().isEmpty());
				assertTrue(resultMac.getMetadata().isEmpty());
				assertTrue(resultIp.getMetadata().isEmpty());
			} else if (r.containsVertex(dev1) && r.containsVertex(mac2)) {
				assertTrue(r.containsVertex(ip1));

				assertTrue(r.containsEdge(devIpEdge1));
				assertTrue(r.containsEdge(ipMacEdge2));

				assertFalse(r.containsVertex(dev3));
				assertFalse(r.containsVertex(dev2));
				assertFalse(r.containsVertex(mac1));
				assertFalse(r.containsEdge(devIpEdge2));
				assertFalse(r.containsEdge(ipMacEdge1));
				assertFalse(r.containsEdge(loopEdge1));
				assertFalse(r.containsEdge(loopEdge2));

				IfmapVertex resultDev = findVertexInGraph(r, dev1);
				IfmapVertex resultMac = findVertexInGraph(r, mac2);
				IfmapVertex resultIp = findVertexInGraph(r, ip1);

				assertTrue(resultDev.getMetadata().isEmpty());
				assertTrue(resultMac.getMetadata().isEmpty());
				assertTrue(resultIp.getMetadata().isEmpty());
			} else if (r.containsVertex(dev2) && r.containsVertex(mac1)) {
				assertTrue(r.containsVertex(ip1));

				assertTrue(r.containsEdge(devIpEdge2));
				assertTrue(r.containsEdge(ipMacEdge1));

				assertFalse(r.containsVertex(dev3));
				assertFalse(r.containsVertex(dev1));
				assertFalse(r.containsVertex(mac2));
				assertFalse(r.containsEdge(devIpEdge1));
				assertFalse(r.containsEdge(ipMacEdge2));
				assertFalse(r.containsEdge(loopEdge1));
				assertFalse(r.containsEdge(loopEdge2));

				IfmapVertex resultDev = findVertexInGraph(r, dev2);
				IfmapVertex resultMac = findVertexInGraph(r, mac1);
				IfmapVertex resultIp = findVertexInGraph(r, ip1);

				assertTrue(resultDev.getMetadata().isEmpty());
				assertTrue(resultMac.getMetadata().isEmpty());
				assertTrue(resultIp.getMetadata().isEmpty());
			} else if (r.containsVertex(dev2) && r.containsVertex(mac2)) {
				assertTrue(r.containsVertex(ip1));

				assertTrue(r.containsEdge(devIpEdge2));
				assertTrue(r.containsEdge(ipMacEdge2));

				assertFalse(r.containsVertex(dev3));
				assertFalse(r.containsVertex(dev1));
				assertFalse(r.containsVertex(mac1));
				assertFalse(r.containsEdge(devIpEdge1));
				assertFalse(r.containsEdge(ipMacEdge1));
				assertFalse(r.containsEdge(loopEdge1));
				assertFalse(r.containsEdge(loopEdge2));

				IfmapVertex resultDev = findVertexInGraph(r, dev2);
				IfmapVertex resultMac = findVertexInGraph(r, mac2);
				IfmapVertex resultIp = findVertexInGraph(r, ip1);

				assertTrue(resultDev.getMetadata().isEmpty());
				assertTrue(resultMac.getMetadata().isEmpty());
				assertTrue(resultIp.getMetadata().isEmpty());
			} else {
				fail("Wrong result!");
			}
		}
	}

	/**
	 * Tests the comparison with a loop pattern to be present (no restrictions or related properties)
	 */
	@Test
	public void testLoopPattern() {
		PatternGraphImpl pattern = new PatternGraphImpl();
		BasicPatternVertex dev = new BasicPatternVertex("device");
		BasicPatternVertex ip = new BasicPatternVertex("ip-address");
		BasicPatternVertex mac = new BasicPatternVertex("mac-address");

		BasicPatternMetadata devIpMeta = new BasicPatternMetadata("device-ip");
		BasicPatternMetadata ipMacMeta = new BasicPatternMetadata("ip-mac");
		BasicPatternMetadata patternLoopMeta = new BasicPatternMetadata("loop");

		BasicPatternEdge devIpEdge = new BasicPatternEdge(dev, ip, devIpMeta);
		BasicPatternEdge ipMacEdge = new BasicPatternEdge(ip, mac, ipMacMeta);
		BasicPatternEdge patternLoopEdge = new BasicPatternEdge(dev, mac, patternLoopMeta);

		pattern.addVertex(dev);
		pattern.addVertex(ip);
		pattern.addVertex(mac);

		pattern.addEdgeSensitive(dev, ip, devIpEdge);
		pattern.addEdgeSensitive(ip, mac, ipMacEdge);
		pattern.addEdgeSensitive(dev, mac, patternLoopEdge);

		BasicPatternRule rule = new BasicPatternRule(pattern);

		Set<RuleMatch> resultSet = new RuleComparison(rule, realGraph).getResult();

		assertEquals(1, resultSet.size());
		for (RuleMatch res : resultSet) {

			IfmapGraph r = res.getResultGraph();
			if (!r.containsEdge(loopEdge1)) {
				fail("loop not found");
			} else {
				assertTrue(r.containsVertex(dev1));
				assertTrue(r.containsVertex(mac1));
				assertTrue(r.containsVertex(ip1));

				assertTrue(r.containsEdge(devIpEdge1));
				assertTrue(r.containsEdge(ipMacEdge1));
				assertTrue(r.containsEdge(loopEdge1));

				assertFalse(r.containsVertex(dev3));
				assertFalse(r.containsVertex(dev2));
				assertFalse(r.containsVertex(mac2));
				assertFalse(r.containsEdge(devIpEdge2));
				assertFalse(r.containsEdge(ipMacEdge2));
				assertFalse(r.containsEdge(loopEdge2));

				IfmapVertex resultDev = findVertexInGraph(r, dev1);
				IfmapVertex resultMac = findVertexInGraph(r, mac1);
				IfmapVertex resultIp = findVertexInGraph(r, ip1);

				assertTrue(resultDev.getMetadata().isEmpty());
				assertTrue(resultMac.getMetadata().isEmpty());
				assertTrue(resultIp.getMetadata().isEmpty());
			}
		}
	}

	private IfmapVertex findVertexInGraph(IfmapGraph result, IfmapVertex vertex) {
		for (IfmapVertex v : result.vertexSet()) {
			if (vertex.equals(v)) {
				return v;
			}
		}
		return null;
	}

}
