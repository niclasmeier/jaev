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

package com.googlecode.jaev.dns;

import static com.googlecode.jaev.ResultCode.Type.LOGICAL;
import static com.googlecode.jaev.ResultCode.Type.TECHNICAL;

import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.ResultCodeHelper;

/**
 * Class to provide result codes for DNS lookup
 * 
 * @author niclas
 */
public enum DnsResultCode implements ResultCode {
	/** The domain could not be found via DNS lookup */
	DOMAIN_NAME_NOT_FOUND(0, LOGICAL),
	/** The DNS lookup timed out */
	DNS_TIMEOUT(98, TECHNICAL),
	/** An unexpected general error occurred */
	GENERAL_DNS_ERROR(99, TECHNICAL);

	private final int code;

	private final Type type;

	DnsResultCode(int code, Type type) {
		this.code = code;
		this.type = type;
		ResultCodeHelper.register(this);
	}

	public Category getCategory() {
		return ResultCode.Category.DNS;
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
