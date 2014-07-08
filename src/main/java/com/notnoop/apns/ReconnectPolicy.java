/*
 * Copyright 2010, Mahmood Ali.
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

import com.notnoop.apns.internal.ReconnectPolicies;

/**
 * Represents the reconnection policy for the library.
 *
 * Each object should be used exclusively for one
 * {@code ApnsService} only.
 */
public interface ReconnectPolicy {
    /**
     * Returns {@code true} if the library should initiate a new
     * connection for sending the message.
     *
     * The library calls this method at every message push.
     *
     * @return true if the library should be reconnected
     */
    public boolean shouldReconnect();

    /**
     * Callback method to be called whenever the library
     * makes a new connection
     */
    public void reconnected();

    /**
     * Returns a deep copy of this reconnection policy, if needed.
     *
     * Subclasses may return this instance if the object is immutable.
     */
    public ReconnectPolicy copy();

    /**
     * Types of the library provided reconnection policies.
     *
     * This should capture most of the commonly used cases.
     */
    public enum Provided {
        /**
         * Only reconnect if absolutely needed, e.g. when the connection is dropped.
         * <p>
         * Apple recommends using a persistent connection.  This improves the latency of sending push notification messages.
         * <p>
         * The down-side is that once the connection is closed ungracefully (e.g. because Apple server drops it), the library wouldn't
         * detect such failure and not warn against the messages sent after the drop before the detection.
         */
        NEVER {
            @Override
            public ReconnectPolicy newObject() {
                return new ReconnectPolicies.Never();
            }
        },

        /**
         * Makes a new connection if the current connection has lasted for more than half an hour.
         * <p>
         * This is the recommended mode.
         * <p>
         * This is the sweat-spot in my experiments between dropped connections while minimizing latency.
         */
        EVERY_HALF_HOUR {
            @Override
            public ReconnectPolicy newObject() {
                return new ReconnectPolicies.EveryHalfHour();
            }
        },

        /**
         * Makes a new connection for every message being sent.
         *
         * This option ensures that each message is actually
         * delivered to Apple.
         *
         * If you send <strong>a lot</strong> of messages though,
         * Apple may consider your requests to be a DoS attack.
         */
        EVERY_NOTIFICATION {
            @Override
            public ReconnectPolicy newObject() {
                return new ReconnectPolicies.Always();
            }
        };

        abstract ReconnectPolicy newObject();
    }
}
