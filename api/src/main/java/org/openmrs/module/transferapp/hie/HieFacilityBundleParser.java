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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.transferapp.model.RegistryFacility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses FHIR Bundle responses from the HIE facility registry.
 */
public class HieFacilityBundleParser {

	private static final String FACILITY_CODE_SYSTEM = "urn:frpr:facility-code";

	private static final String ORG_CATEGORY_EXTENSION = "urn:frpr:org-category";

	public List<RegistryFacility> parse(String json) {
		if (json == null || json.trim().isEmpty()) {
			return new ArrayList<RegistryFacility>();
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(json);
			JsonNode entries = root.get("entry");
			if (entries == null || !entries.isArray()) {
				return new ArrayList<RegistryFacility>();
			}

			List<RegistryFacility> facilities = new ArrayList<RegistryFacility>();
			Iterator<JsonNode> iterator = entries.getElements();
			while (iterator.hasNext()) {
				JsonNode entry = iterator.next();
				RegistryFacility facility = parseResource(entry.get("resource"));
				if (facility != null) {
					facilities.add(facility);
				}
			}
			return facilities;
		}
		catch (Exception ex) {
			throw new HieApiException("Failed to parse HIE facility registry response", ex);
		}
	}

	private RegistryFacility parseResource(JsonNode resource) {
		if (resource == null || resource.isNull()) {
			return null;
		}

		String code = findIdentifierValue(resource.get("identifier"), FACILITY_CODE_SYSTEM);
		if (code == null || code.trim().isEmpty()) {
			return null;
		}

		String name = textValue(resource.get("name"));
		if (name == null || name.trim().isEmpty()) {
			return null;
		}

		// Category comes from urn:frpr:org-category; facilities without it are excluded upstream.
		String category = findExtensionValueString(resource.get("extension"), ORG_CATEGORY_EXTENSION);
		if (category == null || category.trim().isEmpty()) {
			return null;
		}

		RegistryFacility facility = new RegistryFacility();
		facility.setCode(code.trim());
		facility.setName(name.trim());
		facility.setCategory(category.trim());
		return facility;
	}

	private static String findIdentifierValue(JsonNode identifiers, String system) {
		if (identifiers == null || !identifiers.isArray()) {
			return null;
		}
		Iterator<JsonNode> iterator = identifiers.getElements();
		while (iterator.hasNext()) {
			JsonNode identifier = iterator.next();
			if (system.equals(textValue(identifier.get("system")))) {
				return textValue(identifier.get("value"));
			}
		}
		return null;
	}

	private static String findExtensionValueString(JsonNode extensions, String url) {
		if (extensions == null || !extensions.isArray()) {
			return null;
		}
		Iterator<JsonNode> iterator = extensions.getElements();
		while (iterator.hasNext()) {
			JsonNode extension = iterator.next();
			if (url.equals(textValue(extension.get("url")))) {
				return textValue(extension.get("valueString"));
			}
		}
		return null;
	}

	private static String textValue(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		return node.getTextValue();
	}

}
