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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.net.SocketFactory;

import org.junit.Test;

import static com.notnoop.apns.internal.ApnsFeedbackParsingUtils.*;
import static com.notnoop.apns.internal.MockingUtils.mockClosedThenOpenSocket;

public class ApnsFeedbackConnectionTest {

    InputStream simpleStream = new ByteArrayInputStream(simple);
    InputStream threeStream = new ByteArrayInputStream(three);

    /** Simple Parsing **/
    @Test
    public void rowParseOneDevice() {
        checkRawSimple(Utilities.parseFeedbackStreamRaw(simpleStream));
    }

    @Test
    public void threeParseTwoDevices() {
        checkRawThree(Utilities.parseFeedbackStreamRaw(threeStream));
    }

    @Test
    public void parsedSimple() {
        checkParsedSimple(Utilities.parseFeedbackStream(simpleStream));
    }

    @Test
    public void parsedThree() {
        checkParsedThree(Utilities.parseFeedbackStream(threeStream));
    }

    /** With Connection **/
    @Test
    public void connectionParsedOne() {
        SocketFactory sf = MockingUtils.mockSocketFactory(null, simpleStream);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        checkParsedSimple(connection.getInactiveDevices());
    }

    @Test
    public void connectionParsedThree() {
        SocketFactory sf = MockingUtils.mockSocketFactory(null, threeStream);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        checkParsedThree(connection.getInactiveDevices());
    }

    /** Check error recover **/
    @Test
    public void feedbackWithClosedSocket() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 1);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

    @Test
    public void feedbackWithErrorOnce() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 2);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

    /**
     * Connection fails after three retries
     */
    @Test(expected = Exception.class)
    public void feedbackWithErrorTwice() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 3);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

}
