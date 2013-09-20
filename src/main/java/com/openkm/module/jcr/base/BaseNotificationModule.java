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

package com.openkm.module.jcr.base;

import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Notification;
import com.openkm.core.Config;
import com.openkm.module.common.CommonNotificationModule;
import com.openkm.module.jcr.JcrAuthModule;

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
    public static void checkSubscriptions(final Node node, final String user,
            final String eventType, final String comment) {
        log.debug("checkSubscriptions({}, {}, {}, {})", new Object[] { node,
                user, eventType, comment });
        Set<String> users = new HashSet<String>();
        final Set<String> mails = new HashSet<String>();

        try {
            users = checkSubscriptionsHelper(node);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        /**
         * Mail notification
         */
        try {
            for (final String userId : users) {
                final String mail = new JcrAuthModule().getMail(null, userId);

                if (mail != null && !mail.isEmpty()) {
                    mails.add(mail);
                }
            }

            if (!mails.isEmpty()) {
                CommonNotificationModule.sendMailSubscription(user,
                        node.getPath(), eventType, comment, mails);
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
                CommonNotificationModule.sendTwitterSubscription(user,
                        node.getPath(), eventType, comment, users);
            }
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }

        log.debug("checkSubscriptions: void");
    }

    /**
     * Check for subscriptions recursively
     */
    private static Set<String> checkSubscriptionsHelper(final Node node)
            throws javax.jcr.RepositoryException {
        log.debug("checkSubscriptionsHelper: {}", node.getPath());
        final HashSet<String> al = new HashSet<String>();

        if (node.isNodeType(Folder.TYPE) || node.isNodeType(Document.TYPE)) {
            if (node.isNodeType(Notification.TYPE)) {
                final Value[] subscriptors = node.getProperty(
                        Notification.SUBSCRIPTORS).getValues();

                for (final Value subscriptor : subscriptors) {
                    al.add(subscriptor.getString());
                }
            }

            // An user shouldn't be notified twice
            final Set<String> tmp = checkSubscriptionsHelper(node.getParent());

            for (final String usr : tmp) {
                if (!al.contains(usr)) {
                    al.add(usr);
                }
            }
        }

        return al;
    }
}
