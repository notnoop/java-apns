package com.notnoop.apns.internal;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class Utilities {

    public static SSLSocketFactory socketFactory(String cert, String password,
            String ksType, String ksAlgorithm) throws Exception {
        KeyStore ks = KeyStore.getInstance(ksType);
        ks.load(new FileInputStream(cert), password.toCharArray());

        // Get a KeyManager and initialize it
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(ksAlgorithm);
        kmf.init(ks, password.toCharArray());

        // Get a TrustManagerFactory and init with KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(ksAlgorithm);
        tmf.init(ks);

        // Get the SSLContext to help create SSLSocketFactory
        SSLContext sslc = SSLContext.getInstance("TLS");
        sslc.init(kmf.getKeyManagers(), null, null);

        // Get SSLSocketFactory and get a SSLSocket
        SSLSocketFactory sslsf = sslc.getSocketFactory();
        return sslsf;
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

}
