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

package com.openkm.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openkm.bean.LogMessage;

/**
 * @author pavila
 *
 */
public class FormatUtil {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(FormatUtil.class);

    /**
     * Format the document size for human readers
     */
    public static String formatSize(final long size) {
        final DecimalFormat df = new DecimalFormat("#0.0");
        String str;

        if (size / 1024 < 1) {
            str = size + " B";
        } else if (size / 1048576 < 1) {
            str = df.format(size / 1024.0) + " KB";
        } else if (size / 1073741824 < 1) {
            str = df.format(size / 1048576.0) + " MB";
        } else if (size / 1099511627776L < 1) {
            str = df.format(size / 1073741824.0) + " GB";
        } else if (size / 1125899906842624L < 1) {
            str = df.format(size / 1099511627776.0) + " TB";
        } else {
            str = "BIG";
        }

        return str;
    }

    /**
     * Format time for human readers
     */
    public static String formatTime(final long time) {
        final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
        final String str = df.format(time);
        return str;
    }

    /**
     * Format time interval for humans 
     */
    public static String formatSeconds(long time) {
        long hours, minutes, seconds;
        time = time / 1000;
        hours = time / 3600;
        time = time - hours * 3600;
        minutes = time / 60;
        time = time - minutes * 60;
        seconds = time;
        return (hours < 10 ? "0" + hours : hours) + ":"
                + (minutes < 10 ? "0" + minutes : minutes) + ":"
                + (seconds < 10 ? "0" + seconds : seconds);
    }

    /**
     * Format time interval for humans 
     */
    public static String formatMiliSeconds(long time) {
        long hours, minutes, seconds, mseconds;
        mseconds = time % 1000;
        time = time / 1000;
        hours = time / 3600;
        time = time - hours * 3600;
        minutes = time / 60;
        time = time - minutes * 60;
        seconds = time;
        return (hours < 10 ? "0" + hours : hours)
                + ":"
                + (minutes < 10 ? "0" + minutes : minutes)
                + ":"
                + (seconds < 10 ? "0" + seconds : seconds)
                + "."
                + (mseconds < 10 ? "00" + mseconds : mseconds < 100 ? "0"
                        + mseconds : mseconds);
    }

    /**
     * Format calendar date
     */
    public static String formatDate(final Calendar cal) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                .format(cal.getTime());
    }

    /**
     * Format string array
     */
    public static String formatArray(final String[] values) {
        if (values != null) {
            if (values.length == 1) {
                return values[0];
            } else {
                return ArrayUtils.toString(values);
            }
        } else {
            return "NULL";
        }
    }

    /**
     * Format object
     */
    public static String formatObject(final Object value) {
        if (value != null) {
            if (value instanceof Object[]) {
                return ArrayUtils.toString(value);
            } else {
                return value.toString();
            }
        } else {
            return "NULL";
        }
    }

    /**
     * Escape html tags 
     */
    public static String escapeHtml(final String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\n", "<br/>");
    }

    /**
     * Sanitize HTML
     * 
     * @see http://www.rgagnon.com/javadetails/java-0627.html
     */
    public static String sanitizeInput(final String string) {
        return string.replaceAll("(?i)<script.*?>.*?</script.*?>", "") // case 1 - Open and close
                .replaceAll("(?i)<script.*?/>", "") // case 1 - Open / close
                .replaceAll("(?i)<script.*?>", "") // case 1 - Open and !close
                .replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "") // case 2 - Open and close
                .replaceAll("(?i)<.*?javascript:.*?/>", "") // case 2 - Open / close
                .replaceAll("(?i)<.*?javascript:.*?>", "") // case 2 - Open and !close
                .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "") // case 3 - Open and close
                .replaceAll("(?i)<.*?\\s+on.*?/>", "") // case 3 - Open / close
                .replaceAll("(?i)<.*?\\s+on.*?>", ""); // case 3 - Open and !close
    }

    /**
     * Parser log file
     */
    public static Collection<LogMessage> parseLog(final File flog, int begin,
            int end, final String str) throws IOException {
        //log.debug("parseLog({}, {}, {}, {})", new Object[] { flog, begin, end, str });
        final ArrayList<LogMessage> al = new ArrayList<LogMessage>();
        int i = 0;

        if (begin < 0 || end < 0) {
            int maxLines = 0;

            for (final LineIterator lit = FileUtils.lineIterator(flog); lit
                    .hasNext();) {
                lit.nextLine();
                maxLines++;
            }

            if (begin < 0) {
                begin += maxLines;
            }

            if (end < 0) {
                end += maxLines + 1;
            }
        }

        for (final LineIterator lit = FileUtils.lineIterator(flog); lit
                .hasNext();) {
            String line = lit.nextLine();
            final int idx = line.indexOf(str);
            i++;

            if ((str == null || idx > -1) && i >= begin && i <= end) {
                if (idx > -1) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(line.substring(0, idx));
                    sb.append("<span class=\"highlight\">");
                    sb.append(line.substring(idx, idx + str.length()));
                    sb.append("</span>");
                    sb.append(line.substring(idx + str.length()));
                    line = sb.toString();
                }

                final LogMessage lm = new LogMessage();
                lm.setLine(i);
                lm.setMessage(line);
                al.add(lm);
            }
        }

        //log.debug("parseLog: {}", al);
        return al;
    }

    /**
     * Check for valid UTF8
     */
    public static boolean validUTF8(final byte[] input) {
        final CharsetDecoder cd = Charset.availableCharsets().get("UTF-8")
                .newDecoder();

        try {
            cd.decode(ByteBuffer.wrap(input));
        } catch (final CharacterCodingException e) {
            return false;
        }

        return true;
    }

    /**
     * Fix UTF-8 NULL
     */
    public static byte[] fixUTF8(final byte[] input) {
        final byte[] fixed = new byte[input.length];

        for (int i = 0; i < input.length; i++) {
            if (input[i] == 0x00) {
                fixed[i] = 0x20;
            } else {
                fixed[i] = input[i];
            }
        }

        return fixed;
    }

    /**
     * Fix UTF-8 NULL
     */
    public static String fixUTF8(final String input) {
        return input.replace('\u0000', '\u0020');
    }

    /**
     * Trim Unicode surrogate characters
     * 
     * http://en.wikipedia.org/wiki/Mapping_of_Unicode_characters#Surrogates
     */
    public static String trimUnicodeSurrogates(final String text) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            final char ch = text.charAt(i);

            if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
                sb.append(ch);
            }
        }

        return sb.toString();
    }
}
