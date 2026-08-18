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
package org.openmrs.module.transferapp.web;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.TransferPrivilegeHelper;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferHieSubmissionService;
import org.openmrs.module.transferapp.api.TransferProfileService;
import org.openmrs.module.transferapp.api.TransferQrCodeService;
import org.openmrs.module.transferapp.api.TransferService;
import org.openmrs.module.transferapp.api.TransferVerificationUrlService;
import org.openmrs.module.transferapp.model.Transfer;
import org.openmrs.module.transferapp.model.TransferFormExtras;
import org.openmrs.module.transferapp.model.TransferProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TransferSaveController {

	private static final String DATETIME_PATTERN = "dd.MMM.yyyy, HH:mm:ss";

	@Autowired
	private TransferService transferService;

	@Autowired
	private TransferAdminService transferAdminService;

	private TransferHieSubmissionService getTransferHieSubmissionService() {
		return Context.getService(TransferHieSubmissionService.class);
	}

	private TransferVerificationUrlService getTransferVerificationUrlService() {
		return Context.getService(TransferVerificationUrlService.class);
	}

	private TransferQrCodeService getTransferQrCodeService() {
		return Context.getService(TransferQrCodeService.class);
	}

	@RequestMapping(value = "/module/transferapp/transfer/submit.form", method = RequestMethod.POST)
	public void submitTransferToHie(HttpServletResponse response,
			@RequestParam("uuid") String uuid) throws Exception {

		Map<String, Object> data = new HashMap<String, Object>();

		if (!TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER)) {
			writePrivilegeDenied(response, data, TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);
			return;
		}

		try {
			Transfer transfer = getTransferHieSubmissionService().submitTransferToHie(uuid);
			data.put("status", "success");
			data.put("uuid", transfer.getUuid());
			data.put("hieSent", transfer.isSentToHie());
			data.put("hieSentAt", formatDateTime(transfer.getHieSentAt()));
		}
		catch (Exception e) {
			putError(data, e, TransferAppActivator.PRIVILEGE_CREATE_TRANSFER, "Unable to submit transfer to HIE");
		}

		writeJson(response, data);
	}

	@RequestMapping(value = "/module/transferapp/transfer/save.form", method = RequestMethod.POST)
	public void saveReferralTransfer(HttpServletResponse response,
			@RequestParam("patientId") Integer patientId,
			@RequestParam(value = "transferUuid", required = false) String transferUuid,
			@RequestParam(value = "decisionToTransferAt", required = false) String decisionToTransferAt,
			@RequestParam(value = "callingTime", required = false) String callingTime,
			@RequestParam(value = "receivingFacilityCode", required = false) String receivingFacilityCode,
			@RequestParam(value = "receivingFacilityId", required = false) Integer receivingFacilityId,
			@RequestParam(value = "receivingService", required = false) String receivingService,
			@RequestParam(value = "staffContactedName", required = false) String staffContactedName,
			@RequestParam(value = "staffContactedPhone", required = false) String staffContactedPhone,
			@RequestParam(value = "transferType", required = false) String transferType,
			@RequestParam(value = "ambulanceCalledTime", required = false) String ambulanceCalledTime,
			@RequestParam(value = "departureFromReferringTime", required = false) String departureFromReferringTime,
			@RequestParam(value = "transportationType", required = false) String transportationType,
			@RequestParam(value = "transportationOtherSpec", required = false) String transportationOtherSpec,
			@RequestParam(value = "reasonForTransfer", required = false) String reasonForTransfer,
			@RequestParam(value = "clinicalPresentation", required = false) String clinicalPresentation,
			@RequestParam(value = "disabilityType", required = false) String disabilityType,
			@RequestParam(value = "laboratory", required = false) String laboratory,
			@RequestParam(value = "proceduresTreatments", required = false) String proceduresTreatments,
			@RequestParam(value = "otherNotes", required = false) String otherNotes,
			@RequestParam(value = "diagnosis", required = false) String diagnosis,
			@RequestParam(value = "providerQualification", required = false) String providerQualification,
			@RequestParam(value = "signedDate", required = false) String signedDate,
			@RequestParam(value = "signedTime", required = false) String signedTime) throws Exception {

		Map<String, Object> data = new HashMap<String, Object>();

		if (!TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_CREATE_TRANSFER)) {
			writePrivilegeDenied(response, data, TransferAppActivator.PRIVILEGE_CREATE_TRANSFER);
			return;
		}

		try {
			TransferFormExtras formExtras = buildFormExtras(clinicalPresentation, disabilityType, laboratory,
					proceduresTreatments, otherNotes, diagnosis, providerQualification, signedDate, signedTime);
			Transfer transfer = transferService.saveReferralTransfer(
					patientId,
					transferUuid,
					decisionToTransferAt,
					callingTime,
					receivingFacilityCode,
					receivingFacilityId,
					receivingService,
					staffContactedName,
					staffContactedPhone,
					transferType,
					ambulanceCalledTime,
					departureFromReferringTime,
					transportationType,
					transportationOtherSpec,
					reasonForTransfer,
					formExtras);

			data.put("status", "success");
			data.put("transferId", transfer.getTransferId());
			data.put("uuid", transfer.getUuid());
			data.put("hieSent", transfer.isSentToHie());
			data.put("updated", StringUtils.isNotBlank(transferUuid));
		}
		catch (Exception e) {
			putError(data, e, TransferAppActivator.PRIVILEGE_CREATE_TRANSFER, "Unable to save transfer");
		}

		writeJson(response, data);
	}

	@RequestMapping(value = "/module/transferapp/transfer/preview.form", method = RequestMethod.GET)
	public void previewTransfer(HttpServletResponse response,
			@RequestParam("uuid") String uuid) throws Exception {

		Map<String, Object> data = new HashMap<String, Object>();

		if (!TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS)) {
			writePrivilegeDenied(response, data, TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
			return;
		}

		try {
			Transfer transfer = transferService.getTransferByUuid(uuid);
			if (transfer == null) {
				data.put("status", "error");
				data.put("message", "Transfer not found");
			} else {
				data.put("status", "success");
				data.put("transfer", toPreviewMap(transfer));
			}
		}
		catch (Exception e) {
			putError(data, e, TransferAppActivator.PRIVILEGE_LIST_TRANSFERS, "Unable to load transfer");
		}

		writeJson(response, data);
	}

	@RequestMapping(value = "/module/transferapp/transfer/verifyQr.form", method = RequestMethod.GET)
	public void verifyQr(HttpServletResponse response,
			@RequestParam(value = "uuid", required = false) String uuid,
			@RequestParam(value = "transferId", required = false) String transferId) throws Exception {
		if (!TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN,
					TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS));
			return;
		}

		try {
			TransferVerificationUrlService verificationUrlService = getTransferVerificationUrlService();
			String verifyUrl = null;

			if (StringUtils.isNotBlank(uuid)) {
				Transfer transfer = transferService.getTransferByUuid(uuid.trim());
				if (transfer != null && verificationUrlService.shouldShowVerificationQr(transfer)) {
					verifyUrl = verificationUrlService.buildRemoteVerifyUrl(transfer);
				}
			}

			if (StringUtils.isBlank(verifyUrl) && StringUtils.isNotBlank(transferId)) {
				verifyUrl = verificationUrlService.buildRemoteVerifyUrlForTransferId(transferId.trim());
			}

			if (StringUtils.isBlank(verifyUrl)) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			byte[] png = getTransferQrCodeService().generatePng(verifyUrl);
			response.setContentType("image/png");
			response.setHeader("Cache-Control", "no-store");
			response.getOutputStream().write(png);
		}
		catch (Exception e) {
			if (TransferPrivilegeHelper.isPrivilegeException(e)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						TransferPrivilegeHelper.resolveUserFacingMessage(
								e,
								TransferAppActivator.PRIVILEGE_LIST_TRANSFERS,
								TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS)));
				return;
			}
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					e.getMessage() != null ? e.getMessage() : "Unable to generate QR code");
		}
	}

	private Map<String, Object> toPreviewMap(Transfer transfer) {
		Map<String, Object> preview = new HashMap<String, Object>();
		preview.put("uuid", transfer.getUuid());

		preview.put("province", nullToEmpty(transfer.getReceivingProvince()));
		preview.put("district", nullToEmpty(transfer.getReceivingDistrict()));
		preview.put("hospitalName", nullToEmpty(transfer.getSendingFacility()));
		preview.put("referringFacilityName", nullToEmpty(transfer.getSendingFacility()));
		preview.put("referringUnit", nullToEmpty(transfer.getReferringUnit()));
		preview.put("receivingClinicianPhone", nullToEmpty(transfer.getStaffContactedPhone()));

		preview.put("clientName", nullToEmpty(transfer.getClientName()));
		preview.put("emrId", nullToEmpty(transfer.getEmrId()));
		preview.put("serialNumberEmr", nullToEmpty(transfer.getEmrId()));
		preview.put("clientTelephone", nullToEmpty(transfer.getClientTelephone()));
		preview.put("ageOrDob", nullToEmpty(transfer.getAgeOrDob()));
		preview.put("sex", nullToEmpty(transfer.getSex()));
		preview.put("identifierType", nullToEmpty(transfer.getIdentifierType()));
		preview.put("identifierValue", nullToEmpty(transfer.getIdentifierValue()));
		preview.put("caregiverName", nullToEmpty(transfer.getCaregiverName()));
		preview.put("caregiverTelephone", nullToEmpty(transfer.getCaregiverTelephone()));
		preview.put("clientDistrict", nullToEmpty(transfer.getClientDistrict()));
		preview.put("sector", nullToEmpty(transfer.getSector()));
		preview.put("cell", nullToEmpty(transfer.getCell()));
		preview.put("village", nullToEmpty(transfer.getVillage()));
		preview.put("sendingFacility", nullToEmpty(transfer.getSendingFacility()));

		preview.put("admissionAt", formatDateTime(transfer.getAdmissionAt()));
		preview.put("decisionToTransferAt", formatDateTime(transfer.getDecisionToTransferAt()));
		preview.put("transferDecisionDatetime", formatDateTime(transfer.getDecisionToTransferAt()));
		preview.put("callingTime", nullToEmpty(transfer.getCallingTime()));
		preview.put("receivingFacility", resolveFacilityLabel(transfer.getReceivingFacilityCode()));
		preview.put("receivingFacilityCode", nullToEmpty(transfer.getReceivingFacilityCode()));
		preview.put("receivingService", nullToEmpty(transfer.getReceivingService()));
		preview.put("staffContactedName", nullToEmpty(transfer.getStaffContactedName()));
		preview.put("staffContactedAtReceivingFacility", nullToEmpty(transfer.getStaffContactedName()));
		preview.put("staffContactedPhone", nullToEmpty(transfer.getStaffContactedPhone()));

		String transferType = nullToEmpty(transfer.getTransferType());
		preview.put("transferType", transferType);
		preview.put("isEmergency", "EMERGENCY".equals(transferType));
		preview.put("isNonEmergency", "NOT_EMERGENCY".equals(transferType));
		preview.put("isFollowUp", "FOLLOW_UP".equals(transferType));
		preview.put("ambulanceCalledTime", nullToEmpty(transfer.getAmbulanceCallTime()));
		preview.put("departureFromReferringTime", nullToEmpty(transfer.getDepartRefTime()));
		preview.put("reasonForTransfer", nullToEmpty(transfer.getReasonForTransfer()));

		preview.put("clinicalPresentation", nullToEmpty(transfer.getClinicalPresentation()));
		preview.put("disabilityType", nullToEmpty(transfer.getDisabilityType()));
		preview.put("vitalTemp", nullToEmpty(transfer.getVitalTemp()));
		preview.put("vitalSpo2", nullToEmpty(transfer.getVitalSpo2()));
		preview.put("vitalRr", nullToEmpty(transfer.getVitalRr()));
		preview.put("vitalPulse", nullToEmpty(transfer.getVitalPulse()));
		preview.put("vitalBp", nullToEmpty(transfer.getVitalBp()));
		preview.put("vitalWeight", nullToEmpty(transfer.getVitalWt()));
		preview.put("vitalHeight", nullToEmpty(transfer.getVitalHt()));
		preview.put("vitalMuac", nullToEmpty(transfer.getVitalMuac()));
		preview.put("laboratory", nullToEmpty(transfer.getLaboratory()));
		preview.put("othersNotes", nullToEmpty(transfer.getOtherNotes()));
		preview.put("diagnosis", nullToEmpty(transfer.getDiagnosis()));
		preview.put("proceduresAndTreatments", nullToEmpty(transfer.getProceduresTreatments()));

		String transportType = nullToEmpty(transfer.getTransportType());
		preview.put("transportationType", transportType);
		preview.put("isAmbulanceTransport", "AMBULANCE".equals(transportType));
		preview.put("transportationOtherSpec", nullToEmpty(transfer.getTransportOther()));
		preview.put("isNaTransport", "NA".equals(transportType));

		String healthInsuranceType = nullToEmpty(transfer.getHealthInsuranceType());
		preview.put("healthInsuranceType", healthInsuranceType);
		preview.put("isCbhiInsurance", "CBHI".equals(healthInsuranceType));
		preview.put("isRssbInsurance", "RSSB".equals(healthInsuranceType));
		preview.put("isMmiInsurance", "MMI".equals(healthInsuranceType));
		preview.put("healthInsuranceOtherSpec", nullToEmpty(transfer.getHealthInsuranceOther()));
		preview.put("isNoInsurance", "NONE".equals(healthInsuranceType));

		preview.put("referringProviderName", formatReferringProviderNameForPreview(transfer));
		preview.put("referringProviderQualification", nullToEmpty(transfer.getProviderQualification()));
		preview.put("referringSignedDate", formatDateOnly(transfer.getSignedDate()));
		preview.put("referringSignedTime", nullToEmpty(transfer.getSignedTime()));
		preview.put("referringProviderPhone", nullToEmpty(transfer.getProviderPhone()));
		preview.put("signatureAndStamp", "");
		preview.put("dateCreated", formatDateTime(transfer.getDateCreated()));
		preview.put("hieSent", transfer.isSentToHie());
		preview.put("hieSentAt", formatDateTime(transfer.getHieSentAt()));
		preview.put("hieSendError", nullToEmpty(transfer.getHieSendError()));
		preview.put("receivedFromHie", transfer.isReceivedFromHie());
		preview.put("hieTransferId", nullToEmpty(transfer.getHieTransferId()));

		TransferVerificationUrlService verificationUrlService = getTransferVerificationUrlService();
		boolean showVerificationQr = verificationUrlService.shouldShowVerificationQr(transfer);
		preview.put("showVerificationQr", showVerificationQr);
		if (showVerificationQr) {
			String verificationTransferId = verificationUrlService.resolveVerificationTransferId(transfer);
			preview.put("verificationTransferId", verificationTransferId);
			preview.put("verifyRemoteUrl", verificationUrlService.buildRemoteVerifyUrl(transfer));
			preview.put("verifyQrUrl", verificationUrlService.buildVerifyQrFormUrl(verificationTransferId));
		}
		return preview;
	}

	private String formatReferringProviderNameForPreview(Transfer transfer) {
		String name = nullToEmpty(transfer.getReferringProviderName());
		try {
			TransferProfileService profileService = Context.getService(TransferProfileService.class);
			org.openmrs.User owner = transfer.getCreator() != null
					? transfer.getCreator()
					: Context.getAuthenticatedUser();
			if (profileService != null && owner != null) {
				TransferProfile profile = profileService.getProfileForUser(owner);
				if (profile != null) {
					return TransferProfile.formatCareProviderName(name, profile.getLicenseNumber());
				}
			}
		}
		catch (Exception ignored) {
			// Fall back to the stored name when profile lookup is unavailable.
		}
		return name;
	}

	private String nullToEmpty(String value) {
		return value != null ? value : "";
	}

	private String resolveFacilityLabel(String facilityCode) {
		if (StringUtils.isBlank(facilityCode)) {
			return "";
		}
		if (transferAdminService != null) {
			Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
			String label = transferAdminService.resolveReceivingFacilityName(sendingLocationId, facilityCode);
			if (!facilityCode.equals(label)) {
				return label;
			}
		}
		switch (facilityCode) {
			case "KUTH":
				return "Kigali University Teaching Hospital";
			case "RUHENGERI":
				return "Ruhengeri District Hospital";
			case "BUTARO":
				return "Butaro District Hospital";
			case "KFH":
				return "King Faisal Hospital";
			default:
				return facilityCode;
		}
	}

	private String formatDateTime(Date date) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat(DATETIME_PATTERN).format(date);
	}

	private String formatDateOnly(Date date) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat("dd.MMM.yyyy").format(date);
	}

	private TransferFormExtras buildFormExtras(String clinicalPresentation, String disabilityType, String laboratory,
			String proceduresTreatments, String otherNotes, String diagnosis, String providerQualification,
			String signedDate, String signedTime) {
		if (StringUtils.isBlank(clinicalPresentation) && StringUtils.isBlank(disabilityType)
				&& StringUtils.isBlank(laboratory) && StringUtils.isBlank(proceduresTreatments)
				&& StringUtils.isBlank(otherNotes) && StringUtils.isBlank(diagnosis)
				&& StringUtils.isBlank(providerQualification)
				&& StringUtils.isBlank(signedDate) && StringUtils.isBlank(signedTime)) {
			return null;
		}
		TransferFormExtras extras = new TransferFormExtras();
		extras.setClinicalPresentation(clinicalPresentation);
		extras.setDisabilityType(disabilityType);
		extras.setLaboratory(laboratory);
		extras.setProceduresTreatments(proceduresTreatments);
		extras.setOtherNotes(otherNotes);
		extras.setDiagnosis(diagnosis);
		extras.setProviderQualification(providerQualification);
		extras.setSignedDate(signedDate);
		extras.setSignedTime(signedTime);
		return extras;
	}

	private String formatTimeOnly(Date date) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat("HH:mm:ss").format(date);
	}

	private void writePrivilegeDenied(HttpServletResponse response, Map<String, Object> data, String privilege)
			throws Exception {
		data.put("status", "error");
		data.put("message", TransferPrivilegeHelper.requiredPrivilegeMessage(privilege));
		data.put("requiredPrivilege", privilege);
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		writeJson(response, data);
	}

	private void putError(Map<String, Object> data, Exception exception, String requiredPrivilege, String fallback) {
		data.put("status", "error");
		data.put("message", TransferPrivilegeHelper.resolveUserFacingMessage(exception, requiredPrivilege, fallback));
		if (TransferPrivilegeHelper.isPrivilegeException(exception)) {
			data.put("requiredPrivilege", requiredPrivilege);
		}
	}

	private void writeJson(HttpServletResponse response, Map<String, Object> data) throws Exception {
		response.setContentType("application/json");
		new ObjectMapper().writeValue(response.getOutputStream(), data);
	}

}
