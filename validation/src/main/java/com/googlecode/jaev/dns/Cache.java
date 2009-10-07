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

import java.util.List;

import com.googlecode.jaev.dns.ResouceRecord.Type;

/**
 * The DNS cache interface defines methods to enable caching of DNS lookups
 * 
 * @author Niclas Meier
 */
public interface Cache {

	/**
	 * Get cache entry
	 * 
	 * @param domainName
	 *            The domain name
	 * @return An cache entry
	 */
	public Entry get(String domainName);

	/**
	 * Adds values to the cache
	 * 
	 * @param domainName
	 *            The domain name
	 * @param type
	 *            The record type
	 * @param resourceRecords
	 *            The resource records for this type
	 * @return The created cache entry
	 */
	public Entry put(String domainName, Type type, List<ResouceRecord> resourceRecords);

	/**
	 * Signals that the domain name was not found on the last lookup
	 * 
	 * @param domainName
	 *            The not found domain name
	 */
	public void notFound(String domainName);

	/**
	 * Clears a cache entry
	 * 
	 * @param domainName
	 *            The domain name to clear
	 * @return A new empty cache entry
	 */
	public Entry clear(String domainName);

	/**
	 * Returns a list of all cached domains;
	 * 
	 * @return All cached domains
	 */
	public List<String> getCachedDomains();

	/**
	 * The cache entry contains all relevant informations
	 */
	public interface Entry {

		/**
		 * Fetches the resource entries
		 * 
		 * @param type
		 *            The resource record type
		 * @return The list of resource records
		 */
		public List<ResouceRecord> getResourceRecords(Type type);

		/**
		 * How often was this cache entry found.
		 * 
		 * @return The current hit count.
		 */
		public long getHitCount();

		/**
		 * Checks if the resource type cached
		 * 
		 * @param type
		 *            The record type
		 * @return <code>true</code> if the record type is cached
		 */
		public boolean isRecordTypeCached(Type type);

		/**
		 * Checks if the domain was not found
		 * 
		 * @return <code>true</code> if the domain was not found
		 */
		public boolean isNotFound();

		/**
		 * The last modified date of the cache entry
		 * 
		 * @return The last modified date
		 */
		public long getLastModified();
	}

}
