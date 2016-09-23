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
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
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
import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.RuleWrapper;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternPropable;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.util.IfmapPublishUtil;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * @author Bastian Hellmann
 *
 */
public class PolicyPublisher {

	private static final String METADATA_NAMESPACE_PREFIX = "policy";
	private static final String IDENTIFIER_NAMESPACE_PREFIX = "policy";
	private static final String POLICY_METADATA_NS_URI = "http://www.trust.f4.hs-hannover.de/2016/POLICY/IRONGPM/METADATA/1";
	private static final String POLICY_IDENTIFIER_NS_URI = "http://www.trust.f4.hs-hannover.de/2016/POLICY/IRONGPM/IDENTIFIER/1";

	private static final String POLICY_IDENTIFIER = "policy";
	private static final String RULE_IDENTIFIER = "rule";
	private static final String PATTERN_VERTEX_IDENTIFIER = "patternvertex";

	private static final String PATTERN_IDENTIFIER_METADATA = "patternmetadata";
	private static final String PATTERN_EDGE_LINK = "patternedge";

	private static final String DEVICE_TO_POLICY_LINK = "device-policy";
	private static final String POLICY_TO_RULE_LINK = "policy-rule";
	private static final String RULE_TO_FIRST_PATTERN_VERTEX_LINK = "rule-pattern";
	private static final String PUBLISH_VERTEX_TO_MATCHED_IDENTIFIER_LINK = "pattern-matched";

	private static final String POLICY_ADMINSTRATIVE_DOMAIN = "irongpm-policy";

	private static final String POLICY_QUALIFIED_NAME = "policy";

	private static final Logger LOGGER = Logger.getLogger(PolicyPublisher.class);

	private static Properties mConfig = IronGpm.getConfig();

	private static StandardIfmapMetadataFactoryImpl mMetadataFactory = new StandardIfmapMetadataFactoryImpl();
	private static VendorSpecificMetadataFactory mVendorSpecificMetadataFactory =
			IfmapJ.createVendorSpecificMetadataFactory();

	/**
	 * @param ruleLoaderMapping
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public static void publishRules(Map<String, List<RuleWrapper>> ruleLoaderMapping)
			throws IfmapErrorResult, IfmapException {
		LOGGER.trace("Method publishRules(Map<String, List<RuleWrapper>> ruleLoaderMapping) called");
		LOGGER.info("Trying to publish irongpm rules.");

		PublishRequest request = Requests.createPublishReq();
		List<PublishElement> rulePublishElements = new ArrayList<>();

		String startIdentifierDeviceName;
		if (mConfig.getBoolean("irongpm.publisher.selfpublish.enabled", true)) {
			startIdentifierDeviceName = mConfig.getString("irongpm.publisher.selfpublish.devicename", "irongpm");
		} else {
			startIdentifierDeviceName = mConfig.getString("irongpm.publisher.policy.devicename", "irongpm-policy");
		}
		Identifier startDeviceIdentifier = Identifiers.createDev(startIdentifierDeviceName);
		Identifier ironGpmPolicyIdentifier;

		for (String ruleLoaderName : ruleLoaderMapping.keySet()) {
			ironGpmPolicyIdentifier = createPolicyIdentifier(ruleLoaderName);
			PublishElement deviceToPolicy = createDeviceToPolicy(startDeviceIdentifier, ironGpmPolicyIdentifier);
			request.addPublishElement(deviceToPolicy);

			rulePublishElements.addAll(createPublishElementsFromRuleWrappers(ruleLoaderMapping.get(ruleLoaderName),
					ironGpmPolicyIdentifier));
			for (PublishElement publishElement : rulePublishElements) {
				request.addPublishElement(publishElement);
			}
		}

		IfmapPublishUtil.publish(request);
	}

	/**
	 * @param rule
	 * @param match
	 * @throws IfmapErrorResult
	 * @throws IfmapException
	 */
	public static void publishAction(PatternRule rule, RuleMatch match) throws IfmapErrorResult, IfmapException {
		LOGGER.trace("Method publishAction(PatternRule rule, RuleMatch match) called");
		LOGGER.info("Trying to publish irongpm action.");

		Identifier matchedIdentifier = createMatchedIdentifier(match);
		if (matchedIdentifier == null) {
			LOGGER.warn("Could not create matched identifier for rule: "
					+ rule.getId());
			return;
		}

		Identifier publishVertexIdentifier =
				createPatternVertexIdentifier(rule.getPattern().getPublishVertex(), rule.getId());

		PublishRequest request = Requests.createPublishReq();
		PublishUpdate publishUpdate = Requests.createPublishUpdate();
		publishUpdate.setIdentifier1(publishVertexIdentifier);
		publishUpdate.setIdentifier2(matchedIdentifier);
		publishUpdate.setLifeTime(MetadataLifetime.session);

		String timestamp = match.getResultGraph().getLastUpdated().toString();
		String vendorSpecificMetadataXml = "<"
				+ METADATA_NAMESPACE_PREFIX + ":" + PUBLISH_VERTEX_TO_MATCHED_IDENTIFIER_LINK + " "
				+ "ifmap-cardinality=\"singleValue\" "
				+ "xmlns:" + METADATA_NAMESPACE_PREFIX + "=\"" + POLICY_METADATA_NS_URI + "\">"
				+ "<timestamp>" + timestamp + "</timestamp>"
				+ "</" + METADATA_NAMESPACE_PREFIX + ":" + PUBLISH_VERTEX_TO_MATCHED_IDENTIFIER_LINK + ">";

		Document publishVertexToMatchedIdentifierLinkMetadata = mVendorSpecificMetadataFactory
				.createMetadata(vendorSpecificMetadataXml);
		publishUpdate.addMetadata(publishVertexToMatchedIdentifierLinkMetadata);

		request.addPublishElement(publishUpdate);

		IfmapPublishUtil.publish(request);
	}

	private static PublishElement createDeviceToPolicy(Identifier startDeviceIdentifier,
			Identifier ironGpmPolicyIdentifier) {
		LOGGER.trace(
				"Method createDeviceToPolicy(Identifier startDeviceIdentifier, Identifier ironGpmPolicyIdentifier) called");

		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(startDeviceIdentifier);
		result.setIdentifier2(ironGpmPolicyIdentifier);

		Document deviceToPolicyLinkMetadata = mMetadataFactory.create(DEVICE_TO_POLICY_LINK, POLICY_QUALIFIED_NAME,
				POLICY_METADATA_NS_URI, Cardinality.singleValue);
		result.addMetadata(deviceToPolicyLinkMetadata);

		return result;
	}

	private static PublishElement createPolicyToRule(Identifier ironGpmPolicyIdentifier,
			Identifier ruleStartIdentifier) {
		LOGGER.trace(
				"Method createPolicyToRule(Identifier ironGpmPolicyIdentifier, Identifier ruleStartIdentifier) called");

		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(ironGpmPolicyIdentifier);
		result.setIdentifier2(ruleStartIdentifier);

		Document policyToRuleLinkMetadata = mMetadataFactory.create(POLICY_TO_RULE_LINK, POLICY_QUALIFIED_NAME,
				POLICY_METADATA_NS_URI, Cardinality.singleValue);
		result.addMetadata(policyToRuleLinkMetadata);

		return result;
	}

	private static PublishElement createRuleToFirstPatternVertex(Identifier ruleStartIdentifier,
			Identifier firstPatternVertexIdentifier) {
		LOGGER.trace(
				"Method createRuleToFirstPatternVertex(Identifier ruleStartIdentifier, Identifier firstPatternVertexIdentifier) called");

		PublishUpdate result = Requests.createPublishUpdate();
		result.setIdentifier1(ruleStartIdentifier);
		result.setIdentifier2(firstPatternVertexIdentifier);
		result.setLifeTime(MetadataLifetime.session);

		Document ruleToFirstPatternVertexLinkMetadata =
				mMetadataFactory.create(RULE_TO_FIRST_PATTERN_VERTEX_LINK, POLICY_QUALIFIED_NAME,
						POLICY_METADATA_NS_URI, Cardinality.singleValue);
		result.addMetadata(ruleToFirstPatternVertexLinkMetadata);

		return result;
	}

	private static Identifier createPolicyIdentifier(String ruleLoaderName) throws MarshalException {
		LOGGER.trace("Method createPolicyIdentifier(String ruleLoaderName) called");

		String policyIdentifierDocument = "<"
				+ IDENTIFIER_NAMESPACE_PREFIX + ":" + POLICY_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" >"
				+ "<name>" + ruleLoaderName + "</name>"
				+ "</" + IDENTIFIER_NAMESPACE_PREFIX + ":" + POLICY_IDENTIFIER + ">";

		return Identifiers.createExtendedIdentity(policyIdentifierDocument);
	}

	private static Identifier createRuleStartIdentifier(RuleWrapper rule) throws MarshalException {
		LOGGER.trace("Method createRuleStartIdentifier(RuleWrapper rule) called");

		String name = rule.getName();
		long id = rule.getId();
		String description = rule.getDescription();
		String recommendation = rule.getRecommendation();

		String ruleIdentifierDocument = "<"
				+ IDENTIFIER_NAMESPACE_PREFIX + ":" + RULE_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" >"
				+ "<name>" + name + "</name>"
				+ "<id>" + id + "</id>"
				+ "<description>" + description + "</description>"
				+ "<recommendation>" + recommendation + "</recommendation>"
				+ "</" + IDENTIFIER_NAMESPACE_PREFIX + ":" + RULE_IDENTIFIER + ">";

		return Identifiers.createExtendedIdentity(ruleIdentifierDocument);
	}

	private static Identifier createPatternVertexIdentifier(PatternVertex vertex, long ruleId) throws MarshalException {
		LOGGER.trace("Method createPatternVertexIdentifier(PatternVertex vertex, long ruleId) called");

		String vertexDocument = "<"
				+ IDENTIFIER_NAMESPACE_PREFIX + ":" + PATTERN_VERTEX_IDENTIFIER + " "
				+ "administrative-domain=\"" + POLICY_ADMINSTRATIVE_DOMAIN + ":" + ruleId + "\" "
				+ "xmlns:" + IDENTIFIER_NAMESPACE_PREFIX + "=\"" + POLICY_IDENTIFIER_NS_URI + "\" >"
				+ "<typename>" + vertex.getTypeName() + "</typename>"
				+ "<properties>" + createPropertyString(vertex) + "</properties>"
				+ "</" + IDENTIFIER_NAMESPACE_PREFIX + ":" + PATTERN_VERTEX_IDENTIFIER + ">";

		return Identifiers.createExtendedIdentity(vertexDocument);
	}

	private static Identifier createMatchedIdentifier(RuleMatch match) throws MarshalException {
		LOGGER.trace("Method createMatchedIdentifier(RuleMatch match) called");

		Identifier result = null;
		IfmapVertex matchedVertex = match.getPublishVertex();

		String typeName = matchedVertex.getTypeName();
		String rawData = matchedVertex.getRawData();

		LOGGER.trace("MatchedVertex ("
				+ typeName + "): " + rawData);

		// TODO will not support ALL possible identifier types ... e.g. IPv6
		switch (typeName) {
			case IfmapStrings.ACCESS_REQUEST_EL_NAME:
				result = Identifiers.createAr(matchedVertex.valueFor("/access-request[@name]"));
				break;
			case IfmapStrings.DEVICE_EL_NAME:
				result = Identifiers.createDev(matchedVertex.valueFor("/device/name"));
				break;
			case IfmapStrings.IP_ADDRESS_EL_NAME:
				result = Identifiers.createIp4(matchedVertex.valueFor("/ip-address[@value]"));
				break;
			case IfmapStrings.MAC_ADDRESS_EL_NAME:
				result = Identifiers.createMac(matchedVertex.valueFor("/mac-address[@value]"));
				break;
			case IfmapStrings.IDENTITY_EL_NAME:
				if (matchedVertex.isExtendedIdentifier()) {
					result = Identifiers.createExtendedIdentity(rawData);
				} else {
					String name = matchedVertex.valueFor("/identity[@name]");
					String type = matchedVertex.valueFor("/identity[@type]");
					result = Identifiers.createIdentity(IdentityType.valueOf(type), name);
				}
				break;
			default:
				break;
		}

		return result;
	}

	private static List<PublishElement> createPublishElementsFromRuleWrappers(List<RuleWrapper> rules,
			Identifier ironGpmPolicyIdentifier) throws MarshalException {
		LOGGER.trace(
				"Method createPublishElementsFromRuleWrappers(List<RuleWrapper> rules, Identifier ironGpmPolicyIdentifier) called");

		List<PublishElement> result = new ArrayList<>();

		for (RuleWrapper rule : rules) {
			Identifier ruleStartIdentifier = createRuleStartIdentifier(rule);
			PublishElement policyToRule = createPolicyToRule(ironGpmPolicyIdentifier, ruleStartIdentifier);
			result.add(policyToRule);

			result.addAll(createRuleSubgraph(ruleStartIdentifier, rule));
		}

		return result;
	}

	private static List<PublishElement> createRuleSubgraph(Identifier ruleStartIdentifier, RuleWrapper rule)
			throws MarshalException {
		LOGGER.trace("Method createRuleSubgraph(Identifier ruleStartIdentifier, RuleWrapper rule) called");

		List<PublishElement> result = new ArrayList<>();

		PatternGraph pattern = rule.getPattern();
		PatternVertex publishVertex = pattern.getPublishVertex();

		Identifier firstPatternVertexIdentifier = createPatternVertexIdentifier(publishVertex, rule.getId());

		PublishElement ruleToFirstPatternVertex =
				createRuleToFirstPatternVertex(ruleStartIdentifier, firstPatternVertexIdentifier);
		result.add(ruleToFirstPatternVertex);

		Set<PatternVertex> seen = new HashSet<>();
		traversePatternGraph(publishVertex, seen, pattern, result, rule.getId());

		return result;
	}

	private static void traversePatternGraph(PatternVertex current, Set<PatternVertex> seen, PatternGraph graph,
			List<PublishElement> publishElements, long ruleId) throws MarshalException {
		LOGGER.trace(
				"Method traversePatternGraph(PatternVertex current, Set<PatternVertex> seen, PatternGraph graph, List<PublishElement> publishElements, long ruleId) called");

		Identifier detached;
		Identifier detachedOther;
		List<Document> detachedMetadata;
		Document linkMetadata;

		if (!seen.contains(current)) {
			detached = createPatternVertexIdentifier(current, ruleId);
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
					detachedOther = createPatternVertexIdentifier(other, ruleId);
					publishElements.add(createPublishElementForEdge(detached, detachedOther, linkMetadata));
				} else {
					traversePatternGraph(other, seen, graph, publishElements, ruleId);
				}
			}
		}

	}

	private static PublishElement createPublishElementForEdge(Identifier current, Identifier other,
			Document metadata) {
		LOGGER.trace(
				"Method createPublishElementForEdge(Identifier current, Identifier other, Document metadata) called");

		PublishUpdate result = Requests.createPublishUpdate();
		result.setLifeTime(MetadataLifetime.session);
		result.setIdentifier1(current);
		result.setIdentifier2(other);
		result.addMetadata(metadata);
		return result;
	}

	private static List<PublishElement> createPublishElementForAttachedMetadata(Identifier current,
			List<Document> detachedMetadata) {
		LOGGER.trace(
				"Method createPublishElementForAttachedMetadata(Identifier current, List<Document> detachedMetadata) called");

		List<PublishElement> result = new ArrayList<>();

		for (Document doc : detachedMetadata) {
			PublishUpdate publishUpdate = Requests.createPublishUpdate();
			publishUpdate.setIdentifier1(current);
			publishUpdate.setLifeTime(MetadataLifetime.session);
			publishUpdate.addMetadata(doc);

			result.add(publishUpdate);
		}

		return result;
	}

	private static List<Document> createPatternVertexIdentifierMetadata(PatternVertex vertex) {
		LOGGER.trace("Method createPatternVertexIdentifierMetadata(PatternVertex vertex) called");

		List<Document> result = new ArrayList<>();
		String cardinality;
		String vendorSpecificMetadataXml;
		Document metadataDocument;

		for (PatternMetadata metadata : vertex.getMetadata()) {
			cardinality = (metadata.isSingleValue() == true ? "singleValue" : "multiValue");

			vendorSpecificMetadataXml = "<"
					+ METADATA_NAMESPACE_PREFIX + ":" + PATTERN_IDENTIFIER_METADATA + " "
					+ "ifmap-cardinality=\"" + cardinality + "\" "
					+ "xmlns:" + METADATA_NAMESPACE_PREFIX + "=\"" + POLICY_METADATA_NS_URI + "\">"
					+ "<typename>" + metadata.getTypeName() + "</typename>"
					+ "<properties>" + createPropertyString(metadata) + "</properties>"
					+ "</" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_IDENTIFIER_METADATA + ">";
			metadataDocument = mVendorSpecificMetadataFactory
					.createMetadata(vendorSpecificMetadataXml);

			result.add(metadataDocument);
		}

		return result;
	}

	private static Document createPatternEdgeMetadata(PatternEdge edge) {
		LOGGER.trace("Method createPatternEdgeMetadata(PatternEdge edge)called");

		PatternMetadata metadata = edge.getMetadata();
		String cardinality = (metadata.isSingleValue() == true ? "singleValue" : "multiValue");

		String vendorSpecificMetadataXml = "<"
				+ METADATA_NAMESPACE_PREFIX + ":" + PATTERN_EDGE_LINK + " "
				+ "ifmap-cardinality=\"" + cardinality + "\" "
				+ "xmlns:" + METADATA_NAMESPACE_PREFIX + "=\"" + POLICY_METADATA_NS_URI + "\">"
				+ "<typename>" + metadata.getTypeName() + "</typename>"
				+ "<properties>" + createPropertyString(metadata) + "</properties>"
				+ "</" + METADATA_NAMESPACE_PREFIX + ":" + PATTERN_EDGE_LINK + ">";

		return mVendorSpecificMetadataFactory
				.createMetadata(vendorSpecificMetadataXml);
	}

	private static String createPropertyString(PatternPropable propable) {
		LOGGER.trace("Method createPropertyString(PatternPropable propable) called");

		StringBuilder properties = new StringBuilder();
		List<String> keys = propable.getProperties();
		String key;
		if (keys.size() == 0) {
			return "[]";
		} else {
			for (int i = 0; i < keys.size(); i++) {
				key = keys.get(i);
				properties.append("[");
				properties.append(key);
				properties.append(": ");
				properties.append(propable.valueFor(key));
				properties.append("]");
				if (i < (keys.size()
						- 1)) {
					properties.append(",");
				}
			}
			return properties.toString();
		}
	}
}
