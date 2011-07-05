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

import java.util.Arrays;

import static net.nicl.jaev.Check.notNull;
import static net.nicl.jaev.ValidatorResultCode.ADDRESS_VALID;
import static net.nicl.jaev.Validity.INVALID;
import static net.nicl.jaev.Validity.SYNTAX;
import static net.nicl.jaev.mail.MailResultCode.ILLEGAL_EMAIL_FORMAT;

/**
 * This class is a superclass for all results which may be used in the
 * myTalkline applications. It provides facilities for a result code to indicate
 * the success of an operation and a message to provided a detailed description
 * to the caller.
 */
public final class Result {

	/** Result code to provide further informations */
	private final ResultCode code;

	/** The objects associated to this result */
	private final Object[] objects;

	/** The validity */
	private final Validity validity;

	/** The mail address */
	private final MailAddress mailAddress;

	/** Message to provide further informations */
	private transient String message;

	/**
	 * Creates a new successful result with custom ResultCode and a custom
	 * message
	 * 
	 * @param resultCode
	 *            The code
	 * @param validity
	 *            The validity
	 * @param objects
	 *            Objects related to the result
	 * @return A successful result
	 */
	public static Result create(ResultCode resultCode, Validity validity, MailAddress mailAddress, Object... objects) {
		return new Result(resultCode, validity, mailAddress, objects);
	}

	/**
	 * Creates a new successful result with custom ResultCode. The message will
	 * be copied from the result code.
	 * 
	 * @param resultCode
	 *            The code
	 * @param validity
	 *            The validity
	 * @return A result
	 */
	public static Result create(ResultCode resultCode, Validity validity, MailAddress mailAddress) {
		return new Result(resultCode, validity, mailAddress, (Object[]) null);
	}

	/**
	 * Private constructor
	 * 
	 * @param resultCode
	 *            The result code
	 * @param validity
	 *            The validity
	 * @param objects
	 *            Objects related to the result
	 */
	private Result(ResultCode resultCode, Validity validity, MailAddress mailAddress, Object... objects) {
		this.code = notNull(resultCode, "resultCode");
		this.validity = notNull(validity, "validity");
		this.mailAddress = mailAddress;
		this.objects = objects;
	}

	/**
	 * Returns a descriptive message for this result
	 * 
	 * @return The message
	 */
	public String getMessage() {
		if (this.message == null) {
			this.message = ResultCodeHelper.createMessage(this.code, this.objects);
		}

		return this.message;
	}

	/**
	 * The result code
	 * 
	 * @return The result code
	 */
	public ResultCode getResultCode() {
		return this.code;
	}

	/**
	 * The objects associated to this result
	 * 
	 * @return The associated objects
	 */
	public Object[] getObjects() {
		return Arrays.copyOf(this.objects, this.objects.length);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		builder.append(this.code);
		if (this.message != null && !this.message.isEmpty()) {
			builder.append(": ").append(this.message);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the validity of the result
	 * 
	 * @return The validity
	 */
	public Validity getValidity() {
		return this.validity;
	}

	/**
	 * Returns the associated mail address.
	 * 
	 * @return The mail address.
	 */
	public MailAddress getMailAddress() {
		return this.mailAddress;
	}

	/**
	 * Checks if a validity is implied by this result
	 * 
	 * @param validity
	 *            The target validity
	 * @return <code>true</code> if the target validity is implied.
	 */
	public boolean implies(Validity validity) {
		return this.validity.implies(validity);
	}

	/**
	 * This method could be used to transform a result with a technical result
	 * code to a new result with a logical result code.
	 * 
	 * @return A new result with a logical result code or the original result if
	 *         the result code is already logical
	 */
	public Result toLogical() {
		return toLogical(this);
	}

	/**
	 * This method could be used to transform a result with a technical result
	 * code to a new result with a logical result code.
	 * 
	 * @param result
	 *            The result to transform
	 * @return A new result with a logical result code or the original result if
	 *         the result code is already logical
	 */
	public static Result toLogical(Result result) {
		ResultCode resultCode = notNull(result, "result").getResultCode();

		switch (resultCode.getType()) {
		case TECHNICAL:
			Validity lesserValidity = result.getValidity().getLesser();

			if (lesserValidity.implies(SYNTAX)) {
				return create(ADDRESS_VALID, lesserValidity, result.getMailAddress());
			}
			else {
				return create(ILLEGAL_EMAIL_FORMAT, INVALID, result.getMailAddress());
			}
		case LOGICAL:
			return result;
		default:
			throw new AssertionError("Unknown result code type '" + resultCode.getType() + "'.");
		}
	}

}
