/*
 * Copyright 2009 - Niclas Meier
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.nicl.jaev.dns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.nicl.jaev.Check.notNull;
import static net.nicl.jaev.dns.DnsResultCode.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * <p>
 * The simple implementation of the DNS resolver uses the facilities of the SUN JRE JNDI implementation by using the
 * "com.sun.jndi.dns.DnsContextFactory".
 * </p>
 * <p>
 * <i>Note: </i> This class has not been tested with alternate JREs so far.
 * </p>
 * 
 * @author Niclas Meier
 */
public class SimpleResolver implements Resolver {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleResolver.class);

	/** The timeout in milliseconds */
	private final long timeout;

	/** The number of retries */
	private final int retries;

	/**
	 * Default constructor, creates a resolver with 3 retires and 2 second timeout.
	 */
	public SimpleResolver() {
		this(3, 2, SECONDS);
	}

	/**
	 * Argument constructor
	 * 
	 * @param retries Number of retries
	 * @param timeout The timeout in milliseconds
	 */
	public SimpleResolver(int retries, long timeout) {
		this(retries, timeout, MILLISECONDS);
	}

	/**
	 * Argument constructor
	 * 
	 * @param retries Number of retries
	 * @param timeout The timeout
	 * @param timeUnit The time unit (SECONDS, MILLISECONDS, etc.)
	 */
	public SimpleResolver(int retries, long timeout, TimeUnit timeUnit) {
		this.retries = retries;
		this.timeout = notNull(timeUnit, "timeUnit").toMillis(timeout);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initialized with timeout of " + timeout + " " + timeUnit + " with " + retries + " retries.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ResouceRecord> resolve(String domainName, ResouceRecord.Type recordType) throws ResolverException {
		DirContext directory = null;
		try {
			// create directory
			directory = createDirectory();

			// perform lookup
			Attributes attrs = directory.getAttributes(domainName, new String[] { recordType.name() });

			// iterate through result and convert into destination type
			NamingEnumeration<Attribute> enumeration = (NamingEnumeration<Attribute>) attrs.getAll();
			List<ResouceRecord> result = new java.util.ArrayList<ResouceRecord>(attrs.size());

			while (enumeration.hasMoreElements()) {
				Attribute attribute = enumeration.nextElement();
				for (int i = 0; i < attribute.size(); ++i) {

					String value = String.valueOf(attribute.get(i));
					int pos = value.indexOf(' ');
					if (pos > 0) {
						value = value.substring(pos).trim();
					}

					ResouceRecord record = new SimpleResouceRecord(recordType, value);
					result.add(record);

					LOG.trace("Retrieved record {} for domain '{}'.", record, domainName);
				}
			}

			return result;
		}
		catch (NameNotFoundException nnfe) {
			throw new ResolverException(DOMAIN_NAME_NOT_FOUND, nnfe, domainName, recordType);
		}
		catch (NamingException ne) {
			Throwable cause = ne.getCause();

			if (cause instanceof SocketTimeoutException) {
				throw new ResolverException(DNS_TIMEOUT, ne, domainName, recordType);
			}

			throw new ResolverException(GENERAL_DNS_ERROR, ne, domainName, recordType);
		}
		finally {
			try {
				if (directory != null) {
					// close the context to free the resources
					directory.close();
				}
			}
			catch (NamingException e) {
				// must be handled, very unlikely to occur
				LOG.debug("Unable to close directory.", e);
			}
		}
	}

	/**
	 * Creates a fresh directory context for DNS lookup
	 * 
	 * @return A newly created an initialized directory context.
	 * @throws NamingException If a problem occurs with context creation.
	 */
	private DirContext createDirectory() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
		env.put("com.sun.jndi.dns.recursion", "true");
		env.put(Context.AUTHORITATIVE, "false");

		env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(this.timeout / this.retries));
		env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(this.retries));

		return new InitialDirContext(env);

	}

	/**
	 * Private implementation of the <code>ResourceRecord</code> interface
	 */
	private class SimpleResouceRecord implements ResouceRecord {

		/**
		 * Serial version UID
		 */
		private static final long serialVersionUID = -3487364689602363210L;

		private final Type type;

		private final String value;

		SimpleResouceRecord(Type type, String value) {
			this.type = type;
			this.value = value;
		}

		public Type getType() {
			return this.type;
		}

		public String getValue() {
			return this.value;
		}

		@Override
		public String toString() {
			return this.type + ":" + this.value;
		}
	}

}
