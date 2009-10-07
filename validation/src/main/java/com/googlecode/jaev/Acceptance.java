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

import static com.googlecode.jaev.ResultCode.Type.TECHNICAL;
import static com.googlecode.jaev.ValidatorResultCode.ADDRESS_VALID;

/**
 * The check strategy is used to check if a validation result meets the
 * requirements to declare an e-mail address valid.
 * 
 * @author Niclas Meier
 */
public interface Acceptance {

	/**
	 * The <code>SIMPLE</code> acceptance strategy checks the result code of the
	 * specified result. If the code is <code>ADDRESS_VALID</code> the
	 * requirements are met. If the result code is a technical one, the result
	 * is also accepted.
	 */
	public static final Acceptance SIMPLE = new Acceptance() {

		@Override
		public boolean accept(Result result) {
			return result != null
					&& (TECHNICAL.equals(result.getResultCode().getType()) || ADDRESS_VALID.equals(result
							.getResultCode()));
		}

		@Override
		public Validity minimumValidity() {
			return Validity.SYNTAX;
		}

	};

	/**
	 * The <code>ACCOUNT</code> acceptance strategy checks the validity of the
	 * result. If <code>ACCOUNT</code> validity is reached the requirements are
	 * met. If the result code is a technical one, the result is also accepted.
	 */
	public static final Acceptance ACCOUNT = new Acceptance() {

		@Override
		public boolean accept(Result result) {
			return result != null
					&& (result.getValidity().implies(Validity.ACCOUNT) || TECHNICAL.equals(result.getResultCode()
							.getType()));

		}

		@Override
		public Validity minimumValidity() {
			return Validity.ACCOUNT;
		}

	};

	/**
	 * The <code>STRICT<code> strategy checks the validity of the result. If <code>ACCESSIBLE</code> validity is reached
	 * and the the code is <code>ADDRESS_VALID</code> the
	 * requirements are met.If the result code is a technical one, the result is
	 * also accepted.
	 */
	public static final Acceptance STRICT = new Acceptance() {

		@Override
		public boolean accept(Result result) {
			return result != null
					&& (result.getValidity().implies(Validity.ACCESSIBLE)
							&& ADDRESS_VALID.equals(result.getResultCode()) || TECHNICAL.equals(result.getResultCode()
							.getType()));

		}

		@Override
		public Validity minimumValidity() {
			return Validity.ACCESSIBLE;
		}

	};

	/**
	 * @param result
	 *            The validation result related to the e-mail address
	 * @return <code>true</code> if a mail address is valid within the specified
	 *         means.
	 */
	public boolean accept(Result result);

	/**
	 * Defines the minimum validity enforced by the strategy.
	 * 
	 * @return The minimal <code>Validity</code>.
	 */
	public Validity minimumValidity();
}
