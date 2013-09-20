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

package com.openkm.ws.endpoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.apache.commons.io.IOUtils;
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

@WebService(name = "OKMDocument", serviceName = "OKMDocument", targetNamespace = "http://ws.openkm.com")
public class DocumentService {
    private static Logger log = LoggerFactory.getLogger(DocumentService.class);

    @WebMethod
    public Document create(@WebParam(name = "token") final String token,
            @WebParam(name = "doc") final Document doc,
            @WebParam(name = "content") final byte[] content)
            throws IOException, UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("create({})", doc);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final ByteArrayInputStream bais = new ByteArrayInputStream(content);
        final Document newDocument = dm.create(token, doc, bais);
        bais.close();
        log.debug("create: {}", newDocument);
        return newDocument;
    }

    @WebMethod
    public Document createSimple(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "content") final byte[] content)
            throws IOException, UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("createSimple({})", docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final ByteArrayInputStream bais = new ByteArrayInputStream(content);
        final Document doc = new Document();
        doc.setPath(docPath);
        final Document newDocument = dm.create(token, doc, bais);
        bais.close();
        log.debug("createSimple: {}", newDocument);
        return newDocument;
    }

    @WebMethod
    public void delete(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException,
            ExtensionException {
        log.debug("delete({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.delete(token, docPath);
        log.debug("delete: void");
    }

    @WebMethod
    public Document getProperties(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getProperties({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document doc = dm.getProperties(token, docPath);
        log.debug("getProperties: {}", doc);
        return doc;
    }

    @WebMethod
    public byte[] getContent(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "checkout") final boolean checkout)
            throws RepositoryException, IOException, PathNotFoundException,
            AccessDeniedException, DatabaseException {
        log.debug("getContent({}, {}, {})", new Object[] { token, docPath,
                checkout });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final InputStream is = dm.getContent(token, docPath, checkout);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        IOUtils.closeQuietly(is);
        final byte[] data = baos.toByteArray();
        log.debug("getContent: {}", data);
        return data;
    }

    @WebMethod
    public byte[] getContentByVersion(
            @WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "versionId") final String versionId)
            throws RepositoryException, IOException, PathNotFoundException,
            DatabaseException {
        log.debug("getContentByVersion({}, {}, {})", new Object[] { token,
                docPath, versionId });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final InputStream is = dm
                .getContentByVersion(token, docPath, versionId);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        IOUtils.closeQuietly(is);
        final byte[] data = baos.toByteArray();
        log.debug("getContentByVersion: {}", data);
        return data;
    }

    @WebMethod
    @Deprecated
    public Document[] getChilds(@WebParam(name = "token") final String token,
            @WebParam(name = "fldPath") final String fldPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getChilds({}, {})", token, fldPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Document> col = dm.getChilds(token, fldPath);
        final Document[] result = col.toArray(new Document[col.size()]);
        log.debug("getChilds: {}", result);
        return result;
    }

    @WebMethod
    public Document[] getChildren(@WebParam(name = "token") final String token,
            @WebParam(name = "fldPath") final String fldPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getChildren({}, {})", token, fldPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Document> col = dm.getChildren(token, fldPath);
        final Document[] result = col.toArray(new Document[col.size()]);
        log.debug("getChildren: {}", result);
        return result;
    }

    @WebMethod
    public Document rename(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "newName") final String newName)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, ItemExistsException, LockException,
            DatabaseException, ExtensionException {
        log.debug("rename({}, {}, {})",
                new Object[] { token, docPath, newName });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final Document renamedDocument = dm.rename(token, docPath, newName);
        log.debug("rename: {}", renamedDocument);
        return renamedDocument;
    }

    @WebMethod
    public void setProperties(@WebParam(name = "token") final String token,
            @WebParam(name = "doc") final Document doc)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, VersionException, LockException,
            DatabaseException {
        log.debug("setProperties({}, {})", token, doc);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.setProperties(token, doc);
        log.debug("setProperties: void");
    }

    @WebMethod
    public void checkout(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException {
        log.debug("checkout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.checkout(token, docPath);
        log.debug("checkout: void");
    }

    @WebMethod
    public void cancelCheckout(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException {
        log.debug("cancelCheckout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.cancelCheckout(token, docPath);
        log.debug("cancelCheckout: void");
    }

    @WebMethod
    public void forceCancelCheckout(
            @WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, DatabaseException,
            PrincipalAdapterException {
        log.debug("forceCancelCheckout({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.forceCancelCheckout(token, docPath);
        log.debug("forceCancelCheckout: void");
    }

    @WebMethod
    public Version checkin(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "content") final byte[] content,
            @WebParam(name = "comment") final String comment)
            throws FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, LockException, VersionException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, DatabaseException, ExtensionException {
        log.debug("checkin({}, {} ,{})",
                new Object[] { token, docPath, comment });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final ByteArrayInputStream bais = new ByteArrayInputStream(content);
        final Version version = dm.checkin(token, docPath, bais, comment);
        log.debug("checkin: {}", version);
        return version;
    }

    @WebMethod
    public Version[] getVersionHistory(
            @WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getVersionHistory({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final List<Version> col = dm.getVersionHistory(token, docPath);
        final Version[] result = col.toArray(new Version[col.size()]);
        log.debug("getVersionHistory: {}", result);
        return result;
    }

    @WebMethod
    public LockInfo lock(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("lock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final LockInfo lock = dm.lock(token, docPath);
        log.debug("lock: {}", lock);
        return lock;
    }

    @WebMethod
    public void unlock(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("unlock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.unlock(token, docPath);
        log.debug("unlock: void");
    }

    @WebMethod
    public void forceUnlock(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, PrincipalAdapterException {
        log.debug("forceUnlock({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.forceUnlock(token, docPath);
        log.debug("forceUnlock: void");
    }

    @WebMethod
    public void purge(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws LockException, AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException, ExtensionException {
        log.debug("purge({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.purge(token, docPath);
        log.debug("purge: void");
    }

    @WebMethod
    public void move(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "fldPath") final String fldPath)
            throws LockException, PathNotFoundException, ItemExistsException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("move({}, {}, {})", new Object[] { token, docPath, fldPath });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.move(token, docPath, fldPath);
        log.debug("move: void");
    }

    @WebMethod
    public void restoreVersion(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath,
            @WebParam(name = "versionId") final String versionId)
            throws AccessDeniedException, PathNotFoundException, LockException,
            RepositoryException, DatabaseException, ExtensionException {
        log.debug("restoreVersion({}, {}, {})", new Object[] { token, docPath,
                versionId });
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.restoreVersion(token, docPath, versionId);
        log.debug("restoreVersion: void");
    }

    @WebMethod
    public void purgeVersionHistory(
            @WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws AccessDeniedException, PathNotFoundException, LockException,
            RepositoryException, DatabaseException {
        log.debug("purgeVersionHistory({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        dm.purgeVersionHistory(token, docPath);
        log.debug("purgeVersionHistory: void");
    }

    @WebMethod
    public long getVersionHistorySize(
            @WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getVersionHistorySize({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final long size = dm.getVersionHistorySize(token, docPath);
        log.debug("getVersionHistorySize: {}", size);
        return size;
    }

    @WebMethod
    public boolean isValid(@WebParam(name = "token") final String token,
            @WebParam(name = "docPath") final String docPath)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("isValid({}, {})", token, docPath);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final boolean valid = dm.isValid(token, docPath);
        log.debug("isValid: {}", valid);
        return valid;
    }

    @WebMethod
    public String getPath(@WebParam(name = "token") final String token,
            @WebParam(name = "uuid") final String uuid)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("getPath({}, {})", token, uuid);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final String path = dm.getPath(token, uuid);
        log.debug("getPath: {}", path);
        return path;
    }
}
