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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Utilities {

    public static final String SANDBOX_GATEWAY_HOST = "gateway.sandbox.push.apple.com";
    public static final int SANDBOX_GATEWAY_PORT = 2195;

    public static final String SANDBOX_FEEDBACK_HOST = "feedback.sandbox.push.apple.com";
    public static final int SANDBOX_FEEDBACK_PORT = 2196;

    public static final String PRODUCTION_GATEWAY_HOST = "gateway.push.apple.com";
    public static final int PRODUCTION_GATEWAY_PORT = 2195;

    public static final String PRODUCTION_FEEDBACK_HOST = "feedback.push.apple.com";
    public static final int PRODUCTION_FEEDBACK_PORT = 2196;

    public static final int MAX_PAYLOAD_LENGTH = 256;


    public static SSLSocketFactory newSSLSocketFactory(InputStream cert, String password,
         String ksType, String ksAlgorithm) throws Exception {
     SSLContext context = newSSLContext(cert, password, ksType, ksAlgorithm);
     return context.getSocketFactory();
    }

    public static SSLContext newSSLContext(InputStream cert, String password,
         String ksType, String ksAlgorithm) throws Exception {
     KeyStore ks = KeyStore.getInstance(ksType);
     ks.load(cert, password.toCharArray());

     // Get a KeyManager and initialize it
     KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
     kmf.init(ks, password.toCharArray());

     // Get a TrustManagerFactory and init with KeyStore
     TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
     tmf.init(ks);

     // Get the SSLContext to help create SSLSocketFactory
     SSLContext sslc = SSLContext.getInstance("TLS");
     sslc.init(kmf.getKeyManagers(), null, null);
     return sslc;
    }

    private static final Pattern pattern = Pattern.compile("[ -]");
    public static byte[] decodeHex(String deviceToken) {
     String hex = pattern.matcher(deviceToken).replaceAll("");

     byte[] bts = new byte[hex.length() / 2];
     for (int i = 0; i < bts.length; i++) {
         bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
     }
     return bts;
    }

    private static final char base[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    public static String encodeHex(byte[] bytes) {
     char[] chars = new char[bytes.length * 2];

     for (int i = 0; i < bytes.length; ++i) {
         int b = ((int)bytes[i]) & 0xFF;
         chars[2 * i] = base[b >>> 4];
         chars[2 * i + 1] = base[b & 0xF];
     }

     return new String(chars);
    }

    public static byte[] toUTF8Bytes(String s) {
     try {
         return s.getBytes("UTF-8");
     } catch (UnsupportedEncodingException e) {
         throw new RuntimeException(e);
     }
    }

    public static byte[] marshall(byte command, byte[] deviceToken, byte[] payload) {
     ByteArrayOutputStream boas = new ByteArrayOutputStream();
     DataOutputStream dos = new DataOutputStream(boas);

     try {
         dos.writeByte(command);
         dos.writeShort(deviceToken.length);
         dos.write(deviceToken);
         dos.writeShort(payload.length);
         dos.write(payload);
         return boas.toByteArray();
     } catch (IOException e) {
         throw new AssertionError();
     }
    }

    public static Map<byte[], Integer> parseFeedbackStreamRaw(InputStream in) {
     Map<byte[], Integer> result = new HashMap<byte[], Integer>();

     DataInputStream data = new DataInputStream(in);

     while (true) {
         try {
          int time = data.readInt();
          int dtLength = data.readUnsignedShort();
          byte[] deviceToken = new byte[dtLength];
          data.readFully(deviceToken);

          result.put(deviceToken, time);
         } catch (EOFException e) {
          break;
         } catch (IOException e) {
          throw new RuntimeException(e);
         }
     }

     return result;
    }

    public static Map<String, Date> parseFeedbackStream(InputStream in) {
     Map<String, Date> result = new HashMap<String, Date>();

     Map<byte[], Integer> raw = parseFeedbackStreamRaw(in);
     for (Map.Entry<byte[], Integer> entry : raw.entrySet()) {
         byte[] dtArray = entry.getKey();
         int time = entry.getValue(); // in seconds

         Date date = new Date(time * 1000L);    // in ms
         String dtString = encodeHex(dtArray);
         result.put(dtString, date);
     }

     return result;
    }

}
