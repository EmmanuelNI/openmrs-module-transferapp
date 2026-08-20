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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Controller for pending.gsp (must match page name "pending").
 */
public class PendingPageController {

	public void get(UiSessionContext sessionContext,
			PageModel model,
			@SpringBean("transferAppHieSearchService") TransferHieSearchService transferHieSearchService,
			@SpringBean("patientService") PatientService patientService,
			@RequestParam(value = "app", required = false) String app,
			@RequestParam(value = "weeks", required = false) String weeks) {

		sessionContext.requireAuthentication();
		int selectedWeeks = parseSelectedWeeks(weeks);

		boolean canListPending = TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_PENDING);
		model.addAttribute("canListPending", canListPending);
		model.addAttribute("requiredPendingPrivilege", TransferAppActivator.PRIVILEGE_LIST_PENDING);
		model.addAttribute("appId", app != null ? app : "transferapp.dashboard");
		model.addAttribute("rwandaEmrModuleId", TransferAppConstants.RWANDAEMR_MODULE_ID);
		model.addAttribute("requestAppointmentPage", TransferAppConstants.REQUEST_APPOINTMENT_PAGE);
		model.addAttribute("pendingServices", Collections.emptyList());
		model.addAttribute("pendingDates", Collections.emptyList());
		model.addAttribute("selectedWeeks", selectedWeeks);

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
			Map<String, Object> result = transferHieSearchService
					.listPendingTransfersForCurrentFacility(selectedWeeks);
			String status = result.get("status") != null ? String.valueOf(result.get("status")) : "error";
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> data = result.get("data") instanceof List
					? (List<Map<String, Object>>) result.get("data")
					: Collections.<Map<String, Object>>emptyList();
			data = PendingTransferPatientStatusResolver.addPatientStatus(data, patientService);

			model.addAttribute("pendingTransfers", data);
			model.addAttribute("hasPendingTransfers", data != null && !data.isEmpty());
			model.addAttribute("pendingServices", extractReceivingServices(data));
			model.addAttribute("pendingDates", extractTransferDates(data));
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

	private int parseSelectedWeeks(String weeks) {
		if (weeks != null) {
			try {
				int parsed = Integer.parseInt(weeks.trim());
				if (parsed >= TransferHieSearchService.DEFAULT_PENDING_WEEKS
						&& parsed <= TransferHieSearchService.MAX_PENDING_WEEKS) {
					return parsed;
				}
			}
			catch (NumberFormatException ignored) {
				// Invalid query parameters use the one-week default.
			}
		}
		return TransferHieSearchService.DEFAULT_PENDING_WEEKS;
	}

	private List<String> extractReceivingServices(List<Map<String, Object>> transfers) {
		Set<String> services = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		if (transfers != null) {
			for (Map<String, Object> transfer : transfers) {
				if (transfer == null || transfer.get("receivingService") == null) {
					continue;
				}
				String service = String.valueOf(transfer.get("receivingService")).trim();
				if (!service.isEmpty()) {
					services.add(service);
				}
			}
		}
		return new ArrayList<String>(services);
	}

	private List<String> extractTransferDates(List<Map<String, Object>> transfers) {
		Set<String> dates = new TreeSet<String>(Collections.reverseOrder());
		if (transfers != null) {
			for (Map<String, Object> transfer : transfers) {
				if (transfer == null || transfer.get("date") == null) {
					continue;
				}
				String date = String.valueOf(transfer.get("date")).trim();
				if (!date.isEmpty()) {
					dates.add(date);
				}
			}
		}
		return new ArrayList<String>(dates);
	}

}
