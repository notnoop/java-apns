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
 *   * Neither the name of Mahmod Ali. nor the names of its
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
package com.notnoop.apns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.net.ssl.SSLContext;

import com.notnoop.apns.internal.ApnsConnection;
import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.apns.internal.MinaAdaptor;
import com.notnoop.apns.internal.QueuedApnsService;
import static com.notnoop.apns.internal.Utilities.*;

/**
 * The class is used to create instances of {@link ApnsService}.
 *
 * Note that this class is not synchronized.  If multiple threads access a
 * {@code ApnsServiceBuilder} instance concurrently, and at least on of the
 * threads modifies one of the attributes structurally, it must be
 * synchronized externally.
 *
 * Starting a new {@code ApnsService} is easy:
 *
 * <pre>
 *   ApnsService = APNS.newService()
 *                  .withCert("/path/to/certificate.p12", "MyCertPassword")
 *                  .withSandboxDestination()
 *                  .build()
 * </pre>
 */
public class ApnsServiceBuilder {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM = "sunx509";

    private SSLContext sslContext;

    private String host;
    private int port = -1;

    private boolean isQueued = false;
    private boolean isNonBlocking = false;

    /**
     * Constructs a new instanceof {@code ApnsServiceBuilder}
     */
    public ApnsServiceBuilder() { }

    public ApnsServiceBuilder withCert(String fileName, String password) {
        try {
            return withCert(new FileInputStream(fileName), password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ApnsServiceBuilder withCert(InputStream stream, String password) {
        try {
            return withSSLContext(
                    newSSLContext(stream, password,
                            KEYSTORE_TYPE, KEY_ALGORITHM));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ApnsServiceBuilder withSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public ApnsServiceBuilder withDestination(String host, int port) {
        this.host = host;
        this.port = port;
        return this;
    }

    public ApnsServiceBuilder withSandboxDestination() {
        return withDestination(SANDBOX_GATEWAY_HOST, SANDBOX_GATEWAY_PORT);
    }

    public ApnsServiceBuilder withProductionDestination() {
        return withDestination(PRODUCTION_GATEWAY_HOST, PRODUCTION_GATEWAY_PORT);
    }

    public ApnsServiceBuilder asQueued() {
        this.isQueued = true;
        return this;
    }

    public ApnsServiceBuilder asNonBlocking() {
        this.isNonBlocking = true;
        return this;
    }

    public ApnsService build() throws Exception {
        checkInitialization();
        ApnsService service;

        if (isNonBlocking) {
            service = new MinaAdaptor(sslContext, host, port);
        } else {
            ApnsConnection conn = new ApnsConnection(sslContext.getSocketFactory(), host, port);
            service = new ApnsServiceImpl(conn);

            if (isQueued) {
                service = new QueuedApnsService(service);
            }
        }

        service.start();

        return service;
    }

    private void checkInitialization() {
        if (sslContext == null)
            throw new IllegalStateException(
                    "SSL Certificates and attribute are not initialized\n"
                    + "Use .withCert() methods.");
        if (host == null || port == -1)
            throw new IllegalStateException(
                    "The Destination APNS server is not stated\n"
                    + "Use .withDestination(), withSandboxDestination(), "
                    + "or withProductionDestination().");
    }
}
