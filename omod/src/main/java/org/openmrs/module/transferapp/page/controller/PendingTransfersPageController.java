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

import org.openmrs.api.PatientService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.TransferPrivilegeHelper;
import org.openmrs.module.transferapp.api.PendingTransferPatientStatusResolver;
import org.openmrs.module.transferapp.api.TransferHieSearchService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PendingTransfersPageController {

	public void get(UiSessionContext sessionContext,
			PageModel model,
			@SpringBean("transferAppHieSearchService") TransferHieSearchService transferHieSearchService,
			@SpringBean("patientService") PatientService patientService,
			@RequestParam(value = "app", required = false) String app) {

		sessionContext.requireAuthentication();

		boolean canListPending = TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_PENDING);
		model.addAttribute("canListPending", canListPending);
		model.addAttribute("requiredPendingPrivilege", TransferAppActivator.PRIVILEGE_LIST_PENDING);
		model.addAttribute("appId", app != null ? app : "transferapp.dashboard");
		model.addAttribute("rwandaEmrModuleId", TransferAppConstants.RWANDAEMR_MODULE_ID);
		model.addAttribute("requestAppointmentPage", TransferAppConstants.REQUEST_APPOINTMENT_PAGE);

		if (!canListPending) {
			model.addAttribute("pendingAccessDeniedMessage",
					TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_PENDING));
			model.addAttribute("pendingTransfers", Collections.emptyList());
			model.addAttribute("hasPendingTransfers", false);
			model.addAttribute("pendingErrorMessage", null);
			model.addAttribute("targetOrg", "");
			return;
		}

		try {
			Map<String, Object> result = transferHieSearchService.listPendingTransfersForCurrentFacility();
			String status = result.get("status") != null ? String.valueOf(result.get("status")) : "error";
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> data = result.get("data") instanceof List
					? (List<Map<String, Object>>) result.get("data")
					: Collections.<Map<String, Object>>emptyList();
			data = PendingTransferPatientStatusResolver.addPatientStatus(data, patientService);

			model.addAttribute("pendingTransfers", data);
			model.addAttribute("hasPendingTransfers", data != null && !data.isEmpty());
			model.addAttribute("targetOrg", result.get("targetOrg") != null ? String.valueOf(result.get("targetOrg")) : "");
			if ("success".equals(status)) {
				model.addAttribute("pendingErrorMessage", null);
				model.addAttribute("pendingAccessDeniedMessage", null);
			} else {
				Object message = result.get("message");
				model.addAttribute("pendingErrorMessage",
						message != null ? String.valueOf(message) : "Unable to load pending transfers from HIE");
				model.addAttribute("pendingAccessDeniedMessage", null);
			}
		}
		catch (Exception ex) {
			model.addAttribute("canListPending", false);
			model.addAttribute("pendingAccessDeniedMessage", TransferPrivilegeHelper.resolveUserFacingMessage(
					ex,
					TransferAppActivator.PRIVILEGE_LIST_PENDING,
					TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_PENDING)));
			model.addAttribute("pendingTransfers", Collections.emptyList());
			model.addAttribute("hasPendingTransfers", false);
			model.addAttribute("pendingErrorMessage", null);
			model.addAttribute("targetOrg", "");
		}
	}

}
