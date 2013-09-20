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

package com.openkm.module.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import com.openkm.bean.form.FormElement;
import com.openkm.bean.workflow.ProcessDefinition;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.bean.workflow.TaskInstance;
import com.openkm.bean.workflow.Token;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ParseException;
import com.openkm.core.RepositoryException;
import com.openkm.core.WorkflowException;
import com.openkm.module.WorkflowModule;
import com.openkm.module.common.CommonWorkflowModule;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.UserActivity;

public class DbWorkflowModule implements WorkflowModule {
    private static Logger log = LoggerFactory.getLogger(DbWorkflowModule.class);

    @Override
    public void registerProcessDefinition(final String token,
            final InputStream is) throws ParseException, RepositoryException,
            DatabaseException, WorkflowException, IOException {
        log.debug("registerProcessDefinition({}, {})", token, is);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.registerProcessDefinition(is);

            // Activity log
            UserActivity.log(auth.getName(), "REGISTER_PROCESS_DEFINITION",
                    null, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("registerProcessDefinition: void");
    }

    @Override
    public void deleteProcessDefinition(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("deleteProcessDefinition({}, {})", token, processDefinitionId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.deleteProcessDefinition(processDefinitionId);

            // Activity log
            UserActivity.log(auth.getName(), "DELETE_PROCESS_DEFINITION", ""
                    + processDefinitionId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("deleteProcessDefinition: void");
    }

    @Override
    public ProcessDefinition getProcessDefinition(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getProcessDefinition({}, {})", token, processDefinitionId);
        ProcessDefinition vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.getProcessDefinition(processDefinitionId);

            // Activity log
            UserActivity.log(auth.getName(), "GET_PROCESS_DEFINITION", ""
                    + processDefinitionId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getProcessDefinition: {}", vo);
        return vo;
    }

    @Override
    public byte[] getProcessDefinitionImage(final String token,
            final long processDefinitionId, final String node)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getProcessDefinitionImage({}, {}, {})", new Object[] {
                token, processDefinitionId, node });
        byte[] image = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            image = CommonWorkflowModule.getProcessDefinitionImage(
                    processDefinitionId, node);

            // Activity log
            UserActivity.log(auth.getName(), "GET_PROCESS_DEFINITION_IMAGE", ""
                    + processDefinitionId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getProcessDefinitionImage: {}", image);
        return image;
    }

    @Override
    public Map<String, List<FormElement>> getProcessDefinitionForms(
            final String token, final long processDefinitionId)
            throws ParseException, RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("getProcessDefinitionForms({}, {})", token,
                processDefinitionId);
        Map<String, List<FormElement>> forms = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            forms = CommonWorkflowModule
                    .getProcessDefinitionForms(processDefinitionId);

            // Activity log
            UserActivity.log(auth.getName(), "GET_PROCESS_DEFINITION_FORMS",
                    processDefinitionId + "", null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getProcessDefinitionForms: {}", forms);
        return forms;
    }

    @Override
    public ProcessInstance runProcessDefinition(final String token,
            final long processDefinitionId, final String uuid,
            final List<FormElement> variables) throws WorkflowException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("runProcessDefinition({}, {}, {})", new Object[] { token,
                processDefinitionId, variables });
        ProcessInstance vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.runProcessDefinition(auth.getName(),
                    processDefinitionId, uuid, variables);

            // Activity log
            UserActivity.log(auth.getName(), "RUN_PROCESS_DEFINITION", ""
                    + processDefinitionId, null, variables.toString());
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("runProcessDefinition: {}", vo);
        return vo;
    }

    @Override
    public ProcessInstance sendProcessInstanceSignal(final String token,
            final long processInstanceId, final String transitionName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("sendProcessInstanceSignal({}, {}, {})", new Object[] {
                token, processInstanceId, transitionName });
        ProcessInstance vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.sendProcessInstanceSignal(
                    processInstanceId, transitionName);

            // Activity log
            UserActivity.log(auth.getName(), "SEND_PROCESS_INSTANCE_SIGNAL", ""
                    + processInstanceId, null, transitionName);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("sendProcessInstanceSignal: {}", vo);
        return vo;
    }

    @Override
    public void endProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("endProcessInstance({}, {})", token, processInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.endProcessInstance(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "END_PROCESS_INSTANCE", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("endProcessInstance: void");
    }

    @Override
    public void deleteProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("deleteProcessInstance({}, {})", token, processInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.deleteProcessInstance(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "DELETE_PROCESS_INSTANCE", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("deleteProcessInstance: void");
    }

    @Override
    public List<ProcessInstance> findProcessInstances(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findProcessInstances({}, {})", token, processDefinitionId);
        List<ProcessInstance> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findProcessInstances(processDefinitionId);

            // Activity log
            UserActivity.log(auth.getName(), "FIND_PROCESS_INSTANCES", ""
                    + processDefinitionId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findProcessInstances: {}", al);
        return al;
    }

    @Override
    public List<ProcessDefinition> findAllProcessDefinitions(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitions({})", token);
        List<ProcessDefinition> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findAllProcessDefinitions();

            // Activity log
            UserActivity.log(auth.getName(), "FIND_ALL_PROCESS_DEFINITIONS",
                    null, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findAllProcessDefinitions: {}", al);
        return al;
    }

    @Override
    public List<ProcessDefinition> findLatestProcessDefinitions(
            final String token) throws RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("findLatestProcessDefinitions({})", token);
        List<ProcessDefinition> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findLatestProcessDefinitions();

            // Activity log
            UserActivity.log(auth.getName(), "FIND_LATEST_PROCESS_DEFINITIONS",
                    null, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findLatestProcessDefinitions: {}", al);
        return al;
    }

    @Override
    public ProcessDefinition findLastProcessDefinition(final String token,
            final String name) throws RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("findLastProcessDefinition({}, {})", token, name);
        ProcessDefinition pd = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            pd = CommonWorkflowModule.findLastProcessDefinition(name);

            // Activity log
            UserActivity.log(auth.getName(), "FIND_LAST_PROCESS_DEFINITION",
                    name, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findLastProcessDefinition: {}", pd);
        return pd;
    }

    @Override
    public List<ProcessDefinition> findAllProcessDefinitionVersions(
            final String token, final String name) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitionVersions({}, {})", token, name);
        List<ProcessDefinition> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findAllProcessDefinitionVersions(name);

            // Activity log
            UserActivity.log(auth.getName(),
                    "FIND_ALL_PROCESS_DEFINITION_VERSIONS", name, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findAllProcessDefinitionVersions: {}", al);
        return al;
    }

    @Override
    public ProcessInstance getProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getProcessInstance({}, {})", token, processInstanceId);
        ProcessInstance vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.getProcessInstance(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "GET_PROCESS_INSTANCE", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getProcessInstance: {}", vo);
        return vo;
    }

    @Override
    public void suspendProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("suspendProcessInstance({}, {})", token, processInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.suspendProcessInstance(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "SUSPEND_PROCESS_INSTANCE", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("suspendProcessInstance: void");
    }

    @Override
    public void resumeProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("resumeProcessInstance({}, {})", token, processInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.resumeProcessInstance(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "RESUME_PROCESS_INSTANCE", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("resumeProcessInstance: void");
    }

    @Override
    public void addProcessInstanceVariable(final String token,
            final long processInstanceId, final String name, final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addProcessInstanceVariable({}, {}, {}, {})", new Object[] {
                token, processInstanceId, name, value });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.addProcessInstanceVariable(processInstanceId,
                    name, value);

            // Activity log
            UserActivity.log(auth.getName(), "ADD_PROCESS_INSTANCE_VARIABLE",
                    "" + processInstanceId, null, name + ", " + value);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("addProcessInstanceVariable: void");
    }

    @Override
    public void deleteProcessInstanceVariable(final String token,
            final long processInstanceId, final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessInstanceVariable({}, {}, {})", new Object[] {
                token, processInstanceId, name });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.deleteProcessInstanceVariable(
                    processInstanceId, name);

            // Activity log
            UserActivity.log(auth.getName(),
                    "DELETE_PROCESS_INSTANCE_VARIABLE", "" + processInstanceId,
                    null, name);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("deleteProcessInstanceVariable: void");
    }

    @Override
    public List<TaskInstance> findUserTaskInstances(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findUserTaskInstances({})", token);
        List<TaskInstance> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findUserTaskInstances(auth.getName());

            // Activity log
            UserActivity.log(auth.getName(), "FIND_USER_TASK_INSTANCES", null,
                    null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findUserTaskInstances: {}", al);
        return al;
    }

    @Override
    public List<TaskInstance> findPooledTaskInstances(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findPooledTaskInstances({})", token);
        List<TaskInstance> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findPooledTaskInstances(auth.getName());

            // Activity log
            UserActivity.log(auth.getName(), "FIND_POOLED_TASK_INSTANCES",
                    null, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findPooledTaskInstances: {}", al);
        return al;
    }

    @Override
    public List<TaskInstance> findTaskInstances(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findTaskInstances({}, {})", token, processInstanceId);
        List<TaskInstance> al = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            al = CommonWorkflowModule.findTaskInstances(processInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "FIND_TASK_INSTANCES", ""
                    + processInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("findTaskInstances: {}", al);
        return al;
    }

    @Override
    public void setTaskInstanceValues(final String token,
            final long taskInstanceId, final String transitionName,
            final List<FormElement> values) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("setTaskInstanceValues({}, {}, {}, {})", new Object[] {
                token, taskInstanceId, transitionName, values });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.setTaskInstanceValues(taskInstanceId,
                    transitionName, values);

            // Activity log
            UserActivity.log(auth.getName(), "SET_TASK_INSTANCE_VALUES", ""
                    + taskInstanceId, null, transitionName);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("setTaskInstanceValues: void");
    }

    @Override
    public void addTaskInstanceComment(final String token,
            final long taskInstanceId, final String message)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceComment({}, {}, {})", new Object[] { token,
                taskInstanceId, message });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.addTaskInstanceComment(auth.getName(),
                    taskInstanceId, message);

            // Activity log
            UserActivity.log(auth.getName(), "ADD_TASK_INSTANCE_COMMENT", ""
                    + taskInstanceId, null, message);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("addTaskInstanceComment: void");
    }

    @Override
    public TaskInstance getTaskInstance(final String token,
            final long taskInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getTaskInstance({}, {})", token, taskInstanceId);
        TaskInstance vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.getTaskInstance(taskInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "GET_TASK_INSTANCE", ""
                    + taskInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getTaskInstance: {}", vo);
        return vo;
    }

    @Override
    public void setTaskInstanceActorId(final String token,
            final long taskInstanceId, final String actorId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("setTaskInstanceActorId({}, {}, {})", new Object[] { token,
                taskInstanceId, actorId });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule
                    .setTaskInstanceActorId(taskInstanceId, actorId);

            // Activity log
            UserActivity.log(auth.getName(), "SET_TASK_INSTANCE_ACTOR_ID", ""
                    + taskInstanceId, null, actorId);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("setTaskInstanceActorId: void");
    }

    @Override
    public void addTaskInstanceVariable(final String token,
            final long taskInstanceId, final String name, final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceVariable({}, {}, {}, {})", new Object[] {
                token, taskInstanceId, name, value });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.addTaskInstanceVariable(taskInstanceId, name,
                    value);

            // Activity log
            UserActivity.log(auth.getName(), "ADD_TASK_INSTANCE_VARIABLE", ""
                    + taskInstanceId, null, name + ", " + value);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("addTaskInstanceVariable: void");
    }

    @Override
    public void deleteTaskInstanceVariable(final String token,
            final long taskInstanceId, final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteTaskInstanceVariable({}, {}, {})", new Object[] {
                token, taskInstanceId, name });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.deleteTaskInstanceVariable(taskInstanceId,
                    name);

            // Activity log
            UserActivity.log(auth.getName(), "DELETE_TASK_INSTANCE_VARIABLE",
                    "" + taskInstanceId, null, name);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("deleteTaskInstanceVariable: void");
    }

    @Override
    public void startTaskInstance(final String token, final long taskInstanceId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("startTaskInstance({}, {})", token, taskInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.startTaskInstance(taskInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "START_TASK_INSTANCE", ""
                    + taskInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("startTaskInstance: void");
    }

    @Override
    public void endTaskInstance(final String token, final long taskInstanceId,
            final String transitionName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("endTaskInstance({}, {}, {})", new Object[] { token,
                taskInstanceId, transitionName });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule
                    .endTaskInstance(taskInstanceId, transitionName);

            // Activity log
            UserActivity.log(auth.getName(), "END_TASK_INSTANCE", ""
                    + taskInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("endTaskInstance: void");
    }

    @Override
    public void suspendTaskInstance(final String token,
            final long taskInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("suspendTaskInstance({}, {})", token, taskInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.suspendTaskInstance(taskInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "SUSPEND_TASK_INSTANCE", ""
                    + taskInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("suspendTaskInstance: void");
    }

    @Override
    public void resumeTaskInstance(final String token, final long taskInstanceId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeTaskInstance({}, {})", token, taskInstanceId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.resumeTaskInstance(taskInstanceId);

            // Activity log
            UserActivity.log(auth.getName(), "RESUME_TASK_INSTANCE", ""
                    + taskInstanceId, null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("resumeTaskInstance: void");
    }

    @Override
    public Token getToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getToken({}, {})", token, tokenId);
        Token vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            vo = CommonWorkflowModule.getToken(tokenId);

            // Activity log
            UserActivity.log(auth.getName(), "GET_TOKEN", "" + tokenId, null,
                    null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("getToken: {}", vo);
        return vo;
    }

    @Override
    public void addTokenComment(final String token, final long tokenId,
            final String message) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("addTokenComment({}, {}, {})", new Object[] { token, tokenId,
                message });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.addTokenComment(auth.getName(), tokenId,
                    message);

            // Activity log
            UserActivity.log(auth.getName(), "ADD_TOKEN_COMMENT", "" + tokenId,
                    null, message);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("addTokenComment: void");
    }

    @Override
    public void suspendToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("suspendToken({}, {})", token, tokenId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.suspendToken(tokenId);

            // Activity log
            UserActivity.log(auth.getName(), "SUSPEND_TOKEN", "" + tokenId,
                    null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("suspendToken: void");
    }

    @Override
    public void resumeToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeToken({}, {})", token, tokenId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.resumeToken(tokenId);

            // Activity log
            UserActivity.log(auth.getName(), "RESUME_TOKEN", "" + tokenId,
                    null, null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("resumeToken: void");
    }

    @Override
    public Token sendTokenSignal(final String token, final long tokenId,
            final String transitionName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("sendTokenSignal({}, {}, {})", new Object[] { token, tokenId,
                transitionName });
        final Token vo = null;
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.sendTokenSignal(tokenId, transitionName);

            // Activity log
            UserActivity.log(auth.getName(), "SEND_TOKEN_SIGNAL", "" + tokenId,
                    null, transitionName);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("sendTokenSignal: {}", vo);
        return vo;
    }

    @Override
    public void setTokenNode(final String token, final long tokenId,
            final String nodeName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("setTokenNode({}, {}, {})", new Object[] { token, tokenId,
                nodeName });
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.setTokenNode(tokenId, nodeName);

            // Activity log
            UserActivity.log(auth.getName(), "SEND_TOKEN_NODE", "" + tokenId,
                    null, nodeName);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("setTokenNode: void");
    }

    @Override
    public void endToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("endToken({}, {})", token, tokenId);
        Authentication auth = null, oldAuth = null;

        try {
            if (token == null) {
                auth = PrincipalUtils.getAuthentication();
            } else {
                oldAuth = PrincipalUtils.getAuthentication();
                auth = PrincipalUtils.getAuthenticationByToken(token);
            }

            CommonWorkflowModule.endToken(tokenId);

            // Activity log
            UserActivity.log(auth.getName(), "END_TOKEN", "" + tokenId, null,
                    null);
        } finally {
            if (token != null) {
                PrincipalUtils.setAuthentication(oldAuth);
            }
        }

        log.debug("endToken: void");
    }
}
