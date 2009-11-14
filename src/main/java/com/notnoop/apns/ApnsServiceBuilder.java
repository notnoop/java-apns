package com.notnoop.apns;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.notnoop.apns.internal.ApnsConnection;
import com.notnoop.apns.internal.ApnsServiceImpl;

public class ApnsServiceBuilder {

    private String cert;
    private String password;

    private String host;
    private int port;

    public ApnsServiceBuilder withCert(String fileName, String password) {
        this.cert = fileName;
        this.password = password;
        return this;
    }

    public ApnsServiceBuilder withDestination(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public ApnsServiceBuilder withSandboxDestination() {
        return withDestination("gateway.sandbox.push.apple.com", 2195);
    }

    public ApnsServiceBuilder withProductionDestination() {
        return withDestination("gateway.push.apple.com", 2195);
    }

    public ApnsService build() throws Exception {
        ApnsConnection conn = new ApnsConnection(socketFactory(), host, port);
        ApnsServiceImpl service = new ApnsServiceImpl(conn);
        return service;
    }

    protected SSLSocketFactory socketFactory() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(cert), password.toCharArray());

        // Get a KeyManager and initialize it
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunx509");
        kmf.init(ks, password.toCharArray());

        // Get a TrustManagerFactory and init with KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("sunx509");
        tmf.init(ks);

        // Get the SSLContext to help create SSLSocketFactory
        SSLContext sslc = SSLContext.getInstance("TLS");
        sslc.init(kmf.getKeyManagers(), null, null);

        // Get SSLSocketFactory and get a SSLSocket
        SSLSocketFactory sslsf = sslc.getSocketFactory();
        return sslsf;
    }

}
