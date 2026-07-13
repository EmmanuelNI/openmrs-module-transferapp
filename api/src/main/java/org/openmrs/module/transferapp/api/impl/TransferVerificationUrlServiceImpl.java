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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.TransferVerificationUrlService;
import org.openmrs.module.transferapp.model.Transfer;

public class TransferVerificationUrlServiceImpl implements TransferVerificationUrlService {

	@Override
	public String resolveVerificationTransferId(Transfer transfer) {
		if (transfer == null) {
			return null;
		}
		if (StringUtils.isNotBlank(transfer.getHieTransferId())) {
			return transfer.getHieTransferId().trim();
		}
		return StringUtils.trimToNull(transfer.getUuid());
	}

	@Override
	public String buildRemoteVerifyUrl(Transfer transfer) {
		String verificationTransferId = resolveVerificationTransferId(transfer);
		if (StringUtils.isBlank(verificationTransferId)) {
			return null;
		}
		return resolveVerifyBaseUrl() + "/verify/transfer/" + verificationTransferId + "/remote";
	}

	@Override
	public boolean shouldShowVerificationQr(Transfer transfer) {
		if (transfer == null) {
			return false;
		}
		return transfer.isSentToHie() || transfer.isReceivedFromHie();
	}

	private String resolveVerifyBaseUrl() {
		AdministrationService adminService = Context.getAdministrationService();
		String baseUrl = adminService.getGlobalProperty(
				TransferAppConstants.GP_VERIFY_BASE_URL,
				TransferAppConstants.DEFAULT_VERIFY_BASE_URL);
		return normalizeVerifyBaseUrl(baseUrl);
	}

	static String normalizeVerifyBaseUrl(String baseUrl) {
		if (baseUrl == null) {
			return "";
		}
		String trimmed = baseUrl.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

}
