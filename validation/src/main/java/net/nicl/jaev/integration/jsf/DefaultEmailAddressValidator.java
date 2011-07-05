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

package net.nicl.jaev.integration.jsf;

import net.nicl.jaev.BasicValidator;
import net.nicl.jaev.dns.SimpleResolver;
import net.nicl.jaev.integration.ResultTranslator;
import net.nicl.jaev.integration.StandardMappingTransator;
import net.nicl.jaev.mail.MailParseException;
import net.nicl.jaev.mail.SimpleMailAddressFactory;
import net.nicl.jaev.smtp.BasicAccountQuery;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static net.nicl.jaev.Acceptance.SIMPLE;

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
