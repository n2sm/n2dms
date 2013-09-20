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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.dao.bean.MailAccount;
import com.openkm.dao.bean.MailFilter;
import com.openkm.dao.bean.MailFilterRule;

public class MailAccountDAO {
    private static Logger log = LoggerFactory.getLogger(MailAccountDAO.class);

    private MailAccountDAO() {
    }

    /**
     * Create
     */
    public static void create(final MailAccount ma) throws DatabaseException {
        log.debug("create({})", ma);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(ma);
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
    public static void update(final MailAccount ma) throws DatabaseException {
        log.debug("update({})", ma);
        final String qs = "select ma.mailPassword from MailAccount ma where ma.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Query q = session.createQuery(qs);
            q.setParameter("id", ma.getId());
            final String pass = (String) q.setMaxResults(1).uniqueResult();
            ma.setMailPassword(pass);
            session.update(ma);
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
     * Update password
     */
    public static void updatePassword(final long maId, final String mailPassword)
            throws DatabaseException {
        log.debug("updatePassword({}, {})", maId, mailPassword);
        final String qs = "update MailAccount ma set ma.mailPassword=:mailPassword where ma.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            if (mailPassword != null && mailPassword.trim().length() > 0) {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                final Query q = session.createQuery(qs);
                q.setString("mailPassword", mailPassword);
                q.setLong("id", maId);
                q.executeUpdate();
                HibernateUtil.commit(tx);
            }
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updatePassword: void");
    }

    /**
     * Delete
     */
    public static void delete(final long maId) throws DatabaseException {
        log.debug("delete({})", maId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final MailAccount ma = (MailAccount) session.load(
                    MailAccount.class, maId);
            session.delete(ma);
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
     * Find by user
     */
    @SuppressWarnings("unchecked")
    public static List<MailAccount> findByUser(final String usrId,
            final boolean filterByActive) throws DatabaseException {
        log.debug("findByUser({}, {})", usrId, filterByActive);
        final String qs = "from MailAccount ma where ma.user=:user "
                + (filterByActive ? "and ma.active=:active" : "")
                + " order by ma.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("user", usrId);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<MailAccount> ret = q.list();
            log.debug("findByUser: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * find all mail accounts
     */
    @SuppressWarnings("unchecked")
    public static List<MailAccount> findAll(final boolean filterByActive)
            throws DatabaseException {
        log.debug("findAll({})", filterByActive);
        final String qs = "from MailAccount ma "
                + (filterByActive ? "where ma.active=:active" : "")
                + " order by ma.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<MailAccount> ret = q.list();
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
    public static MailAccount findByPk(final long maId)
            throws DatabaseException {
        log.debug("findByPk({})", maId);
        final String qs = "from MailAccount ma where ma.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", maId);
            final MailAccount ret = (MailAccount) q.setMaxResults(1)
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
     * Update
     */
    public static void updateFilter(final MailFilter mf)
            throws DatabaseException {
        log.debug("updateFilter({})", mf);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(mf);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateFilter: void");
    }

    /**
     * Delete
     */
    public static void deleteFilter(final long mfId) throws DatabaseException {
        log.debug("deleteFilter({})", mfId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final MailFilter mf = (MailFilter) session.load(MailFilter.class,
                    mfId);
            session.delete(mf);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteFilter: void");
    }

    /**
     * Find by pk
     */
    public static MailFilter findFilterByPk(final javax.jcr.Session jcrSession,
            final long mfId) throws DatabaseException, RepositoryException {
        log.debug("findFilterByPk({})", mfId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final MailFilter ret = (MailFilter) session.load(MailFilter.class,
                    mfId);
            final Node node = jcrSession.getNodeByUUID(ret.getNode());

            // Always keep path in sync with uuid
            if (!node.getPath().equals(ret.getPath())) {
                ret.setPath(node.getPath());
                session.update(ret);
            }

            HibernateUtil.commit(tx);
            log.debug("findFilterByPk: {}", ret);
            return ret;
        } catch (final javax.jcr.RepositoryException e) {
            HibernateUtil.rollback(tx);
            throw new RepositoryException(e.getMessage(), e);
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
    public static MailFilter findFilterByPk(final long mfId)
            throws PathNotFoundException, DatabaseException {
        log.debug("findFilterByPk({})", mfId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final MailFilter ret = (MailFilter) session.load(MailFilter.class,
                    mfId);
            final String nodePath = NodeBaseDAO.getInstance().getPathFromUuid(
                    session, ret.getNode());

            // Always keep path in sync with uuid
            if (!nodePath.equals(ret.getPath())) {
                ret.setPath(nodePath);
                session.update(ret);
            }

            HibernateUtil.commit(tx);
            log.debug("findFilterByPk: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
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
    public static void updateRule(final MailFilterRule fr)
            throws DatabaseException {
        log.debug("updateRule({})", fr);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(fr);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateRule: void");
    }

    /**
     * Delete
     */
    public static void deleteRule(final long frId) throws DatabaseException {
        log.debug("deleteRule({})", frId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final MailFilterRule fr = (MailFilterRule) session.load(
                    MailFilterRule.class, frId);
            session.delete(fr);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteRule: void");
    }

    /**
     * Find by pk
     */
    public static MailFilterRule findRuleByPk(final long frId)
            throws DatabaseException {
        log.debug("findRuleByPk({})", frId);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final MailFilterRule ret = (MailFilterRule) session.load(
                    MailFilterRule.class, frId);
            log.debug("findRuleByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}
