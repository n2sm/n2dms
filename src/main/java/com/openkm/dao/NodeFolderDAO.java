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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Permission;
import com.openkm.cache.UserItemsManager;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.dao.bean.NodeBase;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.module.db.stuff.DbAccessManager;
import com.openkm.module.db.stuff.SecurityHelper;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.UserActivity;

public class NodeFolderDAO {
    private static Logger log = LoggerFactory.getLogger(NodeFolderDAO.class);

    private static NodeFolderDAO single = new NodeFolderDAO();

    private NodeFolderDAO() {
    }

    public static NodeFolderDAO getInstance() {
        return single;
    }

    /**
     * Create base node
     */
    public void createBase(final NodeFolder nFolder) throws DatabaseException {
        log.debug("createBase({})", nFolder);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.save(nFolder);
            HibernateUtil.commit(tx);
            log.debug("createBase: void");
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Create node
     */
    public void create(final NodeFolder nFolder) throws PathNotFoundException,
            AccessDeniedException, ItemExistsException, DatabaseException {
        log.debug("create({})", nFolder);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase parentNode = (NodeBase) session.load(NodeBase.class,
                    nFolder.getParent());
            SecurityHelper.checkRead(parentNode);
            SecurityHelper.checkWrite(parentNode);

            // Check for same folder name in same parent
            NodeBaseDAO.getInstance().checkItemExistence(session,
                    nFolder.getParent(), nFolder.getName());

            session.save(nFolder);
            HibernateUtil.commit(tx);
            log.debug("create: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final ItemExistsException e) {
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
     * Find by parent
     */
    @SuppressWarnings("unchecked")
    public List<NodeFolder> findByParent(final String parentUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("findByParent({})", parentUuid);
        final String qs = "from NodeFolder nf where nf.parent=:parent order by nf.name";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            if (!Config.ROOT_NODE_UUID.equals(parentUuid)) {
                final NodeBase parentNode = (NodeBase) session.load(
                        NodeBase.class, parentUuid);
                SecurityHelper.checkRead(parentNode);
            }

            final Query q = session.createQuery(qs);
            q.setString("parent", parentUuid);
            final List<NodeFolder> ret = q.list();

            // Security Check
            SecurityHelper.pruneNodeList(ret);

            initialize(ret);
            HibernateUtil.commit(tx);
            log.debug("findByParent: {}", ret);
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
     * Find by pk
     */
    public NodeFolder findByPk(final String uuid) throws PathNotFoundException,
            DatabaseException {
        log.debug("findByPk({})", uuid);
        final String qs = "from NodeFolder nf where nf.uuid=:uuid";
        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("uuid", uuid);
            final NodeFolder nFld = (NodeFolder) q.setMaxResults(1)
                    .uniqueResult();

            if (nFld == null) {
                throw new PathNotFoundException(uuid);
            }

            // Security Check
            SecurityHelper.checkRead(nFld);

            initialize(nFld);
            log.debug("findByPk: {}", nFld);
            return nFld;
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Search nodes by category
     */
    @SuppressWarnings("unchecked")
    public List<NodeFolder> findByCategory(final String catUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("findByCategory({})", catUuid);
        final String qs = "from NodeFolder nf where :category in elements(nf.categories) order by nf.name";
        List<NodeFolder> ret = new ArrayList<NodeFolder>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase catNode = (NodeBase) session.load(NodeBase.class,
                    catUuid);
            SecurityHelper.checkRead(catNode);

            final Query q = session.createQuery(qs);
            q.setString("category", catUuid);
            ret = q.list();

            // Security Check
            SecurityHelper.pruneNodeList(ret);

            initialize(ret);
            HibernateUtil.commit(tx);
            log.debug("findByCategory: {}", ret);
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
     * Search nodes by keyword
     */
    @SuppressWarnings("unchecked")
    public List<NodeFolder> findByKeyword(final String keyword)
            throws DatabaseException {
        log.debug("findByKeyword({})", keyword);
        final String qs = "from NodeFolder nf where :keyword in elements(nf.keywords) order by nf.name";
        List<NodeFolder> ret = new ArrayList<NodeFolder>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            final Query q = session.createQuery(qs);
            q.setString("keyword", keyword);
            ret = q.list();

            // Security Check
            SecurityHelper.pruneNodeList(ret);

            initialize(ret);
            HibernateUtil.commit(tx);
            log.debug("findByKeyword: {}", ret);
            return ret;
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
     * Search nodes by property value
     */
    @SuppressWarnings("unchecked")
    public List<NodeFolder> findByPropertyValue(final String group,
            final String property, final String value) throws DatabaseException {
        log.debug("findByPropertyValue({}, {}, {})", new Object[] { group,
                property, value });
        final String qs = "select nb from NodeFolder nb join nb.properties nbp where nbp.group=:group and nbp.name=:property and nbp.value like :value";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            final Query q = session.createQuery(qs);
            q.setString("group", group);
            q.setString("property", property);
            q.setString("value", "%" + value + "%");
            final List<NodeFolder> ret = q.list();

            // Security Check
            SecurityHelper.pruneNodeList(ret);

            initialize(ret);
            HibernateUtil.commit(tx);
            log.debug("findByPropertyValue: {}", ret);
            return ret;
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
     * Check if folder has childs
     */
    @SuppressWarnings("unchecked")
    public boolean hasChildren(final String parentUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("hasChildren({})", parentUuid);
        final String qs = "from NodeFolder nf where nf.parent=:parent";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            if (!Config.ROOT_NODE_UUID.equals(parentUuid)) {
                final NodeBase parentNode = (NodeBase) session.load(
                        NodeBase.class, parentUuid);
                SecurityHelper.checkRead(parentNode);
            }

            final Query q = session.createQuery(qs);
            q.setString("parent", parentUuid);
            final List<NodeFolder> nodeList = q.list();

            // Security Check
            SecurityHelper.pruneNodeList(nodeList);

            final boolean ret = !nodeList.isEmpty();
            HibernateUtil.commit(tx);
            log.debug("hasChildren: {}", ret);
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
     * Rename folder
     */
    public NodeFolder rename(final String uuid, final String newName)
            throws PathNotFoundException, AccessDeniedException,
            ItemExistsException, DatabaseException {
        log.debug("rename({}, {})", uuid, newName);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeBase parentNode = NodeBaseDAO.getInstance()
                    .getParentNode(session, uuid);
            SecurityHelper.checkRead(parentNode);
            SecurityHelper.checkWrite(parentNode);
            final NodeFolder nFld = (NodeFolder) session.load(NodeFolder.class,
                    uuid);
            SecurityHelper.checkRead(nFld);
            SecurityHelper.checkWrite(nFld);

            // Check for same folder name in same parent
            NodeBaseDAO.getInstance().checkItemExistence(session,
                    nFld.getParent(), newName);

            nFld.setName(newName);
            session.update(nFld);
            initialize(nFld);
            HibernateUtil.commit(tx);
            log.debug("rename: {}", nFld);
            return nFld;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final ItemExistsException e) {
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
     * Move folder
     */
    public void move(final String uuid, final String dstUuid)
            throws PathNotFoundException, AccessDeniedException,
            ItemExistsException, DatabaseException {
        log.debug("move({}, {})", uuid, dstUuid);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeFolder nDstFld = (NodeFolder) session.load(
                    NodeFolder.class, dstUuid);
            SecurityHelper.checkRead(nDstFld);
            SecurityHelper.checkWrite(nDstFld);
            final NodeFolder nFld = (NodeFolder) session.load(NodeFolder.class,
                    uuid);
            SecurityHelper.checkRead(nFld);
            SecurityHelper.checkWrite(nFld);

            // Check if move to itself
            if (uuid.equals(dstUuid)) {
                final String dstPath = NodeBaseDAO.getInstance()
                        .getPathFromUuid(dstUuid);
                throw new ItemExistsException(dstPath);
            }

            // Check for same folder name in same parent
            NodeBaseDAO.getInstance().checkItemExistence(session, dstUuid,
                    nFld.getName());

            // Check if context changes
            if (!nDstFld.getContext().equals(nFld.getContext())) {
                nFld.setContext(nDstFld.getContext());

                // Need recursive context changes
                moveHelper(session, uuid, nDstFld.getContext());
            }

            nFld.setParent(dstUuid);
            session.update(nFld);
            HibernateUtil.commit(tx);
            log.debug("move: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final ItemExistsException e) {
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
     * Delete folder
     */
    public void delete(final String name, final String uuid,
            final String trashUuid) throws PathNotFoundException,
            AccessDeniedException, DatabaseException {
        log.debug("delete({}, {}, {})", new Object[] { name, uuid, trashUuid });
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeFolder nTrashFld = (NodeFolder) session.load(
                    NodeFolder.class, trashUuid);
            SecurityHelper.checkRead(nTrashFld);
            SecurityHelper.checkWrite(nTrashFld);
            final NodeFolder nFld = (NodeFolder) session.load(NodeFolder.class,
                    uuid);
            SecurityHelper.checkRead(nFld);
            SecurityHelper.checkWrite(nFld);

            // Test if already exists a folder with the same name in the trash
            String testName = name;

            for (int i = 1; NodeBaseDAO.getInstance().testItemExistence(
                    session, trashUuid, testName); i++) {
                // log.info("Trying with: {}", testName);
                testName = name + " (" + i + ")";
            }

            // Need recursive context changes
            moveHelper(session, uuid, nTrashFld.getContext());

            nFld.setContext(nTrashFld.getContext());
            nFld.setParent(trashUuid);
            nFld.setName(testName);
            session.update(nFld);
            HibernateUtil.commit(tx);
            log.debug("delete: void");
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

    @SuppressWarnings("unchecked")
    private void moveHelper(final Session session, final String parentUuid,
            final String newContext) throws HibernateException {
        final String qs = "from NodeBase nf where nf.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", parentUuid);

        for (final NodeBase nBase : (List<NodeBase>) q.list()) {
            nBase.setContext(newContext);

            if (nBase instanceof NodeFolder || nBase instanceof NodeMail) {
                moveHelper(session, nBase.getUuid(), newContext);
            }
        }
    }

    /**
     * Get categories from node
     */
    public Set<NodeFolder> resolveCategories(final Set<String> categories)
            throws DatabaseException {
        log.debug("resolveCategories({})", categories);
        final Set<NodeFolder> ret = new HashSet<NodeFolder>();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            final DbAccessManager am = SecurityHelper.getAccessManager();

            for (final String catUuid : categories) {
                final NodeFolder nFld = (NodeFolder) session.load(
                        NodeFolder.class, catUuid);

                // Security Check
                if (am.isGranted(nFld, Permission.READ)) {
                    initialize(nFld);
                    ret.add(nFld);
                }
            }

            log.debug("resolveCategories: {}", ret);
            return ret;
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
     * Purge in depth. Respect the parameter deleteBase, it means if the node nFolder should be deleted itself. This
     * parameter is present because this "purge" method is called from NrRepositoryModule.purgeTrash(String token) and
     * NrFolderModule.purge(String token, String fldPath).
     */
    public void purge(final String uuid, final boolean deleteBase)
            throws PathNotFoundException, AccessDeniedException, LockException,
            DatabaseException, IOException {
        log.debug("purgue({}, {})", uuid, deleteBase);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeFolder nFld = (NodeFolder) session.load(NodeFolder.class,
                    uuid);
            SecurityHelper.checkRead(nFld);
            SecurityHelper.checkDelete(nFld);

            purgeHelper(session, nFld, deleteBase);
            HibernateUtil.commit(tx);
            log.debug("purgue: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final IOException e) {
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
     * Purge in depth helper.
     */
    @SuppressWarnings("unchecked")
    private void purgeHelper(final Session session, final String parentUuid)
            throws PathNotFoundException, AccessDeniedException, LockException,
            IOException, DatabaseException, HibernateException {
        final String qs = "from NodeFolder nf where nf.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", parentUuid);
        final List<NodeFolder> listFolders = q.list();

        for (final NodeFolder nFld : listFolders) {
            purgeHelper(session, nFld, true);
        }
    }

    /**
     * Purge in depth helper.
     */
    private void purgeHelper(final Session session, final NodeFolder nFolder,
            final boolean deleteBase) throws PathNotFoundException,
            AccessDeniedException, LockException, IOException,
            DatabaseException, HibernateException {
        final String author = nFolder.getAuthor();

        // Security Check
        SecurityHelper.checkRead(nFolder);
        SecurityHelper.checkDelete(nFolder);

        // Delete children documents
        NodeDocumentDAO.getInstance().purgeHelper(session, nFolder.getUuid());

        // Delete children mails
        NodeMailDAO.getInstance().purgeHelper(session, nFolder.getUuid());

        // Delete children notes
        NodeNoteDAO.getInstance().purgeHelper(session, nFolder.getUuid());

        // Delete bookmarks
        BookmarkDAO.purgeBookmarksByNode(nFolder.getUuid());

        // Delete children folder
        purgeHelper(session, nFolder.getUuid());

        if (deleteBase) {
            final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                    session, nFolder.getUuid());
            final String user = PrincipalUtils.getUser();

            // Delete the node itself
            session.delete(nFolder);

            // Update user items size
            if (Config.USER_ITEM_CACHE) {
                UserItemsManager.decFolders(author, 1);
            }

            // Activity log
            UserActivity.log(user, "PURGE_FOLDER", nFolder.getUuid(), path,
                    null);
        }
    }

    /**
     * Force initialization of a proxy
     */
    public void initialize(final NodeFolder nFolder) {
        if (nFolder != null) {
            Hibernate.initialize(nFolder);
            Hibernate.initialize(nFolder.getKeywords());
            Hibernate.initialize(nFolder.getCategories());
            Hibernate.initialize(nFolder.getSubscriptors());
            Hibernate.initialize(nFolder.getUserPermissions());
            Hibernate.initialize(nFolder.getRolePermissions());
        }
    }

    /**
     * Force initialization of a proxy
     */
    private void initialize(final List<NodeFolder> nFolderList) {
        for (final NodeFolder nFolder : nFolderList) {
            initialize(nFolder);
        }
    }
}
