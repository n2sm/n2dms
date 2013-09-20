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

package com.openkm.servlet.frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.api.OKMFolder;
import com.openkm.api.OKMMail;
import com.openkm.api.OKMRepository;
import com.openkm.api.OKMSearch;
import com.openkm.bean.Mail;
import com.openkm.bean.Repository;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTMail;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMMailService;
import com.openkm.servlet.frontend.util.MailComparator;
import com.openkm.util.GWTUtil;

/**
 * Servlet Class
 */
public class MailServlet extends OKMRemoteServiceServlet implements
        OKMMailService {
    private static Logger log = LoggerFactory.getLogger(MailServlet.class);

    private static final long serialVersionUID = 6444705787188086209L;

    @Override
    public List<GWTMail> getChilds(String fldPath) throws OKMException {
        log.debug("getChilds({})", fldPath);
        final List<GWTMail> mailList = new ArrayList<GWTMail>();
        updateSessionManager();

        try {
            if (fldPath == null) {
                fldPath = OKMRepository.getInstance().getMailFolder(null)
                        .getPath();
            }

            // Case thesaurus view must search documents in keywords
            if (fldPath.startsWith("/" + Repository.THESAURUS)) {
                final String keyword = fldPath.substring(
                        fldPath.lastIndexOf("/") + 1).replace(" ", "_");
                final List<Mail> results = OKMSearch.getInstance()
                        .getMailsByKeyword(null, keyword);

                for (final Mail mail : results) {
                    mailList.add(GWTUtil.copy(mail, getUserWorkspaceSession()));
                }
            } else if (fldPath.startsWith("/" + Repository.CATEGORIES)) {
                // Case categories view
                final String uuid = OKMFolder.getInstance()
                        .getProperties(null, fldPath).getUuid();
                final List<Mail> results = OKMSearch.getInstance()
                        .getCategorizedMails(null, uuid);

                for (final Mail mail : results) {
                    mailList.add(GWTUtil.copy(mail, getUserWorkspaceSession()));
                }
            } else {
                log.debug("ParentFolder: {}", fldPath);
                for (final Mail mail : OKMMail.getInstance().getChildren(null,
                        fldPath)) {
                    log.debug("Mail: {}", mail);
                    mailList.add(GWTUtil.copy(mail, getUserWorkspaceSession()));
                }
            }
            Collections.sort(mailList,
                    MailComparator.getInstance(getLanguage()));
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("getChilds: {}", mailList);
        return mailList;
    }

    @Override
    public void delete(final String mailPath) throws OKMException {
        log.debug("delete({})", mailPath);
        updateSessionManager();

        try {
            OKMMail.getInstance().delete(null, mailPath);
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Lock),
                    e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("delete: void");
    }

    @Override
    public void move(final String mailPath, final String destPath)
            throws OKMException {
        log.debug("move({}, {})", mailPath, destPath);
        updateSessionManager();

        try {
            OKMMail.getInstance().move(null, mailPath, destPath);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("move: void");
    }

    @Override
    public void purge(final String mailPath) throws OKMException {
        log.debug("purge({})", mailPath);
        updateSessionManager();

        try {
            OKMMail.getInstance().purge(null, mailPath);
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("purge: void");
    }

    @Override
    public void copy(final String mailPath, final String fldPath)
            throws OKMException {
        log.debug("copy({}, {})", mailPath, fldPath);
        updateSessionManager();

        try {
            OKMMail.getInstance().copy(null, mailPath, fldPath);
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("copy: void");
    }

    @Override
    public GWTMail getProperties(final String mailPath) throws OKMException {
        log.debug("getProperties({})", mailPath);
        GWTMail mailClient = new GWTMail();
        updateSessionManager();

        try {
            mailClient = GWTUtil.copy(
                    OKMMail.getInstance().getProperties(null, mailPath),
                    getUserWorkspaceSession());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("copy: getProperties");
        return mailClient;
    }

    @Override
    public GWTMail rename(final String mailId, final String newName)
            throws OKMException {
        log.debug("rename({}, {})", mailId, newName);
        GWTMail gWTMail = new GWTMail();
        updateSessionManager();

        try {
            gWTMail = GWTUtil.copy(
                    OKMMail.getInstance().rename(null, mailId, newName),
                    getUserWorkspaceSession());
        } catch (final ItemExistsException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_ItemExists), e.getMessage());
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }

        log.debug("rename: {}", gWTMail);
        return gWTMail;
    }

    @Override
    public Boolean isValid(final String mailPath) throws OKMException {
        log.debug("isValid({})", mailPath);
        updateSessionManager();

        try {
            return new Boolean(OKMMail.getInstance().isValid(null, mailPath));
        } catch (final PathNotFoundException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.warn(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMMailService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMMailService, ErrorCode.CAUSE_General),
                    e.getMessage());
        }
    }
}
