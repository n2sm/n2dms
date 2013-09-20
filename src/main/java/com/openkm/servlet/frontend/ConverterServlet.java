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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.openkm.api.OKMDocument;
import com.openkm.api.OKMRepository;
import com.openkm.automation.AutomationException;
import com.openkm.automation.AutomationManager;
import com.openkm.automation.AutomationUtils;
import com.openkm.bean.Document;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.ConversionException;
import com.openkm.core.DatabaseException;
import com.openkm.core.MimeTypeConfig;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.bean.AutomationRule;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.module.db.DbDocumentModule;
import com.openkm.module.jcr.JcrDocumentModule;
import com.openkm.util.DocConverter;
import com.openkm.util.FileUtils;
import com.openkm.util.PathUtils;
import com.openkm.util.WebUtils;

/**
 * Document converter service
 */
public class ConverterServlet extends OKMHttpServlet {
    private static Logger log = LoggerFactory.getLogger(ConverterServlet.class);

    private static final long serialVersionUID = 1L;

    public static final String FILE_CONVERTER_STATUS = "file_converter_status";

    @Override
    protected void service(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        log.debug("service({}, {})", request, response);
        request.setCharacterEncoding("UTF-8");
        final String uuid = WebUtils.getString(request, "uuid");
        final boolean inline = WebUtils.getBoolean(request, "inline");
        final boolean toPdf = WebUtils.getBoolean(request, "toPdf");
        final boolean toSwf = WebUtils.getBoolean(request, "toSwf");
        final CharsetDetector detector = new CharsetDetector();
        File tmp = null;
        InputStream is = null;
        final ConverterListener listener = new ConverterListener(
                ConverterListener.STATUS_LOADING);
        updateSessionManager(request);

        try {
            // Now an document can be located by UUID
            if (!uuid.equals("")) {
                // Saving listener to session
                request.getSession().setAttribute(FILE_CONVERTER_STATUS,
                        listener);

                final String path = OKMRepository.getInstance().getNodePath(
                        null, uuid);
                final Document doc = OKMDocument.getInstance().getProperties(
                        null, path);
                final String fileName = PathUtils.getName(doc.getPath());

                // Save content to temporary file
                tmp = File.createTempFile("okm",
                        "." + FileUtils.getFileExtension(fileName));

                if (Config.REPOSITORY_NATIVE) {
                    // If is used to preview, it should workaround the DOWNLOAD extended permission.
                    is = new DbDocumentModule().getContent(null, path, false,
                            !toSwf);
                } else {
                    is = new JcrDocumentModule().getContent(null, path, false);
                }

                // Text files may need encoding conversion
                if (doc.getMimeType().startsWith("text/")) {
                    detector.setText(new BufferedInputStream(is));
                    final CharsetMatch cm = detector.detect();
                    final Reader rd = cm.getReader();

                    FileUtils.copy(rd, tmp);
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(rd);
                } else {
                    FileUtils.copy(is, tmp);
                    IOUtils.closeQuietly(is);
                }

                // Prepare conversion
                final ConversionData cd = new ConversionData();
                cd.uuid = uuid;
                cd.fileName = fileName;
                cd.mimeType = doc.getMimeType();
                cd.file = tmp;

                if (toPdf && !cd.mimeType.equals(MimeTypeConfig.MIME_PDF)) {
                    try {
                        listener.setStatus(ConverterListener.STATUS_CONVERTING_TO_PDF);
                        toPDF(cd);
                        listener.setStatus(ConverterListener.STATUS_CONVERTING_TO_PDF_FINISHED);
                    } catch (final ConversionException e) {
                        log.error(e.getMessage(), e);
                        listener.setError(e.getMessage());
                        InputStream tis = null;
                        try {
                            tis = ConverterServlet.class
                                    .getResourceAsStream("conversion_problem.pdf");
                            FileUtils.copy(tis, cd.file);
                        } finally {
                            IOUtils.closeQuietly(tis);
                        }
                    }
                } else if (toSwf
                        && !cd.mimeType.equals(MimeTypeConfig.MIME_SWF)) {
                    try {
                        listener.setStatus(ConverterListener.STATUS_CONVERTING_TO_SWF);
                        toSWF(cd);
                        listener.setStatus(ConverterListener.STATUS_CONVERTING_TO_SWF_FINISHED);
                    } catch (final ConversionException e) {
                        log.error(e.getMessage(), e);
                        listener.setError(e.getMessage());
                        InputStream tis = null;
                        try {
                            tis = ConverterServlet.class
                                    .getResourceAsStream("conversion_problem.swf");
                            FileUtils.copy(tis, cd.file);
                        } finally {
                            IOUtils.closeQuietly(tis);
                        }
                    }
                }

                // Send back converted document
                listener.setStatus(ConverterListener.STATUS_SENDING_FILE);
                WebUtils.sendFile(request, response, cd.fileName, cd.mimeType,
                        inline, cd.file);
            } else {
                log.error("Missing Conversion Parameters");
                response.setContentType(MimeTypeConfig.MIME_TEXT);
                final PrintWriter out = response.getWriter();
                out.print("Missing Conversion Parameters");
                out.flush();
                out.close();
            }
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage()));
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage()));
        } catch (final RepositoryException e) {
            log.warn(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService,
                    ErrorCode.CAUSE_Repository), e.getMessage()));
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService, ErrorCode.CAUSE_IO),
                    e.getMessage()));
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService,
                    ErrorCode.CAUSE_Database), e.getMessage()));
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            listener.setError(e.getMessage());
            throw new ServletException(new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMDownloadService,
                    ErrorCode.CAUSE_General), e.getMessage()));
        } finally {
            listener.setConversionFinish(true);
            org.apache.commons.io.FileUtils.deleteQuietly(tmp);
        }

        log.debug("service: void");
    }

    /**
     * Handles PDF conversion
     */
    private void toPDF(final ConversionData cd) throws ConversionException,
            AutomationException, DatabaseException, IOException {
        log.debug("toPDF({})", cd);
        final File pdfCache = new File(Config.REPOSITORY_CACHE_PDF
                + File.separator + cd.uuid + ".pdf");
        removeExpiredCache(pdfCache);

        if (DocConverter.getInstance().convertibleToPdf(cd.mimeType)) {
            if (!pdfCache.exists()) {
                try {
                    if (cd.mimeType.equals(MimeTypeConfig.MIME_PDF)) {
                        // Document already in PDF format
                    } else if (cd.mimeType.equals(MimeTypeConfig.MIME_ZIP)) {
                        // This is an internal conversion and does not need 3er party software
                        DocConverter.getInstance().zip2pdf(cd.file, pdfCache);
                    } else if (cd.mimeType.equals(MimeTypeConfig.MIME_HTML)) {
                        // This is an internal conversion and does not need 3er party software
                        DocConverter.getInstance().html2pdf(cd.file, pdfCache);
                    } else if (cd.mimeType
                            .equals(MimeTypeConfig.MIME_POSTSCRIPT)) {
                        DocConverter.getInstance().ps2pdf(cd.file, pdfCache);
                    } else if (DocConverter.validImageMagick
                            .contains(cd.mimeType)) {
                        DocConverter.getInstance().img2pdf(cd.file,
                                cd.mimeType, pdfCache);
                    } else if (DocConverter.validOpenOffice
                            .contains(cd.mimeType)) {
                        DocConverter.getInstance().doc2pdf(cd.file,
                                cd.mimeType, pdfCache);
                    } else {
                        throw new NotImplementedException("Conversion from '"
                                + cd.mimeType + "' to PDF not available");
                    }

                    // AUTOMATION - POST
                    final Map<String, Object> env = new HashMap<String, Object>();
                    env.put(AutomationUtils.DOCUMENT_FILE, pdfCache);
                    env.put(AutomationUtils.DOCUMENT_UUID, cd.uuid);
                    AutomationManager.getInstance().fireEvent(
                            AutomationRule.EVENT_CONVERSION_PDF,
                            AutomationRule.AT_POST, env);
                } catch (final ConversionException e) {
                    pdfCache.delete();
                    cd.file = pdfCache;
                    throw e;
                } finally {
                    FileUtils.deleteQuietly(cd.file);
                    cd.mimeType = MimeTypeConfig.MIME_PDF;
                    cd.fileName = FileUtils.getFileName(cd.fileName) + ".pdf";
                }
            }

            if (pdfCache.exists()) {
                cd.file = pdfCache;
            }
            cd.mimeType = MimeTypeConfig.MIME_PDF;
            cd.fileName = FileUtils.getFileName(cd.fileName) + ".pdf";
        } else {
            throw new NotImplementedException("Conversion from '" + cd.mimeType
                    + "' to PDF not available");
        }

        log.debug("toPDF: {}", cd);
    }

    /**
     * Handles SWF conversion 
     */
    private void toSWF(final ConversionData cd) throws ConversionException,
            AutomationException, DatabaseException, IOException {
        log.debug("toSWF({})", cd);
        final File swfCache = new File(Config.REPOSITORY_CACHE_SWF
                + File.separator + cd.uuid + ".swf");
        removeExpiredCache(swfCache);

        if (DocConverter.getInstance().convertibleToSwf(cd.mimeType)) {
            if (!swfCache.exists()) {
                try {
                    if (cd.mimeType.equals(MimeTypeConfig.MIME_SWF)) {
                        // Document already in SWF format
                    } else if (cd.mimeType.equals(MimeTypeConfig.MIME_PDF)) {
                        // AUTOMATION - PRE
                        final Map<String, Object> env = new HashMap<String, Object>();
                        env.put(AutomationUtils.DOCUMENT_FILE, cd.file);
                        env.put(AutomationUtils.DOCUMENT_UUID, cd.uuid);
                        AutomationManager.getInstance().fireEvent(
                                AutomationRule.EVENT_CONVERSION_SWF,
                                AutomationRule.AT_PRE, env);

                        DocConverter.getInstance().pdf2swf(cd.file, swfCache);
                    } else if (DocConverter.getInstance().convertibleToPdf(
                            cd.mimeType)) {
                        toPDF(cd);

                        // AUTOMATION - PRE
                        final Map<String, Object> env = new HashMap<String, Object>();
                        env.put(AutomationUtils.DOCUMENT_FILE, cd.file);
                        env.put(AutomationUtils.DOCUMENT_UUID, cd.uuid);
                        AutomationManager.getInstance().fireEvent(
                                AutomationRule.EVENT_CONVERSION_SWF,
                                AutomationRule.AT_PRE, env);

                        DocConverter.getInstance().pdf2swf(cd.file, swfCache);
                    } else {
                        throw new NotImplementedException("Conversion from '"
                                + cd.mimeType + "' to SWF not available");
                    }
                } catch (final ConversionException e) {
                    swfCache.delete();
                    cd.file = swfCache;
                    throw e;
                } finally {
                    FileUtils.deleteQuietly(cd.file);
                    cd.mimeType = MimeTypeConfig.MIME_SWF;
                    cd.fileName = FileUtils.getFileName(cd.fileName) + ".swf";
                }
            }

            if (swfCache.exists()) {
                cd.file = swfCache;
            }
            cd.mimeType = MimeTypeConfig.MIME_SWF;
            cd.fileName = FileUtils.getFileName(cd.fileName) + ".swf";
        } else {
            throw new NotImplementedException("Conversion from '" + cd.mimeType
                    + "' to SWF not available");
        }

        log.debug("toSWF: {}", cd);
    }

    /**
     * For internal use only.
     */
    private class ConversionData {
        private String uuid;

        private String fileName;

        private String mimeType;

        private File file;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("uuid=");
            sb.append(uuid);
            sb.append(", fileName=");
            sb.append(fileName);
            sb.append(", mimeType=");
            sb.append(mimeType);
            sb.append(", file=");
            sb.append(file);
            sb.append("}");
            return sb.toString();
        }
    }

    private void removeExpiredCache(final File file) {
        if (file != null && file.exists()
                && System.currentTimeMillis() - file.lastModified() > 60 * 1000) {
            file.delete();
        }
    }
}
