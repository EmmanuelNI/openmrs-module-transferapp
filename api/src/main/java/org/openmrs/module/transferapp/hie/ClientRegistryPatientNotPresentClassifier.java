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
 * Detects HIE SHR rejections that mean the patient must be created in Client Registry first.
 */
public final class ClientRegistryPatientNotPresentClassifier {

	private ClientRegistryPatientNotPresentClassifier() {
	}

	public static boolean isPatientNotPresentInCr(String messageOrBody) {
		if (messageOrBody == null || messageOrBody.trim().isEmpty()) {
			return false;
		}
		String normalized = messageOrBody.toLowerCase(Locale.ENGLISH);
		if (normalized.contains("patient is not present in the cr")
				|| normalized.contains("patient not present in the cr")
				|| normalized.contains("not present in the cr")
				|| normalized.contains("not found in the cr")
				|| normalized.contains("patient is not present in client registry")
				|| normalized.contains("patient not found in client registry")
				|| normalized.contains("patient not found in the client registry")) {
			return true;
		}
		// OperationOutcome: not-found + CR wording
		boolean notFound = normalized.contains("\"code\":\"not-found\"")
				|| normalized.contains("\"code\": \"not-found\"")
				|| normalized.contains("not-found");
		boolean mentionsCr = normalized.contains(" cr")
				|| normalized.contains("client registry")
				|| normalized.contains("clientregistry");
		return notFound && mentionsCr && normalized.contains("patient");
	}
}
