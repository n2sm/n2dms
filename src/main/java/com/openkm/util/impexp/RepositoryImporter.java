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

package com.openkm.util.impexp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.openkm.automation.AutomationException;
import com.openkm.bean.Document;
import com.openkm.bean.Folder;
import com.openkm.bean.Mail;
import com.openkm.core.AccessDeniedException;
import com.openkm.core.Config;
import com.openkm.core.DatabaseException;
import com.openkm.core.FileSizeExceededException;
import com.openkm.core.ItemExistsException;
import com.openkm.core.LockException;
import com.openkm.core.PathNotFoundException;
import com.openkm.core.RepositoryException;
import com.openkm.core.UnsupportedMimeTypeException;
import com.openkm.core.UserQuotaExceededException;
import com.openkm.core.VersionException;
import com.openkm.core.VirusDetectedException;
import com.openkm.extension.core.ExtensionException;
import com.openkm.module.DocumentModule;
import com.openkm.module.FolderModule;
import com.openkm.module.MailModule;
import com.openkm.module.ModuleManager;
import com.openkm.spring.PrincipalUtils;
import com.openkm.util.FileLogger;
import com.openkm.util.MailUtils;
import com.openkm.util.impexp.metadata.DocumentMetadata;
import com.openkm.util.impexp.metadata.FolderMetadata;
import com.openkm.util.impexp.metadata.MailMetadata;
import com.openkm.util.impexp.metadata.MetadataAdapter;
import com.openkm.util.impexp.metadata.VersionMetadata;

public class RepositoryImporter {
    private static Logger log = LoggerFactory
            .getLogger(RepositoryImporter.class);

    private static final String BASE_NAME = RepositoryImporter.class
            .getSimpleName();

    private RepositoryImporter() {
    }

    /**
     * Import documents from filesystem into document repository.
     */
    public static ImpExpStats importDocuments(final String token,
            final File fs, final String fldPath, final boolean metadata,
            final boolean history, final boolean uuid, final Writer out,
            final InfoDecorator deco) throws PathNotFoundException,
            ItemExistsException, AccessDeniedException, RepositoryException,
            FileNotFoundException, IOException, DatabaseException,
            ExtensionException, AutomationException {
        log.debug("importDocuments({}, {}, {}, {}, {}, {}, {}, {})",
                new Object[] { token, fs, fldPath, metadata, history, uuid,
                        out, deco });
        ImpExpStats stats;

        try {
            FileLogger.info(BASE_NAME,
                    "Start repository import from ''{0}'' to ''{1}''",
                    fs.getPath(), fldPath);

            if (fs.exists()) {
                stats = importDocumentsHelper(token, fs, fldPath, metadata,
                        history, uuid, out, deco);
            } else {
                throw new FileNotFoundException(fs.getPath());
            }

            FileLogger.info(BASE_NAME, "Repository import finalized");
        } catch (final PathNotFoundException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "PathNotFoundException ''{0}''",
                    e.getMessage());
            throw e;
        } catch (final AccessDeniedException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "AccessDeniedException ''{0}''",
                    e.getMessage());
            throw e;
        } catch (final FileNotFoundException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "FileNotFoundException ''{0}''",
                    e.getMessage());
            throw e;
        } catch (final RepositoryException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "RepositoryException ''{0}''",
                    e.getMessage());
            throw e;
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "IOException ''{0}''", e.getMessage());
            throw e;
        } catch (final DatabaseException e) {
            log.error(e.getMessage(), e);
            FileLogger.error(BASE_NAME, "DatabaseException ''{0}''",
                    e.getMessage());
            throw e;
        }

        log.debug("importDocuments: {}", stats);
        return stats;
    }

    /**
     * Import documents from filesystem into document repository (recursive).
     */
    private static ImpExpStats importDocumentsHelper(final String token,
            final File fs, final String fldPath, final boolean metadata,
            final boolean history, final boolean uuid, final Writer out,
            final InfoDecorator deco) throws FileNotFoundException,
            PathNotFoundException, AccessDeniedException, RepositoryException,
            IOException, DatabaseException, ExtensionException,
            AutomationException {
        log.debug("importDocumentsHelper({}, {}, {}, {}, {}, {}, {}, {})",
                new Object[] { token, fs, fldPath, metadata, history, uuid,
                        out, deco });
        final File[] files = fs
                .listFiles(new RepositoryImporter.NoVersionFilenameFilter());
        final ImpExpStats stats = new ImpExpStats();
        final FolderModule fm = ModuleManager.getFolderModule();
        final MetadataAdapter ma = MetadataAdapter.getInstance(token);
        ma.setRestoreUuid(uuid);
        final Gson gson = new Gson();

        for (final File file : files) {
            final String fileName = file.getName();

            if (!fileName.endsWith(Config.EXPORT_METADATA_EXT)) {
                if (file.isDirectory()) {
                    final Folder fld = new Folder();
                    boolean api = false;
                    int importedFolder = 0;
                    log.info("Directory: {}", file);

                    try {
                        if (metadata) {
                            // Read serialized folder metadata
                            final File jsFile = new File(file.getPath()
                                    + Config.EXPORT_METADATA_EXT);
                            log.info("Folder Metadata: {}", jsFile.getPath());

                            if (jsFile.exists() && jsFile.canRead()) {
                                final FileReader fr = new FileReader(jsFile);
                                final FolderMetadata fmd = gson.fromJson(fr,
                                        FolderMetadata.class);
                                fr.close();

                                // Apply metadata
                                fld.setPath(fldPath + "/" + fileName);
                                fmd.setPath(fld.getPath());
                                ma.importWithMetadata(fmd);

                                if (out != null) {
                                    out.write(deco.print(file.getPath(),
                                            file.length(), null));
                                    out.flush();
                                }
                            } else {
                                log.warn("Unable to read metadata file: {}",
                                        jsFile.getPath());
                                api = true;
                            }
                        } else {
                            api = true;
                        }

                        if (api) {
                            fld.setPath(fldPath + "/" + fileName);
                            fm.create(token, fld);
                            FileLogger.info(BASE_NAME,
                                    "Created folder ''{0}''", fld.getPath());

                            if (out != null) {
                                out.write(deco.print(file.getPath(),
                                        file.length(), null));
                                out.flush();
                            }
                        }

                        importedFolder = 1;
                    } catch (final ItemExistsException e) {
                        log.warn("ItemExistsException: {}", e.getMessage());

                        if (out != null) {
                            out.write(deco.print(file.getPath(), file.length(),
                                    "ItemExists"));
                            out.flush();
                        }

                        stats.setOk(false);
                        FileLogger.error(BASE_NAME,
                                "ItemExistsException ''{0}''", fld.getPath());
                    } catch (final JsonParseException e) {
                        log.warn("JsonParseException: {}", e.getMessage());

                        if (out != null) {
                            out.write(deco.print(file.getPath(), file.length(),
                                    "Json"));
                            out.flush();
                        }

                        stats.setOk(false);
                        FileLogger.error(BASE_NAME,
                                "JsonParseException ''{0}''", fld.getPath());
                    }

                    final ImpExpStats tmp = importDocumentsHelper(token, file,
                            fld.getPath(), metadata, history, uuid, out, deco);

                    // Stats
                    stats.setOk(stats.isOk() && tmp.isOk());
                    stats.setSize(stats.getSize() + tmp.getSize());
                    stats.setMails(stats.getMails() + tmp.getMails());
                    stats.setDocuments(stats.getDocuments()
                            + tmp.getDocuments());
                    stats.setFolders(stats.getFolders() + tmp.getFolders()
                            + importedFolder);
                } else {
                    log.info("File: {}", file);

                    if (fileName.endsWith(".eml")) {
                        log.info("Mail: {}", file);
                        final ImpExpStats tmp = importMail(token, fs, fldPath,
                                fileName, file, metadata, out, deco);

                        // Stats
                        stats.setOk(stats.isOk() && tmp.isOk());
                        stats.setSize(stats.getSize() + tmp.getSize());
                        stats.setMails(stats.getMails() + tmp.getMails());
                    } else {
                        log.info("Document: {}", file);
                        final ImpExpStats tmp = importDocument(token, fs,
                                fldPath, fileName, file, metadata, history,
                                out, deco);

                        // Stats
                        stats.setOk(stats.isOk() && tmp.isOk());
                        stats.setSize(stats.getSize() + tmp.getSize());
                        stats.setDocuments(stats.getDocuments()
                                + tmp.getDocuments());
                    }
                }
            }
        }

        log.debug("importDocumentsHelper: {}", stats);
        return stats;
    }

    /**
     * Import document.
     */
    private static ImpExpStats importDocument(final String token,
            final File fs, final String fldPath, final String fileName,
            final File fDoc, final boolean metadata, final boolean history,
            final Writer out, final InfoDecorator deco) throws IOException,
            RepositoryException, DatabaseException, PathNotFoundException,
            AccessDeniedException, ExtensionException, AutomationException {
        final FileInputStream fisContent = new FileInputStream(fDoc);
        final MetadataAdapter ma = MetadataAdapter.getInstance(token);
        final DocumentModule dm = ModuleManager.getDocumentModule();
        final ImpExpStats stats = new ImpExpStats();
        int size = fisContent.available();
        final Document doc = new Document();
        final Gson gson = new Gson();
        boolean api = false;

        try {
            // Metadata
            if (metadata) {
                // Read serialized document metadata
                final File jsFile = new File(fDoc.getPath()
                        + Config.EXPORT_METADATA_EXT);
                log.info("Document Metadata File: {}", jsFile.getPath());

                if (jsFile.exists() && jsFile.canRead()) {
                    final FileReader fr = new FileReader(jsFile);
                    final DocumentMetadata dmd = gson.fromJson(fr,
                            DocumentMetadata.class);
                    doc.setPath(fldPath + "/" + fileName);
                    dmd.setPath(doc.getPath());
                    IOUtils.closeQuietly(fr);
                    log.info("Document Metadata: {}", dmd);

                    if (history) {
                        final File[] vhFiles = fs
                                .listFiles(new RepositoryImporter.VersionFilenameFilter(
                                        fileName));
                        final List<File> listFiles = Arrays.asList(vhFiles);
                        Collections.sort(listFiles,
                                FilenameVersionComparator.getInstance());
                        boolean first = true;

                        for (final File vhf : vhFiles) {
                            final String vhfName = vhf.getName();
                            final int idx = vhfName.lastIndexOf('#',
                                    vhfName.length() - 2);
                            final String verName = vhfName.substring(idx + 2,
                                    vhfName.length() - 1);
                            final FileInputStream fis = new FileInputStream(vhf);
                            final File jsVerFile = new File(vhf.getPath()
                                    + Config.EXPORT_METADATA_EXT);
                            log.info("Document Version Metadata File: {}",
                                    jsVerFile.getPath());

                            if (jsVerFile.exists() && jsVerFile.canRead()) {
                                final FileReader verFr = new FileReader(
                                        jsVerFile);
                                final VersionMetadata vmd = gson.fromJson(
                                        verFr, VersionMetadata.class);
                                IOUtils.closeQuietly(verFr);

                                if (first) {
                                    dmd.setVersion(vmd);
                                    size = fis.available();
                                    ma.importWithMetadata(dmd, fis);
                                    first = false;
                                } else {
                                    log.info("Document Version Metadata: {}",
                                            vmd);
                                    size = fis.available();
                                    ma.importWithMetadata(doc.getPath(), vmd,
                                            fis);
                                }
                            } else {
                                log.warn("Unable to read metadata file: {}",
                                        jsVerFile.getPath());
                            }

                            IOUtils.closeQuietly(fis);
                            FileLogger.info(BASE_NAME,
                                    "Created document ''{0}'' version ''{1}''",
                                    doc.getPath(), verName);
                            log.info("Created document '{}' version '{}'",
                                    doc.getPath(), verName);
                        }
                    } else {
                        // Apply metadata
                        ma.importWithMetadata(dmd, fisContent);
                        FileLogger.info(BASE_NAME, "Created document ''{0}''",
                                doc.getPath());
                        log.info("Created document '{}'", doc.getPath());
                    }
                } else {
                    log.warn("Unable to read metadata file: {}",
                            jsFile.getPath());
                    api = true;
                }
            } else {
                api = true;
            }

            if (api) {
                doc.setPath(fldPath + "/" + fileName);

                // Version history
                if (history) {
                    final File[] vhFiles = fs
                            .listFiles(new RepositoryImporter.VersionFilenameFilter(
                                    fileName));
                    final List<File> listFiles = Arrays.asList(vhFiles);
                    Collections.sort(listFiles,
                            FilenameVersionComparator.getInstance());
                    boolean first = true;

                    for (final File vhf : vhFiles) {
                        final String vhfName = vhf.getName();
                        final int idx = vhfName.lastIndexOf('#',
                                vhfName.length() - 2);
                        final String verName = vhfName.substring(idx + 2,
                                vhfName.length() - 1);
                        final FileInputStream fis = new FileInputStream(vhf);

                        if (first) {
                            dm.create(token, doc, fis);
                            first = false;
                        } else {
                            dm.checkout(token, doc.getPath());
                            dm.checkin(token, doc.getPath(), fis,
                                    "Imported from administration");
                        }

                        IOUtils.closeQuietly(fis);
                        FileLogger.info(BASE_NAME,
                                "Created document ''{0}'' version ''{1}''",
                                doc.getPath(), verName);
                        log.info("Created document '{}' version '{}'",
                                doc.getPath(), verName);
                    }
                } else {
                    dm.create(token, doc, fisContent);
                    FileLogger.info(BASE_NAME, "Created document ''{0}''",
                            doc.getPath());
                    log.info("Created document ''{}''", doc.getPath());
                }
            }

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), null));
                out.flush();
            }

            // Stats
            stats.setSize(stats.getSize() + size);
            stats.setDocuments(stats.getDocuments() + 1);
        } catch (final UnsupportedMimeTypeException e) {
            log.warn("UnsupportedMimeTypeException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "UnsupportedMimeType"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "UnsupportedMimeTypeException ''{0}''",
                    doc.getPath());
        } catch (final FileSizeExceededException e) {
            log.warn("FileSizeExceededException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "FileSizeExceeded"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "FileSizeExceededException ''{0}''",
                    doc.getPath());
        } catch (final UserQuotaExceededException e) {
            log.warn("UserQuotaExceededException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "UserQuotaExceeded"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "UserQuotaExceededException ''{0}''",
                    doc.getPath());
        } catch (final VirusDetectedException e) {
            log.warn("VirusWarningException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "VirusWarningException"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "VirusWarningException ''{0}''",
                    doc.getPath());
        } catch (final ItemExistsException e) {
            log.warn("ItemExistsException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "ItemExists"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "ItemExistsException ''{0}''",
                    doc.getPath());
        } catch (final LockException e) {
            log.warn("LockException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), "Lock"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "LockException ''{0}''", doc.getPath());
        } catch (final VersionException e) {
            log.warn("VersionException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), "Version"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "VersionException ''{0}''",
                    doc.getPath());
        } catch (final JsonParseException e) {
            log.warn("JsonParseException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), "Json"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "JsonParseException ''{0}''",
                    doc.getPath());
        } finally {
            IOUtils.closeQuietly(fisContent);
        }

        return stats;
    }

    /**
     * Import mail.
     */
    private static ImpExpStats importMail(final String token, final File fs,
            final String fldPath, final String fileName, final File fDoc,
            final boolean metadata, final Writer out, final InfoDecorator deco)
            throws IOException, PathNotFoundException, AccessDeniedException,
            RepositoryException, DatabaseException, ExtensionException,
            AutomationException {
        final FileInputStream fisContent = new FileInputStream(fDoc);
        final MetadataAdapter ma = MetadataAdapter.getInstance(token);
        final MailModule mm = ModuleManager.getMailModule();
        final Properties props = System.getProperties();
        props.put("mail.host", "smtp.dummydomain.com");
        props.put("mail.transport.protocol", "smtp");
        final ImpExpStats stats = new ImpExpStats();
        final int size = fisContent.available();
        Mail mail = new Mail();
        final Gson gson = new Gson();
        boolean api = false;

        try {
            // Metadata
            if (metadata) {
                // Read serialized document metadata
                final File jsFile = new File(fDoc.getPath()
                        + Config.EXPORT_METADATA_EXT);
                log.info("Document Metadata File: {}", jsFile.getPath());

                if (jsFile.exists() && jsFile.canRead()) {
                    final FileReader fr = new FileReader(jsFile);
                    final MailMetadata mmd = gson.fromJson(fr,
                            MailMetadata.class);
                    mail.setPath(fldPath + "/" + fileName);
                    mmd.setPath(mail.getPath());
                    IOUtils.closeQuietly(fr);
                    log.info("Mail Metadata: {}", mmd);

                    // Apply metadata
                    ma.importWithMetadata(mmd);

                    // Add attachments
                    final Session mailSession = Session.getDefaultInstance(
                            props, null);
                    final MimeMessage msg = new MimeMessage(mailSession,
                            fisContent);
                    mail = MailUtils.messageToMail(msg);
                    mail.setPath(fldPath + "/" + mmd.getName());
                    MailUtils.addAttachments(null, mail, msg,
                            PrincipalUtils.getUser());

                    FileLogger.info(BASE_NAME, "Created document ''{0}''",
                            mail.getPath());
                    log.info("Created document '{}'", mail.getPath());
                } else {
                    log.warn("Unable to read metadata file: {}",
                            jsFile.getPath());
                    api = true;
                }
            } else {
                api = true;
            }

            if (api) {
                final Session mailSession = Session.getDefaultInstance(props,
                        null);
                final MimeMessage msg = new MimeMessage(mailSession, fisContent);
                mail = MailUtils.messageToMail(msg);
                mail.setPath(fldPath + "/" + fileName);
                mm.create(token, mail);
                MailUtils.addAttachments(null, mail, msg,
                        PrincipalUtils.getUser());

                FileLogger.info(BASE_NAME, "Created mail ''{0}''",
                        mail.getPath());
                log.info("Created mail ''{}''", mail.getPath());
            }

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), null));
                out.flush();
            }

            // Stats
            stats.setSize(stats.getSize() + size);
            stats.setMails(stats.getMails() + 1);
        } catch (final UnsupportedMimeTypeException e) {
            log.warn("UnsupportedMimeTypeException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "UnsupportedMimeType"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "UnsupportedMimeTypeException ''{0}''",
                    mail.getPath());
        } catch (final FileSizeExceededException e) {
            log.warn("FileSizeExceededException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "FileSizeExceeded"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "FileSizeExceededException ''{0}''",
                    mail.getPath());
        } catch (final UserQuotaExceededException e) {
            log.warn("UserQuotaExceededException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "UserQuotaExceeded"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "UserQuotaExceededException ''{0}''",
                    mail.getPath());
        } catch (final VirusDetectedException e) {
            log.warn("VirusWarningException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "VirusWarningException"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "VirusWarningException ''{0}''",
                    mail.getPath());
        } catch (final ItemExistsException e) {
            log.warn("ItemExistsException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(),
                        "ItemExists"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "ItemExistsException ''{0}''",
                    mail.getPath());
        } catch (final JsonParseException e) {
            log.warn("JsonParseException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), "Json"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "JsonParseException ''{0}''",
                    mail.getPath());
        } catch (final MessagingException e) {
            log.warn("MessagingException: {}", e.getMessage());

            if (out != null) {
                out.write(deco.print(fDoc.getPath(), fDoc.length(), "Json"));
                out.flush();
            }

            stats.setOk(false);
            FileLogger.error(BASE_NAME, "MessagingException ''{0}''",
                    mail.getPath());
        } finally {
            IOUtils.closeQuietly(fisContent);
        }

        return stats;
    }

    /**
     * Filter filename matching document versions.
     */
    public static class VersionFilenameFilter implements FilenameFilter {
        private String fileName = null;

        public VersionFilenameFilter(final String fileName) {
            this.fileName = fileName;
        }

        @Override
        public boolean accept(final File dir, final String name) {
            if (name.startsWith(fileName + "#") && name.endsWith("#")) {
                final int idx = name.lastIndexOf('#', name.length() - 2);

                if (idx > 0 && idx < name.length()) {
                    return name.charAt(idx + 1) == 'v';
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Filter filename not matching document versions.
     */
    public static class NoVersionFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(final File dir, final String name) {
            if (name.endsWith("#")) {
                final int idx = name.lastIndexOf('#', name.length() - 2);

                if (idx > 0 && idx < name.length()) {
                    return name.charAt(idx + 1) != 'v';
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }
}
