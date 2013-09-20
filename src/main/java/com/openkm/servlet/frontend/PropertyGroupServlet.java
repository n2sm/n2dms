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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMPropertyGroup;
import com.openkm.bean.PropertyGroup;
import com.openkm.bean.form.FormElement;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.NoSuchPropertyException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTPropertyGroup;
import com.openkm.frontend.client.bean.form.GWTFormElement;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMPropertyGroupService;
import com.openkm.servlet.frontend.util.PropertyGroupComparator;
import com.openkm.util.GWTUtil;

/**
 * PropertyGroup Servlet Class
 */
public class PropertyGroupServlet extends OKMRemoteServiceServlet implements
        OKMPropertyGroupService {
    private static Logger log = LoggerFactory
            .getLogger(PropertyGroupServlet.class);

    private static final long serialVersionUID = 2638205115826644606L;

    @Override
    public List<GWTPropertyGroup> getAllGroups() throws OKMException {
        log.debug("getAllGroups()");
        final List<GWTPropertyGroup> groupList = new ArrayList<GWTPropertyGroup>();
        updateSessionManager();

        try {
            for (final PropertyGroup pg : OKMPropertyGroup.getInstance()
                    .getAllGroups(null)) {
                if (pg.isVisible()) {
                    final GWTPropertyGroup group = GWTUtil.copy(pg);
                    groupList.add(group);
                }
            }
            Collections.sort(groupList,
                    PropertyGroupComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getAllGroups: {}", groupList);
        return groupList;
    }

    @Override
    public List<GWTPropertyGroup> getAllGroups(final String path)
            throws OKMException {
        log.debug("getAllGroups({})", path);
        final List<GWTPropertyGroup> groupList = new ArrayList<GWTPropertyGroup>();
        updateSessionManager();

        try {
            final List<GWTPropertyGroup> actualGroupsList = getGroups(path);
            for (final PropertyGroup pg : OKMPropertyGroup.getInstance()
                    .getAllGroups(null)) {
                if (pg.isVisible()) {
                    final GWTPropertyGroup group = GWTUtil.copy(pg);
                    groupList.add(group);
                }
            }

            // Purge from list values that are assigned to document
            if (!actualGroupsList.isEmpty()) {
                for (final GWTPropertyGroup group : actualGroupsList) {
                    for (final GWTPropertyGroup groupListElement : groupList) {
                        if (groupListElement.getName().equals(group.getName())) {
                            groupList.remove(groupListElement);
                            break;
                        }
                    }
                }
            }
            Collections.sort(groupList,
                    PropertyGroupComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final OKMException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getAllGroups: {}", groupList);
        return groupList;
    }

    @Override
    public void addGroup(final String path, final String grpName)
            throws OKMException {
        log.debug("addGroup({}, {})", path, grpName);
        updateSessionManager();

        try {
            OKMPropertyGroup.getInstance().addGroup(null, path, grpName);
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Lock), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("addGroup: void");
    }

    @Override
    public List<GWTPropertyGroup> getGroups(final String path)
            throws OKMException {
        log.debug("getGroups({})", path);
        final List<GWTPropertyGroup> groupList = new ArrayList<GWTPropertyGroup>();
        updateSessionManager();

        try {
            for (final PropertyGroup pg : OKMPropertyGroup.getInstance()
                    .getGroups(null, path)) {
                if (pg.isVisible()) {
                    final GWTPropertyGroup group = GWTUtil.copy(pg);
                    groupList.add(group);
                }
            }
            Collections.sort(groupList,
                    PropertyGroupComparator.getInstance(getLanguage()));
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getGroups: {}", groupList);
        return groupList;
    }

    @Override
    public List<GWTFormElement> getProperties(final String path,
            final String grpName) throws OKMException {
        log.debug("getProperties({}, {})", path, grpName);
        final List<GWTFormElement> properties = new ArrayList<GWTFormElement>();
        updateSessionManager();

        try {
            for (final FormElement formElement : OKMPropertyGroup.getInstance()
                    .getProperties(null, path, grpName)) {
                properties.add(GWTUtil.copy(formElement));
            }
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getProperties: {}", properties);
        return properties;
    }

    @Override
    public List<GWTFormElement> getPropertyGroupForm(final String grpName)
            throws OKMException {
        log.debug("getPropertyGroupForm({})", grpName);
        final List<GWTFormElement> gwtProperties = new ArrayList<GWTFormElement>();
        updateSessionManager();

        try {
            for (final FormElement formElement : OKMPropertyGroup.getInstance()
                    .getPropertyGroupForm(null, grpName)) {
                gwtProperties.add(GWTUtil.copy(formElement));
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_IO), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getPropertyGroupForm: {}", gwtProperties);
        return gwtProperties;
    }

    @Override
    public void setProperties(final String path, final String grpName,
            final List<GWTFormElement> formProperties) throws OKMException {
        log.debug("setProperties({}, {}, {})", new Object[] { path, grpName,
                formProperties });
        updateSessionManager();

        try {
            final List<FormElement> properties = new ArrayList<FormElement>();

            for (final GWTFormElement gWTformElement : formProperties) {
                properties.add(GWTUtil.copy(gWTformElement));
            }

            OKMPropertyGroup.getInstance().setProperties(null, path, grpName,
                    properties);
        } catch (final NoSuchPropertyException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_NoSuchProperty), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Lock), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("setProperties: void");
    }

    @Override
    public void removeGroup(final String path, final String grpName)
            throws OKMException {
        log.debug("removeGroup({}, {})", path, grpName);
        updateSessionManager();

        try {
            OKMPropertyGroup.getInstance().removeGroup(null, path, grpName);
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Lock), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMPropertyGroupService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("removeGroup: void");
    }
}
