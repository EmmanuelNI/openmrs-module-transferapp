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

import org.openmrs.annotation.Authorized;
import org.openmrs.module.transferapp.TransferAppActivator;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional
public interface TransferHieSearchService {

	@Authorized(value = {
			TransferAppActivator.PRIVILEGE_LIST_TRANSFERS,
			TransferAppActivator.PRIVILEGE_LIST_PENDING }, requireAll = false)
	@Transactional(readOnly = true)
	Map<String, Object> searchTransfers(String upid, String transferId, boolean activeOnly);

	/**
	 * Lists pending inbound transfers for the current parent location from HIE.
	 * Uses targetOrg=parent location name, fromDate=today-28, endDate=today+1.
	 */
	@Authorized(TransferAppActivator.PRIVILEGE_LIST_PENDING)
	@Transactional(readOnly = true)
	Map<String, Object> listPendingTransfersForCurrentFacility();

}
