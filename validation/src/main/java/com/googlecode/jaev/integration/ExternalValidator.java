package com.googlecode.jaev.integration;

import com.googlecode.jaev.MailAddress;

public interface ExternalValidator {

	/**
	 * Checks if the specified e-mail address is valid
	 * 
	 * @param mailAddress
	 *            The e-mail address to check
	 * @return The validation result
	 */
	ExternalResult validate(String mailAddress);

	/**
	 * Checks if the specified e-mail address is valid
	 * 
	 * @param mailAddress
	 *            The e-mail address to check
	 * @return The validation result
	 */
	ExternalResult validate(MailAddress mailAddress);
}
