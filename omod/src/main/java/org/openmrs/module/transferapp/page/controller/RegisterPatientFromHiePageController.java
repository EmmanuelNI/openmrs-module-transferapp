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
package org.openmrs.module.transferapp.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.TransferPrivilegeHelper;
import org.openmrs.module.transferapp.api.ClientRegistryRegistrationService;
import org.openmrs.module.transferapp.api.HiePatientRegistrationResult;
import org.openmrs.module.uicommons.util.InfoErrorMessageUtil;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Creates a local OpenMRS patient from the HIE client registry when Register is clicked.
 */
public class RegisterPatientFromHiePageController {

	private static final Log log = LogFactory.getLog(RegisterPatientFromHiePageController.class);

	public String get(UiSessionContext sessionContext,
			UiUtils ui,
			PageModel model,
			@SpringBean("transferAppClientRegistryRegistrationService")
			ClientRegistryRegistrationService clientRegistryRegistrationService,
			@RequestParam("upid") String upid,
			@RequestParam(value = "returnUrl", required = false) String returnUrl) {

		requireAccess(sessionContext);
		String normalizedUpid = StringUtils.trimToNull(upid);
		String safeReturnUrl = safeReturnUrl(returnUrl, ui);

		try {
			Map<String, Object> patientDetails = clientRegistryRegistrationService
					.findRegistrationFieldsByUpid(normalizedUpid);
			if (patientDetails == null) {
				throw new IllegalStateException("No patient was found in the HIE client registry for UPID "
						+ normalizedUpid);
			}

			model.addAttribute("patientDetails", patientDetails);
			model.addAttribute("upid", normalizedUpid);
			model.addAttribute("returnUrl", safeReturnUrl);
			model.addAttribute("cancelUrl", "/" + ui.contextPath() + safeReturnUrl);
			return null;
		}
		catch (Exception ex) {
			log.error("Unable to preview the HIE patient with UPID " + normalizedUpid, ex);
			String reason = StringUtils.isNotBlank(ex.getMessage())
					? ex.getMessage() : ui.message("transferapp.pending.registration.error");
			InfoErrorMessageUtil.flashErrorMessage(sessionContext.getSession(), reason);
			return "redirect:" + safeReturnUrl;
		}
	}

	public String post(UiSessionContext sessionContext,
			UiUtils ui,
			@SpringBean("transferAppClientRegistryRegistrationService")
			ClientRegistryRegistrationService clientRegistryRegistrationService,
			@RequestParam("upid") String upid,
			@RequestParam(value = "returnUrl", required = false) String returnUrl) {

		requireAccess(sessionContext);

		String normalizedUpid = StringUtils.trimToNull(upid);
		try {
			HiePatientRegistrationResult result = clientRegistryRegistrationService.registerPatientByUpid(
					normalizedUpid, sessionContext.getSessionLocation());
			Patient patient = result.getPatient();
			PatientIdentifier preferredIdentifier = patient.getPatientIdentifier();
			String localIdentifier = preferredIdentifier != null ? preferredIdentifier.getIdentifier() : "";
			String messageCode = result.isCreated()
					? "transferapp.pending.registration.created"
					: "transferapp.pending.registration.exists";
			InfoErrorMessageUtil.flashInfoMessage(sessionContext.getSession(),
					ui.message(messageCode, normalizedUpid, localIdentifier));
		}
		catch (Exception ex) {
			log.error("Unable to register the HIE patient with UPID " + normalizedUpid, ex);
			String reason = StringUtils.isNotBlank(ex.getMessage())
					? ex.getMessage() : ui.message("transferapp.pending.registration.error");
			InfoErrorMessageUtil.flashErrorMessage(sessionContext.getSession(),
					ui.message("transferapp.pending.registration.failed", normalizedUpid, reason));
		}

		return "redirect:" + safeReturnUrl(returnUrl, ui);
	}

	private void requireAccess(UiSessionContext sessionContext) {
		sessionContext.requireAuthentication();
		if (!TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_PENDING)) {
			throw new APIAuthenticationException(TransferPrivilegeHelper.requiredPrivilegeMessage(
					TransferAppActivator.PRIVILEGE_LIST_PENDING));
		}
	}

	private String safeReturnUrl(String returnUrl, UiUtils ui) {
		String normalized = StringUtils.trimToNull(returnUrl);
		if (normalized != null && normalized.startsWith("/transferapp/") && !normalized.startsWith("//")) {
			return normalized;
		}
		return ui.pageLinkWithoutContextPath("transferapp", "pending", null);
	}
}
