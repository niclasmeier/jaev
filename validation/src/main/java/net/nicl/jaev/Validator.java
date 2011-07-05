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

import com.googlecode.jaev.dns.Resolver;
import com.googlecode.jaev.mail.MailAddressFactory;
import com.googlecode.jaev.smtp.AccountQuery;

/**
 * The mail validator service provides facilities to validate an e-mail address
 * 
 * @author Niclas Meier
 */
public interface Validator {

	/**
	 * Checks if the specified e-mail address is valid
	 * 
	 * @param mailAddress
	 *            The e-mail address to check
	 * @return The validation result
	 */
	Result validate(String mailAddress);

	/**
	 * Checks if the specified e-mail address is valid
	 * 
	 * @param mailAddress
	 *            The e-mail address to check
	 * @return The validation result
	 */
	Result validate(MailAddress mailAddress);

	/**
	 * The mail address factory to use for e-mail address instantiation
	 * 
	 * @return The e-mail address factory
	 */
	MailAddressFactory getMailAddressFactory();

	/**
	 * The DNS resolver to use for domain verification
	 * 
	 * @return The DNS resolver
	 */
	Resolver getResolver();

	/**
	 * The account query instance to use for e-mail account verification
	 * 
	 * @return The account query
	 */
	AccountQuery getAccountQuery();

}
