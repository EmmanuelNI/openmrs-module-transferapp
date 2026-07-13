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
package org.openmrs.module.transferapp.fragment.controller.patient;

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.api.PatientInsuranceService;
import org.openmrs.module.transferapp.api.PatientTransferListService;
import org.openmrs.module.transferapp.model.PatientInsuranceInfo;
import org.openmrs.module.transferapp.model.PatientTransferListItem;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Collections;
import java.util.List;

/**
 * Patient dashboard fragment listing transfers received from HIE.
 */
public class TransfersSectionFragmentController {

	public void controller(FragmentConfiguration config,
			PageModel pageModel,
			FragmentModel model,
			UiUtils ui,
			UiSessionContext sessionContext,
			@FragmentParam("app") AppDescriptor appDescriptor,
			@InjectBeans PatientDomainWrapper patientWrapper,
			@SpringBean("patientTransferListService") PatientTransferListService patientTransferListService,
			@SpringBean("patientInsuranceService") PatientInsuranceService patientInsuranceService) {

		config.require("patient");
		Object patient = config.get("patient");

		if (patient instanceof Patient) {
			patientWrapper.setPatient((Patient) patient);
			config.addAttribute("patient", patientWrapper);
		} else if (patient instanceof PatientDomainWrapper) {
			patientWrapper = (PatientDomainWrapper) patient;
		}

		User currentUser = Context.getAuthenticatedUser();
		boolean canListTransfers = currentUser != null
				&& currentUser.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
		boolean canCreateTransfer = currentUser != null
				&& currentUser.hasPrivilege(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);

		List<PatientTransferListItem> transfers = Collections.emptyList();
		int totalPatientTransfers = 0;
		if (canListTransfers) {
			ui.includeCss("transferapp", "styles/transferFormPreview.css");
			ui.includeJavascript("transferapp", "scripts/transferMohLogo.js");
			ui.includeJavascript("transferapp", "scripts/transferFormPreview.js");
			totalPatientTransfers = patientTransferListService.countPatientTransfers(patientWrapper.getPatient());
			transfers = patientTransferListService.getPatientTransfers(
					patientWrapper.getPatient(),
					TransferAppConstants.PATIENT_DASHBOARD_TRANSFER_LIMIT);
		}
		if (canCreateTransfer) {
			ui.includeCss("transferapp", "styles/transferWizard.css");
			ui.includeCss("transferapp", "styles/flatpickr.min.css");
			ui.includeCss("transferapp", "styles/select2.min.css");

			PatientInsuranceInfo patientInsurance = patientInsuranceService.getPatientInsurance(patientWrapper.getPatient());
			model.addAttribute("patientInsuranceType", patientInsurance.getInsuranceType());
			model.addAttribute("patientInsuranceNumber", patientInsurance.getInsuranceNumber());
			model.addAttribute("patientInsuranceAvailable", patientInsurance.isAvailable());
		}
		boolean hasTransfers = transfers != null && !transfers.isEmpty();
		boolean hasMorePatientTransfers = totalPatientTransfers > TransferAppConstants.PATIENT_DASHBOARD_TRANSFER_LIMIT;
		String recordsPageUrl = ui.pageLink("transferapp", "records")
				+ "?patientId=" + patientWrapper.getPatient().getPatientId()
				+ "&app=transferapp.dashboard";

		model.addAttribute("patient", patientWrapper);
		model.addAttribute("transfers", transfers);
		model.addAttribute("hasTransfers", hasTransfers);
		model.addAttribute("hasMorePatientTransfers", hasMorePatientTransfers);
		model.addAttribute("totalPatientTransfers", totalPatientTransfers);
		model.addAttribute("recordsPageUrl", recordsPageUrl);
		model.addAttribute("canListTransfers", canListTransfers);
		model.addAttribute("canCreateTransfer", canCreateTransfer);
	}

}
