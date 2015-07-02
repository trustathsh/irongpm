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

import java.util.HashMap;

import org.jgrapht.graph.DefaultEdge;

import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapEdge;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.IfmapVertex;
import de.hshannover.f4.trust.irongpm.ifmap.interfaces.Metadata;

/**
 * Representing a link between two pattern vertices.
 * 
 * @author Leonard Renners
 * 
 */
public class IfmapEdgeImpl extends DefaultEdge implements IfmapEdge {

	private IfmapVertex mV1;
	private IfmapVertex mV2;

	/**
	 *
	 */
	private static final long serialVersionUID = -6826874478634914160L;

	private Metadata mMeta;

	/**
	 * Constructor to explicitly set the target and source vertives, to make sure, that they never are null. Source and
	 * target should not really matter, since a unidirected behaviour is assumed.
	 * 
	 * @param source
	 *            The first vertex of the edge
	 * @param target
	 *            The second vertex of the edge
	 * @param meta
	 *            The IF-MAP metadata of the edge
	 */
	public IfmapEdgeImpl(IfmapVertex source, IfmapVertex target, Metadata meta) {
		this.mV1 = source;
		this.mV2 = target;
		setMetadata(meta);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param cp
	 *            the original Object
	 */
	public IfmapEdgeImpl(IfmapEdgeImpl cp) {
		this.mV1 = new IfmapVertexImpl(cp.getV1());
		this.mV2 = new IfmapVertexImpl(cp.getV2());
		setMetadata(new MetadataImpl(cp.getMetadata()));
	}

	public IfmapVertex getV1() {
		return mV1;
	}

	public IfmapVertex getV2() {
		return mV2;
	}

	public void setMetadata(Metadata meta) {
		mMeta = meta;
	}

	@Override
	public Metadata getMetadata() {
		if (mMeta == null) {
			System.out.println("WTF");
			return new MetadataImpl("DUMMY", new HashMap<String, String>(), null);
		}
		return mMeta;
	}

	// XXX: Equality of edges is not planned like this for JGraphT. Source and
	// Target of an edge should be requested using the graph the edge is in...
	// This results in the fact, that the equals can not yet be used on the
	// IfmapEdge interface...
	@Override
	public boolean equalsNonIfmap(Object e) {
		if (e == null) {
			return false;
		}
		if (!(e instanceof IfmapEdgeImpl)) {
			return false;
		}
		IfmapEdgeImpl other = (IfmapEdgeImpl) e;
		if (getV1().equals(other.getV1())) {
			if (getV2().equals(other.getV2())) {
				return getMetadata().equals(other.getMetadata());
			}
		}
		if (getV1().equals(other.getV2())) {
			if (getV2().equals(other.getV1())) {
				return getMetadata().equals(other.getMetadata());
			}
		}
		return false;
	}

	/**
	 * Equals method using the IF-MAP understanding of edge/metadata equality. This means that two edges of the same
	 * type, between the same vertices (identifiers) are always equal if they are of singleValue cardinality, and never
	 * equal for mutliValue. This method should be used for edge insertion in the graph, but not for graph comparison.
	 * 
	 * @param e
	 *            the edge to compare with
	 * @return whether the edge is equal with regard to the if-map understanding of equality
	 */
	@Override
	public boolean equals(Object e) {
		if (e == null) {
			return false;
		}
		if (!(e instanceof IfmapEdgeImpl)) {
			return false;
		}
		IfmapEdgeImpl other = (IfmapEdgeImpl) e;
		if (!getMetadata().getTypeName().equals(other.getMetadata().getTypeName())) {
			return false;
		}
		if (getV1().equals(other.getV1())) {
			if (getV2().equals(other.getV2())) {
				if (this.getMetadata().isSingleValue() && other.getMetadata().isSingleValue()) {
					return true;
				} else if (!this.getMetadata().isSingleValue() && !other.getMetadata().isSingleValue()) {
					return getMetadata().equals(other.getMetadata());
				}
				return false;
			}
		}
		if (getV1().equals(other.getV2())) {
			if (getV2().equals(other.getV1())) {
				if (this.getMetadata().isSingleValue() && other.getMetadata().isSingleValue()) {
					return true;
				} else if (!this.getMetadata().isSingleValue() && !other.getMetadata().isSingleValue()) {
					return getMetadata().equals(other.getMetadata());
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		if (getMetadata().isSingleValue()) {
			result = prime * result + getMetadata().getTypeName().hashCode() + getV1().hashCode() + getV2().hashCode();
		} else {
			result = prime * result + getMetadata().hashCode() + getV1().hashCode() + getV2().hashCode();
		}
		return result;
	}

	@Override
	public int hashCodeNonIfmap() {
		int prime = 31;
		int result = 1;
		if (getMetadata().isSingleValue()) {
			result = prime * result + getMetadata().hashCode() + getV1().hashCode() + getV2().hashCode();
		} else {
			result = prime * result + getMetadata().hashCode() + getV1().hashCode() + getV2().hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return getMetadata().toString();
	}

}
