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

package com.googlecode.jaev;

/**
 * <p>
 * This interface provides all methods which has to be implemented by a result code. The implementing classes are
 * normally enumerations, so code inheritance is not possible.
 * </p>
 * <p>
 * <i>Note:</i>Shared code should be implemented in <code>ResultCodeHelper</code>.
 * </p>
 * 
 * @author Niclas Meier
 */
public interface ResultCode {

	/**
	 * The categories of result codes
	 */
	public enum Category {
		/**
		 * General result codes - should be used only by very general and not
		 * use-case specific results
		 */
		VALIDATION(1),
		/** Category for parsing related results */
		MAIL(2),
		/** Category for DNS lookup related results */
		DNS(3),
		/** Category for SMTP negotiation results */
		SMTP(4);

		/** Category code */
		private int code = -1;

		/**
		 * Private contructor
		 * 
		 * @param code
		 *            The category code
		 */
		Category(int code) {
			this.code = code;
		}

		/**
		 * Returns the category code
		 * 
		 * @return The category code
		 */
		public int getCode() {
			return code;
		}
	}

	/**
	 * The type of the Result code;
	 */
	public enum Type {
		LOGICAL, TECHNICAL
	}

	/**
	 * <p>
	 * Returns the result code.
	 * </p>
	 * <p>
	 * <b>Note:</b> The code has to be unique in the result code category and the ordinal of the enumeration should
	 * <i>not</i> be used. When somebody shuffles the enumeration the ordinal of a result code will change!
	 * </p>
	 * 
	 * @return The code
	 */
	public int getCode();

	/**
	 * Returns the category of a result code
	 * 
	 * @return The category of a result code
	 */
	public Category getCategory();

	/**
	 * Returns the identifier of the result code. It will be three digits long
	 * (one digits of the category and two of the code)
	 * 
	 * @return An identifier
	 */
	public String getIdentifier();

	/**
	 * The name (enumeration) of the result code
	 * 
	 * @return The name
	 */
	public String name();

	/**
	 * The type of the result code
	 * 
	 * @return The type
	 */
	public Type getType();
}