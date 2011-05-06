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
 *   * Neither the name of Mahmood Ali. nor the names of its
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
package com.notnoop.apns.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.notnoop.exceptions.NetworkIOException;

public class ApnsFeedbackConnection {
    private static final Logger logger = LoggerFactory.getLogger(ApnsFeedbackConnection.class);

    private final SocketFactory factory;
    private final Proxy socksProxy;
    private final String host;
    private final int port;

    public ApnsFeedbackConnection(SocketFactory factory, Proxy socksProxy, String host, int port) {
        this.factory = factory;
        this.socksProxy = socksProxy;
        this.host = host;
        this.port = port;
    }

    int DELAY_IN_MS = 1000;
    private static final int RETRIES = 3;

    public Map<String, Date> getInactiveDevices() throws NetworkIOException {
        int attempts = 0;
        while (true) {
            try {
                attempts++;
                Map<String, Date> result = getInactiveDevicesImpl();

                attempts = 0;
                return result;
            } catch (Exception e) {
                logger.warn("Failed to retreive invalid devices", e);
                if (attempts >= RETRIES) {
                    logger.error("Couldn't get feedback connection", e);
                    Utilities.wrapAndThrowAsRuntimeException(e);
                }
                Utilities.sleep(DELAY_IN_MS);
            }
        }
    }

    public Map<String, Date> getInactiveDevicesImpl() throws IOException {
        Socket socket = null;
        try {
            if (socksProxy == null) {
                socket = factory.createSocket(host, port);
            } else {
            	Socket underlyingSocket = new Socket(socksProxy);
            	underlyingSocket.connect(new InetSocketAddress(host, port));
            	
                socket = ((SSLSocketFactory)factory).createSocket(underlyingSocket, host, port, false);
            }
            
            InputStream stream = socket.getInputStream();
            return Utilities.parseFeedbackStream(stream);
        } finally {
            Utilities.close(socket);
        }
    }

}
