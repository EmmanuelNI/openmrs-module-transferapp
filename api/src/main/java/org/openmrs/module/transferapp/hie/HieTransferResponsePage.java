/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 */
package org.openmrs.module.transferapp.hie;

import java.util.List;
import java.util.Map;

/** A parsed page from the HIE {@code Encounter/$list-transfers} operation. */
public class HieTransferResponsePage {

	private final List<Map<String, Object>> transfers;

	private final boolean hasMore;

	private final Integer page;

	private final Integer size;

	private final Integer total;

	public HieTransferResponsePage(List<Map<String, Object>> transfers, boolean hasMore,
			Integer page, Integer size, Integer total) {
		this.transfers = transfers;
		this.hasMore = hasMore;
		this.page = page;
		this.size = size;
		this.total = total;
	}

	public List<Map<String, Object>> getTransfers() {
		return transfers;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public Integer getPage() {
		return page;
	}

	public Integer getSize() {
		return size;
	}

	public Integer getTotal() {
		return total;
	}
}
