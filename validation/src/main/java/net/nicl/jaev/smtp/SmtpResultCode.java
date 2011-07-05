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

package com.googlecode.jaev.smtp;

import static com.googlecode.jaev.ResultCode.Type.TECHNICAL;

import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.ResultCodeHelper;

/**
 * Class to provide SMTP communication result codes
 * 
 * @author Niclas Meier
 */
public enum SmtpResultCode implements ResultCode {
	/** The mail transfer agent (MTA) does not want to speak with us */
	MTA_NOT_RESPONDING(0, TECHNICAL),
	/**
	 * An IO error occurred during the conversation with the mail transfer agent
	 * (MTA)
	 */
	IO_ERROR_DURING_MTA_CONVERSATION(1, TECHNICAL),
	/**
	 * A timeout occurred during the conversation with the mail transfer agent
	 * (MTA)
	 */
	TIMEOUT_DURING_MTA_CONVERSATION(2, TECHNICAL),
	/**
	 * The mail transfer agent (MTA) does not accept the configured from email
	 * domain
	 */
	MTA_DOES_NOT_ACCEPT_FROM_DOMAIN(3, TECHNICAL),
	/**
	 * The mail transfer agent (MTA) does not accept the configured from email
	 * address
	 */
	MTA_DOES_NOT_ACCEPT_FROM_ADDRESS(4, TECHNICAL),
	/**
	 * The mail transfer agent (MTA) does not accept the configured from email
	 * address
	 */
	MTA_DOES_NOT_ACCEPT_RECIEPIENT(5, TECHNICAL),
	/**
	 * The mail transfer agent (MTA) does not accept our request and suspects
	 * spam
	 */
	MTA_SUSPECTS_SPAM(6, TECHNICAL);

	private final int code;

	private final Type type;

	SmtpResultCode(int code, Type type) {
		this.code = code;
		this.type = type;
		ResultCodeHelper.register(this);
	}

	public Category getCategory() {
		return ResultCode.Category.SMTP;
	}

	public int getCode() {
		return this.code;
	}

	public String getIdentifier() {
		return ResultCodeHelper.getIdentifier(this);
	}

	@Override
	public String toString() {
		return ResultCodeHelper.toString(this);
	}

	@Override
	public Type getType() {
		return this.type;
	}
}
