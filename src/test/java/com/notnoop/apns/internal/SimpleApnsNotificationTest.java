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
package com.notnoop.apns.internal;

import static org.junit.Assert.*;

import org.junit.experimental.theories.*;
import org.junit.runner.RunWith;

import com.notnoop.apns.PayloadBuilder;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.internal.Utilities;

import static com.notnoop.apns.PayloadBuilder.*;
import static com.notnoop.apns.internal.Utilities.*;

@SuppressWarnings("deprecation")
@RunWith(Theories.class)
public class SimpleApnsNotificationTest {

    // Device Tokens
    @DataPoints public static String[] deviceTokens =
    {
        "298893742908AB98C",
        "98234098203BACCCC93284092"
    };

    // Messages
    @DataPoints public static PayloadBuilder[] payloaders =
    {
        newPayload().alertBody("test").sound("default"),
        newPayload().sound("chimes").actionKey("Cancel"),
        newPayload().customField("notice", "this")
    };

    @Theory
    public void lengthConsistency(String deviceToken, PayloadBuilder payload) {
        SimpleApnsNotification msg = new SimpleApnsNotification(deviceToken, payload.build());
        assertEquals(msg.marshall().length, msg.length());
    }

    @Theory
    public void commandIsZero(String deviceToken, PayloadBuilder payload) {
        SimpleApnsNotification msg = new SimpleApnsNotification(deviceToken, payload.build());
        byte[] bytes = msg.marshall();
        assertEquals(0, /*command part*/ bytes[0]);
    }

    @Theory
    public void deviceTokenPart(String deviceToken, PayloadBuilder payload) {
        SimpleApnsNotification msg = new SimpleApnsNotification(deviceToken, payload.build());
        byte[] bytes = msg.marshall();

        byte[] dt = decodeHex(deviceToken);
        assertEquals(dt.length, /* found length */ ((bytes[1] & 0xff) << 8) + (bytes[2]& 0xff));

        // verify the device token part
        assertArrayEquals(dt, Utilities.copyOfRange(bytes, 3, 3 + dt.length));
    }

    @Theory
    public void payloadPart(String deviceToken, PayloadBuilder payload) {
        String payloadString = payload.build();
        SimpleApnsNotification msg = new SimpleApnsNotification(deviceToken, payloadString);
        byte[] bytes = msg.marshall();

        byte[] pl = toUTF8Bytes(payloadString);

        // in reverse
        int plBegin = bytes.length - pl.length;

        /// verify the payload part
        assertArrayEquals(pl, Utilities.copyOfRange(bytes, plBegin, bytes.length));
        assertEquals(pl.length, ((bytes[plBegin - 2] & 0xff) << 8) + (bytes[plBegin - 1] & 0xff));
    }

    @Theory
    public void allPartsLength(String deviceToken, PayloadBuilder payload) {
        String payloadString = payload.build();
        SimpleApnsNotification msg = new SimpleApnsNotification(deviceToken, payloadString);
        byte[] bytes = msg.marshall();

        int expectedLength = 1
            + 2 + decodeHex(deviceToken).length
            + 2 + toUTF8Bytes(payloadString).length;
        assertEquals(expectedLength, bytes.length);
    }
}
