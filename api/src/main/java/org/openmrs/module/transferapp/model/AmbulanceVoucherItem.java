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
import java.util.Date;

/**
 * One ambulance voucher row (transfer linked to a mohbilling consommation).
 */
public class AmbulanceVoucherItem {

	private Integer rowNumber;

	private String transferUuid;

	private Date transferDate;

	private String patientUpid;

	private String patientName;

	private String fromHospital;

	private String destinationHospital;

	private Integer distance;

	private BigDecimal amount;

	private Integer ambulanceConsommationId;

	public Integer getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(Integer rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getTransferUuid() {
		return transferUuid;
	}

	public void setTransferUuid(String transferUuid) {
		this.transferUuid = transferUuid;
	}

	public Date getTransferDate() {
		return transferDate;
	}

	public void setTransferDate(Date transferDate) {
		this.transferDate = transferDate;
	}

	public String getPatientUpid() {
		return patientUpid;
	}

	public void setPatientUpid(String patientUpid) {
		this.patientUpid = patientUpid;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getFromHospital() {
		return fromHospital;
	}

	public void setFromHospital(String fromHospital) {
		this.fromHospital = fromHospital;
	}

	public String getDestinationHospital() {
		return destinationHospital;
	}

	public void setDestinationHospital(String destinationHospital) {
		this.destinationHospital = destinationHospital;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getAmbulanceConsommationId() {
		return ambulanceConsommationId;
	}

	public void setAmbulanceConsommationId(Integer ambulanceConsommationId) {
		this.ambulanceConsommationId = ambulanceConsommationId;
	}

}
