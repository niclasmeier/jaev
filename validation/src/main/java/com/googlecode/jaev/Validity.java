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

import static com.googlecode.jaev.Check.notNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * The <code>Validity</code> class defines the validity level of the result. The
 * structure leans onto an <code>Enum</code> class.
 * 
 * @author Niclas Meier
 */
public class Validity implements Comparable<Validity> {

	/** The mail address is completely invalid */
	public static final Validity INVALID = new Validity("INVALID", null, 0);

	/** The mail address has at least syntactic validity */
	public static final Validity SYNTAX = new Validity("SYNTAX", INVALID, 1);

	/** The domain of the mail address could be confirmed */
	public static final Validity DOMAIN = new Validity("DOMAIN", SYNTAX, 2, SYNTAX);

	/** The account exists on the mail server but seems to be inaccessible */
	public static final Validity ACCOUNT = new Validity("ACCOUNT", DOMAIN, 3, SYNTAX, DOMAIN);

	/**
	 * The highest level of validity: The mail address is semantically correct,
	 * the domain exists and the account exists and accepts e-mails
	 */
	public static final Validity ACCESSIBLE = new Validity("ACCESSIBLE", ACCOUNT, 4, SYNTAX, DOMAIN, ACCOUNT);

	/** Ordinal */
	private final int ordinal;

	private final Validity lesser;

	/** Set of validities that this validity implies */
	private final Set<Validity> implied;

	/** Symbolic name of the validity */
	private final String name;

	/**
	 * Defaultconstructor
	 * 
	 * @param lesser
	 *            The lesser validity
	 * @param ordinal
	 *            The ordinal
	 * @param name
	 *            The name
	 * @param implied
	 *            Set of implied validities
	 */
	private Validity(String name, Validity lesser, int ordinal, Validity... implied) {
		this.ordinal = ordinal;
		this.name = name;
		this.lesser = lesser == null ? this : lesser;
		if (implied == null) {
			this.implied = Collections.emptySet();
		}
		else {
			this.implied = new java.util.TreeSet<Validity>(Arrays.asList(implied));
		}
	}

	public String name() {
		return this.name;
	}

	@Override
	public int compareTo(Validity o) {
		int diff = this.ordinal - o.ordinal;
		if (diff == 0 && this != o) {
			throw new AssertionError("The two values '" + this.name + "' and '" + o.name
					+ "' have the same ordinal but are not identical.");
		}
		else {
			return diff == 0 ? 0 : diff > 0 ? 1 : -1;
		}
	}

	public Set<Validity> getImplied() {
		return this.implied;
	}

	public boolean implies(Validity validity) {
		return this == notNull(validity, "validitiy") || getImplied().contains(validity);
	}

	@Override
	public boolean equals(Object obj) {
		try {
			return obj != null && (obj == this || ((Validity) obj).ordinal == this.ordinal);
		}
		catch (ClassCastException cce) {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 11 + (79 * this.ordinal);
	}

	@Override
	public String toString() {
		return name();
	}

	public Validity getLesser() {
		return this.lesser;
	}

}
