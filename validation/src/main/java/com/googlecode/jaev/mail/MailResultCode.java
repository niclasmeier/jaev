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

import static com.googlecode.jaev.ResultCode.Type.LOGICAL;

import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.ResultCodeHelper;

/**
 * Common class to provide result codes related to e-mail address creation
 * 
 * @author Niclas Meier
 */
public enum MailResultCode implements ResultCode {
	/** The e-mail contains non ASCII characters */
	ILLEGAL_CHARACTERS(0, LOGICAL),
	/** The format of the e-mail does not match the format ^\\s*?(.+)@(.+?)\\s*$ */
	ILLEGAL_EMAIL_FORMAT(1, LOGICAL),
	/** The local part (user name) is not valid */
	INVALID_LOCAL_PART(2, LOGICAL),
	/** The format of the domain name is not valid */
	ILLEGAL_DOMAIN_NAME_FORMAT(3, LOGICAL),
	/**
	 * The top level domain is not registered at the IANA
	 * (http://data.iana.org/TLD/tlds-alpha-by-domain.txt)
	 */
	UNKNOWN_TOPLEVEL_DOMAIN(4, LOGICAL),
	/** The format of the domain IP is not valid */
	ILLEGAL_DOMAIN_IP_FORMAT(5, LOGICAL)

	;

	private final int code;

	private final Type type;

	MailResultCode(int code, Type type) {
		this.code = code;
		this.type = type;
		ResultCodeHelper.register(this);
	}

	public Category getCategory() {
		return ResultCode.Category.MAIL;
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
