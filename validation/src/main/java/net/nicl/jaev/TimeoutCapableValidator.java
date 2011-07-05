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

package net.nicl.jaev;

import net.nicl.jaev.dns.Resolver;
import net.nicl.jaev.dns.SimpleResolver;
import net.nicl.jaev.mail.MailAddressFactory;
import net.nicl.jaev.mail.MailParseException;
import net.nicl.jaev.mail.SimpleMailAddressFactory;
import net.nicl.jaev.smtp.AccountQuery;
import net.nicl.jaev.smtp.BasicAccountQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static net.nicl.jaev.Check.notNull;
import static net.nicl.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static net.nicl.jaev.ValidatorResultCode.VALIDATION_TIMED_OUT;
import static net.nicl.jaev.Validity.SYNTAX;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * <p>
 * The <code>TimeoutValidatorService</code> is a partial proxy implementation of the <code>ValidatorService</code>. It
 * does not implement the algorithm to validate a <code>MailAddress</code> but it can create a <code>MailAddress</code>
 * from a string. Further validation will be delegated to another <code>ValidationServer</code> implementation.
 * </p>
 * <p>
 * The <code>TimeoutValidatorService</code> offers the possiblity to define a time out interval for the validation call.
 * It clamps the DNS lookup and the account query.
 * </p>
 * 
 * @author Niclas Meier
 */
public final class TimeoutCapableValidator extends AbstractValidator {

	/** The logger */
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutCapableValidator.class);

	/** The processing queue of the executor service */
	private final BlockingQueue<Runnable> queryQueue;

	/** The timeout */
	private final long timeout;

	/** The timeout unit */
	private final TimeUnit timeoutUnit;

	/** The executor service */
	private final ExecutorService executorService;

	/** The inner instance */
	private final Validator validatorService;

	private final ThreadLocal<Execution> executionCache = new ThreadLocal<Execution>() {

		@Override
		protected Execution initialValue() {
			return new Execution();
		}

	};

	/**
	 * Constructor for a <code>TimeoutValidatorService</code> with an executor
	 * service with core capacity of one and a max capacity of ten threads.
	 * 
	 * @param timeout
	 *            The time out
	 * @param timeoutUnit
	 *            The time unit to use for time out
	 * @param fromAddress
	 *            The from address
	 * @throws MailParseException
	 *             if the form address is invalid
	 */
	public TimeoutCapableValidator(long timeout, TimeUnit timeoutUnit, String fromAddress) throws MailParseException {
		this(1, 10, timeout, timeoutUnit, fromAddress);
	}

	/**
	 * Complex constructor for a time out validator service.
	 * 
	 * @param coreSize
	 *            The core size of the executor service thread pool
	 * @param maxSize
	 *            The max size of the executor service thread pool
	 * @param timeout
	 *            The time out
	 * @param timeoutUnit
	 *            The time unit to use for time out
	 * @param validatorService
	 *            The validator service to delegate the validation
	 *            to.
	 */
	public TimeoutCapableValidator(int coreSize, int maxSize, long timeout, TimeUnit timeoutUnit,
			Validator validatorService) {
		super(validatorService.getMailAddressFactory());
		this.queryQueue = new ArrayBlockingQueue<Runnable>(maxSize * 3);
		this.executorService = new ThreadPoolExecutor(coreSize, maxSize, 1, MINUTES, this.queryQueue);
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.validatorService = validatorService;
	}

	/**
	 * Most custom constructor for a time out validator service. The executor
	 * service will use a proccessing queue of three times the max pool size.
	 * Idle threads will be removed after 1 minute. A <code>BasicValdatorService</code> with <code>SimpleResolver</code>
	 * and <code>BasicAccountQuery</code> will be used for validation.
	 * 
	 * @param coreSize
	 *            The core size of the executor service thread pool
	 * @param maxSize
	 *            The max size of the executor service thread pool
	 * @param timeout
	 *            The time out
	 * @param timeoutUnit
	 *            The time unit to use for time out
	 * @param fromAddress
	 *            The from address
	 * @throws MailParseException
	 *             if the from address is invalid
	 */
	public TimeoutCapableValidator(int coreSize, int maxSize, long timeout, TimeUnit timeoutUnit, String fromAddress)
			throws MailParseException {
		super(new SimpleMailAddressFactory());
		this.queryQueue = new ArrayBlockingQueue<Runnable>(3 * maxSize);
		this.executorService = new ThreadPoolExecutor(coreSize, maxSize, 1, MINUTES, this.queryQueue);
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.validatorService = new BasicValidator(getMailAddressFactory(), new SimpleResolver(3, timeoutUnit
				.toMillis(timeout) / 4), new BasicAccountQuery(timeout * 3 / 4, timeoutUnit), fromAddress);
	}

	/**
	 * Most custom constructor for a time out validator service. The executor
	 * service will use a proccessing queue of three times the max pool size.
	 * Idle threads will be removed after 1 minute. A <code>BasicValdatorService</code> with <code>SimpleResolver</code>
	 * and <code>BasicAccountQuery</code> will be used for validation.
	 * 
	 * @param queryQueue
	 *            A blocking queue for queries
	 * @param executorService
	 *            An executor service for aync execution
	 * @param timeout
	 *            The time out
	 * @param timeoutUnit
	 *            The time unit to use for time out
	 */
	public TimeoutCapableValidator(Validator validatorService, BlockingQueue<Runnable> queryQueue,
			ExecutorService executorService, long timeout, TimeUnit timeoutUnit) {
		super(validatorService.getMailAddressFactory());
		this.queryQueue = queryQueue;
		this.executorService = executorService;
		this.timeout = timeout;
		this.timeoutUnit = timeoutUnit;
		this.validatorService = validatorService;
	}

	@Override
	public Result validate(MailAddress mailAddress) {
		// get execution instance form cache an initialise it.
		Execution execution = this.executionCache.get();
		execution.init(notNull(mailAddress, "mailAddress"));
		try {
			Future<Result> futureResult;
			try {
				// submit it to the executor service.
				futureResult = this.executorService.submit(execution);
			}
			catch (RejectedExecutionException ree) {
				// if the execution is rejected, threat is as time out
				return Result.create(VALIDATION_TIMED_OUT, SYNTAX, mailAddress);
			}

			// try to get the result three times
			for (int i = 0; i < 3; ++i) {
				try {
					return futureResult.get(this.timeout, this.timeoutUnit);
				}
				catch (InterruptedException e) {
					// if the result is canceled
					if (futureResult.isCancelled()) {
						// return a general error
						// FIXME: get a better result to return
						return Result.create(GENERAL_VALIDATION_ERROR, SYNTAX, mailAddress);
					}
				}
				catch (ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RuntimeException) {
						throw (RuntimeException) cause;
					}
					else if (cause instanceof Error) {
						throw (Error) cause;
					}
					else {
						throw new IllegalStateException("An unexpected exception" + " occured during validation.", e);
					}

				}
				catch (TimeoutException e) {
					// cancel the execution
					if (futureResult.cancel(true)) {
						LOG.warn("Cancelling the validation '{}' failed", mailAddress);
					}

					// return time out result and assume SEMANTIC validity
					return Result.create(VALIDATION_TIMED_OUT, SYNTAX, mailAddress);
				}
			}

			// the thread seems to be interrupted three times, so
			// we return a general error.
			return Result.create(GENERAL_VALIDATION_ERROR, SYNTAX, mailAddress);
		}
		finally {
			// finally clear the execution
			execution.clear();
		}
	}

	@Override
	public AccountQuery getAccountQuery() {
		return this.validatorService.getAccountQuery();
	}

	@Override
	public Resolver getResolver() {
		return this.validatorService.getResolver();
	}

	/**
	 * A simple callable to encapsulate the paramter
	 */
	private class Execution implements Callable<Result> {

		private MailAddress mailAddress;

		private void init(MailAddress mailAddress) {
			this.mailAddress = mailAddress;
		}

		private void clear() {
			init(null);
		}

		@Override
		public Result call() {
			return TimeoutCapableValidator.this.validatorService.validate(this.mailAddress);
		}

	}

	static class Factory implements ValidatorFactory {

		private transient final Validator validatorService;

		private final TimeUnit timeoutUnit;

		private final BasicValidator basicValidator;

		private final ExecutorService executorService;

		private final BlockingQueue<Runnable> workQueue;

		public Factory(MailAddressFactory mailAddressFactory, Resolver resolver, AccountQuery accountQuery,
				MailAddress fromAddress, long timeout, TimeUnit timeoutUnit, int coreSize, int maxSize) {
			this.timeoutUnit = timeoutUnit;
			this.basicValidator = new BasicValidator(mailAddressFactory, resolver, accountQuery, fromAddress);
			this.workQueue = new LinkedBlockingQueue<Runnable>();
			this.executorService = new ThreadPoolExecutor(coreSize, maxSize, 1, MINUTES, this.workQueue);
			this.validatorService = new TimeoutCapableValidator(this.basicValidator, this.workQueue,
					this.executorService, timeout, this.timeoutUnit);
		}

		@Override
		public Validator getValidator() {
			return this.validatorService;
		}

	}
}
