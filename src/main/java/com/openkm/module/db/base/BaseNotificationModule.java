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

package com.openkm.module.db.base;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.dao.NodeBaseDAO;
import com.openkm.dao.bean.NodeBase;
import com.openkm.module.common.CommonNotificationModule;
import com.openkm.module.db.DbAuthModule;

public class BaseNotificationModule {
    private static Logger log = LoggerFactory
            .getLogger(BaseNotificationModule.class);

    /**
     * Check for user subscriptions and send an notification
     * 
     * @param node Node modified (Document or Folder)
     * @param user User who generated the modification event
     * @param eventType Type of modification event
     */
    public static void checkSubscriptions(final NodeBase node,
            final String user, final String eventType, final String comment) {
        log.debug("checkSubscriptions({}, {}, {}, {})", new Object[] { node,
                user, eventType, comment });
        Set<String> users = new HashSet<String>();
        final Set<String> mails = new HashSet<String>();

        try {
            users = checkSubscriptionsHelper(node.getUuid());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        /**
         * Mail notification
         */
        try {
            for (final String userId : users) {
                final String mail = new DbAuthModule().getMail(null, userId);

                if (mail != null && !mail.isEmpty()) {
                    mails.add(mail);
                }
            }

            if (!mails.isEmpty()) {
                final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                        node.getUuid());
                CommonNotificationModule.sendMailSubscription(user, path,
                        eventType, comment, mails);
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        /**
         * Twitter notification
         */
        try {
            if (users != null && !users.isEmpty()
                    && !Config.SUBSCRIPTION_TWITTER_USER.equals("")
                    && !Config.SUBSCRIPTION_TWITTER_PASSWORD.equals("")) {
                final String path = NodeBaseDAO.getInstance().getPathFromUuid(
                        node.getUuid());
                CommonNotificationModule.sendTwitterSubscription(user, path,
                        eventType, comment, users);
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        log.debug("checkSubscriptions: void");
    }

    /**
     * Check for subscriptions recursively
     */
    private static Set<String> checkSubscriptionsHelper(final String uuid)
            throws PathNotFoundException, DatabaseException {
        log.debug("checkSubscriptionsHelper: {}", uuid);
        final Set<String> subscriptors = NodeBaseDAO.getInstance()
                .getSubscriptors(uuid);

        // An user shouldn't be notified twice
        final String parentUuid = NodeBaseDAO.getInstance().getParentUuid(uuid);

        if (!Config.ROOT_NODE_UUID.equals(parentUuid)) {
            final Set<String> tmp = checkSubscriptionsHelper(parentUuid);

            for (final String usr : tmp) {
                if (!subscriptors.contains(usr)) {
                    subscriptors.add(usr);
                }
            }
        }

        return subscriptors;
    }
}
