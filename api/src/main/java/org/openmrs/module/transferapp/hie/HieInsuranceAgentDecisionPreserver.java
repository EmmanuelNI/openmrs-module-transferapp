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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Iterator;

/**
 * When clinicians edit and resubmit an insurance-gated transfer, rebuild clinical content
 * from the local Transfer but keep insurance-agent decision extensions (and agent destination
 * redirect when present) from the existing HIE Encounter.
 */
public class HieInsuranceAgentDecisionPreserver {

	public static final String REQUIRES_VERIFICATION_URL =
			"http://example.org/fhir/StructureDefinition/requires-insurance-agent-verification";
	public static final String AGENT_APPROVED_URL =
			"http://example.org/fhir/StructureDefinition/agent-approved";
	public static final String AGENT_COMMENT_URL =
			"http://example.org/fhir/StructureDefinition/agent-comment";
	public static final String RECEIVING_PROVINCE_URL =
			"http://example.org/fhir/StructureDefinition/receiving-province";
	public static final String RECEIVING_DISTRICT_URL =
			"http://example.org/fhir/StructureDefinition/receiving-district";

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * @return true when the existing HIE encounter already carries an agent decision
	 *         ({@code agent-approved} extension is present).
	 */
	public boolean hasAgentDecision(String existingEncounterJson) {
		JsonNode encounter = unwrapEncounter(existingEncounterJson);
		return encounter != null && findExtension(encounter, AGENT_APPROVED_URL) != null;
	}

	/**
	 * Rebuilds {@code clinicalEncounterJson} so it keeps the same Encounter id and copies
	 * insurance-agent decision content from {@code existingEncounterJson}.
	 * Clinical fields from the rebuilt payload are kept; approver extensions (and destination
	 * when the agent has already decided) come from HIE.
	 *
	 * @param keepRequiresVerification when true and HIE has no requires-verification extension,
	 *        re-attach {@code requires-insurance-agent-verification=true} (external destination).
	 */
	public String mergePreservingAgentDecision(String clinicalEncounterJson, String existingEncounterJson,
			String encounterId, boolean keepRequiresVerification) {
		try {
			ObjectNode clinical = requireEncounterObject(clinicalEncounterJson);
			JsonNode existing = unwrapEncounter(existingEncounterJson);
			if (existing == null || !existing.isObject()) {
				throw new HieApiException("Existing HIE encounter could not be parsed for approval preservation");
			}

			if (StringUtils.isNotBlank(encounterId)) {
				clinical.put("id", encounterId.trim());
			}
			else {
				JsonNode existingId = existing.get("id");
				if (existingId != null && !existingId.isNull() && StringUtils.isNotBlank(existingId.getTextValue())) {
					clinical.put("id", existingId.getTextValue());
				}
			}

			boolean hasDecision = findExtension(existing, AGENT_APPROVED_URL) != null;
			removeDecisionExtensions(clinical);
			copyExtensionIfPresent(clinical, existing, REQUIRES_VERIFICATION_URL);
			copyExtensionIfPresent(clinical, existing, AGENT_APPROVED_URL);
			copyExtensionIfPresent(clinical, existing, AGENT_COMMENT_URL);

			if (keepRequiresVerification && findExtension(clinical, REQUIRES_VERIFICATION_URL) == null) {
				ObjectNode requires = extensionsArray(clinical).addObject();
				requires.put("url", REQUIRES_VERIFICATION_URL);
				requires.put("valueBoolean", true);
			}

			if (hasDecision) {
				// Agent may have redirected destination — keep that HIE destination, not clinician rebuild.
				copyHospitalizationDestination(clinical, (ObjectNode) existing);
				replaceStringExtensionFromExisting(clinical, existing, RECEIVING_PROVINCE_URL);
				replaceStringExtensionFromExisting(clinical, existing, RECEIVING_DISTRICT_URL);
			}

			return objectMapper.writeValueAsString(clinical);
		}
		catch (HieApiException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new HieApiException("Failed to preserve insurance agent decision on transfer update: "
					+ ex.getMessage(), ex);
		}
	}

	private ObjectNode requireEncounterObject(String json) throws Exception {
		JsonNode root = objectMapper.readTree(json);
		JsonNode encounter = unwrapEncounterNode(root);
		if (encounter == null || !encounter.isObject()) {
			throw new HieApiException("Clinical transfer payload is not a valid FHIR Encounter");
		}
		return (ObjectNode) encounter;
	}

	private JsonNode unwrapEncounter(String json) {
		if (StringUtils.isBlank(json)) {
			return null;
		}
		try {
			return unwrapEncounterNode(objectMapper.readTree(json));
		}
		catch (Exception ex) {
			return null;
		}
	}

	private JsonNode unwrapEncounterNode(JsonNode root) {
		if (root == null || root.isNull()) {
			return null;
		}
		if (root.isObject() && "Encounter".equals(text(root.get("resourceType")))) {
			return root;
		}
		// Parameters / Bundle wrappers occasionally returned by HIE
		JsonNode resource = root.get("resource");
		if (resource != null && resource.isObject() && "Encounter".equals(text(resource.get("resourceType")))) {
			return resource;
		}
		JsonNode entry = root.get("entry");
		if (entry != null && entry.isArray() && entry.size() > 0) {
			JsonNode first = entry.get(0);
			if (first != null && first.isObject()) {
				JsonNode nested = first.get("resource");
				if (nested != null && nested.isObject() && "Encounter".equals(text(nested.get("resourceType")))) {
					return nested;
				}
			}
		}
		return root.isObject() ? root : null;
	}

	private void copyHospitalizationDestination(ObjectNode clinical, ObjectNode existing) {
		JsonNode existingHospitalization = existing.get("hospitalization");
		if (existingHospitalization == null || !existingHospitalization.isObject()) {
			return;
		}
		JsonNode existingDestination = existingHospitalization.get("destination");
		if (existingDestination == null || existingDestination.isNull()) {
			return;
		}
		JsonNode hospitalizationNode = clinical.get("hospitalization");
		ObjectNode hospitalization = hospitalizationNode != null && hospitalizationNode.isObject()
				? (ObjectNode) hospitalizationNode
				: clinical.putObject("hospitalization");
		hospitalization.put("destination", existingDestination);
	}

	private void replaceStringExtensionFromExisting(ObjectNode clinical, JsonNode existing, String url) {
		JsonNode existingExt = findExtension(existing, url);
		removeExtensionByUrl(extensionsArray(clinical), url);
		if (existingExt != null && existingExt.isObject()) {
			extensionsArray(clinical).add(existingExt);
		}
	}

	private void copyExtensionIfPresent(ObjectNode clinical, JsonNode existing, String url) {
		JsonNode existingExt = findExtension(existing, url);
		if (existingExt != null && existingExt.isObject()) {
			extensionsArray(clinical).add(existingExt);
		}
	}

	private void removeDecisionExtensions(ObjectNode encounter) {
		ArrayNode extensions = extensionsArray(encounter);
		removeExtensionByUrl(extensions, REQUIRES_VERIFICATION_URL);
		removeExtensionByUrl(extensions, AGENT_APPROVED_URL);
		removeExtensionByUrl(extensions, AGENT_COMMENT_URL);
	}

	private ArrayNode extensionsArray(ObjectNode encounter) {
		JsonNode existing = encounter.get("extension");
		if (existing != null && existing.isArray()) {
			return (ArrayNode) existing;
		}
		return encounter.putArray("extension");
	}

	private void removeExtensionByUrl(ArrayNode extensions, String url) {
		if (extensions == null || StringUtils.isBlank(url)) {
			return;
		}
		for (int i = extensions.size() - 1; i >= 0; i--) {
			JsonNode ext = extensions.get(i);
			if (ext != null && ext.isObject() && urlMatches(text(ext.get("url")), url)) {
				extensions.remove(i);
			}
		}
	}

	private JsonNode findExtension(JsonNode encounter, String url) {
		if (encounter == null || !encounter.isObject()) {
			return null;
		}
		JsonNode extensions = encounter.get("extension");
		if (extensions == null || !extensions.isArray()) {
			return null;
		}
		Iterator<JsonNode> iterator = extensions.getElements();
		while (iterator.hasNext()) {
			JsonNode ext = iterator.next();
			if (ext != null && ext.isObject() && urlMatches(text(ext.get("url")), url)) {
				return ext;
			}
		}
		return null;
	}

	private static boolean urlMatches(String actual, String expected) {
		if (StringUtils.isBlank(actual) || StringUtils.isBlank(expected)) {
			return false;
		}
		String a = actual.trim();
		String e = expected.trim();
		if (a.equals(e)) {
			return true;
		}
		int slash = e.lastIndexOf('/');
		String suffix = slash >= 0 ? e.substring(slash + 1) : e;
		return a.endsWith("/" + suffix) || a.endsWith(suffix);
	}

	private static String text(JsonNode node) {
		if (node == null || node.isNull()) {
			return "";
		}
		String value = node.getTextValue();
		return value == null ? "" : value;
	}
}
