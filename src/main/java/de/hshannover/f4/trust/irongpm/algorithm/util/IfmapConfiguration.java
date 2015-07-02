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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class loads the configuration file from the file system and provides a set of constants and a getter method to
 * access these values.
 * 
 * @author Leonard Renners
 * 
 */

public final class IfmapConfiguration {

	private static final Logger LOGGER = Logger.getLogger(IfmapConfiguration.class.getName());

	/**
	 * The path to the configuration file.
	 */

	private static final String CONFIG_FILE = "/irongpm.properties";

	private static Properties mProperties;

	// begin configuration parameter -------------------------------------------

	private static final String IFMAP_AUTH_METHOD = "ifmap.server.auth.method";
	private static final String IFMAP_URL_BASIC = "ifmap.server.url.basic";
	private static final String IFMAP_URL_CERT = "ifmap.server.url.cert";
	private static final String IFMAP_BASIC_USER = "ifmap.server.auth.basic.user";
	private static final String IFMAP_BASIC_PASSWORD = "ifmap.server.auth.basic.password";

	private static final String KEYSTORE_PATH = "keystore.path";
	private static final String KEYSTORE_PASSWORD = "keystore.password";

	private static final String IFMAP_KEEPALIVE = "irongpm.ifmap.interval";

	/**
	 * Death constructor for code convention -> final class because utility class
	 */
	private IfmapConfiguration() {
	}

	/**
	 * Loads the configuration file. Every time this method is called the file is read again.
	 */
	public static void init() {
		LOGGER.debug("reading " + CONFIG_FILE + " ...");

		mProperties = new Properties();
		InputStream in = IfmapConfiguration.class.getResourceAsStream(CONFIG_FILE);
		loadPropertiesfromFile(in, mProperties);
		LOGGER.debug("config successfully parsed");
	}

	/**
	 * Loads the configuration file. Every time this method is called the file is read again.
	 * 
	 * @param in
	 *            Streamreader
	 * @param props
	 *            properties
	 * 
	 */
	private static void loadPropertiesfromFile(InputStream in, Properties props) {

		try {
			props.load(in);
		} catch (FileNotFoundException e) {
			LOGGER.error("could not find " + CONFIG_FILE);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			LOGGER.error("error while reading " + CONFIG_FILE);
			throw new RuntimeException(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.warn("error while closing properties inputstream: " + e);
			}
		}
	}

	/**
	 * Returns the value assigned to the given key. If the configuration has not been loaded jet this method loads it.
	 * 
	 * @param key
	 * @return the value assigned to key or null if it is none
	 */
	private static String get(String key) {
		if (mProperties == null) {
			init();
		}
		return mProperties.getProperty(key);
	}

	/**
	 * Getter for the ifmapAuthMethod property.
	 * 
	 * @return property string
	 */
	public static String ifmapAuthMethod() {
		return get(IFMAP_AUTH_METHOD);
	}

	/**
	 * Getter for the ifmapUrlBasic property.
	 * 
	 * @return property string
	 */
	public static String ifmapUrlBasic() {
		return get(IFMAP_URL_BASIC);
	}

	/**
	 * Getter for the ifmapUrlCert property.
	 * 
	 * @return property string
	 */
	public static String ifmapUrlCert() {
		return get(IFMAP_URL_CERT);
	}

	/**
	 * Getter for the ifmapBasicUser property.
	 * 
	 * @return property string
	 */
	public static String ifmapBasicUser() {
		return get(IFMAP_BASIC_USER);
	}

	/**
	 * Getter for the ifmapBasicPassword property.
	 * 
	 * @return property string
	 */
	public static String ifmapBasicPassword() {
		return get(IFMAP_BASIC_PASSWORD);
	}

	/**
	 * Getter for the keyStorePath property.
	 * 
	 * @return property string
	 */
	public static String keyStorePath() {
		return get(KEYSTORE_PATH);
	}

	/**
	 * Getter for the keyStorePassword property.
	 * 
	 * @return property string
	 */
	public static String keyStorePassword() {
		return get(KEYSTORE_PASSWORD);
	}

	/**
	 * Getter for the ifmapKeepalive property.
	 * 
	 * @return property integer
	 */
	public static int ifmapKeepalive() {
		return Integer.parseInt(get(IFMAP_KEEPALIVE));
	}

}
