package com.openkm.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NamespaceException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyLockToken {
    private static Logger log = LoggerFactory.getLogger(Dummy.class);

    private static String repHomeDir = "repotest2";

    private static Session systemSession = null;

    private static Repository repository = null;

    public static void main(final String[] args) throws NamingException,
            RepositoryException, FileNotFoundException {
        log.debug("*** DESTROY REPOSITORY ***");
        removeRepository();

        log.debug("*** CREATE REPOSITORY ***");
        createRepository();

        log.debug("*** USER LOGIN ***");
        final Session userSession = login("paco", "pepe");

        log.debug("*** GET MY ROOT NODE ***");
        final Node rootNode = userSession.getRootNode();
        final Node myRoot = rootNode.getNode("my:root");

        log.debug("*** BEGIN: ADD A DOCUMENT NODE ***");
        final Node fileNode = myRoot.addNode("perico", "nt:file");
        fileNode.addMixin("mix:lockable");
        final Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
        contentNode.setProperty("jcr:data", new ByteArrayInputStream(
                "Texto de pruebas".getBytes()));
        contentNode.setProperty("jcr:mimeType", "text/plain");
        contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
        myRoot.save();
        log.debug("*** END: ADD A DOCUMENT NODE ***");

        log.debug("*** LOCK NODE ***");
        fileNode.lock(true, false);
        String[] tokens = userSession.getLockTokens();
        for (final String token : tokens) {
            log.debug("Token: " + token);
        }

        log.debug("*** UNLOCK NODE ***");
        fileNode.unlock();
        tokens = userSession.getLockTokens();
        for (final String token : tokens) {
            log.debug("Token: " + token);
        }

        log.debug("*** SAY BYE ***");
        userSession.logout();
    }

    /**
     * 
     */
    private static void removeRepository() {
        try {
            FileUtils.deleteDirectory(new File(repHomeDir));
        } catch (final IOException e) {
            System.err.println("No previous repo");
        }
    }

    /**
     *
     */
    public static Session login(final String user, final String pass)
            throws NamingException, RepositoryException, LoginException,
            NoSuchWorkspaceException {
        final Repository repository = getRepository();
        final Session session = repository.login(new SimpleCredentials(user,
                pass.toCharArray()), null);
        log.debug("Session: " + session);
        return session;
    }

    /**
     * 
     */
    public static synchronized Repository getRepository()
            throws RepositoryException {
        if (repository == null) {
            // Repository config
            final String repositoryConfig = "repository2.xml";
            final String repositoryHome = "repotest2";

            final RepositoryConfig config = RepositoryConfig.create(
                    repositoryConfig, repositoryHome);
            repository = RepositoryImpl.create(config);
            log.debug("*** System repository created " + repository);
        }

        return repository;
    }

    /**
     * 
     */
    public static synchronized Session getSystemSession()
            throws LoginException, NoSuchWorkspaceException,
            RepositoryException {
        if (systemSession == null) {
            // System User Session
            systemSession = repository.login(
                    new SimpleCredentials("system", "".toCharArray()), null);
            log.debug("*** System user created " + systemSession.getUserID());
        }

        return systemSession;
    }

    /**
     * 
     */
    public static Node createRepository() throws NamespaceException,
            UnsupportedRepositoryOperationException, AccessDeniedException,
            RepositoryException, ItemExistsException, PathNotFoundException,
            NoSuchNodeTypeException, LockException, VersionException,
            ConstraintViolationException, InvalidItemStateException {
        // Initialize repository
        // Repository repository = getRepository();
        final Session systemSession = getSystemSession();

        // Namespace registration
        final Workspace ws = systemSession.getWorkspace();
        ws.getNamespaceRegistry().registerNamespace("my",
                "http://www.guia-ubuntu.org/1.0");

        // Node creation
        final Node root = systemSession.getRootNode();
        final Node okmRoot = root.addNode("my:root", "nt:folder");
        okmRoot.addMixin("mix:referenceable");
        systemSession.save();
        log.info("****** Repository created *******");
        return okmRoot;
    }
}
