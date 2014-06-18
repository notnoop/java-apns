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
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.RuntimeIOException;
import static com.notnoop.apns.internal.Utilities.*;

/**
 * The class is used to create instances of {@link ApnsService}.
 * 
 * Note that this class is not synchronized. If multiple threads access a
 * {@code ApnsServiceBuilder} instance concurrently, and at least on of the
 * threads modifies one of the attributes structurally, it must be synchronized
 * externally.
 * 
 * Starting a new {@code ApnsService} is easy:
 * 
 * <pre>
 * ApnsService = APNS.newService()
 * 		.withCert(&quot;/path/to/certificate.p12&quot;, &quot;MyCertPassword&quot;)
 * 		.withSandboxDestination().build()
 * </pre>
 */
public class ApnsServerSocketBuilder {
	private static final String KEYSTORE_TYPE = "PKCS12";
	private static final String KEY_ALGORITHM = "sunx509";

	private SSLContext sslContext;
	private int gatewayPort = -1;
	private int feedbackPort = -1;
	private ExecutorService executor = null;
	private ApnsServerService serverService = ApnsServerService.EMPTY;
	private ApnsServerExceptionDelegate exceptionDelegate = ApnsServerExceptionDelegate.EMPTY;

	/**
	 * Constructs a new instance of {@code ApnsServiceBuilder}
	 */
	public ApnsServerSocketBuilder() {
		sslContext = null;
	}

	/**
	 * Specify the certificate used to connect to Apple APNS servers. This
	 * relies on the path (absolute or relative to working path) to the keystore
	 * (*.p12) containing the certificate, along with the given password.
	 * 
	 * The keystore needs to be of PKCS12 and the keystore needs to be encrypted
	 * using the SunX509 algorithm. Both of these settings are the default.
	 * 
	 * This library does not support password-less p12 certificates, due to a
	 * Oracle Java library <a
	 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637"> Bug
	 * 6415637</a>. There are three workarounds: use a password-protected
	 * certificate, use a different boot Java SDK implementation, or construct
	 * the `SSLContext` yourself! Needless to say, the password-protected
	 * certificate is most recommended option.
	 * 
	 * @param fileName
	 *            the path to the certificate
	 * @param password
	 *            the password of the keystore
	 * @return this
	 * @throws RuntimeIOException
	 *             if it {@code fileName} cannot be found or read
	 * @throws InvalidSSLConfig
	 *             if fileName is invalid Keystore or the password is invalid
	 */
	public ApnsServerSocketBuilder withCert(String fileName, String password)
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
	 * Specify the certificate used to connect to Apple APNS servers. This
	 * relies on the stream of keystore (*.p12) containing the certificate,
	 * along with the given password.
	 * 
	 * The keystore needs to be of PKCS12 and the keystore needs to be encrypted
	 * using the SunX509 algorithm. Both of these settings are the default.
	 * 
	 * This library does not support password-less p12 certificates, due to a
	 * Oracle Java library <a
	 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637"> Bug
	 * 6415637</a>. There are three workarounds: use a password-protected
	 * certificate, use a different boot Java SDK implementation, or construct
	 * the `SSLContext` yourself! Needless to say, the password-protected
	 * certificate is most recommended option.
	 * 
	 * @param stream
	 *            the keystore represented as input stream
	 * @param password
	 *            the password of the keystore
	 * @return this
	 * @throws InvalidSSLConfig
	 *             if stream is invalid Keystore or the password is invalid
	 */
	public ApnsServerSocketBuilder withCert(InputStream stream, String password)
			throws InvalidSSLConfig {
		assertPasswordNotEmpty(password);
		return withSSLContext(newSSLContext(stream, password, KEYSTORE_TYPE,
				KEY_ALGORITHM));
	}

	/**
	 * Specify the certificate used to connect to Apple APNS servers. This
	 * relies on a keystore (*.p12) containing the certificate, along with the
	 * given password.
	 * 
	 * This library does not support password-less p12 certificates, due to a
	 * Oracle Java library <a
	 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6415637"> Bug
	 * 6415637</a>. There are three workarounds: use a password-protected
	 * certificate, use a different boot Java SDK implementation, or construct
	 * the `SSLContext` yourself! Needless to say, the password-protected
	 * certificate is most recommended option.
	 * 
	 * @param keyStore the keystore
	 * @param password the password of the keystore
	 * @return this
	 * @throws InvalidSSLConfig if stream is invalid Keystore or the password is invalid
	 */
	public ApnsServerSocketBuilder withCert(KeyStore keyStore, String password)
			throws InvalidSSLConfig {
		assertPasswordNotEmpty(password);
		return withSSLContext(newSSLContext(keyStore, password, KEY_ALGORITHM));
	}

	private void assertPasswordNotEmpty(String password) {
		if (password == null || password.length() == 0) {
			throw new IllegalArgumentException(
					"Passwords must be specified."
							+ "Oracle Java SDK does not support passwordless p12 certificates");
		}
	}

	/**
	 * Specify the SSLContext that should be used to initiate the connection to
	 * Apple Server.
	 * 
	 * Most clients would use {@link #withCert(InputStream, String)} or
	 * {@link #withCert(String, String)} instead. But some clients may need to
	 * represent the Keystore in a different format than supported.
	 * 
	 * @param sslContext
	 *            Context to be used to create secure connections
	 * @return this
	 */
	public ApnsServerSocketBuilder withSSLContext(SSLContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}

	/**
	 * Specify the gateway server for sending Apple iPhone notifications.
	 * 
	 * @param port
	 *            port of the notification gateway of Apple
	 * @return this
	 */
	public ApnsServerSocketBuilder withGatewayDestination(int port) {
		this.gatewayPort = port;
		return this;
	}

	/**
	 * Specify the Feedback for getting failed devices from Apple iPhone Push
	 * servers.
	 * 
	 * @param port
	 *            port of the feedback server of Apple
	 * @return this
	 */
	public ApnsServerSocketBuilder withFeedbackDestination(int port) {
		this.feedbackPort = port;
		return this;
	}

	/**
	 * Specify to use the Apple sandbox servers as iPhone gateway and feedback
	 * servers.
	 * 
	 * This is desired when in testing and pushing notifications with a
	 * development provision.
	 * 
	 * @return this
	 */
	public ApnsServerSocketBuilder withSandboxDestination() {
		return withGatewayDestination(SANDBOX_GATEWAY_PORT)
				.withFeedbackDestination(SANDBOX_FEEDBACK_PORT);
	}

	/**
	 * Specify to use the Apple Production servers as iPhone gateway and
	 * feedback servers.
	 * 
	 * This is desired when sending notifications to devices with a production
	 * provision (whether through App Store or Ad hoc distribution).
	 * 
	 * @return this
	 */
	public ApnsServerSocketBuilder withProductionDestination() {
		return withGatewayDestination(PRODUCTION_GATEWAY_PORT)
				.withFeedbackDestination(PRODUCTION_FEEDBACK_PORT);
	}

	/**
	 * Constructs a pool of connections to the notification servers.
	 * 
	 * Apple servers recommend using a pooled connection up to 15 concurrent
	 * persistent connections to the gateways.
	 * 
	 * Note: This option has no effect when using non-blocking connections.
	 */
	public ApnsServerSocketBuilder asPool(int maxConnections) {
		return asPool(new ThreadPoolExecutor(maxConnections, Integer.MAX_VALUE,
				60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
	}

	/**
	 * Constructs a pool of connections to the notification servers.
	 * 
	 * Apple servers recommend using a pooled connection up to 15 concurrent
	 * persistent connections to the gateways.
	 * 
	 * Note: This option has no effect when using non-blocking connections.
	 */
	public ApnsServerSocketBuilder asPool(ExecutorService executor) {
		this.executor = executor;
		return this;
	}

	/**
	 * Sets the delegate of the service, that gets notified of the status of
	 * message delivery.
	 * 
	 * Note: This option has no effect when using non-blocking connections.
	 */
	public ApnsServerSocketBuilder withService(ApnsServerService serverService) {
		this.serverService = serverService == null ? ApnsServerService.EMPTY
				: serverService;
		return this;
	}

	/**
	 * Sets the delegate of the service, that gets notified of the status of
	 * message delivery.
	 * 
	 * Note: This option has no effect when using non-blocking connections.
	 */
	public ApnsServerSocketBuilder withDelegate(
			ApnsServerExceptionDelegate exceptionDelegate) {
		this.exceptionDelegate = exceptionDelegate == null ? ApnsServerExceptionDelegate.EMPTY
				: exceptionDelegate;
		return this;
	}

	/**
	 * Returns a fully initialized instance of {@link ApnsService}, according to
	 * the requested settings.
	 * 
	 * @return a new instance of ApnsService
	 * @throws IOException
	 */
	public ApnsSocketService build() throws IOException {
		checkInitialization();

		AbstractApnsServerSocket apnsPushServerSocket = new ApnsGatewayServerSocket(
				sslContext, gatewayPort, executor, serverService,
				exceptionDelegate);
		AbstractApnsServerSocket apnsFeedbackServerSocket = new ApnsFeedbackServerSocket(
				sslContext, feedbackPort, executor, serverService,
				exceptionDelegate);

		ApnsSocketService service = new ApnsSocketService(apnsPushServerSocket,
				apnsFeedbackServerSocket);
		service.start();
		return service;
	}

	private void checkInitialization() {
		if (sslContext == null) {
			throw new IllegalStateException(
					"SSL Certificates and attribute are not initialized\n"
							+ "Use .withCert() methods.");
		}

		if (executor == null) {
			throw new IllegalStateException(
					"SSL Certificates and attribute are not initialized\n"
							+ "Use .withCert() methods.");
		}

		if (gatewayPort == -1 || feedbackPort == -1) {
			throw new IllegalStateException(
					"The Destination APNS server is not stated\n"
							+ "Use .withDestination(), withSandboxDestination(), "
							+ "or withProductionDestination().");
		}
	}
}
