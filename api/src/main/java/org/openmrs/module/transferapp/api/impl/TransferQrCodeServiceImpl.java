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
import org.openmrs.module.transferapp.api.TransferQrCodeService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;

public class TransferQrCodeServiceImpl implements TransferQrCodeService {

	private static final int QR_SIZE_PX = 180;

	@Override
	public byte[] generatePng(String content) {
		if (StringUtils.isBlank(content)) {
			throw new IllegalArgumentException("QR content is required");
		}
		try {
			BitMatrix matrix = new QRCodeWriter().encode(content.trim(), BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", output);
			return output.toByteArray();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Could not generate QR code", ex);
		}
	}

}
