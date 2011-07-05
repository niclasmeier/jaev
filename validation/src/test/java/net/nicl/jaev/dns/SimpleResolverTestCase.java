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

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SimpleResolverTestCase {

	private final Resolver resolver = new SimpleResolver();

	@Test
	public void findMx() throws ResolverException {
		List<ResouceRecord> records = this.resolver.resolve("nicl.de", ResouceRecord.Type.MX);
		for (ResouceRecord record : records) {
			assertThat(record.getType(), is(ResouceRecord.Type.MX));
		}

	}

	@Test
	public void failFind() {
		try {
			this.resolver.resolve("nicl.invalid", ResouceRecord.Type.MX);
			fail("MX record found for invalid domain.");
		}
		catch (ResolverException e) {
			assertThat(e.getResultCode(), is(DnsResultCode.DOMAIN_NAME_NOT_FOUND));

		}

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
