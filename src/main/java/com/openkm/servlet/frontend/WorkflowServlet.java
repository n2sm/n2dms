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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMWorkflow;
import com.openkm.bean.form.FormElement;
import com.openkm.bean.workflow.ProcessDefinition;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.bean.workflow.TaskInstance;
import com.openkm.core.DatabaseException;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.core.WorkflowException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTProcessDefinition;
import com.openkm.frontend.client.bean.GWTProcessInstance;
import com.openkm.frontend.client.bean.GWTProcessInstanceLogEntry;
import com.openkm.frontend.client.bean.GWTTaskInstance;
import com.openkm.frontend.client.bean.form.GWTFormElement;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMWorkflowService;
import com.openkm.principal.PrincipalAdapterException;
import com.openkm.util.GWTUtil;
import com.openkm.util.WorkflowUtils;
import com.openkm.util.WorkflowUtils.ProcessInstanceLogEntry;

/**
 * Servlet Class
 */
public class WorkflowServlet extends OKMRemoteServiceServlet implements
        OKMWorkflowService {
    private static Logger log = LoggerFactory.getLogger(WorkflowServlet.class);

    private static final long serialVersionUID = 1L;

    @Override
    public List<GWTProcessDefinition> findLatestProcessDefinitions()
            throws OKMException {
        log.debug("findLatestProcessDefinitions()");
        final List<GWTProcessDefinition> processDefinitionList = new ArrayList<GWTProcessDefinition>();
        updateSessionManager();

        try {
            for (final ProcessDefinition processDefinition : OKMWorkflow
                    .getInstance().findLatestProcessDefinitions(null)) {
                processDefinitionList.add(GWTUtil.copy(processDefinition));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("findLatestProcessDefinitions: {}", processDefinitionList);
        return processDefinitionList;
    }

    @Override
    public void runProcessDefinition(final String UUID, final String name,
            final List<GWTFormElement> formElements) throws OKMException {
        log.debug("runProcessDefinition()");
        updateSessionManager();

        try {
            final List<FormElement> formElementList = new ArrayList<FormElement>();

            for (final GWTFormElement gwtFormElement : formElements) {
                formElementList.add(GWTUtil.copy(gwtFormElement));
            }

            final ProcessDefinition pd = OKMWorkflow.getInstance()
                    .findLastProcessDefinition(null, name);
            OKMWorkflow.getInstance().runProcessDefinition(null, pd.getId(),
                    UUID, formElementList);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("runProcessDefinition: void");
    }

    @Override
    public List<GWTTaskInstance> findUserTaskInstances() throws OKMException {
        log.debug("findUserTaskInstances()");
        final List<GWTTaskInstance> taskInstances = new ArrayList<GWTTaskInstance>();
        updateSessionManager();

        try {
            for (final TaskInstance taskInstance : OKMWorkflow.getInstance()
                    .findUserTaskInstances(null)) {
                taskInstances.add(GWTUtil.copy(taskInstance));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("findUserTaskInstances: {}", taskInstances);
        return taskInstances;
    }

    @Override
    public List<GWTTaskInstance> findPooledTaskInstances() throws OKMException {
        log.debug("findPooledTaskInstances()");
        final List<GWTTaskInstance> taskInstances = new ArrayList<GWTTaskInstance>();
        updateSessionManager();

        try {
            for (final TaskInstance taskInstance : OKMWorkflow.getInstance()
                    .findPooledTaskInstances(null)) {
                taskInstances.add(GWTUtil.copy(taskInstance));
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("findPooledTaskInstances: {}", taskInstances);
        return taskInstances;
    }

    @Override
    public Map<String, List<GWTFormElement>> getProcessDefinitionForms(
            final double id) throws OKMException {
        log.debug("getProcessDefinitionForms()");
        final Map<String, List<GWTFormElement>> formElementList = new HashMap<String, List<GWTFormElement>>();
        updateSessionManager();

        try {
            final Map<String, List<FormElement>> list = OKMWorkflow
                    .getInstance().getProcessDefinitionForms(null,
                            new Double(id).longValue());

            for (final String key : list.keySet()) {
                final List<FormElement> col = list.get(key);
                final List<GWTFormElement> gwtCol = new ArrayList<GWTFormElement>();

                for (final FormElement formElement : col) {
                    gwtCol.add(GWTUtil.copy(formElement));
                }

                formElementList.put(key, gwtCol);
            }
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMWorkflowService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getProcessDefinitionForms: {}", formElementList);
        return formElementList;
    }

    @Override
    public Map<String, List<GWTFormElement>> getProcessDefinitionFormsByName(
            final String name) throws OKMException {
        log.debug("getProcessDefinitionFormsByName()");
        final Map<String, List<GWTFormElement>> formElementList = new HashMap<String, List<GWTFormElement>>();
        updateSessionManager();

        try {
            final ProcessDefinition pd = OKMWorkflow.getInstance()
                    .findLastProcessDefinition(null, name);
            final Map<String, List<FormElement>> list = OKMWorkflow
                    .getInstance().getProcessDefinitionForms(null, pd.getId());

            for (final String key : list.keySet()) {
                final List<FormElement> col = list.get(key);
                final List<GWTFormElement> gwtCol = new ArrayList<GWTFormElement>();

                for (final FormElement formElement : col) {
                    gwtCol.add(GWTUtil.copy(formElement));
                }

                formElementList.put(key, gwtCol);
            }
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMWorkflowService,
                            ErrorCode.CAUSE_Parse), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getProcessDefinitionFormsByName: {}", formElementList);
        return formElementList;
    }

    @Override
    public void setTaskInstanceValues(final double id,
            final String transitionName, final List<GWTFormElement> formElements)
            throws OKMException {
        log.debug("setTaskInstanceValues()");
        updateSessionManager();

        try {
            final List<FormElement> formElementList = new ArrayList<FormElement>();

            for (final GWTFormElement gwtFormElement : formElements) {
                formElementList.add(GWTUtil.copy(gwtFormElement));
            }

            OKMWorkflow.getInstance()
                    .setTaskInstanceValues(null, new Double(id).longValue(),
                            transitionName, formElementList);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("setTaskInstanceValues: void");
    }

    @Override
    public void addComment(final double tokenId, final String message)
            throws OKMException {
        log.debug("addComment({}, {})", tokenId, message);
        updateSessionManager();

        try {
            OKMWorkflow.getInstance().addTokenComment(null,
                    new Double(tokenId).longValue(), message);
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("addComment: void");
    }

    @Override
    public void setTaskInstanceActorId(final double id) throws OKMException {
        log.debug("setTaskInstanceActorId({})", id);
        updateSessionManager();

        try {
            OKMWorkflow.getInstance().setTaskInstanceActorId(null,
                    new Double(id).longValue(),
                    getThreadLocalRequest().getRemoteUser());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("setTaskInstanceActorId: void");
    }

    @Override
    public void startTaskInstance(final double id) throws OKMException {
        log.debug("startTaskInstance({})", id);
        updateSessionManager();

        try {
            final OKMWorkflow okmWorkflow = OKMWorkflow.getInstance();
            final long taskInstanceId = new Double(id).longValue();
            final TaskInstance ti = okmWorkflow.getTaskInstance(null,
                    taskInstanceId);

            if (ti.getStart() == null) {
                okmWorkflow.startTaskInstance(null, taskInstanceId);
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("startTaskInstance: void");
    }

    @Override
    public List<GWTProcessInstance> findProcessInstancesByNode(final String uuid)
            throws OKMException {
        log.debug("findProcessInstancesByNode({})", uuid);
        final List<GWTProcessInstance> processInstanceList = new ArrayList<GWTProcessInstance>();
        try {
            for (final ProcessInstance processInstance : WorkflowUtils
                    .findProcessInstancesByNode(uuid)) {
                processInstanceList.add(GWTUtil.copy(processInstance));
            }
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final PrincipalAdapterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMessageService,
                    ErrorCode.CAUSE_PrincipalAdapter), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMessageService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMessageService, ErrorCode.CAUSE_Parse),
                    e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMessageService,
                    ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        }

        log.debug("findProcessInstancesByNode: processInstanceList");
        return processInstanceList;
    }

    @Override
    public List<GWTProcessInstanceLogEntry> findLogsByProcessInstance(
            final int processInstanceId) throws OKMException {
        final List<GWTProcessInstanceLogEntry> instanceLogEntryList = new ArrayList<GWTProcessInstanceLogEntry>();
        try {
            for (final ProcessInstanceLogEntry instanceLogEntry : WorkflowUtils
                    .findLogsByProcessInstance(processInstanceId)) {
                instanceLogEntryList.add(GWTUtil.copy(instanceLogEntry));
            }
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        }
        return instanceLogEntryList;
    }

    @Override
    public GWTTaskInstance getUserTaskInstance(final long taskInstanceId)
            throws OKMException {
        log.debug("getUserTaskInstance(taskInstanceId={})", taskInstanceId);
        updateSessionManager();

        try {
            for (final TaskInstance taskInstance : OKMWorkflow.getInstance()
                    .findUserTaskInstances(null)) {
                if (taskInstance.getId() == taskInstanceId) {
                    log.debug("getUserTaskInstance: " + taskInstance);
                    return GWTUtil.copy(OKMWorkflow.getInstance()
                            .getTaskInstance(null, taskInstanceId));
                }
            }
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Database), e.getMessage());
        } catch (final WorkflowException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_Workflow), e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMWorkflowService,
                    ErrorCode.CAUSE_General), e.getMessage());
        }

        log.debug("getUserTaskInstance: null");
        return null;
    }
}
