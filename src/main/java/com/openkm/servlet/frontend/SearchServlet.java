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

package com.openkm.servlet.frontend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMSearch;
import com.openkm.bean.QueryResult;
import com.openkm.bean.ResultSet;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.QueryParamsDAO;
import com.openkm.dao.bean.QueryParams;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTKeyword;
import com.openkm.frontend.client.bean.GWTQueryParams;
import com.openkm.frontend.client.bean.GWTQueryResult;
import com.openkm.frontend.client.bean.GWTResultSet;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMSearchService;
import com.openkm.servlet.frontend.util.KeywordComparator;
import com.openkm.servlet.frontend.util.QueryParamsComparator;
import com.openkm.util.GWTUtil;

/**
 * SearchServlet
 * 
 * @author jllort
 */
public class SearchServlet extends OKMRemoteServiceServlet implements
        OKMSearchService {
    private static Logger log = LoggerFactory.getLogger(SearchServlet.class);

    private static final long serialVersionUID = 8673521252684830906L;

    @Override
    public List<GWTQueryParams> getAllSearchs() throws OKMException {
        log.debug("getAllSearchs()");
        final List<GWTQueryParams> resultList = new ArrayList<GWTQueryParams>();
        updateSessionManager();

        try {
            for (final QueryParams params : OKMSearch.getInstance()
                    .getAllSearchs(null)) {
                resultList.add(GWTUtil.copy(params));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        Collections.sort(resultList,
                QueryParamsComparator.getInstance(getLanguage()));

        log.debug("getAllSearchs: {}", resultList);
        return resultList;
    }

    @Override
    public Long saveSearch(final GWTQueryParams params, final String type)
            throws OKMException {
        log.debug("saveSearch({}, {}, {})", new Object[] { params, type });
        updateSessionManager();

        try {
            return OKMSearch.getInstance().saveSearch(null,
                    GWTUtil.copy(params));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }
    }

    @Override
    public void deleteSearch(final long id) throws OKMException {
        log.debug("deleteSearch()");
        updateSessionManager();

        try {
            OKMSearch.getInstance().deleteSearch(null, id);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("deleteSearch: void");
    }

    @Override
    public GWTResultSet findPaginated(final GWTQueryParams params,
            final int offset, final int limit) throws OKMException {
        log.debug("findPaginated({}, {}, {})", new Object[] { params, offset,
                limit });
        final List<GWTQueryResult> resultList = new ArrayList<GWTQueryResult>();
        final GWTResultSet gwtResultSet = new GWTResultSet();
        QueryParams queryParams = new QueryParams();
        ResultSet results;
        updateSessionManager();

        try {
            queryParams = GWTUtil.copy(params);
            results = OKMSearch.getInstance().findPaginated(null, queryParams,
                    offset, limit);

            for (final QueryResult queryResult : results.getResults()) {
                final GWTQueryResult gwtQueryResult = GWTUtil.copy(queryResult);
                resultList.add(gwtQueryResult);
            }

            gwtResultSet.setTotal(results.getTotal());
            gwtResultSet.setResults(resultList);
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService, ErrorCode.CAUSE_Parse),
                    e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("findPaginated: {}", resultList);
        return gwtResultSet;
    }

    @Override
    public GWTResultSet find(final GWTQueryParams params) throws OKMException {
        log.debug("find({})", params);
        final List<GWTQueryResult> resultList = new ArrayList<GWTQueryResult>();
        final GWTResultSet gwtResultSet = new GWTResultSet();
        QueryParams queryParams = new QueryParams();
        Collection<QueryResult> results;
        updateSessionManager();

        try {
            queryParams = GWTUtil.copy(params);
            results = OKMSearch.getInstance().find(null, queryParams);

            for (final QueryResult queryResult : results) {
                final GWTQueryResult gwtQueryResult = GWTUtil.copy(queryResult);
                resultList.add(gwtQueryResult);
            }

            gwtResultSet.setTotal(results.size());
            gwtResultSet.setResults(resultList);
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService, ErrorCode.CAUSE_Parse),
                    e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("find: {}", resultList);
        return gwtResultSet;
    }

    @Override
    public List<GWTKeyword> getKeywordMap(final List<String> filter)
            throws OKMException {
        log.debug("getKeywordMap()");
        final List<GWTKeyword> selectedTop10 = new ArrayList<GWTKeyword>();
        final List<GWTKeyword> keyList = new ArrayList<GWTKeyword>();
        final int maxValues[] = new int[10];
        int countTop10 = 0;
        updateSessionManager();

        try {
            final Map<String, Integer> keyMap = OKMSearch.getInstance()
                    .getKeywordMap(null, filter);

            for (final String key : keyMap.keySet()) {
                final GWTKeyword keyword = new GWTKeyword();
                keyword.setKeyword(key);
                keyword.setFrequency(keyMap.get(key).intValue());
                keyword.setTop10(false);

                // Identifiying the top 10 max values
                if (countTop10 < 10) {
                    maxValues[countTop10] = keyword.getFrequency();
                    selectedTop10.add(keyword);
                    countTop10++;
                } else {
                    Arrays.sort(maxValues); // Minimal value is maxValues[0] ( ordering is incremental )

                    if (maxValues[0] < keyword.getFrequency()) {
                        boolean found = false;
                        int index = 0;

                        while (!found && index < selectedTop10.size()) {
                            if (selectedTop10.get(index).getFrequency() == maxValues[0]) {
                                found = true;
                                selectedTop10.remove(index);
                                maxValues[0] = keyword.getFrequency(); // Adding value to max values list
                                selectedTop10.add(keyword); // Adding object to top selected
                            }

                            index++;
                        }
                    }
                }

                keyList.add(keyword);
            }

            // Marks selectedTop10 as selected
            for (final GWTKeyword gwtKeyword : selectedTop10) {
                gwtKeyword.setTop10(true);
            }

            Collections.sort(keyList,
                    KeywordComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getKeywordMap: {}", keyList);
        return keyList;
    }

    @Override
    public void share(final long qpId) throws OKMException {
        log.debug("share({})", qpId);
        updateSessionManager();

        try {
            QueryParamsDAO.share(qpId, getThreadLocalRequest().getRemoteUser());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        }

        log.debug("share: void");
    }

    @Override
    public void unshare(final long qpId) throws OKMException {
        log.debug("share({})", qpId);
        updateSessionManager();

        try {
            QueryParamsDAO.unshare(qpId, getThreadLocalRequest()
                    .getRemoteUser());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        }

        log.debug("share: void");
    }

    @Override
    public GWTResultSet findSimpleQueryPaginated(final String statement,
            final int offset, final int limit) throws OKMException {
        log.debug("findSimpleQueryPaginated({})", statement);
        final List<GWTQueryResult> resultList = new ArrayList<GWTQueryResult>();
        final GWTResultSet gwtResultSet = new GWTResultSet();
        ResultSet results;
        updateSessionManager();

        try {
            results = OKMSearch.getInstance().findSimpleQueryPaginated(null,
                    statement, offset, limit);
            for (final QueryResult queryResult : results.getResults()) {
                final GWTQueryResult gwtQueryResult = GWTUtil.copy(queryResult);
                resultList.add(gwtQueryResult);
            }

            gwtResultSet.setTotal(results.getTotal());
            gwtResultSet.setResults(resultList);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMSearchService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMSearchService,
                            ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("findSimpleQueryPaginated: {}", resultList);
        return gwtResultSet;
    }
}
