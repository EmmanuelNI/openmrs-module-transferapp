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
import org.openmrs.module.transferapp.api.FacilityTransferRecordsService;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.model.FacilityTransferRecordItem;
import org.openmrs.module.transferapp.model.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FacilityTransferRecordsServiceImpl implements FacilityTransferRecordsService {

	private TransferDao transferDao;

	private TransferAdminService transferAdminService;

	public void setTransferDao(TransferDao transferDao) {
		this.transferDao = transferDao;
	}

	public void setTransferAdminService(TransferAdminService transferAdminService) {
		this.transferAdminService = transferAdminService;
	}

	@Override
	public List<FacilityTransferRecordItem> getOutboundTransferRecords(Integer patientId, Date startDate, Date endDate,
			String receivingFacilityCode) {
		if (transferAdminService == null) {
			return Collections.emptyList();
		}

		String sendingFacility = transferAdminService.resolveCurrentSendingFacilityName();
		if (StringUtils.isBlank(sendingFacility)) {
			return Collections.emptyList();
		}

		List<Transfer> transfers = transferDao.getOutboundTransfersBySendingFacility(
				sendingFacility.trim(), patientId, null, startDate, endDate,
				StringUtils.trimToNull(receivingFacilityCode));
		if (transfers == null || transfers.isEmpty()) {
			return Collections.emptyList();
		}

		List<FacilityTransferRecordItem> items = new ArrayList<FacilityTransferRecordItem>();
		for (Transfer transfer : transfers) {
			items.add(toRecordItem(transfer));
		}
		return items;
	}

	private FacilityTransferRecordItem toRecordItem(Transfer transfer) {
		FacilityTransferRecordItem item = new FacilityTransferRecordItem();
		item.setId(transfer.getUuid());
		item.setTransferDate(transfer.getDecisionToTransferAt() != null
				? transfer.getDecisionToTransferAt()
				: transfer.getDateCreated());
		if (transfer.getPatient() != null) {
			item.setPatientId(transfer.getPatient().getPatientId());
		}
		item.setClientName(StringUtils.defaultString(transfer.getClientName()));
		item.setEmrId(StringUtils.defaultString(transfer.getEmrId()));
		item.setReceivingFacility(resolveReceivingFacilityLabel(transfer.getReceivingFacilityCode()));
		item.setService(StringUtils.defaultString(transfer.getReceivingService()));
		item.setHieSent(transfer.isSentToHie());
		return item;
	}

	private String resolveReceivingFacilityLabel(String facilityCode) {
		if (StringUtils.isBlank(facilityCode)) {
			return "";
		}
		if (transferAdminService != null) {
			Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
			String label = transferAdminService.resolveReceivingFacilityName(sendingLocationId, facilityCode);
			if (!facilityCode.equals(label)) {
				return label;
			}
		}
		return defaultFacilityLabel(facilityCode);
	}

	private static String defaultFacilityLabel(String facilityCode) {
		if ("KUTH".equals(facilityCode)) {
			return "Kigali University Teaching Hospital";
		}
		if ("RUHENGERI".equals(facilityCode)) {
			return "Ruhengeri District Hospital";
		}
		if ("BUTARO".equals(facilityCode)) {
			return "Butaro District Hospital";
		}
		if ("KFH".equals(facilityCode)) {
			return "King Faisal Hospital";
		}
		return facilityCode;
	}

}
