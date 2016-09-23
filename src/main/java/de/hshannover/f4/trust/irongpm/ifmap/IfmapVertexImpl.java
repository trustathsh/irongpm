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
package de.hshannover.f4.trust.irongpm.ifmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;

/**
 * Representation of an IF-MAP identifier.
 * 
 * @author Leonard Renners
 * 
 */
public class IfmapVertexImpl implements IfmapVertex {

	private String mTypename;
	private Map<String, String> mProperties;
	private String mRawData;
	private List<Metadata> mMetadata = new ArrayList<Metadata>();

	private IfmapVertexImpl() {
		mProperties = new HashMap<>();
	}

	/**
	 * Constructor.
	 * 
	 * @param typename
	 *            Type of the identifier
	 */
	public IfmapVertexImpl(String typename) {
		this();
		mTypename = typename;
		mRawData = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param typename
	 *            Type of the identifier
	 * @param properties
	 *            Properties of the identifier
	 * @param rawData
	 *            The raw XML String
	 */
	public IfmapVertexImpl(String typename, Map<String, String> properties, String rawData) {
		this();
		mTypename = typename;
		mRawData = rawData;
		for (String key : properties.keySet()) {
			addProperty(key, properties.get(key));
		}
	}

	/**
	 * Copy constructor. Creates a new instance on the basis of an existing.
	 * 
	 * @param id
	 *            The old identifier
	 */
	public IfmapVertexImpl(IfmapVertex id) {
		this();
		mTypename = id.getTypeName();
		mRawData = id.getRawData();
		for (String key : id.getProperties()) {
			addProperty(key, id.valueFor(key));
		}
	}

	@Override
	public boolean isExtendedIdentifier() {
		if (!mTypename.equals("identity")) {
			return false;
		}
		if (mProperties.containsKey("/identity[@type]")) {
			if (mProperties.get("/identity[@type]").equals("other")) {
				if (mProperties.containsKey("/identity[@other-type-definition]")) {
					if (mProperties.get("/identity[@other-type-definition]").equals("extended")) {
						return true;
					}
				}

			}
		}
		return false;
	}

	/**
	 * Adds a Metadatum to the identifier.
	 * 
	 * @see java.util.List#add
	 * @param meta
	 *            The metadata to add
	 * @return Whether the operation was successful
	 */
	public boolean addMetadata(Metadata meta) {
		return mMetadata.add(new MetadataImpl(meta));
	}

	@Override
	public List<String> getProperties() {
		return new ArrayList<>(mProperties.keySet());
	}

	/**
	 * Removes a Metadatum from the identifier.
	 * 
	 * @param meta
	 *            The metadata to remove
	 * @return Whether the removal was successfull
	 */
	public boolean removeMetadata(Metadata meta) {
		return mMetadata.remove(meta);
	}

	/**
	 * Removes multiple Metadata from the identifier.
	 * 
	 * @param metaList
	 *            The metadata to remove
	 * @return Whether the removal was successfull
	 */
	public boolean removeMetadata(List<Metadata> metaList) {
		return mMetadata.removeAll(metaList);
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
		return mRawData;
	}

	@Override
	public String valueFor(String p) {
		return mProperties.get(p);
	}

	@Override
	public List<Metadata> getMetadata() {
		return new ArrayList<>(mMetadata);
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
		return mTypename + mProperties.toString() + mMetadata;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof IfmapVertex)) {
			return false;
		}
		IfmapVertex other = (IfmapVertex) o;
		if (!getTypeName().equals(other.getTypeName())) {
			return false;
		}
		List<String> myProperties = getProperties();
		if (myProperties.size() != other.getProperties().size()) {
			return false;
		}
		for (String property : myProperties) {
			String myValue = valueFor(property);
			if (myValue == null) {
				if (!(other.valueFor(property) == null)) {
					return false;
				}
			} else {
				if (!myValue.equals(other.valueFor(property))) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + getTypeName().hashCode();
		List<String> keys = new ArrayList<String>(getProperties());
		Collections.sort(keys);
		for (String key : keys) {
			result = prime * result + valueFor(key).hashCode();
		}
		return result;
	}

}
