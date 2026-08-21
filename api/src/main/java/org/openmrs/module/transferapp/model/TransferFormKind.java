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
package org.openmrs.module.transferapp.model;

import org.apache.commons.lang.StringUtils;

/**
 * Identifies which transfer form / preview / PDF layout applies.
 * Aligns with eTransfer {@code TransferFormKind}: GENERAL = external transfer form.
 */
public enum TransferFormKind {

	GENERAL("external", "External transfer form"),
	MATERNITY("maternity", "ANC, delivery and PNC external transfer form"),
	NEONATAL("neonatal", "Neonatal transfer form");

	public static final String EXTENSION_URL =
			"http://example.org/fhir/StructureDefinition/transfer-form-kind";

	public static final String CODE_SYSTEM =
			"http://example.org/fhir/CodeSystem/transfer-form-kind";

	private final String code;
	private final String display;

	TransferFormKind(String code, String display) {
		this.code = code;
		this.display = display;
	}

	public String getCode() {
		return code;
	}

	public String getDisplay() {
		return display;
	}

	/**
	 * Resolves from FHIR codes, enum names, or loose labels. Defaults to {@link #GENERAL}
	 * (external transfer form) when blank or unrecognized.
	 */
	public static TransferFormKind fromCodeOrLabel(String raw) {
		if (StringUtils.isBlank(raw)) {
			return GENERAL;
		}
		String normalized = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_');
		if ("EXTERNAL".equals(normalized) || "EXTERNAL_TRANSFER".equals(normalized)
				|| "EXTERNAL_TRANSFER_FORM".equals(normalized) || "GENERAL".equals(normalized)) {
			return GENERAL;
		}
		if ("MATERNITY".equals(normalized) || "ANC".equals(normalized)
				|| "MATERNITY_TRANSFER".equals(normalized) || "MATERNITY_TRANSFER_FORM".equals(normalized)) {
			return MATERNITY;
		}
		if ("NEONATAL".equals(normalized) || "NEONATE".equals(normalized)
				|| "NEONATAL_TRANSFER".equals(normalized) || "NEONATAL_TRANSFER_FORM".equals(normalized)) {
			return NEONATAL;
		}
		for (TransferFormKind kind : values()) {
			if (kind.name().equals(normalized) || kind.code.equalsIgnoreCase(raw.trim())) {
				return kind;
			}
		}
		return GENERAL;
	}

	public boolean isExternalTransferForm() {
		return this == GENERAL;
	}

	public boolean isMaternityTransferForm() {
		return this == MATERNITY;
	}

	public boolean isNeonatalTransferForm() {
		return this == NEONATAL;
	}
}
