package com.googlecode.jaev.integration.springframework;

import javax.annotation.PostConstruct;

import com.googlecode.jaev.Result;
import com.googlecode.jaev.Validation;
import com.googlecode.jaev.Validator;

public class ValidatorService {

	private Validator validator;

	private final String fromAddress;

	public ValidatorService(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	@PostConstruct
	public void init() {
		this.validator = Validation.basic().fromAddress(this.fromAddress).buildFactory().getValidator();
	}

	public Result validate(String emailAddress) {
		return this.validator.validate(emailAddress);
	}

	public String getFromAddress() {
		return this.fromAddress;
	}

}
