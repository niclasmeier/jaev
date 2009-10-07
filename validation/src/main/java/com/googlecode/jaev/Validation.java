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

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.googlecode.jaev.dns.Resolver;
import com.googlecode.jaev.dns.SimpleResolver;
import com.googlecode.jaev.mail.MailAddressFactory;
import com.googlecode.jaev.mail.MailParseException;
import com.googlecode.jaev.mail.SimpleMailAddressFactory;
import com.googlecode.jaev.smtp.AccountQuery;
import com.googlecode.jaev.smtp.BasicAccountQuery;
import com.googlecode.jaev.smtp.NonQuery;
import com.googlecode.jaev.smtp.TimeoutCapableAccountQueryProxy;

/**
 * The Validation class may be used to easily bootstrap a validator.
 * 
 * @author Niclas Meier
 */
public class Validation {

	/**
	 * Creates a basic <code>ValidatorFactoryBuilder</code> with <code>SimpleMailAddressFactory</code>,
	 * <code>SimpleResolver</code> and <code>BasicAccountQuery</code>.
	 * 
	 * @return A basic <code>ValidatorFactoryBuilder</code>.
	 */
	public static ValidatorFactoryBuilder basic() {
		return new BasicFactoryBuilder();
	}

	/**
	 * <p>
	 * Creates a timeout capable <code>ValidatorFactoryBuilder</code> with <code>SimpleMailAddressFactory</code>,
	 * <code>SimpleResolver</code> and <code>TimeoutCapableAccountQueryProxy</code> (which uses a
	 * <code>BasicAccountQuery</code>).
	 * </p>
	 * <p>
	 * <i>Note: </i> The time out is used on each request (DNS lookup and account query), so the overall validation time
	 * <i>is not</i> fix.
	 * </p>
	 * 
	 * @return A <code>TimeoutCapableValidatorFactoryBuilder</code> with used
	 *         timeouts on the resolver and the account query.
	 */
	public static TimeoutCapableValidatorFactoryBuilder timeout() {
		return new TimeoutFactoryBuilder();
	}

	/**
	 * <p>
	 * Creates a timeout capable <code>ValidatorFactoryBuilder</code> with <code>SimpleMailAddressFactory</code>,
	 * <code>SimpleResolver</code> and <code>BasicAccountQuery</code>).
	 * </p>
	 * <p>
	 * <i>Note: </i> The time out is used on the validator (with a <code>TimeoutCapableValidator</code>) so the overall
	 * validation time <i>is</i> fix.
	 * </p>
	 * 
	 * @return A <code>TimeoutCapableValidatorFactoryBuilder</code> with used
	 *         timeouts on the resolver and the account query.
	 */
	public static TimeoutCapableValidatorFactoryBuilder overallTimeout() {
		return new OverallTimeoutFactoryBuilder();
	}

	abstract static class AbstractFactoryBuilder implements TimeoutCapableValidatorFactoryBuilder {

		private AccountQuery accountQuery = null;

		private MailAddressFactory mailAddressFactory = null;

		private Resolver resolver = null;

		private MailAddress fromAddress = null;

		private long timeout = -1;

		private TimeUnit timeoutUnit = null;

		private int coreSize = 1;

		private int maxSize = 10;

		public AbstractFactoryBuilder() {
			this.mailAddressFactory = new SimpleMailAddressFactory();
			this.accountQuery = new BasicAccountQuery();
			this.resolver = new SimpleResolver();
		}

		@Override
		public ValidatorFactoryBuilder accountQuery(AccountQuery accountQuery) {
			this.accountQuery = accountQuery;
			return this;
		}

		@Override
		public ValidatorFactoryBuilder withoutAccountQuery() {
			return accountQuery(NonQuery.getInstance());
		}

		@Override
		public ValidatorFactory buildFactory() {
			if (this.fromAddress == null) {
				throw new IllegalStateException("The from address has to be set.");
			}

			return create(defaultMailAddressFactory(this.mailAddressFactory), defaultResolver(this.resolver,
					this.timeout, this.timeoutUnit), defaultAccountQuery(this.accountQuery, this.timeout,
					this.timeoutUnit, this.coreSize, this.maxSize), this.fromAddress, this.timeout, this.timeoutUnit,
					this.coreSize, this.maxSize);
		}

		@Override
		public ValidatorFactoryBuilder fromAddress(String fromAddress) throws FromAddressParseException {
			try {
				this.fromAddress = this.mailAddressFactory.create(fromAddress);
			}
			catch (MailParseException e) {
				throw new FromAddressParseException("Unable to parse from address '" + fromAddress + "'.", e);
			}
			return this;
		}

		@Override
		public ValidatorFactoryBuilder mailAddressFactory(MailAddressFactory mailAddressFactory) {
			if (this.fromAddress != null) {
				throw new IllegalStateException("The mail address factory must not be changed after "
						+ "setting the from address.");
			}
			this.mailAddressFactory = mailAddressFactory;
			return this;
		}

		@Override
		public ValidatorFactoryBuilder resolver(Resolver resolver) {
			this.resolver = resolver;
			return this;
		}

		@Override
		public TimeoutCapableValidatorFactoryBuilder withTimeout(long timeout, TimeUnit unit) {
			this.timeout = timeout;
			this.timeoutUnit = unit;
			return this;
		}

		@Override
		public TimeoutCapableValidatorFactoryBuilder withPool(int core, int max) {
			this.coreSize = core;
			this.maxSize = max;
			return this;
		}

		private MailAddressFactory defaultMailAddressFactory(MailAddressFactory value) {
			if (value == null) {
				return createMailAddressFactory();
			}
			else {
				return value;
			}
		}

		protected MailAddressFactory createMailAddressFactory() {
			return new SimpleMailAddressFactory();
		}

		private Resolver defaultResolver(Resolver value, long timeout, TimeUnit timeoutUnit) {
			if (value == null) {
				return createResolver(timeout, timeoutUnit);
			}
			else {
				return value;
			}
		}

		protected Resolver createResolver(long timeout, TimeUnit timeoutUnit) {
			return new SimpleResolver(3, timeout, timeoutUnit);
		}

		private AccountQuery defaultAccountQuery(AccountQuery value, long timeout, TimeUnit timeoutUnit, int coreSize,
				int maxSize) {
			if (value == null) {
				return createAccountQuery(timeout, timeoutUnit, coreSize, maxSize);
			}
			else {
				return value;
			}
		}

		protected AccountQuery createAccountQuery(long timeout, TimeUnit timeoutUnit, int coreSize, int maxSize) {
			return new BasicAccountQuery(timeout, timeoutUnit);
		}

		public abstract ValidatorFactory create(MailAddressFactory mailAddressFactory, Resolver resolver,
				AccountQuery accountQuery, MailAddress fromAddress, long timeout, TimeUnit timeoutUnit, int coreSize,
				int maxSize);
	}

	static final class BasicFactoryBuilder extends AbstractFactoryBuilder {

		@Override
		public ValidatorFactory create(MailAddressFactory mailAddressFactory, Resolver resolver,
				AccountQuery accountQuery, MailAddress fromAddress, long timeout, TimeUnit timeoutUnit, int coreSize,
				int maxSize) {
			return new BasicValidator.Factory(mailAddressFactory, resolver, accountQuery, fromAddress);
		}

	}

	static final class TimeoutFactoryBuilder extends AbstractFactoryBuilder {

		@Override
		public ValidatorFactory create(MailAddressFactory mailAddressFactory, Resolver resolver,
				AccountQuery accountQuery, MailAddress fromAddress, long timeout, TimeUnit timeoutUnit, int coreSize,
				int maxSize) {
			return new BasicValidator.Factory(mailAddressFactory, resolver, accountQuery, fromAddress);
		}

		@Override
		protected AccountQuery createAccountQuery(long timeout, TimeUnit timeoutUnit, int coreSize, int maxSize) {
			AccountQuery proxiedInstance = super.createAccountQuery(timeout / 4, timeoutUnit, coreSize, maxSize);

			ExecutorService executorService = new ThreadPoolExecutor(coreSize, maxSize, 1, MINUTES,
					new LinkedBlockingQueue<Runnable>());

			return new TimeoutCapableAccountQueryProxy(proxiedInstance, executorService, timeout, timeoutUnit);
		}
	}

	static final class OverallTimeoutFactoryBuilder extends AbstractFactoryBuilder {

		@Override
		public ValidatorFactory create(MailAddressFactory mailAddressFactory, Resolver resolver,
				AccountQuery accountQuery, MailAddress fromAddress, long timeout, TimeUnit timeoutUnit, int coreSize,
				int maxSize) {
			return new BasicValidator.Factory(mailAddressFactory, resolver, accountQuery, fromAddress);
		}

	}

}
