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

import com.googlecode.jaev.ResultCode;
import com.googlecode.jaev.ResultCodeHelper;

/**
 * This exception indicates problems creating a syntactically correct <code>MailAddress</code> instance. It contains a
 * further informations to
 * create a proper result.
 * 
 * @author Niclas Meier
 */
public class MailParseException extends Exception {

	private static final long serialVersionUID = -1913841180048563826L;

	private final ResultCode resultCode;

	private final Object[] items;

	public MailParseException(ResultCode resultCode, Object... items) {
		super(ResultCodeHelper.createMessage(resultCode, items));
		this.resultCode = resultCode;
		this.items = items;
	}

	public ResultCode getResultCode() {
		return this.resultCode;
	}

	public Object[] getItems() {
		return this.items;
	}

}
