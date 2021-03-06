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
package de.hshannover.f4.trust.irongpm.algorithm.interfaces;

import java.util.List;

/**
 * Interface encapsulating the storage and access of properties. Properties are Key-Value-Pairs<String, String>. Used to
 * store IF-MAP Attributes
 *
 * @author Leonard Renners
 *
 */
public interface PatternPropable {

	/**
	 * @return A list of all stored property keys
	 */
	public List<String> getProperties();

	/**
	 * Adds a property value to the element
	 *
	 * @param key
	 *            the key of the property
	 * @param value
	 *            the value of the property (or the key for the relation)
	 * @param isRestriction
	 *            whether the property is modeled as a NOT in the pattern
	 * @param isRelated
	 *            whether the property is related to the property of another element in the pattern
	 */
	public void addProperty(String key, String value, boolean isRestriction, boolean isRelated);

	/**
	 * @param p
	 *            Key for the property
	 * @return Whether the property for the given key is defined.
	 */
	public boolean hasProperty(String p);

	/**
	 * @param p
	 *            Key for the property
	 * @return The value for the property or the relation key if the value is connected to other properties
	 */
	public String valueFor(String p);

	/**
	 *
	 * @param p
	 *            Key for the property
	 * @return Whether the property for the given key is modeled as a NOT in the pattern
	 */
	public boolean isPropertyRestricted(String p);

	/**
	 *
	 * @param p
	 *            Key for the property
	 * @return Whether the property for the given key is related to another property of another element in the pattern
	 */
	public boolean isPropertyRelated(String p);

	/**
	 * Typename is one special property. It denotes the type of the Metadata or Identifier, respectively.
	 *
	 * @return the type name
	 */
	public String getTypeName();

}
