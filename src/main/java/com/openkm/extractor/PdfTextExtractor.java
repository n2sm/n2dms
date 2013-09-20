package com.openkm.extractor;

import java.io.BufferedInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.jackrabbit.extractor.AbstractTextExtractor;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.core.Config;
import com.openkm.util.FileUtils;

/**
 * Text extractor for Portable Document Format (PDF).
 */
public class PdfTextExtractor extends AbstractTextExtractor {

    /**
     * Logger instance.
     */
    private static final Logger log = LoggerFactory
            .getLogger(PdfTextExtractor.class);

    /**
     * Force loading of dependent class.
     */
    static {
        PDFParser.class.getName();
    }

    /**
     * Creates a new <code>PdfTextExtractor</code> instance.
     */
    public PdfTextExtractor() {
        super(new String[] { "application/pdf" });
    }

    //-------------------------------------------------------< TextExtractor >

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Reader extractText(final InputStream stream, final String type,
            final String encoding) throws IOException {
        try {
            final PDFParser parser = new PDFParser(new BufferedInputStream(
                    stream));

            try {
                parser.parse();
                final PDDocument document = parser.getPDDocument();
                final CharArrayWriter writer = new CharArrayWriter();

                final PDFTextStripper stripper = new PDFTextStripper();
                stripper.setLineSeparator("\n");
                stripper.writeText(document, writer);
                final String st = writer.toString().trim();
                log.debug("TextStripped: '{}'", st);

                if (Config.SYSTEM_PDF_FORCE_OCR || st.length() <= 1) {
                    log.warn("PDF does not contains text layer");

                    // Extract images from PDF
                    final List pages = document.getDocumentCatalog()
                            .getAllPages();
                    final StringBuilder sb = new StringBuilder();

                    for (final Iterator itPg = pages.iterator(); itPg.hasNext();) {
                        final PDPage page = (PDPage) itPg.next();
                        final PDResources resources = page.getResources();
                        final Map images = resources.getImages();

                        if (images != null) {
                            for (final Iterator itImg = images.keySet()
                                    .iterator(); itImg.hasNext();) {
                                String key = (String) itImg.next();
                                final PDXObjectImage image = (PDXObjectImage) images
                                        .get(key);
                                File pdfImg = null;

                                if (key.length() < 3) {
                                    key = key.concat(RandomStringUtils
                                            .randomAlphabetic(2));
                                }

                                try {
                                    pdfImg = File.createTempFile(key, "."
                                            + image.getSuffix());
                                    log.debug("Writing image: {}",
                                            pdfImg.getPath());
                                    image.write2file(pdfImg);
                                    final String txt = doOcr(pdfImg);
                                    sb.append(txt).append(" ");
                                    log.debug("OCR Extracted: {}", txt);
                                } finally {
                                    FileUtils.deleteQuietly(pdfImg);
                                }
                            }
                        }
                    }

                    return new StringReader(sb.toString());
                } else {
                    return new CharArrayReader(writer.toCharArray());
                }
            } finally {
                try {
                    final PDDocument doc = parser.getPDDocument();
                    if (doc != null) {
                        doc.close();
                    }
                } catch (final IOException e) {
                    // ignore
                }
            }
        } catch (final Exception e) {
            // it may happen that PDFParser throws a runtime
            // exception when parsing certain pdf documents
            log.warn("Failed to extract PDF text content", e);
            return new StringReader("");
        } finally {
            stream.close();
        }
    }

    /**
     * Guess the active OCR engine and use it to extract text from image.
     */
    private String doOcr(final File pdfImg) throws Exception {
        String text = "";

        if (RegisteredExtractors.isRegistered(CuneiformTextExtractor.class
                .getCanonicalName())) {
            text = new CuneiformTextExtractor().doOcr(pdfImg);
        } else if (RegisteredExtractors
                .isRegistered(Tesseract3TextExtractor.class.getCanonicalName())) {
            text = new Tesseract3TextExtractor().doOcr(pdfImg);
        } else if (RegisteredExtractors.isRegistered(AbbyTextExtractor.class
                .getCanonicalName())) {
            text = new AbbyTextExtractor().doOcr(pdfImg);
        } else {
            log.warn("No OCR engine configured");
        }

        return text;
    }
}
