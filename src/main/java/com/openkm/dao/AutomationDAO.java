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

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.bean.AutomationAction;
import com.openkm.dao.bean.AutomationMetadata;
import com.openkm.dao.bean.AutomationRule;
import com.openkm.dao.bean.AutomationValidation;

/**
 * AutomationDAO
 * 
 * @author jllort
 */
public class AutomationDAO {
    private static Logger log = LoggerFactory.getLogger(AutomationDAO.class);

    private static AutomationDAO single = new AutomationDAO();

    private AutomationDAO() {
    }

    public static AutomationDAO getInstance() {
        return single;
    }

    /**
     * Create
     */
    public void create(final AutomationRule ar) throws DatabaseException {
        log.debug("create({})", ar);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(ar);
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
    public void update(final AutomationRule ar) throws DatabaseException {
        log.debug("update({})", ar);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(ar);
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
     * Update action
     */
    public void updateAction(final AutomationAction aa)
            throws DatabaseException {
        log.debug("updateAction({})", aa);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(aa);
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
     * Update validation
     */
    public void updateValidation(final AutomationValidation av)
            throws DatabaseException {
        log.debug("updateAction({})", av);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(av);
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
    public void delete(final long raId) throws DatabaseException {
        log.debug("delete({})", raId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final AutomationRule ra = (AutomationRule) session.load(
                    AutomationRule.class, raId);
            session.delete(ra);
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
     * Delete action
     */
    public void deleteAction(final long aaId) throws DatabaseException {
        log.debug("deleteAction({})", aaId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final AutomationAction aa = (AutomationAction) session.load(
                    AutomationAction.class, aaId);
            session.delete(aa);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteAction: void");
    }

    /**
     * Delete validation
     */
    public void deleteValidation(final long avId) throws DatabaseException {
        log.debug("deleteValidation({})", avId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final AutomationValidation av = (AutomationValidation) session
                    .load(AutomationValidation.class, avId);
            session.delete(av);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteAction: void");
    }

    /**
     * Create
     */
    public void createAction(final AutomationAction aa)
            throws DatabaseException {
        log.debug("createAction({})", aa);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(aa);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("createAction: void");
    }

    /**
     * Create
     */
    public void createValidation(final AutomationValidation av)
            throws DatabaseException {
        log.debug("createValidation({})", av);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(av);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("createValidation: void");
    }

    /**
     * find all rules
     */
    @SuppressWarnings("unchecked")
    public List<AutomationRule> findAll() throws DatabaseException {
        log.debug("findAll()");
        final String qs = "from AutomationRule ar order by ar.order";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            final List<AutomationRule> ret = q.list();
            initializeRules(ret);
            log.debug("findAll: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * find filtered riles
     */
    @SuppressWarnings("unchecked")
    public List<AutomationRule> findByEvent(final String event, final String at)
            throws DatabaseException {
        log.debug("findByEvent({}, {})", event, at);
        final String qs = "from AutomationRule ar where ar.event=:event and ar.at=:at order by ar.order";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("event", event);
            q.setString("at", at);
            final List<AutomationRule> ret = q.list();
            initializeRules(ret);
            log.debug("findByEvent: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get all metadata actions
     */
    @SuppressWarnings("unchecked")
    public List<AutomationMetadata> findMetadataValidationsByAt(final String at)
            throws DatabaseException {
        log.debug("findAllMetadataValidations()");
        final String qs = "from AutomationMetadata am where am.group=:group and am.at=:at order by am.name";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("group", AutomationMetadata.GROUP_VALIDATION);
            q.setString("at", at);
            final List<AutomationMetadata> ret = q.list();
            log.debug("findAllMetadataValidations: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get all metadata actions
     */
    @SuppressWarnings("unchecked")
    public List<AutomationMetadata> findMetadataActionsByAt(final String at)
            throws DatabaseException {
        log.debug("findAllMetadataActions()");
        final String qs = "from AutomationMetadata am where am.group=:group and am.at=:at order by am.name";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("group", AutomationMetadata.GROUP_ACTION);
            q.setString("at", at);
            final List<AutomationMetadata> ret = q.list();
            log.debug("findAllMetadataActions: {}", ret);
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
    public AutomationRule findByPk(final long arId) throws DatabaseException {
        log.debug("findByPk({})", arId);
        final String qs = "from AutomationRule ar where ar.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", arId);
            final AutomationRule ret = (AutomationRule) q.setMaxResults(1)
                    .uniqueResult();
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
     * Get metadata by pk
     */
    public AutomationMetadata findMetadataByPk(final long amId)
            throws DatabaseException {
        log.debug("findMetadataByPk({})", amId);
        final String qs = "from AutomationMetadata am where am.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", amId);
            final AutomationMetadata ret = (AutomationMetadata) q
                    .setMaxResults(1).uniqueResult();
            log.debug("findMetadataByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get validation by pk
     */
    public AutomationValidation findValidationByPk(final long avId)
            throws DatabaseException {
        log.debug("findValidationByPk({})", avId);
        final String qs = "from AutomationValidation av where av.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", avId);
            final AutomationValidation ret = (AutomationValidation) q
                    .setMaxResults(1).uniqueResult();
            initialize(ret);
            log.debug("findValidationByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get action by pk
     */
    public AutomationAction findActionByPk(final long aaId)
            throws DatabaseException {
        log.debug("findActionByPk({})", aaId);
        final String qs = "from AutomationAction aa where aa.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setLong("id", aaId);
            final AutomationAction ret = (AutomationAction) q.setMaxResults(1)
                    .uniqueResult();
            initialize(ret);
            log.debug("findActionByPk: {}", ret);
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
    private void initialize(final AutomationRule aRule) {
        if (aRule != null) {
            Hibernate.initialize(aRule);
            initializeActions(aRule.getActions());
            initializeValidations(aRule.getValidations());
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initialize(final AutomationValidation aValidation) {
        if (aValidation != null) {
            Hibernate.initialize(aValidation);
            Hibernate.initialize(aValidation.getParams());
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initialize(final AutomationAction aAction) {
        if (aAction != null) {
            Hibernate.initialize(aAction);
            Hibernate.initialize(aAction.getParams());
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initializeRules(final List<AutomationRule> nRuleList) {
        for (final AutomationRule aRule : nRuleList) {
            initialize(aRule);
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initializeValidations(
            final List<AutomationValidation> nValidationList) {
        for (final AutomationValidation aValidation : nValidationList) {
            initialize(aValidation);
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initializeActions(final List<AutomationAction> nActionList) {
        for (final AutomationAction aAction : nActionList) {
            initialize(aAction);
        }
    }
}
