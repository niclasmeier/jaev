package net.nicl.jaev.smtp;

import net.nicl.jaev.MailAddress;
import net.nicl.jaev.Result;
import net.nicl.jaev.ResultCode;
import net.nicl.jaev.mail.SimpleMailAddressFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;

import static net.nicl.jaev.ValidatorResultCode.ADDRESS_UNKNOWN;
import static net.nicl.jaev.ValidatorResultCode.ADDRESS_VALID;
import static net.nicl.jaev.Validity.*;
import static net.nicl.jaev.integration.Initialiser.defaultFromAddress;
import static net.nicl.jaev.smtp.Reply.Code.*;
import static net.nicl.jaev.smtp.SmtpResultCode.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

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
