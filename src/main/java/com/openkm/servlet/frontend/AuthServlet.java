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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMAuth;
import com.openkm.bean.Permission;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTGrantedUser;
import com.openkm.frontend.client.bean.GWTUser;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMAuthService;
import com.openkm.frontend.client.util.GWTGrantedUserComparator;
import com.openkm.frontend.client.util.RoleComparator;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.servlet.frontend.util.GWTUserComparator;
import com.openkm.util.MappingUtils;
import com.openkm.util.UserActivity;

/**
 * Servlet Class
 */
public class AuthServlet extends OKMRemoteServiceServlet implements
        OKMAuthService {
    private static Logger log = LoggerFactory.getLogger(AuthServlet.class);

    private static final long serialVersionUID = 2638205115826644606L;

    @Override
    public void logout() throws OKMException {
        log.debug("logout()");
        updateSessionManager();

        try {
            OKMAuth.getInstance().logout(null);
            getThreadLocalRequest().getSession().invalidate();
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("logout: void");
    }

    @Override
    public Map<String, Integer> getGrantedRoles(final String nodePath)
            throws OKMException {
        log.debug("getGrantedRoles({})", nodePath);
        Map<String, Integer> hm = new HashMap<String, Integer>();
        updateSessionManager();

        try {
            final Map<String, Integer> tmp = OKMAuth.getInstance()
                    .getGrantedRoles(null, nodePath);
            hm = MappingUtils.map(tmp);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getGrantedRoles: {}", hm);
        return hm;
    }

    @Override
    public List<GWTGrantedUser> getGrantedUsers(final String nodePath)
            throws OKMException {
        log.debug("getGrantedUsers({})", nodePath);
        final List<GWTGrantedUser> guList = new ArrayList<GWTGrantedUser>();
        updateSessionManager();

        try {
            final Map<String, Integer> tmp = OKMAuth.getInstance()
                    .getGrantedUsers(null, nodePath);
            final Map<String, Integer> hm = MappingUtils.map(tmp);

            for (final String userId : hm.keySet()) {
                final GWTGrantedUser gu = new GWTGrantedUser();
                gu.setPermisions(hm.get(userId));
                final GWTUser user = new GWTUser();
                user.setId(userId);
                user.setUsername(OKMAuth.getInstance().getName(null, userId));
                gu.setUser(user);
                guList.add(gu);
            }

            Collections.sort(guList, GWTGrantedUserComparator.getInstance());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getGrantedUsers: {}", guList);
        return guList;
    }

    @Override
    public String getRemoteUser() {
        log.debug("getRemoteUser()");
        updateSessionManager();
        final String user = getThreadLocalRequest().getRemoteUser();
        log.debug("getRemoteUser: {}", user);
        return user;
    }

    @Override
    public List<GWTGrantedUser> getUngrantedUsers(final String nodePath)
            throws OKMException {
        log.debug("getUngrantedUsers({})", nodePath);
        final List<GWTGrantedUser> guList = new ArrayList<GWTGrantedUser>();
        updateSessionManager();

        try {
            final Collection<String> grantedUsers = OKMAuth.getInstance()
                    .getGrantedUsers(null, nodePath).keySet();

            for (final String userId : OKMAuth.getInstance().getUsers(null)) {
                if (!grantedUsers.contains(userId)) {
                    final GWTGrantedUser gu = new GWTGrantedUser();
                    gu.setPermisions(0);
                    final GWTUser user = new GWTUser();
                    user.setId(userId);
                    user.setUsername(OKMAuth.getInstance()
                            .getName(null, userId));
                    gu.setUser(user);
                    guList.add(gu);
                }
            }

            Collections.sort(guList, GWTGrantedUserComparator.getInstance());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getUngrantedUsers: {}", guList);
        return guList;
    }

    @Override
    public List<String> getUngrantedRoles(final String nodePath)
            throws OKMException {
        log.debug("getUngrantedRoles({})", nodePath);
        final List<String> roleList = new ArrayList<String>();
        updateSessionManager();

        try {
            final Collection<String> grantedRoles = OKMAuth.getInstance()
                    .getGrantedRoles(null, nodePath).keySet();

            // Not add roles that are granted
            for (final String role : OKMAuth.getInstance().getRoles(null)) {
                if (!grantedRoles.contains(role) && checkConnectionRole(role)) {
                    roleList.add(role);
                }
            }

            Collections.sort(roleList, RoleComparator.getInstance());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getUngrantedRoles: {}", roleList);
        return roleList;
    }

    @Override
    public List<GWTGrantedUser> getFilteredUngrantedUsers(
            final String nodePath, final String filter) throws OKMException {
        log.debug("getFilteredUngrantedUsers({})", nodePath);
        final List<GWTGrantedUser> guList = new ArrayList<GWTGrantedUser>();
        updateSessionManager();

        try {
            final Collection<String> col = OKMAuth.getInstance().getUsers(null);
            final Collection<String> grantedUsers = OKMAuth.getInstance()
                    .getGrantedUsers(null, nodePath).keySet();

            for (final String userId : col) {
                final String userName = OKMAuth.getInstance().getName(null,
                        userId);

                if (userName != null
                        && !grantedUsers.contains(userId)
                        && userName.toLowerCase().startsWith(
                                filter.toLowerCase())) {
                    final GWTGrantedUser gu = new GWTGrantedUser();
                    gu.setPermisions(0);
                    final GWTUser user = new GWTUser();
                    user.setId(userId);
                    user.setUsername(userName);
                    gu.setUser(user);
                    guList.add(gu);
                }
            }

            Collections.sort(guList, GWTGrantedUserComparator.getInstance());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getFilteredUngrantedUsers: {}", guList);
        return guList;
    }

    @Override
    public List<String> getFilteredUngrantedRoles(final String nodePath,
            final String filter) throws OKMException {
        log.debug("getFilteredUngrantedRoles({})", nodePath);
        final List<String> roleList = new ArrayList<String>();
        updateSessionManager();

        try {
            final Collection<String> grantedRoles = OKMAuth.getInstance()
                    .getGrantedRoles(null, nodePath).keySet();

            // Not add roles that are granted
            for (final String role : OKMAuth.getInstance().getRoles(null)) {
                if (!grantedRoles.contains(role)
                        && role.toLowerCase().startsWith(filter.toLowerCase())
                        && checkConnectionRole(role)) {
                    roleList.add(role);
                }
            }

            Collections.sort(roleList, RoleComparator.getInstance());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getFilteredUngrantedRoles: {}", roleList);
        return roleList;
    }

    @Override
    public void grantUser(final String path, final String user,
            final int permissions, final boolean recursive) throws OKMException {
        log.debug("grantUser({}, {}, {}, {})", new Object[] { path, user,
                permissions, recursive });
        updateSessionManager();

        try {
            OKMAuth.getInstance().grantUser(null, path, user, permissions,
                    recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("grantUser: void");
    }

    @Override
    public void revokeUser(final String path, final String user,
            final boolean recursive) throws OKMException {
        log.debug("revokeUser({}, {}, {})", new Object[] { path, user,
                recursive });
        updateSessionManager();

        try {
            final OKMAuth oKMAuth = OKMAuth.getInstance();
            final int allGrants = Permission.ALL_GRANTS;

            oKMAuth.revokeUser(null, path, user, allGrants, recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("revokeUser: void");
    }

    @Override
    public void revokeUser(final String path, final String user,
            final int permissions, final boolean recursive) throws OKMException {
        log.debug("revokeUser({}, {}, {}, {})", new Object[] { path, user,
                permissions, recursive });
        updateSessionManager();

        try {
            OKMAuth.getInstance().revokeUser(null, path, user, permissions,
                    recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("revokeUser: void");
    }

    @Override
    public void grantRole(final String path, final String role,
            final int permissions, final boolean recursive) throws OKMException {
        log.debug("grantRole({}, {}, {}, {})", new Object[] { path, role,
                permissions, recursive });
        updateSessionManager();

        try {
            OKMAuth.getInstance().grantRole(null, path, role, permissions,
                    recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("grantRole: void");
    }

    @Override
    public void revokeRole(final String path, final String role,
            final boolean recursive) throws OKMException {
        log.debug("revokeRole({}, {}, {})", new Object[] { path, role,
                recursive });
        updateSessionManager();

        try {
            final OKMAuth oKMAuth = OKMAuth.getInstance();
            final int allGrants = Permission.ALL_GRANTS;

            oKMAuth.revokeRole(null, path, role, allGrants, recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("revokeRole: void");
    }

    @Override
    public void revokeRole(final String path, final String role,
            final int permissions, final boolean recursive) throws OKMException {
        log.debug("revokeRole({}, {}, {}, {})", new Object[] { path, role,
                permissions, recursive });
        updateSessionManager();

        try {
            OKMAuth.getInstance().revokeRole(null, path, role, permissions,
                    recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("revokeRole: void");
    }

    @Override
    public void keepAlive() throws OKMException {
        log.debug("keepAlive()");
        updateSessionManager();
        final String user = getThreadLocalRequest().getRemoteUser();

        // Activity log
        UserActivity.log(user, "KEEP_ALIVE", null, null, null);
        log.debug("keepAlive: void");
    }

    @Override
    public List<GWTUser> getAllUsers() throws OKMException {
        log.debug("getAllUsers()");
        final List<GWTUser> userList = new ArrayList<GWTUser>();
        updateSessionManager();

        try {
            for (final String userId : OKMAuth.getInstance().getUsers(null)) {
                final GWTUser user = new GWTUser();
                user.setId(userId);
                user.setUsername(OKMAuth.getInstance().getName(null, userId));
                userList.add(user);
            }

            Collections.sort(userList,
                    GWTUserComparator.getInstance(getLanguage()));
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getAllUsers: {}", userList);
        return userList;
    }

    @Override
    public List<String> getAllRoles() throws OKMException {
        log.debug("getAllRoles()");
        final List<String> roleList = new ArrayList<String>();
        updateSessionManager();

        try {
            for (final String role : OKMAuth.getInstance().getRoles(null)) {
                if (checkConnectionRole(role)) {
                    roleList.add(role);
                }
            }

            Collections.sort(roleList, RoleComparator.getInstance());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getAllRoles: {}", roleList);
        return roleList;
    }

    @Override
    public List<GWTUser> getFilteredAllUsers(final String filter,
            final List<String> selectedUsers) throws OKMException {
        log.debug("getFilteredAllUsers()");
        final List<GWTUser> userList = new ArrayList<GWTUser>();
        updateSessionManager();

        try {
            for (final String userId : OKMAuth.getInstance().getUsers(null)) {
                final String userName = OKMAuth.getInstance().getName(null,
                        userId);
                if (userName.toLowerCase().startsWith(filter.toLowerCase())
                        && !selectedUsers.contains(userId)) {
                    final GWTUser user = new GWTUser();
                    user.setId(userId);
                    user.setUsername(OKMAuth.getInstance()
                            .getName(null, userId));
                    userList.add(user);
                }
            }

            Collections.sort(userList,
                    GWTUserComparator.getInstance(getLanguage()));
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getFilteredAllUsers: {}", userList);
        return userList;
    }

    @Override
    public List<String> getFilteredAllRoles(final String filter,
            final List<String> selectedRoles) throws OKMException {
        log.debug("getFilteredAllRoles()");
        final List<String> roleList = new ArrayList<String>();
        updateSessionManager();

        try {
            for (final String role : OKMAuth.getInstance().getRoles(null)) {
                if (role.toLowerCase().startsWith(filter.toLowerCase())
                        && !selectedRoles.contains(role)
                        && checkConnectionRole(role)) {
                    roleList.add(role);
                }
            }

            Collections.sort(roleList, RoleComparator.getInstance());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getFilteredAllRoles: {}", roleList);
        return roleList;
    }

    @Override
    public void changeSecurity(final String path,
            final Map<String, Integer> grantUsers,
            final Map<String, Integer> revokeUsers,
            final Map<String, Integer> grantRoles,
            final Map<String, Integer> revokeRoles, final boolean recursive)
            throws OKMException {
        log.debug("changeSecurity({}, {}, {}, {}, {}, {})", new Object[] {
                path, grantUsers, revokeUsers, grantRoles, revokeRoles,
                recursive });
        updateSessionManager();

        try {
            OKMAuth.getInstance().changeSecurity(null, path, grantUsers,
                    revokeUsers, grantRoles, revokeRoles, recursive);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMAuthService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMAuthService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("changeSecurity: void");
    }

    private boolean checkConnectionRole(final String role) {
        if (Config.PRINCIPAL_HIDE_CONNECTION_ROLES) {
            return !role.equals(Config.DEFAULT_USER_ROLE)
                    && !role.equals(Config.DEFAULT_ADMIN_ROLE);
        } else {
            return true;
        }
    }
}
