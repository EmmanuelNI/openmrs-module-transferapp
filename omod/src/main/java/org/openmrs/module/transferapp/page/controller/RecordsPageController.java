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

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.FacilityTransferRecordsService;
import org.openmrs.module.transferapp.model.FacilityTransferRecordItem;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

public class RecordsPageController {

	public void get(UiSessionContext sessionContext,
			PageModel model,
			@SpringBean("facilityTransferRecordsService") FacilityTransferRecordsService facilityTransferRecordsService,
			@SpringBean("patientService") PatientService patientService,
			@RequestParam(value = "patientId", required = false) Integer patientId,
			@RequestParam(value = "app", required = false) String app) {

		sessionContext.requireAuthentication();

		User currentUser = Context.getAuthenticatedUser();
		boolean canListTransfers = currentUser != null
				&& currentUser.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
		boolean canCreateTransfer = currentUser != null
				&& currentUser.hasPrivilege(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);

		List<FacilityTransferRecordItem> records = Collections.emptyList();
		String filteredPatientName = null;
		if (canListTransfers) {
			records = facilityTransferRecordsService.getOutboundTransferRecords(patientId);
			if (patientId != null) {
				Patient patient = patientService.getPatient(patientId);
				if (patient != null && patient.getPersonName() != null) {
					filteredPatientName = patient.getPersonName().getFullName();
				}
			}
		}

		model.addAttribute("records", records);
		model.addAttribute("hasRecords", records != null && !records.isEmpty());
		model.addAttribute("canListTransfers", canListTransfers);
		model.addAttribute("canCreateTransfer", canCreateTransfer);
		model.addAttribute("filteredPatientId", patientId);
		model.addAttribute("filteredPatientName", filteredPatientName);
		model.addAttribute("appId", app != null ? app : "transferapp.dashboard");
	}

}
