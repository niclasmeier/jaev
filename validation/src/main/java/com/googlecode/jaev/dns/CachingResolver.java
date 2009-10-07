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
import static com.googlecode.jaev.dns.DnsResultCode.DOMAIN_NAME_NOT_FOUND;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.googlecode.jaev.dns.Cache.Entry;
import com.googlecode.jaev.dns.ResouceRecord.Type;

/**
 * The caching resolver implements the decorator pattern to add caching
 * functionality to the resolver.
 * 
 * @author Niclas Meier
 */
public class CachingResolver implements Resolver {

	/** The DNS cache */
	private final Cache cache;

	/** The delegate resolver */
	private final Resolver delegate;

	/** The maximum age of cache entries in milliseconds */
	private final long maxAge;

	/**
	 * A simple constructor which creates a cache with a 30 minutes cache entry
	 * max age.
	 * 
	 * @param delegate
	 *            The resolver which will used to lookup the request if the
	 *            cache query misses.
	 * @param cache
	 *            The cache.
	 */
	public CachingResolver(Resolver delegate, Cache cache) {
		this(delegate, cache, 30, MINUTES);
	}

	/**
	 * Parameter constructor
	 * 
	 * @param delegate
	 *            The resolver which will used to lookup the request if the
	 *            cache query misses.
	 * @param cache
	 *            The cache.
	 * @param maxAge
	 *            maximum age of cache entries
	 * @param maxAgeUnit
	 *            Time unit of maximum age cache entries
	 */
	public CachingResolver(Resolver delegate, Cache cache, long maxAge, TimeUnit maxAgeUnit) {
		this.delegate = notNull(delegate, "delegate");
		this.cache = notNull(cache, "cache");
		this.maxAge = notNull(maxAgeUnit, "maxAgeUnit").toMillis(maxAge);
	}

	@Override
	public List<ResouceRecord> resolve(String domainName, Type recordType) throws ResolverException {
		// fetch the cache entry
		Entry entry = this.cache.get(domainName);

		// compute the current cache age
		long age = System.currentTimeMillis() - entry.getLastModified();

		if (this.maxAge > 0 && age >= this.maxAge) {
			// the the cache age is greater than the max age, clear the cache
			// entry.
			entry = this.cache.clear(domainName);
		}

		// if the domain is registered as not found
		if (entry.isNotFound()) {
			// throw the exception
			throw new ResolverException(DOMAIN_NAME_NOT_FOUND, null, domainName, recordType);
		}
		else {
			// if the record type is cached
			if (entry.isRecordTypeCached(recordType)) {
				// fetch the resource records from the entry
				return entry.getResourceRecords(recordType);
			}
			else {
				try {
					// the record type is not cached, fetch the information
					// from the delegate
					List<ResouceRecord> records = this.delegate.resolve(domainName, recordType);

					// and cache them
					this.cache.put(domainName, recordType, records);
					return records;
				}
				catch (ResolverException e) {
					if (DOMAIN_NAME_NOT_FOUND.equals(e.getResultCode())) {
						// the lookup failed, mark the domain as not found
						this.cache.notFound(domainName);
					}
					throw e;
				}

			}

		}
	}
}
