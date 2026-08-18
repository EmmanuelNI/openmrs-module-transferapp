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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "TransferappReceivingFacility")
@Table(name = "transfer_receiving_facilities")
public class ReceivingFacility extends BaseOpenmrsData {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "receiving_facility_id")
	private Integer receivingFacilityId;

	@Column(name = "sending_location_id", nullable = false)
	private Integer sendingLocationId;

	@Column(name = "facility_code", nullable = false, length = 64)
	private String facilityCode;

	@Column(name = "facility_name", nullable = false, length = 255)
	private String facilityName;

	@Column(name = "distance")
	private Integer distance;

	@Column(name = "province", length = 120)
	private String province;

	@Column(name = "district", length = 120)
	private String district;

	@Column(name = "external")
	private Boolean external;

	@Override
	public Integer getId() {
		return getReceivingFacilityId();
	}

	@Override
	public void setId(Integer id) {
		setReceivingFacilityId(id);
	}

	public Integer getReceivingFacilityId() {
		return receivingFacilityId;
	}

	public void setReceivingFacilityId(Integer receivingFacilityId) {
		this.receivingFacilityId = receivingFacilityId;
	}

	public Integer getSendingLocationId() {
		return sendingLocationId;
	}

	public void setSendingLocationId(Integer sendingLocationId) {
		this.sendingLocationId = sendingLocationId;
	}

	public String getFacilityCode() {
		return facilityCode;
	}

	public void setFacilityCode(String facilityCode) {
		this.facilityCode = facilityCode;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
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

	public Boolean getExternal() {
		return external;
	}

	public void setExternal(Boolean external) {
		this.external = external;
	}

	public boolean isExternal() {
		return Boolean.TRUE.equals(external);
	}

}
