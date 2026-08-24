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
package org.openmrs.module.transferapp.sms;

/**
 * Outcome of the patient SMS sent after HIE accepts the transfer.
 */
public enum PatientSmsNotificationStatus {
	SENT("Sent"),
	FAILED("Failed"),
	SKIPPED("Not sent");

	private final String label;

	PatientSmsNotificationStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
