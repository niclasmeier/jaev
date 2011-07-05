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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static net.nicl.jaev.MailAddress.DomainFormat.NAME;
import static net.nicl.jaev.ValidatorResultCode.EMAIL_GROUPS_NOT_SUPPORTED;
import static net.nicl.jaev.mail.MailResultCode.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * This test cases are inspried by the test cases from commons validator
 * (http://
 * svn.apache.org/viewvc/commons/proper/validator/trunk/src/test/java/org
 * /apache/commons/validator/routines/EmailValidatorTest.java?view=markup) and
 * perl Mail::RFC822::Address & RFC::RFC822::Address perl test.pl
 * 
 * @author Niclas Meier
 */
public class PerlMailAddressFactoryTestCase {

	private static final Logger LOG = LoggerFactory.getLogger(PerlMailAddressFactoryTestCase.class);

	private static final Set<ResultCode> ACCEPTED_INVALID_RESULT = new java.util.HashSet<ResultCode>();

	private static final MailAddressFactory factory = new ParserMailAddressFactory();

	static {
		ACCEPTED_INVALID_RESULT.add(UNKNOWN_TOPLEVEL_DOMAIN);
	}

	@Test
	public void valid() {
		test("abigail@example.com", "abigail", "example.com", NAME, null, true);
		test("abigail@example.com ", "abigail", "example.com", NAME, null, true);
		test(" abigail@example.com", "abigail", "example.com", NAME, null, true);
		test("abigail @example.com ", "abigail", "example.com", NAME, null, true);
		test("*@example.net", "*", "example.net", NAME, null, true);
		test("\"\\\"\"@foo.com", "\"\\\"\"", "foo.com", NAME, null, true);
		test("fred&barny@example.com", "fred&barny", "example.com", NAME, null, true);
		test("---@example.com", "---", "example.com", NAME, null, true);
		test("foo-bar@example.net", "foo-bar", "example.net", NAME, null, true);
		// test("\"127.0.0.1\"@[127.0.0.1]", "\"127.0.0.1\"", "[127.0.0.1]", IP,
		// null, true);
		test("Abigail <abigail@example.com>", "abigail", "example.com", NAME, null, true);
		test("Abigail<abigail@example.com>", "abigail", "example.com", NAME, null, true);
		test("Abigail<@a,@b,@c:abigail@example.com>", "abigail", "example.com", NAME, null, true);
		test("\"This is a phrase\"<abigail@example.com>", "abigail", "example.com", NAME, null, true);
		test("\"Abigail \"<abigail@example.com>", "abigail", "example.com", NAME, null, true);

		test("Abigail <abigail @ example.com>", "abigail", "example.com", NAME, null, true);
		test("Abigail made this <  abigail   @   example  .    com    >", "abigail", "example.com", NAME, null, true);
		test("Abigail(the bitch)@example.com", "Abigail", "example.com", NAME, null, true);

		// TODO: parser does not work so well with comments yet
		// test("Abigail <abigail @ example . (bar) com >", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT, true);
		// test(
		// "Abigail < (one)  abigail (two) @(three)example . (bar) com (quz) >",
		// null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, true);
		// test(
		// "Abigail (foo) (((baz)(nested) (comment)) ! ) < (one)  abigail (two) @(three)example . (bar) com (quz) >",
		// null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, true);
		// test("Abigail <abigail(fo\\(o)@example.com>", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT, true);
		// test("Abigail <abigail(fo\\)o)@example.com> ", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT, true);
		// test("(foo) abigail@example.com", null, null, null,
		// INVALID_LOCAL_PART,
		// true);
		// test("abigail@example.com (foo)", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT, true);
		test("\"Abi\\\"gail\" <abigail@example.com>", "abigail", "example.com", NAME, null, true);
		// test("abigail@[example.com]", "abigail", "[example.com]", IP, null,
		// true);
		// test("abigail@[exa\\[ple.com]", "abigail", "[exa\\[ple.com]", IP,
		// null,
		// true);
		// test("abigail@[exa\\]ple.com]", "abigail", "[exa\\]ple.com]", IP,
		// null,
		// true);
		test("\":sysmail\"@  Some-Group. Org", "\":sysmail\"", "Some-Group.Org", NAME, null, true);
		// TODO: parser does not work so well with comments yet
		// test("Muhammed.(I am  the greatest) Ali @(the)Vegas.info", null,
		// null,
		// null, ILLEGAL_DOMAIN_NAME_FORMAT, true);
		test("mailbox.sub1.sub2@this-domain.info", "mailbox.sub1.sub2", "this-domain.info", NAME, null, true);
		test("sub-net.mailbox@sub-domain.domain", null, null, null, UNKNOWN_TOPLEVEL_DOMAIN, true);
		test("Alfred Neuman <Neuman@BBN-TENEXA.org>", "Neuman", "BBN-TENEXA.org", NAME, null, true);
		test("Neuman@BBN-TENEXA.org", "Neuman", "BBN-TENEXA.org", NAME, null, true);
		test("\"George, Ted\" <Shared@Group.Arpa>", "Shared", "Group.Arpa", NAME, null, true);
		// TODO: parser does not work so well with comments yet
		// test("Wilt . (the  Stilt) Chamberlain@NBA.US", null, null, null,
		// INVALID_LOCAL_PART, true);
		// test("$@[]", "$", "[]", IP, null, true);
		// TODO: parser does not work so well with comments yet
		// test("*()@[]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT, true);
		// test("\"quoted ( brackets\" ( a comment )@example.com", null, null,
		// null, INVALID_LOCAL_PART, true);
		test("\"Joe & J. Harvey\"\r\n     <ddd@org.Org>", "ddd", "org.Org", NAME, null, true);
		test("\"Joe &\r\n J. Harvey\" <ddd @ org.Org>", "ddd", "org.Org", NAME, null, true);
		// TODO: parser does not work so well with comments yet
		// test("Gourmets:  Pompous Person <WhoZiWhatZit@Cordon-Bleu.com>,\r\n"
		// + "        Childs@WGBH.Boston.us, \"Galloping Gourmet\"@\r\n"
		// + "        ANT.Down-Under.au (Australian National Television),\r\n"
		// + "        Cheapie@Discount-Liquors.com;", null, null, null,
		// ILLEGAL_DOMAIN_NAME_FORMAT, true);
	}

	@Test
	@Ignore
	public void invalid() {
		test("   Just a string", null, null, null, ILLEGAL_EMAIL_FORMAT, false);
		test("string", null, null, null, ILLEGAL_EMAIL_FORMAT, false);
		test("(comment)", null, null, null, ILLEGAL_EMAIL_FORMAT, false);

		// TODO: parser does not work so well with comments yet
		// test("()@example.com", null, null, null, INVALID_LOCAL_PART, false);
		// test("fred(&)barny@example.com", null, null, null,
		// INVALID_LOCAL_PART,
		// false);
		test("Abigail <abi gail @ example.com>", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("fred\\ barny@example.com", null, null, null, INVALID_LOCAL_PART, false);
		test("Abigail <abigail(fo(o)@example.com>", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("Abigail <abigail(fo)o)@example.com>", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("\"Abi\"gail\" <abigail@example.com>", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("abigail@[exa]ple.com]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT, false);
		test("abigail@[exa[ple.com]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT, false);
		test("abigail@[exaple].com]", null, null, null, ILLEGAL_DOMAIN_IP_FORMAT, false);
		test("abigail@", null, null, null, ILLEGAL_EMAIL_FORMAT, false);
		test("@example.com", null, null, null, ILLEGAL_EMAIL_FORMAT, false);
		test("phrase: abigail@example.com abigail@example.com ;", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("invalid�char@example.com", null, null, null, ILLEGAL_CHARACTERS, false);
		test("\"Joe & J. Harvey\" <example @Org>", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);
		test("name:;", null, null, null, EMAIL_GROUPS_NOT_SUPPORTED, false);
		test("':;", null, null, null, EMAIL_GROUPS_NOT_SUPPORTED, false);
		test("name:   ;", null, null, null, EMAIL_GROUPS_NOT_SUPPORTED, false);

		test("Cruisers:  Port@Portugal.org, Jones@SEA.com;", null, null, null, ILLEGAL_DOMAIN_NAME_FORMAT, false);

	}

	private static void test(String address, String expectedLocalPart, String expectedDomain,
			MailAddress.DomainFormat expectedDomainFormat, ResultCode expectedResult, boolean pass) {
		try {
			MailAddress mailAddress = factory.create(address);

			if (expectedResult != null) {
				fail("The address '" + address + "' was successfuly parsed ('" + mailAddress.getLocalPart() + "' and '"
						+ mailAddress.getDomain() + "')but it shouldn't.");
			}

			if (!pass) {
				fail("The test should fail (" + address + ").");
			}

			assertThat(mailAddress.getLocalPart(), is(expectedLocalPart));
			assertThat(mailAddress.getDomain(), is(expectedDomain));
			assertThat(mailAddress.getDomainFormat(), is(expectedDomainFormat));

		}
		catch (MailParseException e) {
			if (pass && !ACCEPTED_INVALID_RESULT.contains(e.getResultCode())) {
				fail("The test should pass but recieved '" + e.getResultCode() + "' (" + address + ").");
			}

			if (expectedResult != e.getResultCode()) {
				LOG.info(e.getMessage() + " - " + e.getResultCode());
			}
			assertThat(e.getResultCode(), is(expectedResult));
		}
	}

}
