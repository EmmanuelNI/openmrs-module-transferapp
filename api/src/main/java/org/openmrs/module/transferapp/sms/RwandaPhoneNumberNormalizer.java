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
 * Normalizes Rwanda mobile numbers to 12 digits for InTouch SMS ({@code 250XXXXXXXXX}).
 */
public final class RwandaPhoneNumberNormalizer {

	private RwandaPhoneNumberNormalizer() {
	}

	public static String normalize(String raw) {
		if (StringUtils.isBlank(raw)) {
			throw new IllegalArgumentException("Phone number is required.");
		}
		String digits = raw.trim().replaceAll("[^0-9]", "");
		if (digits.startsWith("00")) {
			digits = digits.substring(2);
		}
		if (digits.startsWith("250") && digits.length() == 12) {
			return digits;
		}
		if (digits.startsWith("0") && digits.length() == 10) {
			return "250" + digits.substring(1);
		}
		if (digits.length() == 9 && digits.startsWith("7")) {
			return "250" + digits;
		}
		throw new IllegalArgumentException("Unsupported phone number format: " + raw);
	}

	public static boolean looksLikeRwandaMobile(String raw) {
		if (StringUtils.isBlank(raw)) {
			return false;
		}
		try {
			normalize(raw);
			return true;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}
}
