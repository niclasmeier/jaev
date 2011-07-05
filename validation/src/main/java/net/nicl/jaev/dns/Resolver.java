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

package net.nicl.jaev.dns;

import java.util.List;

/**
 * The resolver fetches resource records from the domain name system (DNS)
 * specified in http://tools.ietf.org/html/rfc1034.
 * 
 * @author Niclas Meier
 */
public interface Resolver {

	/**
	 * Resolves the resource records of the specified type from the domain.
	 * 
	 * @param domainName
	 *            The domain to get the informations for
	 * @param recordType
	 *            The type of the resource records
	 * @return A list of resource records.
	 * @throws ResolverException
	 *             if an error (i.e. time out) occurs.
	 */
	public List<ResouceRecord> resolve(String domainName, ResouceRecord.Type recordType) throws ResolverException;

}
