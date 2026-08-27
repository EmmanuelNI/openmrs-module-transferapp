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
 * Detects HIE responses that mean a transfer Encounter is not stored in SHR yet.
 * RHIE sometimes returns HTTP 500 OperationOutcome instead of 404 for missing encounters.
 */
public final class HieEncounterNotFoundClassifier {

	private HieEncounterNotFoundClassifier() {
	}

	public static boolean isEncounterNotFound(String messageOrBody) {
		if (messageOrBody == null || messageOrBody.trim().isEmpty()) {
			return false;
		}
		String normalized = messageOrBody.toLowerCase(Locale.ENGLISH);

		if (normalized.contains("(404)") || normalized.contains(" 404 ") || normalized.contains("\"http-status\",\"valueString\":\"404\"")) {
			return true;
		}

		boolean mentionsEncounter = normalized.contains("encounter");
		boolean notFound = normalized.contains("not found")
				|| normalized.contains("does not exist")
				|| normalized.contains("could not be found")
				|| normalized.contains("no resource found");
		if (mentionsEncounter && notFound) {
			return true;
		}

		// OperationOutcome wording without explicit "Encounter" token still in path form
		return normalized.contains("encounter/") && notFound;
	}
}
