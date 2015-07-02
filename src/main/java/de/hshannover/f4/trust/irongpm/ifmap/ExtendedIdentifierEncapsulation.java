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
package de.hshannover.f4.trust.irongpm.ifmap;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;
import de.hshannover.f4.trust.irongpm.util.DocumentUtils;
import de.hshannover.f4.trust.irongpm.util.SimpleKeyValueExtractor;

/**
 * Wrapper Class for Extended Identifier (see the IF-MAP specification for more information about extended identifiers).
 * The encapsulation enables direct access to the properties in the nested XML.
 * 
 * @author Leonard Renners
 * 
 */
public class ExtendedIdentifierEncapsulation implements IfmapVertex {

	private String mTypename;
	private Map<String, String> mProperties;
	private IfmapVertex mOriginalVertex;

	private ExtendedIdentifierEncapsulation() {
		mProperties = new HashMap<>();
	}

	/**
	 * @param extendedIdentifier
	 *            The extended Identifier vertex to be encapsulated.
	 */
	public ExtendedIdentifierEncapsulation(IfmapVertex extendedIdentifier) {
		this();
		if (!extendedIdentifier.isExtendedIdentifier()) {
			return;
		}
		mOriginalVertex = extendedIdentifier;
		parseIdentifier(extendedIdentifier);
	}

	private void parseIdentifier(IfmapVertex extendedIdentifier) {
		String extendedIdentifierXml = extendedIdentifier.valueFor("/identity[@name]");
		String deEscapedXml = DocumentUtils.deEscapeXml(extendedIdentifierXml);
		DocumentBuilder builder = null;
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(deEscapedXml)));
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleKeyValueExtractor extractor = new SimpleKeyValueExtractor();
		mTypename = extractor.extractTypename(document);
		mProperties = extractor.extractToKeyValuePairs(document);
	}

	@Override
	public List<String> getProperties() {
		return new ArrayList<>(mProperties.keySet());
	}

	/**
	 * Adds a property value to the vertex
	 * 
	 * @param key
	 *            the key of the property
	 * @param value
	 *            the value of the property
	 */
	public void addProperty(String key, String value) {
		mProperties.put(key, value);
	}

	@Override
	public String getRawData() {
		return mOriginalVertex.getRawData();
	}

	@Override
	public String valueFor(String p) {
		return mProperties.get(p);
	}

	@Override
	public List<Metadata> getMetadata() {
		return mOriginalVertex.getMetadata();
	}

	@Override
	public boolean hasProperty(String p) {
		return mProperties.containsKey(p);
	}

	@Override
	public String getTypeName() {
		return mTypename;
	}

	@Override
	public String toString() {
		return mTypename + mProperties.toString() + mOriginalVertex.getMetadata();
	}

	@Override
	public boolean isExtendedIdentifier() {
		return mOriginalVertex.isExtendedIdentifier();
	}

	public IfmapVertex getOriginalVertex() {
		return mOriginalVertex;
	}

}
