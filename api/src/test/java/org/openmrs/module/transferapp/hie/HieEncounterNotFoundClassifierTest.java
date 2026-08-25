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

public class HieEncounterNotFoundClassifierTest {

	@Test
	public void detectsRhie500OperationOutcome() {
		String body = "{\"resourceType\":\"OperationOutcome\",\"text\":{\"status\":\"generated\","
				+ "\"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">ERROR 500: Encounter "
				+ "Encounter/cae9f9af-b23d-4b0c-813d-16431a31a526 not found! (traceId: 744c3b68)</div>\"},"
				+ "\"issue\":[{\"severity\":\"fatal\",\"details\":{\"text\":"
				+ "\"Encounter Encounter/cae9f9af-b23d-4b0c-813d-16431a31a526 not found!\"}}]}";
		assertTrue(HieEncounterNotFoundClassifier.isEncounterNotFound(
				"HIE request failed (500) GET https://hie.example/shr/Encounter/cae9f9af: " + body));
	}

	@Test
	public void detectsHttp404() {
		assertTrue(HieEncounterNotFoundClassifier.isEncounterNotFound(
				"HIE request failed (404) GET https://hie.example/shr/Encounter/abc: not found"));
	}

	@Test
	public void ignoresUnrelatedErrors() {
		assertFalse(HieEncounterNotFoundClassifier.isEncounterNotFound(
				"HIE request failed (500) POST /shr/Encounter/transfer: Validation failed"));
		assertFalse(HieEncounterNotFoundClassifier.isEncounterNotFound(null));
		assertFalse(HieEncounterNotFoundClassifier.isEncounterNotFound(""));
	}
}
