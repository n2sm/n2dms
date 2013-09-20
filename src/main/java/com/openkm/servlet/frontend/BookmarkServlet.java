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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMBookmark;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.bean.Bookmark;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTBookmark;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMBookmarkService;
import com.openkm.servlet.frontend.util.BookmarkComparator;
import com.openkm.util.GWTUtil;

/**
 * Servlet Class
 */
public class BookmarkServlet extends OKMRemoteServiceServlet implements
        OKMBookmarkService {
    private static Logger log = LoggerFactory.getLogger(BookmarkServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public List<GWTBookmark> getAll() throws OKMException {
        log.debug("getAll()");
        final List<GWTBookmark> bookmarkList = new ArrayList<GWTBookmark>();
        updateSessionManager();

        try {
            final Collection<Bookmark> col = OKMBookmark.getInstance().getAll(
                    null);

            for (final Bookmark bookmark : col) {
                log.debug("Bookmark: {}", bookmark);
                final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                        bookmark.getNode());
                final GWTBookmark bookmarkClient = GWTUtil.copy(bookmark, path);
                bookmarkList.add(bookmarkClient);
            }

            Collections.sort(bookmarkList,
                    BookmarkComparator.getInstance(getLanguage()));
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getAll: {}", bookmarkList);
        return bookmarkList;
    }

    @Override
    public GWTBookmark add(final String nodePath, final String name)
            throws OKMException {
        log.debug("add({}, {})", nodePath, name);
        updateSessionManager();

        try {
            final Bookmark bookmark = OKMBookmark.getInstance().add(null,
                    nodePath, name);
            final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                    bookmark.getNode());
            return GWTUtil.copy(bookmark, path);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }
    }

    @Override
    public void remove(final int bmId) throws OKMException {
        log.debug("remove({})", bmId);
        updateSessionManager();

        try {
            OKMBookmark.getInstance().remove(null, bmId);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("remove: void");
    }

    @Override
    public GWTBookmark rename(final int bmId, final String newName)
            throws OKMException {
        log.debug("rename({}, {})", bmId, newName);
        updateSessionManager();

        try {
            final Bookmark bookmark = OKMBookmark.getInstance().rename(null,
                    bmId, newName);
            final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                    bookmark.getNode());
            return GWTUtil.copy(bookmark, path);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }
    }

    @Override
    public GWTBookmark get(final int bmId) throws OKMException {
        log.debug("get({})", bmId);
        updateSessionManager();

        try {
            final Bookmark bookmark = OKMBookmark.getInstance().get(null, bmId);
            final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                    bookmark.getNode());
            return GWTUtil.copy(bookmark, path);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMBookmarkService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }
    }
}
