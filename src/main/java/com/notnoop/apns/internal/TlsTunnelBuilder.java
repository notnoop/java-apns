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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.ProxyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Establishes a TLS connection using an HTTP proxy. See <a
 * href="http://www.ietf.org/rfc/rfc2817.txt">RFC 2817 5.2</a>. This class does
 * not support proxies requiring a "Proxy-Authorization" header.
 */
public final class TlsTunnelBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(TlsTunnelBuilder.class);
    
    public Socket build(SSLSocketFactory factory, Proxy proxy, String proxyUsername, String proxyPassword, String host, int port)
            throws IOException {
        boolean success = false;
        Socket proxySocket = null;
        try {
            logger.debug("Attempting to use proxy : " + proxy.toString());
            InetSocketAddress proxyAddress = (InetSocketAddress) proxy.address();
            proxySocket = makeTunnel(host, port, proxyUsername, proxyPassword, proxyAddress);

            // Handshake with the origin server.
            if(proxySocket ==  null) {
                throw new ProtocolException("Unable to create tunnel through proxy server.");
            }
            Socket socket = factory.createSocket(proxySocket, host, port, true /* auto close */);
            success = true;
            return socket;
        } finally {
            if (!success) {
                Utilities.close(proxySocket);
            }
        }
    }

    @SuppressFBWarnings(value = "VA_FORMAT_STRING_USES_NEWLINE",
            justification = "use <CR><LF> as according to RFC, not platform-linefeed")
    Socket makeTunnel(String host, int port, String proxyUsername, 
            String proxyPassword, InetSocketAddress proxyAddress) throws IOException {
        if(host == null || port < 0 || host.isEmpty() || proxyAddress == null){
            throw new ProtocolException("Incorrect parameters to build tunnel.");   
        }
        logger.debug("Creating socket for Proxy : " + proxyAddress.getAddress() + ":" + proxyAddress.getPort());
        Socket socket;
        try {
            ProxyClient client = new ProxyClient();
            HttpHost target = new HttpHost(host, port);
            String proxyHost = proxyAddress.getAddress().toString().substring(0, proxyAddress.getAddress().toString().indexOf("/"));
            HttpHost proxy = new HttpHost(proxyHost, proxyAddress.getPort());
            
            UsernamePasswordCredentials credentials = null;
            if(proxyUsername != null && proxyPassword != null) {
                credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
            }else{
            	credentials = new UsernamePasswordCredentials("anonymous", "anonymous");
            }
            
            socket = client.tunnel(proxy, target, credentials);           
        } catch (Exception e) {
            throw new ProtocolException("Error occurred while creating proxy socket : " + e.toString());
        }
        if (socket != null) {
            logger.debug("Socket for proxy created successfully : " + socket.getRemoteSocketAddress().toString());
        }
        return socket;
    }    
}

