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

package com.openkm.servlet.frontend;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.DatabaseMetadataDAO;
import com.openkm.dao.bean.DatabaseMetadataValue;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMDatabaseMetadataService;
import com.openkm.util.DatabaseMetadataUtils;

/**
 * DatabaseMetadataServlet
 * 
 * @author jllort
 */
public class DatabaseMetadataServlet extends OKMRemoteServiceServlet implements
        OKMDatabaseMetadataService {
    private static Logger log = LoggerFactory
            .getLogger(DatabaseMetadataServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public List<Map<String, String>> executeValueQuery(final String table,
            final String filter, final String order) throws OKMException {
        log.debug("executeValueQuery({}, {}, {})", new Object[] { table,
                filter, order });
        updateSessionManager();
        final List<Map<String, String>> metadataValues = new ArrayList<Map<String, String>>();

        try {
            for (final DatabaseMetadataValue dmv : DatabaseMetadataDAO
                    .executeValueQuery(DatabaseMetadataUtils.buildQuery(table,
                            filter, order))) {
                metadataValues.add(DatabaseMetadataUtils
                        .getDatabaseMetadataValueMap(dmv));
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_IllegalAccess), e.getMessage());
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_InvocationTarget), e.getMessage());
        } catch (final NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_NoSuchMethod), e.getMessage());
        }

        log.debug("executeValueQuery: {}", metadataValues);
        return metadataValues;
    }

    @Override
    public void updateValue(final Map<String, String> map) throws OKMException {
        log.debug("updateValue({})", map);
        updateSessionManager();

        try {
            DatabaseMetadataDAO.updateValue(DatabaseMetadataUtils
                    .getDatabaseMetadataValueByMap(map));
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_IllegalAccess), e.getMessage());
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_InvocationTarget), e.getMessage());
        }
    }

    @Override
    public Double createValue(final Map<String, String> map)
            throws OKMException {
        log.debug("createValue({})", map);
        updateSessionManager();

        try {
            return new Double(
                    DatabaseMetadataDAO.createValue(DatabaseMetadataUtils
                            .getDatabaseMetadataValueByMap(map)));
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_IllegalAccess), e.getMessage());
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_InvocationTarget), e.getMessage());
        }
    }

    @Override
    public void deleteValue(final Map<String, String> map) throws OKMException {
        log.debug("deleteValue({})", map);
        updateSessionManager();

        try {
            DatabaseMetadataDAO.deleteValue(DatabaseMetadataUtils
                    .getDatabaseMetadataValueByMap(map).getId());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_IllegalAccess), e.getMessage());
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_InvocationTarget), e.getMessage());
        }
    }

    @Override
    public List<List<Map<String, String>>> executeMultiValueQuery(
            final List<String> tables, final String query) throws OKMException {
        log.debug("executeMultiValueQuery({})", query);
        updateSessionManager();
        final List<List<Map<String, String>>> ret = new ArrayList<List<Map<String, String>>>();

        try {
            for (final DatabaseMetadataValue[] dmv : DatabaseMetadataDAO
                    .executeMultiValueQuery(DatabaseMetadataUtils
                            .replaceVirtual(tables, query))) {
                final List<Map<String, String>> dmvRow = new ArrayList<Map<String, String>>();

                for (final DatabaseMetadataValue element : dmv) {
                    dmvRow.add(DatabaseMetadataUtils
                            .getDatabaseMetadataValueMap(element));
                }

                ret.add(dmvRow);
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_IllegalAccess), e.getMessage());
        } catch (final InvocationTargetException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_InvocationTarget), e.getMessage());
        } catch (final NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_NoSuchMethod), e.getMessage());
        }
        return ret;
    }

    @Override
    public Double getNextSequenceValue(final String table, final String column)
            throws OKMException {
        log.debug("getNextSequenceValue({},{})", table, column);
        updateSessionManager();

        try {
            return new Double(DatabaseMetadataDAO.getNextSequenceValue(table,
                    column));
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDatabaseMetadataService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        }
    }
}