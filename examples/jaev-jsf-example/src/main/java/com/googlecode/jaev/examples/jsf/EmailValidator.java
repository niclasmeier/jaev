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

package com.googlecode.jaev.examples.jsf;

import static net.nicl.jaev.integration.Initialiser.defaultFromAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.nicl.jaev.integration.jsf.DefaultEmailAddressValidator;
import net.nicl.jaev.mail.MailParseException;


public class EmailValidator extends DefaultEmailAddressValidator
{
	private static final Logger LOG =
		LoggerFactory.getLogger(EmailValidator.class);

	public EmailValidator() throws MailParseException
	{
		super(defaultFromAddress());
		LOG.info("Initialised JSF validator ...");
	}

}
