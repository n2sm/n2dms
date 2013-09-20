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

package com.openkm.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.AppVersion;
import com.openkm.bean.Folder;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.module.ModuleManager;
import com.openkm.module.RepositoryModule;

public class OKMRepository implements RepositoryModule {
    private static Logger log = LoggerFactory.getLogger(OKMRepository.class);

    private static OKMRepository instance = new OKMRepository();

    private OKMRepository() {
    }

    public static OKMRepository getInstance() {
        return instance;
    }

    @Override
    public Folder getRootFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getRootFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder rootFolder = rm.getRootFolder(token);
        log.debug("getRootFolder: {}", rootFolder);
        return rootFolder;
    }

    @Override
    public Folder getTrashFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getTrashFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder trashFolder = rm.getTrashFolder(token);
        log.debug("getTrashFolder: {}", trashFolder);
        return trashFolder;
    }

    @Override
    public Folder getTrashFolderBase(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getTrashFolderBase({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder trashFolder = rm.getTrashFolderBase(token);
        log.debug("getTrashFolderBase: {}", trashFolder);
        return trashFolder;
    }

    @Override
    public Folder getTemplatesFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getTemplatesFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder templatesFolder = rm.getTemplatesFolder(token);
        log.debug("getTemplatesFolder: {}", templatesFolder);
        return templatesFolder;
    }

    @Override
    public Folder getPersonalFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getPersonalFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder personalFolder = rm.getPersonalFolder(token);
        log.debug("getPersonalFolder: {}", personalFolder);
        return personalFolder;
    }

    @Override
    public Folder getPersonalFolderBase(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getPersonalFolderBase({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder personalFolder = rm.getPersonalFolderBase(token);
        log.debug("getPersonalFolderBase: {}", personalFolder);
        return personalFolder;
    }

    @Override
    public Folder getMailFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getMailFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder mailFolder = rm.getMailFolder(token);
        log.debug("getMailFolder: {}", mailFolder);
        return mailFolder;
    }

    @Override
    public Folder getMailFolderBase(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getMailFolderBase({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder mailFolder = rm.getMailFolderBase(token);
        log.debug("getMailFolderBase: {}", mailFolder);
        return mailFolder;
    }

    @Override
    public Folder getThesaurusFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getThesaurusFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder thesaurusFolder = rm.getThesaurusFolder(token);
        log.debug("getThesaurusFolder: {}", thesaurusFolder);
        return thesaurusFolder;
    }

    @Override
    public Folder getCategoriesFolder(final String token)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getCategoriesFolder({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final Folder categoriesFolder = rm.getCategoriesFolder(token);
        log.debug("getCategoriesFolder: {}", categoriesFolder);
        return categoriesFolder;
    }

    @Override
    public void purgeTrash(final String token) throws PathNotFoundException,
            AccessDeniedException, LockException, RepositoryException,
            DatabaseException {
        log.debug("purgeTrash({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        rm.purgeTrash(token);
        log.debug("purgeTrash: void");
    }

    @Override
    public String getUpdateMessage(final String token)
            throws RepositoryException {
        log.debug("getUpdateMessage({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final String updateMessage = rm.getUpdateMessage(token);
        log.debug("getUpdateMessage: {}", updateMessage);
        return updateMessage;
    }

    @Override
    public String getRepositoryUuid(final String token)
            throws RepositoryException {
        log.debug("getRepositoryUuid({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final String uuid = rm.getRepositoryUuid(token);
        log.debug("getRepositoryUuid: {}", uuid);
        return uuid;
    }

    @Override
    public boolean hasNode(final String token, final String path)
            throws RepositoryException, DatabaseException {
        log.debug("hasNode({})", token, path);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final boolean ret = rm.hasNode(token, path);
        log.debug("hasNode: {}", ret);
        return ret;
    }

    @Override
    public String getNodePath(final String token, final String uuid)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getNodePath({}, {})", token, uuid);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final String ret = rm.getNodePath(token, uuid);
        log.debug("getNodePath: {}", ret);
        return ret;
    }

    @Override
    public String getNodeUuid(final String token, final String path)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getNodeUuid({}, {})", token, path);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final String ret = rm.getNodeUuid(token, path);
        log.debug("getNodeUuid: {}", ret);
        return ret;
    }

    @Override
    public AppVersion getAppVersion(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getAppVersion({})", token);
        final RepositoryModule rm = ModuleManager.getRepositoryModule();
        final AppVersion ret = rm.getAppVersion(token);
        log.debug("getAppVersion: {}", ret);
        return ret;
    }
}
