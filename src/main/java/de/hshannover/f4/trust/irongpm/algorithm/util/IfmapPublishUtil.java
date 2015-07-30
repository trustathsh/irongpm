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

package de.hshannover.f4.trust.irongpm.algorithm.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.config.CertAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irongpm.IronGpm;

/**
 * 
 * @author Leonard Renners
 * 
 */
public final class IfmapPublishUtil {

	private static StandardIfmapMetadataFactory mMf = IfmapJ.createStandardMetadataFactory();

	private static final Logger LOGGER = Logger.getLogger(IfmapPublishUtil.class);

	private static SSRC mSsrc;
	private static DocumentBuilder mDocumentBuilder;

	private static final String XMLNS = "http://simu-project.de/XMLSchema/1";

	/**
	 * Dead constructor for code convention -> final class because utility class
	 */
	private IfmapPublishUtil() {

	}

	/**
	 * The init method initiates the Ifmap Session and the XML Document Builder
	 */
	public static void init() {
		try {
			Properties properties = IronGpm.getConfig();
			String authMethod = properties.getString("ifmap.auth.method", "basic");
			if (authMethod.equals("basic")) {
				LOGGER.info("Initialisiing Session using basic authentication");
				mSsrc = IfmapJ.createSsrc(new BasicAuthConfig(properties.getString("ifmap.auth.basic.url",
						" https://127.0.0.1:8443"), properties.getString("ifmap.auth.basic.user", "irongpm"),
						properties.getString("ifmap.auth.basic.password", "irongpm"), properties.getString(
								"ifmap.truststore.path", "/irongpm.jks"), properties.getString(
								"ifmap.truststore.password", "irongpm"), properties
								.getBoolean("ifmap.threadsafe", true), properties.getInt(
								"ifmap.initialconnectiontimeout", 120000)));
			} else if (authMethod.equals("cert")) {
				LOGGER.info("Initialisiing Session using cert based authentication");
				mSsrc = IfmapJ.createSsrc(new CertAuthConfig(properties.getString("ifmap.auth.cert.url",
						" https://127.0.0.1:8444"), properties.getString("ifmap.truststore.path",
						"/keystore/irongpm.jks"), properties.getString("ifmap.truststore.password", "irongpm"),
						properties.getString("ifmap.truststore.path", "/irongpm.jks"), properties.getString(
								"ifmap.truststore.password", "irongpm"), properties
								.getBoolean("ifmap.threadsafe", true), properties.getInt(
								"ifmap.initialconnectiontimeout", 120000)));
			} else {
				throw new RuntimeException("Invalid Property entry for auth method type : " + authMethod);
			}
			mSsrc.newSession();
		} catch (Exception e) {
			LOGGER.error("could not connect to ifmap server", e);
			System.exit(1);
		}
		LOGGER.info("Session successfully created");

		LOGGER.debug("Initialising DocumentBuilder");
		try {
			mDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOGGER.debug("DocumentBuilder successfully created");
	}

	/**
	 * Encapsulates the publish and error handling
	 * 
	 * @param req
	 *            the request to publish
	 */
	public static void publish(PublishRequest req) {
		try {
			mSsrc.publish(req);
			LOGGER.debug("Publish successful");
		} catch (IfmapErrorResult e) {
			LOGGER.error("Error while publishing: " + e.getMessage());
		} catch (IfmapException e) {
			LOGGER.error("Error while publishing: " + e.getMessage());
		}
	}

	/**
	 * Creates a W3C Document for a given XML String
	 * 
	 * @param xml
	 *            The xml to parse
	 * @return The corresponding document
	 */
	public static Document createDocument(String xml) {
		try {
			StringReader reader = new StringReader(xml);
			InputSource input = new InputSource(reader);
			return mDocumentBuilder.parse(input);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
}
