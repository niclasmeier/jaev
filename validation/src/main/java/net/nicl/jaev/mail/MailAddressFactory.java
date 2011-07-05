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

package net.nicl.jaev.mail;

import net.nicl.jaev.MailAddress;

/**
 * The <code>MailAddressFactory</code> is used to create and validate an e-mail
 * address. It creates a syntactically valid e-mail address or throws a <code>MailParseException</code> containing a
 * <code>Result</code> with further
 * information.
 * 
 * @author Niclas Meier
 */
public interface MailAddressFactory {

	/**
	 * Creates a syntactic correct <code>MailAddress</code>.
	 * 
	 * @param mailAddress
	 *            The mail address as string
	 * @return A <code>MailAddress</code> instance
	 * @throws MailParseException
	 *             if a problem during the construction process
	 *             occurs.
	 */
	public MailAddress create(String mailAddress) throws MailParseException;

}
