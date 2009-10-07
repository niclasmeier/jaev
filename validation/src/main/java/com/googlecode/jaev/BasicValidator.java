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

package com.googlecode.jaev;

import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_VALID;
import static com.googlecode.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static com.googlecode.jaev.ValidatorResultCode.NO_MTA_ACCESSIBLE;
import static com.googlecode.jaev.ValidatorResultCode.NO_MX_RECORDS_FOUND;
import static com.googlecode.jaev.Validity.INVALID;
import static com.googlecode.jaev.Validity.SYNTAX;
import static com.googlecode.jaev.dns.ResouceRecord.Type.MX;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.dns.Resolver;
import com.googlecode.jaev.dns.ResolverException;
import com.googlecode.jaev.dns.ResouceRecord;
import com.googlecode.jaev.mail.MailAddressFactory;
import com.googlecode.jaev.mail.MailParseException;
import com.googlecode.jaev.smtp.AccountQuery;
import com.googlecode.jaev.smtp.BasicAccountQuery;
import com.googlecode.jaev.smtp.NonQuery;

/**
 * <p>
 * This <code>ValidatorService</code> implementation performs a quite basic e-mail address validation.
 * </p>
 * <p>
 * It uses a <code>Resolver</code> to fetch the MX records for a domain from the DNS and queries each mail server (MTA)
 * if the account is known. If the mail address has an IP domain format the IP address will be queried directly
 * </p>
 * <p>
 * <i>Note:</i> If no <code>Resolver</code> or <code>AccountQuery</code> is specified the validation will be aborted
 * with <code>ADDRESS_VALID</code> at the proper position in the validation process. The maximum reachable validity will
 * be granted to the result.
 * </p>
 * 
 * @author Niclas Meier
 */
public final class BasicValidator extends AbstractValidator {

	private static final Logger LOG = LoggerFactory.getLogger(BasicValidator.class);

	private final Resolver resolver;

	private final MailAddress fromAddress;

	private final AccountQuery addressQuery;

	public BasicValidator(MailAddressFactory mailAddressFactory, Resolver resolver, String fromAddress)
			throws MailParseException {
		this(mailAddressFactory, resolver, new BasicAccountQuery(10, SECONDS), fromAddress);
	}

	public BasicValidator(MailAddressFactory mailAddressFactory, Resolver resolver, AccountQuery addressQuery,
			String fromAddress) throws MailParseException {
		this(mailAddressFactory, resolver, addressQuery, mailAddressFactory.create(fromAddress));
	}

	public BasicValidator(MailAddressFactory mailAddressFactory, Resolver resolver, AccountQuery addressQuery,
			MailAddress fromAddress) {
		super(mailAddressFactory);
		this.resolver = resolver;
		this.fromAddress = fromAddress;
		this.addressQuery = addressQuery != null ? addressQuery : NonQuery.getInstance();
	}

	/**
	 * This private method to retrieve a list of <code>InetAddress</code>s for
	 * all MX hosts known for a domain
	 * 
	 * @param domain
	 *            The domain to find the MX addresses for
	 * @return A list of <code>InetAddress</code>
	 * @throws ResolverException
	 *             If a problem during MX record retrival occurs
	 */
	private List<InetAddress> retrieveMxRecords(String domain) throws ResolverException {
		// ask the resolver for the resource records
		List<ResouceRecord> recordList = this.resolver.resolve(domain, MX);

		List<InetAddress> result = new java.util.ArrayList<InetAddress>(recordList.size());

		for (ResouceRecord record : recordList) {
			try {
				// create the InetAddress instance
				result.add(InetAddress.getByName(record.getValue()));
			}
			catch (UnknownHostException e) {

				// and ignore the records with unknown hosts
				LOG.trace("Ignoring unknown host '{}' retrieved in the MX " + "records for domain '{}'.", record
						.getValue(), domain);
			}
		}

		LOG.trace("Retrieved {} as MTAs for the domain '{}'.", result, domain);

		return result;
	}

	@Override
	public Result validate(MailAddress mailAddress) {
		List<InetAddress> mxRecords;
		// discover the the list of records to check depending on the domain
		// format
		switch (mailAddress.getDomainFormat()) {
		case NAME:
			// try to use the resolver
			if (this.resolver == null) {
				// if we have no resolver, the validation will be aborted
				// and the address considered valid with semantic validity
				return Result.create(ADDRESS_VALID, SYNTAX, mailAddress);
			}

			try {
				// receive the InetAddresses for all, in the DNS registered,
				// MX
				// entries
				mxRecords = retrieveMxRecords(mailAddress.getDomain());
			}
			catch (ResolverException re) {
				// if we cannot get the list, abort validation, pass the
				// result
				// code,
				// message and assume semantic validity. Domain validity is
				// not
				// granted because an empty result list would indicate this.
				return Result.create(re.getResultCode(), SYNTAX, mailAddress, re.getItems());
			}

			if (mxRecords == null || mxRecords.isEmpty()) {
				// if no mxRecors could be gathered, abort validation and
				// assume semantic valitiy
				return Result.create(NO_MX_RECORDS_FOUND, SYNTAX, mailAddress, mailAddress.getDomain());
			}
			break;
		case IP:
			// use the IP
			String ip = mailAddress.getDomain();
			try {
				// strip the [] from the IP
				ip = ip.substring(1, ip.length() - 1);
				// and fetch the address
				mxRecords = Arrays.asList(InetAddress.getByName(ip));
			}
			catch (UnknownHostException e) {
				// if the IP couldn't be resolved, abort validation and
				// assume semantic validity
				return Result.create(NO_MTA_ACCESSIBLE, SYNTAX, mailAddress, ip);
			}
			catch (IndexOutOfBoundsException e) {
				// if it's an IP format and the [] couldn't be stripped
				// send a GENERAL_VALIDATION_ERROR and assume no validity
				return Result.create(GENERAL_VALIDATION_ERROR, INVALID, mailAddress, ip);
			}
			break;
		default:
			throw new IllegalArgumentException("Illegal domain name format '" + mailAddress.getDomainFormat()
					+ "' valid values are IP or NAME.");
		}

		// get an iterator of iteration over each MX record
		Iterator<InetAddress> mxAddressIterator = mxRecords.iterator();
		Result result = null;

		while (mxAddressIterator.hasNext() && (result == null || !ADDRESS_VALID.equals(result.getResultCode()))) {
			// while we have more MX entries and there is no result or the
			// result does not meet the requirements
			InetAddress mxAddress = mxAddressIterator.next();

			// query the address and fetch a "better" result.
			result = this.addressQuery.query(mailAddress, this.fromAddress, mxAddress);
		}

		// finally create a result
		if (result == null) {
			// if we still have no result somebody violated the interface
			// contracts strictly speaking. The address query should return
			// a result.
			// In this case we signal, that no MTA is accessible and assume
			// a semantic validity
			return Result.create(NO_MTA_ACCESSIBLE, SYNTAX, mailAddress, mailAddress.getDomain());
		}
		else {
			// if the address query returned a result, simply pass it.
			return result;
		}
	}

	@Override
	public Resolver getResolver() {
		return this.resolver;
	}

	@Override
	public AccountQuery getAccountQuery() {
		return this.addressQuery;
	}

	static class Factory implements ValidatorFactory {

		private final Validator validatorService;

		public Factory(MailAddressFactory mailAddressFactory, Resolver resolver, AccountQuery accountQuery,
				MailAddress fromAddress) {
			this.validatorService = new BasicValidator(mailAddressFactory, resolver, accountQuery, fromAddress);
		}

		@Override
		public Validator getValidator() {
			return this.validatorService;
		}

	}

}
