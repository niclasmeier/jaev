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

import static com.googlecode.jaev.Check.notBlank;
import static com.googlecode.jaev.Check.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The command class describes a command issued to a SMTP mail server. The <code>Command</code> class consists of the
 * infratructure to create the
 * command string to send to the mail server (using the <code>brief()</code> method. Furthermore the
 * <code>Command</code> class provides prototype
 * commands for all relevant SMTP commands.
 * 
 * @author Niclas Meier
 */
public abstract class Command {

	private static final Logger LOG = LoggerFactory.getLogger(Command.class);

	/**
	 * The HELO command prototype
	 */
	public static final Command HELO = new Command("HELO ") {

		@Override
		public Command particularise(String... strings) {

			return new ParticularCommand(this.verb, notBlank(strings[0], "senderDomain"));
		}

	};

	/**
	 * The MAIL (from) command prototype
	 */
	public static final Command MAIL = new Command("MAIL FROM:") {

		@Override
		public Command particularise(String... strings) {

			return new ParticularCommand(this.verb, "<" + notBlank(strings[0], "sender") + ">");
		}

	};

	/**
	 * The RCPT (recipient to) command prototype
	 */
	public static final Command RECIPIENT_TO = new Command("RCPT TO:") {

		@Override
		public Command particularise(String... strings) {

			return new ParticularCommand(this.verb, "<" + notBlank(strings[0], "recipient") + ">");
		}

	};

	/**
	 * The VRFY (verify) command prototype
	 */
	public static final Command VERIFY = new Command("VRFY: ") {

		@Override
		public Command particularise(String... strings) {

			return new ParticularCommand(this.verb, notBlank(strings[0], "mail"));
		}

	};

	/**
	 * The QUIT command prototype
	 */
	public static final Command QUIT = new ParticularCommand("QUIT", null);

	/**
	 * The verb to send to the SMTP MTA
	 */
	protected final String verb;

	/**
	 * Optional parameter to attach to the verb.
	 */
	protected String parameter = null;

	/**
	 * Default constructor for command
	 * 
	 * @param verb
	 *            The verb related to a particular command.
	 */
	protected Command(String verb) {
		this.verb = notNull(verb, "verb");
	}

	/**
	 * Abstract method to make a command particular.
	 * 
	 * @param strings
	 *            Optional parameter for this process.
	 * @return A particular command
	 */
	public abstract Command particularise(String... strings);

	/**
	 * Perform the command briefing. I.g. create a string to send to the MTA
	 * 
	 * @return A briefing for the command.
	 */
	public String brief() {
		throw new IllegalStateException("A not particularised command cannot " + "perform a briefing.");
	}

	/**
	 * An already particular command
	 */
	private static class ParticularCommand extends Command {

		private final String briefing;

		/**
		 * Default constructor
		 * 
		 * @param verb
		 *            The verb related to a particular command.
		 * @param parameter
		 *            The parameter for this command
		 */
		private ParticularCommand(String verb, String parameter) {
			super(verb);
			this.parameter = parameter;
			this.briefing = this.parameter == null ? this.verb : this.verb + this.parameter;
		}

		@Override
		public Command particularise(String... strings) {
			throw new UnsupportedOperationException("This command is already particular.");
		}

		@Override
		public String brief() {
			LOG.debug("Command: {}", this.briefing);

			return this.briefing;
		}

		@Override
		public String toString() {
			return "[" + getClass().getSimpleName() + ": " + brief() + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			try {
				ParticularCommand other = (ParticularCommand) obj;
				return this.briefing.equals(other.briefing);
			}
			catch (ClassCastException e) {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return this.briefing.hashCode();
		}

	}
}
