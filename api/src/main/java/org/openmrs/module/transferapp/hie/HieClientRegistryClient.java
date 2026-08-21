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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.transferapp.TransferAppConstants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * Posts FHIR Patient resources to HIE Client Registry ({@code /clientregistry/Patient}).
 */
public class HieClientRegistryClient {

	private static final Log log = LogFactory.getLog(HieClientRegistryClient.class);

	public ClientRegistryPatientCreateOutcome postPatientAllowingAlreadyExists(HieBasicConnection connection,
			String patientJson) {
		if (connection == null) {
			throw new HieConfigurationException("HIE connection is not configured");
		}
		if (StringUtils.isBlank(patientJson)) {
			throw new HieApiException("Client Registry patient payload is required");
		}

		String url = connection.getBaseUrl() + TransferAppConstants.HIE_CLIENT_REGISTRY_PATIENT_PATH;
		HttpURLConnection httpConnection = null;
		try {
			httpConnection = (HttpURLConnection) new URL(url).openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setConnectTimeout(30000);
			httpConnection.setReadTimeout(60000);
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty("Accept", "*/*");
			httpConnection.setRequestProperty("Content-Type", "application/json");

			String credentials = connection.getUsername() + ":" + connection.getPassword();
			String encoded = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"));
			httpConnection.setRequestProperty("Authorization", "Basic " + encoded);

			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(patientJson.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

			int statusCode = httpConnection.getResponseCode();
			String responseBody = readStream(
					statusCode >= 400 ? httpConnection.getErrorStream() : httpConnection.getInputStream());
			if (responseBody == null) {
				responseBody = "";
			}

			if (statusCode >= 200 && statusCode < 300) {
				log.info("Client Registry patient created successfully");
				return ClientRegistryPatientCreateOutcome.CREATED;
			}

			if (ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(statusCode, responseBody)) {
				log.info("Client Registry reports patient already exists; continuing transfer submit");
				return ClientRegistryPatientCreateOutcome.ALREADY_EXISTS;
			}

			throw new HieApiException("Client Registry patient create failed (" + statusCode + ") POST " + url
					+ ": " + responseBody);
		}
		catch (HieApiException ex) {
			if (ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(ex.getMessage())) {
				return ClientRegistryPatientCreateOutcome.ALREADY_EXISTS;
			}
			throw ex;
		}
		catch (Exception ex) {
			if (ClientRegistryCreateErrorClassifier.isPatientAlreadyExists(ex.getMessage())) {
				return ClientRegistryPatientCreateOutcome.ALREADY_EXISTS;
			}
			throw new HieApiException("Could not reach Client Registry POST " + url + ": " + ex.getMessage(), ex);
		}
		finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
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
