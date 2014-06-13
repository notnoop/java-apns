package com.notnoop.apns.integration;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.utils.ApnsServerStub;
import com.notnoop.apns.utils.junit.DumpThreadsOnErrorRule;
import com.notnoop.apns.utils.junit.Repeat;
import com.notnoop.apns.utils.junit.RepeatRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static com.notnoop.apns.utils.FixedCertificates.*;
import static org.junit.Assert.*;


@SuppressWarnings("ALL")
public class ApnsConnectionTest {

    @Rule
    public TestName testName = new TestName();

    @Rule
    public RepeatRule rr = new RepeatRule();

    @Rule
    public DumpThreadsOnErrorRule dumpRule = new DumpThreadsOnErrorRule();

    ApnsServerStub server;
    static SimpleApnsNotification msg1 = new SimpleApnsNotification("a87d8878d878a79", "{\"aps\":{}}");
    static SimpleApnsNotification msg2 = new SimpleApnsNotification("a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg1 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg2 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    static EnhancedApnsNotification eMsg3 = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID(),
            1, "a87d8878d878a88", "{\"aps\":{}}");
    private int gatewayPort;

    @Before
    public void startup() {
        System.out.println("****** "+testName.getMethodName());
        server = ApnsServerStub.prepareAndStartServer();
        gatewayPort = server.getEffectiveGatewayPort();
    }

    @After
    public void tearDown() {
        server.stop();
        server = null;
    }

    @Repeat(count = 50)
    @Test(timeout = 2000)
    public void sendOneSimple() throws InterruptedException {
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, gatewayPort)
                .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.getMessages().acquire();

        assertArrayEquals(msg1.marshall(), server.getReceived().toByteArray());
    }

    @Repeat(count = 50)
    @Test(timeout = 2000)
    public void sendOneQueued() throws InterruptedException {
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, gatewayPort)
                .asQueued()
                .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.getMessages().acquire();

        assertArrayEquals(msg1.marshall(), server.getReceived().toByteArray());
    }


    @Test
    public void sendOneSimpleWithoutTimeout() throws InterruptedException {
        server.getToWaitBeforeSend().set(2000);
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, gatewayPort)
                .withReadTimeout(5000)
                .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.getMessages().acquire();

        assertArrayEquals(msg1.marshall(), server.getReceived().toByteArray());
    }
    
    /**
     * Unlike in the feedback case, push messages won't expose the socket timeout,
     * as the read is done in a separate monitoring thread.
     * 
     * Therefore, normal behavior is expected in this test.
     * 
     * @throws InterruptedException
     */
    @Test
    public void sendOneSimpleWithTimeout() throws InterruptedException {
        server.getToWaitBeforeSend().set(5000);
        ApnsService service =
                APNS.newService().withSSLContext(clientContext())
                .withGatewayDestination(LOCALHOST, gatewayPort)
                .withReadTimeout(1000)
                .build();
        server.stopAt(msg1.length());
        service.push(msg1);
        server.getMessages().acquire();

        assertArrayEquals(msg1.marshall(), server.getReceived().toByteArray());
    }

}
