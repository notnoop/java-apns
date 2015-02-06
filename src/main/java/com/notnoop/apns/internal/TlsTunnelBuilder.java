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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.ProxyClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            client.getParams().setParameter("http.useragent", "java-apns");
            client.getHostConfiguration().setHost(host, port);
            String proxyHost = proxyAddress.getAddress().toString().substring(0, proxyAddress.getAddress().toString().indexOf("/"));
            client.getHostConfiguration().setProxy(proxyHost, proxyAddress.getPort());
            
        
            ProxyClient.ConnectResponse response = client.connect();
            socket = response.getSocket();
            if (socket == null) {
                ConnectMethod method = response.getConnectMethod();
                // Read the proxy's HTTP response.
                if(method.getStatusLine().getStatusCode() == 407) {
                    // Proxy server returned 407. We will now try to connect with auth Header
                    if(proxyUsername != null && proxyPassword != null) {
                        socket = AuthenticateProxy(method, client,proxyHost, proxyAddress.getPort(),
                                proxyUsername, proxyPassword);
                    } else {
                        throw new ProtocolException("Socket not created: " + method.getStatusLine()); 
                    }
                }             
            }
            
        } catch (Exception e) {
            throw new ProtocolException("Error occurred while creating proxy socket : " + e.toString());
        }
        if (socket != null) {
            logger.debug("Socket for proxy created successfully : " + socket.getRemoteSocketAddress().toString());
        }
        return socket;
    }
    
    private Socket AuthenticateProxy(ConnectMethod method, ProxyClient client, 
            String proxyHost, int proxyPort, 
            String proxyUsername, String proxyPassword) throws IOException {   
        if(method.getProxyAuthState().getAuthScheme().getSchemeName().equalsIgnoreCase("ntlm")) {
            // If Auth scheme is NTLM, set NT credentials with blank host and domain name
            client.getState().setProxyCredentials(new AuthScope(proxyHost, proxyPort), 
                            new NTCredentials(proxyUsername, proxyPassword,"",""));
        } else {
            // If Auth scheme is Basic/Digest, set regular Credentials
            client.getState().setProxyCredentials(new AuthScope(proxyHost, proxyPort), 
                    new UsernamePasswordCredentials(proxyUsername, proxyPassword));
        }
        
        ProxyClient.ConnectResponse response = client.connect();
        Socket socket = response.getSocket();
        
        if (socket == null) {
            method = response.getConnectMethod();
            throw new ProtocolException("Proxy Authentication failed. Socket not created: " 
                    + method.getStatusLine());
        }
        return socket;
    }
    
}

