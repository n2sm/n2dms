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

package com.openkm.api;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.module.AuthModule;
import com.openkm.module.ModuleManager;
import com.openkm.principal.PrincipalAdapterException;

public class OKMAuth implements AuthModule {
    private static Logger log = LoggerFactory.getLogger(OKMAuth.class);

    private static OKMAuth instance = new OKMAuth();

    private OKMAuth() {
    }

    public static OKMAuth getInstance() {
        return instance;
    }

    @Override
    public void login() throws RepositoryException, DatabaseException {
        log.debug("login()");
        final AuthModule am = ModuleManager.getAuthModule();
        am.login();
        log.debug("login: void");
    }

    @Override
    public String login(final String user, final String pass)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("login({}, {})", user, pass);
        final AuthModule am = ModuleManager.getAuthModule();
        final String token = am.login(user, pass);
        log.debug("login: {}", token);
        return token;
    }

    @Override
    public void logout(final String token) throws RepositoryException,
            DatabaseException {
        log.debug("logout({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        am.logout(token);
        log.debug("logout: void");
    }

    @Override
    public void grantUser(final String token, final String nodePath,
            final String user, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantUser({}, {}, {}, {})", new Object[] { token, nodePath,
                user, permissions });
        final AuthModule am = ModuleManager.getAuthModule();
        am.grantUser(token, nodePath, user, permissions, recursive);
        log.debug("grantUser: void");
    }

    @Override
    public void revokeUser(final String token, final String nodePath,
            final String user, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeUser({}, {}, {}, {})", new Object[] { token, nodePath,
                user, permissions });
        final AuthModule am = ModuleManager.getAuthModule();
        am.revokeUser(token, nodePath, user, permissions, recursive);
        log.debug("revokeUser: void");
    }

    @Override
    public Map<String, Integer> getGrantedUsers(final String token,
            final String nodePath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("getGrantedUsers({}, {})", token, nodePath);
        final AuthModule am = ModuleManager.getAuthModule();
        final Map<String, Integer> grantedUsers = am.getGrantedUsers(token,
                nodePath);
        log.debug("getGrantedUsers: {}", grantedUsers);
        return grantedUsers;
    }

    @Override
    public void grantRole(final String token, final String nodePath,
            final String role, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantRole({}, {}, {}, {})", new Object[] { token, nodePath,
                role, permissions });
        final AuthModule am = ModuleManager.getAuthModule();
        am.grantRole(token, nodePath, role, permissions, recursive);
        log.debug("grantRole: void");
    }

    @Override
    public void revokeRole(final String token, final String nodePath,
            final String user, final int permissions, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeRole({}, {}, {}, {})", new Object[] { token, nodePath,
                user, permissions });
        final AuthModule am = ModuleManager.getAuthModule();
        am.revokeRole(token, nodePath, user, permissions, recursive);
        log.debug("revokeRole: void");
    }

    @Override
    public Map<String, Integer> getGrantedRoles(final String token,
            final String nodePath) throws PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("getGrantedRoles({})", nodePath);
        final AuthModule am = ModuleManager.getAuthModule();
        final Map<String, Integer> grantedRoles = am.getGrantedRoles(token,
                nodePath);
        log.debug("getGrantedRoles: {}", grantedRoles);
        return grantedRoles;
    }

    @Override
    public List<String> getUsers(final String token)
            throws PrincipalAdapterException {
        log.debug("getUsers({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> users = am.getUsers(token);
        log.debug("getUsers: {}", users);
        return users;
    }

    @Override
    public List<String> getRoles(final String token)
            throws PrincipalAdapterException {
        log.debug("getRoles({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> roles = am.getRoles(token);
        log.debug("getRoles: {}", roles);
        return roles;
    }

    @Override
    public List<String> getUsersByRole(final String token, final String role)
            throws PrincipalAdapterException {
        log.debug("getUsersByRole({}, {})", token, role);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> users = am.getUsersByRole(token, role);
        log.debug("getUsersByRole: {}", users);
        return users;
    }

    @Override
    public List<String> getRolesByUser(final String token, final String user)
            throws PrincipalAdapterException {
        log.debug("getRolesByUser({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> users = am.getRolesByUser(token, user);
        log.debug("getRolesByUser: {}", users);
        return users;
    }

    @Override
    public String getMail(final String token, final String user)
            throws PrincipalAdapterException {
        log.debug("getMail({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final String mail = am.getMail(token, user);
        log.debug("getMail: {}", mail);
        return mail;
    }

    @Override
    public String getName(final String token, final String user)
            throws PrincipalAdapterException {
        log.debug("getName({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final String name = am.getName(token, user);
        log.debug("getName: {}", name);
        return name;
    }

    @Override
    public void changeSecurity(final String token, final String nodePath,
            final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles, final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("changeSecurity({}, {}, {}, {}, {}, {})", new Object[] {
                token, nodePath, grantUsers, revokeUsers, grantRoles,
                revokeRoles, recursive });
        final AuthModule am = ModuleManager.getAuthModule();
        am.changeSecurity(token, nodePath, grantUsers, revokeUsers, grantRoles,
                revokeRoles, recursive);
        log.debug("changeSecurity: void");
    }
}
