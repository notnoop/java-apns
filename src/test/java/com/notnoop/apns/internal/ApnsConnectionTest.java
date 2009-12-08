package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;

import javax.net.SocketFactory;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import com.notnoop.apns.ApnsNotification;

@RunWith(Theories.class)
public class ApnsConnectionTest {
    ApnsNotification msg = new ApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");

    static ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @DataPoint public static SocketFactory SIMPLE = MockingUtils.mockSocketFactory(baos);
    @DataPoint public static SocketFactory CLOSED_RETRY = MockingUtils.mockClosedThenOpenSocket(baos, true, 1);
    @DataPoint public static SocketFactory ERROR_RETRY = MockingUtils.mockClosedThenOpenSocket(baos, false, 1);
    @DataPoint public static SocketFactory TWICE_ERROR_RETRY = MockingUtils.mockClosedThenOpenSocket(baos, false, 2);

    @Theory
    public void packetSentRegardlesss(SocketFactory sf) {
        baos.reset();
        ApnsConnection connection = new ApnsConnection(sf, "localhost", 80);
        connection.DELAY_IN_MS = 0;
        connection.sendMessage(msg);
        Assert.assertArrayEquals(msg.marshall(), baos.toByteArray());
    }
}
