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

package com.openkm.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.bean.QueryResult;
import com.openkm.bean.ResultSet;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.QueryParams;
import com.openkm.module.ModuleManager;
import com.openkm.module.SearchModule;

/**
 * @author pavila
 */
public class OKMSearch implements SearchModule {
    private static Logger log = LoggerFactory.getLogger(OKMSearch.class);

    private static OKMSearch instance = new OKMSearch();

    private OKMSearch() {
    }

    public static OKMSearch getInstance() {
        return instance;
    }

    @Override
    public List<QueryResult> findByContent(final String token,
            final String words) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("findByContent({}, {})", token, words);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findByContent(token, words);
        log.debug("findByContent: {}", col);
        return col;
    }

    @Override
    public List<QueryResult> findByName(final String token, final String words)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("findByName({}, {})", token, words);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findByName(token, words);
        log.debug("findByName: {}", col);
        return col;
    }

    @Override
    public List<QueryResult> findByKeywords(final String token,
            final Set<String> words) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("findByKeywords({}, {})", token, words);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findByKeywords(token, words);
        log.debug("findByKeywords: {}", col);
        return col;
    }

    @Override
    public List<QueryResult> find(final String token, final QueryParams params)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("find({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.find(token, params);
        log.debug("find: {}", col);
        return col;
    }

    @Override
    public ResultSet findPaginated(final String token,
            final QueryParams params, final int offset, final int limit)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("findPaginated({}, {}, {}, {})", new Object[] { token,
                params, offset, limit });
        final SearchModule sm = ModuleManager.getSearchModule();
        final ResultSet rs = sm.findPaginated(token, params, offset, limit);
        log.debug("findPaginated: {}", rs);
        return rs;
    }

    @Override
    public long saveSearch(final String token, final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("saveSearch({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        final long id = sm.saveSearch(token, params);
        log.debug("saveSearch: {}", id);
        return id;
    }

    @Override
    public void updateSearch(final String token, final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("updateSearch({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        sm.saveSearch(token, params);
        log.debug("updateSearch: void");
    }

    @Override
    public QueryParams getSearch(final String token, final int qpId)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getSearch({}, {})", token, qpId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final QueryParams qp = sm.getSearch(token, qpId);
        log.debug("getSearch: {}", qp);
        return qp;
    }

    @Override
    public List<QueryParams> getAllSearchs(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getAllSearchs({})", token);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryParams> col = sm.getAllSearchs(token);
        log.debug("getAllSearchs: {}", col);
        return col;
    }

    @Override
    public void deleteSearch(final String token, final long qpId)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("deleteSearch({}, {})", token, qpId);
        final SearchModule sm = ModuleManager.getSearchModule();
        sm.deleteSearch(token, qpId);
        log.debug("deleteSearch: void");
    }

    @Override
    public Map<String, Integer> getKeywordMap(final String token,
            final List<String> filter) throws RepositoryException,
            DatabaseException {
        log.debug("getKeywordMap({}, {})", token, filter);
        final SearchModule sm = ModuleManager.getSearchModule();
        final Map<String, Integer> kmap = sm.getKeywordMap(token, filter);
        log.debug("getKeywordMap: {}", kmap);
        return kmap;
    }

    @Override
    public List<Document> getCategorizedDocuments(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedDocuments({}, {})", token, categoryId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Document> col = sm
                .getCategorizedDocuments(token, categoryId);
        log.debug("getCategorizedDocuments: {}", col);
        return col;
    }

    @Override
    public List<Folder> getCategorizedFolders(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedFolders({}, {})", token, categoryId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Folder> col = sm.getCategorizedFolders(token, categoryId);
        log.debug("getCategorizedFolders: {}", col);
        return col;
    }

    @Override
    public List<Mail> getCategorizedMails(final String token,
            final String categoryId) throws RepositoryException,
            DatabaseException {
        log.debug("getCategorizedMails({}, {})", token, categoryId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Mail> col = sm.getCategorizedMails(token, categoryId);
        log.debug("getCategorizedMails: {}", col);
        return col;
    }

    @Override
    public List<Document> getDocumentsByKeyword(final String token,
            final String keyword) throws RepositoryException, DatabaseException {
        log.debug("getDocumentsByKeyword({}, {})", token, keyword);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Document> col = sm.getDocumentsByKeyword(token, keyword);
        log.debug("getDocumentsByKeyword: {}", col);
        return col;
    }

    @Override
    public List<Folder> getFoldersByKeyword(final String token,
            final String keyword) throws RepositoryException, DatabaseException {
        log.debug("getFoldersByKeyword({}, {})", token, keyword);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Folder> col = sm.getFoldersByKeyword(token, keyword);
        log.debug("getFoldersByKeyword: {}", col);
        return col;
    }

    @Override
    public List<Mail> getMailsByKeyword(final String token, final String keyword)
            throws RepositoryException, DatabaseException {
        log.debug("getMailsByKeyword({}, {})", token, keyword);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Mail> col = sm.getMailsByKeyword(token, keyword);
        log.debug("getMailsByKeyword: {}", col);
        return col;
    }

    @Override
    public List<Document> getDocumentsByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getDocumentsByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Document> col = sm.getDocumentsByPropertyValue(token, group,
                property, value);
        log.debug("getDocumentsByPropertyValue: {}", col);
        return col;
    }

    @Override
    public List<Folder> getFoldersByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getFoldersByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Folder> col = sm.getFoldersByPropertyValue(token, group,
                property, value);
        log.debug("getFoldersByPropertyValue: {}", col);
        return col;
    }

    @Override
    public List<Mail> getMailsByPropertyValue(final String token,
            final String group, final String property, final String value)
            throws RepositoryException, DatabaseException {
        log.debug("getMailsByPropertyValue({}, {}, {}, {})", new Object[] {
                token, group, property, value });
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Mail> col = sm.getMailsByPropertyValue(token, group,
                property, value);
        log.debug("getMailsByPropertyValue: {}", col);
        return col;
    }

    @Override
    public List<QueryResult> findSimpleQuery(final String token,
            final String statement) throws RepositoryException,
            DatabaseException {
        log.debug("findSimpleQuery({}, {})", token, statement);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findSimpleQuery(token, statement);
        log.debug("findSimpleQuery: {}", col);
        return col;
    }

    @Override
    public ResultSet findSimpleQueryPaginated(final String token,
            final String statement, final int offset, final int limit)
            throws RepositoryException, DatabaseException {
        log.debug("findSimpleQueryPaginated({})", token);
        final SearchModule sm = ModuleManager.getSearchModule();
        final ResultSet ret = sm.findSimpleQueryPaginated(token, statement,
                offset, limit);
        log.debug("findSimpleQueryPaginated: {}", ret);
        return ret;
    }

    @Override
    public ResultSet findMoreLikeThis(final String token, final String uuid,
            final int maxResults) throws RepositoryException, DatabaseException {
        log.debug("findMoreLikeThis({}, {}, {})", new Object[] { token, uuid,
                maxResults });
        final SearchModule sm = ModuleManager.getSearchModule();
        final ResultSet ret = sm.findMoreLikeThis(token, uuid, maxResults);
        log.debug("findMoreLikeThis: {}", ret);
        return ret;
    }
}
