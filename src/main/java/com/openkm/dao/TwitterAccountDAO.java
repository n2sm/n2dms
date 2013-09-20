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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.bean.TwitterAccount;

public class TwitterAccountDAO {
    private static Logger log = LoggerFactory
            .getLogger(TwitterAccountDAO.class);

    private TwitterAccountDAO() {
    }

    /**
     * Create account
     */
    public static void create(final TwitterAccount ta) throws DatabaseException {
        log.debug("create({})", ta);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(ta);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("create: void");
    }

    /**
     * Update
     */
    public static void update(final TwitterAccount ta) throws DatabaseException {
        log.debug("update({})", ta);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(ta);
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
    public static void delete(final long taId) throws DatabaseException {
        log.debug("delete({})", taId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final TwitterAccount ta = (TwitterAccount) session.load(
                    TwitterAccount.class, taId);
            session.delete(ta);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteTwitterAccount: void");
    }

    /**
     * Find by user
     */
    @SuppressWarnings("unchecked")
    public static List<TwitterAccount> findByUser(final String user,
            final boolean filterByActive) throws DatabaseException {
        log.debug("findByUser({})", user);
        final String qs = "from TwitterAccount ta where ta.user=:user "
                + (filterByActive ? "and ta.active=:active" : "")
                + " order by ta.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("user", user);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<TwitterAccount> ret = q.list();
            log.debug("findByUser: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find all twitter accounts
     */
    @SuppressWarnings("unchecked")
    public static List<TwitterAccount> findAll(final boolean filterByActive)
            throws DatabaseException {
        log.debug("findAll()");
        final String qs = "from TwitterAccount ta "
                + (filterByActive ? "where ta.active=:active" : "")
                + " order by ta.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<TwitterAccount> ret = q.list();
            log.debug("findAll: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find by pk
     */
    public static TwitterAccount findByPk(final long taId)
            throws DatabaseException {
        log.debug("findByPk({})", taId);
        final String qs = "from TwitterAccount ta where ta.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", taId);
            final TwitterAccount ret = (TwitterAccount) q.setMaxResults(1)
                    .uniqueResult();
            log.debug("findByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}
