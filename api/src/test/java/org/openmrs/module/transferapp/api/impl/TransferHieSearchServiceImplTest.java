/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.transferapp.api.impl;

import org.junit.Test;
import org.openmrs.module.transferapp.hie.HieBasicConnection;
import org.openmrs.module.transferapp.hie.HieShrClient;
import org.openmrs.module.transferapp.hie.HieTransferResponsePage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TransferHieSearchServiceImplTest {

	@Test
	public void pendingWeekRangeDefaultsToOneWeekAndSupportsUpToFourWeeks() {
		LocalDate today = LocalDate.of(2026, 8, 18);

		assertEquals(1, TransferHieSearchServiceImpl.normalizePendingWeeks(0));
		assertEquals(1, TransferHieSearchServiceImpl.normalizePendingWeeks(1));
		assertEquals(2, TransferHieSearchServiceImpl.normalizePendingWeeks(2));
		assertEquals(4, TransferHieSearchServiceImpl.normalizePendingWeeks(4));
		assertEquals(1, TransferHieSearchServiceImpl.normalizePendingWeeks(5));
		assertEquals(today.minusDays(7), TransferHieSearchServiceImpl.calculatePendingFromDate(today, 1));
		assertEquals(today.minusDays(14), TransferHieSearchServiceImpl.calculatePendingFromDate(today, 2));
		assertEquals(today.minusDays(28), TransferHieSearchServiceImpl.calculatePendingFromDate(today, 4));
		assertEquals(today.minusDays(7), TransferHieSearchServiceImpl.calculatePendingFromDate(today, 99));
	}

	@Test
	public void fetchAllTransferPagesRequestsEveryHiePageUntilReportedTotalIsLoaded() throws Exception {
		HieBasicConnection connection = new HieBasicConnection("https://hie.example", "user", "password");
		HieShrClient client = mock(HieShrClient.class);
		String path = "/shr/Encounter/$list-transfers?targetOrg=Hospital&fromDate=2026-07-17&endDate=2026-08-15";
		when(client.get(connection, path + "&page=1&size=100"))
				.thenReturn(pageJson(1, true, 3, "enc-1", "enc-2"));
		when(client.get(connection, path + "&page=2&size=100"))
				.thenReturn(pageJson(2, false, 3, "enc-3"));

		TransferHieSearchServiceImpl service = new TransferHieSearchServiceImpl();
		service.setHieShrClient(client);
		List<Map<String, Object>> transfers = service.fetchAllTransferPages(connection, path);

		assertEquals(3, transfers.size());
		assertEquals("enc-1", transfers.get(0).get("id"));
		assertEquals("enc-3", transfers.get(2).get("id"));
		verify(client).get(connection, path + "&page=1&size=100");
		verify(client).get(connection, path + "&page=2&size=100");
		verifyNoMoreInteractions(client);
	}

	@Test
	public void shouldFetchNextPageUsesHasMoreTotalAndFullPageFallback() {
		HieTransferResponsePage hasMore = new HieTransferResponsePage(
				Collections.<Map<String, Object>>emptyList(), true, 1, 100, 200);
		HieTransferResponsePage totalReached = new HieTransferResponsePage(
				Collections.<Map<String, Object>>emptyList(), false, 2, 100, 150);
		HieTransferResponsePage noMetadata = new HieTransferResponsePage(
				Collections.<Map<String, Object>>emptyList(), false, null, null, null);

		assertTrue(TransferHieSearchServiceImpl.shouldFetchNextPage(hasMore, 100, 100));
		assertFalse(TransferHieSearchServiceImpl.shouldFetchNextPage(totalReached, 50, 150));
		assertTrue(TransferHieSearchServiceImpl.shouldFetchNextPage(noMetadata, 100, 100));
		assertFalse(TransferHieSearchServiceImpl.shouldFetchNextPage(noMetadata, 20, 20));
	}

	private String pageJson(int page, boolean hasMore, int total, String... encounterIds) {
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
