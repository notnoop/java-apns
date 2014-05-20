package com.notnoop.apns.integration;

import static com.notnoop.apns.utils.FixedCertificates.*;

import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import static com.notnoop.apns.internal.ApnsFeedbackParsingUtils.*;
import com.notnoop.apns.utils.ApnsServerStub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FeedbackTest {

    ApnsServerStub server;
    SSLContext clientContext = clientContext();


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
    public void simpleFeedback() throws IOException {
        server.toSend.write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }
    
    @Test
    public void simpleFeedbackWithoutTimeout() throws IOException {
        server.toSend.write(simple);
        server.toWaitBeforeSend.set(2000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .withReadTimeout(3000)
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test()
    public void simpleFeedbackWithTimeout() throws IOException {
        server.toSend.write(simple);
        server.toWaitBeforeSend.set(5000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .withReadTimeout(1000)
            .build();
        try {
            service.getInactiveDevices();
            fail("RuntimeException expected");
        }
        catch(RuntimeException e) {
            assertEquals("Socket timeout exception expected", 
                    SocketTimeoutException.class, e.getCause().getClass() );
        }
    }

    @Test
    public void threeFeedback() throws IOException {
        server.toSend.write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

    @Test
    public void simpleQueuedFeedback() throws IOException {
        server.toSend.write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .asQueued()
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test
    public void threeQueuedFeedback() throws IOException {
        server.toSend.write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(TEST_HOST, TEST_GATEWAY_PORT)
            .withFeedbackDestination(TEST_HOST, TEST_FEEDBACK_PORT)
            .asQueued()
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

}
