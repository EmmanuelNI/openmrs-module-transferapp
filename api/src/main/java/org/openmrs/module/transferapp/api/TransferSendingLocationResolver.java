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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
	 * Whether {@code transferapp.outboundFacilityName} is set (required before clinicians may start
	 * an outbound transfer form). Does not fall back to sending aliases or session location.
	 */
	public boolean isOutboundFacilityNameConfigured() {
		return readOutboundFacilityNameProperty() != null;
	}

	/**
	 * Normalized facility name for outbound HIE payloads ({@code transferapp.outboundFacilityName}).
	 * Falls back to the first {@code transferapp.sendingFacilityName} alias, then session location,
	 * only when the outbound GP is blank (callers that create transfers should require
	 * {@link #isOutboundFacilityNameConfigured()} first).
	 */
	public String resolveOutboundFacilityName() {
		String outbound = readOutboundFacilityNameProperty();
		if (outbound != null) {
			return outbound;
		}
		List<String> aliases = parseFacilityNames(readSendingFacilityNameProperty());
		if (!aliases.isEmpty()) {
			return aliases.get(0);
		}
		return resolveSessionLocationFacilityName();
	}

	/**
	 * Single facility name used for local outbound filters / form prefill / HIE submit.
	 * Delegates to {@link #resolveOutboundFacilityName()}.
	 */
	public String resolveCurrentSendingFacilityName() {
		return resolveOutboundFacilityName();
	}

	/**
	 * Facility aliases for inbound destination matching and pending targetOrg queries:
	 * {@code transferapp.sendingFacilityName} (comma-separated) plus outbound name when set.
	 */
	public List<String> resolveCurrentSendingFacilityNames() {
		Set<String> unique = new LinkedHashSet<String>();
		unique.addAll(parseFacilityNames(readSendingFacilityNameProperty()));
		String outbound = readOutboundFacilityNameProperty();
		if (outbound != null) {
			unique.add(outbound);
		}
		if (!unique.isEmpty()) {
			return new ArrayList<String>(unique);
		}
		String locationName = resolveSessionLocationFacilityName();
		if (locationName == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(locationName);
	}

	private String readOutboundFacilityNameProperty() {
		return StringUtils.trimToNull(
				Context.getAdministrationService().getGlobalProperty(TransferAppConstants.GP_OUTBOUND_FACILITY_NAME));
	}

	private String readSendingFacilityNameProperty() {
		return StringUtils.trimToNull(
				Context.getAdministrationService().getGlobalProperty(TransferAppConstants.GP_SENDING_FACILITY_NAME));
	}

	private String resolveSessionLocationFacilityName() {
		Location sessionLocation = Context.getUserContext().getLocation();
		if (sessionLocation == null) {
			return null;
		}
		String locationName = sessionLocation.getParentLocation() != null
				? sessionLocation.getParentLocation().getName()
				: sessionLocation.getName();
		return StringUtils.trimToNull(locationName);
	}

	/**
	 * Splits a comma-separated facility name GP into distinct trimmed aliases.
	 */
	public static List<String> parseFacilityNames(String raw) {
		if (StringUtils.isBlank(raw)) {
			return Collections.emptyList();
		}
		Set<String> unique = new LinkedHashSet<String>();
		String[] parts = raw.split(",");
		for (String part : parts) {
			String trimmed = StringUtils.trimToNull(part);
			if (trimmed != null) {
				unique.add(trimmed);
			}
		}
		return new ArrayList<String>(unique);
	}

}
