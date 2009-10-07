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

package com.googlecode.jaev.integration;

import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_UNKNOWN;
import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_VALID;
import static com.googlecode.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static com.googlecode.jaev.ValidatorResultCode.NO_MTA_ACCESSIBLE;
import static com.googlecode.jaev.ValidatorResultCode.NO_MX_RECORDS_FOUND;
import static com.googlecode.jaev.ValidatorResultCode.VALIDATION_TIMED_OUT;
import static com.googlecode.jaev.Validity.INVALID;
import static com.googlecode.jaev.dns.DnsResultCode.DNS_TIMEOUT;
import static com.googlecode.jaev.dns.DnsResultCode.DOMAIN_NAME_NOT_FOUND;
import static com.googlecode.jaev.dns.DnsResultCode.GENERAL_DNS_ERROR;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_CHARACTERS;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_DOMAIN_IP_FORMAT;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_DOMAIN_NAME_FORMAT;
import static com.googlecode.jaev.mail.MailResultCode.ILLEGAL_EMAIL_FORMAT;
import static com.googlecode.jaev.mail.MailResultCode.INVALID_LOCAL_PART;
import static com.googlecode.jaev.mail.MailResultCode.UNKNOWN_TOPLEVEL_DOMAIN;
import static com.googlecode.jaev.smtp.SmtpResultCode.IO_ERROR_DURING_MTA_CONVERSATION;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_DOES_NOT_ACCEPT_FROM_ADDRESS;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_DOES_NOT_ACCEPT_FROM_DOMAIN;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_DOES_NOT_ACCEPT_RECIEPIENT;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_NOT_RESPONDING;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_SUSPECTS_SPAM;
import static com.googlecode.jaev.smtp.SmtpResultCode.TIMEOUT_DURING_MTA_CONVERSATION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.googlecode.jaev.Result;

public class StandardMappingTransatorTestCase {

	private final ResultTranslator resultTranslator;

	public StandardMappingTransatorTestCase() {
		this.resultTranslator = new StandardMappingTransator();
	}

	@Test
	public void testValidationResultCodes() {
		assertThat(this.resultTranslator.translate(Result.create(ADDRESS_VALID, INVALID, null)), is("valid"));
		assertThat(this.resultTranslator.translate(Result.create(ADDRESS_UNKNOWN, INVALID, null)), is("unknown"));
		assertThat(this.resultTranslator.translate(Result.create(NO_MX_RECORDS_FOUND, INVALID, null)),
				is("no_mailserver_found"));
		assertThat(this.resultTranslator.translate(Result.create(NO_MTA_ACCESSIBLE, INVALID, null)),
				is("no_mailserver_found"));
		assertThat(this.resultTranslator.translate(Result.create(VALIDATION_TIMED_OUT, INVALID, null)), is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(GENERAL_VALIDATION_ERROR, INVALID, null)),
				is("failed"));
	}

	@Test
	public void testDnsResultCodes() {
		assertThat(this.resultTranslator.translate(Result.create(DNS_TIMEOUT, INVALID, null)), is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(DOMAIN_NAME_NOT_FOUND, INVALID, null)),
				is("domain_not_found"));
		assertThat(this.resultTranslator.translate(Result.create(GENERAL_DNS_ERROR, INVALID, null)), is("failed"));

	}

	@Test
	public void testMailResultCode() {
		assertThat(this.resultTranslator.translate(Result.create(ILLEGAL_CHARACTERS, INVALID, null)),
				is("invalid_chararcters"));
		assertThat(this.resultTranslator.translate(Result.create(ILLEGAL_EMAIL_FORMAT, INVALID, null)),
				is("invalid_format"));
		assertThat(this.resultTranslator.translate(Result.create(INVALID_LOCAL_PART, INVALID, null)),
				is("invalid_username"));
		assertThat(this.resultTranslator.translate(Result.create(ILLEGAL_DOMAIN_NAME_FORMAT, INVALID, null)),
				is("invalid_domain"));
		assertThat(this.resultTranslator.translate(Result.create(ILLEGAL_DOMAIN_IP_FORMAT, INVALID, null)),
				is("invalid_domain"));
		assertThat(this.resultTranslator.translate(Result.create(UNKNOWN_TOPLEVEL_DOMAIN, INVALID, null)),
				is("invalid_toplevel_domain"));
	}

	@Test
	public void testSmtpResult() {
		assertThat(this.resultTranslator.translate(Result.create(MTA_NOT_RESPONDING, INVALID, null)), is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(IO_ERROR_DURING_MTA_CONVERSATION, INVALID, null)),
				is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(TIMEOUT_DURING_MTA_CONVERSATION, INVALID, null)),
				is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(MTA_DOES_NOT_ACCEPT_FROM_DOMAIN, INVALID, null)),
				is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(MTA_DOES_NOT_ACCEPT_FROM_ADDRESS, INVALID, null)),
				is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(MTA_DOES_NOT_ACCEPT_RECIEPIENT, INVALID, null)),
				is("failed"));
		assertThat(this.resultTranslator.translate(Result.create(MTA_SUSPECTS_SPAM, INVALID, null)), is("failed"));
	}
}
