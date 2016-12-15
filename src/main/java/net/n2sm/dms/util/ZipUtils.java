/*
 * Copyright 2012-2016 CodeLibs Project and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package net.n2sm.dms.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtils {
    private static Logger log = LoggerFactory.getLogger(ZipUtils.class);

    /**
     * Extract from ZIP archive
     */
    public static void extract(final File zip, final File destDir) throws IOException {
        log.debug("extract({}, {})", new Object[] { zip, destDir });
        String filenameEncoding = System.getProperty("zip.filename.encoding");
        if (filenameEncoding == null) {
            filenameEncoding = "UTF-8";
        }
        try (final ZipFile zf = new ZipFile(zip, Charset.forName(filenameEncoding))) {
            final FileSystem fileSystem = FileSystems.getDefault();
            final Enumeration<? extends ZipEntry> entries = zf.entries();
            final String uncompressedDirectory = destDir.getPath() + "/";

            byte[] buffer = new byte[1024];
            int length;
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String filePath = uncompressedDirectory + entry.getName();
                if (entry.isDirectory()) {
                    log.debug("Creating Directory:" + filePath);
                    Files.createDirectories(fileSystem.getPath(filePath));
                } else {
                    try (final BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(entry));
                            final OutputStream os = Files.newOutputStream(Paths.get(filePath));
                            final OutputStream bos = new BufferedOutputStream(os);) {
                        while ((length = bis.read(buffer)) > 0) {
                            bos.write(buffer, 0, length);
                        }
                        bos.flush();
                        log.debug("Written :" + entry.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract zip. {}", zip, e);
            throw e;
        }

        log.debug("extract: void");
    }
}
