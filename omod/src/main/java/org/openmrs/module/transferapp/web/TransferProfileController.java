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
package org.openmrs.module.transferapp.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.api.TransferProfileService;
import org.openmrs.module.transferapp.model.TransferProfile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TransferProfileController {

	private static final Log log = LogFactory.getLog(TransferProfileController.class);

	private TransferProfileService getTransferProfileService() {
		return Context.getService(TransferProfileService.class);
	}

	@RequestMapping(value = "/module/transferapp/profile/save.form", method = RequestMethod.POST)
	public void saveProfile(HttpServletResponse response,
			@RequestParam("licenseNumber") String licenseNumber,
			@RequestParam("phoneNumber") String phoneNumber,
			@RequestParam("qualification") String qualification,
			@RequestParam(value = "speciality", required = false) String speciality) throws Exception {

		Map<String, Object> data = new HashMap<String, Object>();
		try {
			TransferProfile profile = getTransferProfileService().saveProfileForCurrentUser(
					licenseNumber, phoneNumber, qualification, speciality);
			data.put("status", "success");
			data.put("licenseNumber", profile.getLicenseNumber());
			data.put("phoneNumber", profile.getPhoneNumber());
			data.put("qualification", profile.getQualification());
			data.put("speciality", profile.getSpeciality());
			data.put("qualificationDisplay", profile.getQualificationWithSpeciality());
		}
		catch (Exception e) {
			log.error("Unable to save transfer profile", e);
			data.put("status", "error");
			data.put("message", resolveErrorMessage(e, "Unable to save profile"));
		}
		writeJson(response, data);
	}

	private String resolveErrorMessage(Exception exception, String fallback) {
		Throwable current = exception;
		while (current != null) {
			if (current.getMessage() != null && current.getMessage().trim().length() > 0) {
				return current.getMessage();
			}
			current = current.getCause();
		}
		return fallback;
	}

	private void writeJson(HttpServletResponse response, Map<String, Object> data) throws Exception {
		response.setContentType("application/json");
		new ObjectMapper().writeValue(response.getOutputStream(), data);
	}

}
