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
package de.hshannover.f4.trust.irongpm.algorithm.action;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * Abstract Action for the algorithm to publish different information as the result of a matching rule.
 * 
 * @author Leonard Renners
 * 
 */
public abstract class PublishAction implements RuleAction {

	private static final Logger LOGGER = Logger.getLogger(PublishAction.class);

	/**
	 * Converts an IF-MAP vertex to an ifmapj defined {@link Identifier}
	 * 
	 * @param vertex
	 *            the vertex in internal format
	 * @return the corresponding {@link Identifier}
	 */
	protected Identifier convertVertex(IfmapVertex vertex) {
		switch (vertex.getTypeName()) {
			case "device":
				return Identifiers.createDev(vertex.valueFor("/device/name"));
			case "ip-address":
				return Identifiers.createIp4(vertex.valueFor("/ip-address[@value]"));
			case "access-request":
				return Identifiers.createAr(vertex.valueFor("/access-request[@name]"));
			default:
				LOGGER.warn("Pu types than dev, ip and ar are not supported as entry points yet!");
				break;
		}
		return null;
	}
}
