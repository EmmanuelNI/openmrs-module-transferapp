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

@Entity(name = "TransferappReceivingService")
@Table(name = "transfer_receiving_services")
public class ReceivingService extends BaseOpenmrsData {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "receiving_service_id")
	private Integer receivingServiceId;

	@Column(name = "receiving_facility_id", nullable = false)
	private Integer receivingFacilityId;

	@Column(name = "service_name", nullable = false, length = 255)
	private String serviceName;

	@Override
	public Integer getId() {
		return getReceivingServiceId();
	}

	@Override
	public void setId(Integer id) {
		setReceivingServiceId(id);
	}

	public Integer getReceivingServiceId() {
		return receivingServiceId;
	}

	public void setReceivingServiceId(Integer receivingServiceId) {
		this.receivingServiceId = receivingServiceId;
	}

	public Integer getReceivingFacilityId() {
		return receivingFacilityId;
	}

	public void setReceivingFacilityId(Integer receivingFacilityId) {
		this.receivingFacilityId = receivingFacilityId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

}
