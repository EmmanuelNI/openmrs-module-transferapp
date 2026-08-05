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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;
import org.openmrs.module.transferapp.api.TransferHieSearchService;
import org.openmrs.module.transferapp.api.TransferRegistrationObsService;
import org.openmrs.module.transferapp.api.TransferVerificationUrlService;
import org.openmrs.module.transferapp.api.TransferSendingLocationResolver;
import org.openmrs.module.transferapp.hie.HieApiException;
import org.openmrs.module.transferapp.hie.HieBasicConnection;
import org.openmrs.module.transferapp.hie.HieConnectionResolver;
import org.openmrs.module.transferapp.hie.HieShrClient;
import org.openmrs.module.transferapp.hie.HieTransferResponseParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransferHieSearchServiceImpl implements TransferHieSearchService {

	private static final Log log = LogFactory.getLog(TransferHieSearchServiceImpl.class);

	private HieConnectionResolver hieConnectionResolver = new HieConnectionResolver();

	private HieShrClient hieShrClient = new HieShrClient();

	private HieTransferResponseParser responseParser = new HieTransferResponseParser();

	private TransferSendingLocationResolver sendingLocationResolver = new TransferSendingLocationResolver();

	@Override
	public Map<String, Object> searchTransfers(String upid, String transferId, boolean activeOnly) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();

		if (StringUtils.isBlank(upid)) {
			result.put("status", "error");
			result.put("message", "UPID parameter is required");
			result.put("data", Collections.emptyList());
			return result;
		}

		if (!hieConnectionResolver.isHieConfigured()) {
			log.warn("HIE is not configured for transfer search");
			result.put("status", "error");
			result.put("message", "HIE is not enabled");
			result.put("data", Collections.emptyList());
			return result;
		}

		try {
			HieBasicConnection connection = hieConnectionResolver.resolveConnection();
			String fromDate = null;
			String endDate = null;
			if (activeOnly) {
				LocalDate today = LocalDate.now();
				fromDate = today.minusDays(28).format(DateTimeFormatter.ISO_LOCAL_DATE);
				endDate = today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
			}

			String pathWithQuery = buildPatientListTransfersPath(upid.trim(), fromDate, endDate);
			log.info("Requesting transfers from HIE: " + connection.getBaseUrl() + pathWithQuery);

			String responseJson = hieShrClient.get(connection, pathWithQuery);
			validateHieResponse(responseJson);

			List<Map<String, Object>> transfers = enrichTransfers(responseParser.parse(responseJson));
			if (StringUtils.isNotBlank(transferId)) {
				transfers = filterByTransferId(transfers, transferId.trim());
			}

			result.put("status", "success");
			result.put("data", transfers);
			return result;
		}
		catch (Exception ex) {
			log.error("Error fetching transfers from HIE for UPID: " + upid, ex);
			result.put("status", "error");
			result.put("message", ex.getMessage() != null ? ex.getMessage() : "Failed to fetch transfers from HIE");
			result.put("data", Collections.emptyList());
			return result;
		}
	}

	@Override
	public Map<String, Object> listPendingTransfersForCurrentFacility() {
		Map<String, Object> result = new LinkedHashMap<String, Object>();

		String targetOrg = sendingLocationResolver.resolveCurrentSendingFacilityName();
		if (StringUtils.isBlank(targetOrg)) {
			result.put("status", "error");
			result.put("message", "Current facility location is not available");
			result.put("targetOrg", "");
			result.put("data", Collections.emptyList());
			return result;
		}

		if (!hieConnectionResolver.isHieConfigured()) {
			log.warn("HIE is not configured for pending transfer list");
			result.put("status", "error");
			result.put("message", "HIE is not enabled");
			result.put("targetOrg", targetOrg.trim());
			result.put("data", Collections.emptyList());
			return result;
		}

		try {
			LocalDate today = LocalDate.now();
			String fromDate = today.minusDays(28).format(DateTimeFormatter.ISO_LOCAL_DATE);
			String endDate = today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

			HieBasicConnection connection = hieConnectionResolver.resolveConnection();
			String pathWithQuery = buildTargetOrgListTransfersPath(targetOrg.trim(), fromDate, endDate);
			log.info("Requesting pending transfers from HIE: " + connection.getBaseUrl() + pathWithQuery);

			String responseJson = hieShrClient.get(connection, pathWithQuery);
			validateHieResponse(responseJson);

			List<Map<String, Object>> transfers = enrichTransfers(responseParser.parse(responseJson));
			result.put("status", "success");
			result.put("targetOrg", targetOrg.trim());
			result.put("fromDate", fromDate);
			result.put("endDate", endDate);
			result.put("data", transfers);
			return result;
		}
		catch (Exception ex) {
			log.error("Error fetching pending transfers from HIE for targetOrg: " + targetOrg, ex);
			result.put("status", "error");
			result.put("message", ex.getMessage() != null ? ex.getMessage() : "Failed to fetch pending transfers from HIE");
			result.put("targetOrg", targetOrg.trim());
			result.put("data", Collections.emptyList());
			return result;
		}
	}

	private static String buildPatientListTransfersPath(String upid, String fromDate, String endDate)
			throws UnsupportedEncodingException {
		StringBuilder path = new StringBuilder(TransferAppConstants.HIE_LIST_TRANSFERS_PATH);
		path.append("?patient=").append(URLEncoder.encode(upid, "UTF-8"));
		if (fromDate != null && endDate != null) {
			path.append("&fromDate=").append(URLEncoder.encode(fromDate, "UTF-8"));
			path.append("&endDate=").append(URLEncoder.encode(endDate, "UTF-8"));
		}
		return path.toString();
	}

	private static String buildTargetOrgListTransfersPath(String targetOrg, String fromDate, String endDate)
			throws UnsupportedEncodingException {
		StringBuilder path = new StringBuilder(TransferAppConstants.HIE_LIST_TRANSFERS_PATH);
		path.append("?targetOrg=").append(URLEncoder.encode(targetOrg, "UTF-8"));
		path.append("&fromDate=").append(URLEncoder.encode(fromDate, "UTF-8"));
		path.append("&endDate=").append(URLEncoder.encode(endDate, "UTF-8"));
		return path.toString();
	}

	private static List<Map<String, Object>> enrichTransfers(List<Map<String, Object>> transfers) {
		if (transfers == null) {
			return Collections.emptyList();
		}
		TransferVerificationUrlService verificationUrlService = Context.getService(TransferVerificationUrlService.class);
		TransferRegistrationObsService registrationObsService = Context.getService(TransferRegistrationObsService.class);
		for (Map<String, Object> transfer : transfers) {
			if (transfer == null) {
				continue;
			}
			String uuid = asString(transfer.get("id"));
			String upid = asString(transfer.get("subject"));
			transfer.put("uuid", uuid);
			transfer.put("upid", upid);
			transfer.put("hieTransferId", uuid);
			transfer.put("receivedFromHie", Boolean.TRUE);
			String destination = TransferRegistrationObsServiceImpl.resolveDestination(transfer);
			transfer.put("destinationDisplay", destination);
			if (registrationObsService != null) {
				transfer.put("targetsCurrentFacility",
						registrationObsService.destinationMatchesCurrentFacility(destination));
			}
			else {
				transfer.put("targetsCurrentFacility", Boolean.FALSE);
			}
			if (verificationUrlService != null) {
				verificationUrlService.enrichPreviewVerificationFields(transfer);
			}
		}
		return transfers;
	}

	private static String asString(Object value) {
		return value == null ? "" : String.valueOf(value).trim();
	}

	private static void validateHieResponse(String responseJson) {
		if (responseJson == null || responseJson.trim().isEmpty()) {
			return;
		}
		if (responseJson.contains("\"resourceType\":\"OperationOutcome\"")) {
			String errorMessage = "HIE returned OperationOutcome error";
			if (responseJson.length() < 500) {
				errorMessage += ". Response: " + responseJson;
			}
			throw new HieApiException(errorMessage);
		}
	}

	private static List<Map<String, Object>> filterByTransferId(List<Map<String, Object>> transfers, String transferId) {
		List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> item : transfers) {
			Object idObj = item.get("id");
			if (idObj != null && transferId.equals(String.valueOf(idObj))) {
				filtered.add(item);
				break;
			}
		}
		return filtered;
	}

}
