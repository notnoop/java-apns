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
package com.notnoop.apns.utils;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApnsServerStub {

    /**
     * Create an ApnsServerStub
     *
     * @param gatePort port for the gateway stub server
     * @param feedPort port for the feedback stub server
     * @return an ApnsServerStub
     * @deprecated use prepareAndStartServer() without port numbers and query the port numbers from the server using
     * ApnsServerStub.getEffectiveGatewayPort() and ApnsServerStub.getEffectiveFeedbackPort()
     */
    @Deprecated
    public static ApnsServerStub prepareAndStartServer(int gatePort, int feedPort) {
        ApnsServerStub server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory(), gatePort, feedPort);
        server.start();
        return server;
    }

    /**
     * Create an ApnsServerStub that uses any free port for gateway and feedback.
     *
     * @return the server stub. Use getEffectiveGatewayPort() and getEffectiveFeedbackPort() to ask for ports.
     */
    public static ApnsServerStub prepareAndStartServer() {
        ApnsServerStub server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        server.start();
        return server;
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApnsServerStub.class);
    private final AtomicInteger toWaitBeforeSend = new AtomicInteger(0);
    private final ByteArrayOutputStream received;
    private final ByteArrayOutputStream toSend;
    private final Semaphore messages = new Semaphore(0);
    private final Semaphore startUp = new Semaphore(0);
    private final Semaphore gatewayOutLock = new Semaphore(0);
    private final Semaphore waitForError = new Semaphore(1);
    private final ServerSocketFactory sslFactory;
    private final int gatewayPort;
    private final int feedbackPort;
    private int effectiveGatewayPort;
    private int effectiveFeedbackPort;
    private OutputStream gatewayOutputStream;

    public ApnsServerStub(ServerSocketFactory sslFactory) {
        this(sslFactory, 0, 0);
    }

    public ApnsServerStub(ServerSocketFactory sslFactory, int gatewayPort, int feedbackPort) {
        this.sslFactory = sslFactory;
        this.gatewayPort = gatewayPort;
        this.feedbackPort = feedbackPort;

        this.received = new ByteArrayOutputStream();
        this.toSend = new ByteArrayOutputStream();
    }

    Thread gatewayThread;
    Thread feedbackThread;
    SSLServerSocket gatewaySocket;
    SSLServerSocket feedbackSocket;

    public void start() {
        gatewayThread = new GatewayRunner();
        feedbackThread = new FeedbackRunner();
        gatewayThread.start();
        feedbackThread.start();
        startUp.acquireUninterruptibly(2);
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        try {
            if (gatewaySocket != null) {
                gatewaySocket.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Can not close gatewaySocket properly", e);
        }
        try {
            if (feedbackSocket != null) {
                feedbackSocket.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Can not close feedbackSocket properly", e);
        }

        if (gatewayThread != null) {
            gatewayThread.stop();
        }

        if (feedbackThread != null) {
            feedbackThread.stop();
        }

    }

    public void sendError(int err, int id) {
        ByteBuffer buf = ByteBuffer.allocate(6);
        buf.put((byte) 8).put((byte) err).putInt(id);
        try {
            gatewayOutLock.acquire();
            gatewayOutputStream.write(buf.array());
            gatewayOutputStream.flush();
        } catch (Exception ex) {
            LOGGER.warn("An error occured with accessing gateway", ex);
        }
    }

    public int getEffectiveGatewayPort() {
        return effectiveGatewayPort;
    }

    public int getEffectiveFeedbackPort() {
        return effectiveFeedbackPort;
    }

    public AtomicInteger getToWaitBeforeSend() {
        return toWaitBeforeSend;
    }

    public ByteArrayOutputStream getReceived() {
        return received;
    }

    public ByteArrayOutputStream getToSend() {
        return toSend;
    }

    public Semaphore getMessages() {
        return messages;
    }

    public Semaphore getWaitForError() {
        return waitForError;
    }

    private class GatewayRunner extends Thread {

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            Thread.currentThread().setName("GatewayThread");
            try {
                gatewaySocket = (SSLServerSocket)sslFactory.createServerSocket(gatewayPort);
                gatewaySocket.setNeedClientAuth(true);
            } catch (IOException e) {
                messages.release();
                throw new RuntimeException(e);
            }

            InputStream in = null;
            effectiveGatewayPort = gatewaySocket.getLocalPort();

            try {
                // Listen for connections
                startUp.release();
                while (true) {
                    Socket socket = gatewaySocket.accept();
                    // Work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
                    socket.setSoLinger(true, 1);

                    // Create streams to securely send and receive data to the client
                    in = socket.getInputStream();
                    gatewayOutputStream = socket.getOutputStream();
                    gatewayOutLock.release();

                    // Read from in and write to out...
                    byte[] read = readFully(in);

                    waitBeforeSend();
                    received.write(read);
                    messages.release();


                    waitForError.acquire();

                    // Close the socket
                    in.close();
                    gatewayOutputStream.close();
                }
            } catch (Throwable e) {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioex) {
                    LOGGER.warn("Can not close socket properly", ioex);
                }
                try {
                    if (gatewayOutputStream != null) {
                        gatewayOutputStream.close();
                    }
                } catch (IOException ioex) {
                    LOGGER.warn("Can not close gatewayOutputStream properly", ioex);
                }
                messages.release();
            }
        }

    }

    private class FeedbackRunner extends Thread {

        public void run() {
            Thread.currentThread().setName("FeedbackThread");
            try {
                feedbackSocket = (SSLServerSocket)sslFactory.createServerSocket(feedbackPort);
                feedbackSocket.setNeedClientAuth(true);
            } catch (IOException e) {
                e.printStackTrace();
                messages.release();
                throw new RuntimeException(e);
            }

            effectiveFeedbackPort = feedbackSocket.getLocalPort();
            try {
                // Listen for connections
                startUp.release();
                Socket socket = feedbackSocket.accept();
                // Work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
                socket.setSoLinger(true, 1);

                // Create streams to securely send and receive data to the client
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                waitBeforeSend();
                // Read from in and write to out...
                toSend.writeTo(out);

                // Close the socket
                in.close();
                out.close();
            } catch (SocketException se) {
                // Ignore closed socket.
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            messages.release();
        }
    }

    AtomicInteger readLen = new AtomicInteger();

    public void stopAt(int length) {
        readLen.set(length);
    }

    public byte[] readFully(InputStream st) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        int read;
        try {
            while (readLen.getAndDecrement() > 0 && (read = st.read()) != -1) {
                stream.write(read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return stream.toByteArray();
    }

    /**
     * Introduces a waiting time, used to trigger read timeouts.
     */
    private void waitBeforeSend() {
        int wait = toWaitBeforeSend.get();
        if (wait != 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
