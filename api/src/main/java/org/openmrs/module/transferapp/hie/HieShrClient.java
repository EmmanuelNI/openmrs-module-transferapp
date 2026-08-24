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
package org.openmrs.module.transferapp.hie;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.transferapp.TransferAppConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

public class HieShrClient {

	public String get(HieBasicConnection connection, String pathWithQuery) {
		return get(connection, pathWithQuery, null);
	}

	public String get(HieBasicConnection connection, String pathWithQuery, String authToken) {
		return executeRequest(connection, "GET", pathWithQuery, null, authToken);
	}

	public void postTransferEncounter(HieBasicConnection connection, String encounterJson) {
		executeRequest(connection, "POST", TransferAppConstants.HIE_TRANSFER_ENCOUNTER_PATH, encounterJson, null);
	}

	/**
	 * Same endpoint as create — HIE upserts by Encounter.id (mirrors eTransfer updateTransferEncounter).
	 */
	public void updateTransferEncounter(HieBasicConnection connection, String encounterJson) {
		postTransferEncounter(connection, encounterJson);
	}

	/**
	 * {@code GET /shr/Encounter/{id}}. Returns null when the encounter is not found (HTTP 404).
	 */
	public String fetchEncounterById(HieBasicConnection connection, String encounterId) {
		if (connection == null || StringUtils.isBlank(encounterId)) {
			return null;
		}
		String path = TransferAppConstants.HIE_ENCOUNTER_BY_ID_PATH + encodePathSegment(encounterId.trim());
		try {
			return executeRequest(connection, "GET", path, null, null);
		}
		catch (HieApiException ex) {
			if (isNotFound(ex)) {
				return null;
			}
			throw ex;
		}
	}

	private String executeRequest(HieBasicConnection connection, String method, String pathWithQuery, String requestBody,
			String authToken) {
		String url = connection.getBaseUrl() + pathWithQuery;
		HttpURLConnection httpConnection = null;
		try {
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod(method);
			httpConnection.setConnectTimeout(30000);
			httpConnection.setReadTimeout(60000);

			// Facility registry (X-Auth-Token) matches etransfer: Accept */*, Basic auth, X-Auth-Token.
			// Other HIE calls keep application/json.
			boolean facilityRegistryRequest = authToken != null && authToken.trim().length() > 0;
			httpConnection.setRequestProperty("Accept", facilityRegistryRequest ? "*/*" : "application/json");

			String credentials = connection.getUsername() + ":" + connection.getPassword();
			String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"));
			httpConnection.setRequestProperty("Authorization", "Basic " + encoded);

			if (facilityRegistryRequest) {
				httpConnection.setRequestProperty(TransferAppConstants.HIE_AUTH_TOKEN_HEADER, authToken.trim());
			}

			if ("POST".equals(method)) {
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty("Content-Type", "application/json");
				OutputStream outputStream = httpConnection.getOutputStream();
				outputStream.write(requestBody.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();
			}

			int statusCode = httpConnection.getResponseCode();
			String responseBody = readStream(statusCode >= 400 ? httpConnection.getErrorStream() : httpConnection.getInputStream());
			if (responseBody == null) {
				responseBody = "";
			}

			if (statusCode >= 200 && statusCode < 300) {
				return responseBody;
			}

			throw new HieApiException("HIE request failed (" + statusCode + ") " + method + " " + url + ": " + responseBody);
		}
		catch (HieApiException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new HieApiException("Could not reach HIE endpoint " + method + " " + url + ": " + ex.getMessage(), ex);
		}
		finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
	}

	private static boolean isNotFound(HieApiException ex) {
		if (ex == null || ex.getMessage() == null) {
			return false;
		}
		String message = ex.getMessage();
		return message.contains("(404)") || message.contains(" 404 ");
	}

	private static String encodePathSegment(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8").replace("+", "%20");
		}
		catch (Exception ex) {
			return value;
		}
	}

	private static String readStream(InputStream inputStream) throws Exception {
		if (inputStream == null) {
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

}
