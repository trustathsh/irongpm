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
package de.hshannover.f4.trust.irongpm.algorithm;

import java.util.ArrayList;
import java.util.List;

import de.hshannover.f4.trust.irongpm.algorithm.action.RuleAction;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternGraph;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;

/**
 * Representing one rule in form of a pattern and a unique identifier
 * 
 * @author Leonard Renners
 * 
 */
public class BasicPatternRule implements PatternRule {

	private long mId;

	private PatternGraph mPattern;
	private String mDescription;
	private String mName;
	private String mRecommendation;
	private List<RuleAction> mActions;

	private static long currentNumber = 1;

	/**
	 * Constructor.
	 */
	public BasicPatternRule() {
		super();
		// FIXME: Global id-scheme? Maybe get the ID from "outside"
		mId = currentNumber++;
		mActions = new ArrayList<>();
	}

	/**
	 * Constructor.
	 */
	public BasicPatternRule(PatternGraph pattern) {
		this();
		setPattern(pattern);
	}

	/**
	 * Constructor.
	 */
	public BasicPatternRule(PatternGraph pattern, String name, String description, String recommendation) {
		this();
		setPattern(pattern);
		setDescription(description);
		setName(name);
		setRecommendation(recommendation);
	}

	@Override
	public long getId() {
		return mId;
	}

	@Override
	public PatternGraph getPattern() {
		return mPattern;
	}

	public void setPattern(PatternGraph pattern) {
		this.mPattern = pattern;
	}

	@Override
	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	@Override
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	@Override
	public String getRecommendation() {
		return mRecommendation;
	}

	public void setRecommendation(String recommendation) {
		this.mRecommendation = recommendation;
	}

	@Override
	public List<RuleAction> getActions() {
		return new ArrayList<>(mActions);
	}

	/**
	 * Removes an {@link RuleAction} to the rule
	 * 
	 * @param action
	 */
	public void removeAction(RuleAction action) {
		mActions.remove(action);
	}

	/**
	 * Adds another {@link RuleAction} to the rule
	 * 
	 * @param action
	 *            the action
	 */
	public void addAction(RuleAction action) {
		mActions.add(action);
	}

}
