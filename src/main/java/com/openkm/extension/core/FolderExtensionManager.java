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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ServiceConfigurationError;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.api.XASession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Folder;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.Ref;
import com.openkm.core.RepositoryException;
import com.openkm.core.UserQuotaExceededException;

public class FolderExtensionManager {
    private static Logger log = LoggerFactory
            .getLogger(FolderExtensionManager.class);

    private static FolderExtensionManager service = null;

    private FolderExtensionManager() {
    }

    public static synchronized FolderExtensionManager getInstance() {
        if (service == null) {
            service = new FolderExtensionManager();
        }

        return service;
    }

    /**
     * Handle PRE create extensions
     */
    public void preCreate(final Session session, final Ref<Node> parentNode,
            final Ref<Folder> fld) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, ItemExistsException,
            DatabaseException, ExtensionException {
        log.debug("preCreate({}, {}, {})", new Object[] { session, parentNode,
                fld });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preCreate(session, parentNode, fld);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Handle POST create extensions
     */
    public void postCreate(final Session session, final Ref<Node> parentNode,
            final Ref<Node> fldNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, ItemExistsException,
            DatabaseException, ExtensionException {
        log.debug("postCreate({}, {}, {})", new Object[] { session, parentNode,
                fldNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postCreate(session, parentNode, fldNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preDelete(final Session session, final String fldPath,
            final Ref<Node> refFolderNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, LockException,
            DatabaseException {
        log.debug("preDelete({}, {}, {})", new Object[] { session, fldPath,
                refFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preDelete(session, fldPath, refFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postDelete(final Session session, final String fldPath,
            final Ref<Node> refFolderNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, LockException,
            DatabaseException {
        log.debug("postDelete({}, {}, {})", new Object[] { session, fldPath,
                refFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postDelete(session, fldPath, refFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void prePurge(final Session session, final String fldPath,
            final Ref<Node> refFolderNode) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, DatabaseException {
        log.debug("prePurge({}, {}, {})", new Object[] { session, fldPath,
                refFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.prePurge(session, fldPath, refFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postPurge(final Session session, final String fldPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException {
        log.debug("postPurge({}, {})", new Object[] { session, fldPath });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postPurge(session, fldPath);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preRename(final Session session, final String fldPath,
            final String newPath, final Ref<Node> refFolderNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, DatabaseException {
        log.debug("preRename({}, {}, {}, {})", new Object[] { session, fldPath,
                newPath, refFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preRename(session, fldPath, newPath, refFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postRename(final Session session, final String fldPath,
            final String newPath, final Ref<Node> refFolderNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, DatabaseException {
        log.debug("postRename({}, {}, {}, {})", new Object[] { session,
                fldPath, newPath, refFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postRename(session, fldPath, newPath, refFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preMove(final Session session, final String fldPath,
            final String dstNodePath) throws AccessDeniedException,
            RepositoryException, PathNotFoundException, ItemExistsException,
            DatabaseException {
        log.debug("preMove({}, {}, {})", new Object[] { session, fldPath,
                dstNodePath });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preMove(session, fldPath, dstNodePath);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postMove(final Session session, final String fldPath,
            final String dstNodePath, final Ref<Node> refDstFldNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, DatabaseException {
        log.debug("postMove({}, {}, {}, {})", new Object[] { session, fldPath,
                dstNodePath, refDstFldNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postMove(session, fldPath, dstNodePath, refDstFldNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void preCopy(final XASession session,
            final Ref<Node> refSrcFolderNode, final Ref<Node> refDstFolderNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, IOException,
            DatabaseException, UserQuotaExceededException {
        log.debug("preCopy({}, {}, {})", new Object[] { session,
                refSrcFolderNode, refDstFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.preCopy(session, refSrcFolderNode, refDstFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }

    public void postCopy(final XASession session,
            final Ref<Node> refSrcFolderNode, final Ref<Node> refNewFolderNode)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, IOException,
            DatabaseException, UserQuotaExceededException {
        log.debug("postCopy({}, {}, {})", new Object[] { session,
                refSrcFolderNode, refNewFolderNode });

        try {
            final ExtensionManager em = ExtensionManager.getInstance();
            final List<FolderExtension> col = em
                    .getPlugins(FolderExtension.class);
            Collections.sort(col, new OrderComparator<FolderExtension>());

            for (final FolderExtension ext : col) {
                log.debug("Extension class: {}", ext.getClass()
                        .getCanonicalName());
                ext.postCopy(session, refSrcFolderNode, refNewFolderNode);
            }
        } catch (final ServiceConfigurationError e) {
            log.error(e.getMessage(), e);
        }
    }
}