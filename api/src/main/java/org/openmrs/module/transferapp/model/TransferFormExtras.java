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

/**
 * Optional extended transfer form fields cached on a transfer record.
 */
public class TransferFormExtras {

	private String clinicalPresentation;

	private String disabilityType;

	private String laboratory;

	private String proceduresTreatments;

	private String otherNotes;

	private String diagnosis;

	private String providerQualification;

	private String signedDate;

	private String signedTime;

	public String getClinicalPresentation() {
		return clinicalPresentation;
	}

	public void setClinicalPresentation(String clinicalPresentation) {
		this.clinicalPresentation = clinicalPresentation;
	}

	public String getDisabilityType() {
		return disabilityType;
	}

	public void setDisabilityType(String disabilityType) {
		this.disabilityType = disabilityType;
	}

	public String getLaboratory() {
		return laboratory;
	}

	public void setLaboratory(String laboratory) {
		this.laboratory = laboratory;
	}

	public String getProceduresTreatments() {
		return proceduresTreatments;
	}

	public void setProceduresTreatments(String proceduresTreatments) {
		this.proceduresTreatments = proceduresTreatments;
	}

	public String getOtherNotes() {
		return otherNotes;
	}

	public void setOtherNotes(String otherNotes) {
		this.otherNotes = otherNotes;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getProviderQualification() {
		return providerQualification;
	}

	public void setProviderQualification(String providerQualification) {
		this.providerQualification = providerQualification;
	}

	public String getSignedDate() {
		return signedDate;
	}

	public void setSignedDate(String signedDate) {
		this.signedDate = signedDate;
	}

	public String getSignedTime() {
		return signedTime;
	}

	public void setSignedTime(String signedTime) {
		this.signedTime = signedTime;
	}

}
