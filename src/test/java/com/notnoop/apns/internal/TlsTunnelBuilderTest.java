/*
 *  Copyright 2009, Mahmood Ali.
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndProxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;

import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndProxy.startClientAndProxy;

public class TlsTunnelBuilderTest {
    private static final int MOCK_PROXY_PORT = 1090;

    private static ClientAndProxy mockProxy;

    @BeforeClass
    public static void beforeClass() throws UnknownHostException {
        mockProxy = startClientAndProxy(MOCK_PROXY_PORT);
    }

    @AfterClass
    public static void afterClass() {
        mockProxy.stop();
    }

    @Test
    public void invalidProxyParams() throws IOException {
        try {
            new TlsTunnelBuilder().makeTunnel("origin.example.com", 9876, null, null, null);
            fail();
        } catch (final IOException expected) {
            // No operation
        }
    }

    @Test
    public void makeTunnelByHostNameSuccess() {
        makeTunnelSuccess("localhost");
    }

    @Test
    public void makeTunnelByHostAddressSuccess() {
        makeTunnelSuccess("127.0.0.1");
    }

    @Test
    public void makeTunnelByHostAddressV6Success() {
        makeTunnelSuccess("::1");
    }

    private void makeTunnelSuccess(final String proxyHost) {
        try {
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, MOCK_PROXY_PORT));

            final InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
            final InetSocketAddress destAddress = new InetSocketAddress("example.com", 80);

            new TlsTunnelBuilder().makeTunnel(
                    destAddress.getAddress().toString(),
                    destAddress.getPort(),
                    "", "",
                    proxyAddress);
        } catch (final IOException ignored) {
            fail();
        }
    }
}
