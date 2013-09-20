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
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.bean.QueryParams;

public class QueryParamsDAO {
    private static Logger log = LoggerFactory.getLogger(QueryParamsDAO.class);

    private QueryParamsDAO() {
    }

    /**
     * Create
     */
    public static long create(final QueryParams qp) throws DatabaseException {
        log.debug("create({})", qp);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Long id = (Long) session.save(qp);
            final QueryParams qpTmp = (QueryParams) session.load(
                    QueryParams.class, id);

            for (final String keyword : qp.getKeywords()) {
                qpTmp.getKeywords().add(keyword);
            }

            for (final String category : qp.getCategories()) {
                qpTmp.getCategories().add(category);
            }

            for (final Map.Entry<String, String> entry : qp.getProperties()
                    .entrySet()) {
                qpTmp.getProperties().put(entry.getKey(), entry.getValue());
            }

            HibernateUtil.commit(tx);
            log.debug("create: {}", id);
            return id;
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Update
     */
    public static void update(final QueryParams qp) throws DatabaseException {
        log.debug("update({})", qp);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(qp);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("update: void");
    }

    /**
     * Delete
     */
    public static void delete(final long qpId) throws DatabaseException {
        log.debug("delete({})", qpId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final QueryParams qp = (QueryParams) session.load(
                    QueryParams.class, qpId);
            session.delete(qp);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("delete: void");
    }

    /**
     * Find by pk
     */
    public static QueryParams findByPk(final long qpId)
            throws DatabaseException {
        log.debug("findByPk({})", qpId);
        final String qs = "from QueryParams qp where qp.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", qpId);
            final QueryParams ret = (QueryParams) q.setMaxResults(1)
                    .uniqueResult();
            log.debug("findByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find by user
     */
    @SuppressWarnings("unchecked")
    public static List<QueryParams> findByUser(final String user)
            throws DatabaseException {
        log.debug("findByUser({})", user);
        final String qs = "from QueryParams qp where qp.user=:user";
        Session session = null;
        final Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("user", user);
            final List<QueryParams> ret = q.list();
            HibernateUtil.commit(tx);
            log.debug("findByUser: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Share
     */
    public static void share(final long qpId, final String user)
            throws DatabaseException {
        log.debug("share({})", qpId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final QueryParams qp = (QueryParams) session.load(
                    QueryParams.class, qpId);
            qp.getShared().add(user);
            session.update(qp);
            HibernateUtil.commit(tx);
            log.debug("share: void");
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Unshare
     */
    public static void unshare(final long qpId, final String user)
            throws DatabaseException {
        log.debug("share({})", qpId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final QueryParams qp = (QueryParams) session.load(
                    QueryParams.class, qpId);
            qp.getShared().remove(user);
            session.update(qp);
            HibernateUtil.commit(tx);
            log.debug("share: void");
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}
