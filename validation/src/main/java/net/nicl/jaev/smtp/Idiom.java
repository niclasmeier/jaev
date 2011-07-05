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
import net.nicl.jaev.ResultCode;
import net.nicl.jaev.Validity;
import net.nicl.jaev.smtp.Reply.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static java.util.EnumSet.of;
import static net.nicl.jaev.Utilities.isNotEmpty;
import static net.nicl.jaev.ValidatorResultCode.ADDRESS_UNKNOWN;
import static net.nicl.jaev.ValidatorResultCode.ADDRESS_VALID;
import static net.nicl.jaev.Validity.ACCESSIBLE;
import static net.nicl.jaev.Validity.DOMAIN;
import static net.nicl.jaev.smtp.Command.QUIT;
import static net.nicl.jaev.smtp.Reply.Code.*;
import static net.nicl.jaev.smtp.SmtpResultCode.*;

/**
 * The <code>Idiom</code> class groups issuing of a <code>Command</code> and the
 * listening process of the server response.
 * 
 * @author Niclas Meier
 */
public abstract class Idiom {

	private static final Logger LOG = LoggerFactory.getLogger(Idiom.class);

	private final EnumSet<Code> validReplies;

	private final EnumMap<Code, ResultCode> invalidReplies = new EnumMap<Code, ResultCode>(Code.class);

	private Idiom(EnumSet<Code> validReplies) {
		this.validReplies = validReplies;
		initInvalid(this.invalidReplies);
	}

	protected void initInvalid(Map<Code, ResultCode> invalid) {
		// does nothing
	}

	public Result phrase(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
			throws IOException {
		speak(conversation, mailAddress, fromAddress);
		Reply reply = conversation.listen();

		if (this.validReplies.contains(reply.getCode())) {
			return success(conversation, mailAddress, fromAddress);
		}
		else {
			if (isSpamSuspected(reply)) {
				return endConversation(conversation, MTA_SUSPECTS_SPAM, DOMAIN, mailAddress, mailAddress.getDomain(),
						conversation.getMxAddress());
			}
			else if (this.invalidReplies.containsKey(reply.getCode())) {
				return failure(conversation, mailAddress, fromAddress, this.invalidReplies.get(reply.getCode()));
			}
			else {
				return technical(conversation, mailAddress, fromAddress);
			}
		}

	}

	private boolean isSpamSuspected(Reply reply) {
		String message = reply.getMessage();

		return isNotEmpty(message) && message.toLowerCase().contains("spamhaus");

	}

	protected abstract void speak(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
			throws IOException;

	protected abstract Result success(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress);

	protected abstract Result technical(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress);

	protected Result failure(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress,
			ResultCode suggested) {
		return endConversation(conversation, suggested, DOMAIN, mailAddress, mailAddress.getDomain(), conversation
				.getMxAddress());
	}

	Result endConversation(Conversation conversation, ResultCode resultCode, Validity valid, Object... objects) {
		try {
			conversation.say(QUIT);
			conversation.end();
		}
		catch (IOException e) {
			LOG.trace("An error occured while closing and " + "ending the conversation.", e);
		}

		return Result.create(resultCode, valid, null, objects);
	}

	/**
	 * Initiates a conversation
	 */
	public static final Idiom START = new Idiom(of(SERVICE_READY)) {

		@Override
		protected Result technical(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			InetAddress mxAddress = conversation.getMxAddress();
			LOG.debug("Unable to initiate network connection to {}.", mxAddress);

			return endConversation(conversation, MTA_NOT_RESPONDING, DOMAIN, mxAddress);
		}

		@Override
		protected void speak(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
				throws IOException {
			conversation.start();
		}

		@Override
		protected Result success(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return null;
		}

		@Override
		protected Result failure(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress,
				ResultCode suggested) {
			return endConversation(conversation, MTA_NOT_RESPONDING, DOMAIN, conversation.getMxAddress());
		}
	};

	/**
	 * Says <code>HELO</code> to the mail server.
	 */
	public static final Idiom HELO = new Idiom(of(REQUESTED_MAIL_ACTION_OKAY)) {

		@Override
		protected Result technical(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return endConversation(conversation, MTA_DOES_NOT_ACCEPT_FROM_DOMAIN, DOMAIN, conversation.getMxAddress(),
					fromAddress);
		}

		@Override
		protected void speak(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
				throws IOException {
			conversation.say(Command.HELO.particularise(fromAddress.getDomain()));
		}

		@Override
		protected Result success(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return null;
		}

		@Override
		protected Result failure(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress,
				ResultCode suggested) {
			return endConversation(conversation, MTA_DOES_NOT_ACCEPT_FROM_DOMAIN, DOMAIN, conversation.getMxAddress(),
					fromAddress);
		}

	};

	/**
	 * Initialised the mail transfer process
	 */
	public static final Idiom MAIL = new Idiom(of(REQUESTED_MAIL_ACTION_OKAY)) {

		@Override
		protected Result technical(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return endConversation(conversation, MTA_DOES_NOT_ACCEPT_FROM_ADDRESS, DOMAIN, conversation.getMxAddress(),
					fromAddress);
		}

		@Override
		protected void speak(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
				throws IOException {
			conversation.say(Command.MAIL.particularise(fromAddress.toString()));
		}

		@Override
		protected Result success(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return null;
		}

		@Override
		protected Result failure(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress,
				ResultCode suggested) {
			return endConversation(conversation, MTA_DOES_NOT_ACCEPT_FROM_ADDRESS, DOMAIN, conversation.getMxAddress(),
					fromAddress);
		}
	};

	/**
	 * Sends the recipient address (the one, that we want to check!)
	 */
	public static final Idiom RECIPIENT_TO = new Idiom(of(REQUESTED_MAIL_ACTION_OKAY)) {

		@Override
		protected Result technical(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return endConversation(conversation, MTA_DOES_NOT_ACCEPT_RECIEPIENT, DOMAIN, mailAddress, mailAddress
					.getDomain(), conversation.getMxAddress());
		}

		@Override
		protected void speak(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress)
				throws IOException {
			conversation.say(Command.RECIPIENT_TO.particularise(mailAddress.toString()));
		}

		@Override
		protected Result success(Conversation conversation, MailAddress mailAddress, MailAddress fromAddress) {
			return endConversation(conversation, ADDRESS_VALID, ACCESSIBLE, mailAddress, conversation.getMxAddress());
		}

		@Override
		protected void initInvalid(Map<Code, ResultCode> invalid) {
			invalid.put(MAILBOX_UNAVAILABE, ADDRESS_UNKNOWN);
		}
	};
}
