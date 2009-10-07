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

import static com.googlecode.jaev.integration.Initialiser.defaultFromAddress;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.mail.MailParseException;

public class BasicValidatorTestCase {

	private static final Logger LOG = LoggerFactory.getLogger(BasicValidatorTestCase.class);

	private static Validator validator = null;

	@BeforeClass
	public static void init() throws MailParseException {

		validator = Validation.overallTimeout().withTimeout(1, TimeUnit.MINUTES).withPool(1, 10).fromAddress(
				defaultFromAddress()).buildFactory().getValidator();

	}

	@Test
	public void test() {
		// test("jaev@jeav.googlecode.com", MTA_SUSPECTS_SPAM, DOMAIN);
	}

	private static void test(String mailAddress, ResultCode expectedCode, Validity expectedValidity) {
		Result result = validator.validate(mailAddress);
		LOG.info(mailAddress + " : " + result);
		assertThat(result.getResultCode(), is(expectedCode));
		assertThat(result.getValidity(), is(expectedValidity));
	}

}
