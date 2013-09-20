package com.openkm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.core.jndi.RegistryHelper;

public class ExportImportTest {

    public static void main(final String[] args) throws Exception {
        removeRepository("repo_old");
        removeRepository("repo_new");
        final Session seOld = getSession("repo_old");
        final Node okmRootOld = createRoot(seOld);
        System.out.println("okmRootNew: " + okmRootOld.getUUID());

        // Add a new node
        final Node fileOld = okmRootOld.addNode("prueba", "nt:file");
        fileOld.addMixin("mix:referenceable");
        fileOld.addMixin("mix:lockable");
        fileOld.addMixin("mix:versionable");
        final Node contentOld = fileOld.addNode("jcr:content", "nt:resource");
        contentOld.setProperty("jcr:mimeType", "text/plain");
        contentOld.setProperty("jcr:data", "En un lugar de La Mancha...");
        contentOld.setProperty("jcr:lastModified", Calendar.getInstance());
        okmRootOld.save();

        System.out.println("fileOld: " + fileOld.getUUID());
        System.out.println("contentOld: " + contentOld.getUUID());

        System.out.println("isCheckedOut: " + fileOld.isCheckedOut());
        fileOld.checkin();
        System.out.println("isCheckedOut: " + fileOld.isCheckedOut());

        System.out.println("\n ---- VERSIONS ---- ");
        for (final VersionIterator vi = fileOld.getVersionHistory()
                .getAllVersions(); vi.hasNext();) {
            final Version v = vi.nextVersion();
            System.out.println("Version Path: " + v.getPath());
            System.out.println("Version Name: " + v.getName());
        }
        System.out.println(" --------------------- \n");

        // Export system view
        seOld.exportSystemView("/okm:root", new FileOutputStream(
                "okmRootSysView.xml"), false, false);
        seOld.exportSystemView("/jcr:system/jcr:versionStorage",
                new FileOutputStream("versionStorageSysView.xml"), false, false);

        // Create new repo and import system view
        final Session seNew = getSession("repo_new");
        seNew.getWorkspace().importXML("/",
                new FileInputStream("okmRootSysView.xml"),
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
        seNew.getWorkspace().importXML("/jcr:system",
                new FileInputStream("versionStorageSysView.xml"),
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

        final Node okmRootNew = seNew.getRootNode().getNode("okm:root");
        System.out.println("okmRootNew: " + okmRootNew.getUUID());
        final Node fileNew = okmRootNew.getNode("prueba");
        System.out.println("fileNew: " + fileNew.getUUID());
        final Node contentNew = fileNew.getNode("jcr:content");
        System.out.println("contentNew: " + contentNew.getUUID());
        System.out.println("isCheckedOut: " + fileNew.isCheckedOut());

        System.out.println("\n ---- VERSIONS ---- ");
        for (final VersionIterator vi = fileNew.getVersionHistory()
                .getAllVersions(); vi.hasNext();) {
            final Version v = vi.nextVersion();
            System.out.println("Version Path: " + v.getPath());
            System.out.println("Version Name: " + v.getName());
        }
    }

    /**
     * 
     */
    public static void removeRepository(final String repHomeDir) {
        // Remove previous repo
        try {
            FileUtils.deleteDirectory(new File(repHomeDir));
        } catch (final IOException e) {
            System.err.println("No previous repo");
        }
    }

    /**
     * 
     */
    public static Session getSession(final String repHomeDir) throws Exception {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory");
        env.put(Context.PROVIDER_URL, "localhost");
        final InitialContext ctx = new InitialContext(env);

        // Repository config
        final String configFile = "repositoryImpExp.xml";
        RegistryHelper.registerRepository(ctx, "repo", configFile, repHomeDir,
                true);

        // Obtain the repository through a JNDI lookup
        final Repository r = (Repository) ctx.lookup("repo");

        // Create a new repository session, after authenticating
        final Session session = r.login(
                new SimpleCredentials("paco", "".toCharArray()), null);
        System.out.println("Session: " + session);
        return session;
    }

    /**
     * 
     */
    public static Node createRoot(final Session session) throws Exception {
        // Namespace registration
        final Workspace ws = session.getWorkspace();
        ws.getNamespaceRegistry().registerNamespace("okm",
                "http://www.pepito.org/1.0");

        // Node creation
        final Node root = session.getRootNode();
        final Node okmRoot = root.addNode("okm:root", "nt:folder");
        okmRoot.addMixin("mix:referenceable");
        session.save();
        System.out.println("okm:root created.");
        return okmRoot;
    }
}
