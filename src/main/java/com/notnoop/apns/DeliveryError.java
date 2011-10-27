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

/**
 * Errors in delivery that may get reported by Apple APN servers
 */
public enum DeliveryError {
    /**
     * Connection closed without any error.
     *
     * This may occur if the APN service faces an invalid simple
     * APNS notification while running in enhanced mode
     */
    NO_ERROR(0),
    PROCESSING_ERROR(1),
    MISSING_DEVICE_TOKEN(2),
    MISSING_TOPIC(3),
    MISSING_PAYLOAD(4),
    INVALID_TOKEN_SIZE(5),
    INVALID_TOPIC_SIZE(6),
    INVALID_PAYLOAD_SIZE(7),
    INVALID_TOKEN(8),

    NONE(255),
    UNKNOWN(254);

    private final byte code;
    DeliveryError(int code) {
        this.code = (byte)code;
    }

    /** The status code as specified by Apple */
    public int code() {
        return code;
    }

    /**
     * Returns the appropriate {@code DeliveryError} enum
     * corresponding to the Apple provided status code
     *
     * @param code  status code provided by Apple
     * @return  the appropriate DeliveryError
     */
    public static DeliveryError ofCode(int code) {
        for (DeliveryError e : DeliveryError.values()) {
            if (e.code == code)
                return e;
        }

        return UNKNOWN;
    }
}
