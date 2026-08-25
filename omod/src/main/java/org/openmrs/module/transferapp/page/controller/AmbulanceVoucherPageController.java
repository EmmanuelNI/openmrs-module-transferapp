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
package org.openmrs.module.transferapp.page.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.openmrs.module.transferapp.TransferPrivilegeHelper;
import org.openmrs.module.transferapp.api.TransferAmbulanceVoucherService;
import org.openmrs.module.transferapp.model.AmbulanceVoucherPage;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class AmbulanceVoucherPageController {

	private static final int MAX_DATE_RANGE_MONTHS = 3;

	private static final String DATE_PARAM_PATTERN = "yyyy-MM-dd";

	public void get(UiSessionContext sessionContext,
			PageModel model,
			@SpringBean("transferAmbulanceVoucherService") TransferAmbulanceVoucherService transferAmbulanceVoucherService,
			@RequestParam(value = "startDate", required = false) String startDateParam,
			@RequestParam(value = "endDate", required = false) String endDateParam,
			@RequestParam(value = "app", required = false) String app) {

		sessionContext.requireAuthentication();

		boolean canListTransfers = TransferPrivilegeHelper.hasPrivilege(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
		String listAccessDeniedMessage = null;
		AmbulanceVoucherPage voucherPage = new AmbulanceVoucherPage();

		Date defaultEndDate = endOfDay(new Date());
		Date defaultStartDate = startOfDay(startOfMonth(defaultEndDate));
		Date startDate = parseDateParam(startDateParam, defaultStartDate);
		Date endDate = parseDateParam(endDateParam, defaultEndDate);
		Date[] normalizedRange = normalizeDateRange(startDate, endDate, MAX_DATE_RANGE_MONTHS);
		startDate = normalizedRange[0];
		endDate = normalizedRange[1];

		if (canListTransfers) {
			try {
				voucherPage = transferAmbulanceVoucherService.getVouchers(startDate, endDate);
			}
			catch (Exception ex) {
				canListTransfers = false;
				listAccessDeniedMessage = TransferPrivilegeHelper.resolveUserFacingMessage(
						ex,
						TransferAppActivator.PRIVILEGE_LIST_TRANSFERS,
						TransferPrivilegeHelper.requiredPrivilegeMessage(TransferAppActivator.PRIVILEGE_LIST_TRANSFERS));
				voucherPage = new AmbulanceVoucherPage();
				voucherPage.setItems(Collections.<org.openmrs.module.transferapp.model.AmbulanceVoucherItem>emptyList());
			}
		}
		else {
			listAccessDeniedMessage = TransferPrivilegeHelper.requiredPrivilegeMessage(
					TransferAppActivator.PRIVILEGE_LIST_TRANSFERS);
			voucherPage.setItems(Collections.<org.openmrs.module.transferapp.model.AmbulanceVoucherItem>emptyList());
		}

		model.addAttribute("canListTransfers", canListTransfers);
		model.addAttribute("listAccessDeniedMessage", listAccessDeniedMessage);
		model.addAttribute("vouchers", voucherPage.getItems());
		model.addAttribute("hasVouchers", voucherPage.getItems() != null && !voucherPage.getItems().isEmpty());
		model.addAttribute("totalCount", voucherPage.getTotalCount());
		model.addAttribute("filterStartDate", formatDateParam(startDate));
		model.addAttribute("filterEndDate", formatDateParam(endDate));
		model.addAttribute("maxDateRangeMonths", MAX_DATE_RANGE_MONTHS);
		model.addAttribute("appId", StringUtils.isNotBlank(app) ? app.trim() : "transferapp.dashboard");
	}

	static Date[] normalizeDateRange(Date startDate, Date endDate, int maxMonths) {
		Date normalizedStart = startOfDay(startDate != null ? startDate : new Date());
		Date normalizedEnd = endOfDay(endDate != null ? endDate : new Date());
		if (normalizedStart.after(normalizedEnd)) {
			Date swap = normalizedStart;
			normalizedStart = startOfDay(normalizedEnd);
			normalizedEnd = endOfDay(swap);
		}
		Date earliestAllowedStart = startOfDay(addMonths(normalizedEnd, -maxMonths));
		if (normalizedStart.before(earliestAllowedStart)) {
			normalizedStart = earliestAllowedStart;
		}
		return new Date[] { normalizedStart, normalizedEnd };
	}

	private static Date parseDateParam(String value, Date defaultValue) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_PARAM_PATTERN);
			formatter.setLenient(false);
			return formatter.parse(value.trim());
		}
		catch (ParseException ex) {
			return defaultValue;
		}
	}

	private static String formatDateParam(Date date) {
		if (date == null) {
			return "";
		}
		return new SimpleDateFormat(DATE_PARAM_PATTERN).format(date);
	}

	private static Date startOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return startOfDay(calendar.getTime());
	}

	private static Date startOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	private static Date endOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}

	private static Date addMonths(Date date, int months) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, months);
		return calendar.getTime();
	}

}
