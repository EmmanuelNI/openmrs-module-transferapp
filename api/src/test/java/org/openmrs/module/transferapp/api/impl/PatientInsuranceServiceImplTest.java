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
package org.openmrs.module.transferapp.api.impl;

import org.junit.Test;
import org.openmrs.module.transferapp.TransferAppConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatientInsuranceServiceImplTest {

	@Test
	public void matchCategoryByDisplayName_mapsNoneLabels() {
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_NONE,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("NONE"));
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_NONE,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("None"));
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_NONE,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("no insurance"));
	}

	@Test
	public void matchCategoryByDisplayName_mapsKnownInsurers() {
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_CBHI,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("CBHI (mutuelle)"));
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_RSSB,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("RSSB"));
		assertEquals(TransferAppConstants.HEALTH_INSURANCE_MMI,
				PatientInsuranceServiceImpl.matchCategoryByDisplayName("MMI"));
	}

	@Test
	public void matchCategoryByDisplayName_returnsNullForUnknown() {
		assertNull(PatientInsuranceServiceImpl.matchCategoryByDisplayName("RAMA Custom"));
		assertNull(PatientInsuranceServiceImpl.matchCategoryByDisplayName(""));
		assertNull(PatientInsuranceServiceImpl.matchCategoryByDisplayName(null));
	}
}
