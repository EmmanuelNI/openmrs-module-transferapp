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
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.transferapp.api.dao.TransferProfileDao;
import org.openmrs.module.transferapp.model.TransferProfile;

public class HibernateTransferProfileDao implements TransferProfileDao {

	private DbSessionFactory sessionFactory;

	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public TransferProfile getTransferProfileByUserId(Integer userId) {
		if (userId == null) {
			return null;
		}
		Criteria criteria = getSession().createCriteria(TransferProfile.class);
		criteria.add(Restrictions.eq("userId", userId));
		criteria.add(Restrictions.eq("voided", false));
		return (TransferProfile) criteria.uniqueResult();
	}

	@Override
	public TransferProfile saveTransferProfile(TransferProfile profile) {
		getSession().saveOrUpdate(profile);
		return profile;
	}

	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}

}
