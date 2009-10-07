package com.googlecode.jaev.smtp;

import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_UNKNOWN;
import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_VALID;
import static com.googlecode.jaev.Validity.ACCESSIBLE;
import static com.googlecode.jaev.Validity.ACCOUNT;
import static com.googlecode.jaev.Validity.DOMAIN;
import static com.googlecode.jaev.integration.Initialiser.defaultFromAddress;
import static com.googlecode.jaev.smtp.Reply.Code.LOCAL_ERROR_IN_PROCESSING;
import static com.googlecode.jaev.smtp.Reply.Code.MAILBOX_UNAVAILABE;
import static com.googlecode.jaev.smtp.Reply.Code.REQUESTED_MAIL_ACTION_OKAY;
import static com.googlecode.jaev.smtp.Reply.Code.SERVICE_NOT_AVAILABLE;
import static com.googlecode.jaev.smtp.Reply.Code.SERVICE_READY;
import static com.googlecode.jaev.smtp.SmtpResultCode.IO_ERROR_DURING_MTA_CONVERSATION;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_DOES_NOT_ACCEPT_FROM_DOMAIN;
import static com.googlecode.jaev.smtp.SmtpResultCode.MTA_NOT_RESPONDING;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jaev.MailAddress;
import com.googlecode.jaev.Result;
import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.mail.SimpleMailAddressFactory;

@RunWith(JMock.class)
public final class AccountQueryTestCase {

	private final Mockery context = new JUnit4Mockery();

	@Test
	public void test_ADDRESS_VALID() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(SERVICE_READY, null)));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.HELO.particularise(fromAddress.getDomain()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(REQUESTED_MAIL_ACTION_OKAY, "")));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.MAIL.particularise(fromAddress.toString()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(REQUESTED_MAIL_ACTION_OKAY, "")));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.RECIPIENT_TO.particularise(mailAddress.toString()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(REQUESTED_MAIL_ACTION_OKAY, "")));
				inSequence(commandSequence);

				oneOf(conversation).getMxAddress();
				will(returnValue(localhost));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.QUIT);
				inSequence(commandSequence);

				oneOf(conversation).end();
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) ADDRESS_VALID));
		assertTrue(result.getValidity().implies(ACCESSIBLE));
	}

	@Test
	public void test_ADDRESS_UNKNOWN() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(SERVICE_READY, null)));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.HELO.particularise(fromAddress.getDomain()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(REQUESTED_MAIL_ACTION_OKAY, "")));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.MAIL.particularise(fromAddress.toString()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(REQUESTED_MAIL_ACTION_OKAY, "")));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.RECIPIENT_TO.particularise(mailAddress.toString()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(MAILBOX_UNAVAILABE, "")));
				inSequence(commandSequence);

				oneOf(conversation).getMxAddress();
				will(returnValue(localhost));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.QUIT);
				inSequence(commandSequence);

				oneOf(conversation).end();
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) ADDRESS_UNKNOWN));
		assertTrue(result.getValidity().implies(DOMAIN));
		assertFalse(result.getValidity().implies(ACCOUNT));
		assertFalse(result.getValidity().implies(ACCESSIBLE));
	}

	@Test
	public void test_MTA_NOT_RESPONDING_1() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(SERVICE_NOT_AVAILABLE, null)));
				inSequence(commandSequence);

				oneOf(conversation).getMxAddress();
				will(returnValue(localhost));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.QUIT);
				inSequence(commandSequence);

				oneOf(conversation).end();
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) MTA_NOT_RESPONDING));
		assertTrue(result.getValidity().implies(DOMAIN));
		assertFalse(result.getValidity().implies(ACCOUNT));
		assertFalse(result.getValidity().implies(ACCESSIBLE));
	}

	@Test
	public void test_MTA_NOT_RESPONDING_2() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				will(throwException(new ConnectException("Mocked connection failure.")));
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) MTA_NOT_RESPONDING));
		assertTrue(result.getValidity().implies(DOMAIN));
		assertFalse(result.getValidity().implies(ACCOUNT));
		assertFalse(result.getValidity().implies(ACCESSIBLE));
	}

	@Test
	public void test_IO_ERROR_DURING_MTA_CONVERSATION() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				will(throwException(new IOException("Mocked IO failure.")));
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) IO_ERROR_DURING_MTA_CONVERSATION));
		assertTrue(result.getValidity().implies(DOMAIN));
		assertFalse(result.getValidity().implies(ACCOUNT));
		assertFalse(result.getValidity().implies(ACCESSIBLE));
	}

	@Test
	public void test_MTA_DOES_NOT_ACCEPT_FROM_DOMAIN() throws Exception {
		SimpleMailAddressFactory addressFactory = new SimpleMailAddressFactory();
		final MailAddress fromAddress = addressFactory.create(defaultFromAddress());
		final MailAddress mailAddress = addressFactory.create("jaev@googlecode.com");
		final Sequence commandSequence = this.context.sequence("command");
		final ConversationFactory conversationFactory = this.context.mock(ConversationFactory.class);
		final Conversation conversation = this.context.mock(Conversation.class);
		final InetAddress localhost = InetAddress.getLocalHost();

		BasicAccountQuery accountQuery = new BasicAccountQuery(conversationFactory);

		this.context.checking(new Expectations() {

			{
				allowing(conversationFactory).createConversation();
				will(returnValue(conversation));

				oneOf(conversation).init(localhost, 25);
				inSequence(commandSequence);

				oneOf(conversation).start();
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(SERVICE_READY, null)));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.HELO.particularise(fromAddress.getDomain()));
				inSequence(commandSequence);

				oneOf(conversation).listen();
				will(returnValue(new Reply(LOCAL_ERROR_IN_PROCESSING, "")));
				inSequence(commandSequence);

				oneOf(conversation).getMxAddress();
				will(returnValue(localhost));
				inSequence(commandSequence);

				oneOf(conversation).say(Command.QUIT);
				inSequence(commandSequence);

				oneOf(conversation).end();
				inSequence(commandSequence);

				oneOf(conversation).isActive();
				will(returnValue(false));
				inSequence(commandSequence);

			}
		});

		Result result = accountQuery.query(mailAddress, fromAddress, localhost);
		assertThat(result.getResultCode(), is((ResultCode) MTA_DOES_NOT_ACCEPT_FROM_DOMAIN));
		assertTrue(result.getValidity().implies(DOMAIN));
		assertFalse(result.getValidity().implies(ACCOUNT));
		assertFalse(result.getValidity().implies(ACCESSIBLE));
	}

}
