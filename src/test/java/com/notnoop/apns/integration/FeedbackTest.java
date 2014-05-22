package com.notnoop.apns.integration;

import java.io.IOException;
import java.net.SocketTimeoutException;
import javax.net.ssl.SSLContext;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.utils.ApnsServerStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static com.notnoop.apns.internal.ApnsFeedbackParsingUtils.*;
import static com.notnoop.apns.utils.FixedCertificates.*;
import static org.junit.Assert.*;

public class FeedbackTest {

    ApnsServerStub server;
    SSLContext clientContext = clientContext();


    @Before
    public void startup() {
        server = ApnsServerStub.prepareAndStartServer();
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Test
    public void simpleFeedback() throws IOException {
        server.getToSend().write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }
    
    @Test
    public void simpleFeedbackWithoutTimeout() throws IOException {
        server.getToSend().write(simple);
        server.getToWaitBeforeSend().set(2000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .withReadTimeout(3000)
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test()
    public void simpleFeedbackWithTimeout() throws IOException {
        server.getToSend().write(simple);
        server.getToWaitBeforeSend().set(5000);
        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
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
        server.getToSend().write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

    @Test
    public void simpleQueuedFeedback() throws IOException {
        server.getToSend().write(simple);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .asQueued()
            .build();

        checkParsedSimple(service.getInactiveDevices());
    }

    @Test
    public void threeQueuedFeedback() throws IOException {
        server.getToSend().write(three);

        ApnsService service =
            APNS.newService().withSSLContext(clientContext)
            .withGatewayDestination(LOCALHOST, server.getEffectiveGatewayPort())
            .withFeedbackDestination(LOCALHOST, server.getEffectiveFeedbackPort())
            .asQueued()
            .build();

        checkParsedThree(service.getInactiveDevices());
    }

}
