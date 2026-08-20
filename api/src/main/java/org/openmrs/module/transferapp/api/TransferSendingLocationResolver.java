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
package org.openmrs.module.transferapp.api;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.transferapp.TransferAppConstants;

public class TransferSendingLocationResolver {

	public Integer resolveCurrentSendingLocationId() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation == null) {
			return null;
		}
		if (sessionLocation.getParentLocation() != null) {
			return sessionLocation.getParentLocation().getLocationId();
		}
		return sessionLocation.getLocationId();
	}

	/**
	 * Prefer {@link TransferAppConstants#GP_SENDING_FACILITY_NAME} when set, so the HIE facility
	 * name can be configured without relying on the OpenMRS location hierarchy. Falls back to the
	 * session location's parent name (or the location itself when it has no parent).
	 */
	public String resolveCurrentSendingFacilityName() {
		String configuredName = StringUtils.trimToNull(
				Context.getAdministrationService().getGlobalProperty(TransferAppConstants.GP_SENDING_FACILITY_NAME));
		if (configuredName != null) {
			return configuredName;
		}

		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation == null) {
			return null;
		}
		if (sessionLocation.getParentLocation() != null) {
			return sessionLocation.getParentLocation().getName();
		}
		return sessionLocation.getName();
	}

}
