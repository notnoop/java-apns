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

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.ReconnectPolicy;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
import com.notnoop.exceptions.NetworkIOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApnsConnectionImpl implements ApnsConnection {

    private static final Logger logger = LoggerFactory.getLogger(ApnsConnectionImpl.class);
    private final SocketFactory factory;
    private final String host;
    private final int port;
    private final Proxy proxy;
    private final ReconnectPolicy reconnectPolicy;
    private final ApnsDelegate delegate;
    private int cacheLength;
    private final boolean errorDetection;
    private final boolean autoAdjustCacheLength;
    private final ConcurrentLinkedQueue<ApnsNotification> cachedNotifications, notificationsBuffer;

    public ApnsConnectionImpl(SocketFactory factory, String host, int port) {
        this(factory, host, port, new ReconnectPolicies.Never(), ApnsDelegate.EMPTY);
    }

    public ApnsConnectionImpl(SocketFactory factory, String host,
            int port, ReconnectPolicy reconnectPolicy,
            ApnsDelegate delegate) {
        this(factory, host, port, null, reconnectPolicy,
                delegate, false, ApnsConnection.DEFAULT_CACHE_LENGTH, true);
    }

    public ApnsConnectionImpl(SocketFactory factory, String host,
            int port, Proxy proxy,
            ReconnectPolicy reconnectPolicy, ApnsDelegate delegate,
            boolean errorDetection, int cacheLength, boolean autoAdjustCacheLength) {
        this.factory = factory;
        this.host = host;
        this.port = port;
        this.reconnectPolicy = reconnectPolicy;
        this.delegate = delegate == null ? ApnsDelegate.EMPTY : delegate;
        this.proxy = proxy;
        this.errorDetection = errorDetection;
        this.cacheLength = cacheLength;
        this.autoAdjustCacheLength = autoAdjustCacheLength;
        cachedNotifications = new ConcurrentLinkedQueue<ApnsNotification>();
        notificationsBuffer = new ConcurrentLinkedQueue<ApnsNotification>();
    }

    public synchronized void close() {
        Utilities.close(socket);
    }

    private void monitorSocket(final Socket socket) {
        class MonitoringThread extends Thread {

            @Override
            public void run() {

                try {
                    InputStream in = socket.getInputStream();

                    // TODO(jwilson): this should readFully()
                    final int expectedSize = 6;
                    byte[] bytes = new byte[expectedSize];
                    while (in.read(bytes) == expectedSize) {

                        int command = bytes[0] & 0xFF;
                        if (command != 8) {
                            throw new IOException("Unexpected command byte " + command);
                        }
                        int statusCode = bytes[1] & 0xFF;
                        DeliveryError e = DeliveryError.ofCode(statusCode);

                        int id = Utilities.parseBytes(bytes[2], bytes[3], bytes[4], bytes[5]);

                        Queue<ApnsNotification> tempCache = new LinkedList<ApnsNotification>();
                        ApnsNotification notification = null;
                        boolean foundNotification = false;

                        while (!cachedNotifications.isEmpty()) {
                            notification = cachedNotifications.poll();

                            if (notification.getIdentifier() == id) {
                                foundNotification = true;
                                break;
                            }
                            tempCache.add(notification);
                        }

                        if (foundNotification) {
                            delegate.messageSendFailed(notification, new ApnsDeliveryErrorException(e));
                        } else {
                            cachedNotifications.addAll(tempCache);
                            int resendSize = tempCache.size();
                            logger.warn("Received error for message "
                                    + "that wasn't in the cache...");
                            if (autoAdjustCacheLength) {
                                cacheLength = cacheLength + (resendSize / 2);
                                delegate.cacheLengthExceeded(cacheLength);
                            }
                            delegate.messageSendFailed(null, new ApnsDeliveryErrorException(e));
                        }

                        int resendSize = 0;

                        while (!cachedNotifications.isEmpty()) {
                            resendSize++;
                            notificationsBuffer.add(cachedNotifications.poll());
                        }
                        delegate.notificationsResent(resendSize);

                        delegate.connectionClosed(e, id);

                        drainBuffer();
                    }

                } catch (Exception e) {
                    // An exception when reading the error code is non-critical, it will cause another retry
                    // sending the message. Other than providing a more stable network connection to the APNS
                    // server we can't do much about it - so let's not spam the application's error log.
                    logger.info("Exception while waiting for error code", e);
                    delegate.connectionClosed(DeliveryError.UNKNOWN, -1);
                } finally {
                    close();
                }
            }
        }
        Thread t = new MonitoringThread();
        t.setDaemon(true);
        t.start();
    }
    // This method is only called from sendMessage.  sendMessage
    // has the required logic for retrying
    private Socket socket;

    private synchronized Socket socket() throws NetworkIOException {
        if (reconnectPolicy.shouldReconnect()) {
            Utilities.close(socket);
            socket = null;
        }

        if (socket == null || socket.isClosed()) {
            try {
                if (proxy == null) {
                    socket = factory.createSocket(host, port);
                } else if (proxy.type() == Proxy.Type.HTTP) {
                    TlsTunnelBuilder tunnelBuilder = new TlsTunnelBuilder();
                    socket = tunnelBuilder.build((SSLSocketFactory) factory, proxy, host, port);
                } else {
                    boolean success = false;
                    Socket proxySocket = null;
                    try {
                        proxySocket = new Socket(proxy);
                        proxySocket.connect(new InetSocketAddress(host, port));
                        socket = ((SSLSocketFactory) factory).createSocket(proxySocket, host, port, false);
                        success = true;
                    } finally {
                        if (!success) {
                            Utilities.close(proxySocket);
                        }
                    }
                }

                if (errorDetection) {
                    monitorSocket(socket);
                }

                reconnectPolicy.reconnected();
                logger.debug("Made a new connection to APNS");
            } catch (IOException e) {
                logger.error("Couldn't connect to APNS server", e);
                throw new NetworkIOException(e);
            }
        }
        return socket;
    }
    int DELAY_IN_MS = 1000;
    private static final int RETRIES = 3;

    public synchronized void sendMessage(ApnsNotification m) throws NetworkIOException {
        sendMessage(m, false);
    }

    public synchronized void sendMessage(ApnsNotification m, boolean fromBuffer) throws NetworkIOException {

        int attempts = 0;
        while (true) {
            try {
                attempts++;
                Socket socket = socket();
                socket.getOutputStream().write(m.marshall());
                socket.getOutputStream().flush();
                cacheNotification(m);

                delegate.messageSent(m, fromBuffer);

                logger.debug("Message \"{}\" sent", m);

                attempts = 0;
                drainBuffer();
                break;
            } catch (Exception e) {
                Utilities.close(socket);
                socket = null;
                if (attempts >= RETRIES) {
                    logger.error("Couldn't send message after " + RETRIES + " retries." + m, e);
                    delegate.messageSendFailed(m, e);
                    Utilities.wrapAndThrowAsRuntimeException(e);
                }
                // The first failure might be due to closed connection
                // don't delay quite yet
                if (attempts != 1) {
                    // Do not spam the log files when the APNS server closed the socket (due to a
                    // bad token, for example), only log when on the second retry.
                    logger.info("Failed to send message " + m + "... trying again after delay", e);
                    Utilities.sleep(DELAY_IN_MS);
                }
            }
        }
    }

    private void drainBuffer() {
        if (!notificationsBuffer.isEmpty()) {
            sendMessage(notificationsBuffer.poll(), true);
        }
    }

    private void cacheNotification(ApnsNotification notification) {
        cachedNotifications.add(notification);
        while (cachedNotifications.size() > cacheLength) {
            cachedNotifications.poll();
            logger.debug("Removing notification from cache " + notification);
        }
    }

    public ApnsConnectionImpl copy() {
        return new ApnsConnectionImpl(factory, host, port, proxy, reconnectPolicy.copy(),
                delegate, errorDetection, cacheLength, autoAdjustCacheLength);
    }

    public void testConnection() throws NetworkIOException {
        ApnsConnectionImpl testConnection = null;
        try {
            testConnection = new ApnsConnectionImpl(factory, host, port, reconnectPolicy.copy(), ApnsDelegate.EMPTY);
            testConnection.sendMessage(new SimpleApnsNotification(new byte[]{0}, new byte[]{0}));
        } finally {
            if (testConnection != null) {
                testConnection.close();
            }
        }
    }

    public void setCacheLength(int cacheLength) {
        this.cacheLength = cacheLength;
    }

    public int getCacheLength() {
        return cacheLength;
    }
}
