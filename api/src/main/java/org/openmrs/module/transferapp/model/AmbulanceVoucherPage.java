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
package org.openmrs.module.transferapp.model;

import java.util.Collections;
import java.util.List;

/**
 * Paginated ambulance voucher list for a selected month.
 */
public class AmbulanceVoucherPage {

	private List<AmbulanceVoucherItem> items = Collections.emptyList();

	private int totalCount;

	private int page;

	private int pageSize;

	private int totalPages;

	private String filterMonth;

	public List<AmbulanceVoucherItem> getItems() {
		return items;
	}

	public void setItems(List<AmbulanceVoucherItem> items) {
		this.items = items != null ? items : Collections.<AmbulanceVoucherItem>emptyList();
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public String getFilterMonth() {
		return filterMonth;
	}

	public void setFilterMonth(String filterMonth) {
		this.filterMonth = filterMonth;
	}

	public boolean isHasPrevious() {
		return page > 1;
	}

	public boolean isHasNext() {
		return totalPages > 0 && page < totalPages;
	}

}
