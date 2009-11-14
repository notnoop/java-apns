package com.notnoop.apns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.net.SocketFactory;

import com.notnoop.apns.internal.ApnsConnection;
import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.apns.internal.QueuedApnsService;
import com.notnoop.apns.internal.Utilities;

public class ApnsServiceBuilder {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM = "sunx509";

    private SocketFactory socketFactory;

    private String host;
    private int port;

    private boolean isQueued = false;

    protected ApnsServiceBuilder() { }

    public ApnsServiceBuilder withCert(String fileName, String password) {
        try {
            return withCert(new FileInputStream(fileName), password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ApnsServiceBuilder withCert(InputStream stream, String password) {
        try {
            return withSocketFactory(
                    Utilities.socketFactory(stream, password,
                            KEYSTORE_TYPE, KEY_ALGORITHM));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApnsServiceBuilder withSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
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

    public ApnsServiceBuilder asQueued() {
        this.isQueued = true;
        return this;
    }

    public ApnsService build() throws Exception {
        ApnsConnection conn = new ApnsConnection(socketFactory, host, port);
        ApnsService service = new ApnsServiceImpl(conn);

        if (isQueued) {
            service = new QueuedApnsService(service);
        }

        service.start();

        return service;
    }

}
