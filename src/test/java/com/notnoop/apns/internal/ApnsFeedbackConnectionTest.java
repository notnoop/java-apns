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
    public void feedbackWithclosedSocket() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 1);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

    @Test
    public void feedbackWitherrorOnce() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 2);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

    /**
     * Connection fails after three retries
     */
    @Test(expected = Exception.class)
    public void feedbackWitherrorTwice() {
        SocketFactory sf = mockClosedThenOpenSocket(null, simpleStream, true, 3);
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        checkParsedSimple(connection.getInactiveDevices());
    }

}
