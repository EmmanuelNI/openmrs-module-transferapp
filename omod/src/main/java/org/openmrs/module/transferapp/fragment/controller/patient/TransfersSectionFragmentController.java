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
import org.openmrs.module.transferapp.TransferPrivilegeHelper;
import org.openmrs.module.transferapp.api.PatientInsuranceService;
import org.openmrs.module.transferapp.api.PatientTransferListService;
import org.openmrs.module.transferapp.api.TransferPatientSnapshotResolver;
import org.openmrs.module.transferapp.api.TransferProfileService;
import org.openmrs.module.transferapp.api.TransferRegistrationObsService;
import org.openmrs.module.transferapp.model.PatientInsuranceInfo;
import org.openmrs.module.transferapp.model.PatientTransferListItem;
import org.openmrs.module.transferapp.model.TransferProfile;
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
			@SpringBean("patientInsuranceService") PatientInsuranceService patientInsuranceService,
			@SpringBean("transferProfileService") TransferProfileService transferProfileService,
			@SpringBean("transferAppRegistrationObsService") TransferRegistrationObsService registrationObsService) {

		config.require("patient");
		Object patient = config.get("patient");

		if (patient instanceof Patient) {
			patientWrapper.setPatient((Patient) patient);
			config.addAttribute("patient", patientWrapper);
		} else if (patient instanceof PatientDomainWrapper) {
			patientWrapper = (PatientDomainWrapper) patient;
		}

		boolean canListTransfers = TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
		boolean canCreateTransfer = TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);
		boolean patientInsuranceAvailable = false;
		boolean providerProfileComplete = false;
		String accessDeniedMessage = null;

		List<PatientTransferListItem> transfers = Collections.emptyList();
		int totalPatientTransfers = 0;
		if (canListTransfers) {
			try {
				ui.includeCss("transferapp", "transferFormPreview.css");
				ui.includeCss("transferapp", "transferSection.css");
				ui.includeJavascript("transferapp", "transferMohLogo.js");
				ui.includeJavascript("transferapp", "transferFormPreview.js");
				ui.includeJavascript("transferapp", "hieTransferSection.js");
				totalPatientTransfers = patientTransferListService.countPatientTransfers(patientWrapper.getPatient());
				transfers = patientTransferListService.getPatientTransfers(
						patientWrapper.getPatient(),
						TransferAppConstants.PATIENT_DASHBOARD_TRANSFER_LIMIT);
			}
			catch (Exception ex) {
				canListTransfers = false;
				accessDeniedMessage = TransferPrivilegeHelper.resolveUserFacingMessage(
						ex,
						TransferAppActivator.PRIVILEGE_LIST_TRANSFERS,
						TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS));
				transfers = Collections.emptyList();
				totalPatientTransfers = 0;
			}
		}
		if (canCreateTransfer) {
			try {
				ui.includeCss("transferapp", "styles/transferWizard.css");
				ui.includeCss("transferapp", "styles/flatpickr.min.css");
				ui.includeCss("transferapp", "styles/select2.min.css");

				PatientInsuranceInfo patientInsurance = patientInsuranceService.getPatientInsurance(patientWrapper.getPatient());
				model.addAttribute("patientInsuranceType", patientInsurance.getInsuranceType());
				model.addAttribute("patientInsuranceNumber", patientInsurance.getInsuranceNumber());
				patientInsuranceAvailable = patientInsurance.isAvailable();
				model.addAttribute("patientInsuranceAvailable", patientInsuranceAvailable);

				User user = Context.getAuthenticatedUser();
				TransferProfile profile = user != null ? transferProfileService.getProfileForUser(user) : null;
				providerProfileComplete = profile != null && profile.isCompleteForTransfer();
			}
			catch (Exception ex) {
				canCreateTransfer = false;
				if (accessDeniedMessage == null) {
					accessDeniedMessage = TransferPrivilegeHelper.resolveUserFacingMessage(
							ex,
							TransferAppActivator.PRIVILEGE_CREATE_TRANSFER,
							TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER));
				}
			}
		}
		if (!canListTransfers && !canCreateTransfer && accessDeniedMessage == null) {
			accessDeniedMessage = ui.message("transferapp.patient.transfers.accessDenied",
					TransferAppActivator.PRIVILEGE_LIST_TRANSFERS,
					TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);
		}

		boolean hasTransfers = transfers != null && !transfers.isEmpty();
		boolean hasMorePatientTransfers = totalPatientTransfers > TransferAppConstants.PATIENT_DASHBOARD_TRANSFER_LIMIT;
		String recordsPageUrl = ui.pageLink("transferapp", "records")
				+ "?patientId=" + patientWrapper.getPatient().getPatientId()
				+ "&app=transferapp.dashboard";

		String recordedHieTransferId = null;
		String recordedHieTransferUpid = null;
		if (canListTransfers && patientWrapper.getPatient() != null) {
			try {
				recordedHieTransferId = registrationObsService.findRecordedHieTransferIdOnActiveVisit(
						patientWrapper.getPatient());
				if (recordedHieTransferId != null) {
					recordedHieTransferUpid = new TransferPatientSnapshotResolver()
							.resolveUpid(patientWrapper.getPatient());
				}
			}
			catch (Exception ignored) {
				recordedHieTransferId = null;
				recordedHieTransferUpid = null;
			}
		}
		boolean hasRecordedHieTransfer = recordedHieTransferId != null
				&& recordedHieTransferUpid != null
				&& recordedHieTransferUpid.trim().length() > 0;
		boolean canProvideFeedback = canCreateTransfer && hasRecordedHieTransfer;
		Integer patientId = patientWrapper.getPatient() != null ? patientWrapper.getPatient().getPatientId() : null;

		model.addAttribute("patient", patientWrapper);
		model.addAttribute("transfers", transfers);
		model.addAttribute("hasTransfers", hasTransfers);
		model.addAttribute("hasMorePatientTransfers", hasMorePatientTransfers);
		model.addAttribute("totalPatientTransfers", totalPatientTransfers);
		model.addAttribute("recordsPageUrl", recordsPageUrl);
		model.addAttribute("hasRecordedHieTransfer", hasRecordedHieTransfer);
		model.addAttribute("recordedHieTransferId", recordedHieTransferId != null ? recordedHieTransferId : "");
		model.addAttribute("recordedHieTransferUpid", recordedHieTransferUpid != null ? recordedHieTransferUpid : "");
		model.addAttribute("canListTransfers", canListTransfers);
		model.addAttribute("canCreateTransfer", canCreateTransfer);
		model.addAttribute("canProvideFeedback", canProvideFeedback);
		model.addAttribute("patientId", patientId);
		model.addAttribute("providerProfileComplete", providerProfileComplete);
		model.addAttribute("requiredListPrivilege", TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
		model.addAttribute("requiredCreatePrivilege", TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);
		model.addAttribute("accessDeniedMessage", accessDeniedMessage);
	}

}
