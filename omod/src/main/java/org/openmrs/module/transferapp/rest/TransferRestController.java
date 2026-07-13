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
package org.openmrs.module.transferapp.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.TransferHieReceiveService;
import org.openmrs.module.transferapp.api.TransferHieSearchService;
import org.openmrs.module.transferapp.model.Transfer;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API endpoint for searching patient transfers received from HIE.
 */
@Controller("transferAppRestController")
public class TransferRestController {

	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = "/rest/v1/transferapp/transfer", method = RequestMethod.GET)
	@ResponseBody
	public Object getTransfer(HttpServletRequest request, HttpServletResponse response) throws ResponseException {
		String upid = request.getParameter("upid");

		if (upid == null || upid.trim().isEmpty()) {
			log.warn("Transfer REST API called without UPID parameter");
			throw new IllegalRequestException(
					"UPID parameter is required. Usage: /rest/v1/transferapp/transfer?upid=<patient-upid>[&activeOnly=true]");
		}

		upid = upid.trim();
		String transferId = request.getParameter("transferId");
		if (transferId != null) {
			transferId = transferId.trim();
			if (transferId.isEmpty()) {
				transferId = null;
			}
		}

		String activeOnlyParam = request.getParameter("activeOnly");
		boolean activeOnly = false;
		if (activeOnlyParam != null && !activeOnlyParam.trim().isEmpty()) {
			activeOnly = Boolean.parseBoolean(activeOnlyParam.trim());
		}

		log.info("Transfer REST API endpoint called for UPID: " + upid + ", activeOnly: " + activeOnly);

		Map<String, Object> searchResult = getTransferHieSearchService().searchTransfers(upid, transferId, activeOnly);
		return toSimpleObject(searchResult);
	}

	@RequestMapping(value = "/rest/v1/transferapp/transfer/receive", method = RequestMethod.POST)
	@ResponseBody
	public Object receiveTransfer(@RequestParam("patientId") Integer patientId,
			@RequestParam("hieTransferId") String hieTransferId) throws ResponseException {

		if (patientId == null) {
			throw new IllegalRequestException("patientId parameter is required");
		}
		if (hieTransferId == null || hieTransferId.trim().isEmpty()) {
			throw new IllegalRequestException("hieTransferId parameter is required");
		}

		SimpleObject result = new SimpleObject();
		try {
			Transfer transfer = getTransferHieReceiveService().receiveTransferFromHie(patientId, hieTransferId.trim());
			result.put("status", "success");
			result.put("uuid", transfer.getUuid());
			result.put("transferId", transfer.getTransferId());
			result.put("hieTransferId", transfer.getHieTransferId());
			result.put("message", "Transfer saved successfully");
		}
		catch (Exception ex) {
			log.error("Unable to store received transfer from HIE", ex);
			result.put("status", "error");
			result.put("message", ex.getMessage() != null ? ex.getMessage() : "Unable to store received transfer");
		}
		return result;
	}

	private TransferHieSearchService getTransferHieSearchService() {
		return Context.getService(TransferHieSearchService.class);
	}

	private TransferHieReceiveService getTransferHieReceiveService() {
		return Context.getService(TransferHieReceiveService.class);
	}

	@SuppressWarnings("unchecked")
	private SimpleObject toSimpleObject(Map<String, Object> source) {
		SimpleObject result = new SimpleObject();
		if (source == null) {
			return result;
		}
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if ("data".equals(key) && value instanceof List) {
				result.put(key, toSimpleObjectList((List<Map<String, Object>>) value));
			}
			else {
				result.put(key, value);
			}
		}
		return result;
	}

	private List<SimpleObject> toSimpleObjectList(List<Map<String, Object>> items) {
		List<SimpleObject> converted = new ArrayList<SimpleObject>();
		if (items == null) {
			return converted;
		}
		for (Map<String, Object> item : items) {
			SimpleObject row = new SimpleObject();
			if (item != null) {
				for (Map.Entry<String, Object> entry : item.entrySet()) {
					row.put(entry.getKey(), entry.getValue());
				}
			}
			converted.add(row);
		}
		return converted;
	}

}
