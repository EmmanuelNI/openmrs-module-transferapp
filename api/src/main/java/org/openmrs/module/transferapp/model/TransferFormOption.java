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
 * Label/value pair for transfer form selects and radio groups.
 */
public class TransferFormOption {

	private String value;

	private String label;

	private Integer receivingFacilityId;

	public TransferFormOption() {
	}

	public TransferFormOption(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public TransferFormOption(String value, String label, Integer receivingFacilityId) {
		this.value = value;
		this.label = label;
		this.receivingFacilityId = receivingFacilityId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getReceivingFacilityId() {
		return receivingFacilityId;
	}

	public void setReceivingFacilityId(Integer receivingFacilityId) {
		this.receivingFacilityId = receivingFacilityId;
	}

}
