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
import javax.persistence.UniqueConstraint;

@Entity(name = "TransferappTransferProfile")
@Table(name = "transfer_profile", uniqueConstraints = {
		@UniqueConstraint(name = "transfer_profile_user_id_uk", columnNames = { "user_id" })
})
public class TransferProfile extends BaseOpenmrsData {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "transfer_profile_id")
	private Integer transferProfileId;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "license_number", length = 64)
	private String licenseNumber;

	@Column(name = "phone_number", length = 64)
	private String phoneNumber;

	@Column(name = "qualification", length = 255)
	private String qualification;

	@Override
	public Integer getId() {
		return getTransferProfileId();
	}

	@Override
	public void setId(Integer id) {
		setTransferProfileId(id);
	}

	public Integer getTransferProfileId() {
		return transferProfileId;
	}

	public void setTransferProfileId(Integer transferProfileId) {
		this.transferProfileId = transferProfileId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

}
