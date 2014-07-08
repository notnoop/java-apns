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
package com.notnoop.apns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.notnoop.apns.internal.*;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.RuntimeIOException;

import static com.notnoop.apns.internal.Utilities.*;

/**
 * The class is used to create instances of {@link ApnsService}.
 *
 * Note that this class is not synchronized.  If multiple threads access a
 * {@code ApnsServiceBuilder} instance concurrently, and at least on of the
 * threads modifies one of the attributes structurally, it must be
 * synchronized externally.
 *
 * Starting a new {@code ApnsService} is easy:
 *
 * <pre>
 *   ApnsService = APNS.newService()
 *    .withCert("/path/to/certificate.p12", "MyCertPassword")
 *    .withSandboxDestination()
 *    .build()
 * </pre>
 */
public class ApnsServiceBuilder {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM = ((java.security.Security.getProperty("ssl.KeyManagerFactory.algorithm") == null)? "sunx509" : java.security.Security.getProperty("ssl.KeyManagerFactory.algorithm"));

    private SSLContext sslContext;

    private int readTimeout = 0;
    private int connectTimeout = 0;

    private String gatewayHost;
    private int gatewayPort = -1;

    private String feedbackHost;
    private int feedbackPort;
    private int pooledMax = 1;
    private int cacheLength = ApnsConnection.DEFAULT_CACHE_LENGTH;
    private boolean autoAdjustCacheLength = true;
    private ExecutorService executor = null;

    private ReconnectPolicy reconnectPolicy = ReconnectPolicy.Provided.EVERY_HALF_HOUR.newObject();
    private boolean isQueued = false;
    private ThreadFactory queueThreadFactory = null;
    
    private boolean isBatched = false;
    private int batchWaitTimeInSec;
    private int batchMaxWaitTimeInSec;
    private ThreadFactory batchThreadFactory = null;
    
    private ApnsDelegate delegate = ApnsDelegate.EMPTY;
    private Proxy proxy = null;
    private String proxyUsername = null;
    private String proxyPassword = null;
    private boolean errorDetection = true;
    private ThreadFactory errorDetectionThreadFactory = null;

    /**
     * Constructs a new instance of {@code ApnsServiceBuilder}
     */
    public ApnsServiceBuilder() { sslContext = null; }

    /**
     * Specify the certificate used to connect to Apple APNS
     * servers.  This relies on the path (absolute or relative to
     * working path) to the keystore (*.p12) containing the
     * certificate, along with the given password.
     *
     * The keystore needs to be of PKCS12 and the keystore
     * needs to be encrypted using the SunX509 algorithm.  Both
     * of these settings are the default.
     *
     * This library does not support password-less p12 certificates, due to a
     * Oracle Java library <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637">
     * Bug 6415637</a>.  There are three workarounds: use a password-protected
     * certificate, use a different boot Java SDK implementation, or construct
     * the `SSLContext` yourself!  Needless to say, the password-protected
     * certificate is most recommended option.
     *
     * @param fileName  the path to the certificate
     * @param password  the password of the keystore
     * @return  this
     * @throws RuntimeIOException if it {@code fileName} cannot be
     *          found or read
     * @throws InvalidSSLConfig if fileName is invalid Keystore
     *  or the password is invalid
     */
    public ApnsServiceBuilder withCert(String fileName, String password)
    throws RuntimeIOException, InvalidSSLConfig {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(fileName);
            return withCert(stream, password);
        } catch (FileNotFoundException e) {
            throw new RuntimeIOException(e);
        } finally {
            Utilities.close(stream);
        }
    }

    /**
     * Specify the certificate used to connect to Apple APNS
     * servers.  This relies on the stream of keystore (*.p12)
     * containing the certificate, along with the given password.
     *
     * The keystore needs to be of PKCS12 and the keystore
     * needs to be encrypted using the SunX509 algorithm.  Both
     * of these settings are the default.
     *
     * This library does not support password-less p12 certificates, due to a
     * Oracle Java library <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637">
     * Bug 6415637</a>.  There are three workarounds: use a password-protected
     * certificate, use a different boot Java SDK implementation, or constract
     * the `SSLContext` yourself!  Needless to say, the password-protected
     * certificate is most recommended option.
     *
     * @param stream    the keystore represented as input stream
     * @param password  the password of the keystore
     * @return  this
     * @throws InvalidSSLConfig if stream is invalid Keystore
     *  or the password is invalid
     */
    public ApnsServiceBuilder withCert(InputStream stream, String password)
    throws InvalidSSLConfig {
        assertPasswordNotEmpty(password);
        return withSSLContext(
                newSSLContext(stream, password,
                        KEYSTORE_TYPE, KEY_ALGORITHM));
    }

    /**
     * Specify the certificate used to connect to Apple APNS
     * servers.  This relies on a keystore (*.p12)
     * containing the certificate, along with the given password.
     *
     * This library does not support password-less p12 certificates, due to a
     * Oracle Java library <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637">
     * Bug 6415637</a>.  There are three workarounds: use a password-protected
     * certificate, use a different boot Java SDK implementation, or construct
     * the `SSLContext` yourself!  Needless to say, the password-protected
     * certificate is most recommended option.
     *
     * @param keyStore  the keystore
     * @param password  the password of the keystore
     * @return  this
     * @throws InvalidSSLConfig if stream is invalid Keystore
     *  or the password is invalid
     */
    public ApnsServiceBuilder withCert(KeyStore keyStore, String password)
    throws InvalidSSLConfig {
        assertPasswordNotEmpty(password);
        return withSSLContext(
                newSSLContext(keyStore, password, KEY_ALGORITHM));
    }
    
	private void assertPasswordNotEmpty(String password) {
		if (password == null || password.length() == 0) {
            throw new IllegalArgumentException("Passwords must be specified." +
                    "Oracle Java SDK does not support passwordless p12 certificates");
        }
	}
    
    /**
     * Specify the SSLContext that should be used to initiate the
     * connection to Apple Server.
     *
     * Most clients would use {@link #withCert(InputStream, String)}
     * or {@link #withCert(String, String)} instead.  But some
     * clients may need to represent the Keystore in a different
     * format than supported.
     *
     * @param sslContext    Context to be used to create secure connections
     * @return  this
     */
    public ApnsServiceBuilder withSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }
    
    /**
     * Specify the timeout value to be set in new setSoTimeout in created
     * sockets, for both feedback and push connections, in milliseconds.
     * @param readTimeout timeout value to be set in new setSoTimeout
     * @return this
     */
    public ApnsServiceBuilder withReadTimeout(int readTimeout) {
    	this.readTimeout = readTimeout;
    	return this;
    }

    /**
     * Specify the timeout value to use for connectionTimeout in created
     * sockets, for both feedback and push connections, in milliseconds.
     * @param connectTimeout timeout value to use for connectionTimeout
     * @return this
     */
    public ApnsServiceBuilder withConnectTimeout(int connectTimeout) {
    	this.connectTimeout = connectTimeout;
    	return this;
    }

    /**
     * Specify the gateway server for sending Apple iPhone
     * notifications.
     *
     * Most clients should use {@link #withSandboxDestination()}
     * or {@link #withProductionDestination()}.  Clients may use
     * this method to connect to mocking tests and such.
     *
     * @param host  hostname the notification gateway of Apple
     * @param port  port of the notification gateway of Apple
     * @return  this
     */
    public ApnsServiceBuilder withGatewayDestination(String host, int port) {
        this.gatewayHost = host;
        this.gatewayPort = port;
        return this;
    }

    /**
     * Specify the Feedback for getting failed devices from
     * Apple iPhone Push servers.
     *
     * Most clients should use {@link #withSandboxDestination()}
     * or {@link #withProductionDestination()}.  Clients may use
     * this method to connect to mocking tests and such.
     *
     * @param host  hostname of the feedback server of Apple
     * @param port  port of the feedback server of Apple
     * @return this
     */
    public ApnsServiceBuilder withFeedbackDestination(String host, int port) {
        this.feedbackHost = host;
        this.feedbackPort = port;
        return this;
    }

    /**
     * Specify to use Apple servers as iPhone gateway and feedback servers.
     *
     * If the passed {@code isProduction} is true, then it connects to the
     * production servers, otherwise, it connects to the sandbox servers
     *
     * @param isProduction  determines which Apple servers should be used:
     *               production or sandbox
     * @return this
     */
    public ApnsServiceBuilder withAppleDestination(boolean isProduction) {
        if (isProduction) {
            return withProductionDestination();
        } else {
            return withSandboxDestination();
        }
    }

    /**
     * Specify to use the Apple sandbox servers as iPhone gateway
     * and feedback servers.
     *
     * This is desired when in testing and pushing notifications
     * with a development provision.
     *
     * @return  this
     */
    public ApnsServiceBuilder withSandboxDestination() {
        return withGatewayDestination(SANDBOX_GATEWAY_HOST, SANDBOX_GATEWAY_PORT)
        .withFeedbackDestination(SANDBOX_FEEDBACK_HOST, SANDBOX_FEEDBACK_PORT);
    }

    /**
     * Specify to use the Apple Production servers as iPhone gateway
     * and feedback servers.
     *
     * This is desired when sending notifications to devices with
     * a production provision (whether through App Store or Ad hoc
     * distribution).
     *
     * @return  this
     */
    public ApnsServiceBuilder withProductionDestination() {
        return withGatewayDestination(PRODUCTION_GATEWAY_HOST, PRODUCTION_GATEWAY_PORT)
        .withFeedbackDestination(PRODUCTION_FEEDBACK_HOST, PRODUCTION_FEEDBACK_PORT);
    }

    /**
     * Specify the reconnection policy for the socket connection.
     *
     * Note: This option has no effect when using non-blocking
     * connections.
     */
    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy rp) {
        this.reconnectPolicy = rp;
        return this;
    }
    
    /**
     * Specify if the notification cache should auto adjust.
     * Default is true
     * 
     * @param autoAdjustCacheLength the notification cache should auto adjust.
     * @return this
     */
    public ApnsServiceBuilder withAutoAdjustCacheLength(boolean autoAdjustCacheLength) {
        this.autoAdjustCacheLength = autoAdjustCacheLength;
        return this;
    }

    /**
     * Specify the reconnection policy for the socket connection.
     *
     * Note: This option has no effect when using non-blocking
     * connections.
     */
    public ApnsServiceBuilder withReconnectPolicy(ReconnectPolicy.Provided rp) {
        this.reconnectPolicy = rp.newObject();
        return this;
    }

    /**
     * Specify the address of the SOCKS proxy the connection should
     * use.
     *
     * <p>Read the <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html">
     * Java Networking and Proxies</a> guide to understand the
     * proxies complexity.
     *
     * <p>Be aware that this method only handles SOCKS proxies, not
     * HTTPS proxies.  Use {@link #withProxy(Proxy)} instead.
     *
     * @param host  the hostname of the SOCKS proxy
     * @param port  the port of the SOCKS proxy server
     * @return  this
     */
    public ApnsServiceBuilder withSocksProxy(String host, int port) {
        Proxy proxy = new Proxy(Proxy.Type.SOCKS,
                new InetSocketAddress(host, port));
        return withProxy(proxy);
    }

    /**
     * Specify the proxy and the authentication parameters to be used
     * to establish the connections to Apple Servers.
     *
     * <p>Read the <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html">
     * Java Networking and Proxies</a> guide to understand the
     * proxies complexity.
     *
     * @param proxy the proxy object to be used to create connections
     * @param proxyUsername a String object representing the username of the proxy server
     * @param proxyPassword a String object representing the password of the proxy server
     * @return  this
     */
    public ApnsServiceBuilder withAuthProxy(Proxy proxy, String proxyUsername, String proxyPassword) {
        this.proxy = proxy;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        return this;
    }
    
    /**
     * Specify the proxy to be used to establish the connections
     * to Apple Servers
     *
     * <p>Read the <a href="http://java.sun.com/javase/6/docs/technotes/guides/net/proxies.html">
     * Java Networking and Proxies</a> guide to understand the
     * proxies complexity.
     *
     * @param proxy the proxy object to be used to create connections
     * @return  this
     */
    public ApnsServiceBuilder withProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
    
    /**
     * Specify the number of notifications to cache for error purposes.
     * Default is 100
     * 
     * @param cacheLength  Number of notifications to cache for error purposes
     * @return  this
     */
    public ApnsServiceBuilder withCacheLength(int cacheLength) {
        this.cacheLength = cacheLength;
        return this;
    }

    /**
     * Specify the socket to be used as underlying socket to connect
     * to the APN service.
     *
     * This assumes that the socket connects to a SOCKS proxy.
     *
     * @deprecated use {@link ApnsServiceBuilder#withProxy(Proxy)} instead
     * @param proxySocket   the underlying socket for connections
     * @return  this
     */
    @Deprecated
    public ApnsServiceBuilder withProxySocket(Socket proxySocket) {
        return this.withProxy(new Proxy(Proxy.Type.SOCKS,
                proxySocket.getRemoteSocketAddress()));
    }

    /**
     * Constructs a pool of connections to the notification servers.
     *
     * Apple servers recommend using a pooled connection up to
     * 15 concurrent persistent connections to the gateways.
     *
     * Note: This option has no effect when using non-blocking
     * connections.
     */
    public ApnsServiceBuilder asPool(int maxConnections) {
        return asPool(Executors.newFixedThreadPool(maxConnections), maxConnections);
    }

    /**
     * Constructs a pool of connections to the notification servers.
     *
     * Apple servers recommend using a pooled connection up to
     * 15 concurrent persistent connections to the gateways.
     *
     * Note: This option has no effect when using non-blocking
     * connections.
     *
     * Note: The maxConnections here is used as a hint to how many connections
     * get created.
     */
    public ApnsServiceBuilder asPool(ExecutorService executor, int maxConnections) {
        this.pooledMax = maxConnections;
        this.executor = executor;
        return this;
    }

    /**
     * Constructs a new thread with a processing queue to process
     * notification requests.
     *
     * @return  this
     */
    public ApnsServiceBuilder asQueued() {
        return asQueued(Executors.defaultThreadFactory());
    }
    
    /**
     * Constructs a new thread with a processing queue to process
     * notification requests.
     *
     * @param threadFactory
     *            thread factory to use for queue processing
     * @return  this
     */
    public ApnsServiceBuilder asQueued(ThreadFactory threadFactory) {
        this.isQueued = true;
        this.queueThreadFactory = threadFactory;
        return this;
    }
    
    /**
     * Construct service which will process notification requests in batch.
     * After each request batch will wait <code>waitTimeInSec (set as 5sec)</code> for more request to come
     * before executing but not more than <code>maxWaitTimeInSec (set as 10sec)</code>
     * 
     * Note: It is not recommended to use pooled connection
     */
    public ApnsServiceBuilder asBatched() {
        return asBatched(5, 10);
    }
    
    /**
     * Construct service which will process notification requests in batch.
     * After each request batch will wait <code>waitTimeInSec</code> for more request to come
     * before executing but not more than <code>maxWaitTimeInSec</code>
     * 
     * Note: It is not recommended to use pooled connection
     * 
     * @param waitTimeInSec
     *            time to wait for more notification request before executing
     *            batch
     * @param maxWaitTimeInSec
     *            maximum wait time for batch before executing
     */
    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec) {
        return asBatched(waitTimeInSec, maxWaitTimeInSec, null);
    }
    
    /**
     * Construct service which will process notification requests in batch.
     * After each request batch will wait <code>waitTimeInSec</code> for more request to come
     * before executing but not more than <code>maxWaitTimeInSec</code>
     * 
     * Each batch creates new connection and close it after finished.
     * In case reconnect policy is specified it will be applied by batch processing. 
     * E.g.: {@link ReconnectPolicy.Provided#EVERY_HALF_HOUR} will reconnect the connection in case batch is running for more than half an hour
     * 
     * Note: It is not recommended to use pooled connection
     * 
     * @param waitTimeInSec
     *            time to wait for more notification request before executing
     *            batch
     * @param maxWaitTimeInSec
     *            maximum wait time for batch before executing
     * @param threadFactory
     *            thread factory to use for batch processing
     */
    public ApnsServiceBuilder asBatched(int waitTimeInSec, int maxWaitTimeInSec, ThreadFactory threadFactory) {
        this.isBatched = true;
        this.batchWaitTimeInSec = waitTimeInSec;
        this.batchMaxWaitTimeInSec = maxWaitTimeInSec;
        this.batchThreadFactory = threadFactory;
        return this;
    }
    
    
    /**
     * Sets the delegate of the service, that gets notified of the
     * status of message delivery.
     *
     * Note: This option has no effect when using non-blocking
     * connections.
     */
    public ApnsServiceBuilder withDelegate(ApnsDelegate delegate) {
        this.delegate = delegate == null ? ApnsDelegate.EMPTY : delegate;
        return this;
    }

    /**
     * Disables the enhanced error detection, enabled by the
     * enhanced push notification interface.  Error detection is
     * enabled by default.
     *
     * This setting is desired when the application shouldn't spawn
     * new threads.
     *
     * @return  this
     */
    public ApnsServiceBuilder withNoErrorDetection() {
        this.errorDetection = false;
        return this;
    }

    /**
     * Provide a custom source for threads used for monitoring connections.
     *
     * This setting is desired when the application must obtain threads from a
     * controlled environment Google App Engine. 
     * @param threadFactory
     *            thread factory to use for error detection
     * @return  this
     */
    public ApnsServiceBuilder withErrorDetectionThreadFactory(ThreadFactory threadFactory) {
        this.errorDetectionThreadFactory = threadFactory;
        return this;
    }

    /**
     * Returns a fully initialized instance of {@link ApnsService},
     * according to the requested settings.
     *
     * @return  a new instance of ApnsService
     */
    public ApnsService build() {
        checkInitialization();
        ApnsService service;

        SSLSocketFactory sslFactory = sslContext.getSocketFactory();
        ApnsFeedbackConnection feedback = new ApnsFeedbackConnection(sslFactory, feedbackHost, feedbackPort, proxy, readTimeout, connectTimeout, proxyUsername, proxyPassword);

        ApnsConnection conn = new ApnsConnectionImpl(sslFactory, gatewayHost,
            gatewayPort, proxy, proxyUsername, proxyPassword, reconnectPolicy,
                delegate, errorDetection, errorDetectionThreadFactory, cacheLength,
                autoAdjustCacheLength, readTimeout, connectTimeout);
        if (pooledMax != 1) {
            conn = new ApnsPooledConnection(conn, pooledMax, executor);
        }

        service = new ApnsServiceImpl(conn, feedback);

        if (isQueued) {
            service = new QueuedApnsService(service, queueThreadFactory);
        }
        
        if (isBatched) {
            service = new BatchApnsService(conn, feedback, batchWaitTimeInSec, batchMaxWaitTimeInSec, batchThreadFactory);
        }

        service.start();

        return service;
    }

    private void checkInitialization() {
        if (sslContext == null)
            throw new IllegalStateException(
                    "SSL Certificates and attribute are not initialized\n"
                    + "Use .withCert() methods.");
        if (gatewayHost == null || gatewayPort == -1)
            throw new IllegalStateException(
                    "The Destination APNS server is not stated\n"
                    + "Use .withDestination(), withSandboxDestination(), "
                    + "or withProductionDestination().");
    }
}
