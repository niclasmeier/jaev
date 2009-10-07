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

import static com.googlecode.jaev.Check.notNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>UrlTopLevelDomainChecker</code> uses a <code>java.net.URL</code> to
 * load the domain list from.
 * 
 * @author Niclas Meier
 */
public class UrlTopLevelDomainChecker extends TopLevelDomainChecker {

	/** The logger */
	private static final Logger LOG = LoggerFactory.getLogger(UrlTopLevelDomainChecker.class);

	/** The source URL of the domain list */
	private final URL sourceUrl;

	/** last modified date of the domain list */
	private long lastModified = -1l;

	protected UrlTopLevelDomainChecker(URL souceUrl) {
		this.sourceUrl = notNull(souceUrl, "souceUrl");
		initialise();
	}

	@Override
	public List<String> loadDomains() {
		List<String> domains = new java.util.LinkedList<String>();
		BufferedReader reader = null;
		URLConnection urlConnection;
		try {
			urlConnection = this.sourceUrl.openConnection();

			long modificationDate = urlConnection.getLastModified();

			if (this.lastModified >= modificationDate) {
				return Collections.emptyList();
			}

			InputStream in = urlConnection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					domains.add(line.trim().toUpperCase());
				}
			}

			this.lastModified = urlConnection.getLastModified();
		}
		catch (IOException e) {
			LOG.error("Unable to read top level domain " + "definitions due to an IO error. ", e);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (Exception e) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("An error occured while closing the reader.", e);
				}

			}
		}

		return domains;
	}

}
