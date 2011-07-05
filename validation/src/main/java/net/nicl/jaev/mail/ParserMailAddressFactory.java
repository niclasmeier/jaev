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

import net.nicl.jaev.mail.EmailParser.AddressInfo;
import net.nicl.jaev.mail.EmailParser.ParseException;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static net.nicl.jaev.Check.notBlank;
import static net.nicl.jaev.MailAddress.DomainFormat.IP;
import static net.nicl.jaev.MailAddress.DomainFormat.NAME;
import static net.nicl.jaev.ValidatorResultCode.EMAIL_GROUPS_NOT_SUPPORTED;
import static net.nicl.jaev.mail.MailResultCode.ILLEGAL_CHARACTERS;
import static net.nicl.jaev.mail.MailResultCode.UNKNOWN_TOPLEVEL_DOMAIN;
import static net.nicl.jaev.mail.Utilites.*;

/**
 * Simple implementation of the <code>MailAddressFactory</code> interface. Some
 * regular expressions are used to validate the syntax and a <code>TopLevelDomainChecker</code> is used to validate the
 * top level domain.
 * 
 * @author Niclas Meier
 */
public class ParserMailAddressFactory implements MailAddressFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ParserMailAddressFactory.class);

	// private static final String ATOM_TEXT =
	// "\\p{Alnum}!#\\$%&'\\*\\+-/=\\?^_`\\{|\\}\\~";
	//
	// private static final String ATOM = "(" + ATOM_TEXT + ")+";
	//
	// private static final String DOT_ATOM_TEXT =
	// "(" + ATOM_TEXT + ")+(\\.(" + ATOM_TEXT + ")+)?";
	//
	// private static final String DOT_ATOM = "(" + DOT_ATOM_TEXT + ")+";

	private final TopLevelDomainChecker topLevelDomainChecker;

	private final ThreadLocal<EmailLexer> lexerCache = new ThreadLocal<EmailLexer>() {

		@Override
		protected EmailLexer initialValue() {
			return new EmailLexer();
		}

	};

	private final ThreadLocal<CommonTokenStream> tokenStreamCache = new ThreadLocal<CommonTokenStream>() {

		@Override
		protected CommonTokenStream initialValue() {
			return new CommonTokenStream();
		}

	};

	private final ThreadLocal<EmailParser> parserCache = new ThreadLocal<EmailParser>() {

		@Override
		protected EmailParser initialValue() {
			return new EmailParser(null);
		}

	};

	public ParserMailAddressFactory() {
		this(new UrlTopLevelDomainChecker(ParserMailAddressFactory.class.getClassLoader().getResource(
				"net/nicl/jaev/mail/tlds-alpha-by-domain.txt")));
	}

	public ParserMailAddressFactory(TopLevelDomainChecker topLevelDomainChecker) {
		this.topLevelDomainChecker = topLevelDomainChecker;

	}

	@Override
	public MailAddress create(String mailAddressString) throws MailParseException {
		List<MailAddress> mailAddressList = createList(mailAddressString);

		if (mailAddressList.size() != 1) {
			throw new MailParseException(EMAIL_GROUPS_NOT_SUPPORTED, mailAddressString);
		}

		return mailAddressList.get(0);
	}

	public List<MailAddress> createList(String mailAddressString) throws MailParseException {
		notBlank(mailAddressString, "mailAddressString");

		if (!isAscii(mailAddressString)) {
			throw new MailParseException(ILLEGAL_CHARACTERS, mailAddressString);
		}

		EmailLexer lexer = this.lexerCache.get();
		lexer.setCharStream(new ANTLRStringStream(mailAddressString));

		CommonTokenStream tokenStream = this.tokenStreamCache.get();
		tokenStream.setTokenSource(lexer);

		EmailParser parser = this.parserCache.get();
		parser.setTokenStream(tokenStream);

		List<AddressInfo> addressInfos;
		List<MailAddress> mailAddressList;
		try {
			addressInfos = parser.address();

			mailAddressList = new java.util.ArrayList<MailAddress>();

			for (AddressInfo info : addressInfos) {
				String phrase = trim(info.getPhrase());
				String localPart = trim(info.getLocalPart());
				List<String> domain = trim(info.getDomain());
				boolean domainContainsLiteral = info.isLiteral();

				if (LOG.isDebugEnabled()) {
					LOG.debug("Parsed e-mail address components: local-part '" + localPart + "', domain " + domain
							+ " " + (domainContainsLiteral ? "(literal)" : "")
							+ (phrase == null || phrase.isEmpty() ? "" : ", phrase '" + phrase) + "'.");
				}

				if (domainContainsLiteral) {
					checkIpDomain(domain.get(0));
				}

				MailAddress mailAddress = new MailAddress(phrase, localPart, domain, domainContainsLiteral ? IP : NAME,
						mailAddressString);

				checkTopLevelDomain(mailAddress);

				mailAddressList.add(mailAddress);
			}

			return mailAddressList;
		}
		catch (ParseException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("An error occured while parsing e-mail addresses '" + mailAddressString + "'.", e);
			}
			throw new MailParseException(MailResultCode.ILLEGAL_EMAIL_FORMAT);
		}
		catch (RecognitionException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("An error occured while parsing e-mail addresses '" + mailAddressString + "'.", e);
			}
			throw new MailParseException(MailResultCode.ILLEGAL_EMAIL_FORMAT);
		}

	}

	private MailAddress checkTopLevelDomain(MailAddress mailAddress) throws MailParseException {
		if (NAME.equals(mailAddress.getDomainFormat())
				&& !this.topLevelDomainChecker.isValidTopLevelDomain(mailAddress.getTopLevelDomain())) {
			throw new MailParseException(UNKNOWN_TOPLEVEL_DOMAIN, mailAddress.getTopLevelDomain());
		}
		return mailAddress;
	}

	private void postCheckLocalPart(MailAddress address) {
		address.getLocalPart();

	}

	/**
	 * This is a simple container implementation of the <code>MailAddress</code> interface.
	 */
	private static final class MailAddress extends AbstractMailAddress {

		private final String personal;

		private final String[] subdomains;

		private final String topLevelDomain;

		private transient String domain;

		MailAddress(String personal, String localPart, List<String> domain, DomainFormat domainFormat, String original)
				throws MailParseException {
			super(localPart, domainFormat, original);
			this.personal = personal;
			if (NAME.equals(domainFormat)) {
				if (domain.size() < 2) {
					throw new MailParseException(MailResultCode.ILLEGAL_DOMAIN_NAME_FORMAT, domain.size() == 1 ? domain
							.get(0) : null);
				}
				else {
					this.topLevelDomain = domain.remove(domain.size() - 1);
					this.subdomains = domain.toArray(new String[domain.size()]);
				}
			}
			else {
				if (domain.isEmpty()) {
					throw new MailParseException(MailResultCode.ILLEGAL_DOMAIN_IP_FORMAT);
				}
				else {
					this.topLevelDomain = domain.get(0);
					this.subdomains = new String[0];
				}
			}

		}

		public String getPersonal() {
			return this.personal;
		}

		@Override
		public InternetAddress getInternetAddress() throws AddressException, UnsupportedEncodingException {
			return new InternetAddress(getAddressSpecification(), this.personal);
		}

		@Override
		public String getDomain() {
			if (this.domain == null) {
				StringBuilder builder = new StringBuilder();
				for (String subdomain : this.subdomains) {
					builder.append(subdomain).append('.');
				}
				this.domain = builder.append(this.topLevelDomain).toString();
			}

			return this.domain;
		}

		public String[] getSubdomains() {
			return this.subdomains;
		}

		public String getTopLevelDomain() {
			return this.topLevelDomain;
		}
	}
}
