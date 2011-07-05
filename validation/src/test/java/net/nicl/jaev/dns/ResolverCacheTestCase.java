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

package net.nicl.jaev.dns;

import net.nicl.jaev.dns.Cache.Entry;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ResolverCacheTestCase {

	private static Resolver resolver;

	private static Cache ehCache;

	private static DnsEhCache dnsCache;

	@BeforeClass
	public static void setUp() {
		CacheManager cacheManager = new CacheManager();
		cacheManager.addCache("test");
		ehCache = cacheManager.getCache("test");

		dnsCache = new DnsEhCache(ehCache);
		SimpleResolver simpleResolver = new SimpleResolver();
		resolver = new CachingResolver(simpleResolver, dnsCache);

	}

	@Test
	public void findMx() throws ResolverException {
		List<ResouceRecord> records = resolver.resolve("nicl.de", ResouceRecord.Type.MX);
		for (ResouceRecord record : records) {
			assertThat(record.getType(), is(ResouceRecord.Type.MX));
		}

		Entry cacheEntry = dnsCache.get("nicl.de");
		assertThat(cacheEntry.isNotFound(), is(false));
		assertThat(cacheEntry.getHitCount() == 1, is(true));

	}

	@Test
	public void failFind() {
		try {
			resolver.resolve("nicl.invalid", ResouceRecord.Type.MX);
			fail("MX record found for invalid domain.");
		}
		catch (ResolverException e) {
			assertThat(e.getResultCode(), is(DnsResultCode.DOMAIN_NAME_NOT_FOUND));

		}
		Entry cacheEntry = dnsCache.get("nicl.invalid");
		assertThat(cacheEntry.isNotFound(), is(true));
		assertThat(cacheEntry.getHitCount() == 1, is(true));
	}

	@Test
	@Ignore
	public void failTimeout() {
		Resolver localResolver = new SimpleResolver(1, 1);

		try {
			localResolver.resolve("nicl.de", ResouceRecord.Type.MX);
			fail("No timeout occured.");
		}
		catch (ResolverException e) {
			assertThat(e.getResultCode(), is(DnsResultCode.DNS_TIMEOUT));

		}

	}
}
