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
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irongpm.IronGpm;
import de.hshannover.f4.trust.irongpm.algorithm.RuleMatch;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternRule;
import de.hshannover.f4.trust.irongpm.algorithm.util.IfmapPublishUtil;
import de.hshannover.f4.trust.irongpm.ifmap.PolicyPublisher;

/**
 * Action Class that creates a Metadatum as the result of a rule and publishes it at the map server.
 * 
 * @author Leonard Renners
 * @author Bastian Hellmann
 * 
 */
public class UnexpectedBehaviorAction extends PublishAction {

	private static final Logger LOGGER = Logger.getLogger(UnexpectedBehaviorAction.class);
	private static StandardIfmapMetadataFactory mMf = IfmapJ.createStandardMetadataFactory();
	private static Properties mConfig = IronGpm.getConfig();

	/**
	 * Constructor.
	 * 
	 */
	public UnexpectedBehaviorAction() {
	}

	@Override
	public void performAction(PatternRule rule, RuleMatch result) {
		if (rule.getId() == result.getRuleId()) {
			LOGGER.debug("Performing UnexpectedBehaviorAction for rule: " + rule.getId());
			if (result.getPublishVertex() == null) {
				LOGGER.warn("Publish not successful, PublishVertex is null for rule " + rule.getId());
				return;
			}

			Identifier id = convertVertex(result.getPublishVertex());
			if (id == null) {
				LOGGER.warn("Publish not successful, converting the PublishVertex of rule " + rule.getId()
						+ " to an ifmapj Identifier failed. Check above for other log entries.");
				return;
			}
			Document updateUnexpectedBehavior = mMf.createUnexpectedBehavior(result.getResultGraph().getLastUpdated()
					.toString(), "GPM", 100, 100, Significance.critical, rule.getDescription());

			PublishRequest update = Requests.createPublishReq();
			update.addPublishElement(Requests.createPublishUpdate(id, updateUnexpectedBehavior));
			IfmapPublishUtil.publish(update);

			boolean isPolicyPublisherEnabled = mConfig.getBoolean("irongpm.publisher.policy.enabled", false);
			if (isPolicyPublisherEnabled) {
				try {
					PolicyPublisher.publishAction(rule, result);
				} catch (IfmapErrorResult | IfmapException e) {
					LOGGER.warn("Error at publishing pattern to matched identifier-link: " + e.getMessage());
				}
			}
		} else {
			LOGGER.warn("Failed performing action since rule (" + rule.getId() + ") and result (" + result.getRuleId()
					+ ") id's did not match!");
		}
	}
}
