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

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.automation.AutomationException;
import com.openkm.bean.ContentInfo;
import com.openkm.bean.Folder;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.extension.core.ExtensionException;
import com.openkm.module.FolderModule;
import com.openkm.module.ModuleManager;

/**
 * @author pavila
 */
public class OKMFolder implements FolderModule {
    private static Logger log = LoggerFactory.getLogger(OKMFolder.class);

    private static OKMFolder instance = new OKMFolder();

    private OKMFolder() {
    }

    public static OKMFolder getInstance() {
        return instance;
    }

    @Override
    public Folder create(final String token, final Folder fld)
            throws PathNotFoundException, ItemExistsException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("create({}, {})", token, fld);
        final FolderModule fm = ModuleManager.getFolderModule();
        final Folder newFld = fm.create(token, fld);
        log.debug("create: {}", newFld);
        return newFld;
    }

    public Folder createSimple(final String token, final String fldPath)
            throws PathNotFoundException, ItemExistsException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("createSimple({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final Folder fld = new Folder();
        fld.setPath(fldPath);
        final Folder newFolder = fm.create(token, fld);
        log.debug("createSimple: {}", newFolder);
        return newFolder;
    }

    @Override
    public Folder getProperties(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getProperties({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final Folder fld = fm.getProperties(token, fldPath);
        log.debug("getProperties: {}", fld);
        return fld;
    }

    @Override
    public void delete(final String token, final String fldPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("delete({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        fm.delete(token, fldPath);
        log.debug("delete: void");
    }

    @Override
    public void purge(final String token, final String fldPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("purge({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        fm.purge(token, fldPath);
        log.debug("purge: void");
    }

    @Override
    public Folder rename(final String token, final String fldPath,
            final String newName) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("rename({}, {}, {})",
                new Object[] { token, fldPath, newName });
        final FolderModule fm = ModuleManager.getFolderModule();
        final Folder renamedFolder = fm.rename(token, fldPath, newName);
        log.debug("rename: {}", renamedFolder);
        return renamedFolder;
    }

    @Override
    public void move(final String token, final String fldPath,
            final String dstPath) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("move({}, {}, {})", new Object[] { token, fldPath, dstPath });
        final FolderModule fm = ModuleManager.getFolderModule();
        fm.move(token, fldPath, dstPath);
        log.debug("move: void");
    }

    @Override
    public void copy(final String token, final String fldPath,
            final String dstPath) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, RepositoryException,
            IOException, AutomationException, DatabaseException,
            UserQuotaExceededException {
        log.debug("copy({}, {}, {})", new Object[] { token, fldPath, dstPath });
        final FolderModule fm = ModuleManager.getFolderModule();
        fm.copy(token, fldPath, dstPath);
        log.debug("copy: void");
    }

    @Override
    @Deprecated
    public List<Folder> getChilds(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getChilds({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final List<Folder> col = fm.getChilds(token, fldPath);
        log.debug("getChilds: {}", col);
        return col;
    }

    @Override
    public List<Folder> getChildren(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getChildren({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final List<Folder> col = fm.getChildren(token, fldPath);
        log.debug("getChildren: {}", col);
        return col;
    }

    @Override
    public ContentInfo getContentInfo(final String token, final String fldPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException {
        log.debug("getContentInfo({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final ContentInfo contentInfo = fm.getContentInfo(token, fldPath);
        log.debug("getContentInfo: {}", contentInfo);
        return contentInfo;
    }

    @Override
    public boolean isValid(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isValid({}, {})", token, fldPath);
        final FolderModule fm = ModuleManager.getFolderModule();
        final boolean valid = fm.isValid(token, fldPath);
        log.debug("isValid: {}", valid);
        return valid;
    }

    @Override
    public String getPath(final String token, final String uuid)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("getPath({})", uuid);
        final FolderModule fm = ModuleManager.getFolderModule();
        final String path = fm.getPath(token, uuid);
        log.debug("getPath: {}", path);
        return path;
    }
}
