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

package com.openkm.webdav.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockInfo.LockDepth;
import com.bradmcevoy.http.LockInfo.LockScope;
import com.bradmcevoy.http.LockInfo.LockType;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.QuotaResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.openkm.api.OKMDocument;
import com.openkm.bean.Document;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.util.PathUtils;

public class DocumentResource implements CopyableResource, DeletableResource,
        GetableResource, MoveableResource, PropFindableResource,
        PropPatchableResource, LockableResource, QuotaResource {
    private static final Logger log = LoggerFactory
            .getLogger(DocumentResource.class);

    private Document doc;

    private LockToken lt;

    public DocumentResource(final Document doc) {
        this.doc = ResourceUtils.fixResourcePath(doc);
    }

    @Override
    public String getUniqueId() {
        return doc.getUuid();
    }

    @Override
    public String getName() {
        return PathUtils.getName(doc.getPath());
    }

    @Override
    public Object authenticate(final String user, final String password) {
        // log.debug("authenticate({}, {})", new Object[] { user, password });
        return "OpenKM";
    }

    @Override
    public boolean authorise(final Request request, final Method method,
            final Auth auth) {
        // log.debug("authorise({}, {}, {})", new Object[] { request.getAbsolutePath(), method.toString(),
        // auth.getUser() });
        return true;
    }

    @Override
    public String getRealm() {
        return ResourceFactoryImpl.REALM;
    }

    @Override
    public Date getCreateDate() {
        return doc.getCreated().getTime();
    }

    @Override
    public Date getModifiedDate() {
        return doc.getLastModified().getTime();
    }

    @Override
    public String checkRedirect(final Request request) {
        return null;
    }

    @Override
    public Long getMaxAgeSeconds(final Auth auth) {
        return null;
    }

    @Override
    public String getContentType(final String accepts) {
        return doc.getMimeType();
    }

    @Override
    public Long getContentLength() {
        return doc.getActualVersion().getSize();
    }

    @Override
    public void sendContent(final OutputStream out, final Range range,
            final Map<String, String> params, final String contentType)
            throws IOException, NotAuthorizedException, BadRequestException {
        log.debug("sendContent({}, {})", params, contentType);
        InputStream is = null;

        try {
            final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                    .getPath());
            is = OKMDocument.getInstance()
                    .getContent(null, fixedDocPath, false);
            IOUtils.copy(is, out);
            out.flush();
        } catch (final PathNotFoundException e) {
            log.error("PathNotFoundException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + doc.getPath());
        } catch (final AccessDeniedException e) {
            log.error("AccessDeniedException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + doc.getPath());
        } catch (final RepositoryException e) {
            log.error("RepositoryException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + doc.getPath());
        } catch (final DatabaseException e) {
            log.error("DatabaseException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + doc.getPath());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException,
            BadRequestException {
        log.debug("delete()");

        try {
            final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                    .getPath());
            OKMDocument.getInstance().delete(null, fixedDocPath);
        } catch (final Exception e) {
            throw new ConflictException(this);
        }
    }

    @Override
    public void setProperties(final Fields fields) {
        // MIL-50: not implemented. Just to keep MS Office sweet
    }

    @Override
    public void moveTo(final CollectionResource newParent, final String newName)
            throws ConflictException, NotAuthorizedException,
            BadRequestException {
        log.debug("moveTo({}, {})", newParent, newName);

        if (newParent instanceof FolderResource) {
            final FolderResource newFldParent = (FolderResource) newParent;
            final String dstFolder = newFldParent.getFolder().getPath();
            final String srcFolder = PathUtils.getParent(doc.getPath());
            final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                    .getPath());

            if (dstFolder.equals(srcFolder)) {
                log.debug("moveTo - RENAME {} to {}", fixedDocPath, newName);

                try {
                    doc = OKMDocument.getInstance().rename(null, fixedDocPath,
                            newName);
                } catch (final Exception e) {
                    throw new RuntimeException("Failed to rename to: "
                            + newName);
                }
            } else {
                final String dstPath = newFldParent.getFolder().getPath();
                final String fixedDstPath = ResourceUtils
                        .fixRepositoryPath(dstPath);
                log.debug("moveTo - MOVE from {} to {}", fixedDocPath,
                        fixedDstPath);

                try {
                    OKMDocument.getInstance().move(null, fixedDocPath,
                            fixedDstPath);
                    doc.setPath(dstPath);
                } catch (final Exception e) {
                    throw new RuntimeException("Failed to move to: " + dstPath);
                }
            }
        } else {
            throw new RuntimeException(
                    "Destination is an unknown type. Must be a FsDirectoryResource, is a: "
                            + newParent.getClass());
        }
    }

    @Override
    public void copyTo(final CollectionResource newParent, final String newName)
            throws NotAuthorizedException, BadRequestException,
            ConflictException {
        log.debug("copyTo({}, {})", newParent, newName);

        if (newParent instanceof FolderResource) {
            final FolderResource newFldParent = (FolderResource) newParent;
            final String dstPath = newFldParent.getFolder().getPath() + "/"
                    + newName;

            try {
                final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                        .getPath());
                OKMDocument.getInstance().copy(null, fixedDocPath, dstPath);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to copy to:" + dstPath, e);
            }
        } else {
            throw new RuntimeException(
                    "Destination is an unknown type. Must be a FolderResource, is a: "
                            + newParent.getClass());
        }
    }

    @Override
    public LockResult lock(final LockTimeout timeout, final LockInfo lockInfo)
            throws NotAuthorizedException, PreConditionFailedException,
            LockedException {
        final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                .getPath());

        try {
            if (OKMDocument.getInstance().isLocked(null, fixedDocPath)) {
                throw new LockedException(this);
            } else {
                final com.openkm.bean.LockInfo lock = OKMDocument.getInstance()
                        .lock(null, fixedDocPath);
                lt = new LockToken();
                lt.tokenId = lock.getToken();
                lt.tokenId = lock.getToken();
                lt.info = new LockInfo(LockScope.EXCLUSIVE, LockType.WRITE,
                        lock.getOwner(), LockDepth.INFINITY);
                lt.timeout = new LockTimeout(Long.MAX_VALUE);
                return LockResult.success(lt);
            }
        } catch (final LockException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final PathNotFoundException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final AccessDeniedException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final RepositoryException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final DatabaseException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        }
    }

    @Override
    public LockResult refreshLock(final String token)
            throws NotAuthorizedException, PreConditionFailedException {
        return LockResult.success(lt);
    }

    @Override
    public void unlock(final String tokenId) throws NotAuthorizedException,
            PreConditionFailedException {
        final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                .getPath());

        try {
            OKMDocument.getInstance().unlock(null, fixedDocPath);
        } catch (final LockException e) {
            throw new PreConditionFailedException(this);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        }
    }

    @Override
    public LockToken getCurrentLock() {
        final String fixedDocPath = ResourceUtils.fixRepositoryPath(doc
                .getPath());

        try {
            if (OKMDocument.getInstance().isLocked(null, fixedDocPath)) {
                final com.openkm.bean.LockInfo lock = OKMDocument.getInstance()
                        .getLockInfo(null, fixedDocPath);
                lt = new LockToken();
                lt.tokenId = lock.getToken();
                lt.tokenId = lock.getToken();
                lt.info = new LockInfo(LockScope.EXCLUSIVE, LockType.WRITE,
                        lock.getOwner(), LockDepth.INFINITY);
                lt.timeout = new LockTimeout(Long.MAX_VALUE);
                return lt;
            } else {
                return null;
            }
        } catch (final LockException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final PathNotFoundException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final RepositoryException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        } catch (final DatabaseException e) {
            throw new RuntimeException("Failed to lock: " + fixedDocPath);
        }
    }

    @Override
    public Long getQuotaUsed() {
        return new Long(0);
    }

    @Override
    public Long getQuotaAvailable() {
        return Long.MAX_VALUE;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("doc=").append(doc);
        sb.append("}");
        return sb.toString();
    }
}
