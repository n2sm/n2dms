/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2013  Paco Avila & Josep Llort
 *
 *  No bytes were intentionally harmed during the development of this application.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.dao;

import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.bean.RegisteredPropertyGroup;

public class RegisteredPropertyGroupDAO extends
        GenericDAO<RegisteredPropertyGroup, String> {
    private static Logger log = LoggerFactory
            .getLogger(RegisteredPropertyGroupDAO.class);

    private static RegisteredPropertyGroupDAO single = new RegisteredPropertyGroupDAO();

    private RegisteredPropertyGroupDAO() {
    }

    public static RegisteredPropertyGroupDAO getInstance() {
        return single;
    }

    /**
     * Create or update
     */
    public void createOrUpdate(final RegisteredPropertyGroup rpg)
            throws DatabaseException {
        log.debug("create({})", rpg);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(rpg);
            HibernateUtil.commit(tx);
            log.debug("create: void");
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find by pk
     */
    @Override
    public RegisteredPropertyGroup findByPk(final String grpName)
            throws DatabaseException {
        log.debug("findByPk({})", grpName);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final RegisteredPropertyGroup ret = (RegisteredPropertyGroup) session
                    .load(RegisteredPropertyGroup.class, grpName);
            initialize(ret);
            log.debug("findByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find all property groups
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<RegisteredPropertyGroup> findAll() throws DatabaseException {
        log.debug("findAll()");
        final String qs = "from RegisteredPropertyGroup rpg";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            final List<RegisteredPropertyGroup> ret = q.list();
            initialize(ret);
            log.debug("findAll: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initialize(final RegisteredPropertyGroup propGroup) {
        if (propGroup != null) {
            Hibernate.initialize(propGroup);

            for (final Entry<String, String> entry : propGroup.getProperties()
                    .entrySet()) {
                Hibernate.initialize(entry);
            }
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initialize(final List<RegisteredPropertyGroup> propGroupList) {
        for (final RegisteredPropertyGroup propGroup : propGroupList) {
            initialize(propGroup);
        }
    }
}
