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
package org.openmrs.module.transferapp.model;

public class PatientInsuranceInfo {

	private String insuranceType;

	private Integer insuranceTypeCodedId;

	private String insuranceNumber;

	private String healthInsuranceCategory;

	private String healthInsuranceOtherSpec;

	public String getInsuranceType() {
		return insuranceType;
	}

	public void setInsuranceType(String insuranceType) {
		this.insuranceType = insuranceType;
	}

	public Integer getInsuranceTypeCodedId() {
		return insuranceTypeCodedId;
	}

	public void setInsuranceTypeCodedId(Integer insuranceTypeCodedId) {
		this.insuranceTypeCodedId = insuranceTypeCodedId;
	}

	public String getInsuranceNumber() {
		return insuranceNumber;
	}

	public void setInsuranceNumber(String insuranceNumber) {
		this.insuranceNumber = insuranceNumber;
	}

	public String getHealthInsuranceCategory() {
		return healthInsuranceCategory;
	}

	public void setHealthInsuranceCategory(String healthInsuranceCategory) {
		this.healthInsuranceCategory = healthInsuranceCategory;
	}

	public String getHealthInsuranceOtherSpec() {
		return healthInsuranceOtherSpec;
	}

	public void setHealthInsuranceOtherSpec(String healthInsuranceOtherSpec) {
		this.healthInsuranceOtherSpec = healthInsuranceOtherSpec;
	}

	public boolean isAvailable() {
		return insuranceType != null && insuranceType.trim().length() > 0
				&& insuranceNumber != null && insuranceNumber.trim().length() > 0;
	}

}
