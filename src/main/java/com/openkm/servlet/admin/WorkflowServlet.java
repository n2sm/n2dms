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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMAuth;
import com.openkm.api.OKMDocument;
import com.openkm.api.OKMWorkflow;
import com.openkm.bean.form.FormElement;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.bean.workflow.TaskInstance;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.RepositoryException;
import com.openkm.core.WorkflowException;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.util.FormUtils;
import com.openkm.util.FormatUtil;
import com.openkm.util.UserActivity;
import com.openkm.util.WebUtils;

/**
 * RepositoryView servlet
 */
public class WorkflowServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(WorkflowServlet.class);

    private static Map<String, String> statusFilterValues = new LinkedHashMap<String, String>();

    static {
        statusFilterValues.put("0", "All");
        statusFilterValues.put("1", "Running");
        statusFilterValues.put("2", "Ended");
    }

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
            if (action.equals("processDefinitionDelete")) {
                processDefinitionDelete(userId, request, response);
                processDefinitionList(userId, request, response);
            } else if (action.equals("processDefinitionView")) {
                processDefinitionView(userId, request, response);
            } else if (action.equals("processInstanceView")) {
                processInstanceView(userId, request, response);
            } else if (action.equals("processInstanceDelete")) {
                processInstanceDelete(userId, request, response);
                processDefinitionView(userId, request, response);
            } else if (action.equals("processInstanceEnd")) {
                processInstanceEnd(userId, request, response);
                processDefinitionView(userId, request, response);
            } else if (action.equals("processInstanceResume")) {
                processInstanceResume(userId, request, response);
                processDefinitionView(userId, request, response);
            } else if (action.equals("processInstanceSuspend")) {
                processInstanceSuspend(userId, request, response);
                processDefinitionView(userId, request, response);
            } else if (action.equals("processInstanceVariableDelete")) {
                processInstanceVariableDelete(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("processInstanceVariableAdd")) {
                processInstanceVariableAdd(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceSetActor")) {
                taskInstanceSetActor(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceSuspend")) {
                taskInstanceSuspend(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceResume")) {
                taskInstanceResume(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceStart")) {
                taskInstanceStart(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceEnd")) {
                taskInstanceEnd(userId, request, response);
                if (request.getParameter("transition") == null) {
                    processInstanceView(userId, request, response);
                } else {
                    taskInstanceView(userId, request, response);
                }
            } else if (action.equals("taskInstanceView")) {
                taskInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceAddComment")) {
                taskInstanceAddComment(userId, request, response);
                taskInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceVariableDelete")) {
                taskInstanceVariableDelete(userId, request, response);
                taskInstanceView(userId, request, response);
            } else if (action.equals("taskInstanceVariableAdd")) {
                taskInstanceVariableAdd(userId, request, response);
                taskInstanceView(userId, request, response);
            } else if (action.equals("tokenView")) {
                tokenView(userId, request, response);
            } else if (action.equals("tokenEnd")) {
                tokenEnd(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("tokenSuspend")) {
                tokenSuspend(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("tokenResume")) {
                tokenResume(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("processInstanceAddComment")) {
                processInstanceAddComment(userId, request, response);
                processInstanceView(userId, request, response);
            } else if (action.equals("tokenSignal")) {
                tokenSignal(userId, request, response);
                tokenView(userId, request, response);
            } else if (action.equals("tokenSetNode")) {
                tokenSetNode(userId, request, response);
                tokenView(userId, request, response);
            } else {
                processDefinitionList(userId, request, response);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        } catch (final AccessDeniedException e) {
            log.error(e.getMessage(), e);
            sendErrorRedirect(request, response, e);
        }
    }

    /**
     * List all process definitions
     */
    private void processDefinitionList(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException,
            com.openkm.core.RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("listProcessDefinition({}, {}, {})", new Object[] { userId,
                request, response });
        final ServletContext sc = getServletContext();
        sc.setAttribute("processDefinitions", OKMWorkflow.getInstance()
                .findAllProcessDefinitions(null));
        sc.getRequestDispatcher("/admin/process_definition_list.jsp").forward(
                request, response);
        log.debug("listProcessDefinition: void");
    }

    /**
     * Delete a process definition
     */
    private void processDefinitionDelete(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessDefinition({}, {}, {})", new Object[] { userId,
                request, response });
        final long pdid = WebUtils.getLong(request, "pdid");
        OKMWorkflow.getInstance().deleteProcessDefinition(null, pdid);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_DEFINITION_DELETE",
                Long.toString(pdid), null, null);
        log.debug("deleteProcessDefinition: void");
    }

    /**
     * View process definition
     */
    private void processDefinitionView(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, RepositoryException,
            DatabaseException, WorkflowException, ParseException {
        log.debug("viewProcessDefinition({}, {}, {})", new Object[] { userId,
                request, response });
        final ServletContext sc = getServletContext();
        final long pdid = WebUtils.getLong(request, "pdid");
        final int statusFilter = WebUtils.getInt(request, "statusFilter", 1);
        final Map<String, List<FormElement>> procDefForms = OKMWorkflow
                .getInstance().getProcessDefinitionForms(null, pdid);
        final Map<String, List<Map<String, String>>> pdf = new HashMap<String, List<Map<String, String>>>();

        for (final String key : procDefForms.keySet()) {
            final List<Map<String, String>> value = new ArrayList<Map<String, String>>();

            for (final FormElement fe : procDefForms.get(key)) {
                value.add(FormUtils.toString(fe));
            }

            pdf.put(key, value);
        }

        // Filter process instances by status
        final List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();

        for (final ProcessInstance pi : OKMWorkflow.getInstance()
                .findProcessInstances(null, pdid)) {
            if (statusFilter == 1) { // Running
                if (pi.getEnd() == null && !pi.isSuspended()) {
                    processInstances.add(pi);
                }
            } else if (statusFilter == 2) { // Ended
                if (pi.getEnd() != null && !pi.isSuspended()) {
                    processInstances.add(pi);
                }
            } else { // All
                processInstances.add(pi);
            }
        }

        sc.setAttribute("processDefinition", OKMWorkflow.getInstance()
                .getProcessDefinition(null, pdid));
        sc.setAttribute("processInstances", processInstances);
        sc.setAttribute("processDefinitionForms", pdf);
        sc.setAttribute("statusFilterValues", statusFilterValues);
        sc.setAttribute("statusFilter", statusFilter);
        sc.getRequestDispatcher("/admin/process_definition_view.jsp").forward(
                request, response);
        log.debug("viewProcessDefinition: void");
    }

    /**
     * View process instance
     */
    private void processInstanceView(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, RepositoryException,
            DatabaseException, WorkflowException, PrincipalAdapterException,
            AccessDeniedException {
        log.debug("processInstanceView({}, {}, {})", new Object[] { userId,
                request, response });
        final ServletContext sc = getServletContext();
        final long piid = WebUtils.getLong(request, "piid");
        final ProcessInstance pi = OKMWorkflow.getInstance()
                .getProcessInstance(null, piid);
        final Map<String, String> vars = new HashMap<String, String>();

        for (final Entry<String, Object> entry : pi.getVariables().entrySet()) {
            vars.put(entry.getKey(), FormatUtil.formatObject(entry.getValue()));

            if (entry.getKey().equals(
                    Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_UUID)) {
                String path = null;

                try {
                    path = OKMDocument.getInstance().getPath(null,
                            entry.getValue().toString());
                } catch (final RepositoryException e) {
                    path = "PathNotFoundException";
                }

                vars.put(Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_PATH, path);
            }
        }

        sc.setAttribute("variables", vars);
        sc.setAttribute("processInstance", pi);
        sc.setAttribute("taskInstances", OKMWorkflow.getInstance()
                .findTaskInstances(null, piid));
        sc.setAttribute("users", OKMAuth.getInstance().getUsers(null));
        sc.getRequestDispatcher("/admin/process_instance_view.jsp").forward(
                request, response);
        log.debug("processInstanceView: void");
    }

    /**
     * Delete process instance
     */
    private void processInstanceDelete(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceDelete({}, {}, {})", new Object[] { userId,
                request, response });
        final long piid = WebUtils.getLong(request, "piid");
        OKMWorkflow.getInstance().deleteProcessInstance(null, piid);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_DELETE",
                Long.toString(piid), null, null);
        log.debug("processInstanceDelete: void");
    }

    /**
     * End process instance
     */
    private void processInstanceEnd(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceEnd({}, {}, {})", new Object[] { userId,
                request, response });
        final long piid = WebUtils.getLong(request, "piid");
        OKMWorkflow.getInstance().endProcessInstance(null, piid);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_END",
                Long.toString(piid), null, null);
        log.debug("processInstanceEnd: void");
    }

    /**
     * Resume process instance
     */
    private void processInstanceResume(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceResume({}, {}, {})", new Object[] { userId,
                request, response });
        final long piid = WebUtils.getLong(request, "piid");
        OKMWorkflow.getInstance().resumeProcessInstance(null, piid);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_RESUME",
                Long.toString(piid), null, null);
        log.debug("processInstanceResume: void");
    }

    /**
     * Suspend process instance
     */
    private void processInstanceSuspend(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceSuspend({}, {}, {})", new Object[] { userId,
                request, response });
        final long piid = WebUtils.getLong(request, "piid");
        OKMWorkflow.getInstance().suspendProcessInstance(null, piid);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_SUSPEND",
                Long.toString(piid), null, null);
        log.debug("processInstanceSuspend: void");
    }

    /**
     * Add comment to process instance
     */
    private void processInstanceAddComment(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws DatabaseException, WorkflowException, RepositoryException {
        log.debug("processInstanceAddComment({}, {}, {})", new Object[] {
                userId, request, response });
        final long tid = WebUtils.getLong(request, "tid");
        final String message = WebUtils.getString(request, "message");

        if (!message.equals("")) {
            OKMWorkflow.getInstance().addTokenComment(null, tid, message);
        }

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_ADD_COMMENT",
                Long.toString(tid), null, null);
        log.debug("processInstanceAddComment: void");
    }

    /**
     * Delete process instance variable
     */
    private void processInstanceVariableDelete(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceVariableDelete({}, {}, {})", new Object[] {
                userId, request, response });
        final long piid = WebUtils.getLong(request, "piid");
        final String name = WebUtils.getString(request, "name");
        OKMWorkflow.getInstance().deleteProcessInstanceVariable(null, piid,
                name);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_VARIABLE_DELETE",
                Long.toString(piid), null, null);
        log.debug("processInstanceVariableDelete: void");
    }

    /**
     * Add process instance variable
     */
    private void processInstanceVariableAdd(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("processInstanceVariableAdd({}, {}, {})", new Object[] {
                userId, request, response });
        final long piid = WebUtils.getLong(request, "piid");
        final String name = WebUtils.getString(request, "name");
        final String value = WebUtils.getString(request, "value");
        OKMWorkflow.getInstance().addProcessInstanceVariable(null, piid, name,
                value);

        // Activity log
        UserActivity.log(userId, "ADMIN_PROCESS_INSTANCE_VARIABLE_ADD",
                Long.toString(piid), null, name + "=" + value);
        log.debug("processInstanceVariableAdd: void");
    }

    /**
     * Set task instance actor
     */
    private void taskInstanceSetActor(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceSetActor({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        final String actor = WebUtils.getString(request, "actor");
        OKMWorkflow.getInstance().setTaskInstanceActorId(null, tiid, actor);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_SET_ACTOR",
                Long.toString(tiid), null, actor);
        log.debug("taskInstanceSetActor: void");
    }

    /**
     * View task instance
     */
    private void taskInstanceView(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, RepositoryException,
            DatabaseException, WorkflowException, ParseException,
            AccessDeniedException {
        log.debug("taskInstanceView({}, {}, {})", new Object[] { userId,
                request, response });
        final ServletContext sc = getServletContext();
        final long tiid = WebUtils.getLong(request, "tiid");
        final TaskInstance ti = OKMWorkflow.getInstance().getTaskInstance(null,
                tiid);
        final Map<String, List<FormElement>> procDefForms = OKMWorkflow
                .getInstance().getProcessDefinitionForms(null,
                        ti.getProcessInstance().getProcessDefinition().getId());
        final List<Map<String, String>> pdf = new ArrayList<Map<String, String>>();
        final Map<String, String> vars = new HashMap<String, String>();
        final List<FormElement> fes = procDefForms.get(ti.getName());

        if (fes != null) {
            for (final FormElement fe : fes) {
                pdf.add(FormUtils.toString(fe));
            }
        }

        for (final Entry<String, Object> entry : ti.getVariables().entrySet()) {
            vars.put(entry.getKey(), FormatUtil.formatObject(entry.getValue()));

            if (entry.getKey().equals(
                    Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_UUID)) {
                vars.put(
                        Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_PATH,
                        OKMDocument.getInstance().getPath(null,
                                entry.getValue().toString()));
            }
        }

        sc.setAttribute("variables", vars);
        sc.setAttribute("taskInstance", ti);
        sc.setAttribute("taskInstanceForm", pdf);
        sc.getRequestDispatcher("/admin/task_instance_view.jsp").forward(
                request, response);
        log.debug("taskInstanceView: void");
    }

    /**
     * Start task instance
     */
    private void taskInstanceStart(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceStart({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        OKMWorkflow.getInstance().startTaskInstance(null, tiid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_START",
                Long.toString(tiid), null, null);
        log.debug("taskInstanceStart: void");
    }

    /**
     * End task instance
     */
    private void taskInstanceEnd(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceEnd({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        final String transition = WebUtils.getString(request, "transition",
                null);
        OKMWorkflow.getInstance().endTaskInstance(null, tiid, transition);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_END",
                Long.toString(tiid), null, null);
        log.debug("taskInstanceEnd: void");
    }

    /**
     * Suspend task instance
     */
    private void taskInstanceSuspend(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceSuspend({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        OKMWorkflow.getInstance().suspendTaskInstance(null, tiid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_SUSPEND",
                Long.toString(tiid), null, null);
        log.debug("taskInstanceSuspend: void");
    }

    /**
     * Add comment to task instance
     */
    private void taskInstanceAddComment(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws DatabaseException, WorkflowException, RepositoryException {
        log.debug("processInstanceAddComment({}, {}, {})", new Object[] {
                userId, request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        final String message = WebUtils.getString(request, "message");

        if (!message.equals("")) {
            OKMWorkflow.getInstance().addTaskInstanceComment(null, tiid,
                    message);
        }

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_ADD_COMMENT",
                Long.toString(tiid), null, null);
        log.debug("processInstanceAddComment: void");
    }

    /**
     * Delete task instance variable
     */
    private void taskInstanceVariableDelete(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceVariableDelete({}, {}, {})", new Object[] {
                userId, request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        final String name = WebUtils.getString(request, "name");
        OKMWorkflow.getInstance().deleteTaskInstanceVariable(null, tiid, name);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_VARIABLE_DELETE",
                Long.toString(tiid), null, null);
        log.debug("taskInstanceVariableDelete: void");
    }

    /**
     * Add task instance variable
     */
    private void taskInstanceVariableAdd(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceVariableAdd({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        final String name = WebUtils.getString(request, "name");
        final String value = WebUtils.getString(request, "value");
        OKMWorkflow.getInstance().addTaskInstanceVariable(null, tiid, name,
                value);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_VARIABLE_ADD",
                Long.toString(tiid), null, name + "=" + value);
        log.debug("taskInstanceVariableAdd: void");
    }

    /**
     * Resume task instance
     */
    private void taskInstanceResume(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("taskInstanceResume({}, {}, {})", new Object[] { userId,
                request, response });
        final long tiid = WebUtils.getLong(request, "tiid");
        OKMWorkflow.getInstance().resumeTaskInstance(null, tiid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TASK_INSTANCE_RESUME",
                Long.toString(tiid), null, null);
        log.debug("taskInstanceResume: void");
    }

    /**
     * Suspend token
     */
    private void tokenSuspend(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("tokenSuspend({}, {}, {})", new Object[] { userId, request,
                response });
        final long tid = WebUtils.getLong(request, "tid");
        OKMWorkflow.getInstance().suspendToken(null, tid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TOKEN_SUSPEND", Long.toString(tid),
                null, null);
        log.debug("tokenSuspend: void");
    }

    /**
     * Resume token
     */
    private void tokenResume(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("tokenResume({}, {}, {})", new Object[] { userId, request,
                response });
        final long tid = WebUtils.getLong(request, "tid");
        OKMWorkflow.getInstance().resumeToken(null, tid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TOKEN_RESUME", Long.toString(tid),
                null, null);
        log.debug("tokenResume: void");
    }

    /**
     * End token
     */
    private void tokenEnd(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("tokenEnd({}, {}, {})", new Object[] { userId, request,
                response });
        final long tid = WebUtils.getLong(request, "tid");
        OKMWorkflow.getInstance().endToken(null, tid);

        // Activity log
        UserActivity.log(userId, "ADMIN_TOKEN_END", Long.toString(tid), null,
                null);
        log.debug("tokenEnd: void");
    }

    /**
     * Set token node
     */
    private void tokenSetNode(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws DatabaseException, WorkflowException, RepositoryException {
        log.debug("tokenSetNode({}, {}, {})", new Object[] { userId, request,
                response });
        final long tid = WebUtils.getLong(request, "tid");
        final String node = WebUtils.getString(request, "node");
        OKMWorkflow.getInstance().setTokenNode(null, tid, node);

        // Activity log
        UserActivity.log(userId, "ADMIN_TOKEN_SET_NODE", Long.toString(tid),
                null, node);
        log.debug("tokenSetNode: void");
    }

    /**
     * Send token signal
     */
    private void tokenSignal(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws DatabaseException, WorkflowException, RepositoryException {
        log.debug("tokenSignal({}, {}, {})", new Object[] { userId, request,
                response });
        final long tid = WebUtils.getLong(request, "tid");
        final String transition = WebUtils.getString(request, "transition");
        OKMWorkflow.getInstance().sendTokenSignal(null, tid, transition);

        // Activity log
        UserActivity.log(userId, "ADMIN_TOKEN_SIGNAL", Long.toString(tid),
                null, transition);
        log.debug("tokenSignal: void");
    }

    /**
     * View token
     */
    private void tokenView(final String userId,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("tokenView({}, {}, {})", new Object[] { userId, request,
                response });
        final ServletContext sc = getServletContext();
        final long tid = WebUtils.getLong(request, "tid");
        sc.setAttribute("token", OKMWorkflow.getInstance().getToken(null, tid));
        sc.getRequestDispatcher("/admin/token_view.jsp").forward(request,
                response);
        log.debug("tokenView: void");
    }
}
