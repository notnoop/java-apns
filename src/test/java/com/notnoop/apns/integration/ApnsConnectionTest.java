package com.notnoop.apns.integration;

import org.junit.*;
import static org.junit.Assert.*;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.utils.ApnsServerStub;
import static com.notnoop.apns.utils.FixedCertificates.*;

public class ApnsConnectionTest {
    ApnsServerStub server;

    static SimpleApnsNotification msg1 = new SimpleApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");
    static SimpleApnsNotification msg2 = new SimpleApnsNotification ("a87d8878d878a88", "{\"aps\":{}}");

    @Before
    public void startup() {
        server = ApnsServerStub.prepareAndStartServer(TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Test(timeout = 2000)
    public void sendOneSimple() throws InterruptedException {
        ApnsService service =
            APNS.newService().withSSLContext(clientContext())
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.messages.acquire();

        assertArrayEquals(msg1.marshall(), server.received.toByteArray());
    }

    @Test(timeout = 2000)
    public void sendOneQueued() throws InterruptedException {
        ApnsService service =
            APNS.newService().withSSLContext(clientContext())
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .asQueued()
            .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.messages.acquire();

        assertArrayEquals(msg1.marshall(), server.received.toByteArray());
    }
}
