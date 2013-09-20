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

package com.openkm.servlet.admin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.dao.TwitterAccountDAO;
import com.openkm.dao.bean.TwitterAccount;
import com.openkm.util.UserActivity;
import com.openkm.util.WebUtils;

/**
 * User twitter accounts servlet
 */
public class TwitterAccountServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory
            .getLogger(TwitterAccountServlet.class);

    @Override
    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        log.debug("doGet({}, {})", request, response);
        request.setCharacterEncoding("UTF-8");
        final String action = WebUtils.getString(request, "action");
        final String userId = request.getRemoteUser();
        updateSessionManager(request);

        try {
            if (action.equals("create")) {
                create(userId, request, response);
            } else if (action.equals("edit")) {
                edit(userId, request, response);
            } else if (action.equals("delete")) {
                delete(userId, request, response);
            }

            if (action.equals("") || WebUtils.getBoolean(request, "persist")) {
                list(userId, request, response);
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        }
    }

    /**
     * New twitter account
     */
    private void create(final String userId, final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, DatabaseException {
        log.info("create({}, {}, {})",
                new Object[] { userId, request, response });

        if (WebUtils.getBoolean(request, "persist")) {
            final TwitterAccount ta = new TwitterAccount();
            ta.setUser(WebUtils.getString(request, "ta_user"));
            ta.setTwitterUser(WebUtils.getString(request, "ta_tuser"));
            ta.setActive(WebUtils.getBoolean(request, "ta_active"));
            TwitterAccountDAO.create(ta);

            // Activity log
            UserActivity.log(userId, "ADMIN_TWITTER_ACCOUNT_CREATE",
                    ta.getUser(), null, ta.toString());
        } else {
            final ServletContext sc = getServletContext();
            final TwitterAccount ta = new TwitterAccount();
            ta.setUser(WebUtils.getString(request, "ta_user"));
            sc.setAttribute("action", WebUtils.getString(request, "action"));
            sc.setAttribute("persist", true);
            sc.setAttribute("ta", ta);
            sc.getRequestDispatcher("/admin/twitter_account_edit.jsp").forward(
                    request, response);
        }

        log.debug("create: void");
    }

    /**
     * Edit twitter account
     */
    private void edit(final String userId, final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, DatabaseException, NoSuchAlgorithmException {
        log.debug("edit({}, {}, {})",
                new Object[] { userId, request, response });

        if (WebUtils.getBoolean(request, "persist")) {
            final TwitterAccount ta = new TwitterAccount();
            ta.setId(WebUtils.getInt(request, "ta_id"));
            ta.setUser(WebUtils.getString(request, "ta_user"));
            ta.setTwitterUser(WebUtils.getString(request, "ta_tuser"));
            ta.setActive(WebUtils.getBoolean(request, "ta_active"));
            TwitterAccountDAO.update(ta);

            // Activity log
            UserActivity.log(userId, "ADMIN_TWITTER_ACCOUNT_EDIT",
                    Long.toString(ta.getId()), null, ta.toString());
        } else {
            final ServletContext sc = getServletContext();
            final int taId = WebUtils.getInt(request, "ta_id");
            sc.setAttribute("action", WebUtils.getString(request, "action"));
            sc.setAttribute("persist", true);
            sc.setAttribute("ta", TwitterAccountDAO.findByPk(taId));
            sc.getRequestDispatcher("/admin/twitter_account_edit.jsp").forward(
                    request, response);
        }

        log.debug("edit: void");
    }

    /**
     * Update twitter account
     */
    private void delete(final String userId, final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, DatabaseException, NoSuchAlgorithmException {
        log.debug("delete({}, {}, {})", new Object[] { userId, request,
                response });

        if (WebUtils.getBoolean(request, "persist")) {
            final int taId = WebUtils.getInt(request, "ta_id");
            TwitterAccountDAO.delete(taId);

            // Activity log
            UserActivity.log(userId, "ADMIN_TWITTER_ACCOUNT_DELETE",
                    Integer.toString(taId), null, null);
        } else {
            final ServletContext sc = getServletContext();
            final int taId = WebUtils.getInt(request, "ta_id");
            sc.setAttribute("action", WebUtils.getString(request, "action"));
            sc.setAttribute("persist", true);
            sc.setAttribute("ta", TwitterAccountDAO.findByPk(taId));
            sc.getRequestDispatcher("/admin/twitter_account_edit.jsp").forward(
                    request, response);
        }

        log.debug("delete: void");
    }

    /**
     * List twitter accounts
     */
    private void list(final String userId, final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, DatabaseException {
        log.debug("list({}, {}, {})",
                new Object[] { userId, request, response });
        final ServletContext sc = getServletContext();
        final String usrId = WebUtils.getString(request, "ta_user");
        sc.setAttribute("ta_user", usrId);
        sc.setAttribute("twitterAccounts",
                TwitterAccountDAO.findByUser(usrId, false));
        sc.getRequestDispatcher("/admin/twitter_account_list.jsp").forward(
                request, response);
        log.debug("list: void");
    }
}
