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

import java.io.ByteArrayOutputStream;
import javax.net.SocketFactory;
import com.notnoop.apns.SimpleApnsNotification;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import static com.notnoop.apns.internal.MockingUtils.*;


@SuppressWarnings("deprecation")
public class ApnsConnectionTest {
    private SimpleApnsNotification msg = new SimpleApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");

    @Test
    public void simpleSocket() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SocketFactory factory = mockSocketFactory(baos, null);
        packetSentRegardless(factory, baos);
    }

    @Test
    @Ignore
    public void closedSocket() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SocketFactory factory = mockClosedThenOpenSocket(baos, null, true, 1);
        packetSentRegardless(factory, baos);
    }

    @Test
    public void errorOnce() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SocketFactory factory = mockClosedThenOpenSocket(baos, null, false, 1);
        packetSentRegardless(factory, baos);
    }

    @Test
    public void errorTwice() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SocketFactory factory = mockClosedThenOpenSocket(baos, null, false, 2);
        packetSentRegardless(factory, baos);
    }

    /**
     * Connection fails after three retries
     */
    @Test(expected = Exception.class)
    public void errorThrice() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SocketFactory factory = mockClosedThenOpenSocket(baos, null, false, 3);
        packetSentRegardless(factory, baos);
    }

    private void packetSentRegardless(SocketFactory sf, ByteArrayOutputStream baos) {
        ApnsConnectionImpl connection = new ApnsConnectionImpl(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        connection.sendMessage(msg);
        Assert.assertArrayEquals(msg.marshall(), baos.toByteArray());
        connection.close();
    }
}
