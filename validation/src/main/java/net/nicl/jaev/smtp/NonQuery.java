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

import net.nicl.jaev.MailAddress;
import net.nicl.jaev.Result;

import java.net.InetAddress;

import static net.nicl.jaev.ValidatorResultCode.ADDRESS_VALID;
import static net.nicl.jaev.Validity.DOMAIN;

/**
 * This simple account query implementation skips the account validation process
 * and returns a valid result with a domain validity.
 * 
 * @author Niclas Meier
 */
public final class NonQuery implements AccountQuery {

	/** The singleton instance */
	private static final NonQuery INSTANCE = new NonQuery();

	/**
	 * Private constructor to prevent further instantiation
	 */
	private NonQuery() {
		super();
	}

	/**
	 * Singleton factory method
	 * 
	 * @return The singleton instance
	 */
	public static AccountQuery getInstance() {
		return INSTANCE;
	}

	@Override
	public Result query(MailAddress mailAddress, MailAddress fromAddress, InetAddress mxAddress) {
		return Result.create(ADDRESS_VALID, DOMAIN, mailAddress);
	}

}
