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

package com.openkm.extension.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;

import javax.jcr.Node;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Version;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.FileSizeExceededException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.Ref;
import com.openkm.core.RepositoryException;
import com.openkm.core.UnsupportedMimeTypeException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.core.VersionException;
import com.openkm.core.VirusDetectedException;

public class DocumentExtensionManager {
    private static Logger log = LoggerFactory
            .getLogger(DocumentExtensionManager.class);

    private static DocumentExtensionManager service = null;

    private DocumentExtensionManager() {
    }

    public static synchronized DocumentExtensionManager getInstance() {
        if (service == null) {
            service = new DocumentExtensionManager();
        }

        return service;
    }

    /**
     * Handle PRE create extensions
     */
    public void preCreate(final Session session, final Ref<Node> parentNode,
            final Ref<File> content, final Ref<Document> doc)
            throws UnsupportedMimeTypeException, FileSizeExceededException,
            UserQuotaExceededException, VirusDetectedException,
            ItemExistsException, PathNotFoundException, AccessDeniedException,
            RepositoryException, IOException, DatabaseException,
            ExtensionException {
        log.debug("preCreate({}, {}, {}, {})", new Object[] { session,
                parentNode, content, doc });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preCreate(session, parentNode, content, doc);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Handle POST create extensions
     */
    public void postCreate(final Session session, final Ref<Node> parentNode,
            final Ref<Node> docNode) throws UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException {
        log.debug("postCreate({}, {}, {})", new Object[] { session, parentNode,
                docNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postCreate(session, parentNode, docNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Handle PRE move extensions
     */
    public void preMove(final Session session, final Ref<Node> srcDocNode,
            final Ref<Node> dstFldNode) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, RepositoryException,
            DatabaseException, ExtensionException {
        log.debug("preMove({}, {}, {})", new Object[] { session, srcDocNode,
                dstFldNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preMove(session, srcDocNode, dstFldNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Handle POST move extensions
     * 
     * @param oldDocPath - original docPath
     */
    public void postMove(final Session session, final String oldDocPath,
            final Ref<Node> srcFldNode, final Ref<Node> dstDocNode)
            throws PathNotFoundException, ItemExistsException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException {
        log.debug("postMove({}, {}, {}, {})", new Object[] { session,
                oldDocPath, srcFldNode, dstDocNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postMove(session, oldDocPath, srcFldNode, dstDocNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preDelete(final Session session, final Ref<Node> refDocumentNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException,
            ExtensionException {
        log.debug("preDelete({}, {})",
                new Object[] { session, refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preDelete(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postDelete(final Session session, final String fileName)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException,
            ExtensionException {
        log.debug("postDelete({}, {})", new Object[] { session, fileName });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postDelete(session, fileName);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preSetContent(final Session session,
            final Ref<Node> refDocumentNode) throws FileSizeExceededException,
            UserQuotaExceededException, VirusDetectedException,
            VersionException, LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException {
        log.debug("preSetContent({}, {})", new Object[] { session,
                refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preSetContent(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postSetContent(final Session session,
            final Ref<Node> refDocumentNode) throws FileSizeExceededException,
            UserQuotaExceededException, VirusDetectedException,
            VersionException, LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException {
        log.debug("postSetContent({}, {})", new Object[] { session,
                refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postSetContent(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preRename(final Session session, final String docPath,
            final String newPath, final Ref<Node> refDocumentNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, DatabaseException,
            ExtensionException {
        log.debug("preRename({}, {}, {}, {})", new Object[] { session, docPath,
                newPath, refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preRename(session, docPath, newPath, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postRename(final Session session, final String docPath,
            final String newPath, final Ref<Node> refDocumentNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, DatabaseException,
            ExtensionException {
        log.debug("postRename({}, {}, {}, {})", new Object[] { session,
                docPath, newPath, refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postRename(session, docPath, newPath, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preCheckin(final Session session,
            final Ref<Node> refDocumentNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, LockException,
            VersionException, DatabaseException, ExtensionException {
        log.debug("preCheckin({}, {})",
                new Object[] { session, refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preCheckin(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postCheckin(final Session session,
            final Ref<Node> refDocumentNode, final Ref<Version> refVersion)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, VersionException,
            DatabaseException, ExtensionException {
        log.debug("postCheckin({}, {})", new Object[] { session,
                refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postCheckin(session, refDocumentNode, refVersion);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void prePurge(final Session session, final Ref<Node> refDocumentNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException, ExtensionException {
        log.debug("prePurge({}, {})", new Object[] { session, refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.prePurge(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postPurge(final Session session, final String docPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException, ExtensionException {
        log.debug("postPurge({}, {})", new Object[] { session, docPath });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postPurge(session, docPath);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preCopy(final Session session, final Ref<Node> refSrcNode,
            final Ref<Node> refDstFolderNode) throws ItemExistsException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, DatabaseException, UserQuotaExceededException,
            ExtensionException {
        log.debug("preCopy({}, {}, {})", new Object[] { session, refSrcNode,
                refDstFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preCopy(session, refSrcNode, refDstFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postCopy(final Session session, final Ref<Node> refSrcNode,
            final Ref<Node> refNewDocument, final Ref<Node> refDstFolderNode)
            throws ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, UserQuotaExceededException, ExtensionException {
        log.debug("postCopy({}, {}, {}, {})", new Object[] { session,
                refSrcNode, refNewDocument, refDstFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postCopy(session, refSrcNode, refNewDocument,
                        refDstFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preRestoreVersion(final Session session,
            final Ref<Node> refDocumentNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, DatabaseException,
            ExtensionException {
        log.debug("preRestoreVersion({}, {})", new Object[] { session,
                refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preRestoreVersion(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postRestoreVersion(final Session session,
            final Ref<Node> refDocumentNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, DatabaseException,
            ExtensionException {
        log.debug("postRestoreVersion({}, {})", new Object[] { session,
                refDocumentNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<DocumentExtension> col = em
                    .getPlugins(DocumentExtension.class);
            Collections.sort(col, new OrderComparator<DocumentExtension>());

            for (final DocumentExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postRestoreVersion(session, refDocumentNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }
}
