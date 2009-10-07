/*
 * Copyright 2009 - Niclas Meier Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.googlecode.jaev.mail;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class UrlTopLevelDomainCheckerTestCase {

	TopLevelDomainChecker topLevelDomainChecker = new UrlTopLevelDomainChecker(UrlTopLevelDomainChecker.class
			.getClassLoader().getResource("com/googlecode/jaev/mail/tlds-alpha-by-domain.txt"));

	@Test
	public void testLoadAndReload() {
		assertThat(this.topLevelDomainChecker.initialise(), is(false));
	}

	@Test
	public void testRemote() throws MalformedURLException {
		UrlTopLevelDomainChecker remoteChecker = new UrlTopLevelDomainChecker(new URL(
				"http://data.iana.org/TLD/tlds-alpha-by-domain.txt"));

		assertThat(remoteChecker.initialise(), is(false));

	}

	@Test
	public void testValid() {
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("org"), is(true));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("net"), is(true));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("com"), is(true));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("edu"), is(true));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("De"), is(true));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("uK"), is(true));
	}

	@Test
	public void testInvalid() {
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("orga"), is(false));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("met"), is(false));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("kom"), is(false));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("ehdu"), is(false));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("xxx"), is(false));
		assertThat(this.topLevelDomainChecker.isValidTopLevelDomain("foo"), is(false));
	}

}
