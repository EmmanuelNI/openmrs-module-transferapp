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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Persisted outbound transfer referral record.
 */
@Entity(name = "TransferappTransfer")
@Table(name = "transfers")
public class Transfer extends BaseOpenmrsData {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "transfer_id")
	private Integer transferId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Column(name = "decision_to_transfer_at")
	private Date decisionToTransferAt;

	@Column(name = "admission_at")
	private Date admissionAt;

	@Column(name = "calling_time", length = 8)
	private String callingTime;

	@Column(name = "receiving_facility_code", length = 64)
	private String receivingFacilityCode;

	@Column(name = "receiving_province", length = 120)
	private String receivingProvince;

	@Column(name = "receiving_district", length = 120)
	private String receivingDistrict;

	@Column(name = "receiving_service", length = 255)
	private String receivingService;

	@Column(name = "staff_contacted_name", length = 255)
	private String staffContactedName;

	@Column(name = "staff_contacted_phone", length = 64)
	private String staffContactedPhone;

	@Column(name = "reason_for_transfer")
	private String reasonForTransfer;

	@Column(name = "diagnosis")
	private String diagnosis;

	@Column(name = "vital_ht", length = 32)
	private String vitalHt;

	@Column(name = "vital_wt", length = 32)
	private String vitalWt;

	@Column(name = "vital_temp", length = 32)
	private String vitalTemp;

	@Column(name = "vital_pulse", length = 32)
	private String vitalPulse;

	@Column(name = "vital_bp", length = 32)
	private String vitalBp;

	@Column(name = "vital_spo2", length = 32)
	private String vitalSpo2;

	@Column(name = "vital_muac", length = 32)
	private String vitalMuac;

	@Column(name = "vital_rr", length = 32)
	private String vitalRr;

	@Column(name = "transfer_type", length = 32)
	private String transferType;

	@Column(name = "ambulance_call_time", length = 8)
	private String ambulanceCallTime;

	@Column(name = "depart_ref_time", length = 8)
	private String departRefTime;

	@Column(name = "transport_type", length = 16)
	private String transportType;

	@Column(name = "transport_other", length = 255)
	private String transportOther;

	@Column(name = "emr_id", length = 64)
	private String emrId;

	@Column(name = "client_name", length = 255)
	private String clientName;

	@Column(name = "client_telephone", length = 64)
	private String clientTelephone;

	@Column(name = "age_or_dob", length = 64)
	private String ageOrDob;

	@Column(name = "sex", length = 20)
	private String sex;

	@Column(name = "identifier_type", length = 40)
	private String identifierType;

	@Column(name = "identifier_value", length = 64)
	private String identifierValue;

	@Column(name = "caregiver_name", length = 255)
	private String caregiverName;

	@Column(name = "caregiver_telephone", length = 64)
	private String caregiverTelephone;

	@Column(name = "client_district", length = 120)
	private String clientDistrict;

	@Column(name = "sector", length = 120)
	private String sector;

	@Column(name = "cell", length = 120)
	private String cell;

	@Column(name = "village", length = 120)
	private String village;

	@Column(name = "sending_facility", length = 255)
	private String sendingFacility;

	@Column(name = "referring_unit", length = 255)
	private String referringUnit;

	@Column(name = "referring_provider_name", length = 255)
	private String referringProviderName;

	@Column(name = "health_insurance_type", length = 16)
	private String healthInsuranceType;

	@Column(name = "health_insurance_other", length = 255)
	private String healthInsuranceOther;

	@Column(name = "hie_sent")
	private Boolean hieSent;

	@Column(name = "hie_sent_at")
	private Date hieSentAt;

	@Column(name = "hie_send_err", length = 500)
	private String hieSendError;

	@Column(name = "hie_transfer_id", length = 64)
	private String hieTransferId;

	@Column(name = "received_from_hie", nullable = true)
	private Boolean receivedFromHie;

	@Column(name = "clinical_presentation")
	private String clinicalPresentation;

	@Column(name = "disability_type", length = 255)
	private String disabilityType;

	@Column(name = "laboratory")
	private String laboratory;

	@Column(name = "procedures_treatments")
	private String proceduresTreatments;

	@Column(name = "other_notes")
	private String otherNotes;

	@Column(name = "provider_qualification", length = 255)
	private String providerQualification;

	@Column(name = "signed_date")
	private Date signedDate;

	@Column(name = "signed_time", length = 8)
	private String signedTime;

	@Override
	public Integer getId() {
		return getTransferId();
	}

	@Override
	public void setId(Integer id) {
		setTransferId(id);
	}

	public Integer getTransferId() {
		return transferId;
	}

	public void setTransferId(Integer transferId) {
		this.transferId = transferId;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Date getDecisionToTransferAt() {
		return decisionToTransferAt;
	}

	public void setDecisionToTransferAt(Date decisionToTransferAt) {
		this.decisionToTransferAt = decisionToTransferAt;
	}

	public Date getAdmissionAt() {
		return admissionAt;
	}

	public void setAdmissionAt(Date admissionAt) {
		this.admissionAt = admissionAt;
	}

	public String getCallingTime() {
		return callingTime;
	}

	public void setCallingTime(String callingTime) {
		this.callingTime = callingTime;
	}

	public String getReceivingFacilityCode() {
		return receivingFacilityCode;
	}

	public void setReceivingFacilityCode(String receivingFacilityCode) {
		this.receivingFacilityCode = receivingFacilityCode;
	}

	public String getReceivingProvince() {
		return receivingProvince;
	}

	public void setReceivingProvince(String receivingProvince) {
		this.receivingProvince = receivingProvince;
	}

	public String getReceivingDistrict() {
		return receivingDistrict;
	}

	public void setReceivingDistrict(String receivingDistrict) {
		this.receivingDistrict = receivingDistrict;
	}

	public String getReceivingService() {
		return receivingService;
	}

	public void setReceivingService(String receivingService) {
		this.receivingService = receivingService;
	}

	public String getStaffContactedName() {
		return staffContactedName;
	}

	public void setStaffContactedName(String staffContactedName) {
		this.staffContactedName = staffContactedName;
	}

	public String getStaffContactedPhone() {
		return staffContactedPhone;
	}

	public void setStaffContactedPhone(String staffContactedPhone) {
		this.staffContactedPhone = staffContactedPhone;
	}

	public String getReasonForTransfer() {
		return reasonForTransfer;
	}

	public void setReasonForTransfer(String reasonForTransfer) {
		this.reasonForTransfer = reasonForTransfer;
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getVitalHt() {
		return vitalHt;
	}

	public void setVitalHt(String vitalHt) {
		this.vitalHt = vitalHt;
	}

	public String getVitalWt() {
		return vitalWt;
	}

	public void setVitalWt(String vitalWt) {
		this.vitalWt = vitalWt;
	}

	public String getVitalTemp() {
		return vitalTemp;
	}

	public void setVitalTemp(String vitalTemp) {
		this.vitalTemp = vitalTemp;
	}

	public String getVitalPulse() {
		return vitalPulse;
	}

	public void setVitalPulse(String vitalPulse) {
		this.vitalPulse = vitalPulse;
	}

	public String getVitalBp() {
		return vitalBp;
	}

	public void setVitalBp(String vitalBp) {
		this.vitalBp = vitalBp;
	}

	public String getVitalSpo2() {
		return vitalSpo2;
	}

	public void setVitalSpo2(String vitalSpo2) {
		this.vitalSpo2 = vitalSpo2;
	}

	public String getVitalMuac() {
		return vitalMuac;
	}

	public void setVitalMuac(String vitalMuac) {
		this.vitalMuac = vitalMuac;
	}

	public String getVitalRr() {
		return vitalRr;
	}

	public void setVitalRr(String vitalRr) {
		this.vitalRr = vitalRr;
	}

	public String getTransferType() {
		return transferType;
	}

	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}

	public String getAmbulanceCallTime() {
		return ambulanceCallTime;
	}

	public void setAmbulanceCallTime(String ambulanceCallTime) {
		this.ambulanceCallTime = ambulanceCallTime;
	}

	public String getDepartRefTime() {
		return departRefTime;
	}

	public void setDepartRefTime(String departRefTime) {
		this.departRefTime = departRefTime;
	}

	public String getTransportType() {
		return transportType;
	}

	public void setTransportType(String transportType) {
		this.transportType = transportType;
	}

	public String getTransportOther() {
		return transportOther;
	}

	public void setTransportOther(String transportOther) {
		this.transportOther = transportOther;
	}

	public String getEmrId() {
		return emrId;
	}

	public void setEmrId(String emrId) {
		this.emrId = emrId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getClientTelephone() {
		return clientTelephone;
	}

	public void setClientTelephone(String clientTelephone) {
		this.clientTelephone = clientTelephone;
	}

	public String getAgeOrDob() {
		return ageOrDob;
	}

	public void setAgeOrDob(String ageOrDob) {
		this.ageOrDob = ageOrDob;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getIdentifierType() {
		return identifierType;
	}

	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public String getCaregiverName() {
		return caregiverName;
	}

	public void setCaregiverName(String caregiverName) {
		this.caregiverName = caregiverName;
	}

	public String getCaregiverTelephone() {
		return caregiverTelephone;
	}

	public void setCaregiverTelephone(String caregiverTelephone) {
		this.caregiverTelephone = caregiverTelephone;
	}

	public String getClientDistrict() {
		return clientDistrict;
	}

	public void setClientDistrict(String clientDistrict) {
		this.clientDistrict = clientDistrict;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getVillage() {
		return village;
	}

	public void setVillage(String village) {
		this.village = village;
	}

	public String getSendingFacility() {
		return sendingFacility;
	}

	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}

	public String getReferringUnit() {
		return referringUnit;
	}

	public void setReferringUnit(String referringUnit) {
		this.referringUnit = referringUnit;
	}

	public String getReferringProviderName() {
		return referringProviderName;
	}

	public void setReferringProviderName(String referringProviderName) {
		this.referringProviderName = referringProviderName;
	}

	public String getHealthInsuranceType() {
		return healthInsuranceType;
	}

	public void setHealthInsuranceType(String healthInsuranceType) {
		this.healthInsuranceType = healthInsuranceType;
	}

	public String getHealthInsuranceOther() {
		return healthInsuranceOther;
	}

	public void setHealthInsuranceOther(String healthInsuranceOther) {
		this.healthInsuranceOther = healthInsuranceOther;
	}

	public Boolean getHieSent() {
		return hieSent;
	}

	public void setHieSent(Boolean hieSent) {
		this.hieSent = hieSent;
	}

	public Date getHieSentAt() {
		return hieSentAt;
	}

	public void setHieSentAt(Date hieSentAt) {
		this.hieSentAt = hieSentAt;
	}

	public String getHieSendError() {
		return hieSendError;
	}

	public void setHieSendError(String hieSendError) {
		this.hieSendError = hieSendError;
	}

	public boolean isSentToHie() {
		return Boolean.TRUE.equals(hieSent);
	}

	public String getHieTransferId() {
		return hieTransferId;
	}

	public void setHieTransferId(String hieTransferId) {
		this.hieTransferId = hieTransferId;
	}

	public Boolean getReceivedFromHie() {
		return receivedFromHie;
	}

	public void setReceivedFromHie(Boolean receivedFromHie) {
		this.receivedFromHie = receivedFromHie;
	}

	public boolean isReceivedFromHie() {
		return Boolean.TRUE.equals(receivedFromHie);
	}

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

	public String getProviderQualification() {
		return providerQualification;
	}

	public void setProviderQualification(String providerQualification) {
		this.providerQualification = providerQualification;
	}

	public Date getSignedDate() {
		return signedDate;
	}

	public void setSignedDate(Date signedDate) {
		this.signedDate = signedDate;
	}

	public String getSignedTime() {
		return signedTime;
	}

	public void setSignedTime(String signedTime) {
		this.signedTime = signedTime;
	}

}
