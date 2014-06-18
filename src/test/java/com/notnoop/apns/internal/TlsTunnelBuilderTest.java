/*
 * Copyright 2012, Square, Inc.
 * Copyright 2012, Mahmood Ali.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.net.Socket;
import org.junit.Test;
import static org.junit.Assert.fail;

public class TlsTunnelBuilderTest {

    @Test
    public void makeTunnelSuccess() throws IOException {
        /* Uncomment this test to verify with your proxy settings */
        /*try {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.mydomain.com", 8080));

            InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
            Socket proxySocket = new Socket(proxyAddress.getAddress(), proxyAddress.getPort());
            InetSocketAddress destAddress = new InetSocketAddress("myhost.com", 2195);

            new TlsTunnelBuilder().makeTunnel(destAddress.getAddress().toString(), 
                                              destAddress.getPort(), 
                                              "proxy-username", "proxy-password", 
                                              proxyAddress);
        } catch (IOException ex){
            fail();
        }*/
        
    }

    @Test
    public void invalidProxyParams() throws IOException {
        try {
            new TlsTunnelBuilder().makeTunnel("origin.example.com", 9876, null, null, null);
            fail();
        } catch (IOException expected) {
            // No operation
        }
    }

    private InputStream inputStream(String content) throws IOException {
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
    }
}
