package com.notnoop.apns.internal;

import static com.notnoop.apns.internal.MockingUtils.*;

import java.io.ByteArrayOutputStream;

import javax.net.SocketFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

import com.notnoop.apns.SimpleApnsNotification;


public class ApnsConnectionTest {
    SimpleApnsNotification msg = new SimpleApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");

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
