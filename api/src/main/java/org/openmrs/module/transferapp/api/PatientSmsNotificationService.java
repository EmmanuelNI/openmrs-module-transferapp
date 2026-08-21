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

import org.openmrs.module.transferapp.model.Transfer;
import org.openmrs.module.transferapp.sms.IntouchSmsSendResult;

/**
 * Sends the patient SMS after a transfer has been accepted by HIE (same flow as eTransfer).
 */
public interface PatientSmsNotificationService {

	/**
	 * Sends the post-HIE patient SMS and updates SMS status fields on the transfer.
	 * Does not save the transfer — caller must save.
	 * Never throws for gateway/config issues; returns a result and marks status instead.
	 */
	IntouchSmsSendResult notifyPatientAfterHieAccepted(Transfer transfer, String receivingFacilityName);
}
