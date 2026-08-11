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
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.NewNeonatalTransferOutService;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferPatientSnapshotResolver;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.model.NeonatalTransferFormData;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.TransferFormOption;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Builds MOH Neonatal Transfer Form wizard data with OpenMRS patient prefill.
 *
 * <p>Wired to {@link TransferDao} (the External transfer DAO), not {@code NeonatalTransferDao} —
 * this mirrors {@code NewMaternityTransferOutServiceImpl}'s wiring quirk: the "New*OutService"
 * only needs generic prefill helpers such as {@code getPreferredPersonAddress}, so it shares the
 * same DAO as the External form rather than the type-specific one used for real persistence.</p>
 */
public class NewNeonatalTransferOutServiceImpl implements NewNeonatalTransferOutService {

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
	public NeonatalTransferFormData getNeonatalTransferFormData(Patient patient) {
		NeonatalTransferFormData formData = new NeonatalTransferFormData();
		formData.setPatientId(patient.getPatientId());
		formData.setPatientDisplay(patient.getPersonName() != null ? patient.getPersonName().getFullName() : "");
		formData.setSendingFacility(getCurrentFacilityName());

		formData.setReceivingFacilities(getReceivingFacilities());
		formData.setReceivingServices(Collections.<String>emptyList());
		formData.setTransferTypes(getTransferTypes());
		formData.setTransportTypes(getTransportTypes());

		prefillFromPatient(formData, patient);
		prefillFromCurrentUser(formData);
		prefillDefaults(formData);

		return formData;
	}

	protected void prefillFromPatient(NeonatalTransferFormData formData, Patient patient) {
		if (patient.getPersonName() != null) {
			formData.setBabyName(patient.getPersonName().getFullName());
		}
		if (patient.getGender() != null) {
			formData.setSex(patient.getGender());
		}
		if (patient.getBirthdate() != null) {
			formData.setDob(formatDate(patient.getBirthdate()));
		}

		formData.setMotherName(patientSnapshotResolver.resolvePersonAttribute(patient,
				"Caregiver Name", "CaregiverName", "Name of caregiver", "Next of Kin", "NextOfKin", "Mother's Name"));
		formData.setMotherCaregiverPhone(patientSnapshotResolver.resolvePersonAttribute(patient,
				"Caregiver Telephone", "Caregiver Phone", "CaregiverPhone", "Next of Kin Telephone"));

		PersonAddress address = null;
		if (transferDao != null) {
			address = transferDao.getPreferredPersonAddress(patient.getPatientId());
		}
		if (address == null) {
			address = patientSnapshotResolver.resolveActivePersonAddress(patient);
		}
		if (address != null) {
			formData.setPlaceOfBirth(patientSnapshotResolver.resolveDistrict(address));
		}
	}

	protected void prefillFromCurrentUser(NeonatalTransferFormData formData) {
		User user = Context.getAuthenticatedUser();
		if (user != null && user.getPerson() != null && user.getPerson().getPersonName() != null) {
			formData.setReferringProviderName(user.getPerson().getPersonName().getFullName());
		}
	}

	protected void prefillDefaults(NeonatalTransferFormData formData) {
		Date now = new Date();
		formData.setDecisionToTransferAt(formatDateTimeLocal(now));
		formData.setCallingTime(formatTime(now));
		formData.setReferringSignedDate(formatDate(now));
		formData.setReferringSignedTime(formatTime(now));
	}

	protected String getCurrentFacilityName() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation != null && sessionLocation.getParentLocation() != null) {
			return sessionLocation.getParentLocation().getName();
		} else if (sessionLocation != null && sessionLocation.getParentLocation() == null) {
			return sessionLocation.getName();
		}
		return "";
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

	protected List<TransferFormOption> getTransportTypes() {
		return Arrays.asList(
				new TransferFormOption("AMBULANCE", "Ambulance"),
				new TransferFormOption("OTHER", "Other (specify)"),
				new TransferFormOption("NA", "NA"));
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
