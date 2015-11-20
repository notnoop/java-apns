package com.notnoop.apns.internal;

import com.notnoop.exceptions.InvalidSSLConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public class SSLContextBuilder {
    private String algorithm = "sunx509";
    private KeyManagerFactory keyManagerFactory;
    private TrustManager[] trustManagers;

    public SSLContextBuilder withAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public SSLContextBuilder withDefaultTrustKeyStore() throws InvalidSSLConfig {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
            trustManagerFactory.init((KeyStore)null);
            trustManagers = trustManagerFactory.getTrustManagers();
            return this;
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        }
    }

    public SSLContextBuilder withTrustManager(TrustManager trustManager) {
        trustManagers = new TrustManager[] { trustManager };
        return this;
    }

    public SSLContextBuilder withCertificateKeyStore(InputStream keyStoreStream, String keyStorePassword, String keyStoreType) throws InvalidSSLConfig {
        try {
            final KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(keyStoreStream, keyStorePassword.toCharArray());
            return withCertificateKeyStore(ks, keyStorePassword);
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        } catch (IOException e) {
            throw new InvalidSSLConfig(e);
        }
    }

    public SSLContextBuilder withCertificateKeyStore(KeyStore keyStore, String keyStorePassword) throws InvalidSSLConfig {
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
            return this;
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        }
    }

    public SSLContext build() throws InvalidSSLConfig {
        if (keyManagerFactory == null) {
            throw new InvalidSSLConfig("Missing KeyManagerFactory");
        }

        if (trustManagers == null) {
            throw new InvalidSSLConfig("Missing TrustManagers");
        }

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        }
    }
}
