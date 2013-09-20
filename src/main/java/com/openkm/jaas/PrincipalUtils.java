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

package com.openkm.jaas;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

/**
 * @author pavila
 */
public class PrincipalUtils {
    /**
     * Obtain current authenticated subject
     */
    public static Subject getSubject() throws NamingException {
        final InitialContext ctx = new InitialContext();
        return (Subject) ctx.lookup("java:comp/env/security/subject");
    }

    /**
     * Obtain the logged user.
     */
    public static String getUser() throws NamingException {
        final Subject subject = PrincipalUtils.getSubject();
        String user = null;

        for (final Principal principal2 : subject.getPrincipals()) {
            final Object obj = principal2;

            if (!(obj instanceof java.security.acl.Group)) {
                final java.security.Principal principal = (java.security.Principal) obj;
                user = principal.getName();
            }
        }

        return user;
    }

    /**
     * Obtain the list of user roles.
     */
    public static Set<String> getRoles() throws NamingException {
        final Subject subject = PrincipalUtils.getSubject();
        final Set<String> roles = new HashSet<String>();

        for (final Principal principal : subject.getPrincipals()) {
            final Object obj = principal;

            if (obj instanceof java.security.acl.Group) {
                final java.security.acl.Group group = (java.security.acl.Group) obj;

                for (final Enumeration<? extends java.security.Principal> groups = group
                        .members(); groups.hasMoreElements();) {
                    final java.security.Principal rol = groups.nextElement();
                    roles.add(rol.getName());
                }
            }
        }

        return roles;
    }

    /**
     * Check for role
     */
    public static boolean hasRole(final String role) {
        try {
            final Set<String> roles = getRoles();

            if (roles != null) {
                return roles.contains(role);
            }
        } catch (final NamingException e) {
            // Ignore
        }

        return false;
    }
}
