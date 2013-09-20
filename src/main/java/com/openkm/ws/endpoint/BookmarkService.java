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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.Bookmark;
import com.openkm.module.BookmarkModule;
import com.openkm.module.ModuleManager;

@WebService(name = "OKMBookmark", serviceName = "OKMBookmark", targetNamespace = "http://ws.openkm.com")
public class BookmarkService {
    private static Logger log = LoggerFactory.getLogger(BookmarkService.class);

    @WebMethod
    public Bookmark add(@WebParam(name = "token") final String token,
            @WebParam(name = "nodePath") final String nodePath,
            @WebParam(name = "name") final String name)
            throws AccessDeniedException, PathNotFoundException,
            RepositoryException, DatabaseException {
        log.debug("add({}, {}, {})", new Object[] { token, nodePath, name });
        final BookmarkModule bm = ModuleManager.getBookmarkModule();
        final Bookmark bookmark = bm.add(token, nodePath, name);
        log.debug("add: {}", bookmark);
        return bookmark;
    }

    @WebMethod
    public Bookmark get(@WebParam(name = "token") final String token,
            @WebParam(name = "bmId") final int bmId)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("get({}, {})", new Object[] { token, bmId });
        final BookmarkModule bm = ModuleManager.getBookmarkModule();
        final Bookmark bookmark = bm.get(token, bmId);
        log.debug("get: {}", bookmark);
        return bookmark;
    }

    @WebMethod
    public void remove(@WebParam(name = "token") final String token,
            @WebParam(name = "bmId") final int bmId)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("remove({}, {})", token, bmId);
        final BookmarkModule bm = ModuleManager.getBookmarkModule();
        bm.remove(token, bmId);
        log.debug("remove: void");
    }

    @WebMethod
    public Bookmark rename(@WebParam(name = "token") final String token,
            @WebParam(name = "bmId") final int bmId,
            @WebParam(name = "newName") final String newName)
            throws AccessDeniedException, RepositoryException,
            DatabaseException {
        log.debug("rename({}, {}, {})", new Object[] { token, bmId, newName });
        final BookmarkModule bm = ModuleManager.getBookmarkModule();
        final Bookmark bookmark = bm.rename(token, bmId, newName);
        log.debug("rename: {}", bookmark);
        return bookmark;
    }

    @WebMethod
    public Bookmark[] getAll(@WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException {
        log.debug("getAll({})", token);
        final BookmarkModule bm = ModuleManager.getBookmarkModule();
        final List<Bookmark> col = bm.getAll(token);
        final Bookmark[] result = col.toArray(new Bookmark[col.size()]);
        log.debug("getAll: {}", col);
        return result;
    }
}
