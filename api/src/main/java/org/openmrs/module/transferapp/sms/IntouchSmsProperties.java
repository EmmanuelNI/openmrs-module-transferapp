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
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;

/**
 * Reads InTouch SMS settings from OpenMRS global properties (same gateway as eTransfer).
 */
public class IntouchSmsProperties {

	public boolean isEnabled() {
		return parseBoolean(get(TransferAppConstants.GP_SMS_INTOUCH_ENABLED), false);
	}

	public String getBaseUrl() {
		return firstNonBlank(get(TransferAppConstants.GP_SMS_INTOUCH_BASE_URL),
				TransferAppConstants.DEFAULT_SMS_INTOUCH_BASE_URL);
	}

	public String getSendPath() {
		return firstNonBlank(get(TransferAppConstants.GP_SMS_INTOUCH_SEND_PATH),
				TransferAppConstants.DEFAULT_SMS_INTOUCH_SEND_PATH);
	}

	public String getUsername() {
		return trimToEmpty(get(TransferAppConstants.GP_SMS_INTOUCH_USERNAME));
	}

	public String getPassword() {
		return trimToEmpty(get(TransferAppConstants.GP_SMS_INTOUCH_PASSWORD));
	}

	public String getSenderId() {
		return firstNonBlank(get(TransferAppConstants.GP_SMS_INTOUCH_SENDER_ID),
				TransferAppConstants.DEFAULT_SMS_INTOUCH_SENDER_ID);
	}

	public int getCoding() {
		return parseInt(get(TransferAppConstants.GP_SMS_INTOUCH_CODING), 0);
	}

	public int getDlrLevel() {
		return parseInt(get(TransferAppConstants.GP_SMS_INTOUCH_DLR_LEVEL), 2);
	}

	public String getDlrUrl() {
		return trimToEmpty(get(TransferAppConstants.GP_SMS_INTOUCH_DLR_URL));
	}

	public boolean isLogOutboundMessage() {
		return parseBoolean(get(TransferAppConstants.GP_SMS_INTOUCH_LOG_OUTBOUND), false);
	}

	public boolean isConfigured() {
		return isEnabled()
				&& StringUtils.isNotBlank(getBaseUrl())
				&& StringUtils.isNotBlank(getUsername())
				&& StringUtils.isNotBlank(getPassword())
				&& StringUtils.isNotBlank(getSenderId());
	}

	public String configurationIssue() {
		if (!isEnabled()) {
			return "SMS Feature is disabled";
		}
		if (StringUtils.isBlank(getBaseUrl())
				|| StringUtils.isBlank(getUsername())
				|| StringUtils.isBlank(getPassword())
				|| StringUtils.isBlank(getSenderId())) {
			return "SMS Feature is disabled";
		}
		return "";
	}

	public String sendUrl() {
		String base = trimTrailingSlash(getBaseUrl());
		String path = getSendPath();
		if (StringUtils.isBlank(path)) {
			path = "/send";
		}
		path = path.trim();
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return base + path;
	}

	private static String get(String property) {
		try {
			return Context.getAdministrationService().getGlobalProperty(property);
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static boolean parseBoolean(String value, boolean defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim())
				|| "yes".equalsIgnoreCase(value.trim());
	}

	private static int parseInt(String value, int defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		}
		catch (NumberFormatException ex) {
			return defaultValue;
		}
	}

	private static String firstNonBlank(String primary, String fallback) {
		if (StringUtils.isNotBlank(primary)) {
			return primary.trim();
		}
		return fallback == null ? "" : fallback.trim();
	}

	private static String trimToEmpty(String value) {
		return value == null ? "" : value.trim();
	}

	private static String trimTrailingSlash(String value) {
		if (StringUtils.isBlank(value)) {
			return "";
		}
		String trimmed = value.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}
}
