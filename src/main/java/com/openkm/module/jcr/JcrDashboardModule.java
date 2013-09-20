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

package com.openkm.module.jcr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.DashboardDocumentResult;
import com.openkm.bean.DashboardFolderResult;
import com.openkm.bean.DashboardMailResult;
import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.Repository;
import com.openkm.cache.UserItemsManager;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.ActivityDAO;
import com.openkm.dao.DashboardDAO;
import com.openkm.dao.HibernateUtil;
import com.openkm.dao.QueryParamsDAO;
import com.openkm.dao.bean.Activity;
import com.openkm.dao.bean.Dashboard;
import com.openkm.dao.bean.QueryParams;
import com.openkm.dao.bean.cache.UserItems;
import com.openkm.module.DashboardModule;
import com.openkm.module.jcr.base.BaseDocumentModule;
import com.openkm.module.jcr.base.BaseFolderModule;
import com.openkm.module.jcr.base.BaseMailModule;
import com.openkm.module.jcr.stuff.JCRUtils;
import com.openkm.module.jcr.stuff.JcrSessionManager;
import com.openkm.util.UserActivity;

public class JcrDashboardModule implements DashboardModule {
    private static Logger log = LoggerFactory
            .getLogger(JcrDashboardModule.class);

    private static final int MAX_RESULTS = 20;

    @Override
    public List<DashboardDocumentResult> getUserLockedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLockedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserLockedDocuments(session);
            log.debug("getUserLockedDocuments: {}", al);
            return al;
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLockedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLockedDocuments({})", session);
        final String qs = "/jcr:root/" + Repository.ROOT
                + "//element(*, okm:document)[@jcr:lockOwner='"
                + session.getUserID()
                + "' and okm:content/@jcr:isCheckedOut=false()]";
        final List<DashboardDocumentResult> al = executeQueryDocument(session,
                qs, "LOCK_DOCUMENT", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), "UserLockedDocuments", al);
        log.debug("getUserLockedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserCheckedOutDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserCheckedOutDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserCheckedOutDocuments(session);
            log.debug("getUserCheckedOutDocuments: {}", al);
            return al;
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserCheckedOutDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserCheckedOutDocuments({})", session);
        final String qs = "/jcr:root/" + Repository.ROOT
                + "//element(*, okm:document)[@jcr:lockOwner='"
                + session.getUserID()
                + "' and okm:content/@jcr:isCheckedOut=true()]";
        final List<DashboardDocumentResult> al = executeQueryDocument(session,
                qs, "CHECKOUT_DOCUMENT", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), "UserCheckedOutDocuments",
                al);
        log.debug("getUserCheckedOutDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserSubscribedDocuments(
            final String token) throws RepositoryException {
        log.debug("getUserSubscribedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserSubscribedDocuments(session);
            log.debug("getUserSubscribedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserSubscribedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserSubscribedDocuments({})", session);
        final String qs = "/jcr:root/"
                + Repository.ROOT
                + "//element(*, mix:notification)[@jcr:primaryType='okm:document' and @okm:subscriptors='"
                + session.getUserID() + "']";
        final List<DashboardDocumentResult> al = executeQueryDocument(session,
                qs, "SUBSCRIBE_USER", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), "UserSubscribedDocuments",
                al);
        log.debug("getUserSubscribedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardFolderResult> getUserSubscribedFolders(
            final String token) throws RepositoryException {
        log.debug("getUserSubscribedFolders({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardFolderResult> al = getUserSubscribedFolders(session);
            log.debug("getUserSubscribedFolders: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardFolderResult> getUserSubscribedFolders(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserSubscribedFolders({})", session);
        final String qs = "/jcr:root/"
                + Repository.ROOT
                + "//element(*, mix:notification)[@jcr:primaryType='okm:folder' and @okm:subscriptors='"
                + session.getUserID() + "']";
        final List<DashboardFolderResult> al = executeQueryFolder(session, qs,
                "SUBSCRIBE_USER", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedFolders(session.getUserID(), "UserSubscribedFolders", al);
        log.debug("getUserSubscribedFolders: {}", al);
        return al;
    }

    /**
     * Execute query with documents
     */
    private List<DashboardDocumentResult> executeQueryDocument(
            final Session session, final String qs, final String action,
            final int maxResults) throws javax.jcr.RepositoryException,
            DatabaseException {
        final List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        final Workspace workspace = session.getWorkspace();
        final QueryManager queryManager = workspace.getQueryManager();
        final Query query = queryManager.createQuery(qs, Query.XPATH);
        final QueryResult result = query.execute();
        int i = 0;

        for (final NodeIterator nit = result.getNodes(); nit.hasNext()
                && i++ < maxResults;) {
            final Node node = nit.nextNode();
            final Document doc = BaseDocumentModule
                    .getProperties(session, node);
            final DashboardDocumentResult vo = new DashboardDocumentResult();
            vo.setDocument(doc);
            vo.setDate(ActivityDAO.getActivityDate(session.getUserID(), action,
                    node.getUUID()));
            vo.setVisited(false);
            al.add(vo);
        }

        // Sort results
        Collections.sort(al, new Comparator<DashboardDocumentResult>() {
            @Override
            public int compare(final DashboardDocumentResult doc1,
                    final DashboardDocumentResult doc2) {
                return doc2.getDate().compareTo(doc1.getDate());
            }
        });

        return al;
    }

    /**
     * Execute query with folders
     */
    private ArrayList<DashboardFolderResult> executeQueryFolder(
            final Session session, final String qs, final String action,
            final int maxResults) throws javax.jcr.RepositoryException,
            DatabaseException {
        final ArrayList<DashboardFolderResult> al = new ArrayList<DashboardFolderResult>();
        final Workspace workspace = session.getWorkspace();
        final QueryManager queryManager = workspace.getQueryManager();
        final Query query = queryManager.createQuery(qs, Query.XPATH);
        final QueryResult result = query.execute();
        int i = 0;

        for (final NodeIterator nit = result.getNodes(); nit.hasNext()
                && i++ < maxResults;) {
            final Node node = nit.nextNode();
            final Folder fld = BaseFolderModule.getProperties(session, node);
            final DashboardFolderResult vo = new DashboardFolderResult();
            vo.setFolder(fld);
            vo.setDate(ActivityDAO.getActivityDate(session.getUserID(), action,
                    node.getUUID()));
            vo.setVisited(false);
            al.add(vo);
        }

        // Order results
        Collections.sort(al, new Comparator<DashboardFolderResult>() {
            @Override
            public int compare(final DashboardFolderResult fld1,
                    final DashboardFolderResult fld2) {
                return fld2.getDate().compareTo(fld1.getDate());
            }
        });

        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastUploadedDocuments(
            final String token) throws RepositoryException {
        log.debug("getUserLastUploadedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserLastUploadedDocuments(session);
            log.debug("getUserLastUploadedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastUploadedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLastUploadedDocuments({})", session);
        final String qs = "select a.item, a.date from Activity a "
                + "where a.action='CREATE_DOCUMENT' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastUploadedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(session,
                SOURCE, qs);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getUserLastUploadedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastModifiedDocuments(
            final String token) throws RepositoryException {
        log.debug("getUserLastModifiedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserLastModifiedDocuments(session);
            log.debug("getUserLastModifiedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastModifiedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLastModifiedDocuments({})", session);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.user= :user "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "UserLastModifiedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(session,
                SOURCE, qs);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getUserLastModifiedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastDownloadedDocuments(
            final String token) throws RepositoryException {
        log.debug("getUserLastDownloadedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserLastDownloadedDocuments(session);
            log.debug("getUserLastDownloadedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastDownloadedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLastDownloadedDocuments({})", session);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.user= :user "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "UserLastDownloadedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(session,
                SOURCE, qs);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getUserLastDownloadedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardMailResult> getUserLastImportedMails(final String token)
            throws RepositoryException {
        log.debug("getUserLastImportedMails({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardMailResult> al = getUserLastImportedMails(session);
            log.debug("getUserLastImportedMails: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardMailResult> getUserLastImportedMails(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLastImportedMails({})", session);
        final String sq = "from Activity a where a.action='CREATE_MAIL' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastImportedMails";
        final List<DashboardMailResult> al = getUserMails(session, SOURCE, sq);

        // Check for already visited results
        checkVisitedMails(session.getUserID(), SOURCE, al);
        log.debug("getUserLastImportedMails: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastImportedMailAttachments(
            final String token) throws RepositoryException {
        log.debug("getUserLastImportedMailAttachments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getUserLastImportedMailAttachments(session);
            log.debug("getUserLastImportedMailAttachments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastImportedMailAttachments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getUserLastImportedMailAttachments({})", session);
        final String qs = "select a.item, a.date from Activity a "
                + "where a.action='CREATE_MAIL_ATTACHMENT' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastImportedMailAttachments";
        final List<DashboardDocumentResult> al = getUserDocuments(session,
                SOURCE, qs);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getUserLastImportedMailAttachments: {}", al);
        return al;
    }

    /**
     * Get documents from statement
     */
    @SuppressWarnings("unchecked")
    private ArrayList<DashboardDocumentResult> getUserDocuments(
            final Session session, final String source, final String qs)
            throws javax.jcr.RepositoryException, DatabaseException {
        log.debug("getUserDocuments({}, {}, {})", new Object[] { session,
                source, qs });
        final ArrayList<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        org.hibernate.Session hSession = null;

        try {
            hSession = HibernateUtil.getSessionFactory().openSession();
            final org.hibernate.Query q = hSession.createQuery(qs);
            q.setString("user", session.getUserID());
            q.setMaxResults(MAX_RESULTS);

            for (final Iterator<Object[]> it = q.list().iterator(); it
                    .hasNext();) {
                final Object[] actData = it.next();
                final String actItem = (String) actData[0];
                final Calendar actDate = (Calendar) actData[1];

                try {
                    final Node node = session.getNodeByUUID(actItem);
                    final Document doc = BaseDocumentModule.getProperties(
                            session, node);
                    final DashboardDocumentResult vo = new DashboardDocumentResult();
                    vo.setDocument(doc);
                    vo.setDate(actDate);
                    vo.setVisited(false);
                    al.add(vo);
                } catch (final ItemNotFoundException e) {
                    // Do nothing
                }
            }
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(hSession);
        }

        log.debug("getUserDocuments: {}", al);
        return al;
    }

    /**
     * Get mails from statement
     */
    @SuppressWarnings("unchecked")
    private ArrayList<DashboardMailResult> getUserMails(final Session session,
            final String source, final String qs)
            throws javax.jcr.RepositoryException, DatabaseException {
        log.debug("getUserMails({}, {}, {})", new Object[] { session, source,
                qs });
        final ArrayList<DashboardMailResult> al = new ArrayList<DashboardMailResult>();
        org.hibernate.Session hSession = null;

        try {
            hSession = HibernateUtil.getSessionFactory().openSession();
            final org.hibernate.Query q = hSession.createQuery(qs);
            q.setString("user", session.getUserID());
            q.setMaxResults(MAX_RESULTS);

            for (final Iterator<Activity> it = q.list().iterator(); it
                    .hasNext();) {
                final Activity act = it.next();

                try {
                    final Node node = session.getNodeByUUID(act.getItem());
                    final Mail mail = BaseMailModule.getProperties(session,
                            node);
                    final DashboardMailResult vo = new DashboardMailResult();
                    vo.setMail(mail);
                    vo.setDate(act.getDate());
                    vo.setVisited(false);
                    al.add(vo);
                } catch (final ItemNotFoundException e) {
                    // Do nothing
                }
            }
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(hSession);
        }

        log.debug("getUserMails: {}", al);
        return al;
    }

    @Override
    public long getUserDocumentsSize(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserDocumentsSize({})", token);
        long size = 0;

        if (Config.USER_ITEM_CACHE) {
            size = getUserDocumentsSizeCached(token);
        } else {
            size = getUserDocumentsSizeLive(token);
        }

        log.debug("getUserDocumentsSize: {}", size);
        return size;
    }

    /**
     * Get user document size
     */
    private long getUserDocumentsSizeLive(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserDocumentsSizeLive({})", token);
        long size = 0;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            size = JCRUtils.calculateQuota(session);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getUserDocumentsSizeLive: {}", size);
        return size;
    }

    /**
     * Get user document size
     */
    private long getUserDocumentsSizeCached(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserDocumentsSizeCached({})", token);
        Session session = null;
        UserItems usrItems = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            usrItems = UserItemsManager.get(session.getUserID());
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getUserDocumentsSizeCached: {}", usrItems.getSize());
        return usrItems.getSize();
    }

    @Override
    public List<QueryParams> getUserSearchs(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserSearchs({})", token);
        final List<QueryParams> ret = new ArrayList<QueryParams>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<QueryParams> qParams = QueryParamsDAO.findByUser(session
                    .getUserID());

            for (final QueryParams qp : qParams) {
                // If this is a dashboard user search, dates are used internally
                if (qp.isDashboard()) {
                    qp.setLastModifiedFrom(null);
                    qp.setLastModifiedTo(null);
                    ret.add(qp);
                }
            }

            // Activity log
            UserActivity.log(session.getUserID(), "GET_DASHBOARD_USER_SEARCHS",
                    null, null, null);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getUserSearchs: {}", ret);
        return ret;
    }

    @Override
    public List<DashboardDocumentResult> find(final String token, final int qpId)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("find({}, {})", token, qpId);
        List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            al = find(session, qpId);
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("find: {}", al);
        return al;
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> find(final Session session,
            final int qpId) throws javax.jcr.RepositoryException,
            DatabaseException, ParseException, IOException {
        log.debug("find({}, {})", session, qpId);
        List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        final JcrSearchModule directSearch = new JcrSearchModule();

        // Get the saved query params
        final QueryParams params = QueryParamsDAO.findByPk(qpId);
        log.debug("PARAMS: {}", params.toString());

        // Set query date (first time)
        if (params.getLastModifiedTo() == null) {
            final Calendar firstExecution = Calendar.getInstance();
            firstExecution.add(Calendar.MONTH, -1);
            params.setLastModifiedTo(firstExecution);
        }

        final Calendar lastExecution = resetHours(params.getLastModifiedTo());
        final Calendar actualDate = resetHours(Calendar.getInstance());
        log.debug("lastExecution -> {}", lastExecution.getTime());
        log.debug("actualDate -> {}", actualDate.getTime());

        if (lastExecution.before(actualDate)) {
            params.setLastModifiedFrom(params.getLastModifiedTo());
        }

        params.setLastModifiedTo(Calendar.getInstance());

        // Prepare statement
        log.debug("PARAMS {}", params);
        final String qs = directSearch.prepareStatement(params);
        log.debug("STATEMENT {}", qs);

        // Execute query
        al = executeQueryDocument(session, qs, null, MAX_RESULTS);

        // Update query params
        QueryParamsDAO.update(params);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(),
                Long.toString(params.getId()), al);
        log.debug("find: {}", al);
        return al;
    }

    /**
     * Reset calendar hours
     */
    private Calendar resetHours(final Calendar cal) {
        final Calendar tmp = (Calendar) cal.clone();
        tmp.set(Calendar.HOUR_OF_DAY, 0);
        tmp.set(Calendar.MINUTE, 0);
        tmp.set(Calendar.SECOND, 0);
        tmp.set(Calendar.MILLISECOND, 0);
        return tmp;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopDownloadedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastWeekTopDownloadedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastWeekTopDownloadedDocuments(session);
            log.debug("getLastWeekTopDownloadedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastWeekTopDownloadedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastWeekTopDownloadedDocuments({})", session);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "and a.date>:date group by a.item order by count(a.item) desc";
        final String SOURCE = "LastWeekTopDownloadedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, cal);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getLastWeekTopDownloadedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopDownloadedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastMonthTopDownloadedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastMonthTopDownloadedDocuments(session);
            log.debug("getLastMonthTopDownloadedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastMonthTopDownloadedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastMonthTopDownloadedDocuments({})", session);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "and a.date>:date group by a.item order by count(a.item) desc";
        final String SOURCE = "LastMonthTopDownloadedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, cal);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getLastMonthTopDownloadedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopModifiedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastWeekTopModifiedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastWeekTopModifiedDocuments(session);
            log.debug("getLastWeekTopModifiedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastWeekTopModifiedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastWeekTopModifiedDocuments({})", session);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "and a.date>:date group by a.item order by count(a.item) desc";
        final String SOURCE = "LastWeekTopModifiedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, cal);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getLastWeekTopModifiedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopModifiedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastMonthTopModifiedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastMonthTopModifiedDocuments(session);
            log.debug("getLastMonthTopModifiedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastMonthTopModifiedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastMonthTopModifiedDocuments({})", session);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "and a.date>:date group by a.item order by count(a.item) desc";
        final String SOURCE = "LastMonthTopModifiedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, cal);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getLastMonthTopModifiedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastModifiedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastModifiedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastModifiedDocuments(session);
            log.debug("getLastModifiedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastModifiedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastModifiedDocuments({})", session);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "LastModifiedDocuments";
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, null);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);
        log.debug("getLastModifiedDocuments: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastUploadedDocuments(
            final String token) throws RepositoryException {
        log.debug("getLastUploadedDocuments({})", token);
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final List<DashboardDocumentResult> al = getLastUploadedDocuments(session);
            log.debug("getLastUploadedDocuments: {}", al);
            return al;
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    /**
     * Convenient method for syndication
     * TODO Tiene sentido agrupar por item siendo el UUID único? 
     */
    public List<DashboardDocumentResult> getLastUploadedDocuments(
            final Session session) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getLastUploadedDocuments({})", session);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CREATE_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "LastUploadedDocuments";
        final List<DashboardDocumentResult> al = getTopDocuments(session,
                SOURCE, qs, null);

        // Check for already visited results
        checkVisitedDocuments(session.getUserID(), SOURCE, al);

        log.debug("getLastUploadedDocuments: {}", al);
        return al;
    }

    /**
     * Get top documents
     */
    @SuppressWarnings("unchecked")
    private ArrayList<DashboardDocumentResult> getTopDocuments(
            final Session session, final String source, final String qs,
            final Calendar date) throws javax.jcr.RepositoryException,
            DatabaseException {
        log.debug("getTopDocuments({}, {}, {}, {})", new Object[] { session,
                source, qs, date != null ? date.getTime() : "null" });
        final ArrayList<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        org.hibernate.Session hSession = null;
        int cont = 0;

        try {
            hSession = HibernateUtil.getSessionFactory().openSession();
            final org.hibernate.Query q = hSession.createQuery(qs)
                    .setFetchSize(MAX_RESULTS);

            if (date != null) {
                q.setCalendar("date", date);
            }

            // While there is more query results and the MAX_RESULT limit has reached
            for (final Iterator<Object[]> it = q.iterate(); it.hasNext()
                    && cont < MAX_RESULTS; cont++) {
                final Object[] obj = it.next();
                final String resItem = (String) obj[0];
                final Calendar resDate = (Calendar) obj[1];

                try {
                    final Node node = session.getNodeByUUID(resItem);

                    // Only documents from taxonomy
                    // Already filtered in the query
                    // if (node.getPath().startsWith("/okm:root")) {
                    final Document doc = BaseDocumentModule.getProperties(
                            session, node);
                    final DashboardDocumentResult vo = new DashboardDocumentResult();
                    vo.setDocument(doc);
                    vo.setDate(resDate);
                    vo.setVisited(false);
                    al.add(vo);
                    // }
                } catch (final ItemNotFoundException e) {
                    // Do nothing
                }
            }
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(hSession);
        }

        log.debug("getTopDocuments: {}", al);
        return al;
    }

    @Override
    public void visiteNode(final String token, final String source,
            final String node, final Calendar date) throws RepositoryException {
        log.debug("visiteNode({}, {}, {}, {})", new Object[] { token, source,
                node, date == null ? null : date.getTime() });
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Dashboard vo = new Dashboard();
            vo.setUser(session.getUserID());
            vo.setSource(source);
            vo.setNode(node);
            vo.setDate(date);
            DashboardDAO.createIfNew(vo);
        } catch (final DatabaseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("visiteNode: void");
    }

    /**
     * Check visited documents
     */
    private void checkVisitedDocuments(final String user, final String source,
            final List<DashboardDocumentResult> docResult)
            throws DatabaseException {
        final List<Dashboard> visitedNodes = DashboardDAO.findByUserSource(
                user, source);

        // Set already visited nodes
        for (final DashboardDocumentResult dsDocResult : docResult) {
            for (final Dashboard visitedNode : visitedNodes) {
                // Same node path and same activity log date ? 
                if (visitedNode.getNode().equals(
                        dsDocResult.getDocument().getPath())
                        && visitedNode.getDate().equals(dsDocResult.getDate())) {
                    dsDocResult.setVisited(true);
                }
            }
        }

        for (final Dashboard visitedNode : visitedNodes) {
            boolean old = true;

            for (final DashboardDocumentResult dsDocResult : docResult) {
                // Same node path and same activity log date ? 
                if (visitedNode.getNode().equals(
                        dsDocResult.getDocument().getPath())
                        && visitedNode.getDate().equals(dsDocResult.getDate())) {
                    old = false;
                }
            }

            if (old) {
                DashboardDAO.purgeOldVisitedNode(user, source,
                        visitedNode.getNode(), visitedNode.getDate());
            }
        }
    }

    /**
     * Check visited folders
     */
    private void checkVisitedFolders(final String user, final String source,
            final List<DashboardFolderResult> fldResult)
            throws DatabaseException {
        final List<Dashboard> visitedNodes = DashboardDAO.findByUserSource(
                user, source);

        // Set already visited nodes
        for (final DashboardFolderResult dsFldResult : fldResult) {
            for (final Dashboard visitedNode : visitedNodes) {
                if (visitedNode.getNode().equals(
                        dsFldResult.getFolder().getPath())
                        && visitedNode.getDate().equals(dsFldResult.getDate())) {
                    dsFldResult.setVisited(true);
                }
            }
        }

        for (final Dashboard visitedNode : visitedNodes) {
            boolean old = true;

            for (final DashboardFolderResult dsFldResult : fldResult) {
                // Same node path and same activity log date ? 
                if (visitedNode.getNode().equals(
                        dsFldResult.getFolder().getPath())
                        && visitedNode.getDate().equals(dsFldResult.getDate())) {
                    old = false;
                }
            }

            if (old) {
                DashboardDAO.purgeOldVisitedNode(user, source,
                        visitedNode.getNode(), visitedNode.getDate());
            }
        }
    }

    /**
     * Check visited mails
     */
    private void checkVisitedMails(final String user, final String source,
            final List<DashboardMailResult> mailResult)
            throws DatabaseException {
        final List<Dashboard> visitedNodes = DashboardDAO.findByUserSource(
                user, source);

        // Set already visited nodes
        for (final DashboardMailResult dsMailResult : mailResult) {
            for (final Dashboard visitedNode : visitedNodes) {
                // Same node path and same activity log date ? 
                if (visitedNode.getNode().equals(
                        dsMailResult.getMail().getPath())
                        && visitedNode.getDate().equals(dsMailResult.getDate())) {
                    dsMailResult.setVisited(true);
                }
            }
        }

        for (final Dashboard visitedNode : visitedNodes) {
            boolean old = true;

            for (final DashboardMailResult dsMailResult : mailResult) {
                // Same node path and same activity log date ? 
                if (visitedNode.getNode().equals(
                        dsMailResult.getMail().getPath())
                        && visitedNode.getDate().equals(dsMailResult.getDate())) {
                    old = false;
                }
            }

            if (old) {
                DashboardDAO.purgeOldVisitedNode(user, source,
                        visitedNode.getNode(), visitedNode.getDate());
            }
        }
    }
}
