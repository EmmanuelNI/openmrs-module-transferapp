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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;

import java.util.GregorianCalendar;

public class ClientRegistryPatientPayloadBuilderTest {

	@Test
	public void buildsAddressFromPersonAddressColumns() throws Exception {
		PersonAddress address = new PersonAddress();
		address.setCountry("Rwanda");
		address.setStateProvince("Northern Province/Amajyaruguru");
		address.setCountyDistrict("Burera");
		address.setCityVillage("Rugengabari");
		address.setAddress3("Gahinga");
		address.setAddress1("Mucaca");

		ClientRegistryPatientPayloadBuilder builder = new ClientRegistryPatientPayloadBuilder();
		JsonNode node = builder.buildAddress(address);

		assertEquals("Rwanda", text(node, "country"));
		assertEquals("Northern Province/Amajyaruguru", text(node, "state"));
		assertEquals("Burera", text(node, "district"));
		assertEquals("Rugengabari", text(node, "city"));

		JsonNode line = node.get("line");
		assertEquals(6, line.size());
		assertEquals("Country: Rwanda", line.get(0).getTextValue());
		assertEquals("Province: Northern Province/Amajyaruguru", line.get(1).getTextValue());
		assertEquals("District: Burera", line.get(2).getTextValue());
		assertEquals("Sector: Rugengabari", line.get(3).getTextValue());
		assertEquals("Cell: Gahinga", line.get(4).getTextValue());
		assertEquals("Village: Mucaca", line.get(5).getTextValue());
	}

	@Test
	public void buildsPatientJsonWithUpiAndDemographics() throws Exception {
		Patient patient = new Patient();
		PersonName name = new PersonName();
		name.setGivenName("Speciose");
		name.setFamilyName("NYIRABAHUNDE");
		name.setPreferred(true);
		patient.addName(name);
		patient.setGender("F");
		patient.setBirthdate(new GregorianCalendar(1986, 0, 1).getTime());

		PersonAddress address = new PersonAddress();
		address.setPreferred(true);
		address.setCountry("Rwanda");
		address.setStateProvince("Northern Province");
		address.setCountyDistrict("Burera");
		address.setCityVillage("Rugengabari");
		address.setAddress3("Gahinga");
		address.setAddress1("Mucaca");
		patient.addAddress(address);

		PatientIdentifierType upidType = new PatientIdentifierType();
		upidType.setName("UPID");
		PatientIdentifier upid = new PatientIdentifier("260519-328-7823", upidType, null);
		upid.setPreferred(true);
		patient.addIdentifier(upid);

		PatientIdentifierType nidType = new PatientIdentifierType();
		nidType.setName("National ID");
		patient.addIdentifier(new PatientIdentifier("1198670117406057", nidType, null));

		String json = new ClientRegistryPatientPayloadBuilder().buildPatientJson(patient, null);
		JsonNode root = new ObjectMapper().readTree(json);

		assertEquals("Patient", text(root, "resourceType"));
		assertEquals("260519-328-7823", text(root, "id"));
		assertEquals("female", text(root, "gender"));
		assertEquals("1986-01-01", text(root, "birthDate"));
		assertEquals("NYIRABAHUNDE", root.get("name").get(0).get("family").getTextValue());
		assertTrue(root.get("identifier").toString().contains("UPI"));
		assertTrue(root.get("identifier").toString().contains("NID"));
		assertEquals("Village: Mucaca",
				root.get("address").get(0).get("line").get(5).getTextValue());
	}

	private static String text(JsonNode node, String field) {
		JsonNode child = node.get(field);
		return child == null || child.isNull() ? null : child.getTextValue();
	}
}
