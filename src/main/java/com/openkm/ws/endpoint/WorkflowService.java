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

package com.openkm.ws.endpoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.apache.commons.io.IOUtils;
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
import com.openkm.ws.util.FormElementComplex;

@WebService(name = "OKMWorkflow", serviceName = "OKMWorkflow", targetNamespace = "http://ws.openkm.com")
public class WorkflowService {
    private static Logger log = LoggerFactory.getLogger(WorkflowService.class);

    @WebMethod
    public void registerProcessDefinition(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pda") final byte[] pda) throws ParseException,
            RepositoryException, DatabaseException, WorkflowException,
            IOException {
        log.debug("registerProcessDefinition({}, {})", token, pda);
        final ByteArrayInputStream bais = new ByteArrayInputStream(pda);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.registerProcessDefinition(token, bais);
        IOUtils.closeQuietly(bais);
        log.debug("registerProcessDefinition: void");
    }

    @WebMethod
    public void deleteProcessDefinition(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pdId") final long pdId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessDefinition({}, {})", token, pdId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessDefinition(token, pdId);
        log.debug("deleteProcessDefinition: void");
    }

    @WebMethod
    public ProcessDefinition getProcessDefinition(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pdId") final long pdId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getProcessDefinition({}, {})", token, pdId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessDefinition result = wm.getProcessDefinition(token, pdId);
        log.debug("getProcessDefinition: {}", result);
        return result;
    }

    @WebMethod
    public byte[] getProcessDefinitionImage(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pdId") final long pdId,
            @WebParam(name = "node") final String node)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getProcessDefinitionImage({}, {}, {})", new Object[] {
                token, pdId, node });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final byte[] result = wm.getProcessDefinitionImage(token, pdId, node);
        log.debug("getProcessDefinitionImage: {}", result);
        return result;
    }

    /*
     * public Map<String, List<FormElement>> getProcessDefinitionForms(@WebParam(name = "token") String token,
     * @WebParam(name = "pdId") long pdId) throws ParseException, RepositoryException,
     * DatabaseException, WorkflowException {
     * log.debug("getProcessDefinitionForms({})", pdId);
     * WorkflowModule wm = ModuleManager.getWorkflowModule();
     * Map<String, List<FormElement>> result = wm.getProcessDefinitionForms(pdId);
     * log.debug("getProcessDefinitionForms: "+result);
     * return result;
     * }
     */

    @WebMethod
    public ProcessInstance runProcessDefinition(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pdId") final long pdId,
            @WebParam(name = "uuid") final String uuid,
            @WebParam(name = "values") final FormElementComplex[] values)
            throws WorkflowException, AccessDeniedException,
            RepositoryException, DatabaseException {
        log.debug("runProcessDefinition({}, {}, {}, {})", new Object[] { token,
                pdId, uuid, values });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<FormElement> al = new ArrayList<FormElement>();

        for (final FormElementComplex value : values) {
            al.add(FormElementComplex.toFormElement(value));
        }

        final ProcessInstance result = wm.runProcessDefinition(token, pdId,
                uuid, al);
        log.debug("runProcessDefinition: {}", result);
        return result;
    }

    @WebMethod
    public ProcessInstance sendProcessInstanceSignal(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId,
            @WebParam(name = "transName") final String transName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("sendProcessInstanceSignal({}, {}, {})", new Object[] {
                token, piId, transName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessInstance result = wm.sendProcessInstanceSignal(token,
                piId, transName);
        log.debug("sendProcessInstanceSignal: {}", result);
        return result;
    }

    @WebMethod
    public void endProcessInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("endProcessInstance({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endProcessInstance(token, piId);
        log.debug("endProcessInstance: void");
    }

    @WebMethod
    public void deleteProcessInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessInstance({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessInstance(token, piId);
        log.debug("deleteProcessInstance: void");
    }

    @WebMethod
    public ProcessInstance[] findProcessInstances(
            @WebParam(name = "token") final String token,
            @WebParam(name = "pdId") final long pdId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findProcessInstances({}, {})", token, pdId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessInstance> col = wm.findProcessInstances(token, pdId);
        final ProcessInstance[] result = col.toArray(new ProcessInstance[col
                .size()]);
        log.debug("findProcessInstances: {}", result);
        return result;
    }

    @WebMethod
    public ProcessDefinition[] findAllProcessDefinitions(
            @WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitions({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> col = wm.findAllProcessDefinitions(token);
        final ProcessDefinition[] result = col
                .toArray(new ProcessDefinition[col.size()]);
        log.debug("findAllProcessDefinitions: {}", result);
        return result;
    }

    @WebMethod
    public ProcessDefinition[] findLatestProcessDefinitions(
            @WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findLatestProcessDefinitions({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> col = wm
                .findLatestProcessDefinitions(token);
        final ProcessDefinition[] result = col
                .toArray(new ProcessDefinition[col.size()]);
        log.debug("findLatestProcessDefinitions: {}", result);
        return result;
    }

    @WebMethod
    public ProcessDefinition[] findAllProcessDefinitionVersions(
            @WebParam(name = "token") final String token,
            @WebParam(name = "name") final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findAllProcessDefinitionVersions({}, {})", token, name);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> col = wm
                .findAllProcessDefinitionVersions(token, name);
        final ProcessDefinition[] result = col
                .toArray(new ProcessDefinition[col.size()]);
        log.debug("findAllProcessDefinitionVersions: {}", result);
        return result;
    }

    @WebMethod
    public long findLastProcessDefinitionId(
            @WebParam(name = "token") final String token,
            @WebParam(name = "name") final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findLastProcessDefinitionVersion({}, {})", token, name);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<ProcessDefinition> col = wm
                .findAllProcessDefinitionVersions(token, name);
        long lastProcDefId = 0;

        for (final ProcessDefinition procDef : col) {
            if (procDef.getId() > lastProcDefId) {
                lastProcDefId = procDef.getId();
            }
        }

        log.debug("findLastProcessDefinitionVersion: {}", lastProcDefId);
        return lastProcDefId;
    }

    @WebMethod
    public ProcessInstance getProcessInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getProcessInstance({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final ProcessInstance result = wm.getProcessInstance(token, piId);
        log.debug("getProcessInstance: {}", result);
        return result;
    }

    @WebMethod
    public void suspendProcessInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("suspendProcessInstance({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendProcessInstance(token, piId);
        log.debug("suspendProcessInstance: void");
    }

    @WebMethod
    public void resumeProcessInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeProcessInstance({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeProcessInstance(token, piId);
        log.debug("resumeProcessInstance: void");
    }

    @WebMethod
    public void addProcessInstanceVariable(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId,
            @WebParam(name = "name") final String name,
            @WebParam(name = "value") final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addProcessInstanceVariable({}, {}, {}, {})", new Object[] {
                token, piId, name, value });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addProcessInstanceVariable(token, piId, name, value);
        log.debug("addProcessInstanceVariable: void");
    }

    @WebMethod
    public void deleteProcessInstanceVariable(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId,
            @WebParam(name = "name") final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteProcessInstanceVariable({}, {}, {})", new Object[] {
                token, piId, name });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteProcessInstanceVariable(token, piId, name);
        log.debug("deleteProcessInstanceVariable: void");
    }

    @WebMethod
    public TaskInstance[] findUserTaskInstances(
            @WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findUserTaskInstances({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> col = wm.findUserTaskInstances(token);
        final TaskInstance[] result = col.toArray(new TaskInstance[col.size()]);
        log.debug("findUserTaskInstances: {}", result);
        return result;
    }

    @WebMethod
    public TaskInstance[] findPooledTaskInstances(
            @WebParam(name = "token") final String token)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findPooledTaskInstances({})", token);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> col = wm.findPooledTaskInstances(token);
        final TaskInstance[] result = col.toArray(new TaskInstance[col.size()]);
        log.debug("findPooledTaskInstances: {}", result);
        return result;
    }

    @WebMethod
    public TaskInstance[] findTaskInstances(
            @WebParam(name = "token") final String token,
            @WebParam(name = "piId") final long piId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("findTaskInstances({}, {})", token, piId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<TaskInstance> col = wm.findTaskInstances(token, piId);
        final TaskInstance[] result = col.toArray(new TaskInstance[col.size()]);
        log.debug("findTaskInstances: {}", result);
        return result;
    }

    @WebMethod
    public void setTaskInstanceValues(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "transName") final String transName,
            @WebParam(name = "values") final FormElementComplex[] values)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("setTaskInstanceValues({}, {}, {}, {})", new Object[] {
                token, tiId, transName, values });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final List<FormElement> al = new ArrayList<FormElement>();

        for (final FormElementComplex value : values) {
            al.add(FormElementComplex.toFormElement(value));
        }

        wm.setTaskInstanceValues(token, tiId, transName, al);
        log.debug("setTaskInstanceValues: void");
    }

    @WebMethod
    public void addTaskInstanceComment(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "message") final String message)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceComment({}, {}, {})", new Object[] { token,
                tiId, message });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTaskInstanceComment(token, tiId, message);
        log.debug("addTaskInstanceComment: void");
    }

    @WebMethod
    public TaskInstance getTaskInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getTaskInstance({}, {})", token, tiId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final TaskInstance result = wm.getTaskInstance(token, tiId);
        log.debug("getTaskInstance: {}", result);
        return result;
    }

    @WebMethod
    public void setTaskInstanceActorId(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "actorId") final String actorId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("setTaskInstanceActorId({}, {}, {})", new Object[] { token,
                tiId, actorId });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.setTaskInstanceActorId(token, tiId, actorId);
        log.debug("setTaskInstanceActorId: void");
    }

    @WebMethod
    public void addTaskInstanceVariable(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "name") final String name,
            @WebParam(name = "value") final Object value)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTaskInstanceVariable({}, {}, {}, {})", new Object[] {
                token, tiId, name, value });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTaskInstanceVariable(token, tiId, name, value);
        log.debug("addTaskInstanceVariable: void");
    }

    @WebMethod
    public void deleteTaskInstanceVariable(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "name") final String name)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("deleteTaskInstanceVariable({}, {}, {})", new Object[] {
                token, tiId, name });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.deleteTaskInstanceVariable(token, tiId, name);
        log.debug("deleteTaskInstanceVariable: void");
    }

    @WebMethod
    public void startTaskInstance(@WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("startTaskInstance({}, {})", token, tiId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.startTaskInstance(token, tiId);
        log.debug("startTaskInstance: void");
    }

    @WebMethod
    public void endTaskInstance(@WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId,
            @WebParam(name = "transName") final String transName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("endTaskInstance({}, {}, {})", new Object[] { token, tiId,
                transName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endTaskInstance(token, tiId, transName);
        log.debug("endTaskInstance: void");
    }

    @WebMethod
    public void suspendTaskInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("suspendTaskInstance({}, {})", token, tiId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendTaskInstance(token, tiId);
        log.debug("suspendTaskInstance: void");
    }

    @WebMethod
    public void resumeTaskInstance(
            @WebParam(name = "token") final String token,
            @WebParam(name = "tiId") final long tiId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeTaskInstance({}, {})", token, tiId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeTaskInstance(token, tiId);
        log.debug("resumeTaskInstance: void");
    }

    @WebMethod
    public Token getToken(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("getToken({}, {})", token, tkId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final Token result = wm.getToken(token, tkId);
        log.debug("getToken: {}", result);
        return result;
    }

    @WebMethod
    public void addTokenComment(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId,
            @WebParam(name = "message") final String message)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("addTokenComment({}, {}, {})", new Object[] { token, tkId,
                message });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.addTokenComment(token, tkId, message);
        log.debug("addTokenComment: void");
    }

    @WebMethod
    public void suspendToken(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("suspendToken({}, {})", token, tkId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.suspendToken(token, tkId);
        log.debug("suspendToken: void");
    }

    @WebMethod
    public void resumeToken(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("resumeToken({}, {})", token, tkId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.resumeToken(token, tkId);
        log.debug("resumeToken: void");
    }

    @WebMethod
    public Token sendTokenSignal(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId,
            @WebParam(name = "transName") final String transName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("sendTokenSignal({}, {}, {})", new Object[] { token, tkId,
                transName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        final Token result = wm.sendTokenSignal(token, tkId, transName);
        log.debug("sendTokenSignal: {}", result);
        return result;
    }

    @WebMethod
    public void setTokenNode(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId,
            @WebParam(name = "nodeName") final String nodeName)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("setTokenNode({}, {}, {})", new Object[] { token, tkId,
                nodeName });
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.setTokenNode(token, tkId, nodeName);
        log.debug("setTokenNode: void");
    }

    @WebMethod
    public void endToken(@WebParam(name = "token") final String token,
            @WebParam(name = "tkId") final long tkId)
            throws RepositoryException, DatabaseException, WorkflowException {
        log.debug("endToken({}, {})", token, tkId);
        final WorkflowModule wm = ModuleManager.getWorkflowModule();
        wm.endToken(token, tkId);
        log.debug("endToken: void");
    }
}
