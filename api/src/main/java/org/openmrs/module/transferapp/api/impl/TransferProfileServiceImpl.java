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
import org.openmrs.module.transferapp.api.TransferProfileService;
import org.openmrs.module.transferapp.api.dao.TransferProfileDao;
import org.openmrs.module.transferapp.model.TransferProfile;

import java.util.Date;
import java.util.UUID;

public class TransferProfileServiceImpl implements TransferProfileService {

	private TransferProfileDao transferProfileDao;

	public void setTransferProfileDao(TransferProfileDao transferProfileDao) {
		this.transferProfileDao = transferProfileDao;
	}

	@Override
	public TransferProfile getProfileForUser(User user) {
		if (user == null || user.getUserId() == null) {
			return null;
		}
		return transferProfileDao.getTransferProfileByUserId(user.getUserId());
	}

	@Override
	public TransferProfile saveProfileForCurrentUser(String licenseNumber, String phoneNumber, String qualification) {
		User user = Context.getAuthenticatedUser();
		if (user == null || user.getUserId() == null) {
			throw new APIException("You must be logged in to save your profile");
		}

		String license = StringUtils.trimToNull(licenseNumber);
		String phone = StringUtils.trimToNull(phoneNumber);
		String qualificationValue = StringUtils.trimToNull(qualification);
		if (license == null) {
			throw new APIException("License number is required");
		}
		if (phone == null) {
			throw new APIException("Phone number is required");
		}
		if (qualificationValue == null) {
			throw new APIException("Qualification is required");
		}

		TransferProfile profile = transferProfileDao.getTransferProfileByUserId(user.getUserId());
		if (profile == null) {
			profile = new TransferProfile();
			profile.setUuid(UUID.randomUUID().toString());
			profile.setUserId(user.getUserId());
			profile.setCreator(user);
			profile.setDateCreated(new Date());
			profile.setVoided(false);
		}
		else {
			profile.setChangedBy(user);
			profile.setDateChanged(new Date());
		}

		profile.setLicenseNumber(license);
		profile.setPhoneNumber(phone);
		profile.setQualification(qualificationValue);
		return transferProfileDao.saveTransferProfile(profile);
	}

}
