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

package com.openkm.api;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.DashboardDocumentResult;
import com.openkm.bean.DashboardFolderResult;
import com.openkm.bean.DashboardMailResult;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.QueryParams;
import com.openkm.module.DashboardModule;
import com.openkm.module.ModuleManager;

/**
 * @author pavila
 *
 */
public class OKMDashboard implements DashboardModule {
    private static Logger log = LoggerFactory.getLogger(OKMDashboard.class);

    private static OKMDashboard instance = new OKMDashboard();

    private OKMDashboard() {
    }

    public static OKMDashboard getInstance() {
        return instance;
    }

    @Override
    public List<DashboardDocumentResult> getUserCheckedOutDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserCheckedOutDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserCheckedOutDocuments(token);
        log.debug("getUserCheckedOutDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastModifiedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserLastModifiedDocuments(token);
        log.debug("getUserLastModifiedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserLockedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLockedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserLockedDocuments(token);
        log.debug("getUserLockedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserSubscribedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserSubscribedDocuments(token);
        log.debug("getUserSubscribedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardFolderResult> getUserSubscribedFolders(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserSubscribedFolders({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardFolderResult> result = dm
                .getUserSubscribedFolders(token);
        log.debug("getUserSubscribedFolders: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastUploadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastUploadedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserLastUploadedDocuments(token);
        log.debug("getUserLastUploadedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastDownloadedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserLastDownloadedDocuments(token);
        log.debug("getUserLastDownloadedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardMailResult> getUserLastImportedMails(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserLastImportedMails({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardMailResult> result = dm
                .getUserLastImportedMails(token);
        log.debug("getUserLastImportedMails: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getUserLastImportedMailAttachments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getUserLastImportedMailAttachments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getUserLastImportedMailAttachments(token);
        log.debug("getUserLastImportedMailAttachments: {}", result);
        return result;
    }

    @Override
    public long getUserDocumentsSize(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserDocumentsSize({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final long size = dm.getUserDocumentsSize(token);
        log.debug("getUserDocumentsSize: {}", size);
        return size;
    }

    @Override
    public List<QueryParams> getUserSearchs(final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getUserSearchs({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<QueryParams> searchs = dm.getUserSearchs(token);
        log.debug("getUserSearchs: {}", searchs);
        return searchs;
    }

    @Override
    public List<DashboardDocumentResult> find(final String token, final int qpId)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("find({}, {})", token, qpId);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> documents = dm.find(token, qpId);
        log.debug("find: {}", documents);
        return documents;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastWeekTopDownloadedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastWeekTopDownloadedDocuments(token);
        log.debug("getLastWeekTopDownloadedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopDownloadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopDownloadedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastMonthTopDownloadedDocuments(token);
        log.debug("getLastMonthTopDownloadedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getLastWeekTopModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastWeekTopModifiedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastWeekTopModifiedDocuments(token);
        log.debug("getLastWeekTopModifiedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getLastMonthTopModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastMonthTopModifiedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastMonthTopModifiedDocuments(token);
        log.debug("getLastMonthTopModifiedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getLastModifiedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastModifiedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastModifiedDocuments(token);
        log.debug("getLastModifiedDocuments: {}", result);
        return result;
    }

    @Override
    public List<DashboardDocumentResult> getLastUploadedDocuments(
            final String token) throws RepositoryException, DatabaseException {
        log.debug("getLastUploadedDocuments({})", token);
        final DashboardModule dm = ModuleManager.getDashboardModule();
        final List<DashboardDocumentResult> result = dm
                .getLastUploadedDocuments(token);
        log.debug("getLastUploadedDocuments: {}", result);
        return result;
    }

    @Override
    public void visiteNode(final String token, final String source,
            final String node, final Calendar date) throws RepositoryException,
            DatabaseException {
        log.debug("visiteNode({}, {}, {}, {})", new Object[] { token, source,
                node, date });
        final DashboardModule dm = ModuleManager.getDashboardModule();
        dm.visiteNode(token, source, node, date);
        log.debug("visiteNode: void");
    }
}
