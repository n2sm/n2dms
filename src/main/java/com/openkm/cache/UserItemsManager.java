package com.openkm.cache;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.ContentInfo;
import com.openkm.bean.Document;
import com.openkm.bean.Repository;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.UserItemsDAO;
import com.openkm.dao.bean.cache.UserItems;
import com.openkm.module.db.base.BaseFolderModule;

public class UserItemsManager {
    private static Logger log = LoggerFactory.getLogger(UserItemsManager.class);

    private static Map<String, UserItems> userItemsMgr = new HashMap<String, UserItems>();

    private static volatile boolean running = false;

    /**
     * Get stored user item
     */
    public static UserItems get(final String uid) {
        UserItems userItems = userItemsMgr.get(uid);

        if (userItems == null) {
            userItems = new UserItems();
            userItems.setUser(uid);
            userItemsMgr.put(uid, userItems);
        }

        return userItems;
    }

    /**
     * Increment document number
     */
    public static synchronized void incDocuments(final String uid,
            final int value) {
        log.debug("incDocuments({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setDocuments(userItems.getDocuments() + value);
    }

    /**
     * Decrement document number
     */
    public static synchronized void decDocuments(final String uid,
            final int value) {
        log.debug("decDocuments({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setDocuments(userItems.getDocuments() - value);
    }

    /**
     * Increment folder number 
     */
    public static synchronized void incFolders(final String uid, final int value) {
        log.debug("incFolders({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setFolders(userItems.getFolders() + value);
    }

    /**
     * Decrement folder number
     */
    public static synchronized void decFolders(final String uid, final int value) {
        log.debug("decFolders({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setFolders(userItems.getFolders() - value);
    }

    /**
     * Increment document size
     */
    public static synchronized void incSize(final String uid, final long value) {
        log.debug("incSize({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setSize(userItems.getSize() + value);
    }

    /**
     * Decrement document size
     */
    public static synchronized void decSize(final String uid, final long value) {
        log.debug("decSize({}, {})", uid, value);
        final UserItems userItems = get(uid);
        userItems.setSize(userItems.getSize() - value);
    }

    /**
     * TODO: Not fully implemented
     */
    public static synchronized void refreshJcrUserItems(final Session session)
            throws RepositoryException {
        log.info("refreshJcrUserItems({})", session);

        try {
            final String statement = "/jcr:root/" + Repository.ROOT
                    + "//element(*, okm:document)[okm:content/@okm:author='"
                    + session.getUserID() + "']";
            final Workspace workspace = session.getWorkspace();
            final QueryManager queryManager = workspace.getQueryManager();
            final Query query = queryManager
                    .createQuery(statement, Query.XPATH);
            final QueryResult result = query.execute();
            long size = 0;

            for (final NodeIterator nit = result.getNodes(); nit.hasNext();) {
                final Node node = nit.nextNode();
                final Node contentNode = node.getNode(Document.CONTENT);
                size += contentNode.getProperty(Document.SIZE).getLong();
            }

            log.info("Size: {}", size);
            final UserItems userItems = new UserItems();
            userItemsMgr.put(session.getUserID(), userItems);
        } catch (final javax.jcr.RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new RepositoryException(e.getMessage(), e);
        }

        log.info("refreshJcrUserItems: void");
    }

    /**
     * Refresh user item cache from database.
     */
    public static synchronized void refreshDbUserItems()
            throws RepositoryException {
        log.debug("refreshDbUserItems({})");
        final Map<String, ContentInfo> totalUserContInfo = new HashMap<String, ContentInfo>();
        final String[] bases = new String[] { Repository.ROOT,
                Repository.CATEGORIES, Repository.TEMPLATES,
                Repository.PERSONAL, Repository.MAIL, Repository.TRASH };

        if (running) {
            log.warn("*** Refresh user items already running ***");
        } else {
            running = true;
            log.info("*** Begin refresh user items ***");

            try {
                for (final String base : bases) {
                    log.info("Calculate user content info from '{}'...", base);
                    final String uuid = NodeBaseDAO.getInstance()
                            .getUuidFromPath("/" + base);
                    final Map<String, ContentInfo> userContInfo = BaseFolderModule
                            .getUserContentInfo(uuid);

                    for (final String user : userContInfo.keySet()) {
                        ContentInfo usrTotContInfo = totalUserContInfo
                                .get(user);
                        final ContentInfo usrContInfo = userContInfo.get(user);

                        if (usrTotContInfo == null) {
                            usrTotContInfo = new ContentInfo();
                        }

                        usrTotContInfo.setDocuments(usrTotContInfo
                                .getDocuments() + usrContInfo.getDocuments());
                        usrTotContInfo.setFolders(usrTotContInfo.getFolders()
                                + usrContInfo.getFolders());
                        usrTotContInfo.setMails(usrTotContInfo.getMails()
                                + usrContInfo.getMails());
                        usrTotContInfo.setSize(usrTotContInfo.getSize()
                                + usrContInfo.getSize());

                        totalUserContInfo.put(user, usrTotContInfo);
                    }
                }

                for (final String user : totalUserContInfo.keySet()) {
                    final ContentInfo contInfo = totalUserContInfo.get(user);
                    final UserItems userItems = new UserItems();
                    userItems.setDocuments(contInfo.getDocuments());
                    userItems.setFolders(contInfo.getFolders());
                    userItems.setSize(contInfo.getSize());
                    userItems.setUser(user);
                    userItemsMgr.put(user, userItems);
                }
            } catch (final PathNotFoundException e) {
                throw new RepositoryException("PathNotFoundException: " + e, e);
            } catch (final DatabaseException e) {
                throw new RepositoryException("DatabaseException: " + e, e);
            } finally {
                running = false;
            }

            log.info("*** End refresh user items ***");
        }

        log.debug("refreshDbUserItems: void");
    }

    /**
     * Store data in database
     */
    public static synchronized void serialize() throws DatabaseException {
        for (final String user : userItemsMgr.keySet()) {
            UserItemsDAO.update(userItemsMgr.get(user));
        }
    }

    /**
     * Read data from database
     */
    public static synchronized void deserialize() throws DatabaseException {
        for (final UserItems ui : UserItemsDAO.findAll()) {
            userItemsMgr.put(ui.getUser(), ui);
        }
    }
}
