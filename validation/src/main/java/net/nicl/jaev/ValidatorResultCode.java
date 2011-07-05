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

package net.nicl.jaev;

import static net.nicl.jaev.ResultCode.Category.VALIDATION;
import static net.nicl.jaev.ResultCode.Type.LOGICAL;
import static net.nicl.jaev.ResultCode.Type.TECHNICAL;

/**
 * Common class to provide general result codes
 * 
 * @author Niclas Meier
 */
public enum ValidatorResultCode implements ResultCode {
	/** The email address is valid */
	ADDRESS_VALID(0, LOGICAL),
	/** The email address is not known to the destination mail server */
	ADDRESS_UNKNOWN(1, LOGICAL),
	/** The the domain has no mail exchange (MX) record in the DNS */
	NO_MX_RECORDS_FOUND(2, LOGICAL),
	/** The the domain has no mail exchange (MX) record in the DNS */
	NO_MTA_ACCESSIBLE(3, TECHNICAL),
	/** A timeout occurred during the validation */
	VALIDATION_TIMED_OUT(4, TECHNICAL),
	/** Groups of e-mail addresses are not supported */
	EMAIL_GROUPS_NOT_SUPPORTED(5, LOGICAL),
	/** An unexpected error occurred during the validation */
	GENERAL_VALIDATION_ERROR(99, TECHNICAL);

	private final int code;

	private final Type type;

	ValidatorResultCode(int code, Type type) {
		this.code = code;
		this.type = type;
		ResultCodeHelper.register(this);
	}

	public Category getCategory() {
		return VALIDATION;
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
