/**
 * OpenKM, Open Document Management System (http://www.openkm.com)
 * Copyright (c) 2006-2013 Paco Avila & Josep Llort
 * 
 * No bytes were intentionally harmed during the development of this application.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.openkm.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.Permission;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.NodeBase;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.dao.bean.NodeProperty;
import com.openkm.dao.bean.RegisteredPropertyGroup;
import com.openkm.module.db.stuff.SecurityHelper;

public class NodeBaseDAO {
    private static Logger log = LoggerFactory.getLogger(NodeBaseDAO.class);

    private static NodeBaseDAO single = new NodeBaseDAO();

    private NodeBaseDAO() {
    }

    public static NodeBaseDAO getInstance() {
        return single;
    }

    /**
     * Find by pk
     */
    public NodeBase findByPk(final String uuid) throws PathNotFoundException,
            DatabaseException {
        log.debug("findByPk({})", uuid);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final NodeBase nBase = (NodeBase) session.get(NodeBase.class, uuid);

            if (nBase == null) {
                throw new PathNotFoundException(uuid);
            }

            // Security Check
            SecurityHelper.checkRead(nBase);

            initialize(nBase);
            log.debug("findByPk: {}", nBase);
            return nBase;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get node path from UUID
     */
    public String getPathFromUuid(final String uuid)
            throws PathNotFoundException, DatabaseException {
        return calculatePathFromUuid(uuid);
    }

    /**
     * Get node path from UUID
     */
    public String getPathFromUuid(final Session session, final String uuid)
            throws PathNotFoundException, HibernateException {
        return calculatePathFromUuid(session, uuid);
    }

    /**
     * Get node UUID from path
     */
    public String getUuidFromPath(final String path)
            throws PathNotFoundException, DatabaseException {
        return calculateUuidFromPath(path);
    }

    /**
     * Get node UUID from path
     */
    public String getUuidFromPath(final Session session, final String path)
            throws PathNotFoundException, DatabaseException {
        return calculateUuidFromPath(session, path);
    }

    /**
     * Check for item existence.
     */
    public boolean itemPathExists(final String path) throws DatabaseException {
        try {
            getUuidFromPath(path);
            return true;
        } catch (final PathNotFoundException e) {
            return false;
        }
    }

    /**
     * Check for item existence.
     */
    public boolean itemUuidExists(final String uuid) throws DatabaseException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final NodeBase nBase = (NodeBase) session.get(NodeBase.class, uuid);

            if (nBase == null) {
                return false;
            }

            // Security Check
            SecurityHelper.checkRead(nBase);

            return true;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } catch (final PathNotFoundException e) {
            return false;
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get node path from UUID. This is the old one which calculates the path.
     */
    private String calculatePathFromUuid(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("calculatePathFromUuid({})", uuid);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final String path = getPathFromUuid(session, uuid);
            log.debug("calculatePathFromUuid: {}", path);
            return path;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get node path from UUID. This is the old one which calculates the path.
     */
    private String calculatePathFromUuid(final Session session, String uuid)
            throws PathNotFoundException, HibernateException {
        log.debug("calculatePathFromUuid({}, {})", session, uuid);
        String childUuid = null;
        String path = "";

        do {
            final NodeBase node = (NodeBase) session.get(NodeBase.class, uuid);

            if (node == null) {
                throw new PathNotFoundException(uuid);
            } else {
                path = "/".concat(node.getName()).concat(path);
                childUuid = uuid;
                uuid = node.getParent();

                if (uuid.equals(childUuid)) {
                    log.warn("*** Node is its own parent: {} -> {} ***", uuid,
                            path);
                    break;
                }
            }
        } while (!Config.ROOT_NODE_UUID.equals(uuid));

        log.debug("calculatePathFromUuid: {}", path);
        return path;
    }

    /**
     * Get node UUID from path. This is the old one which calculates the uuid.
     */
    private String calculateUuidFromPath(final String path)
            throws PathNotFoundException, DatabaseException {
        log.debug("calculateUuidFromPath({})", path);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final String uuid = calculateUuidFromPath(session, path);
            log.debug("calculateUuidFromPath: {}", uuid);
            return uuid;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get node UUID from path. This is the old one which calculates the uuid.
     */
    private String calculateUuidFromPath(final Session session,
            final String path) throws PathNotFoundException, HibernateException {
        log.debug("calculateUuidFromPath({}, {})", session, path);
        final String qs = "select nb.uuid from NodeBase nb where nb.parent=:parent and nb.name=:name";
        final Query q = session.createQuery(qs);
        String uuid = Config.ROOT_NODE_UUID;
        String name = "";

        for (final StringTokenizer st = new StringTokenizer(path, "/"); st
                .hasMoreTokens();) {
            name = st.nextToken();
            q.setString("name", name);
            q.setString("parent", uuid);
            uuid = (String) q.setMaxResults(1).uniqueResult();

            if (uuid == null) {
                throw new PathNotFoundException(path);
            }
        }

        log.debug("calculateUuidFromPath: {}", uuid);
        return uuid;
    }

    /**
     * Get user permissions
     */
    public Map<String, Integer> getUserPermissions(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("getUserPermissions({})", uuid);
        Map<String, Integer> ret = new HashMap<String, Integer>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            if (node != null) {
                Hibernate.initialize(ret = node.getUserPermissions());
            }

            HibernateUtil.commit(tx);
            log.debug("getUserPermissions: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Grant user permissions
     */
    public void grantUserPermissions(final String uuid, final String user,
            final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("grantUserPermissions({})", uuid);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Root node
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);

            if (recursive) {
                final int total = grantUserPermissionsInDepth(session, node,
                        user, permissions);
                log.info("grantUserPermissions.Total: {}", total);
            } else {
                grantUserPermissions(session, node, user, permissions, false);
            }

            HibernateUtil.commit(tx);
            log.debug("grantUserPermissions: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Grant user permissions
     */
    private int grantUserPermissions(final Session session,
            final NodeBase node, final String user, final int permissions,
            final boolean recursive) throws PathNotFoundException,
            AccessDeniedException, DatabaseException, HibernateException {
        // log.info("grantUserPermissions({})", node.getUuid());
        boolean canModify = true;

        // Security Check
        if (recursive) {
            canModify = SecurityHelper.isGranted(node, Permission.READ)
                    && SecurityHelper.isGranted(node, Permission.SECURITY);
        } else {
            SecurityHelper.checkRead(node);
            SecurityHelper.checkSecurity(node);
        }

        if (canModify) {
            final Integer currentPermissions = node.getUserPermissions().get(
                    user);

            if (currentPermissions == null) {
                node.getUserPermissions().put(user, permissions);
            } else {
                node.getUserPermissions().put(user,
                        permissions | currentPermissions);
            }

            session.update(node);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Grant recursively
     */
    @SuppressWarnings("unchecked")
    private int grantUserPermissionsInDepth(final Session session,
            final NodeBase node, final String user, final int permissions)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        int total = grantUserPermissions(session, node, user, permissions, true);

        // Calculate children nodes
        final String qs = "from NodeBase nb where nb.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", node.getUuid());
        final List<NodeBase> ret = q.list();

        // Security Check
        SecurityHelper.pruneNodeList(ret);

        for (final NodeBase child : ret) {
            total += grantUserPermissionsInDepth(session, child, user,
                    permissions);
        }

        return total;
    }

    /**
     * Revoke user permissions
     */
    public void revokeUserPermissions(final String uuid, final String user,
            final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("revokeUserPermissions({})", uuid);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Root node
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);

            if (recursive) {
                final int total = revokeUserPermissionsInDepth(session, node,
                        user, permissions);
                log.info("revokeUserPermissions.Total: {}", total);
            } else {
                revokeUserPermissions(session, node, user, permissions, false);
            }

            HibernateUtil.commit(tx);
            log.debug("revokeUserPermissions: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Revoke user permissions
     */
    private int revokeUserPermissions(final Session session,
            final NodeBase node, final String user, final int permissions,
            final boolean recursive) throws PathNotFoundException,
            AccessDeniedException, DatabaseException, HibernateException {
        // log.info("revokeUserPermissions({})", node.getUuid());
        boolean canModify = true;

        // Security Check
        if (recursive) {
            canModify = SecurityHelper.isGranted(node, Permission.READ)
                    && SecurityHelper.isGranted(node, Permission.SECURITY);
        } else {
            SecurityHelper.checkRead(node);
            SecurityHelper.checkSecurity(node);
        }

        if (canModify) {
            final Integer currentPermissions = node.getUserPermissions().get(
                    user);

            if (currentPermissions != null) {
                final Integer perms = ~permissions & currentPermissions;

                if (perms == Permission.NONE) {
                    node.getUserPermissions().remove(user);
                } else {
                    node.getUserPermissions().put(user, perms);
                }
            }

            session.update(node);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Revoke recursively
     */
    @SuppressWarnings("unchecked")
    private int revokeUserPermissionsInDepth(final Session session,
            final NodeBase node, final String user, final int permissions)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        int total = revokeUserPermissions(session, node, user, permissions,
                true);

        // Calculate children nodes
        final String qs = "from NodeBase nb where nb.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", node.getUuid());
        final List<NodeBase> ret = q.list();

        // Security Check
        SecurityHelper.pruneNodeList(ret);

        for (final NodeBase child : ret) {
            total += revokeUserPermissionsInDepth(session, child, user,
                    permissions);
        }

        return total;
    }

    /**
     * Get role permissions
     */
    public Map<String, Integer> getRolePermissions(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("getRolePermissions({})", uuid);
        Map<String, Integer> ret = new HashMap<String, Integer>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            if (node != null) {
                Hibernate.initialize(ret = node.getRolePermissions());
            }

            HibernateUtil.commit(tx);
            log.debug("getRolePermissions: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Grant role permissions
     */
    public void grantRolePermissions(final String uuid, final String role,
            final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("grantRolePermissions({}, {}, {}, {})", new Object[] { uuid,
                role, permissions, recursive });
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Root node
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);

            if (recursive) {
                final int total = grantRolePermissionsInDepth(session, node,
                        role, permissions);
                log.info("grantRolePermissions.Total: {}", total);
            } else {
                grantRolePermissions(session, node, role, permissions, false);
            }

            HibernateUtil.commit(tx);
            log.debug("grantRolePermissions: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Grant role permissions
     */
    private int grantRolePermissions(final Session session,
            final NodeBase node, final String role, final int permissions,
            final boolean recursive) throws PathNotFoundException,
            AccessDeniedException, DatabaseException, HibernateException {
        // log.info("grantRolePermissions({})", node.getUuid());
        boolean canModify = true;

        // Security Check
        if (recursive) {
            canModify = SecurityHelper.isGranted(node, Permission.READ)
                    && SecurityHelper.isGranted(node, Permission.SECURITY);
        } else {
            SecurityHelper.checkRead(node);
            SecurityHelper.checkSecurity(node);
        }

        if (canModify) {
            final Integer currentPermissions = node.getRolePermissions().get(
                    role);

            if (currentPermissions == null) {
                node.getRolePermissions().put(role, permissions);
            } else {
                node.getRolePermissions().put(role,
                        permissions | currentPermissions);
            }

            session.update(node);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Grant recursively
     */
    @SuppressWarnings("unchecked")
    private int grantRolePermissionsInDepth(final Session session,
            final NodeBase node, final String role, final int permissions)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        int total = grantRolePermissions(session, node, role, permissions, true);

        // Calculate children nodes
        final String qs = "from NodeBase nb where nb.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", node.getUuid());
        final List<NodeBase> ret = q.list();

        // Security Check
        SecurityHelper.pruneNodeList(ret);

        for (final NodeBase child : ret) {
            total += grantRolePermissionsInDepth(session, child, role,
                    permissions);
        }

        return total;
    }

    /**
     * Revoke role permissions
     */
    public void revokeRolePermissions(final String uuid, final String role,
            final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("revokeRolePermissions({}, {}, {}, {})", new Object[] { uuid,
                role, permissions, recursive });
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Root node
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);

            if (recursive) {
                final int total = revokeRolePermissionsInDepth(session, node,
                        role, permissions);
                log.info("revokeRolePermissions.Total: {}", total);
            } else {
                revokeRolePermissions(session, node, role, permissions, false);
            }

            HibernateUtil.commit(tx);
            log.debug("revokeRolePermissions: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Revoke role permissions
     */
    private int revokeRolePermissions(final Session session,
            final NodeBase node, final String role, final int permissions,
            final boolean recursive) throws PathNotFoundException,
            AccessDeniedException, DatabaseException, HibernateException {
        // log.info("revokeRolePermissions({})", node.getUuid());
        boolean canModify = true;

        // Security Check
        if (recursive) {
            canModify = SecurityHelper.isGranted(node, Permission.READ)
                    && SecurityHelper.isGranted(node, Permission.SECURITY);
        } else {
            SecurityHelper.checkRead(node);
            SecurityHelper.checkSecurity(node);
        }

        if (canModify) {
            final Integer currentPermissions = node.getRolePermissions().get(
                    role);

            if (currentPermissions != null) {
                final Integer perms = ~permissions & currentPermissions;

                if (perms == Permission.NONE) {
                    node.getRolePermissions().remove(role);
                } else {
                    node.getRolePermissions().put(role, perms);
                }
            }

            session.update(node);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Revoke recursively
     */
    @SuppressWarnings("unchecked")
    private int revokeRolePermissionsInDepth(final Session session,
            final NodeBase node, final String role, final int permissions)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        int total = revokeRolePermissions(session, node, role, permissions,
                true);

        // Calculate children nodes
        final String qs = "from NodeBase nb where nb.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", node.getUuid());
        final List<NodeBase> ret = q.list();

        // Security Check
        SecurityHelper.pruneNodeList(ret);

        for (final NodeBase child : ret) {
            total += revokeRolePermissionsInDepth(session, child, role,
                    permissions);
        }

        return total;
    }

    /**
     * Change security of multiples nodes
     */
    public void changeSecurity(final String uuid,
            final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("changeSecurity({}, {}, {}, {})", new Object[] { uuid,
                grantUsers, revokeUsers, grantRoles, revokeRoles, recursive });
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Root node
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);

            if (recursive) {
                final int total = changeSecurityInDepth(session, node,
                        grantUsers, revokeUsers, grantRoles, revokeRoles);
                log.info("changeSecurity.Total: {}", total);
            } else {
                changeSecurity(session, node, grantUsers, revokeUsers,
                        grantRoles, revokeRoles, false);
            }

            HibernateUtil.commit(tx);
            log.debug("grantRolePermissions: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Change security.
     */
    public int changeSecurity(final Session session, final NodeBase node,
            final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        log.debug("changeSecurity({}, {}, {}, {}, {})",
                new Object[] { node.getUuid(), grantUsers, revokeUsers,
                        grantRoles, revokeRoles });
        boolean canModify = true;

        // Security Check
        if (recursive) {
            canModify = SecurityHelper.isGranted(node, Permission.READ)
                    && SecurityHelper.isGranted(node, Permission.SECURITY);
        } else {
            SecurityHelper.checkRead(node);
            SecurityHelper.checkSecurity(node);
        }

        if (canModify) {
            // Grant Users
            for (final Entry<String, Integer> userGrant : grantUsers.entrySet()) {
                final Integer currentPermissions = node.getUserPermissions()
                        .get(userGrant.getKey());

                if (currentPermissions == null) {
                    node.getUserPermissions().put(userGrant.getKey(),
                            userGrant.getValue());
                } else {
                    node.getUserPermissions().put(userGrant.getKey(),
                            userGrant.getValue() | currentPermissions);
                }
            }

            // Revoke Users
            for (final Entry<String, Integer> userRevoke : revokeUsers
                    .entrySet()) {
                final Integer currentPermissions = node.getUserPermissions()
                        .get(userRevoke.getKey());

                if (currentPermissions != null) {
                    final Integer newPermissions = ~userRevoke.getValue()
                            & currentPermissions;

                    if (newPermissions == Permission.NONE) {
                        node.getUserPermissions().remove(userRevoke.getKey());
                    } else {
                        node.getUserPermissions().put(userRevoke.getKey(),
                                newPermissions);
                    }
                }
            }

            // Grant Roles
            for (final Entry<String, Integer> roleGrant : grantRoles.entrySet()) {
                final Integer currentPermissions = node.getRolePermissions()
                        .get(roleGrant.getKey());

                if (currentPermissions == null) {
                    node.getRolePermissions().put(roleGrant.getKey(),
                            roleGrant.getValue());
                } else {
                    node.getRolePermissions().put(roleGrant.getKey(),
                            roleGrant.getValue() | currentPermissions);
                }
            }

            // Revoke Roles
            for (final Entry<String, Integer> roleRevoke : revokeRoles
                    .entrySet()) {
                final Integer currentPermissions = node.getRolePermissions()
                        .get(roleRevoke.getKey());

                if (currentPermissions != null) {
                    final Integer newPermissions = ~roleRevoke.getValue()
                            & currentPermissions;

                    if (newPermissions == Permission.NONE) {
                        node.getRolePermissions().remove(roleRevoke.getKey());
                    } else {
                        node.getRolePermissions().put(roleRevoke.getKey(),
                                newPermissions);
                    }
                }
            }

            session.update(node);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Change security recursively
     */
    @SuppressWarnings("unchecked")
    public int changeSecurityInDepth(final Session session,
            final NodeBase node, final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, HibernateException {
        int total = changeSecurity(session, node, grantUsers, revokeUsers,
                grantRoles, revokeRoles, true);

        // Calculate children nodes
        final String qs = "from NodeBase nb where nb.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", node.getUuid());
        final List<NodeBase> ret = q.list();

        // Security Check
        SecurityHelper.pruneNodeList(ret);

        for (final NodeBase child : ret) {
            total += changeSecurityInDepth(session, child, grantUsers,
                    revokeUsers, grantRoles, revokeRoles);
        }

        return total;
    }

    /**
     * Add category to node
     */
    public void addCategory(final String uuid, final String catId)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("addCategory({}, {})", uuid, catId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            if (!node.getCategories().contains(catId)) {
                node.getCategories().add(catId);
            }

            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("addCategory: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Remove category from node
     */
    public void removeCategory(final String uuid, final String catId)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("removeCategory({}, {})", uuid, catId);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            node.getCategories().remove(catId);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("removeCategory: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Test for category in a node
     */
    public boolean hasCategory(final String uuid, final String catId)
            throws PathNotFoundException, DatabaseException {
        log.debug("hasCategory({}, {})", uuid, catId);
        Session session = null;
        Transaction tx = null;
        boolean check;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            check = node.getCategories().contains(catId);
            HibernateUtil.commit(tx);

            log.debug("hasCategory: {}", check);
            return check;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Test for category in use
     */
    public boolean isCategoryInUse(final String catUuid)
            throws DatabaseException {
        log.debug("isCategoryInUse({}, {})", catUuid);
        final String qs = "from NodeBase nb where :category in elements(nb.categories)";
        Session session = null;
        Transaction tx = null;
        boolean check;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            final Query q = session.createQuery(qs);
            q.setString("category", catUuid);
            check = !q.list().isEmpty();

            HibernateUtil.commit(tx);
            log.debug("isCategoryInUse: {}", check);
            return check;
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Add keyword to node
     */
    public void addKeyword(final String uuid, final String keyword)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("addKeyword({}, {})", uuid, keyword);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            if (!node.getKeywords().contains(keyword)) {
                node.getKeywords().add(keyword);
            }

            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("addKeyword: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Remove keyword from node
     */
    public void removeKeyword(final String uuid, final String keyword)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("removeCategory({}, {})", uuid, keyword);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            node.getKeywords().remove(keyword);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("removeCategory: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Test for category in a node
     */
    public boolean hasKeyword(final String uuid, final String keyword)
            throws PathNotFoundException, DatabaseException {
        log.debug("hasKeyword({}, {})", uuid, keyword);
        Session session = null;
        Transaction tx = null;
        boolean check;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            check = node.getKeywords().contains(keyword);
            HibernateUtil.commit(tx);

            log.debug("hasKeyword: {}", check);
            return check;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Subscribe user to node
     */
    public void subscribe(final String uuid, final String user)
            throws PathNotFoundException, DatabaseException {
        log.debug("subscribe({}, {})", uuid, user);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            if (!node.getSubscriptors().contains(user)) {
                node.getSubscriptors().add(user);
            }

            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("subscribe: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Remove user subscription
     */
    public void unsubscribe(final String uuid, final String user)
            throws PathNotFoundException, DatabaseException {
        log.debug("unsubscribe({}, {})", uuid, user);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            node.getSubscriptors().remove(user);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("unsubscribe: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Get node subscriptors
     */
    public Set<String> getSubscriptors(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("getSubscriptors({})", uuid);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            final Set<String> subscriptors = node.getSubscriptors();
            Hibernate.initialize(subscriptors);
            log.debug("getSubscriptors: {}", subscriptors);
            return subscriptors;
        } catch (final PathNotFoundException e) {
            throw e;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Set node script
     */
    public void setScript(final String uuid, final String code)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("setScript({}, {})", uuid, code);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            node.setScripting(true);
            node.setScriptCode(code);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("setScript: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Remove node script
     */
    public void removeScript(final String uuid) throws PathNotFoundException,
            AccessDeniedException, DatabaseException {
        log.debug("removeScript({}, {})", uuid);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            node.setScripting(false);
            node.setScriptCode(null);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("removeScript: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Obtain script code
     */
    public String getScript(final String uuid) throws PathNotFoundException,
            DatabaseException {
        log.debug("setScript({}, {})", uuid);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            final String code = node.getScriptCode();
            log.debug("setScript: {}", code);
            return code;
        } catch (final PathNotFoundException e) {
            throw e;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get parent node uuid
     */
    public String getParentUuid(final String uuid) throws DatabaseException {
        log.debug("getParentUuid({})", uuid);
        final String qs = "select nb.parent from NodeBase nb where nb.uuid=:uuid";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("uuid", uuid);
            final String parent = (String) q.setMaxResults(1).uniqueResult();
            log.debug("getParentUuid: {}", parent);
            return parent;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get parent node
     */
    public NodeBase getParentNode(final String uuid) throws DatabaseException {
        log.debug("getParentNode({})", uuid);
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final NodeBase parentNode = getParentNode(session, uuid);
            initializeSecurity(parentNode);
            log.debug("getParentNode: {}", parentNode);
            return parentNode;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get parent node
     */
    public NodeBase getParentNode(final Session session, final String uuid)
            throws HibernateException {
        log.debug("getParentNode({}, {})", session, uuid);
        final String qs = "from NodeBase nb1 where nb1.uuid = (select nb2.parent from NodeBase nb2 where nb2.uuid=:uuid)";
        final Query q = session.createQuery(qs);
        q.setString("uuid", uuid);
        final NodeBase parentNode = (NodeBase) q.setMaxResults(1)
                .uniqueResult();
        log.debug("getParentNode: {}", parentNode);
        return parentNode;
    }

    /**
     * Get result node count.
     * 
     * @see com.openkm.module.db.DbStatsModule
     */
    public long getCount(final String nodeType) throws PathNotFoundException,
            DatabaseException {
        log.debug("getCount({})", new Object[] { nodeType });
        final String qs = "select count(*) from " + nodeType + " nt";
        Session session = null;
        Transaction tx = null;
        long total = 0;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            final Query q = session.createQuery(qs);
            total = (Long) q.setMaxResults(1).uniqueResult();

            HibernateUtil.commit(tx);
            log.debug("getCount: {}", total);
            return total;
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Get result node count.
     * 
     * @see com.openkm.module.db.DbStatsModule
     */
    public long getSubtreeCount(final String nodeType, final String path,
            final int depth) throws PathNotFoundException, DatabaseException {
        log.debug("getSubtreeCount({}, {}, {})", new Object[] { nodeType, path,
                depth });
        Session session = null;
        Transaction tx = null;
        long total = 0;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            final String uuid = getUuidFromPath(path);
            total = getSubtreeCountHelper(session, nodeType, uuid, depth, 1);

            HibernateUtil.commit(tx);
            log.debug("getSubtreeCount: {}", total);
            return total;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Helper method.
     */
    @SuppressWarnings("unchecked")
    private long getSubtreeCountHelper(final Session session,
            final String nodeType, final String parentUuid, final int depth,
            final int level) throws HibernateException, DatabaseException {
        log.debug("getSubtreeCountHelper({}, {}, {},  {})", new Object[] {
                nodeType, parentUuid, depth, level });
        final String qs = "from NodeBase n where n.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", parentUuid);
        final List<NodeBase> nodes = q.list();
        long total = 0;

        for (final NodeBase nBase : nodes) {
            if (nBase instanceof NodeFolder) {
                total += getSubtreeCountHelper(session, nodeType,
                        nBase.getUuid(), depth, level + 1);

                if (NodeFolder.class.getSimpleName().equals(nodeType)) {
                    if (level >= depth) {
                        total += 1;
                    }
                }
            } else if (NodeDocument.class.getSimpleName().equals(nodeType)
                    && nBase instanceof NodeDocument) {
                if (level >= depth) {
                    total += 1;
                }
            }
        }

        return total;
    }

    /**
     * Check for same node name in same parent
     * 
     * @param Session Hibernate session.
     * @param parent Parent node uuid.
     * @param name Name of the child node to test.
     * @return true if child item exists or false otherwise.
     */
    public boolean testItemExistence(final Session session,
            final String parent, final String name) throws HibernateException,
            DatabaseException {
        final String qs = "from NodeBase nb where nb.parent=:parent and nb.name=:name";
        final Query q = session.createQuery(qs);
        q.setString("parent", parent);
        q.setString("name", name);

        return !q.list().isEmpty();
    }

    /**
     * Check for same node name in same parent
     * 
     * @param Session Hibernate session.
     * @param parent Parent node uuid.
     * @param name Name of the child node to test.
     */
    public void checkItemExistence(final Session session, final String parent,
            final String name) throws PathNotFoundException,
            HibernateException, DatabaseException, ItemExistsException {
        if (testItemExistence(session, parent, name)) {
            final String path = getPathFromUuid(session, parent);
            throw new ItemExistsException(path + "/" + name);
        }
    }

    /**
     * Get node type by UUID
     */
    public String getNodeTypeByUuid(final String uuid)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final NodeBase nBase = (NodeBase) session.get(NodeBase.class, uuid);

            if (nBase == null) {
                throw new PathNotFoundException(uuid);
            }

            // Security Check
            SecurityHelper.checkRead(nBase);

            if (nBase instanceof NodeFolder) {
                return Folder.TYPE;
            } else if (nBase instanceof NodeDocument) {
                return Document.TYPE;
            } else if (nBase instanceof NodeMail) {
                return Mail.TYPE;
            } else {
                throw new RepositoryException("Unknown node type");
            }
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Add property group
     */
    public void addPropertyGroup(final String uuid, final String grpName)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.info("addPropertyGroup({}, {})", uuid, grpName);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            final RegisteredPropertyGroup rpg = (RegisteredPropertyGroup) session
                    .get(RegisteredPropertyGroup.class, grpName);

            if (rpg != null) {
                for (final String propName : rpg.getProperties().keySet()) {
                    final NodeProperty nodProp = new NodeProperty();
                    nodProp.setNode(node);
                    nodProp.setGroup(rpg.getName());
                    nodProp.setName(propName);
                    boolean alreadyAssigned = false;

                    for (final NodeProperty np : node.getProperties()) {
                        if (np.getGroup().equals(nodProp.getGroup())
                                && np.getName().equals(nodProp.getName())) {
                            alreadyAssigned = true;
                            break;
                        }
                    }

                    if (!alreadyAssigned) {
                        node.getProperties().add(nodProp);
                    }
                }
            } else {
                HibernateUtil.rollback(tx);
                throw new RepositoryException("Property Group not registered: "
                        + grpName);
            }

            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("addPropertyGroup: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Remove property group
     */
    public void removePropertyGroup(final String uuid, final String grpName)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException {
        log.debug("removePropertyGroup({}, {})", uuid, grpName);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            for (final Iterator<NodeProperty> it = node.getProperties()
                    .iterator(); it.hasNext();) {
                final NodeProperty nodProp = it.next();

                if (grpName.equals(nodProp.getGroup())) {
                    it.remove();
                    session.delete(nodProp);
                }
            }

            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("removePropertyGroup: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Get assigned property groups
     */
    @SuppressWarnings("unchecked")
    public List<String> getPropertyGroups(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("getPropertyGroups({}, {})", uuid);
        final String qs = "select distinct(nbp.group) from NodeBase nb join nb.properties nbp where nb.uuid=:uuid";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            final Query q = session.createQuery(qs);
            q.setString("uuid", uuid);
            final List<String> ret = q.list();
            HibernateUtil.commit(tx);
            log.debug("getPropertyGroups: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Get properties from property group
     */
    public Map<String, String> getProperties(final String uuid,
            final String grpName) throws PathNotFoundException,
            DatabaseException {
        log.debug("getProperties({}, {})", uuid, grpName);
        final Map<String, String> ret = new HashMap<String, String>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            for (final NodeProperty nodProp : node.getProperties()) {
                if (grpName.equals(nodProp.getGroup())) {
                    ret.put(nodProp.getName(), nodProp.getValue());
                }
            }

            HibernateUtil.commit(tx);
            log.debug("getProperties: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Get single property value from property group
     */
    public String getProperty(final String uuid, final String grpName,
            final String propName) throws PathNotFoundException,
            DatabaseException {
        log.debug("getProperty({}, {}, {})", new Object[] { uuid, grpName,
                propName });
        String propValue = null;
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);

            for (final NodeProperty nodProp : node.getProperties()) {
                if (grpName.equals(nodProp.getGroup())
                        && propName.equals(nodProp.getName())) {
                    propValue = nodProp.getValue();
                    break;
                }
            }

            HibernateUtil.commit(tx);
            log.debug("getProperty: {}", propValue);
            return propValue;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Set properties from property group
     */
    public Map<String, String> setProperties(final String uuid,
            final String grpName, final Map<String, String> properties)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("setProperties({}, {}, {})", new Object[] { uuid, grpName,
                properties });
        final Map<String, String> ret = new HashMap<String, String>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase node = (NodeBase) session.load(NodeBase.class, uuid);
            SecurityHelper.checkRead(node);
            SecurityHelper.checkWrite(node);

            final Set<NodeProperty> tmp = new HashSet<NodeProperty>();

            for (final Entry<String, String> prop : properties.entrySet()) {
                boolean alreadyAssigned = false;

                // Set new property group values
                for (final NodeProperty nodProp : node.getProperties()) {
                    if (grpName.equals(nodProp.getGroup())) {
                        if (prop.getKey().equals(nodProp.getName())) {
                            log.debug(
                                    "UPDATE - Group: {}, Property: {}, Value: {}",
                                    new Object[] { grpName, prop.getKey(),
                                            prop.getValue() });
                            nodProp.setValue(prop.getValue());
                            alreadyAssigned = true;

                            // TODO: Workaround for Hibernate Search
                            tmp.add(nodProp);
                        }
                    }
                }

                if (!alreadyAssigned) {
                    log.debug(
                            "ADD - Group: {}, Property: {}, Value: {}",
                            new Object[] { grpName, prop.getKey(),
                                    prop.getValue() });
                    final NodeProperty nodProp = new NodeProperty();
                    nodProp.setNode(node);
                    nodProp.setGroup(grpName);
                    nodProp.setName(prop.getKey());
                    nodProp.setValue(prop.getValue());

                    // TODO: Workaround for Hibernate Search
                    tmp.add(nodProp);
                }
            }

            node.setProperties(tmp);
            session.update(node);
            HibernateUtil.commit(tx);
            log.debug("setProperties: {}", ret);
            return ret;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
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
     * Force initialization of a proxy
     */
    public void initialize(final NodeBase nBase) {
        if (nBase != null) {
            Hibernate.initialize(nBase);
            Hibernate.initialize(nBase.getKeywords());
            Hibernate.initialize(nBase.getCategories());
            Hibernate.initialize(nBase.getSubscriptors());
            Hibernate.initialize(nBase.getUserPermissions());
            Hibernate.initialize(nBase.getRolePermissions());
        }
    }

    /**
     * Force initialization of a proxy
     */
    public void initializeSecurity(final NodeBase nBase) {
        if (nBase != null) {
            Hibernate.initialize(nBase);
            Hibernate.initialize(nBase.getUserPermissions());
            Hibernate.initialize(nBase.getRolePermissions());
        }
    }
}
