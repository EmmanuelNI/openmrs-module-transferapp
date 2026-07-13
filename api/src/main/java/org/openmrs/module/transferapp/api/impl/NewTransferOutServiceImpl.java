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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.NewTransferOutService;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferPatientSnapshotResolver;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.model.NewTransferOutFormData;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.TransferFormOption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Builds MOH External Transfer Form wizard data with OpenMRS patient prefill.
 */
public class NewTransferOutServiceImpl implements NewTransferOutService {

	private static final String DATETIME_LOCAL_PATTERN = "yyyy-MM-dd'T'HH:mm";

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	private static final String TIME_PATTERN = "HH:mm";

	private final TransferPatientSnapshotResolver patientSnapshotResolver = new TransferPatientSnapshotResolver();

	private TransferDao transferDao;

	private TransferAdminService transferAdminService;

	public void setTransferDao(TransferDao transferDao) {
		this.transferDao = transferDao;
	}

	public void setTransferAdminService(TransferAdminService transferAdminService) {
		this.transferAdminService = transferAdminService;
	}

	@Override
	public NewTransferOutFormData getNewTransferOutFormData(Patient patient) {
		NewTransferOutFormData formData = new NewTransferOutFormData();
		formData.setPatientId(patient.getPatientId());
		formData.setPatientDisplay(patient.getPersonName() != null ? patient.getPersonName().getFullName() : "");
		formData.setSendingFacility(getCurrentFacilityName());

		formData.setIdentifierTypes(getIdentifierTypes());
		formData.setReceivingFacilities(getReceivingFacilities());
		formData.setReceivingServices(Collections.<String>emptyList());
		formData.setTransferTypes(getTransferTypes());
		formData.setTransportationTypes(getTransportationTypes());
		formData.setHealthInsuranceTypes(getHealthInsuranceTypes());

		prefillFromPatient(formData, patient);
		prefillFromCurrentUser(formData);
		prefillDefaults(formData);

		return formData;
	}

	protected void prefillFromPatient(NewTransferOutFormData formData, Patient patient) {
		if (patient.getPersonName() != null) {
			formData.setClientName(patient.getPersonName().getFullName());
		}

		String upid = patientSnapshotResolver.resolveUpid(patient);
		if (upid != null) {
			formData.setSerialNumberEmr(upid);
		} else {
			PatientIdentifier openMrsId = patient.getPatientIdentifier();
			if (openMrsId != null) {
				formData.setSerialNumberEmr(openMrsId.getIdentifier());
			}
		}

		PatientIdentifier nationalId = patientSnapshotResolver.resolveNationalIdentifier(patient);
		if (nationalId != null && nationalId.getIdentifierType() != null) {
			formData.setIdentifierType(nationalId.getIdentifierType().getName());
			formData.setIdentifierValue(nationalId.getIdentifier());
		}

		formData.setClientTelephone(patientSnapshotResolver.resolvePatientPhone(patient, transferDao));
		formData.setAgeOrDob(patientSnapshotResolver.resolveAgeOrDob(patient));
		formData.setSex(patientSnapshotResolver.mapGender(patient.getGender()));
		formData.setCaregiverName(patientSnapshotResolver.resolvePersonAttribute(patient,
				"Caregiver Name", "CaregiverName", "Name of caregiver"));
		formData.setCaregiverTelephone(patientSnapshotResolver.resolvePersonAttribute(patient,
				"Caregiver Telephone", "Caregiver Phone", "CaregiverPhone"));

		PersonAddress address = null;
		if (transferDao != null) {
			address = transferDao.getPreferredPersonAddress(patient.getPatientId());
		}
		if (address == null) {
			address = patientSnapshotResolver.resolveActivePersonAddress(patient);
		}
		if (address != null) {
			formData.setClientDistrict(patientSnapshotResolver.resolveDistrict(address));
			formData.setSector(patientSnapshotResolver.resolveSector(address));
			formData.setCell(patientSnapshotResolver.resolveCell(address));
			formData.setVillage(patientSnapshotResolver.resolveVillage(address));
		}
	}

	protected void prefillFromCurrentUser(NewTransferOutFormData formData) {
		User user = Context.getAuthenticatedUser();
		if (user != null && user.getPerson() != null && user.getPerson().getPersonName() != null) {
			formData.setReferringProviderName(user.getPerson().getPersonName().getFullName());
		}
	}

	protected void prefillDefaults(NewTransferOutFormData formData) {
		Date now = new Date();
		formData.setAdmissionAt(formatDateTimeLocal(now));
		formData.setDecisionToTransferAt(formatDateTimeLocal(now));
		formData.setCallingTime(formatTime(now));
		formData.setReferringSignedDate(formatDate(now));
		formData.setReferringSignedTime(formatTime(now));
	}

	protected String getCurrentFacilityName() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation != null && sessionLocation.getParentLocation() != null) {
			return sessionLocation.getParentLocation().getName();
		} else if(sessionLocation.getParentLocation() == null){
			return sessionLocation.getName();
		}
		return "";
	}

	protected List<TransferFormOption> getIdentifierTypes() {
		return Arrays.asList(
				new TransferFormOption("NID", "National ID (NID)"),
				new TransferFormOption("NIDA_APPLICATION_NUMBER", "NIDA application number"),
				new TransferFormOption("NIN", "NIN"),
				new TransferFormOption("UPID", "UPID"));
	}

	protected List<TransferFormOption> getReceivingFacilities() {
		if (transferAdminService != null) {
			Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
			if (sendingLocationId != null) {
				List<ReceivingFacility> facilities = transferAdminService.getReceivingFacilities(sendingLocationId);
				if (!facilities.isEmpty()) {
					List<TransferFormOption> options = new ArrayList<TransferFormOption>();
					for (ReceivingFacility facility : facilities) {
						options.add(new TransferFormOption(
								facility.getFacilityCode(),
								facility.getFacilityName(),
								facility.getReceivingFacilityId()));
					}
					return options;
				}
			}
		}
		return getDefaultReceivingFacilities();
	}

	protected List<TransferFormOption> getDefaultReceivingFacilities() {
		return Arrays.asList(
				new TransferFormOption("KUTH", "Kigali University Teaching Hospital"),
				new TransferFormOption("RUHENGERI", "Ruhengeri District Hospital"),
				new TransferFormOption("BUTARO", "Butaro District Hospital"),
				new TransferFormOption("KFH", "King Faisal Hospital"));
	}

	protected List<TransferFormOption> getTransferTypes() {
		return Arrays.asList(
				new TransferFormOption("EMERGENCY", "Emergency"),
				new TransferFormOption("NOT_EMERGENCY", "Not-Emergency"),
				new TransferFormOption("FOLLOW_UP", "Follow up"));
	}

	protected List<TransferFormOption> getTransportationTypes() {
		return Arrays.asList(
				new TransferFormOption("AMBULANCE", "Ambulance"),
				new TransferFormOption("OTHER", "Other (specify)"),
				new TransferFormOption("NA", "NA"));
	}

	protected List<TransferFormOption> getHealthInsuranceTypes() {
		return Arrays.asList(
				new TransferFormOption("CBHI", "CBHI (mutuelle)"),
				new TransferFormOption("RSSB", "RSSB"),
				new TransferFormOption("MMI", "MMI"),
				new TransferFormOption("OTHER", "Other (specify)"),
				new TransferFormOption("NONE", "None"));
	}

	protected String mapGender(String gender) {
		if (gender == null) {
			return "";
		}
		if ("M".equalsIgnoreCase(gender)) {
			return "MALE";
		}
		if ("F".equalsIgnoreCase(gender)) {
			return "FEMALE";
		}
		return "OTHER";
	}

	protected String formatDateTimeLocal(Date date) {
		return new SimpleDateFormat(DATETIME_LOCAL_PATTERN).format(date);
	}

	protected String formatDate(Date date) {
		return new SimpleDateFormat(DATE_PATTERN).format(date);
	}

	protected String formatTime(Date date) {
		return new SimpleDateFormat(TIME_PATTERN).format(date);
	}

}
