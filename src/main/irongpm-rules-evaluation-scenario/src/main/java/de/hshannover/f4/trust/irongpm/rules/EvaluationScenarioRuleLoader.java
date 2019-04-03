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
import de.hshannover.f4.trust.irongpm.algorithm.action.PrintRecommendationAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.action.UnexpectedBehaviorAction;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternEdge;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.RuleLoader;

/**
 * 
 * @author Bastian Hellmann
 * 
 */
public class EvaluationScenarioRuleLoader implements RuleLoader {

	private static RuleAction publishUnexpectedBehavior = new UnexpectedBehaviorAction();
	private static RuleAction print = new PrintRecommendationAction();

	@Override
	public List<PatternRule> loadRules() {
		//Prepare result list
		ArrayList<PatternRule> result = new ArrayList<>();

//		//Prepare Rules and add them to the ruleSet
		result.add(prepareRule1());
//		result.add(prepareRule2());
		result.add(prepareRule3());

		//Eventually return all "loaded" rules
		return result;
	}

	private PatternRule prepareRule1() {
		//user initiating an attack leveraging that vulnerability (attack-detected:ref-id == vulnerability:id)
		PatternGraphImpl graph = new PatternGraphImpl();
		
		PatternVertex service = new ExtendedPatternVertex("service");	
		service.addProperty("/service[@name]", "serviceName", false, true);
		PatternVertex serviceIp = new ExtendedPatternVertex("ip-address");
		serviceIp.addProperty("/ip-address[@value]", "serviceIp", false, true);

		PatternVertex implementation = new ExtendedPatternVertex("implementation");
		PatternVertex vulnerability = new ExtendedPatternVertex("vulnerability");
		vulnerability.addProperty("/vulnerability[@id]", "cve1", false, true);
		PatternVertex attackerIp = new BasicPatternVertex("ip-address");
		attackerIp.addProperty("/ip-address[@value]", "attackerIp", false, true);
		PatternVertex attackerMacAddress = new BasicPatternVertex("mac-address");
		PatternVertex attackerAccessRequest = new BasicPatternVertex("access-request");
		PatternVertex attackerIdentity = new BasicPatternVertex("identity");

		PatternMetadata serviceImplMeta = new BasicPatternMetadata("service-implementation");
		PatternMetadata implVulnMeta = new BasicPatternMetadata("implementation-vulnerability");
		PatternMetadata serviceIpMeta = new BasicPatternMetadata("service-ip");
		PatternMetadata attackDetectedMeta = new BasicPatternMetadata("attack-detected");
		attackDetectedMeta.addProperty("/simu:attack-detected/simu:ref-id", "cve1", false, true);
		PatternMetadata ipMac = new BasicPatternMetadata("ip-mac");
		PatternMetadata accessRequestMac = new BasicPatternMetadata("access-request-mac");
		PatternMetadata authAs = new BasicPatternMetadata("authenticated-as");

		PatternEdge serviceImplEdge = new BasicPatternEdge(service, implementation, serviceImplMeta);
		PatternEdge serviceIpEdge = new BasicPatternEdge(service, serviceIp, serviceIpMeta);
		PatternEdge ImplVulnEdge = new BasicPatternEdge(implementation, vulnerability, implVulnMeta);
		PatternEdge AttackDetectedEdge = new BasicPatternEdge(attackerIp, service, attackDetectedMeta);
		PatternEdge ipMacEdge = new BasicPatternEdge(attackerIp, attackerMacAddress, ipMac);
		PatternEdge accessRequestMacEdge = new BasicPatternEdge(attackerAccessRequest, attackerMacAddress, accessRequestMac);
		PatternEdge authAsEdge = new BasicPatternEdge(attackerAccessRequest, attackerIdentity, authAs);
		
		graph.addVertex(service);
		graph.addVertex(serviceIp);
		graph.addVertex(implementation);
		graph.addVertex(vulnerability);
		graph.addVertex(attackerIp);
		graph.addVertex(attackerMacAddress);
		graph.addVertex(attackerAccessRequest);
		graph.addVertex(attackerIdentity);

		graph.setPublishVertex(attackerIp);
		
		graph.addEdge(service, implementation, serviceImplEdge);
		graph.addEdge(service, serviceIp, serviceIpEdge);
		graph.addEdge(implementation, vulnerability, ImplVulnEdge);
		graph.addEdge(attackerIp, service, AttackDetectedEdge);
		graph.addEdge(attackerIp, attackerMacAddress, ipMacEdge);
		graph.addEdge(attackerAccessRequest, attackerMacAddress, accessRequestMacEdge);
		graph.addEdge(attackerAccessRequest, attackerIdentity, authAsEdge);

		BasicPatternRule rule1 = new BasicPatternRule(
				graph,
				"Rule 1",
				"attack-detected on vulnerable service",
				"Check if $serviceName$ was affected by the attack using vulnerability $cve1$ and examine quarantined attacker from $attackerIp$ !");
				
		rule1.addAction(print);
		rule1.addAction(publishUnexpectedBehavior);
		
		return rule1;
	}
	
	private PatternRule prepareRule2() {
		PatternGraphImpl graph = new PatternGraphImpl();
		
		PatternVertex service = new ExtendedPatternVertex("service");
		service.addProperty("/service[@name]", "serviceName", false, true);
		PatternVertex serviceIp = new ExtendedPatternVertex("ip-address");
		serviceIp.addProperty("/ip-address[@value]", "serviceIp", false, true);

		PatternVertex implementation = new ExtendedPatternVertex("implementation");
		PatternVertex attackerIp = new BasicPatternVertex("ip-address");
		attackerIp.addProperty("/ip-address[@value]", "attackerIp", false, true);
		PatternVertex attackerMacAddress = new BasicPatternVertex("mac-address");
		PatternVertex attackerAccessRequest = new BasicPatternVertex("access-request");
		PatternVertex attackerIdentity = new BasicPatternVertex("identity");

		PatternMetadata serviceImplMeta = new BasicPatternMetadata("service-implementation");
		PatternMetadata serviceIpMeta = new BasicPatternMetadata("service-ip");
		PatternMetadata attackDetectedMeta = new BasicPatternMetadata("attack-detected");
		PatternMetadata ipMac = new BasicPatternMetadata("ip-mac");
		PatternMetadata accessRequestMac = new BasicPatternMetadata("access-request-mac");
		PatternMetadata authAs = new BasicPatternMetadata("authenticated-as");

		PatternEdge serviceImplEdge = new BasicPatternEdge(service, implementation, serviceImplMeta);
		PatternEdge serviceIpEdge = new BasicPatternEdge(service, serviceIp, serviceIpMeta);
		PatternEdge AttackDetectedEdge = new BasicPatternEdge(attackerIp, service, attackDetectedMeta);
		PatternEdge ipMacEdge = new BasicPatternEdge(attackerIp, attackerMacAddress, ipMac);
		PatternEdge accessRequestMacEdge = new BasicPatternEdge(attackerAccessRequest, attackerMacAddress, accessRequestMac);
		PatternEdge authAsEdge = new BasicPatternEdge(attackerAccessRequest, attackerIdentity, authAs);
		
		graph.addVertex(service);
		graph.addVertex(serviceIp);
		graph.addVertex(implementation);
		graph.addVertex(attackerIp);
		graph.addVertex(attackerMacAddress);
		graph.addVertex(attackerAccessRequest);
		graph.addVertex(attackerIdentity);
		
		graph.setPublishVertex(attackerIp);
		
		graph.addEdge(service, implementation, serviceImplEdge);
		graph.addEdge(service, serviceIp, serviceIpEdge);
		graph.addEdge(attackerIp, service, AttackDetectedEdge);
		graph.addEdge(attackerIp, attackerMacAddress, ipMacEdge);
		graph.addEdge(attackerAccessRequest, attackerMacAddress, accessRequestMacEdge);
		graph.addEdge(attackerAccessRequest, attackerIdentity, authAsEdge);

		BasicPatternRule rule2 = new BasicPatternRule(
				graph,
				"Rule 2",
				"attack-detected on service with specific implementation",
				"Check if $serviceName$ was affected by the attack and examine quarantined attacker from $attackerIp$ !");
				
		rule2.addAction(print);
		rule2.addAction(publishUnexpectedBehavior);
		
		return rule2;
	}
	
	private PatternRule prepareRule3() {
		PatternGraphImpl graph = new PatternGraphImpl();
		
		PatternVertex service = new ExtendedPatternVertex("service");
		service.addProperty("/service[@name]", "serviceName", false, true);
		PatternVertex serviceIp = new ExtendedPatternVertex("ip-address");
		serviceIp.addProperty("/ip-address[@value]", "serviceIp", false, true);

		PatternVertex attackerIp = new BasicPatternVertex("ip-address");
		attackerIp.addProperty("/ip-address[@value]", "attackerIp", false, true);
		PatternVertex attackerMacAddress = new BasicPatternVertex("mac-address");
		PatternVertex attackerAccessRequest = new BasicPatternVertex("access-request");
		PatternVertex attackerIdentity = new BasicPatternVertex("identity");

		PatternMetadata serviceIpMeta = new BasicPatternMetadata("service-ip");
		PatternMetadata attackDetectedMeta = new BasicPatternMetadata("attack-detected");
		PatternMetadata ipMac = new BasicPatternMetadata("ip-mac");
		PatternMetadata accessRequestMac = new BasicPatternMetadata("access-request-mac");
		PatternMetadata authAs = new BasicPatternMetadata("authenticated-as");

		PatternEdge serviceIpEdge = new BasicPatternEdge(service, serviceIp, serviceIpMeta);
		PatternEdge AttackDetectedEdge = new BasicPatternEdge(attackerIp, service, attackDetectedMeta);
		PatternEdge ipMacEdge = new BasicPatternEdge(attackerIp, attackerMacAddress, ipMac);
		PatternEdge accessRequestMacEdge = new BasicPatternEdge(attackerAccessRequest, attackerMacAddress, accessRequestMac);
		PatternEdge authAsEdge = new BasicPatternEdge(attackerAccessRequest, attackerIdentity, authAs);
		
		graph.addVertex(service);
		graph.addVertex(serviceIp);
		graph.addVertex(attackerIp);
		graph.addVertex(attackerMacAddress);
		graph.addVertex(attackerAccessRequest);
		graph.addVertex(attackerIdentity);
		
		graph.setPublishVertex(attackerIp);
		
		graph.addEdge(service, serviceIp, serviceIpEdge);
		graph.addEdge(attackerIp, service, AttackDetectedEdge);
		graph.addEdge(attackerIp, attackerMacAddress, ipMacEdge);
		graph.addEdge(attackerAccessRequest, attackerMacAddress, accessRequestMacEdge);
		graph.addEdge(attackerAccessRequest, attackerIdentity, authAsEdge);

		BasicPatternRule rule3 = new BasicPatternRule(
				graph,
//				"Rule 3",
				"Rule 2",
				"attack-detected on service",
				"Check if $serviceName$ was affected by the attack and examine quarantined attacker from $attackerIp$ !");
				
		rule3.addAction(print);
		rule3.addAction(publishUnexpectedBehavior);
		
		return rule3;
	}
}
