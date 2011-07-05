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
import static com.googlecode.jaev.ValidatorResultCode.EMAIL_GROUPS_NOT_SUPPORTED;
import static com.googlecode.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static com.googlecode.jaev.ValidatorResultCode.NO_MTA_ACCESSIBLE;
import static com.googlecode.jaev.ValidatorResultCode.NO_MX_RECORDS_FOUND;
import static com.googlecode.jaev.ValidatorResultCode.VALIDATION_TIMED_OUT;
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

import java.util.Map;

import com.googlecode.jaev.ResultCode;

/**
 * The standard mapping translator translates the known result codes to strings
 * 
 * @author Niclas Meier
 */
public final class StandardMappingTransator extends MappingTranslator {

	@Override
	protected Map<ResultCode, String> initTranslationTable(Map<ResultCode, String> translationTable) {
		initValidationResultCode(translationTable);
		initDnsResultCode(translationTable);
		initMailResultCode(translationTable);
		initSmtpResultCode(translationTable);

		return translationTable;
	}

	private void initValidationResultCode(Map<ResultCode, String> translationTable) {
		add(ADDRESS_VALID, "valid", translationTable);
		add(ADDRESS_UNKNOWN, "unknown", translationTable);
		add(NO_MX_RECORDS_FOUND, "no_mailserver_found", translationTable);
		add(NO_MTA_ACCESSIBLE, "no_mailserver_found", translationTable);
		add(VALIDATION_TIMED_OUT, "failed", translationTable);
		add(GENERAL_VALIDATION_ERROR, "failed", translationTable);
		add(EMAIL_GROUPS_NOT_SUPPORTED, "groups_not_supported", translationTable);
	}

	private void initDnsResultCode(Map<ResultCode, String> translationTable) {
		add(DOMAIN_NAME_NOT_FOUND, "domain_not_found", translationTable);
		add(DNS_TIMEOUT, "failed", translationTable);
		add(GENERAL_DNS_ERROR, "failed", translationTable);
	}

	private void initMailResultCode(Map<ResultCode, String> translationTable) {
		add(ILLEGAL_CHARACTERS, "invalid_chararcters", translationTable);
		add(ILLEGAL_EMAIL_FORMAT, "invalid_format", translationTable);
		add(INVALID_LOCAL_PART, "invalid_username", translationTable);
		add(ILLEGAL_DOMAIN_NAME_FORMAT, "invalid_domain", translationTable);
		add(ILLEGAL_DOMAIN_IP_FORMAT, "invalid_domain", translationTable);
		add(UNKNOWN_TOPLEVEL_DOMAIN, "invalid_toplevel_domain", translationTable);
	}

	private void initSmtpResultCode(Map<ResultCode, String> translationTable) {
		add(MTA_NOT_RESPONDING, "failed", translationTable);
		add(IO_ERROR_DURING_MTA_CONVERSATION, "failed", translationTable);
		add(TIMEOUT_DURING_MTA_CONVERSATION, "failed", translationTable);
		add(MTA_DOES_NOT_ACCEPT_FROM_DOMAIN, "failed", translationTable);
		add(MTA_DOES_NOT_ACCEPT_FROM_ADDRESS, "failed", translationTable);
		add(MTA_DOES_NOT_ACCEPT_RECIEPIENT, "failed", translationTable);
		add(MTA_SUSPECTS_SPAM, "failed", translationTable);
	}

	private void add(ResultCode resultCode, String code, Map<ResultCode, String> translationTable) {
		translationTable.put(resultCode, code);
	}

}
