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

package com.openkm.util.impexp.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.core.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Note;
import com.openkm.bean.Notification;
import com.openkm.bean.Permission;
import com.openkm.bean.form.Select;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.MimeTypeConfig;
import com.openkm.core.RepositoryException;
import com.openkm.module.jcr.stuff.JCRUtils;
import com.openkm.module.jcr.stuff.JcrSessionManager;
import com.openkm.util.PathUtils;
import com.openkm.util.UserActivity;

public class JcrMetadataAdapter extends MetadataAdapter {
    private static Logger log = LoggerFactory
            .getLogger(JcrMetadataAdapter.class);

    public JcrMetadataAdapter(final String token) {
        super.token = token;
    }

    @Override
    public void importWithMetadata(final DocumentMetadata dmd,
            final InputStream is) throws ItemExistsException,
            RepositoryException, DatabaseException, IOException {
        log.debug("importWithMetadata({}, {})", new Object[] { dmd, is });
        Session session = null;
        Node parentNode = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final String parent = PathUtils.getParent(dmd.getPath());
            final String name = PathUtils.getName(dmd.getPath());
            parentNode = session.getRootNode().getNode(parent.substring(1));
            Node documentNode = null;

            if (uuid && dmd.getUuid() != null && !dmd.getUuid().equals("")) {
                documentNode = ((NodeImpl) parentNode).addNodeWithUuid(name,
                        Document.TYPE, dmd.getUuid());
            } else {
                documentNode = parentNode.addNode(name, Document.TYPE);
            }

            // Basic
            if (dmd.getAuthor() != null && !dmd.getAuthor().equals("")) {
                documentNode.setProperty(Document.AUTHOR, dmd.getAuthor());
            } else {
                documentNode.setProperty(Document.AUTHOR, session.getUserID());
            }

            if (dmd.getName() != null && !dmd.getName().equals("")) {
                documentNode.setProperty(Document.NAME, dmd.getName());
            } else {
                documentNode.setProperty(Document.NAME, name);
            }

            if (dmd.getCreated() != null) {
                documentNode.setProperty(JcrConstants.JCR_CREATED,
                        dmd.getCreated());
            } else {
                documentNode.setProperty(JcrConstants.JCR_CREATED,
                        Calendar.getInstance());
            }

            documentNode.setProperty(com.openkm.bean.Property.KEYWORDS,
                    getValues(dmd.getKeywords()));
            documentNode.setProperty(com.openkm.bean.Property.CATEGORIES,
                    getValues(session, dmd.getCategories()));

            // Notification
            if (!dmd.getSubscriptors().isEmpty()) {
                documentNode.addMixin(Notification.TYPE);
                documentNode.setProperty(Notification.SUBSCRIPTORS,
                        getValues(dmd.getSubscriptors()));
            }

            // Notes
            if (!dmd.getNotes().isEmpty()) {
                documentNode.addMixin(Note.MIX_TYPE);
                final Node notesNode = documentNode.getNode(Note.LIST);

                for (final NoteMetadata nmd : dmd.getNotes()) {
                    final String noteName = PathUtils.getName(nmd.getPath());
                    final Node noteNode = notesNode
                            .addNode(noteName, Note.TYPE);
                    noteNode.setProperty(Note.DATE, nmd.getDate());
                    noteNode.setProperty(Note.USER, nmd.getUser());
                    noteNode.setProperty(Note.TEXT, nmd.getText());
                }
            }

            // Content
            VersionMetadata vmd = dmd.getVersion();

            if (vmd == null) {
                vmd = new VersionMetadata();
            }

            final Node contentNode = documentNode.addNode(Document.CONTENT,
                    Document.CONTENT_TYPE);

            if (vmd.getSize() > 0) {
                contentNode.setProperty(Document.SIZE, vmd.getSize());
            } else {
                contentNode.setProperty(Document.SIZE, is.available());
            }

            if (vmd.getAuthor() != null && !vmd.getAuthor().equals("")) {
                contentNode.setProperty(Document.AUTHOR, vmd.getAuthor());
            } else {
                contentNode.setProperty(Document.AUTHOR, session.getUserID());
            }

            if (vmd.getComment() != null && !vmd.getComment().equals("")) {
                contentNode.setProperty(Document.VERSION_COMMENT,
                        vmd.getComment());
            } else {
                contentNode.setProperty(Document.VERSION_COMMENT,
                        "Imported by " + session.getUserID());
            }

            if (vmd.getCreated() != null) {
                contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED,
                        vmd.getCreated());
            } else {
                contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED,
                        Calendar.getInstance());
            }

            contentNode.setProperty(JcrConstants.JCR_DATA, is);

            if (vmd.getMimeType() != null && !vmd.getMimeType().equals("")) {
                contentNode.setProperty(JcrConstants.JCR_MIMETYPE,
                        vmd.getMimeType());
            } else {
                contentNode.setProperty(JcrConstants.JCR_MIMETYPE,
                        MimeTypeConfig.mimeTypes.getContentType(name
                                .toLowerCase()));
            }

            // Security
            if (dmd.getGrantedUsers() != null
                    && !dmd.getGrantedUsers().isEmpty()) {
                dmd.setGrantedUsers(getGrantedUsers(parentNode));
            }

            documentNode.setProperty(Permission.USERS_READ,
                    getPermissions(dmd.getGrantedUsers(), Permission.READ));
            documentNode.setProperty(Permission.USERS_WRITE,
                    getPermissions(dmd.getGrantedUsers(), Permission.WRITE));
            documentNode.setProperty(Permission.USERS_DELETE,
                    getPermissions(dmd.getGrantedUsers(), Permission.DELETE));
            documentNode.setProperty(Permission.USERS_SECURITY,
                    getPermissions(dmd.getGrantedUsers(), Permission.SECURITY));

            if (dmd.getGrantedRoles() != null
                    && !dmd.getGrantedRoles().isEmpty()) {
                dmd.setGrantedRoles(getGrantedRoles(parentNode));
            }

            documentNode.setProperty(Permission.ROLES_READ,
                    getPermissions(dmd.getGrantedRoles(), Permission.READ));
            documentNode.setProperty(Permission.ROLES_WRITE,
                    getPermissions(dmd.getGrantedRoles(), Permission.WRITE));
            documentNode.setProperty(Permission.ROLES_DELETE,
                    getPermissions(dmd.getGrantedRoles(), Permission.DELETE));
            documentNode.setProperty(Permission.ROLES_SECURITY,
                    getPermissions(dmd.getGrantedRoles(), Permission.SECURITY));

            // Property Groups
            for (final PropertyGroupMetadata pgmd : dmd.getPropertyGroups()) {
                documentNode.addMixin(pgmd.getName());

                for (final PropertyMetadata pmd : pgmd.getProperties()) {
                    if (Select.class.getSimpleName().equals(pmd.getType())) {
                        final String[] values = getValues(pmd.getValues());

                        if (pmd.isMultiValue()) {
                            documentNode.setProperty(pmd.getName(), values);
                        } else {
                            documentNode.setProperty(pmd.getName(),
                                    values.length > 0 ? values[0] : "");
                        }
                    } else {
                        documentNode.setProperty(pmd.getName(), pmd.getValue());
                    }
                }
            }

            // Persists
            parentNode.save();
            ((NodeImpl) contentNode).checkin(vmd.getCreated());

            // Activity log
            UserActivity.log(session.getUserID(), "CREATE_DOCUMENT",
                    documentNode.getUUID(), documentNode.getPath(),
                    "Imported with metadata");
        } catch (final LoginException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.ItemExistsException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new ItemExistsException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    @Override
    public void importWithMetadata(final String parentPath,
            final VersionMetadata vmd, final InputStream is)
            throws ItemExistsException, RepositoryException, DatabaseException,
            IOException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void importWithMetadata(final MailMetadata mmd)
            throws ItemExistsException, RepositoryException, DatabaseException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void importWithMetadata(final FolderMetadata fmd)
            throws ItemExistsException, RepositoryException, DatabaseException {
        log.debug("importWithMetadata({})", fmd);
        Session session = null;
        Node parentNode = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final String parent = PathUtils.getParent(fmd.getPath());
            final String name = PathUtils.getName(fmd.getPath());
            parentNode = session.getRootNode().getNode(parent.substring(1));
            Node folderNode = null;

            if (uuid && fmd.getUuid() != null && !fmd.getUuid().equals("")) {
                folderNode = ((NodeImpl) parentNode).addNodeWithUuid(name,
                        Folder.TYPE, fmd.getUuid());
            } else {
                folderNode = parentNode.addNode(name, Folder.TYPE);
            }

            // Basic
            if (fmd.getAuthor() != null && !fmd.getAuthor().equals("")) {
                folderNode.setProperty(Folder.AUTHOR, fmd.getAuthor());
            } else {
                folderNode.setProperty(Folder.AUTHOR, session.getUserID());
            }

            if (fmd.getName() != null && !fmd.getName().equals("")) {
                folderNode.setProperty(Folder.NAME, fmd.getName());
            } else {
                folderNode.setProperty(Folder.NAME, name);
            }

            if (fmd.getCreated() != null) {
                folderNode.setProperty(JcrConstants.JCR_CREATED,
                        fmd.getCreated());
            } else {
                folderNode.setProperty(JcrConstants.JCR_CREATED,
                        Calendar.getInstance());
            }

            folderNode.setProperty(com.openkm.bean.Property.KEYWORDS,
                    getValues(fmd.getKeywords()));
            folderNode.setProperty(com.openkm.bean.Property.CATEGORIES,
                    getValues(session, fmd.getCategories()));

            // Notification
            if (!fmd.getSubscriptors().isEmpty()) {
                folderNode.addMixin(Notification.TYPE);
                folderNode.setProperty(Notification.SUBSCRIPTORS,
                        getValues(fmd.getSubscriptors()));
            }

            // Notes
            if (!fmd.getNotes().isEmpty()) {
                folderNode.addMixin(Note.MIX_TYPE);
                final Node notesNode = folderNode.getNode(Note.LIST);

                for (final NoteMetadata nmd : fmd.getNotes()) {
                    final String noteName = PathUtils.getName(nmd.getPath());
                    final Node noteNode = notesNode
                            .addNode(noteName, Note.TYPE);
                    noteNode.setProperty(Note.DATE, nmd.getDate());
                    noteNode.setProperty(Note.USER, nmd.getUser());
                    noteNode.setProperty(Note.TEXT, nmd.getText());
                }
            }

            // Security
            if (fmd.getGrantedUsers() != null
                    && !fmd.getGrantedUsers().isEmpty()) {
                fmd.setGrantedUsers(getGrantedUsers(parentNode));
            }

            folderNode.setProperty(Permission.USERS_READ,
                    getPermissions(fmd.getGrantedUsers(), Permission.READ));
            folderNode.setProperty(Permission.USERS_WRITE,
                    getPermissions(fmd.getGrantedUsers(), Permission.WRITE));
            folderNode.setProperty(Permission.USERS_DELETE,
                    getPermissions(fmd.getGrantedUsers(), Permission.DELETE));
            folderNode.setProperty(Permission.USERS_SECURITY,
                    getPermissions(fmd.getGrantedUsers(), Permission.SECURITY));

            if (fmd.getGrantedRoles() != null
                    && !fmd.getGrantedRoles().isEmpty()) {
                fmd.setGrantedRoles(getGrantedRoles(parentNode));
            }

            folderNode.setProperty(Permission.ROLES_READ,
                    getPermissions(fmd.getGrantedRoles(), Permission.READ));
            folderNode.setProperty(Permission.ROLES_WRITE,
                    getPermissions(fmd.getGrantedRoles(), Permission.WRITE));
            folderNode.setProperty(Permission.ROLES_DELETE,
                    getPermissions(fmd.getGrantedRoles(), Permission.DELETE));
            folderNode.setProperty(Permission.ROLES_SECURITY,
                    getPermissions(fmd.getGrantedRoles(), Permission.SECURITY));

            // Property Groups
            for (final PropertyGroupMetadata pgmd : fmd.getPropertyGroups()) {
                folderNode.addMixin(pgmd.getName());

                for (final PropertyMetadata pmd : pgmd.getProperties()) {
                    if (Select.class.getSimpleName().equals(pmd.getType())) {
                        final String[] values = getValues(pmd.getValues());

                        if (pmd.isMultiValue()) {
                            folderNode.setProperty(pmd.getName(), values);
                        } else {
                            folderNode.setProperty(pmd.getName(),
                                    values.length > 0 ? values[0] : "");
                        }
                    } else {
                        folderNode.setProperty(pmd.getName(), pmd.getValue());
                    }
                }
            }

            // Persists
            parentNode.save();

            // Activity log
            UserActivity.log(session.getUserID(), "CREATE_FOLDER",
                    folderNode.getUUID(), folderNode.getPath(),
                    "Imported with metadata");
        } catch (final LoginException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.ItemExistsException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new ItemExistsException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            JCRUtils.discardsPendingChanges(parentNode);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convert between security formats.
     */
    private String[] getPermissions(final Map<String, Integer> grants,
            final int perm) {
        final List<String> principals = new ArrayList<String>();

        for (final Map.Entry<String, Integer> grant : grants.entrySet()) {
            if ((grant.getValue() & perm) != 0) {
                principals.add(grant.getKey());
            }
        }

        return principals.toArray(new String[principals.size()]);
    }

    /**
     * Convert between multivalue formats.
     */
    private String[] getValues(final Collection<String> values) {
        final ArrayList<String> ret = new ArrayList<String>();

        for (final String val : values) {
            ret.add(val);
        }

        return ret.toArray(new String[ret.size()]);
    }

    /**
     * Convert between multivalue formats.
     */
    private String[] getValues(final Session session,
            final Set<CategoryMetadata> categories) {
        final ArrayList<String> ret = new ArrayList<String>();

        for (final CategoryMetadata cmd : categories) {
            try {
                final Node categoryNode = session.getRootNode().getNode(
                        cmd.getPath().substring(1));
                ret.add(categoryNode.getUUID());
            } catch (final javax.jcr.RepositoryException e) {
                log.warn("Category node not found: {}", cmd.getPath());
            }
        }

        return ret.toArray(new String[ret.size()]);
    }

    /**
     * Get parent node granted users.
     */
    private Map<String, Integer> getGrantedUsers(final Node parentNode)
            throws ValueFormatException, PathNotFoundException,
            javax.jcr.RepositoryException {
        final Map<String, Integer> ret = new HashMap<String, Integer>();

        for (final Value userRead : parentNode.getProperty(
                Permission.USERS_READ).getValues()) {
            ret.put(userRead.getString(), Permission.READ);
        }

        for (final Value userWrite : parentNode.getProperty(
                Permission.USERS_WRITE).getValues()) {
            if (ret.get(userWrite.getString()) == null) {
                ret.put(userWrite.getString(), Permission.WRITE);
            } else {
                ret.put(userWrite.getString(), ret.get(userWrite.getString())
                        | Permission.WRITE);
            }
        }

        for (final Value userDelete : parentNode.getProperty(
                Permission.USERS_DELETE).getValues()) {
            if (ret.get(userDelete.getString()) == null) {
                ret.put(userDelete.getString(), Permission.DELETE);
            } else {
                ret.put(userDelete.getString(), ret.get(userDelete.getString())
                        | Permission.DELETE);
            }
        }

        for (final Value userSecurity : parentNode.getProperty(
                Permission.USERS_SECURITY).getValues()) {
            if (ret.get(userSecurity.getString()) == null) {
                ret.put(userSecurity.getString(), Permission.SECURITY);
            } else {
                ret.put(userSecurity.getString(),
                        ret.get(userSecurity.getString()) | Permission.SECURITY);
            }
        }

        return ret;
    }

    /**
     * Get parent node granted roles.
     */
    private Map<String, Integer> getGrantedRoles(final Node parentNode)
            throws ValueFormatException, PathNotFoundException,
            javax.jcr.RepositoryException {
        final Map<String, Integer> ret = new HashMap<String, Integer>();

        for (final Value roleRead : parentNode.getProperty(
                Permission.ROLES_READ).getValues()) {
            ret.put(roleRead.getString(), Permission.READ);
        }

        for (final Value roleWrite : parentNode.getProperty(
                Permission.ROLES_WRITE).getValues()) {
            if (ret.get(roleWrite.getString()) == null) {
                ret.put(roleWrite.getString(), Permission.WRITE);
            } else {
                ret.put(roleWrite.getString(), ret.get(roleWrite.getString())
                        | Permission.WRITE);
            }
        }

        for (final Value roleDelete : parentNode.getProperty(
                Permission.ROLES_DELETE).getValues()) {
            if (ret.get(roleDelete.getString()) == null) {
                ret.put(roleDelete.getString(), Permission.DELETE);
            } else {
                ret.put(roleDelete.getString(), ret.get(roleDelete.getString())
                        | Permission.DELETE);
            }
        }

        for (final Value roleSecurity : parentNode.getProperty(
                Permission.ROLES_SECURITY).getValues()) {
            if (ret.get(roleSecurity.getString()) == null) {
                ret.put(roleSecurity.getString(), Permission.SECURITY);
            } else {
                ret.put(roleSecurity.getString(),
                        ret.get(roleSecurity.getString()) | Permission.SECURITY);
            }
        }

        return ret;
    }
}
