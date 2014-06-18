package com.notnoop.apns.utils;

import java.io.InputStream;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.notnoop.apns.internal.Utilities;

public class FixedCertificates {

    public static final String CLIENT_STORE = "clientStore.p12";
    public static final String CLIENT_PASSWORD = "123456";

    public static final String SERVER_STORE = "serverStore.p12";
    public static final String SERVER_PASSWORD = "123456";

    public static final String LOCALHOST = "localhost";

    public static SSLContext serverContext() {
        try {
            //System.setProperty("javax.net.ssl.trustStore", ClassLoader.getSystemResource(CLIENT_STORE).getPath());
            InputStream stream = ClassLoader.getSystemResourceAsStream(SERVER_STORE);

            return Utilities.newSSLContext(stream, SERVER_PASSWORD, "PKCS12", "sunx509");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SSLContext clientContext() {
        try {
            InputStream stream = ClassLoader.getSystemResourceAsStream(CLIENT_STORE);
            SSLContext context = Utilities.newSSLContext(stream, CLIENT_PASSWORD, "PKCS12", "sunx509");
            context.init(null, new TrustManager[] { new X509TrustManagerTrustAll() }, new SecureRandom());
            return context;
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
