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
package de.hshannover.f4.trust.irongpm.ifmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishElement;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.metadata.Cardinality;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactoryImpl;
import de.hshannover.f4.trust.ifmapj.metadata.VendorSpecificMetadataFactory;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irongpm.IronGpm;
import de.hshannover.f4.trust.irongpm.algorithm.RuleWrapper;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;

/**
 * @author Bastian Hellmann
 *
 */
public class PolicyPublisher {

	private static final String METADATA_NAMESPACE_PREFIX = "policy";
	private static final String IDENTIFIER_NAMESPACE_PREFIX = "policy";
	private static final String POLICY_METADATA_NS_URI = "http://www.trust.f4.hs-hannover.de/2016/POLICY/METADATA/1";
	private static final String POLICY_IDENTIFIER_NS_URI = "http://www.trust.f4.hs-hannover.de/2016/POLICY/IDENTIFIER/1";

	private static final String POLICY_IDENTIFIER = "policy";
	private static final String RULE_IDENTIFIER = "rule";
	private static final String PATTERN_VERTEX_IDENTIFIER = "patternvertex";

	private static final String PATTERN_IDENTIFIER_METADATA = "patternmetadata";
	private static final String PATTERN_EDGE_METADATA = "patternedge";
	
	private static final String DEVICE_TO_POLICY_METADATA_LINK = "device-policy";
	private static final String POLICY_TO_RULE_METADATA_LINK = "policy-rule";
	private static final String RULE_TO_FIRST_PATTERN_VERTEX_METADATA_LINK = "rule-patternvertex";
	
	private static final String POLICY_ADMINSTRATIVE_DOMAIN = "irongpm-policy";
	
	private static final String POLICY_QUALIFIED_NAME = "policy";
	
	private static final Logger LOGGER = Logger.getLogger(PolicyPublisher.class);

	
	private Properties mConfig = IronGpm.getConfig();
	
	private SSRC mSSRC;

	private StandardIfmapMetadataFactoryImpl mMetadataFactory;
	private VendorSpecificMetadataFactory mVendorSpecificMetadataFactory;

	public PolicyPublisher() throws IfmapErrorResult, IfmapException {
		mSSRC = init();
		
		mMetadataFactory = new StandardIfmapMetadataFactoryImpl();
		mVendorSpecificMetadataFactory = IfmapJ.createVendorSpecificMetadataFactory();
	}

	private SSRC init() throws IfmapErrorResult, IfmapException {
		String url = mConfig.getString("ifmap.auth.basic.url", "https://127.0.0.1:8443");
		String username = mConfig.getString("ifmap.auth.basic.policypublisher.user", "irongpm-publisher");
		String password = mConfig.getString("ifmap.auth.basic.policypublisher.password", "irongpm-publisher");
		String trustStorePath = mConfig.getString("ifmap.truststore.path", "/irongpm.jks");
		String trustStorePassword = mConfig.getString("ifmap.truststore.password", "irongpm");
		boolean threadSafe = mConfig.getBoolean("ifmap.threadsafe", true);
		int initialConnectionTimeout = mConfig.getInt("ifmap.auth.initialconnectiontimeout", 120000);
		
		BasicAuthConfig config = new BasicAuthConfig(url, username, password, trustStorePath, trustStorePassword, threadSafe, initialConnectionTimeout);
		LOGGER.debug(config);

		mSSRC = IfmapJ.createSsrc(config);
		mSSRC.newSession();
		LOGGER.info("IF-MAP connection established successfully");
		
		return mSSRC;
	}
	
	public void publishRules(Map<String, List<RuleWrapper>> ruleLoaderMapping) throws IfmapErrorResult, IfmapException {
		LOGGER.info("Trying to publish irongpm rules.");
		
		PublishRequest request = Requests.createPublishReq();
		List<PublishElement> rulePublishElements = new ArrayList<>();
		
		String startIdentifierDeviceName = mConfig.getString("irongpm.publisher.policy.devicename", "irongpm-policy");
		Identifier startDeviceIdentifier = Identifiers.createDev(startIdentifierDeviceName);
		Identifier ironGpmPolicyIdentifier;
		
		for (String ruleLoaderName : ruleLoaderMapping.keySet()) {
			ironGpmPolicyIdentifier = createPolicyIdentifier(ruleLoaderName);
			PublishElement deviceToPolicy = createDeviceToPolicy(startDeviceIdentifier, ironGpmPolicyIdentifier);
			request.addPublishElement(deviceToPolicy);
			
			rulePublishElements.addAll(createPublishElementsFromRuleWrappers(ruleLoaderMapping.get(ruleLoaderName), ironGpmPolicyIdentifier));
			for (PublishElement publishElement : rulePublishElements) {
				request.addPublishElement(publishElement);
			}
		}
		
		mSSRC.publish(request);
	}

	private PublishElement createDeviceToPolicy(Identifier startDeviceIdentifier, Identifier ironGpmPolicyIdentifier) {
		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(startDeviceIdentifier);
		result.setIdentifier2(ironGpmPolicyIdentifier);
		
		Document deviceToPolicyLinkMetadata = mMetadataFactory.create(DEVICE_TO_POLICY_METADATA_LINK, POLICY_QUALIFIED_NAME,
				POLICY_METADATA_NS_URI, Cardinality.singleValue);
		result.addMetadata(deviceToPolicyLinkMetadata);
		
		return result;
	}

	private PublishElement createPolicyToRule(Identifier ironGpmPolicyIdentifier, Identifier ruleStartIdentifier) {
		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(ironGpmPolicyIdentifier);
		result.setIdentifier2(ruleStartIdentifier);
		
		Document policyToRuleLinkMetadata = mMetadataFactory.create(POLICY_TO_RULE_METADATA_LINK, POLICY_QUALIFIED_NAME,
				POLICY_METADATA_NS_URI, Cardinality.singleValue);
		result.addMetadata(policyToRuleLinkMetadata);
		
		return result;
	}

	private PublishElement createRuleToFirstPatternVertex(Identifier ruleStartIdentifier, Identifier firstPatternVertexIdentifier) {
		PublishUpdate result = Requests.createPublishUpdate();
		result.setIdentifier1(ruleStartIdentifier);
		result.setIdentifier2(firstPatternVertexIdentifier);
		result.setLifeTime(MetadataLifetime.session);
		
		Document ruleToFirstPatternVertexLinkMetadata = mMetadataFactory.create(RULE_TO_FIRST_PATTERN_VERTEX_METADATA_LINK, POLICY_QUALIFIED_NAME,
				POLICY_METADATA_NS_URI, Cardinality.singleValue);		
		result.addMetadata(ruleToFirstPatternVertexLinkMetadata);
		
		return result;
	}
	
	private Identifier createPolicyIdentifier(String ruleLoaderName) throws MarshalException {
		String policyIdentifierDocument = "<" + IDENTIFIER_NAMESPACE_PREFIX + ":" + POLICY_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + "\" "
				+ "name=\"" + ruleLoaderName + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" "
				+ "/>";
		
		return Identifiers.createExtendedIdentity(policyIdentifierDocument);
	}

	private Identifier createRuleStartIdentifier(RuleWrapper rule) throws MarshalException {
		String name = rule.getName();
		long id = rule.getId();
		String description = rule.getDescription();
		String recommendation = rule.getRecommendation();
		
		String ruleIdentifierDocument = "<" + IDENTIFIER_NAMESPACE_PREFIX + ":" + RULE_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + "\" "
				+ "name=\"" + name + "\" "
				+ "id=\"" + id + "\" "
				+ "description=\"" + description + "\" "
				+ "recommendation=\"" + recommendation + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" "
				+ "/>";
		
		return Identifiers.createExtendedIdentity(ruleIdentifierDocument);
	}

	private Identifier createPatternVertexIdentifier(PatternVertex vertex) throws MarshalException {
		String vertexDocument = "<" + IDENTIFIER_NAMESPACE_PREFIX + ":" + PATTERN_VERTEX_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + "\" "
				+ "typename=\"" + vertex.getTypeName() + "\" "
				+ "properties=\"" + vertex.getProperties().toString() + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" "
				+ "/>";

		return Identifiers.createExtendedIdentity(vertexDocument);
	}

	private List<PublishElement> createPublishElementsFromRuleWrappers(List<RuleWrapper> rules, Identifier ironGpmPolicyIdentifier) throws MarshalException {
		List<PublishElement> result = new ArrayList<>();
		
		for (RuleWrapper rule : rules) {
			Identifier ruleStartIdentifier = createRuleStartIdentifier(rule);
			PublishElement policyToRule = createPolicyToRule(ironGpmPolicyIdentifier, ruleStartIdentifier);
			result.add(policyToRule);
			
			result.addAll(createRuleSubgraph(ruleStartIdentifier, rule));
		}
		
		return result;
	}

	private List<PublishElement> createRuleSubgraph(Identifier ruleStartIdentifier, RuleWrapper rule) throws MarshalException {
		List<PublishElement> result = new ArrayList<>();

		PatternGraph pattern = rule.getPattern();
		PatternVertex publishVertex = pattern.getPublishVertex();
		
		Identifier firstPatternVertexIdentifier = createPatternVertexIdentifier(publishVertex);
		
		PublishElement ruleToFirstPatternVertex = createRuleToFirstPatternVertex(ruleStartIdentifier, firstPatternVertexIdentifier);
		result.add(ruleToFirstPatternVertex);
		
		Set<PatternVertex> seen = new HashSet<>();
		traversePatternGraph(publishVertex, seen, pattern, result);
				
		return result;
	}
	
	private void traversePatternGraph(PatternVertex current, Set<PatternVertex> seen, PatternGraph graph, List<PublishElement> publishElements) throws MarshalException {
		Identifier detached;
		Identifier detachedOther;
		List<Document> detachedMetadata;
		Document linkMetadata;
		
		if (!seen.contains(current)) {
			detached = createPatternVertexIdentifier(current);
			detachedMetadata = createPatternVertexIdentifierMetadata(current);
			
			publishElements.addAll(createPublishElementForAttachedMetadata(detached, detachedMetadata));
			
			seen.add(current);
			
			for (PatternEdge edge : graph.edgesOf(current)) {
				PatternVertex edgeSource = graph.getEdgeSource(edge);
				PatternVertex edgeTarget = graph.getEdgeTarget(edge);
				
				PatternVertex other = edgeTarget;
				if (edgeSource != current) {
					other = edgeSource;
				}
				
				if (seen.contains(other)) {
					linkMetadata = createPatternEdgeMetadata(edge);
					detachedOther = createPatternVertexIdentifier(other);
					publishElements.add(createPublishElementForEdge(detached, detachedOther, linkMetadata));
				} else {
					traversePatternGraph(other, seen, graph, publishElements);
				}
			}
		}
		
	}

	private PublishElement createPublishElementForEdge(Identifier current, Identifier other,
			Document metadata) {
		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(current);
		result.setIdentifier2(other);
		result.addMetadata(metadata);
		return result;
	}

	private List<PublishElement> createPublishElementForAttachedMetadata(Identifier current, List<Document> detachedMetadata) {
		List<PublishElement> result = new ArrayList<>();
		PublishUpdate publishUpdate;
		
		for (Document metadata : detachedMetadata) {		
			publishUpdate = Requests.createPublishUpdate();
			publishUpdate.setLifeTime(MetadataLifetime.session);
			publishUpdate.setIdentifier1(current);
			publishUpdate.addMetadata(metadata);
			
			result.add(publishUpdate);
		}
		
		return result;
	}

	private List<Document> createPatternVertexIdentifierMetadata(PatternVertex vertex) {
		List<Document> result = new ArrayList<>();
		String cardinality;
		String vendorSpecificMetadataXml;
		Document metadataDocument;
		
		for (PatternMetadata metadata : vertex.getMetadata()) {
			cardinality = (metadata.isSingleValue() == true ? "singleValue" : "multiValue");
			
			vendorSpecificMetadataXml = "<" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_IDENTIFIER_METADATA + " "
					+ "ifmap-cardinality=\"" + cardinality + "\" "
					+ "xmlns:" + METADATA_NAMESPACE_PREFIX + "=\"" + POLICY_METADATA_NS_URI + "\">"
					+ "<typename>" + metadata.getTypeName() + "</typename>"
					+ "<properties>" + metadata.getProperties() + "</properties>"
					+ "</" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_IDENTIFIER_METADATA + ">";
			metadataDocument = mVendorSpecificMetadataFactory
					.createMetadata(vendorSpecificMetadataXml);

			result.add(metadataDocument);
		}

		return result;
	}

	private Document createPatternEdgeMetadata(PatternEdge edge) {
		PatternMetadata metadata = edge.getMetadata();
		String cardinality = (metadata.isSingleValue() == true ? "singleValue" : "multiValue");
		
		String vendorSpecificMetadataXml = "<" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_EDGE_METADATA + " "
				+ "ifmap-cardinality=\"" + cardinality + "\" "
				+ "xmlns:" + METADATA_NAMESPACE_PREFIX + "=\"" + POLICY_METADATA_NS_URI + "\">"
				+ "<typename>" + metadata.getTypeName() + "</typename>"
				+ "<properties>" + metadata.getProperties() + "</properties>"
				+ "</" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_EDGE_METADATA + ">";

		return mVendorSpecificMetadataFactory
				.createMetadata(vendorSpecificMetadataXml);
	}
}
