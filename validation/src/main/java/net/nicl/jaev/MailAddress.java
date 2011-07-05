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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;

/**
 * This interface defines all necessary methods for an e-mail address
 * 
 * @author Niclas Meier
 */
public interface MailAddress {

	/**
	 * The format of the e-mail domain.
	 */
	public enum DomainFormat {
		/** The e-mail domain is an IP, i.e. <code>[10.0.0.1]</code> */
		IP,
		/** The e-mail domain is a name, i.e. <code>code.google.com</code> */
		NAME
	}

	/**
	 * Returns the local part (a.k.a. user name) of an e-mail address.
	 * 
	 * @return The local part
	 */
	public String getLocalPart();

	/**
	 * Returns the domain of an e-mai address.
	 * 
	 * @return The domain
	 */
	public String getDomain();

	/**
	 * Returns the format of the e-mai address domain. It may be an IP (i.e. <code>[10.0.0.1]</code>) or a domain (i.e.
	 * <code>code.google.com</code>)
	 * 
	 * @return The domain format
	 */
	public DomainFormat getDomainFormat();

	/**
	 * Creates an <code>InternetAddress</code>.
	 * 
	 * @return A new <code>InternetAddress</code> instance
	 * @throws AddressException
	 *             if the creation fails
	 * @throws UnsupportedEncodingException
	 *             If the encoding is not suitable to e-mail address creation
	 */
	public InternetAddress getInternetAddress() throws AddressException, UnsupportedEncodingException;

}
