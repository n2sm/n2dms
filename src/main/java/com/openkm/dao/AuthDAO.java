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

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.bean.QueryParams;
import com.openkm.dao.bean.Role;
import com.openkm.dao.bean.User;
import com.openkm.util.SecureStore;

public class AuthDAO {
    private static Logger log = LoggerFactory.getLogger(AuthDAO.class);

    private AuthDAO() {
    }

    /**
     * Create user in database
     */
    public static void createUser(final User user) throws DatabaseException {
        log.debug("createUser({})", user);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            user.setPassword(SecureStore.md5Encode(user.getPassword()
                    .getBytes()));
            session.save(user);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } catch (final NoSuchAlgorithmException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("createUser: void");
    }

    /**
     * Update user in database
     */
    public static void updateUser(final User user) throws DatabaseException {
        log.debug("updateUser({})", user);
        final String qs = "select u.password from User u where u.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Query q = session.createQuery(qs);
            q.setParameter("id", user.getId());
            final String password = (String) q.setMaxResults(1).uniqueResult();
            user.setPassword(password);
            session.update(user);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateUser: void");
    }

    /**
     * Active user in database
     */
    public static void activeUser(final String usrId, final boolean active)
            throws DatabaseException {
        log.debug("activeUser({}, {})", usrId, active);
        final String qs = "update User u set u.active=:active where u.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Query q = session.createQuery(qs);
            q.setBoolean("active", active);
            q.setString("id", usrId);
            q.executeUpdate();
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("activeUser: void");
    }

    /**
     * Update user password in database 
     */
    public static void updateUserPassword(final String usrId,
            final String usrPassword) throws DatabaseException {
        log.debug("updateUserPassword({}, {})", usrId, usrPassword);
        final String qs = "update User u set u.password=:password where u.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            if (usrPassword != null && usrPassword.trim().length() > 0) {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                final Query q = session.createQuery(qs);
                q.setString("password",
                        SecureStore.md5Encode(usrPassword.getBytes()));
                q.setString("id", usrId);
                q.executeUpdate();
                HibernateUtil.commit(tx);
            }
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } catch (final NoSuchAlgorithmException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateUserPassword: void");
    }

    /**
     * Update user email in database
     */
    public static void updateUserEmail(final String usrId, final String usrEmail)
            throws DatabaseException {
        log.debug("updateUserEmail({}, {})", usrId, usrEmail);
        final String qs = "update User u set u.email=:email where u.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            if (usrEmail != null && usrEmail.trim().length() > 0) {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                final Query q = session.createQuery(qs);
                q.setString("email", usrEmail);
                q.setString("id", usrId);
                q.executeUpdate();
                HibernateUtil.commit(tx);
            }
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateUserEmail: void");
    }

    /**
     * Delete user from database
     */
    @SuppressWarnings("unchecked")
    public static void deleteUser(final String usrId) throws DatabaseException {
        log.debug("deleteUser({})", usrId);
        final String qsMail = "delete from MailAccount ma where ma.user=:user";
        final String qsTwitter = "delete from TwitterAccount ta where ta.user=:user";
        final String qsBookmark = "delete from Bookmark bm where bm.user=:user";
        final String qsConfig = "delete from UserConfig uc where uc.user=:user";
        final String qsItems = "delete from UserItems ui where ui.user=:user";
        final String qsSharedQuery = "from QueryParams qp where :user in elements(qp.shared)";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final User user = (User) session.load(User.class, usrId);
            session.delete(user);

            final Query qMail = session.createQuery(qsMail);
            qMail.setString("user", usrId);
            qMail.executeUpdate();

            final Query qTwitter = session.createQuery(qsTwitter);
            qTwitter.setString("user", usrId);
            qTwitter.executeUpdate();

            final Query qBookmark = session.createQuery(qsBookmark);
            qBookmark.setString("user", usrId);
            qBookmark.executeUpdate();

            final Query qConfig = session.createQuery(qsConfig);
            qConfig.setString("user", usrId);
            qConfig.executeUpdate();

            final Query qItems = session.createQuery(qsItems);
            qItems.setString("user", usrId);
            qItems.executeUpdate();

            final Query qSharedQuery = session.createQuery(qsSharedQuery);
            qSharedQuery.setString("user", usrId);
            for (final QueryParams qp : (List<QueryParams>) qSharedQuery.list()) {
                qp.getShared().remove(usrId);
                session.update(qp);
            }

            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteUser: void");
    }

    /**
     * Get all users in database
     * 
     * @param filterByActive If only active user2 should be included.
     */
    @SuppressWarnings("unchecked")
    public static List<User> findAllUsers(final boolean filterByActive)
            throws DatabaseException {
        log.debug("findAllUsers({})", filterByActive);
        final String qs = "from User u "
                + (filterByActive ? "where u.active=:active" : "")
                + " order by u.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<User> ret = q.list();
            log.debug("findAllUsers: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get all users within a role
     */
    @SuppressWarnings("unchecked")
    public static List<User> findUsersByRole(final String rolId,
            final boolean filterByActive) throws DatabaseException {
        log.debug("findUsersByRole({}, {})", rolId, filterByActive);
        final String qs = "select u from User u, Role r where r.id=:rolId and r in elements(u.roles) "
                + (filterByActive ? "and u.active=:active" : "")
                + " order by u.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("rolId", rolId);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<User> ret = q.list();
            log.debug("findUsersByRole: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get all users within a role
     */
    @SuppressWarnings("unchecked")
    public static List<Role> findRolesByUser(final String usrId,
            final boolean filterByActive) throws DatabaseException {
        log.debug("findRolesByUser({}, {})", usrId, filterByActive);
        final String qs = "select r from User u, Role r where u.id=:usrId and r in elements(u.roles) "
                + (filterByActive ? "and r.active=:active" : "")
                + " order by r.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("usrId", usrId);

            if (filterByActive) {
                q.setBoolean("active", true);
            }

            final List<Role> ret = q.list();
            log.debug("findRolesByUser: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get user from database
     */
    public static User findUserByPk(final String usrId)
            throws DatabaseException {
        log.debug("findUserByPk({})", usrId);
        final String qs = "from User u where u.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("id", usrId);
            final User ret = (User) q.setMaxResults(1).uniqueResult();
            log.debug("findUserByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Create role in database
     */
    public static void createRole(final Role role) throws DatabaseException {
        log.debug("createRole({})", role);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(role);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("createRole: void");
    }

    /**
     * Update role in database
     */
    public static void updateRole(final Role role) throws DatabaseException {
        log.debug("updateRole({})", role);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.update(role);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("updateRole: void");
    }

    /**
     * Active role in database
     */
    public static void activeRole(final String rolId, final boolean active)
            throws DatabaseException {
        log.debug("activeRole({}, {})", rolId, active);
        final String qs = "update Role r set r.active=:active where r.id=:id";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Query q = session.createQuery(qs);
            q.setBoolean("active", active);
            q.setString("id", rolId);
            q.executeUpdate();
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("activeRole: void");
    }

    /**
     * Delete role from database
     */
    public static void deleteRole(final String rolId) throws DatabaseException {
        log.debug("deleteRole({})", rolId);
        final String qs = "delete from OKM_USER_ROLE where UR_ROLE=:rolId";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final Role role = (Role) session.load(Role.class, rolId);
            session.delete(role);

            // TODO: Make Hibernate handle this relation.
            final SQLQuery q = session.createSQLQuery(qs);
            q.setString("rolId", rolId);
            q.executeUpdate();

            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("deleteRole: void");
    }

    /**
     * Get all roles in database
     */
    @SuppressWarnings("unchecked")
    public static List<Role> findAllRoles() throws DatabaseException {
        log.debug("findAllRoles()");
        final String qs = "from Role r order by r.id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            final List<Role> ret = q.list();
            log.debug("findAllRoles: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Find role by pk
     */
    public static Role findRoleByPk(final String rolId)
            throws DatabaseException {
        log.debug("findRoleByPk({})", rolId);
        final String qs = "from Role r where r.id=:id";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("id", rolId);
            final Role ret = (Role) q.setMaxResults(1).uniqueResult();
            log.debug("findRoleByPk: {}", ret);
            return ret;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Grant role to user
     */
    public static void grantRole(final String usrId, final String rolId)
            throws DatabaseException {
        log.debug("grantRole({}, {})", usrId, rolId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final User user = (User) session.load(User.class, usrId);
            final Role role = (Role) session.load(Role.class, rolId);
            user.getRoles().add(role);
            session.update(user);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("grantRole: void");
    }

    /**
     * Revoke role from user
     */
    public void revokeRole(final String usrId, final String rolId)
            throws DatabaseException {
        log.debug("revokeRole({}, {})", usrId, rolId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final User user = (User) session.load(User.class, usrId);
            final Role role = (Role) session.load(Role.class, rolId);
            user.getRoles().remove(role);
            session.update(user);
            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("revokeRole: void");
    }
}
