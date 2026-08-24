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
package org.openmrs.module.transferapp.model;

/**
 * Patient outcome on receiving-facility referral feedback (same codes as eTransfer).
 */
public enum ReferralFeedbackOutcome {

	STABILIZED_CURED("Stabilized/Cured"),
	DIED("Died"),
	ESCAPED("Escaped"),
	TO_BE_FOLLOWED_UP("To be followed up"),
	REFERRED_TO_HIGH_LEVEL("Referred to high level");

	private final String label;

	ReferralFeedbackOutcome(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static ReferralFeedbackOutcome fromStoredValue(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() == 0) {
			return null;
		}
		for (ReferralFeedbackOutcome outcome : values()) {
			if (outcome.name().equalsIgnoreCase(trimmed) || outcome.label.equalsIgnoreCase(trimmed)) {
				return outcome;
			}
		}
		return null;
	}
}
