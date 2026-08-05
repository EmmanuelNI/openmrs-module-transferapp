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
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.PatientInsuranceService;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferPatientSnapshotResolver;
import org.openmrs.module.transferapp.api.TransferProfileService;
import org.openmrs.module.transferapp.api.TransferService;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.model.PatientInsuranceInfo;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.Transfer;
import org.openmrs.module.transferapp.model.TransferFormExtras;
import org.openmrs.module.transferapp.model.TransferProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransferServiceImpl implements TransferService {

	private static final String DATETIME_LOCAL_PATTERN = "yyyy-MM-dd'T'HH:mm";
	private static final String DATETIME_SPACE_PATTERN = "yyyy-MM-dd HH:mm";
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	private static final String TIME_PATTERN = "HH:mm";
	private static final String TRANSFER_TYPE_EMERGENCY = "EMERGENCY";
	private static final String TRANSPORT_TYPE_AMBULANCE = "AMBULANCE";

	private TransferDao transferDao;

	private PatientService patientService;

	private TransferAdminService transferAdminService;

	private PatientInsuranceService patientInsuranceService;

	private TransferProfileService transferProfileService;

	private TransferPatientSnapshotResolver patientSnapshotResolver = new TransferPatientSnapshotResolver();

	public void setTransferDao(TransferDao transferDao) {
		this.transferDao = transferDao;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public void setTransferAdminService(TransferAdminService transferAdminService) {
		this.transferAdminService = transferAdminService;
	}

	public void setPatientInsuranceService(PatientInsuranceService patientInsuranceService) {
		this.patientInsuranceService = patientInsuranceService;
	}

	public void setTransferProfileService(TransferProfileService transferProfileService) {
		this.transferProfileService = transferProfileService;
	}

	@Override
	public Transfer saveReferralTransfer(Integer patientId,
			String decisionToTransferAt,
			String callingTime,
			String receivingFacilityCode,
			Integer receivingFacilityId,
			String receivingService,
			String staffContactedName,
			String staffContactedPhone,
			String transferType,
			String ambulanceCalledTime,
			String departureFromReferringTime,
			String transportationType,
			String transportationOtherSpec,
			String reasonForTransfer) {
		return saveReferralTransfer(patientId, decisionToTransferAt, callingTime, receivingFacilityCode,
				receivingFacilityId, receivingService, staffContactedName, staffContactedPhone, transferType,
				ambulanceCalledTime, departureFromReferringTime, transportationType, transportationOtherSpec,
				reasonForTransfer, null);
	}

	@Override
	public Transfer saveReferralTransfer(Integer patientId,
			String decisionToTransferAt,
			String callingTime,
			String receivingFacilityCode,
			Integer receivingFacilityId,
			String receivingService,
			String staffContactedName,
			String staffContactedPhone,
			String transferType,
			String ambulanceCalledTime,
			String departureFromReferringTime,
			String transportationType,
			String transportationOtherSpec,
			String reasonForTransfer,
			TransferFormExtras formExtras) {

		if (patientId == null) {
			throw new APIException("Patient is required");
		}

		Patient patient = patientService.getPatient(patientId);
		if (patient == null) {
			throw new APIException("Patient not found");
		}

		String normalizedTransferType = StringUtils.trimToNull(transferType);
		validateTransferTypeFields(normalizedTransferType, ambulanceCalledTime, departureFromReferringTime);
		validateTransportationFields(normalizedTransferType, transportationType, transportationOtherSpec);
		ensureReceivingServiceConfigured(receivingFacilityCode, receivingFacilityId, receivingService);

		Transfer transfer = new Transfer();
		transfer.setUuid(UUID.randomUUID().toString());
		transfer.setPatient(patient);
		transfer.setDecisionToTransferAt(parseDateTimeLocal(decisionToTransferAt));
		transfer.setCallingTime(StringUtils.trimToNull(callingTime));
		transfer.setReceivingFacilityCode(StringUtils.trimToNull(receivingFacilityCode));
		applyReceivingFacilitySnapshot(transfer, receivingFacilityCode, receivingFacilityId);
		transfer.setReceivingService(StringUtils.trimToNull(receivingService));
		transfer.setStaffContactedName(StringUtils.trimToNull(staffContactedName));
		transfer.setStaffContactedPhone(StringUtils.trimToNull(staffContactedPhone));
		transfer.setTransferType(normalizedTransferType);
		if (TRANSFER_TYPE_EMERGENCY.equals(transfer.getTransferType())) {
			transfer.setAmbulanceCallTime(StringUtils.trimToNull(ambulanceCalledTime));
			transfer.setDepartRefTime(StringUtils.trimToNull(departureFromReferringTime));
		}
		applyTransportationSnapshot(transfer, normalizedTransferType, transportationType, transportationOtherSpec);
		transfer.setReasonForTransfer(StringUtils.trimToNull(reasonForTransfer));
		applyHealthInsuranceSnapshot(transfer, patient);
		patientSnapshotResolver.applyPatientSnapshot(transfer, patient, transferDao);

		PersonAddress personAddress = transferDao.getPreferredPersonAddress(patient.getPatientId());
		if (personAddress == null) {
			personAddress = patientSnapshotResolver.resolveActivePersonAddress(patient);
		}
		patientSnapshotResolver.applyPersonAddressSnapshot(transfer, personAddress);

		transfer.setCreator(Context.getAuthenticatedUser());
		Date now = new Date();
		transfer.setDateCreated(now);
		transfer.setVoided(false);
		transfer.setHieSent(false);
		transfer.setReceivedFromHie(false);
		applyFormExtras(transfer, formExtras);

		return transferDao.saveTransfer(transfer);
	}

	private void applyFormExtras(Transfer transfer, TransferFormExtras formExtras) {
		if (formExtras != null) {
			// Keep obs-resolved clinical presentation unless the form explicitly provides one.
			if (StringUtils.isNotBlank(formExtras.getClinicalPresentation())) {
				transfer.setClinicalPresentation(StringUtils.trimToNull(formExtras.getClinicalPresentation()));
			}
			transfer.setDisabilityType(StringUtils.trimToNull(formExtras.getDisabilityType()));
			transfer.setLaboratory(StringUtils.trimToNull(formExtras.getLaboratory()));
			transfer.setProceduresTreatments(StringUtils.trimToNull(formExtras.getProceduresTreatments()));
			transfer.setOtherNotes(StringUtils.trimToNull(formExtras.getOtherNotes()));
			transfer.setProviderQualification(StringUtils.trimToNull(formExtras.getProviderQualification()));
			transfer.setSignedDate(parseDateValue(formExtras.getSignedDate()));
			transfer.setSignedTime(StringUtils.trimToNull(formExtras.getSignedTime()));
		}

		if (StringUtils.isBlank(transfer.getProviderQualification()) && transferProfileService != null) {
			User user = Context.getAuthenticatedUser();
			if (user != null) {
				TransferProfile profile = transferProfileService.getProfileForUser(user);
				if (profile != null) {
					transfer.setProviderQualification(StringUtils.trimToNull(profile.getQualification()));
				}
			}
		}

		if (transfer.getSignedDate() == null) {
			transfer.setSignedDate(transfer.getDateCreated());
		}
		if (StringUtils.isBlank(transfer.getSignedTime()) && transfer.getDateCreated() != null) {
			transfer.setSignedTime(new SimpleDateFormat(TIME_PATTERN).format(transfer.getDateCreated()));
		}
	}

	protected Date parseDateValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		String trimmed = value.trim();
		for (String pattern : new String[] { DATE_PATTERN, "dd.MMM.yyyy", DATETIME_LOCAL_PATTERN, DATETIME_SPACE_PATTERN }) {
			try {
				return new SimpleDateFormat(pattern).parse(trimmed);
			}
			catch (ParseException ignored) {
				// try next pattern
			}
		}
		return null;
	}

	private void applyHealthInsuranceSnapshot(Transfer transfer, Patient patient) {
		if (patientInsuranceService == null) {
			return;
		}

		PatientInsuranceInfo insurance = patientInsuranceService.getPatientInsurance(patient);
		if (!insurance.isAvailable()) {
			throw new APIException("Patient insurance type and number are required before creating a transfer");
		}

		String category = StringUtils.trimToNull(insurance.getHealthInsuranceCategory());
		if (category == null) {
			category = TransferAppConstants.HEALTH_INSURANCE_OTHER;
		}
		transfer.setHealthInsuranceType(category);
		if (TransferAppConstants.HEALTH_INSURANCE_OTHER.equals(category)) {
			String otherSpec = StringUtils.trimToNull(insurance.getHealthInsuranceOtherSpec());
			if (otherSpec == null) {
				otherSpec = insurance.getInsuranceType();
			}
			transfer.setHealthInsuranceOther(otherSpec);
		}
		else {
			transfer.setHealthInsuranceOther(null);
		}
	}

	private void ensureReceivingServiceConfigured(String receivingFacilityCode, Integer receivingFacilityId,
			String receivingService) {
		if (transferAdminService == null || StringUtils.isBlank(receivingService)) {
			return;
		}
		Integer resolvedFacilityId = receivingFacilityId;
		if (resolvedFacilityId == null && StringUtils.isNotBlank(receivingFacilityCode)) {
			Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
			if (sendingLocationId != null) {
				ReceivingFacility facility = transferAdminService
						.getReceivingFacilityByCode(sendingLocationId, receivingFacilityCode);
				if (facility != null) {
					resolvedFacilityId = facility.getReceivingFacilityId();
				}
			}
		}
		if (resolvedFacilityId != null) {
			transferAdminService.ensureReceivingServiceForFacility(resolvedFacilityId, receivingService);
		}
	}

	private void applyReceivingFacilitySnapshot(Transfer transfer, String receivingFacilityCode,
			Integer receivingFacilityId) {
		if (transferAdminService == null) {
			return;
		}

		ReceivingFacility facility = null;
		if (receivingFacilityId != null) {
			facility = transferAdminService.getReceivingFacility(receivingFacilityId);
		}
		if (facility == null && StringUtils.isNotBlank(receivingFacilityCode)) {
			Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
			if (sendingLocationId != null) {
				facility = transferAdminService.getReceivingFacilityByCode(sendingLocationId, receivingFacilityCode);
			}
		}
		if (facility != null) {
			transfer.setReceivingProvince(StringUtils.trimToNull(facility.getProvince()));
			transfer.setReceivingDistrict(StringUtils.trimToNull(facility.getDistrict()));
		}
	}

	private void validateTransferTypeFields(String transferType, String ambulanceCalledTime,
			String departureFromReferringTime) {
		String type = StringUtils.trimToNull(transferType);
		if (type == null) {
			throw new APIException("Type of transfer is required");
		}
		if (!Arrays.asList("EMERGENCY", "NOT_EMERGENCY", "FOLLOW_UP").contains(type)) {
			throw new APIException("Invalid type of transfer");
		}
		if (TRANSFER_TYPE_EMERGENCY.equals(type)) {
			if (StringUtils.isBlank(ambulanceCalledTime)) {
				throw new APIException("Time ambulance called is required for emergency transfers");
			}
			if (StringUtils.isBlank(departureFromReferringTime)) {
				throw new APIException("Time of departure from referring facility is required for emergency transfers");
			}
		}
	}

	private void validateTransportationFields(String transferType, String transportationType,
			String transportationOtherSpec) {
		if (TRANSFER_TYPE_EMERGENCY.equals(transferType)) {
			return;
		}
		String transport = StringUtils.trimToNull(transportationType);
		if (transport == null) {
			throw new APIException("Type of transportation is required");
		}
		if (TRANSPORT_TYPE_AMBULANCE.equals(transport)) {
			throw new APIException("Ambulance transportation is only allowed for emergency transfers");
		}
		if (!Arrays.asList("OTHER", "NA").contains(transport)) {
			throw new APIException("Invalid type of transportation");
		}
		if ("OTHER".equals(transport) && StringUtils.isBlank(transportationOtherSpec)) {
			throw new APIException("Please specify other transportation type");
		}
	}

	private void applyTransportationSnapshot(Transfer transfer, String transferType, String transportationType,
			String transportationOtherSpec) {
		if (TRANSFER_TYPE_EMERGENCY.equals(transferType)) {
			transfer.setTransportType(TRANSPORT_TYPE_AMBULANCE);
			transfer.setTransportOther(null);
			return;
		}
		String transport = StringUtils.trimToNull(transportationType);
		transfer.setTransportType(transport);
		if ("OTHER".equals(transport)) {
			transfer.setTransportOther(StringUtils.trimToNull(transportationOtherSpec));
		}
		else {
			transfer.setTransportOther(null);
		}
	}

	@Override
	public List<Transfer> getTransfersByPatient(Patient patient) {
		return transferDao.getTransfersByPatient(patient);
	}

	@Override
	public List<Transfer> getTransfersByPatient(Patient patient, Integer limit) {
		return transferDao.getTransfersByPatient(patient, limit);
	}

	@Override
	public int countTransfersByPatient(Patient patient) {
		return transferDao.countTransfersByPatient(patient);
	}

	@Override
	public Transfer getTransferByUuid(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return null;
		}
		return transferDao.getTransferByUuid(uuid.trim());
	}

	protected Date parseDateTimeLocal(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		String trimmed = value.trim();
		for (String pattern : new String[] { DATETIME_LOCAL_PATTERN, DATETIME_SPACE_PATTERN }) {
			try {
				return new SimpleDateFormat(pattern).parse(trimmed);
			}
			catch (ParseException ignored) {
				// try next pattern
			}
		}
		throw new APIException("Invalid decision date and time: " + value);
	}

}
