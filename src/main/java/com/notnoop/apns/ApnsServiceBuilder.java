package com.notnoop.apns;

import javax.net.SocketFactory;

import com.notnoop.apns.internal.ApnsConnection;
import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.apns.internal.ThreadedApnsService;
import com.notnoop.apns.internal.Utilities;

public class ApnsServiceBuilder {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM = "sunx509";

    private SocketFactory socketFactory;

    private String host;
    private int port;

    private boolean isThreaded = false;

    protected ApnsServiceBuilder() { }

    public ApnsServiceBuilder withCert(String fileName, String password) throws Exception {
        return withSocketFactory(
                Utilities.socketFactory(fileName, password,
                        KEYSTORE_TYPE, KEY_ALGORITHM));
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

    public ApnsServiceBuilder withThread() {
        this.isThreaded = true;
        return this;
    }

    public ApnsService build() throws Exception {
        ApnsConnection conn = new ApnsConnection(socketFactory, host, port);
        ApnsService service = new ApnsServiceImpl(conn);

        if (isThreaded) {
            service = new ThreadedApnsService(service);
        }

        service.start();

        return service;
    }

}
