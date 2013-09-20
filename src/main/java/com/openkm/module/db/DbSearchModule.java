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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.PropertyGroup;
import com.openkm.bean.QueryResult;
import com.openkm.bean.Repository;
import com.openkm.bean.ResultSet;
import com.openkm.bean.form.FormElement;
import com.openkm.bean.form.Input;
import com.openkm.bean.form.Select;
import com.openkm.bean.form.TextArea;
import com.openkm.bean.nr.NodeQueryResult;
import com.openkm.bean.nr.NodeResultSet;
import com.openkm.cache.UserNodeKeywordsManager;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.DashboardDAO;
import com.openkm.dao.HibernateUtil;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.NodeDocumentDAO;
import com.openkm.dao.NodeFolderDAO;
import com.openkm.dao.NodeMailDAO;
import com.openkm.dao.QueryParamsDAO;
import com.openkm.dao.SearchDAO;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.dao.bean.QueryParams;
import com.openkm.dao.bean.cache.UserNodeKeywords;
import com.openkm.module.SearchModule;
import com.openkm.module.db.base.BaseDocumentModule;
import com.openkm.module.db.base.BaseFolderModule;
import com.openkm.module.db.base.BaseMailModule;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.FormUtils;
import com.openkm.util.ISO8601;
import com.openkm.util.PathUtils;
import com.openkm.util.UserActivity;

public class DbSearchModule implements SearchModule {
    private static Logger log = LoggerFactory.getLogger(DbSearchModule.class);

    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat(
            "yyyyMMdd");

    @Override
    public List<QueryResult> findByContent(final String token,
            final String expression) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("findByContent({}, {})", token, expression);
        final QueryParams params = new QueryParams();
        params.setContent(expression);
        final List<QueryResult> ret = find(token, params);
        log.debug("findByContent: {}", ret);
        return ret;
    }

    @Override
    public List<QueryResult> findByName(final String token,
            final String expression) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("findByName({}, {})", token, expression);
        final QueryParams params = new QueryParams();
        params.setName(expression);
        final List<QueryResult> ret = find(token, params);
        log.debug("findByName: {}", ret);
        return ret;
    }

    @Override
    public List<QueryResult> findByKeywords(final String token,
            final Set<String> expression) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("findByKeywords({}, {})", token, expression);
        final QueryParams params = new QueryParams();
        params.setKeywords(expression);
        final List<QueryResult> ret = find(token, params);
        log.debug("findByKeywords: {}", ret);
        return ret;
    }

    @Override
    public List<QueryResult> find(final String token, final QueryParams params)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("find({}, {})", token, params);
        final List<QueryResult> ret = findPaginated(token, params, 0,
                Config.MAX_SEARCH_RESULTS).getResults();
        log.debug("find: {}", ret);
        return ret;
    }

    @Override
    public ResultSet findPaginated(final String token,
            final QueryParams params, final int offset, final int limit)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("findPaginated({}, {}, {}, {})", new Object[] { token,
                params, offset, limit });
        Query query = null;

        if (params.getStatementQuery() != null
                && !params.getStatementQuery().equals("")) {
            // query = params.getStatementQuery();
        } else {
            query = prepareStatement(params);
        }

        final ResultSet rs = findByStatementPaginated(token, query, offset,
                limit);
        log.debug("findPaginated: {}", rs);
        return rs;
    }

    /**
     * Prepare statement
     */
    public Query prepareStatement(final QueryParams params) throws IOException,
            ParseException, RepositoryException, DatabaseException {
        log.debug("prepareStatement({})", params);
        final BooleanQuery query = new BooleanQuery();

        // Clean params
        params.setName(params.getName() != null ? params.getName().trim() : "");
        params.setContent(params.getContent() != null ? params.getContent()
                .trim() : "");
        params.setKeywords(params.getKeywords() != null ? params.getKeywords()
                : new HashSet<String>());
        params.setCategories(params.getCategories() != null ? params
                .getCategories() : new HashSet<String>());
        params.setMimeType(params.getMimeType() != null ? params.getMimeType()
                .trim() : "");
        params.setAuthor(params.getAuthor() != null ? params.getAuthor().trim()
                : "");
        params.setPath(params.getPath() != null ? params.getPath().trim() : "");
        params.setMailSubject(params.getMailSubject() != null ? params
                .getMailSubject().trim() : "");
        params.setMailFrom(params.getMailFrom() != null ? params.getMailFrom()
                .trim() : "");
        params.setMailTo(params.getMailTo() != null ? params.getMailTo().trim()
                : "");
        params.setProperties(params.getProperties() != null ? params
                .getProperties() : new HashMap<String, String>());

        // Domains
        final boolean document = (params.getDomain() & QueryParams.DOCUMENT) != 0;
        final boolean folder = (params.getDomain() & QueryParams.FOLDER) != 0;
        final boolean mail = (params.getDomain() & QueryParams.MAIL) != 0;
        log.debug("doc={}, fld={}, mail={}", new Object[] { document, folder,
                mail });

        // Path to UUID conversion and in depth recursion
        final List<String> pathInDepth = new ArrayList<String>();

        if (!params.getPath().equals("")
                && !params.getPath().equals("/" + Repository.ROOT)
                && !params.getPath().equals("/" + Repository.CATEGORIES)
                && !params.getPath().equals("/" + Repository.TEMPLATES)
                && !params.getPath().equals("/" + Repository.PERSONAL)
                && !params.getPath().equals("/" + Repository.MAIL)
                && !params.getPath().equals("/" + Repository.TRASH)) {
            try {
                final String uuid = NodeBaseDAO.getInstance().getUuidFromPath(
                        params.getPath());
                log.debug("Path in depth: {} => {}", uuid, NodeBaseDAO
                        .getInstance().getPathFromUuid(uuid));
                pathInDepth.add(uuid);

                for (final String uuidChild : SearchDAO.getInstance()
                        .findFoldersInDepth(uuid)) {
                    log.debug("Path in depth: {} => {}", uuidChild, NodeBaseDAO
                            .getInstance().getPathFromUuid(uuidChild));
                    pathInDepth.add(uuidChild);
                }
            } catch (final PathNotFoundException e) {
                throw new RepositoryException("Path Not Found: "
                        + e.getMessage());
            }
        }

        /**
         * DOCUMENT
         */
        if (document) {
            final BooleanQuery queryDocument = new BooleanQuery();
            final Term tEntity = new Term("_hibernate_class",
                    NodeDocument.class.getCanonicalName());
            queryDocument.add(new TermQuery(tEntity), BooleanClause.Occur.MUST);

            if (!params.getContent().equals("")) {
                for (final StringTokenizer st = new StringTokenizer(
                        params.getContent(), " "); st.hasMoreTokens();) {
                    final Term t = new Term("text", st.nextToken()
                            .toLowerCase());
                    queryDocument.add(new WildcardQuery(t),
                            BooleanClause.Occur.MUST);
                }
            }

            if (!params.getName().equals("")) {
                if (!params.getName().contains("*")
                        && !params.getName().contains("?")) {
                    params.setName("*" + params.getName() + "*");
                }

                final Term t = new Term("name", params.getName().toLowerCase());
                queryDocument.add(new WildcardQuery(t),
                        BooleanClause.Occur.MUST);
            }

            if (!params.getPath().equals("")) {
                if (pathInDepth.isEmpty()) {
                    final Term t = new Term("context",
                            PathUtils.fixContext(params.getPath()));
                    queryDocument.add(new WildcardQuery(t),
                            BooleanClause.Occur.MUST);
                } else {
                    final BooleanQuery parent = new BooleanQuery();

                    for (final String uuid : pathInDepth) {
                        final Term tChild = new Term("parent", uuid);
                        parent.add(new TermQuery(tChild),
                                BooleanClause.Occur.SHOULD);
                    }

                    queryDocument.add(parent, BooleanClause.Occur.MUST);
                }
            }

            if (!params.getMimeType().equals("")) {
                final Term t = new Term("mimeType", params.getMimeType());
                queryDocument.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }

            if (!params.getAuthor().equals("")) {
                final Term t = new Term("author", params.getAuthor());
                queryDocument.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }

            if (params.getLastModifiedFrom() != null
                    && params.getLastModifiedTo() != null) {
                final Date from = params.getLastModifiedFrom().getTime();
                final String sFrom = DAY_FORMAT.format(from);
                final Date to = params.getLastModifiedTo().getTime();
                final String sTo = DAY_FORMAT.format(to);
                queryDocument.add(new TermRangeQuery("lastModified", sFrom,
                        sTo, true, true), BooleanClause.Occur.MUST);
            }

            appendCommon(params, queryDocument);
            query.add(queryDocument, BooleanClause.Occur.SHOULD);
        }

        /**
         * FOLDER
         */
        if (folder) {
            final BooleanQuery queryFolder = new BooleanQuery();
            final Term tEntity = new Term("_hibernate_class",
                    NodeFolder.class.getCanonicalName());
            queryFolder.add(new TermQuery(tEntity), BooleanClause.Occur.MUST);

            if (!params.getName().equals("")) {
                final Term t = new Term("name", params.getName().toLowerCase());
                queryFolder.add(new WildcardQuery(t), BooleanClause.Occur.MUST);
            }

            if (!params.getPath().equals("")) {
                if (pathInDepth.isEmpty()) {
                    final Term t = new Term("context",
                            PathUtils.fixContext(params.getPath()));
                    queryFolder.add(new WildcardQuery(t),
                            BooleanClause.Occur.MUST);
                } else {
                    final BooleanQuery parent = new BooleanQuery();

                    for (final String uuid : pathInDepth) {
                        final Term tChild = new Term("parent", uuid);
                        parent.add(new TermQuery(tChild),
                                BooleanClause.Occur.SHOULD);
                    }

                    queryFolder.add(parent, BooleanClause.Occur.MUST);
                }
            }

            appendCommon(params, queryFolder);
            query.add(queryFolder, BooleanClause.Occur.SHOULD);
        }

        /**
         * MAIL
         */
        if (mail) {
            final BooleanQuery queryMail = new BooleanQuery();
            final Term tEntity = new Term("_hibernate_class",
                    NodeMail.class.getCanonicalName());
            queryMail.add(new TermQuery(tEntity), BooleanClause.Occur.MUST);

            if (!params.getPath().equals("")) {
                if (pathInDepth.isEmpty()) {
                    final Term t = new Term("context",
                            PathUtils.fixContext(params.getPath()));
                    queryMail.add(new WildcardQuery(t),
                            BooleanClause.Occur.MUST);
                } else {
                    final BooleanQuery parent = new BooleanQuery();

                    for (final String uuid : pathInDepth) {
                        final Term tChild = new Term("parent", uuid);
                        parent.add(new TermQuery(tChild),
                                BooleanClause.Occur.SHOULD);
                    }

                    queryMail.add(parent, BooleanClause.Occur.MUST);
                }
            }

            if (!params.getContent().equals("")) {
                for (final StringTokenizer st = new StringTokenizer(
                        params.getContent(), " "); st.hasMoreTokens();) {
                    final Term t = new Term("content", st.nextToken()
                            .toLowerCase());
                    queryMail.add(new WildcardQuery(t),
                            BooleanClause.Occur.MUST);
                }
            }

            if (!params.getMailSubject().equals("")) {
                final Term t = new Term("subject", params.getMailSubject()
                        .toLowerCase());
                queryMail.add(new WildcardQuery(t), BooleanClause.Occur.MUST);
            }

            if (!params.getMailFrom().equals("")) {
                final Term t = new Term("from", params.getMailFrom()
                        .toLowerCase());
                queryMail.add(new WildcardQuery(t), BooleanClause.Occur.MUST);
            }

            if (!params.getMailTo().equals("")) {
                final Term t = new Term("to", params.getMailTo().toLowerCase());
                queryMail.add(new WildcardQuery(t), BooleanClause.Occur.MUST);
            }

            if (!params.getMimeType().equals("")) {
                final Term t = new Term("mimeType", params.getMimeType());
                queryMail.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }

            appendCommon(params, queryMail);
            query.add(queryMail, BooleanClause.Occur.SHOULD);
        }

        log.debug("prepareStatement: {}", query.toString());
        return query;
    }

    /**
     * Add common fields
     */
    private void appendCommon(final QueryParams params, final BooleanQuery query)
            throws IOException, ParseException {
        if (!params.getKeywords().isEmpty()) {
            for (final String keyword : params.getKeywords()) {
                final Term t = new Term("keyword", keyword);
                query.add(new WildcardQuery(t), BooleanClause.Occur.MUST);
            }
        }

        if (!params.getCategories().isEmpty()) {
            for (final String category : params.getCategories()) {
                final Term t = new Term("category", category);
                query.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
        }

        if (!params.getProperties().isEmpty()) {
            final Map<PropertyGroup, List<FormElement>> formsElements = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);

            for (final Map.Entry<String, String> ent : params.getProperties()
                    .entrySet()) {
                final FormElement fe = FormUtils.getFormElement(formsElements,
                        ent.getKey());

                if (fe != null && ent.getValue() != null) {
                    final String valueTrimmed = ent.getValue().trim()
                            .toLowerCase();

                    if (!valueTrimmed.equals("")) {
                        if (fe instanceof Select) {
                            if (((Select) fe).getType().equals(
                                    Select.TYPE_SIMPLE)) {
                                final Term t = new Term(ent.getKey(),
                                        valueTrimmed);
                                query.add(new TermQuery(t),
                                        BooleanClause.Occur.MUST);
                            } else {
                                final String[] options = valueTrimmed
                                        .split(",");

                                for (final String option : options) {
                                    final Term t = new Term(ent.getKey(),
                                            option);
                                    query.add(new TermQuery(t),
                                            BooleanClause.Occur.MUST);
                                }
                            }
                        } else if (fe instanceof Input
                                && ((Input) fe).getType().equals(
                                        Input.TYPE_DATE)) {
                            final String[] date = valueTrimmed.split(",");

                            if (date.length == 2) {
                                final Calendar from = ISO8601
                                        .parseBasic(date[0]);
                                final Calendar to = ISO8601.parseBasic(date[1]);

                                if (from != null && to != null) {
                                    final String sFrom = DAY_FORMAT.format(from
                                            .getTime());
                                    final String sTo = DAY_FORMAT.format(to
                                            .getTime());
                                    query.add(new TermRangeQuery(ent.getKey(),
                                            sFrom, sTo, true, true),
                                            BooleanClause.Occur.MUST);
                                }
                            }
                        } else if (fe instanceof Input
                                && ((Input) fe).getType().equals(
                                        Input.TYPE_TEXT)
                                || fe instanceof TextArea) {
                            for (final StringTokenizer st = new StringTokenizer(
                                    valueTrimmed, " "); st.hasMoreTokens();) {
                                final Term t = new Term(ent.getKey(), st
                                        .nextToken().toLowerCase());
                                query.add(new WildcardQuery(t),
                                        BooleanClause.Occur.MUST);
                            }
                        } else {
                            final Term t = new Term(ent.getKey(), valueTrimmed);
                            query.add(new WildcardQuery(t),
                                    BooleanClause.Occur.MUST);
                        }
                    }
                }
            }
        }
    }

    /**
     * Find by statement
     */
    private ResultSet findByStatementPaginated(final String token,
            final Query query, final int offset, final int limit)
            throws RepositoryException, DatabaseException {
        log.debug("findByStatementPaginated({}, {}, {}, {}, {})", new Object[] {
                token, query, offset, limit });
        final List<QueryResult> results = new ArrayList<QueryResult>();
        final ResultSet rs = new ResultSet();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (query != null) {
                final NodeResultSet nrs = SearchDAO.getInstance().findByQuery(
                        query, offset, limit);
                rs.setTotal(nrs.getTotal());

                for (final NodeQueryResult nqr : nrs.getResults()) {
                    final QueryResult qr = new QueryResult();
                    qr.setExcerpt(nqr.getExcerpt());
                    qr.setScore((long) (100 * nqr.getScore()));

                    if (nqr.getDocument() != null) {
                        qr.setDocument(BaseDocumentModule.getProperties(
                                auth.getName(), nqr.getDocument()));
                    } else if (nqr.getFolder() != null) {
                        qr.setFolder(BaseFolderModule.getProperties(
                                auth.getName(), nqr.getFolder()));
                    } else if (nqr.getMail() != null) {
                        qr.setMail(BaseMailModule.getProperties(auth.getName(),
                                nqr.getMail()));
                    } else if (nqr.getAttachment() != null) {
                        qr.setAttachment(BaseDocumentModule.getProperties(
                                auth.getName(), nqr.getAttachment()));
                    }

                    results.add(qr);
                }

                rs.setResults(results);
            }

            // Activity log
            UserActivity.log(auth.getName(), "FIND_BY_STATEMENT_PAGINATED",
                    null, null, offset + ", " + limit + ", " + query);
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final ParseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findByStatementPaginated: {}", rs);
        return rs;
    }

    @Override
    public long saveSearch(final String token, final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("saveSearch({}, {})", token, params);
        Authentication auth = null, oldAuth = null;
        long id = 0;

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

            params.setUser(auth.getName());
            id = QueryParamsDAO.create(params);

            // Activity log
            UserActivity.log(auth.getName(), "SAVE_SEARCH", params.getName(),
                    null, params.toString());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("saveSearch: {}", id);
        return id;
    }

    @Override
    public void updateSearch(final String token, final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("updateSearch({}, {})", token, params);
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

            params.setUser(auth.getName());
            QueryParamsDAO.update(params);

            // Activity log
            UserActivity.log(auth.getName(), "UPDATE_SEARCH", params.getName(),
                    null, params.toString());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("updateSearch: void");
    }

    @Override
    public QueryParams getSearch(final String token, final int qpId)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getSearch({}, {})", token, qpId);
        QueryParams qp = new QueryParams();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            qp = QueryParamsDAO.findByPk(qpId);

            // If this is a dashboard user search, dates are used internally
            if (qp.isDashboard()) {
                qp.setLastModifiedFrom(null);
                qp.setLastModifiedTo(null);
            }

            // Activity log
            UserActivity.log(auth.getName(), "GET_SAVED_SEARCH",
                    Integer.toString(qpId), null, qp.toString());
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getSearch: {}", qp);
        return qp;
    }

    @Override
    public List<QueryParams> getAllSearchs(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getAllSearchs({})", token);
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
                if (!qp.isDashboard()) {
                    ret.add(qp);
                }
            }

            // Activity log
            UserActivity.log(auth.getName(), "GET_ALL_SEARCHS", null, null,
                    null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getAllSearchs: {}", ret);
        return ret;
    }

    @Override
    public void deleteSearch(final String token, final long qpId)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("deleteSearch({}, {})", token, qpId);
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

            final QueryParams qp = QueryParamsDAO.findByPk(qpId);
            QueryParamsDAO.delete(qpId);

            // Purge visited nodes table
            if (qp.isDashboard()) {
                DashboardDAO.deleteVisitedNodes(auth.getName(), qp.getName());
            }

            // Activity log
            UserActivity.log(auth.getName(), "DELETE_SAVED_SEARCH",
                    Long.toString(qpId), null, null);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("deleteSearch: void");
    }

    @Override
    public Map<String, Integer> getKeywordMap(final String token,
            final List<String> filter) throws RepositoryException,
            DatabaseException {
        log.debug("getKeywordMap({}, {})", token, filter);
        Map<String, Integer> cloud = null;

        if (Config.USER_KEYWORDS_CACHE) {
            cloud = getKeywordMapCached(token, filter);
        } else {
            cloud = getKeywordMapLive(token, filter);
        }

        log.debug("getKeywordMap: {}", cloud);
        return cloud;
    }

    /**
     * Get keyword map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Integer> getKeywordMapLive(final String token,
            final List<String> filter) throws RepositoryException,
            DatabaseException {
        log.debug("getKeywordMapLive({}, {})", token, filter);
        final String qs = "select elements(nb.keywords) from NodeBase nb";
        final HashMap<String, Integer> cloud = new HashMap<String, Integer>();
        org.hibernate.Session hSession = null;
        Transaction tx = null;
        @SuppressWarnings("unused")
        Authentication oldAuth = null;

        try {
            if (token == null) {
                PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                PrincipalUtils.getAuthenticationByToken(token);
            }

            hSession = HibernateUtil.getSessionFactory().openSession();
            tx = hSession.beginTransaction();
            final org.hibernate.Query hq = hSession.createQuery(qs);
            final List<String> nodeKeywords = hq.list();

            if (filter != null && nodeKeywords.containsAll(filter)) {
                for (final String keyword : nodeKeywords) {
                    if (!filter.contains(keyword)) {
                        final Integer occurs = cloud.get(keyword) != null ? cloud
                                .get(keyword) : 0;
                        cloud.put(keyword, occurs + 1);
                    }
                }
            }

            HibernateUtil.commit(tx);
        } catch (final HibernateException e) {
            HibernateUtil.rollback(tx);
            throw new DatabaseException(e.getMessage(), e);
        } finally {
            HibernateUtil.close(hSession);

            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getKeywordMapLive: {}", cloud);
        return cloud;
    }

    /**
     * Get keyword map
     */
    private Map<String, Integer> getKeywordMapCached(final String token,
            final List<String> filter) throws RepositoryException,
            DatabaseException {
        log.debug("getKeywordMapCached({}, {})", token, filter);
        final HashMap<String, Integer> keywordMap = new HashMap<String, Integer>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final Collection<UserNodeKeywords> userDocKeywords = UserNodeKeywordsManager
                    .get(auth.getName()).values();

            for (final UserNodeKeywords userNodeKeywords : userDocKeywords) {
                final Set<String> docKeywords = userNodeKeywords.getKeywords();

                if (filter != null && docKeywords.containsAll(filter)) {
                    for (final String keyword : docKeywords) {
                        if (!filter.contains(keyword)) {
                            final Integer occurs = keywordMap.get(keyword) != null ? keywordMap
                                    .get(keyword) : 0;
                            keywordMap.put(keyword, occurs + 1);
                        }
                    }
                }
            }
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getKeywordMapCached: {}", keywordMap);
        return keywordMap;
    }

    @Override
    public List<Document> getCategorizedDocuments(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedDocuments({}, {})", token, categoryId);
        final List<Document> documents = new ArrayList<Document>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                    .findByCategory(categoryId)) {
                documents.add(BaseDocumentModule.getProperties(auth.getName(),
                        nDoc));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getCategorizedDocuments: {}", documents);
        return documents;
    }

    @Override
    public List<Folder> getCategorizedFolders(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedFolders({}, {})", token, categoryId);
        final List<Folder> folders = new ArrayList<Folder>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeFolder nFld : NodeFolderDAO.getInstance()
                    .findByCategory(categoryId)) {
                folders.add(BaseFolderModule.getProperties(auth.getName(), nFld));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getCategorizedFolders: {}", folders);
        return folders;
    }

    @Override
    public List<Mail> getCategorizedMails(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedMails({}, {})", token, categoryId);
        final List<Mail> mails = new ArrayList<Mail>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeMail nMail : NodeMailDAO.getInstance()
                    .findByCategory(categoryId)) {
                mails.add(BaseMailModule.getProperties(auth.getName(), nMail));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getCategorizedMails: {}", mails);
        return mails;
    }

    @Override
    public List<Document> getDocumentsByKeyword(final String token,
            final String keyword) throws RepositoryException, DatabaseException {
        log.debug("getDocumentsByKeyword({}, {})", token, keyword);
        final List<Document> documents = new ArrayList<Document>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                    .findByKeyword(keyword)) {
                documents.add(BaseDocumentModule.getProperties(auth.getName(),
                        nDoc));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getDocumentsByKeyword: {}", documents);
        return documents;
    }

    @Override
    public List<Folder> getFoldersByKeyword(final String token,
            final String keyword) throws RepositoryException, DatabaseException {
        log.debug("getFoldersByKeyword({}, {})", token, keyword);
        final List<Folder> folders = new ArrayList<Folder>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeFolder nFld : NodeFolderDAO.getInstance()
                    .findByKeyword(keyword)) {
                folders.add(BaseFolderModule.getProperties(auth.getName(), nFld));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getFoldersByKeyword: {}", folders);
        return folders;
    }

    @Override
    public List<Mail> getMailsByKeyword(final String token, final String keyword)
            throws RepositoryException, DatabaseException {
        log.debug("getMailsByKeyword({}, {})", token, keyword);
        final List<Mail> mails = new ArrayList<Mail>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeMail nMail : NodeMailDAO.getInstance()
                    .findByKeyword(keyword)) {
                mails.add(BaseMailModule.getProperties(auth.getName(), nMail));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getMailsByKeyword: {}", mails);
        return mails;
    }

    @Override
    public List<Document> getDocumentsByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getDocumentsByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final List<Document> documents = new ArrayList<Document>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeDocument nDoc : NodeDocumentDAO.getInstance()
                    .findByPropertyValue(group, property, value)) {
                documents.add(BaseDocumentModule.getProperties(auth.getName(),
                        nDoc));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getDocumentsByPropertyValue: {}", documents);
        return documents;
    }

    @Override
    public List<Folder> getFoldersByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getFoldersByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final List<Folder> folders = new ArrayList<Folder>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeFolder nFld : NodeFolderDAO.getInstance()
                    .findByPropertyValue(group, property, value)) {
                folders.add(BaseFolderModule.getProperties(auth.getName(), nFld));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getFoldersByPropertyValue: {}", folders);
        return folders;
    }

    @Override
    public List<Mail> getMailsByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getMailsByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final List<Mail> mails = new ArrayList<Mail>();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            for (final NodeMail nMail : NodeMailDAO.getInstance()
                    .findByPropertyValue(group, property, value)) {
                mails.add(BaseMailModule.getProperties(auth.getName(), nMail));
            }
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getMailsByPropertyValue: {}", mails);
        return mails;
    }

    @Override
    public List<QueryResult> findSimpleQuery(final String token,
            final String statement) throws RepositoryException,
            DatabaseException {
        log.debug("findSimpleQuery({}, {})", token, statement);
        final List<QueryResult> ret = findSimpleQueryPaginated(token,
                statement, 0, Config.MAX_SEARCH_RESULTS).getResults();
        log.debug("findSimpleQuery: {}", ret);
        return ret;
    }

    @Override
    public ResultSet findSimpleQueryPaginated(final String token,
            String statement, final int offset, final int limit)
            throws RepositoryException, DatabaseException {
        log.debug("findSimpleQueryPaginated({}, {}, {}, {})", new Object[] {
                token, statement, offset, limit });
        final List<QueryResult> results = new ArrayList<QueryResult>();
        final ResultSet rs = new ResultSet();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            if (statement != null && !statement.equals("")) {
                // Only search in Taxonomy
                statement = statement.concat(" AND context:okm_root");

                final NodeResultSet nrs = SearchDAO.getInstance()
                        .findBySimpleQuery(statement, offset, limit);
                rs.setTotal(nrs.getTotal());

                for (final NodeQueryResult nqr : nrs.getResults()) {
                    final QueryResult qr = new QueryResult();
                    qr.setExcerpt(nqr.getExcerpt());
                    qr.setScore((long) (100 * nqr.getScore()));

                    if (nqr.getDocument() != null) {
                        qr.setDocument(BaseDocumentModule.getProperties(
                                auth.getName(), nqr.getDocument()));
                    } else if (nqr.getFolder() != null) {
                        qr.setFolder(BaseFolderModule.getProperties(
                                auth.getName(), nqr.getFolder()));
                    } else if (nqr.getMail() != null) {
                        qr.setMail(BaseMailModule.getProperties(auth.getName(),
                                nqr.getMail()));
                    } else if (nqr.getAttachment() != null) {
                        qr.setAttachment(BaseDocumentModule.getProperties(
                                auth.getName(), nqr.getAttachment()));
                    }

                    results.add(qr);
                }

                rs.setResults(results);
            }

            // Activity log
            UserActivity.log(auth.getName(), "FIND_SIMPLE_QUERY_PAGINATED",
                    null, null, offset + ", " + limit + ", " + statement);
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final ParseException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findSimpleQueryPaginated: {}", rs);
        return rs;
    }

    @Override
    public ResultSet findMoreLikeThis(final String token, final String uuid,
            final int maxResults) throws RepositoryException, DatabaseException {
        log.debug("findMoreLikeThis({}, {}, {})", new Object[] { token, uuid,
                maxResults });
        final List<QueryResult> results = new ArrayList<QueryResult>();
        final ResultSet rs = new ResultSet();
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            final NodeResultSet nrs = SearchDAO.getInstance().moreLikeThis(
                    uuid, maxResults);
            rs.setTotal(nrs.getTotal());

            for (final NodeQueryResult nqr : nrs.getResults()) {
                final QueryResult qr = new QueryResult();
                qr.setExcerpt(nqr.getExcerpt());
                qr.setScore((long) (100 * nqr.getScore()));

                if (nqr.getDocument() != null) {
                    qr.setDocument(BaseDocumentModule.getProperties(
                            auth.getName(), nqr.getDocument()));
                } else if (nqr.getFolder() != null) {
                    qr.setFolder(BaseFolderModule.getProperties(auth.getName(),
                            nqr.getFolder()));
                } else if (nqr.getMail() != null) {
                    qr.setMail(BaseMailModule.getProperties(auth.getName(),
                            nqr.getMail()));
                }

                results.add(qr);
            }

            rs.setResults(results);

            // Activity log
            UserActivity.log(auth.getName(), "FIND_MORE_LIKE_THIS", uuid, null,
                    Integer.toString(maxResults));
        } catch (final PathNotFoundException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final DatabaseException e) {
            throw e;
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findMoreLikeThis: {}", rs);
        return rs;
    }
}
