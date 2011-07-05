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

package net.nicl.jaev.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The reply class contains the response from the mail server.
 * 
 * @author Niclas Meier
 */
public class Reply {

	private static final Logger LOG = LoggerFactory.getLogger(Reply.class);

	public static final Reply EMPTY = new Reply(Code.EMPTY, null);

	public enum Code {
		UNRECOGNIZED(0), EMPTY(1), SYSTEM_STATUS(211), HELP_MESSAGE(214), SERVICE_READY(220), SERVICE_CLOSING(221), REQUESTED_MAIL_ACTION_OKAY(
				250), USER_NOT_LOCAL_FORWARD(251), CANNOT_VERIFY_USER(252), START_MAIL_INPUT(354), SERVICE_NOT_AVAILABLE(
				421), MAILBOX_UNAVAILABE(450), LOCAL_ERROR_IN_PROCESSING(451), INSUFFICENT_SYSTEM_STORAGE(452), COMMAND_UNRECOGNIZED(
				500), SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS(501), COMMAND_NOT_IMPLEMENTED(502), BAD_SEQUENCE_OF_COMMANDS(
				503), COMMAND_PARAMETER_NOT_IMPLEMENTED(504), MAILBOX_NOT_AVAIABLE(550), USER_NOT_LOCAL(551), STORAGE_EXCEEDED(
				552), MAILBOX_NAME_NOT_ALLOWED(553), TRANSACTION_FAILED(554);

		private final int value;

		Code(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return name() + "(" + this.value + ")";
		}

	}

	private final Code code;

	private final String message;

	private static final Map<String, Code> CODE_MAP = new java.util.HashMap<String, Code>();

	static {
		for (Code code : Code.values()) {
			CODE_MAP.put(String.valueOf(code.getValue()), code);
		}
	}

	protected Reply(Code code, String message) {
		this.message = message;
		this.code = code;
	}

	public static Reply create(String codeString, String message) {
		Code code = CODE_MAP.get(codeString);
		if (code == null) {
			code = Code.UNRECOGNIZED;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Reply: " + code + " (" + (message != null ? message.trim() : "") + ")");
		}

		return new Reply(code, message);
	}

	public Code getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return getCode() + ":" + getMessage();
	}

}
