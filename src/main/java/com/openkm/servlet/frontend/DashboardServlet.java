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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMDashboard;
import com.openkm.bean.DashboardDocumentResult;
import com.openkm.bean.DashboardFolderResult;
import com.openkm.bean.DashboardMailResult;
import com.openkm.core.DatabaseException;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.QueryParams;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTDashboardDocumentResult;
import com.openkm.frontend.client.bean.GWTDashboardFolderResult;
import com.openkm.frontend.client.bean.GWTDashboardMailResult;
import com.openkm.frontend.client.bean.GWTQueryParams;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMDashboardService;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.util.GWTUtil;

/**
 * Servlet Class
 * 
 * @web.servlet              name="DashboardServlet"
 *                           display-name="Directory tree service"
 *                           description="Directory tree service"
 * @web.servlet-mapping      url-pattern="/DashboardServlet"
 * @web.servlet-init-param   name="A parameter"
 *                           value="A value"
 */
public class DashboardServlet extends OKMRemoteServiceServlet implements
        OKMDashboardService {
    private static Logger log = LoggerFactory.getLogger(DashboardServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public List<GWTDashboardDocumentResult> getUserLockedDocuments()
            throws OKMException {
        log.debug("getUserLockedDocuments()");
        final List<GWTDashboardDocumentResult> lockList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            final Collection<DashboardDocumentResult> col = OKMDashboard
                    .getInstance().getUserLockedDocuments(null);
            for (final DashboardDocumentResult documentResult : col) {
                GWTDashboardDocumentResult documentResultClient;
                documentResultClient = GWTUtil.copy(documentResult);
                lockList.add(documentResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLockedDocuments: {}", lockList);
        return lockList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserCheckedOutDocuments()
            throws OKMException {
        log.debug("getUserCheckedOutDocuments()");
        final List<GWTDashboardDocumentResult> chekoutList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            final Collection<DashboardDocumentResult> col = OKMDashboard
                    .getInstance().getUserCheckedOutDocuments(null);
            for (final DashboardDocumentResult documentResult : col) {
                final GWTDashboardDocumentResult documentResultClient = GWTUtil
                        .copy(documentResult);
                chekoutList.add(documentResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserCheckedOutDocuments: {}", chekoutList);
        return chekoutList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserLastModifiedDocuments()
            throws OKMException {
        log.debug("getUserLastModifiedDocuments()");
        final List<GWTDashboardDocumentResult> lastModifiedList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            final Collection<DashboardDocumentResult> col = OKMDashboard
                    .getInstance().getUserLastModifiedDocuments(null);
            for (final DashboardDocumentResult documentResult : col) {
                final GWTDashboardDocumentResult documentResultClient = GWTUtil
                        .copy(documentResult);
                lastModifiedList.add(documentResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLastModifiedDocuments: {}", lastModifiedList);
        return lastModifiedList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserSubscribedDocuments()
            throws OKMException {
        log.debug("getUserSubscribedDocuments()");
        final List<GWTDashboardDocumentResult> subscribedList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            final Collection<DashboardDocumentResult> col = OKMDashboard
                    .getInstance().getUserSubscribedDocuments(null);
            for (final DashboardDocumentResult documentResult : col) {
                final GWTDashboardDocumentResult documentResultClient = GWTUtil
                        .copy(documentResult);
                subscribedList.add(documentResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserSubscribedDocuments: {}", subscribedList);
        return subscribedList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserLastUploadedDocuments()
            throws OKMException {
        log.debug("getUserLastUploadedDocuments()");
        final List<GWTDashboardDocumentResult> lastUploadedList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            final Collection<DashboardDocumentResult> col = OKMDashboard
                    .getInstance().getUserLastUploadedDocuments(null);
            for (final DashboardDocumentResult documentResult : col) {
                final GWTDashboardDocumentResult documentResultClient = GWTUtil
                        .copy(documentResult);
                lastUploadedList.add(documentResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLastUploadedDocuments: {}", lastUploadedList);
        return lastUploadedList;
    }

    @Override
    public List<GWTDashboardFolderResult> getUserSubscribedFolders()
            throws OKMException {
        log.debug("getUserSubscribedFolders()");
        final List<GWTDashboardFolderResult> subscribedList = new ArrayList<GWTDashboardFolderResult>();
        updateSessionManager();

        try {
            final Collection<DashboardFolderResult> col = OKMDashboard
                    .getInstance().getUserSubscribedFolders(null);
            for (final DashboardFolderResult folderResult : col) {
                GWTDashboardFolderResult folderResultClient;
                folderResultClient = GWTUtil.copy(folderResult);
                subscribedList.add(folderResultClient);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserSubscribedFolders: {}", subscribedList);
        return subscribedList;
    }

    @Override
    public List<GWTQueryParams> getUserSearchs() throws OKMException {
        log.debug("getUserSearchs()");
        final List<GWTQueryParams> searchList = new ArrayList<GWTQueryParams>();
        updateSessionManager();

        try {
            for (final QueryParams queryParams : OKMDashboard.getInstance()
                    .getUserSearchs(null)) {
                searchList.add(GWTUtil.copy(queryParams));
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        }

        log.debug("getUserSearchs: {}", searchList);
        return searchList;
    }

    @Override
    public List<GWTDashboardDocumentResult> find(final int id)
            throws OKMException {
        log.debug("find({})", id);
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().find(null, id)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("find: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastWeekTopDownloadedDocuments()
            throws OKMException {
        log.debug("getLastWeekTopDownloadedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastWeekTopDownloadedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastWeekTopDownloadedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastMonthTopDownloadedDocuments()
            throws OKMException {
        log.debug("getLastMonthTopDownloadedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastMonthTopDownloadedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastMonthTopDownloadedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastWeekTopModifiedDocuments()
            throws OKMException {
        log.debug("getLastWeekTopModifiedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastWeekTopModifiedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastWeekTopModifiedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastMonthTopModifiedDocuments()
            throws OKMException {
        log.debug("getLastMonthTopModifiedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastMonthTopModifiedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastMonthTopModifiedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserLastDownloadedDocuments()
            throws OKMException {
        log.debug("getUserLastDownloadedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getUserLastDownloadedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLastDownloadedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastModifiedDocuments()
            throws OKMException {
        log.debug("getLastModifiedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastModifiedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastModifiedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getLastUploadedDocuments()
            throws OKMException {
        log.debug("getLastWeekTopUploadedDocuments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getLastUploadedDocuments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getLastWeekTopUploadedDocuments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardDocumentResult> getUserLastImportedMailAttachments()
            throws OKMException {
        log.debug("getUserLastImportedMailAttachments()");
        final List<GWTDashboardDocumentResult> docList = new ArrayList<GWTDashboardDocumentResult>();
        updateSessionManager();

        try {
            for (final DashboardDocumentResult dashboardDocumentResult : OKMDashboard
                    .getInstance().getUserLastImportedMailAttachments(null)) {
                docList.add(GWTUtil.copy(dashboardDocumentResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLastImportedMailAttachments: {}", docList);
        return docList;
    }

    @Override
    public List<GWTDashboardMailResult> getUserLastImportedMails()
            throws OKMException {
        log.debug("getUserLastImportedMails()");
        final List<GWTDashboardMailResult> mailList = new ArrayList<GWTDashboardMailResult>();
        updateSessionManager();

        try {
            for (final DashboardMailResult dashboardMailResult : OKMDashboard
                    .getInstance().getUserLastImportedMails(null)) {
                mailList.add(GWTUtil.copy(dashboardMailResult));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMDashboardService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        }

        log.debug("getUserLastImportedMails: {}", mailList);
        return mailList;

    }

    @Override
    public void visiteNode(final String source, final String node,
            final Date date) throws OKMException {
        log.debug("visiteNode({}, {}, {})", new Object[] { source, node, date });
        updateSessionManager();

        try {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            OKMDashboard.getInstance().visiteNode(null, source, node, cal);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDashboardService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        }

        log.debug("visiteNode: void");
    }
}
