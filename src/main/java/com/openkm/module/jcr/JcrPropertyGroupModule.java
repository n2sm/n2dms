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

package com.openkm.module.jcr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.PropertyGroup;
import com.openkm.bean.form.CheckBox;
import com.openkm.bean.form.FormElement;
import com.openkm.bean.form.Input;
import com.openkm.bean.form.Option;
import com.openkm.bean.form.Select;
import com.openkm.bean.form.SuggestBox;
import com.openkm.bean.form.TextArea;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.NoSuchPropertyException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.Ref;
import com.openkm.core.RepositoryException;
import com.openkm.extension.core.ExtensionException;
import com.openkm.extension.core.PropertyGroupExtensionManager;
import com.openkm.module.PropertyGroupModule;
import com.openkm.module.jcr.base.BasePropertyGroupModule;
import com.openkm.module.jcr.base.BaseScriptingModule;
import com.openkm.module.jcr.stuff.JCRUtils;
import com.openkm.module.jcr.stuff.JcrSessionManager;
import com.openkm.util.FormUtils;
import com.openkm.util.UserActivity;

public class JcrPropertyGroupModule implements PropertyGroupModule {
    private static Logger log = LoggerFactory
            .getLogger(JcrPropertyGroupModule.class);

    @Override
    public void addGroup(final String token, final String nodePath,
            final String grpName) throws NoSuchGroupException, LockException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            DatabaseException, ExtensionException {
        log.debug("addGroup({}, {}, {})", new Object[] { token, nodePath,
                grpName });
        Node documentNode = null;
        Session session = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            documentNode = session.getRootNode().getNode(nodePath.substring(1));

            // PRE
            final Ref<Node> refDocumentNode = new Ref<Node>(documentNode);
            PropertyGroupExtensionManager.getInstance().preAddGroup(session,
                    refDocumentNode, grpName);

            BasePropertyGroupModule.addGroup(session, documentNode, grpName);

            // POST
            PropertyGroupExtensionManager.getInstance().postAddGroup(session,
                    refDocumentNode, grpName);

            // Check scripting
            BaseScriptingModule.checkScripts(session, documentNode,
                    documentNode, "ADD_PROPERTY_GROUP");

            // Activity log
            UserActivity.log(session.getUserID(), "ADD_PROPERTY_GROUP",
                    documentNode.getUUID(), nodePath, grpName);
        } catch (final javax.jcr.nodetype.NoSuchNodeTypeException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new NoSuchGroupException(e.getMessage(), e);
        } catch (final javax.jcr.lock.LockException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new LockException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("addGroup: void");
    }

    @Override
    public void removeGroup(final String token, final String nodePath,
            final String grpName) throws AccessDeniedException,
            NoSuchGroupException, LockException, PathNotFoundException,
            RepositoryException, DatabaseException, ExtensionException {
        log.debug("removeGroup({}, {}, {})", new Object[] { token, nodePath,
                grpName });
        Node documentNode = null;
        Session session = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            documentNode = session.getRootNode().getNode(nodePath.substring(1));

            // PRE
            final Ref<Node> refDocumentNode = new Ref<Node>(documentNode);
            PropertyGroupExtensionManager.getInstance().preRemoveGroup(session,
                    refDocumentNode, grpName);

            synchronized (documentNode) {
                documentNode.removeMixin(grpName);
                documentNode.save();
            }

            // POST
            PropertyGroupExtensionManager.getInstance().postRemoveGroup(
                    session, refDocumentNode, grpName);

            // Check scripting
            BaseScriptingModule.checkScripts(session, documentNode,
                    documentNode, "REMOVE_PROPERTY_GROUP");

            // Activity log
            UserActivity.log(session.getUserID(), "REMOVE_PROPERTY_GROUP",
                    documentNode.getUUID(), nodePath, grpName);
        } catch (final javax.jcr.nodetype.NoSuchNodeTypeException e) {
            log.error(e.getMessage(), e);
            throw new NoSuchGroupException(e.getMessage(), e);
        } catch (final javax.jcr.lock.LockException e) {
            log.error(e.getMessage(), e);
            throw new LockException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("removeGroup: void");
    }

    @Override
    public List<PropertyGroup> getGroups(final String token,
            final String nodePath) throws IOException, ParseException,
            PathNotFoundException, RepositoryException, DatabaseException {
        log.debug("getGroups({}, {})", token, nodePath);
        final ArrayList<PropertyGroup> ret = new ArrayList<PropertyGroup>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Node documentNode = session.getRootNode().getNode(
                    nodePath.substring(1));
            final NodeType[] nt = documentNode.getMixinNodeTypes();
            final Map<PropertyGroup, List<FormElement>> pgf = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);

            // Only return registered property definitions
            for (final NodeType element : nt) {
                if (element.getName().startsWith(PropertyGroup.GROUP + ":")) {
                    for (final PropertyGroup pg : pgf.keySet()) {
                        if (pg.getName().equals(element.getName())) {
                            ret.add(pg);
                        }
                    }
                }
            }
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getGroups: {}", ret);
        return ret;
    }

    @Override
    public List<PropertyGroup> getAllGroups(final String token)
            throws IOException, ParseException, RepositoryException,
            DatabaseException {
        log.debug("getAllGroups({})", token);
        final ArrayList<PropertyGroup> ret = new ArrayList<PropertyGroup>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final NodeTypeManager ntm = session.getWorkspace()
                    .getNodeTypeManager();
            final Map<PropertyGroup, List<FormElement>> pgf = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);

            // Only return registered property definitions
            for (final NodeTypeIterator nti = ntm.getMixinNodeTypes(); nti
                    .hasNext();) {
                final NodeType nt = nti.nextNodeType();

                if (nt.getName().startsWith(PropertyGroup.GROUP + ":")) {
                    for (final PropertyGroup pg : pgf.keySet()) {
                        if (pg.getName().equals(nt.getName())) {
                            ret.add(pg);
                        }
                    }
                }
            }
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getAllGroups: {}", ret);
        return ret;
    }

    @Override
    public List<FormElement> getProperties(final String token,
            final String nodePath, final String grpName) throws IOException,
            ParseException, NoSuchGroupException, PathNotFoundException,
            RepositoryException, DatabaseException {
        log.debug("getProperties({}, {}, {})", new Object[] { token, nodePath,
                grpName });
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Map<PropertyGroup, List<FormElement>> pgfs = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);
            final List<FormElement> pgf = FormUtils.getPropertyGroupForms(pgfs,
                    grpName);
            final Node documentNode = session.getRootNode().getNode(
                    nodePath.substring(1));
            final NodeTypeManager ntm = session.getWorkspace()
                    .getNodeTypeManager();
            final NodeType nt = ntm.getNodeType(grpName);
            final PropertyDefinition[] pd = nt.getDeclaredPropertyDefinitions();

            for (final FormElement fe : pgf) {
                for (int i = 0; i < pd.length; i++) {
                    // Only return registered property definitions
                    if (fe.getName().equals(pd[i].getName())) {
                        try {
                            final Property prop = documentNode
                                    .getProperty(pd[i].getName());

                            if (fe instanceof Select
                                    && ((Select) fe).getType().equals(
                                            Select.TYPE_MULTIPLE)
                                    && pd[i].isMultiple()) {
                                final Value[] values = prop.getValues();
                                final Select select = (Select) fe;

                                for (final Option opt : select.getOptions()) {
                                    for (final Value value : values) {
                                        if (opt.getValue().equals(
                                                value.getString())) {
                                            select.setValue(select.getValue()
                                                    .concat(opt.getValue())
                                                    .concat(","));
                                            opt.setSelected(true);
                                            // log.info("Option: {}, TRUE", opt.getLabel());
                                        } else {
                                            // opt.setSelected(false);
                                            // log.info("Option: {}, FALSE", opt.getLabel());
                                        }
                                    }
                                }

                                if (select.getValue().endsWith(",")) {
                                    select.setValue(select
                                            .getValue()
                                            .substring(
                                                    0,
                                                    select.getValue().length() - 1));
                                }
                            } else if (!pd[i].isMultiple()) {
                                final Value value = prop.getValue();

                                if (fe instanceof Input) {
                                    ((Input) fe).setValue(value.getString());
                                } else if (fe instanceof SuggestBox) {
                                    ((SuggestBox) fe).setValue(value
                                            .getString());
                                } else if (fe instanceof CheckBox) {
                                    ((CheckBox) fe).setValue(Boolean
                                            .parseBoolean(value.getString()));
                                } else if (fe instanceof TextArea) {
                                    ((TextArea) fe).setValue(value.getString());
                                } else if (fe instanceof Select) {
                                    if (!value.getString().equals("")) {
                                        // If has stored value, prioritize over defaults  
                                        for (final Option opt : ((Select) fe)
                                                .getOptions()) {
                                            if (opt.getValue().equals(
                                                    value.getString())) {
                                                ((Select) fe).setValue(opt
                                                        .getValue());
                                                opt.setSelected(true);
                                            } else {
                                                opt.setSelected(false);
                                            }
                                        }
                                    }
                                } else {
                                    throw new ParseException(
                                            "Unknown property definition: "
                                                    + pd[i].getName());
                                }
                            } else {
                                throw new ParseException(
                                        "Inconsistent property definition: "
                                                + pd[i].getName());
                            }
                        } catch (final javax.jcr.PathNotFoundException e) {
                            // Maybe the property is not found because was added after the assignment
                            // throw new RepositoryException("Requested property not found: "+e.getMessage());
                        }
                    }
                }
            }

            // Activity log
            UserActivity.log(session.getUserID(),
                    "GET_PROPERTY_GROUP_PROPERTIES", documentNode.getUUID(),
                    nodePath, grpName + ", " + pgf);

            log.debug("getProperties: {}", pgf);
            return pgf;
        } catch (final javax.jcr.nodetype.NoSuchNodeTypeException e) {
            log.error(e.getMessage(), e);
            throw new NoSuchGroupException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }
    }

    @Override
    public void setProperties(final String token, final String nodePath,
            final String grpName, final List<FormElement> properties)
            throws IOException, ParseException, NoSuchPropertyException,
            NoSuchGroupException, LockException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException,
            ExtensionException {
        log.debug("setProperties({}, {}, {}, {})", new Object[] { token,
                nodePath, grpName, properties });
        Node documentNode = null;
        Session session = null;

        if (Config.SYSTEM_READONLY) {
            throw new AccessDeniedException("System is in read-only mode");
        }

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final NodeTypeManager ntm = session.getWorkspace()
                    .getNodeTypeManager();
            final NodeType nt = ntm.getNodeType(grpName);
            final PropertyDefinition[] pd = nt.getDeclaredPropertyDefinitions();
            documentNode = session.getRootNode().getNode(nodePath.substring(1));

            // PRE
            final Ref<Node> refDocumentNode = new Ref<Node>(documentNode);
            PropertyGroupExtensionManager.getInstance().preSetProperties(
                    session, refDocumentNode, grpName, properties);

            synchronized (documentNode) {
                // Maybe the property is not found because was added after the assignment,
                // so we check if there are any missing node property, and then will remove
                // and add the mixing again.
                for (final FormElement fe : properties) {
                    for (int i = 0; i < pd.length; i++) {
                        if (fe.getName().equals(pd[i].getName())) {
                            if (documentNode.isNodeType(grpName)
                                    && !documentNode.hasProperty(pd[i]
                                            .getName())) {
                                documentNode.removeMixin(grpName);
                                documentNode.addMixin(grpName);
                            }
                        }
                    }
                }

                // Now we can safely set all property values.
                for (final FormElement fe : properties) {
                    for (final PropertyDefinition element : pd) {
                        // Only return registered property definitions
                        if (fe.getName().equals(element.getName())) {
                            try {
                                BasePropertyGroupModule.setPropertyValue(
                                        documentNode, element, fe);
                            } catch (final javax.jcr.PathNotFoundException e) {
                                throw new RepositoryException(
                                        "Requested property not found: "
                                                + e.getMessage());
                            }
                        }
                    }
                }
            }

            documentNode.save();

            // POST
            PropertyGroupExtensionManager.getInstance().postSetProperties(
                    session, refDocumentNode, grpName, properties);

            // Check scripting
            BaseScriptingModule.checkScripts(session, documentNode,
                    documentNode, "SET_PROPERTY_GROUP_PROPERTIES");

            // Activity log
            UserActivity.log(session.getUserID(),
                    "SET_PROPERTY_GROUP_PROPERTIES", documentNode.getUUID(),
                    nodePath, grpName + ", " + properties);
        } catch (final javax.jcr.nodetype.NoSuchNodeTypeException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new NoSuchPropertyException(e.getMessage(), e);
        } catch (final javax.jcr.lock.LockException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new LockException(e.getMessage(), e);
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            JCRUtils.discardsPendingChanges(documentNode);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("setProperties: void");
    }

    @Override
    public List<FormElement> getPropertyGroupForm(final String token,
            final String grpName) throws IOException, ParseException,
            RepositoryException, DatabaseException {
        log.debug("getPropertyGroupForm({}, {})", token, grpName);
        final List<FormElement> ret = new ArrayList<FormElement>();
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final NodeTypeManager ntm = session.getWorkspace()
                    .getNodeTypeManager();
            final NodeType nt = ntm.getNodeType(grpName);
            final PropertyDefinition[] pd = nt.getDeclaredPropertyDefinitions();
            final Map<PropertyGroup, List<FormElement>> pgf = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);
            final List<FormElement> tmp = FormUtils.getPropertyGroupForms(pgf,
                    grpName);

            // Only return registered property definitions
            for (final FormElement fe : tmp) {
                for (final PropertyDefinition element : pd) {
                    if (fe.getName().equals(element.getName())) {
                        ret.add(fe);
                    }
                }
            }
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("getPropertyGroupForm: {}", ret);
        return ret;
    }

    @Override
    public boolean hasGroup(final String token, final String nodePath,
            final String grpName) throws IOException, ParseException,
            PathNotFoundException, RepositoryException, DatabaseException {
        log.debug("hasGroup({}, {}, {})", new Object[] { token, nodePath,
                grpName });
        boolean ret = false;
        Session session = null;

        try {
            if (token == null) {
                session = JCRUtils.getSession();
            } else {
                session = JcrSessionManager.getInstance().get(token);
            }

            final Node documentNode = session.getRootNode().getNode(
                    nodePath.substring(1));
            final NodeType[] nt = documentNode.getMixinNodeTypes();
            final Map<PropertyGroup, List<FormElement>> pgf = FormUtils
                    .parsePropertyGroupsForms(Config.PROPERTY_GROUPS_XML);

            // Only return registered property definitions
            for (final NodeType element : nt) {
                if (element.getName().startsWith(PropertyGroup.GROUP + ":")) {
                    for (final PropertyGroup pg : pgf.keySet()) {
                        if (pg.getName().equals(element.getName())
                                && pg.getName().equals(grpName)) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        } catch (final javax.jcr.PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new PathNotFoundException(e.getMessage(), e);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        } finally {
            if (token == null) {
                JCRUtils.logout(session);
            }
        }

        log.debug("hasGroup: {}", ret);
        return ret;
    }
}
