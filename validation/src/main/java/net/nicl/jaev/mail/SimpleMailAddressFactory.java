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

package com.googlecode.jaev.mail;

import static com.googlecode.jaev.Check.notBlank;
import static com.googlecode.jaev.MailAddress.DomainFormat.IP;
import static com.googlecode.jaev.MailAddress.DomainFormat.NAME;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_CHARACTERS;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_DOMAIN_NAME_FORMAT;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_EMAIL_FORMAT;
import static com.googlecode.jaev.mail.MailResultCode.INVALID_LOCAL_PART;
import static com.googlecode.jaev.mail.MailResultCode.UNKNOWN_TOPLEVEL_DOMAIN;
import static com.googlecode.jaev.mail.Utilites.checkIpDomain;
import static com.googlecode.jaev.mail.Utilites.isAscii;
import static java.nio.charset.CodingErrorAction.REPORT;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.jaev.MailAddress;
import com.googlecode.jaev.MailAddress.DomainFormat;

/**
 * Simple implementation of the <code>MailAddressFactory</code> interface. Some
 * regular expressions are used to validate the syntax and a <code>TopLevelDomainChecker</code> is used to validate the
 * top level domain.
 * 
 * @author Niclas Meier
 */
public class SimpleMailAddressFactory implements MailAddressFactory {

	private static final String SPECIAL_CHARS = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";

	private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";

	private static final String QUOTED_USER = "(\"[^\"]*\")";

	private static final String WORD = "((" + VALID_CHARS + "|')+|" + QUOTED_USER + ")";

	private static final String EMAIL = "^\\s*?(.+)@(.+?)\\s*$";

	private static final String IP_DOMAIN = "^\\[(.*)\\]$";

	private static final String USER = "^\\s*" + WORD + "(\\." + WORD + ")*$";

	private static final String DOMAIN_LABEL = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";

	private static final String TOP_LEVEL = "\\p{Alpha}{2,}";

	private static final String DOMAIN_NAME = new StringBuilder("^(?:").append(DOMAIN_LABEL).append("\\.)+")
			.append("(").append(TOP_LEVEL).append(")$").toString();

	private final Pattern emailPattern;

	private final Pattern userPattern;

	private final Pattern ipDomainPattern;

	private final Pattern domainNamePattern;

	private final TopLevelDomainChecker topLevelDomainChecker;

	public SimpleMailAddressFactory() {
		this(new UrlTopLevelDomainChecker(SimpleMailAddressFactory.class.getClassLoader().getResource(
				"com/googlecode/jaev/mail/tlds-alpha-by-domain.txt")));
	}

	public SimpleMailAddressFactory(TopLevelDomainChecker topLevelDomainChecker) {
		this.topLevelDomainChecker = topLevelDomainChecker;
		CharsetEncoder asciiEncoder = Charset.forName("ASCII").newEncoder();

		asciiEncoder.onMalformedInput(REPORT);
		asciiEncoder.onUnmappableCharacter(REPORT);

		this.emailPattern = Pattern.compile(EMAIL);
		this.userPattern = Pattern.compile(USER);
		this.ipDomainPattern = Pattern.compile(IP_DOMAIN);
		this.domainNamePattern = Pattern.compile(DOMAIN_NAME);
	}

	@Override
	public MailAddress create(String mailAddressString) throws MailParseException {
		notBlank(mailAddressString, "mailAddressString");

		if (!isAscii(mailAddressString)) {
			throw new MailParseException(ILLEGAL_CHARACTERS, mailAddressString);
		}

		Matcher emailMatcher = this.emailPattern.matcher(mailAddressString);
		if (!emailMatcher.matches()) {
			throw new MailParseException(ILLEGAL_EMAIL_FORMAT, mailAddressString);
		}

		String localPart = emailMatcher.group(1);
		String domain = emailMatcher.group(2);

		DomainFormat domainFormat = checkDomain(domain);
		checkLocalPart(localPart);

		return new SimpleMailAddress(localPart, domain, domainFormat, mailAddressString);
	}

	private void checkLocalPart(String localPart) throws MailParseException {
		Matcher matcher = this.userPattern.matcher(localPart);

		if (!matcher.matches()) {
			throw new MailParseException(INVALID_LOCAL_PART, localPart);
		}
	}

	protected DomainFormat checkDomain(String domain) throws MailParseException {
		Matcher ipDomainMatcher = this.ipDomainPattern.matcher(domain);

		if (ipDomainMatcher.matches()) {
			checkIpDomain("[" + ipDomainMatcher.group(1) + "]");
			return IP;
		}
		else {
			checkDomainName(domain);
			return NAME;
		}
	}

	protected void checkDomainName(String domainName) throws MailParseException {
		Matcher domainNameMatcher = this.domainNamePattern.matcher(domainName);

		if (!domainNameMatcher.matches()) {
			throw new MailParseException(ILLEGAL_DOMAIN_NAME_FORMAT, domainName);
		}

		String topLevelDomain = domainNameMatcher.group(domainNameMatcher.groupCount());
		if (topLevelDomain.startsWith(".")) {
			topLevelDomain = topLevelDomain.substring(1);
		}

		if (!this.topLevelDomainChecker.isValidTopLevelDomain(topLevelDomain)) {
			throw new MailParseException(UNKNOWN_TOPLEVEL_DOMAIN, topLevelDomain);
		}
	}

	/**
	 * This is a simple container implementation of the <code>MailAddress</code> interface.
	 * 
	 * @author Niclas Meier
	 */
	final class SimpleMailAddress extends AbstractMailAddress {

		protected final String domain;

		SimpleMailAddress(String localPart, String domain, DomainFormat domainFormat, String original) {
			super(localPart, domainFormat, original);
			this.domain = domain;
		}

		@Override
		public String getDomain() {
			return this.domain;
		}

	}

}
