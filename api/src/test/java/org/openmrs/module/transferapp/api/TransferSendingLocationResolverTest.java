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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openmrs.module.transferapp.api.impl.TransferRegistrationObsServiceImpl;

public class TransferSendingLocationResolverTest {

	@Test
	public void parseFacilityNamesSplitsCommaSeparatedAliases() {
		List<String> names = TransferSendingLocationResolver.parseFacilityNames(
				"Munini DH, Munini District Hospital,  Munini DH ");
		assertEquals(Arrays.asList("Munini DH", "Munini District Hospital"), names);
	}

	@Test
	public void parseFacilityNamesHandlesBlank() {
		assertTrue(TransferSendingLocationResolver.parseFacilityNames(null).isEmpty());
		assertTrue(TransferSendingLocationResolver.parseFacilityNames("  ").isEmpty());
		assertTrue(TransferSendingLocationResolver.parseFacilityNames(", ,").isEmpty());
	}

	@Test
	public void destinationMatchesAnyConfiguredAlias() {
		List<String> aliases = Arrays.asList("Munini DH", "Munini District Hospital");
		assertTrue(TransferRegistrationObsServiceImpl.destinationMatchesAnyFacility(
				"Munini District Hospital", aliases));
		assertTrue(TransferRegistrationObsServiceImpl.destinationMatchesAnyFacility("Munini DH", aliases));
		assertTrue(TransferRegistrationObsServiceImpl.destinationMatchesAnyFacility(
				"Transfer to Munini DH Emergency", aliases));
		assertFalse(TransferRegistrationObsServiceImpl.destinationMatchesAnyFacility(
				"Kigali University Teaching Hospital", aliases));
		assertFalse(TransferRegistrationObsServiceImpl.destinationMatchesAnyFacility("", aliases));
	}
}
