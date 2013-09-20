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

package com.openkm.ws.endpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.QueryResult;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.QueryParams;
import com.openkm.module.ModuleManager;
import com.openkm.module.SearchModule;
import com.openkm.ws.util.IntegerPair;

@WebService(name = "OKMSearch", serviceName = "OKMSearch", targetNamespace = "http://ws.openkm.com")
public class SearchService {
    private static Logger log = LoggerFactory.getLogger(SearchService.class);

    @WebMethod
    public QueryResult[] findByContent(
            @WebParam(name = "token") final String token,
            @WebParam(name = "content") final String content)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("findByContent({}, {})", token, content);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findByContent(token, content);
        final QueryResult[] result = col.toArray(new QueryResult[col.size()]);
        log.debug("findByContent: {}", result);
        return result;
    }

    @WebMethod
    public QueryResult[] findByName(
            @WebParam(name = "token") final String token,
            @WebParam(name = "name") final String name) throws IOException,
            ParseException, RepositoryException, DatabaseException {
        log.debug("findByName({}, {})", token, name);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.findByName(token, name);
        final QueryResult[] result = col.toArray(new QueryResult[col.size()]);
        log.debug("findByName: {}", result);
        return result;
    }

    @WebMethod
    public QueryResult[] findByKeywords(
            @WebParam(name = "token") final String token,
            @WebParam(name = "keywords") final String[] keywords)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("findByKeywords({}, {})", token, keywords);
        final SearchModule sm = ModuleManager.getSearchModule();
        final Set<String> set = new HashSet<String>(Arrays.asList(keywords));
        final List<QueryResult> col = sm.findByKeywords(token, set);
        final QueryResult[] result = col.toArray(new QueryResult[col.size()]);
        log.debug("findByKeywords: {}", result);
        return result;
    }

    @WebMethod
    public QueryResult[] find(@WebParam(name = "token") final String token,
            @WebParam(name = "params") final QueryParams params)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("find({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryResult> col = sm.find(token, params);
        final QueryResult[] result = col.toArray(new QueryResult[col.size()]);
        log.debug("find: {}", result);
        return result;
    }

    @WebMethod
    public IntegerPair[] getKeywordMap(
            @WebParam(name = "token") final String token,
            @WebParam(name = "filter") final String[] filter)
            throws RepositoryException, DatabaseException {
        log.debug("getKeywordMap({}, {})", token, filter);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<String> alFilter = Arrays.asList(filter);
        final Map<String, Integer> map = sm.getKeywordMap(token, alFilter);
        final Set<String> keys = map.keySet();
        final IntegerPair[] result = new IntegerPair[keys.size()];
        int i = 0;

        // Marshall HashMap
        for (final String key : keys) {
            final IntegerPair p = new IntegerPair();
            p.setKey(key);
            p.setValue(map.get(key));
            result[i++] = p;
        }

        log.debug("getKeywordMap: {}", result);
        return result;
    }

    @WebMethod
    public Document[] getCategorizedDocuments(
            @WebParam(name = "token") final String token,
            @WebParam(name = "categoryId") final String categoryId)
            throws RepositoryException, DatabaseException {
        log.debug("getCategorizedDocuments({}, {})", token, categoryId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<Document> col = sm
                .getCategorizedDocuments(token, categoryId);
        final Document[] result = col.toArray(new Document[col.size()]);
        log.debug("getCategorizedDocuments: {}", result);
        return result;
    }

    @WebMethod
    public long saveSearch(@WebParam(name = "token") final String token,
            @WebParam(name = "params") final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("saveSearch({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        final long id = sm.saveSearch(token, params);
        log.debug("saveSearch: {}", id);
        return id;
    }

    @WebMethod
    public void updateSearch(@WebParam(name = "token") final String token,
            @WebParam(name = "params") final QueryParams params)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("updateSearch({}, {})", token, params);
        final SearchModule sm = ModuleManager.getSearchModule();
        sm.saveSearch(token, params);
        log.debug("updateSearch: void");
    }

    @WebMethod
    public QueryParams getSearch(@WebParam(name = "token") final String token,
            @WebParam(name = "qpId") final int qpId)
            throws PathNotFoundException, RepositoryException,
            DatabaseException {
        log.debug("getSearch({}, {})", token, qpId);
        final SearchModule sm = ModuleManager.getSearchModule();
        final QueryParams qp = sm.getSearch(token, qpId);
        log.debug("getSearch: {}", qp);
        return qp;
    }

    @WebMethod
    public QueryParams[] getAllSearchs(
            @WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getAllSearchs({})", token);
        final SearchModule sm = ModuleManager.getSearchModule();
        final List<QueryParams> col = sm.getAllSearchs(token);
        final QueryParams[] result = col.toArray(new QueryParams[col.size()]);
        log.debug("getAllSearchs: {}", col);
        return result;
    }

    @WebMethod
    public void deleteSearch(@WebParam(name = "token") final String token,
            @WebParam(name = "qpId") final int qpId)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("deleteSearch({}, {})", token, qpId);
        final SearchModule sm = ModuleManager.getSearchModule();
        sm.deleteSearch(token, qpId);
        log.debug("deleteSearch: void");
    }
}
