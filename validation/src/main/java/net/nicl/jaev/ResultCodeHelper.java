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

import java.text.MessageFormat;
import java.util.*;

/**
 * This helper class provides facilities for result code management
 * 
 * @author Niclas Meier
 */
public final class ResultCodeHelper {

	/** A sorted collection of all result codes */
	private static final SortedSet<ResultCode> ALL = new java.util.TreeSet<ResultCode>(new Comparator<ResultCode>() {

		public int compare(ResultCode a, ResultCode b) {
			if (a.equals(b)) {
				return 0;
			}
			int diff = a.getCategory().getCode() - b.getCategory().getCode();
			if (diff == 0) {
				diff = a.getCode() - b.getCode();
				// an additional assertion, if the category and the code
				// are equal we have a problem.
				if (diff == 0) {
					throw new AssertionError("Duplicate Code detected. The codes '" + a + "' and '" + b
							+ "' are not equal and have the same code in the same category (" + a.getCategory() + ").");
				}
			}

			return diff > 0 ? 1 : -1;
		}
	});

	/**
	 * Returns a list of all known result codes
	 * 
	 * @return All registered result codes
	 */
	public static Collection<ResultCode> allResultCodes() {
		return Collections.unmodifiableSet(ALL);
	}

	/**
	 * Formats the result code as seven digit string string. Pretty trivial
	 * thing the first three digits are the category code the last four are the
	 * code code
	 * 
	 * @param resultCode
	 *            The result code to get the identifier from
	 * @return A seven digit identifier string
	 */
	public static String getIdentifier(ResultCode resultCode) {
		return String.format("%01d%02d", resultCode.getCategory().getCode(), resultCode.getCode());
	}

	/**
	 * Creates a message for a result code. If no message can be created the
	 * method will return a default text (<code>?RESULT_CODE?</code>).
	 * 
	 * @param resultCode
	 *            The result code, to create the message for
	 * @param objects
	 *            Context objects used for message creation
	 * @return The message
	 */
	public static String createMessage(ResultCode resultCode, Object... objects) {

		ResourceBundle resourceBundle = ResourceBundle.getBundle(resultCode.getClass().getName());

		try {
			MessageFormat format = new MessageFormat(resourceBundle.getString(resultCode.name()));

			return format.format(objects);
		}
		catch (MissingResourceException mre) {
			return "?" + resultCode + "?";
		}
	}

	/**
	 * Registers a new result code
	 * 
	 * @param resultCode
	 *            The result code to register
	 */
	public static void register(ResultCode resultCode) {
		ALL.add(resultCode);
	}

	/**
	 * Method to format a result code into a string
	 * 
	 * @param resultCode
	 *            The result code to format
	 * @return A string formatted result code
	 */
	public static String toString(ResultCode resultCode) {
		StringBuilder builder = new StringBuilder();
		builder.append(resultCode.name());
		builder.append(" (");
		builder.append(getIdentifier(resultCode));
		builder.append(")");
		return builder.toString();
	}

	/**
	 * Private default constructor to prevent instantiation
	 */
	private ResultCodeHelper() {

	}
}
