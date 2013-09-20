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

package com.openkm.omr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.InvalidImageIndexException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import org.apache.commons.io.IOUtils;

import com.openkm.api.OKMDocument;
import com.openkm.api.OKMPropertyGroup;
import com.openkm.api.OKMRepository;
import com.openkm.automation.AutomationException;
import com.openkm.bean.Document;
import com.openkm.bean.PropertyGroup;
import com.openkm.bean.form.FormElement;
import com.openkm.bean.form.Select;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.DatabaseException;
import com.openkm.core.LockException;
import com.openkm.core.MimeTypeConfig;
import com.openkm.core.NoSuchGroupException;
import com.openkm.core.NoSuchPropertyException;
import com.openkm.core.ParseException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.dao.OmrDAO;
import com.openkm.dao.bean.Omr;
import com.openkm.extension.core.ExtensionException;
import com.openkm.util.FileUtils;
import com.openkm.util.OMRException;

/**
 * OMRHelper
 * 
 * @author jllort
 */
public class OMRHelper {
    public static final String ASC_FILE = "ASC_FILE";

    public static final String CONFIG_FILE = "CONFIG_FILE";

    /**
     * isValid
     */
    public static boolean isValid(final Document doc) {
        return doc.getMimeType().equals(MimeTypeConfig.MIME_PNG);
    }

    /**
     * trainingTemplate
     */
    public static Map<String, File> trainingTemplate(final File template)
            throws IOException, InvalidFileStructureException,
            InvalidImageIndexException, UnsupportedTypeException,
            MissingParameterException, WrongParameterException {
        final Map<String, File> fileMap = new HashMap<String, File>();
        final Gray8Image grayimage = ImageUtil.readImage(template
                .getCanonicalPath());
        final ImageManipulation image = new ImageManipulation(grayimage);
        image.locateConcentricCircles();
        image.locateMarks();
        final File ascFile = FileUtils.createTempFile();
        final File configFile = FileUtils.createTempFile();
        image.writeAscTemplate(ascFile.getCanonicalPath());
        image.writeConfig(configFile.getCanonicalPath());
        fileMap.put(ASC_FILE, ascFile);
        fileMap.put(CONFIG_FILE, configFile);
        return fileMap;
    }

    /**
     * process
     */
    public static Map<String, String> process(final File fileToProcess,
            final long omId) throws IOException, OMRException,
            DatabaseException, InvalidFileStructureException,
            InvalidImageIndexException, UnsupportedTypeException,
            MissingParameterException, WrongParameterException {
        final Map<String, String> values = new HashMap<String, String>();
        final Omr omr = OmrDAO.getInstance().findByPk(omId);
        final InputStream asc = new ByteArrayInputStream(
                omr.getAscFileContent());
        final InputStream config = new ByteArrayInputStream(
                omr.getConfigFileContent());
        final InputStream fields = new ByteArrayInputStream(
                omr.getFieldsFileContent());

        if (asc != null && asc.available() > 0 && config != null
                && config.available() > 0 && fields != null
                && fields.available() > 0) {
            final Gray8Image grayimage = ImageUtil.readImage(fileToProcess
                    .getCanonicalPath());
            if (grayimage == null) {
                throw new OMRException(
                        "Not able to process the image as gray image");
            }

            final ImageManipulation image = new ImageManipulation(grayimage);
            image.locateConcentricCircles();
            image.readConfig(config);
            image.readFields(fields);
            image.readAscTemplate(asc);
            image.searchMarks();
            final File dataFile = FileUtils.createTempFile();
            image.saveData(dataFile.getCanonicalPath());

            // Parse data file

            final FileInputStream dfStream = new FileInputStream(dataFile);
            final DataInputStream in = new DataInputStream(dfStream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(
                    in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                // format key=value ( looking for first = )
                String key = "";
                String value = "";

                if (strLine.contains("=")) {
                    key = strLine.substring(0, strLine.indexOf("="));
                    value = strLine.substring(strLine.indexOf("=") + 1);
                    value = value.trim();
                }

                if (!key.equals("")) {
                    if (value.equals("")) {
                        IOUtils.closeQuietly(br);
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(dfStream);
                        IOUtils.closeQuietly(asc);
                        IOUtils.closeQuietly(config);
                        IOUtils.closeQuietly(fields);
                        throw new OMRException("Empty value");
                    }

                    if (omr.getProperties().contains(key)) {
                        values.put(key, value);
                    }
                }
            }

            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(dfStream);
            IOUtils.closeQuietly(asc);
            IOUtils.closeQuietly(config);
            IOUtils.closeQuietly(fields);
            FileUtils.deleteQuietly(dataFile);
            return values;
        } else {
            throw new OMRException(
                    "Error asc, config or fields files not found");
        }
    }

    /**
     * storeMetadata
     */
    public static void storeMetadata(final Map<String, String> results,
            final String docPath) throws IOException, ParseException,
            PathNotFoundException, RepositoryException, DatabaseException,
            NoSuchGroupException, LockException, AccessDeniedException,
            ExtensionException, AutomationException, NoSuchPropertyException,
            OMRException {
        final List<String> groups = new ArrayList<String>();

        for (final String key : results.keySet()) {
            if (key.contains(":")) {
                String grpName = key.substring(0, key.indexOf("."));

                // convert to okg (group name always start with okg )
                grpName = grpName.replace("okp", "okg");
                if (!groups.contains(grpName)) {
                    groups.add(grpName);
                }
            }
        }

        // Add missing groups
        for (final PropertyGroup registeredGroup : OKMPropertyGroup
                .getInstance().getGroups(null, docPath)) {
            if (groups.contains(registeredGroup.getName())) {
                groups.remove(registeredGroup.getName());
            }
        }
        // Add properties
        for (final String grpName : groups) {
            OKMPropertyGroup.getInstance().addGroup(null, docPath, grpName);

            // convert okg to okp ( property format )
            final String propertyBeginning = grpName.replace("okg", "okp");
            final Map<String, String> properties = new HashMap<String, String>();

            for (final String key : results.keySet()) {
                if (key.startsWith(propertyBeginning)) {
                    String value = results.get(key);

                    // Evaluate select multiple otherside throw exception
                    if (value.contains(" ")) {
                        for (final FormElement formElement : OKMPropertyGroup
                                .getInstance().getPropertyGroupForm(null,
                                        grpName)) {
                            if (formElement.getName().equals(key)
                                    && formElement instanceof Select) {
                                if (!((Select) formElement).getType().equals(
                                        Select.TYPE_MULTIPLE)) {
                                    throw new OMRException(
                                            "Found multiple value in a non multiple select. White space indicates multiple value");
                                } else {
                                    // Change " " to ";" the way to pass
                                    // multiple values into setPropertiesSimple
                                    value = value.replaceAll(" ", ";");
                                }
                            }
                        }
                    }

                    properties.put(key, value);
                }
            }

            OKMPropertyGroup.getInstance().setPropertiesSimple(null, docPath,
                    grpName, properties);
        }
    }

    /**
     * processAndStoreMetadata
     */
    public static void processAndStoreMetadata(final long omId,
            final String uuid) throws IOException, PathNotFoundException,
            AccessDeniedException, RepositoryException, DatabaseException,
            OMRException, NoSuchGroupException, LockException,
            ExtensionException, ParseException, NoSuchPropertyException,
            AutomationException, InvalidFileStructureException,
            InvalidImageIndexException, UnsupportedTypeException,
            MissingParameterException, WrongParameterException {
        InputStream is = null;
        File fileToProcess = null;

        try {
            final String docPath = OKMRepository.getInstance().getNodePath(
                    null, uuid);

            // create tmp content file
            fileToProcess = FileUtils.createTempFile();
            is = OKMDocument.getInstance().getContent(null, docPath, false);
            FileUtils.copy(is, fileToProcess);
            is.close();

            // process
            final Map<String, String> results = OMRHelper.process(
                    fileToProcess, omId);
            OMRHelper.storeMetadata(results, docPath);
        } catch (final IOException e) {
            throw e;
        } catch (final PathNotFoundException e) {
            throw e;
        } catch (final AccessDeniedException e) {
            throw e;
        } catch (final RepositoryException e) {
            throw e;
        } catch (final DatabaseException e) {
            throw e;
        } catch (final OMRException e) {
            throw e;
        } catch (final NoSuchGroupException e) {
            throw e;
        } catch (final LockException e) {
            throw e;
        } catch (final ExtensionException e) {
            throw e;
        } catch (final ParseException e) {
            throw e;
        } catch (final NoSuchPropertyException e) {
            throw e;
        } catch (final AutomationException e) {
            throw e;
        } catch (final InvalidFileStructureException e) {
            throw e;
        } catch (final InvalidImageIndexException e) {
            throw e;
        } catch (final UnsupportedTypeException e) {
            throw e;
        } catch (final MissingParameterException e) {
            throw e;
        } catch (final WrongParameterException e) {
            throw e;
        } finally {
            FileUtils.deleteQuietly(fileToProcess);
        }
    }
}