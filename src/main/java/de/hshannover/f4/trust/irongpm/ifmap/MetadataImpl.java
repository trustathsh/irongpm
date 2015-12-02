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
package de.hshannover.f4.trust.irongpm.ifmap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;

/**
 * 
 * Represents an IF-MAP metadatum.
 * 
 * @author Leonard Renners
 * 
 */
public class MetadataImpl implements Metadata {
	private String mTypeName;
	private Map<String, String> mProperties;
	private boolean mIsSingleValue;
	private long mPublishTimestamp;
	private String mRawData;

	private MetadataImpl() {
		mProperties = new HashMap<String, String>();
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The ype of the metadata.
	 * @param properties
	 *            The properties of the metadata.
	 * @param rawData
	 *            The raw XML String.
	 */
	public MetadataImpl(String type, Map<String, String> properties, String rawData) {
		mIsSingleValue = true;
		mPublishTimestamp = 0;
		mTypeName = type;
		mProperties = new HashMap<>(properties);
		for (String prop : mProperties.keySet()) {
			if (prop.endsWith("@ifmap-cardinality]")) {
				mIsSingleValue = valueFor(prop).equalsIgnoreCase("singleValue") ? true : false;
			}
			if (prop.endsWith("@ifmap-timestamp]")) {
				String time = valueFor(prop);
				Calendar cal = DatatypeConverter.parseDateTime(time);
				long unixTime = cal.getTime().getTime();
				mPublishTimestamp = unixTime;
			}
		}
		mRawData = rawData;
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The ype of the metadata.
	 */
	public MetadataImpl(String type) {
		mTypeName = type;
		mPublishTimestamp = 0;
		mProperties = new HashMap<>();
		mRawData = null;
		mIsSingleValue = true;
	}

	/**
	 * Copy constructor. Creates a new instance on the basis of an existing.
	 * 
	 * @param m
	 *            The old metadata
	 */
	public MetadataImpl(Metadata m) {
		this();
		mTypeName = m.getTypeName();
		mPublishTimestamp = m.getPublishTimestamp();
		mRawData = m.getRawData();
		mIsSingleValue = m.isSingleValue();
		for (String key : m.getProperties()) {
			addProperty(key, m.valueFor(key));
		}
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
		if (key.endsWith("@ifmap-cardinality]")) {
			mIsSingleValue = value.equalsIgnoreCase("singleValue") ? true : false;
		}
		if (key.endsWith("@ifmap-timestamp]")) {
			String time = value;
			Calendar cal = DatatypeConverter.parseDateTime(time);
			long unixTime = cal.getTime().getTime();
			mPublishTimestamp = unixTime;
		}
		mProperties.put(key, value);
	}

	@Override
	public List<String> getProperties() {
		return new ArrayList<>(mProperties.keySet());
	}

	@Override
	public boolean hasProperty(String p) {
		return mProperties.containsKey(p);
	}

	@Override
	public String valueFor(String p) {
		return mProperties.get(p);
	}

	@Override
	public String getTypeName() {
		return mTypeName;
	}

	@Override
	public String getRawData() {
		return mRawData;
	}

	@Override
	public boolean isSingleValue() {
		return mIsSingleValue;
	}

	@Override
	public long getPublishTimestamp() {
		return mPublishTimestamp;
	}

	@Override
	public String toString() {
		return mTypeName + mProperties;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Metadata)) {
			return false;
		}
		Metadata other = (Metadata) o;
		if (!getTypeName().equals(other.getTypeName())) {
			return false;
		}
		if (isSingleValue() != other.isSingleValue()) {
			return false;
		}
		if (getProperties().size() != other.getProperties().size()) {
			return false;
		}
		for (String property : getProperties()) {
			String value = valueFor(property);
			if (value == null) {
				if (!(other.valueFor(property) == null)) {
					return false;
				}
			} else {
				if (!valueFor(property).equals(other.valueFor(property))) {
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
			result = prime * result + key.hashCode() + valueFor(key).hashCode();
		}
		if (mRawData != null) {
			result = prime * result + mRawData.hashCode();
		}
		return result;
	}
}
