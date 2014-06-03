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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import com.notnoop.apns.internal.Utilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Represents an APNS notification to be sent to Apple service.
 */
public class EnhancedApnsNotification implements ApnsNotification {

    private final static byte COMMAND = 1;
    private static AtomicInteger nextId = new AtomicInteger(0);
    private final int identifier;
    private final int expiry;
    private final byte[] deviceToken;
    private final byte[] payload;

    public static int INCREMENT_ID() {
        return nextId.incrementAndGet();
    }
    
    /**
     * The infinite future for the purposes of Apple expiry date
     */
    public final static int MAXIMUM_EXPIRY = Integer.MAX_VALUE;

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * The message encodes the payload with a {@code UTF-8} encoding.
     *
     * @param dtoken    The Hex of the device token of the destination phone
     * @param payload   The payload message to be sent
     */
    public EnhancedApnsNotification(
            int identifier, int expiryTime,
            String dtoken, String payload) {
        this.identifier = identifier;
        this.expiry = expiryTime;
        this.deviceToken = Utilities.decodeHex(dtoken);
        this.payload = Utilities.toUTF8Bytes(payload);
    }

    /**
     * Constructs an instance of {@code ApnsNotification}.
     *
     * @param dtoken    The binary representation of the destination device token
     * @param payload   The binary representation of the payload to be sent
     */
    public EnhancedApnsNotification(
            int identifier, int expiryTime,
            byte[] dtoken, byte[] payload) {
        this.identifier = identifier;
        this.expiry = expiryTime;
        this.deviceToken = Utilities.copyOf(dtoken);
        this.payload = Utilities.copyOf(payload);
    }

    /**
     * Returns the binary representation of the device token.
     *
     */
    public byte[] getDeviceToken() {
        return Utilities.copyOf(deviceToken);
    }

    /**
     * Returns the binary representation of the payload.
     *
     */
    public byte[] getPayload() {
        return Utilities.copyOf(payload);
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getExpiry() {
        return expiry;
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
        if (marshall == null) {
            marshall = Utilities.marshallEnhanced(COMMAND, identifier,
                    expiry, deviceToken, payload);
        }
        return marshall.clone();
    }

    /**
     * Returns the length of the message in bytes as it is encoded on the wire.
     *
     * Apple require the message to be of length 255 bytes or less.
     *
     * @return length of encoded message in bytes
     */
    public int length() {
        int length = 1 + 4 + 4 + 2 + deviceToken.length + 2 + payload.length;
        final int marshalledLength = marshall().length;
        assert marshalledLength == length;
        return length;
    }

    @Override
    public int hashCode() {
        return (21
               + 31 * identifier
               + 31 * expiry
               + 31 * Arrays.hashCode(deviceToken)
               + 31 * Arrays.hashCode(payload));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnhancedApnsNotification))
            return false;
        EnhancedApnsNotification o = (EnhancedApnsNotification)obj;
        return (identifier == o.identifier
                && expiry == o.expiry
                && Arrays.equals(this.deviceToken, o.deviceToken)
                && Arrays.equals(this.payload, o.payload));
    }

    @Override
    @SuppressFBWarnings("DE_MIGHT_IGNORE")
    public String toString() {
        String payloadString;
        try {
            payloadString = new String(payload, "UTF-8");
        } catch (Exception ex) {
            payloadString = "???";
        }
        return "Message(Id="+identifier+"; Token="+Utilities.encodeHex(deviceToken)+"; Payload="+payloadString+")";
    }
}
