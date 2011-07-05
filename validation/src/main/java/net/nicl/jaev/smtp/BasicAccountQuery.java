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

package net.nicl.jaev.smtp;

import net.nicl.jaev.MailAddress;
import net.nicl.jaev.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static net.nicl.jaev.Check.notNull;
import static net.nicl.jaev.ValidatorResultCode.GENERAL_VALIDATION_ERROR;
import static net.nicl.jaev.Validity.DOMAIN;
import static net.nicl.jaev.smtp.Idiom.*;
import static net.nicl.jaev.smtp.SmtpResultCode.IO_ERROR_DURING_MTA_CONVERSATION;
import static net.nicl.jaev.smtp.SmtpResultCode.MTA_NOT_RESPONDING;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * This basic account query implementation performs the query to an SMTP mail
 * server. The timeout for this implementation is set on the socket, so the
 * overall timeout is round about four times the specified timeout.
 * 
 * @author Niclas Meier
 */
public class BasicAccountQuery implements AccountQuery {

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(BasicAccountQuery.class);

	/** List of idioms for the SMTP server communication */
	private static final Idiom[] IDIOMS = new Idiom[] { START, HELO, MAIL, RECIPIENT_TO };

	/** The socket timeout for the server socket */
	private final ConversationFactory conversationFactory;

	/**
	 * A very simple cache for <code>Conversation</code> instances to prevent to
	 * much instantiations. Due to the high possibility, that the basic account
	 * query will be executed in a thread pool this realization pattern is quite
	 * efficient.
	 */
	private final ThreadLocal<Conversation> conversationCache = new ThreadLocal<Conversation>() {

		@Override
		protected Conversation initialValue() {
			return BasicAccountQuery.this.conversationFactory.createConversation();
		}
	};

	/**
	 * Default constructor with a five seconds socket time out.
	 */
	public BasicAccountQuery() {
		this(5, SECONDS);
	}

	/**
	 * Default constructor
	 * 
	 * @param socketTimeoutMillis
	 *            The time out to use for the socket.
	 */
	public BasicAccountQuery(long socketTimeoutMillis) {
		this(socketTimeoutMillis, MILLISECONDS);
	}

	/**
	 * A default constructor which will create a <code>SocketChannelConversation.Factory</code> with the specified
	 * timeouts for communication.
	 * 
	 * @param socketTimeout
	 *            The time out to use for the socket.
	 * @param socketTimeoutUnit
	 *            The time unit to use for timeout
	 */
	public BasicAccountQuery(long socketTimeout, TimeUnit socketTimeoutUnit) {
		this(new SocketChannelConversation.Factory(notNull(socketTimeoutUnit, "socketTimeoutUnit").toMillis(
				socketTimeout)));
	}

	/**
	 * Default constructor
	 * 
	 * @param conversationFactory
	 *            The factory instance for conversations
	 */
	public BasicAccountQuery(ConversationFactory conversationFactory) {
		this.conversationFactory = conversationFactory;

	}

	@Override
	public Result query(MailAddress mailAddress, MailAddress fromAddress, InetAddress mxAddress) {
		// get a conversation from the cache
		Conversation conversation = createConversation();
		conversation.init(notNull(mxAddress, "mxAddress"), 25);
		Result result;

		try {
			for (Idiom idiom : IDIOMS) {
				// iterate through the idioms
				try {
					// phrase the idiom
					result = idiom.phrase(conversation, mailAddress, fromAddress);

					// if we receive a result, we got a response from the
					// server,
					// that confirms the existence (or the lack of it) of the
					// e-mail
					// account
					if (result != null) {
						// return it
						return result;
					}
				}
				catch (ConnectException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Unable to connect to MTA '" + mxAddress + "'.", e);
					}
					return Result.create(MTA_NOT_RESPONDING, DOMAIN, mailAddress, mxAddress);
				}
				catch (IOException e) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("An IO error occured during MTA conversation " + "with '" + mxAddress + "'.", e);
					}
					return Result.create(IO_ERROR_DURING_MTA_CONVERSATION, DOMAIN, mailAddress, mxAddress);
				}
			}
		}
		finally {
			if (conversation.isActive()) {
				// finally end the conversation, if it's still active
				conversation.end();
			}
		}

		// if we iterated through all idioms and received no result, something
		// very bad happened. so return a GENERAL_VALIDATION_ERROR and assume
		// that at least the domain is valid.
		return Result.create(GENERAL_VALIDATION_ERROR, DOMAIN, mailAddress, mxAddress);
	}

	/**
	 * Factory method of a conversation
	 * 
	 * @return A new "clean" <code>Conversation</code> instance.
	 */
	protected Conversation createConversation() {
		return this.conversationCache.get();
	}

}
