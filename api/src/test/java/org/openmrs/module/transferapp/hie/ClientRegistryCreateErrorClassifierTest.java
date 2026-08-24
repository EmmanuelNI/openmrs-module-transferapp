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

public class ClientRegistryCreateErrorClassifierTest {

	@Test
	public void detectsDuplicateUpid() {
		assertTrue(ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(409,
				"ERROR 409: Error, a patient with this UPID already exists in CR!"));
		assertTrue(ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(
				"{\"issue\":[{\"code\":\"duplicate\"}]}"));
	}

	@Test
	public void ignoresOtherErrors() {
		assertFalse(ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(422, "Validation failed"));
		assertFalse(ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(null));
	}
}
