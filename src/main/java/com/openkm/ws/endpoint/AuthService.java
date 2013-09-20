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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.module.AuthModule;
import com.openkm.module.ModuleManager;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.ws.util.IntegerPair;

@WebService(name = "OKMAuth", serviceName = "OKMAuth", targetNamespace = "http://ws.openkm.com")
public class AuthService {
    private static Logger log = LoggerFactory.getLogger(AuthService.class);

    @WebMethod
    public String login(@WebParam(name = "user") final String user,
            @WebParam(name = "password") final String password)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("login({}, {})", user, password);
        final AuthModule am = ModuleManager.getAuthModule();
        final String token = am.login(user, password);
        log.debug("login: {}", token);
        return token;
    }

    @WebMethod
    public void logout(@WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException {
        log.debug("logout({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        am.logout(token);
        log.debug("logout: void");
    }

    @WebMethod
    public IntegerPair[] getGrantedRoles(
            @WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("getGrantedRoles({}, {})", token, nodePath);
        final AuthModule am = ModuleManager.getAuthModule();
        final Map<String, Integer> hm = am.getGrantedRoles(token, nodePath);
        final Set<String> keys = hm.keySet();
        final IntegerPair[] result = new IntegerPair[keys.size()];
        int i = 0;

        // Marshall HashMap
        for (final String key : keys) {
            final IntegerPair p = new IntegerPair();
            p.setKey(key);
            p.setValue(hm.get(key));
            result[i++] = p;
        }

        log.debug("getGrantedRoles: {}", result);
        return result;
    }

    @WebMethod
    public IntegerPair[] getGrantedUsers(
            @WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("getGrantedUsers({}, {})", token, nodePath);
        final AuthModule am = ModuleManager.getAuthModule();
        final Map<String, Integer> hm = am.getGrantedUsers(token, nodePath);
        final Set<String> keys = hm.keySet();
        final IntegerPair[] result = new IntegerPair[keys.size()];
        int i = 0;

        // Marshall HashMap
        for (final String key : keys) {
            final IntegerPair p = new IntegerPair();
            p.setKey(key);
            p.setValue(hm.get(key));
            result[i++] = p;
        }

        log.debug("getGrantedUsers: {}", result);
        return result;
    }

    @WebMethod
    public String[] getRoles(@WebParam(name = "token") final String token)
            throws PrincipalAdapterException {
        log.debug("getRoles({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> col = am.getRoles(token);
        final String[] result = col.toArray(new String[col.size()]);
        log.debug("getRoles: {}", result);
        return result;
    }

    @WebMethod
    public String[] getUsers(@WebParam(name = "token") final String token)
            throws PrincipalAdapterException {
        log.debug("getUsers({})", token);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> col = am.getUsers(token);
        final String[] result = col.toArray(new String[col.size()]);
        log.debug("getUsers: {]", result);
        return result;
    }

    @WebMethod
    public void grantRole(@WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath,
            @WebParam(name = "role") final String role,
            @WebParam(name = "permissions") final int permissions,
            @WebParam(name = "recursive") final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantRole({}, {}, {}, {}, {})", new Object[] { token,
                nodePath, role, permissions, recursive });
        final AuthModule am = ModuleManager.getAuthModule();
        am.grantRole(token, nodePath, role, permissions, recursive);
        log.debug("grantRole: void");
    }

    @WebMethod
    public void grantUser(@WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath,
            @WebParam(name = "user") final String user,
            @WebParam(name = "permissions") final int permissions,
            @WebParam(name = "recursive") final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("grantUser({}, {}, {}, {}, {})", new Object[] { token,
                nodePath, user, permissions, recursive });
        final AuthModule am = ModuleManager.getAuthModule();
        am.grantUser(token, nodePath, user, permissions, recursive);
        log.debug("grantUser: void");
    }

    @WebMethod
    public void revokeRole(@WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath,
            @WebParam(name = "user") final String user,
            @WebParam(name = "permissions") final int permissions,
            @WebParam(name = "recursive") final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeRole({}, {}, {}, {}, {})", new Object[] { token,
                nodePath, user, permissions, recursive });
        final AuthModule am = ModuleManager.getAuthModule();
        am.revokeRole(token, nodePath, user, permissions, recursive);
        log.debug("revokeRole: void");
    }

    @WebMethod
    public void revokeUser(@WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath,
            @WebParam(name = "user") final String user,
            @WebParam(name = "permissions") final int permissions,
            @WebParam(name = "recursive") final boolean recursive)
            throws PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("revokeUser({}, {}, {}, {}, {})", new Object[] { token,
                nodePath, user, permissions, recursive });
        final AuthModule am = ModuleManager.getAuthModule();
        am.revokeUser(token, nodePath, user, permissions, recursive);
        log.debug("revokeUser: void");
    }

    @WebMethod
    public String[] getUsersByRole(
            @WebParam(name = "token") final String token,
            @WebParam(name = "role") final String role)
            throws PrincipalAdapterException {
        log.debug("getUsersByRole({}, {})", token, role);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> col = am.getUsersByRole(token, role);
        final String[] result = col.toArray(new String[col.size()]);
        log.debug("getUsersByRole: {}", result);
        return result;
    }

    @WebMethod
    public String[] getRolesByUser(
            @WebParam(name = "token") final String token,
            @WebParam(name = "user") final String user)
            throws PrincipalAdapterException {
        log.debug("getRolesByUser({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final List<String> col = am.getRolesByUser(token, user);
        final String[] result = col.toArray(new String[col.size()]);
        log.debug("getRolesByUser: {}", result);
        return result;
    }

    @WebMethod
    public String getMail(@WebParam(name = "token") final String token,
            @WebParam(name = "user") final String user)
            throws PrincipalAdapterException {
        log.debug("getMail({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final String ret = am.getMail(token, user);
        log.debug("getMail: {}", ret);
        return ret;
    }

    @WebMethod
    public String getName(@WebParam(name = "token") final String token,
            @WebParam(name = "user") final String user)
            throws PrincipalAdapterException {
        log.debug("getName({}, {})", token, user);
        final AuthModule am = ModuleManager.getAuthModule();
        final String ret = am.getName(token, user);
        log.debug("getName: {}", ret);
        return ret;
    }
}
