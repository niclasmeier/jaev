/*
 * Copyright 2009 - Niclas Meier
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.nicl.jaev.suggest;

import net.nicl.jaev.MailAddress;

import java.util.SortedSet;

/**
 * The <code>Suggestor</code> suggests e-mail addresses based on a template
 * 
 * @author Niclas Meier
 */
public interface Suggestor {

	/**
	 * Make a suggestion
	 * 
	 * @param mailAddress The template mail address
	 * @return A sorted set of mail addresses
	 */
	SortedSet<MailAddress> suggest(MailAddress mailAddress);
}
