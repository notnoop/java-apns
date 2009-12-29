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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Represents the connection and interface to the Apple APNS servers.
 *
 * The service is created by {@link ApnsServiceBuilder} like:
 *
 * <pre>
 *   ApnsService = APNS.newService()
 *                  .withCert("/path/to/certificate.p12", "MyCertPassword")
 *                  .withSandboxDestination()
 *                  .build()
 * </pre>
 */
public interface ApnsService {

    /**
     * Sends a push notification with the provided {@code payload} to the
     * iPhone of {@code deviceToken}.
     *
     * The payload needs to be a valid JSON object, otherwise it may fail
     * silently.  It is recommended to use {@link PayloadBuilder} to create
     * one.
     *
     * @param deviceToken   the destination iPhone device token
     * @param payload       The payload message
     */
    void push(String deviceToken, String payload);

    /**
     * Sends a bulk push notification with the provided
     * {@code payload} to iPhone of {@code deviceToken}s set.
     *
     * The payload needs to be a valid JSON object, otherwise it may fail
     * silently.  It is recommended to use {@link PayloadBuilder} to create
     * one.
     *
     * @param deviceTokens   the destination iPhone device tokens
     * @param payload       The payload message
     */
    void push(Collection<String> deviceTokens, String payload);

    /**
     * Sends the provided notification {@code message} to the desired
     * destination.
     */
    void push(ApnsNotification message);

    /**
     * Starts the service.
     *
     * The underlying implementation may prepare its connections or
     * datastructures to be able to send the messages.
     *
     * This method is a blocking call, even if the service represents
     * a Non-blocking push service.  Once the service is returned, it is ready
     * to accept push requests.
     */
    void start();

    /**
     * Stops the service and frees any allocated resources it created for this
     * service.
     *
     * The underlying implementation should close all connections it created,
     * and possibly stop any threads as well.
     */
    void stop();

    /**
     * Returns the list of devices that reported failed-delivery
     * attempts to the Apple Feedback services.
     *
     * The result is map, mapping the device tokens as Hex Strings
     * mapped to the timestamp when APNs determined that the
     * application no longer exists on the device.
     */
    Map<String, Date> getInactiveDevices();
}
