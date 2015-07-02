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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Iterator;

import org.jgrapht.Graphs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.hshannover.f4.trust.irongpm.ifmap.IfmapEdgeImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapVertexImpl;
import de.hshannover.f4.trust.irongpm.ifmap.MetadataImpl;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.rest.IfmapGraphJsonAdapter;

/**
 * Test class for JSON parsing of an IF-MAP Graph using gson.
 *
 * @author Leonard Renners
 *
 */
public class JsonParserTest {

	private static GsonBuilder gsob;
	private static Gson gson;
	private static JsonParser parser;

	/**
	 * Sets up the test environment.
	 */
	@Before
	public void setUp() {
		gsob = new GsonBuilder().disableHtmlEscaping();
		gsob.registerTypeAdapter(IfmapGraphImpl.class, new IfmapGraphJsonAdapter());
		gson = gsob.create();
		parser = new JsonParser();
	}

	/**
	 * Tests the parsing of a JSON String
	 */
	@Test
	public void testJsonDeserialize() {
		String json = "[{\"timestamp\":1409833398907,\"links\":[{\"identifiers\":[{\"typename\":\"device\",\"properties\":{\"/device/name\":\"freeradius-pdp\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"device-ip\",\"properties\":{\"/meta:device-ip[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:device-ip[@ifmap-cardinality]\":\"singleValue\",\"/meta:device-ip[@ifmap-timestamp]\":\"2014-09-04T14:23:08+02:00\",\"/meta:device-ip[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\"}}},{\"identifiers\":[{\"typename\":\"mac-address\",\"properties\":{\"/mac-address[@value]\":\"aa:bb:cc:dd:ee:ff\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"ip-mac\",\"properties\":{\"/meta:ip-mac[@ifmap-cardinality]\":\"multiValue\",\"/meta:ip-mac[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:ip-mac[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\",\"/meta:ip-mac/end-time\":\"2014-09-04T22:22:50+02:00\",\"/meta:ip-mac/dhcp-server\":\"ip-mac-cli\",\"/meta:ip-mac/start-time\":\"2014-09-04T14:22:50+02:00\",\"/meta:ip-mac[@ifmap-timestamp]\":\"2014-09-04T14:22:51+02:00\"}}},{\"identifiers\":[{\"typename\":\"device\",\"properties\":{\"/device/name\":\"bronko\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"device-ip\",\"properties\":{\"/meta:device-ip[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:device-ip[@ifmap-cardinality]\":\"singleValue\",\"/meta:device-ip[@ifmap-timestamp]\":\"2014-09-04T14:22:33+02:00\",\"/meta:device-ip[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\"}}}]}]";

		IfmapGraphImpl graphFromString = new IfmapGraphImpl();
		JsonArray array = parser.parse(json).getAsJsonArray();
		for (JsonElement elem : array) {
			IfmapGraphImpl graphPart = gson.fromJson(elem, IfmapGraphImpl.class);
			graphFromString.setLastUpdated(graphPart.getLastUpdated());
			Graphs.addGraph(graphFromString, graphPart);
		}

		IfmapVertexImpl pdp = new IfmapVertexImpl("device", new HashMap<String, String>(), null);
		pdp.addProperty("/device/name", "freeradius-pdp");

		IfmapVertexImpl bronko = new IfmapVertexImpl("device", new HashMap<String, String>(), null);
		bronko.addProperty("/device/name", "bronko");

		IfmapVertexImpl ip = new IfmapVertexImpl("ip-address", new HashMap<String, String>(), null);
		ip.addProperty("/ip-address[@type]", "IPv4");
		ip.addProperty("/ip-address[@value]", "192.168.0.1");

		IfmapVertexImpl mac = new IfmapVertexImpl("mac-address", new HashMap<String, String>(), null);
		mac.addProperty("/mac-address[@value]", "aa:bb:cc:dd:ee:ff");

		MetadataImpl deviceIpPdp = new MetadataImpl("device-ip", new HashMap<String, String>(), null);
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-timestamp]", "2014-09-04T14:23:08+02:00");
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue");
		deviceIpPdp.addProperty("/meta:device-ip[@xmlns:meta]",
				"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		MetadataImpl ipMac = new MetadataImpl("ip-mac", new HashMap<String, String>(), null);
		ipMac.addProperty("/meta:ip-mac[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		ipMac.addProperty("/meta:ip-mac[@ifmap-timestamp]", "2014-09-04T14:22:51+02:00");
		ipMac.addProperty("/meta:ip-mac/start-time", "2014-09-04T14:22:50+02:00");
		ipMac.addProperty("/meta:ip-mac/end-time", "2014-09-04T22:22:50+02:00");
		ipMac.addProperty("/meta:ip-mac/dhcp-server", "ip-mac-cli");
		ipMac.addProperty("/meta:ip-mac[@ifmap-cardinality]", "multiValue");
		ipMac.addProperty("/meta:ip-mac[@xmlns:meta]", "http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		MetadataImpl deviceIpBronko = new MetadataImpl("device-ip", new HashMap<String, String>(), null);
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-timestamp]", "2014-09-04T14:22:33+02:00");
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue");
		deviceIpBronko.addProperty("/meta:device-ip[@xmlns:meta]",
				"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		IfmapGraphImpl graph = new IfmapGraphImpl();
		graph.setLastUpdated(Long.valueOf("1409833398907"));
		graph.addVertex(pdp);
		graph.addVertex(ip);
		graph.addVertex(mac);
		graph.addVertex(bronko);

		IfmapEdgeImpl e1 = new IfmapEdgeImpl(pdp, ip, deviceIpPdp);

		IfmapEdgeImpl e2 = new IfmapEdgeImpl(ip, mac, ipMac);

		IfmapEdgeImpl e3 = new IfmapEdgeImpl(bronko, ip, deviceIpBronko);

		graph.addEdge(pdp, ip, e1);
		graph.addEdge(ip, mac, e2);
		graph.addEdge(bronko, ip, e3);
		Iterator<IfmapEdge> boing = graphFromString.edgeSet().iterator();
		assertEquals(graph, graphFromString);
	}

	/**
	 * Tests the parsing of a JSON String
	 */
	@Test
	public void testJsonSerialize() {
		String json = "[{\"timestamp\":1409833398907,\"links\":[{\"identifiers\":[{\"typename\":\"device\",\"properties\":{\"/device/name\":\"freeradius-pdp\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"device-ip\",\"properties\":{\"/meta:device-ip[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:device-ip[@ifmap-cardinality]\":\"singleValue\",\"/meta:device-ip[@ifmap-timestamp]\":\"2014-09-04T14:23:08+02:00\",\"/meta:device-ip[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\"}}},{\"identifiers\":[{\"typename\":\"mac-address\",\"properties\":{\"/mac-address[@value]\":\"aa:bb:cc:dd:ee:ff\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"ip-mac\",\"properties\":{\"/meta:ip-mac[@ifmap-cardinality]\":\"multiValue\",\"/meta:ip-mac[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:ip-mac[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\",\"/meta:ip-mac/end-time\":\"2014-09-04T22:22:50+02:00\",\"/meta:ip-mac/dhcp-server\":\"ip-mac-cli\",\"/meta:ip-mac/start-time\":\"2014-09-04T14:22:50+02:00\",\"/meta:ip-mac[@ifmap-timestamp]\":\"2014-09-04T14:22:51+02:00\"}}},{\"identifiers\":[{\"typename\":\"device\",\"properties\":{\"/device/name\":\"bronko\"}},{\"typename\":\"ip-address\",\"properties\":{\"/ip-address[@type]\":\"IPv4\",\"/ip-address[@value]\":\"192.168.0.1\"}}],\"metadata\":{\"typename\":\"device-ip\",\"properties\":{\"/meta:device-ip[@ifmap-publisher-id]\":\"test-4b698afc-1732-42e2-a6df-5063467dc836\",\"/meta:device-ip[@ifmap-cardinality]\":\"singleValue\",\"/meta:device-ip[@ifmap-timestamp]\":\"2014-09-04T14:22:33+02:00\",\"/meta:device-ip[@xmlns:meta]\":\"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2\"}}}]}]";

		IfmapVertexImpl pdp = new IfmapVertexImpl("device", new HashMap<String, String>(), null);
		pdp.addProperty("/device/name", "freeradius-pdp");

		IfmapVertexImpl bronko = new IfmapVertexImpl("device", new HashMap<String, String>(), null);
		bronko.addProperty("/device/name", "bronko");

		IfmapVertexImpl ip = new IfmapVertexImpl("ip-address", new HashMap<String, String>(), null);
		ip.addProperty("/ip-address[@type]", "IPv4");
		ip.addProperty("/ip-address[@value]", "192.168.0.1");

		IfmapVertexImpl mac = new IfmapVertexImpl("mac-address", new HashMap<String, String>(), null);
		mac.addProperty("/mac-address[@value]", "aa:bb:cc:dd:ee:ff");

		MetadataImpl deviceIpPdp = new MetadataImpl("device-ip", new HashMap<String, String>(), null);
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-timestamp]", "2014-09-04T14:23:08+02:00");
		deviceIpPdp.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue");
		deviceIpPdp.addProperty("/meta:device-ip[@xmlns:meta]",
				"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		MetadataImpl ipMac = new MetadataImpl("ip-mac", new HashMap<String, String>(), null);
		ipMac.addProperty("/meta:ip-mac[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		ipMac.addProperty("/meta:ip-mac[@ifmap-timestamp]", "2014-09-04T14:22:51+02:00");
		ipMac.addProperty("/meta:ip-mac/start-time", "2014-09-04T14:22:50+02:00");
		ipMac.addProperty("/meta:ip-mac/end-time", "2014-09-04T22:22:50+02:00");
		ipMac.addProperty("/meta:ip-mac/dhcp-server", "ip-mac-cli");
		ipMac.addProperty("/meta:ip-mac[@ifmap-cardinality]", "multiValue");
		ipMac.addProperty("/meta:ip-mac[@xmlns:meta]", "http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		MetadataImpl deviceIpBronko = new MetadataImpl("device-ip", new HashMap<String, String>(), null);
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-publisher-id]", "test-4b698afc-1732-42e2-a6df-5063467dc836");
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-timestamp]", "2014-09-04T14:22:33+02:00");
		deviceIpBronko.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue");
		deviceIpBronko.addProperty("/meta:device-ip[@xmlns:meta]",
				"http://www.trustedcomputinggroup.org/2010/IFMAP-METADATA/2");

		IfmapGraphImpl graph = new IfmapGraphImpl();
		graph.setLastUpdated(Long.valueOf("1409833398907"));
		graph.addVertex(pdp);
		graph.addVertex(ip);
		graph.addVertex(mac);
		graph.addVertex(bronko);

		IfmapEdgeImpl e1 = new IfmapEdgeImpl(pdp, ip, deviceIpPdp);

		IfmapEdgeImpl e2 = new IfmapEdgeImpl(ip, mac, ipMac);

		IfmapEdgeImpl e3 = new IfmapEdgeImpl(bronko, ip, deviceIpBronko);

		graph.addEdge(pdp, ip, e1);
		graph.addEdge(ip, mac, e2);
		graph.addEdge(bronko, ip, e3);

		IfmapGraphImpl[] binky = new IfmapGraphImpl[1];
		binky[0] = graph;

		assertEquals(gson.fromJson(parser.parse(json).getAsJsonArray().get(0), IfmapGraphImpl.class),
				gson.fromJson(parser.parse(gson.toJson(binky)).getAsJsonArray().get(0), IfmapGraphImpl.class));
	}

	/**
	 * Tears down the test environment.
	 */
	@After
	public void tearDown() {
	}

}
