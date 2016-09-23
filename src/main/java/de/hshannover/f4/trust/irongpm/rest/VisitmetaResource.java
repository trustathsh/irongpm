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
package de.hshannover.f4.trust.irongpm.rest;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import de.hshannover.f4.trust.irongpm.Pulldozer;

/**
 * 
 * @author Leonard Renners
 * 
 */
public class VisitmetaResource {
	private boolean mRawXml;
	private String mBaseUrl;
	private static final Logger LOGGER = Logger.getLogger(Pulldozer.class);

	/**
	 * Constructor with default url: <b>http://localhost:8000/default/graph/</b>
	 */
	public VisitmetaResource() {
		this("http://localhost:8000/default/graph/");
	}

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            URL of the base REST interface of the visitmeta dataservice
	 * @param rawXml
	 *            Whether rawXML should be requested or not
	 */
	public VisitmetaResource(String resource, boolean rawXml) {
		mBaseUrl = resource;
		mRawXml = rawXml;

	}

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            URL of the base REST interface of the visitmeta dataservice
	 */
	public VisitmetaResource(String resource) {
		this(resource, true);
	}

	/**
	 * Requests the REST interface for the given path, e.g. "current" or "changes"
	 * 
	 * @param path
	 *            The path for the HTTP request
	 * @return The JSON response string
	 */
	public String get(String path) {
		String json = null;
		boolean connected = true;
		do {
			try {
				URLConnection conn = new URL(mBaseUrl + path + "?rawData=" + mRawXml).openConnection();
				conn.setConnectTimeout(2000);
				conn.connect();
				JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
				JsonParser parser = new JsonParser();
				JsonElement rootElement = parser.parse(reader);
				json = rootElement.toString();
				if (!connected) {
					LOGGER.info("VisitmetaResource connection (re-)established");
				}
				connected = true;
			} catch (Exception e) {
				try {
					LOGGER.warn("VisitmetaResource connection failed on " + mBaseUrl
							+ ". Trying again in 5 seconds. Reason: [" + e + "]");
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				connected = false;
			}
		} while (!connected);
		return json;
	}
}
