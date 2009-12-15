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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import com.notnoop.apns.internal.ApnsConnection;
import com.notnoop.apns.internal.ApnsFeedbackConnection;
import com.notnoop.apns.internal.ApnsServiceImpl;
import com.notnoop.apns.internal.MinaAdaptor;
import com.notnoop.apns.internal.QueuedApnsService;
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
    private static final String KEY_ALGORITHM = "sunx509";

    private SSLContext sslContext;

    private String gatewayHost;
    private int gatewaPort = -1;

    private String feedbackHost;
    private int feedbackPort;

    private boolean isQueued = false;
    private boolean isNonBlocking = false;

    /**
     * Constructs a new instance of {@code ApnsServiceBuilder}
     */
    public ApnsServiceBuilder() { }

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
     * @param fileName  the path to the certificate
     * @param password  the password of the keystore
     * @return  this
     */
    public ApnsServiceBuilder withCert(String fileName, String password) {
        try {
            return withCert(new FileInputStream(fileName), password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
     * @param stream    the keystore represented as input stream
     * @param password  the password of the keystore
     * @return  this
     */
    public ApnsServiceBuilder withCert(InputStream stream, String password) {
        try {
            return withSSLContext(
                    newSSLContext(stream, password,
                            KEYSTORE_TYPE, KEY_ALGORITHM));
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        this.gatewaPort = port;
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
     * Constructs a new thread with a processing queue to process
     * notification requests.
     *
     * @return  this
     */
    public ApnsServiceBuilder asQueued() {
        this.isQueued = true;
        return this;
    }

    /**
     * Constructs non-blocking queues and sockets connections
     * to send the iPhone notifications.
     *
     * @return  this
     */
    public ApnsServiceBuilder asNonBlocking() {
        this.isNonBlocking = true;
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
        ApnsFeedbackConnection feedback = new ApnsFeedbackConnection(sslFactory, feedbackHost, feedbackPort);

        if (isNonBlocking) {
            service = new MinaAdaptor(sslContext, gatewayHost, gatewaPort, feedback);
        } else {
            ApnsConnection conn = new ApnsConnection(sslFactory, gatewayHost, gatewaPort);
            service = new ApnsServiceImpl(conn, feedback);

            if (isQueued) {
                service = new QueuedApnsService(service);
            }
        }

        service.start();

        return service;
    }

    private void checkInitialization() {
        if (sslContext == null)
            throw new IllegalStateException(
                    "SSL Certificates and attribute are not initialized\n"
                    + "Use .withCert() methods.");
        if (gatewayHost == null || gatewaPort == -1)
            throw new IllegalStateException(
                    "The Destination APNS server is not stated\n"
                    + "Use .withDestination(), withSandboxDestination(), "
                    + "or withProductionDestination().");
    }
}
