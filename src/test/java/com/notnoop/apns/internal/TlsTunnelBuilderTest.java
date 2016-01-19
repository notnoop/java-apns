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

import static org.junit.Assert.assertNotNull;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.ProxyAuthenticator;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class TlsTunnelBuilderTest {

    private static final String PROXY_USERNAME = "proxy-username";
    private static final String PROXY_PASSWORD = "proxy-password";
    private TestHttpService testHttpService;
    private String remoteHost;
    private int remoteport = 9999;
    private int localport = 8888;
    private HttpProxyServer server;

    @Before
    public void setUp() throws UnknownHostException {

        InetSocketAddress address = new InetSocketAddress(remoteport);
        testHttpService = new TestHttpService(address);
        remoteHost = address.getHostName();

        server = DefaultHttpProxyServer.bootstrap()
                .withProxyAuthenticator(new ProxyAuthenticator() {
                    @Override
                    public boolean authenticate(String userName, String password) {
                        return PROXY_USERNAME.equals(userName) && PROXY_PASSWORD.equals(password);
                    }
                })
                .withPort(localport)
                .start();
    }

    @After
    public void tearDown() {
        testHttpService.stop();
        server.stop();
    }

    @Test
    public void makeTunnelSuccess() throws IOException {
        /* Change host in this test to verify with your proxy settings */

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", localport));

        InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
        Socket proxySocket = new Socket(proxyAddress.getAddress(), proxyAddress.getPort());
        assertNotNull(proxySocket);
        proxySocket.close();

        InetSocketAddress destAddress = new InetSocketAddress(remoteHost, remoteport);

        Socket remoteAddress = new Socket(destAddress.getAddress(), destAddress.getPort());
        assertNotNull(remoteAddress);
        remoteAddress.close();

        Socket socket = new TlsTunnelBuilder()
                .makeTunnel(
                        remoteHost,
                        remoteport,
                        PROXY_USERNAME,
                        PROXY_PASSWORD,
                        proxyAddress);
        
        assertNotNull(socket);
    }

    @Test(expected = IOException.class)
    public void invalidProxyParams() throws IOException {
        new TlsTunnelBuilder().makeTunnel("origin.example.com", 9876, null, null, null);
    }


    public class TestHttpService {

        private HttpServer server;

        public TestHttpService(InetSocketAddress inetSocketAddress) {
            try {
                server = HttpServer.create(inetSocketAddress, 0);
                server.createContext("/");
                server.setExecutor(null);
                server.start();
            } catch (IOException e) {
                throw new IllegalStateException("Could not get ip to localhost", e);
            }
        }

        public void stop() {
            server.stop(0);
        }

    }
    
}
