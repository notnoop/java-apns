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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TlsTunnelBuilderTest {

    @Test
    public void readAsciiMultipleLines() throws IOException {
        InputStream in = inputStream("abc\r\ndef\r\n");
        Assert.assertEquals("abc", TlsTunnelBuilder.readAsciiUntilCrlf(in));
        Assert.assertEquals("def", TlsTunnelBuilder.readAsciiUntilCrlf(in));
    }

    @Test
    public void readAsciiMissingCr() throws IOException {
        InputStream in = inputStream("abc\ndef\r\n");
        Assert.assertEquals("abc", TlsTunnelBuilder.readAsciiUntilCrlf(in));
        Assert.assertEquals("def", TlsTunnelBuilder.readAsciiUntilCrlf(in));
    }

    @Test
    public void readAsciiMissingLf() throws IOException {
        InputStream in = inputStream("abc\rdef\r\n");
        Assert.assertEquals("abc\rdef", TlsTunnelBuilder.readAsciiUntilCrlf(in));
    }

    @Test
    public void readAsciiNoCrlf() throws IOException {
        InputStream in = inputStream("abc");
        try {
            TlsTunnelBuilder.readAsciiUntilCrlf(in);
            fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void readAsciiEmptyLine() throws IOException {
        InputStream in = inputStream("abc\r\n\r\n");
        assertEquals("abc", TlsTunnelBuilder.readAsciiUntilCrlf(in));
        assertEquals("", TlsTunnelBuilder.readAsciiUntilCrlf(in));
    }

    @Test
    public void makeTunnelSuccess() throws IOException {
        InputStream response = inputStream("HTTP/1.1 200 OK\r\n" +
                "Header: Value\r\n" +
                "Another-Header: Another Value\r\n" +
                "\r\n" +
                "ORIGIN DATA\r\n");
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        new TlsTunnelBuilder().makeTunnel("origin.example.com", 9876, request, response);

        assertEquals("CONNECT origin.example.com:9876 HTTP/1.1\r\n"
                + "Host: origin.example.com:9876\r\n"
                + "User-Agent: java-apns\r\n"
                + "Proxy-Connection: Keep-Alive\r\n"
                + "\r\n", request.toString("UTF-8"));

        assertEquals("ORIGIN DATA", TlsTunnelBuilder.readAsciiUntilCrlf(response));
    }

    @Test
    public void proxyServerRequestsAuth() throws IOException {
        InputStream response = inputStream("HTTP/1.1 407 AUTH REQUIRED\r\n\r\n");
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        try {
            new TlsTunnelBuilder().makeTunnel("origin.example.com", 9876, request, response);
            fail();
        } catch (IOException expected) {
        }
    }

    private InputStream inputStream(String content) throws IOException {
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
    }
}
