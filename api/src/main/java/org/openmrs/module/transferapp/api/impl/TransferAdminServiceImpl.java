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
package org.openmrs.module.transferapp.api.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferSendingLocationResolver;
import org.openmrs.module.transferapp.api.dao.TransferAdminDao;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.ReceivingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TransferAdminServiceImpl implements TransferAdminService {

	private TransferAdminDao transferAdminDao;

	private LocationService locationService;

	private TransferSendingLocationResolver sendingLocationResolver = new TransferSendingLocationResolver();

	public void setTransferAdminDao(TransferAdminDao transferAdminDao) {
		this.transferAdminDao = transferAdminDao;
	}

	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}

	@Override
	public List<ReceivingFacility> getReceivingFacilities(Integer sendingLocationId) {
		requireSendingLocationId(sendingLocationId);
		return transferAdminDao.getReceivingFacilities(sendingLocationId);
	}

	@Override
	public List<ReceivingService> getReceivingServicesByFacility(Integer receivingFacilityId) {
		requireReceivingFacilityId(receivingFacilityId);
		return transferAdminDao.getReceivingServicesByFacility(receivingFacilityId);
	}

	@Override
	public List<String> getReceivingServiceNamesByFacility(Integer receivingFacilityId) {
		List<String> names = new ArrayList<String>();
		for (ReceivingService service : getReceivingServicesByFacility(receivingFacilityId)) {
			names.add(service.getServiceName());
		}
		return names;
	}

	@Override
	public ReceivingFacility getReceivingFacility(Integer receivingFacilityId) {
		if (receivingFacilityId == null) {
			return null;
		}
		ReceivingFacility facility = transferAdminDao.getReceivingFacility(receivingFacilityId);
		if (facility == null || facility.getVoided()) {
			return null;
		}
		return facility;
	}

	@Override
	public String resolveReceivingFacilityName(Integer sendingLocationId, String facilityCode) {
		if (StringUtils.isBlank(facilityCode)) {
			return "";
		}
		if (sendingLocationId == null) {
			return facilityCode;
		}
		ReceivingFacility facility = transferAdminDao.getReceivingFacilityByCode(sendingLocationId, facilityCode);
		if (facility != null) {
			return facility.getFacilityName();
		}
		return facilityCode;
	}

	@Override
	public ReceivingFacility getReceivingFacilityByCode(Integer sendingLocationId, String facilityCode) {
		if (sendingLocationId == null || StringUtils.isBlank(facilityCode)) {
			return null;
		}
		return transferAdminDao.getReceivingFacilityByCode(sendingLocationId, facilityCode);
	}

	@Override
	public void ensureReceivingServiceForFacility(Integer receivingFacilityId, String serviceName) {
		if (receivingFacilityId == null || StringUtils.isBlank(serviceName)) {
			return;
		}
		String name = StringUtils.trimToNull(serviceName);
		for (ReceivingService existing : getReceivingServicesByFacility(receivingFacilityId)) {
			if (name.equalsIgnoreCase(existing.getServiceName())) {
				return;
			}
		}
		saveReceivingService(receivingFacilityId, name);
	}

	@Override
	public ReceivingFacility saveReceivingFacility(Integer sendingLocationId, String facilityCode, String facilityName,
			Integer distance, String province, String district) {
		return saveReceivingFacility(sendingLocationId, facilityCode, facilityName, distance, province, district, false);
	}

	@Override
	public ReceivingFacility saveReceivingFacility(Integer sendingLocationId, String facilityCode, String facilityName,
			Integer distance, String province, String district, Boolean external) {
		requireSendingLocationId(sendingLocationId);
		String code = StringUtils.trimToNull(facilityCode);
		String name = StringUtils.trimToNull(facilityName);
		String provinceValue = StringUtils.trimToNull(province);
		String districtValue = StringUtils.trimToNull(district);
		boolean externalValue = Boolean.TRUE.equals(external);
		if (code == null) {
			throw new APIException("Facility code is required");
		}
		if (name == null) {
			throw new APIException("Facility name is required");
		}
		if (provinceValue == null) {
			throw new APIException("Province is required");
		}
		if (districtValue == null) {
			throw new APIException("District is required");
		}
		if (distance != null && distance < 0) {
			throw new APIException("Distance must be zero or greater");
		}

		ReceivingFacility existing = transferAdminDao.getReceivingFacilityByCode(sendingLocationId, code);
		if (existing != null) {
			existing.setFacilityName(name);
			existing.setDistance(distance);
			existing.setProvince(provinceValue);
			existing.setDistrict(districtValue);
			existing.setExternal(externalValue);
			existing.setChangedBy(Context.getAuthenticatedUser());
			existing.setDateChanged(new Date());
			return transferAdminDao.saveReceivingFacility(existing);
		}

		ReceivingFacility facility = new ReceivingFacility();
		facility.setUuid(UUID.randomUUID().toString());
		facility.setSendingLocationId(sendingLocationId);
		facility.setFacilityCode(code);
		facility.setFacilityName(name);
		facility.setDistance(distance);
		facility.setProvince(provinceValue);
		facility.setDistrict(districtValue);
		facility.setExternal(externalValue);
		facility.setCreator(requireAuthenticatedUser());
		facility.setDateCreated(new Date());
		facility.setVoided(false);
		return transferAdminDao.saveReceivingFacility(facility);
	}

	@Override
	public void voidReceivingFacility(Integer receivingFacilityId, String reason) {
		ReceivingFacility facility = transferAdminDao.getReceivingFacility(receivingFacilityId);
		if (facility == null || facility.getVoided()) {
			throw new APIException("Receiving facility not found");
		}
		for (ReceivingService service : transferAdminDao.getReceivingServicesByFacility(receivingFacilityId)) {
			voidReceivingService(service.getReceivingServiceId(), reason);
		}
		facility.setVoided(true);
		facility.setVoidedBy(Context.getAuthenticatedUser());
		facility.setDateVoided(new Date());
		facility.setVoidReason(StringUtils.defaultIfEmpty(reason, "Removed by admin"));
		transferAdminDao.saveReceivingFacility(facility);
	}

	@Override
	public ReceivingService saveReceivingService(Integer receivingFacilityId, String serviceName) {
		return saveReceivingService(receivingFacilityId, serviceName, null);
	}

	@Override
	public ReceivingService saveReceivingService(Integer receivingFacilityId, String serviceName,
			Integer receivingServiceId) {
		requireReceivingFacilityId(receivingFacilityId);
		ReceivingFacility facility = getReceivingFacility(receivingFacilityId);
		if (facility == null) {
			throw new APIException("Receiving facility not found");
		}

		String name = StringUtils.trimToNull(serviceName);
		if (name == null) {
			throw new APIException("Service name is required");
		}

		if (receivingServiceId != null) {
			ReceivingService service = transferAdminDao.getReceivingService(receivingServiceId);
			if (service == null || service.getVoided()) {
				throw new APIException("Receiving service not found");
			}
			if (!receivingFacilityId.equals(service.getReceivingFacilityId())) {
				throw new APIException("Receiving service does not belong to this facility");
			}
			for (ReceivingService existing : transferAdminDao.getReceivingServicesByFacility(receivingFacilityId)) {
				if (existing.getReceivingServiceId() != null
						&& existing.getReceivingServiceId().equals(receivingServiceId)) {
					continue;
				}
				if (name.equalsIgnoreCase(existing.getServiceName())) {
					throw new APIException("A service with this name already exists for the facility");
				}
			}
			service.setServiceName(name);
			service.setChangedBy(Context.getAuthenticatedUser());
			service.setDateChanged(new Date());
			return transferAdminDao.saveReceivingService(service);
		}

		for (ReceivingService existing : transferAdminDao.getReceivingServicesByFacility(receivingFacilityId)) {
			if (name.equalsIgnoreCase(existing.getServiceName())) {
				return existing;
			}
		}

		ReceivingService service = new ReceivingService();
		service.setUuid(UUID.randomUUID().toString());
		service.setReceivingFacilityId(receivingFacilityId);
		service.setServiceName(name);
		service.setCreator(requireAuthenticatedUser());
		service.setDateCreated(new Date());
		service.setVoided(false);
		return transferAdminDao.saveReceivingService(service);
	}

	@Override
	public void voidReceivingService(Integer receivingServiceId, String reason) {
		ReceivingService service = transferAdminDao.getReceivingService(receivingServiceId);
		if (service == null || service.getVoided()) {
			throw new APIException("Receiving service not found");
		}
		service.setVoided(true);
		service.setVoidedBy(Context.getAuthenticatedUser());
		service.setDateVoided(new Date());
		service.setVoidReason(StringUtils.defaultIfEmpty(reason, "Removed by admin"));
		transferAdminDao.saveReceivingService(service);
	}

	@Override
	public List<Location> getSendingLocations() {
		List<Location> locations = locationService.getAllLocations(false);
		List<Location> sorted = new ArrayList<Location>(locations);
		Collections.sort(sorted, new Comparator<Location>() {
			@Override
			public int compare(Location left, Location right) {
				return StringUtils.defaultString(left.getName())
						.compareToIgnoreCase(StringUtils.defaultString(right.getName()));
			}
		});
		return sorted;
	}

	@Override
	public Integer resolveCurrentSendingLocationId() {
		return sendingLocationResolver.resolveCurrentSendingLocationId();
	}

	@Override
	public String resolveCurrentSendingFacilityName() {
		return sendingLocationResolver.resolveCurrentSendingFacilityName();
	}

	private void requireSendingLocationId(Integer sendingLocationId) {
		if (sendingLocationId == null) {
			throw new APIException("Sending facility is required");
		}
	}

	private void requireReceivingFacilityId(Integer receivingFacilityId) {
		if (receivingFacilityId == null) {
			throw new APIException("Receiving facility is required");
		}
	}

	private User requireAuthenticatedUser() {
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			throw new APIException("You must be logged in to save transfer settings");
		}
		return user;
	}

}
