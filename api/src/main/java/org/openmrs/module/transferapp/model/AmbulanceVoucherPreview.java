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

import java.math.BigDecimal;

/**
 * Full BON D'AMBULANCE preview payload (all form fields from the paper voucher).
 */
public class AmbulanceVoucherPreview {

	private String transferUuid;

	private String organizationName = "COMMUNITY BASED HEALTH INSURANCE (CBHI)";

	private String fax = "+250 252 584 445";

	private String title = "BON D'AMBULANCE";

	private String province;

	private String district;

	private String sectionHospital;

	private String date;

	private String departureTime;

	private Integer patientCount = 1;

	private String voucherId;

	private String destination;

	private Integer distanceKm;

	private String patientName;

	private String affiliationNumber;

	private String driverName;

	private String accompanyingNurse;

	private String arrivalDate;

	private String arrivalTime;

	private String cbhiAgentName;

	private String receivingClinicianName;

	private BigDecimal amount;

	private Integer consommationId;

	private String noteReferral = "Ce bon d'ambulance accompagne la fiche de référence/contre référence du patient.";

	private String noteInvoice = "La facture de l'ambulance est unique pour l'ensemble des patients à bord.";

	public String getTransferUuid() {
		return transferUuid;
	}

	public void setTransferUuid(String transferUuid) {
		this.transferUuid = transferUuid;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getSectionHospital() {
		return sectionHospital;
	}

	public void setSectionHospital(String sectionHospital) {
		this.sectionHospital = sectionHospital;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public Integer getPatientCount() {
		return patientCount;
	}

	public void setPatientCount(Integer patientCount) {
		this.patientCount = patientCount;
	}

	public String getVoucherId() {
		return voucherId;
	}

	public void setVoucherId(String voucherId) {
		this.voucherId = voucherId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Integer getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(Integer distanceKm) {
		this.distanceKm = distanceKm;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getAffiliationNumber() {
		return affiliationNumber;
	}

	public void setAffiliationNumber(String affiliationNumber) {
		this.affiliationNumber = affiliationNumber;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getAccompanyingNurse() {
		return accompanyingNurse;
	}

	public void setAccompanyingNurse(String accompanyingNurse) {
		this.accompanyingNurse = accompanyingNurse;
	}

	public String getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getCbhiAgentName() {
		return cbhiAgentName;
	}

	public void setCbhiAgentName(String cbhiAgentName) {
		this.cbhiAgentName = cbhiAgentName;
	}

	public String getReceivingClinicianName() {
		return receivingClinicianName;
	}

	public void setReceivingClinicianName(String receivingClinicianName) {
		this.receivingClinicianName = receivingClinicianName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getConsommationId() {
		return consommationId;
	}

	public void setConsommationId(Integer consommationId) {
		this.consommationId = consommationId;
	}

	public String getNoteReferral() {
		return noteReferral;
	}

	public void setNoteReferral(String noteReferral) {
		this.noteReferral = noteReferral;
	}

	public String getNoteInvoice() {
		return noteInvoice;
	}

	public void setNoteInvoice(String noteInvoice) {
		this.noteInvoice = noteInvoice;
	}

}
