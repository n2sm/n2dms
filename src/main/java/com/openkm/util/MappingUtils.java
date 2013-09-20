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

package com.openkm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;

public class MappingUtils {
    private static final Mapper mapper = DozerBeanMapperSingletonWrapper
            .getInstance();

    public static Mapper getMapper() {
        return mapper;
    }

    /**
     * Initialize collection
     */
    public static <E> List<E> map(final List<E> input) {
        final List<E> ret = new ArrayList<E>();

        for (final E tmp : input) {
            ret.add(tmp);
        }

        return ret;
    }

    /**
     * Initialize set
     */
    public static <E> Set<E> map(final Set<E> input) {
        final Set<E> ret = new HashSet<E>();

        for (final E tmp : input) {
            ret.add(tmp);
        }

        return ret;
    }

    /**
     * Initialize map
     */
    public static <K, V> Map<K, V> map(final Map<K, V> input) {
        final Map<K, V> ret = new HashMap<K, V>();

        for (final Entry<K, V> tmp : input.entrySet()) {
            ret.put(tmp.getKey(), tmp.getValue());
        }

        return ret;
    }
}
