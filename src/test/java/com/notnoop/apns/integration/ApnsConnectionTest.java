package com.notnoop.apns.integration;

import org.junit.*;
import static org.junit.Assert.*;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.utils.ApnsServerStub;
import static com.notnoop.apns.utils.FixedCertificates.*;

public class ApnsConnectionTest {
    ApnsServerStub server;

    static ApnsNotification msg1 = new ApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");
    static ApnsNotification msg2 = new ApnsNotification ("a87d8878d878a88", "{\"aps\":{}}");

    @Before
    public void startup() {
        server = ApnsServerStub.prepareAndStartServer(TEST_GATEWAY_PORT, TEST_FEEDBACK_PORT);
    }

    @After 
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Test
    public void sendOneSimple() throws InterruptedException {
        ApnsService service =
            APNS.newService().withSSLContext(clientContext())
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.semaphore.acquire();

        assertArrayEquals(msg1.marshall(), server.received.toByteArray());
    }

    @Test
    public void sendOneQueued() throws InterruptedException {
        ApnsService service =
            APNS.newService().withSSLContext(clientContext())
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .asQueued()
            .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.semaphore.acquire();

        assertArrayEquals(msg1.marshall(), server.received.toByteArray());
    }

    @Test
    @Ignore
    public void sendOneMina() throws InterruptedException {
        ApnsService service =
            APNS.newService().withCert(clientCertPath(), CLIENT_PASSWD)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .asNonBlocking()
            .build();

        server.stopAt(msg1.length());
        service.push(msg1);
        server.semaphore.acquire();

        assertArrayEquals(msg1.marshall(), server.received.toByteArray());
    }
}
