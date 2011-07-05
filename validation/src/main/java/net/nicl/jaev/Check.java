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

/**
 * The check class provides some static method to improve the quality of the
 * code.
 * 
 * @author Niclas Meier
 */
public final class Check {

	/**
	 * Checks that a method parameter value is not null, and returns it. This
	 * method is used in situations where some of the parameters to a method are
	 * allowed to be null and other's aren't.
	 * 
	 * @param <T>
	 *            the value type
	 * @param value
	 *            the value (which is checked to ensure non-nullness)
	 * @param parameterName
	 *            the name of the parameter, used for exception
	 *            messages
	 * @return the value
	 * @throws IllegalArgumentException
	 *             if the value is null
	 */
	public static <T> T notNull(T value, String parameterName) {
		if (value == null) {
			throw new IllegalArgumentException("The parameter '" + parameterName + "' was null.");
		}

		return value;
	}

	/**
	 * Checks that a parameter value is not null and not empty.
	 * 
	 * @param value
	 *            value to check (which is returned)
	 * @param parameterName
	 *            the name of the parameter, used for exception
	 *            messages
	 * @return the value if non-blank
	 * @throws IllegalArgumentException
	 *             if the value is null or empty
	 */
	public static String notBlank(String value, String parameterName) {
		if (value != null) {
			String trimmedValue = value.trim();

			if (!trimmedValue.isEmpty()) {
				return value;
			}
		}

		throw new IllegalArgumentException("The parameter '" + parameterName + "' was blank.");
	}

	/**
	 * Checks that the provided value is not null, and may be cast to the
	 * desired type.
	 * 
	 * @param <T>
	 *            Casted instance
	 * @param parameterValue
	 *            Object to cast
	 * @param type
	 *            Type to cast to
	 * @param parameterName
	 *            The paramter name of the parameter to cast
	 * @return the casted value
	 * @throws IllegalArgumentException
	 *             if the value is null, or is not
	 *             assignable to the indicated type
	 */
	public static <T> T cast(Object parameterValue, Class<T> type, String parameterName) {
		notNull(parameterValue, parameterName);

		if (!type.isInstance(parameterValue)) {
			throw new IllegalArgumentException("The value '" + parameterValue + "' of the parameter '" + parameterName
					+ "' may not be casted into " + type.getName() + ".");
		}

		return type.cast(parameterValue);
	}

	/**
	 * Private default constructor to prevent instantiation
	 */
	private Check() {
	}
}