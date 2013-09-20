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

package com.openkm.servlet.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMFolder;
import com.openkm.api.OKMSearch;
import com.openkm.bean.Folder;
import com.openkm.bean.Repository;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTFolder;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMFolderService;
import com.openkm.servlet.frontend.util.FolderComparator;
import com.openkm.util.GWTUtil;

/**
 * Servlet Class
 */
public class FolderServlet extends OKMRemoteServiceServlet implements
        OKMFolderService {
    private static Logger log = LoggerFactory.getLogger(FolderServlet.class);

    private static final long serialVersionUID = -4436438730167948558L;

    @Override
    public GWTFolder create(final String fldPath, final String fldPathParent)
            throws OKMException {
        log.debug("create({}, {})", fldPath, fldPathParent);
        GWTFolder gWTFolder = new GWTFolder();
        final Folder folder = new Folder();
        folder.setPath(fldPathParent + "/" + fldPath);
        updateSessionManager();

        try {
            gWTFolder = GWTUtil.copy(
                    OKMFolder.getInstance().create(null, folder),
                    getUserWorkspaceSession());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("create: {}", gWTFolder);
        return gWTFolder;
    }

    @Override
    public void delete(final String fldPath) throws OKMException {
        log.debug("delete({})", fldPath);
        updateSessionManager();

        try {
            OKMFolder.getInstance().delete(null, fldPath);
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService, ErrorCode.CAUSE_Lock),
                    e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("delete: void");
    }

    @Override
    public List<GWTFolder> getCategorizedChilds(final String fldPath)
            throws OKMException {
        log.debug("getCategorizedChilds({})", fldPath);
        final List<GWTFolder> folderList = new ArrayList<GWTFolder>();
        updateSessionManager();

        try {
            if (fldPath.startsWith("/" + Repository.CATEGORIES)) {
                // TODO: Possible optimization getting folder really could not
                // be needed we've got UUID in GWT UI
                final String uuid = OKMFolder.getInstance()
                        .getProperties(null, fldPath).getUuid();
                final List<Folder> results = OKMSearch.getInstance()
                        .getCategorizedFolders(null, uuid);

                for (final Folder folder : results) {
                    folderList.add(GWTUtil.copy(folder,
                            getUserWorkspaceSession()));
                }
            }
            Collections.sort(folderList,
                    FolderComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getCategorizedChilds: {}", folderList);
        return folderList;
    }

    @Override
    public List<GWTFolder> getThesaurusChilds(final String fldPath)
            throws OKMException {
        log.debug("getThesaurusChilds({})", fldPath);
        final List<GWTFolder> folderList = new ArrayList<GWTFolder>();
        updateSessionManager();

        try {
            // Thesaurus childs
            if (fldPath.startsWith("/" + Repository.THESAURUS)) {
                final String keyword = fldPath.substring(
                        fldPath.lastIndexOf("/") + 1).replace(" ", "_");
                final List<Folder> results = OKMSearch.getInstance()
                        .getFoldersByKeyword(null, keyword);

                for (final Folder fld : results) {
                    folderList
                            .add(GWTUtil.copy(fld, getUserWorkspaceSession()));
                }
            }
            Collections.sort(folderList,
                    FolderComparator.getInstance(getLanguage()));
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getThesaurusChilds: {}", folderList);
        return folderList;
    }

    @Override
    public List<GWTFolder> getChilds(final String fldPath,
            final boolean extraColumns) throws OKMException {
        log.debug("getChilds({})", fldPath);
        final List<GWTFolder> folderList = new ArrayList<GWTFolder>();
        updateSessionManager();

        try {
            for (final Folder folder : OKMFolder.getInstance().getChildren(
                    null, fldPath)) {
                final GWTFolder gWTFolder = extraColumns ? GWTUtil.copy(folder,
                        getUserWorkspaceSession()) : GWTUtil.copy(folder, null);
                folderList.add(gWTFolder);
            }

            Collections.sort(folderList,
                    FolderComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getChilds: {}", folderList);
        return folderList;
    }

    @Override
    public GWTFolder rename(final String fldId, final String newName)
            throws OKMException {
        log.debug("rename({}, {})", fldId, newName);
        GWTFolder gWTFolder = new GWTFolder();
        updateSessionManager();

        try {
            gWTFolder = GWTUtil.copy(
                    OKMFolder.getInstance().rename(null, fldId, newName),
                    getUserWorkspaceSession());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("rename: {}", gWTFolder);
        return gWTFolder;
    }

    @Override
    public void move(final String fldPath, final String dstPath)
            throws OKMException {
        log.debug("move({}, {})", fldPath, dstPath);
        updateSessionManager();

        try {
            OKMFolder.getInstance().move(null, fldPath, dstPath);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("move: void");
    }

    @Override
    public void purge(final String fldPath) throws OKMException {
        log.debug("purge({})", fldPath);
        updateSessionManager();

        try {
            OKMFolder.getInstance().purge(null, fldPath);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("purge: void");
    }

    @Override
    public GWTFolder getProperties(final String fldPath) throws OKMException {
        log.debug("getProperties({})", fldPath);
        GWTFolder gWTFolder = new GWTFolder();
        updateSessionManager();

        try {
            final Folder fld = OKMFolder.getInstance().getProperties(null,
                    fldPath);
            gWTFolder = GWTUtil.copy(fld, getUserWorkspaceSession());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getProperties: {}", gWTFolder);
        return gWTFolder;
    }

    @Override
    public void copy(final String fldPath, final String dstPath)
            throws OKMException {
        log.debug("copy({}, {})", fldPath, dstPath);
        updateSessionManager();

        try {
            OKMFolder.getInstance().copy(null, fldPath, dstPath);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("copy: void");
    }

    @Override
    public Boolean isValid(final String fldPath) throws OKMException {
        log.debug("isValid({})", fldPath);
        updateSessionManager();

        try {
            return Boolean.valueOf(OKMFolder.getInstance().isValid(null,
                    fldPath));
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMFolderService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMFolderService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }
    }
}
