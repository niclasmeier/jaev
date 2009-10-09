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

package com.googlecode.examples.tapestry5.pages;

import java.util.Date;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.Acceptance;
import com.googlecode.jaev.Result;
import com.googlecode.jaev.Validator;
import com.googlecode.jaev.integration.ResultTranslator;


/**
 * Start page of application jaev-tapetry5-example.
 */
public class Index
{
	private static final Logger LOG = LoggerFactory.getLogger(Index.class);

	@Inject
	private Validator validatorService;

	@Inject
	private ResultTranslator resultTranslator;

	@Inject
	private Messages messages;

	@Component
	private Form emailForm;

	@Component(id = "email")
	private TextField emailField;

	@Persist
	@Validate(value = "required")
	private String email;

	public Date getCurrentTime()
	{
		return new Date();
	}

	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	void onSubmit()
	{
		LOG.debug("entered onSubmit()");
	}

	void onValidateForm()
	{
		LOG.debug("entered onValidateForm(" + this.email + ")");

		if (this.email != null && !this.email.isEmpty())
		{
			Result result = this.validatorService.validate(this.email);
			if (!Acceptance.SIMPLE.accept(result))
			{

				this.emailForm.recordError(this.emailField, this.messages
					.format(this.resultTranslator.translate(result), result
						.getObjects()));
			}
		}
	}
}
