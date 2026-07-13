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
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.PatientInsuranceService;
import org.openmrs.module.transferapp.model.PatientInsuranceInfo;

import java.util.Collections;
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

	private void resolveHealthInsuranceCategory(PatientInsuranceInfo info) {
		if (info.getInsuranceTypeCodedId() == null) {
			return;
		}

		Integer codedId = info.getInsuranceTypeCodedId();
		if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_CBHI)) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_CBHI);
		}
		else if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_RSSB)) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_RSSB);
		}
		else if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_MMI)) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_MMI);
		}
		else if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_NONE)) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_NONE);
		}
		else if (matchesConceptList(codedId, TransferAppConstants.GP_INSURANCE_OTHER)) {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_OTHER);
			info.setHealthInsuranceOtherSpec(info.getInsuranceType());
		}
		else {
			info.setHealthInsuranceCategory(TransferAppConstants.HEALTH_INSURANCE_OTHER);
			info.setHealthInsuranceOtherSpec(info.getInsuranceType());
		}
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

}
