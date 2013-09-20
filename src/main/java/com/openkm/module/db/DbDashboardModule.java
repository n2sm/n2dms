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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import com.openkm.bean.DashboardDocumentResult;
import com.openkm.bean.DashboardFolderResult;
import com.openkm.bean.DashboardMailResult;
import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.Permission;
import com.openkm.bean.Repository;
import com.openkm.bean.nr.NodeQueryResult;
import com.openkm.bean.nr.NodeResultSet;
import com.openkm.cache.UserItemsManager;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.ActivityDAO;
import com.openkm.dao.DashboardDAO;
import com.openkm.dao.HibernateUtil;
import com.openkm.dao.NodeDocumentDAO;
import com.openkm.dao.NodeFolderDAO;
import com.openkm.dao.NodeMailDAO;
import com.openkm.dao.QueryParamsDAO;
import com.openkm.dao.SearchDAO;
import com.openkm.dao.bean.Activity;
import com.openkm.dao.bean.Dashboard;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.dao.bean.QueryParams;
import com.openkm.dao.bean.cache.UserItems;
import com.openkm.module.DashboardModule;
import com.openkm.module.db.base.BaseDocumentModule;
import com.openkm.module.db.base.BaseFolderModule;
import com.openkm.module.db.base.BaseMailModule;
import com.openkm.module.db.stuff.DbUtils;
import com.openkm.module.db.stuff.SecurityHelper;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.UserActivity;

public class DbDashboardModule implements DashboardModule {
    private static Logger log = LoggerFactory
            .getLogger(DbDashboardModule.class);

    private static final int MAX_RESULTS = 20;

    @Override
    public List<DashboardDocumentResult> getUserLockedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLockedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserLockedDocumentsSrv(auth
                    .getName());
            log.debug("getUserLockedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLockedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getUserLockedDocumentsSrv({})", user);
        final String qs = "from NodeDocument nd where nd.checkedOut='F' and nd.lock.owner=:user";
        final List<DashboardDocumentResult> al = executeQueryDocument(user, qs,
                "LOCK_DOCUMENT", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(user, "UserLockedDocuments", al);
        log.debug("getUserLockedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserCheckedOutDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserCheckedOutDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserCheckedOutDocumentsSrv(auth
                    .getName());
            log.debug("getUserCheckedOutDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserCheckedOutDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getUserCheckedOutDocumentsSrv({})", user);
        final String qs = "from NodeDocument nd where nd.checkedOut='T' and nd.lock.owner=:user";
        final List<DashboardDocumentResult> al = executeQueryDocument(user, qs,
                "CHECKOUT_DOCUMENT", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(user, "UserCheckedOutDocuments", al);
        log.debug("getUserCheckedOutDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserSubscribedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserSubscribedDocumentsSrv(auth
                    .getName());
            log.debug("getUserSubscribedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserSubscribedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedDocumentsSrv({})", user);
        final String qs = "from NodeDocument nd where :user in elements(nd.subscriptors)";
        final List<DashboardDocumentResult> al = executeQueryDocument(user, qs,
                "SUBSCRIBE_USER", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedDocuments(user, "UserSubscribedDocuments", al);
        log.debug("getUserSubscribedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardFolderResult> getUserSubscribedFolders(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedFolders({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardFolderResult> al = getUserSubscribedFoldersSrv(auth
                    .getName());
            log.debug("getUserSubscribedFolders: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardFolderResult> getUserSubscribedFoldersSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedFoldersSrv({})", user);
        final String qs = "from NodeFolder nf where '" + user
                + "' in elements(nf.subscriptors)";
        final List<DashboardFolderResult> al = executeQueryFolder(user, qs,
                "SUBSCRIBE_USER", Integer.MAX_VALUE);

        // Check for already visited results
        checkVisitedFolders(user, "UserSubscribedFolders", al);
        log.debug("getUserSubscribedFoldersSrv: {}", al);
        return al;
    }

    /**
     * Execute query with documents
     */
    @SuppressWarnings("unchecked")
    private List<DashboardDocumentResult> executeQueryDocument(
            final String user, final String qs, final String action,
            final int maxResults) throws RepositoryException, DatabaseException {
        log.debug("executeQueryDocument({}, {}, {}, {})", new Object[] { user,
                qs, action, maxResults });
        final List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        Session session = null;
        int i = 0;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            q.setString("user", user);
            final List<NodeDocument> results = q.list();

            for (final Iterator<NodeDocument> it = results.iterator(); it
                    .hasNext() && i < maxResults;) {
                final NodeDocument nDoc = it.next();

                if (SecurityHelper.getAccessManager().isGranted(nDoc,
                        Permission.READ)) {
                    NodeDocumentDAO.getInstance().initialize(nDoc, false);
                    final Document doc = BaseDocumentModule.getProperties(user,
                            nDoc);
                    final DashboardDocumentResult vo = new DashboardDocumentResult();
                    vo.setDocument(doc);
                    vo.setDate(ActivityDAO.getActivityDate(user, action,
                            nDoc.getUuid()));
                    vo.setVisited(false);
                    al.add(vo);
                    i++;
                }
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
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
    @SuppressWarnings("unchecked")
    private List<DashboardFolderResult> executeQueryFolder(final String user,
            final String qs, final String action, final int maxResults)
            throws RepositoryException, DatabaseException {
        final List<DashboardFolderResult> al = new ArrayList<DashboardFolderResult>();
        Session session = null;
        int i = 0;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs);
            final List<NodeFolder> results = q.list();

            for (final Iterator<NodeFolder> it = results.iterator(); it
                    .hasNext() && i < maxResults;) {
                final NodeFolder nFld = it.next();

                if (SecurityHelper.getAccessManager().isGranted(nFld,
                        Permission.READ)) {
                    NodeFolderDAO.getInstance().initialize(nFld);
                    final Folder fld = BaseFolderModule.getProperties(user,
                            nFld);
                    final DashboardFolderResult vo = new DashboardFolderResult();
                    vo.setFolder(fld);
                    vo.setDate(ActivityDAO.getActivityDate(user, action,
                            nFld.getUuid()));
                    vo.setVisited(false);
                    al.add(vo);
                    i++;
                }
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
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
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastUploadedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserLastUploadedDocumentsSrv(auth
                    .getName());
            log.debug("getUserLastUploadedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastUploadedDocumentsSrv(
            final String user) throws DatabaseException {
        log.debug("getUserLastUploadedDocumentsSrv({})", user);
        final String qs = "select a.item, a.date from Activity a "
                + "where a.action='CREATE_DOCUMENT' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastUploadedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(user, SOURCE,
                qs);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getUserLastUploadedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastModifiedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserLastModifiedDocumentsSrv(auth
                    .getName());
            log.debug("getUserLastModifiedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastModifiedDocumentsSrv(
            final String user) throws DatabaseException {
        log.debug("getUserLastModifiedDocumentsSrv({})", user);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.user= :user "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "UserLastModifiedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(user, SOURCE,
                qs);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getUserLastModifiedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastDownloadedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserLastDownloadedDocumentsSrv(auth
                    .getName());
            log.debug("getUserLastDownloadedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getUserLastDownloadedDocumentsSrv(
            final String user) throws DatabaseException {
        log.debug("getUserLastDownloadedDocumentsSrv({})", user);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.user= :user "
                + "group by a.item order by max(a.date) desc";
        final String SOURCE = "UserLastDownloadedDocuments";
        final List<DashboardDocumentResult> al = getUserDocuments(user, SOURCE,
                qs);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getUserLastDownloadedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardMailResult> getUserLastImportedMails(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserLastImportedMails({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardMailResult> al = getUserLastImportedMailsSrv(auth
                    .getName());
            log.debug("getUserLastImportedMails: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardMailResult> getUserLastImportedMailsSrv(
            final String user) throws DatabaseException {
        log.debug("getUserLastImportedMailsSrv({})", user);
        final String qs = "from Activity a "
                + "where a.action='CREATE_MAIL' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastImportedMails";
        final List<DashboardMailResult> al = getUserMails(user, SOURCE, qs);

        // Check for already visited results
        checkVisitedMails(user, SOURCE, al);
        log.debug("getUserLastImportedMailsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastImportedMailAttachments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastImportedMailAttachments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getUserLastImportedMailAttachmentsSrv(auth
                    .getName());
            log.debug("getUserLastImportedMailAttachments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    public List<DashboardDocumentResult> getUserLastImportedMailAttachmentsSrv(
            final String user) throws DatabaseException {
        log.debug("getUserLastImportedMailAttachmentsSrv({})", user);
        final String qs = "select a.item, a.date from Activity a "
                + "where a.action='CREATE_MAIL_ATTACHMENT' and a.user= :user "
                + "order by a.date desc";
        final String SOURCE = "UserLastImportedMailAttachments";
        final List<DashboardDocumentResult> al = getUserDocuments(user, SOURCE,
                qs);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getUserLastImportedMailAttachmentsSrv: {}", al);
        return al;
    }

    @Override
    public long getUserDocumentsSize(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserDocumentsSize({})", token);
        Authentication auth = null, oldAuth = null;
        long size = 0;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (Config.USER_ITEM_CACHE) {
                final UserItems usrItems = UserItemsManager.get(auth.getName());
                size = usrItems.getSize();
            } else {
                size = DbUtils.calculateQuota(auth.getName());
            }

            log.debug("getUserDocumentsSize: {}", size);
            return size;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    @Override
    public List<QueryParams> getUserSearchs(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserSearchs({})", token);
        final List<QueryParams> ret = new ArrayList<QueryParams>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<QueryParams> qParams = QueryParamsDAO.findByUser(auth
                    .getName());

            for (final QueryParams qp : qParams) {
                // If this is a dashboard user search, dates are used internally
                if (qp.isDashboard()) {
                    qp.setLastModifiedFrom(null);
                    qp.setLastModifiedTo(null);
                    ret.add(qp);
                }
            }

            // Activity log
            UserActivity.log(auth.getName(), "GET_DASHBOARD_USER_SEARCHS",
                    null, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
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
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = findSrv(auth.getName(), qpId);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("find: {}", al);
        return al;
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> findSrv(final String user,
            final int qpId) throws RepositoryException, DatabaseException,
            ParseException, IOException {
        log.debug("findSrv({}, {})", user, qpId);
        List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        final DbSearchModule directSearch = new DbSearchModule();

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
        final org.apache.lucene.search.Query query = directSearch
                .prepareStatement(params);
        log.debug("STATEMENT {}", query);

        // Execute query
        al = executeQueryDocument(user, query, MAX_RESULTS);

        // Update query params
        QueryParamsDAO.update(params);

        // Check for already visited results
        checkVisitedDocuments(user, Long.toString(params.getId()), al);
        log.debug("findSrv: {}", al);
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

    /**
     * Execute Lucene query with documents
     */
    private List<DashboardDocumentResult> executeQueryDocument(
            final String user, final org.apache.lucene.search.Query query,
            final int maxResults) throws RepositoryException, DatabaseException {
        final List<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();

        try {
            final NodeResultSet nrs = SearchDAO.getInstance().findByQuery(
                    query, 0, maxResults);

            for (final NodeQueryResult nqr : nrs.getResults()) {
                final DashboardDocumentResult vo = new DashboardDocumentResult();
                final NodeDocument nDoc = nqr.getDocument();
                final Document doc = BaseDocumentModule.getProperties(user,
                        nDoc);
                vo.setDocument(doc);
                vo.setDate(ActivityDAO.getActivityDate(user, null,
                        nDoc.getUuid()));
                vo.setVisited(false);
                al.add(vo);
            }
        } catch (final ParseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        }

        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastWeekTopDownloadedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastWeekTopDownloadedDocumentsSrv(auth
                    .getName());
            log.debug("getLastWeekTopDownloadedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastWeekTopDownloadedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getUserLastImportedMailAttachmentsSrv({})", user);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.path like '/"
                + Repository.ROOT + "/%' and a.date>:date "
                + "group by a.item " + "order by count(a.item) desc";
        final String SOURCE = "LastWeekTopDownloadedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, cal);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getUserLastImportedMailAttachmentsByUser: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopDownloadedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastMonthTopDownloadedDocumentsSrv(auth
                    .getName());
            log.debug("getLastMonthTopDownloadedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastMonthTopDownloadedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopDownloadedDocumentsSrv({})", user);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='GET_DOCUMENT_CONTENT' and a.path like '/"
                + Repository.ROOT + "/%' and a.date>:date "
                + "group by a.item " + "order by count(a.item) desc";
        final String SOURCE = "LastMonthTopDownloadedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, cal);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getLastMonthTopDownloadedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastWeekTopModifiedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastWeekTopModifiedDocumentsSrv(auth
                    .getName());
            log.debug("getLastWeekTopModifiedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastWeekTopModifiedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getLastWeekTopModifiedDocumentsSrv({})", user);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT + "/%' and a.date>:date "
                + "group by a.item " + "order by count(a.item) desc";
        final String SOURCE = "LastWeekTopModifiedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, cal);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getLastWeekTopModifiedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopModifiedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastMonthTopModifiedDocumentsSrv(auth
                    .getName());
            log.debug("getLastMonthTopModifiedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastMonthTopModifiedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopModifiedDocumentsSrv({})", user);
        final String qs = "select a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT + "/%' and a.date>:date "
                + "group by a.item " + "order by count(a.item) desc";
        final String SOURCE = "LastMonthTopModifiedDocuments";
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, cal);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getLastMonthTopModifiedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastModifiedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastModifiedDocumentsSrv(auth
                    .getName());
            log.debug("getLastModifiedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastModifiedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getLastModifiedDocumentsSrv({})", user);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CHECKIN_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "group by a.item "
                + "order by max(a.date) desc";
        final String SOURCE = "LastModifiedDocuments";
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, null);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getLastModifiedDocumentsSrv: {}", al);
        return al;
    }

    @Override
    public List<DashboardDocumentResult> getLastUploadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastUploadedDocuments({})", token);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final List<DashboardDocumentResult> al = getLastUploadedDocumentsSrv(auth
                    .getName());
            log.debug("getLastUploadedDocuments: {}", al);
            return al;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }
    }

    /**
     * Convenient method for syndication
     */
    public List<DashboardDocumentResult> getLastUploadedDocumentsSrv(
            final String user) throws RepositoryException, DatabaseException {
        log.debug("getLastUploadedDocumentsSrv({})", user);
        final String qs = "select distinct a.item, max(a.date) from Activity a "
                + "where a.action='CREATE_DOCUMENT' and a.path like '/"
                + Repository.ROOT
                + "/%' "
                + "group by a.item "
                + "order by max(a.date) desc";
        final String SOURCE = "LastUploadedDocuments";
        final List<DashboardDocumentResult> al = getTopDocuments(user, SOURCE,
                qs, null);

        // Check for already visited results
        checkVisitedDocuments(user, SOURCE, al);
        log.debug("getLastUploadedDocumentsSrv: {}", al);
        return al;
    }

    /**
     * Get top documents
     */
    @SuppressWarnings("unchecked")
    private ArrayList<DashboardDocumentResult> getTopDocuments(
            final String user, final String source, final String qs,
            final Calendar date) throws RepositoryException, DatabaseException {
        log.debug("getTopDocuments({}, {}, {}, {})", new Object[] { user,
                source, qs, date != null ? date.getTime() : "null" });
        final ArrayList<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        Session session = null;
        int cont = 0;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            final Query q = session.createQuery(qs).setFetchSize(MAX_RESULTS);

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
                    final NodeDocument nDoc = NodeDocumentDAO.getInstance()
                            .findByPk(resItem);
                    // String docPath = NodeBaseDAO.getInstance().getPathFromUuid(nDoc.getUuid());

                    // Only documents from taxonomy
                    // Already filtered in the query
                    // if (docPath.startsWith("/okm:root")) {
                    final Document doc = BaseDocumentModule.getProperties(user,
                            nDoc);
                    final DashboardDocumentResult vo = new DashboardDocumentResult();
                    vo.setDocument(doc);
                    vo.setDate(resDate);
                    vo.setVisited(false);
                    al.add(vo);
                    // }
                } catch (final PathNotFoundException e) {
                    // Do nothing
                }
            }
        } catch (final HibernateException e) {
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(session);
        }

        log.debug("getTopDocuments: {}", al);
        return al;
    }

    @Override
    public void visiteNode(final String token, final String source,
            final String node, final Calendar date) throws RepositoryException,
            DatabaseException {
        log.debug("visiteNode({}, {}, {}, {})", new Object[] { token, source,
                node, date == null ? null : date.getTime() });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final Dashboard vo = new Dashboard();
            vo.setUser(auth.getName());
            vo.setSource(source);
            vo.setNode(node);
            vo.setDate(date);
            DashboardDAO.createIfNew(vo);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
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
                        dsDocResult.getDocument().getUuid())
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
                        dsDocResult.getDocument().getUuid())
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
                        dsFldResult.getFolder().getUuid())
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
                        dsFldResult.getFolder().getUuid())
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
                        dsMailResult.getMail().getUuid())
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
                        dsMailResult.getMail().getUuid())
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

    /**
     * Get documents from statement
     */
    @SuppressWarnings("unchecked")
    private ArrayList<DashboardDocumentResult> getUserDocuments(
            final String user, final String source, final String qs)
            throws DatabaseException {
        log.debug("getUserDocuments({}, {}, {})", new Object[] { user, source,
                qs });
        final ArrayList<DashboardDocumentResult> al = new ArrayList<DashboardDocumentResult>();
        org.hibernate.Session hSession = null;

        try {
            hSession = HibernateUtil.getSessionFactory().openSession();
            final org.hibernate.Query q = hSession.createQuery(qs);
            q.setString("user", user);
            q.setMaxResults(MAX_RESULTS);

            for (final Iterator<Object[]> it = q.list().iterator(); it
                    .hasNext();) {
                final Object[] actData = it.next();
                final String actItem = (String) actData[0];
                final Calendar actDate = (Calendar) actData[1];

                try {
                    final NodeDocument nDoc = NodeDocumentDAO.getInstance()
                            .findByPk(actItem);
                    final Document doc = BaseDocumentModule.getProperties(user,
                            nDoc);
                    final DashboardDocumentResult vo = new DashboardDocumentResult();
                    vo.setDocument(doc);
                    vo.setDate(actDate);
                    vo.setVisited(false);
                    al.add(vo);
                } catch (final PathNotFoundException e) {
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
    private ArrayList<DashboardMailResult> getUserMails(final String user,
            final String source, final String qs) throws DatabaseException {
        log.debug("getUserMails({}, {}, {})", new Object[] { user, source, qs });
        final ArrayList<DashboardMailResult> al = new ArrayList<DashboardMailResult>();
        org.hibernate.Session hSession = null;

        try {
            hSession = HibernateUtil.getSessionFactory().openSession();
            final org.hibernate.Query q = hSession.createQuery(qs);
            q.setString("user", user);
            q.setMaxResults(MAX_RESULTS);

            for (final Iterator<Activity> it = q.list().iterator(); it
                    .hasNext();) {
                final Activity act = it.next();

                try {
                    final NodeMail nMail = NodeMailDAO.getInstance().findByPk(
                            act.getItem());
                    final Mail mail = BaseMailModule.getProperties(user, nMail);
                    final DashboardMailResult vo = new DashboardMailResult();
                    vo.setMail(mail);
                    vo.setDate(act.getDate());
                    vo.setVisited(false);
                    al.add(vo);
                } catch (final PathNotFoundException e) {
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
}
