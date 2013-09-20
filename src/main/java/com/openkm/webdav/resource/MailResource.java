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
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.QuotaResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.openkm.api.OKMMail;
import com.openkm.bean.Mail;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.util.MailUtils;
import com.openkm.util.PathUtils;

public class MailResource implements CopyableResource, DeletableResource,
        GetableResource, MoveableResource, PropFindableResource,
        PropPatchableResource, QuotaResource {
    private static final Logger log = LoggerFactory
            .getLogger(MailResource.class);

    private Mail mail;

    public MailResource(final Mail mail) {
        this.mail = ResourceUtils.fixResourcePath(mail);
    }

    @Override
    public String getUniqueId() {
        return mail.getUuid();
    }

    @Override
    public String getName() {
        return PathUtils.getName(mail.getPath());
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
        return mail.getCreated().getTime();
    }

    @Override
    public Date getModifiedDate() {
        return mail.getCreated().getTime();
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
        if (mail.getAttachments().isEmpty()) {
            return mail.getMimeType();
        } else {
            return "message/rfc822";
        }
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public void sendContent(final OutputStream out, final Range range,
            final Map<String, String> params, final String contentType)
            throws IOException, NotAuthorizedException, BadRequestException {
        log.debug("sendContent({}, {})", params, contentType);

        try {
            final String fixedMailPath = ResourceUtils.fixRepositoryPath(mail
                    .getPath());
            final Mail mail = OKMMail.getInstance().getProperties(null,
                    fixedMailPath);

            if (mail.getAttachments().isEmpty()) {
                IOUtils.write(mail.getContent(), out);
            } else {
                final MimeMessage m = MailUtils.create(null, mail);
                m.writeTo(out);
                out.flush();
            }
        } catch (final PathNotFoundException e) {
            log.error("PathNotFoundException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + mail.getPath());
        } catch (final AccessDeniedException e) {
            log.error("AccessDeniedException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + mail.getPath());
        } catch (final RepositoryException e) {
            log.error("RepositoryException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + mail.getPath());
        } catch (final DatabaseException e) {
            log.error("DatabaseException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + mail.getPath());
        } catch (final MessagingException e) {
            log.error("MessagingException: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update content: "
                    + mail.getPath());
        }
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException,
            BadRequestException {
        log.debug("delete()");

        try {
            final String fixedMailPath = ResourceUtils.fixRepositoryPath(mail
                    .getPath());
            OKMMail.getInstance().delete(null, fixedMailPath);
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
            final String srcFolder = PathUtils.getParent(mail.getPath());
            final String fixedMailPath = ResourceUtils.fixRepositoryPath(mail
                    .getPath());

            if (dstFolder.equals(srcFolder)) {
                log.debug("moveTo - RENAME {} to {}", fixedMailPath, newName);

                try {
                    mail = OKMMail.getInstance().rename(null, fixedMailPath,
                            newName);
                } catch (final Exception e) {
                    throw new RuntimeException("Failed to rename to: "
                            + newName);
                }
            } else {
                final String dstPath = newFldParent.getFolder().getPath();
                final String fixedDstPath = ResourceUtils
                        .fixRepositoryPath(dstPath);
                log.debug("moveTo - MOVE from {} to {}", fixedMailPath,
                        fixedDstPath);

                try {
                    OKMMail.getInstance().move(null, fixedMailPath,
                            fixedDstPath);
                    mail.setPath(dstPath);
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
                final String fixedMailPath = ResourceUtils
                        .fixRepositoryPath(mail.getPath());
                OKMMail.getInstance().copy(null, fixedMailPath, dstPath);
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
        sb.append("mail=").append(mail);
        sb.append("}");
        return sb.toString();
    }
}
