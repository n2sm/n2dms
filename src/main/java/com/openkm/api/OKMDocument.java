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
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.automation.AutomationException;
import com.openkm.bean.Document;
import com.openkm.bean.LockInfo;
import com.openkm.bean.Version;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.FileSizeExceededException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.core.UnsupportedMimeTypeException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.core.VersionException;
import com.openkm.core.VirusDetectedException;
import com.openkm.extension.core.ExtensionException;
import com.openkm.module.DocumentModule;
import com.openkm.module.ModuleManager;
import com.openkm.principal.PrincipalAdapterException;

/**
 * @author pavila
 */
public class OKMDocument implements DocumentModule {
    private static Logger log = LoggerFactory.getLogger(OKMDocument.class);

    private static OKMDocument instance = new OKMDocument();

    private OKMDocument() {
    }

    public static OKMDocument getInstance() {
        return instance;
    }

    @Override
    public Document create(final String token, final Document doc,
            final InputStream is) throws UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException, AutomationException {
        log.debug("create({}, {}, {})", new Object[] { token, doc, is });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document newDocument = dm.create(token, doc, is);
        log.debug("create: {}", newDocument);
        return newDocument;
    }

    public Document createSimple(final String token, final String docPath,
            final InputStream is) throws UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException, AutomationException {
        log.debug("createSimple({}, {}, {})",
                new Object[] { token, docPath, is });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document doc = new Document();
        doc.setPath(docPath);
        final Document newDocument = dm.create(token, doc, is);
        log.debug("createSimple: {}", newDocument);
        return newDocument;
    }

    @Override
    public void delete(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, ExtensionException {
        log.debug("delete({})", docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.delete(token, docPath);
        log.debug("delete: void");
    }

    @Override
    public Document getProperties(final String token, final String docPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getProperties({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document doc = dm.getProperties(token, docPath);
        log.debug("getProperties: {}", doc);
        return doc;
    }

    @Override
    public InputStream getContent(final String token, final String docPath,
            final boolean checkout) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException {
        log.debug("getContent({}, {}, {})", new Object[] { token, docPath,
                checkout });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final InputStream is = dm.getContent(token, docPath, checkout);
        log.debug("getContent: {}", is);
        return is;
    }

    @Override
    public InputStream getContentByVersion(final String token,
            final String docPath, final String versionId)
            throws RepositoryException, PathNotFoundException, IOException,
            DatabaseException {
        log.debug("getContentByVersion({}, {}, {})", new Object[] { token,
                docPath, versionId });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final InputStream is = dm
                .getContentByVersion(token, docPath, versionId);
        log.debug("getContentByVersion: {}", is);
        return is;
    }

    @Override
    @Deprecated
    public List<Document> getChilds(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getChilds({}, {})", token, fldPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Document> col = dm.getChilds(token, fldPath);
        log.debug("getChilds: {}", col);
        return col;
    }

    @Override
    public List<Document> getChildren(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getChildren({}, {})", token, fldPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Document> col = dm.getChildren(token, fldPath);
        log.debug("getChildren: {}", col);
        return col;
    }

    @Override
    public Document rename(final String token, final String docPath,
            final String newName) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, LockException,
            RepositoryException, DatabaseException, ExtensionException {
        log.debug("rename({}, {}, {})",
                new Object[] { token, docPath, newName });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document renamedDocument = dm.rename(token, docPath, newName);
        log.debug("rename: {}", renamedDocument);
        return renamedDocument;
    }

    @Override
    public void setProperties(final String token, final Document doc)
            throws LockException, VersionException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("setProperties({}, {})", token, doc);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.setProperties(token, doc);
        log.debug("setProperties: void");
    }

    @Override
    public void checkout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("checkout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.checkout(token, docPath);
        log.debug("checkout: void");
    }

    @Override
    public void cancelCheckout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("cancelCheckout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.cancelCheckout(token, docPath);
        log.debug("cancelCheckout: void");
    }

    @Override
    public void forceCancelCheckout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, PrincipalAdapterException {
        log.debug("forceCancelCheckout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.forceCancelCheckout(token, docPath);
        log.debug("forceCancelCheckout: void");
    }

    @Override
    public boolean isCheckedOut(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isCheckedOut({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final boolean checkedOut = dm.isCheckedOut(token, docPath);
        log.debug("isCheckedOut: {}", checkedOut);
        return checkedOut;
    }

    @Override
    public Version checkin(final String token, final String docPath,
            final InputStream is, final String comment)
            throws FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, LockException, VersionException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, DatabaseException, ExtensionException {
        log.debug("checkin({}, {}, {})",
                new Object[] { token, docPath, comment });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Version version = dm.checkin(token, docPath, is, comment);
        log.debug("checkin: {}", version);
        return version;
    }

    @Override
    public List<Version> getVersionHistory(final String token,
            final String docPath) throws PathNotFoundException,
            RepositoryException, DatabaseException {
        log.debug("getVersionHistory({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Version> history = dm.getVersionHistory(token, docPath);
        log.debug("getVersionHistory: {}", history);
        return history;
    }

    @Override
    public LockInfo lock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("lock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final LockInfo lock = dm.lock(token, docPath);
        log.debug("lock: {}", lock);
        return lock;
    }

    @Override
    public void unlock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("unlock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.unlock(token, docPath);
        log.debug("unlock: void");
    }

    @Override
    public void forceUnlock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, PrincipalAdapterException {
        log.debug("forceUnlock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.forceUnlock(token, docPath);
        log.debug("forceUnlock: void");
    }

    @Override
    public boolean isLocked(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isLocked({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final boolean locked = dm.isLocked(token, docPath);
        log.debug("isLocked: {}", locked);
        return locked;
    }

    @Override
    public LockInfo getLockInfo(final String token, final String docPath)
            throws LockException, PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getLock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final LockInfo lock = dm.getLockInfo(token, docPath);
        log.debug("getLock: {}", lock);
        return lock;
    }

    @Override
    public void purge(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, ExtensionException {
        log.debug("purge({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.purge(token, docPath);
        log.debug("purge: void");
    }

    @Override
    public void move(final String token, final String docPath,
            final String destPath) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, LockException,
            RepositoryException, DatabaseException, ExtensionException,
            AutomationException {
        log.debug("move({}, {}, {})", new Object[] { token, docPath, destPath });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.move(token, docPath, destPath);
        log.debug("move: void");
    }

    @Override
    public void copy(final String token, final String docPath,
            final String destPath) throws ItemExistsException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, DatabaseException, UserQuotaExceededException,
            ExtensionException, AutomationException {
        log.debug("copy({}, {}, {})", new Object[] { token, docPath, destPath });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.copy(token, docPath, destPath);
        log.debug("copy: void");
    }

    @Override
    public void restoreVersion(final String token, final String docPath,
            final String versionId) throws PathNotFoundException,
            AccessDeniedException, LockException, RepositoryException,
            DatabaseException, ExtensionException {
        log.debug("restoreVersion({}, {}, {})", new Object[] { token, docPath,
                versionId });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.restoreVersion(token, docPath, versionId);
        log.debug("restoreVersion: void");
    }

    @Override
    public void purgeVersionHistory(final String token, final String docPath)
            throws PathNotFoundException, AccessDeniedException, LockException,
            RepositoryException, DatabaseException {
        log.debug("purgeVersionHistory({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.purgeVersionHistory(token, docPath);
        log.debug("purgeVersionHistory: void");
    }

    @Override
    public long getVersionHistorySize(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getVersionHistorySize({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final long size = dm.getVersionHistorySize(token, docPath);
        log.debug("getVersionHistorySize: {}", size);
        return size;
    }

    @Override
    public boolean isValid(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isValid({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final boolean valid = dm.isValid(token, docPath);
        log.debug("isValid: {}", valid);
        return valid;
    }

    @Override
    public String getPath(final String token, final String uuid)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("getPath({}, {})", token, uuid);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final String path = dm.getPath(token, uuid);
        log.debug("getPath: {}", path);
        return path;
    }
}
