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

package com.openkm.module.jcr.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.spi.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.Note;
import com.openkm.bean.Permission;
import com.openkm.bean.Property;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.module.jcr.stuff.JCRUtils;

public class BaseMailModule {
    private static Logger log = LoggerFactory.getLogger(BaseMailModule.class);

    /**
     * Create a new mail
     */
    public static Node create(final Session session, final Node parentNode,
            final String name, final long size, final String from,
            final String[] reply, final String[] to, final String[] cc,
            final String[] bcc, final Calendar sentDate,
            final Calendar receivedDate, final String subject,
            final String content, final String mimeType, final String userId)
            throws javax.jcr.ItemExistsException,
            javax.jcr.PathNotFoundException, NoSuchNodeTypeException,
            javax.jcr.lock.LockException, VersionException,
            ConstraintViolationException, javax.jcr.RepositoryException,
            IOException, DatabaseException, UserQuotaExceededException {
        // Create and add a new mail node
        final Node mailNode = parentNode.addNode(name, Mail.TYPE);
        mailNode.setProperty(Property.KEYWORDS, new String[] {});
        mailNode.setProperty(Property.CATEGORIES, new String[] {},
                PropertyType.REFERENCE);
        mailNode.setProperty(Mail.SIZE, size);
        mailNode.setProperty(Mail.FROM, from);
        mailNode.setProperty(Mail.REPLY, reply);
        mailNode.setProperty(Mail.TO, to);
        mailNode.setProperty(Mail.CC, cc);
        mailNode.setProperty(Mail.BCC, bcc);
        mailNode.setProperty(Mail.SENT_DATE, sentDate);
        mailNode.setProperty(Mail.RECEIVED_DATE, receivedDate);
        mailNode.setProperty(Mail.SUBJECT, subject);
        mailNode.setProperty(Mail.CONTENT, content);
        mailNode.setProperty(Mail.MIME_TYPE, mimeType);
        mailNode.setProperty(Mail.AUTHOR, userId);

        // Get parent node auth info
        final Value[] usersReadParent = parentNode.getProperty(
                Permission.USERS_READ).getValues();
        final String[] usersRead = JCRUtils.usrValue2String(usersReadParent,
                session.getUserID());
        final Value[] usersWriteParent = parentNode.getProperty(
                Permission.USERS_WRITE).getValues();
        final String[] usersWrite = JCRUtils.usrValue2String(usersWriteParent,
                session.getUserID());
        final Value[] usersDeleteParent = parentNode.getProperty(
                Permission.USERS_DELETE).getValues();
        final String[] usersDelete = JCRUtils.usrValue2String(
                usersDeleteParent, session.getUserID());
        final Value[] usersSecurityParent = parentNode.getProperty(
                Permission.USERS_SECURITY).getValues();
        final String[] usersSecurity = JCRUtils.usrValue2String(
                usersSecurityParent, session.getUserID());

        final Value[] rolesReadParent = parentNode.getProperty(
                Permission.ROLES_READ).getValues();
        final String[] rolesRead = JCRUtils.rolValue2String(rolesReadParent);
        final Value[] rolesWriteParent = parentNode.getProperty(
                Permission.ROLES_WRITE).getValues();
        final String[] rolesWrite = JCRUtils.rolValue2String(rolesWriteParent);
        final Value[] rolesDeleteParent = parentNode.getProperty(
                Permission.ROLES_DELETE).getValues();
        final String[] rolesDelete = JCRUtils
                .rolValue2String(rolesDeleteParent);
        final Value[] rolesSecurityParent = parentNode.getProperty(
                Permission.ROLES_SECURITY).getValues();
        final String[] rolesSecurity = JCRUtils
                .rolValue2String(rolesSecurityParent);

        // Set auth info
        mailNode.setProperty(Permission.USERS_READ, usersRead);
        mailNode.setProperty(Permission.USERS_WRITE, usersWrite);
        mailNode.setProperty(Permission.USERS_DELETE, usersDelete);
        mailNode.setProperty(Permission.USERS_SECURITY, usersSecurity);
        mailNode.setProperty(Permission.ROLES_READ, rolesRead);
        mailNode.setProperty(Permission.ROLES_WRITE, rolesWrite);
        mailNode.setProperty(Permission.ROLES_DELETE, rolesDelete);
        mailNode.setProperty(Permission.ROLES_SECURITY, rolesSecurity);

        parentNode.save();

        return mailNode;
    }

    /**
     * Get mail properties
     */
    public static Mail getProperties(final Session session, final Node mailNode)
            throws javax.jcr.PathNotFoundException,
            javax.jcr.RepositoryException {
        log.debug("getProperties({}, {})", session, mailNode);
        final Mail mail = new Mail();

        // Properties
        final Value[] replyValues = mailNode.getProperty(Mail.REPLY)
                .getValues();
        final String[] reply = JCRUtils.value2String(replyValues);
        final Value[] toValues = mailNode.getProperty(Mail.TO).getValues();
        final String[] to = JCRUtils.value2String(toValues);
        final Value[] ccValues = mailNode.getProperty(Mail.CC).getValues();
        final String[] cc = JCRUtils.value2String(ccValues);
        final Value[] bccValues = mailNode.getProperty(Mail.BCC).getValues();
        final String[] bcc = JCRUtils.value2String(bccValues);

        mail.setPath(mailNode.getPath());
        mail.setUuid(mailNode.getUUID());
        mail.setReply(reply);
        mail.setTo(to);
        mail.setCc(cc);
        mail.setBcc(bcc);
        mail.setFrom(mailNode.getProperty(Mail.FROM).getString());
        mail.setSize(mailNode.getProperty(Mail.SIZE).getLong());
        mail.setSentDate(mailNode.getProperty(Mail.SENT_DATE).getDate());
        mail.setReceivedDate(mailNode.getProperty(Mail.RECEIVED_DATE).getDate());
        mail.setSubject(mailNode.getProperty(Mail.SUBJECT).getString());
        mail.setContent(mailNode.getProperty(Mail.CONTENT).getString());
        mail.setMimeType(mailNode.getProperty(Mail.MIME_TYPE).getString());
        mail.setAuthor(mailNode.getProperty(Mail.AUTHOR).getString());
        mail.setCreated(mailNode.getProperty(JcrConstants.JCR_CREATED)
                .getDate());

        // Get attachments
        final ArrayList<Document> attachments = new ArrayList<Document>();

        for (final NodeIterator nit = mailNode.getNodes(); nit.hasNext();) {
            final Node node = nit.nextNode();

            if (node.isNodeType(Document.TYPE)) {
                final Document attachment = BaseDocumentModule.getProperties(
                        session, node);
                attachments.add(attachment);
            }
        }

        mail.setAttachments(attachments);

        // Get permissions
        if (Config.SYSTEM_READONLY) {
            mail.setPermissions(Permission.NONE);
        } else {
            final AccessManager am = ((SessionImpl) session).getAccessManager();
            final Path path = ((NodeImpl) mailNode).getPrimaryPath();
            //Path path = ((SessionImpl)session).getHierarchyManager().getPath(((NodeImpl)folderNode).getId());

            if (am.isGranted(
                    path,
                    org.apache.jackrabbit.core.security.authorization.Permission.READ)) {
                mail.setPermissions(Permission.READ);
            }

            if (am.isGranted(
                    path,
                    org.apache.jackrabbit.core.security.authorization.Permission.ADD_NODE)) {
                mail.setPermissions((byte) (mail.getPermissions() | Permission.WRITE));
            }

            if (am.isGranted(
                    path,
                    org.apache.jackrabbit.core.security.authorization.Permission.REMOVE_NODE)) {
                mail.setPermissions((byte) (mail.getPermissions() | Permission.DELETE));
            }

            if (am.isGranted(
                    path,
                    org.apache.jackrabbit.core.security.authorization.Permission.MODIFY_AC)) {
                mail.setPermissions((byte) (mail.getPermissions() | Permission.SECURITY));
            }
        }

        // Get mail keywords
        final Set<String> keywordsSet = new HashSet<String>();
        final Value[] keywords = mailNode.getProperty(Property.KEYWORDS)
                .getValues();

        for (final Value keyword : keywords) {
            keywordsSet.add(keyword.getString());
        }

        mail.setKeywords(keywordsSet);

        // Get mail categories
        final Set<Folder> categoriesSet = new HashSet<Folder>();
        final Value[] categories = mailNode.getProperty(Property.CATEGORIES)
                .getValues();

        for (final Value categorie : categories) {
            final Node node = session.getNodeByUUID(categorie.getString());
            categoriesSet.add(BaseFolderModule.getProperties(session, node));
        }

        mail.setCategories(categoriesSet);

        // Get notes
        if (mailNode.isNodeType(Note.MIX_TYPE)) {
            final List<Note> notes = new ArrayList<Note>();
            final Node notesNode = mailNode.getNode(Note.LIST);

            for (final NodeIterator nit = notesNode.getNodes(); nit.hasNext();) {
                final Node noteNode = nit.nextNode();
                final Note note = new Note();
                note.setDate(noteNode.getProperty(Note.DATE).getDate());
                note.setAuthor(noteNode.getProperty(Note.USER).getString());
                note.setText(noteNode.getProperty(Note.TEXT).getString());
                note.setPath(noteNode.getPath());
                notes.add(note);
            }

            mail.setNotes(notes);
        }

        log.debug("Permisos: {} => {}", mailNode.getPath(),
                mail.getPermissions());
        log.debug("getProperties[session]: {}", mail);
        return mail;
    }

    /**
     * Copy recursively
     */
    public static void copy(final Session session, final Node srcMailNode,
            final Node dstFolderNode) throws ValueFormatException,
            javax.jcr.PathNotFoundException, javax.jcr.RepositoryException,
            IOException, DatabaseException, UserQuotaExceededException {
        log.debug("copy({}, {}, {})", new Object[] { session, srcMailNode,
                dstFolderNode });

        final String name = srcMailNode.getName();
        final long size = srcMailNode.getProperty(Mail.SIZE).getLong();
        final String from = srcMailNode.getProperty(Mail.FROM).getString();
        final String[] reply = JCRUtils.value2String(srcMailNode.getProperty(
                Mail.REPLY).getValues());
        final String[] to = JCRUtils.value2String(srcMailNode.getProperty(
                Mail.TO).getValues());
        final String[] cc = JCRUtils.value2String(srcMailNode.getProperty(
                Mail.CC).getValues());
        final String[] bcc = JCRUtils.value2String(srcMailNode.getProperty(
                Mail.BCC).getValues());
        final Calendar sentDate = srcMailNode.getProperty(Mail.SENT_DATE)
                .getDate();
        final Calendar receivedDate = srcMailNode.getProperty(
                Mail.RECEIVED_DATE).getDate();
        final String subject = srcMailNode.getProperty(Mail.SUBJECT)
                .getString();
        final String content = srcMailNode.getProperty(Mail.CONTENT)
                .getString();
        final String mimeType = srcMailNode.getProperty(Mail.MIME_TYPE)
                .getString();
        final String author = srcMailNode.getProperty(Mail.AUTHOR).getString();

        final Node mNode = BaseMailModule.create(session, dstFolderNode, name,
                size, from, reply, to, cc, bcc, sentDate, receivedDate,
                subject, content, mimeType, author);

        // Get attachments
        for (final NodeIterator nit = srcMailNode.getNodes(); nit.hasNext();) {
            final Node node = nit.nextNode();

            if (node.isNodeType(Document.TYPE)) {
                BaseDocumentModule.copy(session, node, mNode);
            }
        }

        log.debug("copy: void");
    }
}
