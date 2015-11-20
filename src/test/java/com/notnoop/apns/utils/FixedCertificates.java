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
