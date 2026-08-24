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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses InTouch gateway responses. Success bodies include bare UUID, quoted UUID, and
 * {@code Success "d3d5c5c0-86d0-4915-922f-b4dca4bedd31"}.
 */
final class IntouchSmsResponseParser {

	private static final Pattern UUID = Pattern.compile(
			"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

	private IntouchSmsResponseParser() {
	}

	static String parseMessageId(int statusCode, String body) {
		if (statusCode < 200 || statusCode >= 300) {
			return null;
		}
		return extractUuid(normalizeBody(body));
	}

	static String parseErrorMessage(int statusCode, String body) {
		if (statusCode >= 200 && statusCode < 300) {
			return "Unexpected SMS gateway response: " + normalizeBody(body);
		}
		String normalized = normalizeBody(body);
		if (StringUtils.isBlank(normalized)) {
			return "SMS gateway request failed with HTTP " + statusCode + ".";
		}
		return normalized;
	}

	private static String extractUuid(String normalized) {
		if (StringUtils.isBlank(normalized)) {
			return null;
		}
		Matcher matcher = UUID.matcher(normalized);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	private static String normalizeBody(String body) {
		if (body == null) {
			return "";
		}
		String trimmed = body.trim();
		if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
			trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
		}
		String lower = trimmed.toLowerCase(Locale.ROOT);
		if (lower.startsWith("error ")) {
			trimmed = trimmed.substring(6).trim();
			if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
				trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
			}
		}
		else if (lower.startsWith("success")) {
			trimmed = trimmed.substring("success".length()).trim();
			if (trimmed.startsWith(":") || trimmed.startsWith("-")) {
				trimmed = trimmed.substring(1).trim();
			}
			if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
				trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
			}
		}
		return trimmed;
	}
}
