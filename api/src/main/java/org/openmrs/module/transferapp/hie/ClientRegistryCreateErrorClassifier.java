/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.transferapp.hie;

import java.util.Locale;

/**
 * Interprets Client Registry create-error payloads (often FHIR OperationOutcome on HTTP 409).
 */
public final class ClientRegistryCreateErrorClassifier {

	private ClientRegistryCreateErrorClassifier() {
	}

	public static boolean isPatientAlreadyExists(String messageOrBody) {
		return isPatientAlreadyExists(null, messageOrBody);
	}

	public static boolean isPatientAlreadyExists(Integer httpStatus, String messageOrBody) {
		if ((messageOrBody == null || messageOrBody.trim().isEmpty()) && httpStatus == null) {
			return false;
		}
		String normalized = messageOrBody == null ? "" : messageOrBody.toLowerCase(Locale.ENGLISH);

		if (normalized.contains("already exist")
				|| normalized.contains("already registered")
				|| normalized.contains("upid already exists")
				|| normalized.contains("duplicate patient")
				|| normalized.contains("patient exists")) {
			return true;
		}

		if (normalized.contains("\"code\":\"duplicate\"")
				|| normalized.contains("\"code\": \"duplicate\"")) {
			return true;
		}

		if (httpStatus != null && httpStatus.intValue() == 409) {
			return normalized.contains("patient")
					|| normalized.contains("upid")
					|| normalized.contains("operationoutcome")
					|| normalized.contains("duplicate")
					|| normalized.contains("409");
		}

		return normalized.contains("error 409") && normalized.contains("patient");
	}
}
