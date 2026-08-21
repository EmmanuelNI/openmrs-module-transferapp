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

final class IntouchSmsLogSupport {

	private IntouchSmsLogSupport() {
	}

	static String maskPhoneNumber(String phoneNumber) {
		if (StringUtils.isBlank(phoneNumber)) {
			return "(empty)";
		}
		String digits = phoneNumber.replaceAll("\\D", "");
		if (digits.length() <= 4) {
			return "****";
		}
		return digits.substring(0, Math.min(5, digits.length())) + "****"
				+ digits.substring(Math.max(5, digits.length() - 2));
	}

	static String truncate(String value, int maxLength) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		if (trimmed.length() <= maxLength) {
			return trimmed;
		}
		return trimmed.substring(0, maxLength) + "...";
	}
}
