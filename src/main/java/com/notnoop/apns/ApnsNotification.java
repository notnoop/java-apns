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

import java.util.Arrays;

import com.notnoop.apns.internal.Utilities;

/**
 * Represents an APNS notification to be sent to Apple service.
 */
public class ApnsNotification {

    private final static byte COMMAND = 0;
    private final byte[] deviceToken;
    private final byte[] payload;

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * The message encodes the payload with a {@code UTF-8} encoding.
     *
     * @param dtoken    The Hex of the device token of the destination phone
     * @param payload   The payload message to be sent
     */
    public ApnsNotification(String dtoken, String payload) {
        this.deviceToken = Utilities.decodeHex(dtoken);
        this.payload = Utilities.toUTF8Bytes(payload);
    }

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * @param dtoken    The binary representation of the destination device token
     * @param payload   The binary representation of the payload to be sent
     */
    public ApnsNotification(byte[] dtoken, byte[] payload) {
        this.deviceToken = Arrays.copyOf(dtoken, dtoken.length);
        this.payload = Arrays.copyOf(payload, payload.length);
    }

    /**
     * Returns the binary representation of the device token.
     *
     * Note this message returns the underlying byte array, and clients should
     * not modify the returns array.
     */
    public byte[] getDeviceToken() {
        return this.deviceToken;
    }

    /**
     * Returns the binary representation of the payload.
     *
     * Note: this message returns the underlying byte array, and clients should
     * not modify the returned array.
     */
    public byte[] getPayload() {
        return this.payload;
    }

    private byte[] marshall = null;
    /**
     * Returns the binary representation of the message as expected by the
     * APNS server.
     *
     * The returned array can be used to sent directly to the APNS server
     * (on the wire/socket) without any modification.
     */
    public byte[] marshall() {
        if (marshall == null)
            marshall = Utilities.marshall(COMMAND, deviceToken, payload);
        return marshall;
    }

    /**
     * Returns the length of the message in bytes as it is encoded on the wire.
     *
     * Apple require the message to be of length 255 bytes or less.
     *
     * @return length of encoded message in bytes
     */
    public int length() {
        int length = 1 + 2 + deviceToken.length + 2 + payload.length;
        assert marshall().length == length;
        return length;
    }

    @Override
    public int hashCode() {
        return 21
               + 31 * Arrays.hashCode(deviceToken)
               + 31 * Arrays.hashCode(payload);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ApnsNotification))
            return false;
        ApnsNotification o = (ApnsNotification)obj;
        return Arrays.equals(this.deviceToken, o.deviceToken)
                && Arrays.equals(this.payload, o.payload);
    }
}
