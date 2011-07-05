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

import java.io.Serializable;

/**
 * The resource record class defines the data from a resource record used in the
 * DNS.
 * 
 * @author Niclas Meier
 */
public interface ResouceRecord extends Serializable {

	/**
	 * The type of the resource records. For a more complete list:
	 * http://en.wikipedia.org/wiki/List_of_DNS_record_types
	 */
	public enum Type {

		/** address record - http://tools.ietf.org/html/rfc1035 */
		A,
		/** IPv6 address record - http://tools.ietf.org/html/rfc3596 */
		AAAA,
		/** Certificate record - http://tools.ietf.org/html/rfc4398 */
		CERT,
		/** Canonical name record - http://tools.ietf.org/html/rfc1035 */
		CNAME,
		/** mail exchange record - http://tools.ietf.org/html/rfc1035 */
		MX,
		/** name server record - http://tools.ietf.org/html/rfc1035 */
		NS

	}

	/**
	 * The type of the resource record.
	 * 
	 * @return The type
	 */
	public Type getType();

	/**
	 * The value of the resource record
	 * 
	 * @return The value
	 */
	public String getValue();
}
