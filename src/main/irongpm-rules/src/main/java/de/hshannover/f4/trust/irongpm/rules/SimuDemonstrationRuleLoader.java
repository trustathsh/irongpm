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
 * This file is part of irongpm-rules, version 0.0.1,
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
package de.hshannover.f4.trust.irongpm.rules;

import java.util.ArrayList;
import java.util.List;

import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.BasicPatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.ExtendedPatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.PatternGraphImpl;
import de.hshannover.f4.trust.irongpm.algorithm.action.IncidentAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.PrintRecommendationAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.PublishEventAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.UnexpectedBehaviorAction;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.RuleLoader;

/**
 * Example implementation of a rule-loader for the irongraph component. As a first step, the only way to create a rule
 * is to manually define the pattern graph and set properties in java code.
 * 
 * @author Leonard Renners
 * 
 */
public class SimuDemonstrationRuleLoader implements RuleLoader {

	private static RuleAction incident = new IncidentAction();
	private static RuleAction publishUnexpectedBehavior = new UnexpectedBehaviorAction();
	private static RuleAction publishEvent = new PublishEventAction();
	private static RuleAction print = new PrintRecommendationAction();

	@Override
	public List<PatternRule> loadRules() {
		//Prepare result list
		ArrayList<PatternRule> result = new ArrayList<>();

//		//Prepare Rules and add them to the ruleSet
		result.add(prepareExampleRule1());
//		result.add(prepareExampleRule2());
//		result.add(prepareExampleRule3());
//		result.add(prepareRSARule());
		result.add(prepareSimuRule());

		//Eventually return all "loaded" rules
		return result;
	}

	private PatternRule prepareExampleRule1() {
		//Build first example rule (device--device-ip--ip)
		PatternGraphImpl ruleGraph = new PatternGraphImpl();

		//Vertices/Identifier
		PatternVertex dev = new BasicPatternVertex("device");
		PatternVertex ip = new BasicPatternVertex("ip-address");
		ip.addProperty("/ip-address[@type]", "IPv4", false, false);
		ip.addProperty("/ip-address[@value]", "ip", false, true);

		//Metadata
		PatternMetadata devIpMeta = new BasicPatternMetadata("device-ip");
		devIpMeta.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue", false, false);

		ruleGraph.addVertex(dev);
		ruleGraph.addVertex(ip);
		PatternEdge devIpEdge = new BasicPatternEdge(dev, ip, devIpMeta);
		ruleGraph.addEdge(dev, ip, devIpEdge);
		ruleGraph.setPublishVertex(ip);

		//Build rule
		BasicPatternRule rule;
		rule = new BasicPatternRule(ruleGraph, "Rule 1", "Description 1", "Please check IP: $ip$");
		rule.addAction(publishUnexpectedBehavior);

		return rule;
	}

	private PatternRule prepareExampleRule2() {
		//Second rule - same thing (device--device-ip-ip), but device name may not be "bronko"
		PatternGraphImpl r2g = new PatternGraphImpl();

		PatternVertex dev2 = new BasicPatternVertex("device");
		dev2.addProperty("/device/name", "bronko", true, false);

		PatternVertex ip2 = new BasicPatternVertex("ip-address");
		ip2.addProperty("/ip-address[@type]", "IPv4", false, false);

		PatternMetadata devIpMeta2 = new BasicPatternMetadata("device-ip");
		devIpMeta2.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue", false, false);

		r2g.addVertex(dev2);
		r2g.addVertex(ip2);

		PatternEdge devIpEdge2 = new BasicPatternEdge(dev2, ip2, devIpMeta2);

		r2g.addEdge(dev2, ip2, devIpEdge2);
		r2g.setPublishVertex(ip2);
		BasicPatternRule r2 = new BasicPatternRule(r2g, "Rule 2", "Description 2", "do something");
		r2.addAction(incident);

		return r2;
	}

	private PatternRule prepareExampleRule3() {
		//3rd example: (ip--access-request-ar), with ar:name == ip:value (not making sense, but used for testing)
		PatternGraphImpl r3g = new PatternGraphImpl();

		PatternVertex ip3 = new BasicPatternVertex("ip-address");
		ip3.addProperty("/ip-address[@type]", "IPv4", false, false);
		ip3.addProperty("/ip-address[@value]", "XXX", false, true);

		PatternVertex ar3 = new BasicPatternVertex("access-request");
		ar3.addProperty("/access-request[@name]", "XXX", false, true);

		PatternMetadata arIpMeta3 = new BasicPatternMetadata("access-request-ip");
		arIpMeta3.addProperty("/meta:device-ip[@ifmap-cardinality]", "singleValue", false, false);

		PatternEdge arIpEdge3 = new BasicPatternEdge(ip3, ar3, arIpMeta3);

		r3g.addVertex(ip3);
		r3g.addVertex(ar3);
		r3g.addEdge(ip3, ar3, arIpEdge3);
		r3g.setPublishVertex(ip3);
		BasicPatternRule r3 = new BasicPatternRule(r3g, "Rule 3", "Description 3",
				"some more recommendations on AR $XXX$");
		r3.addAction(incident);
		return r3;
	}
	
	private PatternRule prepareRSARule() {
		//Rule used for the RSA demonstration in 2015
		//service with implementation and vulnerability
		//user initiating an attack leveraging that vulnerability (attach detected:ref-id == vulnerability:id)
		PatternGraphImpl rsaGraph = new PatternGraphImpl();
		PatternVertex service = new ExtendedPatternVertex("service");
		service.addProperty("/service[@name]", "serviceName", false, true);

		PatternVertex implementation = new ExtendedPatternVertex("implementation");
		PatternVertex vulnerability = new ExtendedPatternVertex("vulnerability");
		vulnerability.addProperty("/vulnerability[@id]", "cve1", false, true);
		PatternVertex attackerIp = new BasicPatternVertex("ip-address");
		attackerIp.addProperty("/ip-address[@value]", "attackerIp", false, true);

		PatternMetadata serviceImplMeta = new BasicPatternMetadata("service-implementation");
		PatternMetadata implVulnMeta = new BasicPatternMetadata("implementation-vulnerability");
		PatternMetadata attackDetectedMeta = new BasicPatternMetadata("attack-detected");
		attackDetectedMeta.addProperty("/simu:attack-detected/simu:ref-id", "cve", false, true);

		PatternEdge serviceImplEdge = new BasicPatternEdge(service, implementation, serviceImplMeta);
		PatternEdge ImplVulnEdge = new BasicPatternEdge(implementation, vulnerability, implVulnMeta);
		PatternEdge AttackDetectedEdge = new BasicPatternEdge(attackerIp, service, attackDetectedMeta);

		rsaGraph.addVertex(service);
		rsaGraph.addVertex(implementation);
		rsaGraph.addVertex(vulnerability);
		rsaGraph.addVertex(attackerIp);
		rsaGraph.setPublishVertex(attackerIp);
		rsaGraph.addEdge(service, implementation, serviceImplEdge);
		rsaGraph.addEdge(implementation, vulnerability, ImplVulnEdge);
		rsaGraph.addEdge(attackerIp, service, AttackDetectedEdge);
		BasicPatternRule rsa = new BasicPatternRule(
				rsaGraph,
				"Rule RSA",
				"attack-detected on vulnerable service",
				"Check if $serviceName$ was affected by the attack using vulnerability $cve$ and examine quarantined attacker from $attackerIp$ !");
		rsa.addAction(print);
		rsa.addAction(incident);
		rsa.addAction(publishEvent);
		
		return rsa;
	}
	
	private PatternRule prepareSimuRule() {
		//Rule used for the SIMU demonstrator in 2015
		//service with implementation and vulnerability
		//user initiating an attack leveraging that vulnerability (attach detected:ref-id == vulnerability:id)
		PatternGraphImpl simuGraph = new PatternGraphImpl();
		PatternVertex service = new ExtendedPatternVertex("service");
		service.addProperty("/service[@name]", "serviceName", false, true);
		PatternVertex serviceIp = new ExtendedPatternVertex("ip-address");
		PatternVertex serviceDevice = new ExtendedPatternVertex("device");

		PatternVertex implementation = new ExtendedPatternVertex("implementation");
		PatternVertex attackerIp = new BasicPatternVertex("ip-address");
		attackerIp.addProperty("/ip-address[@value]", "attackerIp", false, true);
		PatternVertex attackerMacAddress = new BasicPatternVertex("mac-address");
		PatternVertex attackerAccessRequest = new BasicPatternVertex("access-request");
		PatternVertex attackerIdentity = new BasicPatternVertex("identity");

		PatternMetadata serviceImplMeta = new BasicPatternMetadata("service-implementation");
		PatternMetadata serviceIpMeta = new BasicPatternMetadata("service-ip");
		PatternMetadata serviceIpDeviceMeta = new BasicPatternMetadata("device-ip");
		PatternMetadata attackDetectedMeta = new BasicPatternMetadata("attack-detected");
		attackDetectedMeta.addProperty("/simu:attack-detected/simu:ref-id", "cve1", false, true);
		PatternMetadata ipMac = new BasicPatternMetadata("ip-mac");
		PatternMetadata accessRequestMac = new BasicPatternMetadata("access-request-mac");
		PatternMetadata authAs = new BasicPatternMetadata("authenticated-as");
		PatternMetadata eventMeta = new BasicPatternMetadata("event");
		eventMeta.addProperty("/event/type", "cve", false, false);
		eventMeta.addProperty("/event/vulnerability-uri", "cve1", false, true);

		PatternEdge serviceImplEdge = new BasicPatternEdge(service, implementation, serviceImplMeta);
		PatternEdge serviceIpEdge = new BasicPatternEdge(service, serviceIp, serviceIpMeta);
		PatternEdge serviceIpDeviceEdge = new BasicPatternEdge(serviceIp, serviceDevice, serviceIpDeviceMeta);
		PatternEdge AttackDetectedEdge = new BasicPatternEdge(attackerIp, service, attackDetectedMeta);
		PatternEdge ipMacEdge = new BasicPatternEdge(attackerIp, attackerMacAddress, ipMac);
		PatternEdge accessRequestMacEdge = new BasicPatternEdge(attackerAccessRequest, attackerMacAddress, accessRequestMac);
		PatternEdge authAsEdge = new BasicPatternEdge(attackerAccessRequest, attackerIdentity, authAs);
		
		serviceIp.addMetadata(eventMeta);
		
		simuGraph.addVertex(service);
		simuGraph.addVertex(serviceIp);
		simuGraph.addVertex(serviceDevice);
		simuGraph.addVertex(implementation);
		simuGraph.addVertex(attackerIp);
		simuGraph.addVertex(attackerMacAddress);
		simuGraph.addVertex(attackerAccessRequest);
		simuGraph.addVertex(attackerIdentity);
		simuGraph.setPublishVertex(attackerIp);
		simuGraph.addEdge(service, implementation, serviceImplEdge);
		simuGraph.addEdge(service, serviceIp, serviceIpEdge);
		simuGraph.addEdge(serviceIp, serviceDevice, serviceIpDeviceEdge);
		simuGraph.addEdge(attackerIp, service, AttackDetectedEdge);
		simuGraph.addEdge(attackerIp, attackerMacAddress, ipMacEdge);
		simuGraph.addEdge(attackerAccessRequest, attackerMacAddress, accessRequestMacEdge);
		simuGraph.addEdge(attackerAccessRequest, attackerIdentity, authAsEdge);
		BasicPatternRule simu = new BasicPatternRule(
				simuGraph,
				"Rule SIMU",
				"attack-detected on vulnerable service",
				"Check if $serviceName$ was affected by the attack using vulnerability $cve1$ and examine quarantined attacker from $attackerIp$ !");
		simu.addAction(print);
		simu.addAction(incident);
		simu.addAction(publishUnexpectedBehavior);
		
		return simu;
		
	}
}
