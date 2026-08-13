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
package org.openmrs.module.transferapp.api;

import org.openmrs.Patient;

public class HiePatientRegistrationResult {

	private final Patient patient;

	private final boolean created;

	public HiePatientRegistrationResult(Patient patient, boolean created) {
		this.patient = patient;
		this.created = created;
	}

	public Patient getPatient() {
		return patient;
	}

	public boolean isCreated() {
		return created;
	}
}
