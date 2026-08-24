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
package org.openmrs.module.transferapp.sms;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Posts form-urlencoded messages to the InTouch SMS gateway (same API as eTransfer).
 * Named distinctly from rwandaemr's {@code labnotification.IntouchSmsClient} to avoid Spring bean clashes.
 */
public class TransferIntouchSmsClient {

	private static final Log log = LogFactory.getLog(TransferIntouchSmsClient.class);

	private IntouchSmsProperties properties = new IntouchSmsProperties();

	public void setProperties(IntouchSmsProperties properties) {
		this.properties = properties != null ? properties : new IntouchSmsProperties();
	}

	public IntouchSmsSendResult send(String to, String content) {
		if (!properties.isConfigured()) {
			return IntouchSmsSendResult.notConfigured(properties.configurationIssue());
		}
		if (StringUtils.isBlank(content)) {
			return IntouchSmsSendResult.skipped("SMS content is required.");
		}
		if (StringUtils.isBlank(to) || !RwandaPhoneNumberNormalizer.looksLikeRwandaMobile(to)) {
			return IntouchSmsSendResult.skipped("Recipient phone number is missing or invalid.");
		}

		String normalizedRecipient;
		try {
			normalizedRecipient = RwandaPhoneNumberNormalizer.normalize(to);
		}
		catch (IllegalArgumentException ex) {
			return IntouchSmsSendResult.skipped(ex.getMessage());
		}

		String url = properties.sendUrl();
		String formBody = buildFormBody(content.trim(), normalizedRecipient);
		log.debug("InTouch SMS request to=" + IntouchSmsLogSupport.maskPhoneNumber(normalizedRecipient)
				+ " from=" + properties.getSenderId() + " url=" + url);

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(60000);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			connection.setRequestProperty("Accept", "*/*");

			byte[] bytes = formBody.getBytes(StandardCharsets.UTF_8);
			connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(bytes);
			outputStream.flush();
			outputStream.close();

			int statusCode = connection.getResponseCode();
			String responseBody = readStream(
					statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
			if (responseBody == null) {
				responseBody = "";
			}

			String messageId = IntouchSmsResponseParser.parseMessageId(statusCode, responseBody);
			if (StringUtils.isNotBlank(messageId)) {
				log.info("InTouch SMS queued messageId=" + messageId
						+ " to=" + IntouchSmsLogSupport.maskPhoneNumber(normalizedRecipient));
				return IntouchSmsSendResult.success(statusCode, messageId, normalizedRecipient);
			}

			String error = IntouchSmsResponseParser.parseErrorMessage(statusCode, responseBody);
			log.warn("InTouch SMS failed HTTP " + statusCode
					+ " to=" + IntouchSmsLogSupport.maskPhoneNumber(normalizedRecipient)
					+ " error=" + IntouchSmsLogSupport.truncate(error, 500));
			return IntouchSmsSendResult.failure(statusCode, error, normalizedRecipient);
		}
		catch (Exception ex) {
			log.error("InTouch SMS call failed to="
					+ IntouchSmsLogSupport.maskPhoneNumber(normalizedRecipient)
					+ " url=" + url + ": " + ex.getMessage(), ex);
			return IntouchSmsSendResult.failure(0,
					"Could not reach InTouch SMS gateway: " + ex.getMessage(),
					normalizedRecipient);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String buildFormBody(String content, String normalizedRecipient) {
		Map<String, String> fields = new LinkedHashMap<String, String>();
		fields.put("username", properties.getUsername());
		fields.put("password", properties.getPassword());
		fields.put("to", normalizedRecipient);
		fields.put("from", properties.getSenderId());
		fields.put("content", content);
		fields.put("coding", String.valueOf(properties.getCoding()));
		fields.put("dlr-level", String.valueOf(properties.getDlrLevel()));
		if (StringUtils.isNotBlank(properties.getDlrUrl())) {
			fields.put("dlr-url", properties.getDlrUrl());
		}

		StringBuilder body = new StringBuilder();
		for (Map.Entry<String, String> entry : fields.entrySet()) {
			if (body.length() > 0) {
				body.append('&');
			}
			body.append(urlEncode(entry.getKey())).append('=').append(urlEncode(entry.getValue()));
		}
		return body.toString();
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value == null ? "" : value, "UTF-8");
		}
		catch (Exception ex) {
			return value == null ? "" : value;
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
