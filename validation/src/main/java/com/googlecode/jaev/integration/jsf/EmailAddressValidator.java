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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.googlecode.jaev.Acceptance;
import com.googlecode.jaev.Result;
import com.googlecode.jaev.Validator;
import com.googlecode.jaev.integration.ResultTranslator;

public abstract class EmailAddressValidator implements javax.faces.validator.Validator {

	private final Validator validatorService;

	private final ResultTranslator resultTranslator;

	private final Acceptance checkStrategy;

	protected EmailAddressValidator(Validator validatorService, ResultTranslator resultTranslator,
			Acceptance checkStrategy) {

		this.validatorService = validatorService;
		this.resultTranslator = resultTranslator;
		this.checkStrategy = checkStrategy;
	}

	@Override
	public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
		String emailAddress = String.valueOf(value);

		Result validationResult = this.validatorService.validate(emailAddress);

		if (!this.checkStrategy.accept(validationResult)) {
			String code = this.resultTranslator.translate(validationResult);

			FacesMessage message = new FacesMessage(createMessage(code, validationResult.getObjects(), facesContext));

			throw new ValidatorException(message);
		}

	}

	protected abstract String createMessage(String code, Object[] objects, FacesContext facesContext);

}
