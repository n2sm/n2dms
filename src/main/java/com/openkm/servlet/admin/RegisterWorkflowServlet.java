package com.openkm.servlet.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMWorkflow;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.WorkflowException;
import com.openkm.util.UserActivity;

/**
 * Register workflow Servlet
 */
public class RegisterWorkflowServlet extends BaseServlet {
    private static Logger log = LoggerFactory
            .getLogger(RegisterWorkflowServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException,
            IOException {
        String fileName = null;
        byte[] content = null;
        PrintWriter out = null;
        updateSessionManager(request);

        try {
            final boolean isMultipart = ServletFileUpload
                    .isMultipartContent(request);
            out = response.getWriter();

            // Create a factory for disk-based file items
            if (isMultipart) {
                final FileItemFactory factory = new DiskFileItemFactory();
                final ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setHeaderEncoding("UTF-8");
                final List<FileItem> items = upload.parseRequest(request);

                // Parse the request and get all parameters and the uploaded file
                for (final FileItem item : items) {
                    if (!item.isFormField()) {
                        fileName = item.getName();
                        content = item.get();
                    }
                }

                if (fileName != null && !fileName.equals("")) {
                    fileName = FilenameUtils.getName(fileName);
                    log.debug("Upload file: {}", fileName);
                    final InputStream is = new ByteArrayInputStream(content);
                    OKMWorkflow.getInstance().registerProcessDefinition(null,
                            is);
                    is.close();
                }

                // Activity log
                UserActivity.log(request.getRemoteUser(),
                        "ADMIN_WORKFLOW_REGISTER", fileName, null, null);
                response.sendRedirect("Workflow");
            }
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } finally {
            out.flush();
            out.close();
        }
    }
}
