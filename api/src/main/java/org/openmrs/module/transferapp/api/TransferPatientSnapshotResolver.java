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
package org.openmrs.module.transferapp.api;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.model.Transfer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Resolves patient and session context fields stored on a transfer for preview.
 */
public class TransferPatientSnapshotResolver {

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	private static final String[] PHONE_ATTRIBUTE_NAMES = new String[] {
			"Telephone Number",
			"Telephone number",
			"PhoneNumber",
			"Phone Number",
			"Telephone",
			"Phone",
			"Mobile Number",
			"Mobile"
	};

	private static final String[] PHONE_ATTRIBUTE_KEYWORDS = new String[] {
			"telephone",
			"phone",
			"mobile"
	};

	public void applyPatientSnapshot(Transfer transfer, Patient patient) {
		applyPatientSnapshot(transfer, patient, null);
	}

	public void applyPatientSnapshot(Transfer transfer, Patient patient, TransferDao transferDao) {
		if (transfer == null || patient == null) {
			return;
		}

		if (patient.getPersonName() != null) {
			transfer.setClientName(patient.getPersonName().getFullName());
		}

		transfer.setEmrId(resolveUpid(patient));
		transfer.setClientTelephone(resolvePatientPhone(patient, transferDao));
		transfer.setAgeOrDob(resolveAgeOrDob(patient));
		transfer.setSex(mapGender(patient.getGender()));

		PatientIdentifier nationalId = resolveNationalIdentifier(patient);
		if (nationalId != null && nationalId.getIdentifierType() != null) {
			transfer.setIdentifierType(nationalId.getIdentifierType().getName());
			transfer.setIdentifierValue(nationalId.getIdentifier());
		}

		transfer.setCaregiverName(resolvePersonAttribute(patient, "Caregiver Name", "CaregiverName", "Name of caregiver"));
		transfer.setCaregiverTelephone(resolvePersonAttribute(patient, "Caregiver Telephone", "Caregiver Phone", "CaregiverPhone"));

		transfer.setAdmissionAt(resolveAdmissionDatetime(patient));
		transfer.setDiagnosis(resolveDiagnosis(patient));
		applyVitalSignsSnapshot(transfer, patient);
		transfer.setSendingFacility(resolveCurrentFacilityName());
		transfer.setReferringUnit(resolveReferringUnit());
		transfer.setReferringProviderName(resolveReferringProviderName());
	}

	public PersonAddress resolveActivePersonAddress(Patient patient) {
		if (patient == null) {
			return null;
		}

		// Touch the collection so OpenMRS loads rows from person_address for this patient.
		Set<PersonAddress> addresses = patient.getAddresses();
		if (addresses != null) {
			for (PersonAddress address : addresses) {
				if (isActiveAddress(address) && Boolean.TRUE.equals(address.getPreferred())) {
					return address;
				}
			}
			for (PersonAddress address : addresses) {
				if (isActiveAddress(address)) {
					return address;
				}
			}
		}

		PersonAddress preferred = patient.getPersonAddress();
		return isActiveAddress(preferred) ? preferred : null;
	}

	public void applyPersonAddressSnapshot(Transfer transfer, PersonAddress address) {
		if (transfer == null || address == null) {
			return;
		}
		transfer.setClientDistrict(resolveDistrict(address));
		transfer.setSector(resolveSector(address));
		transfer.setCell(resolveCell(address));
		transfer.setVillage(resolveVillage(address));
	}

	public String resolveDistrict(PersonAddress address) {
		if (address == null) {
			return null;
		}
		return StringUtils.trimToNull(address.getCountyDistrict());
	}

	public String resolveSector(PersonAddress address) {
		if (address == null) {
			return null;
		}
		return StringUtils.trimToNull(address.getCityVillage());
	}

	public String resolveCell(PersonAddress address) {
		if (address == null) {
			return null;
		}
		return StringUtils.trimToNull(address.getAddress3());
	}

	public String resolveVillage(PersonAddress address) {
		if (address == null) {
			return null;
		}
		return StringUtils.trimToNull(address.getAddress1());
	}

	private boolean isActiveAddress(PersonAddress address) {
		return address != null && !Boolean.TRUE.equals(address.getVoided());
	}

	public String resolveUpid(Patient patient) {
		for (PatientIdentifier identifier : patient.getActiveIdentifiers()) {
			if (identifier == null || identifier.getIdentifierType() == null) {
				continue;
			}
			String typeName = identifier.getIdentifierType().getName();
			if (typeName != null && "UPID".equalsIgnoreCase(typeName.trim())) {
				return identifier.getIdentifier();
			}
		}

		for (PatientIdentifier identifier : patient.getActiveIdentifiers()) {
			if (identifier == null || identifier.getIdentifierType() == null) {
				continue;
			}
			String typeName = identifier.getIdentifierType().getName();
			if (typeName != null && typeName.toUpperCase().contains("UPID")) {
				return identifier.getIdentifier();
			}
		}

		return null;
	}

	public PatientIdentifier resolveNationalIdentifier(Patient patient) {
		for (PatientIdentifier identifier : patient.getActiveIdentifiers()) {
			if (identifier == null || identifier.getIdentifierType() == null) {
				continue;
			}
			String typeName = identifier.getIdentifierType().getName();
			if (typeName != null) {
				String upper = typeName.toUpperCase();
				if (upper.contains("NID") || upper.contains("NATIONAL")) {
					return identifier;
				}
			}
		}
		return null;
	}

	public String resolvePatientPhone(Patient patient) {
		return resolvePatientPhone(patient, null);
	}

	public String resolvePatientPhone(Patient patient, TransferDao transferDao) {
		String phone = resolvePersonAttributeValue(
				patient != null ? patient.getAttributes() : null,
				PHONE_ATTRIBUTE_NAMES,
				PHONE_ATTRIBUTE_KEYWORDS);

		if (phone != null || transferDao == null || patient == null) {
			return StringUtils.trimToNull(phone);
		}

		return StringUtils.trimToNull(resolvePersonAttributeValue(
				transferDao.getPersonAttributes(patient.getPatientId()),
				PHONE_ATTRIBUTE_NAMES,
				PHONE_ATTRIBUTE_KEYWORDS));
	}

	public String resolvePersonAttribute(Patient patient, String... attributeNames) {
		if (patient == null || attributeNames == null) {
			return null;
		}

		for (String attributeName : attributeNames) {
			PersonAttribute attribute = patient.getAttribute(attributeName);
			if (isActivePersonAttribute(attribute) && StringUtils.isNotBlank(attribute.getValue())) {
				return attribute.getValue().trim();
			}
		}

		return resolvePersonAttributeValue(patient.getAttributes(), attributeNames, null);
	}

	private String resolvePersonAttributeValue(Collection<PersonAttribute> attributes,
			String[] exactNames,
			String[] keywords) {
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		if (exactNames != null) {
			for (String attributeName : exactNames) {
				for (PersonAttribute attribute : attributes) {
					if (!isActivePersonAttribute(attribute) || attribute.getAttributeType() == null) {
						continue;
					}
					if (attributeName.equalsIgnoreCase(attribute.getAttributeType().getName())
							&& StringUtils.isNotBlank(attribute.getValue())) {
						return attribute.getValue().trim();
					}
				}
			}
		}

		if (keywords != null) {
			for (PersonAttribute attribute : attributes) {
				if (!isActivePersonAttribute(attribute) || attribute.getAttributeType() == null) {
					continue;
				}
				String typeName = attribute.getAttributeType().getName();
				if (StringUtils.isBlank(typeName) || StringUtils.isBlank(attribute.getValue())) {
					continue;
				}
				String lowerTypeName = typeName.toLowerCase();
				for (String keyword : keywords) {
					if (lowerTypeName.contains(keyword) && StringUtils.isNotBlank(attribute.getValue())) {
						return attribute.getValue().trim();
					}
				}
			}
		}

		return null;
	}

	private boolean isActivePersonAttribute(PersonAttribute attribute) {
		return attribute != null && !Boolean.TRUE.equals(attribute.getVoided());
	}

	public String resolveAgeOrDob(Patient patient) {
		Integer age = patient.getAge();
		if (patient.getBirthdate() != null) {
			if (age != null) {
				return age + " years";
			}
			return formatDate(patient.getBirthdate());
		}
		if (age != null) {
			return age + " years";
		}
		return null;
	}

	protected String resolveCurrentFacilityName() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation != null && sessionLocation.getParentLocation() != null) {
			return sessionLocation.getParentLocation().getName();
		} else if (sessionLocation != null) {
			return sessionLocation.getName();
		}
		return null;
	}

	protected String resolveReferringUnit() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation == null) {
			return null;
		}
		return sessionLocation.getName();
	}

	public Date resolveAdmissionDatetime(Patient patient) {
		if (patient == null) {
			return null;
		}

		List<Visit> visits = Context.getVisitService().getVisitsByPatient(patient);
		if (visits == null || visits.isEmpty()) {
			return null;
		}

		Date latestStart = null;
		for (Visit visit : visits) {
			if (visit == null || Boolean.TRUE.equals(visit.getVoided())) {
				continue;
			}
			Date startDatetime = visit.getStartDatetime();
			if (startDatetime == null) {
				continue;
			}
			if (latestStart == null || startDatetime.after(latestStart)) {
				latestStart = startDatetime;
			}
		}
		return latestStart;
	}

	public String resolveDiagnosis(Patient patient) {
		Visit activeVisit = resolveActiveVisit(patient);
		if (activeVisit == null) {
			return null;
		}

		String conceptUuids = Context.getAdministrationService().getGlobalProperty(
				TransferAppConstants.GP_DIAGNOSIS_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_DIAGNOSIS_CONCEPT_UUID);
		if (StringUtils.isBlank(conceptUuids)) {
			return null;
		}

		List<String> diagnosisNames = new ArrayList<String>();
		for (String token : conceptUuids.split(",")) {
			String conceptUuid = StringUtils.trimToNull(token);
			if (conceptUuid == null) {
				continue;
			}
			Concept questionConcept = Context.getConceptService().getConceptByUuid(conceptUuid);
			if (questionConcept == null) {
				continue;
			}
			Obs latestObs = getLatestObservationForVisit(patient, activeVisit, questionConcept);
			if (latestObs != null && latestObs.getValueCoded() != null) {
				String diagnosisName = StringUtils.trimToNull(latestObs.getValueCoded().getDisplayString());
				if (diagnosisName != null) {
					diagnosisNames.add(diagnosisName);
				}
			}
		}

		if (diagnosisNames.isEmpty()) {
			return null;
		}
		return joinDiagnosisNames(diagnosisNames);
	}

	public void applyVitalSignsSnapshot(Transfer transfer, Patient patient) {
		if (transfer == null || patient == null) {
			return;
		}

		Visit activeVisit = resolveActiveVisit(patient);
		if (activeVisit == null) {
			return;
		}

		transfer.setVitalHt(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_HEIGHT_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_HEIGHT_CONCEPT_UUID));
		transfer.setVitalWt(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_WEIGHT_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_WEIGHT_CONCEPT_UUID));
		transfer.setVitalTemp(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_TEMPERATURE_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_TEMPERATURE_CONCEPT_UUID));
		transfer.setVitalPulse(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_PULSE_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_PULSE_CONCEPT_UUID));
		transfer.setVitalBp(resolveBloodPressureValue(patient, activeVisit));
		transfer.setVitalSpo2(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_OXYGEN_SATURATION_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_OXYGEN_SATURATION_CONCEPT_UUID));
		transfer.setVitalMuac(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_MUAC_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_MUAC_CONCEPT_UUID));
		transfer.setVitalRr(resolveVitalSignValue(patient, activeVisit,
				TransferAppConstants.GP_RESPIRATORY_RATE_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_RESPIRATORY_RATE_CONCEPT_UUID));
	}

	private String resolveVitalSignValue(Patient patient, Visit activeVisit, String globalPropertyName,
			String defaultConceptUuid) {
		Concept questionConcept = getConceptByGlobalProperty(globalPropertyName, defaultConceptUuid);
		if (questionConcept == null) {
			return null;
		}
		return formatObsValue(getLatestObservationForVisit(patient, activeVisit, questionConcept));
	}

	private String resolveBloodPressureValue(Patient patient, Visit activeVisit) {
		String conceptUuids = Context.getAdministrationService().getGlobalProperty(
				TransferAppConstants.GP_BLOOD_PRESSURE_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_BLOOD_PRESSURE_CONCEPT_UUID);
		if (StringUtils.isBlank(conceptUuids)) {
			return null;
		}

		String[] parts = conceptUuids.split("/");
		if (parts.length != 2) {
			return null;
		}

		Concept systolicConcept = getConceptByUuid(StringUtils.trimToNull(parts[0]));
		Concept diastolicConcept = getConceptByUuid(StringUtils.trimToNull(parts[1]));
		if (systolicConcept == null || diastolicConcept == null) {
			return null;
		}

		String systolic = formatObsValue(getLatestObservationForVisit(patient, activeVisit, systolicConcept));
		String diastolic = formatObsValue(getLatestObservationForVisit(patient, activeVisit, diastolicConcept));
		if (systolic == null && diastolic == null) {
			return null;
		}
		if (systolic == null) {
			return diastolic;
		}
		if (diastolic == null) {
			return systolic;
		}
		return systolic + "/" + diastolic;
	}

	private Concept getConceptByGlobalProperty(String globalPropertyName, String defaultConceptUuid) {
		String conceptUuid = Context.getAdministrationService().getGlobalProperty(globalPropertyName, defaultConceptUuid);
		return getConceptByUuid(StringUtils.trimToNull(conceptUuid));
	}

	private Concept getConceptByUuid(String conceptUuid) {
		if (conceptUuid == null) {
			return null;
		}
		return Context.getConceptService().getConceptByUuid(conceptUuid);
	}

	private String formatObsValue(Obs obs) {
		if (obs == null) {
			return null;
		}
		if (obs.getValueNumeric() != null) {
			Double numericValue = obs.getValueNumeric();
			if (numericValue.doubleValue() == Math.rint(numericValue.doubleValue())) {
				return String.valueOf(numericValue.intValue());
			}
			return String.valueOf(numericValue);
		}
		String textValue = StringUtils.trimToNull(obs.getValueText());
		if (textValue != null) {
			return textValue;
		}
		if (obs.getValueCoded() != null) {
			return StringUtils.trimToNull(obs.getValueCoded().getDisplayString());
		}
		return StringUtils.trimToNull(obs.getValueAsString(Context.getLocale()));
	}

	public Visit resolveActiveVisit(Patient patient) {
		if (patient == null) {
			return null;
		}

		List<Visit> visits = Context.getVisitService().getActiveVisitsByPatient(patient);
		if (visits == null || visits.isEmpty()) {
			return null;
		}

		Visit latestActiveVisit = null;
		Date latestStart = null;
		for (Visit visit : visits) {
			if (visit == null || Boolean.TRUE.equals(visit.getVoided())) {
				continue;
			}
			if (visit.getStopDatetime() != null) {
				continue;
			}
			Date startDatetime = visit.getStartDatetime();
			if (startDatetime == null) {
				continue;
			}
			if (latestStart == null || startDatetime.after(latestStart)) {
				latestStart = startDatetime;
				latestActiveVisit = visit;
			}
		}
		return latestActiveVisit;
	}

	private Obs getLatestObservationForVisit(Patient patient, Visit visit, Concept questionConcept) {
		List<Encounter> encounters = collectActiveEncounters(visit);
		if (encounters.isEmpty()) {
			return null;
		}

		List<Obs> observations = Context.getObsService().getObservations(
				Collections.singletonList(patient),
				encounters,
				Collections.singletonList(questionConcept),
				null,
				null,
				null,
				null,
				1,
				null,
				null,
				null,
				false);
		if (observations == null || observations.isEmpty()) {
			return null;
		}
		return observations.get(0);
	}

	private List<Encounter> collectActiveEncounters(Visit visit) {
		List<Encounter> encounters = new ArrayList<Encounter>();
		if (visit == null || visit.getEncounters() == null) {
			return encounters;
		}
		for (Encounter encounter : visit.getEncounters()) {
			if (encounter != null && !Boolean.TRUE.equals(encounter.getVoided())) {
				encounters.add(encounter);
			}
		}
		return encounters;
	}

	private String joinDiagnosisNames(List<String> diagnosisNames) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < diagnosisNames.size(); i++) {
			if (i > 0) {
				builder.append("; ");
			}
			builder.append(diagnosisNames.get(i));
		}
		return builder.toString();
	}

	protected String resolveReferringProviderName() {
		User user = Context.getAuthenticatedUser();
		if (user != null && user.getPerson() != null && user.getPerson().getPersonName() != null) {
			return user.getPerson().getPersonName().getFullName();
		}
		return null;
	}

	public String mapGender(String gender) {
		if (gender == null) {
			return null;
		}
		if ("M".equalsIgnoreCase(gender)) {
			return "MALE";
		}
		if ("F".equalsIgnoreCase(gender)) {
			return "FEMALE";
		}
		return "OTHER";
	}

	protected String formatDate(Date date) {
		return new SimpleDateFormat(DATE_PATTERN).format(date);
	}

}
