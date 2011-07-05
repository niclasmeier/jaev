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

package com.googlecode.jaev.integration.jsf;

import static com.googlecode.jaev.Acceptance.SIMPLE;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.googlecode.jaev.BasicValidator;
import com.googlecode.jaev.dns.SimpleResolver;
import com.googlecode.jaev.integration.ResultTranslator;
import com.googlecode.jaev.integration.StandardMappingTransator;
import com.googlecode.jaev.mail.MailParseException;
import com.googlecode.jaev.mail.SimpleMailAddressFactory;
import com.googlecode.jaev.smtp.BasicAccountQuery;

public class DefaultEmailAddressValidator extends EmailAddressValidator {

	private final String resourceBundleName;

	protected DefaultEmailAddressValidator(String fromAddress) throws MailParseException {
		this(fromAddress, new StandardMappingTransator(), DefaultEmailAddressValidator.class.getName());
	}

	protected DefaultEmailAddressValidator(String fromAddress, ResultTranslator resultTranslator,
			String resourceBundleName) throws MailParseException {
		super(new BasicValidator(new SimpleMailAddressFactory(), new SimpleResolver(), new BasicAccountQuery(),
				fromAddress), resultTranslator, SIMPLE);
		this.resourceBundleName = resourceBundleName;
	}

	@Override
	protected String createMessage(String code, Object[] objects, FacesContext facesContext) {
		Locale currentLocale = facesContext.getViewRoot().getLocale();
		ResourceBundle resourceBundle = ResourceBundle.getBundle(this.resourceBundleName, currentLocale);

		String messageString = resourceBundle.getString(code);

		if (messageString == null || messageString.isEmpty()) {
			return "? " + code + " ?";
		}
		else {
			try {

				return new MessageFormat(messageString, resourceBundle.getLocale()).format(objects);
			}
			catch (Exception e) {
				return "! " + code + " : " + e.getMessage() + " !";
			}
		}
	}
}
