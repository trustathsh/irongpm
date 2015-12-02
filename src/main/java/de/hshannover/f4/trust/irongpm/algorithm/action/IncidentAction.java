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
package de.hshannover.f4.trust.irongpm.algorithm.action;

import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.decoit.simu.incidents.entities.IncidentEntity;
import de.decoit.simu.incidents.enums.IncidentStatus;
import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.util.ResultUtil;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraph;
import de.hshannover.f4.trust.irongpm.ifmap.IfmapGraphImpl;
import de.hshannover.f4.trust.irongpm.rest.IfmapGraphJsonAdapter;
import de.hshannover.f4.trust.irongpm.util.HibernateUtil;

/**
 * Action Class that creates an Incident as the result of a rule and stores it in the database via hibernate.
 * 
 * @author Leonard Renners
 * 
 */
public class IncidentAction implements RuleAction {

	private static final Logger LOGGER = Logger.getLogger(IncidentAction.class);
	private static GsonBuilder gsob = new GsonBuilder();
	private static Gson gson;

	/**
	 * Constructor.
	 * 
	 */
	public IncidentAction() {
		gsob.registerTypeAdapter(IfmapGraphImpl.class, new IfmapGraphJsonAdapter());
		gsob.disableHtmlEscaping();
		gson = gsob.create();
	}

	@Override
	public void performAction(PatternRule rule, RuleMatch result) {
		if (rule.getId() == result.getRuleId()) {
			LOGGER.debug("Performing IncidentAction for rule " + rule.getId());

			Session s = HibernateUtil.getSessionFactory().openSession();
			s.beginTransaction();
			IncidentEntity incident = prepareIncidentEntity(rule, result);
			s.save(incident);
			s.getTransaction().commit();
			LOGGER.debug("Incident creation succesfully.");
		} else {
			LOGGER.warn("Failed performing action since rule (" + rule.getId() + ") and result (" + result.getRuleId()
					+ ") id's did not match!");
		}
	}

	private IncidentEntity prepareIncidentEntity(PatternRule rule, RuleMatch result) {
		IfmapGraph graph = result.getResultGraph();

		IncidentEntity entity = new IncidentEntity();
		entity.setRuleId(rule.getId());
		entity.setStatus(IncidentStatus.New);
		entity.setTimestamp(new Date(graph.getLastUpdated()));
		entity.setDescription(rule.getDescription());
		entity.setRisk(10);
		entity.setRecommendation(ResultUtil.buildRecommendation(rule.getRecommendation(), result));
		entity.setGraph(gson.toJson(graph));
		entity.setName(rule.getName());

		return entity;
	}

}
