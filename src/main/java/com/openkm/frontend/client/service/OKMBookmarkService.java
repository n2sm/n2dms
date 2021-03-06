/**
 *  OpenKM, Open Document Management System (http://www.openkm.com)
 *  Copyright (c) 2006-2015  Paco Avila & Josep Llort
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

package com.openkm.frontend.client.service;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTBookmark;

/**
 * @author jllort
 *
 */
@RemoteServiceRelativePath("Bookmark")
public interface OKMBookmarkService extends RemoteService {
    public List<GWTBookmark> getAll() throws OKMException;

    public GWTBookmark add(String nodePath, String name) throws OKMException;

    public void remove(int bmId) throws OKMException;

    public GWTBookmark rename(int bmId, String newName) throws OKMException;

    public GWTBookmark get(int bmId) throws OKMException;
}