/*
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
package com.notnoop.apns.internal;

import com.notnoop.exceptions.InvalidSSLConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

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

    public SSLContextBuilder withTrustKeyStore(InputStream keyStoreStream, String keyStorePassword, String keyStoreType) throws InvalidSSLConfig {
        try {
            final KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(keyStoreStream, keyStorePassword.toCharArray());
            return withTrustKeyStore(ks, keyStorePassword);
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        } catch (IOException e) {
            throw new InvalidSSLConfig(e);
        }

    }
    public SSLContextBuilder withTrustKeyStore(KeyStore keyStore, String keyStorePassword) throws InvalidSSLConfig {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
            trustManagerFactory.init(keyStore);
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

    public SSLContextBuilder withCertificateKeyStore(InputStream keyStoreStream, String keyStorePassword, String keyStoreType, String keyAlias) throws InvalidSSLConfig {
        try {
            final KeyStore ks = KeyStore.getInstance(keyStoreType);
            ks.load(keyStoreStream, keyStorePassword.toCharArray());
            return withCertificateKeyStore(ks, keyStorePassword, keyAlias);
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

    public SSLContextBuilder withCertificateKeyStore(KeyStore keyStore, String keyStorePassword, String keyAlias) throws InvalidSSLConfig {
        try {
            if (!keyStore.containsAlias(keyAlias)) {
                throw new InvalidSSLConfig("No key with alias " + keyAlias);
            }
            KeyStore singleKeyKeyStore = getKeyStoreWithSingleKey(keyStore, keyStorePassword, keyAlias);
            return withCertificateKeyStore(singleKeyKeyStore, keyStorePassword);
        } catch (GeneralSecurityException e) {
            throw new InvalidSSLConfig(e);
        } catch (IOException e) {
            throw new InvalidSSLConfig(e);
        }
    }

    /*
     * Workaround for keystores containing multiple keys. Java will take the first key that matches
     * and this way we can still offer configuration for a keystore with multiple keys and a selection
     * based on alias. Also much easier than making a subclass of a KeyManagerFactory
     */
    private KeyStore getKeyStoreWithSingleKey(KeyStore keyStore, String keyStorePassword, String keyAlias)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        KeyStore singleKeyKeyStore = KeyStore.getInstance(keyStore.getType(), keyStore.getProvider());
        final char[] password = keyStorePassword.toCharArray();
        singleKeyKeyStore.load(null, password);
        Key key = keyStore.getKey(keyAlias, password);
        Certificate[] chain = keyStore.getCertificateChain(keyAlias);
        singleKeyKeyStore.setKeyEntry(keyAlias, key, password, chain);
        return singleKeyKeyStore;
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
