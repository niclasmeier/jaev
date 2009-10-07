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

package com.googlecode.jaev.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.googlecode.jaev.MailAddress;

public abstract class AbstractMailAddress implements MailAddress {

	private final String localPart;

	private final String original;

	private final DomainFormat domainFormat;

	protected AbstractMailAddress(String localPart, DomainFormat domainFormat, String original) {
		this.localPart = localPart;
		this.domainFormat = domainFormat;
		this.original = original;
	}

	@Override
	public String getLocalPart() {
		return this.localPart;
	}

	@Override
	public DomainFormat getDomainFormat() {
		return this.domainFormat;
	}

	@Override
	public String toString() {
		return this.original;
	}

	@Override
	public boolean equals(Object obj) {
		try {
			MailAddress other = (MailAddress) obj;
			return this.localPart.equals(other.getLocalPart()) && getDomain().equals(other.getDomain());
		}
		catch (ClassCastException cce) {
			return false;
		}
	}

	protected String getAddressSpecification() {
		return this.localPart + "@" + getDomain();
	}

	@Override
	public int hashCode() {
		return 7 + 3 * getDomain().hashCode() + 11 * this.localPart.hashCode();
	}

	@Override
	public InternetAddress getInternetAddress() throws AddressException, UnsupportedEncodingException {
		return new InternetAddress(getAddressSpecification());
	}

}
