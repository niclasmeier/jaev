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

package com.googlecode.jaev.smtp;

import java.net.InetAddress;

import com.googlecode.jaev.MailAddress;
import com.googlecode.jaev.Result;

/**
 * The account query performs the query for an e-mail account to the MTA.
 * 
 * @author Niclas Meier
 */
public interface AccountQuery {

	/**
	 * Performs the query
	 * 
	 * @param mailAddress
	 *            The mail address to verify
	 * @param fromAddress
	 *            The from address for authentication at the MTA
	 * @param mxAddress
	 *            The network address of the target mail server.
	 * @return The result of the query.
	 */
	public Result query(MailAddress mailAddress, MailAddress fromAddress, InetAddress mxAddress);

}
