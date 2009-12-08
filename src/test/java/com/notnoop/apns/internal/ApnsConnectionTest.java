package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;

import javax.net.SocketFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.notnoop.apns.ApnsNotification;

public class ApnsConnectionTest {
    ApnsNotification msg = new ApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");

    ByteArrayOutputStream baos;
    SocketFactory sf;
    ApnsConnection connection;

    @Before public void setUp() {
        baos = new ByteArrayOutputStream();
    }

    @After public void after() {
        ApnsConnection connection = new ApnsConnection(sf, "localhost", 80);
        connection.sendMessage(msg);
        Assert.assertArrayEquals(msg.marshall(), baos.toByteArray());
    }

    @Test
    public void messageSentOnWire() {
        sf = MockingUtils.mockSocketFactory(baos);
    }

    @Test
    public void retryOnClosedSocket() {
        sf = MockingUtils.mockClosedThenOpenSocket(baos, true, 2);
    }

    @Test
    public void retryOnError() {
        sf = MockingUtils.mockClosedThenOpenSocket(baos, false, 2);
    }
}
