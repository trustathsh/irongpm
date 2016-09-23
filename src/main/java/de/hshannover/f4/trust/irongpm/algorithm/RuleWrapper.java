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
package de.hshannover.f4.trust.irongpm.algorithm;

import java.util.List;

import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;

/**
 * Wrapper class to encapsulate ruleID handling transparent from the rule definition.
 * 
 * @author Leonard Renners
 * 
 */
public class RuleWrapper implements PatternRule {

	private PatternRule mRule;
	private long mId;

	/**
	 * Constructor.
	 * 
	 * @param rule
	 *            the original PatternRule
	 */
	public RuleWrapper(PatternRule rule) {
		mRule = rule;
		mId = rule.getId() * 10000;
	}

	@Override
	public PatternGraph getPattern() {
		return mRule.getPattern();
	}

	@Override
	public String getDescription() {
		return mRule.getDescription();
	}

	@Override
	public String getName() {
		return mRule.getName();
	}

	@Override
	public String getRecommendation() {
		return mRule.getRecommendation();
	}

	@Override
	public List<RuleAction> getActions() {
		return mRule.getActions();
	}

	@Override
	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}
}
