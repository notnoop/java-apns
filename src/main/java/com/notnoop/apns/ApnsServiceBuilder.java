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
