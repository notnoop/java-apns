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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Establishes a TLS connection using an HTTP proxy. See <a
 * href="http://www.ietf.org/rfc/rfc2817.txt">RFC 2817 5.2</a>. This class does
 * not support proxies requiring a "Proxy-Authorization" header.
 */
public final class TlsTunnelBuilder {
    public Socket build(SSLSocketFactory factory, Proxy proxy, String host, int port)
            throws IOException {
        boolean success = false;
        Socket proxySocket = null;
        try {
            InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
            proxySocket = new Socket(proxyAddress.getAddress(), proxyAddress.getPort());
            makeTunnel(host, port, proxySocket.getOutputStream(), proxySocket.getInputStream());

            // Handshake with the origin server.
            Socket socket = factory.createSocket(proxySocket, host, port, true /* auto close */);
            success = true;
            return socket;
        } finally {
            if (!success) {
                Utilities.close(proxySocket);
            }
        }
    }

    void makeTunnel(String host, int port, OutputStream out, InputStream in) throws IOException {
        // Send the HTTP CONNECT request.
        String userAgent = "java-apns";
        String connect = String.format("CONNECT %1$s:%2$d HTTP/1.1\r\n"
                + "Host: %1$s:%2$d\r\n"
                + "User-Agent: %3$s\r\n"
                + "Proxy-Connection: Keep-Alive\r\n" // For HTTP/1.0 proxies like Squid.
                + "\r\n",
                host, port, userAgent);
        out.write(connect.getBytes("UTF-8"));

        // Read the proxy's HTTP response.
        String statusLine = readAsciiUntilCrlf(in);
        if (!statusLine.matches("HTTP\\/1\\.\\d 2\\d\\d .*")) {
            // We didn't get a successful response like "HTTP/1.1 200 OK".
            throw new ProtocolException("TLS tunnel failed: " + statusLine);
        }
        while (readAsciiUntilCrlf(in).length() != 0) {
            // Discard headers and the blank line that follows them.
        }
    }

    /**
     * Returns the ASCII characters up to but not including the next CRLF
     * ("\r\n") or LF ("\n").
     *
     * @throws java.io.EOFException if the stream runs out of characters.
     */
    public static String readAsciiUntilCrlf(InputStream in) throws IOException {
        StringBuilder result = new StringBuilder(80);
        for (int c; (c = in.read()) != -1; ) {
            if (c == '\n') {
                if (result.length() > 0 && result.charAt(result.length() - 1) == '\r') {
                    result.deleteCharAt(result.length() - 1);
                }
                return result.toString();
            }
            result.append((char) c);
        }
        throw new EOFException("Expected CRLF");
    }
}
