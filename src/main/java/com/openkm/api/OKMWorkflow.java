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

package com.openkm.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.openkm.module.ModuleManager;
import com.openkm.module.WorkflowModule;

/**
 * @author pavila
 *
 */
public class OKMWorkflow implements WorkflowModule {
    private static Logger log = LoggerFactory.getLogger(OKMWorkflow.class);

    private static OKMWorkflow instance = new OKMWorkflow();

    private OKMWorkflow() {
    }

    public static OKMWorkflow getInstance() {
        return instance;
    }

    @Override
    public void registerProcessDefinition(final String token,
            final InputStream is) throws ParseException, RepositoryException,
            DatabaseException, WorkflowException, IOException {
        log.debug("registerProcessDefinition({}, {})", token, is);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.registerProcessDefinition(token, is);
        log.debug("registerProcessDefinition: void");
    }

    @Override
    public void deleteProcessDefinition(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("deleteProcessDefinition({}, {})", token, processDefinitionId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessDefinition(token, processDefinitionId);
        log.debug("deleteProcessDefinition: void");
    }

    @Override
    public ProcessDefinition getProcessDefinition(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getProcessDefinition({}, {})", token, processDefinitionId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessDefinition result = wm.getProcessDefinition(token,
                processDefinitionId);
        log.debug("getProcessDefinition: {}", result);
        return result;
    }

    @Override
    public byte[] getProcessDefinitionImage(final String token,
            final long processDefinitionId, final String node)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getProcessDefinitionImage({}, {}, {})", new Object[] {
                token, processDefinitionId, node });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final byte[] result = wm.getProcessDefinitionImage(token,
                processDefinitionId, node);
        log.debug("getProcessDefinitionImage: {}", result);
        return result;
    }

    @Override
    public Map<String, List<FormElement>> getProcessDefinitionForms(
            final String token, final long processDefinitionId)
            throws ParseException, RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("getProcessDefinitionForms({}, {})", token,
                processDefinitionId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final Map<String, List<FormElement>> result = wm
                .getProcessDefinitionForms(token, processDefinitionId);
        log.debug("getProcessDefinitionForms: {}", result);
        return result;
    }

    @Override
    public ProcessInstance runProcessDefinition(final String token,
            final long processDefinitionId, final String uuid,
            final List<FormElement> variables) throws WorkflowException,
            AccessDeniedException, RepositoryException, DatabaseException {
        log.debug("runProcessDefinition({}, {}, {}, {})", new Object[] { token,
                processDefinitionId, uuid, variables });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessInstance result = wm.runProcessDefinition(token,
                processDefinitionId, uuid, variables);
        log.debug("runProcessDefinition: {}", result);
        return result;
    }

    @Override
    public ProcessInstance sendProcessInstanceSignal(final String token,
            final long processInstanceId, final String transitionName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("sendProcessInstanceSignal({}, {}, {})", new Object[] {
                token, processInstanceId, transitionName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessInstance result = wm.sendProcessInstanceSignal(token,
                processInstanceId, transitionName);
        log.debug("sendProcessInstanceSignal: {}", result);
        return result;
    }

    @Override
    public void endProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("endProcessInstance({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endProcessInstance(token, processInstanceId);
        log.debug("endProcessInstance: void");
    }

    @Override
    public void deleteProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("deleteProcessInstance({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessInstance(token, processInstanceId);
        log.debug("deleteProcessInstance: void");
    }

    @Override
    public List<ProcessInstance> findProcessInstances(final String token,
            final long processDefinitionId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findProcessInstances({}, {})", token, processDefinitionId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessInstance> result = wm.findProcessInstances(token,
                processDefinitionId);
        log.debug("findProcessInstances: {}", result);
        return result;
    }

    @Override
    public List<ProcessDefinition> findAllProcessDefinitions(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitions({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> result = wm
                .findAllProcessDefinitions(token);
        log.debug("findAllProcessDefinitions: {}", result);
        return result;
    }

    @Override
    public List<ProcessDefinition> findLatestProcessDefinitions(
            final String token) throws RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("findLatestProcessDefinitions({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> result = wm
                .findLatestProcessDefinitions(token);
        log.debug("findLatestProcessDefinitions: {}", result);
        return result;
    }

    @Override
    public ProcessDefinition findLastProcessDefinition(final String token,
            final String name) throws RepositoryException, DatabaseException,
            WorkflowException {
        log.debug("findLastProcessDefinition({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessDefinition result = wm.findLastProcessDefinition(token,
                name);
        log.debug("findLastProcessDefinition: {}", result);
        return result;
    }

    @Override
    public List<ProcessDefinition> findAllProcessDefinitionVersions(
            final String token, final String name) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitionVersions({}, {})", token, name);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> result = wm
                .findAllProcessDefinitionVersions(token, name);
        log.debug("findAllProcessDefinitionVersions: {}", result);
        return result;
    }

    @Override
    public ProcessInstance getProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getProcessInstance({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessInstance result = wm.getProcessInstance(token,
                processInstanceId);
        log.debug("getProcessInstance: {}", result);
        return result;
    }

    @Override
    public void suspendProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("suspendProcessInstance({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendProcessInstance(token, processInstanceId);
        log.debug("suspendProcessInstance: void");
    }

    @Override
    public void resumeProcessInstance(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("resumeProcessInstance({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeProcessInstance(token, processInstanceId);
        log.debug("resumeProcessInstance: void");
    }

    @Override
    public void addProcessInstanceVariable(final String token,
            final long processInstanceId, final String name, final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addProcessInstanceVariable({}, {}, {}, {})", new Object[] {
                token, processInstanceId, name, value });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addProcessInstanceVariable(token, processInstanceId, name, value);
        log.debug("addProcessInstanceVariable: void");
    }

    @Override
    public void deleteProcessInstanceVariable(final String token,
            final long processInstanceId, final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessInstanceVariable({}, {}, {})", new Object[] {
                token, processInstanceId, name });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessInstanceVariable(token, processInstanceId, name);
        log.debug("deleteProcessInstanceVariable: void");
    }

    @Override
    public List<TaskInstance> findUserTaskInstances(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findUserTaskInstances({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> result = wm.findUserTaskInstances(token);
        log.debug("findUserTaskInstances: {}", result);
        return result;
    }

    @Override
    public List<TaskInstance> findPooledTaskInstances(final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findPooledTaskInstances({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> result = wm.findPooledTaskInstances(token);
        log.debug("findPooledTaskInstances: {}", result);
        return result;
    }

    @Override
    public List<TaskInstance> findTaskInstances(final String token,
            final long processInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("findTaskInstances({}, {})", token, processInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> result = wm.findTaskInstances(token,
                processInstanceId);
        log.debug("findTaskInstances: {}", result);
        return result;
    }

    @Override
    public void setTaskInstanceValues(final String token,
            final long taskInstanceId, final String transitionName,
            final List<FormElement> values) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("setTaskInstanceValues({}, {}, {}, {})", new Object[] {
                token, taskInstanceId, transitionName, values });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.setTaskInstanceValues(token, taskInstanceId, transitionName, values);
        log.debug("setTaskInstanceValues: void");
    }

    @Override
    public void addTaskInstanceComment(final String token,
            final long taskInstanceId, final String message)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceComment({}, {}, {})", new Object[] { token,
                taskInstanceId, message });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTaskInstanceComment(token, taskInstanceId, message);
        log.debug("addTaskInstanceComment: void");
    }

    @Override
    public TaskInstance getTaskInstance(final String token,
            final long taskInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("getTaskInstance({}, {})", token, taskInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final TaskInstance result = wm.getTaskInstance(token, taskInstanceId);
        log.debug("getTaskInstance: {}", result);
        return result;
    }

    @Override
    public void setTaskInstanceActorId(final String token,
            final long taskInstanceId, final String actorId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("setTaskInstanceActorId({}, {}, {})", new Object[] { token,
                taskInstanceId, actorId });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.setTaskInstanceActorId(token, taskInstanceId, actorId);
        log.debug("setTaskInstanceActorId: void");
    }

    @Override
    public void addTaskInstanceVariable(final String token,
            final long taskInstanceId, final String name, final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceVariable({}, {}, {}, {})", new Object[] {
                token, taskInstanceId, name, value });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTaskInstanceVariable(token, taskInstanceId, name, value);
        log.debug("addTaskInstanceVariable: void");
    }

    @Override
    public void deleteTaskInstanceVariable(final String token,
            final long taskInstanceId, final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteTaskInstanceVariable({}, {}, {})", new Object[] {
                token, taskInstanceId, name });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteTaskInstanceVariable(token, taskInstanceId, name);
        log.debug("deleteTaskInstanceVariable: void");
    }

    @Override
    public void startTaskInstance(final String token, final long taskInstanceId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("startTaskInstance({}, {})", token, taskInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.startTaskInstance(token, taskInstanceId);
        log.debug("startTaskInstance: void");
    }

    @Override
    public void endTaskInstance(final String token, final long taskInstanceId,
            final String transitionName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("endTaskInstance({}, {} ,{})", new Object[] { token,
                taskInstanceId, transitionName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endTaskInstance(token, taskInstanceId, transitionName);
        log.debug("endTaskInstance: void");
    }

    @Override
    public void suspendTaskInstance(final String token,
            final long taskInstanceId) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("suspendTaskInstance({}, {})", token, taskInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendTaskInstance(token, taskInstanceId);
        log.debug("suspendTaskInstance: void");
    }

    @Override
    public void resumeTaskInstance(final String token, final long taskInstanceId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeTaskInstance({}, {})", token, taskInstanceId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeTaskInstance(token, taskInstanceId);
        log.debug("resumeTaskInstance: void");
    }

    @Override
    public Token getToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getToken({}, {})", token, tokenId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final Token result = wm.getToken(token, tokenId);
        log.debug("getToken: {}", result);
        return result;
    }

    @Override
    public void addTokenComment(final String token, final long tokenId,
            final String message) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("addTokenComment({}, {}, {})", new Object[] { token, tokenId,
                message });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTokenComment(token, tokenId, message);
        log.debug("addTokenComment: void");
    }

    @Override
    public void suspendToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("suspendToken({}, {})", token, tokenId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendToken(token, tokenId);
        log.debug("suspendToken: void");
    }

    @Override
    public void resumeToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeToken({}, {})", token, tokenId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeToken(token, tokenId);
        log.debug("resumeToken: void");
    }

    @Override
    public Token sendTokenSignal(final String token, final long tokenId,
            final String transitionName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("sendTokenSignal({}, {}, {})", new Object[] { token, tokenId,
                transitionName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final Token result = wm.sendTokenSignal(token, tokenId, transitionName);
        log.debug("sendTokenSignal: {}", result);
        return result;
    }

    @Override
    public void setTokenNode(final String token, final long tokenId,
            final String nodeName) throws RepositoryException,
            DatabaseException, WorkflowException {
        log.debug("setTokenNode({}, {}, {})", new Object[] { token, tokenId,
                nodeName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.setTokenNode(token, tokenId, nodeName);
        log.debug("setTokenNode: void");
    }

    @Override
    public void endToken(final String token, final long tokenId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("endToken({}, {})", token, tokenId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endToken(token, tokenId);
        log.debug("endToken: void");
    }
}
