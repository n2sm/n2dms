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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.cache.UserItemsManager;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeDocumentVersion;
import com.openkm.module.db.stuff.FsDataStore;
import com.openkm.module.db.stuff.LockHelper;
import com.openkm.module.db.stuff.SecurityHelper;
import com.openkm.vernum.VersionNumerationAdapter;
import com.openkm.vernum.VersionNumerationFactory;

public class NodeDocumentVersionDAO extends
        GenericDAO<NodeDocumentVersion, String> {
    private static Logger log = LoggerFactory
            .getLogger(NodeDocumentVersionDAO.class);

    private static NodeDocumentVersionDAO single = new NodeDocumentVersionDAO();

    private NodeDocumentVersionDAO() {
    }

    public static NodeDocumentVersionDAO getInstance() {
        return single;
    }

    /**
     * Find by parent
     */
    @SuppressWarnings("unchecked")
    public List<NodeDocumentVersion> findByParent(final String docUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("findByParent({})", docUuid);
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent order by ndv.created";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            final List<NodeDocumentVersion> ret = q.list();
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
     * Find current document version
     */
    public NodeDocumentVersion findCurrentVersion(final String docUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("findCurrentVersion({})", docUuid);
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);

            final NodeDocumentVersion currentVersion = findCurrentVersion(
                    session, docUuid);
            HibernateUtil.commit(tx);
            log.debug("findCurrentVersion: {}", currentVersion);
            return currentVersion;
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
     * Find current document version
     */
    public NodeDocumentVersion findCurrentVersion(final Session session,
            final String docUuid) throws HibernateException {
        log.debug("findCurrentVersion({})", docUuid);
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        final Query q = session.createQuery(qs);
        q.setString("parent", docUuid);
        q.setBoolean("current", true);
        final NodeDocumentVersion currentVersion = (NodeDocumentVersion) q
                .setMaxResults(1).uniqueResult();
        return currentVersion;
    }

    /**
     * Get document version content
     * 
     * @param docUuid Id of the document to get the content.
     * This is used to enable the document preview.
     */
    public InputStream getCurrentContentByParent(final String docUuid)
            throws PathNotFoundException, AccessDeniedException,
            DatabaseException, FileNotFoundException, IOException {
        log.debug("getContent({})", docUuid);
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        Session session = null;
        Transaction tx = null;
        InputStream ret = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            q.setBoolean("current", true);
            final NodeDocumentVersion nDocVer = (NodeDocumentVersion) q
                    .setMaxResults(1).uniqueResult();

            if (FsDataStore.DATASTORE_BACKEND_FS
                    .equals(Config.REPOSITORY_DATASTORE_BACKEND)) {
                ret = FsDataStore.read(nDocVer.getUuid());
            } else {
                ret = new ByteArrayInputStream(nDocVer.getContent());
            }

            HibernateUtil.commit(tx);
            log.debug("getContent: {}", ret);
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
     * Get document version content
     */
    public InputStream getVersionContentByParent(final String docUuid,
            final String name) throws PathNotFoundException, DatabaseException,
            FileNotFoundException, IOException {
        log.debug("getContent({})", docUuid);
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.name=:name";
        Session session = null;
        Transaction tx = null;
        InputStream ret = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            q.setString("name", name);
            final NodeDocumentVersion nDocVer = (NodeDocumentVersion) q
                    .setMaxResults(1).uniqueResult();

            if (FsDataStore.DATASTORE_BACKEND_FS
                    .equals(Config.REPOSITORY_DATASTORE_BACKEND)) {
                ret = FsDataStore.read(nDocVer.getUuid());
            } else {
                ret = new ByteArrayInputStream(nDocVer.getContent());
            }

            HibernateUtil.commit(tx);
            log.debug("getContent: {}", ret);
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
     * Create or update dummy version
     */
    public NodeDocumentVersion checkin(final String user, final String comment,
            final String docUuid, final InputStream is, final long size)
            throws IOException, PathNotFoundException, AccessDeniedException,
            LockException, DatabaseException {
        log.debug("checkin({}, {}, {}, {}, {})", new Object[] { user, comment,
                docUuid, is, size });
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        final NodeDocumentVersion newDocVersion = new NodeDocumentVersion();
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);
            SecurityHelper.checkWrite(nDoc);

            // Lock Check
            LockHelper.checkWriteLock(user, nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            q.setBoolean("current", true);
            final NodeDocumentVersion curDocVersion = (NodeDocumentVersion) q
                    .setMaxResults(1).uniqueResult();
            final VersionNumerationAdapter verNumAdapter = VersionNumerationFactory
                    .getVersionNumerationAdapter();
            final String nextVersionNumber = verNumAdapter
                    .getNextVersionNumber(session, nDoc, curDocVersion);

            // Make current version obsolete
            curDocVersion.setCurrent(false);
            session.update(curDocVersion);

            // New document version
            newDocVersion.setUuid(UUID.randomUUID().toString());
            newDocVersion.setParent(docUuid);
            newDocVersion.setName(nextVersionNumber);
            newDocVersion.setAuthor(user);
            newDocVersion.setComment(comment);
            newDocVersion.setCurrent(true);
            newDocVersion.setCreated(Calendar.getInstance());
            newDocVersion.setSize(size);
            newDocVersion.setMimeType(curDocVersion.getMimeType());
            newDocVersion.setPrevious(curDocVersion.getUuid());

            // Persist file in datastore
            FsDataStore.persist(newDocVersion, is);

            session.save(newDocVersion);

            // Set document checkout status to false
            nDoc.setLastModified(newDocVersion.getCreated());
            nDoc.setCheckedOut(false);

            // Text extraction
            nDoc.setText("");
            nDoc.setTextExtracted(false);

            // Remove lock
            NodeDocumentDAO.getInstance().unlock(session, user, nDoc, false);

            session.update(nDoc);
            HibernateUtil.commit(tx);

            log.debug("checkin: {}", newDocVersion);
            return newDocVersion;
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final LockException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);

            // What happen when create fails? This datastore file should be deleted!
            FsDataStore.delete(newDocVersion.getUuid());
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    /**
     * Set version content.
     */
    public void setContent(final String docUuid, final InputStream is,
            final long size) throws IOException, PathNotFoundException,
            AccessDeniedException, LockException, DatabaseException {
        log.debug("setContent({}, {}, {})", new Object[] { docUuid, is, size });
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);
            SecurityHelper.checkWrite(nDoc);

            // Lock Check
            LockHelper.checkWriteLock(nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            q.setBoolean("current", true);

            // Text extraction
            nDoc.setText("");
            nDoc.setTextExtracted(false);
            session.update(nDoc);

            // Update version content
            final NodeDocumentVersion curDocVersion = (NodeDocumentVersion) q
                    .setMaxResults(1).uniqueResult();
            curDocVersion.setText("");
            curDocVersion.setSize(size);
            session.update(curDocVersion);

            // Persist file in datastore
            FsDataStore.persist(curDocVersion, is);

            HibernateUtil.commit(tx);
            log.debug("setContent: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final LockException e) {
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
     * Set a document version as current.
     */
    public void restoreVersion(final String docUuid, final String versionId)
            throws PathNotFoundException, AccessDeniedException, LockException,
            DatabaseException {
        log.debug("restoreVersion({}, {})", new Object[] { docUuid, versionId });
        final String qsCurrent = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        final String qsName = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.name=:name";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);
            SecurityHelper.checkWrite(nDoc);

            // Lock Check
            LockHelper.checkWriteLock(nDoc);

            final Query qCurrent = session.createQuery(qsCurrent);
            qCurrent.setString("parent", docUuid);
            qCurrent.setBoolean("current", true);

            final Query qName = session.createQuery(qsName);
            qName.setString("parent", docUuid);
            qName.setString("name", versionId);

            // Update current version
            final NodeDocumentVersion curDocVersion = (NodeDocumentVersion) qCurrent
                    .setMaxResults(1).uniqueResult();
            final NodeDocumentVersion namDocVersion = (NodeDocumentVersion) qName
                    .setMaxResults(1).uniqueResult();
            curDocVersion.setCurrent(false);
            namDocVersion.setCurrent(true);
            session.update(namDocVersion);
            session.update(curDocVersion);

            // Text extraction
            nDoc.setText(namDocVersion.getText());
            session.update(nDoc);

            HibernateUtil.commit(tx);
            log.debug("restoreVersion: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final LockException e) {
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
     * Purge all non-current document version history nodes
     */
    @SuppressWarnings("unchecked")
    public void purgeVersionHistory(final String docUuid)
            throws PathNotFoundException, AccessDeniedException, LockException,
            IOException, DatabaseException {
        log.debug("purgeVersionHistory({})", docUuid);
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent and ndv.current=:current";
        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Security Check
            final NodeDocument nDoc = (NodeDocument) session.load(
                    NodeDocument.class, docUuid);
            SecurityHelper.checkRead(nDoc);
            SecurityHelper.checkWrite(nDoc);

            // Lock Check
            LockHelper.checkWriteLock(nDoc);

            final Query q = session.createQuery(qs);
            q.setString("parent", docUuid);
            q.setBoolean("current", false);

            // Remove non-current version nodes
            for (final NodeDocumentVersion nDocVer : (List<NodeDocumentVersion>) q
                    .list()) {
                final String author = nDocVer.getAuthor();
                final long size = nDocVer.getSize();

                if (FsDataStore.DATASTORE_BACKEND_FS
                        .equals(Config.REPOSITORY_DATASTORE_BACKEND)) {
                    FsDataStore.delete(nDocVer.getUuid());
                }

                session.delete(nDocVer);

                // Update user items size
                if (Config.USER_ITEM_CACHE) {
                    UserItemsManager.decSize(author, size);
                }
            }

            HibernateUtil.commit(tx);
            log.debug("purgeVersionHistory: void");
        } catch (final PathNotFoundException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final AccessDeniedException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final DatabaseException e) {
            HibernateUtil.rollback(tx);
            throw e;
        } catch (final LockException e) {
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
     * Purge in depth helper
     */
    @SuppressWarnings("unchecked")
    public void purgeHelper(final Session session, final String parentUuid)
            throws HibernateException, IOException {
        final String qs = "from NodeDocumentVersion ndv where ndv.parent=:parent";
        final Query q = session.createQuery(qs);
        q.setString("parent", parentUuid);
        final List<NodeDocumentVersion> listDocVersions = q.list();

        for (final NodeDocumentVersion nDocVer : listDocVersions) {
            final String author = nDocVer.getAuthor();
            final long size = nDocVer.getSize();

            if (FsDataStore.DATASTORE_BACKEND_FS
                    .equals(Config.REPOSITORY_DATASTORE_BACKEND)) {
                FsDataStore.delete(nDocVer.getUuid());
            }

            session.delete(nDocVer);

            // Update user items size
            if (Config.USER_ITEM_CACHE) {
                UserItemsManager.decSize(author, size);
            }
        }
    }
}
