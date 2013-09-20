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

package com.openkm.util.tags;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class ConstantsMapTag extends SimpleTagSupport {
    private String className;

    private String varName;

    //private String scopeName;

    public void setClassName(final String value) {
        className = value;
    }

    public void setVar(final String value) {
        varName = value;
    }

    //public void setScope( String value ) { this.scopeName = value; }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void doTag() throws JspException {
        try {
            final Map constantsMap = new HashMap();
            final Class declaringClass = Class.forName(className);
            final Field[] fields = declaringClass.getFields();

            for (final Field field : fields) {
                if (Modifier.isPublic(field.getModifiers())
                        && Modifier.isStatic(field.getModifiers()) /*&&
                                                                   Modifier.isFinal( fields[n].getModifiers() )*/) {
                    constantsMap.put(field.getName(), field.get(null));
                }
            }

            //ScopedContext scopedContext = (this.scopeName == null) ?
            //    ScopedContext.PAGE : ScopedContext.getInstance( this.scopeName );
            //getJspContext().setAttribute(this.varName, constantsMap, scopedContext.getValue());
            getJspContext().setAttribute(varName, constantsMap);
        } catch (final Exception e) {
            throw new JspException("Exception setting constants map for "
                    + className, e);
        }
    }
}
