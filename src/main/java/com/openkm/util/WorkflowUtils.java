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

package com.openkm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hibernate.Hibernate;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.db.GraphSession;
import org.jbpm.db.LoggingSession;
import org.jbpm.logging.log.ProcessLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.openkm.bean.workflow.Comment;
import com.openkm.bean.workflow.ProcessDefinition;
import com.openkm.bean.workflow.ProcessInstance;
import com.openkm.bean.workflow.TaskInstance;
import com.openkm.bean.workflow.Token;
import com.openkm.bean.workflow.Transition;
import com.openkm.core.WorkflowException;

public class WorkflowUtils {
    private static Logger log = LoggerFactory.getLogger(WorkflowUtils.class);

    /**
     * Get process instances which competes to a document or folder.
     */
    @SuppressWarnings("rawtypes")
    public static List<ProcessInstance> findProcessInstancesByNode(
            final String uuid) throws WorkflowException {
        log.debug("findProcessInstanceByNode({})", uuid);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessInstance> al = new ArrayList<ProcessInstance>();

        try {
            if (uuid != null) {
                final GraphSession graphSession = jbpmContext.getGraphSession();
                final List procDefList = graphSession
                        .findAllProcessDefinitions();

                for (final Iterator itPd = procDefList.iterator(); itPd
                        .hasNext();) {
                    final org.jbpm.graph.def.ProcessDefinition procDef = (org.jbpm.graph.def.ProcessDefinition) itPd
                            .next();
                    final List procInsList = graphSession
                            .findProcessInstances(procDef.getId());

                    for (final Iterator itPi = procInsList.iterator(); itPi
                            .hasNext();) {
                        final org.jbpm.graph.exe.ProcessInstance procIns = (org.jbpm.graph.exe.ProcessInstance) itPi
                                .next();

                        if (uuid.equals(procIns.getContextInstance()
                                .getVariable("uuid"))) {
                            al.add(WorkflowUtils.copy(procIns));
                        }
                    }
                }
            }
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findProcessInstanceByNode: {}", al);
        return al;
    }

    /**
     * Get log entries related to a process instance.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<WorkflowUtils.ProcessInstanceLogEntry> findLogsByProcessInstance(
            final long processInstanceId) throws WorkflowException {
        log.debug("findLogsByProcessInstance({})", processInstanceId);
        final JbpmContext jbpmContext = JBPMUtils.getConfig()
                .createJbpmContext();
        final List<ProcessInstanceLogEntry> al = new ArrayList<ProcessInstanceLogEntry>();

        try {
            final LoggingSession logSession = jbpmContext.getLoggingSession();
            final Map<Object, List> logs = logSession
                    .findLogsByProcessInstance(processInstanceId);

            for (final Entry<Object, List> entry : logs.entrySet()) {
                final org.jbpm.graph.exe.Token token = (org.jbpm.graph.exe.Token) entry
                        .getKey();
                final org.jbpm.graph.exe.ProcessInstance processInstance = token
                        .getProcessInstance();
                final org.jbpm.graph.def.ProcessDefinition processDefinition = processInstance
                        .getProcessDefinition();

                for (final Object obj : entry.getValue()) {
                    final ProcessLog pLog = (ProcessLog) obj;
                    final String className = obj.getClass().getSimpleName();
                    final String classInfo = pLog.toString();
                    String pType = null;
                    String pInfo = null;

                    if (className.endsWith("Log")) {
                        pType = className
                                .substring(0, className.indexOf("Log"));
                    } else {
                        pType = className;
                    }

                    if (classInfo.endsWith("]")) {
                        pInfo = classInfo.substring(classInfo.indexOf('[') + 1,
                                classInfo.length() - 1);
                    } else {
                        pInfo = classInfo;
                    }

                    final ProcessInstanceLogEntry pile = new ProcessInstanceLogEntry(
                            processDefinition.getId(),
                            processDefinition.getName(),
                            processInstance.getId(), token.getFullName(),
                            pLog.getDate(), pType, pInfo);
                    al.add(pile);
                }
            }

            // Sort results
            Collections.sort(al);
        } catch (final JbpmException e) {
            throw new WorkflowException(e.getMessage(), e);
        } finally {
            jbpmContext.close();
        }

        log.debug("findLogsByProcessInstance: {}", al);
        return al;
    }

    public static final class ProcessInstanceLogEntry implements
            Comparable<ProcessInstanceLogEntry> {
        private long processInstanceId;

        private long processDefinitionId;

        private String processDefinitionName;

        private String token;

        private Date date;

        private String type;

        private String info;

        public ProcessInstanceLogEntry(final long processDefinitionId,
                final String processDefinitionName,
                final long processInstanceId, final String token,
                final Date date, final String type, final String info) {
            this.processDefinitionId = processDefinitionId;
            this.processDefinitionName = processDefinitionName;
            this.processInstanceId = processInstanceId;
            this.token = token;
            this.date = date;
            this.type = type;
            this.info = info;
        }

        public long getProcessInstanceId() {
            return processInstanceId;
        }

        public void setProcessInstanceId(final long processInstanceId) {
            this.processInstanceId = processInstanceId;
        }

        public long getProcessDefinitionId() {
            return processDefinitionId;
        }

        public void setProcessDefinitionId(final long processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }

        public String getProcessDefinitionName() {
            return processDefinitionName;
        }

        public void setProcessDefinitionName(final String processDefinitionName) {
            this.processDefinitionName = processDefinitionName;
        }

        public String getToken() {
            return token;
        }

        public void setToken(final String token) {
            this.token = token;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(final Date date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(final String info) {
            this.info = info;
        }

        @Override
        public int compareTo(final ProcessInstanceLogEntry o) {
            if (date != null && o.getDate() != null) {
                return date.compareTo(o.getDate());
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("processDefinitionId=").append(processDefinitionId);
            sb.append(", processDefinitionName=").append(processDefinitionName);
            sb.append(", processInstanceId=").append(processInstanceId);
            sb.append(", token=").append(token);
            sb.append(", date=").append(date);
            sb.append(", type=").append(type);
            sb.append(", info=").append(info);
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * Copy process definition
     */
    @SuppressWarnings("rawtypes")
    public static ProcessDefinition copy(
            final org.jbpm.graph.def.ProcessDefinition pd) {
        final ProcessDefinition vo = new ProcessDefinition();

        vo.setName(pd.getName());
        vo.setDescription(pd.getDescription());
        vo.setId(pd.getId());
        vo.setVersion(pd.getVersion());
        final ArrayList<String> al = new ArrayList<String>();

        for (final Object element : pd.getNodes()) {
            final org.jbpm.graph.def.Node n = (org.jbpm.graph.def.Node) element;
            al.add(n.getName());
        }

        vo.setNodes(al);

        return vo;
    }

    /**
     * Copy process instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ProcessInstance copy(
            final org.jbpm.graph.exe.ProcessInstance pi) {
        final ProcessInstance vo = new ProcessInstance();

        if (pi.getStart() != null) {
            final Calendar start = Calendar.getInstance();
            start.setTime(pi.getStart());
            vo.setStart(start);
        }

        if (pi.getEnd() != null) {
            final Calendar end = Calendar.getInstance();
            end.setTime(pi.getEnd());
            vo.setEnd(end);
        }

        vo.setId(pi.getId());
        vo.setVersion(pi.getVersion());
        vo.setKey(pi.getKey());
        vo.setEnded(pi.hasEnded());
        vo.setSuspended(pi.isSuspended());

        // TODO https://jira.jboss.org/jira/browse/JBPM-1778
        if (pi.getContextInstance().getVariables() != null) {
            vo.setVariables(pi.getContextInstance().getVariables());

            // Workaround for LazyInitializationException
            for (final String key : vo.getVariables().keySet()) {
                Hibernate.initialize(vo.getVariables().get(key));
            }
        } else {
            vo.setVariables(new HashMap<String, Object>());
        }

        final ArrayList<Token> al = new ArrayList<Token>();

        for (final Iterator it = pi.findAllTokens().iterator(); it.hasNext();) {
            final org.jbpm.graph.exe.Token tk = (org.jbpm.graph.exe.Token) it
                    .next();
            al.add(copy(tk));
        }

        vo.setAllTokens(al);
        vo.setRootToken(copy(pi.getRootToken()));
        vo.setProcessDefinition(copy(pi.getProcessDefinition()));

        return vo;
    }

    /**
     * Copy task instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static TaskInstance copy(final org.jbpm.taskmgmt.exe.TaskInstance ti) {
        final TaskInstance vo = new TaskInstance();

        if (ti.getCreate() != null) {
            final Calendar create = Calendar.getInstance();
            create.setTime(ti.getCreate());
            vo.setCreate(create);
        }

        if (ti.getStart() != null) {
            final Calendar start = Calendar.getInstance();
            start.setTime(ti.getStart());
            vo.setStart(start);
        }

        if (ti.getEnd() != null) {
            final Calendar end = Calendar.getInstance();
            end.setTime(ti.getEnd());
            vo.setEnd(end);
        }

        if (ti.getDueDate() != null) {
            final Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(ti.getDueDate());
            vo.setDueDate(dueDate);
        }

        vo.setId(ti.getId());
        vo.setName(ti.getName());
        vo.setDescription(ti.getDescription());
        vo.setVariables(ti.getVariables());
        final ArrayList<Comment> al = new ArrayList<Comment>();

        for (final Iterator it = ti.getComments().iterator(); it.hasNext();) {
            final org.jbpm.graph.exe.Comment c = (org.jbpm.graph.exe.Comment) it
                    .next();
            final Comment tc = new Comment();
            tc.setActorId(c.getActorId());
            tc.setMessage(c.getMessage());
            final Calendar time = Calendar.getInstance();
            time.setTime(c.getTime());
            tc.setTime(time);
            al.add(tc);
        }

        vo.setComments(al);
        vo.setActorId(ti.getActorId());
        vo.setOpen(ti.isOpen());
        vo.setLast(ti.isLast());
        vo.setSuspended(ti.isSuspended());
        vo.setStartTaskInstance(ti.isStartTaskInstance());
        final HashSet<String> hs = new HashSet<String>();

        for (final Object element : ti.getPooledActors()) {
            hs.add(element.toString());
        }

        vo.setPooledActors(hs);
        final ArrayList<Transition> alT = new ArrayList<Transition>();

        // TODO http://www.jboss.com/index.html?module=bb&op=viewtopic&t=144049
        if (ti.getEnd() == null) {
            for (final Iterator it = ti.getAvailableTransitions().iterator(); it
                    .hasNext();) {
                final org.jbpm.graph.def.Transition tr = (org.jbpm.graph.def.Transition) it
                        .next();
                alT.add(copy(tr));
            }
        }

        // Sort
        Collections.sort(alT);
        vo.setAvailableTransitions(alT);

        vo.setToken(copy(ti.getToken()));
        vo.setProcessInstance(copy(ti.getProcessInstance()));

        return vo;
    }

    /**
     * Copy token
     */
    @SuppressWarnings("rawtypes")
    public static Token copy(final org.jbpm.graph.exe.Token t) {
        Token vo = null;

        if (t != null) {
            vo = new Token();
            vo.setName(t.getName());
            vo.setId(t.getId());
            vo.setSuspended(t.isSuspended());
            final ArrayList<Comment> alC = new ArrayList<Comment>();

            if (t.getComments() != null) {
                for (final Iterator it = t.getComments().iterator(); it
                        .hasNext();) {
                    final org.jbpm.graph.exe.Comment c = (org.jbpm.graph.exe.Comment) it
                            .next();
                    alC.add(copy(c));
                }
            }

            vo.setComments(alC);
            vo.setParent(copy(t.getParent()));

            if (t.getNode() != null) {
                vo.setNode(t.getNode().getName());
            }

            if (t.getStart() != null) {
                final Calendar start = Calendar.getInstance();
                start.setTime(t.getStart());
                vo.setStart(start);
            }

            if (t.getEnd() != null) {
                final Calendar end = Calendar.getInstance();
                end.setTime(t.getEnd());
                vo.setEnd(end);
            }

            final ArrayList<Transition> alT = new ArrayList<Transition>();

            try {
                for (final Iterator it = t.getAvailableTransitions().iterator(); it
                        .hasNext();) {
                    final org.jbpm.graph.def.Transition tr = (org.jbpm.graph.def.Transition) it
                            .next();
                    alT.add(copy(tr));
                }
            } catch (final JbpmException e) {
                log.warn("Trasition problem: {}", e.getMessage());
            }

            // Sort
            Collections.sort(alT);
            vo.setAvailableTransitions(alT);

            // Causes recursion
            // vo.setProcessInstance(copy(t.getProcessInstance()));
        }

        return vo;
    }

    /**
     * Copy comment
     */
    public static Comment copy(final org.jbpm.graph.exe.Comment c) {
        final Comment vo = new Comment();

        vo.setActorId(c.getActorId());
        vo.setMessage(c.getMessage());
        final Calendar time = Calendar.getInstance();
        time.setTime(c.getTime());
        vo.setTime(time);

        return vo;
    }

    /**
     * Copy transition
     */
    public static Transition copy(final org.jbpm.graph.def.Transition t) {
        final Transition vo = new Transition();

        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setFrom(t.getFrom().getName());
        vo.setTo(t.getTo().getName());

        return vo;
    }

    /**
     * Get diagram info
     */
    public static WorkflowUtils.DiagramInfo getDiagramInfo(final InputStream is) {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory
                    .newInstance();
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                    false);
            final DocumentBuilder db = dbf.newDocumentBuilder();

            if (is != null) {
                final Document doc = db.parse(is);
                doc.getDocumentElement().normalize();
                final Element processDiagramElement = doc.getDocumentElement();
                final String widthString = processDiagramElement
                        .getAttribute("width");
                final String heightString = processDiagramElement
                        .getAttribute("height");
                final List<DiagramNodeInfo> diagramNodeInfoList = new ArrayList<DiagramNodeInfo>();
                final NodeList nodeNodeList = processDiagramElement
                        .getElementsByTagName("node");
                final int nodeNodeListLength = nodeNodeList.getLength();

                for (int i = 0; i < nodeNodeListLength; i++) {
                    final Node nodeNode = nodeNodeList.item(i);

                    if (nodeNode instanceof Node
                            && nodeNode.getParentNode() == processDiagramElement) {
                        final Element nodeElement = (Element) nodeNode;
                        final String nodeName = nodeElement
                                .getAttribute("name");
                        final String nodeXString = nodeElement
                                .getAttribute("x");
                        final String nodeYString = nodeElement
                                .getAttribute("y");
                        final String nodeWidthString = nodeElement
                                .getAttribute("width");
                        final String nodeHeightString = nodeElement
                                .getAttribute("height");
                        final DiagramNodeInfo nodeInfo = new DiagramNodeInfo(
                                nodeName, Integer.parseInt(nodeXString),
                                Integer.parseInt(nodeYString),
                                Integer.parseInt(nodeWidthString),
                                Integer.parseInt(nodeHeightString));
                        diagramNodeInfoList.add(nodeInfo);
                    }
                }

                final DiagramInfo diagramInfo = new DiagramInfo(
                        Integer.parseInt(heightString),
                        Integer.parseInt(widthString), diagramNodeInfoList);

                return diagramInfo;
            }
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Diagram info helper
     */
    public static final class DiagramInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int width;

        private final int height;

        private final Map<String, DiagramNodeInfo> nodeMap;

        public DiagramInfo(final int height, final int width,
                final List<DiagramNodeInfo> nodeList) {
            this.height = height;
            this.width = width;
            final LinkedHashMap<String, DiagramNodeInfo> map = new LinkedHashMap<String, DiagramNodeInfo>();
            for (final DiagramNodeInfo nodeInfo : nodeList) {
                map.put(nodeInfo.getName(), nodeInfo);
            }
            nodeMap = Collections.unmodifiableMap(map);
        }

        public int getHeight() {
            return height;
        }

        public Map<String, DiagramNodeInfo> getNodeMap() {
            return nodeMap;
        }

        public List<DiagramNodeInfo> getNodes() {
            return Collections.unmodifiableList(new ArrayList<DiagramNodeInfo>(
                    nodeMap.values()));
        }

        public int getWidth() {
            return width;
        }
    }

    /**
     * Diagram node info
     */
    public static final class DiagramNodeInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String name;

        private final int x;

        private final int y;

        private final int width;

        private final int height;

        public DiagramNodeInfo(final String name, final int x, final int y,
                final int width, final int height) {
            this.height = height;
            this.name = name;
            this.width = width;
            this.x = x;
            this.y = y;
        }

        public int getHeight() {
            return height;
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("name=").append(name);
            sb.append(", x=").append(x);
            sb.append(", y=").append(y);
            sb.append(", width=").append(width);
            sb.append(", height=").append(height);
            sb.append("}");
            return sb.toString();
        }
    }
}
