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
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Patient;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.rwandaemr.RwandaEmrConfig;
import org.openmrs.module.rwandaemr.integration.ClientRegistryPatient;
import org.openmrs.module.rwandaemr.integration.ClientRegistryPatientProvider;
import org.openmrs.module.rwandaemr.integration.ClientRegistryPatientTranslator;
import org.openmrs.module.rwandaemr.integration.IntegrationConfig;
import org.openmrs.module.transferapp.api.ClientRegistryRegistrationService;
import org.openmrs.module.transferapp.api.HiePatientRegistrationResult;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses RwandaEMR's configured HIE client-registry integration so credentials and FHIR mappings stay centralized.
 */
public class ClientRegistryRegistrationServiceImpl implements ClientRegistryRegistrationService {

	static final String INVALID_NATIONAL_ID_MESSAGE =
			"National ID is invalid. It must contain exactly 16 digits.";

	private IntegrationConfig integrationConfig;

	private ClientRegistryPatientProvider clientRegistryPatientProvider;

	private ClientRegistryPatientTranslator clientRegistryPatientTranslator;

	private RwandaEmrConfig rwandaEmrConfig;

	private PatientService patientService;

	private LocationService locationService;

	private RegistrationCoreService registrationCoreService;

	@Override
	public boolean isHieEnabled() {
		return integrationConfig != null && integrationConfig.isHieEnabled();
	}

	@Override
	public Map<String, Object> findRegistrationFieldsByUpid(String upid) {
		String normalizedUpid = StringUtils.trimToNull(upid);
		if (normalizedUpid == null) {
			throw new IllegalArgumentException("UPID is required");
		}
		if (!isHieEnabled()) {
			throw new IllegalStateException("The HIE connection is not enabled on this server");
		}

		ClientRegistryPatient registryPatient = clientRegistryPatientProvider.fetchPatientFromClientRegistry(
				normalizedUpid, IntegrationConfig.IDENTIFIER_SYSTEM_UPI);
		if (registryPatient == null) {
			return null;
		}

		Patient patient = clientRegistryPatientTranslator.toPatient(registryPatient);
		validateNationalId(patient);
		Map<String, Object> fields = toRegistrationFields(registryPatient, patient);
		fields.put("upid", normalizedUpid);
		return fields;
	}

	@Override
	public synchronized HiePatientRegistrationResult registerPatientByUpid(String upid, Location identifierLocation) {
		String normalizedUpid = StringUtils.trimToNull(upid);
		if (normalizedUpid == null) {
			throw new IllegalArgumentException("UPID is required");
		}
		if (!isHieEnabled()) {
			throw new IllegalStateException("The HIE connection is not enabled on this server");
		}

		PatientIdentifierType upidType = rwandaEmrConfig.getUPID();
		if (upidType == null) {
			throw new APIException("UPID patient identifier type is not configured");
		}

		Patient existingPatient = findPatientByIdentifier(normalizedUpid, upidType);
		if (existingPatient != null) {
			return new HiePatientRegistrationResult(existingPatient, false);
		}

		ClientRegistryPatient registryPatient = clientRegistryPatientProvider.fetchPatientFromClientRegistry(
				normalizedUpid, IntegrationConfig.IDENTIFIER_SYSTEM_UPI);
		if (registryPatient == null) {
			throw new APIException("No patient was found in the HIE client registry for UPID " + normalizedUpid);
		}

		Patient patient = clientRegistryPatientTranslator.toPatient(registryPatient);
		applyRegistryName(registryPatient, patient);
		validateNationalId(patient);
		Location resolvedLocation = identifierLocation != null ? identifierLocation : locationService.getDefaultLocation();
		ensureIdentifier(patient, normalizedUpid, upidType, resolvedLocation);
		for (PatientIdentifier identifier : patient.getIdentifiers()) {
			if (identifier.getLocation() == null) {
				identifier.setLocation(resolvedLocation);
			}
		}

		Patient registeredPatient = registrationCoreService.registerPatient(
				patient, Collections.emptyList(), resolvedLocation);
		return new HiePatientRegistrationResult(registeredPatient, true);
	}

	private void validateNationalId(Patient patient) {
		PatientIdentifierType nationalIdType = rwandaEmrConfig.getNationalId();
		if (nationalIdType == null || patient == null) {
			return;
		}

		List<PatientIdentifier> emptyNationalIds = new ArrayList<PatientIdentifier>();
		for (PatientIdentifier identifier : patient.getActiveIdentifiers()) {
			if (nationalIdType.equals(identifier.getIdentifierType())) {
				String nationalId = StringUtils.trimToNull(identifier.getIdentifier());
				if (nationalId == null) {
					emptyNationalIds.add(identifier);
				} else if (!nationalId.matches("[0-9]{16}")) {
					throw new APIException(INVALID_NATIONAL_ID_MESSAGE);
				}
			}
		}
		for (PatientIdentifier emptyNationalId : emptyNationalIds) {
			patient.removeIdentifier(emptyNationalId);
		}
	}

	private Patient findPatientByIdentifier(String identifier, PatientIdentifierType identifierType) {
		List<Patient> matches = patientService.getPatients(null, identifier,
				Collections.singletonList(identifierType), true);
		if (matches != null) {
			for (Patient patient : matches) {
				for (PatientIdentifier patientIdentifier : patient.getActiveIdentifiers()) {
					if (identifierType.equals(patientIdentifier.getIdentifierType())
							&& identifier.equalsIgnoreCase(StringUtils.trimToEmpty(patientIdentifier.getIdentifier()))) {
						return patient;
					}
				}
			}
		}
		return null;
	}

	private void ensureIdentifier(Patient patient, String identifier, PatientIdentifierType type, Location location) {
		for (PatientIdentifier patientIdentifier : patient.getIdentifiers()) {
			if (type.equals(patientIdentifier.getIdentifierType())
					&& identifier.equalsIgnoreCase(StringUtils.trimToEmpty(patientIdentifier.getIdentifier()))) {
				return;
			}
		}
		patient.addIdentifier(new PatientIdentifier(identifier, type, location));
	}

	private void applyRegistryName(ClientRegistryPatient registryPatient, Patient patient) {
		HumanName registryName = registryPatient.getName();
		PersonName patientName = patient.getPersonName();
		if (registryName == null || patientName == null) {
			return;
		}

		List<StringType> givenNames = registryName.getGiven();
		if (givenNames != null && !givenNames.isEmpty()) {
			patientName.setGivenName(givenNames.get(0).getValue());
			StringBuilder middleNames = new StringBuilder();
			for (int i = 1; i < givenNames.size(); i++) {
				String middleName = StringUtils.trimToNull(givenNames.get(i).getValue());
				if (middleName != null) {
					if (middleNames.length() > 0) {
						middleNames.append(' ');
					}
					middleNames.append(middleName);
				}
			}
			patientName.setMiddleName(StringUtils.trimToNull(middleNames.toString()));
		}
		if (StringUtils.isNotBlank(registryName.getFamily())) {
			patientName.setFamilyName(registryName.getFamily());
		}
	}

	Map<String, Object> toRegistrationFields(ClientRegistryPatient registryPatient, Patient patient) {
		Map<String, Object> fields = toRegistrationFields(patient);
		if (registryPatient == null) {
			return fields;
		}

		HumanName registryName = registryPatient.getName();
		if (registryName == null) {
			return fields;
		}

		List<StringType> givenNames = registryName.getGiven();
		if (givenNames != null && !givenNames.isEmpty()) {
			putIfNotBlank(fields, "givenName", givenNames.get(0).getValue());
			StringBuilder middleNames = new StringBuilder();
			for (int i = 1; i < givenNames.size(); i++) {
				String middleName = StringUtils.trimToNull(givenNames.get(i).getValue());
				if (middleName != null) {
					if (middleNames.length() > 0) {
						middleNames.append(' ');
					}
					middleNames.append(middleName);
				}
			}
			putIfNotBlank(fields, "middleName", middleNames.toString());
		}
		putIfNotBlank(fields, "familyName", registryName.getFamily());
		return fields;
	}

	Map<String, Object> toRegistrationFields(Patient patient) {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		if (patient == null) {
			return fields;
		}

		putIfNotBlank(fields, "givenName", patient.getGivenName());
		putIfNotBlank(fields, "middleName", patient.getMiddleName());
		putIfNotBlank(fields, "familyName", patient.getFamilyName());
		putIfNotBlank(fields, "gender", patient.getGender());

		if (patient.getBirthdate() != null) {
			Calendar birthdate = Calendar.getInstance();
			birthdate.setTime(patient.getBirthdate());
			fields.put("birthdate", new SimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()));
			fields.put("birthdateDay", birthdate.get(Calendar.DAY_OF_MONTH));
			fields.put("birthdateMonth", birthdate.get(Calendar.MONTH) + 1);
			fields.put("birthdateYear", birthdate.get(Calendar.YEAR));
			fields.put("birthdateEstimated", Boolean.TRUE.equals(patient.getBirthdateEstimated()));
		}

		for (PatientIdentifier identifier : patient.getIdentifiers()) {
			PatientIdentifierType type = identifier.getIdentifierType();
			if (sameMetadata(type, rwandaEmrConfig.getNationalId())) {
				putIfNotBlank(fields, "nationalId", identifier.getIdentifier());
			} else if (sameMetadata(type, rwandaEmrConfig.getNidApplicationNumber())) {
				putIfNotBlank(fields, "applicationNumber", identifier.getIdentifier());
			} else if (sameMetadata(type, rwandaEmrConfig.getUPID())) {
				putIfNotBlank(fields, "upid", identifier.getIdentifier());
			} else if (sameMetadata(type, rwandaEmrConfig.getNIN())) {
				putIfNotBlank(fields, "nin", identifier.getIdentifier());
			} else if (sameMetadata(type, rwandaEmrConfig.getPassportNumber())) {
				putIfNotBlank(fields, "passportNumber", identifier.getIdentifier());
			}
		}

		for (PersonAttribute attribute : patient.getAttributes()) {
			PersonAttributeType type = attribute.getAttributeType();
			if (sameMetadata(type, rwandaEmrConfig.getTelephoneNumber())) {
				putIfNotBlank(fields, "phoneNumber", attribute.getValue());
			} else if (sameMetadata(type, rwandaEmrConfig.getMothersName())) {
				putIfNotBlank(fields, "mothersName", attribute.getValue());
			} else if (sameMetadata(type, rwandaEmrConfig.getFathersName())) {
				putIfNotBlank(fields, "fathersName", attribute.getValue());
			} else if (sameMetadata(type, rwandaEmrConfig.getEducationLevel())) {
				putIfNotBlank(fields, "educationLevel", attribute.getValue());
			} else if (sameMetadata(type, rwandaEmrConfig.getProfession())) {
				putIfNotBlank(fields, "profession", attribute.getValue());
			} else if (sameMetadata(type, rwandaEmrConfig.getReligion())) {
				putIfNotBlank(fields, "religion", attribute.getValue());
			}
		}

		PersonAddress address = patient.getPersonAddress();
		if (address != null) {
			putIfNotBlank(fields, "country", address.getCountry());
			putIfNotBlank(fields, "stateProvince", address.getStateProvince());
			putIfNotBlank(fields, "countyDistrict", address.getCountyDistrict());
			putIfNotBlank(fields, "cityVillage", address.getCityVillage());
			putIfNotBlank(fields, "address3", address.getAddress3());
			putIfNotBlank(fields, "address1", address.getAddress1());
		}

		return fields;
	}

	private boolean sameMetadata(Object left, Object right) {
		return left != null && right != null && left.equals(right);
	}

	private void putIfNotBlank(Map<String, Object> fields, String name, String value) {
		if (StringUtils.isNotBlank(value)) {
			fields.put(name, value);
		}
	}

	public void setIntegrationConfig(IntegrationConfig integrationConfig) {
		this.integrationConfig = integrationConfig;
	}

	public void setClientRegistryPatientProvider(ClientRegistryPatientProvider clientRegistryPatientProvider) {
		this.clientRegistryPatientProvider = clientRegistryPatientProvider;
	}

	public void setClientRegistryPatientTranslator(ClientRegistryPatientTranslator clientRegistryPatientTranslator) {
		this.clientRegistryPatientTranslator = clientRegistryPatientTranslator;
	}

	public void setRwandaEmrConfig(RwandaEmrConfig rwandaEmrConfig) {
		this.rwandaEmrConfig = rwandaEmrConfig;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}

	public void setRegistrationCoreService(RegistrationCoreService registrationCoreService) {
		this.registrationCoreService = registrationCoreService;
	}
}
