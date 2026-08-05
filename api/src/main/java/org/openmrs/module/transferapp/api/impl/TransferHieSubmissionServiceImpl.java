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
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.TransferAdminService;
import org.openmrs.module.transferapp.api.TransferHieSubmissionService;
import org.openmrs.module.transferapp.api.TransferPatientSnapshotResolver;
import org.openmrs.module.transferapp.api.dao.TransferDao;
import org.openmrs.module.transferapp.hie.HieApiException;
import org.openmrs.module.transferapp.hie.HieBasicConnection;
import org.openmrs.module.transferapp.hie.HieConfigurationException;
import org.openmrs.module.transferapp.hie.HieConnectionResolver;
import org.openmrs.module.transferapp.hie.HieShrClient;
import org.openmrs.module.transferapp.hie.TransferEncounterPayloadBuilder;
import org.openmrs.module.transferapp.model.Transfer;

import java.util.Date;

public class TransferHieSubmissionServiceImpl implements TransferHieSubmissionService {

	private TransferDao transferDao;

	private TransferAdminService transferAdminService;

	private HieConnectionResolver hieConnectionResolver = new HieConnectionResolver();

	private HieShrClient hieShrClient = new HieShrClient();

	private TransferEncounterPayloadBuilder payloadBuilder = new TransferEncounterPayloadBuilder();

	private TransferPatientSnapshotResolver patientSnapshotResolver = new TransferPatientSnapshotResolver();

	public void setTransferDao(TransferDao transferDao) {
		this.transferDao = transferDao;
	}

	public void setTransferAdminService(TransferAdminService transferAdminService) {
		this.transferAdminService = transferAdminService;
	}

	@Override
	public Transfer submitTransferToHie(String transferUuid) {
		if (StringUtils.isBlank(transferUuid)) {
			throw new APIException("Transfer UUID is required");
		}

		Transfer transfer = transferDao.getTransferByUuid(transferUuid.trim());
		if (transfer == null) {
			throw new APIException("Transfer not found");
		}
		if (transfer.isSentToHie()) {
			throw new APIException("This transfer has already been sent to HIE");
		}

		try {
			refreshDiagnosisFromObsIfNeeded(transfer);
			HieBasicConnection connection = hieConnectionResolver.resolveConnection();
			String receivingFacilityLabel = resolveReceivingFacilityLabel(transfer);
			User currentUser = Context.getAuthenticatedUser();
			String encounterJson = payloadBuilder.buildEncounterJson(transfer, currentUser, receivingFacilityLabel);
			hieShrClient.postTransferEncounter(connection, encounterJson);

			transfer.setHieSent(true);
			transfer.setHieSentAt(new Date());
			transfer.setHieSendError(null);
			transfer.setChangedBy(currentUser);
			transfer.setDateChanged(new Date());
			return transferDao.saveTransfer(transfer);
		}
		catch (HieConfigurationException ex) {
			recordSubmissionFailure(transfer, ex.getMessage());
			throw ex;
		}
		catch (HieApiException ex) {
			recordSubmissionFailure(transfer, ex.getMessage());
			throw ex;
		}
		catch (RuntimeException ex) {
			recordSubmissionFailure(transfer, ex.getMessage());
			throw ex;
		}
	}

	private void refreshDiagnosisFromObsIfNeeded(Transfer transfer) {
		if (transfer == null || transfer.getPatient() == null) {
			return;
		}
		String current = StringUtils.trimToEmpty(transfer.getDiagnosis());
		boolean needsRefresh = StringUtils.isBlank(current)
				|| "Primary Diagnosis".equalsIgnoreCase(current)
				|| "Secondary Diagnosis".equalsIgnoreCase(current);
		if (!needsRefresh) {
			return;
		}
		String resolved = patientSnapshotResolver.resolveDiagnosis(transfer.getPatient());
		if (StringUtils.isNotBlank(resolved)) {
			transfer.setDiagnosis(resolved);
		} else if ("Primary Diagnosis".equalsIgnoreCase(current)
				|| "Secondary Diagnosis".equalsIgnoreCase(current)) {
			transfer.setDiagnosis(null);
		}
	}

	private void recordSubmissionFailure(Transfer transfer, String message) {
		transfer.setHieSent(false);
		transfer.setHieSendError(truncateMessage(message));
		transfer.setChangedBy(Context.getAuthenticatedUser());
		transfer.setDateChanged(new Date());
		transferDao.saveTransfer(transfer);
	}

	private String resolveReceivingFacilityLabel(Transfer transfer) {
		String facilityCode = transfer.getReceivingFacilityCode();
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

	private static String truncateMessage(String message) {
		if (message == null) {
			return null;
		}
		String trimmed = message.trim();
		if (trimmed.length() <= 500) {
			return trimmed;
		}
		return trimmed.substring(0, 497) + "...";
	}

}
