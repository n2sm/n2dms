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

package com.openkm.module.jcr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.Note;
import com.openkm.bean.Permission;
import com.openkm.bean.Property;
import com.openkm.bean.Repository;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.module.AuthModule;
import com.openkm.module.common.CommonAuthModule;
import com.openkm.module.jcr.stuff.JCRUtils;
import com.openkm.module.jcr.stuff.JcrSessionManager;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.util.UserActivity;

public class JcrAuthModule implements AuthModule {
    private static Logger log = LoggerFactory.getLogger(JcrAuthModule.class);

    @Override
    public void login() throws RepositoryException, DatabaseException {
        Session session = null;

        try {
            session = JCRUtils.getSession();

            // Activity log
            UserActivity.log(session.getUserID(), "LOGIN", null, null, null);
        } catch (final LoginException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            JCRUtils.logout(session);
        }
    }

    @Override
    public String login(final String user, final String password)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        String token = null;

        try {
            if (Config.SYSTEM_MAINTENANCE) {
                throw new AccessDeniedException("System under maintenance");
            } else {
                final javax.jcr.Repository r = JcrRepositoryModule
                        .getRepository();
                final Session session = r.login(new SimpleCredentials(user,
                        password.toCharArray()), null);
                token = UUID.randomUUID().toString();
                JcrSessionManager.getInstance().add(token, session);

                // Activity log
                UserActivity.log(session.getUserID(), "LOGIN", null, null,
                        token);
            }
        } catch (final LoginException e) {
            log.error(e.getMessage(), e);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }

        return token;
    }

    @Override
    public void logout(final String token) throws RepositoryException,
            DatabaseException {
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            if (session != null) {
                // Activity log
                UserActivity.log(session.getUserID(), "LOGOUT", token, null,
                        null);

                JcrSessionManager.getInstance().remove(token);
                session.logout();
            }
        } catch (final LoginException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Load user data
     */
    public static void loadUserData(final Session session)
            throws DatabaseException, javax.jcr.RepositoryException {
        log.debug("loadUserData({}) -> {}", session.getUserID(), session);

        synchronized (session.getUserID()) {
            if (!session.itemExists("/" + Repository.TRASH + "/"
                    + session.getUserID())) {
                log.info("Create {}/{}", Repository.TRASH, session.getUserID());
                final Node okmTrash = session.getRootNode().getNode(
                        Repository.TRASH);
                createBase(session, okmTrash);
                okmTrash.save();
            }

            if (!session.itemExists("/" + Repository.PERSONAL + "/"
                    + session.getUserID())) {
                log.info("Create {}/{}", Repository.PERSONAL,
                        session.getUserID());
                final Node okmPersonal = session.getRootNode().getNode(
                        Repository.PERSONAL);
                createBase(session, okmPersonal);
                okmPersonal.save();
            }

            if (!session.itemExists("/" + Repository.MAIL + "/"
                    + session.getUserID())) {
                log.info("Create {}/{}", Repository.MAIL, session.getUserID());
                final Node okmMail = session.getRootNode().getNode(
                        Repository.MAIL);
                createBase(session, okmMail);
                okmMail.save();
            }
        }

        log.debug("loadUserData: void");
    }

    /**
     * Create base node
     */
    private static Node createBase(final Session session, final Node root)
            throws javax.jcr.RepositoryException {
        log.debug("createBase({}, {})", session, root);
        final Node base = root.addNode(session.getUserID(), Folder.TYPE);

        // Add basic properties
        base.setProperty(Folder.AUTHOR, session.getUserID());
        base.setProperty(Folder.NAME, session.getUserID());
        base.setProperty(Property.KEYWORDS, new String[] {});
        base.setProperty(Property.CATEGORIES, new String[] {},
                PropertyType.REFERENCE);

        // Auth info
        base.setProperty(Permission.USERS_READ,
                new String[] { session.getUserID() });
        base.setProperty(Permission.USERS_WRITE,
                new String[] { session.getUserID() });
        base.setProperty(Permission.USERS_DELETE,
                new String[] { session.getUserID() });
        base.setProperty(Permission.USERS_SECURITY,
                new String[] { session.getUserID() });
        base.setProperty(Permission.ROLES_READ, new String[] {});
        base.setProperty(Permission.ROLES_WRITE, new String[] {});
        base.setProperty(Permission.ROLES_DELETE, new String[] {});
        base.setProperty(Permission.ROLES_SECURITY, new String[] {});

        return base;
    }

    @Override
    public void grantUser(final String token, final String nodePath,
            final String user, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantUser({}, {}, {}, {})", new Object[] { nodePath, user,
                permissions, recursive });
        Node node = null;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            node = session.getRootNode().getNode(nodePath.substring(1));
            String property = null;

            if (permissions == Permission.READ) {
                property = Permission.USERS_READ;
            } else if (permissions == Permission.WRITE) {
                property = Permission.USERS_WRITE;
            } else if (permissions == Permission.DELETE) {
                property = Permission.USERS_DELETE;
            } else if (permissions == Permission.SECURITY) {
                property = Permission.USERS_SECURITY;
            }

            if (property != null) {
                synchronized (node) {
                    if (recursive) {
                        grantUserInDepth(node, user, property);
                    } else {
                        grantUser(node, user, property);
                    }
                }

                // Activity log
                UserActivity.log(session.getUserID(), "GRANT_USER",
                        node.getUUID(), nodePath, user + ", " + permissions);
            }
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("grantUser: void");
    }

    /**
     * Grant user
     */
    private void grantUser(final Node node, final String user,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        final Value[] actualUsers = node.getProperty(property).getValues();
        final ArrayList<String> newUsers = new ArrayList<String>();

        for (final Value actualUser : actualUsers) {
            newUsers.add(actualUser.getString());
        }

        // If the user isn't already granted add him
        if (!newUsers.contains(user)) {
            newUsers.add(user);
        }

        try {
            node.setProperty(property,
                    newUsers.toArray(new String[newUsers.size()]));
            node.save();
        } catch (final javax.jcr.lock.LockException e) {
            log.warn("grantUser -> LockException : {}", node.getPath());
            JCRUtils.discardsPendingChanges(node);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn("grantUser -> AccessDeniedException : {}", node.getPath());
            JCRUtils.discardsPendingChanges(node);
        }
    }

    /**
     * Grant user recursively
     */
    private void grantUserInDepth(final Node node, final String user,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        if (node.isNodeType(Document.TYPE)) {
            grantUser(node, user, property);
        } else if (node.isNodeType(Folder.TYPE)) {
            grantUser(node, user, property);

            for (final NodeIterator it = node.getNodes(); it.hasNext();) {
                final Node child = it.nextNode();
                grantUserInDepth(child, user, property);
            }
        } else if (node.isNodeType(Mail.TYPE)) {
            grantUser(node, user, property);
        } else if (node.isNodeType(Note.LIST_TYPE)) {
            // Note nodes has no security
        }
    }

    @Override
    public void revokeUser(final String token, final String nodePath,
            final String user, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeUser({}, {}, {}, {})", new Object[] { nodePath, user,
                permissions, recursive });
        Node node = null;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            node = session.getRootNode().getNode(nodePath.substring(1));
            String property = null;

            if (permissions == Permission.READ) {
                property = Permission.USERS_READ;
            } else if (permissions == Permission.WRITE) {
                property = Permission.USERS_WRITE;
            } else if (permissions == Permission.DELETE) {
                property = Permission.USERS_DELETE;
            } else if (permissions == Permission.SECURITY) {
                property = Permission.USERS_SECURITY;
            }

            if (property != null) {
                synchronized (node) {
                    if (recursive) {
                        revokeUserInDepth(node, user, property);
                    } else {
                        revokeUser(node, user, property);
                    }
                }

                // Activity log
                UserActivity.log(session.getUserID(), "REVOKE_USER",
                        node.getUUID(), nodePath, user + ", " + permissions);
            }
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("revokeUser: void");
    }

    /**
     * Revoke user
     */
    private void revokeUser(final Node node, final String user,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        final Value[] actualUsers = node.getProperty(property).getValues();
        final List<String> newUsers = new ArrayList<String>();

        for (int i = 0; i < actualUsers.length; i++) {
            if (!actualUsers[i].getString().equals(user)) {
                newUsers.add(actualUsers[i].getString());
            }
        }

        try {
            node.setProperty(property,
                    newUsers.toArray(new String[newUsers.size()]));
            node.save();
        } catch (final javax.jcr.lock.LockException e) {
            log.warn("revokeUser -> LockException : " + node.getPath());
            JCRUtils.discardsPendingChanges(node);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn("revokeUser -> AccessDeniedException : " + node.getPath());
            JCRUtils.discardsPendingChanges(node);
        }
    }

    /**
     * Revoke user recursively
     */
    private void revokeUserInDepth(final Node node, final String user,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        if (node.isNodeType(Document.TYPE)) {
            revokeUser(node, user, property);
        } else if (node.isNodeType(Folder.TYPE)) {
            revokeUser(node, user, property);

            for (final NodeIterator it = node.getNodes(); it.hasNext();) {
                final Node child = it.nextNode();
                revokeUserInDepth(child, user, property);
            }
        } else if (node.isNodeType(Mail.TYPE)) {
            revokeUser(node, user, property);
        } else if (node.isNodeType(Note.LIST_TYPE)) {
            // Note nodes has no security
        }
    }

    @Override
    public void grantRole(final String token, final String nodePath,
            final String role, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantRole({}, {}, {}, {})", new Object[] { nodePath, role,
                permissions, recursive });
        Node node = null;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            node = session.getRootNode().getNode(nodePath.substring(1));
            String property = null;

            if (permissions == Permission.READ) {
                property = Permission.ROLES_READ;
            } else if (permissions == Permission.WRITE) {
                property = Permission.ROLES_WRITE;
            } else if (permissions == Permission.DELETE) {
                property = Permission.ROLES_DELETE;
            } else if (permissions == Permission.SECURITY) {
                property = Permission.ROLES_SECURITY;
            }

            if (property != null) {
                synchronized (node) {
                    if (recursive) {
                        grantRoleInDepth(node, role, property);
                    } else {
                        grantRole(node, role, property);
                    }
                }

                // Activity log
                UserActivity.log(session.getUserID(), "GRANT_ROLE",
                        node.getUUID(), nodePath, role + ", " + permissions);
            }
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("grantRole: void");
    }

    /**
     * Grant role
     */
    private void grantRole(final Node node, final String role,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        final Value[] actualRoles = node.getProperty(property).getValues();
        final List<String> newRoles = new ArrayList<String>();

        for (final Value actualRole : actualRoles) {
            newRoles.add(actualRole.getString());
        }

        // If the role isn't already granted add him
        if (!newRoles.contains(role)) {
            newRoles.add(role);
        }

        try {
            node.setProperty(property,
                    newRoles.toArray(new String[newRoles.size()]));
            node.save();
        } catch (final javax.jcr.lock.LockException e) {
            log.warn("grantRole -> LockException : {}", node.getPath());
            JCRUtils.discardsPendingChanges(node);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn("grantRole -> AccessDeniedException : {}", node.getPath());
            JCRUtils.discardsPendingChanges(node);
        }
    }

    /**
     * Grant role recursively
     */
    private void grantRoleInDepth(final Node node, final String role,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        if (node.isNodeType(Document.TYPE)) {
            grantRole(node, role, property);
        } else if (node.isNodeType(Folder.TYPE)) {
            grantRole(node, role, property);

            for (final NodeIterator it = node.getNodes(); it.hasNext();) {
                final Node child = it.nextNode();
                grantRoleInDepth(child, role, property);
            }
        } else if (node.isNodeType(Mail.TYPE)) {
            grantRole(node, role, property);
        } else if (node.isNodeType(Note.LIST_TYPE)) {
            // Note nodes has no security
        }
    }

    @Override
    public void revokeRole(final String token, final String nodePath,
            final String role, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeRole({}, {}, {}, {})", new Object[] { nodePath, role,
                permissions, recursive });
        Node node = null;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            node = session.getRootNode().getNode(nodePath.substring(1));
            String property = null;

            if (permissions == Permission.READ) {
                property = Permission.ROLES_READ;
            } else if (permissions == Permission.WRITE) {
                property = Permission.ROLES_WRITE;
            } else if (permissions == Permission.DELETE) {
                property = Permission.ROLES_DELETE;
            } else if (permissions == Permission.SECURITY) {
                property = Permission.ROLES_SECURITY;
            }

            if (property != null) {
                synchronized (node) {
                    if (recursive) {
                        revokeRoleInDepth(node, role, property);
                    } else {
                        revokeRole(node, role, property);
                    }
                }

                // Activity log
                UserActivity.log(session.getUserID(), "REVOKE_ROLE",
                        node.getUUID(), nodePath, role + ", " + permissions);
            }
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(node);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("revokeRole: void");
    }

    /**
     * Revoke role
     */
    private void revokeRole(final Node node, final String role,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        final Value[] actualRoles = node.getProperty(property).getValues();
        final List<String> newRoles = new ArrayList<String>();

        for (int i = 0; i < actualRoles.length; i++) {
            if (!actualRoles[i].getString().equals(role)) {
                newRoles.add(actualRoles[i].getString());
            }
        }

        try {
            node.setProperty(property,
                    newRoles.toArray(new String[newRoles.size()]));
            node.save();
        } catch (final javax.jcr.lock.LockException e) {
            log.warn("revokeRole -> LockException : " + node.getPath());
            JCRUtils.discardsPendingChanges(node);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn("revokeRole -> AccessDeniedException : " + node.getPath());
            JCRUtils.discardsPendingChanges(node);
        }
    }

    /**
     * Revoke role recursively
     */
    private void revokeRoleInDepth(final Node node, final String role,
            final String property) throws ValueFormatException,
            PathNotFoundException, javax.jcr.RepositoryException {
        if (node.isNodeType(Document.TYPE)) {
            revokeRole(node, role, property);
        } else if (node.isNodeType(Folder.TYPE)) {
            revokeRole(node, role, property);

            for (final NodeIterator it = node.getNodes(); it.hasNext();) {
                final Node child = it.nextNode();
                revokeRoleInDepth(child, role, property);
            }
        } else if (node.isNodeType(Mail.TYPE)) {
            revokeRole(node, role, property);
        } else if (node.isNodeType(Note.LIST_TYPE)) {
            // Note nodes has no security
        }
    }

    @Override
    public HashMap<String, Integer> getGrantedUsers(final String token,
            final String nodePath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("getGrantedUsers({})", nodePath);
        final HashMap<String, Integer> users = new HashMap<String, Integer>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Node node = session.getRootNode().getNode(
                    nodePath.substring(1));
            final Value[] usersRead = node.getProperty(Permission.USERS_READ)
                    .getValues();

            for (final Value element : usersRead) {
                users.put(element.getString(), new Integer(Permission.READ));
            }

            final Value[] usersWrite = node.getProperty(Permission.USERS_WRITE)
                    .getValues();

            for (final Value element : usersWrite) {
                final Integer previous = users.get(element.getString());

                if (previous != null) {
                    users.put(
                            element.getString(),
                            new Integer(previous.byteValue() | Permission.WRITE));
                } else {
                    users.put(element.getString(),
                            new Integer(Permission.WRITE));
                }
            }

            final Value[] usersDelete = node.getProperty(
                    Permission.USERS_DELETE).getValues();

            for (final Value element : usersDelete) {
                final Integer previous = users.get(element.getString());

                if (previous != null) {
                    users.put(element.getString(),
                            new Integer(previous.byteValue()
                                    | Permission.DELETE));
                } else {
                    users.put(element.getString(), new Integer(
                            Permission.DELETE));
                }
            }

            final Value[] usersSecurity = node.getProperty(
                    Permission.USERS_SECURITY).getValues();

            for (final Value element : usersSecurity) {
                final Integer previous = users.get(element.getString());

                if (previous != null) {
                    users.put(element.getString(),
                            new Integer(previous.byteValue()
                                    | Permission.SECURITY));
                } else {
                    users.put(element.getString(), new Integer(
                            Permission.SECURITY));
                }
            }
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getGrantedUsers: {}", users);
        return users;
    }

    @Override
    public Map<String, Integer> getGrantedRoles(final String token,
            final String nodePath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("getGrantedRoles({})", nodePath);
        final Map<String, Integer> roles = new HashMap<String, Integer>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Node node = session.getRootNode().getNode(
                    nodePath.substring(1));
            final Value[] rolesRead = node.getProperty(Permission.ROLES_READ)
                    .getValues();

            for (final Value element : rolesRead) {
                roles.put(element.getString(), new Integer(Permission.READ));
            }

            final Value[] rolesWrite = node.getProperty(Permission.ROLES_WRITE)
                    .getValues();

            for (final Value element : rolesWrite) {
                final Integer previous = roles.get(element.getString());

                if (previous != null) {
                    roles.put(element.getString(),
                            new Integer(previous.intValue() | Permission.WRITE));
                } else {
                    roles.put(element.getString(),
                            new Integer(Permission.WRITE));
                }
            }

            final Value[] rolesDelete = node.getProperty(
                    Permission.ROLES_DELETE).getValues();

            for (final Value element : rolesDelete) {
                final Integer previous = roles.get(element.getString());

                if (previous != null) {
                    roles.put(
                            element.getString(),
                            new Integer(previous.intValue() | Permission.DELETE));
                } else {
                    roles.put(element.getString(), new Integer(
                            Permission.DELETE));
                }
            }

            final Value[] rolesSecurity = node.getProperty(
                    Permission.ROLES_SECURITY).getValues();

            for (final Value element : rolesSecurity) {
                final Integer previous = roles.get(element.getString());

                if (previous != null) {
                    roles.put(element.getString(),
                            new Integer(previous.intValue()
                                    | Permission.SECURITY));
                } else {
                    roles.put(element.getString(), new Integer(
                            Permission.SECURITY));
                }
            }
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getGrantedRoles: {}", roles);
        return roles;
    }

    /**
     *  View user session info
     */
    public void view(final String token) throws RepositoryException,
            DatabaseException {
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final String[] atributes = session.getAttributeNames();
            log.info("** ATRIBUTES **");
            for (final String atribute : atributes) {
                log.info(atribute + " -> " + session.getAttribute(atribute));
            }

            final String[] lockTokens = session.getLockTokens();
            log.info("** LOCK TOKENS **");
            for (final String lockToken : lockTokens) {
                log.info(lockToken);
            }
        } catch (final LoginException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    @Override
    public List<String> getUsers(final String token)
            throws PrincipalAdapterException {
        return CommonAuthModule.getUsers(token);
    }

    @Override
    public List<String> getRoles(final String token)
            throws PrincipalAdapterException {
        return CommonAuthModule.getRoles(token);
    }

    @Override
    public List<String> getUsersByRole(final String token, final String role)
            throws PrincipalAdapterException {
        return CommonAuthModule.getUsersByRole(token, role);
    }

    @Override
    public List<String> getRolesByUser(final String token, final String user)
            throws PrincipalAdapterException {
        return CommonAuthModule.getRolesByUser(token, user);
    }

    @Override
    public String getMail(final String token, final String user)
            throws PrincipalAdapterException {
        return CommonAuthModule.getMail(token, user);
    }

    @Override
    public String getName(final String token, final String user)
            throws PrincipalAdapterException {
        return CommonAuthModule.getName(token, user);
    }

    @Override
    public void changeSecurity(final String token, final String nodePath,
            final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        throw new NotImplementedException("changeSecurity");
    }
}
