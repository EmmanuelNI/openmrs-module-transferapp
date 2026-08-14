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
package org.openmrs.module.transferapp.api;

import org.openmrs.Location;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.ReceivingService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface TransferAdminService {

	@Transactional(readOnly = true)
	List<ReceivingFacility> getReceivingFacilities(Integer sendingLocationId);

	@Transactional(readOnly = true)
	List<ReceivingService> getReceivingServicesByFacility(Integer receivingFacilityId);

	@Transactional(readOnly = true)
	List<String> getReceivingServiceNamesByFacility(Integer receivingFacilityId);

	@Transactional(readOnly = true)
	ReceivingFacility getReceivingFacility(Integer receivingFacilityId);

	@Transactional(readOnly = true)
	String resolveReceivingFacilityName(Integer sendingLocationId, String facilityCode);

	@Transactional(readOnly = true)
	ReceivingFacility getReceivingFacilityByCode(Integer sendingLocationId, String facilityCode);

	void ensureReceivingServiceForFacility(Integer receivingFacilityId, String serviceName);

	ReceivingFacility saveReceivingFacility(Integer sendingLocationId, String facilityCode, String facilityName,
			Integer distance, String province, String district);

	ReceivingFacility saveReceivingFacility(Integer sendingLocationId, String facilityCode, String facilityName,
			Integer distance, String province, String district, Boolean external);

	void voidReceivingFacility(Integer receivingFacilityId, String reason);

	ReceivingService saveReceivingService(Integer receivingFacilityId, String serviceName);

	ReceivingService saveReceivingService(Integer receivingFacilityId, String serviceName, Integer receivingServiceId);

	void voidReceivingService(Integer receivingServiceId, String reason);

	@Transactional(readOnly = true)
	List<Location> getSendingLocations();

	@Transactional(readOnly = true)
	Integer resolveCurrentSendingLocationId();

	@Transactional(readOnly = true)
	String resolveCurrentSendingFacilityName();

}
