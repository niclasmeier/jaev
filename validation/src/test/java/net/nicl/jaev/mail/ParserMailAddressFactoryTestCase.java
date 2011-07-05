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

package net.nicl.jaev.mail;

import net.nicl.jaev.MailAddress;
import net.nicl.jaev.ResultCode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.nicl.jaev.MailAddress.DomainFormat.IP;
import static net.nicl.jaev.MailAddress.DomainFormat.NAME;
import static net.nicl.jaev.mail.MailResultCode.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ParserMailAddressFactoryTestCase {

	private static final Logger LOG = LoggerFactory.getLogger(ParserMailAddressFactoryTestCase.class);

	private static final MailAddressFactory factory = new ParserMailAddressFactory();

	@Test
	public void valid() {
		test("john.doe@jaev.googlecode.com", "john.doe", "jaev.googlecode.com", NAME, null);
		test("root@[192.168.0.1]", "root", "[192.168.0.1]", IP, null);

		test("tom.o'reilly@sourceforge.net", "tom.o'reilly", "sourceforge.net", NAME, null);
		test("john1doe@sourceforge.net", "john1doe", "sourceforge.net", NAME, null);
		test("john$doe@sourceforge.net", "john$doe", "sourceforge.net", NAME, null);
		test("john_doe@sourceforge.net", "john_doe", "sourceforge.net", NAME, null);
		test("john-doe@sourceforge.net", "john-doe", "sourceforge.net", NAME, null);

		// -- Quoted special characters are valid --
		test("\"joe.\"@sourceforge.net", "\"joe.\"", "sourceforge.net", NAME, null);
		test("\"joe+\"@sourceforge.net", "\"joe+\"", "sourceforge.net", NAME, null);
		test("\"joe!\"@sourceforge.net", "\"joe!\"", "sourceforge.net", NAME, null);
		test("\"joe*\"@sourceforge.net", "\"joe*\"", "sourceforge.net", NAME, null);
		test("\"joe'\"@sourceforge.net", "\"joe'\"", "sourceforge.net", NAME, null);
		test("\"joe,\"@sourceforge.net", "\"joe,\"", "sourceforge.net", NAME, null);
		test("\"joe%45\"@sourceforge.net", "\"joe%45\"", "sourceforge.net", NAME, null);
		test("\"joe;\"@sourceforge.net", "\"joe;\"", "sourceforge.net", NAME, null);
		test("\"joe?\"@sourceforge.net", "\"joe?\"", "sourceforge.net", NAME, null);
		test("\"joe&\"@sourceforge.net", "\"joe&\"", "sourceforge.net", NAME, null);
		test("\"joe=\"@sourceforge.net", "\"joe=\"", "sourceforge.net", NAME, null);
	}

	@Test
	public void illegalEmaiFormat() {
		test("jaev.googlecode.com", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT);
	}

	@Test
	public void invalidLocalPart() {
		// -- Unquoted special characters are not valid --
		test("joe(@sourceforge.net", null, null, null, ILLEGAL_EMAIL_FORMAT);
		test("joe)@sourceforge.net", null, null, null, ILLEGAL_EMAIL_FORMAT);
		test("joe;@sourceforge.net", null, null, null, ILLEGAL_EMAIL_FORMAT);
		test("john,doe@sourceforge.net", null, null, null, ILLEGAL_EMAIL_FORMAT);

		// test("joe.@sourceforge.net", null, null, null, ILLEGAL_EMAIL_FORMAT);
		// FIXME: was valid but it shouldn't
		// test("joe+@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe*@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe/@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe!@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe%45@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe&@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
		// test("joe=@sourceforge.net", null, null, null, INVALID_LOCAL_PART);
	}

	@Test
	public void illegalCharacters() {
		test("john_d�e@sourceforge.net", null, null, null, ILLEGAL_CHARACTERS);
		test("john_doe\u008f@sourceforge.net", null, null, null, ILLEGAL_CHARACTERS);
	}

	@Test
	public void unknownToplevelDomain() {
		test("john_doe@sourceforge.bla", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		test("john_doe@sourceforge.xx", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
	}

	@Test
	public void illegalDomainIpFormat() {
		test("john_doe@[256.0.0.1]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT);
		test("john_doe@[10.0.1]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT);
		test("john_doe@[10.0.0.-1]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT);
	}

	@Test
	public void illegalDomainNameFormat() {
		test("root@192.168.0.1", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		test("john_doe@sourceforge.n", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);

		test("john_doe@sourceforge.-net", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		test("john_doe@sourceforge.n-et", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		test("john_doe@sourceforge.ne-t", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		test("john_doe@sourceforge.net.", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN);
		// test("john_doe@o'reilly.org", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT);

	}

	private static void test(String address, String expectedLocalPart, String expectedDomain,
			MailAddress.DomainFormat expectedDomainFormat, ResultCode expectedResult) {
		try {
			MailAddress mailAddress = factory.create(address);

			if (expectedResult != null) {
				fail("The address '" + address + "' was successfuly parsed but it shouldn't.");
			}

			assertThat(mailAddress.getLocalPart(), is(expectedLocalPart));
			assertThat(mailAddress.getDomain(), is(expectedDomain));
			assertThat(mailAddress.getDomainFormat(), is(expectedDomainFormat));

		}
		catch (MailParseException e) {
			if (expectedResult != e.getResultCode()) {
				LOG.info(e.getMessage() + " - " + e.getResultCode());
			}
			assertThat(e.getResultCode(), is(expectedResult));
		}
	}

}
