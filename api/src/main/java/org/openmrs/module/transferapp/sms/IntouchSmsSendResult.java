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

public final class IntouchSmsSendResult {

	private final boolean success;
	private final boolean configured;
	private final int statusCode;
	private final String messageId;
	private final String errorMessage;
	private final String normalizedRecipient;

	private IntouchSmsSendResult(boolean success, boolean configured, int statusCode, String messageId,
			String errorMessage, String normalizedRecipient) {
		this.success = success;
		this.configured = configured;
		this.statusCode = statusCode;
		this.messageId = messageId;
		this.errorMessage = errorMessage;
		this.normalizedRecipient = normalizedRecipient;
	}

	public static IntouchSmsSendResult success(int statusCode, String messageId, String normalizedRecipient) {
		return new IntouchSmsSendResult(true, true, statusCode, messageId, null, normalizedRecipient);
	}

	public static IntouchSmsSendResult failure(int statusCode, String errorMessage, String normalizedRecipient) {
		return new IntouchSmsSendResult(false, true, statusCode, null, errorMessage, normalizedRecipient);
	}

	public static IntouchSmsSendResult notConfigured(String detail) {
		return new IntouchSmsSendResult(false, false, 0, null, detail, null);
	}

	public static IntouchSmsSendResult skipped(String reason) {
		return new IntouchSmsSendResult(false, true, 0, null, reason, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public boolean isConfigured() {
		return configured;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getNormalizedRecipient() {
		return normalizedRecipient;
	}
}
