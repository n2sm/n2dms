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

package com.openkm.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.DatabaseMetadataDAO;
import com.openkm.dao.bean.DatabaseMetadataType;
import com.openkm.dao.bean.DatabaseMetadataValue;
import com.openkm.frontend.client.util.metadata.DatabaseMetadataMap;

/**
 * DatabaseMetadataUtils
 * 
 * @author pavila
 */
public class DatabaseMetadataUtils {
    private static Logger log = LoggerFactory
            .getLogger(DatabaseMetadataUtils.class);

    /**
     * Build a query
     */
    public static String buildQuery(final String table, final String filter,
            final String order) throws DatabaseException {
        log.debug("buildQuery({}, {}, {})",
                new Object[] { table, filter, order });
        final StringBuilder sb = new StringBuilder();
        String ret = null;

        sb.append("from DatabaseMetadataValue dmv where dmv.table='" + table
                + "'");

        if (filter != null && filter.length() > 0) {
            sb.append(" and ").append(replaceVirtual(table, filter));
        }

        if (order != null && order.length() > 0) {
            sb.append(" order by ").append(replaceVirtual(table, order));
        }

        ret = sb.toString();
        log.debug("buildQuery: {}", ret);
        return ret;
    }

    /**
     * Build a query
     */
    public static String buildQuery(final String table, final String filter)
            throws DatabaseException {
        log.debug("buildQuery({}, {})", new Object[] { table, filter });
        final StringBuilder sb = new StringBuilder();
        String ret = null;

        sb.append("from DatabaseMetadataValue dmv where dmv.table='" + table
                + "'");

        if (filter != null && filter.length() > 0) {
            sb.append(" and ").append(replaceVirtual(table, filter));
        }

        ret = sb.toString();
        log.debug("buildQuery: {}", ret);
        return ret;
    }

    /**
     * Build a query
     */
    public static String buildUpdate(final String table, final String values,
            final String filter) throws DatabaseException {
        log.debug("buildUpdate({}, {}, {})", new Object[] { table, values,
                filter });
        final StringBuilder sb = new StringBuilder();
        String ret = null;

        sb.append("update DatabaseMetadataValue dmv set");

        if (values != null && values.length() > 0) {
            sb.append(" ").append(replaceVirtual(table, values));
        }

        sb.append(" where dmv.table='" + table + "'");

        if (filter != null && filter.length() > 0) {
            sb.append(" and ").append(replaceVirtual(table, filter));
        }

        ret = sb.toString();
        log.debug("buildUpdate: {}", ret);
        return ret;
    }

    /**
     * Build a query
     */
    public static String buildDelete(final String table, final String filter)
            throws DatabaseException {
        log.debug("buildDelete({}, {})", new Object[] { table, filter });
        final StringBuilder sb = new StringBuilder();
        String ret = null;

        sb.append("delete from DatabaseMetadataValue dmv where dmv.table='"
                + table + "'");

        if (filter != null && filter.length() > 0) {
            sb.append(" and ").append(replaceVirtual(table, filter));
        }

        ret = sb.toString();
        log.debug("buildDelete: {}", ret);
        return ret;
    }

    /**
     * Get virtual column string value
     */
    public static String getString(final DatabaseMetadataValue value,
            final String column) throws DatabaseException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        final List<DatabaseMetadataType> types = DatabaseMetadataDAO
                .findAllTypes(value.getTable());

        for (final DatabaseMetadataType emt : types) {
            if (emt.getVirtualColumn().equals(column)) {
                return BeanUtils.getProperty(value, emt.getRealColumn());
            }
        }

        return null;
    }

    /**
     * Get virtual column date value
     */
    public static Calendar getDate(final DatabaseMetadataValue value,
            final String column) throws DatabaseException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        final List<DatabaseMetadataType> types = DatabaseMetadataDAO
                .findAllTypes(value.getTable());

        for (final DatabaseMetadataType emt : types) {
            if (emt.getVirtualColumn().equals(column)) {
                return ISO8601.parseBasic(BeanUtils.getProperty(value,
                        emt.getRealColumn()));
            }
        }

        return null;
    }

    /**
     * Obtain a Map from a DatabaseMetadataValue.
     */
    public static Map<String, String> getDatabaseMetadataValueMap(
            final DatabaseMetadataValue value) throws DatabaseException,
            IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        final Map<String, String> map = new HashMap<String, String>();
        final List<DatabaseMetadataType> types = DatabaseMetadataDAO
                .findAllTypes(value.getTable());

        for (final DatabaseMetadataType emt : types) {
            if (emt.getVirtualColumn().equals(DatabaseMetadataMap.MV_NAME_ID)
                    || emt.getVirtualColumn().equals(
                            DatabaseMetadataMap.MV_NAME_TABLE)) {
                throw new DatabaseException(
                        "Virtual column name restriction violated "
                                + DatabaseMetadataMap.MV_NAME_ID + " or "
                                + DatabaseMetadataMap.MV_NAME_TABLE);
            }

            map.put(emt.getVirtualColumn(),
                    BeanUtils.getProperty(value, emt.getRealColumn()));
        }

        map.put(DatabaseMetadataMap.MV_NAME_TABLE, value.getTable());
        map.put(DatabaseMetadataMap.MV_NAME_ID, String.valueOf(value.getId()));

        return map;
    }

    /**
     * Obtain a DatabaseMetadataValue from a Map
     */
    public static DatabaseMetadataValue getDatabaseMetadataValueByMap(
            final Map<String, String> map) throws DatabaseException,
            IllegalAccessException, InvocationTargetException {
        final DatabaseMetadataValue dmv = new DatabaseMetadataValue();

        if (!map.isEmpty()
                && map.containsKey(DatabaseMetadataMap.MV_NAME_TABLE)) {
            dmv.setTable(map.get(DatabaseMetadataMap.MV_NAME_TABLE));

            if (map.containsKey(DatabaseMetadataMap.MV_NAME_ID)) {
                dmv.setId(new Double(map.get(DatabaseMetadataMap.MV_NAME_ID))
                        .longValue());
            }

            final List<DatabaseMetadataType> types = DatabaseMetadataDAO
                    .findAllTypes(dmv.getTable());
            for (final DatabaseMetadataType emt : types) {
                if (!emt.getVirtualColumn().equals(
                        DatabaseMetadataMap.MV_NAME_ID)
                        && !emt.getVirtualColumn().equals(
                                DatabaseMetadataMap.MV_NAME_TABLE)) {
                    if (map.keySet().contains(emt.getVirtualColumn())) {
                        BeanUtils.setProperty(dmv, emt.getRealColumn(),
                                map.get(emt.getVirtualColumn()));
                    }
                }
            }
        }

        return dmv;
    }

    /**
     * Replace virtual columns by real ones
     */
    public static String replaceVirtual(final List<String> tables,
            final String query) throws DatabaseException {
        String ret = query;
        for (final String table : tables) {
            ret = replaceVirtual(table, ret);
        }

        return ret;
    }

    /**
     * Replace virtual columns by real ones
     */
    private static String replaceVirtual(final String table, String query)
            throws DatabaseException {
        log.debug("replaceVirtual({}, {})", new Object[] { table, query });
        String ret = "";

        if (query != null && query.length() > 0) {
            final List<DatabaseMetadataType> types = DatabaseMetadataDAO
                    .findAllTypes(table);
            Collections.sort(types, LenComparator.getInstance()); //avoid the case in which one of the virtual columns is a substring of another (ex. id and admin_id)
            for (final DatabaseMetadataType emt : types) {
                final String vcol = "\\$"
                        + emt.getVirtualColumn().toLowerCase();
                query = query.replaceAll(vcol, emt.getRealColumn()
                        .toLowerCase());
            }

            ret = query;
        }

        log.debug("replaceVirtual: {}", ret);
        return ret;
    }

    /**
     * custom comparator for sorting strings by length (in descending order)
     * 
     * @author danilo
     */
    public static class LenComparator implements
            Comparator<DatabaseMetadataType> {
        public static LenComparator getInstance() {
            return new LenComparator();
        }

        @Override
        public int compare(final DatabaseMetadataType s1,
                final DatabaseMetadataType s2) {
            return s2.getVirtualColumn().length()
                    - s1.getVirtualColumn().length();
        }
    }
}
