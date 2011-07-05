package net.nicl.jaev.integration.springframework;

import net.nicl.jaev.Result;
import net.nicl.jaev.Validation;
import net.nicl.jaev.Validator;

import javax.annotation.PostConstruct;

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
