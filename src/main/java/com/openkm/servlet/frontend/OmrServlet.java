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

package com.openkm.servlet.frontend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.InvalidImageIndexException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.automation.AutomationException;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.NoSuchPropertyException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.OmrDAO;
import com.openkm.dao.bean.Omr;
import com.openkm.extension.core.ExtensionException;
import com.openkm.frontend.client.OKMException;
import com.openkm.frontend.client.bean.GWTOmr;
import com.openkm.frontend.client.constants.service.ErrorCode;
import com.openkm.frontend.client.service.OKMOmrService;
import com.openkm.omr.OMRHelper;
import com.openkm.util.GWTUtil;
import com.openkm.util.OMRException;

/**
 * OMR service
 */
public class OmrServlet extends OKMRemoteServiceServlet implements
        OKMOmrService {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(OmrServlet.class);

    @Override
    public List<GWTOmr> getAllOmr() throws OKMException {
        final List<GWTOmr> omrList = new ArrayList<GWTOmr>();
        try {
            for (final Omr omr : OmrDAO.getInstance().findAllActive()) {
                omrList.add(GWTUtil.copy(omr));
            }
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        }
        return omrList;
    }

    @Override
    public void process(final long omId, final String uuid) throws OKMException {
        try {
            OMRHelper.processAndStoreMetadata(omId, uuid);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_IO),
                    e.getMessage());
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService,
                    ErrorCode.CAUSE_PathNotFound), e.getMessage());
        } catch (final AccessDeniedException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService,
                    ErrorCode.CAUSE_AccessDenied), e.getMessage());
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMOmrService,
                            ErrorCode.CAUSE_Repository), e.getMessage());
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Database),
                    e.getMessage());
        } catch (final OMRException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        } catch (final NoSuchGroupException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMOmrService,
                            ErrorCode.CAUSE_NoSuchGroup), e.getMessage());
        } catch (final LockException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Lock),
                    e.getMessage());
        } catch (final ExtensionException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Extension),
                    e.getMessage());
        } catch (final ParseException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Parse),
                    e.getMessage());
        } catch (final NoSuchPropertyException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService,
                    ErrorCode.CAUSE_NoSuchProperty), e.getMessage());
        } catch (final AutomationException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(
                    ErrorCode.get(ErrorCode.ORIGIN_OKMOmrService,
                            ErrorCode.CAUSE_Automation), e.getMessage());
        } catch (final InvalidFileStructureException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        } catch (final InvalidImageIndexException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        } catch (final UnsupportedTypeException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        } catch (final MissingParameterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        } catch (final WrongParameterException e) {
            log.error(e.getMessage(), e);
            throw new OKMException(ErrorCode.get(
                    ErrorCode.ORIGIN_OKMOmrService, ErrorCode.CAUSE_Omr),
                    e.getMessage());
        }
    }
}