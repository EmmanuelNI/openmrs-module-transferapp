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

import org.apache.commons.lang.StringUtils;

/**
 * Same Kinyarwanda template used by the eTransfer portal after HIE acceptance.
 * Example: "Patience Ruberandinda, Mwahawe transferi ijya ku Muhima District Hospital. nimero yawe ni 270114-0334-1150"
 */
public final class TransferAcceptedSmsMessage {

	private TransferAcceptedSmsMessage() {
	}

	public static String build(String patientName, String receivingFacility, String upid) {
		return blankToDash(patientName)
				+ ", Mwahawe transferi ijya ku "
				+ blankToDash(receivingFacility)
				+ ". nimero yawe ni "
				+ blankToDash(upid);
	}

	private static String blankToDash(String value) {
		return StringUtils.isNotBlank(value) ? value.trim() : "—";
	}
}
