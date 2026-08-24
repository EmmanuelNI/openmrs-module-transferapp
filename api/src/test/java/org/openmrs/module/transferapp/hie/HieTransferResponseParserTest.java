/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.transferapp.hie;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HieTransferResponseParserTest {

	@Test
	public void parsePageReadsTransfersAndPaginationMetadata() throws Exception {
		HieTransferResponsePage page = new HieTransferResponseParser().parsePage(pageJson(2, true, 250,
				"enc-101", "enc-102"));

		assertEquals(2, page.getTransfers().size());
		assertEquals("enc-101", page.getTransfers().get(0).get("id"));
		assertTrue(page.hasMore());
		assertEquals(Integer.valueOf(2), page.getPage());
		assertEquals(Integer.valueOf(2), page.getSize());
		assertEquals(Integer.valueOf(250), page.getTotal());
	}

	@Test
	public void parsePageUsesBundleTotalWhenParameterTotalIsMissing() throws Exception {
		String json = pageJson(1, false, 1, "enc-1")
				.replace(",{\"name\":\"total\",\"valueInteger\":1}", "");

		HieTransferResponsePage page = new HieTransferResponseParser().parsePage(json);

		assertFalse(page.hasMore());
		assertEquals(Integer.valueOf(1), page.getTotal());
	}

	@Test
	public void mapsResponseTwoPreviewFallbacks() throws Exception {
		java.io.File jsonFile = new java.io.File("../devs/response_two.json");
		if (!jsonFile.exists()) {
			jsonFile = new java.io.File("devs/response_two.json");
		}
		assertTrue("response_two.json should exist for preview fallback mapping", jsonFile.exists());

		String json = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()), "UTF-8");
		java.util.Map<String, Object> transfer = new HieTransferResponseParser().parse(json).get(0);

		assertEquals("20 (2006-01-22)", transfer.get("ageDob"));
		assertEquals("N/A", transfer.get("clientTelephone"));
		assertEquals("opd", transfer.get("receivingService"));
		assertEquals("NA", transfer.get("others"));
		assertEquals("27", transfer.get("muac"));
		assertEquals("No drug prescriptions", transfer.get("proceduresAndTreatments"));
		assertEquals("", transfer.get("caregiverName"));
		assertEquals("", transfer.get("providerPhone"));
		assertTrue(String.valueOf(transfer.get("referringProviderName")).contains("HLC-PRAC-2024-00074"));
	}

	static String pageJson(int page, boolean hasMore, int total, String... encounterIds) {
		StringBuilder entries = new StringBuilder();
		for (String encounterId : encounterIds) {
			if (entries.length() > 0) {
				entries.append(',');
			}
			entries.append("{\"resource\":{\"resourceType\":\"Encounter\",\"id\":\"")
					.append(encounterId)
					.append("\",\"status\":\"planned\"}}");
		}
		return "{\"resourceType\":\"Parameters\",\"parameter\":["
				+ "{\"name\":\"bundle\",\"resource\":{\"resourceType\":\"Bundle\",\"total\":" + total
				+ ",\"entry\":[" + entries + "]}},"
				+ "{\"name\":\"hasMore\",\"valueBoolean\":" + hasMore + "},"
				+ "{\"name\":\"page\",\"valueInteger\":" + page + "},"
				+ "{\"name\":\"size\",\"valueInteger\":" + encounterIds.length + "},"
				+ "{\"name\":\"total\",\"valueInteger\":" + total + "}]}";
	}
}
