/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.utils;

import com.notnoop.apns.internal.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;

public class FixedCertificates {

    public static final String CLIENT_STORE = "clientStore.p12";
    public static final String CLIENT_PASSWORD = "123456";

    public static final String CLIENT_MULTI_KEY_STORE = "clientStore.jks";
    public static final String CLIENT_MULTI_KEY_PASSWORD = "123456";

    public static final String SERVER_STORE = "serverStore.p12";
    public static final String SERVER_PASSWORD = "123456";

    public static final String SERVER_TRUST_STORE = "serverTrustStore.p12";
    public static final String SERVER_TRUST_PASSWORD = "123456";

    public static final String LOCALHOST = "localhost";

    public static SSLContext serverContext() {
        try {
            InputStream stream = FixedCertificates.class.getResourceAsStream("/" + SERVER_STORE);
            InputStream trustStream = FixedCertificates.class.getResourceAsStream("/" + SERVER_TRUST_STORE);
            assert stream != null;
            return new SSLContextBuilder()
                    .withAlgorithm("sunx509")
                    .withCertificateKeyStore(stream, SERVER_PASSWORD, "PKCS12")
                    .withTrustKeyStore(trustStream, SERVER_TRUST_PASSWORD, "PKCS12")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext clientContext() {
        try {
            InputStream stream = FixedCertificates.class.getResourceAsStream("/" + CLIENT_STORE);
            assert stream != null;
            return new SSLContextBuilder()
                    .withAlgorithm("sunx509")
                    .withCertificateKeyStore(stream, CLIENT_PASSWORD, "PKCS12")
                    .withTrustManager(new X509TrustManagerTrustAll())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext clientMultiKeyContext(String keyAlias) {
        try {
            InputStream stream = FixedCertificates.class.getResourceAsStream("/" + CLIENT_MULTI_KEY_STORE);
            assert stream != null;
            return new SSLContextBuilder()
                    .withAlgorithm("sunx509")
                    .withCertificateKeyStore(stream, CLIENT_MULTI_KEY_PASSWORD, "JKS", keyAlias)
                    .withTrustManager(new X509TrustManagerTrustAll())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String clientCertPath() {
        return ClassLoader.getSystemResource(CLIENT_STORE).getPath();
    }

    static class X509TrustManagerTrustAll implements X509TrustManager {
        public boolean checkClientTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] chain){
            return true;
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
    }

}
