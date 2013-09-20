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

package com.openkm.module.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import com.openkm.automation.AutomationException;
import com.openkm.automation.AutomationManager;
import com.openkm.automation.AutomationUtils;
import com.openkm.bean.Document;
import com.openkm.bean.FileUploadResponse;
import com.openkm.bean.LockInfo;
import com.openkm.bean.Repository;
import com.openkm.bean.Version;
import com.openkm.cache.UserItemsManager;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.FileSizeExceededException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.MimeTypeConfig;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.Ref;
import com.openkm.core.RepositoryException;
import com.openkm.core.UnsupportedMimeTypeException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.core.VersionException;
import com.openkm.core.VirusDetectedException;
import com.openkm.core.VirusDetection;
import com.openkm.core.WorkflowException;
import com.openkm.dao.MimeTypeDAO;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.NodeDocumentDAO;
import com.openkm.dao.NodeDocumentVersionDAO;
import com.openkm.dao.NodeFolderDAO;
import com.openkm.dao.bean.AutomationRule;
import com.openkm.dao.bean.NodeBase;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeDocumentVersion;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeLock;
import com.openkm.extension.core.ExtensionException;
import com.openkm.module.DocumentModule;
import com.openkm.module.common.CommonGeneralModule;
import com.openkm.module.db.base.BaseDocumentModule;
import com.openkm.module.db.base.BaseModule;
import com.openkm.module.db.base.BaseNoteModule;
import com.openkm.module.db.base.BaseNotificationModule;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.ConfigUtils;
import com.openkm.util.FormatUtil;
import com.openkm.util.PathUtils;
import com.openkm.util.UserActivity;

public class DbDocumentModule implements DocumentModule {
    private static Logger log = LoggerFactory.getLogger(DbDocumentModule.class);

    @Override
    public Document create(final String token, final Document doc,
            final InputStream is) throws UnsupportedMimeTypeException,
            FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, ItemExistsException, PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException, ExtensionException, AutomationException {
        log.debug("create({}, {}, {})", new Object[] { token, doc, is });
        return create(token, doc, is, is.available(), null);
    }

    /**
     * Used when big files and WebDAV and GoogleDocs
     */
    public Document create(final String token, final Document doc,
            final InputStream is, final long size, final String userId)
            throws UnsupportedMimeTypeException, FileSizeExceededException,
            UserQuotaExceededException, VirusDetectedException,
            ItemExistsException, PathNotFoundException, AccessDeniedException,
            RepositoryException, IOException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("create({}, {}, {}, {}, {})", new Object[] { token, doc, is,
                size, userId });
        return create(token, doc, is, size, userId,
                new Ref<FileUploadResponse>(null));
    }

    /**
     * Used when big files and FileUpload
     */
    public Document create(final String token, final Document doc,
            InputStream is, final long size, final String userId,
            final Ref<FileUploadResponse> fuResponse)
            throws UnsupportedMimeTypeException, FileSizeExceededException,
            UserQuotaExceededException, VirusDetectedException,
            ItemExistsException, PathNotFoundException, AccessDeniedException,
            RepositoryException, IOException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("create({}, {}, {}, {}, {}, {})", new Object[] { token, doc,
                is, size, userId, fuResponse });
        Document newDocument = null;
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        final String parentPath = PathUtils.getParent(doc.getPath());
        String name = PathUtils.getName(doc.getPath());

        // Add to KEA - must have the same extension
        final int idx = name.lastIndexOf('.');
        final String fileExtension = idx > 0 ? name.substring(idx) : ".tmp";
        final File tmp = File.createTempFile("okm", fileExtension);

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (Config.MAX_FILE_SIZE > 0 && size > Config.MAX_FILE_SIZE) {
                log.error(
                        "Uploaded file size: {} ({}), Max file size: {} ({})",
                        new Object[] { FormatUtil.formatSize(size), size,
                                FormatUtil.formatSize(Config.MAX_FILE_SIZE),
                                Config.MAX_FILE_SIZE });
                final String usr = userId == null ? auth.getName() : userId;
                UserActivity.log(usr, "ERROR_FILE_SIZE_EXCEEDED", null,
                        doc.getPath(), Long.toString(size));
                throw new FileSizeExceededException(Long.toString(size));
            }

            // Escape dangerous chars in name
            name = PathUtils.escape(name);

            if (!name.isEmpty()) {
                doc.setPath(parentPath + "/" + name);

                // Check file restrictions
                final String mimeType = MimeTypeConfig.mimeTypes
                        .getContentType(name.toLowerCase());
                doc.setMimeType(mimeType);

                if (Config.RESTRICT_FILE_MIME
                        && MimeTypeDAO.findByName(mimeType) == null) {
                    final String usr = userId == null ? auth.getName() : userId;
                    UserActivity.log(usr, "ERROR_UNSUPPORTED_MIME_TYPE", null,
                            doc.getPath(), mimeType);
                    throw new UnsupportedMimeTypeException(mimeType);
                }

                // Restrict for extension
                if (!Config.RESTRICT_FILE_NAME.isEmpty()) {
                    final StringTokenizer st = new StringTokenizer(
                            Config.RESTRICT_FILE_NAME, Config.LIST_SEPARATOR);

                    while (st.hasMoreTokens()) {
                        final String wc = st.nextToken().trim();
                        final String re = ConfigUtils.wildcard2regexp(wc);

                        if (Pattern.matches(re, name)) {
                            final String usr = userId == null ? auth.getName()
                                    : userId;
                            UserActivity.log(usr,
                                    "ERROR_UNSUPPORTED_MIME_TYPE", null,
                                    doc.getPath(), mimeType);
                            throw new UnsupportedMimeTypeException(mimeType);
                        }
                    }
                }

                // Manage temporary files
                final byte[] buff = new byte[4 * 1024];
                final FileOutputStream fos = new FileOutputStream(tmp);
                int read;

                while ((read = is.read(buff)) != -1) {
                    fos.write(buff, 0, read);
                }

                fos.flush();
                fos.close();
                is.close();
                is = new FileInputStream(tmp);

                if (!Config.SYSTEM_ANTIVIR.equals("")) {
                    final String info = VirusDetection.detect(tmp);

                    if (info != null) {
                        final String usr = userId == null ? auth.getName()
                                : userId;
                        UserActivity.log(usr, "ERROR_VIRUS_DETECTED", null,
                                doc.getPath(), info);
                        throw new VirusDetectedException(info);
                    }
                }

                final String parentUuid = NodeBaseDAO.getInstance()
                        .getUuidFromPath(parentPath);
                final NodeBase parentNode = NodeBaseDAO.getInstance().findByPk(
                        parentUuid);

                // AUTOMATION - PRE
                // INSIDE BaseDocumentModule.create

                // Create node
                final Set<String> keywords = doc.getKeywords() != null ? doc
                        .getKeywords() : new HashSet<String>();
                final NodeDocument docNode = BaseDocumentModule.create(
                        auth.getName(), parentPath, parentNode, name,
                        doc.getTitle(), doc.getCreated(), mimeType, is, size,
                        keywords, new HashSet<String>(), fuResponse);

                // AUTOMATION - POST
                // INSIDE BaseDocumentModule.create

                // Set returned folder properties
                newDocument = BaseDocumentModule.getProperties(auth.getName(),
                        docNode);

                // Setting wizard properties
                // INSIDE BaseDocumentModule.create

                if (fuResponse.get() == null) {
                    fuResponse.set(new FileUploadResponse());
                }

                fuResponse.get().setHasAutomation(
                        AutomationManager.getInstance().hasAutomation());

                if (userId == null) {
                    // Check subscriptions
                    BaseNotificationModule.checkSubscriptions(docNode,
                            auth.getName(), "CREATE_DOCUMENT", null);

                    // Check scripting
                    // BaseScriptingModule.checkScripts(session, parentNode, documentNode, "CREATE_DOCUMENT");

                    // Activity log
                    UserActivity.log(auth.getName(), "CREATE_DOCUMENT",
                            docNode.getUuid(), doc.getPath(), mimeType + ", "
                                    + size);
                } else {
                    // Check subscriptions
                    BaseNotificationModule.checkSubscriptions(docNode, userId,
                            "CREATE_MAIL_ATTACHMENT", null);

                    // Check scripting
                    // BaseScriptingModule.checkScripts(session, parentNode, documentNode, "CREATE_MAIL_ATTACHMENT");

                    // Activity log
                    UserActivity.log(userId, "CREATE_MAIL_ATTACHMENT",
                            docNode.getUuid(), doc.getPath(), mimeType + ", "
                                    + size);
                }
            } else {
                throw new RepositoryException("Invalid document name");
            }
        } finally {
            IOUtils.closeQuietly(is);
            org.apache.commons.io.FileUtils.deleteQuietly(tmp);

            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("create: {}", newDocument);
        return newDocument;
    }

    @Override
    public void delete(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("delete({}, {})", new Object[] { token, docPath });
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);

            if (BaseDocumentModule.hasWorkflowNodes(docUuid)) {
                throw new LockException(
                        "Can't delete a document used in a workflow");
            }

            final String userTrashPath = "/" + Repository.TRASH + "/"
                    + auth.getName();
            final String userTrashUuid = NodeBaseDAO.getInstance()
                    .getUuidFromPath(userTrashPath);
            final String name = PathUtils.getName(docPath);

            NodeDocumentDAO.getInstance().delete(name, docUuid, userTrashUuid);

            // Check subscriptions
            final NodeDocument documentNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            BaseNotificationModule.checkSubscriptions(documentNode,
                    PrincipalUtils.getUser(), "DELETE_DOCUMENT", null);

            // Activity log
            UserActivity.log(auth.getName(), "DELETE_DOCUMENT", docUuid,
                    docPath, null);
        } catch (final WorkflowException e) {
            throw new RepositoryException(e.getMessage());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("delete: void");
    }

    @Override
    public Document rename(final String token, final String docPath,
            String newName) throws PathNotFoundException, ItemExistsException,
            AccessDeniedException, LockException, RepositoryException,
            DatabaseException {
        log.debug("rename({}, {}, {})",
                new Object[] { token, docPath, newName });
        Document renamedDocument = null;
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String name = PathUtils.getName(docPath);
            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);

            // Escape dangerous chars in name
            newName = PathUtils.escape(newName);

            if (newName != null && !newName.isEmpty() && !newName.equals(name)) {
                final NodeDocument documentNode = NodeDocumentDAO.getInstance()
                        .rename(docUuid, newName);
                renamedDocument = BaseDocumentModule.getProperties(
                        auth.getName(), documentNode);

                // Check subscriptions
                BaseNotificationModule.checkSubscriptions(documentNode,
                        PrincipalUtils.getUser(), "RENAME_DOCUMENT", null);
            } else {
                // Don't change anything
                final NodeDocument documentNode = NodeDocumentDAO.getInstance()
                        .findByPk(docUuid);
                renamedDocument = BaseDocumentModule.getProperties(
                        auth.getName(), documentNode);
            }

            // Activity log
            UserActivity.log(auth.getName(), "RENAME_DOCUMENT", docUuid,
                    docPath, newName);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("rename: {}", renamedDocument);
        return renamedDocument;
    }

    @Override
    public Document getProperties(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getProperties({}, {})", token, docPath);
        Document doc = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            doc = BaseDocumentModule.getProperties(auth.getName(), docNode);

            // Activity log
            UserActivity.log(auth.getName(), "GET_DOCUMENT_PROPERTIES",
                    docUuid, docPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getProperties: {}", doc);
        return doc;
    }

    @Override
    public void setProperties(final String token, final Document doc)
            throws VersionException, LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("setProperties({}, {})", token, doc);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    doc.getPath());
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(docNode, auth.getName(),
                    "SET_DOCUMENT_PROPERTIES", null);

            // Check scripting
            // BaseScriptingModule.checkScripts(session, documentNode, documentNode, "SET_DOCUMENT_PROPERTIES");

            // Activity log
            UserActivity.log(auth.getName(), "SET_DOCUMENT_PROPERTIES",
                    docUuid, doc.getPath(), null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("setProperties: void");
    }

    @Override
    public InputStream getContent(final String token, final String docPath,
            final boolean checkout) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, IOException,
            DatabaseException {
        log.debug("getContent({}, {}, {})", new Object[] { token, docPath,
                checkout });
        return getContent(token, docPath, checkout, true);
    }

    /**
     * Retrieve the content input stream from a document
     * 
     * @param token Authorization token.
     * @param docPath Path of the document to get the content.
     * @param checkout If the content is retrieved due to a checkout or not.
     * @param extendedSecurity If the extended security DOWNLOAD permission should be evaluated.
     * This is used to enable the document preview.
     */
    public InputStream getContent(final String token, final String docPath,
            final boolean checkout, final boolean extendedSecurity)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, IOException, DatabaseException {
        log.debug("getContent({}, {}, {}, {})", new Object[] { token, docPath,
                checkout, extendedSecurity });
        InputStream is = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            is = BaseDocumentModule.getContent(auth.getName(), docPath,
                    checkout, extendedSecurity);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getContent: {}", is);
        return is;
    }

    @Override
    public InputStream getContentByVersion(final String token,
            final String docPath, final String verName)
            throws RepositoryException, PathNotFoundException, IOException,
            DatabaseException {
        log.debug("getContentByVersion({}, {}, {})", new Object[] { token,
                docPath, verName });
        InputStream is = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            is = NodeDocumentVersionDAO.getInstance()
                    .getVersionContentByParent(docUuid, verName);

            // Activity log
            UserActivity.log(auth.getName(), "GET_DOCUMENT_CONTENT_BY_VERSION",
                    docUuid, docPath, verName + ", " + is.available());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getContentByVersion: {}", is);
        return is;
    }

    @Override
    @Deprecated
    public List<Document> getChilds(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        return getChildren(token, fldPath);
    }

    @Override
    public List<Document> getChildren(final String token, final String fldPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getChildren({}, {})", token, fldPath);
        final List<Document> children = new ArrayList<Document>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String fldUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    fldPath);

            for (final NodeDocument nDocument : NodeDocumentDAO.getInstance()
                    .findByParent(fldUuid)) {
                children.add(BaseDocumentModule.getProperties(auth.getName(),
                        nDocument));
            }

            // Activity log
            UserActivity.log(auth.getName(), "GET_CHILDREN_DOCUMENTS", fldUuid,
                    fldPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getChildren: {}", children);
        return children;
    }

    @Override
    public void checkout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        checkout(token, docPath, null);
    }

    /**
     * Used in Zoho extension
     */
    public void checkout(final String token, final String docPath, String userId)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("checkout({}, {}, {})",
                new Object[] { token, docPath, userId });
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (userId == null) {
                userId = auth.getName();
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            NodeDocumentDAO.getInstance().checkout(userId, docUuid);

            // Activity log
            UserActivity.log(auth.getName(), "CHECKOUT_DOCUMENT", docUuid,
                    docPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("checkout: void");
    }

    @Override
    public void cancelCheckout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("cancelCheckout({}, {})", token, docPath);
        cancelCheckoutHelper(token, docPath, false);
        log.debug("cancelCheckout: void");
    }

    @Override
    public void forceCancelCheckout(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, PrincipalAdapterException {
        log.debug("forceCancelCheckout({}, {})", token, docPath);

        if (PrincipalUtils.getRoles().contains(Config.DEFAULT_ADMIN_ROLE)) {
            cancelCheckoutHelper(token, docPath, true);
        } else {
            throw new AccessDeniedException("Only administrator use allowed");
        }

        log.debug("forceCancelCheckout: void");
    }

    /**
     * Implement cancelCheckout and forceCancelCheckout features
     */
    private void cancelCheckoutHelper(final String token, final String docPath,
            final boolean force) throws LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("cancelCheckoutHelper({}, {}, {})", new Object[] { token,
                docPath, force });
        Authentication auth = null, oldAuth = null;
        final String action = force ? "FORCE_CANCEL_DOCUMENT_CHECKOUT"
                : "CANCEL_DOCUMENT_CHECKOUT";

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            NodeDocumentDAO.getInstance().cancelCheckout(auth.getName(),
                    docUuid, force);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(docNode, auth.getName(),
                    action, null);

            // Check scripting
            // BaseScriptingModule.checkScripts(session, documentNode, documentNode, action);

            // Activity log
            UserActivity.log(auth.getName(), action, docUuid, docPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("cancelCheckoutHelper: void");
    }

    @Override
    public boolean isCheckedOut(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isCheckedOut({}, {})", token, docPath);
        boolean checkedOut = false;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            checkedOut = NodeDocumentDAO.getInstance().isCheckedOut(docUuid);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("isCheckedOut: {}", checkedOut);
        return checkedOut;
    }

    @Override
    public Version checkin(final String token, final String docPath,
            final InputStream is, final String comment)
            throws FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, VersionException,
            IOException, DatabaseException {
        return checkin(token, docPath, is, comment, null);
    }

    /**
     * Used in Zoho extension
     */
    public Version checkin(final String token, final String docPath,
            final InputStream is, final String comment, final String userId)
            throws FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, VersionException,
            IOException, DatabaseException {
        return checkin(token, docPath, is, is.available(), comment, userId);
    }

    /**
     * Used when big files and WebDAV
     */
    public Version checkin(final String token, final String docPath,
            InputStream is, final long size, final String comment, String userId)
            throws FileSizeExceededException, UserQuotaExceededException,
            VirusDetectedException, AccessDeniedException, RepositoryException,
            PathNotFoundException, LockException, VersionException,
            IOException, DatabaseException {
        log.debug("checkin({}, {}, {}, {}, {}, {})", new Object[] { token,
                docPath, is, size, comment, userId });
        Version version = new Version();
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        final String name = PathUtils.getName(docPath);
        final int idx = name.lastIndexOf('.');
        final String fileExtension = idx > 0 ? name.substring(idx) : ".tmp";
        final File tmp = File.createTempFile("okm", fileExtension);

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (userId == null) {
                userId = auth.getName();
            }

            if (Config.MAX_FILE_SIZE > 0 && size > Config.MAX_FILE_SIZE) {
                log.error(
                        "Uploaded file size: {} ({}), Max file size: {} ({})",
                        new Object[] { FormatUtil.formatSize(size), size,
                                FormatUtil.formatSize(Config.MAX_FILE_SIZE),
                                Config.MAX_FILE_SIZE });
                UserActivity.log(userId, "ERROR_FILE_SIZE_EXCEEDED", null,
                        docPath, Long.toString(size));
                throw new FileSizeExceededException(Long.toString(size));
            }

            // Manage temporary files
            final byte[] buff = new byte[4 * 1024];
            final FileOutputStream fos = new FileOutputStream(tmp);
            int read;

            while ((read = is.read(buff)) != -1) {
                fos.write(buff, 0, read);
            }

            fos.flush();
            fos.close();
            is.close();
            is = new FileInputStream(tmp);

            if (!Config.SYSTEM_ANTIVIR.equals("")) {
                final String info = VirusDetection.detect(tmp);

                if (info != null) {
                    UserActivity.log(userId, "ERROR_VIRUS_DETECTED", null,
                            docPath, info);
                    throw new VirusDetectedException(info);
                }
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            final NodeDocumentVersion newDocVersion = NodeDocumentVersionDAO
                    .getInstance().checkin(userId, comment, docUuid, is, size);
            version = BaseModule.getProperties(newDocVersion);

            // Add comment (as system user)
            final String text = "New version " + version.getName() + " by "
                    + userId + ": " + comment;
            BaseNoteModule.create(docUuid, Config.SYSTEM_USER, text);

            // Update user items size
            if (Config.USER_ITEM_CACHE) {
                UserItemsManager.incSize(auth.getName(), size);
            }

            // Remove pdf & preview from cache
            CommonGeneralModule.cleanPreviewCache(docUuid);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(docNode, userId,
                    "CHECKIN_DOCUMENT", comment);

            // Check scripting
            // BaseScriptingModule.checkScripts(session, documentNode, documentNode, "CHECKIN_DOCUMENT");

            // Activity log
            UserActivity.log(auth.getName(), "CHECKIN_DOCUMENT", docUuid,
                    docPath, size + ", " + comment);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
            org.apache.commons.io.FileUtils.deleteQuietly(tmp);

            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("checkin: {}", version);
        return version;
    }

    @Override
    public LockInfo lock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("lock({}, {})", token, docPath);
        LockInfo lck = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            final NodeLock nLock = NodeDocumentDAO.getInstance().lock(
                    auth.getName(), docUuid);
            lck = BaseModule.getProperties(nLock, docPath);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(docNode, auth.getName(),
                    "LOCK_DOCUMENT", null);

            // Check scripting
            // BaseScriptingModule.checkScripts(session, documentNode, documentNode, "LOCK_DOCUMENT");

            // Activity log
            UserActivity.log(auth.getName(), "LOCK_DOCUMENT", docUuid, docPath,
                    lck.getToken());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("lock: {}", lck);
        return lck;
    }

    @Override
    public void unlock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("unlock({}, {})", token, docPath);
        unlockHelper(token, docPath, false);
        log.debug("unlock: void");
    }

    @Override
    public void forceUnlock(final String token, final String docPath)
            throws LockException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, PrincipalAdapterException {
        log.debug("forceUnlock({}, {})", token, docPath);

        if (PrincipalUtils.getRoles().contains(Config.DEFAULT_ADMIN_ROLE)) {
            unlockHelper(token, docPath, true);
        } else {
            throw new AccessDeniedException("Only administrator use allowed");
        }

        log.debug("forceUnlock: void");
    }

    /**
     * Implement unlock and forceUnlock features
     */
    private void unlockHelper(final String token, final String docPath,
            final boolean force) throws LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("unlock({}, {}, {})", new Object[] { token, docPath, force });
        Authentication auth = null, oldAuth = null;
        final String action = force ? "FORCE_UNLOCK_DOCUMENT"
                : "UNLOCK_DOCUMENT";

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeDocument docNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            NodeDocumentDAO.getInstance()
                    .unlock(auth.getName(), docUuid, force);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(docNode, auth.getName(),
                    action, null);

            // Check scripting
            // BaseScriptingModule.checkScripts(session, documentNode, documentNode, action);

            // Activity log
            UserActivity.log(auth.getName(), action, docUuid, docPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("unlock: void");
    }

    @Override
    public boolean isLocked(final String token, final String docPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("isLocked({}, {})", token, docPath);
        boolean locked = false;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            locked = NodeDocumentDAO.getInstance().isLocked(docUuid);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("isLocked: {}", locked);
        return locked;
    }

    @Override
    public LockInfo getLockInfo(final String token, final String docPath)
            throws RepositoryException, PathNotFoundException, LockException,
            DatabaseException {
        log.debug("getLock({}, {})", token, docPath);
        LockInfo lock = null;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final NodeLock nLock = NodeDocumentDAO.getInstance().getLock(
                    docUuid);
            lock = BaseModule.getProperties(nLock, docPath);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getLock: {}", lock);
        return lock;
    }

    @Override
    public void purge(final String token, final String docPath)
            throws LockException, AccessDeniedException, RepositoryException,
            PathNotFoundException, DatabaseException {
        log.debug("purge({}, {})", token, docPath);
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            NodeDocumentDAO.getInstance().purge(docUuid);

            // Activity log - Already inside DAO
            // UserActivity.log(auth.getName(), "PURGE_DOCUMENT", docUuid, docPath, null);
        } catch (final IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("purge: void");
    }

    @Override
    public void move(final String token, final String docPath,
            final String dstPath) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, LockException,
            RepositoryException, DatabaseException, ExtensionException,
            AutomationException {
        log.debug("move({}, {}, {})", new Object[] { token, docPath, dstPath });
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final String dstUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    dstPath);

            // AUTOMATION - PRE
            final Map<String, Object> env = new HashMap<String, Object>();
            env.put(AutomationUtils.DOCUMENT_UUID, docUuid);
            env.put(AutomationUtils.FOLDER_UUID, dstUuid);
            AutomationManager.getInstance().fireEvent(
                    AutomationRule.EVENT_DOCUMENT_MOVE, AutomationRule.AT_PRE,
                    env);

            NodeDocumentDAO.getInstance().move(docUuid, dstUuid);

            // AUTOMATION - POST
            AutomationManager.getInstance().fireEvent(
                    AutomationRule.EVENT_DOCUMENT_MOVE, AutomationRule.AT_POST,
                    env);

            // Activity log
            UserActivity.log(auth.getName(), "MOVE_DOCUMENT", docUuid, docPath,
                    dstPath);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("move: void");
    }

    @Override
    public void copy(final String token, final String docPath,
            final String dstPath) throws ItemExistsException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, AutomationException, DatabaseException,
            UserQuotaExceededException {
        log.debug("copy({}, {}, {})", new Object[] { token, docPath, dstPath });
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            // Escape dangerous chars in name
            final String docName = PathUtils.escape(PathUtils.getName(docPath));

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final String dstUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    dstPath);
            final NodeDocument srcDocNode = NodeDocumentDAO.getInstance()
                    .findByPk(docUuid);
            final NodeFolder dstFldNode = NodeFolderDAO.getInstance().findByPk(
                    dstUuid);
            final NodeDocument newDocNode = BaseDocumentModule.copy(
                    auth.getName(), srcDocNode, dstPath, dstFldNode, docName);

            // Check subscriptions
            BaseNotificationModule.checkSubscriptions(dstFldNode,
                    auth.getName(), "COPY_DOCUMENT", null);

            // Activity log
            UserActivity.log(auth.getName(), "COPY_DOCUMENT",
                    newDocNode.getUuid(), docPath, dstPath);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    @Override
    public void restoreVersion(final String token, final String docPath,
            final String versionId) throws PathNotFoundException,
            AccessDeniedException, LockException, RepositoryException,
            DatabaseException {
        log.debug("restoreVersion({}, {}, {})", new Object[] { token, docPath,
                versionId });
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            NodeDocumentVersionDAO.getInstance().restoreVersion(docUuid,
                    versionId);

            // Remove pdf & preview from cache
            CommonGeneralModule.cleanPreviewCache(docUuid);

            // Activity log
            UserActivity.log(auth.getName(), "RESTORE_DOCUMENT_VERSION",
                    docUuid, docPath, versionId);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("restoreVersion: void");
    }

    @Override
    public void purgeVersionHistory(final String token, final String docPath)
            throws AccessDeniedException, PathNotFoundException, LockException,
            RepositoryException, DatabaseException {
        log.debug("purgeVersionHistory({}, {})", token, docPath);
        Authentication auth = null, oldAuth = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            NodeDocumentVersionDAO.getInstance().purgeVersionHistory(docUuid);

            // Activity log
            UserActivity.log(auth.getName(), "PURGE_DOCUMENT_VERSION_HISTORY",
                    docUuid, docPath, null);
        } catch (final IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("purgeVersionHistory: void");
    }

    @Override
    public List<Version> getVersionHistory(final String token,
            final String docPath) throws PathNotFoundException,
            RepositoryException, DatabaseException {
        log.debug("getVersionHistory({}, {})", token, docPath);
        final List<Version> history = new ArrayList<Version>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final List<NodeDocumentVersion> docVersions = NodeDocumentVersionDAO
                    .getInstance().findByParent(docUuid);

            for (final NodeDocumentVersion nDocVersion : docVersions) {
                history.add(BaseModule.getProperties(nDocVersion));
            }

            // Activity log
            UserActivity.log(auth.getName(), "GET_DOCUMENT_VERSION_HISTORY",
                    docUuid, docPath, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getVersionHistory: {}", history);
        return history;
    }

    @Override
    public long getVersionHistorySize(final String token, final String docPath)
            throws RepositoryException, PathNotFoundException,
            DatabaseException {
        log.debug("getVersionHistorySize({}, {})", token, docPath);
        long versionHistorySize = 0;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);
            final List<NodeDocumentVersion> docVersions = NodeDocumentVersionDAO
                    .getInstance().findByParent(docUuid);

            for (final NodeDocumentVersion nDocVersion : docVersions) {
                versionHistorySize += nDocVersion.getSize();
            }
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getVersionHistorySize: {}", versionHistorySize);
        return versionHistorySize;
    }

    @Override
    public boolean isValid(final String token, final String docPath)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("isValid({}, {})", token, docPath);
        boolean valid = true;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            final String docUuid = NodeBaseDAO.getInstance().getUuidFromPath(
                    docPath);

            try {
                NodeDocumentDAO.getInstance().findByPk(docUuid);
            } catch (final PathNotFoundException e) {
                valid = false;
            }
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("isValid: {}", valid);
        return valid;
    }

    @Override
    public String getPath(final String token, final String uuid)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        try {
            return NodeBaseDAO.getInstance().getPathFromUuid(uuid);
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
}
