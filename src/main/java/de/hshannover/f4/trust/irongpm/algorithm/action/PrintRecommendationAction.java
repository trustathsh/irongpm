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
package de.hshannover.f4.trust.irongpm.algorithm.action;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.util.ResultUtil;

/**
 * Action Class that creates an Incident as the result of a rule and stores it in the database via hibernate.
 * 
 * @author Leonard Renners
 * 
 */
public class PrintRecommendationAction implements RuleAction {

	private static final Logger LOGGER = Logger.getLogger(PrintRecommendationAction.class);

	/**
	 * Constructor.
	 * 
	 */
	public PrintRecommendationAction() {
	}

	@Override
	public void performAction(PatternRule rule, RuleMatch result) {
		if (rule.getId() == result.getRuleId()) {
			LOGGER.info("Rule " + rule.getId() + " fired. " + "Recommendation: "
					+ ResultUtil.buildRecommendation(rule.getRecommendation(), result));
		} else {
			LOGGER.warn("Failed performing action since rule (" + rule.getId() + ") and result (" + result.getRuleId()
					+ ") id's did not match!");
		}
	}
}
