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
import com.googlecode.jaev.mail.MailParseException;
import com.googlecode.jaev.smtp.AccountQuery;

/**
 * The validator factory builder is used to create a validator factory. The
 * buider accepts lots of configuration parameter for the later on validator
 * creation.
 * 
 * @author Niclas Meier
 */
public interface ValidatorFactoryBuilder {

	/**
	 * Build the <code>ValidatorFactory</code>
	 * 
	 * @return A <code>ValidatorFactory</code> which uses the specified
	 *         parameter.
	 */
	ValidatorFactory buildFactory();

	/**
	 * Sets the from address for the <code>Validator</code>.
	 * 
	 * @param fromAddress
	 *            The from address as string
	 * @return The <code>ValidatorFactoryBuilder</code> instance
	 * @throws FromAddressParseException
	 *             if the from address cannot be parsed
	 */
	ValidatorFactoryBuilder fromAddress(String fromAddress) throws FromAddressParseException;

	/**
	 * Sets mail address factory to use
	 * 
	 * @param mailAddressFactory
	 *            The mail address factory to use
	 * @return The <code>ValidatorFactoryBuilder</code> instance
	 */
	ValidatorFactoryBuilder mailAddressFactory(MailAddressFactory mailAddressFactory);

	/**
	 * Sets the resolver
	 * 
	 * @param resolver
	 *            The resolver to use
	 * @return The <code>ValidatorFactoryBuilder</code> instance
	 */
	ValidatorFactoryBuilder resolver(Resolver resolver);

	/**
	 * Sets the account query instance to use
	 * 
	 * @param accountQuery
	 *            The account query
	 * @return The <code>ValidatorFactoryBuilder</code> instance
	 */
	ValidatorFactoryBuilder accountQuery(AccountQuery accountQuery);

	/**
	 * Uses the <code>NonQuery</code> instance to prevent account querying.
	 * 
	 * @return The <code>ValidatorFactoryBuilder</code> instance
	 */
	ValidatorFactoryBuilder withoutAccountQuery();

	/**
	 * This exception indicates a problem while parsing the from address.
	 */
	public class FromAddressParseException extends RuntimeException {

		/**
		 * The serialVersionUID
		 */
		private static final long serialVersionUID = -2044306347065470219L;

		public FromAddressParseException(String message, MailParseException mailParseException) {
			super(message, mailParseException);
		}

		/**
		 * Returns the originating mail parse exception.
		 * 
		 * @return The same as <code>getCause()</code>
		 */
		public MailParseException getMailParseException() {
			return (MailParseException) getCause();
		}
	}
}
