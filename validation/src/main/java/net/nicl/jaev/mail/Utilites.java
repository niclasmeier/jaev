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

package net.nicl.jaev.mail;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.nicl.jaev.mail.MailResultCode.ILLEGAL_DOMAIN_IP_FORMAT;
import static java.nio.charset.CodingErrorAction.REPORT;

/**
 * This helper class provides some shared functions for e-mail validation
 * 
 * @author Niclas Meier
 */
class Utilites {

	private static final CharsetEncoder ASCII_ENCODER = initEncoder();

	private static CharsetEncoder initEncoder() {
		CharsetEncoder asciiEncoder = Charset.forName("ASCII").newEncoder();

		asciiEncoder.onMalformedInput(REPORT);
		asciiEncoder.onUnmappableCharacter(REPORT);

		return asciiEncoder;
	}

	private static final Pattern IPV4_PATTERN = Pattern
			.compile("^\\[(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\]$");

	public static boolean isAscii(String string) {
		return ASCII_ENCODER.canEncode(string);
	}

	/**
	 * Trims a list of values
	 * 
	 * @param values
	 *            The values
	 * @return A list of trimmed values
	 */
	public static List<String> trim(List<String> values) {
		if (values == null) {
			return Collections.emptyList();
		}

		for (int i = 0; i < values.size(); ++i) {
			values.set(i, trim(values.get(i)));

		}
		return values;
	}

	/**
	 * Trims a value to null
	 * 
	 * @param value
	 *            The string value to trim
	 * @return The trimmed string. Returns <code>null</code> if the string is empty.
	 */
	public static String trim(String value) {
		if (value == null) {
			return null;
		}
		else {
			String trimmed = value.trim();
			if (trimmed.isEmpty()) {
				return null;
			}
			else {
				return trimmed;
			}
		}
	}

	public static void checkIpDomain(String domainIp) throws MailParseException {
		Matcher matcher = IPV4_PATTERN.matcher(domainIp);

		if (matcher.matches()) {

			for (int i = 1; i <= 4; ++i) {
				String segment = matcher.group(i);

				try {
					int value = Integer.parseInt(segment);

					if (value > 255) {
						throw new MailParseException(ILLEGAL_DOMAIN_IP_FORMAT, domainIp);
					}
				}
				catch (Exception e) {
					throw new MailParseException(ILLEGAL_DOMAIN_IP_FORMAT, domainIp);
				}
			}
		}
		else {
			throw new MailParseException(ILLEGAL_DOMAIN_IP_FORMAT, domainIp);
		}

	}

	private Utilites() {
	}

}
