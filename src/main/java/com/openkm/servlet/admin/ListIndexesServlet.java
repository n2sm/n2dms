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

package com.openkm.servlet.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.ProjectionConstants;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.Config;
import com.openkm.dao.HibernateUtil;
import com.openkm.dao.SearchDAO;
import com.openkm.dao.bean.NodeBase;
import com.openkm.dao.bean.NodeDocument;
import com.openkm.dao.bean.NodeFolder;
import com.openkm.dao.bean.NodeMail;
import com.openkm.util.WebUtils;

/**
 * Rebuild Lucene indexes
 */
public class ListIndexesServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory
            .getLogger(ListIndexesServlet.class);

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
            if (action.equals("search")) {
                searchLuceneDocuments(request, response);
            } else {
                showLuceneDocument(request, response);
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        }
    }

    /**
     * List Lucene indexes
     */
    @SuppressWarnings("unchecked")
    private void showLuceneDocument(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        final boolean showTerms = WebUtils.getBoolean(request, "showTerms");
        final int id = WebUtils.getInt(request, "id", 0);
        FullTextSession ftSession = null;
        ReaderProvider rProv = null;
        Session session = null;
        IndexReader idx = null;
        final List<Map<String, String>> fields = new ArrayList<Map<String, String>>();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            ftSession = Search.getFullTextSession(session);
            final SearchFactory sFactory = ftSession.getSearchFactory();
            rProv = sFactory.getReaderProvider();

            final DirectoryProvider<Directory>[] dirProv = sFactory
                    .getDirectoryProviders(NodeDocument.class);
            idx = rProv.openReader(dirProv[0]);

            // Print Lucene documents
            if (!idx.isDeleted(id)) {
                final Document doc = idx.document(id);
                String hibClass = null;

                for (final Fieldable fld : doc.getFields()) {
                    final Map<String, String> field = new HashMap<String, String>();
                    field.put("name", fld.name());
                    field.put("value", fld.stringValue());
                    fields.add(field);

                    if (fld.name().equals("_hibernate_class")) {
                        hibClass = fld.stringValue();
                    }
                }

                /**
                 * 1) Get all the terms using indexReader.terms()
                 * 2) Process the term only if it belongs to the target field.
                 * 3) Get all the docs using indexReader.termDocs(term);
                 * 4) So, we have the term-doc pairs at this point.
                 */
                if (showTerms
                        && NodeDocument.class.getCanonicalName().equals(
                                hibClass)) {
                    final List<String> terms = new ArrayList<String>();

                    for (final TermEnum te = idx.terms(); te.next();) {
                        final Term t = te.term();

                        if ("text".equals(t.field())) {
                            for (final TermDocs tds = idx.termDocs(t); tds
                                    .next();) {
                                if (id == tds.doc()) {
                                    terms.add(t.text());
                                }
                            }
                        }
                    }

                    final Map<String, String> field = new HashMap<String, String>();
                    field.put("name", "terms");
                    field.put("value", terms.toString());
                    fields.add(field);
                }
            }

            final ServletContext sc = getServletContext();
            sc.setAttribute("fields", fields);
            sc.setAttribute("id", id);
            sc.setAttribute("max", idx.maxDoc() - 1);
            sc.setAttribute("prev", id > 0);
            sc.setAttribute("next", id < idx.maxDoc() - 1);
            sc.setAttribute("showTerms", showTerms);
            sc.getRequestDispatcher("/admin/list_indexes.jsp").forward(request,
                    response);
        } finally {
            if (rProv != null && idx != null) {
                rProv.closeReader(idx);
            }

            HibernateUtil.close(ftSession);
            HibernateUtil.close(session);
        }
    }

    /**
     * Search Lucene indexes
     */
    @SuppressWarnings("unchecked")
    private void searchLuceneDocuments(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException, ParseException {
        final String exp = WebUtils.getString(request, "exp");
        FullTextSession ftSession = null;
        Session session = null;
        final List<Map<String, String>> results = new ArrayList<Map<String, String>>();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            ftSession = Search.getFullTextSession(session);

            if (exp != null && !exp.isEmpty()) {
                final QueryParser parser = new QueryParser(
                        Config.LUCENE_VERSION, NodeDocument.TEXT_FIELD,
                        SearchDAO.analyzer);
                final Query query = parser.parse(exp);
                log.info("Query: {}", query);

                final FullTextQuery ftq = ftSession.createFullTextQuery(query,
                        NodeDocument.class, NodeFolder.class, NodeMail.class);
                ftq.setProjection(ProjectionConstants.DOCUMENT_ID,
                        ProjectionConstants.SCORE, ProjectionConstants.THIS);

                for (final Iterator<Object[]> it = ftq.iterate(); it.hasNext();) {
                    final Object[] qRes = it.next();
                    final Integer docId = (Integer) qRes[0];
                    final Float score = (Float) qRes[1];
                    final NodeBase nBase = (NodeBase) qRes[2];

                    // Add result
                    final Map<String, String> res = new HashMap<String, String>();
                    res.put("docId", String.valueOf(docId));
                    res.put("score", String.valueOf(score));
                    res.put("uuid", nBase.getUuid());
                    res.put("name", nBase.getName());

                    if (nBase instanceof NodeDocument) {
                        res.put("type", "Document");
                    } else if (nBase instanceof NodeFolder) {
                        res.put("type", "Folder");
                    } else {
                        log.warn("Unknown");
                    }

                    results.add(res);
                }
            }

            final ServletContext sc = getServletContext();
            sc.setAttribute("results", results);
            sc.setAttribute("exp", exp);
            sc.getRequestDispatcher("/admin/search_indexes.jsp").forward(
                    request, response);
        } finally {
            HibernateUtil.close(ftSession);
            HibernateUtil.close(session);
        }
    }
}
