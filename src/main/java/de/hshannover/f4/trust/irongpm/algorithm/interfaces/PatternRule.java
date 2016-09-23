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
 * This file is part of irongpm, version 0.3.1,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 - 2016 Trust@HsH
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
package de.hshannover.f4.trust.irongpm.algorithm.interfaces;

import java.util.List;

import de.hshannover.f4.trust.irongpm.algorithm.BasicMatchingAlgorithm;
import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.util.ResultUtil;

/**
 * Interface for the rule-class of used by the {@link BasicMatchingAlgorithm}
 * 
 * @author Leonard Renners
 * 
 */
public interface PatternRule {

	/**
	 * @return The {@link PatternGraph} of the rule
	 */
	public PatternGraph getPattern();

	/**
	 * @return The description of the rule
	 */
	public String getDescription();

	/**
	 * 
	 * @return The name of the rule
	 */
	public String getName();

	/**
	 * @return The recommendation of the rule. NOTE: This will return the <i>raw</i> format of the recommendation.
	 *         {@link RuleMatch} and {@link ResultUtil} are to be used for the final output.
	 **/
	public String getRecommendation();

	/**
	 * @return The actions to fire, when the rule matches.
	 */
	public List<RuleAction> getActions();

	/**
	 * @return The ID of the rule
	 */
	public long getId();
}
