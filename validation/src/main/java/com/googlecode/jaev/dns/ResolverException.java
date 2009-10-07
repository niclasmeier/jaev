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

package com.googlecode.jaev.dns;

import com.googlecode.jaev.ResultCodeHelper;

/**
 * This exception provides additional information if a problem during the DNS
 * lookup occurs.
 * 
 * @author Niclas Meier
 */
public class ResolverException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6817369714034125679L;

	private final DnsResultCode resultCode;

	private final Object[] items;

	public ResolverException(DnsResultCode resultCode, Object... items) {
		super(ResultCodeHelper.createMessage(resultCode, items));
		this.resultCode = resultCode;
		this.items = items;
	}

	public ResolverException(DnsResultCode resultCode, Throwable cause, Object... items) {
		super(ResultCodeHelper.createMessage(resultCode, items), cause);
		this.resultCode = resultCode;
		this.items = items;
	}

	public DnsResultCode getResultCode() {
		return this.resultCode;
	}

	public Object[] getItems() {
		return this.items;
	}

}
