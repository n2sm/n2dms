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

package com.openkm.module.db.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.automation.AutomationException;
import com.openkm.bean.ContentInfo;
import com.openkm.bean.Folder;
import com.openkm.bean.Note;
import com.openkm.bean.Permission;
import com.openkm.bean.workflow.ProcessDefinition;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.cache.UserItemsManager;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.core.WorkflowException;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.NodeDocumentDAO;
import com.openkm.dao.NodeDocumentVersionDAO;
import com.openkm.dao.NodeFolderDAO;
import com.openkm.dao.NodeMailDAO;
import com.openkm.dao.NodeNoteDAO;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeDocumentVersion;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.dao.bean.NodeNote;
import com.openkm.module.common.CommonWorkflowModule;
import com.openkm.module.db.stuff.DbAccessManager;
import com.openkm.module.db.stuff.SecurityHelper;
import com.openkm.util.CloneUtils;

public class BaseFolderModule {
    private static Logger log = LoggerFactory.getLogger(BaseFolderModule.class);

    /**
     * Create a new folder
     */
    public static NodeFolder create(final String user,
            final NodeFolder parentFolder, final String name,
            final Calendar created) throws PathNotFoundException,
            AccessDeniedException, ItemExistsException, DatabaseException {

        // Create and add a new folder node
        final NodeFolder folderNode = new NodeFolder();
        folderNode.setUuid(UUID.randomUUID().toString());
        folderNode.setContext(parentFolder.getContext());
        folderNode.setParent(parentFolder.getUuid());
        folderNode.setAuthor(user);
        folderNode.setName(name);
        folderNode.setCreated(created != null ? created : Calendar
                .getInstance());

        // Get parent node auth info
        final Map<String, Integer> userPerms = parentFolder
                .getUserPermissions();
        final Map<String, Integer> rolePerms = parentFolder
                .getRolePermissions();

        // Always assign all grants to creator
        if (Config.USER_ASSIGN_DOCUMENT_CREATION) {
            userPerms.put(user, Permission.ALL_GRANTS);
        }

        // Set auth info
        // NOTICE: Pay attention to the need of cloning
        folderNode.setUserPermissions(CloneUtils.clone(userPerms));
        folderNode.setRolePermissions(CloneUtils.clone(rolePerms));

        NodeFolderDAO.getInstance().create(folderNode);

        if (Config.USER_ITEM_CACHE) {
            // Update user items size
            UserItemsManager.incFolders(user, 1);
        }

        return folderNode;
    }

    /**
     * Get folder properties
     */
    public static Folder getProperties(final String user,
            final NodeFolder nFolder) throws PathNotFoundException,
            DatabaseException {
        log.debug("getProperties({}, {})", user, nFolder);
        final Folder fld = new Folder();

        // Properties
        final String fldPath = NodeBaseDAO.getInstance().getPathFromUuid(
                nFolder.getUuid());
        fld.setPath(fldPath);
        fld.setCreated(nFolder.getCreated());
        fld.setAuthor(nFolder.getAuthor());
        fld.setUuid(nFolder.getUuid());
        fld.setHasChildren(NodeFolderDAO.getInstance().hasChildren(
                nFolder.getUuid()));

        // Get permissions
        if (Config.SYSTEM_READONLY) {
            fld.setPermissions(Permission.NONE);
        } else {
            final DbAccessManager am = SecurityHelper.getAccessManager();

            if (am.isGranted(nFolder, Permission.READ)) {
                fld.setPermissions(Permission.READ);
            }

            if (am.isGranted(nFolder, Permission.WRITE)) {
                fld.setPermissions(fld.getPermissions() | Permission.WRITE);
            }

            if (am.isGranted(nFolder, Permission.DELETE)) {
                fld.setPermissions(fld.getPermissions() | Permission.DELETE);
            }

            if (am.isGranted(nFolder, Permission.SECURITY)) {
                fld.setPermissions(fld.getPermissions() | Permission.SECURITY);
            }
        }

        // Get user subscription & keywords
        fld.setSubscriptors(nFolder.getSubscriptors());
        fld.setSubscribed(nFolder.getSubscriptors().contains(user));
        fld.setKeywords(nFolder.getKeywords());

        // Get categories
        final Set<Folder> categories = new HashSet<Folder>();
        final NodeFolderDAO nFldDao = NodeFolderDAO.getInstance();
        final Set<NodeFolder> resolvedCategories = nFldDao
                .resolveCategories(nFolder.getCategories());

        for (final NodeFolder nfldCat : resolvedCategories) {
            categories.add(BaseFolderModule.getProperties(user, nfldCat));
        }

        fld.setCategories(categories);

        // Get notes
        final List<Note> notes = new ArrayList<Note>();
        final List<NodeNote> nNoteList = NodeNoteDAO.getInstance()
                .findByParent(nFolder.getUuid());

        for (final NodeNote nNote : nNoteList) {
            notes.add(BaseNoteModule.getProperties(nNote, nNote.getUuid()));
        }

        fld.setNotes(notes);

        log.debug("getProperties: {}", fld);
        return fld;
    }

    /**
     * Duplicates a folder into another one
     */
    public static NodeFolder copy(final String user,
            final NodeFolder srcFldNode, final NodeFolder dstFldNode)
            throws ItemExistsException, UserQuotaExceededException,
            PathNotFoundException, AccessDeniedException, AutomationException,
            DatabaseException, IOException {
        log.debug("copy({}, {}, {})", new Object[] { user, srcFldNode,
                dstFldNode });
        final InputStream is = null;
        NodeFolder newFolder = null;

        try {
            final String name = srcFldNode.getName();
            newFolder = BaseFolderModule.create(user, dstFldNode, name,
                    Calendar.getInstance());

            for (final NodeFolder nFolder : NodeFolderDAO.getInstance()
                    .findByParent(srcFldNode.getUuid())) {
                copy(user, nFolder, newFolder);
            }

            for (final NodeDocument nDocument : NodeDocumentDAO.getInstance()
                    .findByParent(srcFldNode.getUuid())) {
                final String newPath = NodeBaseDAO.getInstance()
                        .getPathFromUuid(newFolder.getUuid());
                BaseDocumentModule.copy(user, nDocument, newPath, newFolder,
                        nDocument.getName());
            }

            for (final NodeMail nMail : NodeMailDAO.getInstance().findByParent(
                    srcFldNode.getUuid())) {
                BaseMailModule.copy(user, nMail, newFolder);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        log.debug("copy: {}", newFolder);
        return newFolder;
    }

    /**
     * Check recursively if the folder contains locked nodes
     */
    public static boolean hasLockedNodes(final String fldUuid)
            throws PathNotFoundException, DatabaseException,
            RepositoryException {
        boolean hasLock = false;

        for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                .findByParent(fldUuid)) {
            hasLock |= nDoc.isLocked();
        }

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                fldUuid)) {
            hasLock |= hasLockedNodes(nFld.getUuid());
        }

        return hasLock;
    }

    /**
     * Check if a node has removable childs
     * 
     * TODO: Is this necessary? The access manager should prevent this and make the core thrown an exception.
     */
    public static boolean hasWriteAccess(final String fldUuid)
            throws PathNotFoundException, DatabaseException,
            RepositoryException {
        log.debug("hasWriteAccess({})", fldUuid);
        final DbAccessManager am = SecurityHelper.getAccessManager();
        boolean canWrite = true;

        for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                .findByParent(fldUuid)) {
            canWrite &= am.isGranted(nDoc, Permission.WRITE);
        }

        for (final NodeMail nMail : NodeMailDAO.getInstance().findByParent(
                fldUuid)) {
            canWrite &= am.isGranted(nMail, Permission.WRITE);
        }

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                fldUuid)) {
            canWrite &= am.isGranted(nFld, Permission.WRITE);
            canWrite &= hasWriteAccess(nFld.getUuid());
        }

        log.debug("hasWriteAccess: {}", canWrite);
        return canWrite;
    }

    /**
     * Check if a node is being used in a running workflow
     */
    public static boolean hasWorkflowNodes(final String fldUuid)
            throws WorkflowException, PathNotFoundException, DatabaseException {
        final Set<String> workflowNodes = new HashSet<String>();

        for (final ProcessDefinition procDef : CommonWorkflowModule
                .findAllProcessDefinitions()) {
            for (final ProcessInstance procIns : CommonWorkflowModule
                    .findProcessInstances(procDef.getId())) {
                if (procIns.getEnd() == null) {
                    final String uuid = (String) procIns.getVariables().get(
                            Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_UUID);
                    workflowNodes.add(uuid);
                }
            }
        }

        return hasWorkflowNodesInDepth(fldUuid, workflowNodes);
    }

    /**
     * Check if a node is being used in a running workflow (Helper)
     */
    private static boolean hasWorkflowNodesInDepth(final String fldUuid,
            final Set<String> workflowNodes) throws WorkflowException,
            PathNotFoundException, DatabaseException {
        for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                .findByParent(fldUuid)) {
            if (workflowNodes.contains(nDoc.getUuid())) {
                return true;
            }
        }

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                fldUuid)) {
            return hasWorkflowNodesInDepth(nFld.getUuid(), workflowNodes);
        }

        return false;
    }

    /**
     * Check if a folder is used as category in other nodes.
     */
    public static boolean isCategoryInUse(final String fldUuid)
            throws PathNotFoundException, DatabaseException,
            RepositoryException {
        boolean inUse = NodeBaseDAO.getInstance().isCategoryInUse(fldUuid);

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                fldUuid)) {
            inUse |= isCategoryInUse(nFld.getUuid());
        }

        return inUse;
    }

    /**
     * Get content info recursively
     */
    public static ContentInfo getContentInfo(final String folderUuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("getContentInfo({})", folderUuid);
        final ContentInfo contentInfo = new ContentInfo();

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                folderUuid)) {
            final ContentInfo ci = getContentInfo(nFld.getUuid());
            contentInfo.setFolders(contentInfo.getFolders() + ci.getFolders()
                    + 1);
            contentInfo.setDocuments(contentInfo.getDocuments()
                    + ci.getDocuments());
            contentInfo.setSize(contentInfo.getSize() + ci.getSize());
        }

        for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                .findByParent(folderUuid)) {
            final NodeDocumentVersion nDocVer = NodeDocumentVersionDAO
                    .getInstance().findCurrentVersion(nDoc.getUuid());
            final long size = nDocVer.getSize();
            contentInfo.setDocuments(contentInfo.getDocuments() + 1);
            contentInfo.setSize(contentInfo.getSize() + size);
        }

        for (final NodeMail nMail : NodeMailDAO.getInstance().findByParent(
                folderUuid)) {
            final long size = nMail.getSize();
            contentInfo.setDocuments(contentInfo.getDocuments() + 1);
            contentInfo.setSize(contentInfo.getSize() + size);
        }

        log.debug("getContentInfo: {}", contentInfo);
        return contentInfo;
    }

    /**
     * Get content info by user recursively
     */
    public static Map<String, ContentInfo> getUserContentInfo(
            final String folderUuid) throws PathNotFoundException,
            DatabaseException {
        log.debug("getUserContentInfo({})", folderUuid);
        final Map<String, ContentInfo> userContentInfo = new HashMap<String, ContentInfo>();

        for (final NodeFolder nFld : NodeFolderDAO.getInstance().findByParent(
                folderUuid)) {
            final Map<String, ContentInfo> usrContInfoRt = getUserContentInfo(nFld
                    .getUuid());

            for (final String user : usrContInfoRt.keySet()) {
                final ContentInfo ciRt = usrContInfoRt.get(user);
                final ContentInfo ci = getOrCreate(userContentInfo, user);
                ci.setDocuments(ci.getDocuments() + ciRt.getDocuments());
                ci.setSize(ci.getSize() + ciRt.getSize());
                userContentInfo.put(user, ci);
            }

            final ContentInfo ci = getOrCreate(userContentInfo,
                    nFld.getAuthor());
            ci.setFolders(ci.getFolders() + ci.getFolders() + 1);
            userContentInfo.put(nFld.getAuthor(), ci);
        }

        for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                .findByParent(folderUuid)) {
            for (final NodeDocumentVersion nDocVer : NodeDocumentVersionDAO
                    .getInstance().findByParent(nDoc.getUuid())) {
                final ContentInfo ci = getOrCreate(userContentInfo,
                        nDocVer.getAuthor());
                ci.setSize(ci.getSize() + nDocVer.getSize());
                userContentInfo.put(nDocVer.getAuthor(), ci);
            }

            final ContentInfo ci = getOrCreate(userContentInfo,
                    nDoc.getAuthor());
            ci.setDocuments(ci.getDocuments() + 1);
            userContentInfo.put(nDoc.getAuthor(), ci);
        }

        for (final NodeMail nMail : NodeMailDAO.getInstance().findByParent(
                folderUuid)) {
            final ContentInfo ci = getOrCreate(userContentInfo,
                    nMail.getAuthor());
            ci.setDocuments(ci.getDocuments() + 1);
            ci.setSize(ci.getSize() + nMail.getSize());
            userContentInfo.put(nMail.getAuthor(), ci);
        }

        log.debug("getUserContentInfo: {}", userContentInfo);
        return userContentInfo;
    }

    /**
     * Helper method
     */
    private static ContentInfo getOrCreate(
            final Map<String, ContentInfo> userContentInfo, final String user) {
        ContentInfo ci = userContentInfo.get(user);

        if (ci == null) {
            ci = new ContentInfo();
        }

        return ci;
    }
}
