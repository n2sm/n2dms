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
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;

import com.openkm.core.Cron;
import com.openkm.core.DatabaseException;
import com.openkm.core.MimeTypeConfig;
import com.openkm.dao.CronTabDAO;
import com.openkm.dao.bean.CronTab;
import com.openkm.util.SecureStore;
import com.openkm.util.UserActivity;
import com.openkm.util.WebUtils;

/**
 * Execute crontab servlet
 */
public class CronTabServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(CronTabServlet.class);

    @Override
    public void service(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        final String method = request.getMethod();

        if (isAdmin(request)) {
            if (method.equals(METHOD_GET)) {
                doGet(request, response);
            } else if (method.equals(METHOD_POST)) {
                doPost(request, response);
            }
        }
    }

    @Override
    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        log.debug("doGet({}, {})", request, response);
        request.setCharacterEncoding("UTF-8");
        final String action = WebUtils.getString(request, "action");
        updateSessionManager(request);

        try {
            final Map<String, String> types = new LinkedHashMap<String, String>();
            types.put(CronTab.BSH, "BSH");
            types.put(CronTab.JAR, "JAR");

            if (action.equals("create")) {
                final ServletContext sc = getServletContext();
                final CronTab ct = new CronTab();
                sc.setAttribute("action", action);
                sc.setAttribute("types", types);
                sc.setAttribute("ct", ct);
                sc.getRequestDispatcher("/admin/crontab_edit.jsp").forward(
                        request, response);
            } else if (action.equals("edit")) {
                final ServletContext sc = getServletContext();
                final int ctId = WebUtils.getInt(request, "ct_id");
                final CronTab ct = CronTabDAO.findByPk(ctId);
                sc.setAttribute("action", action);
                sc.setAttribute("types", types);
                sc.setAttribute("ct", ct);
                sc.getRequestDispatcher("/admin/crontab_edit.jsp").forward(
                        request, response);
            } else if (action.equals("delete")) {
                final ServletContext sc = getServletContext();
                final int ctId = WebUtils.getInt(request, "ct_id");
                final CronTab ct = CronTabDAO.findByPk(ctId);
                sc.setAttribute("action", action);
                sc.setAttribute("types", types);
                sc.setAttribute("ct", ct);
                sc.getRequestDispatcher("/admin/crontab_edit.jsp").forward(
                        request, response);
            } else if (action.equals("execute")) {
                execute(request, response);
                list(request, response);
            } else {
                list(request, response);
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final EvalError e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doPost(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        log.debug("doPost({}, {})", request, response);
        request.setCharacterEncoding("UTF-8");
        String action = "";
        final String userId = request.getRemoteUser();
        updateSessionManager(request);

        try {
            if (ServletFileUpload.isMultipartContent(request)) {
                InputStream is = null;
                final FileItemFactory factory = new DiskFileItemFactory();
                final ServletFileUpload upload = new ServletFileUpload(factory);
                final List<FileItem> items = upload.parseRequest(request);
                final CronTab ct = new CronTab();

                for (final FileItem item : items) {
                    if (item.isFormField()) {
                        if (item.getFieldName().equals("action")) {
                            action = item.getString("UTF-8");
                        } else if (item.getFieldName().equals("ct_id")) {
                            ct.setId(Integer.parseInt(item.getString("UTF-8")));
                        } else if (item.getFieldName().equals("ct_name")) {
                            ct.setName(item.getString("UTF-8"));
                        } else if (item.getFieldName().equals("ct_mail")) {
                            ct.setMail(item.getString("UTF-8"));
                        } else if (item.getFieldName().equals("ct_expression")) {
                            ct.setExpression(item.getString("UTF-8"));
                        } else if (item.getFieldName().equals("ct_active")) {
                            ct.setActive(true);
                        }
                    } else {
                        is = item.getInputStream();
                        ct.setFileName(FilenameUtils.getName(item.getName()));
                        ct.setFileContent(SecureStore.b64Encode(IOUtils
                                .toByteArray(is)));
                        ct.setFileMime(MimeTypeConfig.mimeTypes
                                .getContentType(item.getName()));
                        is.close();
                    }
                }

                if (action.equals("create")) {
                    CronTabDAO.create(ct);

                    // Activity log
                    UserActivity.log(userId, "ADMIN_CRONTAB_CREATE", null,
                            null, ct.toString());
                    list(request, response);
                } else if (action.equals("edit")) {
                    CronTabDAO.update(ct);

                    // Activity log
                    UserActivity.log(userId, "ADMIN_CRONTAB_EDIT",
                            Long.toString(ct.getId()), null, ct.toString());
                    list(request, response);
                } else if (action.equals("delete")) {
                    CronTabDAO.delete(ct.getId());

                    // Activity log
                    UserActivity.log(userId, "ADMIN_CRONTAB_DELETE",
                            Long.toString(ct.getId()), null, null);
                    list(request, response);
                }
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final FileUploadException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        }
    }

    /**
     * List registered reports
     */
    private void list(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, DatabaseException {
        log.debug("list({}, {})", new Object[] { request, response });
        final ServletContext sc = getServletContext();
        final List<CronTab> list = CronTabDAO.findAll();
        sc.setAttribute("crontabs", list);
        sc.getRequestDispatcher("/admin/crontab_list.jsp").forward(request,
                response);
        log.debug("list: void");
    }

    /**
     * Execute report
     */
    private void execute(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            DatabaseException, EvalError {
        log.debug("execute({}, {})", new Object[] { request, response });
        final int ctId = WebUtils.getInt(request, "ct_id");
        final CronTab ct = CronTabDAO.findByPk(ctId);

        if (CronTab.BSH.equals(ct.getFileMime())) {
            final Cron.RunnerBsh runner = new Cron.RunnerBsh(ct.getId(),
                    ct.getName(), ct.getMail(), new String(
                            SecureStore.b64Decode(ct.getFileContent())));
            runner.run();
        } else if (CronTab.JAR.equals(ct.getFileMime())) {
            final Cron.RunnerJar runner = new Cron.RunnerJar(ct.getId(),
                    ct.getName(), ct.getMail(), SecureStore.b64Decode(ct
                            .getFileContent()));
            runner.run();
        }

        // Activity log
        UserActivity.log(request.getRemoteUser(), "ADMIN_CRONTAB_EXECUTE",
                Integer.toString(ctId), null, ct.toString());
        log.debug("execute: void");
    }
}
