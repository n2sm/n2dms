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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.DatabaseException;
import com.openkm.module.jcr.stuff.JCRUtils;
import com.openkm.util.UserActivity;
import com.openkm.util.WebUtils;

/**
 * Repository search servlet
 */
public class RepositorySearchServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory
            .getLogger(RepositorySearchServlet.class);

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
        final String statement = WebUtils.getString(request, "statement");
        final String type = WebUtils.getString(request, "type");
        Session session = null;
        updateSessionManager(request);

        try {
            session = JCRUtils.getSession();

            if (!statement.equals("") && !type.equals("")) {
                search(session, statement, type, request, response);

                // Activity log
                UserActivity.log(request.getRemoteUser(),
                        "ADMIN_REPOSITORY_SEARCH", null, null, type + ", "
                                + statement);
            } else {
                final ServletContext sc = getServletContext();
                sc.setAttribute("statement", null);
                sc.setAttribute("type", null);
                sc.setAttribute("size", null);
                sc.setAttribute("columns", null);
                sc.setAttribute("results", null);
                sc.getRequestDispatcher("/admin/repository_search.jsp")
                        .forward(request, response);
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final LoginException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } finally {
            JCRUtils.logout(session);
        }
    }

    /**
     * Perform JCR search
     */
    private void search(final Session session, final String statement,
            final String type, final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, RepositoryException {
        log.debug("search({}, {}, {}, {}, {})", new Object[] { session,
                statement, type, request, response });
        final ServletContext sc = getServletContext();
        final Workspace workspace = session.getWorkspace();
        final QueryManager queryManager = workspace.getQueryManager();
        final Query query = queryManager.createQuery(statement, type);
        final QueryResult result = query.execute();
        final RowIterator it = result.getRows();
        final String[] cols = result.getColumnNames();
        final List<String> columns = new ArrayList<String>();
        final List<List<String>> results = new ArrayList<List<String>>();

        for (final String col : cols) {
            columns.add(col);
        }

        while (it.hasNext()) {
            final Row row = it.nextRow();
            final List<String> tmp = new ArrayList<String>();

            for (final String col : cols) {
                if (col.startsWith("jcr:")) {
                    // Get property from row
                    tmp.add(row.getValue(col) != null ? row.getValue(col)
                            .getString() : "NULL");
                } else {
                    // Get property from node
                    final String path = row.getValue(JcrConstants.JCR_PATH)
                            .getString();
                    final Node node = session.getRootNode().getNode(
                            path.substring(1));

                    if (node.hasProperty(col)) {
                        final Property prop = node.getProperty(col);

                        if (prop != null) {
                            if (prop.getDefinition().isMultiple()) {
                                final Value[] values = prop.getValues();
                                final StringBuilder sb = new StringBuilder();

                                for (final Value value : values) {
                                    sb.append(value.getString() + " ");
                                }

                                tmp.add(sb.toString());
                            } else {
                                tmp.add(prop.getValue() != null ? prop
                                        .getValue().getString() : "NULL");
                            }
                        }
                    } else {
                        tmp.add("");
                    }
                }
            }

            results.add(tmp);
        }

        sc.setAttribute("statement", statement);
        sc.setAttribute("type", type);
        sc.setAttribute("size", it.getSize());
        sc.setAttribute("columns", columns);
        sc.setAttribute("results", results);
        sc.getRequestDispatcher("/admin/repository_search.jsp").forward(
                request, response);
        log.debug("search: void");
    }
}
