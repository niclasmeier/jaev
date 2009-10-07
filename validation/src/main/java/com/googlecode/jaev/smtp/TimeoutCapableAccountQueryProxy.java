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

package com.googlecode.jaev.smtp;

import static com.googlecode.jaev.Check.notNull;
import static com.googlecode.jaev.Validity.DOMAIN;
import static com.googlecode.jaev.smtp.SmtpResultCode.IO_ERROR_DURING_MTA_CONVERSATION;
import static com.googlecode.jaev.smtp.SmtpResultCode.TIMEOUT_DURING_MTA_CONVERSATION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.MailAddress;
import com.googlecode.jaev.Result;

/**
 * This <code>AccountQuery</code> proxy implementation executes the query in a
 * defined time out.
 * 
 * @author Niclas Meier
 */
public class TimeoutCapableAccountQueryProxy implements AccountQuery {

	/**
	 * The Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutCapableAccountQueryProxy.class);

	/** The timeout of the proxy */
	private final long timeout;

	/** Executor service for query execution */
	private final ExecutorService executorService;

	/** Inner <code>AccountQuery</code> instance to use for proxy request */
	private final AccountQuery accountQuery;

	/**
	 * A simple cache for <code>AccountQueryCallable</code>s to prevent <code>AccountQueryCallable</code> instantiation.
	 */
	private final ThreadLocal<AccountQueryCallable> queryCallableCache = new ThreadLocal<AccountQueryCallable>() {

		@Override
		protected AccountQueryCallable initialValue() {
			return new AccountQueryCallable();
		}

	};

	/**
	 * Convenience constructor which creates a <code>BasicAccountQuery</code> as
	 * proxy instance to perform the real query. The time out of the <code>BasicAccountQuery</code> will be set to 75%
	 * of the specified
	 * timeout.
	 * 
	 * @param executorService
	 *            The executor service instance to use
	 * @param timeout
	 *            The time out to use for query
	 * @param timeoutUnit
	 *            The time unit to use for time out.
	 */
	public TimeoutCapableAccountQueryProxy(ExecutorService executorService, long timeout, TimeUnit timeoutUnit) {
		this(new BasicAccountQuery(timeout / 4 * 3, timeoutUnit), executorService, timeout, timeoutUnit);
	}

	/**
	 * Default constructor
	 * 
	 * @param accountQuery
	 *            The inner account query instance
	 * @param executorService
	 *            The executor service instance to use
	 * @param timeout
	 *            The time out to use for query
	 * @param timeoutUnit
	 *            The time unit to use for time out.
	 */
	public TimeoutCapableAccountQueryProxy(AccountQuery accountQuery, ExecutorService executorService, long timeout,
			TimeUnit timeoutUnit) {
		this.accountQuery = accountQuery;
		this.executorService = executorService;
		this.timeout = notNull(timeoutUnit, "timeoutUnit").toMillis(timeout);
	}

	@Override
	public Result query(MailAddress mailAddress, MailAddress fromAddress, InetAddress mxAddress) {
		// get callable from cache
		AccountQueryCallable queryCallable = this.queryCallableCache.get();

		// set values
		queryCallable.set(this.accountQuery, mxAddress, mailAddress, fromAddress);

		Future<Result> futureResult;
		try {
			// submit callable for execution
			futureResult = this.executorService.submit(queryCallable);
		}
		catch (RejectedExecutionException ree) {
			// this is not 100% correct. The executor service rejected
			// execution because the execution queue is full, but
			// does make not any difference to the validation process.
			LOG.warn("Executor service rejected execution.", ree);

			return Result.create(TIMEOUT_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
		}

		// try three times
		for (int i = 0; i < 3; ++i) {
			try {
				return futureResult.get(this.timeout, MILLISECONDS);
			}

			catch (InterruptedException e) {
				// somebody interrupted the thread

				// may occur if the executor is shut down and the result is
				// canceled.
				if (futureResult.isCancelled()) {
					Result.create(IO_ERROR_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
				}

				// if anyone else interrupted the thread just retry
			}
			catch (ExecutionException e) {
				// an exception during execution occurred
				return interpretException(mailAddress, e, mxAddress);
			}
			catch (TimeoutException e) {
				// cancel the execution
				if (futureResult.cancel(true)) {
					LOG.warn("Cancelling the account query to '" + mxAddress.getCanonicalHostName() + "' failed");
				}

				// a timeout occurred
				return Result.create(TIMEOUT_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
			}
		}

		// all three attempts to query the account failed. this only can happen,
		// if the thread is interrupted repeatedly.
		return Result.create(IO_ERROR_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
	}

	private Result interpretException(MailAddress mailAddress, ExecutionException ee, InetAddress mxAddress) {
		Throwable cause = ee.getCause();
		if (cause instanceof Error) {
			throw (Error) cause;
		}
		else if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("An unexpected error occured during MTA " + "conversation with '" + mxAddress + "'." + cause);
			}
			return Result.create(IO_ERROR_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
		}
	}

	private class AccountQueryCallable implements Callable<Result> {

		private AccountQuery accountQuery;

		private InetAddress mxAddress;

		private MailAddress mailAddress;

		private MailAddress fromAddress;

		public void set(AccountQuery accountQuery, InetAddress mxAddress, MailAddress mailAddress,
				MailAddress fromAddress) {
			this.accountQuery = accountQuery;
			this.mxAddress = mxAddress;
			this.mailAddress = mailAddress;
			this.fromAddress = fromAddress;
		}

		public void clear() {
			set(null, null, null, null);
		}

		@Override
		public Result call() throws IOException {
			return this.accountQuery.query(this.mailAddress, this.fromAddress, this.mxAddress);
		}

	}

}
