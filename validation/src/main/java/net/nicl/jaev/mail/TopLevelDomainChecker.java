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

import static com.googlecode.jaev.Utilities.isNotEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The top level domain checker manages a list of top level domains and provides
 * method to check the validiy of the toplevel domain.
 * 
 * @author Niclas Meier
 */
public abstract class TopLevelDomainChecker {

	/** The logger */
	private static final Logger LOG = LoggerFactory.getLogger(TopLevelDomainChecker.class);

	private Set<String> toplevelDomains = Collections.emptySet();

	public abstract List<String> loadDomains();

	public TopLevelDomainChecker() {
	}

	/**
	 * Checks if the specified top level domain is valid.
	 * 
	 * @param domain
	 *            The domain to check
	 * @return <code>true</code> if the specified domain is in the list of the
	 *         known top level domain
	 */
	public boolean isValidTopLevelDomain(String domain) {
		return isNotEmpty(domain) && this.toplevelDomains.contains(domain.toLowerCase());
	}

	/**
	 * Initialisation method to load the list of domains
	 * 
	 * @return <code>true</code> if a new list is loaded.
	 */
	protected boolean initialise() {
		List<String> list = loadDomains();

		if (list == null || list.isEmpty()) {
			return false;
		}

		Set<String> domains = new java.util.HashSet<String>();

		for (String domain : list) {
			if (domain != null && !domain.isEmpty()) {
				domains.add(domain.trim().toLowerCase());
			}
		}
		this.toplevelDomains = domains;

		if (LOG.isDebugEnabled()) {
			LOG.debug("Loaded " + this.toplevelDomains.size() + " top level domains");
		}

		return true;
	}

}
