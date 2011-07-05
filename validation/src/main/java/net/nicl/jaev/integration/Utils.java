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
package net.nicl.jaev.integration;

import net.nicl.jaev.MailAddress;
import net.nicl.jaev.Result;
import net.nicl.jaev.ResultCode;

import java.net.InetAddress;
import java.util.Date;

public final class Utils {

	public static ExternalResult external(Result result) {
		ExternalResult primitiveResult = new ExternalResult();

		primitiveResult.setCode(primitive(result.getResultCode()));
		primitiveResult.setValidity(result.getValidity().name());
		primitiveResult.setObjects(trivializeObjects(result.getObjects()));

		return primitiveResult;
	}

	public static ExternalResultCode primitive(ResultCode code) {
		ExternalResultCode externalCode = new ExternalResultCode();

		externalCode.setCategory(code.getCategory().name());
		externalCode.setType(code.getType().name());
		externalCode.setIdentifier(code.getIdentifier());
		externalCode.setName(code.name());

		return externalCode;
	}

	public static Result trivialize(Result sourceResult) {
		Object[] sourceObjects = sourceResult.getObjects();
		Object[] trivializedObjects = trivializeObjects(sourceObjects);

		return Result.create(sourceResult.getResultCode(), sourceResult.getValidity(), sourceResult.getMailAddress(),
				trivializedObjects);

	}

	private static Object[] trivializeObjects(Object[] sourceObjects) {
		if (sourceObjects == null || sourceObjects.length == 0) {
			return null;
		}

		Object[] trivializedObjects = new Object[sourceObjects.length];
		for (int i = 0; i < sourceObjects.length; ++i) {
			trivializedObjects[i] = trivializeObject(sourceObjects[i]);
		}

		return trivializedObjects;
	}

	private static Object trivializeObject(Object object) {
		if (object == null) {
			return null;
		}
		else if (object instanceof Date) {
			return object;
		}
		else if (object instanceof InetAddress) {
			return ((InetAddress) object).getCanonicalHostName();

		}
		else if (object instanceof MailAddress) {
			return object.toString();
		}
		else {
			return String.valueOf(object);
		}
	}

	private Utils() {
	}

}
