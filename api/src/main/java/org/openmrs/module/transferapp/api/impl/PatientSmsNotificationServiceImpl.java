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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.transferapp.api.PatientSmsNotificationService;
import org.openmrs.module.transferapp.model.Transfer;
import org.openmrs.module.transferapp.sms.IntouchSmsSendResult;
import org.openmrs.module.transferapp.sms.IntouchSmsService;

import java.util.Date;
import java.util.Locale;

public class PatientSmsNotificationServiceImpl implements PatientSmsNotificationService {

	private static final Log log = LogFactory.getLog(PatientSmsNotificationServiceImpl.class);

	private IntouchSmsService intouchSmsService = new IntouchSmsService();

	public void setIntouchSmsService(IntouchSmsService intouchSmsService) {
		this.intouchSmsService = intouchSmsService != null ? intouchSmsService : new IntouchSmsService();
	}

	@Override
	public IntouchSmsSendResult notifyPatientAfterHieAccepted(Transfer transfer, String receivingFacilityName) {
		if (transfer == null) {
			return IntouchSmsSendResult.skipped("Transfer is required.");
		}

		IntouchSmsSendResult result;
		try {
			result = intouchSmsService.sendTransferAcceptedByHieIfEnabled(
					transfer.getClientTelephone(),
					transfer.getClientName(),
					receivingFacilityName,
					transfer.getEmrId());
		}
		catch (RuntimeException ex) {
			log.error("Patient SMS notification failed unexpectedly for transfer uuid="
					+ transfer.getUuid() + ": " + ex.getMessage(), ex);
			result = IntouchSmsSendResult.failure(0,
					"Could not send patient SMS: " + ex.getMessage(),
					null);
		}

		applySendResult(transfer, result);
		return result;
	}

	static void applySendResult(Transfer transfer, IntouchSmsSendResult result) {
		Date now = new Date();
		transfer.setPatientSmsLastAttemptAt(now);
		if (result == null) {
			transfer.markPatientSmsFailed("No response from SMS gateway.");
			return;
		}
		if (result.isSuccess() && StringUtils.isNotBlank(result.getMessageId())) {
			transfer.markPatientSmsSent(result.getMessageId(), now);
			log.info("Patient SMS sent for transfer uuid=" + transfer.getUuid()
					+ " messageId=" + result.getMessageId());
			return;
		}
		if (!result.isConfigured()) {
			transfer.markPatientSmsSkipped("SMS Feature is disabled");
			return;
		}
		if (result.getStatusCode() == 0 && StringUtils.isNotBlank(result.getErrorMessage())
				&& result.getErrorMessage().toLowerCase(Locale.ROOT).contains("invalid")) {
			transfer.markPatientSmsSkipped(result.getErrorMessage());
			return;
		}
		if (result.getStatusCode() == 0 && !result.isSuccess()) {
			transfer.markPatientSmsSkipped(blankToDefault(result.getErrorMessage(), "SMS was not sent."));
			return;
		}
		transfer.markPatientSmsFailed(blankToDefault(result.getErrorMessage(),
				"SMS gateway did not return a message id."));
	}

	private static String blankToDefault(String value, String fallback) {
		return StringUtils.isNotBlank(value) ? value.trim() : fallback;
	}
}
