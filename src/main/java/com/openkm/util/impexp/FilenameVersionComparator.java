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

package com.openkm.util.impexp;

import java.io.File;
import java.util.Comparator;

public class FilenameVersionComparator implements Comparator<File> {
    private static final Comparator<File> INSTANCE = new FilenameVersionComparator();

    public static Comparator<File> getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(final File f0, final File f1) {
        final String name0 = f0.getName();
        final int idx0 = name0.lastIndexOf('#', name0.length() - 2);
        final String ver0 = name0.substring(idx0 + 2, name0.length() - 1);

        final String name1 = f1.getName();
        final int idx1 = name1.lastIndexOf('#', name1.length() - 2);
        final String ver1 = name1.substring(idx1 + 2, name1.length() - 1);

        return compare(ver0.split("\\."), ver1.split("\\."), 0);
    }

    private int compare(final String[] ary1, final String[] ary2, int index) {
        // if arrays do not have equal size then and comparison reached the upper bound of one of them
        // then the longer array is considered the bigger ( --> 2.2.0 is bigger then 2.2)
        if (ary1.length <= index || ary2.length <= index) {
            return ary1.length - ary2.length;
        }
        final int result = Integer.parseInt(ary1[index])
                - Integer.parseInt(ary2[index]);
        return result == 0 ? compare(ary1, ary2, ++index) : result;
    }
}
