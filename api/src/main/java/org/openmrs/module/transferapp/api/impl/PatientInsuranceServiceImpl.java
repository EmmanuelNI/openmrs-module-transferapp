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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.PatientInsuranceService;
import org.openmrs.module.transferapp.model.PatientInsuranceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientInsuranceServiceImpl implements PatientInsuranceService {

	@Override
	public PatientInsuranceInfo getPatientInsurance(Patient patient) {
		PatientInsuranceInfo info = new PatientInsuranceInfo();
		if (patient == null || patient.getPatientId() == null) {
			return info;
		}

		Concept typeConcept = getConceptByGlobalProperty(
				TransferAppConstants.GP_INSURANCE_TYPE_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_INSURANCE_TYPE_CONCEPT_UUID);
		Concept numberConcept = getConceptByGlobalProperty(
				TransferAppConstants.GP_INSURANCE_NUMBER_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_INSURANCE_NUMBER_CONCEPT_UUID);

		if (typeConcept != null) {
			Obs typeObs = getLatestObservation(patient, typeConcept);
			if (typeObs != null && typeObs.getValueCoded() != null) {
				Concept valueCoded = typeObs.getValueCoded();
				info.setInsuranceType(valueCoded.getDisplayString());
				info.setInsuranceTypeCodedId(valueCoded.getConceptId());
			}
		}

		if (numberConcept != null) {
			Obs numberObs = getLatestObservation(patient, numberConcept);
			if (numberObs != null) {
				String number = StringUtils.trimToNull(numberObs.getValueText());
				if (number == null) {
					number = StringUtils.trimToNull(numberObs.getValueAsString(Context.getLocale()));
				}
				info.setInsuranceNumber(number);
			}
		}

		resolveHealthInsuranceCategory(info);
		return info;
	}

	@Override
	public String resolveInsuranceCardNumber(Patient patient) {
		if (patient == null || patient.getPatientId() == null) {
			return null;
		}
		String fromRegistration = resolveInsuranceNumberFromLatestRegistration(patient);
		if (fromRegistration != null) {
			return fromRegistration;
		}
		PatientInsuranceInfo info = getPatientInsurance(patient);
		return info != null ? StringUtils.trimToNull(info.getInsuranceNumber()) : null;
	}

	private String resolveInsuranceNumberFromLatestRegistration(Patient patient) {
		Concept numberConcept = getConceptByGlobalProperty(
				TransferAppConstants.GP_INSURANCE_NUMBER_CONCEPT_UUID,
				TransferAppConstants.DEFAULT_INSURANCE_NUMBER_CONCEPT_UUID);
		if (numberConcept == null) {
			return null;
		}
		Encounter registration = findLatestRegistrationEncounter(patient);
		if (registration == null) {
			return null;
		}
		Set<Obs> obsSet = registration.getAllObs(false);
		if (obsSet == null || obsSet.isEmpty()) {
			return null;
		}
		for (Obs obs : obsSet) {
			if (obs == null || Boolean.TRUE.equals(obs.getVoided()) || obs.getConcept() == null) {
				continue;
			}
			if (!numberConcept.equals(obs.getConcept())) {
				continue;
			}
			String number = StringUtils.trimToNull(obs.getValueText());
			if (number == null) {
				number = StringUtils.trimToNull(obs.getValueAsString(Context.getLocale()));
			}
			if (number != null) {
				return number;
			}
		}
		return null;
	}

	private Encounter findLatestRegistrationEncounter(Patient patient) {
		Integer registrationTypeId = resolveRegistrationEncounterTypeId();
		if (registrationTypeId == null) {
			return null;
		}
		List<Encounter> encounters = getEncounterService().getEncountersByPatient(patient);
		if (encounters == null || encounters.isEmpty()) {
			return null;
		}
		List<Encounter> registrationEncounters = new ArrayList<Encounter>();
		for (Encounter encounter : encounters) {
			if (encounter == null || Boolean.TRUE.equals(encounter.getVoided())) {
				continue;
			}
			EncounterType type = encounter.getEncounterType();
			if (type == null || type.getEncounterTypeId() == null) {
				continue;
			}
			if (registrationTypeId.equals(type.getEncounterTypeId())) {
				registrationEncounters.add(encounter);
			}
		}
		if (registrationEncounters.isEmpty()) {
			return null;
		}
		Collections.sort(registrationEncounters, new Comparator<Encounter>() {
			@Override
			public int compare(Encounter left, Encounter right) {
				Date leftDate = left != null ? left.getEncounterDatetime() : null;
				Date rightDate = right != null ? right.getEncounterDatetime() : null;
				if (leftDate == null && rightDate == null) {
					return 0;
				}
				if (leftDate == null) {
					return 1;
				}
				if (rightDate == null) {
					return -1;
				}
				return rightDate.compareTo(leftDate);
			}
		});
		return registrationEncounters.get(0);
	}

	private Integer resolveRegistrationEncounterTypeId() {
		String raw = StringUtils.trimToNull(getAdministrationService().getGlobalProperty(
				TransferAppConstants.GP_REGISTRATION_ENCOUNTER_TYPE_ID,
				TransferAppConstants.DEFAULT_REGISTRATION_ENCOUNTER_TYPE_ID));
		if (raw == null) {
			raw = StringUtils.trimToNull(getAdministrationService().getGlobalProperty(
					TransferAppConstants.GP_RWANDAEMR_REGISTRATION_ENCOUNTER_TYPE_ID));
		}
		if (raw == null) {
			return null;
		}
		try {
			int id = Integer.parseInt(raw);
			return id > 0 ? Integer.valueOf(id) : null;
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	private void resolveHealthInsuranceCategory(PatientInsuranceInfo info) {
		Integer codedId = info.getInsuranceTypeCodedId();
		if (codedId != null) {
			if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_CBHI)) {
				info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_CBHI);
				return;
			}
			if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_RSSB)) {
				info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_RSSB);
				return;
			}
			if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_MMI)) {
				info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_MMI);
				return;
			}
			if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_NONE)) {
				info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_NONE);
				return;
			}
			if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_OTHER)) {
				info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_OTHER);
				info.setHealthInsuranceOtherSpec(info.getInsuranceType());
				return;
			}
		}

		// Concept-id GPs may be unset; match common display labels (e.g. "NONE") so we do not
		// store NONE as OTHER + otherSpec "NONE".
		String byDisplay = matchCategoryByDisplayName(info.getInsuranceType());
		if (byDisplay != null) {
			info.setHealthInsuranceCategory(byDisplay);
			if (TransferAppConstants.HEALTH_INSURANCE_OTHER.equals(byDisplay)) {
				info.setHealthInsuranceOtherSpec(info.getInsuranceType());
			}
			return;
		}

		if (StringUtils.isNotBlank(info.getInsuranceType())) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_OTHER);
			info.setHealthInsuranceOtherSpec(info.getInsuranceType());
		}
	}

	/**
	 * Maps insurance type display text to transfer categories when concept-id GPs do not match.
	 */
	public static String matchCategoryByDisplayName(String displayName) {
		String raw = StringUtils.trimToNull(displayName);
		if (raw == null) {
			return null;
		}
		String normalized = raw.toLowerCase().replace('_', ' ').replace('-', ' ');
		normalized = normalized.replaceAll("\\s+", " ").trim();

		if ("none".equals(normalized) || "n/a".equals(normalized) || "na".equals(normalized)
				|| "no".equals(normalized) || "no insurance".equals(normalized)
				|| "without insurance".equals(normalized) || "uninsured".equals(normalized)) {
			return TransferAppConstants.HEALTH_INSURANCE_NONE;
		}
		if (normalized.contains("cbhi") || normalized.contains("mutuelle")) {
			return TransferAppConstants.HEALTH_INSURANCE_CBHI;
		}
		if (normalized.contains("rssb")) {
			return TransferAppConstants.HEALTH_INSURANCE_RSSB;
		}
		if (normalized.contains("mmi")) {
			return TransferAppConstants.HEALTH_INSURANCE_MMI;
		}
		if ("other".equals(normalized) || normalized.startsWith("other ")) {
			return TransferAppConstants.HEALTH_INSURANCE_OTHER;
		}
		return null;
	}

	private boolean matchesConceptList(Integer conceptId, String globalPropertyName) {
		if (conceptId == null || StringUtils.isBlank(globalPropertyName)) {
			return false;
		}
		return loadConceptIdsFromGlobalProperty(globalPropertyName).contains(conceptId);
	}

	private Set<Integer> loadConceptIdsFromGlobalProperty(String globalPropertyName) {
		String rawValue = getAdministrationService().getGlobalProperty(globalPropertyName);
		if (StringUtils.isBlank(rawValue)) {
			return Collections.emptySet();
		}

		Set<Integer> conceptIds = new HashSet<Integer>();
		for (String token : rawValue.split(",")) {
			String trimmed = StringUtils.trimToNull(token);
			if (trimmed == null) {
				continue;
			}
			try {
				conceptIds.add(Integer.valueOf(trimmed));
			}
			catch (NumberFormatException ignored) {
				Concept concept = getConceptService().getConceptByUuid(trimmed);
				if (concept != null && concept.getConceptId() != null) {
					conceptIds.add(concept.getConceptId());
				}
			}
		}
		return conceptIds;
	}

	private Concept getConceptByGlobalProperty(String propertyName, String defaultUuid) {
		String conceptUuid = getAdministrationService().getGlobalProperty(propertyName, defaultUuid);
		if (StringUtils.isBlank(conceptUuid)) {
			return null;
		}
		return getConceptService().getConceptByUuid(conceptUuid.trim());
	}

	private Obs getLatestObservation(Patient patient, Concept questionConcept) {
		List<Obs> observations = getObsService().getObservations(
				Collections.singletonList(patient),
				null,
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

	protected AdministrationService getAdministrationService() {
		return Context.getAdministrationService();
	}

	protected ConceptService getConceptService() {
		return Context.getConceptService();
	}

	protected ObsService getObsService() {
		return Context.getObsService();
	}

	protected EncounterService getEncounterService() {
		return Context.getEncounterService();
	}

}
