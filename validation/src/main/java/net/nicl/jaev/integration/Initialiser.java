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

package net.nicl.jaev.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public final class Initialiser {

	private static final Logger LOG = LoggerFactory.getLogger(Initialiser.class);

	public static String defaultFromAddress() {
		String fromAddress;
		try {
			fromAddress = String.valueOf(new InitialContext().lookup("java:comp/env/jaev/from_address"));
		}
		catch (NamingException e) {
			fromAddress = null;

		}
		if (fromAddress == null || fromAddress.isEmpty()) {
			fromAddress = System.getenv("JAEV_FROM_ADDRESS");
		}
		if (fromAddress == null) {
			fromAddress = System.getProperty("jaev.from_address");
		}

		if (fromAddress == null || fromAddress.isEmpty()) {
			throw new IllegalStateException("The from address could not be found. Please configure a from "
					+ "address with the JNDI entry 'java:comp/env/jaev/from_address', "
					+ "the environment property 'JAEV_FROM_ADDRESS'"
					+ " or the java virtual machine parameter '-Djaev.from_address'.");
		}
		else {
			LOG.debug("Initialized default from address with value '{}'.", fromAddress);

			return fromAddress;
		}
	}
}
