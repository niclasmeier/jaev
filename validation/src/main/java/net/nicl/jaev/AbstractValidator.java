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

import net.nicl.jaev.mail.MailAddressFactory;
import net.nicl.jaev.mail.MailParseException;

import static net.nicl.jaev.Check.notNull;
import static net.nicl.jaev.Validity.INVALID;

/**
 * The abstract validator service implementation provides basic implementation
 * for various methods
 * 
 * @author Niclas Meier
 */
public abstract class AbstractValidator implements Validator {

	/**
	 * Mail address factory instance. Due to the fact, that the creation of the
	 * mail factory instance is not timing relevant, the instance may be stored
	 * here.
	 */
	private final MailAddressFactory mailAddressFactory;

	/**
	 * Instance constructor
	 * 
	 * @param mailAddressFactory
	 *            The mail address factory
	 */
	protected AbstractValidator(MailAddressFactory mailAddressFactory) {
		this.mailAddressFactory = notNull(mailAddressFactory, "mailAddressFactory");
	}

	@Override
	public Result validate(String mailAddressString) {
		try {
			return validate(this.mailAddressFactory.create(mailAddressString));
		}
		catch (MailParseException mpe) {
			return Result.create(mpe.getResultCode(), INVALID, null, mpe.getItems());
		}
	}

	public abstract Result validate(MailAddress mailAddress);

	@Override
	public MailAddressFactory getMailAddressFactory() {
		return this.mailAddressFactory;
	}

}
