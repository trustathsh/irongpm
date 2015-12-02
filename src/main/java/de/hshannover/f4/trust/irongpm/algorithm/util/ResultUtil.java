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
package de.hshannover.f4.trust.irongpm.algorithm.util;

import java.util.StringTokenizer;

import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;

/**
 * Utility class which combines the description of the action and recommendation in the rule with the actual match of a
 * rule, i.e. extracting relevant information according to the named properties.
 * 
 * @author Leonard Renners
 * 
 */
public final class ResultUtil {

	/**
	 * Cause Security
	 */
	private ResultUtil() {

	}

	/**
	 * Replaces the "placeholders"
	 * 
	 * @param rawRecommendation
	 *            The raw recommendation with placeholders
	 * @param result
	 *            The matching result of a rule firing
	 * @return The combined recommendation with replaced properties (if possible)
	 */
	public static String buildRecommendation(String rawRecommendation, RuleMatch result) {
		String recommendation = replacePlaceHolders(rawRecommendation, result);
		return recommendation;
	}

	private static String replacePlaceHolders(String raw, RuleMatch result) {
		StringBuffer str = new StringBuffer(raw.length());
		StringTokenizer tokenizer = new StringTokenizer(raw);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("$") && token.endsWith("$")) {
				String replace = result.getNamedProperties().get(token.substring(1, token.length() - 1));
				str.append(replace != null ? replace : token);
			} else {
				str.append(token);
			}
			if (tokenizer.hasMoreTokens()) {
				str.append(" ");
			}
		}
		return str.toString();
	}

}
