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
package org.openmrs.module.transferapp.api.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.transferapp.api.dao.TransferAdminDao;
import org.openmrs.module.transferapp.model.ReceivingFacility;
import org.openmrs.module.transferapp.model.ReceivingService;

import java.util.List;

public class HibernateTransferAdminDao implements TransferAdminDao {

	private DbSessionFactory sessionFactory;

	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ReceivingFacility> getReceivingFacilities(Integer sendingLocationId) {
		Criteria criteria = getSession().createCriteria(ReceivingFacility.class);
		criteria.add(Restrictions.eq("sendingLocationId", sendingLocationId));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.asc("facilityName"));
		return criteria.list();
	}

	@Override
	public ReceivingFacility getReceivingFacility(Integer receivingFacilityId) {
		return (ReceivingFacility) getSession().get(ReceivingFacility.class, receivingFacilityId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ReceivingService> getReceivingServicesByFacility(Integer receivingFacilityId) {
		Criteria criteria = getSession().createCriteria(ReceivingService.class);
		criteria.add(Restrictions.eq("receivingFacilityId", receivingFacilityId));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.asc("serviceName"));
		return criteria.list();
	}

	@Override
	public ReceivingService getReceivingService(Integer receivingServiceId) {
		return (ReceivingService) getSession().get(ReceivingService.class, receivingServiceId);
	}

	@Override
	public ReceivingFacility getReceivingFacilityByCode(Integer sendingLocationId, String facilityCode) {
		Criteria criteria = getSession().createCriteria(ReceivingFacility.class);
		criteria.add(Restrictions.eq("sendingLocationId", sendingLocationId));
		criteria.add(Restrictions.eq("facilityCode", facilityCode));
		criteria.add(Restrictions.eq("voided", false));
		return (ReceivingFacility) criteria.uniqueResult();
	}

	@Override
	public ReceivingFacility saveReceivingFacility(ReceivingFacility facility) {
		getSession().saveOrUpdate(facility);
		return facility;
	}

	@Override
	public ReceivingService saveReceivingService(ReceivingService service) {
		getSession().saveOrUpdate(service);
		return service;
	}

	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}

}
