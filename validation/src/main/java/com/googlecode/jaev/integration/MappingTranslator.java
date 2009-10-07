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

package com.googlecode.jaev.integration;

import static com.googlecode.jaev.Check.notNull;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jaev.Result;
import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.ResultCodeHelper;

/**
 * This result translator variant uses a map to assign the code of a result to a
 * string for result translation.
 * 
 * @author Niclas Meier
 */
public abstract class MappingTranslator implements ResultTranslator {

	/**
	 * The logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MappingTranslator.class);

	/** The translation tabel */
	private final Map<ResultCode, String> translationTable;

	/**
	 * Default constructor
	 */
	protected MappingTranslator() {
		this.translationTable = initTranslationTable(new java.util.HashMap<ResultCode, String>());
		checkCompleteness();
	}

	@Override
	public String translate(Result result) {
		String code = this.translationTable.get(notNull(result, "result").getResultCode());
		if (code == null || code.isEmpty()) {
			ResultCode resultCode = result.getResultCode();
			throw new IllegalArgumentException("No code found for '" + resultCode.getClass().getSimpleName() + "."
					+ resultCode.name() + "'");
		}

		return code;
	}

	/**
	 * This method checks if every known <code>ResultCode</code> instance is
	 * assigned to a string.
	 */
	protected void checkCompleteness() {

		Set<ResultCode> codes = new java.util.HashSet<ResultCode>();
		codes.addAll(ResultCodeHelper.allResultCodes());
		codes.removeAll(this.translationTable.keySet());

		if (!codes.isEmpty()) {
			for (ResultCode code : codes) {
				LOG.warn("Missing code " + code.getClass().getSimpleName() + "." + code.name()
						+ " in translation table.");
			}

			throw new AssertionError("Found " + codes.size() + " codes, which are note mappend in translation table.");
		}
	}

	/**
	 * Abstract initialising method for the translation table
	 * 
	 * @param translationTable
	 *            The translation table to initialise
	 * @return The initialised translation table.
	 */
	protected abstract Map<ResultCode, String> initTranslationTable(Map<ResultCode, String> translationTable);
}
