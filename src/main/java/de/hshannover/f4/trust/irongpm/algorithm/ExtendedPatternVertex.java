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
package de.hshannover.f4.trust.irongpm.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternMetadata;
import de.hshannover.f4.trust.irongpm.algorithm.interfaces.PatternVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;

/**
 * Vertex implementation for pattern matching. Adds definition of required and prohibited properties and metadata. Also
 * implements equals() methods to enable comparison methods in the algorithm.
 * 
 * @author Leonard Renners
 * 
 */
public class ExtendedPatternVertex implements PatternVertex {

	private String mTypename;
	private Map<String, String> mProperties;
	private List<String> mRestrictedProperties;
	private List<String> mRelatedProperties;
	private List<PatternMetadata> mMetadata;
	private boolean mMatched;
	private IfmapVertex mMatchedVertex;

	private ExtendedPatternVertex() {
		mProperties = new HashMap<>();
		mMetadata = new ArrayList<>();
		mRestrictedProperties = new ArrayList<>();
		mRelatedProperties = new ArrayList<>();
		mMatched = false;
		mMatchedVertex = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param typename
	 *            Type of the identifier
	 */
	public ExtendedPatternVertex(String typename) {
		this();
		mTypename = typename;
	}

	@Override
	public boolean addMetadata(PatternMetadata meta) {
		return mMetadata.add(meta);
	}

	@Override
	public List<String> getProperties() {
		return new ArrayList<>(mProperties.keySet());
	}

	public List<String> getRestrictedProperties() {
		return new ArrayList<>(mRestrictedProperties);
	}

	public List<String> getRelatedProperties() {
		return new ArrayList<>(mRelatedProperties);
	}

	@Override
	public String valueFor(String p) {
		return mProperties.get(p);
	}

	@Override
	public List<PatternMetadata> getMetadata() {
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
		if (!(o instanceof PatternVertex)) {
			return false;
		}
		PatternVertex other = (PatternVertex) o;
		if (!getTypeName().equals(other.getTypeName())) {
			return false;
		}
		List<String> myProperties = getProperties();
		if (myProperties.size() != other.getProperties().size()) {
			return false;
		}
		for (String property : myProperties) {
			boolean myRestriction = isPropertyRestricted(property);
			if (myRestriction != other.isPropertyRestricted(property)) {
				return false;
			}
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

	@Override
	public void addProperty(String key, String value, boolean isRestriction, boolean isRelated) {
		mProperties.put(key, value);
		if (isRestriction) {
			mRestrictedProperties.add(key);
		}
		if (isRelated) {
			mRelatedProperties.add(key);
		}
	}

	@Override
	public boolean isPropertyRestricted(String p) {
		return mRestrictedProperties.contains(p);
	}

	@Override
	public boolean isPropertyRelated(String p) {
		return mRelatedProperties.contains(p);
	}

	@Override
	public void assignMatch(IfmapVertex matchedVertex) {
		if (matchedVertex != null) {
			this.mMatched = true;
			this.mMatchedVertex = matchedVertex;
		}
		if (matchedVertex == null) {
			this.mMatched = false;
			this.mMatchedVertex = null;
		}
	}

	@Override
	public boolean isMatched() {
		return mMatched;
	}

	@Override
	public IfmapVertex getMatchedVertex() {
		return mMatchedVertex;
	}

	@Override
	public PatternVertex copy() {
		ExtendedPatternVertex copy = new ExtendedPatternVertex(this.getTypeName());
		copy.assignMatch(this.getMatchedVertex());
		for (String p : this.getProperties()) {
			copy.addProperty(p, this.valueFor(p), this.isPropertyRestricted(p), this.isPropertyRelated(p));
		}
		for (PatternMetadata m : this.getMetadata()) {
			copy.addMetadata(m);
		}
		return copy;
	}

	@Override
	public boolean equalsWithMatch(Object o) {
		if (!this.equals(o)) {
			return false;
		}
		PatternVertex other = (PatternVertex) o;
		if (this.isMatched() != other.isMatched()) {
			return false;
		}
		if (this.isMatched()) {
			if (!this.getMatchedVertex().equals(other.getMatchedVertex())) {
				return false;
			}
		}
		return true;
	}

}
