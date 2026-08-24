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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * High-level SMS helpers matching eTransfer {@code IntouchSmsService}.
 */
public class IntouchSmsService {

	private static final Log log = LogFactory.getLog(IntouchSmsService.class);

	private TransferIntouchSmsClient client = new TransferIntouchSmsClient();

	private IntouchSmsProperties properties = new IntouchSmsProperties();

	public void setClient(TransferIntouchSmsClient client) {
		this.client = client != null ? client : new TransferIntouchSmsClient();
	}

	public void setProperties(IntouchSmsProperties properties) {
		this.properties = properties != null ? properties : new IntouchSmsProperties();
		if (this.client != null) {
			this.client.setProperties(this.properties);
		}
	}

	public boolean isEnabled() {
		return properties.isConfigured();
	}

	public IntouchSmsSendResult sendIfEnabled(String to, String content) {
		if (properties.isLogOutboundMessage()) {
			log.info("Outbound SMS (local log) to=" + IntouchSmsLogSupport.maskPhoneNumber(to)
					+ " content=" + content);
		}
		if (!properties.isConfigured()) {
			return IntouchSmsSendResult.notConfigured(properties.configurationIssue());
		}
		if (StringUtils.isBlank(to) || !RwandaPhoneNumberNormalizer.looksLikeRwandaMobile(to)) {
			return IntouchSmsSendResult.skipped("Recipient phone number is missing or invalid.");
		}
		return client.send(to, content);
	}

	/**
	 * Sends the transfer-accepted SMS after HIE has successfully received the encounter.
	 */
	public IntouchSmsSendResult sendTransferAcceptedByHieIfEnabled(String recipientPhone, String patientName,
			String receivingFacility, String upid) {
		String message = TransferAcceptedSmsMessage.build(patientName, receivingFacility, upid);
		IntouchSmsSendResult result = sendIfEnabled(recipientPhone, message);
		if (!result.isSuccess() && result.isConfigured()) {
			log.warn("Transfer accepted SMS was not sent to "
					+ IntouchSmsLogSupport.maskPhoneNumber(recipientPhone) + ": " + result.getErrorMessage());
		}
		return result;
	}
}
