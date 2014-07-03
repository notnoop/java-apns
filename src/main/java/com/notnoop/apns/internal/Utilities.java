/*
 * Copyright 2009, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.NetworkIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utilities {
    private static Logger logger = LoggerFactory.getLogger(Utilities.class);

    public static final String SANDBOX_GATEWAY_HOST = "gateway.sandbox.push.apple.com";
    public static final int SANDBOX_GATEWAY_PORT = 2195;

    public static final String SANDBOX_FEEDBACK_HOST = "feedback.sandbox.push.apple.com";
    public static final int SANDBOX_FEEDBACK_PORT = 2196;

    public static final String PRODUCTION_GATEWAY_HOST = "gateway.push.apple.com";
    public static final int PRODUCTION_GATEWAY_PORT = 2195;

    public static final String PRODUCTION_FEEDBACK_HOST = "feedback.push.apple.com";
    public static final int PRODUCTION_FEEDBACK_PORT = 2196;

    public static final int MAX_PAYLOAD_LENGTH = 2048;

    private Utilities() { throw new AssertionError("Uninstantiable class"); }

    public static SSLSocketFactory newSSLSocketFactory(final InputStream cert, final String password,
         final String ksType, final String ksAlgorithm) throws InvalidSSLConfig {
        final SSLContext context = newSSLContext(cert, password, ksType, ksAlgorithm);
        return context.getSocketFactory();
    }

    public static SSLContext newSSLContext(final InputStream cert, final String password,
            final String ksType, final String ksAlgorithm) throws InvalidSSLConfig {
           try {
               final KeyStore ks = KeyStore.getInstance(ksType);
               ks.load(cert, password.toCharArray());
               return newSSLContext(ks, password, ksAlgorithm);
           } catch (final Exception e) {
               throw new InvalidSSLConfig(e);
           }
       }
    
    public static SSLContext newSSLContext(final KeyStore ks, final String password,
            final String ksAlgorithm) throws InvalidSSLConfig {
           try {
               // Get a KeyManager and initialize it
               final KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
               kmf.init(ks, password.toCharArray());

               // Get a TrustManagerFactory with the DEFAULT KEYSTORE, so we have all
               // the certificates in cacerts trusted
               final TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
               tmf.init((KeyStore)null);

               // Get the SSLContext to help create SSLSocketFactory
               final SSLContext sslContext = SSLContext.getInstance("TLS");
               sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
               return sslContext;
           } catch (final GeneralSecurityException e) {
               throw new InvalidSSLConfig(e);
           }
       }

    private static final Pattern pattern = Pattern.compile("[ -]");
    public static byte[] decodeHex(final String deviceToken) {
        final String hex = pattern.matcher(deviceToken).replaceAll("");

        final byte[] bts = new byte[hex.length() / 2];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) (charVal(hex.charAt(2 * i)) * 16 + charVal(hex.charAt(2 * i + 1)));
        }
        return bts;
    }

    private static int charVal(final char a) {
        if ('0' <= a && a <= '9') {
            return (a - '0');
        } else if ('a' <= a && a <= 'f') {
            return (a - 'a') + 10;
        } else if ('A' <= a && a <= 'F') {
            return (a - 'A') + 10;
        } else {
            throw new RuntimeException("Invalid hex character: " + a);
        }
    }

    private static final char base[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public static String encodeHex(final byte[] bytes) {
        final char[] chars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; ++i) {
            final int b = (bytes[i]) & 0xFF;
            chars[2 * i] = base[b >>> 4];
            chars[2 * i + 1] = base[b & 0xF];
        }

        return new String(chars);
    }

    public static byte[] toUTF8Bytes(final String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] marshall(final byte command, final byte[] deviceToken, final byte[] payload) {
        final ByteArrayOutputStream boas = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(boas);

        try {
            dos.writeByte(command);
            dos.writeShort(deviceToken.length);
            dos.write(deviceToken);
            dos.writeShort(payload.length);
            dos.write(payload);
            return boas.toByteArray();
        } catch (final IOException e) {
            throw new AssertionError();
        }
    }

    public static byte[] marshallEnhanced(final byte command, final int identifier,
            final int expiryTime, final byte[] deviceToken, final byte[] payload) {
        final ByteArrayOutputStream boas = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(boas);

        try {
            dos.writeByte(command);
            dos.writeInt(identifier);
            dos.writeInt(expiryTime);
            dos.writeShort(deviceToken.length);
            dos.write(deviceToken);
            dos.writeShort(payload.length);
            dos.write(payload);
            return boas.toByteArray();
        } catch (final IOException e) {
            throw new AssertionError();
        }
    }

    public static Map<byte[], Integer> parseFeedbackStreamRaw(final InputStream in) {
        final Map<byte[], Integer> result = new HashMap<byte[], Integer>();

        final DataInputStream data = new DataInputStream(in);

        while (true) {
            try {
                final int time = data.readInt();
                final int dtLength = data.readUnsignedShort();
                final byte[] deviceToken = new byte[dtLength];
                data.readFully(deviceToken);

                result.put(deviceToken, time);
            } catch (final EOFException e) {
                break;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    public static Map<String, Date> parseFeedbackStream(final InputStream in) {
        final Map<String, Date> result = new HashMap<String, Date>();

        final Map<byte[], Integer> raw = parseFeedbackStreamRaw(in);
        for (final Map.Entry<byte[], Integer> entry : raw.entrySet()) {
            final byte[] dtArray = entry.getKey();
            final int time = entry.getValue(); // in seconds

            final Date date = new Date(time * 1000L);    // in ms
            final String dtString = encodeHex(dtArray);
            result.put(dtString, date);
        }

        return result;
    }

    public static void close(final Closeable closeable) {
        logger.debug("close {}", closeable);

        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            logger.debug("error while closing resource", e);
        }
    }

    public static void close(final Socket closeable) {
        logger.debug("close {}", closeable);

        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException e) {
            logger.debug("error while closing socket", e);
        }
    }

    public static void sleep(final int delay) {
        try {
            Thread.sleep(delay);
        } catch (final InterruptedException e1) {
            Thread.currentThread().interrupt();
        }
    }

    public static byte[] copyOf(final byte[] bytes) {
        final byte[] copy = new byte[bytes.length];
        System.arraycopy(bytes, 0, copy, 0, bytes.length);
        return copy;
    }

    public static byte[] copyOfRange(final byte[] original, final int from, final int to) {
        final int newLength = to - from;
        if (newLength < 0) {
            throw new IllegalArgumentException(from + " > " + to);
        }
        final byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    public static void wrapAndThrowAsRuntimeException(final Exception e) throws NetworkIOException {
        if (e instanceof IOException) {
            throw new NetworkIOException((IOException)e);
        } else if (e instanceof NetworkIOException) {
            throw (NetworkIOException)e;
        } else if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
        } else {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"PointlessArithmeticExpression", "PointlessBitwiseExpression"})
    public static int parseBytes(final int b1, final int b2, final int b3, final int b4) {
        return  ((b1 << 3 * 8) & 0xFF000000)
              | ((b2 << 2 * 8) & 0x00FF0000)
              | ((b3 << 1 * 8) & 0x0000FF00)
              | ((b4 << 0 * 8) & 0x000000FF);
    }

    // @see http://stackoverflow.com/questions/119328/how-do-i-truncate-a-java-string-to-fit-in-a-given-number-of-bytes-once-utf-8-enc
    public static String truncateWhenUTF8(final String s, final int maxBytes) {
        int b = 0;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);

            // ranges from http://en.wikipedia.org/wiki/UTF-8
            int skip = 0;
            int more;
            if (c <= 0x007f) {
                more = 1;
            }
            else if (c <= 0x07FF) {
                more = 2;
            } else if (c <= 0xd7ff) {
                more = 3;
            } else if (c <= 0xDFFF) {
                // surrogate area, consume next char as well
                more = 4;
                skip = 1;
            } else {
                more = 3;
            }

            if (b + more > maxBytes) {
                return s.substring(0, i);
            }
            b += more;
            i += skip;
        }
        return s;
    }

}
