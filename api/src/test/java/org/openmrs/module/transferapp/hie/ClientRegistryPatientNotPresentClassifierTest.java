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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientRegistryPatientNotPresentClassifierTest {

	@Test
	public void detectsSampleOperationOutcome() {
		String body = "{\"resourceType\":\"OperationOutcome\",\"issue\":[{\"severity\":\"error\","
				+ "\"code\":\"not-found\",\"details\":{\"text\":\"Patient is not present in the CR.\"},"
				+ "\"diagnostics\":\"Patient is not present in the CR.\"}]}";
		assertTrue(ClientRegistryPatientNotPresentClassifier.isPatientNotPresentInCr(body));
	}

	@Test
	public void ignoresUnrelatedValidationErrors() {
		assertFalse(ClientRegistryPatientNotPresentClassifier.isPatientNotPresentInCr(
				"ERROR 422: Validation failed — missing diagnosis"));
		assertFalse(ClientRegistryPatientNotPresentClassifier.isPatientNotPresentInCr(null));
		assertFalse(ClientRegistryPatientNotPresentClassifier.isPatientNotPresentInCr(""));
	}
}
