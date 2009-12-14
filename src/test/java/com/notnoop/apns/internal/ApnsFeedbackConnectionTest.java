package com.notnoop.apns.internal;

import java.io.ByteArrayInputStream;

import javax.net.SocketFactory;

import org.junit.Ignore;
import org.junit.Test;

import static com.notnoop.apns.internal.ApnsFeedbackParsingUtils.*;

public class ApnsFeedbackConnectionTest {

    /** Simple Parsing **/
    @Test
    public void rowParseOneDevice() {
        checkRawSimple(Utilities.parseFeedbackStreamRaw(new ByteArrayInputStream(simple)));
    }

    @Test
    @Ignore
    public void threeParseTwoDevices() {
        checkRawThree(Utilities.parseFeedbackStreamRaw(new ByteArrayInputStream(three)));
    }

    @Test
    public void parsedSimple() {
        checkParsedSimple(Utilities.parseFeedbackStream(new ByteArrayInputStream(simple)));
    }

    @Test
    @Ignore
    public void parsedThree() {
        checkParsedThree(Utilities.parseFeedbackStream(new ByteArrayInputStream(three)));
    }

    /** With Connection **/
    @Test
    public void connectionParsedOne() {
        SocketFactory sf = MockingUtils.mockSocketFactory(null, new ByteArrayInputStream(simple));
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        checkParsedSimple(connection.getInactiveDevices());
    }

    @Test
    @Ignore
    public void connectionParsedThree() {
        SocketFactory sf = MockingUtils.mockSocketFactory(null, new ByteArrayInputStream(three));
        ApnsFeedbackConnection connection = new ApnsFeedbackConnection(sf, "localhost", 80);
        checkParsedThree(connection.getInactiveDevices());
    }

}
