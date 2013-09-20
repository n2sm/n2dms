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

package com.openkm.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMDocument;
import com.openkm.bean.Document;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.ConversionException;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.util.DocConverter;
import com.openkm.util.WebUtils;

/**
 * Only for enjoy
 */
public class TextToSpeechServlet extends HttpServlet {
    private static Logger log = LoggerFactory
            .getLogger(TextToSpeechServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException,
            ServletException {
        final String cmd = "espeak -v mb-es1 -f input.txt | mbrola -e /usr/share/mbrola/voices/es1 - -.wav "
                + "| oggenc -Q - -o output.ogg";
        final String text = WebUtils.getString(request, "text");
        final String docPath = WebUtils.getString(request, "docPath");
        response.setContentType("audio/ogg");
        FileInputStream fis = null;
        OutputStream os = null;

        try {
            if (!text.equals("")) {
                FileUtils.writeStringToFile(new File("input.txt"), text);
            } else if (!docPath.equals("")) {
                final InputStream is = OKMDocument.getInstance().getContent(
                        null, docPath, false);
                final Document doc = OKMDocument.getInstance().getProperties(
                        null, docPath);
                DocConverter.getInstance().doc2txt(is, doc.getMimeType(),
                        new File("input.txt"));
            }

            // Convert to voice
            final ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
            final Process process = pb.start();
            process.waitFor();
            final String info = IOUtils.toString(process.getInputStream());
            process.destroy();

            if (process.exitValue() == 1) {
                log.warn(info);
            }

            // Send to client
            os = response.getOutputStream();
            fis = new FileInputStream("output.ogg");
            IOUtils.copy(fis, os);
            os.flush();
        } catch (final InterruptedException e) {
            log.warn(e.getMessage(), e);
        } catch (final IOException e) {
            log.warn(e.getMessage(), e);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
        } catch (final RepositoryException e) {
            log.warn(e.getMessage(), e);
        } catch (final DatabaseException e) {
            log.warn(e.getMessage(), e);
        } catch (final ConversionException e) {
            log.warn(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(os);
        }
    }
}
