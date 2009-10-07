/*
 * Copyright 2009 - Niclas Meier
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.jaev.dns;

import static com.googlecode.jaev.Check.notNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import com.googlecode.jaev.dns.ResouceRecord.Type;

/**
 * DNS cache implementation based on EH cache
 * 
 * @author Niclas Meier
 */
public class DnsEhCache implements Cache {

	/** Empty cache entry */
	private static final Entry EMPTY = new Entry() {

		@Override
		public long getHitCount() {
			return 0;
		}

		@Override
		public long getLastModified() {
			return System.currentTimeMillis();
		}

		@Override
		public List<ResouceRecord> getResourceRecords(Type type) {
			return Collections.emptyList();
		}

		@Override
		public boolean isNotFound() {
			return false;
		}

		@Override
		public boolean isRecordTypeCached(Type type) {
			return false;
		}
	};

	/** Internal EH cache instance */
	private final net.sf.ehcache.Cache cache;

	/**
	 * Base constructor
	 * 
	 * @param cache
	 *            The EH cache instance
	 */
	public DnsEhCache(net.sf.ehcache.Cache cache) {
		this.cache = notNull(cache, "cache");
	}

	@Override
	public Entry clear(String domainName) {
		this.cache.remove(domainName);
		return EMPTY;
	}

	@Override
	public Entry get(String domainName) {
		Element cacheElement = this.cache.get(domainName);

		if (cacheElement == null) {
			return EMPTY;
		}
		else {
			return new ElementEntry(cacheElement);
		}
	}

	@Override
	public void notFound(String domainName) {
		Element element = new Element(domainName, new Data(true));
		this.cache.put(element);
	}

	@Override
	public Entry put(String domainName, Type type, List<ResouceRecord> resourceRecords) {
		Element element = this.cache.get(domainName);
		Data data;
		if (element == null) {
			data = new Data(false);
			element = new Element(domainName, data);
			this.cache.put(element);
		}
		else {
			data = (Data) element.getValue();
		}

		data.getRecords().put(type, resourceRecords.toArray(new ResouceRecord[resourceRecords.size()]));

		return new ElementEntry(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getCachedDomains() {
		return this.cache.getKeys();
	}

	/**
	 * The data object which will be stored into EH cache
	 */
	private static final class Data implements Serializable {

		private static final long serialVersionUID = -2278390802776792600L;

		/** The records */
		private final Map<Type, ResouceRecord[]> records;

		/** Flag if domain is not found */
		private final boolean notFound;

		/**
		 * Base constructor
		 * 
		 * @param notFound
		 *            <code>true</code> if the domain is not found
		 */
		public Data(boolean notFound) {
			this.notFound = notFound;
			if (notFound) {
				this.records = Collections.emptyMap();
			}
			else {
				this.records = new java.util.HashMap<Type, ResouceRecord[]>();
			}
		}

		/**
		 * Gets the cached resource records map
		 * 
		 * @return Map of types to resource records
		 */
		public Map<Type, ResouceRecord[]> getRecords() {
			return this.records;
		}

		/**
		 * Is the domain not found.
		 * 
		 * @return <code>true</code> if the domain is not found
		 */
		public boolean isNotFound() {
			return this.notFound;
		}

	}

	/**
	 * The entry implementation which is used to bridge the <code>Element</code> to the <code>Entry</code> interface.
	 */
	private static final class ElementEntry implements Entry {

		/** Internal element implementation */
		private final Element element;

		/**
		 * Default constructor
		 * 
		 * @param element
		 *            The internal element
		 */
		public ElementEntry(Element element) {
			this.element = element;
		}

		@Override
		public long getHitCount() {
			return this.element.getHitCount();
		}

		@Override
		public long getLastModified() {
			return this.element.getLastUpdateTime();
		}

		@Override
		public List<ResouceRecord> getResourceRecords(Type type) {
			Map<Type, ResouceRecord[]> recordMap = getData().getRecords();

			if (recordMap.isEmpty()) {
				return Collections.emptyList();
			}
			else {
				ResouceRecord[] records = recordMap.get(type);
				if (records.length > 0) {
					return Arrays.asList(records);
				}
				else {
					return Collections.emptyList();
				}
			}

		}

		@Override
		public boolean isNotFound() {
			return getData().isNotFound();
		}

		@Override
		public boolean isRecordTypeCached(Type type) {
			Map<Type, ResouceRecord[]> recordMap = getData().getRecords();

			return recordMap.get(type) == null;
		}

		public Data getData() {
			return ((Data) this.element.getValue());
		}

	}

}
