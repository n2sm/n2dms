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

package com.openkm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

public class SecureStore {
    /**
     * DES encoder
     */
    public static byte[] desEncode(final String key, final byte[] src)
            throws InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        final DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey sKey = keyFactory.generateSecret(keySpec);

        final Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
        cipher.init(Cipher.ENCRYPT_MODE, sKey);
        final byte[] dst = cipher.doFinal(src);

        return dst;
    }

    /**
     * DES decoder
     */
    public static byte[] desDecode(final String key, final byte[] src)
            throws InvalidKeyException, UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException {
        final DESKeySpec keySpec = new DESKeySpec(key.getBytes("UTF8"));
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        final SecretKey sKey = keyFactory.generateSecret(keySpec);

        final Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
        cipher.init(Cipher.DECRYPT_MODE, sKey);
        final byte[] dst = cipher.doFinal(src);

        return dst;
    }

    /**
     * Base64 encoder
     */
    public static String b64Encode(final byte[] src) {
        return new String(Base64.encodeBase64(src));
    }

    /**
     * Base64 decoder
     */
    public static byte[] b64Decode(final String src) {
        return Base64.decodeBase64(src.getBytes());
    }

    /**
     * MD5 encoder
     */
    public static String md5Encode(final byte[] src)
            throws NoSuchAlgorithmException {
        final StringBuilder sb = new StringBuilder();
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] dst = md.digest(src);

        for (final byte element : dst) {
            sb.append(Integer.toHexString(element >> 4 & 0xf));
            sb.append(Integer.toHexString(element & 0xf));
        }

        return sb.toString();
    }

    /**
     * MD5 encoder
     */
    public static String md5Encode(final File file)
            throws NoSuchAlgorithmException, IOException {
        final StringBuilder sb = new StringBuilder();
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final InputStream is = new FileInputStream(file);
        final byte[] buffer = new byte[1024];
        int numRead;

        try {
            do {
                if ((numRead = is.read(buffer)) > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);

            final byte[] dst = md.digest();

            for (final byte element : dst) {
                sb.append(Integer.toHexString(element >> 4 & 0xf));
                sb.append(Integer.toHexString(element & 0xf));
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        return sb.toString();
    }
}
