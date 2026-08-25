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
import org.openmrs.Patient;
import org.openmrs.module.transferapp.api.PatientTransferListService;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferService;
import org.openmrs.module.transferapp.model.PatientTransferListItem;
import org.openmrs.module.transferapp.model.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientTransferListServiceImpl implements PatientTransferListService {

	private TransferService transferService;

	private TransferAdminService transferAdminService;

	public void setTransferService(TransferService transferService) {
		this.transferService = transferService;
	}

	public void setTransferAdminService(TransferAdminService transferAdminService) {
		this.transferAdminService = transferAdminService;
	}

	@Override
	public List<PatientTransferListItem> getPatientTransfers(Patient patient) {
		return getPatientTransfers(patient, null);
	}

	@Override
	public List<PatientTransferListItem> getPatientTransfers(Patient patient, Integer limit) {
		List<Transfer> transfers = transferService.getTransfersByPatient(patient, limit);
		return mapTransferItems(transfers);
	}

	@Override
	public int countPatientTransfers(Patient patient) {
		return transferService.countTransfersByPatient(patient);
	}

	private List<PatientTransferListItem> mapTransferItems(List<Transfer> transfers) {
		if (transfers == null || transfers.isEmpty()) {
			return Collections.emptyList();
		}

		List<PatientTransferListItem> items = new ArrayList<PatientTransferListItem>();
		for (Transfer transfer : transfers) {
			PatientTransferListItem item = new PatientTransferListItem();
			item.setId(transfer.getUuid());
			item.setTransferDate(transfer.getDecisionToTransferAt() != null
					? transfer.getDecisionToTransferAt()
					: transfer.getDateCreated());
			// Destination only: receiving facility selected on the transfer form.
			item.setToFacility(resolveSelectedDestinationName(transfer.getReceivingFacilityCode()));
			item.setService(StringUtils.defaultString(transfer.getReceivingService()));
			item.setClientName(transfer.getClientName());
			item.setEmrId(transfer.getEmrId());
			item.setHieSent(transfer.isSentToHie());
			items.add(item);
		}
		return items;
	}

	/**
	 * Display name for the destination facility chosen on the form ({@code receivingFacilityCode}).
	 * Never uses sending / outbound facility name.
	 */
	private String resolveSelectedDestinationName(String receivingFacilityCode) {
		if (StringUtils.isBlank(receivingFacilityCode)) {
			return "";
		}
		String code = receivingFacilityCode.trim();
		if (transferAdminService == null) {
			return code;
		}
		Integer sendingLocationId = transferAdminService.resolveCurrentSendingLocationId();
		String name = transferAdminService.resolveReceivingFacilityName(sendingLocationId, code);
		return StringUtils.isNotBlank(name) ? name : code;
	}

}
