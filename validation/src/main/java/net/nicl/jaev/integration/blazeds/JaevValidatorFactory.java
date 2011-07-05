package com.googlecode.jaev.integration.blazeds;

import static com.googlecode.jaev.integration.Initialiser.defaultFromAddress;
import static com.googlecode.jaev.integration.Utils.external;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.Acceptance;
import com.googlecode.jaev.MailAddress;
import com.googlecode.jaev.Result;
import com.googlecode.jaev.Validation;
import com.googlecode.jaev.Validator;
import com.googlecode.jaev.ValidatorFactory;
import com.googlecode.jaev.integration.ExternalResult;
import com.googlecode.jaev.integration.ExternalValidator;
import com.googlecode.jaev.integration.ResultTranslator;
import com.googlecode.jaev.integration.StandardMappingTransator;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.config.ConfigMap;

public class JaevValidatorFactory implements FlexFactory {

	private static final Logger LOG = LoggerFactory.getLogger(JaevValidatorFactory.class);

	private static final String SOURCE = "source";

	private ValidatorFactory validatorFactory;

	public void initialize(String id, ConfigMap configMap) {

		this.validatorFactory = createValidatorFactory(configMap);

		LOG.debug("Initialized validator factory ...");
	}

	public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
		JaevValidatorFactoryInstance instance = new JaevValidatorFactoryInstance(this, id, properties);
		instance.setSource(properties.getPropertyAsString(SOURCE, instance.getId()));
		return instance;
	}

	public Object lookup(FactoryInstance inst) {
		JaevValidatorFactoryInstance factoryInstance = (JaevValidatorFactoryInstance) inst;
		return factoryInstance.lookup();
	}

	protected ValidatorFactory createValidatorFactory(ConfigMap configMap) {
		String fromAddress = configMap.getPropertyAsString("from-address", defaultFromAddress());
		long timeout;
		TimeUnit timeoutUnit;
		int coreSize;
		int maxSize;

		try {
			timeout = Integer.parseInt(configMap.getPropertyAsString("timeout", "10"));
		}
		catch (Exception e) {
			timeout = 10;
		}

		try {
			timeoutUnit = TimeUnit.valueOf(configMap.getPropertyAsString("timeout-unit", "SECONDS").toUpperCase());
		}
		catch (Exception e) {
			timeoutUnit = SECONDS;
		}

		try {
			coreSize = Integer.parseInt(configMap.getPropertyAsString("core-size", "1"));
		}
		catch (Exception e) {
			coreSize = 1;
		}

		try {
			maxSize = Integer.parseInt(configMap.getPropertyAsString("max-size", "10"));
		}
		catch (Exception e) {
			maxSize = 10;
		}

		return Validation.overallTimeout().withTimeout(timeout, timeoutUnit).withPool(coreSize, maxSize).fromAddress(
				fromAddress).buildFactory();
	}

	class JaevValidatorFactoryInstance extends FactoryInstance {

		private final ValidatorProxy proxy;

		JaevValidatorFactoryInstance(JaevValidatorFactory factory, String id, ConfigMap properties) {
			super(factory, id, properties);
			this.proxy = new ValidatorProxy(JaevValidatorFactory.this.validatorFactory.getValidator());
		}

		@Override
		public String toString() {
			return "SpringFactory instance for id=" + getId() + " source=" + getSource() + " scope=" + getScope();
		}

		@Override
		public ExternalValidator lookup() {
			return this.proxy;
		}
	}

	public static class ValidatorProxy implements ExternalValidator {

		private final Validator validator;

		private final ResultTranslator resultTranslator;

		private final Acceptance acceptance;

		private ValidatorProxy(Validator validator) {
			this.validator = validator;
			this.resultTranslator = new StandardMappingTransator();
			this.acceptance = Acceptance.SIMPLE;
		}

		@Override
		public ExternalResult validate(String mailAddress) {
			Result result = this.validator.validate(mailAddress);
			ExternalResult external = external(result);
			external.setAccepted(this.acceptance.accept(result));
			external.setTranslatedCode(this.resultTranslator.translate(result));
			return external;
		}

		@Override
		public ExternalResult validate(MailAddress mailAddress) {
			Result result = this.validator.validate(mailAddress);
			ExternalResult external = external(result);
			external.setAccepted(this.acceptance.accept(result));
			external.setTranslatedCode(this.resultTranslator.translate(result));
			return external;
		}

	}
}
