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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.QuotaResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.openkm.api.OKMFolder;
import com.openkm.api.OKMRepository;
import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.core.Config;
import com.openkm.core.PathNotFoundException;
import com.openkm.module.db.DbDocumentModule;
import com.openkm.module.jcr.JcrDocumentModule;
import com.openkm.util.ConfigUtils;
import com.openkm.util.PathUtils;

public class FolderResource implements MakeCollectionableResource,
        PutableResource, CopyableResource, DeletableResource, MoveableResource,
        PropFindableResource, GetableResource, QuotaResource {
    private final Logger log = LoggerFactory.getLogger(FolderResource.class);

    private final List<Document> docChilds;

    private final List<Folder> fldChilds;

    private final List<Mail> mailChilds;

    private Folder fld;

    private final Path path;

    public FolderResource(final Folder fld) {
        fldChilds = null;
        docChilds = null;
        mailChilds = null;
        path = null;
        this.fld = ResourceUtils.fixResourcePath(fld);
    }

    public FolderResource(final Path path, final Folder fld,
            final List<Folder> fldChilds, final List<Document> docChilds,
            final List<Mail> mailChilds) {
        this.fldChilds = fldChilds;
        this.docChilds = docChilds;
        this.mailChilds = mailChilds;
        this.path = path;
        this.fld = ResourceUtils.fixResourcePath(fld);
    }

    public Folder getFolder() {
        return fld;
    }

    @Override
    public String getUniqueId() {
        return fld.getUuid();
    }

    @Override
    public String getName() {
        return PathUtils.getName(fld.getPath());
    }

    @Override
    public Object authenticate(final String user, final String password) {
        // log.debug("authenticate({}, {})", new Object[] { user, password });
        return ResourceFactoryImpl.REALM;
    }

    @Override
    public boolean authorise(final Request request, final Method method,
            final Auth auth) {
        // log.debug("authorise({}, {}, {})", new Object[] {
        // request.getAbsolutePath(), method, auth });
        return true;
    }

    @Override
    public String getRealm() {
        return ResourceFactoryImpl.REALM;
    }

    @Override
    public Date getCreateDate() {
        return fld.getCreated().getTime();
    }

    @Override
    public Date getModifiedDate() {
        return fld.getCreated().getTime();
    }

    @Override
    public String checkRedirect(final Request request) {
        return null;
    }

    @Override
    public Resource child(final String childName) {
        log.debug("child({})", childName);

        try {
            return ResourceUtils.getNode(path, fld.getPath() + "/" + childName);
        } catch (final PathNotFoundException e) {
            log.error("PathNotFoundException: " + e.getMessage());
        } catch (final Exception e) {
            log.error("Exception: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<? extends Resource> getChildren() {
        log.debug("getChildren()");
        final List<Resource> resources = new ArrayList<Resource>();

        if (fldChilds != null) {
            for (final Folder fld : fldChilds) {
                resources.add(new FolderResource(fld));
            }
        }

        if (docChilds != null) {
            for (final Document doc : docChilds) {
                resources.add(new DocumentResource(doc));
            }
        }

        if (mailChilds != null) {
            for (final Mail mail : mailChilds) {
                resources.add(new MailResource(mail));
            }
        }

        return resources;
    }

    @Override
    public Resource createNew(final String newName, final InputStream is,
            final Long length, final String contentType) throws IOException,
            ConflictException, NotAuthorizedException, BadRequestException {
        log.debug("createNew({}, {}, {}, {})", new Object[] { newName, is,
                length, contentType });
        Document newDoc = new Document();
        final String fixedDocPath = ResourceUtils.fixRepositoryPath(fld
                .getPath());
        newDoc.setPath(fixedDocPath + "/" + newName);

        try {
            if (OKMRepository.getInstance().hasNode(null, newDoc.getPath())) {
                // Already exists, so create new version
                if (Config.REPOSITORY_NATIVE) {
                    new DbDocumentModule().checkout(null, newDoc.getPath());
                    new DbDocumentModule().checkin(null, newDoc.getPath(), is,
                            length, "Modified from WebDAV", null);
                } else {
                    new JcrDocumentModule().checkout(null, newDoc.getPath());
                    new JcrDocumentModule().checkin(null, newDoc.getPath(), is,
                            "Modified from WebDAV");
                }
            } else {
                // Restrict for extension
                if (!Config.RESTRICT_FILE_NAME.isEmpty()) {
                    final StringTokenizer st = new StringTokenizer(
                            Config.RESTRICT_FILE_NAME, Config.LIST_SEPARATOR);

                    while (st.hasMoreTokens()) {
                        final String wc = st.nextToken().trim();
                        final String re = ConfigUtils.wildcard2regexp(wc);

                        if (Pattern.matches(re, newName)) {
                            log.warn("Filename BAD -> {} ({})", re, wc);
                            return null;
                        }
                    }
                }

                // Create a new one
                if (Config.REPOSITORY_NATIVE) {
                    newDoc = new DbDocumentModule().create(null, newDoc, is,
                            length, null);
                } else {
                    newDoc = new JcrDocumentModule().create(null, newDoc, is);
                }
            }

            return new DocumentResource(newDoc);
        } catch (final PathNotFoundException e) {
            log.warn("PathNotFoundException: " + e.getMessage());
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public CollectionResource createCollection(final String newName)
            throws NotAuthorizedException, ConflictException,
            BadRequestException {
        log.debug("createCollection({})", newName);
        Folder newFld = new Folder();
        final String fixedFldPath = ResourceUtils.fixRepositoryPath(fld
                .getPath());
        newFld.setPath(fixedFldPath + "/" + newName);

        try {
            newFld = OKMFolder.getInstance().create(null, newFld);
            return new FolderResource(newFld);
        } catch (final Exception e) {
            throw new ConflictException(this);
        }
    }

    @Override
    public void sendContent(final OutputStream out, final Range range,
            final Map<String, String> params, final String contentType)
            throws IOException, NotAuthorizedException, BadRequestException {
        log.debug("sendContent({}, {})", params, contentType);
        ResourceUtils
                .createContent(out, path, fldChilds, docChilds, mailChilds);
    }

    @Override
    public Long getMaxAgeSeconds(final Auth auth) {
        return null;
    }

    @Override
    public String getContentType(final String accepts) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException,
            BadRequestException {
        log.debug("delete()");

        try {
            final String fixedFldPath = ResourceUtils.fixRepositoryPath(fld
                    .getPath());
            OKMFolder.getInstance().delete(null, fixedFldPath);
        } catch (final Exception e) {
            throw new ConflictException(this);
        }
    }

    @Override
    public void moveTo(final CollectionResource newParent, final String newName)
            throws ConflictException, NotAuthorizedException,
            BadRequestException {
        log.debug("moveTo({}, {})", newParent, newName);

        if (newParent instanceof FolderResource) {
            final FolderResource newFldParent = (FolderResource) newParent;
            final String dstFolder = newFldParent.getFolder().getPath();
            final String srcFolder = PathUtils.getParent(fld.getPath());
            final String fixedFldPath = ResourceUtils.fixRepositoryPath(fld
                    .getPath());

            if (dstFolder.equals(srcFolder)) {
                log.debug("moveTo - RENAME {} to {}", fixedFldPath, newName);

                try {
                    fld = OKMFolder.getInstance().rename(null, fixedFldPath,
                            newName);
                } catch (final Exception e) {
                    throw new RuntimeException("Failed to rename to: "
                            + newName);
                }
            } else {
                final String dstPath = newFldParent.getFolder().getPath();
                final String fixedDstPath = ResourceUtils
                        .fixRepositoryPath(dstPath);
                log.debug("moveTo - MOVE from {} to {}", fixedFldPath,
                        fixedDstPath);

                try {
                    OKMFolder.getInstance().move(null, fixedFldPath,
                            fixedDstPath);
                    fld.setPath(dstPath);
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
                final String fixedFldPath = ResourceUtils.fixRepositoryPath(fld
                        .getPath());
                OKMFolder.getInstance().copy(null, fixedFldPath, dstPath);
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
        sb.append("fld=").append(fld);
        sb.append("}");
        return sb.toString();
    }
}
