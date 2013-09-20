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

package com.openkm.module.common;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.GraphSession;
import org.jbpm.db.TaskMgmtSession;
import org.jbpm.file.def.FileDefinition;
import org.jbpm.taskmgmt.exe.TaskMgmtInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.form.FormElement;
import com.openkm.bean.workflow.ProcessDefinition;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.bean.workflow.TaskInstance;
import com.openkm.bean.workflow.Token;
import com.openkm.core.Config;
import com.openkm.core.ParseException;
import com.openkm.core.WorkflowException;
import com.openkm.util.FormUtils;
import com.openkm.util.JBPMUtils;
import com.openkm.util.WorkflowUtils;

public class CommonWorkflowModule {
    private static Logger log = LoggerFactory
            .getLogger(CommonWorkflowModule.class);

    /**
     * Register Process Definition
     */
    public static void registerProcessDefinition(final InputStream is)
            throws WorkflowException, ParseException, IOException {
        log.debug("registerProcessDefinition({})", is);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        InputStream isForms = null;
        ZipInputStream zis = null;

        if (Config.SYSTEM_READONLY) {
            throw new WorkflowException("System is in read-only mode");
        }

        try {
            zis = new ZipInputStream(is);
            final org.jbpm.graph.def.ProcessDefinition processDefinition = org.jbpm.graph.def.ProcessDefinition
                    .parseParZipInputStream(zis);

            // Check xml form definition
            final FileDefinition fileDef = processDefinition
                    .getFileDefinition();
            isForms = fileDef.getInputStream("forms.xml");
            FormUtils.parseWorkflowForms(isForms);

            // If it is ok, deploy it
            jbpmContext.deployProcessDefinition(processDefinition);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(isForms);
            IOUtils.closeQuietly(zis);
            jbpmContext.close();
        }

        log.debug("registerProcessDefinition: void");
    }

    /**
     * Delete Process Definition
     */
    public static void deleteProcessDefinition(final long processDefinitionId)
            throws WorkflowException {
        log.debug("deleteProcessDefinition({})", processDefinitionId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        if (Config.SYSTEM_READONLY) {
            throw new WorkflowException("System is in read-only mode");
        }

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            graphSession.deleteProcessDefinition(processDefinitionId);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("deleteProcessDefinition: void");
    }

    /**
     * Get Process Definition
     */
    public static ProcessDefinition getProcessDefinition(
            final long processDefinitionId) throws WorkflowException {
        log.debug("getProcessDefinition({})", processDefinitionId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        ProcessDefinition vo = new ProcessDefinition();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.def.ProcessDefinition pd = graphSession
                    .getProcessDefinition(processDefinitionId);
            vo = WorkflowUtils.copy(pd);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("getProcessDefinition: {}", vo);
        return vo;
    }

    /**
     * Get Process Definition Image
     */
    public static byte[] getProcessDefinitionImage(
            final long processDefinitionId, final String node)
            throws WorkflowException {
        log.debug("getProcessDefinitionImage({}, {})", new Object[] {
                processDefinitionId, node });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        byte[] image = null;

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.def.ProcessDefinition pd = graphSession
                    .getProcessDefinition(processDefinitionId);
            final FileDefinition fileDef = pd.getFileDefinition();

            final WorkflowUtils.DiagramInfo dInfo = WorkflowUtils
                    .getDiagramInfo(fileDef.getInputStream("gpd.xml"));
            final WorkflowUtils.DiagramNodeInfo dNodeInfo = dInfo.getNodeMap()
                    .get(node);
            final BufferedImage img = ImageIO.read(fileDef
                    .getInputStream("processimage.jpg"));

            // Obtain all nodes Y and X
            final List<Integer> ordenadas = new ArrayList<Integer>();
            final List<Integer> abcisas = new ArrayList<Integer>();

            for (final WorkflowUtils.DiagramNodeInfo nodeInfo : dInfo
                    .getNodeMap().values()) {
                ordenadas.add(nodeInfo.getY());
                abcisas.add(nodeInfo.getX());
            }

            // Calculate minimal Y
            Collections.sort(ordenadas);
            final int fixOrd = ordenadas.get(0) < 0 ? ordenadas.get(0) : 0;

            // Calculate minimal X
            Collections.sort(abcisas);
            final int fixAbs = abcisas.get(0) < 0 ? abcisas.get(0) : 0;

            if (dNodeInfo != null) {
                // Select node
                log.debug("DiagramNodeInfo: {}", dNodeInfo);
                final Graphics g = img.getGraphics();
                final Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.25F));
                g2d.setColor(Color.blue);
                g2d.fillRect(dNodeInfo.getX() - fixAbs, dNodeInfo.getY()
                        - fixOrd, dNodeInfo.getWidth(), dNodeInfo.getHeight());
                g.dispose();
            }

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            image = baos.toByteArray();
            baos.flush();
            baos.close();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } catch (final IOException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("getProcessDefinitionImage: {}", image);
        return image;
    }

    /**
     * Get Process Definition Forms
     */
    public static Map<String, List<FormElement>> getProcessDefinitionForms(
            final long processDefinitionId) throws ParseException {
        log.debug("getProcessDefinitionForms({})", processDefinitionId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        Map<String, List<FormElement>> forms = new HashMap<String, List<FormElement>>();
        InputStream is = null;

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.def.ProcessDefinition pd = graphSession
                    .getProcessDefinition(processDefinitionId);
            final FileDefinition fileDef = pd.getFileDefinition();
            is = fileDef.getInputStream("forms.xml");

            if (is != null) {
                forms = FormUtils.parseWorkflowForms(is);
            } else {
                log.warn("Process definition '{}' has no forms.xml file",
                        processDefinitionId);
            }
        } finally {
            IOUtils.closeQuietly(is);
            jbpmContext.close();
        }

        log.debug("getProcessDefinitionForms: {}", forms);
        return forms;
    }

    /**
     * Start Process Definition
     */
    public static ProcessInstance runProcessDefinition(final String user,
            final long processDefinitionId, final String uuid,
            final List<FormElement> variables) throws WorkflowException {
        log.debug("runProcessDefinition({}, {}, {}, {})", new Object[] { user,
                processDefinitionId, uuid, variables });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        ProcessInstance vo = new ProcessInstance();

        if (Config.SYSTEM_READONLY) {
            throw new WorkflowException("System is in read-only mode");
        }

        try {
            jbpmContext.setActorId(user);
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final Map<String, Object> hm = new HashMap<String, Object>();
            hm.put(Config.WORKFLOW_PROCESS_INSTANCE_VARIABLE_UUID, uuid);

            for (final FormElement fe : variables) {
                hm.put(fe.getName(), fe);
            }

            final org.jbpm.graph.def.ProcessDefinition pd = graphSession
                    .getProcessDefinition(processDefinitionId);
            final org.jbpm.graph.exe.ProcessInstance pi = pd
                    .createProcessInstance(hm);

            if (pi != null) {
                final org.jbpm.taskmgmt.exe.TaskMgmtInstance tmi = pi
                        .getTaskMgmtInstance();

                // http://community.jboss.org/thread/115182
                if (tmi.getTaskMgmtDefinition().getStartTask() != null) {
                    final org.jbpm.taskmgmt.exe.TaskInstance ti = tmi
                            .createStartTaskInstance();

                    if (Config.WORKFLOW_START_TASK_AUTO_RUN) {
                        ti.start();
                        ti.end();
                    }
                } else {
                    pi.getRootToken().signal();
                }

                jbpmContext.save(pi);
                vo = WorkflowUtils.copy(pi);
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("runProcessDefinition: {}", vo);
        return vo;
    }

    /**
     * Send Process Instance Signal
     */
    public static ProcessInstance sendProcessInstanceSignal(
            final long processInstanceId, final String transitionName)
            throws WorkflowException {
        log.debug("sendProcessInstanceSignal({}, {})", new Object[] {
                processInstanceId, transitionName });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        ProcessInstance vo = new ProcessInstance();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.exe.ProcessInstance pi = graphSession
                    .getProcessInstance(processInstanceId);
            final org.jbpm.graph.exe.Token t = pi.getRootToken();

            if (transitionName != null && !transitionName.equals("")) {
                t.signal(transitionName);
            } else {
                t.signal();
            }

            jbpmContext.getSession().flush();
            vo = WorkflowUtils.copy(pi);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("sendProcessInstanceSignal: {}", vo);
        return vo;
    }

    /**
     * End Process Instance
     */
    public static void endProcessInstance(final long processInstanceId)
            throws WorkflowException {
        log.debug("endProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            graphSession.getProcessInstance(processInstanceId).end();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Delete Process Instance
     */
    public static void deleteProcessInstance(final long processInstanceId)
            throws WorkflowException {
        log.debug("deleteProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            graphSession.deleteProcessInstance(processInstanceId);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Find Process Instance
     */
    @SuppressWarnings("rawtypes")
    public static List<ProcessInstance> findProcessInstances(
            final long processDefinitionId) throws WorkflowException {
        log.debug("findProcessInstances({})", processDefinitionId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessInstance> al = new ArrayList<ProcessInstance>();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();

            for (final Iterator it = graphSession.findProcessInstances(
                    processDefinitionId).iterator(); it.hasNext();) {
                final org.jbpm.graph.exe.ProcessInstance procInst = (org.jbpm.graph.exe.ProcessInstance) it
                        .next();
                al.add(WorkflowUtils.copy(procInst));
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findProcessInstances: {}", al);
        return al;
    }

    /**
     * Find All Process Definition
     */
    @SuppressWarnings("rawtypes")
    public static List<ProcessDefinition> findAllProcessDefinitions()
            throws WorkflowException {
        log.debug("findAllProcessDefinitions()");
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessDefinition> al = new ArrayList<ProcessDefinition>();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();

            for (final Iterator it = graphSession.findAllProcessDefinitions()
                    .iterator(); it.hasNext();) {
                final org.jbpm.graph.def.ProcessDefinition procDef = (org.jbpm.graph.def.ProcessDefinition) it
                        .next();
                al.add(WorkflowUtils.copy(procDef));
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findAllProcessDefinitions: {}", al);
        return al;
    }

    /**
     * Find Latest Process Definitions
     */
    @SuppressWarnings("rawtypes")
    public static List<ProcessDefinition> findLatestProcessDefinitions()
            throws WorkflowException {
        log.debug("findLatestProcessDefinitions()");
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessDefinition> al = new ArrayList<ProcessDefinition>();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();

            for (final Iterator it = graphSession
                    .findLatestProcessDefinitions().iterator(); it.hasNext();) {
                final org.jbpm.graph.def.ProcessDefinition procDef = (org.jbpm.graph.def.ProcessDefinition) it
                        .next();
                al.add(WorkflowUtils.copy(procDef));
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findLatestProcessDefinitions: {}", al);
        return al;
    }

    @SuppressWarnings("rawtypes")
    public static ProcessDefinition findLastProcessDefinition(final String name)
            throws WorkflowException {
        log.debug("findLastProcessDefinition()");
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        ProcessDefinition pd = new ProcessDefinition();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();

            for (final Iterator it = graphSession
                    .findLatestProcessDefinitions().iterator(); it.hasNext();) {
                final org.jbpm.graph.def.ProcessDefinition procDef = (org.jbpm.graph.def.ProcessDefinition) it
                        .next();

                if (procDef.getName().equals(name)) {
                    pd = WorkflowUtils.copy(procDef);
                }
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findLastProcessDefinition: {}", pd);
        return pd;
    }

    /**
     * Find All Process Definition Versions
     */
    @SuppressWarnings("rawtypes")
    public static List<ProcessDefinition> findAllProcessDefinitionVersions(
            final String name) throws WorkflowException {
        log.debug("findAllProcessDefinitionVersions({})", name);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessDefinition> al = new ArrayList<ProcessDefinition>();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();

            for (final Iterator it = graphSession
                    .findAllProcessDefinitionVersions(name).iterator(); it
                    .hasNext();) {
                final org.jbpm.graph.def.ProcessDefinition procDef = (org.jbpm.graph.def.ProcessDefinition) it
                        .next();
                al.add(WorkflowUtils.copy(procDef));
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findAllProcessDefinitionVersions: {}", al);
        return al;
    }

    /**
     * Get Process Instance
     */
    public static ProcessInstance getProcessInstance(
            final long processInstanceId) throws WorkflowException {
        log.debug("getProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        ProcessInstance vo = new ProcessInstance();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.exe.ProcessInstance pi = graphSession
                    .getProcessInstance(processInstanceId);
            vo = WorkflowUtils.copy(pi);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("getProcessInstance: {}", vo);
        return vo;
    }

    /**
     * Suspend (pause) Process Instance
     */
    public static void suspendProcessInstance(final long processInstanceId)
            throws WorkflowException {
        log.debug("suspendProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.ProcessInstance pi = jbpmContext
                    .getProcessInstance(processInstanceId);
            pi.suspend();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Resume Process Instance
     */
    public static void resumeProcessInstance(final long processInstanceId)
            throws WorkflowException {
        log.debug("resumeProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.ProcessInstance pi = jbpmContext
                    .getProcessInstance(processInstanceId);
            pi.resume();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Add Process Instance Variable
     */
    public static void addProcessInstanceVariable(final long processInstanceId,
            final String name, final Object value) throws WorkflowException {
        log.debug("addProcessInstanceVariable({}, {}, {})", new Object[] {
                processInstanceId, name, value });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.ProcessInstance pi = jbpmContext
                    .getProcessInstance(processInstanceId);
            pi.getContextInstance().setVariable(name, value);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Delete Process Instance
     */
    public static void deleteProcessInstanceVariable(
            final long processInstanceId, final String name)
            throws WorkflowException {
        log.debug("deleteProcessInstanceVariable({}, {})", new Object[] {
                processInstanceId, name });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.ProcessInstance pi = jbpmContext
                    .getProcessInstance(processInstanceId);
            pi.getContextInstance().deleteVariable(name);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Find User Task Instances
     */
    @SuppressWarnings("rawtypes")
    public static List<TaskInstance> findUserTaskInstances(final String user)
            throws WorkflowException {
        log.debug("findUserTaskInstances({})", user);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<TaskInstance> al = new ArrayList<TaskInstance>();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();

            for (final Iterator it = taskMgmtSession.findTaskInstances(user)
                    .iterator(); it.hasNext();) {
                final org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) it
                        .next();
                al.add(WorkflowUtils.copy(ti));
            }

            // Sort
            Collections.sort(al);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findUserTaskInstances: {}", al);
        return al;
    }

    /**
     * Find Pooled Task Instances
     */
    @SuppressWarnings("rawtypes")
    public static List<TaskInstance> findPooledTaskInstances(final String user)
            throws WorkflowException {
        log.debug("findPooledTaskInstances({})", user);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final ArrayList<TaskInstance> al = new ArrayList<TaskInstance>();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();

            for (final Iterator it = taskMgmtSession.findPooledTaskInstances(
                    user).iterator(); it.hasNext();) {
                final org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) it
                        .next();
                al.add(WorkflowUtils.copy(ti));
            }

            // Sort
            Collections.sort(al);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findPooledTaskInstances: {}", al);
        return al;
    }

    /**
     * Find Task Instances
     */
    @SuppressWarnings("rawtypes")
    public static List<TaskInstance> findTaskInstances(
            final long processInstanceId) throws WorkflowException {
        log.debug("findTaskInstances({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final ArrayList<TaskInstance> al = new ArrayList<TaskInstance>();

        try {
            final GraphSession graphSession = jbpmContext.getGraphSession();
            final org.jbpm.graph.exe.ProcessInstance pi = graphSession
                    .getProcessInstance(processInstanceId);
            final TaskMgmtInstance taskMgmtInstance = pi.getTaskMgmtInstance();

            if (taskMgmtInstance.getTaskInstances() != null) {
                for (final Object element : taskMgmtInstance.getTaskInstances()) {
                    final org.jbpm.taskmgmt.exe.TaskInstance ti = (org.jbpm.taskmgmt.exe.TaskInstance) element;
                    al.add(WorkflowUtils.copy(ti));
                }
            }

            // Sort
            Collections.sort(al);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findTaskInstances: {}", al);
        return al;
    }

    /**
     * Set Task Instance Values
     */
    public static void setTaskInstanceValues(final long taskInstanceId,
            final String transitionName, final List<FormElement> values)
            throws WorkflowException {
        log.debug("setTaskInstanceValues({}, {}, {})", new Object[] {
                taskInstanceId, transitionName, values });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final Map<String, FormElement> hm = new HashMap<String, FormElement>();

            for (final FormElement fe : values) {
                hm.put(fe.getName(), fe);
            }

            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            ti.setVariables(hm);

            if (transitionName != null && !transitionName.equals("")) {
                if (ti.getStart() == null) {
                    ti.start();
                }

                ti.end(transitionName);
            } else {
                if (ti.getStart() == null) {
                    ti.start();
                }

                ti.end();
            }

            jbpmContext.save(ti);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Add Task Instance Comment
     */
    public static void addTaskInstanceComment(final String user,
            final long taskInstanceId, final String message)
            throws WorkflowException {
        log.debug("addTaskInstanceComment({}, {}, {})", new Object[] { user,
                taskInstanceId, message });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            ti.addComment(new org.jbpm.graph.exe.Comment(user, message));
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }
    }

    /**
     * Get Task Instance
     */
    public static TaskInstance getTaskInstance(final long taskInstanceId)
            throws WorkflowException {
        log.debug("getTaskInstance({})", taskInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        TaskInstance vo = new TaskInstance();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            vo = WorkflowUtils.copy(ti);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("getTaskInstance: {}", vo);
        return vo;
    }

    /**
     * Set Task Instance Actor Id
     */
    public static void setTaskInstanceActorId(final long taskInstanceId,
            final String actorId) throws WorkflowException {
        log.debug("setTaskInstanceActorId({}, {})", new Object[] {
                taskInstanceId, actorId });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.taskmgmt.exe.TaskInstance ti = jbpmContext
                    .getTaskInstance(taskInstanceId);
            ti.setActorId(actorId);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("setTaskInstanceActorId: void");
    }

    /**
     * Add Task Instance Variable Esto creo que sobra pq no se puede hacer
     */
    public static void addTaskInstanceVariable(final long taskInstanceId,
            final String name, final Object value) throws WorkflowException {
        log.debug("addTaskInstanceVariable({}, {}, {})", new Object[] {
                taskInstanceId, name, value });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.taskmgmt.exe.TaskInstance ti = jbpmContext
                    .getTaskInstance(taskInstanceId);
            ti.setVariable(name, value);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("addTaskInstanceVariable: void");
    }

    /**
     * Delete Task Instance Variable
     */
    public static void deleteTaskInstanceVariable(final long taskInstanceId,
            final String name) throws WorkflowException {
        log.debug("deleteTaskInstanceVariable({}, {})", new Object[] {
                taskInstanceId, name });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.taskmgmt.exe.TaskInstance ti = jbpmContext
                    .getTaskInstance(taskInstanceId);
            ti.deleteVariable(name);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("deleteTaskInstanceVariable: void");
    }

    /**
     * Start Task Instance
     */
    public static void startTaskInstance(final long taskInstanceId)
            throws WorkflowException {
        log.debug("startTaskInstance({})", taskInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            ti.start();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("startTaskInstance: void");
    }

    /**
     * End Task Instance
     */
    public static void endTaskInstance(final long taskInstanceId,
            final String transitionName) throws WorkflowException {
        log.debug("endTaskInstance({}, {})", new Object[] { taskInstanceId,
                transitionName });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);

            if (transitionName != null && !transitionName.equals("")) {
                if (ti.getStart() == null) {
                    ti.start();
                }

                ti.end(transitionName);
            } else {
                if (ti.getStart() == null) {
                    ti.start();
                }

                ti.end();
            }

            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("endTaskInstance: void");
    }

    /**
     * Suspend (pause) Task Instance
     */
    public static void suspendTaskInstance(final long taskInstanceId)
            throws WorkflowException {
        log.debug("suspendTaskInstance({})", taskInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            ti.suspend();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("suspendTaskInstance: void");
    }

    /**
     * Resume Task Instance
     */
    public static void resumeTaskInstance(final long taskInstanceId)
            throws WorkflowException {
        log.debug("resumeTaskInstance({})", taskInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final TaskMgmtSession taskMgmtSession = jbpmContext
                    .getTaskMgmtSession();
            final org.jbpm.taskmgmt.exe.TaskInstance ti = taskMgmtSession
                    .getTaskInstance(taskInstanceId);
            ti.resume();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("resumeTaskInstance: void");
    }

    /**
     * Get Token
     */
    public static Token getToken(final long tokenId) throws WorkflowException {
        log.debug("getToken({})", tokenId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        Token vo = new Token();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);
            vo = WorkflowUtils.copy(t);

            // Avoid recursion
            vo.setProcessInstance(WorkflowUtils.copy(t.getProcessInstance()));
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("getToken: " + vo);
        return vo;
    }

    /**
     * Add Token Comment
     */
    public static void addTokenComment(final String user, final long tokenId,
            final String message) throws WorkflowException {
        log.debug("addTokenComment({}, {}, {})", new Object[] { user, tokenId,
                message });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);
            t.addComment(new org.jbpm.graph.exe.Comment(user, message));
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("addTokenComment: void");
    }

    /**
     * Suspend (pause) Token
     */
    public static void suspendToken(final long tokenId)
            throws WorkflowException {
        log.debug("suspendToken({})", tokenId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);
            t.suspend();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("suspendToken: void");
    }

    /**
     * Resume Token
     */
    public static void resumeToken(final long tokenId) throws WorkflowException {
        log.debug("resumeToken({})", tokenId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);
            t.resume();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("resumeToken: void");
    }

    /**
     * Send Token Signal
     */
    public static Token sendTokenSignal(final long tokenId,
            final String transitionName) throws WorkflowException {
        log.debug("sendTokenSignal({}, {})", new Object[] { tokenId,
                transitionName });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        Token vo = new Token();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);

            if (transitionName != null && !transitionName.equals("")) {
                t.signal(transitionName);
            } else {
                t.signal();
            }

            jbpmContext.getSession().flush();
            vo = WorkflowUtils.copy(t);

            // Avoid recursion
            vo.setProcessInstance(WorkflowUtils.copy(t.getProcessInstance()));
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("sendTokenSignal: {}", vo);
        return vo;
    }

    /**
     * Set Token Node
     */
    public static void setTokenNode(final long tokenId, final String nodeName)
            throws WorkflowException {
        log.debug("setTokenNode({}, {})", new Object[] { tokenId, nodeName });
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            final org.jbpm.graph.exe.Token t = jbpmContext.getToken(tokenId);
            final org.jbpm.graph.def.Node node = t.getProcessInstance()
                    .getProcessDefinition().getNode(nodeName);
            t.setNode(node);
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("setTokenNode: void");
    }

    /**
     * End Token
     */
    public static void endToken(final long tokenId) throws WorkflowException {
        log.debug("endToken({})", tokenId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();

        try {
            jbpmContext.getToken(tokenId).end();
            jbpmContext.getSession().flush();
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("endToken: void");
    }
}
